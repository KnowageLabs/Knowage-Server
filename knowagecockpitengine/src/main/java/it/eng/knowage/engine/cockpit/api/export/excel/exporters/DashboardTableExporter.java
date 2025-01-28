package it.eng.knowage.engine.cockpit.api.export.excel.exporters;

import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONObject;

public class DashboardTableExporter extends GenericDashboardWidgetExporter implements IWidgetExporter{
    public static transient Logger logger = Logger.getLogger(TableExporter.class);

    public DashboardTableExporter(ExcelExporter excelExporter, String widgetType, String templateString, String widgetId, Workbook wb, JSONObject options) {
        super(excelExporter, widgetType, templateString, widgetId, wb, options);
    }

    @Override
    public int export() {
        try {
            JSONObject template = new JSONObject(templateString);
            JSONObject widget = getDashboardWidgetById(template, widgetId);
            JSONObject settings = widget.getJSONObject("settings");
            String dashboardSheetName = getDashboardSheetName(template, widgetId);
            String widgetName = getDashboardWidgetName(widget);
            Sheet sheet = excelExporter.createUniqueSafeSheet(wb, "", dashboardSheetName);

            int offset = 0;
            int fetchSize = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
            JSONObject dataStore = excelExporter.getDataStoreForDashboardWidget(template, widget, offset, fetchSize);
            if (dataStore != null) {
                int totalNumberOfRows = dataStore.getInt("results");
                while (offset < totalNumberOfRows) {
                    excelExporter.fillDashboardSheetWithData(dataStore, wb, sheet, widgetName, offset, settings);
                    offset += fetchSize;
                    dataStore = excelExporter.getDataStoreForDashboardWidget(template, widget, offset, fetchSize);
                }
                return 1;
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Unable to export table widget: " + widgetId, e);
        }
        return 0;
    }
}
