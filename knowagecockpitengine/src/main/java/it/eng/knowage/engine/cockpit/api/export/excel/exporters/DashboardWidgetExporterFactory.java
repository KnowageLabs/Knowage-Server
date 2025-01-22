package it.eng.knowage.engine.cockpit.api.export.excel.exporters;

import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONObject;

public class DashboardWidgetExporterFactory {
    public static transient Logger logger = Logger.getLogger(WidgetExporterFactory.class);

    /**
     * @param widgetType the type of the widget to be exported
     */

    public static IWidgetExporter getExporter(ExcelExporter exporter, String widgetType, String templateString, String widgetId, Workbook wb,
                                              JSONObject options) {
        if (widgetType.equalsIgnoreCase("table")) {
            // table widget supports pagination
            return new DashboardTableExporter(exporter, widgetType, templateString, widgetId, wb, options);
        } else {
            throw new SpagoBIRuntimeException("Unable to export table widget: " + widgetId);
        }
    }
}

