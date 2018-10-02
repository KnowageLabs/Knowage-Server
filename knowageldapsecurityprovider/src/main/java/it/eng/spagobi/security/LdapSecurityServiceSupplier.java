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

package it.eng.spagobi.security;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This class implement the authentication method on LDAP but the authorization method on Knowage Repository
 *
 */
public class LdapSecurityServiceSupplier implements ISecurityServiceSupplier {

	static private Logger logger = Logger.getLogger(LdapSecurityServiceSupplier.class);

	static private String LDAP_AUTHENTICATION_CONFIG = "ldap.config";

	private static int USER_JWT_TOKEN_EXPIRE_HOURS = 10; // JWT token for regular users will expire in 10 HOURS

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		SpagoBIUserProfile profile = null;
		try {
			if (userId != null) {
				SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);
				if (user != null) {
					if (!bind(userId, psw)) {
						logger.error("Impossible to bind user " + userId + ". Usernmae or password password not valid.");
						return null;
					}

					profile = new SpagoBIUserProfile();

					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.HOUR, USER_JWT_TOKEN_EXPIRE_HOURS);
					Date expiresAt = calendar.getTime();

					String jwtToken = JWTSsoService.userId2jwtToken(userId, expiresAt);

					profile.setUniqueIdentifier(jwtToken);
					profile.setUserId(user.getUserId());
					profile.setUserName(user.getFullName());
					profile.setOrganization(user.getCommonInfo().getOrganization());
					profile.setIsSuperadmin(user.getIsSuperadmin());
				} else {
					logger.error("UserName " + userId + " not found into database. Returning [null] as user profile");
				}

			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user profile object from database", e);
		}
		return profile;
	}

	/**
	 * In this method you can find the authentication source code for LDAP bind process
	 *
	 * @param userId:
	 *            ex: CN=angelo,OU=ADAM USERS,O=Microsoft,C=US
	 * @param psw
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */

	protected boolean bind(String userId, String psw) throws FileNotFoundException, IOException {

		String filename = System.getProperty(LDAP_AUTHENTICATION_CONFIG);
		Assert.assertNotNull(filename, "System property " + LDAP_AUTHENTICATION_CONFIG + " has not been configured. Please add it while starting your JVM.");
		Properties properties = new Properties();
		properties.load(new FileInputStream(filename));

		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, properties.getProperty("INITIAL_CONTEXT_FACTORY"));
		env.put(Context.PROVIDER_URL, properties.getProperty("PROVIDER_URL"));

		env.put(Context.SECURITY_AUTHENTICATION, properties.getProperty("SECURITY_AUTHENTICATION"));
		env.put(Context.SECURITY_PRINCIPAL, properties.getProperty("DN_PREFIX") + userId + properties.getProperty("DN_POSTFIX"));
		logger.debug("User ID=" + properties.getProperty("DN_PREFIX") + userId + properties.getProperty("DN_POSTFIX"));
		env.put(Context.SECURITY_CREDENTIALS, psw);

		env.put("javax.security.sasl.qop", "auth-conf");
		env.put("javax.security.sasl.strength", "high");

		try {
			new InitialDirContext(env);
			logger.debug("Authentication successfull.");
			return true;
		} catch (NamingException e) {
			logger.error("Authentication NOT successfull. Reason: ", e);

		}
		return false;
	}

	@Override
	public SpagoBIUserProfile createUserProfile(String userId) {
		logger.debug("IN: userId parameter is " + userId);
		try {
			SpagoBIUserProfile profile = getSpagoBIUserProfile(userId);
			return profile;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while creating user profile object", e);
		} finally {
			logger.debug("OUT");
		}
	}

	public SpagoBIUserProfile getSpagoBIUserProfile(String userId) throws EMFUserError {
		logger.debug("IN - userId: " + userId);
		SpagoBIUserProfile profile = null;
		SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);

		if (user == null) {
			logger.error("UserName [" + userId + "] not found!!");
			return null;
		}

		profile = new SpagoBIUserProfile();
		profile.setUniqueIdentifier(user.getUserId());
		profile.setUserId(user.getUserId());
		profile.setUserName(user.getFullName());
		profile.setOrganization(user.getCommonInfo().getOrganization());
		profile.setIsSuperadmin(user.getIsSuperadmin());

		// get user name
		String userName = userId;

		// get roles of the user
		List<SbiExtRoles> assignedUserRoles = DAOFactory.getSbiUserDAO().loadSbiUserRolesById(user.getId());
		List<String> roles = new ArrayList<>();

		for (SbiExtRoles role : assignedUserRoles) {
			roles.add(role.getName());
		}

		HashMap<String, String> attributes = new HashMap<>();
		ArrayList<SbiUserAttributes> assignedUserAttributes = DAOFactory.getSbiUserDAO().loadSbiUserAttributesById(user.getId());
		if (assignedUserAttributes != null) {
			for (SbiUserAttributes attribute : assignedUserAttributes) {
				String attributeName = attribute.getSbiAttribute().getAttributeName();
				String attributeValue = attribute.getAttributeValue();
				if (attributeValue != null) {
					logger.debug("Add attribute. " + attributeName + "=" + attributeName + " to the user" + userName);
					attributes.put(attributeName, attributeValue);
				}
			}
		}

		logger.debug("Attributes load into Knowage user profile: " + attributes);
		// end load profile attributes

		profile.setAttributes(attributes);
		profile.setRoles(roles.toArray(new String[0]));

		logger.debug("OUT");
		return profile;

	}

	@Override
	public SpagoBIUserProfile checkAuthenticationWithToken(String userId, String token) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean checkAuthorization(String userId, String function) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SpagoBIUserProfile checkAuthenticationToken(String token) {
		throw new UnsupportedOperationException();
	}

}