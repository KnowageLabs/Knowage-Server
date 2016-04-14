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
package it.eng.spagobi.utilities.engines.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.eng.spagobi.security.hmacfilter.DummyServlet;
import it.eng.spagobi.security.hmacfilter.HMACFilterTest;

public class SimpleRestClientTest {

	@BeforeClass
	public static void setUpClass() throws Exception {
		// Create Server
		HMACFilterTest.startHMACFilterServer();
	}

	@AfterClass
	public static void teatDownClass() throws Exception {
		// Create Server
		HMACFilterTest.stopHMACFilterServer();
	}

	@Before
	public void setUp() throws Exception {
		DummyServlet.arrived = false;
		DummyServlet.body = null;
	}

	@Test
	public void testExecuteGetService() throws Exception {
		SimpleRestClient client = getSimpleRestClient();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("a", "b");
		parameters.put("c", "d");
		ClientResponse<?> resp = client.executeGetService(parameters, "http://localhost:8080/hmac", "biadmin");
		Assert.assertEquals(200, resp.getStatus());
		Assert.assertTrue(DummyServlet.arrived);
	}

	@Test
	public void testExecuteGetServiceFail() throws Exception {
		SimpleRestClient client = getSimpleRestClientFail();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("a", "b");
		parameters.put("c", "d");
		boolean done = false;

		try {
			client.executeGetService(parameters, "http://localhost:8080/hmac", "biadmin");
		} catch (Exception e) {
			done = true;
		}
		Assert.assertTrue(done);
		Assert.assertFalse(DummyServlet.arrived);
	}

	protected SimpleRestClient getSimpleRestClient() {
		SimpleRestClient client = new SimpleRestClient(HMACFilterTest.key) {
			@Override
			protected HttpClient getHttpClient() {
				return null;
			}
		};
		return client;
	}

	protected SimpleRestClient getSimpleRestClientFail() {
		SimpleRestClient client = new SimpleRestClient("qw9") {
			@Override
			protected HttpClient getHttpClient() {
				return null;
			}
		};
		return client;
	}

	@Test
	public void testExecutePostService() throws Exception {
		SimpleRestClient client = getSimpleRestClient();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("a", "b");
		parameters.put("c", "d");
		ClientResponse<?> resp = client.executePostService(parameters, "http://localhost:8080/hmac", "biadmin", MediaType.TEXT_PLAIN_TYPE, "etc.17");
		Assert.assertEquals(200, resp.getStatus());
		Assert.assertTrue(DummyServlet.arrived);
	}

	@Test
	public void testExecutePostServiceFail() throws Exception {
		SimpleRestClient client = getSimpleRestClientFail();
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("a", "b");
		parameters.put("c", "d");
		boolean done = false;

		try {
			client.executePostService(parameters, "http://localhost:8080/hmac", "biadmin", MediaType.TEXT_PLAIN_TYPE, "etc.17");
		} catch (Exception e) {
			done = true;
		}
		Assert.assertTrue(done);
		Assert.assertFalse(DummyServlet.arrived);
	}

}
