/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	public JSONObject convert(JSONObject widget) throws IConverterException {
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
			throw new IConverterException("error while converting aggregations", e);
		}

		return aggregations;
	}

}
