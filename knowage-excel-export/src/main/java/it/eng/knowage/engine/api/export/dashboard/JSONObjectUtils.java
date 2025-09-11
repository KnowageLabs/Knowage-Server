package it.eng.knowage.engine.api.export.dashboard;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectUtils {
    private static final Logger LOGGER = LogManager.getLogger(JSONObjectUtils.class);


    protected JSONObject getVisualizationFromSettings(JSONObject settings) {
        try {
            return settings.getJSONObject("visualization");
        } catch (Exception e) {
            LOGGER.error("Error while getting visualization from settings", e);
            return new JSONObject();
        }
    }

    protected JSONObject getConfigurationFromSettings(JSONObject settings) throws JSONException {
        if (settings.has("configuration")) {
            return settings.getJSONObject("configuration");
        } else {
            return new JSONObject();
        }
    }

    protected JSONObject getStyleFromSettings(JSONObject settings) {
        try {
            return settings.getJSONObject("style");
        } catch (Exception e) {
            LOGGER.error("Error while getting style from settings", e);
            return new JSONObject();
        }
    }

    public String getDashboardWidgetName(JSONObject widget) {
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

    public String replacePlaceholderIfPresent(String widgetName, JSONObject drivers) {
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
