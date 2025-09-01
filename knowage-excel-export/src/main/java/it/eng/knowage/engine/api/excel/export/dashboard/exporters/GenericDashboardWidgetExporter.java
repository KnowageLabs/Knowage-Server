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

package it.eng.knowage.engine.api.excel.export.dashboard.exporters;

import com.fasterxml.jackson.databind.node.ArrayNode;
import it.eng.knowage.engine.api.excel.export.IWidgetExporter;
import it.eng.knowage.engine.api.excel.export.dashboard.DashboardExcelExporter;
import it.eng.knowage.engine.api.excel.export.dashboard.DatastoreUtils;
import it.eng.knowage.engine.api.excel.export.dashboard.StyleProvider;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

class GenericDashboardWidgetExporter implements IWidgetExporter {

    DashboardExcelExporter excelExporter;
    Workbook wb;
    JSONObject widget;
    String documentName;
    Map<String, Map<String, JSONArray>> selections;
    JSONObject drivers;
    JSONObject parameters;
    DatastoreUtils datastoreUtils;
    StyleProvider styleProvider;

    public GenericDashboardWidgetExporter(DashboardExcelExporter excelExporter, Workbook wb, JSONObject widget, String documentName, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, DatastoreUtils datastoreUtils, StyleProvider styleProvider, JSONObject parameters) {
        super();
        this.excelExporter = excelExporter;
        this.wb = wb;
        this.widget = widget;
        this.documentName = documentName;
        this.selections = selections;
        this.drivers = drivers;
        this.datastoreUtils = datastoreUtils;
        this.styleProvider = styleProvider;
        this.parameters = parameters;
    }

    @Override
    public int export() {
        String widgetId = widget.optString("id");
        try {
            JSONObject settings = widget.getJSONObject("settings");
            JSONObject dataStore = datastoreUtils.getDataStoreforDashboardSingleWidget(widget, selections, drivers, parameters);
            String widgetName = getDashboardWidgetName(widget);
            widgetName = replacePlaceholderIfPresent(widgetName);
            if (dataStore != null) {
                String dashboardSheetName = documentName != null ? documentName : "Dashboard";
                Sheet sheet = excelExporter.createUniqueSafeSheet(wb, widgetName, dashboardSheetName);
                excelExporter.fillGenericWidgetSheetWithData(dataStore, wb, sheet, widgetName, 0, settings);
                return 1;
                }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Unable to export generic widget: " + widgetId, e);
        }
        return 0;
    }

    protected String getDashboardWidgetName(JSONObject widget) {
        String widgetName = "";
        JSONObject settings = widget.optJSONObject("settings");
        JSONObject style = settings.optJSONObject("style");
        if (style != null) {
            JSONObject title = style.optJSONObject("title");
            if (title != null && !title.optString("text").isEmpty()) {
                widgetName = title.optString("text");
            } else {
                widgetName = getWidgetGenericName(widget);
            }
        }
        return widgetName;
    }

    private static String getWidgetGenericName(JSONObject widget) {
        return widget.optString("type").concat(" ").concat(widget.optString("id"));
    }

    protected String replacePlaceholderIfPresent(String widgetName) {
        if (widgetName.contains("$P{")) {
            String placeholder = widgetName.substring(widgetName.indexOf("$P{") + 3, widgetName.indexOf("}"));
            if (drivers.has(placeholder)) {
                try {
                    JSONArray valuesArray =  drivers.getJSONArray(placeholder);
                    if (valuesArray.length() > 0) {
                        JSONObject value = valuesArray.getJSONObject(0);
                        widgetName = widgetName.replace("$P{" + placeholder + "}", value.getString("value"));
                    }
                } catch (JSONException e) {
                    throw new SpagoBIRuntimeException("Unable to replace placeholder in widget name", e);
                }
            }
        }
        return widgetName;
    }
}
