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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class TableExporter extends GenericWidgetExporter implements IWidgetExporter {

	public static transient Logger logger = Logger.getLogger(TableExporter.class);

	public TableExporter(ExcelExporter excelExporter, String widgetType, String templateString, long widgetId, Workbook wb, JSONObject options) {
		super(excelExporter, widgetType, templateString, widgetId, wb, options);
	}

	@Override
	public int export() {
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			String widgetName = getWidgetName(widget);
			String cockpitSheetName = getCockpitSheetName(template, widgetId);
			Sheet sheet = excelExporter.createUniqueSafeSheet(wb, widgetName, cockpitSheetName);

			int offset = 0;
			int fetchSize = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
			JSONObject dataStore = excelExporter.getDataStoreForWidget(template, widget, offset, fetchSize);
			if (dataStore != null) {
				int totalNumberOfRows = dataStore.getInt("results");
				while (offset < totalNumberOfRows) {
					excelExporter.fillSheetWithData(dataStore, wb, sheet, widgetName, offset);
					offset += fetchSize;
					dataStore = excelExporter.getDataStoreForWidget(template, widget, offset, fetchSize);
				}
				return 1;
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to export table widget: " + widgetId, e);
		}
		return 0;
	}
}
