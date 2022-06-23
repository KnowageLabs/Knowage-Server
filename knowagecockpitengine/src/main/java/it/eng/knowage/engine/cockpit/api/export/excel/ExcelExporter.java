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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

import it.eng.qbe.serializer.SerializationException;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.i18n.dao.I18NMessagesDAO;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 * @author Marco Balestri (marco.balestri@eng.it)
 */

public class ExcelExporter {

	static private Logger logger = Logger.getLogger(ExcelExporter.class);

	public static final String UNIQUE_ALIAS_PLACEHOLDER = "_$_";

	private final String userUniqueIdentifier;
	private final boolean isSingleWidgetExport;
	private final JSONObject body;
	private Locale locale;
	private int uniqueId = 0;
	private String requestURL = "";
	private List<Integer> hiddenColumns;
	private Map<String, String> i18nMessages;

	private Map<String, CellStyle> format2CellStyle = new HashMap<String, CellStyle>();

	private static final String[] WIDGETS_TO_IGNORE = { "image", "text", "selector", "selection", "html" };
	private static final String SCRIPT_NAME = "cockpit-export-xls.js";
	private static final String CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH = "internal.nodejs.chromium.export.path";
	private static final int SHEET_NAME_MAX_LEN = 31;
	private static final String DATE_FORMAT = "dd/MM/yyyy";
	public static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss.SSS";

	// used only for scheduled export
	public ExcelExporter(String outputType, String userUniqueIdentifier, Map<String, String[]> parameterMap, String requestURL) {
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.isSingleWidgetExport = false;
		this.requestURL = requestURL;
		this.body = new JSONObject();
	}

	public ExcelExporter(String outputType, String userUniqueIdentifier, JSONObject body) {
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.isSingleWidgetExport = body.optBoolean("exportWidget");
		this.body = body;
		this.locale = getLocale(body);
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

	public String getMimeType() {
		return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	}

	// used only for scheduled exports
	// leverages on an external script that uses chromium to open the cockpit and click on the export button
	public byte[] getBinaryData(String documentLabel) throws IOException, InterruptedException, EMFUserError {
		try {
			logger.debug("Creating temp dir...");
			final Path outputDir = Files.createTempDirectory("knowage-xls-exporter-");
			logger.debug("Created temp dir: " + outputDir);
			if (Files.exists(outputDir) && Files.isDirectory(outputDir)) {
				logger.debug("Temp dir exists");
			} else {
				logger.debug("Temp dir DOES NOT exist");
			}
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

			if (isSingleWidgetExport) {
				long widgetId = body.getLong("widget");
				String widgetType = getWidgetTypeFromCockpitTemplate(templateString, widgetId);
				JSONObject optionsObj = new JSONObject();
				if (options != null && !options.isEmpty())
					optionsObj = new JSONObject(options);
				WidgetXLSXExporter widgetExporter = new WidgetXLSXExporter(this, widgetType, templateString, widgetId, wb, optionsObj);
				widgetExporter.export();
			} else {
				// export whole cockpit
				JSONArray widgetsJson = getWidgetsJson(templateString);
				JSONObject optionsObj = buildOptionsForCrosstab(templateString);
				exportCockpit(templateString, widgetsJson, wb, optionsObj);
			}

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

	String getWidgetTypeFromCockpitTemplate(String templateString, long widgetId) {
		try {
			JSONObject templateJson = new JSONObject(templateString);
			JSONArray sheets = templateJson.getJSONArray("sheets");
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				JSONArray widgets = sheet.getJSONArray("widgets");
				for (int j = 0; j < widgets.length(); j++) {
					JSONObject widget = widgets.getJSONObject(j);
					long currWidgetId = widget.getLong("id");
					if (currWidgetId == widgetId) {
						return widget.getString("type");
					}
				}
			}
		} catch (Exception e) {
			logger.error("Couldn't get widget " + widgetId + " type, it will be exported as a normal widget.");
		}
		logger.error("Couldn't get widget " + widgetId + " type, it will be exported as a normal widget.");
		return "";
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
						ExcelExporterClient client = new ExcelExporterClient();
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

	private JSONObject getCockpitVariables() {
		try {
			if (body.get("COCKPIT_VARIABLES") instanceof JSONObject)
				return body.getJSONObject("COCKPIT_VARIABLES");
			else
				return body.getJSONArray("COCKPIT_VARIABLES").getJSONObject(0);
		} catch (JSONException e) {
			logger.error("Cannot retrieve cockpit variables", e);
			return new JSONObject();
		}
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

	private void exportCockpit(String templateString, JSONArray widgetsJson, Workbook wb, JSONObject optionsObj) {
		try {
			int totExportedWidgets = 0;
			for (int i = 0; i < widgetsJson.length(); i++) {
				JSONObject currWidget = widgetsJson.getJSONObject(i);
				String widgetId = currWidget.getString("id");
				String widgetType = currWidget.getString("type");
				if (Arrays.asList(WIDGETS_TO_IGNORE).contains(widgetType.toLowerCase()))
					continue;
				totExportedWidgets++;
				JSONObject currWidgetOptions = new JSONObject();
				if (optionsObj.has(widgetId))
					currWidgetOptions = optionsObj.getJSONObject(widgetId);
				WidgetXLSXExporter widgetExporter = new WidgetXLSXExporter(this, widgetType, templateString, Long.parseLong(widgetId), wb, currWidgetOptions);
				widgetExporter.export();
			}
			if (totExportedWidgets == 0) {
				exportEmptyExcel(wb);
			}
		} catch (Exception e) {
			logger.error("Error while exporting cockpit. Operation aborted.", e);
		}
	}

	private void exportEmptyExcel(Workbook wb) {
		Sheet sh = wb.createSheet();
		Row row = sh.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("No data");
	}

	protected JSONArray getMultiDataStoreForWidget(JSONObject template, JSONObject widget) {
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

	protected JSONObject getDataStoreForWidget(JSONObject template, JSONObject widget, int offset, int fetchSize) {
		Map<String, Object> map = new java.util.HashMap<String, Object>();
		JSONObject datastore = null;
		try {
			JSONObject configuration = template.getJSONObject("configuration");
			JSONObject datasetObj = widget.getJSONObject("dataset");
			int datasetId = datasetObj.getInt("dsId");
			IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
			String datasetLabel = dataset.getLabel();

			if (getRealtimeFromWidget(datasetId, configuration))
				map.put("nearRealtime", true);

			JSONObject cockpitSelections = getCockpitSelectionsFromBody(widget);

			JSONArray summaryRow = getSummaryRowFromWidget(widget);
			if (summaryRow != null)
				cockpitSelections.put("summaryRow", summaryRow);

			if (isSolrDataset(dataset) && !widget.getString("type").equalsIgnoreCase("discovery")) {
				JSONObject jsOptions = new JSONObject();
				jsOptions.put("solrFacetPivot", true);
				cockpitSelections.put("options", jsOptions);
			}

			datastore = getDatastore(datasetLabel, map, cockpitSelections.toString(), offset, fetchSize);
			datastore.put("widgetData", widget);

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error getting datastore for widget [type=" + widget.optString("type") + "] [id=" + widget.optLong("id") + "]",
					e);
		}
		return datastore;
	}

	private JSONObject getCockpitSelectionsFromBody(JSONObject widget) {
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

	private void forceUniqueHeaders(JSONObject cockpitSelections) throws JSONException {
		JSONObject aggregations = cockpitSelections.getJSONObject("aggregations");
		JSONArray measures = aggregations.getJSONArray("measures");
		manipulateDimensions(measures);
		JSONArray categories = aggregations.getJSONArray("categories");
		manipulateDimensions(categories);
	}

	private void manipulateDimensions(JSONArray dimensions) throws JSONException {
		Set<String> dimensionsAliases = new HashSet<String>();
		for (int i = 0; i < dimensions.length(); i++) {
			JSONObject d = dimensions.getJSONObject(i);
			String alias = d.getString("alias");
			if (dimensionsAliases.contains(alias)) {
				d.put("alias", alias + UNIQUE_ALIAS_PLACEHOLDER + i);
			}
			dimensionsAliases.add(alias);
		}
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

	protected void createAndFillExcelSheet(JSONObject dataStore, Workbook wb, String widgetName, String cockpitSheetName) {
		Sheet newSheet = createUniqueSafeSheet(wb, widgetName, cockpitSheetName);
		fillSheetWithData(dataStore, wb, newSheet, widgetName, 0);
	}

	protected void fillSheetWithData(JSONObject dataStore, Workbook wb, Sheet sheet, String widgetName, int offset) {
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

	private String getInternationalizedHeader(String columnName) {
		if (i18nMessages == null) {
			I18NMessagesDAO messageDao = DAOFactory.getI18NMessageDAO();
			try {
				i18nMessages = messageDao.getAllI18NMessages(locale);
			} catch (Exception e) {
				logger.error("Error while getting i18n messages", e);
				i18nMessages = new HashMap<String, String>();
			}
		}
		return i18nMessages.getOrDefault(columnName, columnName);
	}

	private String getCellType(JSONObject column, String colName, JSONObject colStyle) {
		try {
			String toReturn = column.getString("type");
			if ("date".equals(toReturn) && column.has("subtype")) {
				toReturn = column.getString("subtype");
			}
			return toReturn;
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

	private JSONObject[] getColumnsStyles(JSONArray columnsOrdered, JSONObject widgetContent) {
		try {
			JSONObject[] toReturn = new JSONObject[columnsOrdered.length() + 10];
			JSONArray columns = widgetContent.getJSONArray("columnSelectedOfDataset");
			for (int i = 0; i < columnsOrdered.length(); i++) {
				JSONObject orderedCol = columnsOrdered.getJSONObject(i);
				for (int j = 0; j < columns.length(); j++) {
					JSONObject col = columns.getJSONObject(j);
					if (orderedCol.getString("header").equals(getTableColumnHeaderValue(col))) {
						if (col.has("style")) {
							toReturn[i] = col.getJSONObject("style");
						}
						break;
					}
				}
			}
			return toReturn;
		} catch (Exception e) {
			logger.error("Error while retrieving table columns styles.", e);
			return new JSONObject[columnsOrdered.length() + 10];
		}
	}

	private List<Integer> getHiddenColumnsList(JSONArray columns) {
		List<Integer> hiddenColumns = new ArrayList<Integer>();
		try {
			for (int i = 0; i < columns.length(); i++) {
				JSONObject column = columns.getJSONObject(i);
				// check if column is hidden with flag "Hide column"
				if (column.has("style")) {
					JSONObject style = column.optJSONObject("style");
					if (style.has("hiddenColumn")) {
						if (style.getString("hiddenColumn").equals("true")) {
							hiddenColumns.add(i);
						}
					}
				}
				// check if columns is hidden by using variables
				if (column.has("variables")) {
					JSONArray variables = column.getJSONArray("variables");
					for (int j = 0; j < variables.length(); j++) {
						JSONObject variable = variables.getJSONObject(j);
						if (variable.optString("action").equalsIgnoreCase("hide")) {
							if (variableMustHideColumn(column, variable))
								hiddenColumns.add(i);
						}
					}
				}
			}
			return hiddenColumns;
		} catch (Exception e) {
			logger.error("Error while getting hidden columns list", e);
			return new ArrayList<Integer>();
		}
	}

	private boolean variableMustHideColumn(JSONObject column, JSONObject variable) {
		try {
			String variableValue = "";
			Object value = getCockpitVariables().get(variable.getString("variable"));
			if (value instanceof String) {
				// static variable
				variableValue = (String) value;
			} else if (value instanceof JSONObject) {
				// dataset variable
				String key = variable.optString("key");
				variableValue = ((JSONObject) value).optString(key);
			}
			String condition = variable.getString("condition");
			switch (condition) {
			case "==":
				if (variable.getString("value").equals(variableValue))
					return true;
				break;
			case "!=":
				if (!variable.getString("value").equals(variableValue))
					return true;
				break;
			case ">":
				if (variable.getString("value").compareTo(variableValue) > 0)
					return true;
				break;
			case "<":
				if (variable.getString("value").compareTo(variableValue) < 0)
					return true;
				break;
			case ">=":
				if (variable.getString("value").compareTo(variableValue) >= 0)
					return true;
				break;
			case "<=":
				if (variable.getString("value").compareTo(variableValue) <= 0)
					return true;
				break;
			default:
				break;
			}
			return false;
		} catch (Exception e) {
			logger.error("Error while evaluating if column must be hidden according to variable.", e);
			return false;
		}
	}

	private JSONArray getTableOrderedColumns(JSONArray columnsNew, JSONArray columnsOld) {
		JSONArray columnsOrdered = new JSONArray();
		// new columns are in the correct order
		// for each of them we have to find the correspondent old column and push it into columnsOrdered
		try {
			for (int i = 0; i < columnsNew.length(); i++) {

				if (hiddenColumns.contains(i))
					continue;

				JSONObject columnNew = columnsNew.getJSONObject(i);
				String newHeader = getTableColumnHeaderValue(columnNew);

				for (int j = 0; j < columnsOld.length(); j++) {
					JSONObject columnOld = columnsOld.getJSONObject(j);
					if (columnOld.getString("header").equals(newHeader)) {
						columnsOrdered.put(columnOld);
						break;
					}
				}
			}
			return columnsOrdered;
		} catch (Exception e) {
			logger.error("Error retrieving ordered columns");
			return new JSONArray();
		}
	}

	private String getTableColumnHeaderValue(JSONObject column) {
		try {
			if (column.has("variables")) {
				JSONArray variables = column.getJSONArray("variables");
				for (int i = 0; i < variables.length(); i++) {
					JSONObject variable = variables.getJSONObject(i);
					if (variable.getString("action").equalsIgnoreCase("header"))
						return getCockpitVariables().getString(variable.getString("variable"));
				}
				return column.getString("aliasToShow");
			} else
				return column.getString("aliasToShow");
		} catch (Exception e) {
			logger.error("Error retrieving table column header values.", e);
			return "";
		}
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

	protected Sheet createUniqueSafeSheet(Workbook wb, String widgetName, String cockpitSheetName) {
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

	private HashMap<String, String> getMapFromGroupsArray(JSONArray groupsArray, JSONArray aggr) {
		HashMap<String, String> returnMap = new HashMap<String, String>();
		try {
			if (aggr != null && groupsArray != null) {

				for (int i = 0; i < groupsArray.length(); i++) {

					String id = groupsArray.getJSONObject(i).getString("id");
					String groupName = groupsArray.getJSONObject(i).getString("name");

					for (int ii = 0; ii < aggr.length(); ii++) {
						JSONObject column = aggr.getJSONObject(ii);

						if (column.has("group") && column.getString("group").equals(id)) {
							String nameToInsert = getTableColumnHeaderValue(column);
							returnMap.put(nameToInsert, groupName);
						}

					}
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Couldn't create map from groups array", e);
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
		// if pagination is disabled offset = 0, fetchSize = -1
		return getDatastore(datasetLabel, map, selections, 0, -1);
	}

	private JSONObject getDatastore(String datasetLabel, Map<String, Object> map, String selections, int offset, int fetchSize) {
		ExcelExporterClient client = new ExcelExporterClient();
		try {
			JSONObject datastore = client.getDataStore(map, datasetLabel, userUniqueIdentifier, selections, offset, fetchSize);
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

	private JSONArray getSummaryRowFromWidget(JSONObject widget) {
		try {
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
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	private boolean getRealtimeFromWidget(int dsId, JSONObject configuration) {
		try {
			JSONObject dataset = getDataset(dsId, configuration);
			return !dataset.optBoolean("useCache");
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	private JSONObject getDataset(int dsId, JSONObject configuration) {
		try {
			JSONArray datasets = configuration.getJSONArray("datasets");
			for (int i = 0; i < datasets.length(); i++) {
				JSONObject dataset = (JSONObject) datasets.get(i);
				int id = dataset.getInt("dsId");
				if (id == dsId) {
					return dataset;
				}
			}
			return null;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
		}
	}

	protected Locale getLocale() {
		return locale;
	}

}