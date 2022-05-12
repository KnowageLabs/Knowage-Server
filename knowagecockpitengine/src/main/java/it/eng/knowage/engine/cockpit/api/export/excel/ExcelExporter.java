/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.export.AbstractFormatExporter;
import it.eng.knowage.engine.cockpit.api.export.ExporterClient;
import it.eng.knowage.engine.cockpit.api.export.excel.exporters.IWidgetExporter;
import it.eng.knowage.engine.cockpit.api.export.excel.exporters.WidgetExporterFactory;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 * @author Marco Balestri (marco.balestri@eng.it)
 */

public class ExcelExporter extends AbstractFormatExporter {

	static private Logger logger = Logger.getLogger(ExcelExporter.class);

	public static final String UNIQUE_ALIAS_PLACEHOLDER = "_$_";

	private final boolean isSingleWidgetExport;
	private int uniqueId = 0;
	private String requestURL = "";

	private Map<String, CellStyle> format2CellStyle = new HashMap<String, CellStyle>();

	private static final String[] WIDGETS_TO_IGNORE = { "image", "text", "selector", "selection", "html" };
	private static final String SCRIPT_NAME = "cockpit-export-xls.js";
	private static final String CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH = "internal.nodejs.chromium.export.path";
	private static final int SHEET_NAME_MAX_LEN = 31;

	// used only for scheduled export
	public ExcelExporter(String outputType, String userUniqueIdentifier, Map<String, String[]> parameterMap, String requestURL) {
		super(userUniqueIdentifier, new JSONObject());
		this.isSingleWidgetExport = false;
		this.requestURL = requestURL;
	}

	public ExcelExporter(String outputType, String userUniqueIdentifier, JSONObject body) {
		super(userUniqueIdentifier, body);
		this.isSingleWidgetExport = body.optBoolean("exportWidget");
	}

	public String getMimeType() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}

	// used only for scheduled exports
	// leverages on an external script that uses chromium to open the cockpit and click on the export button
	public byte[] getBinaryData(String documentLabel) throws IOException, InterruptedException, EMFUserError {
		try {
			final Path outputDir = Files.createTempDirectory("knowage-xls-exporter-");

			String encodedUserId = Base64.encodeBase64String(userUniqueIdentifier.getBytes("UTF-8"));

			// Script
			String cockpitExportScriptPath = SingletonConfig.getInstance().getConfigValue(CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH);
			Path exportScriptFullPath = Paths.get(cockpitExportScriptPath, SCRIPT_NAME);

			if (!Files.isRegularFile(exportScriptFullPath)) {
				String msg = String.format("Cannot find export script at \"%s\": did you set the correct value for %s configuration?", exportScriptFullPath,
						CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH);
				IllegalStateException ex = new IllegalStateException(msg);
				logger.error(msg, ex);
				throw ex;
			}

			URI url = UriBuilder.fromUri(requestURL).replaceQueryParam("outputType_description", "HTML").replaceQueryParam("outputType", "HTML").build();

			ProcessBuilder processBuilder = new ProcessBuilder("node", exportScriptFullPath.toString(), encodedUserId, outputDir.toString(), url.toString());
			Process exec = processBuilder.start();
			exec.waitFor();
			// the script creates the resulting xls and saves it to outputFile
			Path outputFile = outputDir.resolve(documentLabel + ".xlsx");
			return getByteArrayFromFile(outputFile, outputDir);
		} catch (Exception e) {
			logger.error("Error during scheduled export execution", e);
			throw e;
		}
	}

	private byte[] getByteArrayFromFile(Path excelFile, Path outputDir) {
		try {
			FileInputStream fis = new FileInputStream(excelFile.toString());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			for (int readNum; (readNum = fis.read(buf)) != -1;) {
				// Writes len bytes from the specified byte array starting at offset off to this byte array output stream
				bos.write(buf, 0, readNum); // no doubt here is 0
			}
			fis.close();
			return bos.toByteArray();
		} catch (Exception e) {
			logger.error("Cannot serialize excel file", e);
			throw new SpagoBIRuntimeException("Cannot serialize excel file", e);
		} finally {
			try {
				if (Files.isRegularFile(excelFile))
					Files.delete(excelFile);
				Files.delete(outputDir);
			} catch (Exception e) {
				logger.error("Cannot delete temp file", e);
			}
		}
	}

	public byte[] getBinaryData(Integer documentId, String documentLabel, String templateString, String options) throws JSONException, SerializationException {
		if (templateString == null) {
			ObjTemplate template = null;
			String message = "Unable to get template for document with id [" + documentId + "] and label [" + documentLabel + "]";
			try {
				if (documentId != null && documentId.intValue() != 0)
					template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplate(documentId);
				else if (documentLabel != null && !documentLabel.isEmpty())
					template = DAOFactory.getObjTemplateDAO().getBIObjectActiveTemplateByLabel(documentLabel);

				if (template == null)
					throw new SpagoBIRuntimeException(message);

				templateString = new String(template.getContent());
			} catch (EMFAbstractError e) {
				throw new SpagoBIRuntimeException(message);
			}
		}

		int windowSize = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("KNOWAGE.DASHBOARD.EXPORT.EXCEL.STREAMING_WINDOW_SIZE"));
		try (Workbook wb = new SXSSFWorkbook(windowSize)) {

			int exportedSheets = 0;
			if (isSingleWidgetExport) {
				long widgetId = body.getLong("widget");
				String widgetType = getWidgetTypeFromCockpitTemplate(templateString, widgetId);
				JSONObject optionsObj = new JSONObject();
				if (options != null && !options.isEmpty())
					optionsObj = new JSONObject(options);
				IWidgetExporter widgetExporter = WidgetExporterFactory.getExporter(this, widgetType, templateString, widgetId, wb, optionsObj);
				exportedSheets = widgetExporter.export();
			} else {
				// export whole cockpit
				JSONArray widgetsJson = getWidgetsJson(templateString);
				JSONObject optionsObj = buildOptionsForCrosstab(templateString);
				exportedSheets = exportCockpit(templateString, widgetsJson, wb, optionsObj);
			}

			if (exportedSheets == 0)
				exportEmptyExcel(wb);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			out.flush();
			out.close();
			return out.toByteArray();
		} catch (IOException e) {
			throw new SpagoBIRuntimeException("Unable to generate output file", e);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot export data to excel", e);
		}
	}

	private JSONObject buildOptionsForCrosstab(String templateString) {
		try {
			JSONObject template = new JSONObject(templateString);
			JSONArray sheets = template.getJSONArray("sheets");
			JSONObject toReturn = new JSONObject();
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				JSONArray sheetWidgets = sheet.getJSONArray("widgets");
				for (int j = 0; j < sheetWidgets.length(); j++) {
					JSONObject widget = sheetWidgets.getJSONObject(j);
					if (!widget.getString("type").equals("static-pivot-table"))
						continue;
					long widgetId = widget.getLong("id");
					JSONObject options = new JSONObject();
					try {
						options.put("config", new JSONObject().put("type", "pivot"));
						options.put("sortOptions", widget.getJSONObject("content").getJSONObject("sortOptions"));
						options.put("name", widget.getJSONObject("content").getString("name"));
						options.put("crosstabDefinition", widget.getJSONObject("content").getJSONObject("crosstabDefinition"));
						options.put("style", widget.getJSONObject("content").getJSONObject("style"));
						// variables cannot be retrieved from template so we must recover them from request body
						options.put("variables", getCockpitVariables());
						ExporterClient client = new ExporterClient();
						int datasetId = widget.getJSONObject("dataset").getInt("dsId");
						String dsLabel = getDatasetLabel(template, datasetId);
						String selections = getCockpitSelectionsFromBody(widget).toString();
						JSONObject configuration = template.getJSONObject("configuration");
						Map<String, Object> parametersMap = new HashMap<String, Object>();
						if (getRealtimeFromWidget(datasetId, configuration))
							parametersMap.put("nearRealtime", true);
						JSONObject datastore = client.getDataStore(parametersMap, dsLabel, userUniqueIdentifier, selections);
						options.put("metadata", datastore.getJSONObject("metaData"));
						options.put("jsonData", datastore.getJSONArray("rows"));
						toReturn.put(String.valueOf(widgetId), options);
					} catch (Exception e) {
						logger.warn("Cannot build crosstab options for widget [" + widgetId + "]. Only raw data without formatting will be exported.", e);
					}
				}
			}
			return toReturn;
		} catch (Exception e) {
			logger.warn("Error while building crosstab options. Only raw data without formatting will be exported.", e);
			return new JSONObject();
		}
	}

	@Override
	protected JSONObject getCockpitSelectionsFromBody(JSONObject widget) {
		JSONObject cockpitSelections = new JSONObject();
		if (body == null || body.length() == 0)
			return cockpitSelections;
		try {
			if (isSingleWidgetExport) { // export single widget
				cockpitSelections = body.getJSONObject("COCKPIT_SELECTIONS");
			} else { // export whole cockpit
				JSONArray allWidgets = body.getJSONArray("widget");
				int i;
				for (i = 0; i < allWidgets.length(); i++) {
					JSONObject curWidget = allWidgets.getJSONObject(i);
					if (curWidget.getLong("id") == widget.getLong("id"))
						break;
				}
				cockpitSelections = body.getJSONArray("COCKPIT_SELECTIONS").getJSONObject(i);
			}
			forceUniqueHeaders(cockpitSelections);
		} catch (Exception e) {
			logger.error("Cannot get cockpit selections", e);
			return new JSONObject();
		}
		return cockpitSelections;
	}

	private String getDatasetLabel(JSONObject template, int dsId) {
		try {
			JSONArray cockpitDatasets = template.getJSONObject("configuration").getJSONArray("datasets");
			for (int i = 0; i < cockpitDatasets.length(); i++) {
				int currDsId = cockpitDatasets.getJSONObject(i).getInt("dsId");
				if (currDsId == dsId)
					return cockpitDatasets.getJSONObject(i).getString("dsLabel");
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot retrieve dataset label for dsId: " + dsId, e);
		}
		throw new SpagoBIRuntimeException("No dataset found with dsId: " + dsId);
	}

	private JSONArray getWidgetsJson(String templateString) {
		try {
			if (body != null && body.has("widget"))
				return body.getJSONArray("widget");
			else {
				JSONArray toReturn = new JSONArray();
				JSONObject template = new JSONObject(templateString);
				JSONArray sheets = template.getJSONArray("sheets");
				for (int i = 0; i < sheets.length(); i++) {
					JSONObject sheet = sheets.getJSONObject(i);
					JSONArray sheetWidgets = sheet.getJSONArray("widgets");
					for (int j = 0; j < sheetWidgets.length(); j++) {
						JSONObject widget = sheetWidgets.getJSONObject(j);
						toReturn.put(widget);
					}
				}
				return toReturn;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot retrieve widgets list", e);
		}
	}

	private int exportCockpit(String templateString, JSONArray widgetsJson, Workbook wb, JSONObject optionsObj) {
		String widgetId = null;
		int exportedSheets = 0;
		for (int i = 0; i < widgetsJson.length(); i++) {
			try {
				JSONObject currWidget = widgetsJson.getJSONObject(i);
				widgetId = currWidget.getString("id");
				String widgetType = currWidget.getString("type");
				if (Arrays.asList(WIDGETS_TO_IGNORE).contains(widgetType.toLowerCase()))
					continue;
				JSONObject currWidgetOptions = new JSONObject();
				if (optionsObj.has(widgetId))
					currWidgetOptions = optionsObj.getJSONObject(widgetId);
				IWidgetExporter widgetExporter = WidgetExporterFactory.getExporter(this, widgetType, templateString, Long.parseLong(widgetId), wb,
						currWidgetOptions);
				exportedSheets += widgetExporter.export();
			} catch (Exception e) {
				logger.error("Error while exporting widget [" + widgetId + "]", e);
			}
		}
		return exportedSheets;
	}

	private void exportEmptyExcel(Workbook wb) {
		if (wb.getNumberOfSheets() == 0) {
			Sheet sh = wb.createSheet();
			Row row = sh.createRow(0);
			Cell cell = row.createCell(0);
			cell.setCellValue("No data");
		}
	}

	public JSONArray getMultiDataStoreForWidget(JSONObject template, JSONObject widget) {
		Map<String, Object> map = new java.util.HashMap<String, Object>();
		JSONArray multiDataStore = new JSONArray();
		try {
			JSONObject configuration = template.getJSONObject("configuration");
			JSONArray datasetIds = widget.getJSONArray("datasetId");
			for (int i = 0; i < datasetIds.length(); i++) {
				int datasetId = datasetIds.getInt(i);
				IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
				String datasetLabel = dataset.getLabel();
				JSONObject cockpitSelections = getMultiCockpitSelectionsFromBody(widget, datasetId);
				if (isEmptyLayer(cockpitSelections))
					continue;

				if (getRealtimeFromWidget(datasetId, configuration))
					map.put("nearRealtime", true);

				JSONArray summaryRow = getSummaryRowFromWidget(widget);
				if (summaryRow != null)
					cockpitSelections.put("summaryRow", summaryRow);

				if (isSolrDataset(dataset)) {
					JSONObject jsOptions = new JSONObject();
					jsOptions.put("solrFacetPivot", true);
					cockpitSelections.put("options", jsOptions);
				}

				JSONObject dataStore = getDatastore(datasetLabel, map, cockpitSelections.toString());
				dataStore.put("widgetData", widget);
				multiDataStore.put(dataStore);
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to get multi datastore for map widget: ", e);
		}
		return multiDataStore;
	}

	private boolean isEmptyLayer(JSONObject cockpitSelections) {
		try {
			JSONObject aggregations = cockpitSelections.getJSONObject("aggregations");
			JSONArray measures = aggregations.getJSONArray("measures");
			JSONArray categories = aggregations.getJSONArray("categories");
			if (measures.length() > 0 || categories.length() > 0)
				return false;
			else
				return true;
		} catch (Exception e) {
			logger.warn("Error while checking if layer is empty", e);
			return false;
		}
	}

	public JSONObject getDataStoreForWidget(JSONObject template, JSONObject widget) {
		// if pagination is disabled offset = 0, fetchSize = -1
		return getDataStoreForWidget(template, widget, 0, -1);
	}

	private JSONObject getMultiCockpitSelectionsFromBody(JSONObject widget, int datasetId) {
		JSONObject cockpitSelections = new JSONObject();
		JSONArray allSelections = new JSONArray();
		try {
			if (body == null || body.length() == 0)
				return cockpitSelections;
			if (isSingleWidgetExport) { // export single widget with multi dataset
				allSelections = body.getJSONArray("COCKPIT_SELECTIONS");
				for (int i = 0; i < allSelections.length(); i++) {
					if (allSelections.getJSONObject(i).getInt("datasetId") == datasetId) {
						cockpitSelections = allSelections.getJSONObject(i);
					}
				}
			} else { // export whole cockpit
				JSONArray allWidgets = body.getJSONArray("widget");
				int i;
				for (i = 0; i < allWidgets.length(); i++) {
					JSONObject curWidget = allWidgets.getJSONObject(i);
					if (curWidget.getString("id").equals(widget.getString("id")))
						break;
				}
				allSelections = body.getJSONArray("COCKPIT_SELECTIONS").getJSONArray(i);
				for (int j = 0; j < allSelections.length(); j++) {
					if (allSelections.getJSONObject(j).getInt("datasetId") == datasetId) {
						cockpitSelections = allSelections.getJSONObject(j);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Cannot get cockpit selections", e);
			return new JSONObject();
		}
		return cockpitSelections;
	}

	public void createAndFillExcelSheet(JSONObject dataStore, Workbook wb, String widgetName, String cockpitSheetName) {
		Sheet newSheet = createUniqueSafeSheet(wb, widgetName, cockpitSheetName);
		fillSheetWithData(dataStore, wb, newSheet, widgetName, 0);
	}

	public void fillSheetWithData(JSONObject dataStore, Workbook wb, Sheet sheet, String widgetName, int offset) {
		try {
			JSONObject metadata = dataStore.getJSONObject("metaData");
			JSONArray columns = metadata.getJSONArray("fields");
			columns = filterDataStoreColumns(columns);
			JSONArray rows = dataStore.getJSONArray("rows");

			JSONObject widgetData = dataStore.getJSONObject("widgetData");
			JSONObject widgetContent = widgetData.getJSONObject("content");
			HashMap<String, String> arrayHeader = new HashMap<String, String>();
			HashMap<String, String> chartAggregationsMap = new HashMap<String, String>();
			if (widgetData.getString("type").equalsIgnoreCase("table")) {
				for (int i = 0; i < widgetContent.getJSONArray("columnSelectedOfDataset").length(); i++) {
					JSONObject column = widgetContent.getJSONArray("columnSelectedOfDataset").getJSONObject(i);
					String key;
					if (column.optBoolean("isCalculated") && !column.has("name")) {
						key = column.getString("alias");
					} else {
						key = column.getString("name");
					}
					arrayHeader.put(key, column.getString("aliasToShow"));
				}
			} else if (widgetData.getString("type").equalsIgnoreCase("chart")) {
				for (int i = 0; i < widgetContent.getJSONArray("columnSelectedOfDataset").length(); i++) {
					JSONObject column = widgetContent.getJSONArray("columnSelectedOfDataset").getJSONObject(i);
					if (column.has("aggregationSelected") && column.has("alias")) {
						String col = column.getString("alias");
						String aggregation = column.getString("aggregationSelected");
						chartAggregationsMap.put(col, aggregation);
					}
				}
			}

			// column.header matches with name or alias
			// Fill Header
			JSONArray groupsArray = new JSONArray();
			if (widgetData.has("groups")) {
				groupsArray = widgetData.getJSONArray("groups");
			}

			HashMap<String, String> mapGroupsAndColumns = new HashMap<String, String>();
			JSONArray columnsOrdered;
			if (widgetData.getString("type").equalsIgnoreCase("table") && widgetContent.has("columnSelectedOfDataset")) {
				hiddenColumns = getHiddenColumnsList(widgetContent.getJSONArray("columnSelectedOfDataset"));
				columnsOrdered = getTableOrderedColumns(widgetContent.getJSONArray("columnSelectedOfDataset"), columns);
			} else {
				columnsOrdered = columns;
			}

			try {
				mapGroupsAndColumns = getMapFromGroupsArray(groupsArray, widgetContent.getJSONArray("columnSelectedOfDataset"));
			} catch (JSONException e) {
				logger.error("Couldn't retrieve groups", e);
			}

			if (offset == 0) { // if pagination is active, headers must be created only once
				Row header = null;
				if (isSingleWidgetExport) { // export single widget
					header = createHeaderColumnNames(sheet, mapGroupsAndColumns, columnsOrdered, 0);
				} else { // export whole cockpit
					// First row is for Widget name in case exporting whole Cockpit document
					Row firstRow = sheet.createRow((short) 0);
					Cell firstCell = firstRow.createCell(0);
					firstCell.setCellValue(widgetName);
					header = createHeaderColumnNames(sheet, mapGroupsAndColumns, columnsOrdered, 1);
				}

				for (int i = 0; i < columnsOrdered.length(); i++) {
					JSONObject column = columnsOrdered.getJSONObject(i);
					String columnName = column.getString("header");
					String chartAggregation = null;
					if (widgetData.getString("type").equalsIgnoreCase("table") || widgetData.getString("type").equalsIgnoreCase("discovery")) {
						if (arrayHeader.get(columnName) != null) {
							columnName = arrayHeader.get(columnName);
						}
					} else if (widgetData.getString("type").equalsIgnoreCase("chart")) {
						chartAggregation = chartAggregationsMap.get(columnName);
						if (chartAggregation != null) {
							columnName = columnName.split("_" + chartAggregation)[0];
						}
					}

					columnName = getInternationalizedHeader(columnName);

					if (widgetData.getString("type").equalsIgnoreCase("chart") && chartAggregation != null) {
						columnName = columnName + "_" + chartAggregation;
					}

					Cell cell = header.createCell(i);
					cell.setCellValue(columnName);
				}
			}

			// Cell styles for int and float
			CreationHelper createHelper = wb.getCreationHelper();

			CellStyle intCellStyle = wb.createCellStyle();
			intCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("0"));

			CellStyle floatCellStyle = wb.createCellStyle();
			floatCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, getLocale());
			CellStyle dateCellStyle = wb.createCellStyle();
			dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(DATE_FORMAT));

			SimpleDateFormat timeStampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, getLocale());
			CellStyle tsCellStyle = wb.createCellStyle();
			tsCellStyle.setDataFormat(createHelper.createDataFormat().getFormat(TIMESTAMP_FORMAT));

			// cell styles for table widget
			JSONObject[] columnStyles = getColumnsStyles(columnsOrdered, widgetContent);

			// FILL RECORDS
			int isGroup = mapGroupsAndColumns.isEmpty() ? 0 : 1;
			for (int r = 0; r < rows.length(); r++) {
				JSONObject rowObject = rows.getJSONObject(r);
				Row row;
				if (isSingleWidgetExport)
					row = sheet.createRow((offset + r + isGroup) + 1); // starting from second row, because the 0th (first) is Header
				else
					row = sheet.createRow((offset + r + isGroup) + 2);

				for (int c = 0; c < columnsOrdered.length(); c++) {
					JSONObject column = columnsOrdered.getJSONObject(c);
					String type = getCellType(column, column.getString("name"), columnStyles[c]);
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
							cell.setCellStyle(getCellStyle(wb, createHelper, column, columnStyles[c], intCellStyle));
							break;
						case "float":
							if (!s.trim().isEmpty()) {
								cell.setCellValue(Double.parseDouble(s));
							}
							cell.setCellStyle(getCellStyle(wb, createHelper, column, columnStyles[c], floatCellStyle));
							break;
						case "date":
							try {
								if (!s.trim().isEmpty()) {
									Date date = dateFormat.parse(s);
									cell.setCellValue(date);
									cell.setCellStyle(dateCellStyle);
								}
							} catch (Exception e) {
								logger.debug("Date will be exported as string due to error: ", e);
								cell.setCellValue(s);
							}
							break;
						case "timestamp":
							try {
								if (!s.trim().isEmpty()) {
									Date ts = timeStampFormat.parse(s);
									cell.setCellValue(ts);
									cell.setCellStyle(tsCellStyle);
								}
							} catch (Exception e) {
								logger.debug("Timestamp will be exported as string due to error: ", e);
								cell.setCellValue(s);
							}
							break;
						default:
							cell.setCellValue(s);
							break;
						}
					}
				}
			}

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot write data to Excel file", e);
		}
	}

	private String getCellType(JSONObject column, String colName, JSONObject colStyle) {
		try {
			return column.getString("type");
		} catch (Exception e) {
			logger.error("Error while retrieving column {" + colName + "} type. It will be treated as string.", e);
			return "string";
		}
	}

	private boolean isAvoidSeparator(JSONObject colStyle) throws JSONException {
		if (colStyle != null && colStyle.has("asString")) {
			if (colStyle.getBoolean("asString")) {
				return true;
			}
		}
		return false;
	}

	private CellStyle getCellStyle(Workbook wb, CreationHelper helper, JSONObject column, JSONObject colStyle, CellStyle defaultStyle) {
		String colName = null;
		try {
			colName = column.getString("name");
			boolean isAvoidSeparator = isAvoidSeparator(colStyle);
			String format = null;
			if (isAvoidSeparator) {
				format = "0";
			} else {
				format = "#,##0";
			}
			// precision (i.e. number of digits to right of the decimal point) that is specified on dashboard design wins
			if ((colStyle != null && colStyle.has("precision")) || isAvoidSeparator) {
				int precision = (colStyle != null && colStyle.has("precision")) ? colStyle.getInt("precision") : 2;
				format = getNumberFormatByPrecision(precision, format);
				CellStyle toReturn = getCellStyleByFormat(wb, helper, format);
				return toReturn;
			}
			return defaultStyle;
		} catch (Exception e) {
			logger.error("Error while building column {" + colName + "} CellStyle. Default style will be used.", e);
			return defaultStyle;
		}
	}

	protected String getNumberFormatByPrecision(int precision, String initialFormat) {
		String format = initialFormat;
		if (precision > 0) {
			format += ".";
			for (int j = 0; j < precision; j++) {
				format += "0";
			}
		}
		return format;
	}

	/*
	 * This method avoids cell style objects number to increase by rows number (see https://production.eng.it/jira/browse/KNOWAGE-6692 and
	 * https://production.eng.it/jira/browse/KNOWAGE-6693)
	 */
	protected CellStyle getCellStyleByFormat(Workbook wb, CreationHelper helper, String format) {
		if (!format2CellStyle.containsKey(format)) {
			// if cell style does not exist
			CellStyle cellStyle = wb.createCellStyle();
			cellStyle.setDataFormat(helper.createDataFormat().getFormat(format));
			format2CellStyle.put(format, cellStyle);
		}
		return format2CellStyle.get(format);
	}

	private Row createHeaderColumnNames(Sheet sheet, Map<String, String> mapGroupsAndColumns, JSONArray columnsOrdered, int startRowOffset) {
		try {
			Row header = null;
			if (!mapGroupsAndColumns.isEmpty()) {
				Row newheader = sheet.createRow((short) startRowOffset);
				for (int i = 0; i < columnsOrdered.length(); i++) {
					JSONObject column = columnsOrdered.getJSONObject(i);
					String groupName = mapGroupsAndColumns.get(column.get("header"));
					if (groupName != null) {
						Cell cell = newheader.createCell(i);
						cell.setCellValue(groupName);
					}

				}
				header = sheet.createRow((short) (startRowOffset + 1));
			} else
				header = sheet.createRow((short) startRowOffset); // first row
			return header;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Couldn't create header column names", e);
		}
	}

	public Sheet createUniqueSafeSheet(Workbook wb, String widgetName, String cockpitSheetName) {
		Sheet sheet;
		String sheetName;
		try {
			if (!isSingleWidgetExport && cockpitSheetName != null && !cockpitSheetName.equals(""))
				sheetName = cockpitSheetName.concat(".").concat(widgetName);
			else
				sheetName = widgetName;
			String safeSheetName = WorkbookUtil.createSafeSheetName(sheetName);
			if (safeSheetName.length() + String.valueOf(uniqueId).length() > SHEET_NAME_MAX_LEN)
				safeSheetName = safeSheetName.substring(0, safeSheetName.length() - String.valueOf(uniqueId).length());
			String uniqueSafeSheetName = safeSheetName + String.valueOf(uniqueId);
			sheet = wb.createSheet(uniqueSafeSheetName);
			uniqueId++;
			return sheet;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Couldn't create sheet", e);
		}
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
		// if pagination is disabled offset = 0, fetchSize = -1
		return getDatastore(datasetLabel, map, selections, 0, -1);
	}

}