package it.eng.knowage.engine.api.excel.export.dashboard.exporters;

import it.eng.knowage.engine.api.excel.export.ExcelExporter;
import it.eng.knowage.engine.api.excel.export.exporters.IWidgetExporter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class DashboardPivotExporter extends GenericDashboardWidgetExporter implements IWidgetExporter {

    public static Logger logger = Logger.getLogger(DashboardPivotExporter.class);

    public DashboardPivotExporter(ExcelExporter excelExporter, Workbook wb, JSONObject widget, String documentName, Map<String, Map<String, JSONArray>> selections, JSONObject drivers) {
        super(excelExporter, wb, widget, documentName, selections, drivers);
    }

    @Override
    public int export() {
        String widgetId = widget.optString("id");
        try {
            JSONObject settings = widget.getJSONObject("settings");
            String dashboardSheetName = documentName != null ? documentName : "Dashboard";
            String widgetName = getDashboardWidgetName(widget);
            Sheet sheet = excelExporter.createUniqueSafeSheet(wb, widgetName, dashboardSheetName);

            int offset = 0;
            int fetchSize = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
            JSONObject dataStore = excelExporter.getDataStoreForDashboardWidget(widget, offset, fetchSize, selections, drivers);
            if (dataStore != null) {
                int totalNumberOfRows = dataStore.getInt("results");
                while (offset < totalNumberOfRows) {
                    excelExporter.fillTableSheetWithData(dataStore, wb, sheet, widgetName, offset, settings);
                    offset += fetchSize;
                    dataStore = excelExporter.getDataStoreForDashboardWidget(widget, offset, fetchSize, selections, drivers);
                }
                excelExporter.createPivotTable(wb, sheet, widget, widgetName);
                return 1;
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Unable to export table widget: " + widgetId, e);
        }
        return 0;
    }


}
