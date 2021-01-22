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
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.security.exceptions.LDAPAuthenticationFailed;
import it.eng.spagobi.security.exceptions.LDAPBindAsAnonymuousFailed;
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

	static protected String LDAP_AUTHENTICATION_CONFIG = "ldap.config";

	private static final String SEARCH_USER_BEFORE_PSW = "SEARCH_USER_BEFORE_PSW";
	private static final String SEARCH_USER_BEFORE_USER = "SEARCH_USER_BEFORE_USER";
	private static final String SEARCH_USER_BEFORE = "SEARCH_USER_BEFORE";
	private static final String SEARCH_USER_BEFORE_FILTER = "SEARCH_USER_BEFORE_FILTER";

	protected static int USER_JWT_TOKEN_EXPIRE_HOURS = 10; // JWT token for regular users will expire in 10 HOURS

	private String ldapPrefix = "";

	public LdapSecurityServiceSupplier() {

	}

	/**
	 * @param authModeFromUserAttribute
	 */
	public LdapSecurityServiceSupplier(String authModeFromUserAttribute) {
		this.ldapPrefix = authModeFromUserAttribute;
	}

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		logger.debug("IN: userId = [" + userId + "]");
		InitialDirContext ctx = null;
		try {
			Properties properties = getConfig();
			Boolean searchUserBefore = new Boolean(properties.getProperty(ldapPrefix + SEARCH_USER_BEFORE));
			String distinguishName = searchUserBefore ? findUserDistinguishName(userId) : userId;
			logger.debug("Binding with distinguishName [" + distinguishName + "] ...");
			try {
				ctx = bindWithCredentials(distinguishName, psw);
			} catch (LDAPAuthenticationFailed e) {
				throw e;
			}
			logger.debug("Authentication successfull for user [" + userId + "].");
			SpagoBIUserProfile toReturn = getUserProfile(userId);
			logger.debug("Profile object created for user [" + userId + "].");
			return toReturn;
		} catch (Exception e) {
			logger.error("LDAP authentication failed for user [" + userId + "]", e);
			return null;
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					logger.error("An error occurred while closing context", e);
				}
			}
		}
	}

	protected Properties getConfig() {
		try {
			String filename = System.getProperty(LDAP_AUTHENTICATION_CONFIG);
			Assert.assertNotNull(filename,
					"System property " + LDAP_AUTHENTICATION_CONFIG + " has not been configured. Please add it while starting your JVM.");
			Properties properties = new Properties();
			properties.load(new FileInputStream(filename));
			return properties;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occurred while retrieving LDAP configuration", e);
		}

	}

	private SpagoBIUserProfile getUserProfile(String userId) {

		Assert.assertNotNull(userId, "User id in input cannot be null");

		SpagoBIUserProfile profile = null;
		try {

			SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);
			if (user == null) {
				throw new SpagoBIRuntimeException("User [" + userId + "] not found into database");
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

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while getting user profile object from database", e);
		}
		return profile;
	}

	/**
	 * In this method you can find the authentication source code for LDAP bind process
	 *
	 * @param userId
	 * @param psw
	 * @return
	 * @throws NamingException
	 */

	protected InitialDirContext bindWithCredentials(String userId, String psw) throws LDAPAuthenticationFailed {

		Properties properties = getConfig();

		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, properties.getProperty(ldapPrefix + "INITIAL_CONTEXT_FACTORY"));
		env.put(Context.PROVIDER_URL, properties.getProperty(ldapPrefix + "PROVIDER_URL"));

		env.put(Context.SECURITY_AUTHENTICATION, properties.getProperty(ldapPrefix + "SECURITY_AUTHENTICATION"));

		String distinguishName = null;
		if (!userId.startsWith(properties.getProperty(ldapPrefix + "DN_PREFIX")) && !userId.endsWith(properties.getProperty(ldapPrefix + "DN_POSTFIX"))) {
			distinguishName = properties.getProperty(ldapPrefix + "DN_PREFIX") + userId + properties.getProperty(ldapPrefix + "DN_POSTFIX");
		} else {
			distinguishName = userId;
		}
		logger.debug("User distinguishName = [" + distinguishName + "]");
		env.put(Context.SECURITY_PRINCIPAL, distinguishName);
		env.put(Context.SECURITY_CREDENTIALS, psw);
		env.put("javax.security.sasl.qop", "auth-conf");
		env.put("javax.security.sasl.strength", "high");

		InitialDirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
		} catch (NamingException e) {
			throw new LDAPAuthenticationFailed("Authentication NOT successfull for user [" + userId + "]", e);
		}
		return ctx;
	}

	protected InitialDirContext bindAsAnonymuous() throws LDAPBindAsAnonymuousFailed {

		Properties properties = getConfig();

		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, properties.getProperty(ldapPrefix + "INITIAL_CONTEXT_FACTORY"));
		env.put(Context.PROVIDER_URL, properties.getProperty(ldapPrefix + "PROVIDER_URL"));

		env.put(Context.SECURITY_AUTHENTICATION, properties.getProperty(ldapPrefix + "SECURITY_AUTHENTICATION"));
		env.put("javax.security.sasl.qop", "auth-conf");
		env.put("javax.security.sasl.strength", "high");

		InitialDirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
		} catch (NamingException e) {
			throw new LDAPBindAsAnonymuousFailed("Anonymuous bind failed", e);
		}
		return ctx;
	}

	protected String findUserDistinguishName(String userId) throws LDAPBindAsAnonymuousFailed, LDAPAuthenticationFailed {

		Properties properties = getConfig();

		String toReturn = null;

		String postfix = properties.getProperty(ldapPrefix + "DN_POSTFIX");
		postfix = postfix.startsWith(",") ? postfix.substring(1) : postfix;

		String usernameForLDAPAuthBefore = new String(properties.getProperty(ldapPrefix + SEARCH_USER_BEFORE_USER));

		boolean validUsername = usernameForLDAPAuthBefore != null && !usernameForLDAPAuthBefore.isEmpty();

		String username = usernameForLDAPAuthBefore;
		String password = null;
		if (validUsername) {
			password = properties.getProperty(ldapPrefix + SEARCH_USER_BEFORE_PSW);
			logger.debug("Found credentials in properties file for authentication before looking for attribute.");
		}

		InitialDirContext ctx = null;
		try {
			if (validUsername) {
				ctx = bindWithCredentials(username, password);
			} else {
				ctx = bindAsAnonymuous();
			}

			String[] searchAttrs = { "nsRole", "uid", "objectClass", "givenName", "gn", "sn", "cn" };
			SearchControls ctrls = new SearchControls();
			ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			ctrls.setCountLimit(0);
			ctrls.setTimeLimit(0);
			ctrls.setReturningAttributes(searchAttrs);

			String filter = String.format(properties.getProperty(ldapPrefix + SEARCH_USER_BEFORE_FILTER), userId);

			NamingEnumeration<SearchResult> answer = ctx.search(postfix, filter, ctrls);

			if (!answer.hasMore()) {
				throw new SpagoBIRuntimeException("No user was found on LDAP using filter [" + filter + "]");
			}
			SearchResult ldapResult = answer.next();
			// distinguish name
			toReturn = ldapResult.getNameInNamespace();
			if (answer.hasMore()) {
				throw new SpagoBIRuntimeException("More than one user were found on LDAP using filter [" + filter + "]");
			}
		} catch (NamingException e) {
			throw new SpagoBIRuntimeException("Error while finding user distinguish name for user [" + userId + "]", e);
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					logger.error("An error occurred while closing context", e);
				}
			}
		}
		return toReturn;
	}

	@Override
	public SpagoBIUserProfile createUserProfile(String jwtToken) {
		return new InternalSecurityServiceSupplierImpl().createUserProfile(jwtToken);
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