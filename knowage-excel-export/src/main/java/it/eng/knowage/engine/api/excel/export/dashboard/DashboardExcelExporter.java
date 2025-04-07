package it.eng.knowage.engine.api.excel.export.dashboard;

import it.eng.knowage.commons.multitenant.OrganizationImageManager;
import it.eng.knowage.commons.security.PathTraversalChecker;
import it.eng.knowage.engine.api.excel.export.IWidgetExporter;
import it.eng.knowage.engine.api.excel.export.dashboard.exporters.DashboardWidgetExporterFactory;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDashboardThemeDAO;
import it.eng.spagobi.commons.metadata.SbiDashboardTheme;
import it.eng.spagobi.i18n.dao.I18NMessagesDAO;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import lombok.Getter;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.apache.poi.xssf.usermodel.XSSFSheet;
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

import static it.eng.knowage.engine.api.excel.export.dashboard.StaticLiterals.EXCEL_ERROR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.poi.ss.usermodel.DataConsolidateFunction.*;

public class DashboardExcelExporter extends Common {
    private static final Logger LOGGER = LogManager.getLogger(DashboardExcelExporter.class);

    private static final String[] WIDGETS_TO_IGNORE = {"image", "text", "selector", "selection", "html"};
    private static final int SHEET_NAME_MAX_LEN = 31;
    protected static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String TIMESTAMP_FORMAT = "dd/MM/yyyy HH:mm:ss.SSS";

    private final StyleProvider styleProvider;
    private final JSONObjectUtils jsonObjectUtils;
    private final DatastoreUtils datastoreUtils;

    // SCHEDULER
    private static final String CONFIG_NAME_FOR_EXPORT_SCRIPT_PATH = "internal.nodejs.chromium.export.path";
    private static final String SCRIPT_NAME = "cockpit-export-xls.js";
    private String role;
    private String userUniqueIdentifier = "";
    private String requestURL = "";
    private String organization = "";


    private String imageB64 = "";
    private static final String DOCUMENT_NAME = "";
    private final boolean isSingleWidgetExport;
    private int uniqueId = 0;
    @Getter
    protected Locale locale;
    protected final JSONObject body;
    protected Map<String, String> i18nMessages;

    public DashboardExcelExporter(DatastoreUtils datastoreUtils, JSONObject body) {
        this.datastoreUtils = datastoreUtils;
        this.isSingleWidgetExport = body.optBoolean("exportWidget");
        this.body = body;
        locale = getLocaleFromBody(body);
        jsonObjectUtils = new JSONObjectUtils();
        styleProvider = new StyleProvider(jsonObjectUtils);
    }

    public DashboardExcelExporter(DatastoreUtils datastoreUtils, JSONObject body, String role, String userId, String requestUrl, String organization) {
        this.datastoreUtils = datastoreUtils;
        this.isSingleWidgetExport = body.optBoolean("exportWidget");
        this.body = body;
        locale = getLocaleFromBody(body);
        jsonObjectUtils = new JSONObjectUtils();
        styleProvider = new StyleProvider(jsonObjectUtils);
        this.role = role;
        this.userUniqueIdentifier = userId;
        this.requestURL = requestUrl;
        this.organization = organization;
    }

    private Locale getLocaleFromBody(JSONObject body) {
        try {
            String language = body.getString(SpagoBIConstants.SBI_LANGUAGE);
            String country = body.getString(SpagoBIConstants.SBI_COUNTRY);
            return new Locale(language, country);
        } catch (Exception e) {
            LOGGER.warn("Cannot get locale information from input parameters body", e);
            return Locale.ENGLISH;
        }

    }
    public byte[] getScheduledBinaryData(String documentLabel) throws IOException, InterruptedException {
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

            URI url = UriBuilder.fromUri(requestURL)
                        .replaceQueryParam("outputType_description", "HTML")
                        .replaceQueryParam("outputType", "HTML")
                        .replaceQueryParam("role", role)
                        .replaceQueryParam("organization", organization)
                        .build();

            // avoid sonar security hotspot issue
            String cockpitExportExternalProcessName = SingletonConfig.getInstance()
                    .getConfigValue("KNOWAGE.DASHBOARD.EXTERNAL_PROCESS_NAME");
            LOGGER.info("CONFIG label=\"KNOWAGE.DASHBOARD.EXTERNAL_PROCESS_NAME\": " + cockpitExportExternalProcessName);

            String stringifiedRequestUrl = url.toString();
            // replace localhost:8080 with 127.0.0.1:3000
//            stringifiedRequestUrl = stringifiedRequestUrl.replace("localhost:8080", "127.0.0.1:3000");

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
            JSONObject drivers = getDrivers(body);
            if (isDashboardSingleWidgetExport) {
                exportedSheets = exportWidget(body, wb, null, selections, drivers);
            } else {
                JSONArray widgetsJson = getDashboardWidgetsJson(stringifiedBody);
                exportedSheets += exportDashboard(widgetsJson, wb, getDocumentName(body), selections, drivers);
            }

            if (!selections.isEmpty()) {
                Sheet selectionsSheet = createUniqueSafeSheetForSelections(wb, "Active Selections");
                fillDashboardSelectionsSheetWithData(selections, selectionsSheet);
                exportedSheets++;
            }

            if (drivers != null && drivers.length() > 0) {
                Sheet driversSheet = createUniqueSafeSheetForSelections(wb, "Filters");
                fillDashboardDriversSheetWithData(getDriversFromBody(body), driversSheet);
                exportedSheets++;
            }

            if (exportedSheets == 0) {
                exportEmptyExcel(wb);
            } else {
                for (Sheet sheet : wb) {
                    if (sheet != null) {
                        // Adjusts the column width to fit the contents
                        styleProvider.adjustColumnWidth(sheet, this.imageB64);
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

    private Sheet createUniqueSafeSheetForSelections(Workbook wb, String widgetName) {
        Sheet sheet;
        try {
            sheet = wb.createSheet(widgetName);
            return sheet;
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Couldn't create sheet", e);
        }
    }

    public void drawBrandendHeaderImage(Sheet sheet, String imageB64, int pictureType, int startCol, int startRow,
                                        int colspan, int rowspan) {
        try {
            Workbook wb = sheet.getWorkbook();
            CreationHelper helper = wb.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();

            Picture pict = drawImage(sheet, imageB64, pictureType, startCol, startRow, colspan, rowspan, anchor, wb);
            buildImageMesaures(sheet, startCol, startRow, colspan, rowspan, pict, anchor);

        } catch (Exception e) {
            throw new SpagoBIRuntimeException(EXCEL_ERROR, e);
        }
    }

    private Picture drawImage(Sheet sheet, String imageB64, int pictureType, int startCol, int startRow, int colspan, int rowspan, ClientAnchor anchor, Workbook wb) {

        String encodingPrefix = "base64,";
        int contentStartIndex = imageB64.indexOf(encodingPrefix) + encodingPrefix.length();
        byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(imageB64.substring(contentStartIndex));
        int pictureIdx = wb.addPicture(bytes, pictureType);

        anchor.setCol1(startCol);
        anchor.setRow1(startRow);
        anchor.setCol2(startCol + colspan);
        anchor.setRow2(startRow + rowspan);

        Drawing<?> drawing = sheet.createDrawingPatriarch();
        return drawing.createPicture(anchor, pictureIdx);
    }

    private static void buildImageMesaures(Sheet sheet, int startCol, int startRow, int colspan, int rowspan, Picture pict, ClientAnchor anchor) {
        float colsWidthPx;
        int horCenterPosPx;
        float rowsHeightPx;
        int vertCenterPosPx;
        int pictWidthPx = pict.getImageDimension().width;
        int pictHeightPx = pict.getImageDimension().height;

        // get the heights of all merged rows in px
        float[] rowHeightsPx = new float[startRow + rowspan];
        rowsHeightPx = 0f;
        for (int r = startRow; r < startRow + rowspan; r++) {
            Row row = sheet.getRow(r);
            float rowHeightPt = row.getHeightInPoints();
            rowHeightsPx[r- startRow] = rowHeightPt * Units.PIXEL_DPI / Units.POINT_DPI;
            rowsHeightPx += rowHeightsPx[r- startRow];
        }

        // get the widths of all merged cols in px
        float[] colWidthsPx = new float[startCol + colspan];
        colsWidthPx = 0f;
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
        horCenterPosPx = Math.round(colsWidthPx / 2f - pictWidthPx * scale / 2f);
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
        vertCenterPosPx = Math.round(rowsHeightPx / 2f - pictHeightPx * scale / 2f);
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
            anchor.setDy1(Math.round(vertCenterPosPx - rowsHeightPx) * Units.EMU_PER_PIXEL); //in unit EMU for XSSF
        }

        if (sheet instanceof SXSSFSheet) {
            anchor.setDx2(Math.round(colsWidthPx - Math.round(horCenterPosPx - colsWidthPx)) * Units.EMU_PER_PIXEL);
            anchor.setDy2(Math.round(rowsHeightPx - Math.round(vertCenterPosPx - rowsHeightPx)) * Units.EMU_PER_PIXEL);
        }
    }

    public int createBrandedHeaderSheet(Sheet sheet, String imageB64,
                                        int startRow, float rowHeight, int rowspan, int startCol, int colWidth, int colspan, int namespan, int dataspan,
                                        String documentName, String widgetName) {
        if (StringUtils.isNotEmpty(imageB64)) {
            for (int r = startRow; r < startRow+rowspan; r++) {
                sheet.createRow(r).setHeightInPoints(rowHeight);
                for (int c = startCol; c < startCol+colspan; c++) {
                    sheet.getRow(r).createCell(c);
                    sheet.setColumnWidth(c, colWidth * 256);
                }
            }

            // set brandend header image
            sheet.addMergedRegion(new CellRangeAddress(startRow, startRow+rowspan-1, startCol, startCol+colspan-1));

            drawBrandendHeaderImage(sheet, imageB64, Workbook.PICTURE_TYPE_PNG, startCol, startRow, colspan, rowspan);

            // set document name
            sheet.getRow(startRow).createCell(startCol+colspan).setCellValue(documentName);
            sheet.addMergedRegion(new CellRangeAddress(startRow, startRow, startCol+colspan, namespan));
            // set cell style
            CellStyle documentNameCellStyle = styleProvider.buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 16);
            sheet.getRow(startRow).getCell(startCol+colspan).setCellStyle(documentNameCellStyle);

            // set date
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            sheet.getRow(startRow+1).createCell(startCol+colspan).setCellValue("Data di generazione: " + dateFormat.format(date));
            sheet.addMergedRegion(new CellRangeAddress(startRow+1, startRow+1, startCol+colspan, dataspan));
            // set cell style
            CellStyle dateCellStyle = styleProvider.buildCellStyle(sheet, false, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 8);
            sheet.getRow(startRow+1).getCell(startCol+colspan).setCellStyle(dateCellStyle);
        }

        int headerIndex = (StringUtils.isNotEmpty(imageB64)) ? (startRow+rowspan) : 0;
        Row widgetNameRow = sheet.createRow((short) headerIndex);
        Cell widgetNameCell = widgetNameRow.createCell(0);
        widgetNameCell.setCellValue(widgetName);
        // set cell style
        CellStyle widgetNameStyle = styleProvider.buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 14);
        widgetNameCell.setCellStyle(widgetNameStyle);

        return headerIndex;
    }

    private void fillDashboardDriversSheetWithData(JSONArray drivers, Sheet sheet) {

        try {
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
                    DOCUMENT_NAME,
                    sheet.getSheetName());

            newheader = sheet.createRow((short) headerIndex + 1); // first row

            Cell cell = newheader.createCell(0);
            cell.setCellValue("Name");
            CellStyle headerCellStyle = styleProvider.buildCellStyle(sheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 11);
            cell.setCellStyle(headerCellStyle);

            Cell cell2 = newheader.createCell(1);
            cell2.setCellValue("Type");
            cell2.setCellStyle(headerCellStyle);

            Cell cell3 = newheader.createCell(2);
            cell3.setCellValue("Multivalue");
            cell3.setCellStyle(headerCellStyle);

            Cell cell4 = newheader.createCell(3);
            cell4.setCellValue("Value");
            cell4.setCellStyle(headerCellStyle);

            Cell cell5 = newheader.createCell(4);
            cell5.setCellValue("Url Name");
            cell5.setCellStyle(headerCellStyle);

            Cell cell6 = newheader.createCell(5);
            cell6.setCellValue("Driver label");
            cell6.setCellStyle(headerCellStyle);

            int j = headerIndex + 2;

            for (int i = 0; i < drivers.length(); i++) {
                JSONObject driver = drivers.getJSONObject(i);
                Row newRow = sheet.createRow(j);
                newRow.createCell(0).setCellValue(driver.getString("name"));
                newRow.createCell(1).setCellValue(driver.getString("type"));
                newRow.createCell(2).setCellValue(driver.getBoolean("multivalue"));
                newRow.createCell(3).setCellValue(driver.getString("value"));
                newRow.createCell(4).setCellValue(driver.getString("urlName"));
                newRow.createCell(5).setCellValue(driver.getString("driverLabel"));

                j++;
            }

        } catch (Exception e) {
            LOGGER.error("Cannot fill drivers sheet", e);
            throw new SpagoBIRuntimeException("Cannot fill drivers sheet", e);
        }

    }

    private JSONObject getDrivers(JSONObject body) {
        try {
            JSONObject driversToReturn = new JSONObject();
            JSONArray drivers = getDriversFromBody(body);
            if (drivers.length() > 0) {
                int counter = 0;
                for (int i = 0; i < drivers.length(); i++) {
                    JSONObject driver = drivers.getJSONObject(i);
                    driversToReturn.put("parameter" + (counter != 0 ? counter : ""), driver.getString("value"));
                    counter++;
                }
            }
            return driversToReturn;
        } catch (JSONException e) {
            return null;
        }
    }

    private JSONArray getDriversFromBody(JSONObject body) throws JSONException {
        if (body.has("drivers")) {
            return body.getJSONArray("drivers");
        } else {
            return new JSONArray();
        }
    }

    protected Map<String, Map<String, JSONArray>> getSelections(JSONObject body) {
        try {
            Map<String, Map<String, JSONArray>> selectionsToReturn = new HashMap<>();
            if (body.has("selections") && body.getJSONArray("selections").length() > 0) {
                for (int i = 0; i < body.getJSONArray("selections").length(); i++) {
                    JSONObject selection = body.getJSONArray("selections").getJSONObject(i);
                    if (!selectionsToReturn.containsKey(selection.getString("datasetLabel"))) {
                        selectionsToReturn.put(selection.getString("datasetLabel"), new HashMap<>());
                    }
                    if (!selectionsToReturn.get(selection.getString("datasetLabel")).containsKey(selection.getString("columnName"))) {
                        selectionsToReturn.get(selection.getString("datasetLabel")).put(selection.getString("columnName"), new JSONArray());
                    }
                    loopOverSelectionValues(selection, selectionsToReturn);
                }
            }
            return selectionsToReturn;
        } catch (JSONException e) {
            return Collections.emptyMap();
        }
    }

    private static void loopOverSelectionValues(JSONObject selection, Map<String, Map<String, JSONArray>> selectionsToReturn) throws JSONException {
        for (int j = 0; j < selection.getJSONArray("value").length(); j++) {
            String valueToInsert = "('" + selection.getJSONArray("value").getString(j) + "')";
            selectionsToReturn.get(selection.getString("datasetLabel")).get(selection.getString("columnName")).put(valueToInsert);
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

    private int exportDashboard(JSONArray widgetsArray, Workbook wb, String documentName, Map<String, Map<String, JSONArray>> selections, JSONObject drivers) {
        int exportedSheets = 0;
        for (int i = 0; i < widgetsArray.length(); i++) {
            try {
                JSONObject currWidget = widgetsArray.getJSONObject(i);
                exportedSheets = exportWidget(currWidget, wb, documentName, selections, drivers);
            } catch (Exception e) {
                LOGGER.error("Error while exporting widget", e);
            }
        }
        return exportedSheets;
    }

    private void fillDashboardSelectionsSheetWithData(Map<String, Map<String, JSONArray>> selections, Sheet selectionsSheet) {
        try {
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
                    selectionsSheet,
                    this.imageB64,
                    startRow,
                    rowHeight,
                    rowspan,
                    startCol,
                    colWidth,
                    colspan,
                    namespan,
                    dataspan,
                    DOCUMENT_NAME,
                    selectionsSheet.getSheetName());

            newheader = selectionsSheet.createRow((short) headerIndex + 1); // first row
            CellStyle headerCellStyle = styleProvider.buildCellStyle(selectionsSheet, true, HorizontalAlignment.LEFT, VerticalAlignment.CENTER, (short) 11);

            Cell cell = newheader.createCell(0);
            cell.setCellValue("Dataset");
            cell.setCellStyle(headerCellStyle);

            Cell cell2 = newheader.createCell(1);
            cell2.setCellValue("Field");
            cell2.setCellStyle(headerCellStyle);

            Cell cell3 = newheader.createCell(2);
            cell3.setCellValue("Values");
            cell3.setCellStyle(headerCellStyle);

            int j = headerIndex + 2;

            for (Map.Entry<String, Map<String, JSONArray>> entry : selections.entrySet()) {
                String datasetLabel = entry.getKey();
                Map<String, JSONArray> datasetSelections = entry.getValue();
                for (Map.Entry<String, JSONArray> datasetSelection : datasetSelections.entrySet()) {
                    String columnName = datasetSelection.getKey();
                    JSONArray values = datasetSelection.getValue();
                    for (int i = 0; i < values.length(); i++) {
                        Row newRow = selectionsSheet.createRow(j);
                        newRow.createCell(0).setCellValue(datasetLabel);
                        newRow.createCell(1).setCellValue(columnName);
                        newRow.createCell(2).setCellValue(values.getString(i));
                        j++;
                    }
                }
            }
            // Create a new row in current sheet

        } catch (Exception e) {
            LOGGER.error("Cannot fill selections sheet", e);
            throw new SpagoBIRuntimeException("Cannot fill selections sheet", e);
        }
    }

    public int exportWidget(JSONObject body, Workbook wb, String documentName, Map<String, Map<String, JSONArray>> selections, JSONObject drivers) {

        int exportedSheets;
        try {
            String widgetType = body.getString("type");
            if (Arrays.asList(WIDGETS_TO_IGNORE).contains(widgetType.toLowerCase())) {
                return 0;
            }
            IWidgetExporter widgetExporter = DashboardWidgetExporterFactory.getExporter(this,
                    wb, body, documentName, selections, drivers, datastoreUtils, styleProvider);
            exportedSheets = widgetExporter.export();
        } catch (Exception e) {
            LOGGER.error("Cannot export data to excel", e);
            throw new SpagoBIRuntimeException("Cannot export data to excel", e);
        }
        return exportedSheets;
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

    protected String getNumberFormatByPrecision(int precision, String initialFormat) {
        StringBuilder format = new StringBuilder(initialFormat);
        if (precision > 0) {
            format.append(".");
            format.append("0".repeat(precision));
        }
        return format.toString();
    }

    protected JSONArray filterDataStoreColumns(JSONArray columns) {
        try {
            for (int i = 0; i < columns.length(); i++) {
                String element = columns.getString(i);
                if (element != null && element.equals("recNo")) {
                    columns.remove(i);
                    break;
                }
            }
        } catch (JSONException e) {
            LOGGER.error("Can not filter Columns Array");
        }
        return columns;
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
                hiddenColumns = getDashboardHiddenColumnsList(settings);
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
            this.imageB64 = OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName());
            int startRow = 0;
            float rowHeight = 35; // in points
            int rowspan = 2;
            int startCol = 0;
            int colWidth = 25;
            int colspan = 2;
            int namespan = 10;
            int dataspan = 10;

            buildFirstPageHeaders(wb, sheet, widgetName, offset, settings, startRow, rowHeight, rowspan, startCol, colWidth, colspan, namespan, dataspan, groupsAndColumnsMap, columnsOrdered);

            int isGroup = groupsAndColumnsMap.isEmpty() ? 0 : 1;

            replaceWithThemeSettingsIfPresent(settings);


            buildRowsAndCols(wb, sheet, offset, settings, rows, isGroup, startRow, rowspan, columnsOrdered, numberOfSummaryRows, summaryRowsLabels);
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

    private int doSummaryRowsLogic(JSONObject settings, int numberOfSummaryRows, List<String> summaryRowsLabels) throws JSONException {
        if (summaryRowsEnabled(settings)) {
            JSONArray list = settings.getJSONObject("configuration").getJSONObject("summaryRows").getJSONArray("list");
            numberOfSummaryRows = list.length();

            for (int i = 0; i < numberOfSummaryRows; i++) {
                summaryRowsLabels.add(list.getJSONObject(i).getString("label"));
            }

        }
        return numberOfSummaryRows;
    }

    private static void replaceWithThemeSettingsIfPresent(JSONObject settings) throws JSONException {
        IDashboardThemeDAO dao = DAOFactory.getDashboardThemeDAO();
        Optional<SbiDashboardTheme> optionalTheme = dao.readByThemeName(settings.getJSONObject("style").optString("themeName"));
        if (optionalTheme.isPresent()) {
            SbiDashboardTheme dashboardTheme = optionalTheme.get();
            settings.remove("style");
            settings.put("style", dashboardTheme.getConfig().getJSONObject("table").getJSONObject("style"));
        }
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
            this.imageB64 = OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName());
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

    private void buildFirstPageHeaders(Workbook wb, Sheet sheet, String widgetName, int offset, JSONObject settings, int startRow, float rowHeight, int rowspan, int startCol, int colWidth, int colspan, int namespan, int dataspan, Map<String, String> groupsAndColumnsMap, JSONArray columnsOrdered) throws JSONException {
        if (offset == 0) { // if pagination is active, headers must be created only once
            Row header = createHeader(sheet, startRow, rowHeight, rowspan, startCol, colWidth, colspan, namespan, dataspan, widgetName, groupsAndColumnsMap, columnsOrdered);
            for (int i = 0; i < columnsOrdered.length(); i++) {
                JSONObject column = columnsOrdered.getJSONObject(i);
                String columnName = column.getString("header");
                // renaming table columns names of the excel export
                columnName = getInternationalizedHeader(columnName);

                Cell cell = header.createCell(i);
                cell.setCellValue(columnName);

                XSSFFont font = (XSSFFont) wb.createFont();
                styleProvider.buildHeaderCellStyle(wb, sheet, settings, font, cell);
            }

            // adjusts the column width to fit the contents
            styleProvider.adjustColumnWidth(sheet, this.imageB64);
        }
    }

    protected String getInternationalizedHeader(String columnName) {
        if (i18nMessages == null) {
            I18NMessagesDAO messageDao = DAOFactory.getI18NMessageDAO();
            try {
                i18nMessages = messageDao.getAllI18NMessages(locale);
            } catch (Exception e) {
                LOGGER.error("Error while getting i18n messages", e);
                i18nMessages = new HashMap<>();
            }
        }
        return i18nMessages.getOrDefault(columnName, columnName);
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
                this.imageB64,
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
        Map<String, JSONArray> columnStylesMap = styleProvider.getStylesMap(settings);
        Map<String, CellStyle> columnsCellStyles = new HashMap<>();
        CellStyle cellStyle = null;
        JSONObject alternatedRows = styleProvider.getRowStyle(settings);
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
            String defaultRowBackgroundColor = styleProvider.getDefaultRowBackgroundColor(alternatedRows, rowIsEven);

            boolean styleAlreadyAppliedToPreviousCells = false;
            String styleKeyToApplyToTheEntireRow = null;
            List<Boolean> styleCanBeOverriddenByWholeRowStyle = new ArrayList<>();
            cellStyle = buildCols(wb, sheet, settings, rows, columnsOrdered, columnStylesMap, cellStyle, columnsCellStyles, numberOfSummaryRows, summaryRowsLabels, row, rowObject, r, styleCanBeOverriddenByWholeRowStyle, rawCurrentNumberType, styleAlreadyAppliedToPreviousCells, defaultRowBackgroundColor, styleKeyToApplyToTheEntireRow);
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
            if (r >= rows.length() - numberOfSummaryRows) {
                CellStyle summaryCellStyle = styleProvider.buildPoiCellStyle(styleProvider.getStyleCustomObjFromProps(sheet, settings.getJSONObject("style").getJSONObject("summary"), ""), (XSSFFont) wb.createFont(), wb);
                cell.setCellStyle(summaryCellStyle);
            } else {
                JSONObject theRightStyle = styleProvider.getTheRightStyleByColumnIdAndValue(columnStylesMap, stringifiedValue, column.optString("id"));

                styleCanBeOverriddenByWholeRowStyle.add(c, styleProvider.styleCanBeOverridden(theRightStyle));
                if (theRightStyle.has("applyToWholeRow") && theRightStyle.getBoolean("applyToWholeRow")) {
                    styleKey = styleProvider.getStyleKey(column, theRightStyle, rawCurrentNumberType);
                    if (!styleAlreadyAppliedToPreviousCells) {
                        cellStyle = styleProvider.getCellStyleByStyleKey(wb, sheet, styleKey, columnsCellStyles, theRightStyle, defaultRowBackgroundColor);
                        styleProvider.applyWholeRowStyle(c, styleCanBeOverriddenByWholeRowStyle, row, cellStyle);
                        styleAlreadyAppliedToPreviousCells = true;
                    }
                    styleKeyToApplyToTheEntireRow = styleKey;
                } else if (styleKeyToApplyToTheEntireRow != null && styleProvider.styleCanBeOverridden(theRightStyle)) {
                    cellStyle = columnsCellStyles.get(styleKeyToApplyToTheEntireRow);
                } else {
                    styleKey = styleProvider.getStyleKey(column, theRightStyle, rawCurrentNumberType);
                    cellStyle = styleProvider.getCellStyleByStyleKey(wb, sheet, styleKey, columnsCellStyles, theRightStyle, defaultRowBackgroundColor);
                }
                cell.setCellStyle(cellStyle);
            }
            doTypeLogic(wb, getPrecisionByColumn(settings, column), type, cell, stringifiedValue);

            if (r >= rows.length() - numberOfSummaryRows) {
                cell.setCellValue(summaryRowsLabels.get(r - (rows.length() - numberOfSummaryRows)).concat(": ").concat(stringifiedValue));
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
            CellStyle groupsCellStyle = styleProvider.buildCellStyle(sheet, true, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, (short) 11);
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

    public void createPivotTable(Workbook workbook, Sheet sheet, JSONObject widget, String widgetName) {

        SXSSFWorkbook sxssfWorkbook = (SXSSFWorkbook) workbook;
        XSSFSheet pivotSheet = sxssfWorkbook.getXSSFWorkbook().createSheet(widgetName);

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

        createBrandedHeaderSheet(
                pivotSheet,
                this.imageB64,
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

        int sourceSheetLastRow = sheet.getLastRowNum();
        int sourceSheetLastColumn = sheet.getRow(sourceSheetLastRow).getLastCellNum();
        String sourceSheetFinalCell = new CellReference(sourceSheetLastRow, sourceSheetLastColumn - 1).formatAsString();
        int targetSheetLastRow = pivotSheet.getLastRowNum();
        CellReference position = new CellReference(targetSheetLastRow + 1, 0);
        XSSFPivotTable pivotTable = pivotSheet.createPivotTable(new AreaReference(this.imageB64 == null || this.imageB64.isEmpty() ? "A2:".concat(sourceSheetFinalCell) : "A4:".concat(sourceSheetFinalCell), workbook.getSpreadsheetVersion()), position, sheet);

        try {
            JSONObject fields = widget.getJSONObject("fields");
            JSONArray columns = fields.getJSONArray("columns");
            JSONArray rows = fields.getJSONArray("rows");
            JSONArray data = fields.getJSONArray("data");

            int counter = 0;

            for (int i = 0; i < columns.length(); ++i) {
                pivotTable.addRowLabel(counter);
                counter++;
            }

            for (int i = 0; i < rows.length(); ++i) {
                pivotTable.addRowLabel(counter);
                counter++;
            }

            for (int i = 0; i < data.length(); ++i) {
                JSONObject datum = data.getJSONObject(i);
                DataConsolidateFunction function = getAggregationFunction(datum.getString("aggregation").toUpperCase());
                pivotTable.addColumnLabel(function, counter, datum.getString("alias"));
                counter++;
            }

            workbook.setSheetVisibility(workbook.getSheetIndex(sheet), SheetVisibility.VISIBLE);

        } catch (JSONException e) {
            LOGGER.error("Error while creating pivot table", e);
        }
    }

    private DataConsolidateFunction getAggregationFunction(String aggregation) {

        return switch (aggregation) {
            case "SUM" -> SUM;
            case "COUNT" -> COUNT;
            case "AVG" -> AVERAGE;
            case "MAX" -> MAX;
            case "MIN" -> MIN;
            default -> null;
        };
    }

    protected List<String> getDashboardHiddenColumnsList(JSONObject settings) {
        try {
            List<String> hiddenColumns = new ArrayList<>();
            if (areVisibilityConditionsEnabled(settings)) {
                JSONObject visualization = settings.getJSONObject("visualization");
                JSONObject visibilityConditions = visualization.getJSONObject("visibilityConditions");
                JSONArray conditions = visibilityConditions.getJSONArray("conditions");

                for (int i = 0; i < conditions.length(); i++) {
                    JSONObject condition = conditions.getJSONObject(i);
                    if (columnMustBeHidden(condition)) {
                        JSONArray target;
                        try {
                            target = condition.getJSONArray("target");
                        } catch (JSONException e) {
                            target = new JSONArray();
                            target.put(condition.getString("target"));
                        }
                        for (int j = 0; j < target.length(); j++) {
                            String targetColumn = target.getString(j);
                            hiddenColumns.add(targetColumn);
                        }
                    }
                }
            }
            return hiddenColumns;
        } catch (JSONException je) {
            LOGGER.error("Error while getting hidden columns list", je);
            return new ArrayList<>();
        }
    }

    private boolean columnMustBeHidden(JSONObject condition) {
        try {
            JSONObject conditionDefinition = condition.getJSONObject("condition");

            return  (conditionDefinition.getString("type").equals("always") &&
                    condition.getBoolean("hide"))
                    ||
                    (conditionDefinition.getString("type").equals("variable") &&
                            condition.getBoolean("hide") && conditionIsApplicable(conditionDefinition.getString("variableValue"), conditionDefinition.getString("operator"), conditionDefinition.getString("value")));

        } catch (JSONException jsonException) {
            LOGGER.error("Error while evaluating if column must be hidden according to variable.", jsonException);
            return false;
        }
    }

    private boolean areVisibilityConditionsEnabled(JSONObject settings) throws JSONException {
        JSONObject visualization = jsonObjectUtils.getVisualizationFromSettings(settings);
        return settings.has("visualization") &&
                visualization.has("visibilityConditions") &&
                visualization.getJSONObject("visibilityConditions").getBoolean("enabled") &&
                visualization.getJSONObject("visibilityConditions").has("conditions");
    }

    protected JSONArray getDashboardTableOrderedColumns(JSONArray columnsNew, List<String> hiddenColumns, JSONArray columnsOld) {
        JSONArray columnsOrdered = new JSONArray();
        // new columns are in the correct order
        // for each of them we have to find the correspondent old column and push it into columnsOrdered
        try {
            for (int i = 0; i < columnsNew.length(); i++) {

                JSONObject columnNew = columnsNew.getJSONObject(i);

                if (hiddenColumns.contains(columnNew.getString("id"))) {
                    continue;
                }

                for (int j = 0; j < columnsOld.length(); j++) {
                    JSONObject columnOld = columnsOld.getJSONObject(j);

                    if (columnOld.getString("header").equals(columnNew.getString("alias"))) {
                        columnOld.put("id", columnNew.getString("id"));

                        if (columnNew.has("ranges")) {
                            JSONArray ranges = columnNew.getJSONArray("ranges");
                            columnOld.put("ranges", ranges); // added ranges for column thresholds
                        }

                        columnsOrdered.put(columnOld);
                        break;
                    }
                }
            }
            return columnsOrdered;
        } catch (Exception e) {
            LOGGER.error("Error retrieving ordered columns");
            return new JSONArray();
        }
    }

    protected int getPrecisionByColumn(JSONObject settings, JSONObject column) {
        try {
            JSONObject visualization = jsonObjectUtils.getVisualizationFromSettings(settings);
            if (visualization == null)
                return -1;

            JSONObject visualizationTypes = visualization.getJSONObject("visualizationTypes");

            if (visualizationTypes == null) {
                return -1;
            }

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
        } catch (Exception e) {
            LOGGER.error("Error while getting precision by column", e);
        }
        return -1;
    }

    protected final Map<String, String> getDashboardGroupAndColumnsMap(JSONObject widgetContent, JSONArray groupsArray) {
        Map<String, String> mapGroupsAndColumns = new HashMap<>();
        try {
            if (widgetContent.get("columns") instanceof JSONArray)
                mapGroupsAndColumns = getMapFromDashboardGroupsArray(groupsArray,
                        widgetContent.getJSONArray("columns"));
        } catch (JSONException e) {
            LOGGER.error("Couldn't retrieve groups", e);
        }
        return mapGroupsAndColumns;
    }

    protected Map<String, String> getMapFromDashboardGroupsArray(JSONArray groupsArray, JSONArray aggr) {
        Map<String, String> returnMap = new HashMap<>();
        try {
            if (aggr != null && groupsArray != null) {

                for (int i = 0; i < groupsArray.length(); i++) {

                    String groupName = groupsArray.getJSONObject(i).getString("label");
                    JSONArray columns = groupsArray.getJSONObject(i).getJSONArray("columns");

                    for (int ii = 0; ii < aggr.length(); ii++) {
                        JSONObject column = aggr.getJSONObject(ii);

                        if (columns.toString().contains(column.getString("id"))) {
                            String nameToInsert = column.getString("alias");
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

    protected final JSONArray getGroupsFromDashboardWidget(JSONObject settings) throws JSONException {
        // column.header matches with name or alias
        // Fill Header
        JSONArray groupsArray = new JSONArray();
        JSONObject configuration = jsonObjectUtils.getConfigurationFromSettings(settings);
        if (configuration.has("columnGroups") && configuration.getJSONObject("columnGroups").getBoolean("enabled")) {
            groupsArray = configuration.getJSONObject("columnGroups").getJSONArray("groups");
        }
        return groupsArray;
    }
}
