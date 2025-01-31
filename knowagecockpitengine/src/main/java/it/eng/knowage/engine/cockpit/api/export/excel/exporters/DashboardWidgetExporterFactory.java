package it.eng.knowage.engine.cockpit.api.export.excel.exporters;

import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONObject;

public class DashboardWidgetExporterFactory {
    public static Logger logger = Logger.getLogger(WidgetExporterFactory.class);

    public static IWidgetExporter getExporter(ExcelExporter exporter, Workbook wb, JSONObject widget) {
        if (widget.optString("tipe").equalsIgnoreCase("table")) {
            // table widget supports pagination
            return new DashboardTableExporter(exporter, wb, widget);
        } else {
            // chart widget does not support pagination
            return new GenericDashboardWidgetExporter(exporter, wb, widget);
        }
    }
}

