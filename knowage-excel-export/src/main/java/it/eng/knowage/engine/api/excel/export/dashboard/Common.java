package it.eng.knowage.engine.api.excel.export.dashboard;

import org.json.JSONException;
import org.json.JSONObject;

public class Common {

    protected boolean summaryRowsEnabled(JSONObject settings) {
        try {
            JSONObjectUtils jsonObjectUtils = new JSONObjectUtils();
            JSONObject configuration = jsonObjectUtils.getConfigurationFromSettings(settings);
            return configuration.has("summaryRows") && configuration.getJSONObject("summaryRows").getBoolean("enabled");
        } catch (JSONException e) {
            return false;
        }
    }

    protected boolean conditionIsApplicable(String valueToCompare, String operator, String comparisonValue) {
        return switch (operator) {
            case "==" -> {
                try {
                    yield Double.parseDouble(valueToCompare) == Double.parseDouble(comparisonValue);
                } catch (RuntimeException rte) {
                    yield valueToCompare.equals(comparisonValue);
                }
            }
            case "!=" -> {
                try {
                    yield Double.parseDouble(valueToCompare) != Double.parseDouble(comparisonValue);
                } catch (RuntimeException rte) {
                    yield valueToCompare.equals(comparisonValue);
                }
            }
            case ">" -> {
                try {
                    yield Double.parseDouble((valueToCompare)) > Double.parseDouble(comparisonValue);
                } catch (RuntimeException rte) {
                    yield false;
                }
            }
            case "<" -> {
                try {
                    yield Double.parseDouble(valueToCompare) < Double.parseDouble(comparisonValue);
                } catch (RuntimeException rte) {
                    yield false;
                }
            }
            case ">=" -> {
                try {
                    yield Double.parseDouble(valueToCompare) >= Double.parseDouble(comparisonValue);
                } catch (RuntimeException rte) {
                    yield false;
                }
            }
            case "<=" -> {
                try {
                    yield Double.parseDouble(valueToCompare) <= Double.parseDouble(comparisonValue);
                } catch (RuntimeException rte) {
                    yield false;
                }
            }
            case "IN" -> valueToCompare.contains(comparisonValue);
            default -> false;
        };
    }


}
