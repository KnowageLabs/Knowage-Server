package it.eng.knowage.engine.api.excel.export.dashboard.exporters;

import it.eng.knowage.engine.api.excel.export.ExcelExporter;
import it.eng.knowage.engine.api.excel.export.exporters.IWidgetExporter;
import it.eng.knowage.engine.api.excel.export.exporters.WidgetExporterFactory;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class DashboardWidgetExporterFactory {
    public static Logger logger = Logger.getLogger(WidgetExporterFactory.class);

    public static IWidgetExporter getExporter(ExcelExporter exporter, Workbook wb, JSONObject widget, String documentName, Map<String, Map<String, JSONArray>> selections, JSONObject drivers) {
        if (widget.optString("type").equalsIgnoreCase("table")) {
            // table widget supports pagination
            return new DashboardTableExporter(exporter, wb, widget, documentName, selections, drivers);
        } else if (widget.optString("type").equalsIgnoreCase("static-pivot-table")) {
            return new DashboardPivotExporter(exporter, wb, widget, documentName, selections, drivers);
        }
        else {
            // chart widget does not support pagination
            return new GenericDashboardWidgetExporter(exporter, wb, widget, documentName, selections, drivers);
        }
    }
}

