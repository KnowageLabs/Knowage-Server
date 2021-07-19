package it.eng.knowage.engine.cockpit.api.export.excel.exporters;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;

public class WidgetExporterFactory {

	public static transient Logger logger = Logger.getLogger(WidgetExporterFactory.class);

	/**
	 *
	 * @param widgetType the type of the widget to be exported
	 */

	public static IWidgetExporter getExporter(ExcelExporter exporter, String widgetType, String templateString, long widgetId, Workbook wb,
			JSONObject options) {
		if (widgetType.equalsIgnoreCase("static-pivot-table") && options != null) {
			// crosstab widget object must be retrieved BE side
			return new CrossTabExporter(exporter, widgetType, templateString, widgetId, wb, options);
		} else if (widgetType.equalsIgnoreCase("map")) {
			// map widget supports multiple datasets
			return new MapExporter(exporter, widgetType, templateString, widgetId, wb, options);
		} else if (widgetType.equalsIgnoreCase("table")) {
			// table widget supports pagination
			return new TableExporter(exporter, widgetType, templateString, widgetId, wb, options);
		} else {
			return new GenericExporter(exporter, widgetType, templateString, widgetId, wb, options);
		}
	}

}
