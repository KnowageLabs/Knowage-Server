/**
 *
 */
package it.eng.knowage.document.export.cockpit.converter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.document.export.cockpit.IConverter;

/**
 * @author Dragan Pirkovic
 *
 */
public class SelectionsConverter implements IConverter<JSONObject, JSONArray> {

	private final JSONObject filters;
	private final String dataSetLabel;

	/**
	 *
	 */
	public SelectionsConverter(String dataSetLabel, JSONObject filters) {
		this.dataSetLabel = dataSetLabel;
		this.filters = filters;
	}

	@Override
	public JSONObject convert(JSONArray widgetFilters) {
		;
		JSONObject selections = new JSONObject();
		JSONObject datasetFilters = new JSONObject();

		if (filters != null) {
			JSONObject obj = filters.optJSONObject(dataSetLabel);
			if (obj != null) {
				String[] names = JSONObject.getNames(obj);
				if (names != null) {
					for (int i = 0; i < names.length; i++) {
						String filter = names[i];
						JSONArray array = new JSONArray();
						try {
							array.put("('" + obj.get(filter) + "')");
							datasetFilters.put(filter, array);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			}
		}

		if (widgetFilters != null) {
			for (int i = 0; i < widgetFilters.length(); i++) {
				JSONObject widgetFilter;
				try {
					widgetFilter = widgetFilters.getJSONObject(i);
					JSONArray filterVals = widgetFilter.getJSONArray("filterVals");
					if (filterVals.length() > 0) {
						String colName = widgetFilter.getString("colName");

						JSONArray values = new JSONArray();
						for (int j = 0; j < filterVals.length(); j++) {
							Object filterVal = filterVals.get(j);
							values.put("('" + filterVal + "')");
						}

						String filterOperator = widgetFilter.getString("filterOperator");
						if (filterOperator != null) {
							JSONObject filter = new JSONObject();
							filter.put("filterOperator", filterOperator);
							filter.put("filterVals", values);
							datasetFilters.put(colName, filter);
						} else {
							datasetFilters.put(colName, values);
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		try {
			selections.put(dataSetLabel, datasetFilters);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return selections;
	}

}
