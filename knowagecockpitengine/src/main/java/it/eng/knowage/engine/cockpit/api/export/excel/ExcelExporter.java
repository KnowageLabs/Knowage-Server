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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
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
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.utils.ParamDefaultValue;
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
	private final Map<String, String[]> parameterMap;

	private final Map<String, String> i18nMessages;

	private final Map<String, JSONObject> actualSelectionMap;

	private final boolean exportWidget;

	private final JSONObject body;

	private Locale locale;

	// Old implementation with parameterMap
	public ExcelExporter(String outputType, String userUniqueIdentifier, Map<String, String[]> parameterMap) {
		this.outputType = outputType;
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.parameterMap = parameterMap;
		this.exportWidget = false;
		this.body = new JSONObject();

		Locale locale = getLocale(parameterMap);
		try {
			i18nMessages = DAOFactory.getI18NMessageDAO().getAllI18NMessages(locale);
			LogMF.debug(logger, "Loaded messages [{0}]", i18nMessages);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while retrieving the I18N messages", e);
		}

		this.actualSelectionMap = new HashMap<>();
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

		this.actualSelectionMap = new HashMap<>();
		this.parameterMap = new HashMap<>();
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

	public byte[] getBinaryDataPivot(Integer documentId, String documentLabel, String templateString, String options)
			throws JSONException, SerializationException {

		JSONObject optionsObj = new JSONObject(options);

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

		if (exportWidget) {
			try {
				String widgetId = String.valueOf(body.get("widget"));
				exportWidgetCrossTab(templateString, widgetId, wb, optionsObj);
			} catch (JSONException e) {
				logger.error("Cannot find widget in body request");
			}
		} else {
			// TODO: Implement logic for DataReader, now everything stays in memory - bad when widget have large number of Data
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

	public byte[] getBinaryData(Integer documentId, String documentLabel, String templateString) throws JSONException, SerializationException {
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

		if (exportWidget) {
			try {
				String widgetId = String.valueOf(body.get("widget"));
				exportWidget(templateString, widgetId, wb);
			} catch (JSONException e) {
				logger.error("Cannot find widget in body request");
			}
		} else {
			// TODO: Implement logic for DataReader, now everything stays in memory - bad when widget have large number of Data
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

				CrosstabXLSXExporter exporter = new CrosstabXLSXExporter(null);

				JSONObject crosstabDefinitionJo = optionsObj.getJSONObject("crosstabDefinition");
				JSONObject crosstabDefinitionConfigJo = crosstabDefinitionJo.optJSONObject(CrosstabSerializationConstants.CONFIG);
				JSONObject crosstabStyleJo = (optionsObj.isNull("style")) ? new JSONObject() : optionsObj.getJSONObject("style");
				crosstabDefinitionConfigJo.put("style", crosstabStyleJo);

				JSONObject crosstabDefinition = optionsObj.getJSONObject("crosstabDefinition");

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

				Map<Integer, NodeComparator> columnsSortKeysMap = toComparatorMap(columnsSortKeys);
				Map<Integer, NodeComparator> rowsSortKeysMap = toComparatorMap(rowsSortKeys);
				Map<Integer, NodeComparator> measuresSortKeysMap = toComparatorMap(measuresSortKeys);
				JSONObject styleJSON = (!optionsObj.isNull("style") ? optionsObj.getJSONObject("style") : new JSONObject());
				CrosstabBuilder builder = new CrosstabBuilder(locale, crosstabDefinition, optionsObj.getJSONArray("jsonData"),
						optionsObj.getJSONObject("metadata"), styleJSON);

				CrossTab cs = builder.getSortedCrosstabObj(columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, myGlobalId);

				Sheet sheet;

				widgetName = WorkbookUtil.createSafeSheetName(widgetName);
				sheet = wb.createSheet(widgetName);

				CreationHelper createHelper = wb.getCreationHelper();

				exporter.fillAlreadyCreatedSheet(sheet, cs, createHelper, 0, locale);

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

			JSONObject cockpitSelections = body.getJSONObject("COCKPIT_SELECTIONS");

			if (isSolrDataset(dataset) && !widget.getString("type").equalsIgnoreCase("discovery")) {

				JSONObject jsOptions = new JSONObject();
				jsOptions.put("solrFacetPivot", true);
				cockpitSelections.put("options", jsOptions);
			}

			JSONArray summaryRow = getSummaryRowFromWidget(widget, isSolrDataset(dataset));
			if (summaryRow != null) {
				logger.debug("summaryRow = " + summaryRow);
				cockpitSelections.put("summaryRow", summaryRow);
			}

			datastore = getDatastore(datasetLabel, map, cockpitSelections.toString());

			if (datastore != null) {
				logger.debug("datasetLabel: " + datasetLabel + " datastoreObj = " + datastore.toString());
			}

			datastore.put("widgetData", widget);
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

	private void createExcelFile(JSONObject dataStore, Workbook wb, String widgetName) throws JSONException, SerializationException {
		CreationHelper createHelper = wb.getCreationHelper();

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

			CrosstabBuilder builder = new CrosstabBuilder(locale, crosstabDefinitionJo, datastoreObjData.getJSONArray("rows"),
					datastoreObjData.getJSONObject("metaData"), styleJSON);

			CrossTab cs = builder.getSortedCrosstabObj(columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, myGlobalId);

			Sheet sheet;

			widgetName = WorkbookUtil.createSafeSheetName(widgetName);
			sheet = wb.createSheet(widgetName);

			exporter.fillAlreadyCreatedSheet(sheet, cs, createHelper, 0, locale);

		} else {
			try {
				JSONObject metadata = dataStore.getJSONObject("metaData");
				JSONArray columns = metadata.getJSONArray("fields");
				columns = filterDataStoreColumns(columns);
				JSONArray rows = dataStore.getJSONArray("rows");

				Sheet sheet;
				Row header = null;
				if (exportWidget) {
					widgetName = WorkbookUtil.createSafeSheetName(widgetName);
					sheet = wb.createSheet(widgetName);

					// Create HEADER - Column Names
					header = sheet.createRow((short) 0); // first row
				} else {
					String sheetName = "empty";
					if (dataStore.has("widgetName") && dataStore.getString("widgetName") != null && !dataStore.getString("widgetName").isEmpty()) {
						if (dataStore.has("sheetInfo")) {
							sheetName = dataStore.getString("sheetInfo").concat(".").concat(widgetName);
						} else {
							sheetName = widgetName;
						}
					}

					sheetName = WorkbookUtil.createSafeSheetName(sheetName);
					sheet = wb.createSheet(sheetName);
					// First row for Widget name in case exporting whole Cockpit document
					Row firstRow = sheet.createRow((short) 0);
					Cell firstCell = firstRow.createCell(0);
					firstCell.setCellValue(widgetName);
					header = sheet.createRow((short) 1);
				}

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

				// column.header matches with name or alias
				// Fill Header
				JSONArray columnsOrdered = new JSONArray();
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

										columnsOrdered.put(columnOld);
										break;
									} else if (columnOld.getString("header").equals(column.getString("aliasToShow"))) {
										columnsOrdered.put(columnOld);
										break;
									}
								} else {
									if (columnOld.getString("header").equals(column.getString("alias"))) {
										columnsOrdered.put(columnOld);
										break;
									} else if (columnOld.getString("header").equals(column.getString("aliasToShow"))) {
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
						row = sheet.createRow(r + 1); // starting from second row, because the 0th (first) is Header
					else
						row = sheet.createRow(r + 2);

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
				if ("static-pivot-table".equals(widgetType)) {
					JSONObject content = widget.getJSONObject("content");
					JSONObject crosstabDefinitionJo = content.getJSONObject("crosstabDefinition");
					JSONObject crosstabDefinitionConfigJo = crosstabDefinitionJo.optJSONObject(CrosstabSerializationConstants.CONFIG);
					JSONObject crosstabStyleJo = (content.isNull("style")) ? new JSONObject() : content.getJSONObject("style");
					crosstabDefinitionConfigJo.put("style", crosstabStyleJo);

					JSONObject datastoreObj = new JSONObject();

					datastoreObj.append("widget", widget);
					datastoreObj.append("widgetType", "static-pivot-table");
					datastoreObj.append("crosstabDefinition", crosstabDefinitionJo);
					datastoreObj.append("style", "style");

					String sheetName = getI18NMessage("Widget") + " " + (sheetIndex + 1) + "." + (++widgetCounter);
					datastoreObj.put("sheetName", sheetName);
					datastoreObj.put("widgetData", widget);

					String widgetName = null;
					if (content.has("style")) {
						JSONObject style = widget.optJSONObject("style");
						if (style.has("title")) {
							JSONObject title = style.optJSONObject("title");
							if (title.has("label")) {
								widgetName = title.getString("label") + "_" + widget.getString("id");
							}
						}

					}
					if (widgetName == null && content != null) {

						widgetName = content.getString("name");
					}
					datastoreObj.put("widgetName", widgetName);
					datastoreObj.put("sheetInfo", sheet.getString("label"));

					JSONObject datasetObj = widget.getJSONObject("dataset");
					int datasetId = datasetObj.getInt("dsId");
					IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
					String datasetLabel = dataset.getLabel();

					JSONObject body = new JSONObject();

					JSONObject aggregations = getAggregationsFromWidget(widget, configuration, widgetType);
					logger.debug("aggregations = " + aggregations);
					body.put("aggregations", aggregations);

					JSONObject parameters = getParametersFromWidget(widget, configuration);

					logger.debug("parameters = " + parameters);
					body.put("parameters", parameters);

					JSONArray summaryRow = getSummaryRowFromWidget(widget, isSolrDataset(dataset));
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
					if (body != null) {
						logger.debug("Export cockpit crosstab body.toString(): " + body.toString());
					}
					JSONObject datastoreObjData = getDatastore(datasetLabel, map, body.toString());

					datastoreObj.put("datastoreObjData", datastoreObjData);
					excelSheets.add(datastoreObj);

				} else if ("table".equals(widgetType) || "chart".equals(widgetType) || "advanced-table".equals(widgetType) || "discovery".equals(widgetType)) {
					JSONObject datasetObj = widget.getJSONObject("dataset");
					int datasetId = datasetObj.getInt("dsId");
					IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
					String datasetLabel = dataset.getLabel();

					JSONObject body = new JSONObject();

					JSONObject aggregations = getAggregationsFromWidget(widget, configuration, widgetType);
					logger.debug("aggregations = " + aggregations);
					body.put("aggregations", aggregations);

					JSONObject parameters = getParametersFromWidget(widget, configuration);

					logger.debug("parameters = " + parameters);
					body.put("parameters", parameters);

					JSONArray summaryRow = getSummaryRowFromWidget(widget, isSolrDataset(dataset));
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

					if (isSolrDataset(dataset) && !widget.getString("type").equalsIgnoreCase("discovery")) {

						JSONObject jsOptions = new JSONObject();
						jsOptions.put("solrFacetPivot", true);
						body.put("options", jsOptions);
					}
					if (body != null) {
						logger.debug("Export cockpit body.toString(): " + body.toString());
					}
					JSONObject datastoreObj = getDatastore(datasetLabel, map, body.toString());

					if (datastoreObj != null) {
						logger.debug("datasetLabel: " + datasetLabel + " datastoreObj = " + datastoreObj.toString());
					}

					String sheetName = getI18NMessage("Widget") + " " + (sheetIndex + 1) + "." + (++widgetCounter);
					datastoreObj.put("sheetName", sheetName);
					datastoreObj.put("widgetData", widget);
					JSONObject content = widget.optJSONObject("content");
					String widgetName = null;
					if (widget.has("style")) {
						JSONObject style = widget.optJSONObject("style");
						if (style.has("title")) {
							JSONObject title = style.optJSONObject("title");
							if (title.has("label")) {
								widgetName = title.getString("label") + "_" + widget.getString("id");
							}
						}

					}
					if (widgetName == null && content != null) {

						widgetName = content.getString("name");
					}
					datastoreObj.put("widgetName", widgetName);
					datastoreObj.put("sheetInfo", sheet.getString("label"));
					excelSheets.add(datastoreObj);
				}
			}
		}

		logger.debug("OUT");
		return excelSheets;
	}

	private void exportWidgetsToExcel(List<JSONObject> excelSheets, Workbook wb) throws JSONException, SerializationException {
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

	private void loadCockpitSelections(JSONObject configuration) throws JSONException {
		// String[] cockpitSelections = parameterMap.get("COCKPIT_SELECTIONS");
		JSONObject cockpitSelections = body.optJSONObject("COCKPIT_SELECTIONS");
		if (cockpitSelections != null) {
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

			loadAggregationsFromCockpitSelections(paramDatasets, paramNearRealtime, cockpitSelections);
			loadFiltersFromCockpitSelections(cockpitSelections);
		}

		else {
			logger.warn("Unable to load cockpit selections");
		}
	}

	private void loadAggregationsFromCockpitSelections(JSONObject paramDatasets, JSONArray paramNearRealtime, JSONObject cs) throws JSONException {
		JSONArray aggregations = cs.getJSONArray("aggregations");
		JSONArray cockpitSelectionsDatasetParameters = body.getJSONArray("parametersDataArray");
		for (int i = 0; i < aggregations.length(); i++) {
			JSONObject aggregation = aggregations.getJSONObject(i);
			JSONObject selections = aggregation.getJSONObject("selection");
			if (selections != null && selections.names() != null && selections.names().length() > 0) {
				// aggregation.remove("selection");

				JSONObject newParameters = new JSONObject();

				String objToChange = "";
				for (int j = 0; j < cockpitSelectionsDatasetParameters.length(); j++) {

					JSONObject jsonobject = cockpitSelectionsDatasetParameters.getJSONObject(j);

					Iterator<String> iterator = paramDatasets.keys();
					while (iterator.hasNext()) {
						String obj = iterator.next();
						String val = paramDatasets.getString(obj);
						String key = "p_" + jsonobject.getString("urlName");
						if (val.contains("$P{" + jsonobject.getString("urlName") + "}") || val.contains("$P{" + key + "}")) {
							objToChange = obj;
							if (!jsonobject.isNull("parameterValue")) {
								Object values = jsonobject.get("parameterValue");
								String valuesToChange = values.toString();
								if (jsonobject.has("type") && jsonobject.get("type").equals("DATE")) {

									DateTimeFormatter dateTime = ISODateTimeFormat.dateTime();
									DateTime parsedDateTime = dateTime.parseDateTime(valuesToChange);
									Date dateToconvert = parsedDateTime.toDate();
									SimpleDateFormat sdf = new SimpleDateFormat(
											SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"));
									valuesToChange = sdf.format(dateToconvert).toString();

								}
								if (valuesToChange != null && valuesToChange.length() > 0 && !valuesToChange.contains(",")) {
									valuesToChange = valuesToChange.replaceAll("\\[", "").replaceAll("\\]", "");
									valuesToChange = valuesToChange.replaceAll("\"", ""); // single value parameter
								}
								newParameters.put(obj, valuesToChange);
							}

							else {
								newParameters.put(obj, "");
							}

						} else if ((val != null && val.length() > 0) && (!val.contains("$P{"))) {
							newParameters.put(obj, val);
						}

					}

				}

				paramDatasets.put(objToChange, newParameters);

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

	private JSONObject getAggregationsFromWidget(JSONObject widget, JSONObject configuration, String widgetType) throws JSONException {
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

		JSONObject datasetObj = widget.getJSONObject("dataset");
		int datasetId = datasetObj.getInt("dsId");
		IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
		String datasetLabel = dataset.getLabel();
		boolean isSortingDefined = sortingColumn != null && !sortingColumn.isEmpty() && sortingOrder != null && !sortingOrder.isEmpty();
		boolean isSortingUsed = false;

		boolean isSolrDataset = isSolrDataset(dataset);
		JSONObject content = widget.optJSONObject("content");
		if (content != null) {

			widgetType = widget.getString("type");
			if ("static-pivot-table".equals(widgetType)) {

				JSONObject columns = content.getJSONObject("crosstabDefinition");
				JSONArray measuresJSON = columns.getJSONArray("measures");
				JSONArray rowsJSON = columns.getJSONArray("rows");
				JSONArray columnsJSON = columns.getJSONArray("columns");

				for (int i = 0; i < measuresJSON.length(); i++) { // looping over measures
					JSONObject measureObj = measuresJSON.getJSONObject(i);
					JSONObject measuress = new JSONObject();
					measuress.put("id", measureObj.getString("id"));
					measuress.put("alias", measureObj.getString("alias"));
					measuress.put("funct", measureObj.getString("funct"));
					measuress.put("orderType", "");
					measuress.put("columnName", measureObj.getString("id"));
					measures.put(measuress);
				}

				for (int i = 0; i < rowsJSON.length(); i++) { // looping over rows
					JSONObject attributesObj = rowsJSON.getJSONObject(i);
					JSONObject attributess = new JSONObject();
					attributess.put("id", attributesObj.getString("id"));
					attributess.put("alias", attributesObj.getString("alias"));
					attributess.put("orderType", "");
					attributess.put("columnName", attributesObj.getString("id"));
					categories.put(attributess);
				}

				for (int i = 0; i < columnsJSON.length(); i++) { // looping over columns
					JSONObject attributesObj = columnsJSON.getJSONObject(i);
					JSONObject attributess = new JSONObject();
					attributess.put("id", attributesObj.getString("id"));
					attributess.put("alias", attributesObj.getString("alias"));
					attributess.put("orderType", "");
					attributess.put("columnName", attributesObj.getString("id"));
					categories.put(attributess);
				}

			} else {

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

						String formula = column.optString("formula");
						String name = formula.isEmpty() ? column.optString("name") : formula;
						if (column.has("formula")) {
							categoryOrMeasure.put("columnName", aliasToShow);
							categoryOrMeasure.put("formula", formula);
						} else {
							categoryOrMeasure.put("columnName", name);
						}
						if (column.has("formula") && datasetLabel.equalsIgnoreCase("SolRDataset")) {
							categoryOrMeasure.put("datasetOrTableFlag", false);
						}
						if (isSortingDefined && column.has("name") && sortingColumn.equals(name)) {
							categoryOrMeasure.put("orderType", sortingOrder);
							isSortingUsed = true;
						} else {
							categoryOrMeasure.put("orderType", "");
						}
						if (column.has("facet")) {
							categoryOrMeasure.put("funct", column.getString("aggregationSelected"));
						}
						String fieldType = column.getString("fieldType");
						if ("ATTRIBUTE".equalsIgnoreCase(fieldType)) {

							if (isSolrDataset && !column.has("facet"))
								categoryOrMeasure.put("funct", "NONE");
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
		}
		JSONObject datasetJsn = getDatasetFromWidget(widget, configuration);
		String datasetName = datasetJsn.getString("name");
		aggregations.put("dataset", datasetName);

		return aggregations;

	}

	private JSONObject getParametersFromWidget(JSONObject widget, JSONObject configuration) throws JSONException {

		JSONArray cockpitSelectionsDatasetParameters = null;
		try {
			cockpitSelectionsDatasetParameters = body.getJSONArray("parametersDataArray");
		} catch (JSONException e) {
			logger.warn("No cockpit selections specified");
		}

		JSONObject dataset = getDatasetFromWidget(widget, configuration);
		JSONObject parameters = dataset.getJSONObject("parameters");
		String datasetName = dataset.getString("name");
		Integer datasetId = dataset.getInt("dsId");
		JSONObject newParameters = new JSONObject();
		for (int i = 0; i < parameters.length(); i++) {
			newParameters.put(parameters.names().getString(i), "");
		}
		if (actualSelectionMap.containsKey(datasetName)) {
			JSONObject actualSelections = actualSelectionMap.get(datasetName);
			Iterator<String> actualSelectionKeys = actualSelections.keys();

			while (actualSelectionKeys.hasNext()) {
				String key = actualSelectionKeys.next();
				if (key.contains("$")) {
					Object values = actualSelections.get(key);
					newParameters.put(key, values);
				}
			}
			JSONObject params = getReplacedAssociativeParameters(parameters, newParameters);
			newParameters = getReplacedParameters(params, datasetId);
		}
		if (cockpitSelectionsDatasetParameters != null && cockpitSelectionsDatasetParameters.length() > 0 && parameters.length() != 0) {

			for (int i = 0; i < cockpitSelectionsDatasetParameters.length(); i++) {

				JSONObject jsonobject = cockpitSelectionsDatasetParameters.getJSONObject(i);

				Iterator<String> iterator = parameters.keys();
				while (iterator.hasNext()) {
					String obj = iterator.next();
					String val = parameters.getString(obj);
					String key = jsonobject.getString("urlName");
					if (val.contains("$P{" + jsonobject.getString("urlName") + "}") || val.contains("$P{" + key + "}")) {
						if (!jsonobject.isNull("parameterValue")) {
							Object values = jsonobject.get("parameterValue");
							String valuesToChange = values.toString();
							if (jsonobject.has("type") && jsonobject.get("type").equals("DATE")) {

								DateTimeFormatter dateTime = ISODateTimeFormat.dateTime();
								DateTime parsedDateTime = dateTime.parseDateTime(valuesToChange);
								Date dateToconvert = parsedDateTime.toDate();
								SimpleDateFormat sdf = new SimpleDateFormat(SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"));
								valuesToChange = sdf.format(dateToconvert).toString();
							}
							if (valuesToChange != null && valuesToChange.length() > 0 && !valuesToChange.contains(",")) {
								valuesToChange = valuesToChange.replaceAll("\\[", "").replaceAll("\\]", "");
								valuesToChange = valuesToChange.replaceAll("\"", ""); // single value parameter
							}
							if (!(newParameters.length() != 0 && newParameters.has(key) && newParameters.getString(key).length() != 0))
								newParameters.put(obj, valuesToChange);
						} else {

							newParameters.put(obj, "");

						}
					} else if ((val != null && val.length() > 0) && (!val.contains("$P{"))) { // parameter already set in data configuration
						newParameters.put(obj, val);
					}

				}

			}

			return newParameters;
		} else
			return getReplacedParameters(parameters, datasetId);
	}

	private boolean isSolrDataset(IDataSet dataSet) {
		if (dataSet instanceof VersionedDataSet) {
			dataSet = ((VersionedDataSet) dataSet).getWrappedDataset();
		}
		return dataSet instanceof SolrDataSet;
	}

	private JSONObject getReplacedAssociativeParameters(JSONObject oldParameters, JSONObject newParameters) throws JSONException {
		JSONObject parameters = new JSONObject();
		Iterator<String> newParameterKeys = newParameters.keys();
		while (newParameterKeys.hasNext()) {
			String parameter = newParameterKeys.next();
			String regex = "\\$P\\{(.*)\\}";
			Matcher parameterMatcher = Pattern.compile(regex).matcher(parameter);
			if (parameterMatcher.matches()) {
				String parameterName = parameterMatcher.group(1);
				Object exists = oldParameters.get(parameterName);
				if (exists != null) {
					JSONArray value = (JSONArray) newParameters.get(parameter);
					String regex2 = "\\(\\'(.*)\\'\\)";
					Matcher parameterMatcher2 = Pattern.compile(regex2).matcher(value.get(0).toString());
					if (parameterMatcher2.matches()) {
						String realValue = parameterMatcher2.group(1);
						parameters.put(parameterName, realValue);
					}
				}
			}
		}
		return parameters;
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
				String parameterValue = body.optString(parameterName);
				if (parameterValue != null) {
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
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while retrieving dataset with id [" + datasetId + "]", e);
		}
		return newValue;
	}

	private JSONArray getSummaryRowFromWidget(JSONObject widget, boolean isSolrDataset) throws JSONException {
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
												if (column.has("datasetOrTableFlag")) {
													// calculated field case
													measure.put("datasetOrTableFlag", column.getBoolean("datasetOrTableFlag"));
												}
												if (column.has("datasetOrTableFlag") && !column.getBoolean("datasetOrTableFlag")) {
													// in case of table-level calculaated field and the measures have no aggregation set, on summary row it must
													// be
													// changed to
													// be SUM instead
													String formula = getSummaryRowFormula(column);
													measure.put("columnName", formula);
												} else {
													String formula = column.optString("formula");
													String name = formula.isEmpty() ? column.optString("name") : formula;
													measure.put("columnName", name);
												}
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
												if (column.has("datasetOrTableFlag")) {
													// calculated field case
													measure.put("datasetOrTableFlag", column.getBoolean("datasetOrTableFlag"));
												}
												if (column.has("datasetOrTableFlag") && !column.getBoolean("datasetOrTableFlag")) {
													// in case of table-level calculaated field and the measures have no aggregation set, on summary row it must
													// be
													// changed to
													// be SUM instead
													String formula = getSummaryRowFormula(column);
													measure.put("columnName", formula);
												} else {
													String formula = column.optString("formula");
													String name = formula.isEmpty() ? column.optString("name") : formula;
													measure.put("columnName", name);
												}
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
										if (column.has("datasetOrTableFlag")) {
											// calculated field case
											measure.put("datasetOrTableFlag", column.getBoolean("datasetOrTableFlag"));
										}
										if (column.has("datasetOrTableFlag") && !column.getBoolean("datasetOrTableFlag")) {
											// in case of table-level calculaated field and the measures have no aggregation set, on summary row it must be
											// changed
											// to
											// be SUM instead
											String formula = getSummaryRowFormula(column);
											measure.put("columnName", formula);
										} else {
											String formula = column.optString("formula");
											String name = formula.isEmpty() ? column.optString("name") : formula;
											measure.put("columnName", name);
										}
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

	private String getSummaryRowFormula(JSONObject column) throws JSONException {
		JSONArray formulaArray = column.getJSONArray("formulaArray");
		for (int i = 0; i < formulaArray.length(); i++) {
			JSONObject formulaComponent = formulaArray.getJSONObject(i);
			if (formulaComponent.getString("type").equals("measure") && formulaComponent.getString("aggregation").equals(AggregationFunctions.NONE)) {
				// in case the measure has no aggregation set, on summary row it must be changed to be SUM instead
				formulaComponent.put("aggregation", AggregationFunctions.SUM);
			}
		}
		return buildFormula(formulaArray);
	}

	private String buildFormula(JSONArray formulaArray) throws JSONException {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < formulaArray.length(); i++) {
			JSONObject formulaComponent = formulaArray.getJSONObject(i);
			if (formulaComponent.getString("type").equals("measure")) {
				builder.append(formulaComponent.getString("aggregation").equals(AggregationFunctions.NONE) ? "\"" + formulaComponent.getString("value") + "\""
						: formulaComponent.getString("aggregation") + "(\"" + formulaComponent.getString("value") + "\")");
			} else {
				builder.append(formulaComponent.getString("value"));
			}
			builder.append(" ");
		}
		return builder.toString();
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
				if (!key.contains("$")) {
					Object values = actualSelections.get(key);
					datasetFilters.put(key, values);
				}
			}
		}

		selections.put(datasetName, datasetFilters);
		return selections;
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
