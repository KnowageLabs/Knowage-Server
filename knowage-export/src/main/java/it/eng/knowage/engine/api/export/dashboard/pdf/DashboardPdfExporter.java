package it.eng.knowage.engine.api.export.dashboard.pdf;

import be.quodlibet.boxable.*;
import it.eng.knowage.engine.api.export.dashboard.DashboardExporter;
import it.eng.knowage.engine.api.export.dashboard.Style;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.log4j.Logger;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class DashboardPdfExporter extends DashboardExporter {

    public static Logger logger = Logger.getLogger(DashboardPdfExporter.class);

    private static final float POINTS_PER_INCH = 72;
    private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
    private static final int DEFAULT_COLUMN_WIDTH = 150;
    protected static final String DATE_FORMAT = "dd/MM/yyyy";

    private float totalColumnsWidth = 0;
    private float[] columnPercentWidths;

    public DashboardPdfExporter(String userUniqueIdentifier, JSONObject body) {
        super(userUniqueIdentifier);
        locale = getLocaleFromBody(body);
    }

    public byte[] getBinaryData(JSONObject template) throws JSONException {

        String creationUser;

        if (template == null) {
            throw new SpagoBIRuntimeException("Unable to get template for dashboard");
        }

        Map<String, Map<String, JSONArray>> selections = getSelections(template);

        JSONObject drivers = transformDriversForDatastore(getDrivers(template));
        JSONObject parameters = transformParametersForDatastore(template, getParametersFromBody(template));
        //TODO GET THE CREATION USER FROM THE BODY
        creationUser = "DEFAULT_TO_CHANGE";

        try (PDDocument document = new PDDocument(MemoryUsageSetting.setupTempFileOnly())) {
            String widgetId = template.getString("id");

            exportTableWidget(document, template, widgetId, creationUser, selections, drivers, parameters);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.save(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new SpagoBIRuntimeException("Unable to generate output file", e);
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Cannot export data to PDF", e);
        }
    }

    private void exportTableWidget(PDDocument document, JSONObject widget, String widgetId, String creationUser, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, JSONObject parameters) {
        try {
            JSONObject settings = widget.optJSONObject("settings");

            JSONObject dataStore;
            int totalNumberOfRows = 0;
            int offset = 0;
            int fetchSize = Integer
                    .parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
            BaseTable table;
            JSONObject metadata;
            JSONArray columns;
            JSONArray rows;
            JSONArray columnsOrdered = null;
            String[] columnDateFormats = null;
            int numberOfSummaryRows = 0;
            List<String> summaryRowsLabels = new ArrayList<>();
            PDFont font;
            URL resource = getClass().getClassLoader().getResource("/fonts/DejaVuSans.ttf");
            if (resource == null) {
                throw new SpagoBIRuntimeException("Unable to find font file");
            }
            File pdfFontFile = new File(resource.toURI());
            Map<String, JSONArray> columnStylesMap = Map.of();
            do {
                dataStore = getDataStoreForDashboardWidget(widget, offset, fetchSize, selections, drivers, parameters);

                //this method is present in the cockpit pdf exporter
                PDPage newPage = createPage(widget);
                document.addPage(newPage);
                table = createBaseTable(document, newPage);

                JSONObject widgetData = dataStore.getJSONObject("widgetData");

                if (offset == 0) {
                    metadata = dataStore.getJSONObject("metaData");
                    columns = metadata.getJSONArray("fields");
                    columns = filterDataStoreColumns(columns);

                    List<String> hiddenColumns;
                    if (widgetData.has("columns") && widgetData.getJSONArray("columns").length() > 0) {
                        hiddenColumns = getDashboardHiddenColumnsList(settings, "hide");
                        columnsOrdered = getDashboardTableOrderedColumns(widgetData.getJSONArray("columns"), hiddenColumns, columns);
                    } else {
                        columnsOrdered = columns;
                    }
                    JSONArray groupsFromWidgetContent = getGroupsFromDashboardWidget(settings);
                    Map<String, String> groupsAndColumnsMap = getDashboardGroupAndColumnsMap(widgetData, groupsFromWidgetContent);

                    numberOfSummaryRows = doSummaryRowsLogic(settings, numberOfSummaryRows, summaryRowsLabels);
                    totalNumberOfRows = dataStore.getInt("results");
                    font = PDType0Font.load(table.document, pdfFontFile);
                    replaceWithThemeSettingsIfPresent(settings);
                    columnStylesMap = getStylesMap(settings);
                    initColumnWidths(columnsOrdered, columnStylesMap);
                    buildFirstPageHeaders(table, settings, groupsAndColumnsMap, columnsOrdered, creationUser, selections, font);
                }

                rows = dataStore.getJSONArray("rows");

                font = PDType0Font.load(table.document, pdfFontFile);

                buildRowsAndCols(table, settings, rows, columnsOrdered, numberOfSummaryRows, summaryRowsLabels, font, columnDateFormats, columnStylesMap);

                offset += fetchSize;

                table.draw();


            } while (offset < totalNumberOfRows);


        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Unable to export table widget: " + widgetId, e);
        }
    }

    private void initColumnWidths(JSONArray columnsOrdered, Map<String, JSONArray> columnStylesMap) throws JSONException {
        columnPercentWidths = new float[columnsOrdered.length() + 10];
        for (int i = 0; i < columnsOrdered.length(); i++) {
            JSONObject column = columnsOrdered.getJSONObject(i);
            JSONObject style = getTheRightStyleByColumnIdAndValue(columnStylesMap, "", column.getString("id"), null);
            if (style != null && style.optJSONObject("properties") != null && style.getJSONObject("properties").has("width") && style.getJSONObject("properties").get("width") != null && style.getJSONObject("properties").get("width") instanceof Number) {
                columnPercentWidths[i] = style.getJSONObject("properties").getInt("width") > 0 ? style.getJSONObject("properties").getInt("width") : DEFAULT_COLUMN_WIDTH;
            } else {
                columnPercentWidths[i] = DEFAULT_COLUMN_WIDTH;
            }
            totalColumnsWidth += columnPercentWidths[i];
        }

        for (int i = 0; i < columnsOrdered.length(); i++) {
            columnPercentWidths[i] = (columnPercentWidths[i] / totalColumnsWidth) * 100;
        }
    }

    private void buildFirstPageHeaders(BaseTable table, JSONObject settings, Map<String, String> groupsAndColumnsMap, JSONArray columnsOrdered, String creationUser, Map<String, Map<String, JSONArray>> selections, PDFont font) throws JSONException {
        createDocumentInformationRow(table, font, creationUser);
        createSelectionsRows(table, font, selections);

        if (!groupsAndColumnsMap.isEmpty()) {
            Row<PDPage> groupHeaderRow = table.createRow(15f);
            for (int i = 0; i < columnsOrdered.length(); i++) {
                JSONObject column = columnsOrdered.getJSONObject(i);
                String groupName = groupsAndColumnsMap.get(column.get("header"));
                if (groupName != null) {

                    Cell<PDPage> cell = groupHeaderRow.createCell(columnPercentWidths[i], groupName,
                            HorizontalAlignment.get("center"), VerticalAlignment.get("top"));
                    styleHeaderCell(settings, cell, font);
                    // check if adjacent header cells have same group names in order to add merged region
                    int adjacents = getAdjacentEqualNamesAmount(groupsAndColumnsMap, columnsOrdered, i, groupName);
                    if (adjacents > 1) {
                        cell.setRightBorderStyle(null);
                        for (int j = 1; j < adjacents; j++) {
                            cell = groupHeaderRow.createCell(columnPercentWidths[i+j], "",
                                    HorizontalAlignment.get("center"), VerticalAlignment.get("top"));
                            styleHeaderCell(settings, cell, font);
                            cell.setLeftBorderStyle(null);
                            if (j + 1 < adjacents) {
                                cell.setRightBorderStyle(null);
                            }
                        }
                    }
                    i += adjacents - 1;
                } else {
                    Cell<PDPage> blankCell = groupHeaderRow.createCell(columnPercentWidths[i], "",
                            HorizontalAlignment.get("center"), VerticalAlignment.get("top"));
                    styleHeaderCell(settings, blankCell, font);
                }

            }
            table.addHeaderRow(groupHeaderRow);
        }

        Row<PDPage> headerRow = table.createRow(15f);

        for (int i = 0; i < columnsOrdered.length(); i++) {

            JSONObject column = columnsOrdered.getJSONObject(i);
            String columnName = column.getString("alias");

            Cell<PDPage> cell = headerRow.createCell(columnPercentWidths[i], columnName,
                    HorizontalAlignment.get("center"), VerticalAlignment.get("top"));
            styleHeaderCell(settings, cell, font);
        }

        table.addHeaderRow(headerRow);
    }

    private void styleHeaderCell(JSONObject settings, Cell<PDPage> headerCell, PDFont font) throws JSONException {
        JSONObject styleJSONObject = getJsonObjectUtils().getStyleFromSettings(settings);
        if (settings != null && settings.has("style") && styleJSONObject.has("headers")) {
            Style style = getStyleCustomObjFromProps(null, styleJSONObject.getJSONObject("headers"), "");
            headerCell.setFont(font);
            headerCell.setFontSize(Float.parseFloat(style.getFontSize().replace("px", "")));
            headerCell.setFillColor(getJavaColorFromRGBA(style.getBackgroundColor()));
            headerCell.setTextColor(getJavaColorFromRGBA(style.getColor()));
        }
    }

    //TODO ALREADY PRESENT IN THE COCKPIT EXPORTER
    private static void createDocumentInformationRow(BaseTable table, PDFont font, String creationUser) {
        Row<PDPage> documentInformationRow = table.createRow(12f);
        String executionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        Cell<PDPage> executionDateCell = documentInformationRow.createCell(50, "Execution Date: " + executionDate,
                HorizontalAlignment.get("center"), VerticalAlignment.get("middle"));
        executionDateCell.setFont(font);
        executionDateCell.setFillColor(Color.LIGHT_GRAY);
        executionDateCell.setTextColor(Color.BLACK);
        executionDateCell.setFontSize(10);
        Cell<PDPage> creationUserCell = documentInformationRow.createCell(50, "Creation User: " + creationUser,
                HorizontalAlignment.get("center"), VerticalAlignment.get("middle"));
        creationUserCell.setFont(font);
        creationUserCell.setFillColor(Color.LIGHT_GRAY);
        creationUserCell.setTextColor(Color.BLACK);
        creationUserCell.setFontSize(10);
    }

    private static void createSelectionsRows(BaseTable table, PDFont font, Map<String, Map<String, JSONArray>> selections) {
        try {
            if (selections != null && !selections.isEmpty()) {
                for (Map.Entry<String, Map<String, JSONArray>> datasetEntry : selections.entrySet()) {
                    String datasetName = datasetEntry.getKey();
                    Map<String, JSONArray> selectionContent = datasetEntry.getValue();

                    if (selectionContent == null || selectionContent.isEmpty()) {
                        continue;
                    }

                    for (Map.Entry<String, JSONArray> columnEntry : selectionContent.entrySet()) {
                        String subKey = columnEntry.getKey();
                        JSONArray valuesArray = columnEntry.getValue();

                        Row<PDPage> selectionRow = table.createRow(12f);

                        Cell<PDPage> selectionDatasetNameCell = selectionRow.createCell(
                                50,
                                "Selections Dataset: " + datasetName,
                                HorizontalAlignment.get("right"),
                                VerticalAlignment.get("middle")
                        );
                        selectionDatasetNameCell.setFont(font);
                        selectionDatasetNameCell.setFillColor(Color.LIGHT_GRAY);
                        selectionDatasetNameCell.setTextColor(Color.BLACK);
                        selectionDatasetNameCell.setFontSize(10);

                        StringBuilder sb = new StringBuilder();
                        if (valuesArray != null) {
                            for (int i = 0; i < valuesArray.length(); i++) {
                                if (i > 0) sb.append(", ");
                                Object v = valuesArray.opt(i);
                                sb.append(v != null ? String.valueOf(v) : "null");
                            }
                        }

                        Cell<PDPage> subSelectionValueCell = selectionRow.createCell(
                                50,
                                "COLUMN " + subKey + ": " + sb,
                                HorizontalAlignment.get("left"),
                                VerticalAlignment.get("middle")
                        );
                        subSelectionValueCell.setFont(font);
                        subSelectionValueCell.setFillColor(Color.LIGHT_GRAY);
                        subSelectionValueCell.setTextColor(Color.BLACK);
                        subSelectionValueCell.setFontSize(10);
                    }
                }
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Couldn't add selection information to the table", e);
        }
    }

    private void buildRowsAndCols(BaseTable table, JSONObject settings, JSONArray rows, JSONArray columnsOrdered, int numberOfSummaryRows, List<String> summaryRowsLabels, PDFont font, String[] columnDateFormats,  Map<String, JSONArray> styles) throws JSONException {

        // Check if summary row is enabled
        boolean summaryRowEnabled = settings.getJSONObject("configuration").has("summaryRows") &&
                settings.getJSONObject("configuration").getJSONObject("summaryRows").getBoolean("enabled");
        DateFormat inputDateFormat = new SimpleDateFormat(DATE_FORMAT, getLocale());

        for (int r = 0; r < rows.length(); r++) {
            JSONObject rowObject = rows.getJSONObject(r);
            Row<PDPage> row = table.createRow(10);
            List<Boolean> styleCanBeOverriddenByWholeRowStyle = new ArrayList<>();
            boolean styleAlreadyAppliedToPreviousCells = false;
            String styleKeyToApplyToTheEntireRow = null;
            Map<String, Style> columnsCellStyles = new HashMap<>();
            String defaultRowBackgroundColor;
            boolean rowIsEven = r % 2 == 0;
            String currentRowType = rowIsEven ? "even" : "odd";
            JSONObject alternatedRows = getRowStyle(settings);
            defaultRowBackgroundColor = getDefaultRowBackgroundColor(alternatedRows, rowIsEven);
            Style cellStyle = null;

            buildColumns(settings, rows, columnsOrdered, numberOfSummaryRows, summaryRowsLabels, font, columnDateFormats, styles, rowObject, inputDateFormat, row, styleCanBeOverriddenByWholeRowStyle, currentRowType, styleAlreadyAppliedToPreviousCells, cellStyle, columnsCellStyles, defaultRowBackgroundColor, styleKeyToApplyToTheEntireRow, r, summaryRowEnabled);
        }
    }

    private void buildColumns(JSONObject settings, JSONArray rows, JSONArray columnsOrdered, int numberOfSummaryRows, List<String> summaryRowsLabels, PDFont font, String[] columnDateFormats, Map<String, JSONArray> styles, JSONObject rowObject, DateFormat inputDateFormat, Row<PDPage> row, List<Boolean> styleCanBeOverriddenByWholeRowStyle, String currentRowType, boolean styleAlreadyAppliedToPreviousCells, Style cellStyle, Map<String, Style> columnsCellStyles, String defaultRowBackgroundColor, String styleKeyToApplyToTheEntireRow, int r, boolean summaryRowEnabled) throws JSONException {
        for (int c = 0; c < columnsOrdered.length(); c++) {

            JSONObject column = columnsOrdered.getJSONObject(c);
            String type = column.getString("type");
            String colIndex = column.getString("name"); // column_1, column_2, column_3...
            Object value = rowObject.get(colIndex);
            if (value != null) {
                String valueStr = value.toString();
                if (value instanceof Number) {
                    if (value instanceof Double) {
                        valueStr = new BigDecimal(valueStr).toPlainString();
                    } else {
                        valueStr = value.toString();
                    }
                }
                JSONObject style = getTheRightStyleByColumnIdAndValue(styles, valueStr, column.getString("id"), defaultRowBackgroundColor);
                if (type.equalsIgnoreCase("float")) {
                    int precision = style.optInt("precision");
                    int pos = valueStr.indexOf(".");
                    // offset = 0 se devo tagliare fuori anche la virgola ( in caso precision fosse 0 )
                    int offset = (precision == 0 ? 0 : 1);
                    if (pos != -1 && valueStr.length() >= pos + precision + offset) {
                        try {
                            valueStr = valueStr.substring(0, pos + precision + offset);
                        } catch (Exception e) {
                            // value stays as it is
                            logger.error("Cannot format value according to precision", e);
                        }
                    } else {
                        logger.warn("Cannot format value according to precision. Value: " + valueStr);
                    }
                }
                if (type.equalsIgnoreCase("date")) {
                    try {
                        DateFormat outputDateFormat = new SimpleDateFormat(columnDateFormats[c], getLocale());
                        Date date = inputDateFormat.parse(valueStr);
                        valueStr = outputDateFormat.format(date);
                    } catch (Exception e) {
                        // value stays as it is
                        logger.warn("Cannot format date value. Value: " + valueStr, e);
                    }
                }
                valueStr = workaroundToRemoveCommonProblematicChars(valueStr);

                Cell<PDPage> cell = row.createCell(columnPercentWidths[c], valueStr,
                        HorizontalAlignment.get("center"), VerticalAlignment.get("top"));

                cell.setFont(font);
                // first of all set alternate rows color
                String styleKey;

                styleCanBeOverriddenByWholeRowStyle.add(c, styleCanBeOverridden(style));
                if (style.has("applyToWholeRow") && style.getBoolean("applyToWholeRow")) {
                    styleKey = getStyleKey(column, style, currentRowType);
                    if (!styleAlreadyAppliedToPreviousCells) {
                        cellStyle = getCellStyleByStyleKey(styleKey, columnsCellStyles, style, defaultRowBackgroundColor);
                        applyWholeRowStyle(c, styleCanBeOverriddenByWholeRowStyle, row, cellStyle);
                        styleAlreadyAppliedToPreviousCells = true;
                    }
                    styleKeyToApplyToTheEntireRow = styleKey;
                } else if (styleKeyToApplyToTheEntireRow != null && styleCanBeOverridden(style)) {
                    cellStyle = columnsCellStyles.get(styleKeyToApplyToTheEntireRow);
                } else {
                    styleKey = getStyleKey(column, style, currentRowType);
                    cellStyle = getCellStyleByStyleKey(styleKey, columnsCellStyles, style, defaultRowBackgroundColor);
                }

                applyStyleToCell(cellStyle, cell);

                // If summary row is enabled, add the summary label to the value
                if (r == (rows.length() - 1) && summaryRowEnabled) {
                    if (isSummaryColumnVisible(getDashboardHiddenColumnsList(settings, "hideFromSummary"), column)) {
                        String label = "";
                        if (colIndex.equals("column_1")) {
                            label = summaryRowsLabels.get(r - (rows.length() - numberOfSummaryRows)).concat(" ");
                        }
                        cell.setText(label + valueStr);
                    }
                }
            }
        }
    }

    private void applyWholeRowStyle(int c, List<Boolean> styleCanBeOverriddenByWholeRowStyle, Row<PDPage> row, Style cellStyle) {
        for (int previousCell = c - 1; previousCell >= 0; previousCell--) {
            if (styleCanBeOverriddenByWholeRowStyle.get(previousCell).equals(Boolean.TRUE)) {
                Cell<PDPage> cell = row.getCells().get(previousCell);
                applyStyleToCell(cellStyle, cell);
            }
        }
    }
    
    private void applyStyleToCell(Style style, Cell<PDPage> cell) {
        if (!style.getBackgroundColor().isEmpty()) {
            cell.setFillColor(getJavaColorFromRGBA(style.getBackgroundColor()));
        }
        if (!style.getColor().isEmpty()) {
            cell.setTextColor(getJavaColorFromRGBA(style.getColor()));
        }
    }


    private Style getCellStyleByStyleKey(String styleKey, Map<String, Style> columnsCellStyles, JSONObject theRightStyle, String defaultRowBackgroundColor) {
        Style cellStyle;
        if (columnsCellStyles.containsKey(styleKey)) {
            cellStyle = columnsCellStyles.get(styleKey);
        } else {
            cellStyle = getStyleCustomObjFromProps(null, theRightStyle, defaultRowBackgroundColor);
            columnsCellStyles.put(styleKey, cellStyle);
        }
        return cellStyle;
    }


    private Color getJavaColorFromRGBA(String colorStr) {
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

            return new Color(red, green, blue);
    }


    //TODO GENERALISE THIS METHOD
    private BaseTable createBaseTable(PDDocument doc, PDPage page) {
        try {
            float margin = 20;
            // starting y position is whole page height subtracted by top and bottom margin
            float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
            float bottomMargin = 20;
            float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
            // y position is your coordinate of top left corner of the table
            Assert.assertTrue(tableWidth > 0, "Page dimension is too small!");
            float yPosition = 550;
            return new BaseTable(yPosition, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Cannot create PDF Base Table object:", e);
        }
    }

    //TODO WATCH OUT FOR THE PAGE CREATION (ALREADY PRESENT IN THE COCKPIT EXPORTER)
    private PDPage createPage(JSONObject widget) {
        try {
            JSONObject settings = widget.getJSONObject("settings");
            JSONObject exportPdf = getJsonObjectUtils().getConfigurationFromSettings(settings).optJSONObject("exports").optJSONObject("pdf");
            int columnsLength = widget.getJSONArray("columns").length();
            if (exportPdf == null || exportPdf.length() == 0) {
                return new PDPage(calculateTableDimensions(columnsLength));
            } else if (exportPdf.optBoolean("a4portrait")) {
                return new PDPage(PDRectangle.A4);
            } else if (exportPdf.optBoolean("a4landscape")) {
                return new PDPage(new PDRectangle(297 * POINTS_PER_MM, 210 * POINTS_PER_MM));
            } else if (exportPdf.has("custom") && exportPdf.getJSONObject("custom").optBoolean("enabled")) {
                int width = exportPdf.getJSONObject("custom").getInt("width");
                int height = exportPdf.getJSONObject("custom").getInt("height");
                return new PDPage(new PDRectangle(width * POINTS_PER_MM, height * POINTS_PER_MM));
            } else {
                return new PDPage(calculateTableDimensions(columnsLength));
            }
        } catch (Exception e) {
            logger.error("Cannot instantiate custom page. Default A4 format will be used.", e);
            return new PDPage(PDRectangle.A4);
        }
    }

    private PDRectangle calculateTableDimensions(int columnsLength) {
        try {
            int totalWidth = 0;
            for (int i = 0; i < columnsLength; i++) {
                totalWidth += DEFAULT_COLUMN_WIDTH;
            }
            return new PDRectangle(totalWidth, 210 * POINTS_PER_MM);
        } catch (Exception e) {
            logger.error("Error while calculating dimensions. Default A4 format will be used.", e);
            return PDRectangle.A4;
        }
    }


    public String getMimeType() {
        return "application/pdf";
    }

    /**
     * Replace common problematic characters for Apache PDFBox, used by Boxable.
     * The characters below aren't available in common font like Times New Roman, Helvetica, etc... So we replace them with spaces.
     *
     * @since Ticket#2023051987000018
     */
    private String workaroundToRemoveCommonProblematicChars(String valueStr) {
        valueStr = valueStr.replace('\u00A0', ' ');
        valueStr = valueStr.replace('\u2007', ' ');
        valueStr = valueStr.replace('\u202F', ' ');
        valueStr = valueStr.replace('\n', ' ');
        valueStr = valueStr.replace('\r', ' ');
        valueStr = valueStr.replace('\t', ' ');
        return valueStr;
    }


}
