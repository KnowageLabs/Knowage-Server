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
package it.eng.knowage.document.cockpit.template.widget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.utilities.assertion.Assert;

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
		Assert.assertNotNull(getContent(), "content cannot be null");
		return getContent().optJSONArray("columnSelectedOfDataset");

	}

	@Override
	public Integer getDsId() {

		Assert.assertNotNull(getDataset(), "dataset cannot be null");
		return getDataset().optInt("dsId");

	}

	@Override
	public String getDsLabel() {
		Assert.assertNotNull(getDataset(), "dataset cannot be null");
		return getDataset().optString("dsLabel");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.widget.ICockpitWidget#getId()
	 */
	@Override
	public Integer getId() {
		Assert.assertNotNull(jsonWidget, "jsonWidget cannot be null");
		return this.jsonWidget.optInt("id");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.widget.ICockpitWidget#getJsonWidget()
	 */
	@Override
	public JSONObject getJsonWidget() {
		return jsonWidget;
	}

	/**
	 *
	 */
	protected JSONObject getContent() {

		Assert.assertNotNull(jsonWidget, "jsonWidget cannot be null");
		return this.jsonWidget.optJSONObject("content");

	}

	/**
	 * @return
	 */
	protected String getContentName() {
		Assert.assertNotNull(getContent(), "content cannot be null");
		return getContent().optString("name");

	}

	/**
	 * @return
	 * @throws JSONException
	 */
	protected JSONObject getDataset() {
		Assert.assertNotNull(jsonWidget, "jsonWidget cannot be null");
		return jsonWidget.optJSONObject("dataset");

	}

	/**
	 * @return
	 */
	protected String getWidgetTitleLabel() {
		Assert.assertNotNull(getTitle(), "title cannot be null");
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
		Assert.assertNotNull(jsonWidget, "jsonWidget cannot be null");
		return jsonWidget.optJSONObject("style");

	}

	/**
	 * @return
	 * @throws JSONException
	 */
	private JSONObject getTitle() {
		Assert.assertNotNull(getStyle(), "style cannot be null");
		return getStyle().optJSONObject("title");

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