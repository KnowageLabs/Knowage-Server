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

package it.eng.knowage.engine.api.excel.export.oldcockpit.exporters;

import it.eng.knowage.engine.api.excel.export.IWidgetExporter;
import it.eng.knowage.engine.api.excel.export.oldcockpit.ExcelExporter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class MapExporter extends GenericWidgetExporter implements IWidgetExporter {

	public static Logger logger = Logger.getLogger(MapExporter.class);

	public MapExporter(ExcelExporter excelExporter, String widgetType, String templateString, long widgetId, Workbook wb, JSONObject options, Map<String, Map<String, Object>> driversMap) {
		super(excelExporter, widgetType, templateString, widgetId, wb, options, driversMap);
	}

	@Override
	public int export() {
		int exportedSheets = 0;
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			String widgetName = getWidgetName(widget);
            widgetName = replacePlaceholderIfPresent(widgetName);

			JSONArray dataStoreArray = excelExporter.getMultiDataStoreForWidget(template, widget);
			for (int i = 0; i < dataStoreArray.length(); i++) {
				try {
					JSONObject dataStore = dataStoreArray.getJSONObject(i);
					if (dataStore != null) {
						String cockpitSheetName = getCockpitSheetName(template, widgetId) + i;
						excelExporter.createAndFillExcelSheet(dataStore, wb, widgetName, cockpitSheetName);
						exportedSheets++;
					}
				} catch (Exception e) {
					logger.error("Couldn't export layer [" + (i + 1) + "] of map widget [" + widgetId + "]");
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to export map widget: " + widgetId, e);
		}
		return exportedSheets;
	}
}
