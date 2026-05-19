package it.eng.knowage.engine.api.export.dashboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DashboardExporterTest {

    @Test
    public void shouldPreserveOrderBySummaryPathInPivotAggregations() throws JSONException {
        DashboardExporter exporter = new DashboardExporter("test-user", null);

        JSONObject widget = new JSONObject()
                .put("fields", new JSONObject()
                        .put("columns", new JSONArray()
                                .put(new JSONObject()
                                        .put("id", "customer-column")
                                        .put("columnName", "COD_CLIENTE")
                                        .put("alias", "Cliente")
                                        .put("fieldType", "ATTRIBUTE")))
                        .put("rows", new JSONArray()
                                .put(new JSONObject()
                                        .put("id", "manager-row")
                                        .put("columnName", "RESP_CLIENTE")
                                        .put("alias", "Responsabile")
                                        .put("fieldType", "ATTRIBUTE")
                                        .put("sort", "ASC")
                                        .put("orderColumn", "measure-1")
                                        .put("orderBySummaryPath", new JSONArray().put("CLIENTE ABC"))))
                        .put("filters", new JSONArray())
                        .put("data", new JSONArray()
                                .put(new JSONObject()
                                        .put("id", "measure-1")
                                        .put("columnName", "M_1")
                                        .put("alias", "Misura 1")
                                        .put("fieldType", "MEASURE")
                                        .put("aggregation", "SUM"))));

        JSONObject aggregations = exporter.getPivotAggregations(widget, "dataset").getJSONObject("aggregations");
        JSONArray categories = aggregations.getJSONArray("categories");

        assertFalse(categories.getJSONObject(0).has("orderBySummaryPath"));

        JSONObject sortedCategory = categories.getJSONObject(1);
        assertEquals("ASC", sortedCategory.getString("orderType"));
        assertEquals("M_1", sortedCategory.getString("orderColumn"));
        assertEquals("CLIENTE ABC", sortedCategory.getJSONArray("orderBySummaryPath").getString(0));
    }

    @Test
    public void shouldPreserveRowSummaryPathWhenSortingPivotColumns() throws JSONException {
        DashboardExporter exporter = new DashboardExporter("test-user", null);

        JSONObject widget = new JSONObject()
                .put("fields", new JSONObject()
                        .put("columns", new JSONArray()
                                .put(new JSONObject()
                                        .put("id", "description-column")
                                        .put("columnName", "DESC_MISURE")
                                        .put("alias", "Descrizione")
                                        .put("fieldType", "ATTRIBUTE")
                                        .put("sort", "DESC")
                                        .put("orderColumn", "measure-1")
                                        .put("orderBySummaryPath", new JSONArray()
                                                .put("COD_1")
                                                .put("Resp 3"))))
                        .put("rows", new JSONArray()
                                .put(new JSONObject()
                                        .put("id", "customer-row")
                                        .put("columnName", "COD_CLIENTE")
                                        .put("alias", "Cliente")
                                        .put("fieldType", "ATTRIBUTE")
                                        .put("orderColumn", "COD_CLIENTE"))
                                .put(new JSONObject()
                                        .put("id", "manager-row")
                                        .put("columnName", "RESP_CLIENTE")
                                        .put("alias", "Responsabile")
                                        .put("fieldType", "ATTRIBUTE")
                                        .put("orderColumn", "RESP_CLIENTE")))
                        .put("filters", new JSONArray())
                        .put("data", new JSONArray()
                                .put(new JSONObject()
                                        .put("id", "measure-1")
                                        .put("columnName", "M_1")
                                        .put("alias", "Misura 1")
                                        .put("fieldType", "MEASURE")
                                        .put("aggregation", "SUM"))));

        JSONObject aggregations = exporter.getPivotAggregations(widget, "dataset").getJSONObject("aggregations");
        JSONArray categories = aggregations.getJSONArray("categories");

        JSONObject sortedCategory = categories.getJSONObject(0);
        assertEquals("DESC", sortedCategory.getString("orderType"));
        assertEquals("M_1", sortedCategory.getString("orderColumn"));
        assertEquals("COD_1", sortedCategory.getJSONArray("orderBySummaryPath").getString(0));
        assertEquals("Resp 3", sortedCategory.getJSONArray("orderBySummaryPath").getString(1));
        assertFalse(categories.getJSONObject(1).has("orderBySummaryPath"));
        assertFalse(categories.getJSONObject(2).has("orderBySummaryPath"));
    }
}
