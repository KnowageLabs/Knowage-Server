/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.security;

import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 *
 */
public class OAuth2SecurityInfoProvider implements ISecurityInfoProvider {
	static private Logger logger = Logger.getLogger(OAuth2SecurityInfoProvider.class);

	private static String adminEmail = null;
	private static String adminPassword = null;
	private static String applicationName = null;
	// Token for access admin information in fi-ware
	private static String token = null;
	// It contains data about the application, such its name and its roles
	private static JsonObject jsonApplicationData = null;
	private static List<String> tenants = null;

	@Override
	public List getRoles() {
		logger.debug("IN");
		// Initialize all the static properties if they have not been already initialized
		calculateJSonApplicationData();
		getTenants();

		List<Role> roles = new ArrayList<Role>();
		JsonArray jsonRolesArray = jsonApplicationData.getJsonArray("roles");

		String name;
		for (JsonValue jsonValue : jsonRolesArray) {
			name = ((JsonObject) jsonValue).getString("name");
			if (!name.equals("Provider") && !name.equals("Purchaser")) {
				Role role = new Role(name, name);
				role.setOrganization("SPAGOBI");
				roles.add(role);

				for (String tenant : tenants) {
					role = new Role(name, name);
					role.setOrganization(tenant);
					roles.add(role);
				}
			}
		}
		logger.debug("OUT");
		return roles;
	}

	@Override
	public List getAllProfileAttributesNames() {
		List<String> attributes = new ArrayList<String>();
		attributes.add("displayName");
		attributes.add("email");

		return attributes;
	}

	public static List<String> getTenants() {
		logger.debug("IN");
		if (tenants != null) {
			logger.debug("OUT");
			return tenants;
		}

		if (token == null) {
			loadConfigs();
		}

		tenants = new ArrayList<String>();

		HttpsURLConnection connection = null;
		JsonReader jsonReader = null;
		try {
			URL url = new URL("https://account.lab.fiware.org/applications/" + applicationName + "/actors?auth_token=" + token);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);

			jsonReader = Json.createReader(connection.getInputStream());
			JsonArray actorList = jsonReader.readObject().getJsonArray("actors");

			for (JsonValue jsonValue : actorList) {
				if (((JsonObject) jsonValue).getString("actor_type").equals("Group")) {
					tenants.add(((JsonObject) jsonValue).getString("name"));
				}
			}

			return tenants;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while trying to obtain tenants' informations from fi-ware", e);
		} finally {
			logger.debug("OUT");

			if (connection != null) {
				connection.disconnect();
			}
			if (jsonReader != null) {
				jsonReader.close();
			}
		}
	}

	// It looks for informations about the application, such its name and its roles and initialize jsonApplicationData with them
	private static void calculateJSonApplicationData() {
		if (jsonApplicationData == null) {
			logger.debug("IN");
			if (token == null) {
				loadConfigs();
			}

			HttpsURLConnection connection = null;
			JsonReader jsonReader = null;
			try {
				URL url = new URL("https://account.lab.fiware.org/applications/" + applicationName + ".json?auth_token=" + token);
				connection = (HttpsURLConnection) url.openConnection();
				connection.setRequestMethod("GET");

				connection.setConnectTimeout(10000);
				connection.setReadTimeout(10000);

				jsonReader = Json.createReader(connection.getInputStream());
				jsonApplicationData = jsonReader.readObject();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIRuntimeException("Error while trying to obtain application's informations from fi-ware", e);
			} finally {
				logger.debug("OUT");

				if (connection != null) {
					connection.disconnect();
				}
				if (jsonReader != null) {
					jsonReader.close();
				}
			}
		}

	}

	// It loads the authentication credentials used for retrieving the application's information and retrieve the token for the admin user
	private static void loadConfigs() {
		logger.debug("IN");

		ResourceBundle resourceBundle = null;
		String configFile = "it.eng.spagobi.security.OAuth2.configs";

		HttpsURLConnection connection = null;
		JsonReader jsonReader = null;
		try {
			resourceBundle = ResourceBundle.getBundle(configFile);

			applicationName = resourceBundle.getString("APPLICATION_NAME");
			adminEmail = resourceBundle.getString("ADMIN_EMAIL");
			adminPassword = resourceBundle.getString("ADMIN_PASSWORD");

			final String proxyUrl = resourceBundle.getString("PROXY_URL");
			final String proxyPort = resourceBundle.getString("PROXY_PORT");
			final String proxyUser = resourceBundle.getString("PROXY_USER");
			final String proxyPassword = resourceBundle.getString("PROXY_PASSWORD");

			if (proxyUrl != null && proxyPort != null) {
				System.setProperty("https.proxyHost", proxyUrl);
				System.setProperty("https.proxyPort", proxyPort);

				if (proxyUser != null && proxyPassword != null) {
					Authenticator authenticator = new Authenticator() {

						@Override
						public PasswordAuthentication getPasswordAuthentication() {
							return (new PasswordAuthentication(proxyUser, proxyPassword.toCharArray()));
						}
					};
					Authenticator.setDefault(authenticator);
				}
			}

			URL url = new URL("https://account.lab.fiware.org/api/v1/tokens.json");

			// HttpsURLConnection
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("POST");

			connection.setDoOutput(true);
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);

			OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
			String body = "email=" + adminEmail + "&password=" + adminPassword;
			out.write(body);
			out.close();

			jsonReader = Json.createReader(connection.getInputStream());
			JsonObject jsonObject = jsonReader.readObject();

			token = jsonObject.getString("token");
		} catch (MissingResourceException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRuntimeException("Impossible to find configurations file [" + configFile + "]", e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while trying to contact fi-ware", e);
		} finally {
			logger.debug("OUT");

			if (connection != null) {
				connection.disconnect();
			}
			if (jsonReader != null) {
				jsonReader.close();
			}
		}
	}

}
