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
public class ChartWidgetReader extends AbstactWidgetReader {

	/**
	 * @param jsonWidget
	 */
	public ChartWidgetReader(JSONObject jsonWidget) {
		this.jsonWidget = jsonWidget;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.widget.ICockpitWidget#getName()
	 */
	@Override
	public String getName() {

		if (isTilteDefined()) {
			return getWidgetTitleLabel();
		}
		return getContentName();

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.knowage.document.cockpit.template.widget.ICockpitWidget#getFilters()
	 */
	@Override
	public JSONArray getFilters() {
		if (getContent() != null)
			return getContent().optJSONArray("filters");
		return null;
	}

}
