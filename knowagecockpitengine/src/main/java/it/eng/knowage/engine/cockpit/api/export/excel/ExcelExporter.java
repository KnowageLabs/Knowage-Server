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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFAbstractError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.utils.ParamDefaultValue;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @authors Francesco Lucchi (francesco.lucchi@eng.it)
 */

public class ExcelExporter {

	static private Logger logger = Logger.getLogger(ExcelExporter.class);

	private final String outputType;
	private final String userUniqueIdentifier;
	private final Map<String, String[]> parameterMap;

	private final Map<String, String> i18nMessages;

	private final Map<String, JSONObject> actualSelectionMap;

	private final boolean exportWidget;

	public ExcelExporter(String outputType, String userUniqueIdentifier, Map<String, String[]> parameterMap) {
		this.outputType = outputType;
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.parameterMap = parameterMap;
		this.exportWidget = setExportWidget(parameterMap);

		Locale locale = getLocale(parameterMap);
		try {
			i18nMessages = DAOFactory.getI18NMessageDAO().getAllI18NMessages(locale);
			LogMF.debug(logger, "Loaded messages [{0}]", i18nMessages);
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while retrieving the I18N messages", e);
		}

		this.actualSelectionMap = new HashMap<>();
	}

	private boolean setExportWidget(Map<String, String[]> parameterMap) {
		boolean exportWidgetOnly = false;
		if (parameterMap.containsKey("exportWidget")) {
			String parameter = parameterMap.get("exportWidget")[0];
			exportWidgetOnly = Boolean.valueOf(parameter);
		}
		return exportWidgetOnly;
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

	private String getI18NMessage(String code) {
		if (!i18nMessages.containsKey(code)) {
			return code;
		}
		return i18nMessages.get(code);
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

	public byte[] getBinaryData(Integer documentId, String documentLabel, String templateString) {
		if (templateString == null) {
			ObjTemplate template;
			try {
				template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(documentId);
				if (template == null) {
					throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
				}
				templateString = new String(template.getContent());
			} catch (EMFAbstractError e) {
				throw new SpagoBIRuntimeException("Unable to get template for document with id [" + documentId + "]");
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

		if (exportWidget) {
			String widgetId = parameterMap.get("widget")[0];
			exportWidget(templateString, widgetId, wb);
		} else {
			/**
			 * Old implementation. ExcelSheet class creates a grid of Data where all records are represented as Strings even if they are suppose to be numbers
			 */
			// ExcelSheet[] excelSheets = getExcelSheets(templateString);
			// if (excelSheets != null) {
			// importCsvData(excelSheets, wb);
			// }

			/**
			 * New implementation - consider Data types when exporting global cockpit document
			 */
			// TODO: Implement logic for DataReader, now everything stays in memory - bad when widget we have large number of Data
			List<JSONObject> excelSheets = getExcelSheetsList(templateString);
			if (!excelSheets.isEmpty()) {
				exportWidgetsToExcel(excelSheets, wb);
			}
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

	private void exportWidget(String templateString, String widgetId, Workbook wb) {
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			if (widget != null) {
				String widgetName = null;
				JSONObject content = widget.optJSONObject("content");
				if (content != null) {
					widgetName = content.getString("name");
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

			String[] cockpitSelections = parameterMap.get("COCKPIT_SELECTIONS");
			JSONObject body = new JSONObject(cockpitSelections[0]);
			datastore = getDatastore(datasetLabel, map, body.toString());
		} catch (Exception e) {
			logger.error("Cannot get Datastore for widget", e);
		}
		return datastore;
	}

	private void createExcelFile(JSONObject dataStore, Workbook wb, String widgetName) {
		CreationHelper createHelper = wb.getCreationHelper();
		try {
			JSONObject metadata = dataStore.getJSONObject("metaData");
			JSONArray columns = metadata.getJSONArray("fields");
			columns = filterDataStoreColumns(columns);
			JSONArray rows = dataStore.getJSONArray("rows");

			Sheet sheet;
			Row header = null;
			if (exportWidget) {
				if (widgetName != null && !widgetName.isEmpty()) {
					sheet = wb.createSheet(widgetName);
				} else {
					sheet = wb.createSheet("Data");
				}
				// Create HEADER - Column Names
				header = sheet.createRow((short) 0); // first row
			} else {
				String sheetName = dataStore.getString("sheetName");
				sheet = wb.createSheet(sheetName);
				// First row for Widget name in case exporting whole Cockpit document
				Row firstRow = sheet.createRow((short) 0);
				Cell firstCell = firstRow.createCell(0);
				firstCell.setCellValue(widgetName);
				header = sheet.createRow((short) 1);
			}

			// Fill Header
			for (int i = 0; i < columns.length(); i++) {
				JSONObject column = columns.getJSONObject(i);
				String columnName = column.getString("header");
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
					row = sheet.createRow(r + 1); // starting from second row, because the 0th (first) is Header
				else
					row = sheet.createRow(r + 2);

				for (int c = 0; c < columns.length(); c++) {
					JSONObject column = columns.getJSONObject(c);
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
							if(!s.trim().isEmpty()) {
								cell.setCellValue(Double.parseDouble(s));
							}
							cell.setCellStyle(intCellStyle);
							break;
						case "float":
							if(!s.trim().isEmpty()) {
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

	/**
	 * Global export. Table and Chart widgets
	 *
	 * @param templateString
	 * @return
	 */
	private ExcelSheet[] getExcelSheets(String templateString) {
		List<ExcelSheet> sheets = new ArrayList<>(0);
		try {
			JSONObject template = new JSONObject(templateString);
			/**
			 * Disabled functionality for including Datasets to be exported when exporting Cockpit document. getCsvsFromDatasets(String template)
			 */
			// sheets.addAll(getCsvsFromDatasets(template));
			sheets.addAll(getCsvsFromWidgets(template));
		} catch (JSONException | EMFUserError e) {
			logger.error("Unable to load template", e);
		}

		return sheets.toArray(new ExcelSheet[0]);
	}

	private List<JSONObject> getExcelSheetsList(String templateString) {
		List<JSONObject> sheets = new ArrayList<>(0);
		try {
			JSONObject template = new JSONObject(templateString);
			sheets.addAll(getDatastoresByWidget(template));
		} catch (Exception e) {
			logger.error("Unable to load template", e);
		}
		return sheets;
	}

	private List<JSONObject> getDatastoresByWidget(JSONObject template) throws JSONException, EMFUserError {
		logger.debug("IN");
		JSONObject configuration = template.getJSONObject("configuration");
		JSONArray sheets = template.getJSONArray("sheets");

		loadCockpitSelections(configuration);

		List<JSONObject> excelSheets = new ArrayList<>();

		for (int i = 0; i < sheets.length(); i++) {
			JSONObject sheet = sheets.getJSONObject(i);
			int sheetIndex = sheet.getInt("index");

			JSONArray widgets = sheet.getJSONArray("widgets");
			int widgetCounter = 0;
			for (int j = 0; j < widgets.length(); j++) {
				JSONObject widget = widgets.getJSONObject(j);
				String widgetType = widget.getString("type");

				if ("table".equals(widgetType) || "chart".equals(widgetType)) {
					JSONObject datasetObj = widget.getJSONObject("dataset");
					int datasetId = datasetObj.getInt("dsId");
					IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
					String datasetLabel = dataset.getLabel();

					JSONObject body = new JSONObject();

					JSONObject aggregations = getAggregationsFromWidget(widget, configuration);
					logger.debug("aggregations = " + aggregations);
					body.put("aggregations", aggregations);

					JSONObject parameters = getParametersFromWidget(widget, configuration);
					logger.debug("parameters = " + parameters);
					body.put("parameters", parameters);

					JSONObject summaryRow = getSummaryRowFromWidget(widget);
					if (summaryRow != null) {
						logger.debug("summaryRow = " + summaryRow);
						body.put("summaryRow", summaryRow);
					}

					JSONObject likeSelections = getLikeSelectionsFromWidget(widget, configuration);
					if (likeSelections != null) {
						logger.debug("likeSelections = " + likeSelections);
						body.put("likeSelections", likeSelections);
					}

					JSONObject selections = getSelectionsFromWidget(widget, configuration);
					logger.debug("selections = " + selections);
					body.put("selections", selections);

					Map<String, Object> map = new java.util.HashMap<String, Object>();

					if (getRealtimeFromTableWidget(datasetId, configuration)) {
						logger.debug("nearRealtime = true");
						map.put("nearRealtime", true);
					}

					int limit = getLimitFromTableWidget(widget);
					if (limit > 0) {
						logger.debug("limit = " + limit);
						map.put("limit", limit);
					}

					JSONObject datastoreObj = getDatastore(datasetLabel, map, body.toString());
					String sheetName = getI18NMessage("Widget") + " " + (sheetIndex + 1) + "." + (++widgetCounter);
					datastoreObj.put("sheetName", sheetName);
					JSONObject content = widget.optJSONObject("content");
					String widgetName = null;
					if (content != null) {
						widgetName = content.getString("name");
					}
					datastoreObj.put("widgetName", widgetName);
					excelSheets.add(datastoreObj);
				}
			}
		}

		logger.debug("OUT");
		return excelSheets;
	}

	private void exportWidgetsToExcel(List<JSONObject> excelSheets, Workbook wb) {
		Iterator<JSONObject> it = excelSheets.iterator();
		while (it.hasNext()) {
			JSONObject widgetDatastore = it.next();
			String widgetName = widgetDatastore.optString("widgetName");
			if (widgetName == null || widgetName.isEmpty()) {
				widgetName = "Widget";
			}
			createExcelFile(widgetDatastore, wb, widgetName);
		}
	}

	private List<ExcelSheet> getCsvsFromDatasets(JSONObject template) throws JSONException, EMFUserError, UnsupportedEncodingException {
		logger.debug("IN");

		JSONObject configuration = template.getJSONObject("configuration");
		JSONArray datasetsObj = configuration.getJSONArray("datasets");

		List<ExcelSheet> excelSheets = new ArrayList<>(datasetsObj.length());
		for (int i = 0; i < datasetsObj.length(); i++) {
			JSONObject datasetObj = datasetsObj.getJSONObject(i);
			int datasetId = datasetObj.getInt("dsId");
			IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
			String datasetLabel = dataset.getLabel();

			JSONObject body = new JSONObject();

			JSONObject parameters = datasetObj.getJSONObject("parameters");
			logger.debug("parameters = " + parameters);
			body.put("parameters", getReplacedParameters(parameters, datasetId));

			JSONObject aggregations = getAggregationsFromDataset(dataset);
			logger.debug("aggregations = " + aggregations);
			body.put("aggregations", aggregations);

			Map<String, Object> map = new java.util.HashMap<String, Object>();

			if (getRealtimeFromTableWidget(datasetId, configuration)) {
				logger.debug("nearRealtime = true");
				map.put("nearRealtime", true);
			}

			JSONObject datastoreObj = getDatastore(datasetLabel, map, body.toString());
			List<String[]> table = getTable(datastoreObj);
			excelSheets.add(new ExcelSheet(datasetLabel, table));
		}

		logger.debug("OUT");
		return excelSheets;
	}

	private JSONObject getAggregationsFromDataset(IDataSet dataset) throws JSONException {
		JSONObject aggregations = new JSONObject();

		JSONArray categories = new JSONArray();
		aggregations.put("categories", categories);

		IMetaData metadata = dataset.getMetadata();
		for (int i = 0; i < metadata.getFieldCount(); i++) {
			JSONObject category = new JSONObject();
			String alias = metadata.getFieldAlias(i);
			category.put("id", alias);
			category.put("alias", alias);
			category.put("columnName", metadata.getFieldName(i));
			category.put("orderType", "");

			categories.put(category);
		}

		JSONArray measures = new JSONArray();
		aggregations.put("measures", measures);

		aggregations.put("dataset", dataset.getLabel());

		return aggregations;
	}

	private List<ExcelSheet> getCsvsFromWidgets(JSONObject template) throws JSONException, EMFUserError {
		logger.debug("IN");

		JSONObject configuration = template.getJSONObject("configuration");
		JSONArray sheets = template.getJSONArray("sheets");

		loadCockpitSelections(configuration);

		List<ExcelSheet> excelSheets = new ArrayList<>();

		for (int i = 0; i < sheets.length(); i++) {
			JSONObject sheet = sheets.getJSONObject(i);
			int sheetIndex = sheet.getInt("index");

			JSONArray widgets = sheet.getJSONArray("widgets");
			int tableWidgetCounter = 0;
			for (int j = 0; j < widgets.length(); j++) {
				JSONObject widget = widgets.getJSONObject(j);
				String widgetType = widget.getString("type");

				if ("table".equals(widgetType) || "chart".equals(widgetType)) {
					JSONObject datasetObj = widget.getJSONObject("dataset");
					int datasetId = datasetObj.getInt("dsId");
					IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
					String datasetLabel = dataset.getLabel();

					JSONObject body = new JSONObject();

					JSONObject aggregations = getAggregationsFromWidget(widget, configuration);
					logger.debug("aggregations = " + aggregations);
					body.put("aggregations", aggregations);

					JSONObject parameters = getParametersFromWidget(widget, configuration);
					logger.debug("parameters = " + parameters);
					body.put("parameters", parameters);

					JSONObject summaryRow = getSummaryRowFromWidget(widget);
					if (summaryRow != null) {
						logger.debug("summaryRow = " + summaryRow);
						body.put("summaryRow", summaryRow);
					}

					JSONObject likeSelections = getLikeSelectionsFromWidget(widget, configuration);
					if (likeSelections != null) {
						logger.debug("likeSelections = " + likeSelections);
						body.put("likeSelections", likeSelections);
					}

					JSONObject selections = getSelectionsFromWidget(widget, configuration);
					logger.debug("selections = " + selections);
					body.put("selections", selections);

					Map<String, Object> map = new java.util.HashMap<String, Object>();

					if (getRealtimeFromTableWidget(datasetId, configuration)) {
						logger.debug("nearRealtime = true");
						map.put("nearRealtime", true);
					}

					int limit = getLimitFromTableWidget(widget);
					if (limit > 0) {
						logger.debug("limit = " + limit);
						map.put("limit", limit);
					}

					JSONObject datastoreObj = getDatastore(datasetLabel, map, body.toString());
					List<String[]> table = getTable(datastoreObj, widget);
					String sheetName = getI18NMessage("Widget") + " " + (sheetIndex + 1) + "." + (++tableWidgetCounter);
					excelSheets.add(new ExcelSheet(sheetName, table));
				}
			}
		}

		logger.debug("OUT");
		return excelSheets;
	}

	private void loadCockpitSelections(JSONObject configuration) throws JSONException {
		String[] cockpitSelections = parameterMap.get("COCKPIT_SELECTIONS");
		if (cockpitSelections != null && cockpitSelections.length == 1) {
			JSONArray configDatasets = configuration.getJSONArray("datasets");

			JSONObject paramDatasets = new JSONObject();
			JSONArray paramNearRealtime = new JSONArray();
			for (int i = 0; i < configDatasets.length(); i++) {
				JSONObject dataset = configDatasets.getJSONObject(i);
				String label = dataset.getString("dsLabel");
				JSONObject parameters = dataset.getJSONObject("parameters");
				paramDatasets.put(label, parameters);

				if (!dataset.getBoolean("useCache")) {
					paramNearRealtime.put(label);
				}
			}

			JSONObject cs = new JSONObject(cockpitSelections[0]);
			loadAggregationsFromCockpitSelections(paramDatasets, paramNearRealtime, cs);
			loadFiltersFromCockpitSelections(cs);
		} else {
			logger.warn("Unable to load cockpit selections");
		}
	}

	private void loadAggregationsFromCockpitSelections(JSONObject paramDatasets, JSONArray paramNearRealtime, JSONObject cs) throws JSONException {
		JSONArray aggregations = cs.getJSONArray("aggregations");
		for (int i = 0; i < aggregations.length(); i++) {
			JSONObject aggregation = aggregations.getJSONObject(i);
			JSONObject selections = aggregation.getJSONObject("selection");
			if (selections != null && selections.names() != null && selections.names().length() > 0) {
				aggregation.remove("selection");

				JSONObject associativeSelectionsPayload = new JSONObject();
				associativeSelectionsPayload.put("associationGroup", aggregation);
				associativeSelectionsPayload.put("selections", selections);
				associativeSelectionsPayload.put("datasets", paramDatasets);
				associativeSelectionsPayload.put("nearRealtime", paramNearRealtime);

				AssociativeSelectionsClient client = new AssociativeSelectionsClient();
				try {
					JSONObject associativeSelections = client.getAssociativeSelections(new HashMap<String, Object>(), userUniqueIdentifier,
							associativeSelectionsPayload.toString());

					JSONArray datasetLabels = aggregation.getJSONArray("datasets");
					for (int j = 0; j < datasetLabels.length(); j++) {
						String label = datasetLabels.getString(j);
						actualSelectionMap.put(label, associativeSelections.getJSONObject(label));
					}
				} catch (Exception e) {
					logger.error("Unable to load associative selection", e);
				}
			}
		}
	}

	private void loadFiltersFromCockpitSelections(JSONObject cs) throws JSONException {
		JSONObject filters = cs.getJSONObject("filters");
		if (filters != null) {
			JSONArray datasets = filters.names();
			if (datasets != null) {
				for (int i = 0; i < datasets.length(); i++) {
					String dataset = datasets.getString(i);
					JSONObject selection = filters.getJSONObject(dataset);
					Iterator<String> columns = selection.keys();
					while (columns.hasNext()) {
						String column = columns.next();
						Object values = selection.get(column);
						if (values instanceof JSONArray) {
							JSONArray array = (JSONArray) values;
							for (int j = 0; j < array.length(); j++) {
								array.put(j, "('" + array.getString(j) + "')");
							}
						} else if (values instanceof String) {
							JSONArray array = new JSONArray();
							array.put("('" + values + "')");
							selection.put(column, array);
						} else {
							throw new SpagoBIRuntimeException("Not recognised values [" + values + "]");
						}
					}
					actualSelectionMap.put(dataset, selection);
				}
			}
		}
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

	private List<String[]> getTable(JSONObject datastore) throws JSONException {
		List<String[]> table = new ArrayList<>();

		JSONObject metaData = datastore.getJSONObject("metaData");
		JSONArray fields = metaData.getJSONArray("fields");
		String[] columnMap = new String[fields.length() - 1];
		String[] headerRow = new String[fields.length() - 1];
		for (int i = 1; i < fields.length(); i++) {
			JSONObject field = fields.getJSONObject(i);
			String name = field.getString("name");
			String header = field.getString("header");
			columnMap[i - 1] = name;
			headerRow[i - 1] = header;
		}
		table.add(headerRow);

		JSONArray rows = datastore.getJSONArray("rows");
		for (int i = 0; i < rows.length(); i++) {
			String[] tableRow = new String[columnMap.length];
			JSONObject row = rows.getJSONObject(i);
			for (int j = 0; j < columnMap.length; j++) {
				String column = columnMap[j];
				String value = row.optString(column);
				if (value != null) {
					tableRow[j] = value;
				}
			}
			table.add(tableRow);
		}

		return table;
	}

	private List<String[]> getTable(JSONObject datastore, JSONObject widget) throws JSONException {
		List<String[]> table = new ArrayList<>();

		JSONObject content = widget.getJSONObject("content");
		String widgetName = content.getString("name");
		String[] titleRow = { widgetName };
		table.add(titleRow);

		JSONArray columns = content.getJSONArray("columnSelectedOfDataset");
		int columnCount = columns.length();
		List<String> headers = new ArrayList<String>(columnCount);

		String[] headerRow = new String[columnCount];
		for (int i = 0; i < columnCount; i++) {
			JSONObject column = columns.getJSONObject(i);
			String aliasToShow = column.optString("aliasToShow");
			if (aliasToShow != null && aliasToShow.isEmpty()) {
				aliasToShow = column.getString("alias");
			}
			headers.add(aliasToShow);
			aliasToShow = getI18NMessage(aliasToShow);
			headerRow[i] = aliasToShow;
		}
		table.add(headerRow);

		String[] columnMap = new String[columnCount];
		JSONObject metaData = datastore.getJSONObject("metaData");
		JSONArray fields = metaData.getJSONArray("fields");
		for (int i = 1; i < fields.length(); i++) {
			JSONObject field = fields.getJSONObject(i);
			String name = field.getString("name");
			String header = field.getString("header");
			int index = headers.indexOf(header);
			if (index > -1) {
				columnMap[index] = name;
			}
		}

		JSONArray rows = datastore.getJSONArray("rows");
		for (int i = 0; i < rows.length(); i++) {
			JSONObject row = rows.getJSONObject(i);
			String[] tableRow = new String[columnMap.length];
			for (int j = 0; j < columnMap.length; j++) {
				String column = columnMap[j];
				String value = row.optString(column);
				if (value != null) {
					tableRow[j] = value;
				}
			}
			table.add(tableRow);
		}

		return table;
	}

	private JSONObject getAggregationsFromWidget(JSONObject widget, JSONObject configuration) throws JSONException {
		JSONObject aggregations = new JSONObject();

		JSONArray measures = new JSONArray();
		aggregations.put("measures", measures);

		JSONArray categories = new JSONArray();
		aggregations.put("categories", categories);

		String sortingColumn = null;
		String sortingOrder = null;
		JSONObject settings = widget.optJSONObject("content");
		if (settings != null) {
			sortingColumn = settings.optString("sortingColumn");
			sortingOrder = settings.optString("sortingOrder");
		}

		boolean isSortingDefined = sortingColumn != null && !sortingColumn.isEmpty() && sortingOrder != null && !sortingOrder.isEmpty();
		boolean isSortingUsed = false;

		JSONObject content = widget.optJSONObject("content");
		if (content != null) {
			JSONArray columns = content.optJSONArray("columnSelectedOfDataset");
			if (columns != null) {
				for (int i = 0; i < columns.length(); i++) {
					JSONObject column = columns.getJSONObject(i);

					String aliasToShow = column.optString("aliasToShow");
					if (aliasToShow != null && aliasToShow.isEmpty()) {
						aliasToShow = column.getString("alias");
					}

					JSONObject categoryOrMeasure = new JSONObject();
					categoryOrMeasure.put("id", column.getString("alias"));
					categoryOrMeasure.put("alias", aliasToShow);
					categoryOrMeasure.put("columnName", column.getString("name"));
					if (isSortingDefined && sortingColumn.equals(column.getString("name"))) {
						categoryOrMeasure.put("orderType", sortingOrder);
						isSortingUsed = true;
					} else {
						categoryOrMeasure.put("orderType", "");
					}

					String fieldType = column.getString("fieldType");
					if ("ATTRIBUTE".equalsIgnoreCase(fieldType)) {
						categories.put(categoryOrMeasure);
					} else if ("MEASURE".equalsIgnoreCase(fieldType)) {
						categoryOrMeasure.put("funct", column.getString("aggregationSelected"));
						measures.put(categoryOrMeasure);
					} else {
						throw new SpagoBIRuntimeException("Unsupported field type");
					}
				}

				if (isSortingDefined && !isSortingUsed) {
					JSONObject category = new JSONObject();
					category.put("alias", sortingColumn);
					category.put("columnName", sortingColumn);
					category.put("id", sortingColumn);
					category.put("orderType", sortingOrder);
					categories.put(category);
				}
			}
		}

		JSONObject dataset = getDatasetFromWidget(widget, configuration);
		String datasetName = dataset.getString("name");
		aggregations.put("dataset", datasetName);

		return aggregations;
	}

	private JSONObject getParametersFromWidget(JSONObject widget, JSONObject configuration) throws JSONException {
		JSONObject dataset = getDatasetFromWidget(widget, configuration);
		JSONObject parameters = dataset.getJSONObject("parameters");

		Integer datasetId = dataset.getInt("dsId");
		return getReplacedParameters(parameters, datasetId);
	}

	private JSONObject getReplacedParameters(JSONObject parameters, Integer datasetId) throws JSONException {
		JSONObject newParameters = new JSONObject(parameters.toString());
		Map<String, String> newValues = new HashMap<>();
		Iterator<String> newParameterKeys = newParameters.keys();
		while (newParameterKeys.hasNext()) {
			String parameter = newParameterKeys.next();
			String value = newParameters.getString(parameter);
			String parameterRegex = "\\$P\\{(.*)\\}";
			Matcher parameterMatcher = Pattern.compile(parameterRegex).matcher(value);
			if (parameterMatcher.matches()) {
				String newValue = "";
				String parameterName = parameterMatcher.group(1);
				String[] parameterArray = parameterMap.get(parameterName);
				if (parameterArray != null && parameterArray.length > 0) {
					String parameterValue = parameterArray[0];
					String multiValueRegex = "\\{;\\{(.*)\\}(.*)\\}";
					Matcher multiValueMatcher = Pattern.compile(multiValueRegex).matcher(parameterValue);
					if (multiValueMatcher.matches()) {
						String[] split = multiValueMatcher.group(1).split(";");
						parameterValue = "'" + StringUtils.join(split, "','") + "'";
					}
					newValue = value.replaceAll(parameterRegex, parameterValue);
				} else {
					if (datasetId != null) {
						newValue = getParameterDefaultValue(datasetId, parameter);
					}
				}
				newValues.put(parameter, newValue);
			}
		}
		for (String parameter : newValues.keySet()) {
			String value = newValues.get(parameter);
			if (value != null) {
				newParameters.put(parameter, value);
			} else {
				newParameters.put(parameter, JSONObject.NULL);
			}
		}
		return newParameters;
	}

	private String getParameterDefaultValue(int datasetId, String parameter) {
		String newValue = null;
		try {
			IDataSet iDataSet = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
			if (iDataSet != null) {
				ParamDefaultValue paramDefaultValue = (ParamDefaultValue) iDataSet.getDefaultValues().get(parameter);
				if (paramDefaultValue != null) {
					newValue = paramDefaultValue.getDefaultValue();
				}
			}
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while retrieving dataset with id [" + datasetId + "]", e);
		}
		return newValue;
	}

	private JSONObject getSummaryRowFromWidget(JSONObject widget) throws JSONException {
		JSONObject settings = widget.optJSONObject("settings");
		if (settings != null) {
			JSONObject summary = settings.optJSONObject("summary");
			if (summary.optBoolean("enabled")) {
				JSONObject summaryRow = new JSONObject();

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
								measure.put("funct", column.getString("funcSummary"));
								measure.put("columnName", column.getString("name"));
								measures.put(measure);
							}
						}
					}
				}
				summaryRow.put("measures", measures);

				JSONObject dataset = widget.optJSONObject("dataset");
				if (dataset != null) {
					int dsId = dataset.getInt("dsId");
					summaryRow.put("dataset", dsId);
				}

				return summaryRow;
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

	private JSONObject getDataset(String dsLabel, JSONObject configuration) throws JSONException {
		JSONArray datasets = configuration.getJSONArray("datasets");
		for (int i = 0; i < datasets.length(); i++) {
			JSONObject dataset = (JSONObject) datasets.get(i);
			String label = dataset.getString("dsLabel");
			if (label.equals(dsLabel)) {
				return dataset;
			}
		}
		return null;
	}

	private JSONObject getDatasetFromWidget(JSONObject widget, JSONObject configuration) throws JSONException {
		JSONObject widgetDataset = widget.optJSONObject("dataset");
		if (widgetDataset != null) {
			int dsId = widgetDataset.getInt("dsId");
			JSONObject configurationDataset = getDataset(dsId, configuration);
			return configurationDataset;
		}
		return null;
	}

	private int getLimitFromTableWidget(JSONObject widget) throws JSONException {
		JSONObject limitRows = widget.optJSONObject("limitRows");
		if (widget.getString("type").equalsIgnoreCase("chart")) {
			JSONObject content = widget.optJSONObject("content");
			limitRows = content.optJSONObject("limitRows");
		}
		if (limitRows != null && limitRows.getBoolean("enable")) {
			return limitRows.getInt("rows");
		}
		return 0;
	}

	private JSONObject getLikeSelectionsFromWidget(JSONObject widget, JSONObject configuration) throws JSONException {
		JSONObject search = widget.optJSONObject("search");
		if (search != null) {
			String text = search.optString("text");
			JSONArray columns = search.optJSONArray("columns");
			if (text != null && !text.isEmpty() && columns != null && columns.length() > 0) {
				JSONObject obj = new JSONObject();
				obj.put(columns.join(","), text);

				JSONObject dataset = getDatasetFromWidget(widget, configuration);
				String datasetName = dataset.getString("name");

				JSONObject likeSelections = new JSONObject();
				likeSelections.put(datasetName, obj);
				return likeSelections;
			}
		}
		return null;
	}

	private JSONObject getSelectionsFromWidget(JSONObject widget, JSONObject configuration) throws JSONException {
		JSONObject dataset = getDatasetFromWidget(widget, configuration);
		String datasetName = dataset.getString("name");

		JSONObject selections = new JSONObject();
		JSONObject datasetFilters = new JSONObject();

		// get configuration filters
		JSONObject configurationFilters = configuration.optJSONObject("filters");
		if (configurationFilters != null) {
			JSONObject obj = configurationFilters.optJSONObject(datasetName);
			if (obj != null) {
				String[] names = JSONObject.getNames(obj);
				if (names != null) {
					for (int i = 0; i < names.length; i++) {
						String filter = names[i];
						JSONArray array = new JSONArray();
						array.put("('" + obj.get(filter) + "')");
						datasetFilters.put(filter, array);
					}
				}
			}
		}

		// get widget filters
		JSONArray widgetFilters = null;
		String widgetType = widget.getString("type");

		if (widgetType.equals("chart")) {
			JSONObject content = widget.getJSONObject("content");
			widgetFilters = content.optJSONArray("filters");
		} else {
			widgetFilters = widget.optJSONArray("filters");
		}

		if (widgetFilters != null) {
			for (int i = 0; i < widgetFilters.length(); i++) {
				JSONObject widgetFilter = widgetFilters.getJSONObject(i);
				JSONArray filterVals = widgetFilter.getJSONArray("filterVals");
				if (filterVals.length() > 0) {
					String colName = widgetFilter.getString("colName");

					JSONArray values = new JSONArray();
					for (int j = 0; j < filterVals.length(); j++) {
						Object filterVal = filterVals.get(j);
						values.put("('" + filterVal + "')");
					}

					String filterOperator = widgetFilter.getString("filterOperator");
					if (filterOperator != null) {
						JSONObject filter = new JSONObject();
						filter.put("filterOperator", filterOperator);
						filter.put("filterVals", values);
						datasetFilters.put(colName, filter);
					} else {
						datasetFilters.put(colName, values);
					}
				}
			}
		}

		if (actualSelectionMap.containsKey(datasetName)) {
			JSONObject actualSelections = actualSelectionMap.get(datasetName);
			Iterator<String> actualSelectionKeys = actualSelections.keys();
			while (actualSelectionKeys.hasNext()) {
				String key = actualSelectionKeys.next();
				Object values = actualSelections.get(key);
				datasetFilters.put(key, values);
			}
		}

		selections.put(datasetName, datasetFilters);
		return selections;
	}

	private void importCsvData(ExcelSheet[] excelSheets, Workbook wb) {
		for (int i = 0; i < excelSheets.length; i++) {
			ExcelSheet excelSheet = excelSheets[i];

			Sheet sheet = wb.createSheet(excelSheet.getLabel());

			List<String[]> table = excelSheet.getTable();
			for (int rowIndex = 0; rowIndex < table.size(); rowIndex++) {
				Row row = sheet.createRow(rowIndex);
				String[] columns = table.get(rowIndex);
				for (int columnIndex = 0; columnIndex < columns.length; columnIndex++) {
					Cell cell = row.createCell(columnIndex);
					cell.setCellValue(columns[columnIndex]);
				}
			}
		}
	}

}
