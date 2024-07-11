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

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Condition.post;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.httpclient.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.semantics.Predicate;
import com.xebialabs.restito.server.StubServer;

import it.eng.spagobi.security.hmacfilter.HMACSecurityException;
import it.eng.spagobi.tools.dataset.bo.RESTDataSet;
import it.eng.spagobi.tools.dataset.bo.RESTDataSetTest;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.HelperForTest;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import junit.framework.TestCase;

/**
 * To run these tests you need another machine with Orion Context Broker installed)
 *
 * @author fabrizio
 *
 */
public class OrionContextSubscriberTest extends TestCase {

	private static final String VM_URL = "http://192.168.2.137:1026";
	private static final String LOCAL_URL = "http://192.168.93.1:8090";

	private static final String VM_URL_SUBSCRIBE = VM_URL + "/v1/subscribeContext";
	private static final String VM_URL_UPDATE = VM_URL + "/v1/updateContext";
	private static final String LOCAL_URL_NOTIFY = LOCAL_URL + "/notify";

	private OrionContextSubscriber subscriber;
	private StubServer server;

	@Override
	protected void setUp() throws Exception {
		server = new StubServer(8090).run();
		RESTDataSet dataSet = getRestDataSetOrion();
		dataSet.setUserIn("u1");
		dataSet.setLabel("lab1");
		subscriber = new OrionContextSubscriber(dataSet, UserProfileManager.getProfile(), LOCAL_URL_NOTIFY);
	}

	@Override
	protected void tearDown() throws Exception {
		server.stop();
	}

	/**
	 * Example:
	 *
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
	 * @throws InterruptedException
	 *
	 * @throws JSONException
	 * @throws IOException
	 * @throws HttpException
	 * @throws HMACSecurityException
	 *
	 */
	public void testSendSubscription() throws InterruptedException, JSONException, HttpException, IOException, HMACSecurityException {
		SaveBodyCondition sbc = new SaveBodyCondition();
		whenHttp(server).match(sbc, post("/notify")).then(ok());
		String subId = subscriber.sendSubscription();
		System.out.println(subId);
		assertTrue(subId.length() > 10);
		updateOrion();
		Thread.sleep(500); // sleep to wait Orion notification
		/**
		 * <pre>
		 * {
		 *   "subscriptionId" : "55dae93ff23205eb4241ccd0",
		 *   "originator" : "localhost",
		 *   "contextResponses" : [
		 *     {
		 *       "contextElement" : {
		 *         "type" : "Meter",
		 * </pre>
		 */
		String rb = sbc.requestBody;
		System.out.println(rb);
		JSONObject rbJson = new JSONObject(rb);
		JSONArray crs = rbJson.getJSONArray("contextResponses");
		JSONObject cr = crs.getJSONObject(0);
		JSONObject el = cr.getJSONObject("contextElement");
		assertEquals("Meter", el.get("type"));
	}

	private void updateOrion() throws HttpException, IOException, HMACSecurityException {
		RestUtilities.makeRequest(HttpMethod.Post, VM_URL_UPDATE, RestUtilities.getJSONHeaders(), HelperForTest.readFile("update-orion.json", getClass()));
	}

	private static class SaveBodyCondition extends Condition {
		private String requestBody;

		public SaveBodyCondition() {
			super(null);

		}

		@Override
		public Predicate<Call> getPredicate() {
			return new Predicate<>() {

				@Override
				public boolean apply(Call c) {
					requestBody = c.getPostBody();
					return true;
				}
			};
		}

	}

	public void testGetOrionSubscriptionBaseAddress() throws MalformedURLException {
		String o = subscriber.getOrionSubscriptionBaseAddress();
		assertEquals(o + "/v1/subscribeContext", VM_URL_SUBSCRIBE);
	}

	public static RESTDataSet getRestDataSetOrion() throws IOException {
		String conf = HelperForTest.readFile("restdataset-conf-orion.json", OrionContextSubscriberTest.class);
		return RESTDataSetTest.getRestDataSet(conf);

	}

}
