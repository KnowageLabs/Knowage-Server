/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
			return new GenericWidgetExporter(exporter, widgetType, templateString, widgetId, wb, options);
		}
	}

}
