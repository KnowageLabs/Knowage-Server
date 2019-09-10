/**
 *
 */
package it.eng.knowage.document.export.cockpit.converter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.document.export.cockpit.IConverter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Dragan Pirkovic
 *
 */
public class AggregationConverter implements IConverter<JSONObject, JSONObject> {

	private final String dataSetLabel;

	/**
	 *
	 */
	public AggregationConverter(String dataSetLabel) {
		this.dataSetLabel = dataSetLabel;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.IConverter#convert(java.lang.Object)
	 */
	@Override
	public JSONObject convert(JSONObject widget) {
		JSONObject aggregations = new JSONObject();
		try {
			JSONArray measures = new JSONArray();
			aggregations.put("measures", measures);

			JSONArray categories = new JSONArray();
			aggregations.put("categories", categories);

			String sortingColumn = null;
			String sortingOrder = null;
			JSONObject settings = widget.optJSONObject("content");
			if (settings != null) {
				sortingColumn = settings.optString("sortingColumn");
				sortingOrder = settings.optString("sortingOrder");
			}

			boolean isSortingDefined = sortingColumn != null && !sortingColumn.isEmpty() && sortingOrder != null && !sortingOrder.isEmpty();
			boolean isSortingUsed = false;

			JSONObject content = widget.optJSONObject("content");
			if (content != null) {
				JSONArray columns = content.optJSONArray("columnSelectedOfDataset");
				if (columns != null) {
					for (int i = 0; i < columns.length(); i++) {
						JSONObject column = columns.getJSONObject(i);

						String aliasToShow = column.optString("aliasToShow");
						if (aliasToShow != null && aliasToShow.isEmpty()) {
							aliasToShow = column.getString("alias");
						}

						JSONObject categoryOrMeasure = new JSONObject();
						categoryOrMeasure.put("id", column.getString("alias"));
						categoryOrMeasure.put("alias", aliasToShow);

						String formula = column.optString("formula");
						String name = formula.isEmpty() ? column.optString("name") : formula;
						categoryOrMeasure.put("columnName", name);
						if (isSortingDefined && column.has("name") && sortingColumn.equals(name)) {
							categoryOrMeasure.put("orderType", sortingOrder);
							isSortingUsed = true;
						} else {
							categoryOrMeasure.put("orderType", "");
						}

						String fieldType = column.getString("fieldType");
						if ("ATTRIBUTE".equalsIgnoreCase(fieldType)) {
							categories.put(categoryOrMeasure);
						} else if ("MEASURE".equalsIgnoreCase(fieldType)) {
							categoryOrMeasure.put("funct", column.getString("aggregationSelected"));
							measures.put(categoryOrMeasure);
						} else {
							throw new SpagoBIRuntimeException("Unsupported field type");
						}
					}

					if (isSortingDefined && !isSortingUsed) {
						JSONObject category = new JSONObject();
						category.put("alias", sortingColumn);
						category.put("columnName", sortingColumn);
						category.put("id", sortingColumn);
						category.put("orderType", sortingOrder);
						categories.put(category);
					}
				}
			}

			aggregations.put("dataset", dataSetLabel);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return aggregations;
	}

}
