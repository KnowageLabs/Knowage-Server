/**
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

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.metasql.query.item.CoupledProjection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 *
 * @author raselako
 * @email radmila.selakovic@eng.it
 *
 */
public class CommonJSON {

	protected IDataSet dataSet;

	/**
	 *
	 */
	public CommonJSON() {
		super();
	}

	/**
	 * @param categories
	 * @param columnAliasToName
	 * @throws JSONException
	 */
	protected void loadColumnAliasToName(JSONArray jsonArray, Map<String, String> columnAliasToName) throws JSONException {
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject category = jsonArray.getJSONObject(i);
			String alias = category.optString("alias");
			if (alias != null && !alias.isEmpty()) {
				String id = category.optString("id");
				if (id != null && !id.isEmpty()) {
					columnAliasToName.put(alias, id);
				}

				String columnName = category.optString("columnName");
				if (columnName != null && !columnName.isEmpty()) {
					columnAliasToName.put(alias, columnName);
				}
			}
		}

	}

	/**
	 * @param aggregations
	 * @return
	 * @throws JSONException
	 */
	protected JSONArray getMeasures(JSONObject aggregations) throws JSONException {

		if (aggregations != null) {
			return aggregations.getJSONArray("measures");
		}
		return null;
	}

	/**
	 * @param aggregations
	 * @return
	 * @throws JSONException
	 */
	protected JSONArray getCategories(JSONObject aggregations) throws JSONException {
		if (aggregations != null) {
			return aggregations.getJSONArray("categories");
		}
		return null;

	}

	protected String getColumnName(JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		if (jsonObject.isNull("id") && jsonObject.isNull("columnName")) {
			return getColumnAlias(jsonObject, columnAliasToName);
		} else {

			if (jsonObject.has("formula")) {
				// it is a calculated field
				return jsonObject.getString("columnName");
			}

			String id = jsonObject.getString("id");
			boolean isIdMatching = columnAliasToName.containsKey(id) || columnAliasToName.containsValue(id);

			String columnName = jsonObject.getString("columnName");
			boolean isColumnNameMatching = columnAliasToName.containsKey(columnName) || columnAliasToName.containsValue(columnName);

			Assert.assertTrue(isIdMatching || isColumnNameMatching, "Column name [" + columnName + "] not found in dataset metadata");
			return isColumnNameMatching ? columnName : id;
		}
	}

	protected String getColumnAlias(JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		String columnAlias = jsonObject.getString("alias");
		Assert.assertTrue(columnAliasToName.containsKey(columnAlias) || columnAliasToName.containsValue(columnAlias),
				"Column alias [" + columnAlias + "] not found in dataset metadata");
		return columnAlias;
	}

	protected Projection getProjection(IDataSet dataSet, JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		return getProjectionWithFunct(dataSet, jsonObject, columnAliasToName, jsonObject.optString("funct")); // caso in cui ci siano facets complesse (coupled
																												// proj)
	}

	protected Projection getProjectionWithFunct(IDataSet dataSet, JSONObject jsonObject, Map<String, String> columnAliasToName, String functName)
			throws JSONException {
		String columnName = getColumnName(jsonObject, columnAliasToName);
		String columnAlias = getColumnAlias(jsonObject, columnAliasToName);
		IAggregationFunction function = AggregationFunctions.get(functName);
		String functionColumnName = jsonObject.optString("functColumn");
		if (jsonObject.has("formula")) {
			function = AggregationFunctions.get("NONE");
		}
		Projection projection;
		if (!function.equals(AggregationFunctions.COUNT_FUNCTION) && functionColumnName != null && !functionColumnName.isEmpty()) {
			Projection aggregatedProjection = new Projection(dataSet, functionColumnName);
			projection = new CoupledProjection(function, aggregatedProjection, dataSet, columnName, columnAlias);
		} else {
			projection = new Projection(function, dataSet, columnName, columnAlias);
		}
		return projection;
	}

}