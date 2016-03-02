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
package it.eng.spagobi.commons.initializers.metadata;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OAuth2TenantInitializer extends SpagoBIInitializer {
	static private Logger logger = Logger.getLogger(OAuth2TenantInitializer.class);
	String applicationName;

	// It retrieves organizations associated with fi-ware application. If they are not inside the database, it stores them in it
	@Override
	public void init(SourceBean config, Session hibernateSession) {
		logger.debug("IN");
		try {
			String hql = "from SbiTenant";
			Query hqlQuery = hibernateSession.createQuery(hql);
			List<SbiTenant> existingTenants = hqlQuery.list();

			List<String> configuredTenants = getTenants();
			for (String aConfiguredTenant : configuredTenants) {
				if (exists(aConfiguredTenant, existingTenants)) {
					LogMF.debug(logger, "Tenant {0} already exists", aConfiguredTenant);
				} else {
					LogMF.info(logger, "Tenant {0} does not exist. It will be inserted", aConfiguredTenant);
					writeTenant(aConfiguredTenant, hibernateSession);
					LogMF.debug(logger, "Tenant {0} was inserted", aConfiguredTenant);
				}
			}
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			throw new SpagoBIRuntimeException("An unexpected error occured while initializing Tenants", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private boolean exists(String aConfiguredTenant, List<SbiTenant> existingTenants) {
		for (SbiTenant aTenant : existingTenants) {
			if (aTenant.getName().equals(aConfiguredTenant)) {
				return true;
			}
		}
		return false;
	}

	private void writeTenant(String tenantName, Session hibernateSession) throws Exception {
		logger.debug("IN");
		SbiTenant aTenant = new SbiTenant();
		aTenant.setName(tenantName);
		logger.debug("Inserting tenant with name = [" + tenantName + "]...");
		hibernateSession.save(aTenant);
		logger.debug("OUT");
	}

	private List<String> getTenants() {
		logger.debug("IN");

		String token = loadConfigs();
		List<String> tenants = new ArrayList<String>();

		HttpsURLConnection connection = null;
		InputStream is = null;
		BufferedReader reader = null;
		try {
			URL url = new URL("https://account.lab.fiware.org/applications/" + applicationName + "/actors?auth_token=" + token);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);

			is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}

			JSONObject jsonObject = new JSONObject(stringBuilder.toString());
			JSONArray actorList = jsonObject.getJSONArray("actors");

			JSONObject obj;
			for (int i = 0; i < actorList.length(); i++) {
				obj = actorList.getJSONObject(i);
				if (obj.getString("actor_type").equals("Group")) {
					tenants.add(obj.getString("name"));
				}
			}

			return tenants;
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while trying to read JSon array containing the list of tenants", e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while trying to obtain tenants' informations from fi-ware", e);
		} finally {
			logger.debug("OUT");

			if (connection != null) {
				connection.disconnect();
			}
			try {
				if (reader != null) {
					reader.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String loadConfigs() {
		logger.debug("IN");

		ResourceBundle resourceBundle = null;
		String configFile = "it.eng.spagobi.security.OAuth2.configs";

		HttpsURLConnection connection = null;
		InputStream is = null;
		BufferedReader reader = null;
		try {
			resourceBundle = ResourceBundle.getBundle(configFile);

			applicationName = resourceBundle.getString("APPLICATION_NAME");
			String adminEmail = resourceBundle.getString("ADMIN_EMAIL");
			String adminPassword = resourceBundle.getString("ADMIN_PASSWORD");

			final String proxyUrl = resourceBundle.getString("PROXY_URL");
			final String proxyPort = resourceBundle.getString("PROXY_PORT");
			final String proxyUser = resourceBundle.getString("PROXY_USER");
			final String proxyPassword = resourceBundle.getString("PROXY_PASSWORD");

			if (!proxyUrl.equals("")) {
				System.setProperty("https.proxyHost", proxyUrl);
				System.setProperty("https.proxyPort", proxyPort);

				if (!proxyUser.equals("")) {
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

			is = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is));
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
			}

			JSONObject jsonObject = new JSONObject(stringBuilder.toString());
			return jsonObject.getString("token");
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while trying to read JSon object containing access token", e);
		} catch (MissingResourceException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRuntimeException("Impossible to find the specified resource inside the configurations file [" + configFile + "]", e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while trying to contact fi-ware", e);
		} finally {
			logger.debug("OUT");

			if (connection != null) {
				connection.disconnect();
			}
			try {
				if (reader != null) {
					reader.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
