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
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;

/**
 * @author Dragan Pirkovic
 *
 */
public class ProjectionConverter extends CommonJSON implements IConverter<List<AbstractSelectionField>, JSONObject> {

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
	public List<AbstractSelectionField> convert(JSONObject aggregations) {
		Map<String, String> columnAliasToName = new HashMap<String, String>();

		try {

			loadColumnAliasToName(getCategories(aggregations), columnAliasToName);
			loadColumnAliasToName(getMeasures(aggregations), columnAliasToName);
			return getProjections(dataSet, getCategories(aggregations), getMeasures(aggregations), columnAliasToName);
		} catch (JSONException e) {

		}
		return null;
	}

	protected List<AbstractSelectionField> getProjections(IDataSet dataSet, JSONArray categories, JSONArray measures, Map<String, String> columnAliasToName)
			throws JSONException {
		ArrayList<AbstractSelectionField> projections = new ArrayList<>(categories.length() + measures.length());
		addProjections(dataSet, categories, columnAliasToName, projections);
		addProjections(dataSet, measures, columnAliasToName, projections);
		return projections;
	}

	private void addProjections(IDataSet dataSet, JSONArray categories, Map<String, String> columnAliasToName, ArrayList<AbstractSelectionField> projections)
			throws JSONException {
		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			addProjection(dataSet, projections, category, columnAliasToName);
		}
	}

	private void addProjection(IDataSet dataSet, ArrayList<AbstractSelectionField> projections, JSONObject catOrMeasure, Map<String, String> columnAliasToName)
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

}
