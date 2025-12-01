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

import java.util.Map;

import static org.apache.poi.ss.usermodel.DataConsolidateFunction.*;

public class DashboardPivotExporter extends GenericDashboardWidgetExporter implements IWidgetExporter {

    public static Logger logger = Logger.getLogger(DashboardPivotExporter.class);

    public DashboardPivotExporter(DashboardExcelExporter excelExporter, JSONObject widget, String documentName, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, JSONObject parameters, String userUniqueIdentifier, String imageB64) {
        super(excelExporter, null, widget, documentName, selections, drivers, parameters, userUniqueIdentifier, imageB64);
    }

    public Workbook exportPivot() {
        String widgetId = widget.optString("id");
        try {
            JSONObject settings = widget.getJSONObject("settings");
            String widgetName = getJsonObjectUtils().replacePlaceholderIfPresent(getJsonObjectUtils().getDashboardWidgetName(widget), drivers);

            int offset = 0;
            int fetchSize = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
            JSONObject dataStore = getDataStoreForDashboardWidget(widget, offset, fetchSize, selections, drivers, parameters);
            if (dataStore != null) {
                String imageB64 = OrganizationImageManager.getOrganizationB64ImageWide(TenantManager.getTenant().getName());

                int totalNumberOfRows = dataStore.getInt("results");
                XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
                XSSFSheet xssfSheet = xssfWorkbook.createSheet("Source_sheet");

                excelExporter.fillTableSheetWithData(dataStore, xssfWorkbook, xssfSheet, widgetName, offset, settings);

                XSSFSheet pivotSheet = xssfWorkbook.createSheet("Pivot_sheet");

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


                int xssfSheetLastRowNum = xssfSheet.getLastRowNum();
                int sourceSheetLastColumn = xssfSheet.getRow(xssfSheetLastRowNum).getLastCellNum();
                String lastColLetter = CellReference.convertNumToColString(sourceSheetLastColumn - 1);

                boolean isImagePresent = imageB64 != null;
                XSSFPivotTable pivotTable = pivotSheet.createPivotTable(
                        new AreaReference(isImagePresent? new CellReference("Source_sheet!A4") : new CellReference("Source_sheet!A2"),
                                new CellReference("Source_sheet!" + lastColLetter + (totalNumberOfRows + (isImagePresent ? 3 : 1))), //make the reference big enough for later data
                                SpreadsheetVersion.EXCEL2007),
                        new CellReference("A8"));

                formatPivot(pivotTable);

                SXSSFWorkbook swb = new SXSSFWorkbook(xssfWorkbook);
                SXSSFSheet ssheet = swb.getSheet("Source_sheet");

                while (offset < totalNumberOfRows) {
                    offset += fetchSize;
                    dataStore = getDataStoreForDashboardWidget(widget, offset, fetchSize, selections, drivers, parameters);
                    excelExporter.fillTableSheetWithData(dataStore, swb, ssheet, widgetName, offset, settings);
                }

                return swb;
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Unable to export table widget: " + widgetId, e);
        }
        return null;
    }

    private void formatPivot(XSSFPivotTable pivotTable) {
        try {
            JSONObject fields = widget.getJSONObject("fields");
            JSONArray columns = fields.getJSONArray("columns");
            JSONArray rows = fields.getJSONArray("rows");
            JSONArray filters = fields.getJSONArray("filters");
            JSONArray data = fields.getJSONArray("data");

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
                    pivotTable.addColumnLabel(func, counter);
                    counter++;
                }
            } catch (JSONException e) {
                logger.error("Error while adding data columns to pivot table", e);
            }

        } catch (JSONException e) {
            logger.error("Error while creating pivot table", e);
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


    }
