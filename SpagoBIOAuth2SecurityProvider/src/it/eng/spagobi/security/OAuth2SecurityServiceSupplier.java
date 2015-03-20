/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.security;

import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 *
 */
public class OAuth2SecurityServiceSupplier implements ISecurityServiceSupplier {
	static private Logger logger = Logger.getLogger(OAuth2SecurityServiceSupplier.class);

	@Override
	public SpagoBIUserProfile createUserProfile(String userId) {
		logger.debug("IN");

		SpagoBIUserProfile profile;
		HttpsURLConnection connection = null;
		JsonReader jsonReader = null;
		try {
			URL url = new URL("https://account.lab.fiware.org/user?access_token=" + userId);
			connection = (HttpsURLConnection) url.openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);

			jsonReader = Json.createReader(connection.getInputStream());
			JsonObject jsonObject = jsonReader.readObject();

			profile = new SpagoBIUserProfile();
			profile.setUniqueIdentifier(Integer.toString(jsonObject.getInt("id")));
			profile.setUserId(Integer.toString(jsonObject.getInt("id")));
			profile.setUserName(jsonObject.getString("displayName"));
			profile.setOrganization("SPAGOBI");

			String adminEmail = null;
			String email = jsonObject.getString("email");

			ResourceBundle resourceBundle = null;
			String configFile = "it.eng.spagobi.security.OAuth2.configs";

			try {
				resourceBundle = ResourceBundle.getBundle(configFile);

				adminEmail = resourceBundle.getString("ADMIN_EMAIL");
			} catch (MissingResourceException e) {
				throw new SpagoBIRuntimeException("Impossible to find configurations file [" + configFile + "]", e);
			}

			profile.setIsSuperadmin(email.equals(adminEmail.toLowerCase()));

			JsonArray jsonRolesArray = jsonObject.getJsonArray("roles");
			List<String> roles = new ArrayList<String>();

			// Read roles
			String name;
			for (int i = 0; i < jsonRolesArray.size(); i++) {
				name = jsonRolesArray.getJsonObject(i).getString("name");
				if (!name.equals("Provider") && !name.equals("Purchaser"))
					roles.add(name);
			}

			// If no roles were found, search for roles in the organizations
			if (roles.size() == 0) {
				JsonArray organizations = jsonObject.getJsonArray("organizations");

				if (organizations != null) {
					// For each organization
					for (int i = 0; i < organizations.size(); i++) {
						String organizationName = organizations.getJsonObject(i).getString("displayName");
						jsonRolesArray = organizations.getJsonObject(i).getJsonArray("roles");

						// For each role in the current organization
						for (int k = 0; k < jsonRolesArray.size(); k++) {
							name = jsonRolesArray.getJsonObject(k).getString("name");

							if (!name.equals("Provider") && !name.equals("Purchaser")) {
								profile.setOrganization(organizationName);
								roles.add(name);
							}
						}
					}
				}
			}

			String[] rolesString = new String[roles.size()];
			profile.setRoles(roles.toArray(rolesString));

			HashMap<String, String> attributes = new HashMap<String, String>();
			attributes.put("displayName", jsonObject.getString("displayName"));
			attributes.put("email", email);
			profile.setAttributes(attributes);

			return profile;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while trying to obtain user information from fi-ware", e);
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

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SpagoBIUserProfile checkAuthenticationWithToken(String userId, String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkAuthorization(String userId, String function) {
		// TODO Auto-generated method stub
		return false;
	}

}
