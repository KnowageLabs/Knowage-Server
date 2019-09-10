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

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.document.export.cockpit.IConverter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 *
 * @author raselako
 * @email radmila.selakovic@eng.it
 *
 */
public class CommonJSON {

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
	
			if (jsonObject.has("datasetOrTableFlag")) {
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

}