package it.eng.knowage.engine.api.export.dashboard;

import be.quodlibet.boxable.*;
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
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardPdfExporter extends DashboardExporter {

    public static Logger logger = Logger.getLogger(DashboardPdfExporter.class);

    private static final float POINTS_PER_INCH = 72;
    private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
    private static final int DEFAULT_COLUMN_WIDTH = 150;

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
            long widgetId = template.getLong("widget");

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

    private void exportTableWidget(PDDocument document, JSONObject widget, long widgetId, String creationUser, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, JSONObject parameters) {
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
            int isColumnGroupingPresent = 0;
            int numberOfSummaryRows = 0;
            List<String> summaryRowsLabels = new ArrayList<>();
            PDFont font;
            URL resource = getClass().getClassLoader().getResource("/fonts/DejaVuSans.ttf");
            if (resource == null) {
                throw new SpagoBIRuntimeException("Unable to find font file");
            }
            File pdfFontFile = new File(resource.toURI());
            Map<String, JSONArray> columnStylesMap;
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

                    String widgetName = getJsonObjectUtils().replacePlaceholderIfPresent(getJsonObjectUtils().getDashboardWidgetName(widget), drivers);
                    numberOfSummaryRows = doSummaryRowsLogic(settings, numberOfSummaryRows, summaryRowsLabels);
                    totalNumberOfRows = dataStore.getInt("results");
                    font = PDType0Font.load(table.document, pdfFontFile);
                    columnStylesMap = getStylesMap(settings);
                    initColumnWidths(columnsOrdered, settings, columnStylesMap);
                    buildFirstPageHeaders(table, widgetName, offset, settings, groupsAndColumnsMap, columnsOrdered, creationUser, selections, font);
                    isColumnGroupingPresent = groupsAndColumnsMap.isEmpty() ? 0 : 1;
                    replaceWithThemeSettingsIfPresent(settings);
                }

                rows = dataStore.getJSONArray("rows");

                font = PDType0Font.load(table.document, pdfFontFile);

                buildRowsAndCols(table, offset, settings, rows, isColumnGroupingPresent, columnsOrdered, numberOfSummaryRows, summaryRowsLabels, font, columnDateFormats);

                offset += fetchSize;

                table.draw();


            } while (offset < totalNumberOfRows);


        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Unable to export generic widget: " + widgetId, e);
        }
    }

    private void initColumnWidths(JSONArray columnsOrdered, JSONObject settings, Map<String, JSONArray> columnStylesMap) throws JSONException {
        columnPercentWidths = new float[columnsOrdered.length() + 10];
        for (int i = 0; i < columnsOrdered.length(); i++) {
            JSONObject column = columnsOrdered.getJSONObject(i);
            JSONObject style = getTheRightStyleByColumnIdAndValue(columnStylesMap, "", column.getString("id"));

        }


    }

    private void buildFirstPageHeaders(BaseTable table, String widgetName, int offset, JSONObject settings, Map<String, String> groupsAndColumnsMap, JSONArray columnsOrdered, String creationUser, Map<String, Map<String, JSONArray>> selections, PDFont font) throws JSONException {
        createDocumentInformationRow(table, font, creationUser);
        createSelectionsRows(table, font, selections);

//        if (!groupsAndColumnsMap.isEmpty()) {
//            Row<PDPage> groupHeaderRow = table.createRow(15f);
//            for (int i = 0; i < columnsOrdered.length(); i++) {
//                JSONObject column = columnsOrdered.getJSONObject(i);
//                String groupName = groupsAndColumnsMap.get(column.get("header"));
//                if (groupName != null) {
//
//                    Cell<PDPage> cell = groupHeaderRow.createCell(columnPercentWidths[i], groupName,
//                            HorizontalAlignment.get("center"), VerticalAlignment.get("top"));
//                    styleHeaderCell(style, cell, font);
//                    // check if adjacent header cells have same group names in order to add merged region
//                    int adjacents = getAdjacentEqualNamesAmount(groupsAndColumnsMap, columnsOrdered, i, groupName);
//                    if (adjacents > 1) {
//                        cell.setRightBorderStyle(null);
//                        for (int j = 1; j < adjacents; j++) {
//                            cell = groupHeaderRow.createCell(columnPercentWidths[i+j], "",
//                                    HorizontalAlignment.get("center"), VerticalAlignment.get("top"));
//                            styleHeaderCell(style, cell, font);
//                            cell.setLeftBorderStyle(null);
//                            if (j + 1 < adjacents) {
//                                cell.setRightBorderStyle(null);
//                            }
//                        }
//                    }
//                    i += adjacents - 1;
//                } else {
//                    Cell<PDPage> blankCell = groupHeaderRow.createCell(columnPercentWidths[i], "",
//                            HorizontalAlignment.get("center"), VerticalAlignment.get("top"));
//                    styleHeaderCell(style, blankCell, font);
//                }
//
//            }
//            table.addHeaderRow(groupHeaderRow);
//        }
//
//        Row<PDPage> headerRow = table.createRow(15f);
//
//        for (int i = 0; i < columnsOrdered.length(); i++) {
//
//            if (pdfHiddenColumns.contains(i))
//                continue;
//
//            JSONObject column = columnsOrdered.getJSONObject(i);
//            String columnName = column.getString("header");
////			if (arrayHeader.get(columnName) != null) {
////				columnName = arrayHeader.get(columnName);
////			}
//
//            JSONArray columnSelectedOfDataset = widgetContent.getJSONArray("columnSelectedOfDataset");
//            for (int j = 0; j < columnSelectedOfDataset.length(); j++) {
//                JSONObject columnSelected = columnSelectedOfDataset.getJSONObject(j);
//                if (columnName.equals(columnSelected.getString("aliasToShow"))) {
//                    columnName = getTableColumnHeaderValue(columnSelected);
//                    break;
//                }
//            }
//
//            Cell<PDPage> cell = headerRow.createCell(columnPercentWidths[i], columnName,
//                    HorizontalAlignment.get("center"), VerticalAlignment.get("top"));
//            styleHeaderCell(style, cell, font);
//        }

//        table.addHeaderRow(headerRow);
    }

    //TODO ALREADY PRESENT IN THE COCKPIT EXPORTER
    private static void createDocumentInformationRow(BaseTable table, PDFont font, String creationUser) {
        Row<PDPage> documentInformationRow = table.createRow(12f);
        String executionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        be.quodlibet.boxable.Cell<PDPage> executionDateCell = documentInformationRow.createCell(50, "Execution Date: " + executionDate,
                HorizontalAlignment.get("center"), VerticalAlignment.get("middle"));
        executionDateCell.setFont(font);
        executionDateCell.setFillColor(Color.LIGHT_GRAY);
        executionDateCell.setTextColor(Color.BLACK);
        executionDateCell.setFontSize(10);
        be.quodlibet.boxable.Cell<PDPage> creationUserCell = documentInformationRow.createCell(50, "Creation User: " + creationUser,
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

                        be.quodlibet.boxable.Cell<PDPage> selectionDatasetNameCell = selectionRow.createCell(
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
                                "COLUMN " + subKey + ": " + sb.toString(),
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
    // ... existing code ...
    private void buildRowsAndCols(BaseTable table, int offset, JSONObject settings, JSONArray rows, int isColumnGroupingPresent, JSONArray columnsOrdered, int numberOfSummaryRows, List<String> summaryRowsLabels, PDFont font, String[] columnDateFormats) {
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
            int columnsLength = widget.getJSONObject("columns").length();
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


}
