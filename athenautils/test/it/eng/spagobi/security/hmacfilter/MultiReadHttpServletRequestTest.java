package it.eng.spagobi.security.hmacfilter;

import it.eng.spagobi.RestUtilitiesTest;
import it.eng.spagobi.RestUtilitiesTest.HttpMethod;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MultiReadHttpServletRequestTest {

	private static String body2;
	private static String body;
	private static boolean arrived;
	private Server server;

	@Before
	public void setUp() throws Exception {
		// Create Server
		server = new Server(8080);
		ServletContextHandler context = new ServletContextHandler();
		ServletHolder defaultServ = new ServletHolder("default", DummyServlet.class);
		context.addServlet(defaultServ, "/multi");
		server.setHandler(context);
		server.start();
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}

	@Test
	public void test() throws Exception {
		// test success post with params in URL

		String b = "c=d";
		HashMap<String, String> requestHeaders = new HashMap<String, String>();
		requestHeaders.put(RestUtilitiesTest.CONTENT_TYPE, "application/x-www-form-urlencoded");
		RestUtilitiesTest.makeRequest(HttpMethod.Post, "http://localhost:8080/multi?g=h&y=i", requestHeaders, b);
		Assert.assertTrue(arrived);
		Assert.assertEquals(b, body);
		Assert.assertEquals(b, body2);
	}

	/**
	 * public for Jetty
	 *
	 * @author fabrizio
	 *
	 */
	@SuppressWarnings("serial")
	public static class DummyServlet extends HttpServlet {

		@Override
		protected void doPost(HttpServletRequest r, HttpServletResponse resp) throws ServletException, IOException {
			arrived = true;
			MultiReadHttpServletRequest req = new MultiReadHttpServletRequest(r);
			body = RestUtilitiesTest.readBody(req);
			// read twice
			body2 = RestUtilitiesTest.readBody(req);
		}

	}

}
