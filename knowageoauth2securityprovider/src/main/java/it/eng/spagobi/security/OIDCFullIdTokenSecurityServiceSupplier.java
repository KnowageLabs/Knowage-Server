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

import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jayway.jsonpath.JsonPath;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.profiling.bean.SbiAttribute;
import it.eng.spagobi.security.OAuth2.OAuth2Config;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.utilities.assertion.UnreachableCodeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class OIDCFullIdTokenSecurityServiceSupplier implements ISecurityServiceSupplier {

	private static Logger logger = Logger.getLogger(OIDCFullIdTokenSecurityServiceSupplier.class);

	@Override
	public SpagoBIUserProfile createUserProfile(String jwtToken) {
		DecodedJWT decodedJWT = JWT.decode(jwtToken);
		String issuer = decodedJWT.getIssuer();
		if (JWTSsoService.KNOWAGE_ISSUER.equals(issuer)) {
			return JWTSsoService.fullJWTToken2UserProfile(jwtToken);
		} else {
			String internalFullJWTToken = externalJWTToken2InternalJWTToken(jwtToken);
			return JWTSsoService.fullJWTToken2UserProfile(internalFullJWTToken);
		}
	}

	private String externalJWTToken2InternalJWTToken(String jwtToken) {
		try {
			LogMF.info(logger, "Input JWT token is [{0}]", jwtToken);
			DecodedJWT decodedJWT = JWT.decode(jwtToken); // ID TOKEN IS TRUSTED: it was validated by OAuth2Filter
			logger.info("JWT token properly decoded");
			String userId = decodedJWT.getClaim(OAuth2Config.getInstance().getUserIdClaim()).asString();
			LogMF.info(logger, "User id is [{0}]", userId);
			String userName = getUserName(decodedJWT);
			LogMF.info(logger, "User name is [{0}]", userName);
			String[] roles = getUserRoles(decodedJWT);
			LogMF.info(logger, "Roles list is [{0}]", roles);
			boolean isSuperAdmin = isSuperAdmin(roles);
			LogMF.info(logger, "Super admin flag is [{0}]", isSuperAdmin);
			Map<String, String> attributes = getUserAttributes(decodedJWT);
			LogMF.info(logger, "Attributes are [{0}]", attributes);
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR, InternalSecurityServiceSupplierImpl.USER_JWT_TOKEN_EXPIRE_HOURS);
			Date expiresAt = calendar.getTime();

			String toReturn = JWTSsoService.getFullJWTToken(userId, userName, roles, attributes, isSuperAdmin, expiresAt);
			LogMF.info(logger, "Output JWT token is [{0}]", toReturn);

			return toReturn;
		} catch (JWTDecodeException e) {
			throw new SpagoBIRuntimeException("An error occured while parsing input JWT token", e);
		}
	}

	protected Map<String, String> getUserAttributes(DecodedJWT decodedJWT) {
		Map<String, String> attributes = new HashMap<>();
		try {
			List<SbiAttribute> sbiAttributes = DAOFactory.getSbiAttributeDAO().loadSbiAttributes();
			sbiAttributes.forEach(sbiAttribute -> {
				String claimName = sbiAttribute.getAttributeName();
				Claim valueClaim = decodedJWT.getClaim(claimName);
				if (!valueClaim.isNull()) {
					LogMF.info(logger, "Got attribute/claim with name [{0}] and value [{1}]", claimName, valueClaim.asString());
					attributes.put(claimName, valueClaim.asString());
				} else {
					LogMF.info(logger, "Claim with name [{0}] not found into JWT token", claimName);
				}
			});
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("An error occured while getting user attributes", e);
		}
		return attributes;
	}

	// this is promoting admin users to superadmins
	protected boolean isSuperAdmin(String[] roles) {
		IRoleDAO dao = DAOFactory.getRoleDAO();
		for (String roleName : roles) {
			try {
				Role role = dao.loadByName(roleName);
				if (role != null && role.getRoleTypeCD().equals(SpagoBIConstants.ADMIN_ROLE_TYPE)) {
					return true;
				}
			} catch (EMFUserError e) {
				throw new SpagoBIRuntimeException("An error occured while reading role " + roleName, e);
			}
		}
		return false;
	}

	protected String[] getUserRoles(DecodedJWT decodedJWT) {
		IRoleDAO dao = DAOFactory.getRoleDAO();
		// we get all roles coming from JWT token
		String[] rolesInJWTToken = getUserRolesFromJWTToken(decodedJWT);
		// we filter roles: the ones that do not exist within knowage metadata are discarded
		return Arrays.asList(rolesInJWTToken).stream().filter(roleName -> {
			Role role;
			try {
				role = dao.loadByName(roleName);
			} catch (EMFUserError e) {
				throw new SpagoBIRuntimeException("An error occured while reading role " + roleName, e);
			}
			return role != null;
		}).toArray(String[]::new);
	}

	private String[] getUserRolesFromJWTToken(DecodedJWT decodedJWT) {
		try {
			String payload = decodedJWT.getPayload();
			String decodedPayload = new String(Base64.getDecoder().decode(payload));
			net.minidev.json.JSONArray parsed = JsonPath.read(decodedPayload, OAuth2Config.getInstance().getIdTokenJsonRolesPath());
			LogMF.info(logger, "Got parsed roles [{0}]", parsed);
			if (parsed == null || parsed.isEmpty()) {
				logger.info("No roles detected");
				return new String[0];
			}
			String[] roles = new String[parsed.size()];
			for (int i = 0; i < roles.length; i++) {
				roles[i] = (String) parsed.get(i);
			}
			return roles;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An error occured while getting roles from JWT token", e);
		}
	}

	protected String getUserName(DecodedJWT decodedJWT) {
		String userNameClaimsConfig = OAuth2Config.getInstance().getUserNameClaim();
		String[] userNameClaims = userNameClaimsConfig.split(" ");
		// @formatter:off
		return Arrays.asList(userNameClaims).stream() // get all claims (for example given_name and family_name)
				.map(claimName -> decodedJWT.getClaim(claimName).asString()) // for each claim get its value from JWT token
				.collect(Collectors.joining(" ")); // join values
		// @formatter:on
	}

	@Override
	public SpagoBIUserProfile checkAuthenticationToken(String jwtToken) {
		return createUserProfile(jwtToken);
	}

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		throw new UnreachableCodeException("Method not implemented!");
	}

	@Override
	public SpagoBIUserProfile checkAuthenticationWithToken(String userId, String token) {
		throw new UnreachableCodeException("Method not implemented!");
	}

	@Override
	public boolean checkAuthorization(String userId, String function) {
		throw new UnreachableCodeException("Method not implemented!");
	}

}
