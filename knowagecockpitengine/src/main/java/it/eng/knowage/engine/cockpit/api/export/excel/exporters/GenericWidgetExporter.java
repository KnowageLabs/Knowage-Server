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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

class GenericWidgetExporter implements IWidgetExporter {

	static private Logger logger = Logger.getLogger(GenericWidgetExporter.class);

	ExcelExporter excelExporter;
	String widgetType;
	String templateString;
	long widgetId;
	Workbook wb;
	JSONObject optionsObj;

	public GenericWidgetExporter() {
		super();
	}

	public GenericWidgetExporter(ExcelExporter excelExporter, String widgetType, String templateString, long widgetId, Workbook wb, JSONObject options) {
		super();
		this.excelExporter = excelExporter;
		this.widgetType = widgetType;
		this.templateString = templateString;
		this.widgetId = widgetId;
		this.wb = wb;
		this.optionsObj = options;
	}

	@Override
	public int export() {
		try {
			JSONObject template = new JSONObject(templateString);
			JSONObject widget = getWidgetById(template, widgetId);
			String widgetName = getWidgetName(widget);

			JSONObject dataStore = excelExporter.getDataStoreForWidget(template, widget);
			if (dataStore != null) {
				String cockpitSheetName = getCockpitSheetName(template, widgetId);
				excelExporter.createAndFillExcelSheet(dataStore, wb, widgetName, cockpitSheetName);
				return 1;
			}

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Unable to export generic widget: " + widgetId, e);
		}
		return 0;
	}

	protected String getCockpitSheetName(JSONObject template, long widgetId) {
		try {
			JSONArray sheets = template.getJSONArray("sheets");
			if (sheets.length() == 1)
				return "";
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				JSONArray widgets = sheet.getJSONArray("widgets");
				for (int j = 0; j < widgets.length(); j++) {
					JSONObject widget = widgets.getJSONObject(j);
					if (widgetId == widget.getLong("id"))
						return sheet.getString("label");
				}
			}
			return "";
		} catch (Exception e) {
			logger.error("Unable to retrieve cockpit sheet name from template", e);
			return "";
		}
	}

	protected String getWidgetName(JSONObject widget) throws JSONException {
		String widgetName = null;
		JSONObject style = widget.optJSONObject("style");
		if (style != null) {
			JSONObject title = style.optJSONObject("title");
			if (title != null) {
				widgetName = title.optString("label");
			} else {
				JSONObject content = widget.optJSONObject("content");
				if (content != null) {
					widgetName = content.getString("name");
				}
			}
		}
		return widgetName;
	}

	protected JSONObject getWidgetById(JSONObject template, long widgetId) {
		try {

			JSONArray sheets = template.getJSONArray("sheets");
			for (int i = 0; i < sheets.length(); i++) {
				JSONObject sheet = sheets.getJSONObject(i);
				JSONArray widgets = sheet.getJSONArray("widgets");
				for (int j = 0; j < widgets.length(); j++) {
					JSONObject widget = widgets.getJSONObject(j);
					long id = widget.getLong("id");
					if (id == widgetId) {
						return widget;
					}
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting widget with id [" + widgetId + "] from template", e);
		}
		throw new SpagoBIRuntimeException("Unable to find widget with id [" + widgetId + "] in template");
	}

}
