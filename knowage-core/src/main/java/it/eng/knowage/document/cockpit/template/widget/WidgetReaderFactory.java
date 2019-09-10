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
		WidgetType type = getWidgetType(jsonWidget);
		if (type != null) {
			switch (type) {
			case TABLE:
				cockpitWidget = new TableWidgetReader(jsonWidget);
				break;
			case CHART:
				cockpitWidget = new ChartWidgetReader(jsonWidget);
				break;

			default:
				break;
			}
		}

		return cockpitWidget;
	}

	/**
	 * @param jsonWidget
	 * @throws JSONException
	 */
	private static WidgetType getWidgetType(JSONObject jsonWidget) throws JSONException {
		WidgetType[] values = WidgetType.values();
		String type = jsonWidget.getString("type").toUpperCase();

		for (int i = 0; i < values.length; i++) {
			if (values[i].name().equals(type))
				return values[i];
		}
		return null;

	}

}
