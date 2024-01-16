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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import it.eng.knowage.commons.multitenant.OrganizationImageManager;
import it.eng.knowage.engine.cockpit.api.export.AbstractFormatExporter;
import it.eng.knowage.engine.cockpit.api.export.ExporterClient;
import it.eng.knowage.engine.cockpit.api.export.excel.exporters.IWidgetExporter;
import it.eng.knowage.engine.cockpit.api.export.excel.exporters.WidgetExporterFactory;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 * @author Marco Balestri (marco.balestri@eng.it)
 */

public class ExcelExporter extends AbstractFormatExporter {

	private static final Logger LOGGER = LogManager.getLogger(ExcelExporter.class);
	private static final String[] WIDGETS_TO_IGNORE = { "image", "text", "selector", "selection", "html" };
	private static final String SCRIPT_NAME = "cockpit-export-xls.js";
	private static final String CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH = "internal.nodejs.chromium.export.path";
	private static final int SHEET_NAME_MAX_LEN = 31;

	public static final String UNIQUE_ALIAS_PLACEHOLDER = "_$_";

	private final boolean isSingleWidgetExport;
	private int uniqueId = 0;
	private String requestURL = "";

	private static final String INT_CELL_DEFAULT_FORMAT = "0";
	private static final String FLOAT_CELL_DEFAULT_FORMAT = "#,##0.00";

	private String documentName = "";
	private Map<String, Object> properties;

	// used only for scheduled export
	public ExcelExporter(String userUniqueIdentifier, Map<String, String[]> parameterMap, String requestURL) {
		super(userUniqueIdentifier, new JSONObject());
		this.isSingleWidgetExport = false;
		this.requestURL = requestURL;
		this.properties = new HashMap<String, Object>();
	}

	public ExcelExporter(String userUniqueIdentifier, JSONObject body) {
		super(userUniqueIdentifier, body);
		this.isSingleWidgetExport = body.optBoolean("exportWidget");
		this.properties = new HashMap<String, Object>();
	}

	public void setProperty(String propertyName, Object propertyValue) {
		this.properties.put(propertyName, propertyValue);
	}

	public Object getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}

	public String getMimeType() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}

	// used only for scheduled exports
	// leverages on an external script that uses chromium to open the cockpit and click on the export button
	public byte[] getBinaryData(String documentLabel) throws IOException, InterruptedException {
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
				LOGGER.error(msg, ex);
				throw ex;
			}

			URI url = UriBuilder.fromUri(requestURL).replaceQueryParam("outputType_description", "HTML").replaceQueryParam("outputType", "HTML").build();

			ProcessBuilder processBuilder = new ProcessBuilder("node", exportScriptFullPath.toString(), encodedUserId, outputDir.toString(), url.toString());

			setWorkingDirectory(cockpitExportScriptPath, processBuilder);

			LOGGER.info("Node complete command line: {}", processBuilder.command());

			LOGGER.info("Starting export script");
			Process exec = processBuilder.start();

			logOutputToCoreLog(exec);

			LOGGER.info("Waiting...");
			exec.waitFor();
			LOGGER.warn("Exit value: {}", exec.exitValue());

			// the script creates the resulting xls and saves it to outputFile
			Path outputFile = outputDir.resolve(documentLabel + ".xlsx");
			return getByteArrayFromFile(outputFile, outputDir);
		} catch (Exception e) {
			LOGGER.error("Error during scheduled export execution", e);
			throw e;
		}
	}

	private byte[] getByteArrayFromFile(Path excelFile, Path outputDir) {
		String fileName = excelFile.toString();

		try (FileInputStream fis = new FileInputStream(fileName); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			byte[] buf = new byte[1024];
			for (int readNum; (readNum = fis.read(buf)) != -1;) {
				// Writes len bytes from the specified byte array starting at offset off to this byte array output stream
				bos.write(buf, 0, readNum); // no doubt here is 0
			}
			fis.close();
			return bos.toByteArray();
		} catch (Exception e) {
			LOGGER.error("Cannot serialize excel file", e);
			throw new SpagoBIRuntimeException("Cannot serialize excel file", e);
		} finally {
			try {
				if (Files.isRegularFile(excelFile))
					Files.delete(excelFile);
				Files.delete(outputDir);
			} catch (Exception e) {
				LOGGER.error("Cannot delete temp file", e);
			}
		}
	}

	public byte[] getBinaryData(Integer documentId, String documentLabel, String documentName, String templateString, String options) throws JSONException {
		// set document name for exporting
		this.documentName = documentName;

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
				Map<String, Map<String, Object>> selectionsMap = new HashMap<>();
				try {
					selectionsMap = createSelectionsMap();
				} catch (JSONException e) {
					throw new SpagoBIRuntimeException("Unable to get selection map: ", e);
				}
				if (!selectionsMap.isEmpty()) {
					Sheet selectionsSheet = createUniqueSafeSheetForSelections(wb, "Active Selections");
					fillSelectionsSheetWithData(selectionsMap, wb, selectionsSheet, "Selections");
					exportedSheets++;
				}
				///
				Map<String, Map<String, Object>> driversMap = new HashMap<>();
				try {
					driversMap = createDriversMap();
				} catch (JSONException e) {
					throw new SpagoBIRuntimeException("Unable to get driver map: ", e);
				}
				if (!driversMap.isEmpty()) {
					Sheet driversSheet = createUniqueSafeSheetForDrivers(wb, "Filters");
					fillDriversSheetWithData(driversMap, wb, driversSheet, "Filters");
					exportedSheets++;
				}
			} else {
				// export whole cockpit
				JSONArray widgetsJson = getWidgetsJson(templateString);
				JSONObject optionsObj = buildOptionsForCrosstab(templateString);
				exportedSheets = exportCockpit(templateString, widgetsJson, wb, optionsObj);
			}

			if (exportedSheets == 0) {
				exportEmptyExcel(wb);
			} else {
				for (Sheet sheet : wb) {
					if (sheet != null) {
						// Adjusts the column width to fit the contents
						adjustColumnWidth(sheet);
					}
				}
			}

			byte[] ret = null;
			try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
				wb.write(out);
				out.flush();
				ret = out.toByteArray();
			}
			return ret;
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
						Map<String, Object> parametersMap = new HashMap<>();
						if (getRealtimeFromWidget(datasetId, configuration))
							parametersMap.put("nearRealtime", true);
						JSONObject datastore = client.getDataStore(parametersMap, dsLabel, userUniqueIdentifier, selections);
						options.put("metadata", datastore.getJSONObject("metaData"));
						options.put("jsonData", datastore.getJSONArray("rows"));
						toReturn.put(String.valueOf(widgetId), options);
					} catch (Exception e) {
						LOGGER.warn("Cannot build crosstab options for widget {}. Only raw data without formatting will be exported.", widgetId, e);
					}
				}
			}
			return toReturn;
		} catch (Exception e) {
			LOGGER.warn("Error while building crosstab options. Only raw data without formatting will be exported.", e);
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
			LOGGER.error("Cannot get cockpit selections", e);
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
				LOGGER.error("Error while exporting widget {}", widgetId, e);
			}
		}
		Map<String, Map<String, Object>> selectionsMap = new HashMap<>();
		try {
			selectionsMap = createSelectionsMap();
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Unable to get selection map: ", e);
		}
		if (!selectionsMap.isEmpty()) {
			Sheet selectionsSheet = createUniqueSafeSheetForSelections(wb, "Active Selections");
			fillSelectionsSheetWithData(selectionsMap, wb, selectionsSheet, "Selections");
			exportedSheets++;
		}

		//
		Map<String, Map<String, Object>> driversMap = new HashMap<>();
		try {
			driversMap = createDriversMap();
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Unable to get driver map: ", e);
		}
		if (!driversMap.isEmpty()) {
			Sheet driversSheet = createUniqueSafeSheetForSelections(wb, "Filters");
			fillDriversSheetWithData(driversMap, wb, driversSheet, "Filters");
			exportedSheets++;
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
		Map<String, Object> map = new java.util.HashMap<>();
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
			LOGGER.warn("Error while checking if layer is empty", e);
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
			LOGGER.error("Cannot get cockpit selections", e);
			return new JSONObject();
		}
		return cockpitSelections;
	}

	public void createAndFillExcelSheet(JSONObject dataStore, Workbook wb, String widgetName, String cockpitSheetName) {
		Sheet newSheet = createUniqueSafeSheet(wb, widgetName, cockpitSheetName);
		fillSheetWithData(dataStore, wb, newSheet, widgetName, 0, null);
	}

	private void fillSelectionsSheetWithData(Map<String, Map<String, Object>> selectionsMap, Workbook wb, Sheet sheet, String widgetName) {

		Row newheader = sheet.createRow((short) 0);
		Cell cell = newheader.createCell(0);
		cell.setCellValue("Dataset");
		Cell cell2 = newheader.createCell(1);
		cell2.setCellValue("Field");
		Cell cell3 = newheader.createCell(2);
		cell3.setCellValue("Values");

		int j = 1;
		for (String key : selectionsMap.keySet()) {

			for (String selectionskey : selectionsMap.get(key).keySet()) {

				Row row = sheet.createRow(j++);

				Cell cellData0 = row.createCell(0);
				cellData0.setCellValue(key);

				Cell cellData1 = row.createCell(1);
				cellData1.setCellValue(selectionskey);

				Cell cellData2 = row.createCell(2);

				cellData2.setCellValue(extractSelectionValues("" + selectionsMap.get(key).get(selectionskey)));

			}

		}

	}

	private void fillDriversSheetWithData(Map<String, Map<String, Object>> driversMap, Workbook wb, Sheet sheet, String widgetName) {

		Row newheader = sheet.createRow((short) 0);
		Cell cell = newheader.createCell(0);
		cell.setCellValue("Filter");
		Cell cell2 = newheader.createCell(1);
		cell2.setCellValue("Value");

		int j = 1;
		for (String key : driversMap.keySet()) {

			Row row = sheet.createRow(j++);

			Cell cellData0 = row.createCell(0);
			cellData0.setCellValue(key);

			Cell cellData1 = row.createCell(1);

			if (driversMap.get(key).keySet().contains("description")) {
				cellData1.setCellValue(extractSelectionValues("" + driversMap.get(key).get("description")));
			} else {
				cellData1.setCellValue(extractSelectionValues("" + driversMap.get(key).get("value")));
			}
		}

	}

	private String extractSelectionValues(String selectionValues) {
		return selectionValues.replace("[\"(", "").replace(")\"]", "");
	}

	public void fillSheetWithData(JSONObject dataStore, Workbook wb, Sheet sheet, String widgetName, int offset, JSONObject settings) {
		try {
			JSONObject metadata = dataStore.getJSONObject("metaData");
			JSONArray columns = metadata.getJSONArray("fields");
			columns = filterDataStoreColumns(columns);
			JSONArray rows = dataStore.getJSONArray("rows");
			HashMap<String, Object> variablesMap = new HashMap<>();
			JSONObject widgetData = dataStore.getJSONObject("widgetData");
			JSONObject widgetContent = widgetData.getJSONObject("content");
			HashMap<String, String> arrayHeader = new HashMap<>();
			HashMap<String, String> chartAggregationsMap = new HashMap<>();
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
						if (col.contains("$V")) {
							if (body.has("COCKPIT_VARIABLES")) {
								String columnAlias = "";
								Pattern patt = Pattern.compile("(\\$V\\{)([\\w\\s]+)(\\})");
								Matcher matcher = patt.matcher(col);
								if (body.get("COCKPIT_VARIABLES") instanceof JSONObject) {
									JSONObject variableOBJ = body.getJSONObject("COCKPIT_VARIABLES");
									while (matcher.find()) {
										columnAlias = matcher.group(2);
									}
									col = col.replace("$V{" + columnAlias + "}", variableOBJ.getString(columnAlias));
								} else {
									JSONArray arr = body.getJSONArray("COCKPIT_VARIABLES");
									for (int j = 0; j < arr.length(); j++) {
										JSONObject variableOBJ = arr.getJSONObject(j);
										while (matcher.find()) {
											columnAlias = matcher.group(2);
										}
										col = col.replace("$V{" + columnAlias + "}", variableOBJ.getString(columnAlias));
									}
								}
							}
						}
						chartAggregationsMap.put(col, aggregation);
					}
				}
			}

			JSONArray columnsOrdered;
			if (widgetData.getString("type").equalsIgnoreCase("table") && widgetContent.has("columnSelectedOfDataset")) {
				hiddenColumns = getHiddenColumnsList(widgetContent.getJSONArray("columnSelectedOfDataset"));
				columnsOrdered = getTableOrderedColumns(widgetContent.getJSONArray("columnSelectedOfDataset"), columns);
			} else {
				columnsOrdered = columns;
			}

			JSONArray groupsFromWidgetContent = getGroupsFromWidgetContent(widgetData);
			Map<String, String> groupsAndColumnsMap = getGroupAndColumnsMap(widgetContent, groupsFromWidgetContent);

			// CREATE BRANDED HEADER SHEET
			String imageB64 = OrganizationImageManager.getOrganizationB64Image(TenantManager.getTenant().getName());
			int startRow = 0;
			float rowHeight = 35; // in points
			int rowspan = 2;
			int startCol = 0;
			int colWidth = 25;
			int colspan = 2;
			int namespan = 10;
			int dataspan = 10;

			if (offset == 0) { // if pagination is active, headers must be created only once
				Row header = null;

				if (imageB64 != null) {

					for (int r = startRow; r < startRow + rowspan; r++) {
						sheet.createRow(r).setHeightInPoints(rowHeight);
						for (int c = startCol; c < startCol + colspan; c++) {
							sheet.getRow(r).createCell(c);
							sheet.setColumnWidth(c, colWidth * 256);
						}
					}

					// set brandend header image
					sheet.addMergedRegion(new CellRangeAddress(startRow, startRow + rowspan - 1, startCol, startCol + colspan - 1));
					drawBrandendHeaderImage(sheet, imageB64, Workbook.PICTURE_TYPE_PNG, startCol, startRow, colspan, rowspan);

					// set document name
					sheet.getRow(startRow).createCell(startCol + colspan).setCellValue(documentName);
					sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, startCol + colspan, namespan));
					// set cell style
					CellStyle documentNameCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 16);
					sheet.getRow(startRow).getCell(startCol + colspan).setCellStyle(documentNameCellStyle);

					// set date
					DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					Date date = new Date();
					sheet.getRow(startRow + 1).createCell(startCol + colspan).setCellValue("Data di generazione: " + dateFormat.format(date));
					sheet.addMergedRegion(new CellRangeAddress(startRow + 1, startRow + 1, startCol + colspan, dataspan));
					// set cell style
					CellStyle dateCellStyle = buildCellStyle(sheet, false, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 8);
					sheet.getRow(startRow + 1).getCell(startCol + colspan).setCellStyle(dateCellStyle);
				}

//				ATTENTION: exporting single widget must not be different from exporting whole cockpit
//				if (isSingleWidgetExport) { // export single widget
//					header = createHeaderColumnNames(sheet, groupsAndColumnsMap, columnsOrdered, 0);
//				} else { // export whole cockpit
//					// First row is for Widget name in case exporting whole Cockpit document
//					Row firstRow = sheet.createRow((short) 0);
//					Cell firstCell = firstRow.createCell(0);
//					firstCell.setCellValue(widgetName);
//					header = createHeaderColumnNames(sheet, groupsAndColumnsMap, columnsOrdered, 1);
//				}

				int headerIndex = (imageB64 != null) ? (startRow + rowspan) : 0;
				Row widgetNameRow = sheet.createRow((short) headerIndex);
				Cell widgetNameCell = widgetNameRow.createCell(0);
				widgetNameCell.setCellValue(widgetName);
				// set cell style
				CellStyle widgetNameStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 14);
				widgetNameCell.setCellStyle(widgetNameStyle);
				header = createHeaderColumnNames(sheet, groupsAndColumnsMap, columnsOrdered, headerIndex + 1);

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

					// set cell style
					CellStyle headerCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 11);
					cell.setCellStyle(headerCellStyle);
				}

				// adjusts the column width to fit the contents
				adjustColumnWidth(sheet);
			}

			// Cell styles for int and float
			CreationHelper createHelper = wb.getCreationHelper();

			DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, getLocale());

			SimpleDateFormat timeStampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, getLocale());

			// cell styles for table widget
			JSONObject[] columnStyles = new JSONObject[columnsOrdered.length() + 10];
			HashMap<String, String> mapColumns = new HashMap<>();
			HashMap<String, String> mapColumnsTypes = new HashMap<>();
			HashMap<String, Object> mapParameters = new HashMap<>();
			if (widgetData.getString("type").equalsIgnoreCase("table")) {
				columnStyles = getColumnsStyles(columnsOrdered, widgetContent);
				mapColumns = getColumnsMap(columnsOrdered);
				mapColumnsTypes = getColumnsMapTypes(columnsOrdered);
				mapParameters = createMapParameters(mapParameters);
			}
			variablesMap = createMapVariables(variablesMap);
			// FILL RECORDS
			int isGroup = groupsAndColumnsMap.isEmpty() ? 0 : 1;
			for (int r = 0; r < rows.length(); r++) {
				JSONObject rowObject = rows.getJSONObject(r);
				Row row;

//				if (isSingleWidgetExport)
//					row = sheet.createRow((offset + r + isGroup) + 1); // starting from second row, because the 0th (first) is Header
//				else
//					row = sheet.createRow((offset + r + isGroup) + 2);

				if (imageB64 != null)
					row = sheet.createRow((offset + r + isGroup) + (startRow + rowspan) + 2); // starting by Header
				else
					row = sheet.createRow((offset + r + isGroup) + 2);

				for (int c = 0; c < columnsOrdered.length(); c++) {
					JSONObject column = columnsOrdered.getJSONObject(c);
					String type = getCellType(column, column.getString("name"));
					String colIndex = column.getString("name"); // column_1, column_2, column_3...

					Cell cell = row.createCell(c);
					Object value = rowObject.get(colIndex);

					if (value != null) {
						String s = value.toString();
						switch (type) {
						case "string":
							cell.setCellValue(s);
							cell.setCellStyle(getStringCellStyle(wb, createHelper, column, columnStyles[c], FLOAT_CELL_DEFAULT_FORMAT, settings, s, rowObject,
									mapColumns, mapColumnsTypes, variablesMap, mapParameters));
							break;
						case "int":
							if (!s.trim().isEmpty()) {
								cell.setCellValue(Double.parseDouble(s));
								cell.setCellStyle(getIntCellStyle(wb, createHelper, column, columnStyles[c], INT_CELL_DEFAULT_FORMAT, settings,
										Integer.parseInt(s), rowObject, mapColumns, mapColumnsTypes, variablesMap, mapParameters));
							} else {
								cell.setCellStyle(getGenericCellStyle(wb, createHelper, column, columnStyles[c], INT_CELL_DEFAULT_FORMAT, settings, rowObject,
										mapColumns, mapColumnsTypes, variablesMap, mapParameters));
							}
							break;
						case "float":
							if (!s.trim().isEmpty()) {
								cell.setCellValue(Double.parseDouble(s));
								cell.setCellStyle(getDoubleCellStyle(wb, createHelper, column, columnStyles[c], FLOAT_CELL_DEFAULT_FORMAT, settings,
										Double.parseDouble(s), rowObject, mapColumns, mapColumnsTypes, variablesMap, mapParameters));
							} else {
								cell.setCellStyle(getGenericCellStyle(wb, createHelper, column, columnStyles[c], FLOAT_CELL_DEFAULT_FORMAT, settings, rowObject,
										mapColumns, mapColumnsTypes, variablesMap, mapParameters));
							}
							break;
						case "date":
							try {
								if (!s.trim().isEmpty()) {
									Date date = dateFormat.parse(s);
									cell.setCellValue(date);
									cell.setCellStyle(getDateCellStyle(wb, createHelper, column, columnStyles[c], DATE_FORMAT, settings, rowObject, mapColumns,
											mapColumnsTypes, variablesMap, mapParameters));
								}
							} catch (Exception e) {
								LOGGER.debug("Date will be exported as string due to error: ", e);
								cell.setCellValue(s);
							}
							break;
						case "timestamp":
							try {
								if (!s.trim().isEmpty()) {
									Date ts = timeStampFormat.parse(s);
									cell.setCellValue(ts);
									cell.setCellStyle(getDateCellStyle(wb, createHelper, column, columnStyles[c], TIMESTAMP_FORMAT, settings, rowObject,
											mapColumns, mapColumnsTypes, variablesMap, mapParameters));
								}
							} catch (Exception e) {
								LOGGER.debug("Timestamp will be exported as string due to error: ", e);
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

	public CellStyle buildCellStyle(Sheet sheet, boolean bold, HorizontalAlignment alignment, VerticalAlignment verticalAlignment, short headerFontSizeShort) {

		// CELL
		CellStyle cellStyle = sheet.getWorkbook().createCellStyle();

		// alignment
		cellStyle.setAlignment(alignment);
		cellStyle.setVerticalAlignment(verticalAlignment);

		// foreground color
//		String headerBGColor = (String) this.getProperty(PROPERTY_HEADER_BACKGROUND_COLOR);
//		short backgroundColorIndex = headerBGColor != null ? IndexedColors.valueOf(headerBGColor).getIndex()
//				: IndexedColors.valueOf(DEFAULT_HEADER_BACKGROUND_COLOR).getIndex();
//		cellStyle.setFillForegroundColor(backgroundColorIndex);

		// pattern
//		cellStyle.setFillPattern(fp);

		// borders
//		cellStyle.setBorderBottom(borderBottom);
//		cellStyle.setBorderLeft(borderLeft);
//		cellStyle.setBorderRight(borderRight);
//		cellStyle.setBorderTop(borderTop);

//		String bordeBorderColor = (String) this.getProperty(PROPERTY_HEADER_BORDER_COLOR);
//		short borderColorIndex = bordeBorderColor != null ? IndexedColors.valueOf(bordeBorderColor).getIndex()
//				: IndexedColors.valueOf(DEFAULT_HEADER_BORDER_COLOR).getIndex();
//		cellStyle.setLeftBorderColor(borderColorIndex);
//		cellStyle.setRightBorderColor(borderColorIndex);
//		cellStyle.setBottomBorderColor(borderColorIndex);
//		cellStyle.setTopBorderColor(borderColorIndex);

		// FONT
		Font font = sheet.getWorkbook().createFont();

		// size
//		Short headerFontSize = (Short) this.getProperty(PROPERTY_HEADER_FONT_SIZE);
//		short headerFontSizeShort = headerFontSize != null ? headerFontSize.shortValue() : DEFAULT_HEADER_FONT_SIZE;
		font.setFontHeightInPoints(headerFontSizeShort);

		// name
//		String fontName = (String) this.getProperty(PROPERTY_FONT_NAME);
//		fontName = fontName != null ? fontName : DEFAULT_FONT_NAME;
//		font.setFontName(fontName);

		// color
//		String headerColor = (String) this.getProperty(PROPERTY_HEADER_COLOR);
//		short headerColorIndex = headerColor != null ? IndexedColors.valueOf(headerColor).getIndex()
//				: IndexedColors.valueOf(DEFAULT_HEADER_COLOR).getIndex();
//		font.setColor(headerColorIndex);

		// bold
		font.setBold(bold);

		cellStyle.setFont(font);
		return cellStyle;
	}

	public void adjustColumnWidth(Sheet sheet) {
		try {
			boolean enabled = true;
			((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
			Row row = sheet.getRow(sheet.getLastRowNum());
			if (row != null) {
				for (int i = 0; i < row.getLastCellNum(); i++) {
					sheet.autoSizeColumn(i);
					if (enabled && (i == 0 || i == 1)) {
						// first or second column
						int colWidth = 25;
						if (sheet.getColumnWidthInPixels(i) < (colWidth * 256))
							sheet.setColumnWidth(i, colWidth * 256);
					}
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot write data to Excel file", e);
		}
	}

	public void drawBrandendHeaderImage(Sheet sheet, String imageB64, int pictureType, int startCol, int startRow, int colspan, int rowspan) {
		try {
			Workbook wb = sheet.getWorkbook();

			// load the picture
			String encodingPrefix = "base64,";
			int contentStartIndex = imageB64.indexOf(encodingPrefix) + encodingPrefix.length();
			byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(imageB64.substring(contentStartIndex));
			int pictureIdx = wb.addPicture(bytes, pictureType);

			// create an anchor with upper left cell startCol/startRow
			CreationHelper helper = wb.getCreationHelper();
			ClientAnchor anchor = helper.createClientAnchor();
			anchor.setCol1(startCol);
			anchor.setRow1(startRow);

			Drawing drawing = sheet.createDrawingPatriarch();
			Picture pict = drawing.createPicture(anchor, pictureIdx);

			int pictWidthPx = pict.getImageDimension().width;
			int pictHeightPx = pict.getImageDimension().height;

			// get the heights of all merged rows in px
			float[] rowHeightsPx = new float[startRow + rowspan];
			float rowsHeightPx = 0f;
			for (int r = startRow; r < startRow + rowspan; r++) {
				Row row = sheet.getRow(r);
				float rowHeightPt = row.getHeightInPoints();
				rowHeightsPx[r - startRow] = rowHeightPt * Units.PIXEL_DPI / Units.POINT_DPI;
				rowsHeightPx += rowHeightsPx[r - startRow];
			}

			// get the widths of all merged cols in px
			float[] colWidthsPx = new float[startCol + colspan];
			float colsWidthPx = 0f;
			for (int c = startCol; c < startCol + colspan; c++) {
				colWidthsPx[c - startCol] = sheet.getColumnWidthInPixels(c);
				colsWidthPx += colWidthsPx[c - startCol];
			}

			// calculate scale
			float scale = 1;
			if (pictHeightPx > rowsHeightPx) {
				float tmpscale = rowsHeightPx / pictHeightPx;
				if (tmpscale < scale)
					scale = tmpscale;
			}
			if (pictWidthPx > colsWidthPx) {
				float tmpscale = colsWidthPx / pictWidthPx;
				if (tmpscale < scale)
					scale = tmpscale;
			}

			// calculate the horizontal center position
			int horCenterPosPx = Math.round(colsWidthPx / 2f - pictWidthPx * scale / 2f);
			Integer col1 = null;
			colsWidthPx = 0f;
			for (int c = 0; c < colWidthsPx.length; c++) {
				float colWidthPx = colWidthsPx[c];
				if (colsWidthPx + colWidthPx > horCenterPosPx) {
					col1 = c + startCol;
					break;
				}
				colsWidthPx += colWidthPx;
			}

			// set the horizontal center position as Col1 plus Dx1 of anchor
			if (col1 != null) {
				anchor.setCol1(col1);
				anchor.setDx1(Math.round(horCenterPosPx - colsWidthPx) * Units.EMU_PER_PIXEL);
			}

			// calculate the vertical center position
			int vertCenterPosPx = Math.round(rowsHeightPx / 2f - pictHeightPx * scale / 2f);
			Integer row1 = null;
			rowsHeightPx = 0f;
			for (int r = 0; r < rowHeightsPx.length; r++) {
				float rowHeightPx = rowHeightsPx[r];
				if (rowsHeightPx + rowHeightPx > vertCenterPosPx) {
					row1 = r + startRow;
					break;
				}
				rowsHeightPx += rowHeightPx;
			}

			if (row1 != null) {
				anchor.setRow1(row1);
				anchor.setDy1(Math.round(vertCenterPosPx - rowsHeightPx) * Units.EMU_PER_PIXEL); // in unit EMU for XSSF
			}

			anchor.setCol2(startCol + colspan);
			anchor.setDx2(Math.round(colsWidthPx - Math.round(horCenterPosPx - colsWidthPx)) * Units.EMU_PER_PIXEL);
			anchor.setRow2(startRow + rowspan);
			anchor.setDy2(Math.round(rowsHeightPx - Math.round(vertCenterPosPx - rowsHeightPx)) * Units.EMU_PER_PIXEL);

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot write data to Excel file", e);
		}
	}

	private HashMap<String, Object> createMapVariables(HashMap<String, Object> variablesMap) throws JSONException {
		if (body.has("COCKPIT_VARIABLES")) {
			if (body.get("COCKPIT_VARIABLES") instanceof JSONObject) {
				JSONObject variableOBJ = body.getJSONObject("COCKPIT_VARIABLES");
				variablesMap = new Gson().fromJson(variableOBJ.toString(), HashMap.class);

			} else if (body.get("COCKPIT_VARIABLES") instanceof JSONArray) {

				for (int j = 0; j < body.getJSONArray("COCKPIT_VARIABLES").length(); j++) {
					JSONObject variableOBJ = body.getJSONArray("COCKPIT_VARIABLES").getJSONObject(j);
					variablesMap = new Gson().fromJson(variableOBJ.toString(), HashMap.class);

				}

			}

		}
		return variablesMap;
	}

	private HashMap<String, Object> createMapParameters(HashMap<String, Object> mapParameters) throws JSONException {
		if (body.has("COCKPIT_SELECTIONS") && body.get("COCKPIT_SELECTIONS") instanceof JSONObject && body.getJSONObject("COCKPIT_SELECTIONS").has("drivers")) {
			mapParameters = getParametersMap(body.getJSONObject("COCKPIT_SELECTIONS").getJSONObject("drivers"));
		} else if (body.has("COCKPIT_SELECTIONS") && body.get("COCKPIT_SELECTIONS") instanceof JSONArray) {
			for (int j = 0; j < body.getJSONArray("COCKPIT_SELECTIONS").length(); j++) {
				if ((body.getJSONArray("COCKPIT_SELECTIONS").get(j) instanceof JSONArray)
						&& (!(body.getJSONArray("COCKPIT_SELECTIONS").get(j) instanceof JSONArray))
						&& body.getJSONArray("COCKPIT_SELECTIONS").getJSONObject(j).has("drivers")) {
					mapParameters = getParametersMap(body.getJSONArray("COCKPIT_SELECTIONS").getJSONObject(j).getJSONObject("drivers"));
				}
			}
		}
		return mapParameters;
	}

	private HashMap<String, String> getColumnsMap(JSONArray columnsOrdered) {
		HashMap<String, String> mapp = new HashMap<>();
		for (int c = 0; c < columnsOrdered.length(); c++) {
			try {
				JSONObject column = columnsOrdered.getJSONObject(c);

				mapp.put(column.getString("header"), column.getString("name"));
			} catch (JSONException e) {
				throw new SpagoBIRuntimeException("Couldn't create columns map", e);
			}
		}
		return mapp;
	}

	private HashMap<String, String> getColumnsMapTypes(JSONArray columnsOrdered) {
		HashMap<String, String> mapp = new HashMap<>();
		for (int c = 0; c < columnsOrdered.length(); c++) {
			try {
				JSONObject column = columnsOrdered.getJSONObject(c);

				mapp.put(column.getString("name"), column.getString("type"));
			} catch (JSONException e) {
				throw new SpagoBIRuntimeException("Couldn't create columns map", e);
			}
		}
		return mapp;
	}

	private HashMap<String, Object> getParametersMap(JSONObject drivers) {
		HashMap<String, Object> mapp = new HashMap<>();

		Iterator<String> keys = drivers.keys();

		while (keys.hasNext()) {
			String key = keys.next();
			try {
				if (drivers.get(key) instanceof JSONArray) {
					JSONArray parameterArray = drivers.getJSONArray(key);
					for (int c = 0; c < parameterArray.length(); c++) {

						JSONObject paramValueOBJ = parameterArray.getJSONObject(c);

						Iterator<String> paramkeys = paramValueOBJ.keys();
						while (paramkeys.hasNext()) {
							String paramkey = paramkeys.next();
							mapp.put(key, paramValueOBJ.get(paramkey));

						}

					}

				}
			} catch (JSONException e) {
				throw new SpagoBIRuntimeException("Couldn't create parameter map", e);
			}
		}

		return mapp;
	}

	// create drivers map

	private Map<String, Map<String, Object>> createDriversMap() throws JSONException {
		Map<String, Map<String, Object>> selectionsMap = new HashMap<>();
		if (body.has("COCKPIT_SELECTIONS") && body.get("COCKPIT_SELECTIONS") instanceof JSONArray) {
			JSONArray cockpitSelections = body.getJSONArray("COCKPIT_SELECTIONS");

			for (int i = 0; i < cockpitSelections.length(); i++) {
				if (!(cockpitSelections.get(i) instanceof JSONArray)) {
					JSONObject cockpitSelection = cockpitSelections.getJSONObject(i);

					manageDriversFromJSONObject(selectionsMap, cockpitSelection);

				}
			}
		} else if (body.has("COCKPIT_SELECTIONS") && body.get("COCKPIT_SELECTIONS") instanceof JSONObject) {

			JSONObject cockpitSelection = body.getJSONObject("COCKPIT_SELECTIONS");
			manageDriversFromJSONObject(selectionsMap, cockpitSelection);
		}

		return selectionsMap;
	}

	private void manageDriversFromJSONObject(Map<String, Map<String, Object>> selectionsMap, JSONObject cockpitSelection) throws JSONException {
		if (cockpitSelection.has("drivers")) {
			manageDriversFromJSONObjectUsingKey(selectionsMap, cockpitSelection, "drivers");
		}
	}

	private void manageDriversFromJSONObjectUsingKey(Map<String, Map<String, Object>> selectionsMap, JSONObject cockpitSelection, String key)
			throws JSONException {
		JSONObject drivers = cockpitSelection.getJSONObject(key);

		Iterator<String> keys = drivers.keys();

		manageSingleDriver(selectionsMap, drivers, keys);
	}

	private void manageSingleDriver(Map<String, Map<String, Object>> selectionsMap, JSONObject drivers, Iterator<String> keys) throws JSONException {
		while (keys.hasNext()) {
			String key = keys.next();
			if (drivers.get(key) instanceof JSONArray) {
				manageDriver(selectionsMap, drivers, key);
			}
		}
	}

	private void manageDriver(Map<String, Map<String, Object>> selectionsMap, JSONObject drivers, String key) throws JSONException {
		JSONArray driver = (JSONArray) drivers.get(key);
		Iterator<String> driverKeys = ((JSONObject) driver.get(0)).keys();
		HashMap<String, Object> selects = new HashMap<>();
		while (driverKeys.hasNext()) {
			String selKey = driverKeys.next();
			Object select = ((JSONObject) driver.get(0)).get(selKey);
			if (!selKey.contains(",")) {
				manageUserSelectionValue(selects, selKey, select);
			}
		}
		if (!selects.isEmpty()) {
			selectionsMap.put(key, selects);
		}
	}

	// create selections map

	private Map<String, Map<String, Object>> createSelectionsMap() throws JSONException {
		Map<String, Map<String, Object>> selectionsMap = new HashMap<>();
		if (body.has("COCKPIT_SELECTIONS") && body.get("COCKPIT_SELECTIONS") instanceof JSONArray) {
			JSONArray cockpitSelections = body.getJSONArray("COCKPIT_SELECTIONS");

			for (int i = 0; i < cockpitSelections.length(); i++) {
				if (!(cockpitSelections.get(i) instanceof JSONArray)) {
					JSONObject cockpitSelection = cockpitSelections.getJSONObject(i);

					manageUserSelectionFromJSONObject(selectionsMap, cockpitSelection);
				}
			}
		} else if (body.has("COCKPIT_SELECTIONS") && body.get("COCKPIT_SELECTIONS") instanceof JSONObject) {

			JSONObject cockpitSelection = body.getJSONObject("COCKPIT_SELECTIONS");

			manageUserSelectionFromJSONObject(selectionsMap, cockpitSelection);
		}

		return selectionsMap;
	}

	private void manageUserSelectionFromJSONObject(Map<String, Map<String, Object>> selectionsMap, JSONObject cockpitSelection) throws JSONException {
		if (cockpitSelection.has("userSelections") && !((cockpitSelection.getJSONObject("userSelections")).length() == 0)) {
			manageUserSelectionFromJSONObjectUsingKey(selectionsMap, cockpitSelection, "userSelections");
		} else if (cockpitSelection.has("selections") && !((cockpitSelection.getJSONObject("selections")).length() == 0)) {
			// TODO : Map widget seems to have a different syntax
			manageUserSelectionFromJSONObjectUsingKey(selectionsMap, cockpitSelection, "selections");
		}
	}

	private void manageUserSelectionFromJSONObjectUsingKey(Map<String, Map<String, Object>> selectionsMap, JSONObject cockpitSelection, String key)
			throws JSONException {
		JSONObject selections = cockpitSelection.getJSONObject(key);

		Iterator<String> keys = selections.keys();

		manageSingleUserSelection(selectionsMap, selections, keys);
	}

	private void manageSingleUserSelection(Map<String, Map<String, Object>> selectionsMap, JSONObject selections, Iterator<String> keys) throws JSONException {
		while (keys.hasNext()) {
			String key = keys.next();
			if (selections.get(key) instanceof JSONObject) {
				manageSelection(selectionsMap, selections, key);
			}
		}
	}

	private void manageSelection(Map<String, Map<String, Object>> selectionsMap, JSONObject selections, String key) throws JSONException {
		JSONObject selection = (JSONObject) selections.get(key);
		Iterator<String> selectionKeys = selection.keys();
		HashMap<String, Object> selects = new HashMap<>();
		while (selectionKeys.hasNext()) {
			String selKey = selectionKeys.next();
			Object select = selection.get(selKey);
			if (!selKey.contains(",")) {
				selects.put(selKey, select);
			}
		}
		if (!selects.isEmpty()) {
			selectionsMap.put(key, selects);
		}
	}

	private void manageUserSelectionValue(HashMap<String, Object> selects, String selKey, Object select) throws JSONException {
		if (select instanceof JSONObject) {
			if (((JSONObject) select).has("filterOperator")) {
				// Do nothing
			}
		} else {
			if (select instanceof JSONArray) {
				JSONArray selectArray = (JSONArray) select;
				for (int j = 0; j < selectArray.length(); j++) {
					Object selObj = selectArray.get(j);
					if (selObj instanceof JSONObject) {
						if (((JSONObject) selObj).has("filterOperator")) {
							// Do nothing
						} else {
							selects.put(selKey, selObj);
						}

					} else {
						selects.put(selKey, selObj);
					}
				}
			} else {
				selects.put(selKey, select);
			}
		}
	}

	private String getCellType(JSONObject column, String colName) {
		try {
			return column.getString("type");
		} catch (Exception e) {
			LOGGER.error("Error while retrieving column {} type. It will be treated as string.", colName, e);
			return "string";
		}
	}

	@Override
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

	private Row createHeaderColumnNames(Sheet sheet, Map<String, String> groupsAndColumnsMap, JSONArray columnsOrdered, int startRowOffset) {
		try {
			Row header = null;
			if (!groupsAndColumnsMap.isEmpty()) {
				Row newheader = sheet.createRow((short) startRowOffset);
				for (int i = 0; i < columnsOrdered.length(); i++) {
					JSONObject column = columnsOrdered.getJSONObject(i);
					String groupName = groupsAndColumnsMap.get(column.get("header"));
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
			String uniqueSafeSheetName = safeSheetName/* + String.valueOf(uniqueId) */;
			try {
				sheet = wb.createSheet(uniqueSafeSheetName);
				uniqueId++;
				return sheet;
			} catch (Exception e) {
				sheet = wb.createSheet(uniqueSafeSheetName + "(" + String.valueOf(uniqueId) + ")");
				uniqueId++;
				return sheet;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Couldn't create sheet", e);
		}
	}

	private Sheet createUniqueSafeSheetForSelections(Workbook wb, String widgetName) {
		Sheet sheet;
		try {
			sheet = wb.createSheet(widgetName);
			return sheet;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Couldn't create sheet", e);
		}
	}

	private Sheet createUniqueSafeSheetForDrivers(Workbook wb, String widgetName) {
		Sheet sheet;
		try {
			sheet = wb.createSheet(widgetName);
			return sheet;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Couldn't create sheet", e);
		}
	}

	private JSONObject getDatastore(String datasetLabel, Map<String, Object> map, String selections) {
		// if pagination is disabled offset = 0, fetchSize = -1
		return getDatastore(datasetLabel, map, selections, 0, -1);
	}

	private void logOutputToCoreLog(Process exec) throws IOException {
		InputStreamReader isr = new InputStreamReader(exec.getInputStream());
		BufferedReader b = new BufferedReader(isr);
		String line = null;
		LOGGER.warn("Process output");
		while ((line = b.readLine()) != null) {
			LOGGER.warn(line);
		}
	}

	private void setWorkingDirectory(String cockpitExportScriptPath, ProcessBuilder processBuilder) {
		// Required by puppeteer v19
		processBuilder.directory(new File(cockpitExportScriptPath));
	}

}