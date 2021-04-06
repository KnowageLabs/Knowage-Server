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
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.security.hmacfilter.HMACUtils;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This class implements SSO interface with JWT standard
 */
public class JWTSsoService implements SsoServiceInterface {

	static private Logger logger = Logger.getLogger(JWTSsoService.class);

	static private Algorithm algorithm;

	static {
		try {
			String key = getHMACKey();
			algorithm = Algorithm.HMAC256(key);
		} catch (Exception e) {
			logger.error("Cannot initialize JWT algorithm", e);
			throw new SpagoBIRuntimeException("Cannot initialize JWT algorithm", e);
		}
	}

	/**
	 * Gets the HMAC key from configuration
	 *
	 * @return the HMAC key
	 */
	protected static String getHMACKey() {
		try {
			String key = EnginConf.getInstance().getHmacKey();
			if (key == null || key.isEmpty()) {
				key = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue(HMACUtils.HMAC_JNDI_LOOKUP));
			}
			return key;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot retrieve the HMAC key", e);
		}
	}

	@Override
	public String readUserIdentifier(HttpServletRequest request) {
		try {
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
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 5); // token for services will expire in 5 minutes
		Date expiresAt = calendar.getTime();
		LogMF.debug(logger, "JWT token will expire at [{0}]", expiresAt);
		// @formatter:off
		String token = JWT.create()
				.withIssuer("knowage")
				.withExpiresAt(expiresAt)
				.sign(algorithm);
		// @formatter:on
		LogMF.debug(logger, "JWT token is [{0}]", token);
		return token;
	}

	@Override
	public void validateTicket(String ticket, String userId) throws SecurityException {
		try {
			String jwtToken = ticket;
			logger.debug("JWT token in input : [" + jwtToken + "]");
			JWTVerifier verifier = JWT.require(algorithm).withIssuer("knowage").build();
			verifier.verify(jwtToken);
			logger.debug("JWT token verified properly");
		} catch (JWTVerificationException e) {
			throw new SecurityException("Invalid JWT token!", e);
		}
	}

	public static void assertNotEmpty(Claim claim, String message) {
		if (claim.isNull() || claim.asString().trim().equals(""))
			throw new SpagoBIRuntimeException(message);
	}

	public static String userId2jwtToken(String userId, Date expiresAt) {
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

	public static String pythonDataset2jwtToken(String script, Date expiresAt) {
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

	public static String catalogFunction2jwtToken(String script, Date expiresAt) {
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
		LogMF.debug(logger, "JWT token in input is [{0}]", jwtToken);
		JWTVerifier verifier = JWT.require(algorithm).build();
		DecodedJWT decodedJWT = verifier.verify(jwtToken);
		logger.debug("JWT token verified properly");
		Claim userIdClaim = decodedJWT.getClaim(SsoServiceInterface.USER_ID);
		LogMF.debug(logger, "User id detected is [{0}]", userIdClaim.asString());
		assertNotEmpty(userIdClaim, "User id information is missing!!!");
		String userId = userIdClaim.asString();
		LogMF.debug(logger, "User id is [{0}]", userId);
		return userId;
	}

	public static String map2jwtToken(Map<String, String> claims, Date expiresAt, String issuer) {
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

}
