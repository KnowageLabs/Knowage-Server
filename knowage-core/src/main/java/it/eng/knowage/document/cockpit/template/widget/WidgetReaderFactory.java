/**
 *
 */
package it.eng.knowage.document.cockpit.template.widget;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Dragan Pirkovic
 *
 */
public class WidgetReaderFactory {

	/**
	 * @param jsonWidget
	 * @return
	 * @throws JSONException
	 */
	public static ICockpitWidget getWidget(JSONObject jsonWidget) throws JSONException {
		ICockpitWidget cockpitWidget = null;
		switch (getWidgetType(jsonWidget)) {
		case TABLE:
			cockpitWidget = new TableWidgetReader(jsonWidget);
			break;
		case CHART:
			cockpitWidget = new ChartWidgetReader(jsonWidget);
			break;

		default:
			break;
		}
		return cockpitWidget;
	}

	/**
	 * @param jsonWidget
	 * @throws JSONException
	 */
	private static WidgetType getWidgetType(JSONObject jsonWidget) throws JSONException {
		return WidgetType.valueOf(jsonWidget.getString("type").toUpperCase());

	}

}
