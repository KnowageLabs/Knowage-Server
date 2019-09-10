/**
 *
 */
package it.eng.knowage.document.cockpit.template;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Dragan Pirkovic
 *
 */
public interface ICockpitTemplateReader {

	/**
	 * @return
	 */
	JSONArray getWidgets();

	JSONObject getFilters();

	JSONObject getParamsByDataSetId(Integer dsId);

	String getDataSetLabelById(Integer dsId);

}
