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
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.checkerframework.checker.nullness.qual.NonNull;
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

import static it.eng.spagobi.commons.utilities.UserUtilities.getUserProfile;

public class DashboardPdfExporter extends DashboardExporter {

    public static Logger logger = Logger.getLogger(DashboardPdfExporter.class);

    private static final float POINTS_PER_INCH = 72;
    private static final float POINTS_PER_MM = 1 / (10 * 2.54f) * POINTS_PER_INCH;
    private static final int DEFAULT_COLUMN_WIDTH = 150;
    private static final String KN_EXPORT_EXTRA_FIELD_LABEL = "kn-export-extra-field-label";
    private static final String KN_EXPORT_EXTRA_FIELD_VALUE = "kn-export-extra-field-value";
    protected static final String DATE_FORMAT = "dd/MM/yyyy";

    private float totalColumnsWidth = 0;
    private float[] columnPercentWidths;
    private Locale locale;
    public DashboardPdfExporter(String userUniqueIdentifier, Locale locale) {
        super(userUniqueIdentifier, null);
        this.locale = locale;
    }

    public byte[] getBinaryData(JSONObject template) throws JSONException {

        String creationUser;

        if (template == null) {
            throw new SpagoBIRuntimeException("Unable to get template for dashboard");
        }

        Map<String, Map<String, JSONArray>> selections = getSelections(template);

        JSONObject drivers = transformDriversForDatastore(getDrivers(template));
        JSONObject parameters = transformParametersForDatastore(template, getParametersFromBody(template));
        creationUser = template.getString("creationUser");

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

                PDPage newPage = createPage(widget);
                document.addPage(newPage);

                // compute how many selection lines we will draw so we can reserve space at the bottom
                int reservedSelectionLines = getReservedSelectionLines(selections);
                table = createBaseTable(document, newPage, reservedSelectionLines);

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
                    JSONArray variables = widget.optJSONArray("variables");
                    String extraValueLabel = null;
                    String extraValue = null;
                    if (variables != null && variables.length() > 0) {
                        for (int i = 0; i < variables.length(); i++) {
                            JSONObject variable = variables.getJSONObject(i);
                            if (variable.getString("name").equals(KN_EXPORT_EXTRA_FIELD_LABEL)) {
                                extraValueLabel = variable.getString("value");
                            } else if (variable.getString("name").equals(KN_EXPORT_EXTRA_FIELD_VALUE)) {
                                extraValue = variable.optString("value", "NA");
                            }
                        }
                    }
                    buildFirstPageHeaders(table, settings, groupsAndColumnsMap, columnsOrdered, creationUser, font, extraValueLabel, extraValue);
                }

                rows = dataStore.getJSONArray("rows");

                font = PDType0Font.load(table.document, pdfFontFile);

                buildRowsAndCols(table, settings, rows, columnsOrdered, numberOfSummaryRows, summaryRowsLabels, font, columnDateFormats, columnStylesMap);

                offset += fetchSize;

                table.draw();

                if (offset >= totalNumberOfRows) {
                    createSelectionsRows(table, font, selections);
                }

            } while (offset < totalNumberOfRows);
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Unable to export table widget: " + widgetId, e);
        }
    }

    private static int getReservedSelectionLines(Map<String, Map<String, JSONArray>> selections) {
        if (selections == null || selections.isEmpty()) {
            return 0;
        }

        // Count the actual number of selections
        int selectionCount = 0;
        for (Map.Entry<String, Map<String, JSONArray>> datasetEntry : selections.entrySet()) {
            Map<String, JSONArray> selectionContent = datasetEntry.getValue();
            if (selectionContent != null && !selectionContent.isEmpty()) {
                selectionCount += selectionContent.size();
            }
        }
        return selectionCount;
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

    private void buildFirstPageHeaders(BaseTable table, JSONObject settings, Map<String, String> groupsAndColumnsMap, JSONArray columnsOrdered, String creationUser, PDFont font, String extraValueLabel, String extraValueField) throws JSONException {
        createDocumentInformationRow(table, font, creationUser, extraValueLabel, extraValueField);

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

    private void createDocumentInformationRow(BaseTable table, PDFont font, String creationUser, String extraValueLabel, String extraValue) {
        try {

            String executionDateLabel;
            String creationUserLabel;

            if (this.locale.toString().equals("sk_SK")) {
                executionDateLabel = "Dátum vytvorenia: ";
                creationUserLabel = "Vytvoril: ";
            } else if (this.locale.toString().equals("it_IT")) {
                executionDateLabel = "Data di esecuzione: ";
                creationUserLabel = "Utente di creazione: ";
            } else {
                executionDateLabel = "Execution Date: ";
                creationUserLabel = "Creation User: ";
            }

            PDPage page = table.getCurrentPage();

            String executionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

            String line1 = executionDateLabel + executionDate;
            String line2 = creationUserLabel + creationUser;
            String line3 = null;
            boolean isExtraValuePresent = extraValueLabel != null && !extraValueLabel.isEmpty() && extraValue != null && !extraValue.isEmpty();
            if (isExtraValuePresent) {
                line3 = extraValueLabel + ": " + extraValue;
            }

            float fontSize = 10f;
            float leading = 12f;
            float margin = 20f;

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            float w1 = font.getStringWidth(line1) / 1000f * fontSize;
            float w2 = font.getStringWidth(line2) / 1000f * fontSize;

            float w3 = isExtraValuePresent ? font.getStringWidth(line3) / 1000f * fontSize : 0;

            float maxW = Math.max(w1, Math.max(w2, w3));

            float x = pageWidth - margin - maxW;

            float yStartNewPage = pageHeight - (2 * margin);

            float additionalGap = 12f;
            float y = yStartNewPage + additionalGap + leading;

            try (PDPageContentStream contentStream =
                       new PDPageContentStream(table.document, page, PDPageContentStream.AppendMode.APPEND, true)) {

                contentStream.beginText();
                contentStream.setFont(font, fontSize);
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(line1);
                contentStream.newLineAtOffset(0, -leading);
                contentStream.showText(line2);
                if (isExtraValuePresent) {
                    contentStream.newLineAtOffset(0, -leading);
                    contentStream.showText(line3);
                }
                contentStream.endText();
            }

        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Couldn't add document information to the page", e);
        }
    }

    private void createSelectionsRows(BaseTable table, PDFont font, Map<String, Map<String, JSONArray>> selections) {
        try {
            if (selections == null || selections.isEmpty()) {
                return;
            }

            float fontSize = 10f;
            float leading = 12f;
            float margin = 20f;
            float baseBottomMargin = 20f;

            PDPage page = table.getCurrentPage();
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float availableTextWidth = pageWidth - (2 * margin);

            // Build wrapped lines first so we can paginate correctly
            final class SelectionLine {
                final String key;
                final float keyWidth;
                final java.util.List<String> wrappedValueLines;

                SelectionLine(String key, float keyWidth, java.util.List<String> wrappedValueLines) {
                    this.key = key;
                    this.keyWidth = keyWidth;
                    this.wrappedValueLines = wrappedValueLines;
                }
            }

            java.util.List<SelectionLine> lines = new java.util.ArrayList<>();

            for (Map.Entry<String, Map<String, JSONArray>> datasetEntry : selections.entrySet()) {
                Map<String, JSONArray> selectionContent = datasetEntry.getValue();
                if (selectionContent == null || selectionContent.isEmpty()) {
                    continue;
                }

                for (Map.Entry<String, JSONArray> columnEntry : selectionContent.entrySet()) {
                    String key = columnEntry.getKey();
                    key = key.replace("('", "").replace("')", "");

                    String valueStr = getValues(columnEntry).toString().replace("('", "").replace("')", "");

                    float keyWidth = font.getStringWidth(key) / 1000f * fontSize;
                    float prefixWidth = keyWidth + (font.getStringWidth(": ") / 1000f * fontSize);
                    float valueStartX = margin + prefixWidth;
                    float valueAvailableWidth = Math.max(10f, (margin + availableTextWidth) - valueStartX);

                    java.util.List<String> wrappedValues = wrapText(font, fontSize, valueStr, valueAvailableWidth);
                    if (wrappedValues.isEmpty()) {
                        wrappedValues = java.util.Collections.singletonList("");
                    }

                    lines.add(new SelectionLine(key, keyWidth, wrappedValues));
                }
            }

            if (lines.isEmpty()) {
                return;
            }

            int totalLines = 0;
            for (SelectionLine sl : lines) {
                totalLines += Math.max(1, sl.wrappedValueLines.size());
            }

            // Calculate where the table actually ends
            // Table starts at y=550 (from createBaseTable)
            float tableStartY = 550f;
            float tableHeight = 0f;

            try {
                List<Row<PDPage>> rows = table.getRows();
                for (Row<PDPage> row : rows) {
                    tableHeight += row.getHeight();
                }
            } catch (Exception e) {
                logger.warn("Could not calculate exact table bottom");
                tableHeight = 0;
            }

            float tableEndY = tableStartY - tableHeight;

            float requiredHeight = totalLines * leading;
            float availableSpace = tableEndY - baseBottomMargin;
            boolean needsNewPage = availableSpace < requiredHeight;

            PDPage targetPage;
            float currentY;

            if (needsNewPage) {
                targetPage = new PDPage(page.getMediaBox());
                table.document.addPage(targetPage);
                currentY = pageHeight - margin - leading;
            } else {
                targetPage = page;
                currentY = tableEndY - leading;
            }

            try (PDPageContentStream contentStream =
                         new PDPageContentStream(table.document, targetPage, PDPageContentStream.AppendMode.APPEND, true)) {

                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.setFont(font, fontSize);

                for (SelectionLine sl : lines) {
                    float keyX = margin;
                    float keyWidth = sl.keyWidth;
                    float afterKeyX = keyX + keyWidth;
                    float colonWidth = font.getStringWidth(": ") / 1000f * fontSize;
                    float valuesX = afterKeyX + colonWidth;

                    java.util.List<String> wrapped = sl.wrappedValueLines;
                    int lineCount = Math.max(1, wrapped.size());

                    for (int i = 0; i < lineCount; i++) {
                        // page break
                        if (currentY <= baseBottomMargin + leading) {
                            targetPage = new PDPage(page.getMediaBox());
                            table.document.addPage(targetPage);
                            currentY = pageHeight - margin - leading;

                            try (PDPageContentStream csNew =
                                         new PDPageContentStream(table.document, targetPage, PDPageContentStream.AppendMode.APPEND, true)) {
                                csNew.setNonStrokingColor(Color.BLACK);
                                csNew.setFont(font, fontSize);

                                for (int j = i; j < lineCount; j++) {
                                    if (j == 0) {
                                        csNew.beginText();
                                        csNew.newLineAtOffset(keyX, currentY);
                                        csNew.showText(sl.key);
                                        csNew.endText();

                                        float underlineY = currentY - 2.0f;
                                        csNew.setLineWidth(0.5f);
                                        csNew.moveTo(keyX, underlineY);
                                        csNew.lineTo(keyX + keyWidth, underlineY);
                                        csNew.stroke();

                                        csNew.beginText();
                                        csNew.newLineAtOffset(afterKeyX, currentY);
                                        csNew.showText(": ");
                                        csNew.endText();

                                        csNew.beginText();
                                        csNew.newLineAtOffset(valuesX, currentY);
                                        csNew.showText(wrapped.get(j));
                                        csNew.endText();
                                    } else {
                                        csNew.beginText();
                                        csNew.newLineAtOffset(valuesX, currentY);
                                        csNew.showText(wrapped.get(j));
                                        csNew.endText();
                                    }

                                    currentY -= leading;
                                    if (currentY <= baseBottomMargin + leading && j + 1 < lineCount) {
                                        targetPage = new PDPage(page.getMediaBox());
                                        table.document.addPage(targetPage);
                                        currentY = pageHeight - margin - leading;
                                        i = j + 1;
                                        break;
                                    }
                                }
                            }

                            i = lineCount;
                            break;
                        }

                        if (i == 0) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(keyX, currentY);
                            contentStream.showText(sl.key);
                            contentStream.endText();

                            float underlineY = currentY - 2.0f;
                            contentStream.setLineWidth(0.5f);
                            contentStream.moveTo(keyX, underlineY);
                            contentStream.lineTo(keyX + keyWidth, underlineY);
                            contentStream.stroke();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(afterKeyX, currentY);
                            contentStream.showText(": ");
                            contentStream.endText();

                            contentStream.beginText();
                            contentStream.newLineAtOffset(valuesX, currentY);
                            contentStream.showText(wrapped.get(i));
                            contentStream.endText();
                        } else {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(valuesX, currentY);
                            contentStream.showText(wrapped.get(i));
                            contentStream.endText();
                        }

                        currentY -= leading;
                    }
                }
            }

        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Couldn't add selection information to the page", e);
        }
    }

    private static @NonNull StringBuilder getValues(Map.Entry<String, JSONArray> columnEntry) {
        JSONArray valuesArray = columnEntry.getValue();

        StringBuilder values = new StringBuilder();
        if (valuesArray != null) {
            for (int i = 0; i < valuesArray.length(); i++) {
                if (i > 0) values.append(", ");
                Object v = valuesArray.opt(i);
                values.append(v != null ? String.valueOf(v) : "null");
            }
        }
        return values;
    }

    /**
     * Wrap text so that each line fits within maxWidth using the provided font metrics.
     * Splits by whitespace; if a single token is wider than maxWidth it will be hard-split.
     */
    private static java.util.List<String> wrapText(PDFont font, float fontSize, String text, float maxWidth) throws IOException {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            if (line.length() == 0) {
                if (stringWidth(font, fontSize, word) <= maxWidth) {
                    line.append(word);
                } else {
                    for (String part : hardSplitToken(font, fontSize, word, maxWidth)) {
                        if (!part.isEmpty()) {
                            result.add(part);
                        }
                    }
                }
            } else {
                String candidate = line + " " + word;
                if (stringWidth(font, fontSize, candidate) <= maxWidth) {
                    line.append(' ').append(word);
                } else {
                    result.add(line.toString());
                    line.setLength(0);
                    if (stringWidth(font, fontSize, word) <= maxWidth) {
                        line.append(word);
                    } else {
                        for (String part : hardSplitToken(font, fontSize, word, maxWidth)) {
                            if (!part.isEmpty()) {
                                result.add(part);
                            }
                        }
                    }
                }
            }
        }

        if (line.length() > 0) {
            result.add(line.toString());
        }

        return result;
    }

    private static float stringWidth(PDFont font, float fontSize, String text) throws IOException {
        return (font.getStringWidth(text) / 1000f) * fontSize;
    }

    private static java.util.List<String> hardSplitToken(PDFont font, float fontSize, String token, float maxWidth) throws IOException {
        java.util.List<String> parts = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            current.append(c);
            if (stringWidth(font, fontSize, current.toString()) > maxWidth) {
                current.setLength(current.length() - 1);
                if (current.length() > 0) {
                    parts.add(current.toString());
                }
                current.setLength(0);
                current.append(c);
            }
        }
        if (current.length() > 0) {
            parts.add(current.toString());
        }
        return parts;
    }

    private void buildRowsAndCols(BaseTable table, JSONObject settings, JSONArray rows, JSONArray columnsOrdered, int numberOfSummaryRows, List<String> summaryRowsLabels, PDFont font, String[] columnDateFormats,  Map<String, JSONArray> styles) throws JSONException {

        // Check if summary row is enabled
        boolean summaryRowEnabled = settings.getJSONObject("configuration").has("summaryRows") &&
                settings.getJSONObject("configuration").getJSONObject("summaryRows").getBoolean("enabled");
        DateFormat inputDateFormat = new SimpleDateFormat(DATE_FORMAT, this.locale);

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

                JSONObject visualizationType = getVisualizationTypeByColumn(settings, column);
                valueStr = applyVisualizationTypeToValueStr(visualizationType, valueStr);

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

    private String applyVisualizationTypeToValueStr(JSONObject visualizationType, String valueStr) throws JSONException {

        if (visualizationType.has("precision")) {
            int precision = visualizationType.getInt("precision");
            try {
                double numericValue = Double.parseDouble(valueStr);
                String formatString = "%." + precision + "f";
                valueStr = String.format(formatString, numericValue);
            } catch (NumberFormatException e) {
                logger.warn("Cannot apply precision to non-numeric value: " + valueStr, e);
            }
        }

        String prefix = visualizationType.optString("prefix", "");
        String suffix = visualizationType.optString("suffix", "");
        return prefix + valueStr + suffix;
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

    /**
     * Create a BaseTable reserving space at the bottom for custom contents (like selections).
     * reservedSelectionLines is the number of text lines we'll draw below the table.
     */
    private BaseTable createBaseTable(PDDocument doc, PDPage page, int reservedSelectionLines) {
        try {
            float margin = 20;
            // starting y position is whole page height subtracted by top and bottom margin
            float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
            float baseBottomMargin = 20;
            float leading = 12f;
            float extraGapFromTable = 6f; // gap between table and selections

            float bottomMargin = baseBottomMargin + reservedSelectionLines * leading + extraGapFromTable;
            float tableWidth = page.getMediaBox().getWidth() - (2 * margin);
            // y position is your coordinate of top left corner of the table
            Assert.assertTrue(tableWidth > 0, "Page dimension is too small!");
            float yPosition = 550;
            return new BaseTable(yPosition, yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, true);
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Cannot create PDF Base Table object:", e);
        }
    }

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
