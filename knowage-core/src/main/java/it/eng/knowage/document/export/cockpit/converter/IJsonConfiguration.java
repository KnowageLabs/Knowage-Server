/**
 *
 */
package it.eng.knowage.document.export.cockpit.converter;

import org.json.JSONObject;

/**
 * @author Dragan Pirkovic
 *
 */
public interface IJsonConfiguration {

	/**
	 * @return
	 */
	String getDatasetLabel();

	/**
	 * @return
	 */
	JSONObject getParameters();

	/**
	 * @return
	 */
	JSONObject getSelections();

	/**
	 * @return
	 */
	JSONObject getLikeSelections();

	/**
	 * @return
	 */
	JSONObject getAggregations();

	/**
	 * @return
	 */
	JSONObject getSummaryRow();

	JSONObject getOptions();

}
