package it.eng.knowage.engine.api.excel.export.dashboard;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

}
