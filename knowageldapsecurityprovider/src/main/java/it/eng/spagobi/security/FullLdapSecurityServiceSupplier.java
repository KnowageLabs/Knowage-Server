/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import it.eng.knowage.ldap.commons.LdapUser;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.security.exceptions.LDAPAuthenticationFailed;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class FullLdapSecurityServiceSupplier extends LdapSecurityServiceSupplier implements ISecurityServiceSupplier {

	static private Logger logger = Logger.getLogger(FullLdapSecurityServiceSupplier.class);

	private static final String AUTHENTICATION_FILTER = "AUTHENTICATION_FILTER";
	private static final String ROLES_ATTRIBUTE = "USER_ROLES_ATTRIBUTE_NAME";
	private static final String ROLES_FIELD = "USER_ROLES_ATTRIBUTE_FIELD";
	private static final String SUPERADMIN_ATTRIBUTE = "SUPERADMIN_ATTRIBUTE";

	private final Properties properties;

	public FullLdapSecurityServiceSupplier() {
		properties = getConfig();
	}

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		logger.debug("IN: userId = [" + userId + "]");
		try {
			Boolean searchUserBefore = new Boolean(properties.getProperty(ldapPrefix + SEARCH_USER_BEFORE));
			String distinguishName = searchUserBefore ? findUserDistinguishName(userId) : userId;
			logger.debug("Binding with distinguishName [" + distinguishName + "] ...");
			LdapUser ldapUser = bindLdapUserWithCredentials(userId, distinguishName, psw);
			logger.debug("Building profile object for user [" + userId + "]");
			SpagoBIUserProfile toReturn = getUserProfile(ldapUser);
			return toReturn;
		} catch (Exception e) {
			logger.error("LDAP authentication failed for user [" + userId + "]", e);
			return null;
		}
	}

	private LdapUser bindLdapUserWithCredentials(String userId, String distinguishName, String psw) throws LDAPAuthenticationFailed {
		Hashtable<String, Object> env = getContextEnv(distinguishName, psw);
		InitialDirContext ctx = null;
		Attributes ldapUserAttributes = null;
		try {
			// check credentials
			ctx = new InitialDirContext(env);
			// validate authentication filter
			String filter = properties.getProperty(ldapPrefix + AUTHENTICATION_FILTER);
			if (filter != null && !filter.isEmpty()) {
				logger.debug("Validating dn: {" + distinguishName + "} against filter: {" + filter + "}");
				SearchControls ctrls = new SearchControls();
				ctrls.setSearchScope(SearchControls.OBJECT_SCOPE);
				NamingEnumeration<SearchResult> answer = ctx.search(distinguishName, filter, ctrls);
				if (!answer.hasMoreElements()) {
					throw new LDAPAuthenticationFailed("Validation filter not satisfied for user [" + userId + "]");
				}
			}
			logger.debug("User [" + userId + "] validation successfully completed");
			// retrieve LDAP attributes
			ldapUserAttributes = ctx.getAttributes(distinguishName);
		} catch (NamingException e) {
			throw new LDAPAuthenticationFailed("Authentication NOT successfull for user [" + userId + "]", e);
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (NamingException e) {
					logger.error("An error occurred while closing context", e);
				}
			}
		}
		logger.debug("Authentication successfull for user [" + userId + "]");
		return new LdapUser(distinguishName, userId, ldapUserAttributes);
	}

	private Hashtable<String, Object> getContextEnv(String distinguishName, String psw) {
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, properties.getProperty(ldapPrefix + "INITIAL_CONTEXT_FACTORY"));
		env.put(Context.PROVIDER_URL, properties.getProperty(ldapPrefix + "PROVIDER_URL"));
		env.put(Context.SECURITY_AUTHENTICATION, properties.getProperty(ldapPrefix + "SECURITY_AUTHENTICATION"));
		env.put(Context.SECURITY_PRINCIPAL, distinguishName);
		env.put(Context.SECURITY_CREDENTIALS, psw);
		env.put("javax.security.sasl.qop", "auth-conf");
		env.put("javax.security.sasl.strength", "high");
		return env;
	}

	private SpagoBIUserProfile getUserProfile(LdapUser ldapUser) {
		SpagoBIUserProfile profile = new SpagoBIUserProfile();
		try {
			profile.setRoles(getRoles(ldapUser));
			profile.setAttributes(getProfileAttributes(ldapUser));
			profile.setUniqueIdentifier(getUserUniqueIdentifier(ldapUser.getUserId()));
			profile.setUserId(ldapUser.getUserId());
			profile.setUserName(ldapUser.getUserId());
			profile.setOrganization("DEFAULT_TENANT");
			profile.setIsSuperadmin(getIsSuperAdmin(ldapUser));
		} catch (NamingException e) {
			throw new SpagoBIRuntimeException("Error while building profile for user [" + ldapUser.getUserId() + "]", e);
		}
		logger.debug("Profile object created for user [" + ldapUser.getUserId() + "]");
		return profile;
	}

	private HashMap getProfileAttributes(LdapUser ldapUser) {
		Map<String, String> toReturn = new HashMap<String, String>();
		try {
			logger.debug("Getting profile attributes for user [" + ldapUser.getUserId() + "]");
			List<SbiAttribute> profileAttributes = DAOFactory.getSbiAttributeDAO().loadSbiAttributes();
			for (SbiAttribute attr : profileAttributes) {
				String attrName = attr.getAttributeName();
				logger.debug("Searching for attribute [" + attrName + "] in LDAP");
				Attribute attrValue = ldapUser.getAttribute(attrName);
				if (ldapUser.getAttribute(attrName) != null) {
					logger.debug("Adding new profile attribute to user profile: {" + attrName + "," + attrValue.toString() + "}");
					toReturn.put(attrName, attrValue.toString());
				}
			}
		} catch (EMFUserError e) {
			logger.error("Error while retrieving profile attributes, returning null.");
			return null;
		}
		return (HashMap) toReturn;
	}

	private String[] getRoles(LdapUser ldapUser) throws NamingException {
		List<String> toReturn = new ArrayList<String>();
		String rolesAttributeName = properties.getProperty(ldapPrefix + ROLES_ATTRIBUTE);
		IRoleDAO roleDao = DAOFactory.getRoleDAO();

		logger.debug("Getting roles for user [" + ldapUser.getUserId() + "]");
		NamingEnumeration<?> ldapRoles = ldapUser.getAttribute(rolesAttributeName).getAll();
		while (ldapRoles.hasMore()) {
			String entry = (String) ldapRoles.next();
			String ldapRole = getRoleFromLdapEntry(entry);
			logger.debug("Retrieved role: {" + ldapRole + "} from LDAP entry: {" + entry + "}");
			try {
				if (ldapRole != null && roleDao.loadByName(ldapRole) != null) {
					logger.debug("Adding role [" + ldapRole + "] to user profile");
					toReturn.add(ldapRole);
				}
			} catch (Exception e) {
				logger.debug("Skipping role {" + ldapRole + "} because of an exception:", e);
				continue;
			}
		}
		return toReturn.toArray(new String[0]);
	}

	private String getRoleFromLdapEntry(String entry) {
		String rolesField = properties.getProperty(ldapPrefix + ROLES_FIELD);
		try {
			String[] entries = entry.split(",");
			for (String e : entries) {
				String[] fields = e.split("=");
				if (fields[0].equals(rolesField)) {
					return fields[1];
				}
			}
		} catch (Exception e) {
			logger.warn("Error while getting role from LDAP entry {" + entry + "}");
			return null;
		}
		return null;
	}

	private String getUserUniqueIdentifier(String userId) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, USER_JWT_TOKEN_EXPIRE_HOURS);
		Date expiresAt = calendar.getTime();
		String jwtToken = JWTSsoService.userId2jwtToken(userId, expiresAt);
		return jwtToken;
	}

	private boolean getIsSuperAdmin(LdapUser ldapUser) {
		String superAdminLdapAttribute = properties.getProperty(ldapPrefix + SUPERADMIN_ATTRIBUTE);
		if (ldapUser.getAttribute(superAdminLdapAttribute) != null)
			return true;
		return false;
	}

}
