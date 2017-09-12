/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.tools.dataset.notifier.fiware;

import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.bo.RESTDataSet;
import it.eng.spagobi.tools.dataset.common.dataproxy.RESTDataProxy;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader;
import it.eng.spagobi.tools.dataset.common.datareader.JSONPathDataReader.JSONPathAttribute;
import it.eng.spagobi.tools.dataset.notifier.NotifierManager;
import it.eng.spagobi.tools.dataset.notifier.NotifierManagerFactory;
import it.eng.spagobi.tools.dataset.notifier.NotifierServlet;
import it.eng.spagobi.tools.dataset.notifier.UserLabelId;
import it.eng.spagobi.tools.dataset.notifier.fiware.ngsi.v2.Condition;
import it.eng.spagobi.tools.dataset.notifier.fiware.ngsi.v2.Entity;
import it.eng.spagobi.tools.dataset.notifier.fiware.ngsi.v2.Http;
import it.eng.spagobi.tools.dataset.notifier.fiware.ngsi.v2.Notification;
import it.eng.spagobi.tools.dataset.notifier.fiware.ngsi.v2.Subject;
import it.eng.spagobi.tools.dataset.notifier.fiware.ngsi.v2.Subscription;
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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.NameValuePair;
import org.joda.time.DurationFieldType;
import org.joda.time.MutableDateTime;
import org.json.JSONException;

public class OrionContextSubscriber {

	/*
	 * the last char indicates the time unit (hour -> H, day -> D, month -> M, year -> Y) 1M -> one month 33D -> 33 days
	 */
	private static final int SUBSCRIPTION_DURATION = 1;
	private static DurationFieldType DURATION_TYPE = DurationFieldType.months();

	private static final int THROTTLING = 5;

	private static final String SUBSCRIBE_CONTEXT_PATH = "/v2/subscriptions";

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

		authToken = dataSet.getOAuth2Token();

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
			if (resp.getStatusCode() != 201) {
				// not ok
				throw new NGSISubscribingException("Status code of subscribing request is not 201: " + resp.getStatusCode());
			}
			String subscriptionId = getSubscriptionId(resp);
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
	private static String getSubscriptionId(Response response) throws JSONException {
		Header[] headers = response.getHeaders();
		Assert.assertTrue((headers != null && headers.length > 0), "No headers in response");
		for (Header header : headers) {
			if (header.getName().equals("Location")) {
				String location = header.getValue();
				return location.substring(location.lastIndexOf('/') + 1);
			}
		}
		throw new NGSISubscribingException("No Location header, thus no subscriptionId in response");
	}

	private Map<String, String> getSubscriptionRequestHeaders() throws MalformedURLException {
		// same as data proxy
		Map<String, String> res = proxy.getRequestHeaders();
		if (OAuth2Utils.isOAuth2()) {
			if (authToken != null) {
				res.putAll(OAuth2Utils.getOAuth2Headers(authToken));
			}
		}
		res.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		return res;
	}

	/**
	 * <pre>
	 * {
	 *   "description": "A subscription to get info about Room1",
	 *   "subject": {
	 *     "entities": [
	 *       {
	 *         "id": "Room1",
	 *         "type": "Room"
	 *       }
	 *     ],
	 *     "condition": {
	 *       "attrs": [
	 *         "pressure"
	 *       ]
	 *     }
	 *   },
	 *   "notification": {
	 *     "http": {
	 *       "url": "http://localhost:1028/accumulate"
	 *     },
	 *     "attrs": [
	 *       "temperature"
	 *     ]
	 *   },
	 *   "expires": "2040-01-01T14:00:00.00Z", // OPTIONAL
	 *   "throttling": 5
	 * }
	 * </pre>
	 *
	 * @return
	 * @throws JSONException
	 * @throws MalformedURLException
	 */
	protected String getSubscriptionRequestBody() throws JSONException, MalformedURLException {

		List<String> attrs = new ArrayList<String>();
		for (JSONPathAttribute attr : dataReader.getJsonPathAttributes()) {
			attrs.add(attr.getName());
		}

		Subscription subscription = new Subscription();
		subscription.setDescription("A subscription Knowage app. Requested by the user [ " + user + " ] for the dataset [ " + label);

		Subject subject = new Subject();
		Entity entity = new Entity();
		List<NameValuePair> params = RestUtilities.getAddressPairs(proxy.getAddress());
		for (NameValuePair param : params) {
			if (param.getName().equals("id")) {
				entity.setId(param.getValue());
			} else if (param.getName().equals("type")) {
				entity.setType(param.getValue());
			} else if (param.getName().equals("idPattern")) {
				entity.setIdPattern(param.getValue());
			}
		}

		if (!((entity.getId() != null) ^ (entity.getIdPattern() != null))) {
			throw new NGSISubscribingException("One and only one param between id and idPattern can be submitted.");
		}

		List<Entity> entities = new ArrayList<>();
		entities.add(entity);
		subject.setEntities(entities);
		Condition condition = new Condition();
		condition.setAttrs(attrs);
		subject.setCondition(condition);
		subscription.setSubject(subject);

		Notification notification = new Notification();
		Http http = new Http();
		http.setUrl(new URL(spagoBInotifyAddress));
		notification.setHttp(http);
		notification.setAttrs(attrs);
		subscription.setNotification(notification);

		MutableDateTime dateTime = MutableDateTime.now();
		dateTime.add(DURATION_TYPE, SUBSCRIPTION_DURATION);
		subscription.setExpires(dateTime.toDate());

		subscription.setThrottling(THROTTLING);

		return JsonConverter.objectToJson(subscription, Subscription.class);
	}
}
