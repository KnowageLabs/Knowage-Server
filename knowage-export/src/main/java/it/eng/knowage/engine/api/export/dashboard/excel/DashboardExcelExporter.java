package it.eng.knowage.engine.api.export.dashboard.excel;

import it.eng.knowage.commons.security.PathTraversalChecker;
import it.eng.knowage.engine.api.export.IWidgetExporter;
import it.eng.knowage.engine.api.export.dashboard.DashboardExporter;
import it.eng.knowage.engine.api.export.dashboard.Style;
import it.eng.knowage.engine.api.export.dashboard.excel.exporters.DashboardPivotExporter;
import it.eng.knowage.engine.api.export.dashboard.excel.exporters.DashboardWidgetExporterFactory;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import lombok.Getter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
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
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
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
    private static final int COLOR_WHITE_COMPONENT = 255;
    private static final int COLOR_BLACK_COMPONENT = 0;

    // SCHEDULER
    private static final String CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH = "internal.nodejs.chromium.export.path";
    private static final String CONFIG_NAME_FOR_DRIVERS_SHEET_EXPORT = "DASHBOARD.EXPORT.SHOW_DRIVERS_SHEET";
    private static final String CONFIG_NAME_FOR_SELECTIONS_SHEET_EXPORT = "DASHBOARD.EXPORT.SHOW_SELECTIONS_SHEET";
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
            Path outputDir;
            if (SystemUtils.IS_OS_UNIX) {
                FileAttribute<Set<PosixFilePermission>> attr =
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
                outputDir = Files.createTempDirectory("knowage-xls-exporter-", attr);
            } else {
                File dir = Files.createTempDirectory("knowage-xls-exporter-").toFile();
                // try to restrict to owner where supported; second param ensures owner-only on platforms honoring it
                dir.setReadable(true, true);
                dir.setWritable(true, true);
                dir.setExecutable(true, true);
                outputDir = dir.toPath();
            }
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
            Map<String, Map<String, Object>> selections = getSelections(body);

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

            IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
            Optional<Config> selectionsCfg = configsDao.loadConfigParametersByLabelIfExist(CONFIG_NAME_FOR_SELECTIONS_SHEET_EXPORT);

            if (selectionsCfg.isPresent() && selectionsCfg.get().isActive() && Boolean.parseBoolean(selectionsCfg.get().getValueCheck()) && !selections.isEmpty()) {
                Sheet selectionsSheet = createUniqueSafeSheetForSelections(wb, "Active Selections");
                fillDashboardSelectionsSheetWithData(getSelections(body), selectionsSheet);
                exportedSheets++;
            }

            Optional<Config> driversConfig = configsDao.loadConfigParametersByLabelIfExist(CONFIG_NAME_FOR_DRIVERS_SHEET_EXPORT);

            if (driversConfig.isPresent() && driversConfig.get().isActive() && Boolean.parseBoolean(driversConfig.get().getValueCheck()) && driversFromBody != null && driversFromBody.length() > 0) {
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
            Map<String, Map<String, Object>> selections = getSelections(body);

            JSONArray driversFromBody = getDrivers(body);
            JSONObject drivers = transformDriversForDatastore(driversFromBody);
            JSONArray parameters = getParametersFromBody(body);

            Workbook wb;
            if (body.has("datasetDrivers") && body.getJSONArray("datasetDrivers") != null && body.getJSONArray("datasetDrivers").length() > 0) {
                wb = exportPivotWidget(body, null, selections, transformDriversForDatastore(body.getJSONArray("datasetDrivers")), parameters);
            } else {
                wb = exportPivotWidget(body, null, selections, drivers, parameters);
            }

            IConfigDAO configsDao = DAOFactory.getSbiConfigDAO();
            Optional<Config> selectionsCfg = configsDao.loadConfigParametersByLabelIfExist(CONFIG_NAME_FOR_SELECTIONS_SHEET_EXPORT);

            if (selectionsCfg.isPresent() && selectionsCfg.get().isActive() && Boolean.parseBoolean(selectionsCfg.get().getValueCheck()) && !selections.isEmpty()) {
                Sheet selectionsSheet = createUniqueSafeSheetForSelections(wb, "Active Selections");
                fillDashboardSelectionsSheetWithData(getSelections(body), selectionsSheet);
            }

            Optional<Config> driversConfig = configsDao.loadConfigParametersByLabelIfExist(CONFIG_NAME_FOR_DRIVERS_SHEET_EXPORT);

            if (driversConfig.isPresent() && driversConfig.get().isActive() && Boolean.parseBoolean(driversConfig.get().getValueCheck()) && driversFromBody != null && driversFromBody.length() > 0) {
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

    private int exportDashboard(JSONArray widgetsArray, Workbook wb, String documentName, Map<String, Map<String, Object>> selections, JSONObject drivers, JSONArray parameters) {
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

    public int exportWidget(JSONObject body, Workbook wb, String documentName, Map<String, Map<String, Object>> selections, JSONObject drivers, JSONArray parameters) {

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

    public Workbook exportPivotWidget(JSONObject body, String documentName, Map<String, Map<String, Object>> selections, JSONObject drivers, JSONArray parameters) {
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

    private void setFormattedCellValue(Workbook wb, JSONObject visualizationType, String type, Cell cell, String stringifiedValue) throws JSONException {
        CreationHelper creationHelper = wb.getCreationHelper();
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, getLocale());

        switch (type) {
            case "int":
                if (!stringifiedValue.trim().isEmpty()) {
                    try {
                        long longValue = Long.parseLong(stringifiedValue.trim());
                        cell.getCellStyle().setDataFormat(buildNumericFormat(visualizationType, 0, creationHelper));
                        cell.setCellValue(longValue);
                    } catch (NumberFormatException e) {
                        // value may arrive as a decimal string even for int columns
                        try {
                            double doubleValue = Double.parseDouble(stringifiedValue.trim());
                            cell.getCellStyle().setDataFormat(buildNumericFormat(visualizationType, 0, creationHelper));
                            cell.setCellValue(doubleValue);
                        } catch (NumberFormatException e2) {
                            cell.setCellValue(stringifiedValue);
                        }
                    }
                }
                break;
            case "float":
                if (!stringifiedValue.trim().isEmpty()) {
                    try {
                        double doubleValue = Double.parseDouble(stringifiedValue.trim());
                        cell.getCellStyle().setDataFormat(buildNumericFormat(visualizationType, 0, creationHelper));
                        cell.setCellValue(doubleValue);
                    } catch (NumberFormatException e) {
                        cell.setCellValue(stringifiedValue);
                    }
                }
                break;
            case "date":
                try {
                    if (!stringifiedValue.trim().isEmpty()) {
                        cell.getCellStyle().setDataFormat(getFormat(visualizationType, creationHelper));
                        if (visualizationType != null && (visualizationType.has("prefix") || visualizationType.has("suffix"))) {
                            stringifiedValue = visualizationType.optString("prefix", "") + stringifiedValue;
                            stringifiedValue = stringifiedValue + visualizationType.optString("suffix", "");
                            cell.setCellValue(stringifiedValue);
                        } else {
                            Date date = dateFormat.parse(stringifiedValue);
                            cell.setCellValue(date);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.debug("Date will be exported as string due to error: ", e);
                    cell.setCellValue(stringifiedValue);
                }
                break;
            case "string":
                if (!stringifiedValue.trim().isEmpty()) {
                    if (visualizationType != null && (visualizationType.has("prefix") || visualizationType.has("suffix"))) {
                        stringifiedValue = visualizationType.optString("prefix", "") + stringifiedValue;
                        stringifiedValue = stringifiedValue + visualizationType.optString("suffix", "");
                    }
                    try {
                        cell.getCellStyle().setDataFormat(getFormat(visualizationType, creationHelper));
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

    /**
     * Builds an Excel format string that:
     * <ul>
     *   <li>Uses {@code #,##0} so Excel renders the <em>locale-aware</em> grouping (thousands) and
     *       decimal separators – e.g. {@code 1,234.56} for en-US and {@code 1.234,56} for it-IT.</li>
     *   <li>Applies the requested decimal precision.</li>
     *   <li>Embeds prefix/suffix as Excel literal strings so the cell value stays numeric.</li>
     * </ul>
     */
    private short buildNumericFormat(JSONObject visualizationType, int defaultPrecision, CreationHelper creationHelper) throws JSONException {
        String prefix = visualizationType != null ? visualizationType.optString("prefix", "") : "";
        String suffix = visualizationType != null ? visualizationType.optString("suffix", "") : "";
        int precision = defaultPrecision;
        if (visualizationType != null && visualizationType.has("precision")) {
            precision = visualizationType.getInt("precision");
        }

        StringBuilder format = new StringBuilder();

        // Prefix as an Excel literal (double-quotes inside are stripped to avoid breaking the format)
        if (!prefix.isEmpty()) {
            format.append("\"").append(prefix.replace("\"", "")).append("\"");
        }

        // #,##0 → grouping placeholder; Excel replaces , and . with the locale's actual separators
        format.append("#,##0");
        if (precision > 0) {
            format.append(".");
            format.append("0".repeat(precision));
        }

        // Suffix as an Excel literal
        if (!suffix.isEmpty()) {
            format.append("\"").append(suffix.replace("\"", "")).append("\"");
        }

        return creationHelper.createDataFormat().getFormat(format.toString());
    }

    private short getFormat(JSONObject visualizationType, CreationHelper helper) throws JSONException {
        // Use #,##0 as the base so grouping separators are locale-aware in Excel
        String format = "#,##0";
        if (visualizationType != null) {
            if (visualizationType.has("precision")) {
                format = getNumberFormatByPrecision(visualizationType.getInt("precision"), format);
            }
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
            JSONObject visualizationType = getVisualizationTypeByColumn(settings, column);

            if (r < rows.length() - numberOfSummaryRows) {
                setFormattedCellValue(wb, visualizationType, type, cell, stringifiedValue);
            } else {
                    if (isSummaryColumnVisible(getDashboardHiddenColumnsList(settings, "hideFromSummary"), column)) {
                        String label = "";
                        if (colIndex.equals("column_1")) {
                            label = summaryRowsLabels.get(r - (rows.length() - numberOfSummaryRows)).concat(" ");
                        }
                        // Do NOT overwrite cell style here: it was already set correctly above
                        // (either summaryCellStyle for configured summary style, or regular cellStyle).
                        setFormattedCellValue(wb, visualizationType, type, cell, label.concat(stringifiedValue));
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

    public CellStyle buildPoiCellStyle(Style style, XSSFFont font, Workbook wb) {
        CellStyle cellStyle = wb.createCellStyle();

        if (!stringIsEmpty(style.getFontSize())) {
            font.setFontHeightInPoints(Short.parseShort(getOnlyTheNumericValueFromString(style.getFontSize())));
        } else {
            font.setFontHeightInPoints(DEFAULT_FONT_SIZE);
        }

        if (!stringIsEmpty(style.getColor())) {
            XSSFColor parsedFontColor = parseXSSFColor(style.getColor());
            if (parsedFontColor != null && !isRgbColor(parsedFontColor, COLOR_BLACK_COMPONENT, COLOR_BLACK_COMPONENT, COLOR_BLACK_COMPONENT)) {
                font.setColor(parsedFontColor);
            }
        }

        if (!stringIsEmpty(style.getBackgroundColor())) {
            XSSFColor parsedBackgroundColor = parseXSSFColor(style.getBackgroundColor());
            if (parsedBackgroundColor != null && !isRgbColor(parsedBackgroundColor, COLOR_WHITE_COMPONENT, COLOR_WHITE_COMPONENT, COLOR_WHITE_COMPONENT)) {
                cellStyle.setFillForegroundColor(parsedBackgroundColor);
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
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
            cellStyle.setVerticalAlignment(getVerticalAlignment(style.getAlignItems().toUpperCase()));
        } else {
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        }

        if (!stringIsEmpty(style.getJustifyContent())) {
            cellStyle.setAlignment(getHorizontalAlignment(style.getJustifyContent().toUpperCase()));
        } else {
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
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

    private XSSFColor parseXSSFColor(String colorStr) {
        try {
            return getXSSFColorFromRGBA(colorStr);
        } catch (Exception e) {
            LOGGER.debug("Cannot parse style color '{}', skipping color override", colorStr, e);
            return null;
        }
    }

    private boolean isRgbColor(XSSFColor color, int red, int green, int blue) {
        byte[] rgb = color.getRGB();
        return rgb != null
                && rgb.length >= 3
                && (rgb[0] & 0xFF) == red
                && (rgb[1] & 0xFF) == green
                && (rgb[2] & 0xFF) == blue;
    }

    private CellStyle getCellStyleByStyleKey(Workbook wb, Sheet sheet, String styleKey, Map<String, CellStyle> columnsCellStyles, JSONObject theRightStyle, String defaultRowBackgroundColor) {
        CellStyle cellStyle;
        if (columnAlreadyHasTheRightStyle(styleKey, columnsCellStyles)) {
            cellStyle = columnsCellStyles.get(styleKey);
        } else {
            Style styleCustomObj = getStyleCustomObjFromProps(sheet, theRightStyle, defaultRowBackgroundColor);
            // If a style explicitly asks for white background, preserve the existing row background fallback.
            XSSFColor styleBackgroundColor = parseXSSFColor(styleCustomObj.getBackgroundColor());
            if (styleBackgroundColor != null
                    && isRgbColor(styleBackgroundColor, COLOR_WHITE_COMPONENT, COLOR_WHITE_COMPONENT, COLOR_WHITE_COMPONENT)
                    && !stringIsEmpty(defaultRowBackgroundColor)) {
                styleCustomObj.setBackgroundColor(defaultRowBackgroundColor);
            }
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
