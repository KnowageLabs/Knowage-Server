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
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
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
import it.eng.knowage.engine.cockpit.api.export.excel.crosstab.CrosstabXLSExporter;
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
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * @authors Francesco Lucchi (francesco.lucchi@eng.it)
 * @authors Marco Balestri (marco.balestri@eng.it)
 */

public class ExcelExporter {

	static private Logger logger = Logger.getLogger(ExcelExporter.class);

	private final String outputType;
	private final String userUniqueIdentifier;
	private final boolean exportWidget;
	private final JSONObject body;
	private Locale locale;
	private int uniqueId = 0;
	private String requestURL = "";
	private List<Integer> hiddenColumns;

	private static final String[] WIDGETS_TO_IGNORE = { "image", "text", "python", "r" };
	private static final String SCRIPT_NAME = "cockpit-export-xls.js";
	private static final String CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH = "internal.nodejs.chromium.export.path";
	private static final int SHEET_NAME_MAX_LEN = 31;

	// used only for scheduled export
	public ExcelExporter(String outputType, String userUniqueIdentifier, Map<String, String[]> parameterMap, String requestURL) {
		this.outputType = outputType;
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.exportWidget = false;
		this.requestURL = requestURL;
		this.body = new JSONObject();
	}

	public ExcelExporter(String outputType, String userUniqueIdentifier, JSONObject body) {
		this.outputType = outputType;
		this.userUniqueIdentifier = userUniqueIdentifier;
		this.exportWidget = body.optBoolean("exportWidget");
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
		String mimeType;

		if ("xlsx".equalsIgnoreCase(outputType))
			mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		else if ("xls".equalsIgnoreCase(outputType))
			mimeType = "application/vnd.ms-excel";
		else
			throw new SpagoBIRuntimeException("Unsupported output type [" + outputType + "]");

		return mimeType;
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
			Path outputFile = outputDir.resolve(documentLabel + "." + outputType.toLowerCase());
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

		Workbook wb;

		if ("xlsx".equalsIgnoreCase(outputType))
			wb = new XSSFWorkbook();
		else if ("xls".equalsIgnoreCase(outputType))
			wb = new HSSFWorkbook();
		else
			throw new SpagoBIRuntimeException("Unsupported output type [" + outputType + "]");

		try {
			if (exportWidget) {
				String widgetId = String.valueOf(body.get("widget"));
				String widgetType = getWidgetTypeFromCockpitTemplate(templateString, widgetId);
				if (widgetType.equalsIgnoreCase("static-pivot-table")) {
					JSONObject optionsObj = new JSONObject(options);
					exportWidgetCrossTab(templateString, widgetId, wb, optionsObj);
				} else if (widgetType.equalsIgnoreCase("map")) {
					exportWidgetMap(templateString, widgetId, wb);
				} else {
					exportWidget(templateString, widgetId, wb);
				}
			} else {
				JSONArray widgetsJson = getWidgetsJson(templateString);
				JSONObject optionsObj = buildOptionsForCrosstab(templateString);
				exportCockpit(templateString, widgetsJson, wb, optionsObj);
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

	String getWidgetTypeFromCockpitTemplate(String templateString, String widgetId) {
		try {
			JSONObject templateJson = new JSONObject(templateString);
			JSONArray sheets = templateJson.getJSONArray("sheets");
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				JSONArray widgets = sheet.getJSONArray("widgets");
				for (int j = 0; j < widgets.length(); j++) {
					JSONObject widget = widgets.getJSONObject(j);
					String currWidgetId = widget.getString("id");
					if (currWidgetId.equals(widgetId)) {
						return widget.getString("type");
					}
				}
			}
		} catch (JSONException e) {
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
					String widgetId = widget.getString("id");
					JSONObject options = new JSONObject();
					options.put("config", new JSONObject().put("type", "pivot"));
					options.put("sortOptions", widget.getJSONObject("content").getJSONObject("sortOptions"));
					options.put("name", widget.getJSONObject("content").getString("name"));
					options.put("crosstabDefinition", widget.getJSONObject("content").getJSONObject("crosstabDefinition"));
					options.put("style", widget.getJSONObject("content").getJSONObject("style"));
					// variables cannot be retrieved from template so we must recover them from request body
					options.put("variables", getCockpitVariables());
					ExcelExporterClient client = new ExcelExporterClient();
					String dsLabel = getDatasetLabel(template, widget.getJSONObject("dataset").getString("dsId"));
					String selections = getCockpitSelectionsFromBody(widget).toString();
					JSONObject datastore = client.getDataStore(new HashMap<String, Object>(), dsLabel, userUniqueIdentifier, selections);
					options.put("metadata", datastore.getJSONObject("metaData"));
					options.put("jsonData", datastore.getJSONArray("rows"));
					toReturn.put(widgetId, options);
				}
			}
			return toReturn;
		} catch (Exception e) {
			logger.error("Cannot retrieve cross table options from data service", e);
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

	private String getDatasetLabel(JSONObject template, String dsId) {
		try {
			JSONArray cockpitDatasets = template.getJSONObject("configuration").getJSONArray("datasets");
			for (int i = 0; i < cockpitDatasets.length(); i++) {
				String currDsId = cockpitDatasets.getJSONObject(i).getString("dsId");
				if (currDsId.equals(dsId))
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

	private void exportCockpit(String templateString, JSONArray widgetsJson, Workbook wb, JSONObject optionsObj) throws SerializationException {
		try {
			for (int i = 0; i < widgetsJson.length(); i++) {
				JSONObject currWidget = widgetsJson.getJSONObject(i);
				String widgetId = currWidget.getString("id");
				String widgetType = currWidget.getString("type");
				if (Arrays.asList(WIDGETS_TO_IGNORE).contains(widgetType.toLowerCase()))
					continue;
				else if (widgetType.equalsIgnoreCase("static-pivot-table") && optionsObj.has(widgetId)) {
					JSONObject options = optionsObj.getJSONObject(widgetId);
					exportWidgetCrossTab(templateString, widgetId, wb, options);
				} else if (widgetType.equalsIgnoreCase("map")) {
					exportWidgetMap(templateString, widgetId, wb);
				} else {
					exportWidget(templateString, widgetId, wb);
				}
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
					String cockpitSheetName = getCockpitSheetName(template, widgetId);
					createExcelFile(dataStore, wb, widgetName, cockpitSheetName);
				}
			}
		} catch (JSONException e) {
			logger.error("Unable to load template", e);
		}
	}

	private void exportWidgetMap(String templateString, String widgetId, Workbook wb) throws SerializationException {
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

				JSONArray dataStoreArray = getMultiDataStoreForWidget(template, widget);
				for (int i = 0; i < dataStoreArray.length(); i++) {
					JSONObject dataStore = dataStoreArray.getJSONObject(i);
					if (dataStore != null) {
						String cockpitSheetName = getCockpitSheetName(template, widgetId) + String.valueOf(i);
						createExcelFile(dataStore, wb, widgetName, cockpitSheetName);
					}
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
				JSONObject variables = optionsObj.optJSONObject("variables");
				Map<String, List<Threshold>> thresholdColorsMap = getThresholdColorsMap(measures);

				CrosstabXLSExporter exporter;
				if (outputType != null && outputType.toLowerCase().equals("xlsx"))
					exporter = new CrosstabXLSXExporter(null, variables, thresholdColorsMap);
				else
					exporter = new CrosstabXLSExporter(null, variables);

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
				CrosstabBuilder builder = new CrosstabBuilder(locale, crosstabDefinition, optionsObj.getJSONArray("jsonData"),
						optionsObj.getJSONObject("metadata"), null);

				CrossTab cs = builder.getSortedCrosstabObj(columnsSortKeysMap, rowsSortKeysMap, measuresSortKeysMap, myGlobalId);

				Sheet sheet;

				String cockpitSheetName = getCockpitSheetName(template, widgetId);
				sheet = createUniqueSafeSheet(wb, widgetName, cockpitSheetName);

				CreationHelper createHelper = wb.getCreationHelper();

				exporter.fillAlreadyCreatedSheet(sheet, cs, createHelper, 0, locale);

			}
		} catch (JSONException e) {
			logger.error("Unable to load template", e);
		}
	}

	private String getCockpitSheetName(JSONObject template, String widgetId) {
		try {
			JSONArray sheets = template.getJSONArray("sheets");
			if (sheets.length() == 1)
				return "";
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				JSONArray widgets = sheet.getJSONArray("widgets");
				for (int j = 0; j < widgets.length(); j++) {
					JSONObject widget = widgets.getJSONObject(j);
					if (widgetId.equals(widget.getString("id")))
						return sheet.getString("label");
				}
			}
			return "";
		} catch (Exception e) {
			logger.error("Unable to retrieve cockpit sheet name from template", e);
			return "";
		}
	}

	private Map<String, List<Threshold>> getThresholdColorsMap(JSONArray measures) {
		Map<String, List<Threshold>> toReturn = new HashMap<String, List<Threshold>>();
		try {
			for (int i = 0; i < measures.length(); i++) {
				JSONObject measure = measures.getJSONObject(i);
				String id = measure.getString("id");
				if (!measure.has("ranges"))
					continue;
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

	private JSONArray getMultiDataStoreForWidget(JSONObject template, JSONObject widget) {
		Map<String, Object> map = new java.util.HashMap<String, Object>();
		JSONArray multiDataStore = new JSONArray();
		try {
			JSONArray datasetIds = widget.getJSONArray("datasetId");
			for (int i = 0; i < datasetIds.length(); i++) {
				int datasetId = datasetIds.getInt(i);
				IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
				String datasetLabel = dataset.getLabel();
				JSONObject cockpitSelections = getMultiCockpitSelectionsFromBody(widget, datasetId);

				JSONArray summaryRow = getSummaryRowFromWidget(widget);
				if (summaryRow != null)
					cockpitSelections.put("summaryRow", summaryRow);

				if (isSolrDataset(dataset) && !widget.getString("type").equalsIgnoreCase("discovery")) {
					JSONObject jsOptions = new JSONObject();
					jsOptions.put("solrFacetPivot", true);
					cockpitSelections.put("options", jsOptions);
				}

				JSONObject dataStore = getDatastore(datasetLabel, map, cockpitSelections.toString());
				dataStore.put("widgetData", widget);
				multiDataStore.put(dataStore);
			}
		} catch (Exception e) {
			logger.error("Cannot get Datastore for widget", e);
		}
		return multiDataStore;
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

			if (getRealtimeFromTableWidget(datasetId, configuration))
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

			datastore = getDatastore(datasetLabel, map, cockpitSelections.toString());
			datastore.put("widgetData", widget);

		} catch (Exception e) {
			logger.error("Cannot get Datastore for widget", e);
		}
		return datastore;
	}

	private JSONObject getCockpitSelectionsFromBody(JSONObject widget) {
		JSONObject cockpitSelections = new JSONObject();
		if (body == null || body.length() == 0)
			return cockpitSelections;
		try {
			if (exportWidget) { // export single widget
				cockpitSelections = body.getJSONObject("COCKPIT_SELECTIONS");
			} else { // export whole cockpit
				JSONArray allWidgets = body.getJSONArray("widget");
				int i;
				for (i = 0; i < allWidgets.length(); i++) {
					JSONObject curWidget = allWidgets.getJSONObject(i);
					if (new BigDecimal(curWidget.getString("id")).compareTo(new BigDecimal(widget.getString("id"))) == 0)
						break;
				}
				cockpitSelections = body.getJSONArray("COCKPIT_SELECTIONS").getJSONObject(i);
			}
		} catch (Exception e) {
			logger.error("Cannot get cockpit selections", e);
		}
		return cockpitSelections;
	}

	private JSONObject getMultiCockpitSelectionsFromBody(JSONObject widget, int datasetId) {
		JSONObject cockpitSelections = new JSONObject();
		JSONArray allSelections = new JSONArray();
		if (body == null || body.length() == 0)
			return cockpitSelections;
		try {
			if (exportWidget) { // export single widget with multi dataset
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
		}
		return cockpitSelections;
	}

	private void createExcelFile(JSONObject dataStore, Workbook wb, String widgetName, String cockpitSheetName) throws JSONException, SerializationException {
		try {
			JSONObject metadata = dataStore.getJSONObject("metaData");
			JSONArray columns = metadata.getJSONArray("fields");
			columns = filterDataStoreColumns(columns);
			JSONArray rows = dataStore.getJSONArray("rows");

			JSONObject widgetData = dataStore.getJSONObject("widgetData");
			JSONObject widgetContent = widgetData.getJSONObject("content");
			HashMap<String, String> arrayHeader = new HashMap<String, String>();
			if (widgetData.getString("type").equalsIgnoreCase("table")) {
				for (int i = 0; i < widgetContent.getJSONArray("columnSelectedOfDataset").length(); i++) {
					JSONObject column = widgetContent.getJSONArray("columnSelectedOfDataset").getJSONObject(i);
					arrayHeader.put(column.getString("name"), column.getString("aliasToShow"));
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
			Sheet sheet;
			Row header = null;
			if (exportWidget) { // export single widget
				sheet = createUniqueSafeSheet(wb, widgetName, cockpitSheetName);
				header = createHeaderColumnNames(sheet, mapGroupsAndColumns, columnsOrdered, 0);
			} else { // export whole cockpit
				sheet = createUniqueSafeSheet(wb, widgetName, cockpitSheetName);
				// First row is for Widget name in case exporting whole Cockpit document
				Row firstRow = sheet.createRow((short) 0);
				Cell firstCell = firstRow.createCell(0);
				firstCell.setCellValue(widgetName);
				header = createHeaderColumnNames(sheet, mapGroupsAndColumns, columnsOrdered, 1);
			}

			for (int i = 0; i < columnsOrdered.length(); i++) {
				JSONObject column = columnsOrdered.getJSONObject(i);
				String columnName = column.getString("header");
				if (widgetData.getString("type").equalsIgnoreCase("table") || widgetData.getString("type").equalsIgnoreCase("discovery")) {
					if (arrayHeader.get(columnName) != null) {
						columnName = arrayHeader.get(columnName);
					}
				}

				Cell cell = header.createCell(i);
				cell.setCellValue(columnName);
			}

			// Cell styles for int and float
			CreationHelper createHelper = wb.getCreationHelper();

			CellStyle intCellStyle = wb.createCellStyle();
			intCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("0"));

			CellStyle floatCellStyle = wb.createCellStyle();
			floatCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

			// FILL RECORDS
			int isGroup = mapGroupsAndColumns.isEmpty() ? 0 : 1;
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

	private List<Integer> getHiddenColumnsList(JSONArray columns) {
		List<Integer> hiddenColumns = new ArrayList<Integer>();
		try {
			for (int i = 0; i < columns.length(); i++) {
				JSONObject column = columns.getJSONObject(i);
				if (column.has("style")) {
					JSONObject style = column.optJSONObject("style");
					if (style.has("hiddenColumn")) {
						if (style.getString("hiddenColumn").equals("true")) {
							hiddenColumns.add(i);
						}
					}
				}
			}
			return hiddenColumns;
		} catch (Exception e) {
			logger.error("Error while getting hidden columns list");
			return new ArrayList<Integer>();
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
		String header = null;
		try {
			if (column.has("variables")) {
				JSONArray variables = column.getJSONArray("variables");
				for (int i = 0; i < variables.length(); i++) {
					JSONObject variable = variables.getJSONObject(i);
					if (variable.getString("action").equalsIgnoreCase("header"))
						header = getCockpitVariables().getString(variable.getString("variable"));
				}
			} else
				header = column.getString("aliasToShow");
			return header;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException(e);
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
			throw new SpagoBIRuntimeException(e);
		}
	}

	private Sheet createUniqueSafeSheet(Workbook wb, String widgetName, String cockpitSheetName) {
		Sheet sheet;
		String sheetName;
		if (!exportWidget && cockpitSheetName != null && !cockpitSheetName.equals(""))
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
						String nameToInsert = getTableColumnHeaderValue(column);
						returnMap.put(nameToInsert, groupName);
					}

				}
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
