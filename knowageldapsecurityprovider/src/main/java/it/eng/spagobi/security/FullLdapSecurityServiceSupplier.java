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
import java.util.List;
import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
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

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		logger.debug("IN: userId = [" + userId + "]");
		InitialDirContext ctx = null;
		try {
			Properties properties = getConfig();
			Boolean searchUserBefore = new Boolean(properties.getProperty(ldapPrefix + SEARCH_USER_BEFORE));
			String distinguishName = searchUserBefore ? findUserDistinguishName(userId) : userId;
			logger.debug("Binding with distinguishName [" + distinguishName + "] ...");
			ctx = bindWithCredentials(distinguishName, psw);
			validateUser(userId, ctx, distinguishName);
			logger.debug("Authentication successfull for user [" + userId + "].");
			SpagoBIUserProfile toReturn = getUserProfile(userId, ctx, distinguishName);
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

	private void validateUser(String userId, InitialDirContext ctx, String distinguishName) throws LDAPAuthenticationFailed {
		Properties properties = getConfig();
		String filter = properties.getProperty(ldapPrefix + AUTHENTICATION_FILTER);
		try {
			if (filter != null && !filter.isEmpty()) {
				logger.debug("Validating dn: {" + distinguishName + "} against filter: {" + filter + "}");
				SearchControls ctrls = new SearchControls();
				ctrls.setSearchScope(SearchControls.OBJECT_SCOPE);
				NamingEnumeration<SearchResult> answer = ctx.search(distinguishName, filter, ctrls);
				if (!answer.hasMoreElements()) {
					throw new LDAPAuthenticationFailed("Validation filter not satisfied for user [" + userId + "]");
				}
			}
		} catch (NamingException e) {
			throw new SpagoBIRuntimeException("Error while validating filter for user [" + userId + "]", e);
		}
	}

	private SpagoBIUserProfile getUserProfile(String userId, InitialDirContext ctx, String distinguishName) {
		SpagoBIUserProfile profile = new SpagoBIUserProfile();
		Properties properties = getConfig();
		String ldapAttributeName = properties.getProperty(ldapPrefix + ROLES_ATTRIBUTE);
		try {
			NamingEnumeration<?> ldapAttributes = ctx.getAttributes(distinguishName).get(ldapAttributeName).getAll();
			List<String> ldapRoles = getLdapRoles(ldapAttributes);
			profile.setRoles(toKnowageRoles(ldapRoles));
			profile.setUniqueIdentifier(getUserUniqueIdentifier(userId));
			profile.setUserId(userId);
			profile.setUserName(userId);
			profile.setOrganization("DEFAULT_TENANT");
			profile.setIsSuperadmin(getIsSuperadmin(ldapRoles));
		} catch (NamingException e) {
			throw new SpagoBIRuntimeException("Error while building profile for user [" + userId + "]", e);
		}
		return profile;
	}

	private String[] toKnowageRoles(List<String> ldapRoles) {
		List<String> toReturn = new ArrayList<String>();
		IRoleDAO roleDao = DAOFactory.getRoleDAO();
		for (String role : ldapRoles) {
			try {
				if (roleDao.loadByName(role) != null) {
					toReturn.add(role);
				}
			} catch (Exception e) {
				logger.debug("Skipping role {" + role + "}", e);
				continue;
			}
		}
		return toReturn.toArray(new String[0]);
	}

	private List<String> getLdapRoles(NamingEnumeration<?> rolesList) throws NamingException {
		List<String> toReturn = new ArrayList<String>();
		while (rolesList.hasMore()) {
			String entry = (String) rolesList.next();
			String ldapRole = getAttributeFromLdapEntry(entry);
			logger.debug("Retrieved role: {" + ldapRole + "} from LDAP");
			try {
				if (ldapRole != null) {
					toReturn.add(ldapRole);
				}
			} catch (Exception e) {
				logger.debug("Skipping role {" + ldapRole + "}", e);
				continue;
			}
		}
		return toReturn;
	}

	private String getAttributeFromLdapEntry(String entry) {
		Properties properties = getConfig();
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

	private boolean getIsSuperadmin(List<String> ldapRoles) {
		Properties properties = getConfig();
		String superAdminLdapAttribute = properties.getProperty(ldapPrefix + SUPERADMIN_ATTRIBUTE);
		if (ldapRoles.stream().anyMatch(s -> s.equalsIgnoreCase(superAdminLdapAttribute)))
			return true;
		return false;
	}

}
