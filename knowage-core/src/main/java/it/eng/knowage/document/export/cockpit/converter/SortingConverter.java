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
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;

/**
 * @author Dragan Pirkovic
 *
 */
public class SortingConverter extends CommonJSON implements IConverter<List<Sorting>, JSONObject> {

	/**
	 * @param dataSet
	 */
	public SortingConverter(IDataSet dataSet) {
		this.dataSet = dataSet;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.export.cockpit.IConverter#convert(java.lang.Object)
	 */
	@Override
	public List<Sorting> convert(JSONObject aggregations) {
		Map<String, String> columnAliasToName = new HashMap<String, String>();

		try {
			loadColumnAliasToName(getCategories(aggregations), columnAliasToName);
			loadColumnAliasToName(getMeasures(aggregations), columnAliasToName);
			return getSortings(dataSet, getCategories(aggregations), getMeasures(aggregations), columnAliasToName);
		} catch (JSONException e) {

		}
		return null;
	}

	protected List<Sorting> getSortings(IDataSet dataSet, JSONArray categories, JSONArray measures, Map<String, String> columnAliasToName)
			throws JSONException {
		ArrayList<Sorting> sortings = new ArrayList<Sorting>(0);

		for (int i = 0; i < categories.length(); i++) {
			JSONObject categoryObject = categories.getJSONObject(i);
			Sorting sorting = getSorting(dataSet, categoryObject, columnAliasToName);
			if (sorting != null) {
				sortings.add(sorting);
			}
		}

		for (int i = 0; i < measures.length(); i++) {
			JSONObject measure = measures.getJSONObject(i);
			Sorting sorting = getSorting(dataSet, measure, columnAliasToName);
			if (sorting != null) {
				sortings.add(sorting);
			}
		}

		return sortings;
	}

	private Sorting getSorting(IDataSet dataSet, JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		Sorting sorting = null;

		String orderType = (String) jsonObject.opt("orderType");
		if (orderType != null && !orderType.isEmpty() && ("ASC".equalsIgnoreCase(orderType) || "DESC".equalsIgnoreCase(orderType))) {
			IAggregationFunction function = AggregationFunctions.get(jsonObject.optString("funct"));
			String orderColumn = (String) jsonObject.opt("orderColumn");

			Projection projection;
			if (orderColumn != null && !orderColumn.isEmpty() && !orderType.isEmpty()) {
				String alias = jsonObject.optString("alias");
				projection = new Projection(function, dataSet, orderColumn, alias);
			} else {
				String columnName = getColumnName(jsonObject, columnAliasToName);
				projection = new Projection(function, dataSet, columnName, orderColumn);
			}

			boolean isAscending = "ASC".equalsIgnoreCase(orderType);

			sorting = new Sorting(projection, isAscending);
		}

		return sorting;
	}

}
