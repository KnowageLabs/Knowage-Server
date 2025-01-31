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

import it.eng.knowage.engine.cockpit.api.export.excel.ExcelExporter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONObject;

class GenericDashboardWidgetExporter implements IWidgetExporter {

    ExcelExporter excelExporter;
    Workbook wb;
    JSONObject widget;

    public GenericDashboardWidgetExporter(ExcelExporter excelExporter, Workbook wb, JSONObject widget) {
        super();
        this.excelExporter = excelExporter;
        this.wb = wb;
        this.widget = widget;
    }

    @Override
    public int export() {
        JSONObject dataStore;
        String widgetId;
        widgetId = widget.optString("id");
        try {
                dataStore = excelExporter.getDataStoreforDashboardSingleWidget(widget);
                if (dataStore != null) {
                    String dashboardSheetName = widget.getString("type").concat(" ").concat(widget.getString("id"));
                    excelExporter.createAndFillDashboardExcelSheet(dataStore, wb, widgetId, dashboardSheetName);
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
            if (title != null) {
                widgetName = title.optString("text");
            } else {
                widgetName = widget.optString("type").concat(" ").concat(widget.optString("id"));
            }
        }
        return widgetName;
    }
}
