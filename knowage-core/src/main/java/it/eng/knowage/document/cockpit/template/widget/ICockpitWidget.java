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
public interface ICockpitWidget {

	/**
	 * @return
	 */
	JSONObject getJsonWidget();

	String getName();

	Integer getDsId();

	String getDsLabel();

	JSONArray getColumnSelectedOfDataSet();

	JSONArray getFilters();

	/**
	 * @return
	 */
	Integer getId();

}
