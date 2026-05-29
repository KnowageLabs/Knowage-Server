package it.eng.knowage.engine.api.export.dashboard.excel.exporters;

import org.apache.poi.ss.usermodel.DataConsolidateFunction;
import org.apache.poi.ss.usermodel.Row;
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
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotAreaReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTPivotField;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DashboardPivotExporterTest {

    @Test
    public void shouldExtractTrackedRowKeysFromMixedMetadataArray() throws JSONException {
        JSONArray metadataFields = new JSONArray()
                .put("recNo")
                .put(new JSONObject()
                        .put("name", "column-1")
                        .put("header", "CLIENTE"))
                .put(new JSONObject()
                        .put("dataIndex", "column-2")
                        .put("header", "COD_CLIENTE"))
                .put("recCk");

        List<String> trackedRowKeys = DashboardPivotExporter.extractTrackedRowKeys(metadataFields, 2);

        assertEquals(2, trackedRowKeys.size());
        assertEquals("column-1", trackedRowKeys.get(0));
        assertEquals("column-2", trackedRowKeys.get(1));
    }

    @Test
    public void shouldBuildSummaryPathReferencesFromConfiguredPath() throws JSONException {
        JSONObject pivotFieldDefinition = new JSONObject()
                .put("alias", "COD_CLIENTE")
                .put("orderBySummaryPath", new JSONArray().put("CLIENTE IKO"));

        JSONArray summaryPathFields = new JSONArray()
                .put(new JSONObject()
                        .put("id", "pivot-column")
                        .put("columnName", "CLIENTE")
                        .put("alias", "Cliente")
                        .put("orderColumn", "CLIENTE"));

        Map<String, Integer> sourceFieldIndexes = new HashMap<>();
        sourceFieldIndexes.put("pivot-column", 0);
        sourceFieldIndexes.put("cliente", 0);

        Map<Integer, Map<String, Integer>> fieldItemIndexes = new HashMap<>();
        Map<String, Integer> itemIndexes = new LinkedHashMap<>();
        itemIndexes.put("CLIENTE IKO", 0);
        fieldItemIndexes.put(0, itemIndexes);

        List<DashboardPivotExporter.PivotSortReference> references =
                DashboardPivotExporter.buildSummaryPathReferences(pivotFieldDefinition, summaryPathFields, sourceFieldIndexes, fieldItemIndexes);

        assertEquals(1, references.size());
        assertEquals(0L, references.get(0).getFieldIndex());
        assertEquals(0L, references.get(0).getItemIndex());
    }

    @Test
    public void shouldBuildSummaryPathReferencesFromMultiLevelConfiguredPath() throws JSONException {
        JSONObject pivotFieldDefinition = new JSONObject()
                .put("alias", "COD_CLIENTE")
                .put("orderBySummaryPath", new JSONArray()
                        .put("ITALIA")
                        .put("CLIENTE IKO"));

        JSONArray summaryPathFields = new JSONArray()
                .put(new JSONObject()
                        .put("id", "country-column")
                        .put("columnName", "COUNTRY")
                        .put("alias", "Country")
                        .put("orderColumn", "COUNTRY"))
                .put(new JSONObject()
                        .put("id", "customer-column")
                        .put("columnName", "CLIENTE")
                        .put("alias", "Cliente")
                        .put("orderColumn", "CLIENTE"));

        Map<String, Integer> sourceFieldIndexes = new HashMap<>();
        sourceFieldIndexes.put("country-column", 0);
        sourceFieldIndexes.put("customer-column", 1);

        Map<Integer, Map<String, Integer>> fieldItemIndexes = new HashMap<>();
        Map<String, Integer> countryIndexes = new LinkedHashMap<>();
        countryIndexes.put("FRANCE", 0);
        countryIndexes.put("ITALIA", 1);
        fieldItemIndexes.put(0, countryIndexes);

        Map<String, Integer> customerIndexes = new LinkedHashMap<>();
        customerIndexes.put("CLIENTE ABC", 0);
        customerIndexes.put("CLIENTE IKO", 1);
        fieldItemIndexes.put(1, customerIndexes);

        List<DashboardPivotExporter.PivotSortReference> references =
                DashboardPivotExporter.buildSummaryPathReferences(pivotFieldDefinition, summaryPathFields, sourceFieldIndexes, fieldItemIndexes);

        assertEquals(2, references.size());
        assertEquals(0L, references.get(0).getFieldIndex());
        assertEquals(1L, references.get(0).getItemIndex());
        assertEquals(1L, references.get(1).getFieldIndex());
        assertEquals(1L, references.get(1).getItemIndex());
    }

    @Test
    public void shouldResolveSummaryPathUsingSortedColumnItemOrder() throws JSONException {
        JSONObject pivotFieldDefinition = new JSONObject()
                .put("alias", "COD_CLIENTE")
                .put("orderBySummaryPath", new JSONArray().put("CLIENTE ABC"));

        JSONArray summaryPathFields = new JSONArray()
                .put(new JSONObject()
                        .put("id", "description-column")
                        .put("columnName", "DESC_MISURE")
                        .put("alias", "Descrizione")
                        .put("orderColumn", "DESC_MISURE"));

        Map<String, Integer> sourceFieldIndexes = new HashMap<>();
        sourceFieldIndexes.put("description-column", 0);

        Map<Integer, Map<String, Integer>> fieldItemIndexes = new HashMap<>();
        Map<String, Integer> itemIndexes = new LinkedHashMap<>();
        itemIndexes.put("CLIENTE IKO", 0);
        itemIndexes.put("CLIENTE ABC", 1);
        itemIndexes.put("CLIENTE KSA", 2);
        fieldItemIndexes.put(0, itemIndexes);

        List<DashboardPivotExporter.PivotSortReference> references =
                DashboardPivotExporter.buildSummaryPathReferences(pivotFieldDefinition, summaryPathFields, sourceFieldIndexes, fieldItemIndexes);

        assertEquals(1, references.size());
        assertEquals(0L, references.get(0).getFieldIndex());
        assertEquals(0L, references.get(0).getItemIndex());
    }

    @Test
    public void shouldApplyMeasureAutoSortUsingDataFieldReferenceAndSummaryPath() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Month");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Amount");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Jan");
            row1.createCell(1).setCellValue("John");
            row1.createCell(2).setCellValue(5);

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("Feb");
            row2.createCell(1).setCellValue("John");
            row2.createCell(2).setCellValue(15);

            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("Jan");
            row3.createCell(1).setCellValue("Mary");
            row3.createCell(2).setCellValue(10);

            AreaReference source = workbook.getCreationHelper().createAreaReference("A1:C4");
            XSSFPivotTable pivotTable = sheet.createPivotTable(source, new CellReference("H1"));

            int monthCol = 0;
            int nameCol = 1;
            int amountCol = 2;

            pivotTable.addColLabel(monthCol);
            pivotTable.addRowLabel(nameCol);
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, amountCol, "Amount");

            CTPivotField pivotField = pivotTable.getCTPivotTableDefinition().getPivotFields().getPivotFieldArray(nameCol);
            DashboardPivotExporter.applyMeasureAutoSort(
                    pivotField,
                    0,
                    Collections.singletonList(new DashboardPivotExporter.PivotSortReference(monthCol, 1))
            );

            CTPivotAreaReference[] references = pivotField.getAutoSortScope().getPivotArea().getReferences().getReferenceArray();
            assertEquals(2, references.length);
            assertEquals(DashboardPivotExporter.DATA_FIELD_REFERENCE, references[0].getField());
            assertEquals(1, references[0].sizeOfXArray());
            assertEquals(0L, references[0].getXArray(0).getV());
            assertEquals(monthCol, references[1].getField());
            assertEquals(1L, references[1].getXArray(0).getV());
        } catch (Exception e) {
            throw new AssertionError("Unexpected exception while creating the pivot sort scope", e);
        }
    }

    @Test
    public void shouldApplyMeasureAutoSortForColumnFieldUsingRowSummaryPath() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet();

            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Description");
            headerRow.createCell(1).setCellValue("Customer");
            headerRow.createCell(2).setCellValue("Amount");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("Measure 1");
            row1.createCell(1).setCellValue("COD_1");
            row1.createCell(2).setCellValue(5);

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("Measure 2");
            row2.createCell(1).setCellValue("COD_1");
            row2.createCell(2).setCellValue(15);

            Row row3 = sheet.createRow(3);
            row3.createCell(0).setCellValue("Measure 1");
            row3.createCell(1).setCellValue("COD_2");
            row3.createCell(2).setCellValue(10);

            AreaReference source = workbook.getCreationHelper().createAreaReference("A1:C4");
            XSSFPivotTable pivotTable = sheet.createPivotTable(source, new CellReference("H1"));

            int descriptionCol = 0;
            int customerCol = 1;
            int amountCol = 2;

            pivotTable.addColLabel(descriptionCol);
            pivotTable.addRowLabel(customerCol);
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, amountCol, "Amount");

            CTPivotField pivotField = pivotTable.getCTPivotTableDefinition().getPivotFields().getPivotFieldArray(descriptionCol);
            DashboardPivotExporter.applyMeasureAutoSort(
                    pivotField,
                    0,
                    Collections.singletonList(new DashboardPivotExporter.PivotSortReference(customerCol, 1))
            );

            CTPivotAreaReference[] references = pivotField.getAutoSortScope().getPivotArea().getReferences().getReferenceArray();
            assertEquals(2, references.length);
            assertEquals(DashboardPivotExporter.DATA_FIELD_REFERENCE, references[0].getField());
            assertEquals(1, references[0].sizeOfXArray());
            assertEquals(0L, references[0].getXArray(0).getV());
            assertEquals(customerCol, references[1].getField());
            assertEquals(1L, references[1].getXArray(0).getV());
        } catch (Exception e) {
            throw new AssertionError("Unexpected exception while creating the pivot sort scope for columns", e);
        }
    }

    @Test
    public void shouldPersistPivotAndSourceSheetsWhenStreamingAdditionalSourceRows() {
        try (SXSSFWorkbook streamingWorkbook = new SXSSFWorkbook(new XSSFWorkbook());
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFWorkbook xssfWorkbook = streamingWorkbook.getXSSFWorkbook();

            SXSSFSheet sourceStreamingSheet = streamingWorkbook.createSheet("Source_sheet");
            XSSFSheet sourceSheet = xssfWorkbook.getSheet("Source_sheet");
            streamingWorkbook.createSheet("Pivot_sheet");
            XSSFSheet pivotSheet = xssfWorkbook.getSheet("Pivot_sheet");

            Row headerRow = sourceSheet.createRow(0);
            headerRow.createCell(0).setCellValue("Month");
            headerRow.createCell(1).setCellValue("Customer");
            headerRow.createCell(2).setCellValue("Amount");

            Row firstDataRow = sourceSheet.createRow(1);
            firstDataRow.createCell(0).setCellValue("Jan");
            firstDataRow.createCell(1).setCellValue("John");
            firstDataRow.createCell(2).setCellValue(5);

            XSSFPivotTable pivotTable = pivotSheet.createPivotTable(
                    new AreaReference(new CellReference("Source_sheet!A1"),
                            new CellReference("Source_sheet!C3"),
                            org.apache.poi.ss.SpreadsheetVersion.EXCEL2007),
                    new CellReference("A1"));
            pivotTable.addRowLabel(0);
            pivotTable.addColumnLabel(DataConsolidateFunction.SUM, 2, "Amount");

            Row streamedDataRow = sourceStreamingSheet.createRow(2);
            streamedDataRow.createCell(0).setCellValue("Feb");
            streamedDataRow.createCell(1).setCellValue("John");
            streamedDataRow.createCell(2).setCellValue(15);

            streamingWorkbook.write(outputStream);

            try (XSSFWorkbook reopenedWorkbook = new XSSFWorkbook(new ByteArrayInputStream(outputStream.toByteArray()))) {
                XSSFSheet reopenedSourceSheet = reopenedWorkbook.getSheet("Source_sheet");
                XSSFSheet reopenedPivotSheet = reopenedWorkbook.getSheet("Pivot_sheet");

                assertEquals(2, reopenedWorkbook.getNumberOfSheets());
                assertEquals(2, reopenedSourceSheet.getLastRowNum());
                assertEquals("Feb", reopenedSourceSheet.getRow(2).getCell(0).getStringCellValue());
                assertEquals(1, reopenedPivotSheet.getPivotTables().size());
            }
        } catch (Exception e) {
            throw new AssertionError("Unexpected exception while creating a shared streaming pivot workbook", e);
        }
    }
}
