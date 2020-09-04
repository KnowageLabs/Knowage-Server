/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.knowage.engine.cockpit.api.export.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.crosstable.CrossTab;
import it.eng.knowage.engine.cockpit.api.crosstable.CrosstabBuilder;
import it.eng.knowage.engine.cockpit.api.crosstable.CrosstabSerializationConstants;
import it.eng.knowage.engine.cockpit.api.crosstable.NodeComparator;
import it.eng.knowage.engine.cockpit.api.export.excel.crosstab.CrosstabXLSXExporter;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * @authors Francesco Lucchi (francesco.lucchi@eng.it)
 */

public class ExcelExporter {

	static private Logger logger = Logger.getLogger(ExcelExporter.class);

	private final String outputType;
	private final String userUniqueIdentifier;
	private final Map<String, String> i18nMessages;
	private final boolean exportWidget;
	private final JSONObject body;
	private Locale locale;
	private int uniqueId = 0;

	private static final String[] WIDGETS_TO_IGNORE = { "image", "text", "python", "r" };

	// Old implementation with parameterMap
	public ExcelExporter(String outputType, String userUniqueIdentifier, Map<String, String[]> parameterMap) {
		this.outputType = outputType;
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.exportWidget = false;
		this.body = new JSONObject();

		Locale locale = getLocale(parameterMap);
		try {
			i18nMessages = DAOFactory.getI18NMessageDAO().getAllI18NMessages(locale);
			LogMF.debug(logger, "Loaded messages [{0}]", i18nMessages);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while retrieving the I18N messages", e);
		}
	}

	public ExcelExporter(String outputType, String userUniqueIdentifier, JSONObject body) {
		this.outputType = outputType;
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.exportWidget = setExportWidget(body);
		this.body = body;

		Locale locale = getLocale(body);
		this.locale = locale;
		try {
			i18nMessages = DAOFactory.getI18NMessageDAO().getAllI18NMessages(locale);
			LogMF.debug(logger, "Loaded messages [{0}]", i18nMessages);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while retrieving the I18N messages", e);
		}
	}

	private boolean setExportWidget(JSONObject body) {
		boolean exportWidgetOnly = body.optBoolean("exportWidget");
		return Boolean.valueOf(exportWidgetOnly);

	}

	private Locale getLocale(JSONObject body) {
		try {
			String language = body.getString(SpagoBIConstants.SBI_LANGUAGE);
			String country = body.getString(SpagoBIConstants.SBI_COUNTRY);
			Locale toReturn = new Locale(language, country);
			return toReturn;
		} catch (Exception e) {
			logger.warn("Cannot get locale information from input parameters body", e);
			return Locale.ENGLISH;
		}

	}

	private Locale getLocale(Map<String, String[]> parameterMap) {
		try {
			Assert.assertNotNull(parameterMap, "Empty input parameters map");
			Assert.assertNotNull(parameterMap.get(SpagoBIConstants.SBI_LANGUAGE), "Missing language code in input parameters map");
			Assert.assertNotNull(parameterMap.get(SpagoBIConstants.SBI_COUNTRY), "Missing country code in input parameters map");
			Assert.assertTrue(parameterMap.get(SpagoBIConstants.SBI_LANGUAGE).length == 1, "More than one language code in input parameters map");
			Assert.assertTrue(parameterMap.get(SpagoBIConstants.SBI_COUNTRY).length == 1, "More than one country code in input parameters map");

			String language = parameterMap.get(SpagoBIConstants.SBI_LANGUAGE)[0];
			String country = parameterMap.get(SpagoBIConstants.SBI_COUNTRY)[0];
			Locale toReturn = new Locale(language, country);
			return toReturn;
		} catch (Exception e) {
			logger.warn("Could get locale information from input parameters map", e);
			return Locale.ENGLISH;
		}

	}

	public String getMimeType() {
		String mimeType;
		if ("xlsx".equalsIgnoreCase(outputType)) {
			mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		} else if ("xls".equalsIgnoreCase(outputType)) {
			mimeType = "application/vnd.ms-excel";
		} else {
			throw new SpagoBIRuntimeException("Unsupported output type [" + outputType + "]");
		}
		return mimeType;
	}

	public byte[] getBinaryData(Integer documentId, String documentLabel, String templateString, String options) throws JSONException, SerializationException {
		if (templateString == null) {
			ObjTemplate template = null;
			String message = "Unable to get template for document with id [" + documentId + "] and label [" + documentLabel + "]";
			try {
				if (documentId != null && documentId.intValue() != 0) {
					template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(documentId);
				} else if (documentLabel != null && !documentLabel.isEmpty()) {
					template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplateByLabel(documentLabel);
				}

				if (template == null) {
					throw new SpagoBIRuntimeException(message);
				}
				templateString = new String(template.getContent());
			} catch (EMFAbstractError e) {
				throw new SpagoBIRuntimeException(message);
			}
		}

		Workbook wb;

		if ("xlsx".equalsIgnoreCase(outputType)) {
			wb = new XSSFWorkbook();
		} else if ("xls".equalsIgnoreCase(outputType)) {
			wb = new HSSFWorkbook();
		} else {
			throw new SpagoBIRuntimeException("Unsupported output type [" + outputType + "]");
		}

		try {
			if (exportWidget) {
				String widgetId = String.valueOf(body.get("widget"));
				if (options.isEmpty()) // check if exporting crosstab
					exportWidget(templateString, widgetId, wb);
				else {
					JSONObject optionsObj = new JSONObject(options);
					exportWidgetCrossTab(templateString, widgetId, wb, optionsObj);
				}
			} else {
				JSONArray widgetsJson = body.getJSONArray("widget");
				exportCockpit(templateString, widgetsJson, wb);
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot export data to excel", e);
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			wb.write(out);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Unable to generate output file with extension [" + outputType + "]", e);
		}

		return out.toByteArray();
	}

	private void exportCockpit(String templateString, JSONArray widgetsJson, Workbook wb) throws SerializationException {
		JSONObject options = new JSONObject();
		try {
			for (int i = 0; i < widgetsJson.length(); i++) {
				JSONObject currWidget = widgetsJson.getJSONObject(i);
				String widgetId = currWidget.getString("id");
				String widgetType = currWidget.getString("type");
				if (Arrays.asList(WIDGETS_TO_IGNORE).contains(widgetType.toLowerCase()))
					continue;
				else if (widgetType.equalsIgnoreCase("static-pivot-table"))
					exportWidgetCrossTab(templateString, widgetId, wb, options);
				else
					exportWidget(templateString, widgetId, wb);
			}
		} catch (JSONException e) {
			logger.error("Error exporting cockpit", e);
		}
	}

	private void exportWidget(String templateString, String widgetId, Workbook wb) throws SerializationException {
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			if (widget != null) {
				String widgetName = null;
				JSONObject style = widget.optJSONObject("style");
				if (style != null) {
					JSONObject title = style.optJSONObject("title");
					if (title != null) {
						widgetName = title.optString("label");
					} else {
						JSONObject content = widget.optJSONObject("content");
						if (content != null) {
							widgetName = content.getString("name");
						}
					}
				}

				JSONObject dataStore = getDataStoreForWidget(template, widget);
				if (dataStore != null) {
					createExcelFile(dataStore, wb, widgetName);
				}
			}
		} catch (JSONException e) {
			logger.error("Unable to load template", e);
		}
	}

	private void exportWidgetCrossTab(String templateString, String widgetId, Workbook wb, JSONObject optionsObj) throws SerializationException {
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			if (widget != null) {
				String widgetName = null;
				JSONObject style = widget.optJSONObject("style");
				if (style != null) {
					JSONObject title = style.optJSONObject("title");
					if (title != null) {
						widgetName = title.optString("label");
					} else {
						JSONObject content = widget.optJSONObject("content");
						if (content != null) {
							widgetName = content.getString("name");
						}
					}
				}

				JSONObject crosstabDefinition = optionsObj.getJSONObject("crosstabDefinition");
				JSONArray measures = crosstabDefinition.optJSONArray("measures");
				Map<String, List<Threshold>> thresholdColorsMap = getThresholdColorsMap(measures);

				CrosstabXLSXExporter exporter = new CrosstabXLSXExporter(null, thresholdColorsMap);

				JSONObject crosstabDefinitionJo = optionsObj.getJSONObject("crosstabDefinition");
				JSONObject crosstabDefinitionConfigJo = crosstabDefinitionJo.optJSONObject(CrosstabSerializationConstants.CONFIG);
				JSONObject crosstabStyleJo = (optionsObj.isNull("style")) ? new JSONObject() : optionsObj.getJSONObject("style");
				crosstabDefinitionConfigJo.put("style", crosstabStyleJo);

				JSONObject sortOptions = optionsObj.getJSONObject("sortOptions");

				List<Map<String, Object>> columnsSortKeys;
				List<Map<String, Object>> rowsSortKeys;
				List<Map<String, Object>> measuresSortKeys;

				// the id of the crosstab in the client configuration array
				Integer myGlobalId;
				JSONArray columnsSortKeysJo = sortOptions.optJSONArray("columnsSortKeys");
				JSONArray rowsSortKeysJo = sortOptions.optJSONArray("rowsSortKeys");
				JSONArray measuresSortKeysJo = sortOptions.optJSONArray("measuresSortKeys");
				myGlobalId = sortOptions.optInt("myGlobalId");
				columnsSortKeys = JSONUtils.toMap(columnsSortKeysJo);
				rowsSortKeys = JSONUtils.toMap(rowsSortKeysJo);
				measuresSortKeys = JSONUtils.toMap(measuresSortKeysJo);
				if (optionsObj != null) {
					logger.debug("Export cockpit crosstab optionsObj.toString(): " + optionsObj.toString());
				}

				Map<Integer, NodeComparator> columnsSortKeysMap = toComparatorMap(columnsSortKeys);
				Map<Integer, NodeComparator> rowsSortKeysMap = toComparatorMap(rowsSortKeys);
				Map<Integer, NodeComparator> measuresSortKeysMap = toComparatorMap(measuresSortKeys);
				JSONObject styleJSON = (!optionsObj.isNull("style") ? optionsObj.getJSONObject("style") : new JSONObject());
				CrosstabBuilder builder = new CrosstabBuilder(locale, crosstabDefinition, optionsObj.getJSONArray("jsonData"),
						optionsObj.getJSONObject("metadata"), styleJSON, null);

				CrossTab cs = builder.getSortedCrosstabObj(columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, myGlobalId);

				Sheet sheet;

				widgetName = WorkbookUtil.createSafeSheetName(widgetName);
				sheet = createUniqueSheet(wb, widgetName);

				CreationHelper createHelper = wb.getCreationHelper();

				exporter.fillAlreadyCreatedSheet(sheet, cs, createHelper, 0, locale);

			}
		} catch (JSONException e) {
			logger.error("Unable to load template", e);
		}
	}

	private Map<String, List<Threshold>> getThresholdColorsMap(JSONArray measures) {
		Map<String, List<Threshold>> toReturn = new HashMap<String, List<Threshold>>();
		try {
			for (int i = 0; i < measures.length(); i++) {
				JSONObject measure = measures.getJSONObject(i);
				String id = measure.getString("id");
				JSONArray ranges = measure.getJSONArray("ranges");
				List<Threshold> allThresholds = new ArrayList<Threshold>();
				for (int j = 0; j < ranges.length(); j++) {
					JSONObject range = ranges.getJSONObject(j);
					String operator = range.getString("operator");
					if (!operator.equals("none")) {
						Double value = range.getDouble("value");
						String color = range.getString("background-color");
						Threshold threshold = new Threshold(operator, value, color);
						allThresholds.add(threshold);
					}
				}
				toReturn.put(id, allThresholds);
			}
		} catch (Exception e) {
			logger.error("Unable to build threshold color map", e);
			Map<String, List<Threshold>> emptyMap = new HashMap<String, List<Threshold>>();
			return emptyMap;
		}
		return toReturn;
	}

	private JSONObject getWidgetById(JSONObject template, String widgetId) {
		try {
			long widget_id = Long.parseLong(widgetId);

			JSONArray sheets = template.getJSONArray("sheets");
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				JSONArray widgets = sheet.getJSONArray("widgets");
				for (int j = 0; j < widgets.length(); j++) {
					JSONObject widget = widgets.getJSONObject(j);
					long id = widget.getLong("id");
					if (id == widget_id) {
						return widget;
					}
				}
			}
		} catch (JSONException e) {
			logger.error("Unable to get widget", e);
		}
		return null;
	}

	private JSONObject getDataStoreForWidget(JSONObject template, JSONObject widget) {
		Map<String, Object> map = new java.util.HashMap<String, Object>();
		JSONObject datastore = null;
		try {
			JSONObject configuration = template.getJSONObject("configuration");
			JSONObject datasetObj = widget.getJSONObject("dataset");

			int datasetId = datasetObj.getInt("dsId");
			IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
			String datasetLabel = dataset.getLabel();

			if (getRealtimeFromTableWidget(datasetId, configuration)) {
				logger.debug("nearRealtime = true");
				map.put("nearRealtime", true);
			}

			JSONObject cockpitSelections = getCockpitSelectionsFromBody(widget);
			JSONArray summaryRow = getSummaryRowFromWidget(widget);
			if (summaryRow != null) {
				logger.debug("summaryRow = " + summaryRow);
				cockpitSelections.put("summaryRow", summaryRow);
			}

			if (isSolrDataset(dataset) && !widget.getString("type").equalsIgnoreCase("discovery")) {

				JSONObject jsOptions = new JSONObject();
				jsOptions.put("solrFacetPivot", true);
				cockpitSelections.put("options", jsOptions);
			}
			if (body != null) {
				logger.debug("Export single widget cockpitSelections.toString(): " + cockpitSelections.toString());
			}
			datastore = getDatastore(datasetLabel, map, cockpitSelections.toString());

			if (datastore != null) {
				logger.debug("datasetLabel: " + datasetLabel + " datastoreObj = " + datastore.toString());
			}

			datastore.put("widgetData", widget);
			datastore.put("cocpitSelectionAggregations", cockpitSelections.get("aggregations"));
			JSONObject content = widget.optJSONObject("content");
			String widgetName = null;
			if (widget.has("style")) {
				JSONObject style = widget.optJSONObject("style");
				if (style.has("title")) {
					JSONObject title = style.optJSONObject("title");
					if (title.has("label")) {
						widgetName = title.getString("label");
					}
				}

			}
			if (widgetName == null && content != null) {
				widgetName = content.getString("name");
			}
			datastore.put("widgetName", widgetName);

		} catch (Exception e) {
			logger.error("Cannot get Datastore for widget", e);
		}
		return datastore;
	}

	private JSONObject getCockpitSelectionsFromBody(JSONObject widget) {
		JSONObject cockpitSelections = new JSONObject();
		try {
			if (exportWidget) // export single widget
				cockpitSelections = body.getJSONObject("COCKPIT_SELECTIONS");
			else { // export whole cockpit
				JSONArray allWidgets = body.getJSONArray("widget");
				int i;
				for (i = 0; i < allWidgets.length(); i++) {
					JSONObject curWidget = allWidgets.getJSONObject(i);
					if (curWidget.getString("id").equals(widget.getString("id")))
						break;
				}
				cockpitSelections = body.getJSONArray("COCKPIT_SELECTIONS").getJSONObject(i);
			}
		} catch (Exception e) {
			logger.error("Cannot get cockpit selections", e);
		}
		return cockpitSelections;
	}

	private void createExcelFile(JSONObject dataStore, Workbook wb, String widgetName) throws JSONException, SerializationException {
		CreationHelper createHelper = wb.getCreationHelper();
		JSONArray widgetsMapAggregations = new JSONArray();
		if (body.has("widgetsMapAggregations")) {
			widgetsMapAggregations = body.getJSONArray("widgetsMapAggregations");
		}

		if (dataStore.has("widgetType") && dataStore.getString("widgetType").equalsIgnoreCase("[\"static-pivot-table\"]")) {

			CrosstabXLSXExporter exporter = new CrosstabXLSXExporter(null);

			JSONObject content = dataStore.getJSONArray("widget").getJSONObject(0).getJSONObject("content");
			JSONObject crosstabDefinitionJo = content.getJSONObject("crosstabDefinition");
			JSONObject crosstabDefinitionConfigJo = crosstabDefinitionJo.optJSONObject(CrosstabSerializationConstants.CONFIG);
			JSONObject crosstabStyleJo = (content.isNull("style")) ? new JSONObject() : content.getJSONObject("style");
			crosstabDefinitionConfigJo.put("style", crosstabStyleJo);

			JSONObject sortOptions = content.getJSONObject("sortOptions");

			List<Map<String, Object>> columnsSortKeys;
			List<Map<String, Object>> rowsSortKeys;
			List<Map<String, Object>> measuresSortKeys;

			// the id of the crosstab in the client configuration array
			Integer myGlobalId;
			JSONArray columnsSortKeysJo = sortOptions.optJSONArray("columnsSortKeys");
			JSONArray rowsSortKeysJo = sortOptions.optJSONArray("rowsSortKeys");
			JSONArray measuresSortKeysJo = sortOptions.optJSONArray("measuresSortKeys");
			myGlobalId = sortOptions.optInt("myGlobalId");
			columnsSortKeys = JSONUtils.toMap(columnsSortKeysJo);
			rowsSortKeys = JSONUtils.toMap(rowsSortKeysJo);
			measuresSortKeys = JSONUtils.toMap(measuresSortKeysJo);

			Map<Integer, NodeComparator> columnsSortKeysMap = toComparatorMap(columnsSortKeys);
			Map<Integer, NodeComparator> rowsSortKeysMap = toComparatorMap(rowsSortKeys);
			Map<Integer, NodeComparator> measuresSortKeysMap = toComparatorMap(measuresSortKeys);
			JSONObject styleJSON = (!content.isNull("style") ? content.getJSONObject("style") : new JSONObject());

			JSONObject datastoreObjData = dataStore.getJSONObject("datastoreObjData");

			if (datastoreObjData != null) {
				logger.debug("Export cockpit crosstab datastoreObjData.toString(): " + datastoreObjData.toString());
			}

			CrosstabBuilder builder = new CrosstabBuilder(locale, crosstabDefinitionJo, datastoreObjData.getJSONArray("rows"),
					datastoreObjData.getJSONObject("metaData"), styleJSON, null);

			CrossTab cs = builder.getSortedCrosstabObj(columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, myGlobalId);

			Sheet sheet;

			widgetName = WorkbookUtil.createSafeSheetName(widgetName);
			sheet = createUniqueSheet(wb, widgetName);

			exporter.fillAlreadyCreatedSheet(sheet, cs, createHelper, 0, locale);

		} else {
			try {
				JSONObject metadata = dataStore.getJSONObject("metaData");
				JSONArray columns = metadata.getJSONArray("fields");
				columns = filterDataStoreColumns(columns);
				JSONArray rows = dataStore.getJSONArray("rows");
				JSONObject columnsAggregations = new JSONObject();
				if (dataStore.has("cocpitSelectionAggregations")) {
					columnsAggregations = dataStore.getJSONObject("cocpitSelectionAggregations");
				}

				HashMap<String, String> mapColumnsAggregations = getMapFromAggregations(columnsAggregations);

				Sheet sheet;

				JSONObject widgetData = dataStore.getJSONObject("widgetData");

				if (widgetData != null)
					logger.debug("widgetData: " + widgetData.toString());

				JSONObject widgetContent = widgetData.getJSONObject("content");
				HashMap<String, String> arrayHeader = new HashMap<String, String>();

				if (widgetData.getString("type").equalsIgnoreCase("table") || widgetData.getString("type").equalsIgnoreCase("advanced-table")) {

					if (widgetContent.has("columnSelectedOfDataset") && widgetContent.getJSONArray("columnSelectedOfDataset").length() > 0) {

						if (widgetContent.has("columnSelectedOfDataset"))
							logger.debug("columnSelectedOfDataset: " + widgetContent.getJSONArray("columnSelectedOfDataset").toString());

						for (int i = 0; i < widgetContent.getJSONArray("columnSelectedOfDataset").length(); i++) {

							JSONObject column = widgetContent.getJSONArray("columnSelectedOfDataset").getJSONObject(i);

							if (column.has("name")) {

								arrayHeader.put(column.getString("name"), column.getString("aliasToShow"));

							} else {

								if (column.has("aliasToShow")) {
									arrayHeader.put(column.getString("alias"), column.getString("aliasToShow"));
								} else {

									arrayHeader.put(column.getString("alias"), column.getString("alias"));
								}
							}
						}
					}

				}

				JSONArray aggrNewVar = new JSONArray();
				JSONObject currentWidgetMapAggregations = new JSONObject();
				if (widgetsMapAggregations != null && !widgetsMapAggregations.isNull(0) && widgetData.has("id")) {
					for (int i = 0; i < widgetsMapAggregations.length(); i++) {
						if (widgetsMapAggregations.getJSONObject(i).getInt("id") == widgetData.getInt("id")) {
							currentWidgetMapAggregations = widgetsMapAggregations.getJSONObject(i);
							break;
						}
					}

					if (currentWidgetMapAggregations.has("columnSelectedOfDataset"))
						aggrNewVar = (JSONArray) currentWidgetMapAggregations.get("columnSelectedOfDataset");

					mapColumnsAggregations = getMapFromAggregationsFromArray(aggrNewVar);

				}

				// column.header matches with name or alias
				// Fill Header
				JSONArray columnsOrdered = new JSONArray();
				JSONArray groupsArray = new JSONArray();
				if (widgetData.has("groups")) {
					groupsArray = widgetData.getJSONArray("groups");
				}
				HashMap<String, String> mapGroupsAndColumns = new HashMap<String, String>();

				HashMap<String, String> headerToAlias = new HashMap<String, String>();

				if ((widgetData.getString("type").equalsIgnoreCase("table") || widgetData.getString("type").equalsIgnoreCase("advanced-table"))
						&& widgetContent.has("columnSelectedOfDataset")) {
					for (int i = 0; i < widgetContent.getJSONArray("columnSelectedOfDataset").length(); i++) {

						JSONObject column = widgetContent.getJSONArray("columnSelectedOfDataset").getJSONObject(i);
						boolean hidden = false;
						if (column.has("style")) {
							JSONObject style = column.optJSONObject("style");
							if (style.has("hiddenColumn")) {
								if (style.getString("hiddenColumn").equals("true")) {
									hidden = true;
								}

							}
						}
						if (!hidden) {

							for (int j = 0; j < columns.length(); j++) {
								JSONObject columnOld = columns.getJSONObject(j);
								if (column.has("name")) {
									if (columnOld.getString("header").equals(column.getString("name"))) {
										headerToAlias.put(columnOld.getString("header"), column.getString("name"));
										columnsOrdered.put(columnOld);
										break;
									} else if (columnOld.getString("header").equals(column.getString("aliasToShow"))) {
										headerToAlias.put(columnOld.getString("header"), column.getString("name"));
										columnsOrdered.put(columnOld);
										break;
									} else if (columnOld.getString("header").equals(mapColumnsAggregations.get(column.getString("aliasToShow")))) {
										headerToAlias.put(columnOld.getString("header"), column.getString("name"));
										columnsOrdered.put(columnOld);
										break;
									} else if (columnOld.getString("header").equals(mapColumnsAggregations.get(column.getString("name")))) {
										headerToAlias.put(columnOld.getString("header"), column.getString("name"));
										columnsOrdered.put(columnOld);
										break;
									}
								} else {
									if (columnOld.getString("header").equals(column.getString("alias"))) {
										headerToAlias.put(columnOld.getString("header"), column.getString("alias"));
										columnsOrdered.put(columnOld);
										break;
									} else if (columnOld.getString("header").equals(column.getString("aliasToShow"))) {
										headerToAlias.put(columnOld.getString("header"), column.getString("aliasToShow"));
										columnsOrdered.put(columnOld);
										break;
									}
								}
							}
						}
					}
				} else {
					columnsOrdered = columns;
				}
				int isGroup = 0;
				if (widgetContent.has("columnSelectedOfDataset"))
					mapGroupsAndColumns = getMapFromGroupsArray(groupsArray, widgetContent.getJSONArray("columnSelectedOfDataset"));

				Row header = null;
				Row newheader = null;
				if (exportWidget) { // export single widget
					widgetName = WorkbookUtil.createSafeSheetName(widgetName);
					sheet = createUniqueSheet(wb, widgetName);

					// Create HEADER - Column Names
					if (!mapGroupsAndColumns.isEmpty()) {
						isGroup = 1;
						newheader = sheet.createRow((short) 0);
						for (int i = 0; i < columnsOrdered.length(); i++) {
							JSONObject column = columnsOrdered.getJSONObject(i);
							String groupName = mapGroupsAndColumns.get(headerToAlias.get(column.get("header")));
							if (groupName != null) {
								Cell cell = newheader.createCell(i);
								cell.setCellValue(groupName);
							}

						}
						header = sheet.createRow((short) 1);
					} else
						header = sheet.createRow((short) 0); // first row
				} else { // export whole cockpit
					String sheetName = "empty";
					if (dataStore.has("widgetName") && dataStore.getString("widgetName") != null && !dataStore.getString("widgetName").isEmpty()) {
						if (dataStore.has("sheetInfo")) {
							sheetName = dataStore.getString("sheetInfo").concat(".").concat(widgetName);
						} else {
							sheetName = widgetName;
						}
					}
					sheetName = WorkbookUtil.createSafeSheetName(sheetName);
					sheet = createUniqueSheet(wb, sheetName);
					// First row for Widget name in case exporting whole Cockpit document
					Row firstRow = sheet.createRow((short) 0);
					Cell firstCell = firstRow.createCell(0);
					firstCell.setCellValue(widgetName);
					// Create HEADER - Column Names
					if (!mapGroupsAndColumns.isEmpty()) {
						isGroup = 1;
						newheader = sheet.createRow((short) 1);
						for (int i = 0; i < columnsOrdered.length(); i++) {
							JSONObject column = columnsOrdered.getJSONObject(i);
							String groupName = mapGroupsAndColumns.get(headerToAlias.get(column.get("header")));
							if (groupName != null) {
								Cell cell = newheader.createCell(i);
								cell.setCellValue(groupName);
							}

						}
						header = sheet.createRow((short) 2);
					} else
						header = sheet.createRow((short) 1);
				}

				for (int i = 0; i < columnsOrdered.length(); i++) {
					JSONObject column = columnsOrdered.getJSONObject(i);
					String columnName = column.getString("header");
					if (widgetData.getString("type").equalsIgnoreCase("table") || widgetData.getString("type").equalsIgnoreCase("advanced-table")
							|| widgetData.getString("type").equalsIgnoreCase("discovery")) {
						if (arrayHeader.get(columnName) != null) {
							columnName = arrayHeader.get(columnName);
						}
					}

					Cell cell = header.createCell(i);
					cell.setCellValue(columnName);
				}

				// Cell styles for int and float
				CellStyle intCellStyle = wb.createCellStyle();
				intCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("0"));

				CellStyle floatCellStyle = wb.createCellStyle();
				floatCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

				// FILL RECORDS
				for (int r = 0; r < rows.length(); r++) {
					JSONObject rowObject = rows.getJSONObject(r);
					Row row;
					if (exportWidget)
						row = sheet.createRow((r + isGroup) + 1); // starting from second row, because the 0th (first) is Header
					else
						row = sheet.createRow((r + isGroup) + 2);

					for (int c = 0; c < columnsOrdered.length(); c++) {
						JSONObject column = columnsOrdered.getJSONObject(c);
						String type = column.getString("type");
						String colIndex = column.getString("name"); // column_1, column_2, column_3...

						Cell cell = row.createCell(c);
						Object value = rowObject.get(colIndex);

						if (value != null) {
							String s = value.toString();
							switch (type) {
							case "string":
								cell.setCellValue(s);
								break;
							case "int":
								if (!s.trim().isEmpty()) {
									cell.setCellValue(Double.parseDouble(s));
								}
								cell.setCellStyle(intCellStyle);
								break;
							case "float":
								if (!s.trim().isEmpty()) {
									cell.setCellValue(Double.parseDouble(s));
								}
								cell.setCellStyle(floatCellStyle);
								break;
							default:
								cell.setCellValue(s);
								break;
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("Cannot write data to Excel file", e);
			}
		}
	}

	private Sheet createUniqueSheet(Workbook wb, String sheetName) {
		Sheet sheet;
		sheetName = sheetName.concat("_").concat(String.valueOf(uniqueId));
		sheet = wb.createSheet(sheetName);
		uniqueId++;
		return sheet;
	}

	private HashMap<String, String> getMapFromGroupsArray(JSONArray groupsArray, JSONArray aggr) throws JSONException {
		HashMap<String, String> returnMap = new HashMap<String, String>();
		if (aggr != null && groupsArray != null) {

			for (int i = 0; i < groupsArray.length(); i++) {

				String id = groupsArray.getJSONObject(i).getString("id");
				String groupName = groupsArray.getJSONObject(i).getString("name");

				for (int ii = 0; ii < aggr.length(); ii++) {
					JSONObject column = aggr.getJSONObject(ii);

					if (column.has("group") && column.getString("group").equals(id)) {
						String nameToInsert = "";
						if (!column.has("name"))
							nameToInsert = column.getString("alias");
						else
							nameToInsert = column.getString("name");

						returnMap.put(nameToInsert, groupName);
					}

				}
			}
		}

		return returnMap;

	}

	private HashMap<String, String> getMapFromAggregationsFromArray(JSONArray aggr) throws JSONException {
		HashMap<String, String> returnMap = new HashMap<String, String>();
		if (aggr != null) {
			for (int i = 0; i < aggr.length(); i++) {
				JSONObject column = aggr.getJSONObject(i);
				String nameToInsert = "";
				if (!column.has("name"))
					nameToInsert = column.getString("alias");
				else
					nameToInsert = column.getString("name");
				returnMap.put(nameToInsert, column.getString("aliasToShow"));

			}

		}

		return returnMap;

	}

	private HashMap<String, String> getMapFromAggregations(JSONObject aggr) throws JSONException {

		HashMap<String, String> returnMap = new HashMap<String, String>();
		if (aggr.has("measures")) {
			JSONArray measures = aggr.getJSONArray("measures");

			for (int i = 0; i < measures.length(); i++) {
				returnMap.put(measures.getJSONObject(i).getString("id"), measures.getJSONObject(i).getString("alias"));
			}
		}
		if (aggr.has("categories")) {
			JSONArray categories = aggr.getJSONArray("categories");

			for (int i = 0; i < categories.length(); i++) {
				returnMap.put(categories.getJSONObject(i).getString("id"), categories.getJSONObject(i).getString("alias"));
			}
		}
		return returnMap;
	}

	private JSONArray filterDataStoreColumns(JSONArray columns) {
		try {
			for (int i = 0; i < columns.length(); i++) {
				String element = columns.getString(i);
				if (element != null && element.equals("recNo")) {
					columns.remove(i);
					break;
				}
			}
		} catch (JSONException e) {
			logger.error("Can not filter Columns Array");
		}
		return columns;
	}

	public static String[] toStringArray(JSONArray array) {
		if (array == null)
			return null;

		String[] arr = new String[array.length()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = array.optString(i);
		}
		return arr;
	}

	private JSONObject getDatastore(String datasetLabel, Map<String, Object> map, String selections) {
		ExcelExporterClient client = new ExcelExporterClient();
		try {
			JSONObject datastore = client.getDataStore(map, datasetLabel, userUniqueIdentifier, selections);
			return datastore;
		} catch (Exception e) {
			String message = "Unable to get data";
			logger.error(message, e);
			throw new SpagoBIRuntimeException(message);
		}
	}

	private boolean isSolrDataset(IDataSet dataSet) {
		if (dataSet instanceof VersionedDataSet) {
			dataSet = ((VersionedDataSet) dataSet).getWrappedDataset();
		}
		return dataSet instanceof SolrDataSet;
	}

	private JSONArray getSummaryRowFromWidget(JSONObject widget) throws JSONException {
		JSONObject settings = widget.optJSONObject("settings");
		JSONArray jsonArrayForSummary = new JSONArray();
		if (settings != null) {
			JSONObject summary = settings.optJSONObject("summary");
			if (settings.has("summary") && summary.has("enabled") && summary.optBoolean("enabled")) {

				if (summary.has("list")) {
					JSONArray listArray = summary.getJSONArray("list");

					if (listArray.length() > 1) {
						for (int jj = 0; jj < listArray.length(); jj++) {

							JSONObject aggrObj = listArray.getJSONObject(jj);

							if (!aggrObj.has("aggregation")) {

								JSONArray measures = new JSONArray();
								JSONObject content = widget.optJSONObject("content");
								if (content != null) {
									JSONArray columns = content.optJSONArray("columnSelectedOfDataset");
									if (columns != null) {
										for (int i = 0; i < columns.length(); i++) {
											JSONObject column = columns.getJSONObject(i);
											if ("MEASURE".equalsIgnoreCase(column.getString("fieldType"))) {
												JSONObject measure = new JSONObject();
												measure.put("id", column.getString("alias"));
												measure.put("alias", column.getString("aliasToShow"));

												String formula = column.optString("formula");
												String name = formula.isEmpty() ? column.optString("name") : formula;
												if (column.has("formula")) {
													measure.put("formula", name);
												} else
													measure.put("columnName", name);

												measure.put("funct", column.getString("funcSummary"));

												boolean hidden = false;

												if (column.has("style")) {

													JSONObject style = column.optJSONObject("style");
													if (style != null) {

														String hideSummary = style.optString("hideSummary");

														if (hideSummary != null && !hideSummary.isEmpty() && hideSummary.equalsIgnoreCase("true")) {
															hidden = true;
														}

													}

												}
												if (!hidden)
													measures.put(measure);
											}
										}
									}
								}
								JSONObject summaryRow = new JSONObject();
								summaryRow.put("measures", measures);

								JSONObject dataset = widget.optJSONObject("dataset");
								if (dataset != null) {
									int dsId = dataset.getInt("dsId");
									summaryRow.put("dataset", dsId);
								}

								jsonArrayForSummary.put(summaryRow);

							} else {

								JSONArray measures = new JSONArray();
								JSONObject content = widget.optJSONObject("content");
								if (content != null) {
									JSONArray columns = content.optJSONArray("columnSelectedOfDataset");
									if (columns != null) {
										for (int i = 0; i < columns.length(); i++) {
											JSONObject column = columns.getJSONObject(i);
											if ("MEASURE".equalsIgnoreCase(column.getString("fieldType"))) {
												JSONObject measure = new JSONObject();
												measure.put("id", column.getString("alias"));
												measure.put("alias", column.getString("aliasToShow"));

												String formula = column.optString("formula");
												String name = formula.isEmpty() ? column.optString("name") : formula;
												if (column.has("formula")) {
													measure.put("formula", name);
												} else
													measure.put("columnName", name);

												measure.put("funct", aggrObj.get("aggregation"));

												boolean hidden = false;

												if (column.has("style")) {

													JSONObject style = column.optJSONObject("style");
													if (style != null) {

														String hideSummary = style.optString("hideSummary");

														if (hideSummary != null && !hideSummary.isEmpty() && hideSummary.equalsIgnoreCase("true")) {
															hidden = true;
														}

													}

												}
												if (!hidden)
													measures.put(measure);
											}
										}
									}
								}
								JSONObject summaryRow = new JSONObject();
								summaryRow.put("measures", measures);

								JSONObject dataset = widget.optJSONObject("dataset");
								if (dataset != null) {
									int dsId = dataset.getInt("dsId");
									summaryRow.put("dataset", dsId);
								}

								jsonArrayForSummary.put(summaryRow);

							}

						}
					} else {
						JSONArray measures = new JSONArray();
						JSONObject content = widget.optJSONObject("content");
						if (content != null) {
							JSONArray columns = content.optJSONArray("columnSelectedOfDataset");
							if (columns != null) {
								for (int i = 0; i < columns.length(); i++) {
									JSONObject column = columns.getJSONObject(i);
									if ("MEASURE".equalsIgnoreCase(column.getString("fieldType"))) {
										JSONObject measure = new JSONObject();
										measure.put("id", column.getString("alias"));
										measure.put("alias", column.getString("aliasToShow"));

										String formula = column.optString("formula");
										String name = formula.isEmpty() ? column.optString("name") : formula;
										if (column.has("formula")) {
											measure.put("formula", name);
										} else
											measure.put("columnName", name);

										measure.put("funct", column.getString("funcSummary"));

										boolean hidden = false;

										if (column.has("style")) {

											JSONObject style = column.optJSONObject("style");
											if (style != null) {

												String hideSummary = style.optString("hideSummary");

												if (hideSummary != null && !hideSummary.isEmpty() && hideSummary.equalsIgnoreCase("true")) {
													hidden = true;
												}

											}

										}
										if (!hidden)
											measures.put(measure);
									}
								}
							}
						}
						JSONObject summaryRow = new JSONObject();
						summaryRow.put("measures", measures);

						JSONObject dataset = widget.optJSONObject("dataset");
						if (dataset != null) {
							int dsId = dataset.getInt("dsId");
							summaryRow.put("dataset", dsId);
						}

						jsonArrayForSummary.put(summaryRow);
					}
				}
				return jsonArrayForSummary;
			}
		}
		return null;
	}

	private boolean getRealtimeFromTableWidget(int dsId, JSONObject configuration) throws JSONException {
		JSONObject dataset = getDataset(dsId, configuration);
		return !dataset.getBoolean("useCache");
	}

	private JSONObject getDataset(int dsId, JSONObject configuration) throws JSONException {
		JSONArray datasets = configuration.getJSONArray("datasets");
		for (int i = 0; i < datasets.length(); i++) {
			JSONObject dataset = (JSONObject) datasets.get(i);
			int id = dataset.getInt("dsId");
			if (id == dsId) {
				return dataset;
			}
		}
		return null;
	}

	private Map<Integer, NodeComparator> toComparatorMap(List<Map<String, Object>> sortKeyMap) {
		Map<Integer, NodeComparator> sortKeys = new HashMap<Integer, NodeComparator>();

		for (int s = 0; s < sortKeyMap.size(); s++) {
			Map<String, Object> sMap = sortKeyMap.get(s);
			NodeComparator nc = new NodeComparator();

			nc.setParentValue((String) sMap.get("parentValue"));
			nc.setMeasureLabel((String) sMap.get("measureLabel"));
			if (sMap.get("direction") != null) {
				nc.setDirection(Integer.valueOf(sMap.get("direction").toString()));
				sortKeys.put(Integer.valueOf(sMap.get("column").toString()), nc);
			}
		}
		return sortKeys;
	}

}
