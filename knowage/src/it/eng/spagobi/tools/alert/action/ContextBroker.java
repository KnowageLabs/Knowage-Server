package it.eng.spagobi.tools.alert.action;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.alert.exception.AlertActionException;
import it.eng.spagobi.tools.alert.job.AbstractAlertAction;
import it.eng.spagobi.tools.alert.listener.KpiListener;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContextBroker extends AbstractAlertAction {

	private static Logger logger = Logger.getLogger(ContextBroker.class);

	@Override
	public void executeAction(String jsonOptions, Map<String, String> externalParameters) throws AlertActionException {
		logger.debug("IN");
		JSONObject result = new JSONObject();
		String url = null;
		String type = null;
		try {
			JSONObject temp = new JSONObject(new JSONObject(jsonOptions).getString("jsonActionParameters"));
			logger.debug("get json action parameters: " + temp);

			url = temp.optString("contextBrokerUrl");
			type = temp.optString("contextBrokerType");

			logger.debug("url: " + url + ", type: " + type);

		} catch (JSONException e) {
			logger.error("Error in recovering context broker settings ");
			return;
		}

		// Get KPI Info
		JSONObject kpiInfo = null;
		if (externalParameters != null && !externalParameters.isEmpty()) {
			for (Entry<String, String> entry : externalParameters.entrySet()) {
				if (entry.getKey().equals(KpiListener.KPI_INFO_PLACEHOLDER)) {
					String jsonString = entry.getValue();
					try {
						kpiInfo = new JSONObject(jsonString);
					} catch (JSONException e) {
						logger.error("No JsonObject from string " + jsonString + " could be parsed");
						return;
					}
				}
			}
		}

		try {

			JSONObject jsonToSend = createJSONObject(type, kpiInfo);

			Map<String, String> headersMap = new HashMap<String, String>();
			headersMap.put("Content-Type", "application/json");
			headersMap.put("Accept", "application/json");

			RestUtilities.Response response = RestUtilities.makeRequest(HttpMethod.Post, url, headersMap, jsonToSend.toString(), null);
			int statusCode = response.getStatusCode();
			logger.debug("KPI object sent, status code returned " + statusCode);

		} catch (Exception e) {
			logger.error("Error while sending schedule to context broker", e);
			return;
		}

	}

	private JSONObject buildValueJsonObject(String name, String type, String value) throws JSONException {
		JSONObject valueJson = new JSONObject();
		valueJson.put("name", name);
		valueJson.put("type", type);
		valueJson.put("value", value);
		return valueJson;
	}

	private JSONObject createJSONObject(String contextBrokerType, JSONObject kpiInfo) throws JSONException {
		logger.debug("IN");

		JSONObject jsonToSend = new JSONObject();

		JSONArray contextElementsJsonArray = new JSONArray();

		// for each KPI
		int i = 0;
		for (Iterator it = kpiInfo.keys(); it.hasNext();) {
			String key = (String) it.next();

			JSONObject contextElementsJsonObj = new JSONObject();
			contextElementsJsonObj.put("type", contextBrokerType);
			contextElementsJsonObj.put("isPattern", "false");

			JSONObject containedInfo = kpiInfo.getJSONObject(key);
			String kpiId = containedInfo.optString("kpiId");
			String kpiLabel = containedInfo.optString("kpiLabel");
			String computedValue = containedInfo.optString("computedValue");
			// String manualValue = containedInfo.optString("manualValue");

			contextElementsJsonObj.put("id", kpiId);

			JSONArray jsonAttributes = new JSONArray();
			JSONObject valueJson = buildValueJsonObject("value", "double", computedValue);
			JSONObject nameJson = buildValueJsonObject("label", "string", kpiLabel);

			jsonAttributes.put(0, valueJson);
			jsonAttributes.put(1, nameJson);
			contextElementsJsonObj.put("attributes", jsonAttributes);

			contextElementsJsonArray.put(i, contextElementsJsonObj);
			i++;
		}

		jsonToSend.put("contextElements", contextElementsJsonArray);
		jsonToSend.put("updateAction", "APPEND");

		logger.debug("Returning value parsed in JSON: " + jsonToSend.toString());

		logger.debug("OUT");

		return jsonToSend;
	}

	@Override
	public List<SbiHibernateModel> exportAction(String actionParams) {
		return new ArrayList<SbiHibernateModel>();
	}

}
