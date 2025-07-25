package it.eng.knowage.engine.api.excel.export.dashboard.exporters;

import it.eng.knowage.engine.api.excel.export.IWidgetExporter;
import it.eng.knowage.engine.api.excel.export.dashboard.DashboardExcelExporter;
import it.eng.knowage.engine.api.excel.export.dashboard.DatastoreUtils;
import it.eng.knowage.engine.api.excel.export.dashboard.StyleProvider;
import it.eng.knowage.engine.api.excel.export.oldcockpit.exporters.WidgetExporterFactory;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class DashboardWidgetExporterFactory {
    public static Logger logger = Logger.getLogger(WidgetExporterFactory.class);

    public static IWidgetExporter getExporter(DashboardExcelExporter exporter, Workbook wb, JSONObject widget, String documentName, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, DatastoreUtils datastoreUtils, StyleProvider styleProvider, JSONObject parameters) {
        if (widget.optString("type").equalsIgnoreCase("table")) {
            return new DashboardTableExporter(exporter, wb, widget, documentName, selections, drivers, datastoreUtils, styleProvider, parameters);
        } else if (widget.optString("type").equalsIgnoreCase("static-pivot-table")) {
            return new DashboardPivotExporter(exporter, wb, widget, documentName, selections, drivers, datastoreUtils, styleProvider, parameters);
        }
        else {
            return new GenericDashboardWidgetExporter(exporter, wb, widget, documentName, selections, drivers, datastoreUtils, styleProvider, parameters);
        }
    }
}

