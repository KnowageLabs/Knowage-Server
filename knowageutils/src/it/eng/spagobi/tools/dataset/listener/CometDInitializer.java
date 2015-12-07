/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
