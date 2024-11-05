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

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.auth0.jwt.interfaces.Claim;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.security.OAuth2.OAuth2Config;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.utilities.assertion.UnreachableCodeException;

/**
 * This security provider is conceived when authentication is delegated to an OAuth2 provider, but Knowage is in charge for the authorization, i.e. users' roles
 * and profile attributes are stored within Knowage metadata. It has to be used along with it.eng.spagobi.services.oauth2.Oauth2HybridSsoService, in a way that
 * the user unique identifier is the regular Knowage JWT token (see class it.eng.spagobi.services.common.JWTSsoService).
 *
 * @author Davide Zerbetto
 *
 */
public class OAuth2HybridSecurityServiceSupplier extends InternalSecurityServiceSupplierImpl {

	private static Logger logger = Logger.getLogger(OAuth2HybridSecurityServiceSupplier.class);

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		throw new UnreachableCodeException("You cannot invoke this method, since authentication must be delegated to the OAuth2 provider");
	}

	@Override
	public SpagoBIUserProfile createUserProfile(String jwtToken) {
		logger.debug("IN - JWT token: " + jwtToken);
		try {
			String userId = JWTSsoService.jwtToken2userId(jwtToken);
			logger.debug("Clear text userId: " + userId);
			SbiUser user = DAOFactory.getSbiUserDAO().loadSbiUserByUserId(userId);
			if (user == null) {
				if (OAuth2Config.getInstance().isNonProfiledUserAllowed()) {
					logger.info("User [" + userId
							+ "] was not found into internal metadata; non profiled users are allowed to enter. Returning a minimal profile object...");
					return createMinimumUserProfile(jwtToken, userId);
				} else {
					logger.info("User [" + userId + "] was not found into internal metadata; non profiled users are not allowed to enter. Returning null.");
					return null;
				}
			} else {
				return super.createUserProfile(jwtToken);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	private SpagoBIUserProfile createMinimumUserProfile(String jwtToken, String userId) {
		logger.debug("IN");

		SpagoBIUserProfile profile = new SpagoBIUserProfile();
		profile.setUniqueIdentifier(jwtToken);
		profile.setUserId(userId);

		String userName;
		Map<String, Claim> claims = JWTSsoService.getClaims(jwtToken);
		if (claims.containsKey(JWTSsoService.USERNAME_CLAIM)) {
			Claim userNameClaim = claims.get(JWTSsoService.USERNAME_CLAIM);
			userName = userNameClaim.asString();
		} else {
			userName = userId;
		}

		profile.setUserName(userName);
		profile.setIsSuperadmin(false);

		String email;
		if (claims.containsKey(JWTSsoService.EMAIL_CLAIM)) {
			Claim emailClaim = claims.get(JWTSsoService.EMAIL_CLAIM);
			email = emailClaim.asString();
		} else {
			email = null;
		}

		HashMap attributes = new HashMap();

		// add email as attribute
		if (StringUtilities.isNotEmpty(email)) {
			logger.debug("Email is [" + email + "]");
			attributes.put("email", email);
		} else {
			logger.debug("Email not found");
		}

		logger.debug("Attributes load into Knowage profile: " + attributes);

		// end load profile attributes

		profile.setAttributes(attributes);

		return profile;
	}
}
