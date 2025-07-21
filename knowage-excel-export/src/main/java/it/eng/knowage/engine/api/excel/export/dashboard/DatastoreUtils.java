package it.eng.knowage.engine.api.excel.export.dashboard;

import it.eng.knowage.engine.api.excel.export.ExporterClient;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DatastoreUtils extends Common {
    private static final Logger LOGGER = LogManager.getLogger(DatastoreUtils.class);

    protected final String userUniqueIdentifier;

    private static final String SORTING_OBJ = "sortingObj";
    private static final String DRILL_SORTING_OBJ = "drillSortingObj";
    private static final String BOTH = "both";

    public DatastoreUtils(String userUniqueIdentifier) {
        this.userUniqueIdentifier = userUniqueIdentifier;
    }

    protected JSONObject getDatastore(String datasetLabel, Map<String, Object> map, String selections, int offset,
                                      int fetchSize) {
        ExporterClient client = new ExporterClient();
        try {
            LOGGER.info("calling the datastore with userUniqueIdentifier: {}", userUniqueIdentifier);
            return client.getDataStore(map, datasetLabel, userUniqueIdentifier, selections, offset, fetchSize);
        } catch (Exception e) {
            String message = "Unable to get data";
            LOGGER.error(message, e);
            throw new SpagoBIRuntimeException(message);
        }
    }


    public JSONObject getDataStoreforDashboardSingleWidget(JSONObject singleWidget, Map<String, Map<String, JSONArray>> selections, JSONObject drivers, JSONObject parameters) {
        DatastoreUtils datastoreUtils = new DatastoreUtils(userUniqueIdentifier);
        return datastoreUtils.getDataStoreForDashboardWidget(singleWidget, 0, -1, selections, drivers, parameters);
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
            dashboardSelections.put("parameters", drivers);
            dashboardSelections.put("parameters", parameters);

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


}
