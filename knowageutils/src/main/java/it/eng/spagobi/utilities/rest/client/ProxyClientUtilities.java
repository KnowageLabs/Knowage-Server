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

package it.eng.spagobi.utilities.rest.client;


import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public final class ProxyClientUtilities {

	private static transient Logger logger = Logger.getLogger(ProxyClientUtilities.class);



	public static WebTarget getTarget(String url) {

		// Getting proxy properties set as JVM args
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		int proxyPortInt = portAsInteger(proxyPort);
		String proxyUsername = System.getProperty("http.proxyUsername");
		String proxyPassword = System.getProperty("http.proxyPassword");

		logger.debug("Setting REST client");


		ResteasyWebTarget target = null;


		if (proxyHost != null && proxyPortInt > 0) {
			if (proxyUsername != null && proxyPassword != null) {
				logger.debug("Setting proxy with authentication");
				Client client = new ResteasyClientBuilder().defaultProxy(proxyHost, proxyPortInt).establishConnectionTimeout(10, TimeUnit.SECONDS).build();
				target = (ResteasyWebTarget)client.register(new BasicAuthentication(proxyUsername, proxyPassword)).target(url);
				logger.debug("Proxy with authentication set");
			} else {
				// Username and/or password not acceptable. Trying to set proxy without credentials
				logger.debug("Setting proxy without authentication");
				Client client = new ResteasyClientBuilder().defaultProxy(proxyHost, proxyPortInt).establishConnectionTimeout(10, TimeUnit.SECONDS).build();
				target = (ResteasyWebTarget)client.target(url);
				logger.debug("Proxy without authentication set");
			}
		} else {
			Client client = new ResteasyClientBuilder().establishConnectionTimeout(10, TimeUnit.SECONDS).build();
			target = (ResteasyWebTarget)client.target(url);
			logger.debug("No proxy configuration found");
		}
		logger.debug("REST client set");

		return target;
	}

	private static Integer portAsInteger(String input) {
		logger.debug("Getting integer port from string");
		try {
			int port = Integer.parseInt(input);
			if (port <= 0)
				logger.debug("Integer lower than 1: it is not a valid port.", new Exception());
			logger.debug("Integer port obtained");
			return port;
		} catch (Exception e) {
			return 0;
		}
	}
}