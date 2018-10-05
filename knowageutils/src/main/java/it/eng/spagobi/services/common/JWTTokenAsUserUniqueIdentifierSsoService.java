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

import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.security.hmacfilter.HMACUtils;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This class implements SSO interface with JWT standard
 */
public class JWTTokenAsUserUniqueIdentifierSsoService implements SsoServiceInterface {

	static private Logger logger = Logger.getLogger(JWTTokenAsUserUniqueIdentifierSsoService.class);
	
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
	
	public String readUserIdentifier(HttpServletRequest request) {
		try {
			String jwtToken = request.getParameter(SsoServiceInterface.USER_ID);
			if (jwtToken == null) {
				logger.debug("JWT token not found in request");
				return null;
			}
			logger.debug("JWT token retrieved : [" + jwtToken + "]");
			JWTVerifier verifier = JWT.require(algorithm).build();
			verifier.verify(jwtToken);
			logger.debug("JWT token verified properly");
			return jwtToken; // we consider the JWT token as user unique identifier
		} catch (JWTVerificationException e) {
			throw new SpagoBIRuntimeException("Invalid JWT token!", e);
		}
	}

	public String readUserIdentifier(PortletSession session) {
		logger.debug("NOT Implemented");
		return "";
	}

	public String readTicket(HttpSession session) throws IOException {
		Calendar date = Calendar.getInstance();
		long t = date.getTimeInMillis();
		Date expireDate = new Date(t + 5 * 60 * 1000); // 5 minutes
		String token = JWT.create()
		        .withIssuer("knowage")
		        .withExpiresAt(expireDate)
		        .sign(algorithm);
		return token;
	}

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

}
