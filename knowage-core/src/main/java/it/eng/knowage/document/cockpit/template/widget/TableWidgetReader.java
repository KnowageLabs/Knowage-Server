/**
 *
 */
package it.eng.knowage.document.cockpit.template.widget;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Dragan Pirkovic
 *
 */
public class TableWidgetReader extends AbstactWidgetReader {

	/**
	 * @param jsonWidget
	 */
	public TableWidgetReader(JSONObject jsonWidget) {
		this.jsonWidget = jsonWidget;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.widget.ICockpitWidget#getName()
	 */
	@Override
	public String getName() {
		return getContentName();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.widget.ICockpitWidget#getFilters()
	 */
	@Override
	public JSONArray getFilters() {
		if (jsonWidget != null)
			return jsonWidget.optJSONArray("filters");
		return null;
	}

}
