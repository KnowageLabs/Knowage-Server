/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 *
 * Renderer of the model...
 * It uses the WhatIfHTMLRenderer to render the table in a HTML format
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */
package it.eng.spagobi.engines.whatif.model;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.dimension.SbiDimension;
import it.eng.spagobi.engines.whatif.hierarchy.SbiHierarchy;
import it.eng.spagobi.engines.whatif.version.VersionManager;
import it.eng.spagobi.pivot4j.ui.WhatIfHTMLRenderer;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.jdbc.util.BasicFormatterImpl;
import org.json.JSONException;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotModel;
import com.eyeq.pivot4j.query.QueryAdapter;
import com.eyeq.pivot4j.transform.ChangeSlicer;
import com.eyeq.pivot4j.transform.NonEmpty;
import com.eyeq.pivot4j.transform.impl.ChangeSlicerImpl;
import com.eyeq.pivot4j.ui.command.DrillCollapseMemberCommand;
import com.eyeq.pivot4j.ui.command.DrillCollapsePositionCommand;
import com.eyeq.pivot4j.ui.command.DrillDownCommand;
import com.eyeq.pivot4j.ui.command.DrillDownReplaceCommand;
import com.eyeq.pivot4j.ui.command.DrillExpandMemberCommand;
import com.eyeq.pivot4j.ui.command.DrillExpandPositionCommand;
import com.eyeq.pivot4j.ui.command.DrillUpReplaceCommand;
import com.eyeq.pivot4j.ui.impl.NonInternalPropertyCollector;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PivotJsonHTMLSerializer extends JsonSerializer<PivotModel> {

	public static transient Logger logger = Logger.getLogger(PivotJsonHTMLSerializer.class);

	private static final int FILTERS_AXIS_POS = -1;
	private static final String NAME = "name";
	private static final String UNIQUE_NAME = "uniqueName";
	private static final String COLUMNS = "columns";
	private static final String ROWS = "rows";
	private static final String FILTERS = "filters";
	private static final String TABLE = "table";
	private static final String ROWSAXISORDINAL = "rowsAxisOrdinal";
	private static final String COLUMNSAXISORDINAL = "columnsAxisOrdinal";
	private static final String MDXFORMATTED = "mdxFormatted";
	private static final String MODELCONFIG = "modelConfig";
	private static final String HAS_PENDING_TRANSFORMATIONS = "hasPendingTransformations";

	private final OlapConnection connection;
	private final ModelConfig modelConfig;

	public PivotJsonHTMLSerializer(OlapConnection connection, ModelConfig modelConfig) {
		this.connection = connection;
		this.modelConfig = modelConfig;
	}

	@Override
	public void serialize(PivotModel value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

		logger.debug("IN");
		String table = "";

		logger.debug("Creating the renderer");
		StringWriter writer = new StringWriter();
		WhatIfHTMLRenderer renderer = new WhatIfHTMLRenderer(writer, modelConfig);

		logger.debug("Setting the properties of the renderer");

		renderer.setShowParentMembers(false); // Optionally make the parent
												// members visible.
		renderer.setShowDimensionTitle(false); // Optionally hide the dimension
												// title headers.

		renderer.setCellSpacing(0);
		renderer.setRowHeaderStyleClass("x-column-header-inner x-column-header x-column-header-align-left x-box-item x-column-header-default x-unselectable x-grid-header-ct x-docked x-grid-header-ct-default x-docked-top x-grid-header-ct-docked-top x-grid-header-ct-default-docked-top x-box-layout-ct x-docked-noborder-top x-docked-noborder-right x-docked-noborder-left x-pivot-header");
		renderer.setColumnHeaderStyleClass("x-column-header-inner x-column-header x-column-header-align-left x-box-item x-column-header-default x-unselectable x-grid-header-ct x-docked x-grid-header-ct-default x-docked-top x-grid-header-ct-docked-top x-grid-header-ct-default-docked-top x-box-layout-ct x-docked-noborder-top x-docked-noborder-right x-docked-noborder-left x-pivot-header");
		renderer.setCornerStyleClass("x-column-header-inner x-column-header x-column-header-align-left x-box-item x-column-header-default x-unselectable x-grid-header-ct x-docked x-grid-header-ct-default x-docked-top x-grid-header-ct-docked-top x-grid-header-ct-default-docked-top x-box-layout-ct x-docked-noborder-top x-docked-noborder-right x-docked-noborder-left x-pivot-header");
		renderer.setCellStyleClass("x-grid-cell x-grid-td x-grid-cell-gridcolumn-1014 x-unselectable x-grid-cell-inner  x-grid-row-alt x-grid-data-row x-grid-with-col-lines x-grid-cell x-pivot-cell");
		renderer.setTableStyleClass("x-panel-body x-grid-body x-panel-body-default x-box-layout-ct x-panel-body-default x-pivot-table");
		renderer.setRowStyleClass(" generic-row-style ");

		renderer.setEvenRowStyleClass(" even-row ");
		renderer.setOddRowStyleClass(" odd-row ");

		renderer.setEvenColumnStyleClass(" even-column ");
		renderer.setOddColumnStyleClass(" odd-column ");

		String drillDownModeValue = modelConfig.getDrillType();

		renderer.removeCommand(new DrillUpReplaceCommand(renderer).getName());
		renderer.removeCommand(new DrillDownReplaceCommand(renderer).getName());
		renderer.removeCommand(new DrillExpandMemberCommand(renderer).getName());
		renderer.removeCommand(new DrillCollapseMemberCommand(renderer).getName());
		renderer.removeCommand(new DrillCollapsePositionCommand(renderer).getName());
		renderer.removeCommand(new DrillExpandPositionCommand(renderer).getName());

		if (drillDownModeValue.equals(DrillDownCommand.MODE_POSITION)) {
			renderer.addCommand(new DrillExpandPositionCommand(renderer));
			renderer.addCommand(new DrillCollapsePositionCommand(renderer));
		} else if (drillDownModeValue.equals(DrillDownCommand.MODE_MEMBER)) {
			renderer.addCommand(new DrillCollapseMemberCommand(renderer));
			renderer.addCommand(new DrillExpandMemberCommand(renderer));

		} else if (drillDownModeValue.equals(DrillDownCommand.MODE_REPLACE)) {
			renderer.addCommand(new DrillDownReplaceCommand(renderer));
			renderer.addCommand(new DrillUpReplaceCommand(renderer));
			/* NOT TO BE CHANGED ---> used for drill-up in replace mode */
			renderer.setShowDimensionTitle(true); // Optionally hide the
													// dimension title headers.
			/*--------------------------------------------------------*/
		}

		renderer.setDrillDownMode(drillDownModeValue);

		renderer.setEnableColumnDrillDown(true);
		renderer.setEnableRowDrillDown(true);
		renderer.setEnableSort(true);

		// /show parent members
		Boolean showParentMembers = modelConfig.getShowParentMembers();
		renderer.setShowParentMembers(showParentMembers);
		if (showParentMembers) {
			renderer.setShowDimensionTitle(true);
		}
		// /hide spans
		Boolean hideSpans = modelConfig.getHideSpans();
		renderer.setHideSpans(hideSpans);
		// /show properties
		Boolean showProperties = modelConfig.getShowProperties();

		if (showProperties) {
			renderer.setPropertyCollector(new NonInternalPropertyCollector());
		} else {
			renderer.setPropertyCollector(null);
		}

		// /suppress empty rows/columns
		Boolean suppressEmpty = modelConfig.getSuppressEmpty();
		NonEmpty transformNonEmpty = value.getTransform(NonEmpty.class);
		transformNonEmpty.setNonEmpty(suppressEmpty);

		// updates the actual version in the model config
		if (modelConfig.isWhatIfScenario()) {
			Integer actualVersion = VersionManager.getActualVersion(value, modelConfig);
			modelConfig.setActualVersion(actualVersion);
		}

		logger.debug("Rendering the model");
		renderer.render(value);

		try {
			writer.flush();
			writer.close();
			table = writer.getBuffer().toString();
		} catch (IOException e) {
			logger.error("Error serializing the table", e);
			throw new SpagoBIEngineRuntimeException("Error serializing the table", e);
		}

		CellSet cellSet = value.getCellSet();
		List<CellSetAxis> axis = cellSet.getAxes();

		try {

			List<Hierarchy> axisHierarchies = axis.get(0).getAxisMetaData().getHierarchies();
			axisHierarchies.addAll(axis.get(1).getAxisMetaData().getHierarchies());
			List<Dimension> axisDimensions = CubeUtilities.getDimensions(axisHierarchies);
			List<Dimension> otherHDimensions = CubeUtilities.getDimensions(value.getCube().getHierarchies());

			otherHDimensions.removeAll(axisDimensions);

			jgen.writeStartObject();
			jgen.writeStringField(TABLE, table);
			serializeAxis(ROWS, jgen, axis, Axis.ROWS);
			serializeAxis(COLUMNS, jgen, axis, Axis.COLUMNS);
			serializeDimensions(jgen, otherHDimensions, FILTERS_AXIS_POS, FILTERS, true, value);
			jgen.writeNumberField(COLUMNSAXISORDINAL, Axis.COLUMNS.axisOrdinal());
			jgen.writeNumberField(ROWSAXISORDINAL, Axis.ROWS.axisOrdinal());
			jgen.writeObjectField(MODELCONFIG, modelConfig);

			// build the query mdx
			String mdxQuery = formatQueryString(value.getCurrentMdx());
			jgen.writeStringField(MDXFORMATTED, mdxQuery);

			boolean hasPendingTransformations = value instanceof SpagoBIPivotModel && ((SpagoBIPivotModel) value).hasPendingTransformations();
			jgen.writeBooleanField(HAS_PENDING_TRANSFORMATIONS, hasPendingTransformations);

			jgen.writeEndObject();

		} catch (Exception e) {
			logger.error("Error serializing the pivot table", e);
			throw new SpagoBIRuntimeException("Error serializing the pivot table", e);
		}

		logger.debug("OUT");

	}

	private void serializeAxis(String field, JsonGenerator jgen, List<CellSetAxis> axis, Axis type) throws JSONException, JsonGenerationException, IOException {
		CellSetAxis aAxis = axis.get(0);
		int axisPos = 0;
		if (!aAxis.getAxisOrdinal().equals(type)) {
			aAxis = axis.get(1);
			axisPos = 1;
		}
		List<Hierarchy> hierarchies = aAxis.getAxisMetaData().getHierarchies();
		List<Dimension> dimensions = CubeUtilities.getDimensions(hierarchies);
		serializeDimensions(jgen, dimensions, axisPos, field, false, null);

	}

	private void serializeDimensions(JsonGenerator jgen, List<Dimension> dimensions, int axis, String field, boolean withSlicers, PivotModel model)
			throws JSONException, JsonGenerationException, IOException {

		QueryAdapter qa = null;
		ChangeSlicer ph = null;

		if (withSlicers) {
			qa = new QueryAdapter(model);
			qa.initialize();
			ph = new ChangeSlicerImpl(qa, connection);
		}

		jgen.writeArrayFieldStart(field);

		for (int i = 0; i < dimensions.size(); i++) {
			Dimension aDimension = dimensions.get(i);

			SbiDimension myDimension = new SbiDimension(aDimension, axis, i);
			List<Hierarchy> dimensionHierarchies = aDimension.getHierarchies();

			String selectedHierarchyName = modelConfig.getDimensionHierarchyMap().get(myDimension.getUniqueName());
			if (selectedHierarchyName == null) {
				selectedHierarchyName = aDimension.getDefaultHierarchy().getUniqueName();
			}

			myDimension.setSelectedHierarchyUniqueName(selectedHierarchyName);

			for (int j = 0; j < dimensionHierarchies.size(); j++) {
				Hierarchy hierarchy = dimensionHierarchies.get(j);
				SbiHierarchy hierarchyObject = new SbiHierarchy(hierarchy, i);

				if (withSlicers) {
					List<Member> slicers = ph.getSlicer(hierarchy);
					if (slicers != null && slicers.size() > 0) {
						List<Map<String, String>> slicerMap = new ArrayList<Map<String, String>>();
						for (int k = 0; k < slicers.size(); k++) {
							Map<String, String> slicer = new HashMap<String, String>();
							slicer.put(UNIQUE_NAME, slicers.get(k).getUniqueName());
							slicer.put(NAME, slicers.get(k).getCaption());
							slicerMap.add(slicer);
						}
						hierarchyObject.setSlicers(slicerMap);
					}
				}
				myDimension.getHierarchies().add(hierarchyObject);

				// set the position of the selected hierarchy
				if (selectedHierarchyName.equals(hierarchy.getUniqueName())) {
					myDimension.setSelectedHierarchyPosition(j);
				}
			}
			jgen.writeObject(myDimension);

		}

		jgen.writeEndArray();
	}

	//
	// private void serializeFilters(String field, JsonGenerator
	// jgen,List<Hierarchy> hierarchies, PivotModel model) throws JSONException,
	// JsonGenerationException, IOException{
	//
	// QueryAdapter qa = new QueryAdapter(model);
	// qa.initialize();
	//
	// ChangeSlicer ph = new ChangeSlicerImpl(qa, connection);
	//
	//
	// jgen.writeArrayFieldStart(field);
	// if(hierarchies!=null){
	// for (int i=0; i<hierarchies.size(); i++) {
	// Hierarchy hierarchy = hierarchies.get(i);
	// Map<String,Object> hierarchyObject = new HashMap<String,Object>();
	// hierarchyObject.put(NAME, hierarchy.getName());
	// hierarchyObject.put(UNIQUE_NAME, hierarchy.getUniqueName());
	// hierarchyObject.put(POSITION, ""+i);
	// hierarchyObject.put(AXIS, ""+FILTERS_AXIS_POS);
	//
	//
	// List<Member> slicers = ph.getSlicer(hierarchy);
	// if(slicers!= null && slicers.size()>0){
	// List<Map<String,String>> slicerMap = new ArrayList<Map<String,String>>();
	// for(int j=0; j<slicers.size(); j++){
	// Map<String,String> slicer = new HashMap<String,String>();
	// slicer.put(UNIQUE_NAME, slicers.get(j).getUniqueName());
	// slicer.put(NAME, slicers.get(j).getName());
	// slicerMap.add(slicer);
	// }
	// hierarchyObject.put(SLICERS, slicerMap);
	// }
	// jgen.writeObject(hierarchyObject);
	//
	// }
	// }
	// jgen.writeEndArray();
	// }

	public String formatQueryString(String queryString) {
		String formattedQuery;
		BasicFormatterImpl fromatter;

		if (queryString == null || queryString.equals("")) {
			logger.error("Impossible to get the query string because the query is null");
			return "";
		}

		fromatter = new BasicFormatterImpl();
		formattedQuery = fromatter.format(queryString);
		return StringUtilities.fromStringToHTML(formattedQuery);
	}

}
