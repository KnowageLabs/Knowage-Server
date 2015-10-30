package it.eng.spagobi.utilities.engines.rest;

import it.eng.spagobi.security.hmacfilter.DummyServlet;
import it.eng.spagobi.security.hmacfilter.HMACFilterTest;

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
		ClientResponse<?> resp = client.executeGetService(parameters, "http://localhost:8080/hmac");
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
			client.executeGetService(parameters, "http://localhost:8080/hmac");
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
		ClientResponse<?> resp = client.executePostService(parameters, "http://localhost:8080/hmac", MediaType.TEXT_PLAIN_TYPE, "etc.17");
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
			client.executePostService(parameters, "http://localhost:8080/hmac", MediaType.TEXT_PLAIN_TYPE, "etc.17");
		} catch (Exception e) {
			done = true;
		}
		Assert.assertTrue(done);
		Assert.assertFalse(DummyServlet.arrived);
	}

}
