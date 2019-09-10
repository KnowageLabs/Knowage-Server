/**
 *
 */
package it.eng.knowage.document.export.cockpit.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.document.export.cockpit.IConverter;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.metasql.query.item.CoupledProjection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;

/**
 * @author Dragan Pirkovic
 *
 */
public class ProjectionConverter extends CommonJSON implements IConverter<List<Projection>, JSONObject> {

	private final IDataSet dataSet;

	/**
	 * @param dataset
	 */
	public ProjectionConverter(IDataSet dataset) {
		this.dataSet = dataset;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.IConverter#convert(java.lang.Object)
	 */
	@Override
	public List<Projection> convert(JSONObject aggregations) {
		Map<String, String> columnAliasToName = new HashMap<String, String>();

		try {

			loadColumnAliasToName(getCategories(aggregations), columnAliasToName);
			loadColumnAliasToName(getMeasures(aggregations), columnAliasToName);
			return getProjections(dataSet, getCategories(aggregations), getMeasures(aggregations), columnAliasToName);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	protected List<Projection> getProjections(IDataSet dataSet, JSONArray categories, JSONArray measures, Map<String, String> columnAliasToName)
			throws JSONException {
		ArrayList<Projection> projections = new ArrayList<>(categories.length() + measures.length());
		addProjections(dataSet, categories, columnAliasToName, projections);
		addProjections(dataSet, measures, columnAliasToName, projections);
		return projections;
	}

	private void addProjections(IDataSet dataSet, JSONArray categories, Map<String, String> columnAliasToName, ArrayList<Projection> projections)
			throws JSONException {
		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			addProjection(dataSet, projections, category, columnAliasToName);
		}
	}

	private void addProjection(IDataSet dataSet, ArrayList<Projection> projections, JSONObject catOrMeasure, Map<String, String> columnAliasToName)
			throws JSONException {

		String functionObj = catOrMeasure.optString("funct");
		// check if it is an array
		if (functionObj.startsWith("[")) {
			// call for each aggregation function
			JSONArray functs = new JSONArray(functionObj);
			for (int j = 0; j < functs.length(); j++) {
				String functName = functs.getString(j);
				Projection projection = getProjectionWithFunct(dataSet, catOrMeasure, columnAliasToName, functName);
				projections.add(projection);
			}
		} else {
			// only one aggregation function
			Projection projection = getProjection(dataSet, catOrMeasure, columnAliasToName);
			projections.add(projection);
		}

	}

	private Projection getProjection(IDataSet dataSet, JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		return getProjectionWithFunct(dataSet, jsonObject, columnAliasToName, jsonObject.optString("funct")); // caso in cui ci siano facets complesse (coupled
																												// proj)
	}

	private Projection getProjectionWithFunct(IDataSet dataSet, JSONObject jsonObject, Map<String, String> columnAliasToName, String functName)
			throws JSONException {
		String columnName = getColumnName(jsonObject, columnAliasToName);
		String columnAlias = getColumnAlias(jsonObject, columnAliasToName);
		IAggregationFunction function = AggregationFunctions.get(functName);
		String functionColumnName = jsonObject.optString("functColumn");
		Projection projection;
		if (jsonObject.has("datasetOrTableFlag") && !jsonObject.getBoolean("datasetOrTableFlag"))
			function = AggregationFunctions.get("NONE");
		if (!function.equals(AggregationFunctions.COUNT_FUNCTION) && functionColumnName != null && !functionColumnName.isEmpty()) {
			Projection aggregatedProjection = new Projection(dataSet, functionColumnName);
			projection = new CoupledProjection(function, aggregatedProjection, dataSet, columnName, columnAlias);
		} else {
			projection = new Projection(function, dataSet, columnName, columnAlias);
		}
		return projection;
	}

}
