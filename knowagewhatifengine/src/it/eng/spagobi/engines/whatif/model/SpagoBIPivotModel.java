/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 */

package it.eng.spagobi.engines.whatif.model;

import it.eng.spagobi.engines.whatif.calculatedmember.CalculatedMember;
import it.eng.spagobi.engines.whatif.calculatedmember.CalculatedMemberManager;
import it.eng.spagobi.engines.whatif.crossnavigation.SpagoBICrossNavigationConfig;
import it.eng.spagobi.engines.whatif.crossnavigation.TargetClickable;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.exception.WhatIfPersistingTransformationException;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformation;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformationsAnalyzer;
import it.eng.spagobi.engines.whatif.model.transform.CellTransformationsStack;
import it.eng.spagobi.engines.whatif.model.transform.algorithm.IAllocationAlgorithm;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.impl.PivotModelImpl;
import com.eyeq.pivot4j.transform.ChangeSlicer;

public class SpagoBIPivotModel extends PivotModelImpl {

	public static transient Logger logger = Logger.getLogger(SpagoBIPivotModel.class);
	private final CellTransformationsStack pendingTransformations = new CellTransformationsStack();
	private SpagoBICellSetWrapper wrapper = null;
	private List<CalculatedMember> calculatedFields;
	private String queryWithOutCC;
	private SpagoBICrossNavigationConfig crossNavigation;
	private List<TargetClickable> targetsClickable;

	@Override
	public synchronized CellSet getCellSet() {
		// get cellset from super class (Mondrian)
		CellSet cellSet = super.getCellSet();

		// since the getCellSet method is invoked many times, we get the
		// previous cell set and compare the new one
		SpagoBICellSetWrapper previous = this.getCellSetWrapper();
		if (previous != null && previous.unwrap() == cellSet) { // TODO check if
																// this
																// comparison is
																// 100% valid
			return previous;
		}

		// wrap the cellset
		SpagoBICellSetWrapper wrapper = new SpagoBICellSetWrapper(cellSet, this);
		// apply pending transformations
		wrapper.restorePendingTransformations(pendingTransformations);
		// store cell set wrapper
		this.setCellSetWrapper(wrapper);

		return wrapper;
	}

	public void applyCal() {
		queryWithOutCC = getCurrentMdx();
		try {
			String queryString = CalculatedMemberManager.injectCalculatedFieldsIntoMdxQuery(this);
			setMdx(queryString);
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Error calculating the field", e);
		}
	}

	/**
	 * Restores the query without calculated fields
	 */
	public void restoreQuery() {
		try {
			setMdx(queryWithOutCC);
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Error calculating the field", e);
		}

	}

	public SpagoBIPivotModel(OlapDataSource dataSource) {
		super(dataSource);
		this.calculatedFields = new ArrayList<CalculatedMember>();
	}

	public SpagoBICellSetWrapper getCellSetWrapper() {
		return wrapper;
	}

	public void setCellSetWrapper(SpagoBICellSetWrapper wrapper) {
		this.wrapper = wrapper;
	}

	public void setValue(Object newValue, Cell cell, IAllocationAlgorithm algorithm) {
		// store the transformation into the stack
		SpagoBICellSetWrapper cellSetWrapper = this.getCellSetWrapper();
		SpagoBICellWrapper cellWrapper = SpagoBICellWrapper.wrap(cell, cellSetWrapper);
		CellTransformation transformation = new CellTransformation(newValue, cellWrapper.getValue(), cellWrapper, algorithm);
		pendingTransformations.add(transformation);
	}

	public boolean hasPendingTransformations() {
		return pendingTransformations.size() > 0;
	}

	public void addPendingTransformation(CellTransformation transformation) {
		pendingTransformations.add(transformation);
	}

	public final CellTransformationsStack getPendingTransformations() {
		return pendingTransformations;
	}

	public void persistTransformations(Connection connection) throws WhatIfPersistingTransformationException {
		persistTransformations(connection, null);
	}

	/**
	 * Persist the modifications in the selected version
	 *
	 * @param version
	 *            the version of the model in witch persist the modification. In null persist in the version selected in the Version dimension
	 * @throws WhatIfPersistingTransformationException
	 */
	public void persistTransformations(Connection connection, Integer version) throws WhatIfPersistingTransformationException {
		CellTransformationsAnalyzer analyzer = new CellTransformationsAnalyzer();
		CellTransformationsStack bestStack = analyzer.getShortestTransformationsStack(pendingTransformations);
		Iterator<CellTransformation> iterator = bestStack.iterator();

		while (iterator.hasNext()) {
			CellTransformation transformation = iterator.next();
			try {
				IAllocationAlgorithm algorithm = transformation.getAlgorithm();
				algorithm.persist(transformation.getCell(), transformation.getOldValue(), transformation.getNewValue(), connection, version);
			} catch (Exception e) {
				logger.error("Error persisting the transformation " + transformation, e);
				throw new WhatIfPersistingTransformationException(getLocale(), bestStack, e);
			}
		}

		// everithing goes right so we can clean the pending transformations
		pendingTransformations.clear();
	}

	/**
	 * Undo last modification
	 */
	public synchronized void undo() {
		if (!this.hasPendingTransformations()) {
			throw new SpagoBIEngineRuntimeException("There are no modifications to undo!!");
		}
		pendingTransformations.remove(pendingTransformations.size() - 1);
		// remove previous stored cell set, in any
		this.setCellSetWrapper(null);
		// force recalculation
		this.getCellSet();
	}

	/**
	 * @see com.eyeq.pivot4j.PivotModel#refresh()
	 */
	@Override
	public void refresh() {
		super.refresh();
		this.setCellSetWrapper(null);
	}

	public Integer getActualVersionSlicer(ModelConfig modelConfig) {
		logger.debug("IN");

		// get cube
		Cube cube = getCube();

		Hierarchy hierarchy = CubeUtilities.getVersionHierarchy(cube, modelConfig);

		ChangeSlicer ph = getTransform(ChangeSlicer.class);
		List<Member> slicers = ph.getSlicer(hierarchy);

		if (slicers == null || slicers.size() == 0) {
			logger.error("No version slicer deifined in the mdx query");
			throw new SpagoBIEngineRestServiceRuntimeException("versionresource.getactualversion.no.slicer.error", getLocale(), "No version in the mdx query");
		}

		String slicerValue = slicers.get(0).getName();

		if (slicerValue == null) {
			logger.error("No version slicer deifined in the mdx query");
			throw new SpagoBIEngineRestServiceRuntimeException("versionresource.getactualversion.no.slicer.error", getLocale(), "No version in the mdx query");
		}

		logger.debug("The actual version is " + slicerValue);
		logger.debug("OUT");
		return new Integer(slicerValue);
	}

	public List<CalculatedMember> getCalculatedFields() {
		return calculatedFields;
	}

	public void setCalculatedFields(List<CalculatedMember> calculatedFields) {
		this.calculatedFields = calculatedFields;
	}

	public void addCalculatedField(CalculatedMember calculatedField) {
		this.calculatedFields.add(calculatedField);
	}

	public OlapConnection getOlapConnection() {
		return getConnection();
	}

	public SpagoBICrossNavigationConfig getCrossNavigation() {
		return crossNavigation;
	}

	public void setCrossNavigation(SpagoBICrossNavigationConfig crossNavigation) {
		this.crossNavigation = crossNavigation;
	}

	public List<TargetClickable> getTargetsClickable() {
		return targetsClickable;
	}

	public void setTargetsClickable(List<TargetClickable> targetsClickable) {
		this.targetsClickable = targetsClickable;
	}

}
