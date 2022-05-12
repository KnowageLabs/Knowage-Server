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
package it.eng.spagobi.services.oauth2;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;

import it.eng.spagobi.security.OAuth2.OAuth2Config;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This SSO service retrieves the OAuth2 access token from session, then decodes it as a JWT token and gets its subject as the user id. Then returns the regular
 * JWT token as per it.eng.spagobi.services.oauth2.JWTSsoService class.
 *
 * @author Davide Zerbetto
 *
 */
public class Oauth2HybridSsoService extends JWTSsoService {

	static private Logger logger = Logger.getLogger(Oauth2HybridSsoService.class);

	@Override
	public String readUserIdentifier(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String accessToken = (String) session.getAttribute(Oauth2SsoService.ACCESS_TOKEN);
		if (accessToken == null) {
			logger.debug("Access token not found.");
			return super.readUserIdentifier(request);
		}
		LogMF.debug(logger, "Access token found: [{0}]", accessToken);
		String toReturn = accessToken2JWTToken(accessToken);
		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	private String accessToken2JWTToken(String accessToken) {
		String userId = getUserId(accessToken);
		LogMF.debug(logger, "User id detected from access token [{0}]", userId);
		return JWTSsoService.userId2jwtToken(userId);
	}

	private String getUserId(String accessToken) {
		try {
			Properties oauth2Config = OAuth2Config.getInstance().getConfig();
			DecodedJWT decodedJWT = JWT.decode(accessToken);
			JwkProvider provider = new JwkProviderBuilder(new URL(oauth2Config.getProperty("JWKS_URL"))).build();
			Jwk jwk = provider.get(decodedJWT.getKeyId());
			Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
			Verification verifier = JWT.require(algorithm);
			verifier.build().verify(accessToken);
			String subject = decodedJWT.getSubject();
			return subject;
		} catch (JWTVerificationException | JwkException | MalformedURLException e) {
			throw new SpagoBIRuntimeException("Cannot get user id from access token [" + accessToken + "]", e);
		}
	}

}