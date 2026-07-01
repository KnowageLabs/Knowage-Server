package it.eng.knowage.engine.api.export.dashboard;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONObjectUtils {
    private static final Logger LOGGER = LogManager.getLogger(JSONObjectUtils.class);
    private static final String DRIVER_DESCRIPTION_SUFFIX = "_description";
    private static final Pattern PARAMETER_PLACEHOLDER_PATTERN = Pattern.compile("\\$P\\{([^}]+)}");
    private static final Pattern VARIABLE_PLACEHOLDER_PATTERN = Pattern.compile("\\$V\\{([^}]+)}");


    public JSONObject getVisualizationFromSettings(JSONObject settings) {
        try {
            return settings.getJSONObject("visualization");
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    public JSONObject getConfigurationFromSettings(JSONObject settings) throws JSONException {
        if (settings.has("configuration")) {
            return settings.getJSONObject("configuration");
        } else {
            return new JSONObject();
        }
    }

    public JSONObject getStyleFromSettings(JSONObject settings) {
        try {
            return settings.getJSONObject("style");
        } catch (Exception e) {
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
        return replacePlaceholderIfPresent(widgetName, drivers, null);
    }

    public String replacePlaceholderIfPresent(String widgetName, JSONObject drivers, JSONArray variables) {
        if (widgetName == null || drivers == null || !widgetName.contains("$P{")) {
            return replaceVariablePlaceholdersIfPresent(widgetName, variables);
        }

        Matcher placeholderMatcher = PARAMETER_PLACEHOLDER_PATTERN.matcher(widgetName);
        StringBuffer resolvedWidgetName = new StringBuffer();
        while (placeholderMatcher.find()) {
            String placeholder = placeholderMatcher.group(1);
            String replacement = resolveDriverPlaceholderValue(drivers, placeholder);
            if (replacement == null) {
                placeholderMatcher.appendReplacement(resolvedWidgetName, Matcher.quoteReplacement(placeholderMatcher.group(0)));
            } else {
                placeholderMatcher.appendReplacement(resolvedWidgetName, Matcher.quoteReplacement(replacement));
            }
        }
        placeholderMatcher.appendTail(resolvedWidgetName);
        return replaceVariablePlaceholdersIfPresent(resolvedWidgetName.toString(), variables);
    }

    private String resolveDriverPlaceholderValue(JSONObject drivers, String placeholder) {
        String driverKey = placeholder;
        boolean useDescription = false;
        if (placeholder.endsWith(DRIVER_DESCRIPTION_SUFFIX)) {
            driverKey = placeholder.substring(0, placeholder.length() - DRIVER_DESCRIPTION_SUFFIX.length());
            useDescription = true;
        }

        if (!drivers.has(driverKey)) {
            return null;
        }

        try {
            JSONArray valuesArray = drivers.getJSONArray(driverKey);
            if (valuesArray.length() == 0) {
                return null;
            }

            JSONObject value = valuesArray.getJSONObject(0);
            String resolvedValue = useDescription ? value.optString("description") : value.optString("value");
            if (resolvedValue.isEmpty() && useDescription) {
                resolvedValue = value.optString("value");
            }
            return resolvedValue.isEmpty() ? null : resolvedValue;
        } catch (JSONException e) {
            throw new SpagoBIRuntimeException("Unable to replace placeholder in widget name", e);
        }
    }

    private String replaceVariablePlaceholdersIfPresent(String widgetName, JSONArray variables) {
        if (widgetName == null || variables == null || !widgetName.contains("$V{")) {
            return widgetName;
        }

        Matcher placeholderMatcher = VARIABLE_PLACEHOLDER_PATTERN.matcher(widgetName);
        StringBuffer resolvedWidgetName = new StringBuffer();
        while (placeholderMatcher.find()) {
            String placeholder = placeholderMatcher.group(1);
            String replacement = resolveVariablePlaceholderValue(variables, placeholder);
            if (replacement == null) {
                placeholderMatcher.appendReplacement(resolvedWidgetName, Matcher.quoteReplacement(placeholderMatcher.group(0)));
            } else {
                placeholderMatcher.appendReplacement(resolvedWidgetName, Matcher.quoteReplacement(replacement));
            }
        }
        placeholderMatcher.appendTail(resolvedWidgetName);
        return resolvedWidgetName.toString();
    }

    private String resolveVariablePlaceholderValue(JSONArray variables, String placeholder) {
        for (int i = 0; i < variables.length(); i++) {
            JSONObject variable = variables.optJSONObject(i);
            if (variable == null) {
                continue;
            }

            String variableName = variable.optString("name");
            if (variableName.equals(placeholder)) {
                String variableValue = variable.optString("value");
                return variableValue.isEmpty() ? null : variableValue;
            }

            String pivotSeparator = ".";
            if (placeholder.startsWith(variableName + pivotSeparator)) {
                JSONObject pivotedValues = variable.optJSONObject("pivotedValues");
                if (pivotedValues == null) {
                    continue;
                }

                String variableKey = placeholder.substring(variableName.length() + pivotSeparator.length());
                String variableValue = pivotedValues.optString(variableKey);
                return variableValue.isEmpty() ? null : variableValue;
            }
        }

        return null;
    }
}
