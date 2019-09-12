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
package it.eng.knowage.document.cockpit.template;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Dragan Pirkovic
 *
 */
public class CockpitTemplateReader implements ICockpitTemplateReader {

	private JSONObject jsonTemplate;

	/**
	 * @param jsonTemplate
	 */
	public CockpitTemplateReader(JSONObject jsonTemplate) {
		this.jsonTemplate = jsonTemplate;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.ICockpitTemplateReader#getFilters()
	 */
	@Override
	public JSONObject getFilters() {
		Assert.assertNotNull(getConfiguration(), "configuration cannot be null");
		return getConfiguration().optJSONObject("filters");

	}

	/**
	 * @return the jsonTemplate
	 */
	public JSONObject getJsonTemplate() {
		return jsonTemplate;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.ICockpitTemplateReader#getParamsByDataSetId(java.lang.Integer)
	 */
	@Override
	public JSONObject getParamsByDataSetId(Integer dsId) {

		Assert.assertNotNull(getDatasets(), "Datasets cannot be null");
		for (int i = 0; i < getDatasets().length(); i++) {
			if (getDatasetId(getDatasets().optJSONObject(i)).equals(dsId)) {
				return getDatasets().optJSONObject(i).optJSONObject("parameters");
			}
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.ICockpitTemplateReader#getWidgets()
	 */
	@Override
	public JSONArray getWidgets() {
		JSONArray widgets = new JSONArray();
		Assert.assertNotNull(getSheets(), "sheets cannot be null");
		for (int i = 0; i < getSheets().length(); i++) {

			JSONArray widgetsInSheet = getWidgets(getSheets().optJSONObject(i));

			if (widgetsInSheet != null) {
				for (int j = 0; j < widgetsInSheet.length(); j++) {
					widgets.put(widgetsInSheet.optJSONObject(j));
				}
			}

		}

		return widgets;
	}

	/**
	 * @param jsonTemplate
	 *            the jsonTemplate to set
	 */
	public void setJsonTemplate(JSONObject jsonTemplate) {
		this.jsonTemplate = jsonTemplate;
	}

	/**
	 * @return
	 * @throws JSONException
	 */
	private JSONObject getConfiguration() {
		Assert.assertNotNull(jsonTemplate, "jsonTemplate cannot be null");
		return jsonTemplate.optJSONObject("configuration");
	}

	private Integer getDatasetId(JSONObject dataset) {
		return dataset.optInt("dsId");
	}

	/**
	 *
	 */
	private JSONArray getDatasets() {

		Assert.assertNotNull(getConfiguration(), "configuration cannot be null");
		return getConfiguration().optJSONArray("datasets");

	}

	private JSONArray getSheets() {
		return jsonTemplate.optJSONArray("sheets");

	}

	/**
	 *
	 */
	private JSONArray getWidgets(JSONObject sheet) {
		return sheet.optJSONArray("widgets");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.ICockpitTemplateReader#getDataSetLabelById(java.lang.Integer)
	 */
	@Override
	public String getDataSetLabelById(Integer dsId) {
		Assert.assertNotNull(getDatasets(), "datasets cannot be null");
		for (int i = 0; i < getDatasets().length(); i++) {
			if (getDatasetId(getDatasets().optJSONObject(i)).equals(dsId)) {
				return getDatasets().optJSONObject(i).optString("dsLabel");
			}
		}

		return null;
	}

}
