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

import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;

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
		requestHeaders.put(RestUtilities.CONTENT_TYPE, "application/x-www-form-urlencoded");
		RestUtilities.makeRequest(HttpMethod.Post, "http://localhost:8080/multi?g=h&y=i", requestHeaders, b);
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
			body = RestUtilities.readBody(req);
			// read twice
			body2 = RestUtilities.readBody(req);
		}

	}

}
