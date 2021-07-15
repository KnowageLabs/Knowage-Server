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

import java.util.Properties;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;

import it.eng.spagobi.security.exceptions.LDAPAuthenticationFailed;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class FullLdapSecurityServiceSupplier extends LdapSecurityServiceSupplier implements ISecurityServiceSupplier {

	static private Logger logger = Logger.getLogger(FullLdapSecurityServiceSupplier.class);

	protected static final String VALIDATION_FILTER = "VALIDATION_FILTER";

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

	private void validateUser(String userId, InitialDirContext ctx, String distinguishName) throws LDAPAuthenticationFailed {
		Properties properties = getConfig();
		String filter = properties.getProperty(ldapPrefix + VALIDATION_FILTER);
		try {
			if (filter != null && !filter.isEmpty()) {
				logger.debug("Validating user against filter: {" + filter + "}");
				SearchControls ctrls = new SearchControls();
				ctrls.setSearchScope(SearchControls.OBJECT_SCOPE);
				NamingEnumeration<SearchResult> answer = ctx.search(distinguishName, filter, ctrls);
				if (!answer.hasMoreElements()) {
					throw new LDAPAuthenticationFailed("Validation filter not satisfied for user [" + userId + "]");
				}
			}
		} catch (NamingException e) {
			throw new SpagoBIRuntimeException("Error while validating filter for user [" + userId + "]", e);
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

}
