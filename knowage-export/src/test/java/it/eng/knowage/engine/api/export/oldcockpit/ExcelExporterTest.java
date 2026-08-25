package it.eng.knowage.engine.api.export.oldcockpit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExcelExporterTest {

    @Test
    public void shouldExportWidgetsInCockpitSheetIndexOrder() throws JSONException {
        JSONObject template = new JSONObject()
                .put("sheets", new JSONArray()
                        .put(sheet("Last", 2, "last-widget"))
                        .put(sheet("First", 0, "first-widget"))
                        .put(sheet("Second", 1, "second-widget")));

        JSONArray orderedWidgets = ExcelExporter.getCockpitWidgetsJson(template);

        assertEquals("first-widget", orderedWidgets.getJSONObject(0).getString("id"));
        assertEquals("second-widget", orderedWidgets.getJSONObject(1).getString("id"));
        assertEquals("last-widget", orderedWidgets.getJSONObject(2).getString("id"));
    }

    @Test
    public void shouldPreserveTemplateOrderWhenSheetIndexesAreMissing() throws JSONException {
        JSONObject template = new JSONObject()
                .put("sheets", new JSONArray()
                        .put(sheet("First", null, "first-widget"))
                        .put(sheet("Second", null, "second-widget")));

        JSONArray orderedSheets = ExcelExporter.getOrderedCockpitSheets(template);

        assertEquals("First", orderedSheets.getJSONObject(0).getString("label"));
        assertEquals("Second", orderedSheets.getJSONObject(1).getString("label"));
    }

    private JSONObject sheet(String label, Integer index, String widgetId) throws JSONException {
        JSONObject sheet = new JSONObject()
                .put("label", label)
                .put("widgets", new JSONArray().put(new JSONObject().put("id", widgetId)));
        if (index != null) {
            sheet.put("index", index);
        }
        return sheet;
    }
}
