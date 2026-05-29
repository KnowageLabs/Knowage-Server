package it.eng.knowage.engine.api.export.dashboard.excel.exporters;

import it.eng.knowage.commons.multitenant.OrganizationImageManager;
import it.eng.knowage.engine.api.export.IWidgetExporter;
import it.eng.knowage.engine.api.export.dashboard.excel.DashboardExcelExporter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.log4j.Logger;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFPivotTable;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotAreaReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotField;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STFieldSortType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.apache.poi.ss.usermodel.DataConsolidateFunction.*;

public class DashboardPivotExporter extends GenericDashboardWidgetExporter implements IWidgetExporter {

    public static Logger logger = Logger.getLogger(DashboardPivotExporter.class);
    private static final String SOURCE_SHEET_NAME = "Source_sheet";
    private static final String PIVOT_SHEET_NAME = "Pivot_sheet";
    static final long DATA_FIELD_REFERENCE = 4294967294L; // OOXML uses unsigned -2 to point to the pivot data field.

    private static final class PivotSheetContext {
        private final SXSSFWorkbook streamingWorkbook;
        private final XSSFWorkbook xssfWorkbook;
        private final SXSSFSheet sourceStreamingSheet;
        private final XSSFSheet sourceXssfSheet;
        private final XSSFSheet pivotXssfSheet;
        private final String sourceSheetName;
        private final String pivotSheetName;

        private PivotSheetContext(SXSSFWorkbook streamingWorkbook,
                                  XSSFWorkbook xssfWorkbook,
                                  SXSSFSheet sourceStreamingSheet,
                                  XSSFSheet sourceXssfSheet,
                                  XSSFSheet pivotXssfSheet,
                                  String sourceSheetName,
                                  String pivotSheetName) {
            this.streamingWorkbook = streamingWorkbook;
            this.xssfWorkbook = xssfWorkbook;
            this.sourceStreamingSheet = sourceStreamingSheet;
            this.sourceXssfSheet = sourceXssfSheet;
            this.pivotXssfSheet = pivotXssfSheet;
            this.sourceSheetName = sourceSheetName;
            this.pivotSheetName = pivotSheetName;
        }
    }

    static final class PivotSortReference {
        private final long fieldIndex;
        private final long itemIndex;

        PivotSortReference(long fieldIndex, long itemIndex) {
            this.fieldIndex = fieldIndex;
            this.itemIndex = itemIndex;
        }

        long getFieldIndex() {
            return fieldIndex;
        }

        long getItemIndex() {
            return itemIndex;
        }
    }

    public DashboardPivotExporter(DashboardExcelExporter excelExporter, Workbook wb, JSONObject widget, String documentName, Map<String, Map<String, Object>> selections, JSONObject drivers, JSONObject parameters, String userUniqueIdentifier, String imageB64) {
        super(excelExporter, wb, widget, documentName, selections, drivers, parameters, userUniqueIdentifier, imageB64);
    }

    @Override
    public int export() {
        String widgetId = widget.optString("id");
        try {
            if (!(wb instanceof SXSSFWorkbook)) {
                throw new SpagoBIRuntimeException("Unable to export pivot widget: shared workbook is not SXSSFWorkbook");
            }
            return populatePivotWorkbook((SXSSFWorkbook) wb, true);
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Unable to export pivot widget: " + widgetId, e);
        }
    }

    public Workbook exportPivot() {
        String widgetId = widget.optString("id");
        SXSSFWorkbook streamingWorkbook = new SXSSFWorkbook(new XSSFWorkbook());
        try {
            int exportedSheets = populatePivotWorkbook(streamingWorkbook, false);
            if (exportedSheets == 0) {
                streamingWorkbook.close();
                return null;
            }
            return streamingWorkbook;
        } catch (Exception e) {
            try {
                streamingWorkbook.close();
            } catch (Exception closeException) {
                logger.warn("Unable to close temporary pivot workbook", closeException);
            }
            throw new SpagoBIRuntimeException("Unable to export pivot widget: " + widgetId, e);
        }
    }

    private int populatePivotWorkbook(SXSSFWorkbook streamingWorkbook, boolean registerAutoSizeSkip) throws JSONException {
        JSONObject settings = getJsonObject();
        JSONObject fields = widget.getJSONObject("fields");
        JSONArray columns = fields.getJSONArray("columns");
        JSONArray rows = fields.getJSONArray("rows");
        int trackedPathFieldCount = columns.length() + rows.length();
        String widgetName = getJsonObjectUtils().replacePlaceholderIfPresent(getJsonObjectUtils().getDashboardWidgetName(widget), drivers);

        int offset = 0;
        int fetchSize = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
        JSONObject dataStore = getDataStoreForDashboardWidget(widget, offset, fetchSize, selections, drivers, parameters);
        if (dataStore == null) {
            return 0;
        }

        Map<Integer, Map<String, Integer>> fieldItemIndexes = new HashMap<>();
        updateFieldItemIndexes(dataStore, trackedPathFieldCount, fieldItemIndexes);

        int totalNumberOfRows = dataStore.getInt("results");
        if (totalNumberOfRows == 0) {
            return 0;
        }

        PivotSheetContext pivotSheetContext = createPivotSheetContext(streamingWorkbook);
        if (registerAutoSizeSkip) {
            excelExporter.registerSheetToSkipAutoSize(pivotSheetContext.sourceSheetName);
            excelExporter.registerSheetToSkipAutoSize(pivotSheetContext.pivotSheetName);
        }

        excelExporter.fillTableSheetWithData(dataStore, pivotSheetContext.xssfWorkbook, pivotSheetContext.sourceXssfSheet, widgetName, offset, settings);
        String imageB64 = createPivotHeaderSheet(pivotSheetContext.pivotXssfSheet, widgetName);

        int sourceSheetLastRowNum = pivotSheetContext.sourceXssfSheet.getLastRowNum();
        int currentPageRows = dataStore.getJSONArray("rows").length();
        int finalSourceLastRowNum = sourceSheetLastRowNum + (totalNumberOfRows - currentPageRows);
        int sourceSheetLastColumn = pivotSheetContext.sourceXssfSheet.getRow(sourceSheetLastRowNum).getLastCellNum();
        String lastColLetter = CellReference.convertNumToColString(sourceSheetLastColumn - 1);

        boolean isImagePresent = imageB64 != null;
        XSSFPivotTable pivotTable = pivotSheetContext.pivotXssfSheet.createPivotTable(
                new AreaReference(isImagePresent ? new CellReference(pivotSheetContext.sourceSheetName + "!A4") : new CellReference(pivotSheetContext.sourceSheetName + "!A2"),
                        new CellReference(pivotSheetContext.sourceSheetName + "!" + lastColLetter + (finalSourceLastRowNum + 1)),
                        SpreadsheetVersion.EXCEL2007),
                new CellReference("A8"));

        while (offset + fetchSize < totalNumberOfRows) {
            offset += fetchSize;
            dataStore = getDataStoreForDashboardWidget(widget, offset, fetchSize, selections, drivers, parameters);
            updateFieldItemIndexes(dataStore, trackedPathFieldCount, fieldItemIndexes);
            excelExporter.fillTableSheetWithData(dataStore, pivotSheetContext.streamingWorkbook, pivotSheetContext.sourceStreamingSheet, widgetName, offset, settings);
        }

        formatPivot(pivotTable, fieldItemIndexes);
        return 2;
    }

    private PivotSheetContext createPivotSheetContext(SXSSFWorkbook streamingWorkbook) {
        XSSFWorkbook xssfWorkbook = streamingWorkbook.getXSSFWorkbook();
        Sheet sourceSheet = excelExporter.createUniqueSafeSheet(streamingWorkbook, SOURCE_SHEET_NAME, null);
        Sheet pivotSheet = excelExporter.createUniqueSafeSheet(streamingWorkbook, PIVOT_SHEET_NAME, null);

        if (!(sourceSheet instanceof SXSSFSheet)) {
            throw new SpagoBIRuntimeException("Unable to export pivot widget: source sheet is not SXSSFSheet");
        }

        XSSFSheet sourceXssfSheet = xssfWorkbook.getSheet(sourceSheet.getSheetName());
        XSSFSheet pivotXssfSheet = xssfWorkbook.getSheet(pivotSheet.getSheetName());
        if (sourceXssfSheet == null || pivotXssfSheet == null) {
            throw new SpagoBIRuntimeException("Unable to export pivot widget: backing XSSF sheets are not available");
        }

        return new PivotSheetContext(
                streamingWorkbook,
                xssfWorkbook,
                (SXSSFSheet) sourceSheet,
                sourceXssfSheet,
                pivotXssfSheet,
                sourceSheet.getSheetName(),
                pivotSheet.getSheetName()
        );
    }

    private String createPivotHeaderSheet(XSSFSheet pivotSheet, String widgetName) {
        String imageB64 = OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName());
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
                imageB64,
                startRow,
                rowHeight,
                rowspan,
                startCol,
                colWidth,
                colspan,
                namespan,
                dataspan,
                "Dashboard",
                widgetName);
        return imageB64;
    }

    private JSONObject getJsonObject() throws JSONException {
        JSONObject settings = widget.getJSONObject("settings");
        return settings;
    }

    private void formatPivot(XSSFPivotTable pivotTable, Map<Integer, Map<String, Integer>> fieldItemIndexes) {
        try {
            JSONObject fields = widget.getJSONObject("fields");
            JSONArray columns = fields.getJSONArray("columns");
            JSONArray rows = fields.getJSONArray("rows");
            JSONArray filters = fields.getJSONArray("filters");
            JSONArray data = fields.getJSONArray("data");
            Map<String, Integer> dataFieldIndexes = buildDataFieldIndexes(data);
            Map<String, Integer> sourceFieldIndexes = buildSourceFieldIndexes(columns, rows, filters, data);

            int counter = 0;

            for (int i = 0; i < columns.length(); ++i) {
                pivotTable.addColLabel(counter);
                counter++;
            }

            for (int i = 0; i < rows.length(); ++i) {
                pivotTable.addRowLabel(counter);
                counter++;
            }

            for (int i = 0; i < filters.length(); ++i) {
                pivotTable.addReportFilter(counter);
                counter++;
            }

            try {
                for (int i = 0; i < data.length(); ++i) {
                    JSONObject dataObj = data.getJSONObject(i);
                    String aggregation = dataObj.optString("aggregation", "SUM");
                    DataConsolidateFunction func = getAggregationFunction(aggregation);
                    if (func == null) {
                        func = SUM;
                    }
                    String valueFieldName = dataObj.optString("alias");
                    if (valueFieldName.isEmpty()) {
                        valueFieldName = dataObj.optString("columnName", func.getName());
                    }
                    pivotTable.addColumnLabel(func, counter, valueFieldName);
                    counter++;
                }
            } catch (JSONException e) {
                logger.error("Error while adding data columns to pivot table", e);
            }

            applyPivotSorting(pivotTable, columns, rows, 0, dataFieldIndexes, sourceFieldIndexes, fieldItemIndexes);
            applyPivotSorting(pivotTable, rows, columns, columns.length(), dataFieldIndexes, sourceFieldIndexes, fieldItemIndexes);

        } catch (JSONException e) {
            logger.error("Error while creating pivot table", e);
        }
    }

    private void updateFieldItemIndexes(JSONObject dataStore, int trackedFieldCount, Map<Integer, Map<String, Integer>> fieldItemIndexes)
            throws JSONException {
        JSONArray metadataFields = dataStore.getJSONObject("metaData").getJSONArray("fields");
        JSONArray dataRows = dataStore.getJSONArray("rows");
        List<String> trackedRowKeys = extractTrackedRowKeys(metadataFields, trackedFieldCount);
        int effectiveFieldCount = trackedRowKeys.size();

        for (int fieldIndex = 0; fieldIndex < effectiveFieldCount; fieldIndex++) {
            String rowKey = trackedRowKeys.get(fieldIndex);
            if (rowKey.isEmpty()) {
                continue;
            }

            Map<String, Integer> itemIndexes = fieldItemIndexes.computeIfAbsent(fieldIndex, key -> new LinkedHashMap<>());
            for (int rowIndex = 0; rowIndex < dataRows.length(); rowIndex++) {
                JSONObject row = dataRows.getJSONObject(rowIndex);
                String itemValue = normalizePivotItemValue(row.opt(rowKey));
                if (!itemIndexes.containsKey(itemValue)) {
                    itemIndexes.put(itemValue, itemIndexes.size());
                }
            }
        }
    }

    static List<String> extractTrackedRowKeys(JSONArray metadataFields, int trackedFieldCount) {
        List<String> trackedRowKeys = new ArrayList<>();
        if (metadataFields == null || trackedFieldCount <= 0) {
            return trackedRowKeys;
        }

        for (int i = 0; i < metadataFields.length() && trackedRowKeys.size() < trackedFieldCount; i++) {
            JSONObject metadataField = metadataFields.optJSONObject(i);
            if (metadataField == null) {
                continue;
            }

            String rowKey = metadataField.optString("name");
            if (rowKey.isEmpty()) {
                rowKey = metadataField.optString("dataIndex");
            }
            if (rowKey.isEmpty()) {
                rowKey = metadataField.optString("header");
            }

            if (!rowKey.isEmpty()) {
                trackedRowKeys.add(rowKey);
            }
        }

        return trackedRowKeys;
    }

    private Map<String, Integer> buildSourceFieldIndexes(JSONArray columns, JSONArray rows, JSONArray filters, JSONArray data) throws JSONException {
        Map<String, Integer> sourceFieldIndexes = new HashMap<>();
        int fieldIndex = 0;
        fieldIndex = indexFieldIdentifiers(columns, fieldIndex, sourceFieldIndexes);
        fieldIndex = indexFieldIdentifiers(rows, fieldIndex, sourceFieldIndexes);
        fieldIndex = indexFieldIdentifiers(filters, fieldIndex, sourceFieldIndexes);
        indexFieldIdentifiers(data, fieldIndex, sourceFieldIndexes);
        return sourceFieldIndexes;
    }

    private Map<String, Integer> buildDataFieldIndexes(JSONArray data) throws JSONException {
        Map<String, Integer> dataFieldIndexes = new HashMap<>();
        for (int i = 0; i < data.length(); i++) {
            indexFieldIdentifiers(data.getJSONObject(i), i, dataFieldIndexes);
        }
        return dataFieldIndexes;
    }

    private int indexFieldIdentifiers(JSONArray fields, int startIndex, Map<String, Integer> fieldIndexes) throws JSONException {
        int currentIndex = startIndex;
        for (int i = 0; i < fields.length(); i++) {
            indexFieldIdentifiers(fields.getJSONObject(i), currentIndex, fieldIndexes);
            currentIndex++;
        }
        return currentIndex;
    }

    private void indexFieldIdentifiers(JSONObject field, int fieldIndex, Map<String, Integer> fieldIndexes) {
        putFieldIndex(fieldIndexes, field.optString("id"), fieldIndex);
        putFieldIndex(fieldIndexes, field.optString("columnName"), fieldIndex);
        putFieldIndex(fieldIndexes, field.optString("alias"), fieldIndex);
    }

    private void putFieldIndex(Map<String, Integer> fieldIndexes, String fieldIdentifier, int fieldIndex) {
        String normalizedFieldIdentifier = normalizeFieldIdentifier(fieldIdentifier);
        if (!normalizedFieldIdentifier.isEmpty()) {
            fieldIndexes.putIfAbsent(normalizedFieldIdentifier, fieldIndex);
        }
    }

    private void applyPivotSorting(XSSFPivotTable pivotTable, JSONArray pivotFields, JSONArray summaryPathFields, int startIndex, Map<String, Integer> dataFieldIndexes,
                                   Map<String, Integer> sourceFieldIndexes, Map<Integer, Map<String, Integer>> fieldItemIndexes)
            throws JSONException {
        for (int i = 0; i < pivotFields.length(); i++) {
            JSONObject pivotFieldDefinition = pivotFields.getJSONObject(i);
            int pivotFieldIndex = startIndex + i;
            CTPivotField pivotField = pivotTable.getCTPivotTableDefinition().getPivotFields().getPivotFieldArray(pivotFieldIndex);

            String orderColumn = pivotFieldDefinition.optString("orderColumn");
            Integer dataFieldIndex = resolveFieldIndex(orderColumn, dataFieldIndexes);
            boolean isSelfOrderingField = isSelfOrderingField(pivotFieldDefinition, orderColumn);
            String effectiveSort = getEffectiveSort(pivotFieldDefinition, orderColumn, dataFieldIndex, isSelfOrderingField);
            if (effectiveSort.isEmpty()) {
                continue;
            }

            if (dataFieldIndex != null) {
                List<PivotSortReference> summaryPathReferences = buildSummaryPathReferences(pivotFieldDefinition, summaryPathFields, sourceFieldIndexes, fieldItemIndexes);
                if (hasConfiguredSummaryPath(pivotFieldDefinition) && summaryPathReferences.isEmpty()) {
                    logger.debug("Skipping pivot autosort for field [" + getFieldDisplayName(pivotFieldDefinition) + "] because the configured summary path could not be resolved");
                    continue;
                }
                pivotField.setSortType(getSortType(effectiveSort));
                applyMeasureAutoSort(pivotField, dataFieldIndex, summaryPathReferences);
                continue;
            }

            pivotField.setSortType(getSortType(effectiveSort));
            if (!orderColumn.isEmpty() && !isSelfOrderingField) {
                logger.debug("Skipping unsupported pivot autosort for field [" + getFieldDisplayName(pivotFieldDefinition) + "] using order column [" + orderColumn + "]");
            }
        }
    }

    static void applyMeasureAutoSort(CTPivotField pivotField, int dataFieldIndex, List<PivotSortReference> summaryPathReferences) {
        CTPivotAreaReference dataReference = pivotField.addNewAutoSortScope().addNewPivotArea().addNewReferences().addNewReference();
        dataReference.setField(DATA_FIELD_REFERENCE);
        dataReference.setCount(1);
        dataReference.addNewX().setV(dataFieldIndex);

        for (PivotSortReference summaryPathReference : summaryPathReferences) {
            CTPivotAreaReference reference = pivotField.getAutoSortScope().getPivotArea().getReferences().addNewReference();
            reference.setField(summaryPathReference.getFieldIndex());
            reference.setCount(1);
            reference.addNewX().setV(summaryPathReference.getItemIndex());
        }
    }

    static List<PivotSortReference> buildSummaryPathReferences(JSONObject pivotFieldDefinition, JSONArray summaryPathFields,
                                                               Map<String, Integer> sourceFieldIndexes, Map<Integer, Map<String, Integer>> fieldItemIndexes)
            throws JSONException {
        List<PivotSortReference> references = new ArrayList<>();
        JSONArray orderBySummaryPath = pivotFieldDefinition.optJSONArray("orderBySummaryPath");
        if (orderBySummaryPath == null || orderBySummaryPath.length() == 0) {
            return references;
        }

        if (summaryPathFields == null || orderBySummaryPath.length() > summaryPathFields.length()) {
            logger.debug("Skipping pivot autosort path for field [" + getFieldDisplayName(pivotFieldDefinition) + "] because the configured summary path does not match the pivot axis");
            return references;
        }

        for (int i = 0; i < orderBySummaryPath.length(); i++) {
            JSONObject summaryPathField = summaryPathFields.optJSONObject(i);
            Integer summaryPathFieldIndex = resolveFieldIndex(summaryPathField, sourceFieldIndexes);
            if (summaryPathFieldIndex == null) {
                logger.debug("Skipping pivot autosort path for field [" + getFieldDisplayName(pivotFieldDefinition) + "] because field [" + summaryPathField + "] is not available in the source");
                return new ArrayList<>();
            }

            String pathValue = normalizePivotItemValue(orderBySummaryPath.opt(i));
            Integer itemIndex = resolveItemIndex(summaryPathField, fieldItemIndexes.get(summaryPathFieldIndex), pathValue);
            if (itemIndex == null) {
                logger.debug("Skipping pivot autosort path for field [" + getFieldDisplayName(pivotFieldDefinition) + "] because value [" + pathValue + "] is not available in the source");
                return new ArrayList<>();
            }

            references.add(new PivotSortReference(summaryPathFieldIndex, itemIndex));
        }

        return references;
    }

    private static boolean hasConfiguredSummaryPath(JSONObject pivotFieldDefinition) {
        JSONArray orderBySummaryPath = pivotFieldDefinition.optJSONArray("orderBySummaryPath");
        return orderBySummaryPath != null && orderBySummaryPath.length() > 0;
    }

    private static Integer resolveFieldIndex(String fieldIdentifier, Map<String, Integer> fieldIndexes) {
        String normalizedFieldIdentifier = normalizeFieldIdentifier(fieldIdentifier);
        if (normalizedFieldIdentifier.isEmpty()) {
            return null;
        }
        return fieldIndexes.get(normalizedFieldIdentifier);
    }

    private static Integer resolveFieldIndex(JSONObject fieldDefinition, Map<String, Integer> fieldIndexes) {
        if (fieldDefinition == null) {
            return null;
        }

        Integer fieldIndex = resolveFieldIndex(fieldDefinition.optString("id"), fieldIndexes);
        if (fieldIndex == null) {
            fieldIndex = resolveFieldIndex(fieldDefinition.optString("columnName"), fieldIndexes);
        }
        if (fieldIndex == null) {
            fieldIndex = resolveFieldIndex(fieldDefinition.optString("alias"), fieldIndexes);
        }
        return fieldIndex;
    }

    private static Integer resolveItemIndex(JSONObject fieldDefinition, Map<String, Integer> itemIndexes, String pathValue) {
        if (itemIndexes == null) {
            return null;
        }

        List<String> orderedValues = new ArrayList<>(itemIndexes.keySet());
        String itemSort = getFieldItemSort(fieldDefinition);
        if (!itemSort.isEmpty()) {
            Comparator<String> itemComparator = getFieldItemComparator();
            if ("DESC".equalsIgnoreCase(itemSort)) {
                itemComparator = itemComparator.reversed();
            }
            orderedValues.sort(itemComparator);
        }

        for (int i = 0; i < orderedValues.size(); i++) {
            if (orderedValues.get(i).equalsIgnoreCase(pathValue)) {
                return i;
            }
        }
        return null;
    }

    private static String getFieldItemSort(JSONObject fieldDefinition) {
        if (fieldDefinition == null) {
            return "";
        }

        String orderColumn = fieldDefinition.optString("orderColumn");
        if (!matchesFieldIdentifier(fieldDefinition, orderColumn)) {
            return "";
        }

        String explicitSort = fieldDefinition.optString("sort");
        return explicitSort.isEmpty() ? "ASC" : explicitSort;
    }

    private static Comparator<String> getFieldItemComparator() {
        return (left, right) -> {
            int caseInsensitiveCompare = left.compareToIgnoreCase(right);
            if (caseInsensitiveCompare != 0) {
                return caseInsensitiveCompare;
            }
            return left.compareTo(right);
        };
    }

    private String getEffectiveSort(JSONObject pivotFieldDefinition, String orderColumn, Integer dataFieldIndex, boolean isSelfOrderingField) {
        String explicitSort = pivotFieldDefinition.optString("sort");
        if (!explicitSort.isEmpty()) {
            return explicitSort;
        }

        return !orderColumn.isEmpty() && (dataFieldIndex != null || isSelfOrderingField) ? "ASC" : "";
    }

    private boolean isSelfOrderingField(JSONObject pivotFieldDefinition, String orderColumn) {
        return !orderColumn.isEmpty() && matchesFieldIdentifier(pivotFieldDefinition, orderColumn);
    }

    private static boolean matchesFieldIdentifier(JSONObject pivotFieldDefinition, String identifier) {
        String normalizedIdentifier = normalizeFieldIdentifier(identifier);
        return normalizedIdentifier.equals(normalizeFieldIdentifier(pivotFieldDefinition.optString("id")))
                || normalizedIdentifier.equals(normalizeFieldIdentifier(pivotFieldDefinition.optString("columnName")))
                || normalizedIdentifier.equals(normalizeFieldIdentifier(pivotFieldDefinition.optString("alias")));
    }

    private static String normalizeFieldIdentifier(String fieldIdentifier) {
        if (fieldIdentifier == null) {
            return "";
        }
        return fieldIdentifier.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePivotItemValue(Object fieldValue) {
        if (fieldValue == null || JSONObject.NULL.equals(fieldValue)) {
            return "";
        }
        return String.valueOf(fieldValue);
    }

    private static String getFieldDisplayName(JSONObject field) {
        String alias = field.optString("alias");
        return alias.isEmpty() ? field.optString("columnName") : alias;
    }

    private static STFieldSortType.Enum getSortType(String sort) {
        return "DESC".equalsIgnoreCase(sort) ? STFieldSortType.DESCENDING : STFieldSortType.ASCENDING;
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


    }
