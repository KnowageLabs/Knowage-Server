package it.eng.knowage.engine.api.export.dashboard.excel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DashboardExcelExporterTest {

    @Test
    public void shouldKeepXlsxStyleEnabledByDefaultForSingleWidgetExports() {
        assertTrue(DashboardExcelExporter.isXlsxStyleEnabled(new JSONObject(), true));
    }

    @Test
    public void shouldReadXlsxStyleEnabledFromSingleWidgetRoot() throws JSONException {
        JSONObject body = new JSONObject().put("xlsxStyleEnabled", false);

        assertFalse(DashboardExcelExporter.isXlsxStyleEnabled(body, true));
    }

    @Test
    public void shouldKeepXlsxStyleEnabledByDefaultForDashboardExports() {
        assertTrue(DashboardExcelExporter.isXlsxStyleEnabled(new JSONObject(), false));
    }

    @Test
    public void shouldReadXlsxStyleEnabledFromDashboardMenuWidgetsConfiguration() throws JSONException {
        JSONObject body = new JSONObject()
                .put("configuration", new JSONObject()
                        .put("menuWidgets", new JSONObject()
                                .put("xlsxStyleEnabled", false)));

        assertFalse(DashboardExcelExporter.isXlsxStyleEnabled(body, false));
    }

    @Test
    public void shouldOrderDashboardWidgetsByGridPosition() throws JSONException {
        JSONObject dashboard = new JSONObject()
                .put("widgets", new JSONArray()
                        .put(new JSONObject().put("id", "bottom").put("row", 1).put("col", 0))
                        .put(new JSONObject().put("id", "right").put("row", 0).put("col", 1))
                        .put(new JSONObject().put("id", "left").put("row", 0).put("col", 0)));

        JSONArray orderedWidgets = DashboardExcelExporter.getDashboardWidgetsJson(dashboard);

        assertEquals("left", orderedWidgets.getJSONObject(0).getString("id"));
        assertEquals("right", orderedWidgets.getJSONObject(1).getString("id"));
        assertEquals("bottom", orderedWidgets.getJSONObject(2).getString("id"));
    }

    @Test
    public void shouldPreserveWidgetArrayOrderWhenGridPositionsAreMissing() throws JSONException {
        JSONObject dashboard = new JSONObject()
                .put("widgets", new JSONArray()
                        .put(new JSONObject().put("id", "first"))
                        .put(new JSONObject().put("id", "second")));

        JSONArray orderedWidgets = DashboardExcelExporter.getDashboardWidgetsJson(dashboard);

        assertEquals("first", orderedWidgets.getJSONObject(0).getString("id"));
        assertEquals("second", orderedWidgets.getJSONObject(1).getString("id"));
    }

    @Test
    public void shouldUseDashboardSheetLabelAsMultiSheetPrefix() throws JSONException {
        JSONObject dashboardSheet = new JSONObject().put("label", "Overview");

        assertEquals("Overview", DashboardExcelExporter.getDashboardSheetName(dashboardSheet, true));
    }

    @Test
    public void shouldNotPrefixSingleSheetDashboardTabs() throws JSONException {
        JSONObject dashboardSheet = new JSONObject().put("label", "Overview");

        assertEquals("", DashboardExcelExporter.getDashboardSheetName(dashboardSheet, false));
    }

    @Test
    public void shouldUseLargeLayoutOrderForDashboardSheetWidgets() throws JSONException {
        JSONObject dashboard = new JSONObject()
                .put("widgets", new JSONArray()
                        .put(new JSONObject().put("id", "first"))
                        .put(new JSONObject().put("id", "second")))
                .put("sheets", new JSONArray()
                        .put(new JSONObject()
                                .put("label", "Overview")
                                .put("widgets", new JSONObject()
                                        .put("lg", new JSONArray()
                                                .put(new JSONObject().put("id", "second").put("x", 1).put("y", 0))
                                                .put(new JSONObject().put("id", "first").put("x", 0).put("y", 0))))));
        Map<String, JSONObject> widgetsById = DashboardExcelExporter.getDashboardWidgetsById(dashboard);

        JSONArray orderedWidgets = DashboardExcelExporter.getDashboardSheetWidgets(
                dashboard.getJSONArray("sheets").getJSONObject(0), widgetsById);

        assertEquals("first", orderedWidgets.getJSONObject(0).getString("id"));
        assertEquals("second", orderedWidgets.getJSONObject(1).getString("id"));
    }
}
