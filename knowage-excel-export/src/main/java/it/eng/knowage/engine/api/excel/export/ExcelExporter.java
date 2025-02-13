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
package it.eng.knowage.engine.api.excel.export;

import com.google.gson.Gson;
import it.eng.knowage.commons.multitenant.OrganizationImageManager;
import it.eng.knowage.commons.security.PathTraversalChecker;
import it.eng.knowage.engine.api.excel.export.dashboard.exporters.DashboardWidgetExporterFactory;
import it.eng.knowage.engine.api.excel.export.exporters.IWidgetExporter;
import it.eng.knowage.engine.api.excel.export.exporters.WidgetExporterFactory;
import it.eng.knowage.engine.api.excel.export.dashboard.models.Style;
import it.eng.spago.error.EMFAbstractError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 * @author Marco Balestri (marco.balestri@eng.it)
 */

public class ExcelExporter extends AbstractFormatExporter {

    private static final Logger LOGGER = LogManager.getLogger(ExcelExporter.class);
    private static final String[] WIDGETS_TO_IGNORE = {"image", "text", "selector", "selection", "html"};
    private static final String SCRIPT_NAME = "cockpit-export-xls.js";
    private static final String CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH = "internal.nodejs.chromium.export.path";
    private static final int SHEET_NAME_MAX_LEN = 31;
    private static final String SORTING_OBJ = "sortingObj";
    private static final String DRILL_SORTING_OBJ = "drillSortingObj";
    private static final String BOTH = "both";

    public static final String UNIQUE_ALIAS_PLACEHOLDER = "_$_";
    private static final String STATIC_CUSTOM_STYLE = "static";
    private static final String CONDITIONAL_STYLE = "conditional";
    private static final String ALL_COLUMNS_STYLE = "all";

    private final boolean isSingleWidgetExport;
    private int uniqueId = 0;
    private String requestURL = "";

    private static final String INT_CELL_DEFAULT_FORMAT = "0";
    private static final String FLOAT_CELL_DEFAULT_FORMAT = "#,##0.00";

    private String imageB64 = "";
    private String documentName = "";
    private final Map<String, Object> properties;

    // used only for scheduled export
    public ExcelExporter(String userUniqueIdentifier, Map<String, String[]> parameterMap, String requestURL) {
        super(userUniqueIdentifier, new JSONObject());
        this.isSingleWidgetExport = false;
        this.requestURL = requestURL;
        this.properties = new HashMap<>();
    }

    public ExcelExporter(String userUniqueIdentifier, JSONObject body) {
        super(userUniqueIdentifier, body);
        this.isSingleWidgetExport = body.optBoolean("exportWidget");
        this.properties = new HashMap<>();
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

            String encodedUserId = Base64.encodeBase64String(userUniqueIdentifier.getBytes(UTF_8));

            // Script
            String cockpitExportScriptPath = SingletonConfig.getInstance()
                    .getConfigValue(CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH);
            Path exportScriptFullPath = Paths.get(cockpitExportScriptPath, SCRIPT_NAME);

            if (!Files.isRegularFile(exportScriptFullPath)) {
                String msg = String.format(
                        "Cannot find export script at \"%s\": did you set the correct value for %s configuration?",
                        exportScriptFullPath, CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH);
                IllegalStateException ex = new IllegalStateException(msg);
                LOGGER.error(msg, ex);
                throw ex;
            }

            URI url = UriBuilder.fromUri(requestURL).replaceQueryParam("outputType_description", "HTML")
                    .replaceQueryParam("outputType", "HTML").build();

            // avoid sonar security hotspot issue
            String cockpitExportExternalProcessName = SingletonConfig.getInstance()
                    .getConfigValue("KNOWAGE.DASHBOARD.EXTERNAL_PROCESS_NAME");
            LOGGER.info("CONFIG label=\"KNOWAGE.DASHBOARD.EXTERNAL_PROCESS_NAME\": " + cockpitExportExternalProcessName);

            ProcessBuilder processBuilder = new ProcessBuilder(cockpitExportExternalProcessName, exportScriptFullPath.toString(),
                    encodedUserId, outputDir.toString(), url.toString());

            setWorkingDirectory(cockpitExportScriptPath, processBuilder);

            LOGGER.info("Node complete command line: {}", processBuilder.command());

            LOGGER.info("Starting export script");
            Process exec = processBuilder.start();

            logOutputToCoreLog(exec);

            LOGGER.info("Waiting...");
            exec.waitFor();
            LOGGER.warn("Exit value: {}", exec.exitValue());

            // the script creates the resulting xls and saves it to outputFile
            Path outputFile = PathTraversalChecker.get(outputDir.toString(), documentLabel + ".xlsx").toPath();
            return getByteArrayFromFile(outputFile, outputDir);
        } catch (Exception e) {
            LOGGER.error("Error during scheduled export execution", e);
            throw e;
        }
    }

    private byte[] getByteArrayFromFile(Path excelFile, Path outputDir) {
        String fileName = excelFile.toString();

        try (FileInputStream fis = new FileInputStream(fileName);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[1024];
            for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                // Writes len bytes from the specified byte array starting at offset off to this byte array output stream
                bos.write(buf, 0, readNum); // no doubt here is 0
            }
            return bos.toByteArray();
        } catch (Exception e) {
            LOGGER.error("Cannot serialize excel file", e);
            throw new SpagoBIRuntimeException("Cannot serialize excel file", e);
        } finally {
            try {
                if (Files.isRegularFile(excelFile)) {
                    Files.delete(excelFile);
                }
                Files.delete(outputDir);
            } catch (Exception e) {
                LOGGER.error("Cannot delete temp file", e);
            }
        }
    }

    public byte[] getBinaryData(Integer documentId, String documentLabel, String documentName, String templateString, String options)
            throws JSONException {
        // set document name for exporting
        this.documentName = documentName;

        if (templateString == null) {
            ObjTemplate template = null;
            String message = "Unable to get template for document with id [" + documentId + "] and label ["
                    + documentLabel + "]";
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

        int windowSize = Integer.parseInt(
                SingletonConfig.getInstance().getConfigValue("KNOWAGE.DASHBOARD.EXPORT.EXCEL.STREAMING_WINDOW_SIZE"));
        try (Workbook wb = new SXSSFWorkbook(windowSize)) {

            int exportedSheets = 0;
            if (isSingleWidgetExport) {
                long widgetId = body.getLong("widget");
                String widgetType = getWidgetTypeFromCockpitTemplate(templateString, widgetId);
                JSONObject optionsObj = new JSONObject();
                if (options != null && !options.isEmpty()) {
                    optionsObj = new JSONObject(options);
                }
                IWidgetExporter widgetExporter = WidgetExporterFactory.getExporter(this, widgetType, templateString,
                        widgetId, wb, optionsObj);
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
                        adjustColumnWidth(sheet, this.imageB64);
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


    public byte[] getDashboardBinaryData(JSONObject body, boolean isDashboardSingleWidgetExport) {

        if (body == null) {
            throw new SpagoBIRuntimeException("Unable to get template for dashboard");
        }
        String stringifiedBody = body.toString();
        int windowSize = Integer.parseInt(
                SingletonConfig.getInstance().getConfigValue("KNOWAGE.DASHBOARD.EXPORT.EXCEL.STREAMING_WINDOW_SIZE"));
        try (Workbook wb = new SXSSFWorkbook(windowSize)) {

            int exportedSheets = 0;
            if (isDashboardSingleWidgetExport) {
                exportedSheets = exportWidget(body, wb, null);
            } else {
                JSONArray widgetsJson = getDashboardWidgetsJson(stringifiedBody);
                //TODO OPTIONS FOR PIVOT TABLE
//				JSONObject optionsObj = buildOptionsForCrosstab(stringifiedBody);
                exportedSheets += exportDashboard(widgetsJson, wb, getDocumentName(body));
            }

            if (exportedSheets == 0) {
                exportEmptyExcel(wb);
            } else {
                for (Sheet sheet : wb) {
                    if (sheet != null) {
                        // Adjusts the column width to fit the contents
                        adjustColumnWidth(sheet, this.imageB64);
                    }
                }
            }

            byte[] ret;
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

    private String getDocumentName(JSONObject template) {
        try {
            return template.getJSONObject("document").getString("label");
        } catch (Exception e) {
            LOGGER.info("Cannot get document name", e);
            return null;
        }
    }

    private int exportDashboard(JSONArray widgetsArray, Workbook wb, String documentName) {
        int exportedSheets = 0;
        for (int i = 0; i < widgetsArray.length(); i++) {
            try {
                JSONObject currWidget = widgetsArray.getJSONObject(i);
                exportedSheets = exportWidget(currWidget, wb, documentName);
            } catch (Exception e) {
                LOGGER.error("Error while exporting widget", e);
            }
        }
        Map<String, Map<String, Object>> selectionsMap;
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

        Map<String, Map<String, Object>> driversMap;
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

    public int exportWidget(JSONObject body, Workbook wb, String documentName) {

        int exportedSheets;
        try {
            String widgetType = body.getString("type");
            if (Arrays.asList(WIDGETS_TO_IGNORE).contains(widgetType.toLowerCase())) {
                return 0;
            }
            IWidgetExporter widgetExporter = DashboardWidgetExporterFactory.getExporter(this,
                    wb, body, documentName);
            exportedSheets = widgetExporter.export();
        } catch (Exception e) {
            LOGGER.error("Cannot export data to excel", e);
            throw new SpagoBIRuntimeException("Cannot export data to excel", e);
        }
        return exportedSheets;
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
                    if (!widget.getString("type").equals("static-pivot-table")) {
                        continue;
                    }
                    long widgetId = widget.getLong("id");
                    JSONObject options = new JSONObject();
                    JSONObject widgetContentFromTemplate = widget.optJSONObject("content");
                    JSONObject widgetContentFromBody = getWidgetContentFromBody(widget);
                    try {
                        // config retrieved from static object
                        options.put("config", new JSONObject().put("type", "pivot"));

                        // sortOptions retrieved from template otherwise from request body
                        if (!ObjectUtils.isEmpty(widgetContentFromTemplate) && !widgetContentFromTemplate.isNull("sortOptions")) {
                            options.put("sortOptions", widgetContentFromTemplate.getJSONObject("sortOptions"));
                        } else if (!ObjectUtils.isEmpty(widgetContentFromBody) && !widgetContentFromBody.isNull("sortOptions")) {
                            options.put("sortOptions", widgetContentFromBody.getJSONObject("sortOptions"));
                        } else {
                            options.put("sortOptions", new JSONObject());
                        }

                        // name retrieved from template otherwise from request body
                        if (!ObjectUtils.isEmpty(widgetContentFromTemplate) && !widgetContentFromTemplate.isNull("name")) {
                            options.put("name", widgetContentFromTemplate.getString("name"));
                        } else if (!ObjectUtils.isEmpty(widgetContentFromBody) && !widgetContentFromBody.isNull("name")) {
                            options.put("name", widgetContentFromBody.getString("name"));
                        } else {
                            options.put("name", new JSONObject());
                        }

                        // crosstabDefinition retrieved from template otherwise from request body
                        if (!ObjectUtils.isEmpty(widgetContentFromTemplate) && !widgetContentFromTemplate.isNull("crosstabDefinition")) {
                            options.put("crosstabDefinition", widgetContentFromTemplate.getJSONObject("crosstabDefinition"));
                        } else if (!ObjectUtils.isEmpty(widgetContentFromBody) && !widgetContentFromBody.isNull("crosstabDefinition")) {
                            options.put("crosstabDefinition", widgetContentFromBody.getJSONObject("crosstabDefinition"));
                        } else {
                            options.put("crosstabDefinition", new JSONObject());
                        }

                        // style retrieved from template otherwise from request body
                        if (!ObjectUtils.isEmpty(widgetContentFromTemplate) && !widgetContentFromTemplate.isNull("style")) {
                            options.put("style", widgetContentFromTemplate.getJSONObject("style"));
                        } else if (!ObjectUtils.isEmpty(widgetContentFromBody) && !widgetContentFromBody.isNull("style")) {
                            options.put("style", widgetContentFromBody.getJSONObject("style"));
                        } else {
                            options.put("style", new JSONObject());
                        }

                        // variables cannot be retrieved from template so we must recover them from request body
                        options.put("variables", getCockpitVariables());

                        ExporterClient client = new ExporterClient();
                        int datasetId = widget.getJSONObject("dataset").getInt("dsId");
                        String dsLabel = getDatasetLabel(template, datasetId);
                        String selections = getCockpitSelectionsFromBody(widget).toString();
                        JSONObject configuration = template.getJSONObject("configuration");
                        Map<String, Object> parametersMap = new HashMap<>();
                        if (getRealtimeFromWidget(datasetId, configuration)) {
                            parametersMap.put("nearRealtime", true);
                        }
                        JSONObject datastore = client.getDataStore(parametersMap, dsLabel, userUniqueIdentifier,
                                selections);
                        options.put("metadata", datastore.getJSONObject("metaData"));
                        options.put("jsonData", datastore.getJSONArray("rows"));
                        toReturn.put(String.valueOf(widgetId), options);
                    } catch (Exception e) {
                        LOGGER.warn(
                                "Cannot build crosstab options for widget {}. Only raw data without formatting will be exported.",
                                widgetId, e);
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
        if (body == null || body.length() == 0) {
            return cockpitSelections;
        }
        try {
            if (isSingleWidgetExport) { // export single widget
                cockpitSelections = body.getJSONObject("COCKPIT_SELECTIONS");
            } else { // export whole cockpit
                JSONArray allWidgets = body.getJSONArray("widget");
                int i;
                for (i = 0; i < allWidgets.length(); i++) {
                    JSONObject curWidget = allWidgets.getJSONObject(i);
                    if (curWidget.getLong("id") == widget.getLong("id")) {
                        break;
                    }
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

    @Override
    protected JSONObject getDashboardSelections(JSONObject widget, String datasetLabel) {
        JSONObject dashboardSelections;
        try {
            JSONArray columns = widget.getJSONArray("columns");
            JSONObject settings = widget.getJSONObject("settings");

            dashboardSelections = buildDashboardSelections(columns, datasetLabel, settings);
        } catch (Exception e) {
            LOGGER.error("Cannot get dashboard selections", e);
            throw new SpagoBIRuntimeException("Cannot get dashboard selections", e);
        }
        return dashboardSelections;
    }

    @Override
    protected JSONObject getPivotSelections(JSONObject widget, String datasetLabel) {
        JSONObject pivotSelections;
        try {
            JSONObject settings = widget.getJSONObject("settings");
            JSONObject fields = widget.getJSONObject("fields");

            pivotSelections = buildPivotSelections(fields, datasetLabel, settings);
        } catch (Exception e) {
            LOGGER.error("Cannot get pivot selections", e);
            throw new SpagoBIRuntimeException("Cannot get pivot selections", e);
        }
        return pivotSelections;
    }

    private JSONObject buildPivotSelections(JSONObject fields, String datasetLabel, JSONObject settings) {
        JSONObject selections = new JSONObject();
        try {
            selections.put("aggregations", new JSONObject());
            JSONObject aggregations = selections.getJSONObject("aggregations");
            aggregations.put("measures", new JSONArray());
            JSONArray measures = aggregations.getJSONArray("measures");
            aggregations.put("categories", new JSONArray());
            JSONArray categories = aggregations.getJSONArray("categories");

            JSONArray columns = fields.getJSONArray("columns");
            JSONArray data = fields.getJSONArray("data");

            for (int i = 0; i < columns.length(); i++) {
                JSONObject category = getCategory(columns.getJSONObject(i), new JSONObject(), new JSONObject());
                categories.put(category);
            }
            for (int i = 0; i < data.length(); i++) {
                JSONObject measure = getMeasure(data.getJSONObject(i), new JSONObject(), new JSONObject());
                measures.put(measure);
            }
            aggregations.put("dataset", datasetLabel);
        } catch (Exception e) {
            LOGGER.error("Cannot build dashboard selections", e);
            throw new SpagoBIRuntimeException("Cannot build dashboard selections", e);
        }
        return selections;

    }

    private JSONObject buildDashboardSelections(JSONArray columns, String datasetLabel, JSONObject settings) {
        JSONObject selections = new JSONObject();
        String sortingColumnId = settings.optString("sortingColumn");
        String sortingOrder = settings.optString("sortingOrder");

        try {
            selections.put("aggregations", new JSONObject());
            JSONObject aggregations = selections.getJSONObject("aggregations");
            aggregations.put("measures", new JSONArray());
            JSONArray measures = aggregations.getJSONArray("measures");
            aggregations.put("categories", new JSONArray());
            JSONArray categories = aggregations.getJSONArray("categories");

            for (int i = 0; i < columns.length(); i++) {
                JSONObject column = columns.getJSONObject(i);
                String orderTypeCol = column.optString("orderType");
                if (sortingColumnId.isEmpty() && !orderTypeCol.isEmpty()) {
                    sortingColumnId = column.getString("id");
                    sortingOrder = column.getString("orderType");
                }
                if (column.getString("fieldType").equalsIgnoreCase("measure")) {
                    JSONObject measure = getMeasure(columns.getJSONObject(i), getSortingObj(column, sortingColumnId, sortingOrder), getDrillSortingObj(column));
                    measures.put(measure);
                } else {
                    JSONObject category = getCategory(columns.getJSONObject(i), getSortingObj(column, sortingColumnId, sortingOrder), getDrillSortingObj(column));
                    categories.put(category);
                }
            }
            aggregations.put("dataset", datasetLabel);
        } catch (Exception e) {
            LOGGER.error("Cannot build dashboard selections", e);
            throw new SpagoBIRuntimeException("Cannot build dashboard selections", e);
        }
        return selections;
    }

    private JSONObject getDrillSortingObj(JSONObject column) {
        return column.optJSONObject("drillOrder");
    }

    private JSONObject getSortingObj(JSONObject column, String sortingColumnId, String sortingOrder) {
        JSONObject sortingObj = new JSONObject();
        try {
            if (column.getString("id").equals(sortingColumnId)) {
                sortingObj.put("sortingColumn", column.getString("columnName"));
                sortingObj.put("sortingOrder", sortingOrder);
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Cannot check if column is sorting column", e);
            throw new SpagoBIRuntimeException("Cannot check if column is sorting column", e);
        }
        return sortingObj;
    }

    private JSONObject getCategory(JSONObject column, JSONObject sortingObj, JSONObject drillSortingObj) {
        try {
            JSONObject category = new JSONObject();
            category.put("id", column.getString("columnName"));
            category.put("alias", column.getString("columnName"));
            category.put("columnName", column.getString("columnName"));
            category.put("funct", "NONE");
            String sorting = getSortingObj(sortingObj, drillSortingObj);
            buildSorting(sortingObj, drillSortingObj, sorting, category);
            return category;
        } catch (Exception e) {
            LOGGER.error("Cannot get category", e);
            throw new SpagoBIRuntimeException("Cannot get category", e);
        }
    }

    private JSONObject getMeasure(JSONObject column, JSONObject sortingObj, JSONObject drillSortingObj) {
        try {
            JSONObject measure = new JSONObject();
            measure.put("id", column.getString("columnName"));
            measure.put("alias", column.getString("columnName"));
            measure.put("columnName", column.getString("columnName"));
            measure.put("funct", column.getString("aggregation"));
            String sorting = getSortingObj(sortingObj, drillSortingObj);
            buildSorting(sortingObj, drillSortingObj, sorting, measure);
            return measure;
        } catch (Exception e) {
            LOGGER.error("Cannot get measure", e);
            throw new SpagoBIRuntimeException("Cannot get measure", e);
        }
    }

    private static void buildSorting(JSONObject sortingObj, JSONObject drillSortingObj, String sorting, JSONObject measure) throws JSONException {
        switch (sorting) {
            case BOTH -> {
                measure.put("orderType", sortingObj.getString("sortingOrder"));
                measure.put("orderColumn", sortingObj.getString("sortingColumn"));
                measure.put("drillOrder", drillSortingObj);
            }
            case SORTING_OBJ -> {
                measure.put("orderType", sortingObj.getString("sortingOrder"));
                measure.put("orderColumn", sortingObj.getString("sortingColumn"));
            }
            case DRILL_SORTING_OBJ -> measure.put("drillOrder", drillSortingObj);
            default -> {
                measure.put("orderType", "");
                measure.put("orderColumn", "");
            }
        }
    }

    private String getSortingObj(JSONObject sortingObj, JSONObject drillSortingObj) {
        if (objectsAreNotEmpty(sortingObj, drillSortingObj)) {
            return BOTH;
        } else if (objectsAreNotEmpty(sortingObj)) {
            return SORTING_OBJ;
        } else if (objectsAreNotEmpty(drillSortingObj)) {
            return DRILL_SORTING_OBJ;
        } else {
            return "";
        }
    }

    private boolean objectsAreNotEmpty(JSONObject... objects) {
        for (JSONObject obj : objects) {
            if (obj == null || obj.length() == 0) {
                return false;
            }
        }
        return true;
    }


    private String getDatasetLabel(JSONObject template, int dsId) {
        try {
            JSONArray cockpitDatasets = template.getJSONObject("configuration").getJSONArray("datasets");
            for (int i = 0; i < cockpitDatasets.length(); i++) {
                int currDsId = cockpitDatasets.getJSONObject(i).getInt("dsId");
                if (currDsId == dsId) {
                    return cockpitDatasets.getJSONObject(i).getString("dsLabel");
                }
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Cannot retrieve dataset label for dsId: " + dsId, e);
        }
        throw new SpagoBIRuntimeException("No dataset found with dsId: " + dsId);
    }

    private JSONArray getWidgetsJson(String templateString) {
        try {
            if (body != null && body.has("widget")) {
                return body.getJSONArray("widget");
            } else {
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

    private JSONArray getDashboardWidgetsJson(String templateString) {
        try {
            JSONArray toReturn = new JSONArray();
            JSONObject template = new JSONObject(templateString);
            JSONArray widgets = template.getJSONArray("widgets");
            for (int i = 0; i < widgets.length(); i++) {
                JSONObject widget = widgets.getJSONObject(i);
                toReturn.put(widget);
            }
            return toReturn;
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
                if (Arrays.asList(WIDGETS_TO_IGNORE).contains(widgetType.toLowerCase())) {
                    continue;
                }
                JSONObject currWidgetOptions = new JSONObject();
                if (optionsObj.has(widgetId)) {
                    currWidgetOptions = optionsObj.getJSONObject(widgetId);
                }
                IWidgetExporter widgetExporter = WidgetExporterFactory.getExporter(this, widgetType, templateString,
                        Long.parseLong(widgetId), wb, currWidgetOptions);
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
        Map<String, Object> map = new HashMap<>();
        JSONArray multiDataStore = new JSONArray();
        try {
            JSONObject configuration = template.getJSONObject("configuration");
            JSONArray datasetIds = widget.getJSONArray("datasetId");
            for (int i = 0; i < datasetIds.length(); i++) {
                int datasetId = datasetIds.getInt(i);
                IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
                String datasetLabel = dataset.getLabel();
                JSONObject cockpitSelections = getMultiCockpitSelectionsFromBody(widget, datasetId);
                if (isEmptyLayer(cockpitSelections)) {
                    continue;
                }

                if (getRealtimeFromWidget(datasetId, configuration)) {
                    map.put("nearRealtime", true);
                }

                JSONArray summaryRow = getSummaryRowFromWidget(widget);
                if (summaryRow != null) {
                    cockpitSelections.put("summaryRow", summaryRow);
                }

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
            if (measures.length() > 0 || categories.length() > 0) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            LOGGER.warn("Error while checking if layer is empty", e);
            return false;
        }
    }

    public JSONObject getDataStoreForWidget(JSONObject template, JSONObject widget) {
        // if pagination is disabled offset = 0, fetchSize = -1
        return getDataStoreForWidget(template, widget, 0, -1);
    }

    public JSONObject getDataStoreforDashboardSingleWidget(JSONObject singleWidget) {
        return getDataStoreForDashboardWidget(singleWidget, 0, -1);
    }

    private JSONObject getMultiCockpitSelectionsFromBody(JSONObject widget, int datasetId) {
        JSONObject cockpitSelections = new JSONObject();
        JSONArray allSelections = new JSONArray();
        try {
            if (body == null || body.length() == 0) {
                return cockpitSelections;
            }
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
                    if (curWidget.getString("id").equals(widget.getString("id"))) {
                        break;
                    }
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

    private void fillSelectionsSheetWithData(Map<String, Map<String, Object>> selectionsMap, Workbook wb, Sheet sheet,
                                             String widgetName) {

        // CREATE BRANDED HEADER SHEET
        this.imageB64 = OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName());
        int startRow = 0;
        float rowHeight = 35; // in points
        int rowspan = 2;
        int startCol = 0;
        int colWidth = 25;
        int colspan = 2;
        int namespan = 10;
        int dataspan = 10;

        Row newheader;

        int headerIndex = createBrandedHeaderSheet(
                sheet,
                this.imageB64,
                startRow,
                rowHeight,
                rowspan,
                startCol,
                colWidth,
                colspan,
                namespan,
                dataspan,
                this.documentName,
                sheet.getSheetName());

        newheader = sheet.createRow((short) headerIndex + 1); // first row

        Cell cell = newheader.createCell(0);
        cell.setCellValue("Dataset");
        CellStyle headerCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 11);
        cell.setCellStyle(headerCellStyle);

        Cell cell2 = newheader.createCell(1);
        cell2.setCellValue("Field");
        cell2.setCellStyle(headerCellStyle);

        Cell cell3 = newheader.createCell(2);
        cell3.setCellValue("Values");
        cell3.setCellStyle(headerCellStyle);

        int j = headerIndex + 2;
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

    private void fillDriversSheetWithData(Map<String, Map<String, Object>> driversMap, Workbook wb, Sheet sheet,
                                          String widgetName) {

        // CREATE BRANDED HEADER SHEET
        this.imageB64 = OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName());
        int startRow = 0;
        float rowHeight = 35; // in points
        int rowspan = 2;
        int startCol = 0;
        int colWidth = 25;
        int colspan = 2;
        int namespan = 10;
        int dataspan = 10;

        Row newheader;

        int headerIndex = createBrandedHeaderSheet(
                sheet,
                this.imageB64,
                startRow,
                rowHeight,
                rowspan,
                startCol,
                colWidth,
                colspan,
                namespan,
                dataspan,
                this.documentName,
                sheet.getSheetName());

        newheader = sheet.createRow((short) headerIndex + 1);

        Cell cell = newheader.createCell(0);
        cell.setCellValue("Filter");
        CellStyle headerCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 11);
        cell.setCellStyle(headerCellStyle);

        Cell cell2 = newheader.createCell(1);
        cell2.setCellValue("Value");
        cell2.setCellStyle(headerCellStyle);

        List<BIObject> allDocuments = getDocuments();
        List<BIObjectParameter> drivers = getDriversByDocumentName(allDocuments, this.documentName);

        int j = headerIndex + 2;
        for (String key : driversMap.keySet()) {

            Row row = sheet.createRow(j++);

            Cell cellData0 = row.createCell(0);
            cellData0.setCellValue(getDriverLabelByParameterUrlName(drivers, key));

            Cell cellData1 = row.createCell(1);
            if (driversMap.get(key).keySet().contains("description")) {
                cellData1.setCellValue(extractSelectionValues("" + driversMap.get(key).get("description")));
            } else {
                cellData1.setCellValue(extractSelectionValues("" + driversMap.get(key).get("value")));
            }
        }

    }

    private List<BIObject> getDocuments() {
        IBIObjectDAO documentsDao = null;
        List<BIObject> allDocuments = null;
        try {
            documentsDao = DAOFactory.getBIObjectDAO();
            allDocuments = documentsDao.loadAllBIObjects();
        } catch (Exception e) {
            LOGGER.debug("Documents objects can not be provided", e);
        }
        return allDocuments;
    }

    private List<BIObjectParameter> getDriversByDocumentName(List<BIObject> allDocuments, String documentName) {
        List<BIObjectParameter> drivers = null;
        try {
            for (BIObject document : allDocuments) {
                if (document.getName().equals(documentName)) {
                    drivers = document.getDrivers();
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Drivers objects can not be provided", e);
        }
        return drivers;
    }

    private String getDriverLabelByParameterUrlName(List<BIObjectParameter> drivers, String parameterUrlName) {
        String label = "";
        try {
            for (BIObjectParameter driver : drivers) {
                if (driver.getParameterUrlName().equals(parameterUrlName)) {
                    label = driver.getLabel();
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Driver label can not be provided", e);
        }
        return label;
    }

    private String extractSelectionValues(String selectionValues) {
        return selectionValues.replace("[\"(", "").replace(")\"]", "");
    }

    private JSONArray getChildFromWidgetContent(JSONObject widgetContent, String childName) {
        JSONArray ret = new JSONArray();
        if (widgetContent.has(childName)) {
            JSONArray childArray = widgetContent.optJSONArray(childName);
            if (childArray != null) {
                ret = childArray;
            } else {
                JSONObject childObject = widgetContent.optJSONObject(childName);
                if (childObject != null) {
                    ret.put(childObject);
                }
            }
        }
        return ret;
    }

    public void fillSheetWithData(JSONObject dataStore, Workbook wb, Sheet sheet, String widgetName, int offset,
                                  JSONObject settings) {
        try {
            JSONObject metadata = dataStore.getJSONObject("metaData");
            JSONArray columns = metadata.getJSONArray("fields");
            columns = filterDataStoreColumns(columns);
            JSONArray rows = dataStore.getJSONArray("rows");
            HashMap<String, Object> variablesMap = new HashMap<>();
            JSONObject widgetData = dataStore.getJSONObject("widgetData");
            JSONObject widgetContent = widgetData.getJSONObject("content");
            JSONArray columnSelectedOfDataset = getChildFromWidgetContent(widgetContent, "columnSelectedOfDataset");
//			HashMap<String, String> arrayHeader = new HashMap<>();
            HashMap<String, String> chartAggregationsMap = new HashMap<>();
            if (widgetData.getString("type").equalsIgnoreCase("table")) {
//				ATTENTION: renaming table columns names of the excel export has been placed at the end
//				for (int i = 0; i < columnSelectedOfDataset.length(); i++) {
//					JSONObject column = columnSelectedOfDataset.getJSONObject(i);
//					String key;
//					if (column.optBoolean("isCalculated") && !column.has("name")) {
//						key = column.getString("alias");
//					} else {
//						key = column.getString("name");
//					}
//					// arrayHeader is used to rename table columns names of the excel export
//					arrayHeader.put(key, column.getString("aliasToShow"));
//				}
            } else if (widgetData.getString("type").equalsIgnoreCase("chart")) {
                for (int i = 0; i < columnSelectedOfDataset.length(); i++) {
                    JSONObject column = columnSelectedOfDataset.getJSONObject(i);
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
                                        col = col.replace("$V{" + columnAlias + "}",
                                                variableOBJ.getString(columnAlias));
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
                hiddenColumns = getHiddenColumnsList(columnSelectedOfDataset);
                columnsOrdered = getTableOrderedColumns(columnSelectedOfDataset, columns);
            } else if (widgetData.getString("type").equalsIgnoreCase("discovery") && widgetContent.has("columnSelectedOfDataset")) {
                columnsOrdered = getDiscoveryOrderedColumns(columnSelectedOfDataset, columns);
            } else {
                columnsOrdered = columns;
            }

            JSONArray groupsFromWidgetContent = getGroupsFromWidgetContent(widgetData);
            Map<String, String> groupsAndColumnsMap = getGroupAndColumnsMap(widgetContent, groupsFromWidgetContent);

            // CREATE BRANDED HEADER SHEET
            this.imageB64 = OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName());
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

                int headerIndex = createBrandedHeaderSheet(
                        sheet,
                        this.imageB64,
                        startRow,
                        rowHeight,
                        rowspan,
                        startCol,
                        colWidth,
                        colspan,
                        namespan,
                        dataspan,
                        this.documentName,
                        widgetName);

                header = createHeaderColumnNames(sheet, groupsAndColumnsMap, columnsOrdered, headerIndex + 1);

                for (int i = 0; i < columnsOrdered.length(); i++) {
                    JSONObject column = columnsOrdered.getJSONObject(i);
                    String columnName = column.getString("header");
                    String chartAggregation = null;
                    if (widgetData.getString("type").equalsIgnoreCase("table")) {
                        // renaming table columns names of the excel export
                        for (int j = 0; j < columnSelectedOfDataset.length(); j++) {
                            JSONObject columnSelected = columnSelectedOfDataset.getJSONObject(j);
                            if (columnSelected.has("aliasToShow") && columnName.equals(columnSelected.getString("aliasToShow"))) {
                                columnName = getTableColumnHeaderValue(columnSelected);
                                break;
                            }
                        }
                    } else if (widgetData.getString("type").equalsIgnoreCase("discovery")) {
                        // renaming table columns names of the excel export
                        for (int j = 0; j < columnSelectedOfDataset.length(); j++) {
                            JSONObject columnSelected = columnSelectedOfDataset.getJSONObject(j);
                            if (columnSelected.has("name") && columnName.equals(columnSelected.getString("name"))) {
                                columnName = getTableColumnHeaderValue(columnSelected);
                                break;
                            }
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

                    CellStyle headerCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 11);
                    cell.setCellStyle(headerCellStyle);
                }

                // adjusts the column width to fit the contents
                adjustColumnWidth(sheet, this.imageB64);
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

                if (StringUtils.isNotEmpty(imageB64)) {
                    row = sheet.createRow((offset + r + isGroup) + (startRow + rowspan) + 2); // starting by Header
                } else {
                    row = sheet.createRow((offset + r + isGroup) + 2);
                }

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
                                cell.setCellStyle(getStringCellStyle(wb, createHelper, column, columnStyles[c],
                                        FLOAT_CELL_DEFAULT_FORMAT, settings, s, rowObject, mapColumns, mapColumnsTypes,
                                        variablesMap, mapParameters));
                                break;
                            case "int":
                                if (!s.trim().isEmpty()) {
                                    cell.setCellValue(Integer.parseInt(s));
                                    cell.setCellStyle(getIntCellStyle(wb, createHelper, column, columnStyles[c],
                                            INT_CELL_DEFAULT_FORMAT, settings, Integer.parseInt(s), rowObject, mapColumns,
                                            mapColumnsTypes, variablesMap, mapParameters));
                                } else {
                                    cell.setCellStyle(getGenericCellStyle(wb, createHelper, column, columnStyles[c],
                                            INT_CELL_DEFAULT_FORMAT, settings, rowObject, mapColumns, mapColumnsTypes,
                                            variablesMap, mapParameters));
                                }
                                break;
                            case "float":
                                if (!s.trim().isEmpty()) {
                                    cell.setCellValue(Double.parseDouble(s));
                                    cell.setCellStyle(getDoubleCellStyle(wb, createHelper, column, columnStyles[c],
                                            FLOAT_CELL_DEFAULT_FORMAT, settings, Double.parseDouble(s), rowObject,
                                            mapColumns, mapColumnsTypes, variablesMap, mapParameters));
                                } else {
                                    cell.setCellStyle(getGenericCellStyle(wb, createHelper, column, columnStyles[c],
                                            FLOAT_CELL_DEFAULT_FORMAT, settings, rowObject, mapColumns, mapColumnsTypes,
                                            variablesMap, mapParameters));
                                }
                                break;
                            case "date":
                                try {
                                    if (!s.trim().isEmpty()) {
                                        Date date = dateFormat.parse(s);
                                        cell.setCellValue(date);
                                        cell.setCellStyle(getDateCellStyle(wb, createHelper, column, columnStyles[c],
                                                DATE_FORMAT, settings, rowObject, mapColumns, mapColumnsTypes, variablesMap,
                                                mapParameters));
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
                                        cell.setCellStyle(getDateCellStyle(wb, createHelper, column, columnStyles[c],
                                                TIMESTAMP_FORMAT, settings, rowObject, mapColumns, mapColumnsTypes,
                                                variablesMap, mapParameters));
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

    public void fillGenericWidgetSheetWithData(JSONObject dataStore, Workbook wb, Sheet sheet, String widgetName, int offset,
                                               JSONObject settings) {
        try {
            JSONObject metadata = dataStore.getJSONObject("metaData");
            JSONArray columns = metadata.getJSONArray("fields");
            columns = filterDataStoreColumns(columns);
            JSONArray rows = dataStore.getJSONArray("rows");
            HashMap<String, Object> variablesMap = new HashMap<>();
            JSONObject widgetData = dataStore.getJSONObject("widgetData");
            HashMap<String, String> chartAggregationsMap = new HashMap<>();

            JSONArray columnsOrdered = columns;

            JSONArray groupsFromWidgetContent = getGroupsFromWidgetContent(widgetData);
            Map<String, String> groupsAndColumnsMap = getDashboardGroupAndColumnsMap(widgetData, groupsFromWidgetContent);

            // CREATE BRANDED HEADER SHEET
            this.imageB64 = OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName());
            int startRow = 0;
            float rowHeight = 35; // in points
            int rowspan = 2;
            int startCol = 0;
            int colWidth = 25;
            int colspan = 2;
            int namespan = 10;
            int dataspan = 10;

            if (offset == 0) { // if pagination is active, headers must be created only once
                Row header;

                int headerIndex = createBrandedHeaderSheet(
                        sheet,
                        this.imageB64,
                        startRow,
                        rowHeight,
                        rowspan,
                        startCol,
                        colWidth,
                        colspan,
                        namespan,
                        dataspan,
                        this.documentName,
                        widgetName);

                header = createDashboardHeaderColumnNames(sheet, groupsAndColumnsMap, columnsOrdered, headerIndex + 1);

                for (int i = 0; i < columnsOrdered.length(); i++) {
                    JSONObject column = columnsOrdered.getJSONObject(i);
                    String columnName = column.getString("header");
                    String chartAggregation;

                    chartAggregation = chartAggregationsMap.get(columnName);

                    if (chartAggregation != null) {
                        columnName = columnName.split("_" + chartAggregation)[0];
                    }

                    columnName = getInternationalizedHeader(columnName);

                    if (chartAggregation != null) {
                        columnName = columnName + "_" + chartAggregation;
                    }

                    Cell cell = header.createCell(i);

                    cell.setCellValue(columnName);
                }

                // adjusts the column width to fit the contents
                adjustColumnWidth(sheet, this.imageB64);
            }


            // cell styles for table widget
            JSONObject[] columnStyles = new JSONObject[columnsOrdered.length() + 10];
            HashMap<String, String> mapColumns = new HashMap<>();
            HashMap<String, String> mapColumnsTypes = new HashMap<>();
            HashMap<String, Object> mapParameters = new HashMap<>();

            variablesMap = createMapVariables(variablesMap);
            // FILL RECORDS
            int isGroup = groupsAndColumnsMap.isEmpty() ? 0 : 1;
            for (int r = 0; r < rows.length(); r++) {
                JSONObject rowObject = rows.getJSONObject(r);
                Row row;

                if (StringUtils.isNotEmpty(imageB64)) {
                    row = sheet.createRow((offset + r + isGroup) + (startRow + rowspan) + 2); // starting by Header
                } else {
                    row = sheet.createRow((offset + r + isGroup) + 2);
                }

                for (int c = 0; c < columnsOrdered.length(); c++) {
                    JSONObject column = columnsOrdered.getJSONObject(c);
                    String type = getCellType(column, column.getString("name"));
                    String colIndex = column.getString("name"); // column_1, column_2, column_3...

                    Cell cell = row.createCell(c);
                    Object value = rowObject.get(colIndex);

                    if (value != null) {
                        String stringifiedValue = value.toString();
                        doTypeLogic(wb, -1, type, cell, stringifiedValue);
                    }
                }
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Cannot write data to Excel file", e);
        }

    }

    private void doTypeLogic(Workbook wb, int precision, String type, Cell cell, String stringifiedValue) {
        CreationHelper creationHelper = wb.getCreationHelper();

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, getLocale());

        SimpleDateFormat timeStampFormat = new SimpleDateFormat(TIMESTAMP_FORMAT, getLocale());
        switch (type) {
            case "int":
                if (!stringifiedValue.trim().isEmpty()) {
                    cell.getCellStyle().setDataFormat(getFormat(precision, creationHelper));
                    cell.setCellValue(Integer.parseInt(stringifiedValue));
                }
                break;
            case "float":
                if (!stringifiedValue.trim().isEmpty()) {
                    cell.getCellStyle().setDataFormat(getFormat(precision, creationHelper));
                    cell.setCellValue(Double.parseDouble(stringifiedValue));
                }
                break;
            case "date":
                try {
                    if (!stringifiedValue.trim().isEmpty()) {
                        Date date = dateFormat.parse(stringifiedValue);
                        cell.setCellValue(date);
                    }
                } catch (Exception e) {
                    LOGGER.debug("Date will be exported as string due to error: ", e);
                    cell.setCellValue(stringifiedValue);
                }
                break;
            case "timestamp":
                try {
                    if (!stringifiedValue.trim().isEmpty()) {
                        Date ts = timeStampFormat.parse(stringifiedValue);
                        cell.setCellValue(ts);
                    }
                } catch (Exception e) {
                    LOGGER.debug("Timestamp will be exported as string due to error: ", e);
                    cell.setCellValue(stringifiedValue);
                }
                break;
            default:
                cell.setCellValue(stringifiedValue);
                break;
        }
    }

    private short getFormat(int precision, CreationHelper helper) {
        String format = "0";
        if (precision != -1) {
            format = getNumberFormatByPrecision(precision, format);
            return helper.createDataFormat().getFormat(format);
        }
        return helper.createDataFormat().getFormat(format);
    }

    public void fillTableSheetWithData(JSONObject dataStore, Workbook wb, Sheet sheet, String widgetName, int offset,
                                       JSONObject settings) {
        try {
            JSONObject metadata = dataStore.getJSONObject("metaData");
            JSONArray columns = metadata.getJSONArray("fields");
            columns = filterDataStoreColumns(columns);
            JSONArray rows = dataStore.getJSONArray("rows");
            HashMap<String, Object> variablesMap = new HashMap<>();
            JSONObject widgetData = dataStore.getJSONObject("widgetData");
            JSONArray columnSelectedOfDataset = widgetData.getJSONArray("columns");

            JSONArray columnsOrdered;
            if (widgetData.has("columns")) {
                hiddenColumns = getHiddenColumnsList(columnSelectedOfDataset);
            }
            columnsOrdered = columns;

            JSONArray groupsFromWidgetContent = getGroupsFromWidgetContent(widgetData);
            Map<String, String> groupsAndColumnsMap = getDashboardGroupAndColumnsMap(widgetData, groupsFromWidgetContent);

            // CREATE BRANDED HEADER SHEET
            this.imageB64 = OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName());
            int startRow = 0;
            float rowHeight = 35; // in points
            int rowspan = 2;
            int startCol = 0;
            int colWidth = 25;
            int colspan = 2;
            int namespan = 10;
            int dataspan = 10;

            if (offset == 0) { // if pagination is active, headers must be created only once
                Row header;
                int headerIndex = createBrandedHeaderSheet(
                        sheet,
                        this.imageB64,
                        startRow,
                        rowHeight,
                        rowspan,
                        startCol,
                        colWidth,
                        colspan,
                        namespan,
                        dataspan,
                        this.documentName,
                        widgetName);

                header = createDashboardHeaderColumnNames(sheet, groupsAndColumnsMap, columnsOrdered, headerIndex + 1);

                for (int i = 0; i < columnsOrdered.length(); i++) {
                    JSONObject column = columnsOrdered.getJSONObject(i);
                    String columnName = column.getString("header");
                    // renaming table columns names of the excel export
                    for (int j = 0; j < columnSelectedOfDataset.length(); j++) {
                        JSONObject columnSelected = columnSelectedOfDataset.getJSONObject(j);
                        if (columnSelected.has("aliasToShow") && columnName.equals(columnSelected.getString("aliasToShow"))) {
                            columnName = getTableColumnHeaderValue(columnSelected);
                            break;
                        }
                    }

                    columnName = getInternationalizedHeader(columnName);

                    Cell cell = header.createCell(i);
                    cell.setCellValue(columnName);

                    CellStyle headerCellStyle;
                    XSSFFont font = (XSSFFont) wb.createFont();
                    if (settings != null && settings.has("style") && settings.getJSONObject("style").has("headers")) {
                        Style style = getStyleCustomObjFromProps(sheet, settings.getJSONObject("style").getJSONObject("headers"), "");
                        headerCellStyle = buildPoiCellStyle(style, font, wb);
                    } else {
                        headerCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 11);
                    }
                    cell.setCellStyle(headerCellStyle);
                }

                // adjusts the column width to fit the contents
                adjustColumnWidth(sheet, this.imageB64);
            }

            // cell styles for table widget
//			hashmap<string, string> mapcolumns;
//			hashmap<string, string> mapcolumnstypes;
//			hashmap<string, object> mapparameters = new hashmap<>();
//			mapcolumns = getcolumnsmap(columnsordered);
//			mapcolumnstypes = getcolumnsmaptypes(columnsordered);
//			mapparameters = createmapparameters(mapparameters);
//			variablesmap = createmapvariables(variablesmap);
            int isGroup = groupsAndColumnsMap.isEmpty() ? 0 : 1;

            assert settings != null;

            Map<String, JSONArray> columnStylesMap = getStylesMap(settings);
            Map<String, CellStyle> columnsCellStyles = new HashMap<>();
            CellStyle cellStyle = null;
            JSONObject alternatedRows = getRowStyle(settings);
            for (int r = 0; r < rows.length(); r++) {
                JSONObject rowObject = rows.getJSONObject(r);
                Row row;
                if (StringUtils.isNotEmpty(imageB64)) {
                    row = sheet.createRow((offset + r + isGroup) + (startRow + rowspan) + 2); // starting by Header
                } else {
                    row = sheet.createRow((offset + r + isGroup) + 2);
                }

                boolean rowIsEven = (r % 2 == 0);
                String rawCurrentNumberType = rowIsEven ? "even" : "odd";
                String defaultRowBackgroundColor = getDefaultRowBackgroundColor(alternatedRows, rowIsEven);

                boolean styleAlreadyAppliedToPreviousCells = false;
                String styleKeyToApplyToTheEntireRow = null;
                List<Boolean> styleCanBeOverriddenByWholeRowStyle = new ArrayList<>();
                for (int c = 0; c < columnsOrdered.length(); c++) {
                    JSONObject column = columnsOrdered.getJSONObject(c);
                    String type = getCellType(column, column.getString("name"));
                    String colIndex = column.getString("name"); // column_1, column_2, column_3...

                    Cell cell = row.createCell(c);
                    Object value = rowObject.get(colIndex);

                    String stringifiedValue = value != null ? value.toString() : "";
                    JSONObject theRightStyle = getTheRightStyleByColumnIdAndValue(columnStylesMap, stringifiedValue, column.optString("id"), defaultRowBackgroundColor);

                    styleCanBeOverriddenByWholeRowStyle.add(c, styleCanBeOverridden(theRightStyle));

                    String styleKey;
                    if (theRightStyle.has("applyToWholeRow") && theRightStyle.getBoolean("applyToWholeRow")) {
                        styleKey = getStyleKey(column, theRightStyle, rawCurrentNumberType);
                        if (!styleAlreadyAppliedToPreviousCells) {
                            cellStyle = getCellStyleByStyleKey(wb, sheet, styleKey, columnsCellStyles, theRightStyle, defaultRowBackgroundColor);
                            for (int previousCell = c - 1; previousCell >= 0; previousCell--) {
                                if (styleCanBeOverriddenByWholeRowStyle.get(previousCell).equals(Boolean.TRUE)) {
                                    row.getCell(previousCell).setCellStyle(cellStyle);
                                }
                            }
                            styleAlreadyAppliedToPreviousCells = true;
                        }
                        styleKeyToApplyToTheEntireRow = styleKey;
                    } else if (styleKeyToApplyToTheEntireRow != null && styleCanBeOverridden(theRightStyle)) {
                        cellStyle = columnsCellStyles.get(styleKeyToApplyToTheEntireRow);
                    } else {
                        styleKey = getStyleKey(column, theRightStyle, rawCurrentNumberType);
                        cellStyle = getCellStyleByStyleKey(wb, sheet, styleKey, columnsCellStyles, theRightStyle, defaultRowBackgroundColor);
                    }
                    cell.setCellStyle(cellStyle);
                    doTypeLogic(wb, getPrecisionByColumn(settings, column), type, cell, stringifiedValue);
                }
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Cannot write data to Excel file", e);
        }
    }

    private String getDefaultRowBackgroundColor(JSONObject altenatedRows, boolean rowIsEven) {
        try {
            if (altenatedRows != null && altenatedRows.getBoolean("enabled")) {
                if (rowIsEven) {
                    return altenatedRows.optString("evenBackgroundColor");
                } else {
                    return altenatedRows.optString("oddBackgroundColor");
                }
            }
        } catch (JSONException e) {
            LOGGER.error("Error while getting current row background color", e);
        }
        return "";
    }

    private JSONObject getRowStyle(JSONObject settings) {
        JSONObject style = settings.optJSONObject("style");
        if (style != null && style.has("rows")) {
            JSONObject rows = style.optJSONObject("rows");
            return rows.optJSONObject("alternatedRows");
        }
        return null;
    }

    private static boolean styleCanBeOverridden(JSONObject theRightStyle) throws JSONException {
        return (theRightStyle.has("type") && theRightStyle.getString("type").equals(STATIC_CUSTOM_STYLE)) || !theRightStyle.has("type");
    }

    private CellStyle getCellStyleByStyleKey(Workbook wb, Sheet sheet, String styleKey, Map<String, CellStyle> columnsCellStyles, JSONObject theRightStyle, String defaultRowBackgroundColor) {
        CellStyle cellStyle;
        if (columnAlreadyHasTheRightStyle(styleKey, columnsCellStyles)) {
            cellStyle = columnsCellStyles.get(styleKey);
        } else {
            Style styleCustomObj = getStyleCustomObjFromProps(sheet, theRightStyle, defaultRowBackgroundColor);
            cellStyle = buildPoiCellStyle(styleCustomObj, (XSSFFont) wb.createFont(), wb);
            columnsCellStyles.put(styleKey, cellStyle);
        }
        return cellStyle;
    }

    private static String getStyleKey(JSONObject column, JSONObject theRightStyle, String rawCurrentNumberType) throws JSONException {
        return column.optString("id").concat(theRightStyle.getString("type").concat(theRightStyle.getString("styleIndex").concat(rawCurrentNumberType)));
    }

    private boolean columnAlreadyHasTheRightStyle(String styleKey, Map<String, CellStyle> stylesMap) {
        return stylesMap.containsKey(styleKey);
    }

    private Map<String, JSONArray> getStylesMap(JSONObject settings) {
        Map<String, JSONArray> stylesMap = new HashMap<>();
        try {
            JSONObject columns = settings.getJSONObject("style").getJSONObject("columns");

            if (columns.getBoolean("enabled")) {
                JSONArray styles = columns.getJSONArray("styles");
                stylesMap = buildStylesMap(stylesMap, styles);
            }

            if (settings.has("conditionalStyles") && settings.getJSONObject("conditionalStyles").getBoolean("enabled")) {
                JSONObject conditionalStyles = settings.getJSONObject("conditionalStyles");
                JSONArray conditions = conditionalStyles.getJSONArray("conditions");

                stylesMap = buildStylesMap(stylesMap, conditions);
            }

        } catch (JSONException e) {
            LOGGER.debug("No styles found in settings", e);
            return stylesMap;
        }
        return stylesMap;
    }

    private Map<String, JSONArray> buildStylesMap(Map<String, JSONArray> stylesMap, JSONArray styles) throws JSONException {

        if (stylesMap == null) {
            stylesMap = new HashMap<>();
        }
        for (int i = 0; i < styles.length(); i++) {
            JSONObject style = styles.getJSONObject(i);
            JSONObject props = new JSONObject();
            if (style.has("condition")) {
                JSONObject condition = style.getJSONObject("condition");
                boolean applyToWholeRow = style.getBoolean("applyToWholeRow");
                props.put("applyToWholeRow", applyToWholeRow);
                props.put("condition", condition);
            }
            props.put("properties", style.getJSONObject("properties"));

            JSONArray target = getTarget(styles, i);

            for (int j = 0; j < target.length(); j++) {
                if (stylesMap.containsKey(target.getString(j))) {
                    stylesMap.get(target.getString(j)).put(props);
                } else {
                    JSONArray propertiesArray = new JSONArray();
                    propertiesArray.put(props);
                    stylesMap.put(target.getString(j), propertiesArray);
                }
            }
        }
        return stylesMap;
    }

    private static JSONArray getTarget(JSONArray styles, int i) throws JSONException {
        JSONArray target;
        try {
            target = styles.getJSONObject(i).getJSONArray("target");
        } catch (JSONException e) {
            target = new JSONArray();
            target.put(styles.getJSONObject(i).getString("target"));
        }
        return target;
    }

    private JSONObject getTheRightStyleByColumnIdAndValue(Map<String, JSONArray> styles, String stringifiedValue, String columnId, String defaultRowBackgroundColor) throws JSONException {
        JSONObject customStyle = getTheStyleByValueAndColumnId(styles, stringifiedValue, columnId);

        if (customStyle.has("properties") && customStyle.getJSONObject("properties").length() == 0) {
            return getTheStyleByValueAndColumnId(styles, stringifiedValue, ALL_COLUMNS_STYLE);
        }
        return customStyle;
    }


    private JSONObject getTheStyleByValueAndColumnId(Map<String, JSONArray> styles, String stringifiedValue, String columnId) {
        try {
            JSONObject nonConditionalProps = new JSONObject();
            if (styles != null) {
                JSONArray columnStyles = styles.get(columnId);

                if (columnStyles == null) {
                    if (styles.get(ALL_COLUMNS_STYLE) == null) {
                        return getStyleObject(nonConditionalProps, STATIC_CUSTOM_STYLE, 0, false);
                    } else {
                        columnStyles = styles.get(ALL_COLUMNS_STYLE);
                    }
                }

                for (int i = 0; i < columnStyles.length(); i++) {
                    JSONObject style = columnStyles.getJSONObject(i);
                    if (style.has("condition") && conditionIsApplicable(style.getJSONObject("condition"), stringifiedValue)) {
                        return getStyleObject(style.getJSONObject("properties"), CONDITIONAL_STYLE, i, style.getBoolean("applyToWholeRow"));
                    } else if (!style.has("condition")) {
                        nonConditionalProps = style.getJSONObject("properties");
                    }
                }
            } else {
                return getStyleObject(nonConditionalProps, STATIC_CUSTOM_STYLE, 0, false);
            }
            return getStyleObject(nonConditionalProps, STATIC_CUSTOM_STYLE, 0, false);
        } catch (JSONException e) {
            LOGGER.error("Error while checking if conditional style applies", e);
            throw new SpagoBIRuntimeException("Error while checking if conditional style applies", e);
        }
    }

    private JSONObject getStyleObject(JSONObject properties, String type, int styleIndex, boolean applyToWholeRow) {
        try {
            JSONObject style = new JSONObject();
            style.put("properties", properties);
            style.put("type", type);
            style.put("styleIndex", styleIndex);

            if (style.get("type").equals(CONDITIONAL_STYLE)) {
                style.put("applyToWholeRow", applyToWholeRow);
            }

            return style;
        } catch (JSONException e) {
            LOGGER.error("Error while building default non conditional style", e);
            throw new SpagoBIRuntimeException("Error while building default non conditional style", e);
        }
    }

    private boolean conditionIsApplicable(JSONObject condition, String stringifiedValue) {
        try {
            return switch (condition.optString("operator")) {
                case "==" -> {
                    try {
                        yield Double.parseDouble(stringifiedValue) == Double.parseDouble(condition.getString("value"));
                    } catch (RuntimeException rte) {
                        yield stringifiedValue.equals(condition.getString("value"));
                    }
                }
                case "!=" -> {
                    try {
                        yield Double.parseDouble(stringifiedValue) != Double.parseDouble(condition.getString("value"));
                    } catch (RuntimeException rte) {
                        yield stringifiedValue.equals(condition.getString("value"));
                    }
                }
                case ">" -> {
                    if (stringIsNotEmpty(stringifiedValue)) {
                        yield Double.parseDouble((stringifiedValue)) > Double.parseDouble(condition.getString("value"));
                    } else {
                        yield false;
                    }
                }
                case "<" -> {
                    if (stringIsNotEmpty(stringifiedValue)) {
                        yield Double.parseDouble(stringifiedValue) < Double.parseDouble(condition.getString("value"));
                    } else {
                        yield false;
                    }
                }
                case ">=" -> {
                    if (stringIsNotEmpty(stringifiedValue)) {
                        yield Double.parseDouble(stringifiedValue) >= Double.parseDouble(condition.getString("value"));
                    } else {
                        yield false;
                    }
                }
                case "<=" -> {
                    if (stringIsNotEmpty(stringifiedValue)) {
                        yield Double.parseDouble(stringifiedValue) <= Double.parseDouble(condition.getString("value"));
                    } else {
                        yield false;
                    }
                }
                case "IN" -> stringifiedValue.contains(condition.getString("value"));
                default -> false;
            };
        } catch (JSONException e) {
            LOGGER.error("Error while checking if condition is applicable", e);
            throw new SpagoBIRuntimeException("Error while checking if condition is applicable", e);
        }
    }

    private Style getStyleCustomObjFromProps(Sheet sheet, JSONObject props, String defaultRowBackgroundColor) {
        Style style = new Style();
        style.setSheet(sheet);
        props = props.optJSONObject("properties");
        style.setAlignItems(props.optString("align-items"));
        style.setJustifyContent(props.optString("justify-content"));
        style.setBackgroundColor(props.optString("background-color").isEmpty() ? defaultRowBackgroundColor : props.optString("background-color"));
        style.setColor(props.optString("color"));
        style.setFontSize(props.optString("font-size"));
        style.setFontWeight(props.optString("font-weight"));
        style.setFontStyle(props.optString("font-style"));

        return style;
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
        if (body.has("COCKPIT_SELECTIONS") && body.get("COCKPIT_SELECTIONS") instanceof JSONObject
                && body.getJSONObject("COCKPIT_SELECTIONS").has("drivers")) {
            mapParameters = getParametersMap(body.getJSONObject("COCKPIT_SELECTIONS").getJSONObject("drivers"));
        } else if (body.has("COCKPIT_SELECTIONS") && body.get("COCKPIT_SELECTIONS") instanceof JSONArray) {
            for (int j = 0; j < body.getJSONArray("COCKPIT_SELECTIONS").length(); j++) {
                if ((body.getJSONArray("COCKPIT_SELECTIONS").get(j) instanceof JSONArray)
                        && (!(body.getJSONArray("COCKPIT_SELECTIONS").get(j) instanceof JSONArray))
                        && body.getJSONArray("COCKPIT_SELECTIONS").getJSONObject(j).has("drivers")) {
                    mapParameters = getParametersMap(
                            body.getJSONArray("COCKPIT_SELECTIONS").getJSONObject(j).getJSONObject("drivers"));
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

    private void manageUserSelectionFromJSONObject(Map<String, Map<String, Object>> selectionsMap,
                                                   JSONObject cockpitSelection) throws JSONException {
        if (cockpitSelection.has("userSelections") && !((cockpitSelection.getJSONObject("userSelections")).length() == 0)) {
            manageUserSelectionFromJSONObjectUsingKey(selectionsMap, cockpitSelection, "userSelections");
        } else if (cockpitSelection.has("selections") && !((cockpitSelection.getJSONObject("selections")).length() == 0)) {
            // TODO : Map widget seems to have a different syntax
            manageUserSelectionFromJSONObjectUsingKey(selectionsMap, cockpitSelection, "selections");
        }
    }

    private void manageUserSelectionFromJSONObjectUsingKey(Map<String, Map<String, Object>> selectionsMap,
                                                           JSONObject cockpitSelection, String key) throws JSONException {
        JSONObject selections = cockpitSelection.getJSONObject(key);

        Iterator<String> keys = selections.keys();

        manageSingleUserSelection(selectionsMap, selections, keys);
    }

    private void manageSingleUserSelection(Map<String, Map<String, Object>> selectionsMap, JSONObject selections,
                                           Iterator<String> keys) throws JSONException {
        while (keys.hasNext()) {
            String key = keys.next();
            if (selections.get(key) instanceof JSONObject) {
                manageSelection(selectionsMap, selections, key);
            }
        }
    }

    private void manageSelection(Map<String, Map<String, Object>> selectionsMap, JSONObject selections, String key)
            throws JSONException {
        JSONObject selection = (JSONObject) selections.get(key);
        Iterator<String> selectionKeys = selection.keys();
        HashMap<String, Object> selects = new HashMap<>();
        while (selectionKeys.hasNext()) {
            String selKey = selectionKeys.next();
            Object select = selection.get(selKey);
            if (!selKey.contains(",")) {
                manageUserSelectionValue(selects, selKey, select);
            }
        }
        if (!selects.isEmpty()) {
            selectionsMap.put(key, selects);
        }
    }

    private void manageUserSelectionValue(HashMap<String, Object> selects, String selKey, Object select)
            throws JSONException {
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

    private void manageDriversFromJSONObject(Map<String, Map<String, Object>> selectionsMap,
                                             JSONObject cockpitSelection) throws JSONException {
        if (cockpitSelection.has("drivers")) {
            manageDriversFromJSONObjectUsingKey(selectionsMap, cockpitSelection, "drivers");
        }
    }

    private void manageDriversFromJSONObjectUsingKey(Map<String, Map<String, Object>> selectionsMap,
                                                     JSONObject cockpitSelection, String key) throws JSONException {
        JSONObject drivers = cockpitSelection.getJSONObject(key);

        Iterator<String> keys = drivers.keys();

        manageSingleDriver(selectionsMap, drivers, keys);
    }

    private void manageSingleDriver(Map<String, Map<String, Object>> selectionsMap, JSONObject drivers,
                                    Iterator<String> keys) throws JSONException {
        while (keys.hasNext()) {
            String key = keys.next();
            if (drivers.get(key) instanceof JSONArray) {
                manageDriver(selectionsMap, drivers, key);
            }
        }
    }

    private void manageDriver(Map<String, Map<String, Object>> selectionsMap, JSONObject drivers, String key)
            throws JSONException {
        JSONArray driver = (JSONArray) drivers.get(key);
        Iterator<String> driverKeys = ((JSONObject) driver.get(0)).keys();
        HashMap<String, Object> selects = new HashMap<>();
        while (driverKeys.hasNext()) {
            String selKey = driverKeys.next();
            Object select = ((JSONObject) driver.get(0)).get(selKey);
            if (!selKey.contains(",") && !select.toString().isEmpty()) {
                manageUserSelectionValue(selects, selKey, select);
            }
        }
        if (!selects.isEmpty()) {
            selectionsMap.put(key, selects);
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
        StringBuilder format = new StringBuilder(initialFormat);
        if (precision > 0) {
            format.append(".");
            format.append("0".repeat(precision));
        }
        return format.toString();
    }

    private Row createHeaderColumnNames(Sheet sheet, Map<String, String> groupsAndColumnsMap, JSONArray columnsOrdered,
                                        int startRowOffset) {
        try {
            Row header = null;
            if (!groupsAndColumnsMap.isEmpty()) {
                Row newheader = sheet.createRow((short) startRowOffset);
                for (int i = 0; i < columnsOrdered.length(); i++) {
                    JSONObject column = columnsOrdered.getJSONObject(i);
                    String groupName = groupsAndColumnsMap.get(column.get("header"));
                    if (groupName != null) {
                        // check if adjacent header cells have same group names in order to add merged region
                        int adjacents = getAdjacentEqualNamesAmount(groupsAndColumnsMap, columnsOrdered, i, groupName);
                        if (adjacents > 1) {
                            sheet.addMergedRegion(new CellRangeAddress(newheader.getRowNum(), // first row (0-based)
                                    newheader.getRowNum(), // last row (0-based)
                                    i, // first column (0-based)
                                    i + adjacents - 1 // last column (0-based)
                            ));
                        }
                        Cell cell = newheader.createCell(i);
                        cell.setCellValue(groupName);
                        i += adjacents - 1;
                    }
                }
                header = sheet.createRow((short) (startRowOffset + 1));
            } else {
                header = sheet.createRow((short) startRowOffset); // first row
            }
            return header;
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Couldn't create header column names", e);
        }
    }

    private Row createDashboardHeaderColumnNames(Sheet sheet, Map<String, String> groupsAndColumnsMap, JSONArray columnsOrdered,
                                                 int startRowOffset) {
        try {
            Row header;
            if (!groupsAndColumnsMap.isEmpty()) {
                Row newheader = sheet.createRow((short) startRowOffset);
                for (int i = 0; i < columnsOrdered.length(); i++) {
                    JSONObject column = columnsOrdered.getJSONObject(i);
                    String groupName = groupsAndColumnsMap.get(column.get("header"));
                    if (groupName != null) {
                        // check if adjacent header cells have same group names in order to add merged region
                        int adjacents = getAdjacentEqualNamesAmount(groupsAndColumnsMap, columnsOrdered, i, groupName);
                        if (adjacents > 1) {
                            sheet.addMergedRegion(new CellRangeAddress(newheader.getRowNum(), // first row (0-based)
                                    newheader.getRowNum(), // last row (0-based)
                                    i, // first column (0-based)
                                    i + adjacents - 1 // last column (0-based)
                            ));
                        }
                        Cell cell = newheader.createCell(i);
                        cell.setCellValue(groupName);
                        i += adjacents - 1;
                    }
                }
                header = sheet.createRow((short) (startRowOffset + 1));
            } else {
                header = sheet.createRow((short) startRowOffset); // first row
            }
            return header;
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Couldn't create header column names", e);
        }
    }


    private int getAdjacentEqualNamesAmount(Map<String, String> groupsAndColumnsMap, JSONArray columnsOrdered, int matchStartIndex, String groupNameToMatch) {
        try {
            int adjacents = 0;
            for (int i = matchStartIndex; i < columnsOrdered.length(); i++) {
                JSONObject column = columnsOrdered.getJSONObject(i);
                String groupName = groupsAndColumnsMap.get(column.get("header"));
                if (groupName != null && groupName.equals(groupNameToMatch)) {
                    adjacents++;
                } else {
                    return adjacents;
                }
            }
            return adjacents;
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Couldn't compute adjacent equal names amount", e);
        }
    }

    public Sheet createUniqueSafeSheet(Workbook wb, String widgetName, String cockpitSheetName) {
        Sheet sheet;
        String sheetName;
        try {
            if (!isSingleWidgetExport && cockpitSheetName != null && !cockpitSheetName.isEmpty()) {
                sheetName = cockpitSheetName.concat(".").concat(widgetName);
            } else {
                sheetName = widgetName;
            }
            String safeSheetName = WorkbookUtil.createSafeSheetName(sheetName);
            if (safeSheetName.length() +
                    "(".length() + String.valueOf(uniqueId).length() + "(".length() > SHEET_NAME_MAX_LEN) {
                safeSheetName = safeSheetName.substring(0, safeSheetName.length() -
                        "(".length() - String.valueOf(uniqueId).length() - ")".length());
            }
            String uniqueSafeSheetName = safeSheetName/* + String.valueOf(uniqueId)*/;
            try {
                sheet = wb.createSheet(uniqueSafeSheetName);
                uniqueId++;
                return sheet;
            } catch (Exception e) {
                sheet = wb.createSheet(uniqueSafeSheetName + "(" + uniqueId + ")");
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

    public void createPivotTable(Workbook workbook, Sheet sheet, JSONObject settings) {
        //create pivot

        // Crea una tabella pivot
        SXSSFWorkbook sxssfWorkbook = (SXSSFWorkbook) workbook;
        XSSFSheet pivotSheet = sxssfWorkbook.getXSSFWorkbook().createSheet("Pivot");
        XSSFPivotTable pivotTable = pivotSheet.createPivotTable(new AreaReference("A4:D239", workbook.getSpreadsheetVersion()), new CellReference("A1"), sheet);

        pivotTable.addRowLabel(0);
        pivotTable.addRowLabel(1);
    }
}

