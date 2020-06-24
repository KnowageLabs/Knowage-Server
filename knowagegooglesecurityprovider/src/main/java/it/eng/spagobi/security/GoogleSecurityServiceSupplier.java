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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import it.eng.spagobi.security.google.config.GoogleSignInConfig;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;

public class GoogleSecurityServiceSupplier implements ISecurityServiceSupplier {

	private static int USER_JWT_TOKEN_EXPIRE_HOURS = 10; // JWT token for regular users will expire in 10 HOURS

	static private Logger logger = Logger.getLogger(GoogleSecurityServiceSupplier.class);

	@Override
	public SpagoBIUserProfile checkAuthentication(String userId, String psw) {
		throw new UnsupportedOperationException();
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
		logger.debug("IN");
		LogMF.debug(logger, "Verifying token [{0}]...", token);
		GoogleIdToken idToken;
		try {
			idToken = verifyToken(token);
		} catch (GeneralSecurityException | IOException e) {
			logger.error("An exception occurred while verifying Google token [" + token + "]", e);
			return null;
		}
		if (idToken == null) {
			logger.error("Invalid ID token [" + token + "]");
			return null;
		}
		LogMF.debug(logger, "Token [{0}] verified successfully", token);

		Payload payload = idToken.getPayload();

		String userId = payload.getSubject();
		LogMF.debug(logger, "User ID: [{0}]", userId);
		String email = payload.getEmail();
		LogMF.debug(logger, "User email: [{0}]", email);
		String name = (String) payload.get("name");
		LogMF.debug(logger, "User name: [{0}]", name);
		LogMF.debug(logger, "Creating user profile object for user [{0}]...", email);
		SpagoBIUserProfile profile = createUserProfileObject(email);
		LogMF.debug(logger, "User profile object for user [{0}] created", email);
		return profile;
	}

	private GoogleIdToken verifyToken(String token) throws GeneralSecurityException, IOException {
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), JacksonFactory.getDefaultInstance())
				.setAudience(Collections.singletonList(GoogleSignInConfig.getClientId())).build();
		GoogleIdToken idToken = verifier.verify(token);
		return idToken;
	}

	private SpagoBIUserProfile createUserProfileObject(String email) {
		SpagoBIUserProfile profile = new SpagoBIUserProfile();

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, USER_JWT_TOKEN_EXPIRE_HOURS);
		Date expiresAt = calendar.getTime();

		String jwtToken = JWTSsoService.userId2jwtToken(email, expiresAt);

		profile.setUniqueIdentifier(jwtToken);
		return profile;
	}

}