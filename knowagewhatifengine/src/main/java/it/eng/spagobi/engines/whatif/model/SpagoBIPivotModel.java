/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.engines.whatif.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.Axis.Standard;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.pivot4j.impl.PivotModelImpl;
import org.pivot4j.impl.Quax;
import org.pivot4j.mdx.Exp;
import org.pivot4j.mdx.FunCall;
import org.pivot4j.mdx.Literal;
import org.pivot4j.mdx.MdxStatement;
import org.pivot4j.mdx.QueryAxis;
import org.pivot4j.mdx.Syntax;
import org.pivot4j.sort.SortCriteria;
import org.pivot4j.sort.SortMode;
import org.pivot4j.transform.ChangeSlicer;
import org.pivot4j.transform.NonEmpty;
import org.pivot4j.transform.SwapAxes;
import org.pivot4j.util.OlapUtils;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

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
import it.eng.spagobi.engines.whatif.template.Formula;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;

public class SpagoBIPivotModel extends PivotModelImpl {

	public static transient Logger logger = Logger.getLogger(SpagoBIPivotModel.class);
	private final CellTransformationsStack pendingTransformations = new CellTransformationsStack();
	private SpagoBICellSetWrapper wrapper = null;
	private List<CalculatedMember> calculatedFields;
	private String queryWithOutCC;

	private SpagoBICrossNavigationConfig crossNavigation;
	private List<TargetClickable> targetsClickable;
	private List<Member> sortPosMembers1;
	public static final String DIVISION_SIGN = new String("spagobi.operator.division");

	public List<Member> getSortPosMembers1() {
		sortPosMembers1 = new ArrayList<Member>();
		if (isSorting() && getSortPosMembers() != null) {
			List<Member> list = getSortPosMembers();
			for (Member member : list)
				sortPosMembers1.add(member);
		}

		return sortPosMembers1;
	}

	/*
	 * public void addCalucatedMembers(boolean add) {
	 *
	 * if (add) { QueryAxis qaRows = getQueryAxis(Axis.ROWS); QueryAxis qaColumns = getQueryAxis(Axis.COLUMNS);
	 *
	 * Exp rowsExp = qaRows.getExp();
	 *
	 * List<Exp> rowsArgs = new ArrayList<Exp>(1); rowsArgs.add(rowsExp);
	 *
	 * FunCall rowsCC = new FunCall("AddCalculatedMembers", Syntax.Function, rowsArgs); qaRows.setExp(rowsCC);
	 *
	 * Exp columnsExp = qaColumns.getExp();
	 *
	 * List<Exp> columsArgs = new ArrayList<Exp>(1); columsArgs.add(columnsExp);
	 *
	 * FunCall columnsCC = new FunCall("AddCalculatedMembers", Syntax.Function, columsArgs); qaColumns.setExp(columnsCC); }
	 *
	 * fireModelChanged();
	 *
	 * }
	 */
	@Override
	public boolean isSorting(Position position) {
		if (position != null) {
			if (sortPosMembers1 == null) {
				return false;
			}
			if (sortPosMembers1.size() != position.getMembers().size()) {
				return false;
			}

			for (int i = 0; i < sortPosMembers1.size(); i++) {
				Member member1 = sortPosMembers1.get(i);
				Member member2 = position.getMembers().get(i);
				// any null does not compare
				if (member1 == null) {
					return false;
				} else if (!OlapUtils.equals(member1, member2)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}

	};

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
		Monitor totalTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel.total");

		Monitor getQuery = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel.getQuery");
		queryWithOutCC = getCurrentMdx();
		getQuery.stop();

		try {
			Monitor injectCalculatedFieldsIntoMdxQueryTime = MonitorFactory
					.start("WhatIfEngine/it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel.injectCalculatedFieldsIntoMdxQueryTime");
			String queryString = CalculatedMemberManager.injectCalculatedFieldsIntoMdxQuery(this);
			injectCalculatedFieldsIntoMdxQueryTime.stop();

			Monitor setMdxTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel.setMdxTime");
			setMdx(queryString);
			setMdxTime.stop();
			Monitor getCellSetTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel.getCellSetTime");
			this.getCellSet();

			getCellSetTime.stop();

			// System.out.println("rows "+ cs.getAxes().get(0).getPositions().size());
			// System.out.println("columns "+cs.getAxes().get(1).getPositions().size());

		} catch (Exception e) {

			this.getCalculatedFields().remove(this.getCalculatedFields().size() - 1);
			restoreQuery();

			throw new SpagoBIEngineRuntimeException("Error calculating the field", e);
		}
		totalTime.stop();
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
	 * @param version the version of the model in witch persist the modification. In null persist in the version selected in the Version dimension
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
		this.removeCalculatedField(calculatedField.getCalculateFieldName());
		this.calculatedFields.add(calculatedField);
	}

	public void addCalculatedField(String calculatedFieldName, String calculatedFieldFormula, String parentMemberUniqueName, Formula formula) {
		Axis axis;
		String calculateFieldFormulaParsed = new String();
		try {
			if (!calculatedFieldFormula.isEmpty()) {
				calculateFieldFormulaParsed = calculatedFieldFormula.replaceAll("\\{" + DIVISION_SIGN + "\\}", "/");
			}

		} catch (Exception e) {
			logger.error("Error parsing the formula. The original formula is " + calculatedFieldFormula, e);
		}
		try {

			Member parentMember = CubeUtilities.getMember(this.getCube(), parentMemberUniqueName);

			logger.debug("Adding the calculated fields into the model");
			CalculatedMember cc = new CalculatedMember(calculatedFieldName, calculateFieldFormulaParsed, parentMember, formula);
			this.addCalculatedField(cc);

		} catch (OlapException e) {
			logger.error("Error getting the parent of the calculated field. The unique name of the parent is " + parentMemberUniqueName, e);
			throw new SpagoBIEngineRestServiceRuntimeException("sbi.olap.celculated.definition.error", getLocale(),
					"Error getting the parent of the calculated field. The unique name of the parent is " + parentMemberUniqueName, e);
		}
	}

	public boolean removeCalculatedField(String calculatedFieldName) {

		for (int i = 0; i < this.calculatedFields.size(); i++) {
			if (calculatedFieldName.equals(this.calculatedFields.get(i).getCalculateFieldName())) {
				this.calculatedFields.remove(i);
				return true;
			}
		}
		return false;
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

	/**
	 * @return the queryWithOutCC
	 */
	public String getQueryWithOutCC() {
		return queryWithOutCC;
	}

	public void setNonEmpty(boolean suppressEmpty) {
		NonEmpty transform = this.getTransform(NonEmpty.class);
		transform.setNonEmpty(suppressEmpty);
	}

	public void setSubset(Integer startRow, Integer startColumn, Integer rowSet, Integer columnSet) {

		QueryAxis qaRows = getQueryAxis(Axis.ROWS);
		QueryAxis qaColumns = getQueryAxis(Axis.COLUMNS);

		if (!isSubset(qaRows)) {

			Exp setForAx = qaRows.getExp();
			// axis.getPositionCount();

			List<Exp> args = new ArrayList<Exp>(3);

			args.add(setForAx);
			args.add(Literal.create(startRow));
			args.add(Literal.create(rowSet));
			FunCall subset = new FunCall("Subset", Syntax.Function, args);
			qaRows.setExp(subset);

		} else {

		}
		if (!isSubset(qaColumns)) {

			Exp setForAx = qaColumns.getExp();
			// axis.getPositionCount();

			List<Exp> args = new ArrayList<Exp>(3);

			args.add(setForAx);
			args.add(Literal.create(startColumn));
			args.add(Literal.create(columnSet));
			FunCall subset = new FunCall("Subset", Syntax.Function, args);
			qaColumns.setExp(subset);

		} else {

		}

		fireModelChanged();
	}

	public void removeSubset() {

		QueryAxis qaRows = null;
		QueryAxis qaColumns = null;
		FunCall subsetRows = null;
		FunCall subsetColumns = null;

		qaRows = getQueryAxis(Axis.ROWS);
		if (qaRows != null) {
			subsetRows = getSubSetFunction(qaRows);
			if (subsetRows != null && subsetRows.getFunction().equalsIgnoreCase("Subset")) {
				Exp exp = subsetRows.getArgs().get(0);
				qaRows.setExp(exp);
			}
		}

		qaColumns = getQueryAxis(Axis.COLUMNS);
		if (qaColumns != null) {
			subsetColumns = getSubSetFunction(qaColumns);
			if (subsetColumns != null && subsetColumns.getFunction().equalsIgnoreCase("Subset")) {
				Exp exp = subsetColumns.getArgs().get(0);
				qaColumns.setExp(exp);
			}
		}

		fireModelChanged();

	}

	public void removeOrder(Standard axis) {
		QueryAxis qa = getQueryAxis(axis);
		FunCall f = getSubSetFunction(qa);
		if (f.getFunction().equalsIgnoreCase("Order")) {
			Exp exp = f.getArgs().get(0);
			qa.setExp(exp);
		}
		if (f.getFunction().equalsIgnoreCase("TopCount")) {
			Exp exp = f.getArgs().get(0);
			qa.setExp(exp);
		}
		if (f.getFunction().equalsIgnoreCase("BottomCount")) {
			Exp exp = f.getArgs().get(0);
			qa.setExp(exp);
		}

		fireModelChanged();

	}

	private QueryAxis getQueryAxis(Standard axis) {
		CellSetAxis cellSetAxis = this.getCellSet().getAxes().get(axis.axisOrdinal());
		QueryAxis qa = null;
		MdxStatement pq = null;
		Quax quax = null;

		pq = getQueryAdapter().getParsedQuery();
		quax = getQueryAdapter().getQuax(cellSetAxis.getAxisOrdinal());

		if (pq != null && quax != null) {
			qa = pq.getAxis(Axis.Factory.forOrdinal(quax.getOrdinal()));
		}

		return qa;
	}

	private boolean isSubset(QueryAxis qa) {
		if (qa.getExp() instanceof FunCall) {
			FunCall f = (FunCall) qa.getExp();
			if (f.getFunction().equalsIgnoreCase("Subset")) {
				return true;
			}
		}

		return false;
	}

	private FunCall getSubSetFunction(QueryAxis qa) {
		if (qa.getExp() instanceof FunCall) {
			FunCall f = (FunCall) qa.getExp();
			f.getFunction();
			return f;
		}
		return null;

	}

	public Integer getSubsetStart(Standard axis) {
		QueryAxis qa = getQueryAxis(axis);
		if (isSubset(qa)) {
			FunCall f = getSubSetFunction(qa);
			Double d = (Double.parseDouble(f.getArgs().get(1).toString()));
			return d.intValue();
		}

		return 0;
	}

	public void swapAxisSort() {
		CellSetAxis rows = getCellSet().getAxes().get(Axis.ROWS.axisOrdinal());
		CellSetAxis columns = getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal());
		this.setSorting(true);
		SwapAxes transform = getTransform(SwapAxes.class);

		if (transform.isSwapAxes()) {
			for (Position positionOnRows : rows.getPositions()) {
				if (isSorting(positionOnRows) & this.getSortCriteria() != null) {
					sort(rows, positionOnRows);
				}

			}
			for (Position positionOnColunms : columns.getPositions()) {
				if (isSorting(positionOnColunms) & this.getSortCriteria() != null) {
					sort(columns, positionOnColunms);
				}
			}
		} else {
			for (Position positionOnRows : rows.getPositions()) {
				if (isSorting(positionOnRows) & this.getSortCriteria() != null) {
					sort(columns, positionOnRows);
				}

			}
			for (Position positionOnColunms : columns.getPositions()) {
				if (isSorting(positionOnColunms) & this.getSortCriteria() != null) {
					sort(rows, positionOnColunms);
				}
			}

		}
		fireModelChanged();
	}

	public void sortModel(Integer axisToSortpos, Integer axis, String positionUniqueName, String sortMode) {

		CellSetAxis axisToSort = null;
		CellSetAxis axisM = null;
		this.setSorting(true);

		this.removeSubset();
		this.removeOrder(Axis.ROWS);
		this.removeOrder(Axis.COLUMNS);
		axisToSort = this.getCellSet().getAxes().get(axisToSortpos);
		axisM = this.getCellSet().getAxes().get(axis);

		List<Position> positions = axisM.getPositions();

		Position position = CubeUtilities.getPosition(positions, positionUniqueName);

		if (position != null) {
			if (!this.isSorting(position)) {
				while (this.getSortCriteria() != null) {
					SortCriteria nextSortCriteria = SortMode.fromName(sortMode).nextMode(this.getSortCriteria());
					this.setSortCriteria(nextSortCriteria);
				}
				SortCriteria nextSortCriteria = SortMode.fromName(sortMode).nextMode(this.getSortCriteria());
				this.setSortCriteria(nextSortCriteria);
			}
			if (this.getSortCriteria() != null) {

				this.sort(axisToSort, position);
				this.getSortPosMembers1();

			}
		}
		this.swapAxisSort();
		fireModelChanged();

	}

	public OlapConnection getSbiConnection() {
		return getConnection();
	}

}
