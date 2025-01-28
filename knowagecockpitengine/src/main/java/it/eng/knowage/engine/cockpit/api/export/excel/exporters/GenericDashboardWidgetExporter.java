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
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.Objects;

class GenericDashboardWidgetExporter implements IWidgetExporter {

    private static final Logger logger = Logger.getLogger(GenericDashboardWidgetExporter.class);

    ExcelExporter excelExporter;
    String widgetType;
    String templateString;
    String widgetId;
    Workbook wb;
    JSONObject optionsObj;

    public GenericDashboardWidgetExporter(ExcelExporter excelExporter, String widgetType, String templateString, String widgetId, Workbook wb, JSONObject options) {
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
            JSONObject widget = getDashboardWidgetById(template, widgetId);
            String widgetName = getDashboardWidgetName(widget);
            JSONObject dataStore = excelExporter.getDataStoreforDashboardWidget(template, widget);
            if (dataStore != null) {
                String dashboardSheetName = getDashboardSheetName(template, widgetId);
                excelExporter.createAndFillDashboardExcelSheet(dataStore, wb, widgetName, dashboardSheetName);
                return 1;
            }

        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Unable to export generic widget: " + widgetId, e);
        }
        return 0;
    }
    protected String getDashboardSheetName(JSONObject template, String widgetId) {
        try {
            JSONArray widgets = template.getJSONArray("widgets");
            if (widgets.length() == 1) {
                return "";
            }
            for (int i = 0; i < widgets.length(); i++) {
                JSONObject widget = widgets.getJSONObject(i);
                if (Objects.equals(widgetId, widget.getString("id"))) {
                    return widget.getString("type").concat(" ").concat(widget.getString("id"));
                }
            }
            return "";
        } catch (Exception e) {
            logger.error("Unable to retrieve cockpit sheet name from template", e);
            return "";
        }
    }

    protected String getDashboardWidgetName(JSONObject widget) {
        String widgetName = "";
        JSONObject settings = widget.optJSONObject("settings");
        JSONObject style = settings.optJSONObject("style");
        if (style != null) {
            JSONObject title = style.optJSONObject("title");
            if (title != null) {
                widgetName = title.optString("text");
            }
        }
        return widgetName;
    }

    protected JSONObject getDashboardWidgetById(JSONObject template, String widgetId) {
        try {

            JSONArray widgets = template.getJSONArray("widgets");
            for (int i = 0; i < widgets.length(); i++) {
                JSONObject widget = widgets.getJSONObject(i);
                String id = widget.getString("id");
                if (Objects.equals(id, widgetId)) {
                    return widget;
                }
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Error while getting widget with id [" + widgetId + "] from template", e);
        }
        throw new SpagoBIRuntimeException("Unable to find widget with id [" + widgetId + "] in template");
    }




}
