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

package it.eng.knowage.engine.api.export.dashboard.exporters;

import it.eng.knowage.engine.api.export.IWidgetExporter;
import it.eng.knowage.engine.api.export.dashboard.*;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

class GenericDashboardWidgetExporter extends DashboardExporter implements IWidgetExporter {

    DashboardExcelExporter excelExporter;
    Workbook wb;
    JSONObject widget;
    String documentName;
    Map<String, Map<String, JSONArray>> selections;
    JSONObject drivers;
    JSONObject parameters;

    public GenericDashboardWidgetExporter(DashboardExcelExporter excelExporter, Workbook wb, JSONObject widget, String documentName, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, JSONObject parameters, String userUniqueIdentifier) {
        super(userUniqueIdentifier);
        this.excelExporter = excelExporter;
        this.wb = wb;
        this.widget = widget;
        this.documentName = documentName;
        this.selections = selections;
        this.drivers = drivers;
        this.parameters = parameters;
    }

    @Override
    public int export() {
        String widgetId = widget.optString("id");
        try {
            JSONObject settings = widget.getJSONObject("settings");
            JSONObject dataStore = getDataStoreforDashboardSingleWidget(widget, selections, drivers, parameters);
            String widgetName = getJsonObjectUtils().replacePlaceholderIfPresent(getJsonObjectUtils().getDashboardWidgetName(widget), drivers);
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
    }}
