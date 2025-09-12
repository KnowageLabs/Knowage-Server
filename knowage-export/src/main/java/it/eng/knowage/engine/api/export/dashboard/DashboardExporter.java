package it.eng.knowage.engine.api.export.dashboard;

import it.eng.knowage.engine.api.export.ExporterClient;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDashboardThemeDAO;
import it.eng.spagobi.commons.metadata.SbiDashboardTheme;
import it.eng.spagobi.i18n.dao.I18NMessagesDAO;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

@Getter
public class DashboardExporter {

    private final JSONObjectUtils jsonObjectUtils;
    private final String userUniqueIdentifier;
    protected Map<String, String> i18nMessages;
    protected Locale locale;
    private static final String STATIC_CUSTOM_STYLE = "static";
    private static final String CONDITIONAL_STYLE = "conditional";
    private static final String ALL_COLUMNS_STYLE = "all";
    private static final String SORTING_OBJ = "sortingObj";
    private static final String DRILL_SORTING_OBJ = "drillSortingObj";
    private static final String BOTH = "both";

    public DashboardExporter(String userUniqueIdentifier) {
        this.jsonObjectUtils = new JSONObjectUtils();
        this.userUniqueIdentifier = userUniqueIdentifier;
    }

    private static final Logger LOGGER = LogManager.getLogger(DashboardExporter.class);

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


    public JSONArray getDrivers(JSONObject body) {
        try {
            return getDriversFromBody(body);
        } catch (JSONException e) {
            LOGGER.error("Cannot get drivers from body", e);
            throw new SpagoBIRuntimeException("Cannot get drivers from body", e);
        }
    }

    public JSONArray getDriversFromBody(JSONObject body) throws JSONException {
        if (body.has("drivers")) {
            return body.getJSONArray("drivers");
        } else {
            return new JSONArray();
        }
    }

    public JSONObject transformDriversForDatastore(JSONArray driversFromBody) {

        JSONObject drivers = new JSONObject();

        if (driversFromBody != null && driversFromBody.length() > 0) {
            for (int i = 0; i < driversFromBody.length(); i++) {
                try {
                    JSONObject driver = driversFromBody.getJSONObject(i);
                    String urlName = driver.getString("urlName");
                    JSONArray values = new JSONArray();
                    JSONObject value = new JSONObject();
                    value.put("value", driver.getString("value"));
                    value.put("description", driver.getString("description"));
                    values.put(value);
                    drivers.put(urlName, values);
                }
                catch (JSONException e) {
                    try {
                        getDatasetDrivers(driversFromBody, i, drivers);
                    } catch (JSONException e1) {
                        LOGGER.error("Unable to transform driver", e1);
                        throw new SpagoBIRuntimeException("Unable to transform driver", e1);
                    }
                }
            }
        }

        return drivers;
    }


    private static void getDatasetDrivers(JSONArray driversFromBody, int i, JSONObject drivers) throws JSONException {
        JSONObject driver = driversFromBody.getJSONObject(i);
        String urlName = driver.getString("urlName");
        JSONArray values = new JSONArray();
        JSONArray parameterValues = driver.getJSONArray("parameterValue");
        if (parameterValues != null && parameterValues.length() > 0) {
            for (int j = 0; j < parameterValues.length(); j++) {
                JSONObject parameterValue = parameterValues.getJSONObject(j);
                JSONObject value = new JSONObject();
                value.put("value", parameterValue.getString("value"));
                value.put("description", parameterValue.getString("description"));
                values.put(value);
            }
        }
        drivers.put(urlName, values);
    }

    protected Map<String, Map<String, JSONArray>> getSelections(JSONObject body) {
        try {
            Map<String, Map<String, JSONArray>> selectionsToReturn = new HashMap<>();
            if (body.has("selections") && body.getJSONArray("selections").length() > 0) {
                for (int i = 0; i < body.getJSONArray("selections").length(); i++) {
                    JSONObject selection = body.getJSONArray("selections").getJSONObject(i);
                    if (!selectionsToReturn.containsKey(selection.getString("datasetLabel"))) {
                        selectionsToReturn.put(selection.getString("datasetLabel"), new HashMap<>());
                    }
                    if (!selectionsToReturn.get(selection.getString("datasetLabel")).containsKey(selection.getString("columnName"))) {
                        selectionsToReturn.get(selection.getString("datasetLabel")).put(selection.getString("columnName"), new JSONArray());
                    }
                    loopOverSelectionValues(selection, selectionsToReturn);
                }
            }
            return selectionsToReturn;
        } catch (JSONException e) {
            return Collections.emptyMap();
        }
    }

    private void loopOverSelectionValues(JSONObject selection, Map<String, Map<String, JSONArray>> selectionsToReturn) throws JSONException {
        for (int j = 0; j < selection.getJSONArray("value").length(); j++) {
            String valueToInsert = "('" + selection.getJSONArray("value").getString(j) + "')";
            selectionsToReturn.get(selection.getString("datasetLabel")).get(selection.getString("columnName")).put(valueToInsert);
        }
    }

    public JSONArray getParametersFromBody(JSONObject body) {
        try {
            JSONArray parametersToReturn = new JSONArray();
            JSONArray datasets = body.getJSONObject("configuration").getJSONArray("datasets");
            if (datasets.length() > 0) {
                for (int i = 0; i < datasets.length(); i++) {
                    JSONObject dataset = datasets.getJSONObject(i);
                    if (dataset.has("parameters")) {
                        JSONArray parameters = dataset.getJSONArray("parameters");
                        if (parameters.length() > 0) {
                            for (int j = 0; j < parameters.length(); j++) {
                                JSONObject parameter = parameters.getJSONObject(j);
                                parameter.put("dataset", dataset.getInt("id"));
                                getActualValueFromDriverPlaceholder(body, parametersToReturn, parameter);
                            }
                        }
                    }
                }
            }
            return parametersToReturn;
        } catch (JSONException e) {
            return getParametersFromSingleWidgetBody(body);
        }
    }

    private JSONArray getParametersFromSingleWidgetBody(JSONObject body) {
        try {
            JSONArray parametersToReturn = new JSONArray();
            if (body.has("parameters")) {
                JSONArray parameters = body.getJSONArray("parameters");
                if (parameters.length() > 0) {
                    for (int i = 0; i < parameters.length(); i++) {
                        JSONObject parameter = parameters.getJSONObject(i);
                        parameter.put("dataset", body.getInt("dataset"));
                        getActualValueFromDriverPlaceholder(body, parametersToReturn, parameter);
                    }
                }
            }
            return parametersToReturn;
        } catch (JSONException e) {
            LOGGER.error("Cannot get parameters from body", e);
            throw new SpagoBIRuntimeException("Cannot get parameters from body", e);
        }
    }


    private void getActualValueFromDriverPlaceholder(JSONObject body, JSONArray parametersToReturn, JSONObject parameter) throws JSONException {
        if (parameter.getString("value").contains("$P{") || parameter.getString("value").contains("${")) {
            String placeholderToReplace = parameter.getString("value")
                    .replace("$P{", "")
                    .replace("${", "")
                    .replace("}", "");
            String actualValue = replaceParameterPlaceholderWithActualValue(placeholderToReplace, getDriversFromBody(body));
            parameter.put("value", actualValue);
        }
        parametersToReturn.put(parameter);
    }

    private String replaceParameterPlaceholderWithActualValue(String placeholderToReplace, JSONArray driversFromBody) {
        String actualValue = null;
        try {
            for (int i = 0; i < driversFromBody.length(); i++) {
                JSONObject driver = driversFromBody.getJSONObject(i);
                if (driver.getString("urlName").equals(placeholderToReplace)) {
                    actualValue = driver.getString("value");
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return actualValue;
    }

    public JSONObject transformParametersForDatastore(JSONObject body, JSONArray parameters) throws JSONException {
        JSONObject parametersToSend = new JSONObject();
        if (parameters != null && parameters.length() > 0) {
            for (int i = 0; i < parameters.length(); i++) {
                JSONObject parameter = parameters.getJSONObject(i);
                if (parameter.getInt("dataset") == body.getInt("dataset")) {
                    parametersToSend.put(parameter.getString("name"), parameter.get("value"));
                }
            }
        }
        return parametersToSend;
    }

    protected JSONArray filterDataStoreColumns(JSONArray columns) {
        try {
            for (int i = 0; i < columns.length(); i++) {
                String element = columns.getString(i);
                if (element != null && element.equals("recNo")) {
                    columns.remove(i);
                    break;
                }
            }
        } catch (JSONException e) {
            LOGGER.error("Can not filter Columns Array");
        }
        return columns;
    }

    protected List<String> getDashboardHiddenColumnsList(JSONObject settings, String conditionToEvaluate) {
        try {
            List<String> hiddenColumns = new ArrayList<>();
            if (areVisibilityConditionsEnabled(settings)) {
                JSONObject visualization = settings.getJSONObject("visualization");
                JSONObject visibilityConditions = visualization.getJSONObject("visibilityConditions");
                JSONArray conditions = visibilityConditions.getJSONArray("conditions");

                for (int i = 0; i < conditions.length(); i++) {
                    JSONObject condition = conditions.getJSONObject(i);
                    if (columnMustBeHidden(condition, conditionToEvaluate)) {
                        JSONArray target;
                        try {
                            target = condition.getJSONArray("target");
                        } catch (JSONException e) {
                            target = new JSONArray();
                            target.put(condition.getString("target"));
                        }
                        for (int j = 0; j < target.length(); j++) {
                            String targetColumn = target.getString(j);
                            hiddenColumns.add(targetColumn);
                        }
                    }
                }
            }
            return hiddenColumns;
        } catch (JSONException je) {
            LOGGER.error("Error while getting hidden columns list", je);
            return new ArrayList<>();
        }
    }

    private boolean columnMustBeHidden(JSONObject condition, String conditionToEvaluate) {
        try {
            JSONObject conditionDefinition = condition.getJSONObject("condition");


            return  (conditionDefinition.getString("type").equals("always") &&
                    condition.getBoolean(conditionToEvaluate))
                    ||
                    (conditionDefinition.getString("type").equals("variable") &&
                            condition.getBoolean(conditionToEvaluate) && conditionIsApplicable(conditionDefinition.getString("variableValue"), conditionDefinition.getString("operator"), conditionDefinition.getString("value")));

        } catch (JSONException jsonException) {
            LOGGER.error("Error while evaluating if column must be hidden according to variable.", jsonException);
            return false;
        }
    }
    private boolean areVisibilityConditionsEnabled(JSONObject settings) throws JSONException {
        JSONObject visualization = getJsonObjectUtils().getVisualizationFromSettings(settings);
        return settings.has("visualization") &&
                visualization.has("visibilityConditions") &&
                visualization.getJSONObject("visibilityConditions").getBoolean("enabled") &&
                visualization.getJSONObject("visibilityConditions").has("conditions");
    }

    protected JSONArray getDashboardTableOrderedColumns(JSONArray columnsNew, List<String> hiddenColumns, JSONArray columnsOld) {
        JSONArray columnsOrdered = new JSONArray();
        // new columns are in the correct order
        // for each of them we have to find the correspondent old column and push it into columnsOrdered
        try {
            for (int i = 0; i < columnsNew.length(); i++) {

                JSONObject columnNew = columnsNew.getJSONObject(i);

                if (hiddenColumns.contains(columnNew.getString("id"))) {
                    continue;
                }

                for (int j = 0; j < columnsOld.length(); j++) {
                    JSONObject columnOld = columnsOld.getJSONObject(j);

                    if (columnOld.getString("header").equals(columnNew.getString("alias"))) {
                        columnOld.put("id", columnNew.getString("id"));

                        if (columnNew.has("ranges")) {
                            JSONArray ranges = columnNew.getJSONArray("ranges");
                            columnOld.put("ranges", ranges); // added ranges for column thresholds
                        }

                        columnsOrdered.put(columnOld);
                        break;
                    }
                }
            }
            return columnsOrdered;
        } catch (Exception e) {
            LOGGER.error("Error retrieving ordered columns");
            return new JSONArray();
        }
    }


    protected final JSONArray getGroupsFromDashboardWidget(JSONObject settings) throws JSONException {
        // column.header matches with name or alias
        // Fill Header
        JSONArray groupsArray = new JSONArray();
        JSONObject configuration = getJsonObjectUtils().getConfigurationFromSettings(settings);
        if (configuration.has("columnGroups") && configuration.getJSONObject("columnGroups").getBoolean("enabled")) {
            groupsArray = configuration.getJSONObject("columnGroups").getJSONArray("groups");
        }
        return groupsArray;
    }


    protected final Map<String, String> getDashboardGroupAndColumnsMap(JSONObject widgetContent, JSONArray groupsArray) {
        Map<String, String> mapGroupsAndColumns = new HashMap<>();
        try {
            if (widgetContent.get("columns") instanceof JSONArray)
                mapGroupsAndColumns = getMapFromDashboardGroupsArray(groupsArray,
                        widgetContent.getJSONArray("columns"));
        } catch (JSONException e) {
            LOGGER.error("Couldn't retrieve groups", e);
        }
        return mapGroupsAndColumns;
    }

    protected Map<String, String> getMapFromDashboardGroupsArray(JSONArray groupsArray, JSONArray aggr) {
        Map<String, String> returnMap = new HashMap<>();
        try {
            if (aggr != null && groupsArray != null) {

                for (int i = 0; i < groupsArray.length(); i++) {

                    String groupName = groupsArray.getJSONObject(i).getString("label");
                    JSONArray columns = groupsArray.getJSONObject(i).getJSONArray("columns");

                    for (int ii = 0; ii < aggr.length(); ii++) {
                        JSONObject column = aggr.getJSONObject(ii);

                        if (columns.toString().contains(column.getString("id"))) {
                            String nameToInsert = column.getString("alias");
                            returnMap.put(nameToInsert, groupName);
                        }

                    }
                }
            }
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Couldn't create map from groups array", e);
        }
        return returnMap;
    }

    protected void replaceWithThemeSettingsIfPresent(JSONObject settings) throws JSONException {
        IDashboardThemeDAO dao = DAOFactory.getDashboardThemeDAO();
        Optional<SbiDashboardTheme> optionalTheme = dao.readByThemeName(settings.getJSONObject("style").optString("themeName"));
        if (optionalTheme.isPresent()) {
            SbiDashboardTheme dashboardTheme = optionalTheme.get();
            settings.remove("style");
            settings.put("style", dashboardTheme.getConfig().getJSONObject("table").getJSONObject("style"));
        }
    }

    protected int doSummaryRowsLogic(JSONObject settings, int numberOfSummaryRows, List<String> summaryRowsLabels) throws JSONException {
        if (summaryRowsEnabled(settings)) {
            JSONArray list = settings.getJSONObject("configuration").getJSONObject("summaryRows").getJSONArray("list");
            numberOfSummaryRows = list.length();

            for (int i = 0; i < numberOfSummaryRows; i++) {
                summaryRowsLabels.add(list.getJSONObject(i).getString("label"));
            }

        }
        return numberOfSummaryRows;
    }

    protected String getInternationalizedHeader(String columnName) {
        if (i18nMessages == null) {
            I18NMessagesDAO messageDao = DAOFactory.getI18NMessageDAO();
            try {
                i18nMessages = messageDao.getAllI18NMessages(locale);
            } catch (Exception e) {
                LOGGER.error("Error while getting i18n messages", e);
                i18nMessages = new HashMap<>();
            }
        }
        return i18nMessages.getOrDefault(columnName, columnName);
    }

    protected Locale getLocaleFromBody(JSONObject body) {
        try {
            String language = body.getString(SpagoBIConstants.SBI_LANGUAGE);
            String country = body.getString(SpagoBIConstants.SBI_COUNTRY);
            return new Locale(language, country);
        } catch (Exception e) {
            LOGGER.warn("Cannot get locale information from input parameters body", e);
            return Locale.ENGLISH;
        }

    }


    protected String replaceWithCustomHeaderIfPresent(JSONObject settings, String columnName, JSONObject column) throws JSONException {
        if (settings.has("configuration") && settings.getJSONObject("configuration").has("headers")) {
            JSONObject customHeader = settings.getJSONObject("configuration").getJSONObject("headers").getJSONObject("custom");
            if (customHeader.getBoolean("enabled")) {
                JSONArray rules = customHeader.getJSONArray("rules");
                for (int i = 0; i < rules.length(); i++) {
                    JSONObject rule = rules.getJSONObject(i);
                    JSONArray target = rule.getJSONArray("target");
                    if (target.length() > 0) {
                        for (int j = 0; j < target.length(); j++) {
                            if (target.getString(j).equals(column.getString("id"))) {
                                if (rule.getString("action").equals("setLabel")) {
                                    columnName = rule.getString("value");
                                    break;
                                } else if (rule.getString("action").equals("hide")) {
                                    columnName = "";
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return columnName;
    }

    protected boolean isSummaryColumnVisible(List<String> columnsToHide, JSONObject column) {
        try {
            return !columnsToHide.contains(column.getString("id"));
        } catch (JSONException e) {
            throw new SpagoBIRuntimeException(e);
        }
    }

    /**
     * -------------------------------------------------------------------------------------------------------------------
     * START OF STYLE METHODS
     */

    protected JSONObject getRowStyle(JSONObject settings) {
        JSONObject style = jsonObjectUtils.getStyleFromSettings(settings);
        if (style != null && style.has("rows")) {
            JSONObject rows = style.optJSONObject("rows");
            return rows.optJSONObject("alternatedRows");
        }
        return null;
    }

    protected String getDefaultRowBackgroundColor(JSONObject altenatedRows, boolean rowIsEven) {
        try {
            if (altenatedRows != null && altenatedRows.getBoolean("enabled")) {
                if (rowIsEven) {
                    return altenatedRows.optString("evenBackgroundColor");
                } else {
                    return altenatedRows.optString("oddBackgroundColor");
                }
            }
        } catch (JSONException e) {
            LOGGER.error("Error while getting current row background color", e);
        }
        return "";
    }

    protected boolean columnAlreadyHasTheRightStyle(String styleKey, Map<String, CellStyle> stylesMap) {
        return stylesMap.containsKey(styleKey);
    }

    protected boolean styleCanBeOverridden(JSONObject theRightStyle) throws JSONException {
        return (theRightStyle.has("type") && theRightStyle.getString("type").equals(STATIC_CUSTOM_STYLE)) || !theRightStyle.has("type");
    }

    protected String getStyleKey(JSONObject column, JSONObject theRightStyle, String rawCurrentNumberType) throws JSONException {
        return column.optString("id").concat(theRightStyle.getString("type").concat(theRightStyle.getString("styleIndex").concat(rawCurrentNumberType)));
    }

    protected Map<String, JSONArray> getStylesMap(JSONObject settings) {
        Map<String, JSONArray> stylesMap = new HashMap<>();
        try {
            JSONObject columns = jsonObjectUtils.getStyleFromSettings(settings).getJSONObject("columns");

            if (columns.getBoolean("enabled")) {
                JSONArray styles = columns.getJSONArray("styles");
                buildStylesMap(stylesMap, styles);
            }

            if (settings.has("conditionalStyles") && settings.getJSONObject("conditionalStyles").getBoolean("enabled")) {
                JSONObject conditionalStyles = settings.getJSONObject("conditionalStyles");
                JSONArray conditions = conditionalStyles.getJSONArray("conditions");

                buildStylesMap(stylesMap, conditions);
            }

        } catch (JSONException e) {
            LOGGER.debug("No styles found in settings", e);
            return stylesMap;
        }
        return stylesMap;
    }

    private void buildStylesMap(Map<String, JSONArray> stylesMap, JSONArray styles) throws JSONException {

        if (stylesMap == null) {
            stylesMap = new HashMap<>();
        }
        for (int i = 0; i < styles.length(); i++) {
            JSONObject style = styles.getJSONObject(i);
            JSONObject props = new JSONObject();
            if (style.has("condition")) {
                JSONObject condition = style.getJSONObject("condition");
                boolean applyToWholeRow = style.getBoolean("applyToWholeRow");
                props.put("applyToWholeRow", applyToWholeRow);
                props.put("condition", condition);
            }
            props.put("properties", style.getJSONObject("properties"));

            JSONArray target = getTarget(styles, i);

            for (int j = 0; j < target.length(); j++) {
                if (stylesMap.containsKey(target.getString(j))) {
                    stylesMap.get(target.getString(j)).put(props);
                } else {
                    JSONArray propertiesArray = new JSONArray();
                    propertiesArray.put(props);
                    stylesMap.put(target.getString(j), propertiesArray);
                }
            }
        }
    }

    private JSONArray getTarget(JSONArray styles, int i) throws JSONException {
        JSONArray target;
        try {
            target = styles.getJSONObject(i).getJSONArray("target");
        } catch (JSONException e) {
            target = new JSONArray();
            target.put(styles.getJSONObject(i).getString("target"));
        }
        return target;
    }

    protected JSONObject getTheRightStyleByColumnIdAndValue(Map<String, JSONArray> styles, String stringifiedValue, String columnId, String currentAlternateRowColor) throws JSONException {
        JSONObject customStyle = getTheStyleByValueAndColumnId(styles, stringifiedValue, columnId, currentAlternateRowColor);

        if (customStyle.has("properties") && customStyle.getJSONObject("properties").length() == 0) {
            return getTheStyleByValueAndColumnId(styles, stringifiedValue, ALL_COLUMNS_STYLE, currentAlternateRowColor);
        }
        return customStyle;
    }

    private JSONObject getTheStyleByValueAndColumnId(Map<String, JSONArray> styles, String stringifiedValue, String columnId, String currentAlternateRowColor) throws JSONException {
        try {
            JSONObject nonConditionalProps = new JSONObject();
            if (styles != null) {
                JSONArray columnStyles = styles.get(columnId);

                if (columnStyles == null) {
                    if (styles.get(ALL_COLUMNS_STYLE) == null) {
                        return getStyleObject(nonConditionalProps, STATIC_CUSTOM_STYLE, 0, false);
                    } else {
                        columnStyles = styles.get(ALL_COLUMNS_STYLE);
                        if (currentAlternateRowColor != null) {
                            for (int i = 0; i < columnStyles.length(); i++) {
                                JSONObject style = columnStyles.getJSONObject(i);
                                style.optJSONObject("properties").put("background-color", currentAlternateRowColor);
                            }
                        }
                    }
                }

                for (int i = 0; i < columnStyles.length(); i++) {
                    JSONObject style = columnStyles.getJSONObject(i);
                    JSONObject condition = style.optJSONObject("condition");
                    if (style.has("condition") && conditionIsApplicable(stringifiedValue, condition.optString("operator"), condition.getString("value"))) {
                        return getStyleObject(style.getJSONObject("properties"), CONDITIONAL_STYLE, i, style.getBoolean("applyToWholeRow"));
                    } else if (!style.has("condition")) {
                        nonConditionalProps = style.getJSONObject("properties");
                    }
                }
            } else {
                return getStyleObject(nonConditionalProps, STATIC_CUSTOM_STYLE, 0, false);
            }
            return getStyleObject(nonConditionalProps, STATIC_CUSTOM_STYLE, 0, false);
        } catch (JSONException e) {
            LOGGER.error("Error while checking if conditional style applies", e);
            throw new SpagoBIRuntimeException("Error while checking if conditional style applies", e);
        }
    }

    private JSONObject getStyleObject(JSONObject properties, String type, int styleIndex, boolean applyToWholeRow) {
        try {
            JSONObject style = new JSONObject();
            style.put("properties", properties);
            style.put("type", type);
            style.put("styleIndex", styleIndex);

            if (style.get("type").equals(CONDITIONAL_STYLE)) {
                style.put("applyToWholeRow", applyToWholeRow);
            }

            return style;
        } catch (JSONException e) {
            LOGGER.error("Error while building default non conditional style", e);
            throw new SpagoBIRuntimeException("Error while building default non conditional style", e);
        }
    }

    protected Style getStyleCustomObjFromProps(Sheet sheet, JSONObject props, String defaultRowBackgroundColor) {
        Style style = new Style();
        style.setSheet(sheet);
        props = props.optJSONObject("properties") == null ? props : props.optJSONObject("properties");

        style.setAlignItems(props.optString("align-items"));
        style.setJustifyContent(props.optString("justify-content"));
        style.setBackgroundColor(props.optString("background-color").isEmpty() ? defaultRowBackgroundColor : props.optString("background-color"));
        style.setColor(props.optString("color"));
        style.setFontSize(props.optString("font-size"));
        style.setFontWeight(props.optString("font-weight"));
        style.setFontStyle(props.optString("font-style"));

        return style;
    }

    protected HorizontalAlignment getHorizontalAlignment(String alignItem) {
        return switch (alignItem) {
            case "CENTER" -> HorizontalAlignment.CENTER;
            case "FLEX-END" -> HorizontalAlignment.RIGHT;
            default -> HorizontalAlignment.LEFT;
        };
    }

    protected VerticalAlignment getVerticalAlignment(String justifyContent) {
        return switch (justifyContent) {
            case "CENTER" -> VerticalAlignment.CENTER;
            case "FLEX-END" -> VerticalAlignment.BOTTOM;
            default -> VerticalAlignment.TOP;
        };
    }

    protected String getOnlyTheNumericValueFromString(String string) {
        return string.replaceAll("[^0-9]", "");
    }

    protected boolean stringIsEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public boolean styleIsEmpty(Style style) {
        return stringIsEmpty(style.getBackgroundColor()) &&
                stringIsEmpty(style.getColor()) &&
                stringIsEmpty(style.getFontSize()) &&
                stringIsEmpty(style.getFontFamily()) &&
                stringIsEmpty(style.getFontWeight()) &&
                stringIsEmpty(style.getFontStyle()) &&
                stringIsEmpty(style.getAlignItems()) &&
                stringIsEmpty(style.getJustifyContent());
    }

    /*
     * END OF STYLE METHODS
     * -------------------------------------------------------------------------------------------------------------------
     * START OF DATASTORE METHODS
     */

    protected JSONObject getDatastore(String datasetLabel, Map<String, Object> parameters, String selections, int offset,
                                      int fetchSize) {
        ExporterClient client = new ExporterClient();
        try {
            LOGGER.info("calling the datastore with userUniqueIdentifier: {}", userUniqueIdentifier);
            return client.getDataStore(parameters, datasetLabel, userUniqueIdentifier, selections, offset, fetchSize);
        } catch (Exception e) {
            String message = "Unable to get data";
            LOGGER.error(message, e);
            throw new SpagoBIRuntimeException(message);
        }
    }


    public JSONObject getDataStoreforDashboardSingleWidget(JSONObject singleWidget, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, JSONObject parameters) {
        return getDataStoreForDashboardWidget(singleWidget, 0, -1, selections, drivers, parameters);
    }


    public JSONObject getDataStoreForDashboardWidget(JSONObject widget, int offset, int fetchSize, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, JSONObject parameters) {
        Map<String, Object> map = new HashMap<>();
        JSONObject datastore;
        try {
            Integer datasetId = Integer.valueOf(widget.optString("dataset"));
            IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetById(datasetId);
            String datasetLabel = dataset.getLabel();

            JSONObject dashboardSelections;
            if (widget.getString("type").equalsIgnoreCase("static-pivot-table")) {
                dashboardSelections = getPivotAggregations(widget, datasetLabel);
            } else {
                dashboardSelections = getDashboardAggregations(widget, datasetLabel);
            }

            dashboardSelections.put("selections", selections);
            dashboardSelections.put("parameters", parameters);
            dashboardSelections.put("drivers", drivers);

            if (isSolrDataset(dataset) && !widget.getString("type").equalsIgnoreCase("discovery")) {
                JSONObject jsOptions = new JSONObject();
                jsOptions.put("solrFacetPivot", true);
                dashboardSelections.put("options", jsOptions);
            }

            if (isSolrDataset(dataset) && widget.getString("type").equalsIgnoreCase("discovery")) {
                buildLikeSelections(dashboardSelections, widget);
            }

            datastore = getDatastore(datasetLabel, map, dashboardSelections.toString(), offset, fetchSize);
            datastore.put("widgetData", widget);

        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Error getting datastore for widget [type=" + widget.optString("type")
                    + "] [id=" + widget.optLong("id") + "]", e);
        }
        return datastore;
    }

    protected boolean isSolrDataset(IDataSet dataSet) {
        if (dataSet instanceof VersionedDataSet versionedDataSet) {
            dataSet = versionedDataSet.getWrappedDataset();
        }
        return dataSet instanceof SolrDataSet;
    }

    private void buildLikeSelections(JSONObject dashboardSelections, JSONObject widget) {
        try {
            JSONObject likeSelections = new JSONObject();
            JSONObject solrObject = new JSONObject();
            JSONObject settings = widget.optJSONObject("settings");
            JSONObject search = settings.optJSONObject("search");
            JSONArray columns = search.optJSONArray("columns");
            StringBuilder key = new StringBuilder();
            if (!search.optString("searchWord").isEmpty()) {
                for (int i = 0; i < columns.length(); i++) {
                    if (i == columns.length() - 1) {
                        key.append(columns.optString(i));
                    } else {
                        key.append(columns.optString(i)).append(",");
                    }
                }
                solrObject.put(key.toString(), search.optString("searchWord"));
            }
            likeSelections.put("solr", solrObject);
            dashboardSelections.put("likeSelections", likeSelections);
        } catch (Exception e) {
            LOGGER.error("Error while building like selections", e);
            throw new SpagoBIRuntimeException("Error while building like selections", e);
        }
    }

    protected JSONObject getDashboardAggregations(JSONObject widget, String datasetLabel) {
        JSONObject dashboardSelections;
        try {
            JSONArray columns = widget.getJSONArray("columns");
            JSONObject settings = widget.getJSONObject("settings");

            dashboardSelections = buildDashboardSelections(columns, datasetLabel, settings);
        } catch (Exception e) {
            LOGGER.error("Cannot get dashboard selections", e);
            throw new SpagoBIRuntimeException("Cannot get dashboard selections", e);
        }
        return dashboardSelections;
    }

    protected JSONObject getPivotAggregations(JSONObject widget, String datasetLabel) {
        JSONObject pivotSelections;
        try {
            JSONObject fields = widget.getJSONObject("fields");

            pivotSelections = buildPivotSelections(fields, datasetLabel);
        } catch (Exception e) {
            LOGGER.error("Cannot get pivot selections", e);
            throw new SpagoBIRuntimeException("Cannot get pivot selections", e);
        }
        return pivotSelections;
    }

    private JSONObject buildDashboardSelections(JSONArray columns, String datasetLabel, JSONObject settings) {
        JSONObject selections = new JSONObject();
        String sortingColumnId = settings.optString("sortingColumn");
        String sortingOrder = settings.optString("sortingOrder");

        try {
            selections.put("aggregations", new JSONObject());
            JSONObject aggregations = selections.getJSONObject("aggregations");
            aggregations.put("measures", new JSONArray());
            JSONArray measures = aggregations.getJSONArray("measures");
            aggregations.put("categories", new JSONArray());
            JSONArray categories = aggregations.getJSONArray("categories");

            buildDatastoreSummaryRowObject(selections, columns, settings);

            for (int i = 0; i < columns.length(); i++) {
                JSONObject column = columns.getJSONObject(i);
                String orderTypeCol = column.optString("orderType");
                if (sortingColumnId.isEmpty() && !orderTypeCol.isEmpty()) {
                    sortingColumnId = column.getString("id");
                    sortingOrder = column.getString("orderType");
                }
                if (column.getString("fieldType").equalsIgnoreCase("measure")) {
                    JSONObject measure = getMeasure(columns.getJSONObject(i));
                    measures.put(measure);
                } else {
                    JSONObject category = getCategory(columns.getJSONObject(i), getSortingObj(column, sortingColumnId, sortingOrder), getDrillSortingObj(column));
                    categories.put(category);
                }

            }
            aggregations.put("dataset", datasetLabel);
        } catch (Exception e) {
            LOGGER.error("Cannot build dashboard selections", e);
            throw new SpagoBIRuntimeException("Cannot build dashboard selections", e);
        }
        return selections;
    }

    private void buildDatastoreSummaryRowObject(JSONObject selections, JSONArray columns, JSONObject settings) throws JSONException {
        if (summaryRowsEnabled(settings)) {
            selections.put("summaryRow", new JSONArray());
            JSONObjectUtils utils = new JSONObjectUtils();
            JSONArray list = utils.getConfigurationFromSettings(settings).getJSONObject("summaryRows").getJSONArray("list");
            selections.put("summaryRow", new JSONArray());
            JSONArray summaryRows = selections.getJSONArray("summaryRow");
            for (int i = 0; i < list.length(); i++) {
                JSONObject summaryRow = new JSONObject();
                JSONObject listElement = list.getJSONObject(i);

                loopOverSummaryColumns(columns, summaryRow, listElement);
                summaryRows.put(summaryRow);
            }
        }


    }

    private JSONObject getDrillSortingObj(JSONObject column) {
        return column.optJSONObject("drillOrder");
    }

    private JSONObject buildPivotSelections(JSONObject fields, String datasetLabel) {
        JSONObject selections = new JSONObject();
        try {
            selections.put("aggregations", new JSONObject());
            JSONObject aggregations = selections.getJSONObject("aggregations");
            aggregations.put("measures", new JSONArray());
            JSONArray measures = aggregations.getJSONArray("measures");
            aggregations.put("categories", new JSONArray());
            JSONArray categories = aggregations.getJSONArray("categories");

            JSONArray columns = fields.getJSONArray("columns");
            JSONArray data = fields.getJSONArray("data");
            JSONArray rows = fields.getJSONArray("rows");
            JSONArray filters = fields.getJSONArray("filters");

            String sortingColumnId = "";
            String sortingOrder = "";
            for (int i = 0; i < columns.length(); i++) {
                JSONObject column = columns.getJSONObject(i);
                String sort = column.optString("sort");
                if (!sort.isEmpty()) {
                    sortingColumnId = column.getString("id");
                    sortingOrder = sort;
                }
                addToCategoriesOrMeasuresArray(column, categories, sortingColumnId, sortingOrder, measures);
            }
            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                addToCategoriesOrMeasuresArray(row, categories, sortingColumnId, sortingOrder, measures);
            }
            for (int i = 0; i < data.length(); i++) {
                JSONObject datum = getMeasure(data.getJSONObject(i));
                measures.put(datum);
            }
            for (int i = 0; i < filters.length(); i++) {
                JSONObject filter = filters.getJSONObject(i);
                addToCategoriesOrMeasuresArray(filter, categories, sortingColumnId, sortingOrder, measures);
            }
            aggregations.put("dataset", datasetLabel);
        } catch (Exception e) {
            LOGGER.error("Cannot build dashboard selections", e);
            throw new SpagoBIRuntimeException("Cannot build dashboard selections", e);
        }
        return selections;

    }

    private static void loopOverSummaryColumns(JSONArray columns, JSONObject summaryRow, JSONObject listElement) throws JSONException {
        JSONArray measures;
        for (int j = 0; j < columns.length(); j++) {
            JSONObject column = columns.getJSONObject(j);
            if (column.getString("fieldType").equalsIgnoreCase("measure")) {
                if (summaryRow.has("measures")) {
                    measures = summaryRow.getJSONArray("measures");
                    buildSummaryMeasure(column, listElement, measures);
                } else {
                    summaryRow.put("measures", new JSONArray());
                    measures = summaryRow.getJSONArray("measures");
                    buildSummaryMeasure(column, listElement, measures);
                }
            }
        }
    }

    private void addToCategoriesOrMeasuresArray(JSONObject column, JSONArray categories, String sortingColumnId, String sortingOrder, JSONArray measures) throws JSONException {
        boolean isCategory = column.getString("fieldType").equalsIgnoreCase("attribute");
        if (isCategory) {
            categories.put(getCategory(column, getSortingObj(column, sortingColumnId, sortingOrder), new JSONObject()));
        } else {
            measures.put(getMeasure(column));
        }
    }

    private JSONObject getSortingObj(JSONObject column, String sortingColumnId, String sortingOrder) {
        JSONObject sortingObj = new JSONObject();
        try {
            if (column.getString("id").equals(sortingColumnId)) {
                sortingObj.put("sortingColumn", column.getString("columnName"));
                sortingObj.put("sortingOrder", sortingOrder);
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Cannot check if column is sorting column", e);
            throw new SpagoBIRuntimeException("Cannot check if column is sorting column", e);
        }
        return sortingObj;
    }

    private JSONObject getCategory(JSONObject column, JSONObject sortingObj, JSONObject drillSortingObj) {
        try {
            JSONObject category = new JSONObject();
            category.put("id", column.getString("columnName"));
            category.put("alias", column.getString("columnName"));
            category.put("columnName", column.getString("columnName"));
            category.put("funct", column.optString("aggregation").isEmpty() ? "NONE" : column.getString("aggregation"));
            String sorting = getSortingObj(sortingObj, drillSortingObj);
            buildSorting(sortingObj, drillSortingObj, sorting, category);
            return category;
        } catch (Exception e) {
            LOGGER.error("Cannot get category", e);
            throw new SpagoBIRuntimeException("Cannot get category", e);
        }
    }

    private JSONObject getMeasure(JSONObject object) {
        try {
            final String formula = "formula";
            JSONObject measure = new JSONObject();
            measure.put("id", object.getString("columnName"));
            measure.put("alias", object.getString("columnName"));
            measure.put("columnName", object.getString("columnName"));
            measure.put("funct", object.getString("aggregation"));
            if (object.has(formula)) {
                measure.put(formula, object.getString(formula));
            }
            measure.put("orderColumn", object.getString("columnName"));
            measure.put("orderType", "");
            return measure;
        } catch (Exception e) {
            LOGGER.error("Cannot get measure", e);
            throw new SpagoBIRuntimeException("Cannot get measure", e);
        }
    }

    private static void buildSummaryMeasure(JSONObject column, JSONObject listElement, JSONArray measures) throws JSONException {
        JSONObject measure = new JSONObject();
        measure.put("alias", column.getString("alias"));
        measure.put("columnName", column.getString("columnName"));
        measure.put("id", column.getString("alias"));
        measure.put("funct", listElement.getString("aggregation").equals("Columns Default Aggregation") ? column.getString("aggregation") : listElement.getString("aggregation"));
        measures.put(measure);
    }

    private static void buildSorting(JSONObject sortingObj, JSONObject drillSortingObj, String sorting, JSONObject measure) throws JSONException {
        switch (sorting) {
            case BOTH -> {
                measure.put("orderType", sortingObj.getString("sortingOrder"));
                measure.put("orderColumn", sortingObj.getString("sortingColumn"));
                measure.put("drillOrder", drillSortingObj);
            }
            case SORTING_OBJ -> {
                measure.put("orderType", sortingObj.getString("sortingOrder"));
                measure.put("orderColumn", sortingObj.getString("sortingColumn"));
            }
            case DRILL_SORTING_OBJ -> measure.put("drillOrder", drillSortingObj);
            default -> {
                measure.put("orderType", "");
                measure.put("orderColumn", "");
            }
        }
    }

    private String getSortingObj(JSONObject sortingObj, JSONObject drillSortingObj) {
        if (objectsAreNotEmpty(sortingObj, drillSortingObj)) {
            return BOTH;
        } else if (objectsAreNotEmpty(sortingObj)) {
            return SORTING_OBJ;
        } else if (objectsAreNotEmpty(drillSortingObj)) {
            return DRILL_SORTING_OBJ;
        } else {
            return "";
        }
    }

    private boolean objectsAreNotEmpty(JSONObject... objects) {
        for (JSONObject obj : objects) {
            if (obj == null || obj.length() == 0) {
                return false;
            }
        }
        return true;
    }

    /*
     * END OF DATASTORE METHODS
     * -------------------------------------------------------------------------------------------------------------------
     */


    protected int getAdjacentEqualNamesAmount(Map<String, String> groupsAndColumnsMap, JSONArray columnsOrdered, int matchStartIndex, String groupNameToMatch) {
        try {
            int adjacents = 0;
            for (int i = matchStartIndex; i < columnsOrdered.length(); i++) {
                JSONObject column = columnsOrdered.getJSONObject(i);
                String groupName = groupsAndColumnsMap.get(column.get("header"));
                if (groupName != null && groupName.equals(groupNameToMatch)) {
                    adjacents++;
                } else {
                    return adjacents;
                }
            }
            return adjacents;
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Couldn't compute adjacent equal names amount", e);
        }
    }




}
