package it.eng.knowage.engine.api.export.dashboard.excel.exporters;

import it.eng.knowage.engine.api.export.IWidgetExporter;
import it.eng.knowage.engine.api.export.dashboard.excel.DashboardExcelExporter;
import it.eng.knowage.engine.api.export.oldcockpit.exporters.WidgetExporterFactory;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class DashboardWidgetExporterFactory {
    public static Logger logger = Logger.getLogger(WidgetExporterFactory.class);

    public static IWidgetExporter getExporter(DashboardExcelExporter exporter,
                                              Workbook wb,
                                              JSONObject widget,
                                              String documentName,
                                              Map<String, Map<String, JSONArray>> selections,
                                              JSONObject drivers,
                                              JSONObject parameters,
                                              String userUniqueIdentifier,
                                              String imageB64) {
        if (widget.optString("type").equalsIgnoreCase("table")) {
            return new DashboardTableExporter(exporter, wb, widget, documentName, selections, drivers, parameters, userUniqueIdentifier, imageB64);
        } else if (widget.optString("type").equalsIgnoreCase("static-pivot-table")) {
            return new DashboardPivotExporter(exporter, widget, documentName, selections, drivers, parameters, userUniqueIdentifier, imageB64);
        }
        else {
            return new GenericDashboardWidgetExporter(exporter, wb, widget, documentName, selections, drivers, parameters, userUniqueIdentifier, imageB64);
        }
    }
}

