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
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;

import it.eng.spagobi.security.OAuth2.OAuth2Client;
import it.eng.spagobi.security.OAuth2.OAuth2Config;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * This SSO service retrieves the OAuth2 access token from session, then retrieves the user id in clear text: in case oauth2_user_info_url is defined, this SSO
 * service invokes that URL to get user info, otherwise it decodes the access token as a JWT token; then gets configured claim ('sub' is the default) as user
 * id. Then returns the regular JWT token as per it.eng.spagobi.services.oauth2.JWTSsoService class.
 *
 * @author Davide Zerbetto
 *
 */
public class Oauth2HybridSsoService extends JWTSsoService {

	static private Logger logger = Logger.getLogger(Oauth2HybridSsoService.class);

	private static int USER_JWT_TOKEN_EXPIRE_HOURS = 10; // JWT token for regular users will expire in 10 HOURS

	@Override
	public String readUserIdentifier(HttpServletRequest request) {
		String toReturn = null;
		String clearTextUserId = null;
		HttpSession session = request.getSession();
		String accessToken = (String) session.getAttribute(Oauth2SsoService.ACCESS_TOKEN);
		if (accessToken == null) {
			logger.debug("Access token not found.");
			return super.readUserIdentifier(request);
		}
		LogMF.debug(logger, "Access token found: [{0}]", accessToken);
		if (OAuth2Config.getInstance().hasUserInfoUrl()) {
			logger.debug("User info URL found from config [" + OAuth2Config.getInstance().getUserInfoUrl() + "]; getting user id from it ...");
			clearTextUserId = getUserIdFromProfileInfoURL(accessToken, OAuth2Config.getInstance().getUserInfoUrl());
		} else {
			logger.debug("User info URL not found from config; getting user id from access token as JWT token ....");
			clearTextUserId = getUserIdFromAccessToken(accessToken);
		}

		LogMF.debug(logger, "User id detected [{0}]", clearTextUserId);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, USER_JWT_TOKEN_EXPIRE_HOURS);
		Date expiresAt = calendar.getTime();
		toReturn = JWTSsoService.userId2jwtToken(clearTextUserId, expiresAt);

		LogMF.debug(logger, "OUT: returning [{0}]", toReturn);
		return toReturn;
	}

	private String getUserIdFromProfileInfoURL(String accessToken, String userInfoUrl) {
		try {
			OAuth2Client oauth2Client = new OAuth2Client();

			HttpClient httpClient = oauth2Client.getHttpClient();

			// We call the OAuth2 provider to get user's info
			GetMethod httpget = new GetMethod(userInfoUrl);
			httpget.addRequestHeader("Authorization", "Bearer " + accessToken);
			int statusCode = httpClient.executeMethod(httpget);
			byte[] response = httpget.getResponseBody();
			if (statusCode != HttpStatus.SC_OK) {
				logger.error("Error while getting user information from OAuth2 provider: server returned statusCode = " + statusCode);
				LogMF.error(logger, "Server response is:\n{0}", new Object[] { new String(response) });
				throw new SpagoBIRuntimeException("Error while getting user information from OAuth2 provider: server returned statusCode = " + statusCode);
			}

			String responseStr = new String(response);
			LogMF.debug(logger, "Server response is:\n{0}", responseStr);
			JSONObject jsonObject = new JSONObject(responseStr);

			String userId = jsonObject.getString(OAuth2Config.getInstance().getUserIdClaim());
			logger.debug("User id is [" + userId + "]");
			return userId;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot get user id from access token [" + accessToken + "] by user profile info URL [" + userInfoUrl + "]", e);
		}
	}

	private String getUserIdFromAccessToken(String accessToken) {
		try {
			DecodedJWT decodedJWT = JWT.decode(accessToken);
			logger.debug("Access token properly decoded as JWT token");
			JwkProvider provider = new JwkProviderBuilder(new URL(OAuth2Config.getInstance().getJWKSUrl())).build();
			Jwk jwk = provider.get(decodedJWT.getKeyId());
			Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
			Verification verifier = JWT.require(algorithm);
			verifier.build().verify(accessToken);
			logger.debug("JWT token verified.");
			String claimName = OAuth2Config.getInstance().getUserIdClaim();
			logger.debug("Looking for claim [" + claimName + "] ...");
			Claim userIdClaim = decodedJWT.getClaim(claimName);
			if (userIdClaim.isNull()) {
				throw new SpagoBIRuntimeException("Claim [" + claimName + "] not found on access token.");
			}
			String userId = decodedJWT.getClaim(claimName).asString();
			return userId;
		} catch (JWTVerificationException | JwkException | MalformedURLException e) {
			throw new SpagoBIRuntimeException("Cannot get user id from access token [" + accessToken + "]", e);
		}
	}

}
