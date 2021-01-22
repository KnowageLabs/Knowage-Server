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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.profiling.bean.SbiUserAttributes;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;

public class ProfiledLdapSecurityServiceSupplier implements ISecurityServiceSupplier {

	static private Logger logger = Logger.getLogger(ProfiledLdapSecurityServiceSupplier.class);

	private InternalSecurityServiceSupplierImpl internalSecurityServiceSupplierImpl = new InternalSecurityServiceSupplierImpl();

	private static final String ATTRIBUTE_AUTHENTICATION_MODE = "auth_mode";
	private static final String ATTRIBUTE_AUTHENTICATION_MODE_LDAP = "LDAP";
	private static final String ATTRIBUTE_AUTHENTICATION_MODE_INTERNAL = "internal";

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {

		SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);
		if (user == null) {
			logger.error("UserName [" + userId + "] not found into database");
			return null;
		} else {
			String authModeValueFromUserAttribute = getAuthMode(user);
			logger.warn("auth_mode=" + authModeValueFromUserAttribute);

			if (ATTRIBUTE_AUTHENTICATION_MODE_INTERNAL.equals(authModeValueFromUserAttribute)) {
				return internalSecurityServiceSupplierImpl.checkAuthentication(userId, psw);
			}

			String ldapPrefix = StringUtils.isBlank(authModeValueFromUserAttribute) ? "" : String.format("%s.", authModeValueFromUserAttribute);
			SpagoBIUserProfile spagoBIUserProfile = new LdapSecurityServiceSupplier(ldapPrefix).checkAuthentication(userId, psw);

			/* To be backwards compatible with old versions */
			if (spagoBIUserProfile == null && ATTRIBUTE_AUTHENTICATION_MODE_LDAP.equals(authModeValueFromUserAttribute)) {
				String message = String.format(
						"auth_mode = [%s]. No LDAP found using prefix [%s]. Trying to use default empty prefix for backwards compatibility.",
						authModeValueFromUserAttribute, ldapPrefix);
				logger.warn(message);
				return new LdapSecurityServiceSupplier("").checkAuthentication(userId, psw);
			} else {
				return spagoBIUserProfile;
			}
		}

	}

	private String getAuthMode(SbiUser user) {

		List<SbiUserAttributes> attributes = DAOFactory.getSbiUserDAO().loadSbiUserAttributesById(user.getId());
		String authMode = ATTRIBUTE_AUTHENTICATION_MODE_LDAP;
		for (SbiUserAttributes attribute : attributes) {
			if (ATTRIBUTE_AUTHENTICATION_MODE.equals(attribute.getSbiAttribute().getAttributeName())) {
				authMode = attribute.getAttributeValue();
				break;
			}
		}
		logger.debug("Authentication mode: " + authMode);
		return authMode;

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