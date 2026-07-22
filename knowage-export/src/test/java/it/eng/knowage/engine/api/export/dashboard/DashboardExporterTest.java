package it.eng.knowage.engine.api.export.dashboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DashboardExporterTest {

    private static class TestableDashboardExporter extends DashboardExporter {

        private TestableDashboardExporter() {
            super("test-user", null);
        }

        @Override
        protected String getInternationalizedHeader(String columnName) {
            return columnName;
        }

        private String resolveColumnDisplayName(JSONObject settings, JSONObject column, JSONArray variables) throws JSONException {
            return getDashboardColumnDisplayName(settings, column, variables);
        }

        private String resolveLocaleTag(JSONObject body) {
            return getLocaleFromBody(body).toLanguageTag();
        }

        private String resolveWidgetXlsxSheetName(JSONObject widget, JSONObject drivers, String defaultWidgetName) {
            return getWidgetXlsxSheetName(widget, drivers, defaultWidgetName);
        }

        private boolean hasConfiguredCustomWidgetXlsxSheetName(JSONObject widget) {
            return hasCustomWidgetXlsxSheetName(widget);
        }

        private String resolveAppliedFiltersSheetName(JSONObject body) {
            this.locale = getLocaleFromBody(body);
            return getAppliedFiltersSheetName();
        }


        private void copyWidgetLikeSelections(JSONObject dashboardSelections, JSONObject widget) throws JSONException {
            addWidgetLikeSelections(dashboardSelections, widget);
        }
    }

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

    @Test
    public void shouldResolveVariablePlaceholderInCustomHeaderLabel() throws JSONException {
        TestableDashboardExporter exporter = new TestableDashboardExporter();

        JSONObject settings = new JSONObject()
                .put("configuration", new JSONObject()
                        .put("headers", new JSONObject()
                                .put("custom", new JSONObject()
                                        .put("enabled", true)
                                        .put("rules", new JSONArray()
                                                .put(new JSONObject()
                                                        .put("action", "setLabel")
                                                        .put("value", "$V{QQ}")
                                                        .put("target", new JSONArray().put("quarter-column")))))));
        JSONObject column = new JSONObject()
                .put("id", "quarter-column")
                .put("alias", "Quarter");
        JSONArray variables = new JSONArray()
                .put(new JSONObject()
                        .put("name", "QQ")
                        .put("value", "Q1 2026"));

        assertEquals("Q1 2026", exporter.resolveColumnDisplayName(settings, column, variables));
    }

    @Test
    public void shouldResolveDirectVariableNameInCustomHeaderLabel() throws JSONException {
        TestableDashboardExporter exporter = new TestableDashboardExporter();

        JSONObject settings = new JSONObject()
                .put("configuration", new JSONObject()
                        .put("headers", new JSONObject()
                                .put("custom", new JSONObject()
                                        .put("enabled", true)
                                        .put("rules", new JSONArray()
                                                .put(new JSONObject()
                                                        .put("action", "setLabel")
                                                        .put("value", "QQ")
                                                        .put("target", new JSONArray().put("quarter-column")))))));
        JSONObject column = new JSONObject()
                .put("id", "quarter-column")
                .put("alias", "Quarter");
        JSONArray variables = new JSONArray()
                .put(new JSONObject()
                        .put("name", "QQ")
                        .put("value", "Q1 2026"));

        assertEquals("Q1 2026", exporter.resolveColumnDisplayName(settings, column, variables));
    }

    @Test
    public void shouldCopyIncomingWidgetLikeSelections() throws JSONException {
        TestableDashboardExporter exporter = new TestableDashboardExporter();
        JSONObject dashboardSelections = new JSONObject();
        JSONObject widget = new JSONObject()
                .put("likeSelections", new JSONObject()
                        .put("sales", new JSONObject()
                                .put("COUNTRY,REGION", "it")));

        exporter.copyWidgetLikeSelections(dashboardSelections, widget);

        assertTrue(dashboardSelections.has("likeSelections"));
        assertEquals("it", dashboardSelections.getJSONObject("likeSelections").getJSONObject("sales").getString("COUNTRY,REGION"));
    }

    @Test
    public void shouldNormalizeIso3LocaleForExport() throws JSONException {
        TestableDashboardExporter exporter = new TestableDashboardExporter();

        JSONObject body = new JSONObject()
                .put("locale", "eng");

        assertEquals("en-US", exporter.resolveLocaleTag(body));
    }

    @Test
    public void shouldFallbackToDefaultLocaleWhenLocaleIsMissing() {
        TestableDashboardExporter exporter = new TestableDashboardExporter();

        assertEquals("en-US", exporter.resolveLocaleTag(new JSONObject()));
    }

    @Test
    public void shouldResolveCustomXlsxSheetNamePlaceholders() throws JSONException {
        TestableDashboardExporter exporter = new TestableDashboardExporter();

        JSONObject widget = new JSONObject()
                .put("settings", new JSONObject()
                        .put("configuration", new JSONObject()
                                .put("exports", new JSONObject()
                                        .put("xlsxSheetName", "$V{Year} $P{country} $P{country_description}"))))
                .put("variables", new JSONArray()
                        .put(new JSONObject()
                                .put("name", "Year")
                                .put("value", "2024")));
        JSONObject drivers = new JSONObject()
                .put("country", new JSONArray()
                        .put(new JSONObject()
                                .put("value", "IT")
                                .put("description", "Italy")));

        assertEquals("2024 IT Italy", exporter.resolveWidgetXlsxSheetName(widget, drivers, "Default Sheet"));
    }

    @Test
    public void shouldDetectConfiguredCustomXlsxSheetName() throws JSONException {
        TestableDashboardExporter exporter = new TestableDashboardExporter();

        JSONObject widget = new JSONObject()
                .put("settings", new JSONObject()
                        .put("configuration", new JSONObject()
                                .put("exports", new JSONObject()
                                        .put("xlsxSheetName", "$P{country}"))));

        assertTrue(exporter.hasConfiguredCustomWidgetXlsxSheetName(widget));
    }

    @Test
    public void shouldTreatBlankCustomXlsxSheetNameAsMissing() throws JSONException {
        TestableDashboardExporter exporter = new TestableDashboardExporter();

        JSONObject widget = new JSONObject()
                .put("settings", new JSONObject()
                        .put("configuration", new JSONObject()
                                .put("exports", new JSONObject()
                                        .put("xlsxSheetName", "   "))));

        assertFalse(exporter.hasConfiguredCustomWidgetXlsxSheetName(widget));
    }

    @Test
    public void shouldUseItalianFiltersSheetNameForItalianLocale() throws JSONException {
        TestableDashboardExporter exporter = new TestableDashboardExporter();

        JSONObject body = new JSONObject()
                .put("locale", "it-IT");

        assertEquals("Filtri Applicati", exporter.resolveAppliedFiltersSheetName(body));
    }

    @Test
    public void shouldKeepDefaultFiltersSheetNameForNonItalianLocale() throws JSONException {
        TestableDashboardExporter exporter = new TestableDashboardExporter();

        JSONObject body = new JSONObject()
                .put("locale", "en-US");

        assertEquals("Filters", exporter.resolveAppliedFiltersSheetName(body));
    }
}
