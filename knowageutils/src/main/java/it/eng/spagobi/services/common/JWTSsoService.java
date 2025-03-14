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
package it.eng.spagobi.services.common;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.PublicClaims;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This class implements SSO interface with JWT standard
 */
public class JWTSsoService implements SsoServiceInterface {

	private static Logger logger = Logger.getLogger(JWTSsoService.class);

	private static final JWTSsoServiceAlgorithmFactory ALGORITHM_FACTORY = JWTSsoServiceAlgorithmFactory.getInstance();

	public static final String KNOWAGE_ISSUER = "knowage";
	public static final String USERNAME_CLAIM = "kn_username";
	public static final String ROLES_CLAIM = "kn_roles";
	public static final String IS_SUPER_ADMIN_CLAIM = "kn_is_super_admin";
	public static final String EMAIL_CLAIM = "email";

	protected static final List<String> PREDEFINED_CLAIMS_LIST = Arrays.asList(SsoServiceInterface.USER_ID,
			USERNAME_CLAIM, ROLES_CLAIM, IS_SUPER_ADMIN_CLAIM, PublicClaims.ISSUER, PublicClaims.EXPIRES_AT);

	@Override
	public String readUserIdentifier(HttpServletRequest request) {
		try {
			Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
			String jwtToken = request.getParameter(SsoServiceInterface.USER_ID);
			if (jwtToken == null) {
				logger.debug("JWT token not found in request");
				return null;
			}
			LogMF.debug(logger, "JWT token in input is [{0}]", jwtToken);
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT decodedJWT = verifier.verify(jwtToken);
			logger.debug("JWT token verified properly");
			Claim userIdClaim = decodedJWT.getClaim(SsoServiceInterface.USER_ID);
			LogMF.debug(logger, "User id detected is [{0}]", userIdClaim.asString());
			assertNotEmpty(userIdClaim, "User id information is missing!!!");
			return jwtToken;
		} catch (JWTVerificationException e) {
			throw new SpagoBIRuntimeException("Invalid JWT token!", e);
		}
	}

	@Override
	public String readUserIdentifier(PortletSession session) {
		logger.debug("NOT Implemented");
		return "";
	}

	@Override
	public String readTicket(HttpSession session) throws IOException {
		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 5); // token for services will expire in 5 minutes
		Date expiresAt = calendar.getTime();
		LogMF.debug(logger, "JWT token will expire at [{0}]", expiresAt);
		// @formatter:off
		String token = JWT.create()
				.withIssuer(KNOWAGE_ISSUER)
				.withExpiresAt(expiresAt)
				.sign(algorithm);
		// @formatter:on
		LogMF.debug(logger, "JWT token is [{0}]", token);
		return token;
	}

	@Override
	public void validateTicket(String ticket, String userId) throws SecurityException {
		try {
			Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
			String jwtToken = ticket;
			logger.debug("JWT token in input : [" + jwtToken + "]");
			JWTVerifier verifier = JWT.require(algorithm).withIssuer(KNOWAGE_ISSUER).build();
			verifier.verify(jwtToken);
			logger.debug("JWT token verified properly");
		} catch (JWTVerificationException e) {
			throw new SecurityException("Invalid JWT token!", e);
		}
	}

	public static void assertNotEmpty(Claim claim, String message) {
		if (claim.isNull() || claim.asString().trim().equals("")) {
			throw new SpagoBIRuntimeException(message);
		}
	}

	/**
	 * Creates a JWT token with the input user id as {@link SsoServiceInterface#USER_ID} claim; the JWT token will expire at the input date.
	 *
	 * @param userId    the user id
	 * @param expiresAt the expiration date
	 * @return The JWT token with the input user id and expiration date.
	 */
	public static String userId2jwtToken(String userId, Date expiresAt) {
		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		LogMF.debug(logger, "User id in input is [{0}]", userId);
		LogMF.debug(logger, "JWT token will expire at [{0}]", expiresAt);
		// @formatter:off
		String token = JWT.create()
				.withClaim(SsoServiceInterface.USER_ID, userId)
				.withExpiresAt(expiresAt) // token will expire at the desired expire date
				.sign(algorithm);
		// @formatter:on
		LogMF.debug(logger, "JWT token is [{0}]", token);
		return token;
	}

	public static String userIdAndRole2jwtToken(String userId, List<String> roles, Date expiresAt) {
		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		LogMF.debug(logger, "User id in input is [{0}]", userId);
		LogMF.debug(logger, "JWT token will expire at [{0}]", expiresAt);
		// @formatter:off
		String token = JWT.create()
				.withClaim(SsoServiceInterface.USER_ID, userId)
				.withArrayClaim(SsoServiceInterface.ROLES, roles.stream()
						  .toArray(String[]::new))
				.withExpiresAt(expiresAt) // token will expire at the desired expire date
				.sign(algorithm);
		// @formatter:on
		LogMF.debug(logger, "JWT token is [{0}]", token);
		return token;
	}

	/**
	 * Creates a JWT token with the input user id as {@link SsoServiceInterface#USER_ID} claim; the token DOES NOT EXPIRE!!! Use this method carefully. This method
	 * was designed for the public user, in that case the JWT token is not intended to expire. Use this method carefully: in case you need a JWT token with an
	 * expiration date, use the method {@link #userId2jwtToken(String userId, Date expiresAt)}
	 *
	 * @param userId the user id
	 * @return The JWT token with the input user id: this token will last forever.
	 */
	public static String userId2jwtToken(String userId) {
		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		LogMF.debug(logger, "User id in input is [{0}]", userId);
		logger.debug("Expire date not set, JWT token will last forever");
		// @formatter:off
		String token = JWT.create()
				.withClaim(SsoServiceInterface.USER_ID, userId)
				.sign(algorithm);
		// @formatter:on
		LogMF.debug(logger, "JWT token is [{0}]", token);
		return token;
	}

	public static String ldapUser2jwtToken(String userId, String distinguishName, String psw, Date expiresAt) {
		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		LogMF.debug(logger, "User id in input is [{0}]", userId);
		LogMF.debug(logger, "JWT token will expire at [{0}]", expiresAt);
		// @formatter:off
		String token = JWT.create()
				.withClaim(SsoServiceInterface.USER_ID, userId)
				.withClaim(SsoServiceInterface.DISTINGUISH_NAME, distinguishName)
				.withClaim(SsoServiceInterface.PASSWORD, psw)
				.withExpiresAt(expiresAt) // token will expire at the desired expire date
				.sign(algorithm);
		// @formatter:on
		LogMF.debug(logger, "JWT token is [{0}]", token);
		return token;
	}

	public static String pythonScript2jwtToken(String script, Date expiresAt) {
		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		LogMF.debug(logger, "Python script in input is [{0}]", script);
		LogMF.debug(logger, "JWT token will expire at [{0}]", expiresAt);
		// @formatter:off
		String token = JWT.create()
				.withClaim(SsoServiceInterface.PYTHON_SCRIPT, script)
				.withExpiresAt(expiresAt) // token will expire at the desired expire date
				.sign(algorithm);
		// @formatter:on
		LogMF.debug(logger, "JWT token is [{0}]", token);
		return token;
	}

	public static String jwtToken2userId(String jwtToken) throws JWTVerificationException {
		Map<String, Claim> claims = getClaims(jwtToken);
		Claim userIdClaim = claims.get(SsoServiceInterface.USER_ID);
		LogMF.debug(logger, "User id detected is [{0}]", userIdClaim.asString());
		assertNotEmpty(userIdClaim, "User id information is missing!!!");
		String userId = userIdClaim.asString();
		LogMF.debug(logger, "User id is [{0}]", userId);
		return userId;
	}

	public static Map<String, Claim> getClaims(String jwtToken) throws JWTVerificationException {
		DecodedJWT decodedJWT = getDecodedJWT(jwtToken);
		logger.debug("JWT token verified properly");
		return decodedJWT.getClaims();
	}

	public static DecodedJWT getDecodedJWT(String jwtToken) throws JWTVerificationException {
		LogMF.debug(logger, "JWT token in input is [{0}]", jwtToken);
		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		JWTVerifier verifier = JWT.require(algorithm).build();
		DecodedJWT decodedJWT = verifier.verify(jwtToken);
		logger.debug("JWT token verified and decoded properly");
		return decodedJWT;
	}

	public static String map2jwtToken(Map<String, String> claims, Date expiresAt) {
		return map2jwtToken(claims, expiresAt, KNOWAGE_ISSUER);
	}

	public static String map2jwtToken(Map<String, String> claims, Date expiresAt, String issuer) {
		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		LogMF.debug(logger, "Claims map in input is [{0}]", claims);
		LogMF.debug(logger, "JWT token will expire at [{0}]", expiresAt);
		LogMF.debug(logger, "JWT token issuer [{0}]", issuer);
		Assert.assertTrue(claims != null && !claims.isEmpty(), "Claims map in input is empty!!!");
		Builder builder = JWT.create();
		for (Map.Entry<String, String> entry : claims.entrySet()) {
			builder = builder.withClaim(entry.getKey(), entry.getValue());
		}
		builder.withIssuer(issuer);
		builder.withExpiresAt(expiresAt); // token will expire at the desired expire date
		String token = builder.sign(algorithm);
		LogMF.debug(logger, "JWT token is [{0}]", token);
		return token;
	}

	public static Map<String, String> jwtToken2ldapUser(String jwtToken) throws JWTVerificationException {
		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		LogMF.debug(logger, "JWT token in input is [{0}]", jwtToken);
		JWTVerifier verifier = JWT.require(algorithm).build();
		DecodedJWT decodedJWT = verifier.verify(jwtToken);
		logger.debug("JWT token verified properly");
		Claim userIdClaim = decodedJWT.getClaim(SsoServiceInterface.USER_ID);
		LogMF.debug(logger, "User id detected is [{0}]", userIdClaim.asString());
		assertNotEmpty(userIdClaim, "User id information is missing!!!");
		String userId = userIdClaim.asString();
		LogMF.debug(logger, "User id is [{0}]", userId);
		Claim distinguishNameClaim = decodedJWT.getClaim(SsoServiceInterface.DISTINGUISH_NAME);
		LogMF.debug(logger, "Distinguish name detected is [{0}]", distinguishNameClaim.asString());
		assertNotEmpty(distinguishNameClaim, "Distinguish name information is missing!!!");
		String dn = distinguishNameClaim.asString();
		Claim pswClaim = decodedJWT.getClaim(SsoServiceInterface.PASSWORD);
		assertNotEmpty(pswClaim, "Password information is missing!!!");
		String psw = pswClaim.asString();
		Map<String, String> toReturn = new HashMap<>();
		toReturn.put("userId", userId);
		toReturn.put("dn", dn);
		toReturn.put("psw", psw);
		return toReturn;
	}

	public static String getFullJWTToken(String userId, String userName, String[] roles, Map<String, String> attributes,
			boolean isSuperAdmin, Date expiresAt) {
		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		// @formatter:off
		Builder builder = JWT.create()
				.withClaim(SsoServiceInterface.USER_ID, userId)
				.withClaim(USERNAME_CLAIM, userName)
				.withArrayClaim(ROLES_CLAIM, roles)
				.withClaim(IS_SUPER_ADMIN_CLAIM, isSuperAdmin)
				.withIssuer(KNOWAGE_ISSUER)
				.withExpiresAt(expiresAt);
		// @formatter:on
		// attributes cannot be nested at the moment (https://github.com/auth0/java-jwt/issues/163)
		attributes.forEach(builder::withClaim);

		return builder.sign(algorithm);
	}

	public static SpagoBIUserProfile fullJWTToken2UserProfile(String jwtToken) {
		LogMF.debug(logger, "JWT token in input is [{0}]", jwtToken);

		SpagoBIUserProfile profile = new SpagoBIUserProfile();
		profile.setUniqueIdentifier(jwtToken);

		Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
		JWTVerifier verifier = JWT.require(algorithm).build();
		DecodedJWT decodedJWT = verifier.verify(jwtToken);
		logger.debug("JWT token verified properly");

		Claim userIdClaim = decodedJWT.getClaim(SsoServiceInterface.USER_ID);
		LogMF.debug(logger, "User id detected is [{0}]", userIdClaim.asString());
		assertNotEmpty(userIdClaim, "User id information is missing!!!");
		profile.setUserId(userIdClaim.asString());

		Claim usernameClaim = decodedJWT.getClaim(USERNAME_CLAIM);
		LogMF.debug(logger, "User name detected is [{0}]", usernameClaim.asString());
		assertNotEmpty(usernameClaim, "User name information is missing!!!");
		profile.setUserName(usernameClaim.asString());

		Claim rolesClaim = decodedJWT.getClaim(ROLES_CLAIM);
		LogMF.debug(logger, "Roles claim detected is [{0}]", rolesClaim.asString());
		profile.setRoles(rolesClaim.asArray(String.class));

		Claim isSuperAdminClaim = decodedJWT.getClaim(IS_SUPER_ADMIN_CLAIM);
		LogMF.debug(logger, "Super admin flag detected is [{0}]", isSuperAdminClaim.asBoolean());
		profile.setIsSuperadmin(isSuperAdminClaim.asBoolean());

		// @formatter:off
		HashMap<String, String> attributes = decodedJWT.getClaims()
				.entrySet()
				.stream()
				.filter(entry -> !PREDEFINED_CLAIMS_LIST.contains(entry.getKey()))
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().asString(), (prev, next) -> next, HashMap::new));
		// @formatter:on
		LogMF.debug(logger, "Attributs detected are [{0}]", attributes);
		profile.getAttributes().putAll(attributes);

		return profile;
	}

}
