package it.eng.knowage.engine.api.export.dashboard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JSONObjectUtilsTest {

    private final JSONObjectUtils jsonObjectUtils = new JSONObjectUtils();

    @Test
    public void shouldReplaceDriverDescriptionPlaceholderWhenVariablePlaceholderIsPresentBeforeIt() throws JSONException {
        JSONObject drivers = new JSONObject()
                .put("country", new JSONArray().put(new JSONObject().put("value", "IT").put("description", "Italy")));
        JSONArray variables = new JSONArray()
                .put(new JSONObject().put("name", "Year").put("value", "2024"));

        String resolvedWidgetName = jsonObjectUtils.replacePlaceholderIfPresent("$V{Year} $P{country_description}", drivers, variables);

        assertEquals("2024 Italy", resolvedWidgetName);
    }

    @Test
    public void shouldReplaceBothDriverValueAndDescriptionPlaceholders() throws JSONException {
        JSONObject drivers = new JSONObject()
                .put("country", new JSONArray().put(new JSONObject().put("value", "IT").put("description", "Italy")));

        String resolvedWidgetName = jsonObjectUtils.replacePlaceholderIfPresent("$P{country} - $P{country_description}", drivers);

        assertEquals("IT - Italy", resolvedWidgetName);
    }

    @Test
    public void shouldReplacePivotedVariablePlaceholder() throws JSONException {
        JSONArray variables = new JSONArray()
                .put(new JSONObject()
                        .put("name", "DatasetSummary")
                        .put("pivotedValues", new JSONObject().put("Revenue", "1250")));

        String resolvedWidgetName = jsonObjectUtils.replacePlaceholderIfPresent("$V{DatasetSummary.Revenue}", new JSONObject(), variables);

        assertEquals("1250", resolvedWidgetName);
    }
}
