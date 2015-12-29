/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.whatif.api;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.calculatedmember.CalculatedMember;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.dimension.SbiDimension;
import it.eng.spagobi.engines.whatif.hierarchy.SbiHierarchy;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotModel;

@Path("/1.0/calculatedmembers")
public class CalculatedMembersResource extends AbstractWhatIfEngineService {
	public static transient Logger logger = Logger.getLogger(CalculatedMembersResource.class);
	public static final String DIVISION_SIGN = new String("spagobi.operator.division");

	/**
	 * Service to get Dimensions
	 * 
	 * @return the dimensions
	 */
	@GET
	@Path("/initializeData")
	@Produces("text/html; charset=UTF-8")
	public String initializeData() {
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();
		List<SbiDimension> dimensions = new ArrayList<SbiDimension>();
		String serializedNames = new String();
		try {
			dimensions = getDimensions(model, ei.getModelConfig());
			serializedNames = serialize(dimensions);
		} catch (Exception e) {
			logger.error("Error serializing dimensions");
			throw new SpagoBIEngineRestServiceRuntimeException(getLocale(), e);
		}
		logger.debug("OUT");
		return serializedNames;
	}

	/**
	 * Service to get the dimensions
	 * 
	 * @return The SbiDimension List
	 */
	public List<SbiDimension> getDimensions(PivotModel model, ModelConfig modelConfig) throws SpagoBIEngineException {
		logger.debug("IN");
		CellSet cellSet = model.getCellSet();
		List<CellSetAxis> axis = cellSet.getAxes();
		List<Dimension> otherHDimensions;
		List<SbiDimension> dimensions = new ArrayList<SbiDimension>();
		try {
			List<Hierarchy> axisHierarchies = axis.get(0).getAxisMetaData().getHierarchies();
			axisHierarchies.addAll(axis.get(1).getAxisMetaData().getHierarchies());
			otherHDimensions = CubeUtilities.getDimensions(model.getCube().getHierarchies());
			for (int i = 0; i < otherHDimensions.size(); i++) {
				Dimension aDimension = otherHDimensions.get(i);
				SbiDimension myDimension = new SbiDimension(aDimension, -1, i);
				List<Hierarchy> dimensionHierarchies = aDimension.getHierarchies();
				String selectedHierarchyName = modelConfig.getDimensionHierarchyMap().get(myDimension.getUniqueName());
				if (selectedHierarchyName == null) {
					selectedHierarchyName = aDimension.getDefaultHierarchy().getUniqueName();
				}
				myDimension.setSelectedHierarchyUniqueName(selectedHierarchyName);
				for (int j = 0; j < dimensionHierarchies.size(); j++) {
					Hierarchy hierarchy = dimensionHierarchies.get(j);
					SbiHierarchy hierarchyObject = new SbiHierarchy(hierarchy, i);
					myDimension.getHierarchies().add(hierarchyObject);
					// set the position of the selected hierarchy
					if (selectedHierarchyName.equals(hierarchy.getUniqueName())) {
						myDimension.setSelectedHierarchyPosition(j);
					}
				}
				dimensions.add(myDimension);
			}
		} catch (Exception e) {
			logger.error("Error getting dimensions", e);
			throw new SpagoBIEngineException("Error getting dimensions", e);
		}
		logger.debug("OUT");
		return dimensions;
	}

	/**
	 * Service to create the calculated member
	 * 
	 * @return the rendered pivot table
	 */
	@POST
	@Path("/execute/{calculateFieldName}/{calculateFieldFormula}/{parentMemberUniqueName}/{axisOrdinal}")
	@Produces("text/html; charset=UTF-8")
	public String execute(
			@PathParam("calculateFieldName") String calculateFieldName,
			@PathParam("calculateFieldFormula") String calculateFieldFormula,
			@PathParam("parentMemberUniqueName") String parentMemberUniqueName,
			@PathParam("axisOrdinal") int axisOrdinal) {
		logger.debug("IN");
		Member parentMember;
		logger.debug("expression= " + calculateFieldFormula);
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		Axis axis;
		String calculateFieldFormulaParsed = new String();
		try {
			if (!calculateFieldFormula.isEmpty()) {
				calculateFieldFormulaParsed = calculateFieldFormula.replaceAll("\\{" + DIVISION_SIGN + "\\}", "/");
			}

		} catch (Exception e) {
			logger.error("Error parsing the formula. The original formula is " + calculateFieldFormula, e);
		}
		try {
			parentMember = CubeUtilities.getMember(ei.getPivotModel().getCube(), parentMemberUniqueName);
			axis = CubeUtilities.getAxis(axisOrdinal);
		} catch (OlapException e) {
			logger.error("Error getting the parent of the calculated field. The unique name of the parent is " + parentMemberUniqueName, e);
			throw new SpagoBIEngineRestServiceRuntimeException("sbi.olap.celculated.definition.error", getLocale(),
					"Error getting the parent of the calculated field. The unique name of the parent is " + parentMemberUniqueName, e);
		}

		logger.debug("Adding the calculated fields into the model");
		CalculatedMember cc = new CalculatedMember(calculateFieldName, calculateFieldFormulaParsed, parentMember, axis);
		ei.getSpagoBIPivotModel().addCalculatedField(cc);

		String table = renderModel(ei.getPivotModel());
		logger.debug("OUT");
		return table;
	}

}
