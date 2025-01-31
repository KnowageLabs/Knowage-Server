package it.eng.knowage.engine.cockpit.api.export.excel.exporters;

import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONObject;

public class DashboardTableExporter extends GenericDashboardWidgetExporter implements IWidgetExporter{
    public static Logger logger = Logger.getLogger(TableExporter.class);

    public DashboardTableExporter(ExcelExporter excelExporter, Workbook wb, JSONObject widget) {
        super(excelExporter, wb, widget);
    }

    @Override
    public int export() {
        String widgetId = widget.optString("id");
        try {
            JSONObject settings = widget.getJSONObject("settings");
            String dashboardSheetName = widget.getString("type").concat(" ").concat(widget.getString("id"));
            String widgetName = getDashboardWidgetName(widget);
            Sheet sheet = excelExporter.createUniqueSafeSheet(wb, widgetName, dashboardSheetName);

            int offset = 0;
            int fetchSize = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
            JSONObject dataStore = excelExporter.getDataStoreForDashboardWidget(widget, offset, fetchSize);
            if (dataStore != null) {
                int totalNumberOfRows = dataStore.getInt("results");
                while (offset < totalNumberOfRows) {
                    excelExporter.fillDashboardSheetWithData(dataStore, wb, sheet, widgetName, offset, settings);
                    offset += fetchSize;
                    dataStore = excelExporter.getDataStoreForDashboardWidget(widget, offset, fetchSize);
                }
                return 1;
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Unable to export table widget: " + widgetId, e);
        }
        return 0;
    }
}
