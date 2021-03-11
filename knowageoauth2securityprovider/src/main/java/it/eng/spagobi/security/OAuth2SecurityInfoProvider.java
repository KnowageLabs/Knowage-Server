/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.security;

import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.security.OAuth2.OAuth2Client;
import it.eng.spagobi.security.OAuth2.OAuth2Config;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 *
 */
public class OAuth2SecurityInfoProvider implements ISecurityInfoProvider {

	static private Logger logger = Logger.getLogger(OAuth2SecurityInfoProvider.class);

	@Override
	public List getRoles() {
		logger.debug("IN");

		List<SbiTenant> tenants = DAOFactory.getTenantsDAO().loadAllTenants();

		List<Role> roles = new ArrayList<Role>();
		try {

			JSONObject jsonApplicationData = getRolesAsJson();

			JSONArray jsonRolesArray = jsonApplicationData.getJSONArray("roles");

			for (int i = 0; i < jsonRolesArray.length(); i++) {
				String name = jsonRolesArray.getJSONObject(i).getString("name");

				for (SbiTenant tenant : tenants) {
					Role role = new Role(name, name);
					role.setOrganization(tenant.getName());
					roles.add(role);
				}
			}
		} catch (JSONException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while trying to read JSon array containing the list of roles", e);
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

	// It looks for informations about the application, such its name and its
	// roles and it returns a json object containing them
	private static JSONObject getRolesAsJson() {
		try {

			OAuth2Client oauth2Client = new OAuth2Client();

			Properties config = OAuth2Config.getInstance().getConfig();

			// Retrieve the admin's token for REST services authentication
			String token = oauth2Client.getAdminToken();

			HttpClient httpClient = oauth2Client.getHttpClient();

			// Get roles of the application (specified in the
			// oauth2.config.properties)
			
			String rolesPath = config.getProperty("ROLES_PATH");
			rolesPath = MessageFormat.format(rolesPath, config.getProperty("APPLICATION_ID"));
			String url = config.getProperty("REST_BASE_URL") + rolesPath;
			GetMethod httpget = new GetMethod(url);
			httpget.addRequestHeader("X-Auth-Token", token);

			int statusCode = httpClient.executeMethod(httpget);
			byte[] response = httpget.getResponseBody();
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("Error while getting application information from IdM REST API: server returned statusCode = " + statusCode);
				LogMF.error(logger, "Server response is:\n{0}", new Object[] { new String(response) });
				throw new SpagoBIRuntimeException("Error while getting application information from IdM REST API: server returned statusCode = " + statusCode);
			}

			String responseStr = new String(response);
			LogMF.debug(logger, "Server response is:\n{0}", responseStr);
			JSONObject jsonObject = new JSONObject(responseStr);

			return jsonObject;

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting roles list from OAuth2 provider", e);
		}

	}

}
