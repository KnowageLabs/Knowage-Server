/*
CKANClient-J - Data Catalogue Software client in Java
Copyright (C) 2013 Newcastle University
Copyright (C) 2012 Open Knowledge Foundation

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.tools.dataset.ckan;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * Connection holds the connection details for this session
 *
 * @author Andrew Martin <andrew.martin@ncl.ac.uk>, Ross Jones <ross.jones@okfn.org>
 * @version 1.8
 * @since 2012-05-01
 */
public final class Connection {

	private static transient Logger logger = Logger.getLogger(Connection.class);

	private String host;
	private int port;
	private String apiKey;
	private String userId;

	/**
	 * Connects to a demo instance of CKAN if connection details are not provided. For debug purpose.
	 */
	private Connection() {
	}

	public Connection(String host) {
		this(host, 80);
	}

	public Connection(String host, int port) {
		this(host, port, null, null);
	}

	public Connection(String host, String apiKey, String userId) {
		this(host, 80, apiKey, userId);
	}

	public Connection(String host, int port, String apiKey, String userId) {
		this.host = host;
		this.port = port;
		this.apiKey = apiKey;
		this.userId = userId;

		try {
			logger.debug("Validating connection URI");
			URL u = new URL(this.host + ":" + this.port + "/api");
			logger.debug("Connection URI validated");
		} catch (MalformedURLException mue) {
			logger.debug("[FAILED] Connection URI is non valid");
			System.out.println(mue);
		}
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getUserId() {
		return userId;
	}
}