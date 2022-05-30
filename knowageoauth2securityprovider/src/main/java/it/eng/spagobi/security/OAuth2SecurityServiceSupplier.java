/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.security.OAuth2.OAuth2Client;
import it.eng.spagobi.security.OAuth2.OAuth2Config;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 *
 */
public class OAuth2SecurityServiceSupplier implements ISecurityServiceSupplier {

	static private Logger logger = Logger.getLogger(OAuth2SecurityServiceSupplier.class);

	@Override
	public SpagoBIUserProfile createUserProfile(String userUniqueIdentifier) {
		logger.debug("IN");

		SpagoBIUserProfile profile;
		try {
			OAuth2Config config = OAuth2Config.getInstance();

			OAuth2Client oauth2Client = new OAuth2Client();

			HttpClient httpClient = oauth2Client.getHttpClient();

			// We call the OAuth2 provider to get user's info
			GetMethod httpget = new GetMethod(config.getUserInfoUrl() + "?access_token=" + userUniqueIdentifier);
			int statusCode = httpClient.executeMethod(httpget);
			byte[] response = httpget.getResponseBody();
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("Error while getting user information from OAuth2 provider: server returned statusCode = " + statusCode);
				LogMF.error(logger, "Server response is:\n{0}", new Object[] { new String(response) });
				throw new SpagoBIRuntimeException("Error while getting user information from OAuth2 provider: server returned statusCode = " + statusCode);
			}

			String responseStr = new String(response);
			LogMF.debug(logger, "Server response is:\n{0}", responseStr);
			JSONObject jsonObject = new JSONObject(responseStr);

			String userId = jsonObject.getString("id");
			logger.debug("User id is [" + userId + "]");
			String userName = jsonObject.getString("displayName");
			logger.debug("User name is [" + userName + "]");

			profile = new SpagoBIUserProfile();
			profile.setUniqueIdentifier(userUniqueIdentifier); // The OAuth2
																// access token
			profile.setUserId(userId);
			profile.setUserName(userName);
			profile.setOrganization("DEFAULT_TENANT");

			/*
			 * If the user's email is the same as the owner of the application (as configured in the oauth2.config.properties file) we consider him as the
			 * superadmin
			 */
			String adminEmail = config.getAdminEmail();
			String email = jsonObject.getString("email");
			profile.setIsSuperadmin(email.equalsIgnoreCase(adminEmail));

			JSONArray jsonRolesArray = jsonObject.getJSONArray("roles");
			List<String> roles = new ArrayList<String>();

			// Read roles
			String name;
			for (int i = 0; i < jsonRolesArray.length(); i++) {
				name = jsonRolesArray.getJSONObject(i).getString("name");
				if (!name.equalsIgnoreCase("provider") && !name.equalsIgnoreCase("purchaser"))
					roles.add(name);
			}

			// If no roles were found, search for roles in the organizations
			if (roles.size() == 0 && !jsonObject.isNull("organizations")) {
				JSONArray organizations = jsonObject.getJSONArray("organizations");

				if (organizations != null) { // TODO: more than one organization
					// For each organization
					for (int i = 0; i < organizations.length() && roles.size() == 0; i++) {
						String organizationName = organizations.getJSONObject(i).getString("name");
						jsonRolesArray = organizations.getJSONObject(i).getJSONArray("roles");

						// For each role in the current organization
						for (int k = 0; k < jsonRolesArray.length(); k++) {
							name = jsonRolesArray.getJSONObject(k).getString("name");

							if (!name.equalsIgnoreCase("provider") && !name.equalsIgnoreCase("purchaser")) {
								profile.setOrganization(organizationName);
								roles.add(name);
							}
						}
					}
				}
			}

			if (roles.size() == 0) { // Add the default role
				roles.add(SingletonConfig.getInstance().getConfigValue("SPAGOBI.SECURITY.DEFAULT_ROLE_ON_SIGNUP"));
			}

			String[] rolesString = new String[roles.size()];
			profile.setRoles(roles.toArray(rolesString));

			HashMap<String, String> attributes = new HashMap<String, String>();
			attributes.put("userUniqueIdentifier", userUniqueIdentifier);
			attributes.put("userId", userId);
			attributes.put("userName", userName);
			attributes.put("email", email);
			profile.setAttributes(attributes);

			return profile;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while trying to read JSon object containing user profile's information from OAuth2 provider", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		OAuth2Client oauth2Client = new OAuth2Client();
		return createUserProfile(oauth2Client.getAccessToken(userId, psw));
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

	@Override
	public SpagoBIUserProfile checkAuthenticationToken(String token) {
		return createUserProfile(token);
	}

}
