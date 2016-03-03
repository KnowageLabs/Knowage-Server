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
package it.eng.spagobi.tools.dataset.listener;

import java.io.IOException;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.cometd.bayeux.server.BayeuxServer;

/**
 * It's the initializer of the CometD Bayeux Server. It must be present in the web.xml. Also the cometd servlet must be present and mapped to a specified url
 * (in this case /cometd/*).
 * 
 * Example of changes of web.xml
 * 
 * <pre>
 *  <servlet>
 *     <servlet-name>cometDInitializer</servlet-name>
 *     <servlet-class>it.eng.spagobi.tools.dataset.listener.CometDInitializer</servlet-class>
 *     <load-on-startup>2</load-on-startup>
 *   </servlet>
 * 	<servlet>
 *     <servlet-name>cometd</servlet-name>
 *     <servlet-class>org.cometd.server.CometdServlet</servlet-class>
 *     <init-param>
 *       <param-name>ws.cometdURLMapping</param-name>
 *       <param-value>/cometd/*</param-value>
 *     </init-param>
 *     <load-on-startup>1</load-on-startup>
 *     <async-supported>true</async-supported>
 *   </servlet>
 *   <servlet-mapping>
 *     <servlet-name>cometd</servlet-name>
 *     <url-pattern>/cometd/*</url-pattern>
 *   </servlet-mapping>
 * </pre>
 * 
 * @author fabrizio
 *
 */
@SuppressWarnings("serial")
public class CometDInitializer extends GenericServlet {

	private static BayeuxServer server;

	public void init() throws ServletException {
		server = (BayeuxServer) getServletContext().getAttribute(BayeuxServer.ATTRIBUTE);
		CometDSpagoBIAuthenticationPolicy authenticator = new CometDSpagoBIAuthenticationPolicy();
		server.setSecurityPolicy(authenticator);
		CometDInitializerChecker.setCometdInitialized();
	}

	@Override
	public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
		throw new ServletException("It's not a real servlet, only for comet initialization");
	}

	public static BayeuxServer getServer() {
		return server;
	}

}
