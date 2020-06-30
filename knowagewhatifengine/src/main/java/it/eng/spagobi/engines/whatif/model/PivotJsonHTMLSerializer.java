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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotModel;
import org.pivot4j.impl.PivotModelImpl;
import org.pivot4j.impl.QueryAdapter;
import org.pivot4j.transform.ChangeSlicer;
import org.pivot4j.transform.impl.ChangeSlicerImpl;
import org.pivot4j.ui.collector.NonInternalPropertyCollector;
import org.pivot4j.ui.command.BasicDrillThroughCommand;
import org.pivot4j.ui.command.DrillCollapseMemberCommand;
import org.pivot4j.ui.command.DrillCollapsePositionCommand;
import org.pivot4j.ui.command.DrillDownCommand;
import org.pivot4j.ui.command.DrillDownReplaceCommand;
import org.pivot4j.ui.command.DrillExpandMemberCommand;
import org.pivot4j.ui.command.DrillExpandPositionCommand;
import org.pivot4j.ui.command.DrillUpReplaceCommand;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;
import it.eng.spagobi.engines.whatif.calculatedmember.CalculatedMember;
import it.eng.spagobi.engines.whatif.calculatedmember.MDXFormula;
import it.eng.spagobi.engines.whatif.calculatedmember.MDXFormulaHandler;
import it.eng.spagobi.engines.whatif.calculatedmember.MDXFormulas;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.dimension.SbiDimension;
import it.eng.spagobi.engines.whatif.hierarchy.SbiHierarchy;
import it.eng.spagobi.engines.whatif.version.VersionManager;
import it.eng.spagobi.pivot4j.ui.WhatIfHTMLRenderer;
import it.eng.spagobi.pivot4j.ui.html.WhatIfHTMLRendereCallback;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class PivotJsonHTMLSerializer extends JsonSerializer<PivotObjectForRendering> {

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
	public static final String ROW_OFFSET = "ROW_OFFSET";
	public static final String SUBSET_AXIS_LENGTH = "SUBSET_AXIS_LENGTH";
	public static final String COLUMN_OFFSET = "COLUMN_OFFSET";
	public static final String AXIS_LENGTH = "AXIS_LENGTH";
	private static final String FORMULAS = "formulas";
	private static final String CALCULATED_FIELDS = "CALCULATED_FIELDS";
	private static final int PAGES_COUNT = WhatIfEngineConfig.getInstance().getPivotTableLoadCount();
	private static final String MDXWITHOUTCF = "MDXWITHOUTCF";;

	public PivotJsonHTMLSerializer() {
	}

	@Override
	public void serialize(PivotObjectForRendering pivotobject, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

		Monitor totalTime = MonitorFactory
				.start("WhatIfEngine/it.eng.spagobi.engines.whatif.model.PivotJsonHTMLSerializer.serialize.increaseVersion.totalTime");

		PivotModel value = pivotobject.getModel();
		ModelConfig modelConfig = pivotobject.getConfig();
		OlapConnection connection = pivotobject.getConnection();

		logger.debug("IN");

		String table = "";

		logger.debug("Creating the renderer");
		StringWriter writer = new StringWriter();
		SpagoBIPivotModel model = (SpagoBIPivotModel) value;
		// model.setSubset(modelConfig.getStartRow(),
		// modelConfig.getStartColumn(), modelConfig.getRowsSet(),
		// modelConfig.getColumnSet());

		// WhatIfHTMLRenderer renderer = new WhatIfHTMLRenderer();
		WhatIfHTMLRenderer renderer = new WhatIfHTMLRenderer();
		WhatIfHTMLRendereCallback callback = new WhatIfHTMLRendereCallback(writer);

		logger.debug("Setting the properties of the renderer");

		renderer.setShowParentMembers(false); // Optionally make the parent
												// members visible.
		renderer.setShowDimensionTitle(false); // Optionally hide the dimension
												// title headers.

		// callback.setCellSpacing(0);

		callback.setRowHeaderStyleClass(null);// x-pivot-header
		callback.setColumnHeaderStyleClass(null);// x-pivot-header-column
		callback.setCornerStyleClass(null);// x-pivot-header x-pivot-corner
		callback.setCellStyleClass(null);// x-pivot-cell
											// x-pivot-header-column
		callback.setTableStyleClass("pivot-table");// x-pivot-table
		callback.setRowStyleClass(null);// generic-row-style

		callback.setEvenRowStyleClass(null);// even-row
		callback.setOddRowStyleClass(null);// odd-row

		// callback.setEvenColumnStyleClass(" even-column ");
		// callback.setOddColumnStyleClass(" odd-column ");

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
		renderer.addCommand(new BasicDrillThroughCommand(renderer));
		renderer.setEnableDrillThrough(modelConfig.getEnableDrillThrough());

		renderer.setDrillDownMode(drillDownModeValue);
		renderer.setEnableDrillDown(true);
		// renderer.setEnableColumnDrillDown(true);
		// renderer.setEnableRowDrillDown(true);

		renderer.setEnableSort(modelConfig.getSortingEnabled());

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
		/*
		 * NonEmpty transformNonEmpty = value.getTransform(NonEmpty.class); transformNonEmpty.setNonEmpty(suppressEmpty);
		 */

		model.setNonEmpty(suppressEmpty);
		/*******************************************************/
		if (modelConfig.getSortingEnabled()) {

			if (modelConfig.getSortingPositionUniqueName() != null) {
				model.sortModel(modelConfig.getAxisToSort(), modelConfig.getAxis(), modelConfig.getSortingPositionUniqueName(), modelConfig.getSortMode());
			}

		}

		jgen.writeStartObject();

		if (modelConfig.isPagination()) {
			doPagination(false, modelConfig, model, callback, writer, renderer, jgen, table);
		} else {
			int axisLength = model.getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal()).getPositionCount();

			writer.getBuffer().setLength(0);
			model.removeSubset();
			// model.setSubset(0, modelConfig.getStartColumn(), modelConfig.getRowsSet(), modelConfig.getColumnSet());
			callback.addProperty(COLUMN_OFFSET, 0);
			callback.addProperty(AXIS_LENGTH, axisLength);
			callback.addProperty(ROW_OFFSET, 0);
			callback.addProperty(SUBSET_AXIS_LENGTH, axisLength);
			Monitor renderTime = MonitorFactory
					.start("WhatIfEngine/it.eng.spagobi.engines.whatif.model.PivotJsonHTMLSerializer.serialize.increaseVersion.renderTime");
			renderer.render(model, callback);
			writer.flush();
			writer.close();
			table = writer.getBuffer().toString();
			jgen.writeStringField(TABLE, table);
			renderTime.stop();
		}

		/*******************************************************/

		// model.setSubset(modelConfig.getStartRow() + 1,
		// modelConfig.getStartColumn(), modelConfig.getRowsSet(),
		// modelConfig.getColumnSet());
		// renderer.render(model, callback);
		// modelConfig.setRowCount(rowCount);

		// updates the actual version in the model config
		if (modelConfig.isWhatIfScenario()) {
			Integer actualVersion = VersionManager.getActualVersion(value, modelConfig);
			modelConfig.setActualVersion(actualVersion);
		}
		// model.addCalucatedMembers(true);
		// List<String> tables = new ArrayList<String>();

		CellSet cellSet = value.getCellSet();

		List<CellSetAxis> axis = cellSet.getAxes();

		try {

			List<Hierarchy> axisHierarchies = axis.get(0).getAxisMetaData().getHierarchies();
			axisHierarchies.addAll(axis.get(1).getAxisMetaData().getHierarchies());
			List<Dimension> axisDimensions = CubeUtilities.getDimensions(axisHierarchies);
			List<Dimension> otherHDimensions = CubeUtilities.getDimensions(value.getCube().getHierarchies());

			otherHDimensions.removeAll(axisDimensions);

			// serializeTables("tables", jgen, tables);

			serializeAxis(ROWS, jgen, axis, Axis.ROWS, connection, modelConfig);
			serializeAxis(COLUMNS, jgen, axis, Axis.COLUMNS, connection, modelConfig);
			// List<Hierarchy> hierarchy = value.getCube().getHierarchies();
			// serializeFilters(FILTERS, jgen, hierarchy, (PivotModelImpl)
			// value);
			serializeDimensions(jgen, otherHDimensions, FILTERS_AXIS_POS, FILTERS, true, (PivotModelImpl) value, connection, modelConfig);
			/***********************************************/
			serializeFunctions(FORMULAS, jgen, model, modelConfig);
			/***********************************************/
			serializeCalculatedFields(CALCULATED_FIELDS, jgen, model);

			jgen.writeNumberField(COLUMNSAXISORDINAL, Axis.COLUMNS.axisOrdinal());
			jgen.writeNumberField(ROWSAXISORDINAL, Axis.ROWS.axisOrdinal());
			jgen.writeObjectField(MODELCONFIG, modelConfig);

			// build the query mdx
			String mdxQuery = formatQueryString(value.getCurrentMdx());
			jgen.writeStringField(MDXFORMATTED, mdxQuery);
			jgen.writeStringField(MDXWITHOUTCF, model.getQueryWithOutCC());

			boolean hasPendingTransformations = value instanceof SpagoBIPivotModel && ((SpagoBIPivotModel) value).hasPendingTransformations();
			jgen.writeBooleanField(HAS_PENDING_TRANSFORMATIONS, hasPendingTransformations);

			jgen.writeEndObject();

		} catch (Exception e) {
			logger.error("Error serializing the pivot table", e);
			throw new SpagoBIRuntimeException("Error serializing the pivot table", e);
		}
		totalTime.stop();
		logger.debug("OUT");

	}

	/**
	 * @param calculatedFields
	 * @param jgen
	 * @param model
	 * @throws IOException
	 */
	private void serializeCalculatedFields(String fieldName, JsonGenerator jgen, SpagoBIPivotModel model) throws IOException {

		jgen.writeArrayFieldStart(fieldName);
		for (CalculatedMember cf : model.getCalculatedFields()) {
			Map<String, Object> map = new HashMap<>();
			map.put("name", cf.getCalculateFieldName());
			map.put("parentMemberUniqueName", cf.getParentMemberUniqueName());
			map.put("formula", cf.getFormula());

			jgen.writeObject(map);
		}
		jgen.writeEndArray();
	}

	private void serializeAxis(String field, JsonGenerator jgen, List<CellSetAxis> axis, Axis type, OlapConnection connection, ModelConfig modelConfig)
			throws JSONException, JsonGenerationException, IOException {
		CellSetAxis aAxis = axis.get(0);
		int axisPos = 0;
		if (!aAxis.getAxisOrdinal().equals(type)) {
			aAxis = axis.get(1);
			axisPos = 1;
		}
		List<Hierarchy> hierarchies = aAxis.getAxisMetaData().getHierarchies();
		if (hierarchies != null) {
			List<Dimension> dimensions = CubeUtilities.getDimensions(hierarchies);
			serializeDimensions(jgen, dimensions, axisPos, field, false, null, connection, modelConfig);
		}
	}

	private void serializeDimensions(JsonGenerator jgen, List<Dimension> dimensions, int axis, String field, boolean withSlicers, PivotModelImpl model,
			OlapConnection connection, ModelConfig modelConfig) throws JSONException, JsonGenerationException, IOException {

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

	private void serializeFunctions(String field, JsonGenerator jgen, SpagoBIPivotModel model, ModelConfig modelConfig)
			throws JsonProcessingException, IOException, JSONException, JAXBException {

		MDXFormulaHandler.setModel(model);
		MDXFormulaHandler.setModelConfig(modelConfig);
		MDXFormulas formulas = MDXFormulaHandler.getFormulas();
		jgen.writeArrayFieldStart(field);

		if (formulas != null) {
			for (MDXFormula formula : formulas.getFormulas()) {

				Map<String, Object> formulaObject = new HashMap<String, Object>();

				formulaObject.put("name", formula.getName());
				formulaObject.put("syntax", formula.getSyntax());
				formulaObject.put("body", formula.getBody());
				formulaObject.put("argument", formula.getArguments());
				formulaObject.put("description", formula.getDescription());
				formulaObject.put("output", formula.getOutput());
				formulaObject.put("type", formula.getType());

				jgen.writeObject(formulaObject);
			}
		}

		jgen.writeEndArray();
		// String name = MDXFormula.class.getDeclaredFields()[0].getName();
		// System.out.println(name);
	}

	/*
	 * private void serializeTables(String field, JsonGenerator jgen, Map<Integer, String> tables) throws JsonGenerationException, IOException {
	 * jgen.writeArrayFieldStart(field);
	 *
	 * jgen.writeObject(tables);
	 *
	 * jgen.writeEndArray(); }
	 */

	/*
	 * private void serializeFilters(String field, JsonGenerator jgen, List<Hierarchy> hierarchies, PivotModelImpl model, OlapConnection connection, ModelConfig
	 * modelConfig) throws JSONException, JsonGenerationException, IOException {
	 *
	 * QueryAdapter qa = new QueryAdapter(model); qa.initialize();
	 *
	 * ChangeSlicer ph = new ChangeSlicerImpl(qa, connection);
	 *
	 * jgen.writeArrayFieldStart(field); if (hierarchies != null) { for (int i = 0; i < hierarchies.size(); i++) { Hierarchy hierarchy = hierarchies.get(i);
	 * Map<String, Object> hierarchyObject = new HashMap<String, Object>(); hierarchyObject.put(NAME, hierarchy.getName()); hierarchyObject.put(UNIQUE_NAME,
	 * hierarchy.getUniqueName()); hierarchyObject.put(POSITION, "" + i); hierarchyObject.put(AXIS, "" + FILTERS_AXIS_POS);
	 *
	 * List<Member> slicers = ph.getSlicer(hierarchy); if (slicers != null && slicers.size() > 0) { List<Map<String, String>> slicerMap = new
	 * ArrayList<Map<String, String>>(); for (int j = 0; j < slicers.size(); j++) { Map<String, String> slicer = new HashMap<String, String>();
	 * slicer.put(UNIQUE_NAME, slicers.get(j).getUniqueName()); slicer.put(NAME, slicers.get(j).getName()); slicerMap.add(slicer); }
	 * hierarchyObject.put(SLICERS, slicerMap); } jgen.writeObject(hierarchyObject);
	 *
	 * } } jgen.writeEndArray(); }
	 */

	public String formatQueryString(String queryString) {
		if (queryString == null || queryString.equals("")) {
			logger.error("Impossible to get the query string because the query is null");
			return "";
		}
		return StringUtilities.fromStringToHTML(queryString);
	}

	private void doPagination(boolean condition, ModelConfig modelConfig, SpagoBIPivotModel model, WhatIfHTMLRendereCallback callback, StringWriter writer,
			WhatIfHTMLRenderer renderer, JsonGenerator jgen, String table) {
		modelConfig.setRowCount(model.getCellSet().getAxes().get(Axis.ROWS.axisOrdinal()).getPositionCount());
		modelConfig.setColumnCount(model.getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal()).getPositionCount());

		Map<Integer, String> tables = new HashMap<Integer, String>();

		// SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss.SSS");

		int axisLength = model.getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal()).getPositionCount();

		// System.out.println(time);
		int pages = Math.round(PAGES_COUNT / 2);

		// used for translation from cellset of subset mdx to cellset of plain mdx
		callback.addProperty(COLUMN_OFFSET, modelConfig.getStartColumn());
		callback.addProperty(AXIS_LENGTH, axisLength);

		int min = modelConfig.getStartRow() - pages;
		int max = modelConfig.getStartRow() + 3 * pages;
		modelConfig.setPageSize(pages);

		if (pages == 0) {
			pages = 1;
		}
		if (max == 0) {
			min = 0;
			max = pages;
		}
		if (min < 0) {
			min = 0;
		}
		for (int i = min; i <= max && i < modelConfig.getRowCount(); i++) {

			writer.getBuffer().setLength(0);
			model.removeSubset();
			model.setSubset(i, 0, modelConfig.getRowsSet(), modelConfig.getColumnCount());

			// used for translation from cellset of subset mdx to cellset of plain mdx
			callback.addProperty(ROW_OFFSET, modelConfig.getStartRow() + i);
			callback.addProperty(SUBSET_AXIS_LENGTH, model.getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal()).getPositionCount());

			if (!(model.getCellSet().getAxes().get(Axis.ROWS.axisOrdinal()).getPositionCount() < 1)) {

				renderer.render(model, callback);

				try {
					writer.flush();
					writer.close();
					table = writer.getBuffer().toString();
					tables.put(i, table);

				} catch (IOException e) {
					logger.error("Error serializing the table", e);
					throw new SpagoBIEngineRuntimeException("Error serializing the table", e);
				}
			}

		}

		try {

			jgen.writeStringField(TABLE, tables.get(modelConfig.getStartRow()));
			jgen.writeObjectField("tables", tables);

		} catch (JsonGenerationException e) {
			logger.error("Error serializing the table", e);
			throw new SpagoBIEngineRuntimeException("Error serializing the table", e);
		} catch (IOException e) {
			logger.error("Error serializing the table", e);
			throw new SpagoBIEngineRuntimeException("Error serializing the table", e);
		}

	}

}
