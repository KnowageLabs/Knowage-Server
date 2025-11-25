package it.eng.knowage.engine.api.export.dashboard.excel;

import it.eng.knowage.commons.security.PathTraversalChecker;
import it.eng.knowage.engine.api.export.IWidgetExporter;
import it.eng.knowage.engine.api.export.dashboard.DashboardExporter;
import it.eng.knowage.engine.api.export.dashboard.Style;
import it.eng.knowage.engine.api.export.dashboard.excel.exporters.DashboardPivotExporter;
import it.eng.knowage.engine.api.export.dashboard.excel.exporters.DashboardWidgetExporterFactory;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import lombok.Getter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
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

import static it.eng.knowage.engine.api.export.dashboard.StaticLiterals.EXCEL_ERROR;
import static it.eng.knowage.engine.api.export.oldcockpit.exporters.CrossTabExporter.DEFAULT_FONT_NAME;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.poi.xssf.usermodel.XSSFFont.DEFAULT_FONT_SIZE;

public class DashboardExcelExporter extends DashboardExporter {
    private static final Logger LOGGER = LogManager.getLogger(DashboardExcelExporter.class);

    private static final String[] WIDGETS_TO_IGNORE = {"image", "text", "selector", "selection", "html", "static-pivot-table"};
    private static final int SHEET_NAME_MAX_LEN = 31;
    protected static final String DATE_FORMAT = "dd/MM/yyyy";

    // SCHEDULER
    private static final String CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH = "internal.nodejs.chromium.export.path";
    private static final String SCRIPT_NAME = "cockpit-export-xls.js";
    private static final String DOCUMENT_NAME = "";
    private String role;
    private String requestURL = "";
    private String organization = "";


    private final boolean isSingleWidgetExport;
    private int uniqueId = 0;
    @Getter
    protected final JSONObject body;

    public DashboardExcelExporter(String userUniqueIdentifier, JSONObject body, String imageB64) {
        super(userUniqueIdentifier, imageB64);
        this.isSingleWidgetExport = body.optBoolean("exportWidget");
        this.body = body;
        locale = getLocaleFromBody(body);
    }

    public DashboardExcelExporter(JSONObject body, String role, String requestUrl, String organization, String userUniqueIdentifier, String imageB64) {
        super(userUniqueIdentifier, imageB64);
        this.isSingleWidgetExport = body.optBoolean("exportWidget");
        this.body = body;
        locale = getLocaleFromBody(body);
        this.role = role;
        this.requestURL = requestUrl;
        this.organization = organization;
    }

    public byte[] getScheduledBinaryData(String documentLabel) throws IOException, InterruptedException {
        try {
            final Path outputDir = Files.createTempDirectory("knowage-xls-exporter-");

            String encodedUserId = Base64.encodeBase64String(getUserUniqueIdentifier().getBytes(UTF_8));
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

            URI url = UriBuilder.fromUri(requestURL)
                        .replaceQueryParam("outputType_description", "HTML")
                        .replaceQueryParam("outputType", "HTML")
                        .replaceQueryParam("role", role)
                        .replaceQueryParam("organization", organization)
                        .build();

            // avoid sonar security hotspot issue
            String cockpitExportExternalProcessName = SingletonConfig.getInstance()
                    .getConfigValue("KNOWAGE.DASHBOARD.EXTERNAL_PROCESS_NAME");
            LOGGER.info("CONFIG label=\"KNOWAGE.DASHBOARD.EXTERNAL_PROCESS_NAME\": {}", cockpitExportExternalProcessName);

            String stringifiedRequestUrl = url.toString();
            ProcessBuilder processBuilder = new ProcessBuilder(cockpitExportExternalProcessName, exportScriptFullPath.toString(),
                    encodedUserId, outputDir.toString(), stringifiedRequestUrl);

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
    private void logOutputToCoreLog(Process exec) throws IOException {
        InputStreamReader isr = new InputStreamReader(exec.getInputStream());
        BufferedReader b = new BufferedReader(isr);
        String line;
        LOGGER.warn("Process output");
        while ((line = b.readLine()) != null) {
            LOGGER.warn(line);
        }
    }

    private void setWorkingDirectory(String cockpitExportScriptPath, ProcessBuilder processBuilder) {
        // Required by puppeteer v19
        processBuilder.directory(new File(cockpitExportScriptPath));

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
            Map<String, Map<String, JSONArray>> selections = getSelections(body);

            JSONArray driversFromBody = getDrivers(body);
            JSONObject drivers = transformDriversForDatastore(driversFromBody);
            JSONArray parameters = getParametersFromBody(body);

            if (isDashboardSingleWidgetExport) {
                if (body.has("datasetDrivers") && body.getJSONArray("datasetDrivers") != null && body.getJSONArray("datasetDrivers").length() > 0) {
                    exportedSheets = exportWidget(body, wb, null, selections, transformDriversForDatastore(body.getJSONArray("datasetDrivers")), parameters);
                } else {
                    exportedSheets = exportWidget(body, wb, null, selections, drivers, parameters);
                }
            } else {
                JSONArray widgetsJson = getDashboardWidgetsJson(stringifiedBody);
                exportedSheets += exportDashboard(widgetsJson, wb, getDocumentName(body), selections, drivers, parameters);
            }

            if (!selections.isEmpty()) {
                Sheet selectionsSheet = createUniqueSafeSheetForSelections(wb, "Active Selections");
                fillDashboardSelectionsSheetWithData(selections, selectionsSheet);
                exportedSheets++;
            }

            if (driversFromBody != null && driversFromBody.length() > 0) {
                Sheet driversSheet = createUniqueSafeSheetForSelections(wb, "Filters");
                fillDashboardDriversSheetWithData(driversFromBody, driversSheet);
                exportedSheets++;
            }

            if (exportedSheets == 0) {
                exportEmptyExcel(wb);
            } else {
                for (Sheet sheet : wb) {
                    if (sheet != null) {
                        // Adjusts the column width to fit the contents
                        adjustColumnWidth(sheet, this.getImageB64());
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
            throw new SpagoBIRuntimeException(EXCEL_ERROR, e);
        }

    }

    public byte[] getPivotBinaryData(JSONObject body) {
        if (body == null) {
            throw new SpagoBIRuntimeException("Unable to get template for dashboard");
        }
        try {
            Map<String, Map<String, JSONArray>> selections = getSelections(body);

            JSONArray driversFromBody = getDrivers(body);
            JSONObject drivers = transformDriversForDatastore(driversFromBody);
            JSONArray parameters = getParametersFromBody(body);

            Workbook wb;
            if (body.has("datasetDrivers") && body.getJSONArray("datasetDrivers") != null && body.getJSONArray("datasetDrivers").length() > 0) {
                wb = exportPivotWidget(body, null, selections, transformDriversForDatastore(body.getJSONArray("datasetDrivers")), parameters);
            } else {
                wb = exportPivotWidget(body, null, selections, drivers, parameters);
            }

            if (!selections.isEmpty()) {
                Sheet selectionsSheet = createUniqueSafeSheetForSelections(wb, "Active Selections");
                fillDashboardSelectionsSheetWithData(selections, selectionsSheet);
            }

            if (driversFromBody != null && driversFromBody.length() > 0) {
                Sheet driversSheet = createUniqueSafeSheetForSelections(wb, "Filters");
                fillDashboardDriversSheetWithData(driversFromBody, driversSheet);
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                wb.write(out);
                out.flush();
                return out.toByteArray();
            }

        } catch (Exception e) {
            throw new SpagoBIRuntimeException(EXCEL_ERROR, e);
        }

    }

    private void exportEmptyExcel(Workbook wb) {
        if (wb.getNumberOfSheets() == 0) {
            Sheet sh = wb.createSheet();
            Row row = sh.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("No data");
        }
    }

    public String getMimeType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    private String getDocumentName(JSONObject template) {
        try {
            return template.getJSONObject("document").getString("label");
        } catch (Exception e) {
            LOGGER.info("Cannot get document name", e);
            return null;
        }
    }

    private int exportDashboard(JSONArray widgetsArray, Workbook wb, String documentName, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, JSONArray parameters) {
        int exportedSheets = 0;
        for (int i = 0; i < widgetsArray.length(); i++) {
            try {
                JSONObject currWidget = widgetsArray.getJSONObject(i);
                setDatasetDriversIfPresent(body, currWidget, drivers);
                if (currWidget.has("datasetDrivers") && currWidget.getJSONArray("datasetDrivers") != null && currWidget.getJSONArray("datasetDrivers").length() > 0) {
                    exportedSheets = exportWidget(currWidget, wb, documentName, selections, transformDriversForDatastore(currWidget.getJSONArray("datasetDrivers")), parameters);
                } else {
                    exportedSheets = exportWidget(currWidget, wb, documentName, selections, drivers, parameters);
                }
            } catch (Exception e) {
                LOGGER.error("Error while exporting widget", e);
            }
        }
        return exportedSheets;
    }

    private void setDatasetDriversIfPresent(JSONObject body, JSONObject currWidget, JSONObject drivers) {
        try {
            if (body.has("configuration") && body.getJSONObject("configuration").has("datasets") && body.getJSONObject("configuration").getJSONArray("datasets").length() > 0) {
                for (int i = 0; i < body.getJSONObject("configuration").getJSONArray("datasets").length(); i++) {
                    JSONObject dataset = body.getJSONObject("configuration").getJSONArray("datasets").getJSONObject(i);
                    if (currWidget.has("dataset") && currWidget.get("dataset") != null && currWidget.optInt("dataset") == dataset.getInt("id")) {
                        if (dataset.has("drivers") && dataset.getJSONArray("drivers").length() > 0) {
                            JSONObject datasetDrivers = transformDriversForDatastore(dataset.getJSONArray("drivers"));
                            for (int j = 0; j < datasetDrivers.names().length(); j++) {
                                drivers.put(datasetDrivers.names().getString(j), datasetDrivers.get(datasetDrivers.names().getString(j)));
                            }
                        }
                    }
                }
            }

        } catch (JSONException jsonException) {
            throw new SpagoBIRuntimeException("Cannot set dataset drivers", jsonException);
        }
    }

    public int exportWidget(JSONObject body, Workbook wb, String documentName, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, JSONArray parameters) {

        int exportedSheets;
        try {
            String widgetType = body.getString("type");
            JSONObject parametersToSend = transformParametersForDatastore(body, parameters);
            if (Arrays.asList(WIDGETS_TO_IGNORE).contains(widgetType.toLowerCase()) || excelExportIsNotEnabled(body)) {
                return 0;
            }
            IWidgetExporter widgetExporter = DashboardWidgetExporterFactory.getExporter(this,
                    wb, body, documentName, selections, drivers, parametersToSend, getUserUniqueIdentifier(), getImageB64());
            exportedSheets = widgetExporter.export();
        } catch (Exception e) {
            LOGGER.error("Cannot export data to excel", e);
            throw new SpagoBIRuntimeException("Cannot export data to excel", e);
        }
        return exportedSheets;
    }

    public Workbook exportPivotWidget(JSONObject body, String documentName, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, JSONArray parameters) {
        try {
            JSONObject parametersToSend = transformParametersForDatastore(body, parameters);
            if (excelExportIsNotEnabled(body)) {
                return null;
            }
            DashboardPivotExporter dashboardPivotExporter = new DashboardPivotExporter(
                    this, body, documentName, selections, drivers, parametersToSend, getUserUniqueIdentifier(), getImageB64());
            return dashboardPivotExporter.exportPivot();
        } catch (Exception e) {
            LOGGER.error("Cannot export pivot data to excel", e);
            throw new SpagoBIRuntimeException("Cannot export pivot data to excel", e);
        }
    }

    private boolean excelExportIsNotEnabled(JSONObject body) {
        try {
            JSONObject settings = body.getJSONObject("settings");
            if (settings.has("configuration") && settings.getJSONObject("configuration").has("exports")) {
                return !settings.getJSONObject("configuration").getJSONObject("exports").getBoolean("showExcelExport");
            } else {
                return true;
            }

        } catch (Exception e) {
            LOGGER.info("Cannot get widget settings, assuming excel export is not enabled", e);
            return true;
        }
    }

    public Sheet createUniqueSafeSheet(Workbook wb, String widgetName, String dashboardSheetName) {
        Sheet sheet;
        String sheetName;
        try {
            if (!isSingleWidgetExport && dashboardSheetName != null && !dashboardSheetName.isEmpty()) {
                sheetName = dashboardSheetName.concat(".").concat(widgetName);
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

    private void setFormattedCellValue(Workbook wb, int precision, String type, Cell cell, String stringifiedValue) {
        CreationHelper creationHelper = wb.getCreationHelper();

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, getLocale());

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
            case "string":
                if (!stringifiedValue.trim().isEmpty()) {
                    try {
                        cell.getCellStyle().setDataFormat(getFormat(precision, creationHelper));
                        cell.setCellValue(Integer.parseInt(stringifiedValue));
                    } catch (Exception e) {
                        cell.setCellValue(stringifiedValue);
                    }
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

    protected String getNumberFormatByPrecision(int precision, String initialFormat) {
        StringBuilder format = new StringBuilder(initialFormat);
        if (precision > 0) {
            format.append(".");
            format.append("0".repeat(precision));
        }
        return format.toString();
    }

    public void fillTableSheetWithData(JSONObject dataStore, Workbook wb, Sheet sheet, String widgetName, int offset,
                                       JSONObject settings) {
        try {
            JSONObject metadata = dataStore.getJSONObject("metaData");
            JSONArray columns = metadata.getJSONArray("fields");
            columns = filterDataStoreColumns(columns);
            JSONArray rows = dataStore.getJSONArray("rows");
            JSONObject widgetData = dataStore.getJSONObject("widgetData");
            JSONArray columnSelectedOfDataset = widgetData.getJSONArray("columns");

            JSONArray columnsOrdered;
            List<String> hiddenColumns;
            if (widgetData.has("columns") && widgetData.getJSONArray("columns").length() > 0) {
                hiddenColumns = getDashboardHiddenColumnsList(settings, "hide");
                columnsOrdered = getDashboardTableOrderedColumns(columnSelectedOfDataset, hiddenColumns, columns);
            } else {
                columnsOrdered = columns;
            }

            int numberOfSummaryRows = 0;
            List<String> summaryRowsLabels = new ArrayList<>();

            numberOfSummaryRows = doSummaryRowsLogic(settings, numberOfSummaryRows, summaryRowsLabels);

            JSONArray groupsFromWidgetContent = getGroupsFromDashboardWidget(settings);
            Map<String, String> groupsAndColumnsMap = getDashboardGroupAndColumnsMap(widgetData, groupsFromWidgetContent);

            // CREATE BRANDED HEADER SHEET
            int startRow = 0;
            float rowHeight = 35; // in points
            int rowspan = 2;
            int startCol = 0;
            int colWidth = 25;
            int colspan = 2;
            int namespan = 10;
            int dataspan = 10;

            buildFirstPageHeaders(wb, sheet, widgetName, offset, settings, startRow, rowHeight, rowspan, startCol, colWidth, colspan, namespan, dataspan, groupsAndColumnsMap, columnsOrdered);

            int isColumnGroupingPresent = groupsAndColumnsMap.isEmpty() ? 0 : 1;

            replaceWithThemeSettingsIfPresent(settings);

            buildRowsAndCols(wb, sheet, offset, settings, rows, isColumnGroupingPresent, startRow, rowspan, columnsOrdered, numberOfSummaryRows, summaryRowsLabels);
        } catch (Exception e) {
            throw new SpagoBIRuntimeException(EXCEL_ERROR, e);
        }
    }
    protected final JSONArray getGroupsFromWidgetContent(JSONObject widgetData) throws JSONException {
        JSONArray groupsArray = new JSONArray();
        if (widgetData.has("groups")) {
            groupsArray = widgetData.getJSONArray("groups");
        }
        return groupsArray;
    }

    public void fillGenericWidgetSheetWithData(JSONObject dataStore, Workbook wb, Sheet sheet, String widgetName, int offset,
                                               JSONObject settings) {
        try {
            JSONObject metadata = dataStore.getJSONObject("metaData");
            JSONArray columns = metadata.getJSONArray("fields");
            columns = filterDataStoreColumns(columns);
            JSONArray rows = dataStore.getJSONArray("rows");
            JSONObject widgetData = dataStore.getJSONObject("widgetData");

            JSONArray columnsOrdered = columns;

            JSONArray groupsFromWidgetContent = getGroupsFromWidgetContent(widgetData);
            Map<String, String> groupsAndColumnsMap = getDashboardGroupAndColumnsMap(widgetData, groupsFromWidgetContent);

            // CREATE BRANDED HEADER SHEET
            int startRow = 0;
            float rowHeight = 35; // in points
            int rowspan = 2;
            int startCol = 0;
            int colWidth = 25;
            int colspan = 2;
            int namespan = 10;
            int dataspan = 10;

            buildFirstPageHeaders(wb, sheet, widgetName, offset, settings, startRow, rowHeight, rowspan, startCol, colWidth, colspan, namespan, dataspan, groupsAndColumnsMap, columnsOrdered);
            // FILL RECORDS
            int isGroup = groupsAndColumnsMap.isEmpty() ? 0 : 1;

            replaceWithThemeSettingsIfPresent(settings);

            buildRowsAndCols(wb, sheet, offset, settings, rows, isGroup, startRow, rowspan, columnsOrdered, 0, new ArrayList<>());
        } catch (Exception e) {
            throw new SpagoBIRuntimeException(EXCEL_ERROR, e);
        }

    }
    void buildFirstPageHeaders(Workbook wb, Sheet sheet, String widgetName, int offset, JSONObject settings, int startRow, float rowHeight, int rowspan, int startCol, int colWidth, int colspan, int namespan, int dataspan, Map<String, String> groupsAndColumnsMap, JSONArray columnsOrdered) throws JSONException {
        if (offset == 0) { // if pagination is active, headers must be created only once
            Row header = createHeader(sheet, startRow, rowHeight, rowspan, startCol, colWidth, colspan, namespan, dataspan, widgetName, groupsAndColumnsMap, columnsOrdered);
            for (int i = 0; i < columnsOrdered.length(); i++) {
                JSONObject column = columnsOrdered.getJSONObject(i);
                String columnName;
                try {
                    columnName = column.getString("alias");
                } catch (JSONException e) {
                    try {
                        columnName = column.getString("header");
                    } catch(JSONException e2) {
                        columnName = column.getString("columnName");
                    }
                }
                // renaming table columns names of the excel export
                columnName = getInternationalizedHeader(columnName);

                columnName = replaceWithCustomHeaderIfPresent(settings, columnName, column);

                Cell cell = header.createCell(i);
                cell.setCellValue(columnName);

                XSSFFont font = (XSSFFont) wb.createFont();
                buildHeaderCellStyle(wb, sheet, settings, font, cell);
            }

            // adjusts the column width to fit the contents
            adjustColumnWidth(sheet, this.getImageB64());
        }
    }

    void buildHeaderCellStyle(Workbook wb, Sheet sheet, JSONObject settings, XSSFFont font, Cell cell) throws JSONException {
        CellStyle headerCellStyle;
        JSONObject styleJSONObject = getJsonObjectUtils().getStyleFromSettings(settings);
        if (settings != null && settings.has("style") && styleJSONObject.has("headers")) {
            Style style = getStyleCustomObjFromProps(sheet, styleJSONObject.getJSONObject("headers"), "");
            headerCellStyle = buildPoiCellStyle(style, font, wb);
        } else {
            headerCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 11);
        }
        cell.setCellStyle(headerCellStyle);
    }

    private Row createHeader(
            Sheet sheet,
            int startRow,
            float rowHeight,
            int rowspan,
            int startCol,
            int colWidth,
            int colspan,
            int namespan,
            int dataspan,
            String widgetName,
            Map<String, String> groupsAndColumnsMap,
            JSONArray columnsOrdered) {
        int headerIndex = createBrandedHeaderSheet(
                sheet,
                this.getImageB64(),
                startRow,
                rowHeight,
                rowspan,
                startCol,
                colWidth,
                colspan,
                namespan,
                dataspan,
                DOCUMENT_NAME,
                widgetName);

        return createDashboardHeaderColumnNames(sheet, groupsAndColumnsMap, columnsOrdered, headerIndex + 1);
    }

    private void buildRowsAndCols(Workbook wb, Sheet sheet, int offset, JSONObject settings, JSONArray rows, int isGroup, int startRow, int rowspan, JSONArray columnsOrdered, int numberOfSummaryRows, List<String> summaryRowsLabels) throws JSONException {
        Map<String, JSONArray> columnStylesMap = getStylesMap(settings);
        Map<String, CellStyle> columnsCellStyles = new HashMap<>();
        CellStyle cellStyle = null;
        JSONObject alternatedRows = getRowStyle(settings);
        for (int r = 0; r < rows.length(); r++) {
            JSONObject rowObject = rows.getJSONObject(r);
            Row row;
            if (StringUtils.isNotEmpty(this.getImageB64())) {
                row = sheet.createRow((offset + r + isGroup) + (startRow + rowspan) + 2); // starting by Header
            } else {
                row = sheet.createRow((offset + r + isGroup) + 2);
            }

            boolean rowIsEven = (r % 2 == 0);
            String rawCurrentNumberType = rowIsEven ? "even" : "odd";
            String defaultRowBackgroundColor = getDefaultRowBackgroundColor(alternatedRows, rowIsEven);

            boolean styleAlreadyAppliedToPreviousCells = false;
            List<Boolean> styleCanBeOverriddenByWholeRowStyle = new ArrayList<>();
            cellStyle = buildCols(wb, sheet, settings, rows, columnsOrdered, columnStylesMap, cellStyle, columnsCellStyles, numberOfSummaryRows, summaryRowsLabels, row, rowObject, r, styleCanBeOverriddenByWholeRowStyle, rawCurrentNumberType, styleAlreadyAppliedToPreviousCells, defaultRowBackgroundColor, null);
        }
    }

    private CellStyle buildCols(Workbook wb, Sheet sheet, JSONObject settings, JSONArray rows, JSONArray columnsOrdered, Map<String, JSONArray> columnStylesMap, CellStyle cellStyle, Map<String, CellStyle> columnsCellStyles, int numberOfSummaryRows, List<String> summaryRowsLabels, Row row, JSONObject rowObject, int r, List<Boolean> styleCanBeOverriddenByWholeRowStyle, String rawCurrentNumberType, boolean styleAlreadyAppliedToPreviousCells, String defaultRowBackgroundColor, String styleKeyToApplyToTheEntireRow) throws JSONException {
        for (int c = 0; c < columnsOrdered.length(); c++) {
            JSONObject column = columnsOrdered.getJSONObject(c);
            String type = getCellType(column, column.getString("name"));
            String colIndex = column.getString("name"); // column_1, column_2, column_3...

            Cell cell = row.createCell(c);
            Object value = rowObject.get(colIndex);

            String stringifiedValue = value != null ? value.toString() : "";

            String styleKey;
            if (r >= rows.length() - numberOfSummaryRows && !styleIsEmpty(getStyleCustomObjFromProps(sheet, settings.getJSONObject("style").getJSONObject("summary"), ""))) {
                CellStyle summaryCellStyle = buildPoiCellStyle(getStyleCustomObjFromProps(sheet, settings.getJSONObject("style").getJSONObject("summary"), ""), (XSSFFont) wb.createFont(), wb);
                cell.setCellStyle(summaryCellStyle);
            } else {
                JSONObject theRightStyle = getTheRightStyleByColumnIdAndValue(columnStylesMap, stringifiedValue, column.optString("id"), defaultRowBackgroundColor);

                styleCanBeOverriddenByWholeRowStyle.add(c, styleCanBeOverridden(theRightStyle));
                if (theRightStyle.has("applyToWholeRow") && theRightStyle.getBoolean("applyToWholeRow")) {
                    styleKey = getStyleKey(column, theRightStyle, rawCurrentNumberType);
                    if (!styleAlreadyAppliedToPreviousCells) {
                        cellStyle = getCellStyleByStyleKey(wb, sheet, styleKey, columnsCellStyles, theRightStyle, defaultRowBackgroundColor);
                        applyWholeRowStyle(c, styleCanBeOverriddenByWholeRowStyle, row, cellStyle);
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
            }
            int precision = getPrecisionByColumn(settings, column);

            if (r < rows.length() - numberOfSummaryRows) {
                setFormattedCellValue(wb, precision, type, cell, stringifiedValue);
            } else {
                    if (isSummaryColumnVisible(getDashboardHiddenColumnsList(settings, "hideFromSummary"), column)) {
                        String label = "";
                        if (colIndex.equals("column_1")) {
                            label = summaryRowsLabels.get(r - (rows.length() - numberOfSummaryRows)).concat(" ");
                        }
                        cell.setCellStyle(cellStyle);
                        setFormattedCellValue(wb, precision, type, cell, label.concat(stringifiedValue));
                     }
                }
            }
            return cellStyle;
    }

    private String getCellType(JSONObject column, String colName) {
        try {
            return column.getString("type");
        } catch (Exception e) {
            LOGGER.error("Error while retrieving column {} type. It will be treated as string.", colName, e);
            return "string";
        }
    }

    private Row createDashboardHeaderColumnNames(Sheet sheet, Map<String, String> groupsAndColumnsMap, JSONArray columnsOrdered,
                                                 int startRowOffset) {
        try {
            Row header;
            CellStyle groupsCellStyle = buildCellStyle(sheet, true, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, (short) 11);
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
                        cell.setCellStyle(groupsCellStyle);
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

    protected int getPrecisionByColumn(JSONObject settings, JSONObject column) {
        try {
            JSONObject visualization = getJsonObjectUtils().getVisualizationFromSettings(settings);
            
            if (visualization == null || !visualization.has("visualizationTypes"))
                return -1;

            JSONObject visualizationTypes = visualization.getJSONObject("visualizationTypes");

            JSONArray types = visualizationTypes.getJSONArray("types");

            for (int i = 0; i < types.length(); i++) {
                JSONObject type = types.getJSONObject(i);
                JSONArray target;
                try {
                    target = type.getJSONArray("target");
                } catch (JSONException e) {
                    target = new JSONArray();
                    target.put(type.getString("target"));
                }

                if (type.has("precision")) {
                    if (target.toString().contains(column.getString("id")) || target.toString().contains("all")) {
                        return type.getInt("precision");
                    } else {
                        return -1;
                    }
                }
            }
        } catch (Exception ignored) {
            return -1;
        }
        return -1;
    }

    public CellStyle buildPoiCellStyle(Style style, XSSFFont font, Workbook wb) {
        CellStyle cellStyle = wb.createCellStyle();

        if (!stringIsEmpty(style.getFontSize())) {
            font.setFontHeightInPoints(Short.parseShort(getOnlyTheNumericValueFromString(style.getFontSize())));
        } else {
            font.setFontHeightInPoints(DEFAULT_FONT_SIZE);
        }

        if (!stringIsEmpty(style.getColor())) {
            font.setColor(getXSSFColorFromRGBA(style.getColor()));
        }

        if (!stringIsEmpty(style.getBackgroundColor())) {
            cellStyle.setFillForegroundColor(getXSSFColorFromRGBA(style.getBackgroundColor()));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        if (!stringIsEmpty(style.getFontFamily())) {
            font.setFontName(style.getFontFamily());
        } else {
            font.setFontName(DEFAULT_FONT_NAME);
        }

        if (!stringIsEmpty(style.getFontWeight())) {
            font.setBold(style.getFontWeight().equals("bold"));
        }

        if (!stringIsEmpty(style.getFontStyle())) {
            font.setItalic(style.getFontStyle().equals("italic"));
        }

        if (!stringIsEmpty(style.getAlignItems())) {
            cellStyle.setAlignment(getHorizontalAlignment(style.getAlignItems().toUpperCase()));
        } else {
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
        }

        if (!stringIsEmpty(style.getJustifyContent())) {
            cellStyle.setVerticalAlignment(getVerticalAlignment(style.getJustifyContent().toUpperCase()));
        } else {
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        }

        cellStyle.setFont(font);
        return cellStyle;
    }


    private XSSFColor getXSSFColorFromRGBA(String colorStr) {
        String[] values = colorStr.replace(colorStr.contains("rgba(") ? "rgba(" : "rgb(", "").replace(")", "").split(",");
        int red = Integer.parseInt(values[0].trim());
        int green = Integer.parseInt(values[1].trim());
        int blue = Integer.parseInt(values[2].trim());

        // Handle alpha transparency
        if (values.length > 3) {
            float alpha = Float.parseFloat(values[3].trim());

            // For partial transparency, blend with white background
            // This simulates how transparent colors appear on a white Excel background
            if (alpha <= 1.0f) {
                red = (int) (red * alpha + 255 * (1 - alpha));
                green = (int) (green * alpha + 255 * (1 - alpha));
                blue = (int) (blue * alpha + 255 * (1 - alpha));
            }
        }

        // Ensure values are within valid range
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        return new XSSFColor(new java.awt.Color(red, green, blue), new DefaultIndexedColorMap());
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

    protected void applyWholeRowStyle(int c, List<Boolean> styleCanBeOverriddenByWholeRowStyle, Row row, CellStyle cellStyle) {
        for (int previousCell = c - 1; previousCell >= 0; previousCell--) {
            if (styleCanBeOverriddenByWholeRowStyle.get(previousCell).equals(Boolean.TRUE)) {
                row.getCell(previousCell).setCellStyle(cellStyle);
            }
        }
    }

    public void adjustColumnWidth(Sheet sheet, String imageB64) {
        try {
            try {
                ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
            } catch (ClassCastException e) {
                LOGGER.info("cannot track all columns for auto sizing, probably not an SXSSFSheet", e);
            }
            Row row = sheet.getRow(sheet.getLastRowNum());
            if (row != null) {
                for (int i = 0; i < row.getLastCellNum(); i++) {
                    sheet.autoSizeColumn(i);
                    if (StringUtils.isNotEmpty(imageB64) && (i == 0 || i == 1)) {
                        // first or second column
                        int colWidth = 25;
                        if (sheet.getColumnWidthInPixels(i) < (colWidth * 256))
                            sheet.setColumnWidth(i, colWidth * 256);
                    }
                }
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException(EXCEL_ERROR, e);
        }
    }

}
