package it.eng.spagobi.tools.alert.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.security.hmacfilter.HMACSecurityException;
import it.eng.spagobi.tools.alert.exception.AlertActionException;
import it.eng.spagobi.tools.alert.job.AbstractAlertAction;
import it.eng.spagobi.tools.alert.listener.KpiListener;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;

public class NotifyContextBroker extends AbstractAlertAction {

	private static Logger logger = Logger.getLogger(NotifyContextBroker.class);

	@Override
	public void executeAction(String jsonOptions, Map<String, String> externalParameters) throws AlertActionException {
		logger.debug("IN");
		try {
			JSONObject temp = new JSONObject(new JSONObject(jsonOptions).getString("jsonActionParameters"));
			logger.debug("Get JSON action parameters: " + temp);

			String createUrl = temp.optString("contextBrokerUrl");
			String entityType = temp.optString("contextBrokerType");

			logger.debug("Url: " + createUrl + ", Entity type: " + entityType);

			// Get KPI Info
			JSONObject kpiInfo = null;
			if (externalParameters != null && !externalParameters.isEmpty()) {
				for (Entry<String, String> entry : externalParameters.entrySet()) {
					if (entry.getKey().equals(KpiListener.KPI_INFO_PLACEHOLDER)) {
						String jsonString = entry.getValue();
						kpiInfo = new JSONObject(jsonString);
						break;
					}
				}
			}

			Assert.assertNotNull(kpiInfo, "Impossible to obtain KPI info.");
			Assert.assertTrue(kpiInfo.length() == 1, "Invalid length for kpiInfo. It must be equals to 1.");

			kpiInfo = kpiInfo.getJSONObject("kpiId");
			JSONObject jsonToSend = createJSONObject(kpiInfo);
			String entityId = kpiInfo.getString("kpiLabel");
			String updateUrl = createUrl.endsWith("/") ? (createUrl + entityId + "/attrs") : (createUrl + "/" + entityId + "/attrs");
			logger.debug("Update url: " + updateUrl);

			Map<String, String> headersMap = new HashMap<>();
			headersMap.put("Content-Type", "application/json");
			headersMap.put("Accept", "application/json");

			RestUtilities.Response response = RestUtilities.makeRequest(HttpMethod.Post, updateUrl, headersMap, jsonToSend.toString(), null);
			int statusCode = response.getStatusCode();
			if (statusCode == HttpStatus.SC_NOT_FOUND) {
				logger.debug("The requested entity has not been found. This can be the first update, thus an entity will be created.");
				jsonToSend.put("id", entityId);
				jsonToSend.put("type", entityType);
				response = RestUtilities.makeRequest(HttpMethod.Post, createUrl, headersMap, jsonToSend.toString(), null);
				Assert.assertTrue(response.getStatusCode() == HttpStatus.SC_CREATED,
						"Impossible to create entity " + entityId + ".\n Status code is not 201 but " + response.getStatusCode());
			} else {
				if (statusCode == HttpStatus.SC_NO_CONTENT) {
					logger.debug("KPI object sent, status code returned " + statusCode);
				} else {
					throw new HttpException("Status code: " + statusCode + ".\n" + response.getResponseBody());
				}
			}

		} catch (JSONException | HMACSecurityException | IOException e) {
			throw new AlertActionException("Error while sending schedule to context broker", e);
		}

	}

	private JSONObject buildValueJsonObject(String type, String value) throws JSONException {
		JSONObject valueJson = new JSONObject();
		valueJson.put("value", value);
		valueJson.put("type", type);
		return valueJson;
	}

	private JSONObject createJSONObject(JSONObject kpiInfo) throws JSONException {
		logger.debug("IN");

		JSONObject jsonToSend = new JSONObject();

		String computedValue = kpiInfo.optString("computedValue");
		JSONObject valueJson = buildValueJsonObject("Double", computedValue);
		jsonToSend.put("value", valueJson);

		logger.debug("Returning value parsed in JSON: " + jsonToSend.toString());

		logger.debug("OUT");

		return jsonToSend;
	}

	@Override
	public List<SbiHibernateModel> exportAction(String actionParams) {
		return new ArrayList<>();
	}

}
