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

import it.eng.spagobi.commons.utilities.StringUtilities;
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

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

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
	private static final String POSITION = "position";
	private static final String AXIS = "axis";
	private static final String SLICERS = "slicers";
	private static final String FORMULAS = "formulas";

	public PivotJsonHTMLSerializer() {
	}

	@Override
	public void serialize(PivotObjectForRendering pivotobject, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {

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

		callback.setCellSpacing(0);

		callback.setRowHeaderStyleClass(" x-pivot-header ");
		callback.setColumnHeaderStyleClass(" x-pivot-header-column");
		callback.setCornerStyleClass(" x-pivot-header x-pivot-corner");
		callback.setCellStyleClass(" x-pivot-cell x-pivot-header-column");
		callback.setTableStyleClass("x-pivot-table");
		callback.setRowStyleClass(" generic-row-style ");

		callback.setEvenRowStyleClass(" even-row ");
		callback.setOddRowStyleClass(" odd-row ");

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
		 * NonEmpty transformNonEmpty = value.getTransform(NonEmpty.class);
		 * transformNonEmpty.setNonEmpty(suppressEmpty);
		 */

		model.setNonEmpty(suppressEmpty);
		modelConfig.setRowCount(model.getCellSet().getAxes().get(Axis.ROWS.axisOrdinal()).getPositionCount());
		modelConfig.setColumnCount(model.getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal()).getPositionCount());
		model.setSubset(modelConfig.getStartRow(), modelConfig.getStartColumn(), modelConfig.getRowsSet(), modelConfig.getColumnSet());

		// modelConfig.setRowCount(rowCount);

		// updates the actual version in the model config
		if (modelConfig.isWhatIfScenario()) {
			Integer actualVersion = VersionManager.getActualVersion(value, modelConfig);
			modelConfig.setActualVersion(actualVersion);
		}
//		model.addCalucatedMembers(true);
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss.SSS");
		String time = "Serilize start " + format.format(new Date());
		// System.out.println(time);
		renderer.render(value, callback);
		time = "Serilize end " + format.format(new Date());
		// System.out.println(time);

		// System.out.println();
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
			/***********************************************/
			serializeFunctions(FORMULAS, jgen);
			/***********************************************/
			serializeAxis(ROWS, jgen, axis, Axis.ROWS, connection, modelConfig);
			serializeAxis(COLUMNS, jgen, axis, Axis.COLUMNS, connection, modelConfig);
			List<Hierarchy> hierarchy = value.getCube().getHierarchies();
			// serializeFilters(FILTERS, jgen, hierarchy, (PivotModelImpl)
			// value);
			serializeDimensions(jgen, otherHDimensions, FILTERS_AXIS_POS, FILTERS, true, (PivotModelImpl) value, connection, modelConfig);
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

	private int getNonEmptySet(int nonEmptyStart, int set, int count, int axisOrdinal, SpagoBIPivotModel model) {
		int newSet = 1;
		int subSetCount = 1;

		while (subSetCount != set && count > newSet) {
			newSet++;
			model.removeSubset();
			if (axisOrdinal == Axis.ROWS.axisOrdinal()) {
				model.setSubset(nonEmptyStart, 0, newSet, 1);
			} else {
				model.setSubset(0, nonEmptyStart, 1, newSet);
			}

			subSetCount = model.getCellSet().getAxes().get(axisOrdinal).getPositionCount();
		}
		model.removeSubset();
		return newSet;
	}

	private int getNonEmptyStart(int start, int axisOrdinal, SpagoBIPivotModel model) {

		int nonEmptyStart = 0;

		int subSetCount = model.getCellSet().getAxes().get(axisOrdinal).getPositionCount();

		for (int i = 0; i < start; i++) {
			model.removeSubset();
			if (axisOrdinal == Axis.ROWS.axisOrdinal()) {
				model.setSubset(nonEmptyStart, 0, 1, 1);
			} else {
				model.setSubset(0, nonEmptyStart, 1, 1);
			}

			subSetCount = model.getCellSet().getAxes().get(axisOrdinal).getPositionCount();

			while (subSetCount == 0) {
				nonEmptyStart++;
				model.removeSubset();
				if (axisOrdinal == Axis.ROWS.axisOrdinal()) {
					model.setSubset(nonEmptyStart, 0, 1, 1);
				} else {
					model.setSubset(0, nonEmptyStart, 1, 1);
				}

				subSetCount = model.getCellSet().getAxes().get(axisOrdinal).getPositionCount();

			}
			nonEmptyStart++;
		}
		model.removeSubset();
		return nonEmptyStart;
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

	private void serializeFunctions(String field, JsonGenerator jgen) throws JsonProcessingException, IOException, JSONException, JAXBException {

		MDXFormulas formulas = MDXFormulaHandler.getFormulas();
		jgen.writeArrayFieldStart(field);

		if (formulas != null) {
			for (MDXFormula formula : formulas.getFormulas()) {

				Map<String, Object> formulaObject = new HashMap<String, Object>();

				formulaObject.put("name", formula.getName());
				formulaObject.put("syntax", formula.getSyntax());
				formulaObject.put("argument", formula.getArguments());
				formulaObject.put("description", formula.getDescription());
				formulaObject.put("output", formula.getOutput());
				formulaObject.put("type", formula.getType());

				jgen.writeObject(formulaObject);
			}
		}

		jgen.writeEndArray();
		String name = MDXFormula.class.getDeclaredFields()[0].getName();
		// System.out.println(name);
	}

	/*
	 * private void serializeFilters(String field, JsonGenerator jgen,
	 * List<Hierarchy> hierarchies, PivotModelImpl model, OlapConnection
	 * connection, ModelConfig modelConfig) throws JSONException,
	 * JsonGenerationException, IOException {
	 *
	 * QueryAdapter qa = new QueryAdapter(model); qa.initialize();
	 *
	 * ChangeSlicer ph = new ChangeSlicerImpl(qa, connection);
	 *
	 * jgen.writeArrayFieldStart(field); if (hierarchies != null) { for (int i =
	 * 0; i < hierarchies.size(); i++) { Hierarchy hierarchy =
	 * hierarchies.get(i); Map<String, Object> hierarchyObject = new
	 * HashMap<String, Object>(); hierarchyObject.put(NAME,
	 * hierarchy.getName()); hierarchyObject.put(UNIQUE_NAME,
	 * hierarchy.getUniqueName()); hierarchyObject.put(POSITION, "" + i);
	 * hierarchyObject.put(AXIS, "" + FILTERS_AXIS_POS);
	 *
	 * List<Member> slicers = ph.getSlicer(hierarchy); if (slicers != null &&
	 * slicers.size() > 0) { List<Map<String, String>> slicerMap = new
	 * ArrayList<Map<String, String>>(); for (int j = 0; j < slicers.size();
	 * j++) { Map<String, String> slicer = new HashMap<String, String>();
	 * slicer.put(UNIQUE_NAME, slicers.get(j).getUniqueName()); slicer.put(NAME,
	 * slicers.get(j).getName()); slicerMap.add(slicer); }
	 * hierarchyObject.put(SLICERS, slicerMap); }
	 * jgen.writeObject(hierarchyObject);
	 *
	 * } } jgen.writeEndArray(); }
	 */

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
