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
package it.eng.spagobi.security.hmacfilter;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;

public class HMACFilterTest {

	public final static String key = "ki98";

	private static Server server;

	private final String body = "c=d&j=u";

	@BeforeClass
	public static void setUpClass() throws Exception {
		// Create Server
		startHMACFilterServer();
	}

	@AfterClass
	public static void teatDownClass() throws Exception {
		// Create Server
		stopHMACFilterServer();
	}

	public static void startHMACFilterServer() throws Exception {
		startHMACFilterServer(null);
	}

	public static void startHMACFilterServer(Class<? extends Filter> filterManInTheMiddle) throws Exception {
		server = new Server(8080);
		ServletContextHandler context = new ServletContextHandler();
		ServletHolder defaultServ = new ServletHolder("default", DummyServlet.class);
		context.addServlet(defaultServ, "/hmac");

		if (filterManInTheMiddle != null) {
			FilterHolder fh = new FilterHolder(filterManInTheMiddle);
			context.addFilter(fh, "/hmac", EnumSet.of(DispatcherType.REQUEST));
		}

		FilterHolder fh = new FilterHolder(HMACFilter.class);
		fh.setInitParameter(HMACFilter.KEY_CONFIG_NAME, key);
		context.addFilter(fh, "/hmac", EnumSet.of(DispatcherType.REQUEST));
		server.setHandler(context);
		server.start();
	}

	@Before
	public void setUp() {
		DummyServlet.arrived = false;
	}

	@Test
	public void testDoFilter() throws IOException, NoSuchAlgorithmException {
		Map<String, String> headers = new HashMap<String, String>();

		// test success get
		String token = "" + System.currentTimeMillis();
		headers.put(HMACFilter.HMAC_TOKEN_HEADER, token);
		headers.put(HMACFilter.HMAC_SIGNATURE_HEADER, getSignature("/hmac", "a=b", "", token));
		RestUtilities.makeRequest(HttpMethod.Get, "http://localhost:8080/hmac?a=b", headers, null);
		Assert.assertTrue(DummyServlet.arrived);
		DummyServlet.arrived = false;

		// test success post
		headers.put(HMACFilter.HMAC_TOKEN_HEADER, token);
		headers.put(HMACFilter.HMAC_SIGNATURE_HEADER, getSignature("/hmac", "", body, token));
		RestUtilities.makeRequest(HttpMethod.Post, "http://localhost:8080/hmac", headers, body);
		Assert.assertTrue(DummyServlet.arrived);
		DummyServlet.arrived = false;

		// test success post with params in URL
		headers.put(HMACFilter.HMAC_TOKEN_HEADER, token);
		headers.put(RestUtilities.CONTENT_TYPE, "application/x-www-form-urlencoded");
		headers.put(HMACFilter.HMAC_SIGNATURE_HEADER, getSignature("/hmac", "g=h&y=i", body, token));
		RestUtilities.makeRequest(HttpMethod.Post, "http://localhost:8080/hmac?g=h&y=i", headers, body);
		Assert.assertTrue(DummyServlet.arrived);
		DummyServlet.arrived = false;

		// test fail post
		headers.put(HMACFilter.HMAC_SIGNATURE_HEADER, "i0pe5");
		RestUtilities.makeRequest(HttpMethod.Post, "http://localhost:8080/hmac?g=h&y=i", headers, body);
		Assert.assertFalse(DummyServlet.arrived);

		// test success put with params in URL
		headers.put(HMACFilter.HMAC_TOKEN_HEADER, token);
		headers.put(RestUtilities.CONTENT_TYPE, "application/x-www-form-urlencoded");
		headers.put(HMACFilter.HMAC_SIGNATURE_HEADER, getSignature("/hmac", "g=h&y=i", body, token));
		RestUtilities.makeRequest(HttpMethod.Put, "http://localhost:8080/hmac?g=h&y=i", headers, body);
		Assert.assertTrue(DummyServlet.arrived);
	}

	@Test
	public void testDoFilterDelete() throws IOException, NoSuchAlgorithmException {
		Map<String, String> headers = new HashMap<String, String>();
		String token = "" + System.currentTimeMillis();
		headers.put(HMACFilter.HMAC_TOKEN_HEADER, token);

		// test success delete with params in URL
		headers.put(RestUtilities.CONTENT_TYPE, "application/x-www-form-urlencoded");
		// delete: body completely ignored
		headers.put(HMACFilter.HMAC_SIGNATURE_HEADER, getSignature("/hmac", "g=h&y=i", "", token));
		RestUtilities.makeRequest(HttpMethod.Delete, "http://localhost:8080/hmac?g=h&y=i", headers, body);
		Assert.assertTrue(DummyServlet.arrived);
	}

	@Test
	public void testDoFilterDeleteFailToken() throws IOException, NoSuchAlgorithmException {
		Map<String, String> headers = new HashMap<String, String>();
		// plus an hour
		String token = "" + (System.currentTimeMillis() + 3600000);
		headers.put(HMACFilter.HMAC_TOKEN_HEADER, token);

		// test success delete with params in URL
		headers.put(RestUtilities.CONTENT_TYPE, "application/x-www-form-urlencoded");
		// delete: body completely ignored
		headers.put(HMACFilter.HMAC_SIGNATURE_HEADER, getSignature("/hmac", "g=h&y=i", "", token));
		RestUtilities.makeRequest(HttpMethod.Delete, "http://localhost:8080/hmac?g=h&y=i", headers, body);
		Assert.assertFalse(DummyServlet.arrived);
	}

	private String getSignature(String queryPath, String paramsString, String body, String uniqueToken) throws IOException, NoSuchAlgorithmException {
		StringBuilder res = new StringBuilder(queryPath);
		res.append(paramsString);
		res.append(body);
		res.append(uniqueToken);
		res.append(key);
		String s = res.toString();

		return StringUtilities.sha256(s);
	}

	public static void stopHMACFilterServer() throws Exception {
		server.stop();
	}

}
