/**
 *
 */
package it.eng.knowage.document.cockpit.template.widget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Dragan Pirkovic
 *
 */
public abstract class AbstactWidgetReader implements ICockpitWidget {

	protected JSONObject jsonWidget;

	/**
	 *
	 */
	public AbstactWidgetReader() {
		super();
	}

	@Override
	public JSONArray getColumnSelectedOfDataSet() {
		if (getContent() != null) {
			return getContent().optJSONArray("columnSelectedOfDataset");
		}
		return null;
	}

	@Override
	public Integer getDsId() {

		if (getDataset() != null) {
			return getDataset().optInt("dsId");
		}
		return null;
	}

	@Override
	public String getDsLabel() {
		if (getDataset() != null) {
			return getDataset().optString("dsLabel");
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.widget.ICockpitWidget#getId()
	 */
	@Override
	public Integer getId() {
		if (jsonWidget != null)
			return this.jsonWidget.optInt("id");
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.widget.ICockpitWidget#getJsonWidget()
	 */
	@Override
	public JSONObject getJsonWidget() {
		// TODO Auto-generated method stub
		return jsonWidget;
	}

	/**
	 *
	 */
	protected JSONObject getContent() {

		if (jsonWidget != null)
			return this.jsonWidget.optJSONObject("content");
		return null;
	}

	/**
	 * @return
	 */
	protected String getContentName() {
		if (getContent() != null)
			return getContent().optString("name");
		return null;

	}

	/**
	 * @return
	 * @throws JSONException
	 */
	protected JSONObject getDataset() {
		if (jsonWidget != null)
			return jsonWidget.optJSONObject("dataset");
		return null;

	}

	/**
	 * @return
	 */
	protected String getWidgetTitleLabel() {
		return getTitle().optString("label");
	}

	/**
	 * @return
	 * @throws JSONException
	 */
	protected boolean isTilteDefined() {
		return styleContainsProperties() && isWidgetTitleLabelDefined();
	}

	/**
	 * @return
	 * @throws JSONException
	 */
	private JSONObject getStyle() {
		if (jsonWidget != null)
			jsonWidget.optJSONObject("style");
		return null;
	}

	/**
	 * @return
	 * @throws JSONException
	 */
	private JSONObject getTitle() {
		if (getStyle() != null)
			return getStyle().optJSONObject("title");
		return null;
	}

	private boolean isWidgetTitleLabelDefined() {
		return getTitle() != null && getWidgetTitleLabel() != null && !getWidgetTitleLabel().equals("");
	}

	/**
	 * @return
	 * @throws JSONException
	 */
	private boolean styleContainsProperties() {
		return getStyle() != null && getStyle().length() != 0;
	}

}