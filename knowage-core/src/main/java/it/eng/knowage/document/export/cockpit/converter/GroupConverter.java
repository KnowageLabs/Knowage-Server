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
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;

/**
 * @author Dragan Pirkovic
 *
 */
public class GroupConverter extends CommonJSON implements IConverter<List<Projection>, JSONObject> {

	private IDataSet dataset;

	/**
	 * @param dataset
	 */
	public GroupConverter(IDataSet dataset) {
		this.dataset = dataset;
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
			Map<String, Object> optionMap = new HashMap<>();
			loadColumnAliasToName(getCategories(aggregations), columnAliasToName);
			loadColumnAliasToName(getMeasures(aggregations), columnAliasToName);
			return getGroups(dataset, getCategories(aggregations), getMeasures(aggregations), columnAliasToName, hasSolrFacetPivotOption(dataset, optionMap));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private boolean hasSolrFacetPivotOption(IDataSet dataSet, Map<String, Object> options) {
		return isSolrDataset(dataSet) && Boolean.TRUE.equals(options.get("solrFacetPivot"));
	}

	private boolean isSolrDataset(IDataSet dataSet) {
		if (dataSet instanceof VersionedDataSet) {
			dataSet = ((VersionedDataSet) dataSet).getWrappedDataset();
		}
		return dataSet instanceof SolrDataSet;
	}

	private List<Projection> getGroups(IDataSet dataSet, JSONArray categories, JSONArray measures, Map<String, String> columnAliasToName, boolean forceGroups)
			throws JSONException {
		ArrayList<Projection> groups = new ArrayList<>(0);

		// hasAggregationInCategory se categoria di aggregazione del for ha una funzione di aggregazione

		boolean hasAggregatedMeasures = hasAggregations(measures);

		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			String functionName = category.optString("funct");
			if (forceGroups || hasAggregatedMeasures || hasAggregationInCategory(category) || hasCountAggregation(functionName)) {
				Projection projection = getProjection(dataSet, category, columnAliasToName);
				groups.add(projection);
			}
		}

		if (forceGroups) {
			for (int i = 0; i < measures.length(); i++) {
				JSONObject measure = measures.getJSONObject(i);
				String functionName = measure.optString("funct");
				if (hasNoneAggregation(functionName)) {
					Projection projection = getProjection(dataSet, measure, columnAliasToName);
					groups.add(projection);
				}
			}
		}

		return groups;
	}

	private boolean hasAggregations(JSONArray fields) throws JSONException {
		for (int i = 0; i < fields.length(); i++) {
			JSONObject field = fields.getJSONObject(i);
			String functionName = field.optString("funct");
			if (!AggregationFunctions.get(functionName).getName().equals(AggregationFunctions.NONE)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasAggregationInCategory(JSONObject field) throws JSONException {
		String functionName = field.optString("funct");
		if (!AggregationFunctions.get(functionName).getName().equals(AggregationFunctions.NONE)) {
			return true;
		}

		return false;
	}

	private boolean hasNoneAggregation(String functionName) {
		return AggregationFunctions.get(functionName).getName().equals(AggregationFunctions.NONE);
	}

	private boolean hasCountAggregation(String functionName) { // caso in cui arrivano facets semplici
		return AggregationFunctions.get(functionName).getName().equals(AggregationFunctions.COUNT)
				|| AggregationFunctions.get(functionName).getName().equals(AggregationFunctions.COUNT_DISTINCT);
	}
}
