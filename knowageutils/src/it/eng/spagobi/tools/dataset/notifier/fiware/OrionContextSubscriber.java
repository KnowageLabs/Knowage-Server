/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.dataset.notifier.fiware;

import it.eng.spagobi.tools.dataset.bo.RESTDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.RESTDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader.JSONPathAttribute;
import it.eng.spagobi.tools.dataset.notifier.NotifierManager;
import it.eng.spagobi.tools.dataset.notifier.NotifierManagerFactory;
import it.eng.spagobi.tools.dataset.notifier.NotifierServlet;
import it.eng.spagobi.tools.dataset.notifier.UserLabelId;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import it.eng.spagobi.utilities.rest.RestUtilities.Response;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OrionContextSubscriber {

	private static final String SUBSCRIPTION_DURATION = "P1M";

	private static final String SUBSCRIBE_CONTEXT_PATH = "/v1/subscribeContext";

	private final RESTDataProxy proxy;
	private final JSONPathDataReader dataReader;
	private String spagoBInotifyAddress;

	private final String user;

	private final String label;

	private final String authToken;

	public OrionContextSubscriber(RESTDataSet dataSet, String spagoBInotifyAddress) {
		Helper.checkNotNull(dataSet, "dataSet");

		user = dataSet.getUserId();
		if (user == null || user.isEmpty()) {
			throw new NGSISubscribingException("No user associated with dataset");
		}

		label = dataSet.getLabel();
		if (user == null || user.isEmpty()) {
			throw new NGSISubscribingException("No label associated with dataset");
		}
		
		authToken=dataSet.getOAuth2Token();

		this.proxy = dataSet.getDataProxy();
		this.dataReader = dataSet.getDataReader();

		Helper.checkNotNull(proxy, "proxy");
		Helper.checkNotNull(dataReader, "dataReader");

		this.spagoBInotifyAddress = spagoBInotifyAddress;
	}

	/**
	 * Called from {@link RESTDataSet}
	 * 
	 * @param dataSet
	 * @param user
	 * @param label
	 */
	public OrionContextSubscriber(RESTDataSet dataSet) {
		this(dataSet, null);

		initSpagoBInotifyAddress();
	}

	private void initSpagoBInotifyAddress() {
		this.spagoBInotifyAddress = NotifierServlet.getNotifyUrl();
	}

	public synchronized void subscribeNGSI() {
		NotifierManager manager = NotifierManagerFactory.getManager();

		UserLabelId subscriptionKey = new UserLabelId(user, label);
		if (manager.containsOperator(subscriptionKey)) {
			// already present
			return;
		}

		String subscriptionId = sendSubscription();
		// In this mode (listening after subscription) I lose the first notification with all context elements
		ContextBrokerNotifierOperator op = new ContextBrokerNotifierOperator(subscriptionId, user, label, dataReader);
		manager.addOperatorIfAbsent(subscriptionKey, op);
	}

	protected String sendSubscription() {
		try {
			String requestBody = getSubscriptionRequestBody();
			String address = getOrionSubscriptionBaseAddress();
			address += SUBSCRIBE_CONTEXT_PATH;
			Map<String, String> requestHeaders = getSubscriptionRequestHeaders();
			Response resp = RestUtilities.makeRequest(HttpMethod.Post, address, requestHeaders, requestBody);
			if (resp.getStatusCode() != 200) {
				// not ok
				throw new NGSISubscribingException("Status code of subscribing request is not 200: " + resp.getStatusCode());
			}
			String respBody = resp.getResponseBody();
			String subscriptionId = getSubscriptionId(respBody);
			return subscriptionId;
		} catch (Exception e) {
			throw new NGSISubscribingException("Error while subscribing to Orion Context Broker", e);
		}
	}

	protected String getOrionSubscriptionBaseAddress() throws MalformedURLException {
		String address = proxy.getAddress();
		URL url = new URL(address);
		return url.getProtocol() + "://" + url.getAuthority();
	}

	/**
	 * <pre>
	 * {
	 *   "subscribeResponse": {
	 *     "subscriptionId": "55dae93ff23205eb4241ccd0",
	 *     "duration": "P1M",
	 *     "throttling": "PT5S"
	 *   }
	 * }
	 * </pre>
	 * 
	 * @param respBody
	 * @return
	 * @throws JSONException
	 */
	private static String getSubscriptionId(String respBody) throws JSONException {
		JSONObject json = new JSONObject(respBody);
		if (!json.has("subscribeResponse")) {
			throw new NGSISubscribingException("No subscribeResponse in response");
		}
		JSONObject subResp = json.getJSONObject("subscribeResponse");
		if (!subResp.has("subscriptionId")) {
			throw new NGSISubscribingException("No subscriptionId in response");
		}
		return subResp.getString("subscriptionId");
	}

	private Map<String, String> getSubscriptionRequestHeaders() throws MalformedURLException {
		// same as data proxy
		Map<String, String> res = proxy.getRequestHeaders();
		if (OAuth2Utils.isOAuth2()) {
			if (authToken!=null) {
				res.putAll(OAuth2Utils.getOAuth2Headers(authToken));
			}
		}
		return res;
	}

	

	/**
	 * <pre>
	 * {
	 *     "entities": [
	 *         {
	 *             "type": "Meter",
	 *             "isPattern": "true",
	 *             "id": ".*"
	 *         }
	 *     ],
	 *     "reference": "http://192.168.93.1:9000/notify",
	 *     "duration": "P1M",
	 *     "notifyConditions": [
	 *         {
	 *             "type": "ONCHANGE",
	 *             "condValues": [
	 *                 "atTime"
	 *             ]
	 *         }
	 *     ],
	 *     "throttling": "PT5S"
	 * }
	 * </pre>
	 * 
	 * @return
	 * @throws JSONException
	 */
	protected String getSubscriptionRequestBody() throws JSONException {
		/**
		 * <pre>
		 *  "entities": [
		 *         {
		 *             "type": "Meter",
		 *             "isPattern": "true",
		 *             "id": ".*"
		 *         }
		 *     ],
		 * </pre>
		 */
		String proxyBody = proxy.getRequestBody();
		// use similar request body of data proxy
		JSONObject res = new JSONObject(proxyBody);
		Assert.assertTrue(res.has("entities"), "request body has no entities key");
		res.put("reference", spagoBInotifyAddress);
		res.put("duration", SUBSCRIPTION_DURATION);
		JSONObject condition = new JSONObject();
		condition.put("type", "ONCHANGE");
		List<String> attributes = new ArrayList<String>();
		List<JSONPathAttribute> attrs = dataReader.getJsonPathAttributes();
		for (JSONPathAttribute attr : attrs) {
			attributes.add(attr.getName());
		}
		condition.put("condValues", attributes);
		res.put("notifyConditions", new JSONArray(new JSONObject[] { condition }));
		return res.toString();
	}

}
