/**
 *
 */
package it.eng.knowage.document.cockpit.template;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
		if (getConfiguration() != null)
			return getConfiguration().optJSONObject("filters");
		return null;

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

		if (getDatasets() != null) {
			for (int i = 0; i < getDatasets().length(); i++) {
				if (getDatasetId(getDatasets().optJSONObject(i)).equals(dsId)) {
					return getDatasets().optJSONObject(i).optJSONObject("parameters");
				}
			}
		}
		;

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
		if (getSheets() != null) {
			for (int i = 0; i < getSheets().length(); i++) {

				JSONArray widgetsInSheet = getWidgets(getSheets().optJSONObject(i));

				if (widgetsInSheet != null) {
					for (int j = 0; j < widgetsInSheet.length(); j++) {
						widgets.put(widgetsInSheet.optJSONObject(j));
					}
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

		return jsonTemplate.optJSONObject("configuration");
	}

	private Integer getDatasetId(JSONObject dataset) {
		return dataset.optInt("dsId");
	}

	/**
	 *
	 */
	private JSONArray getDatasets() {

		if (getConfiguration() != null) {
			return getConfiguration().optJSONArray("datasets");
		}

		return null;
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

}
