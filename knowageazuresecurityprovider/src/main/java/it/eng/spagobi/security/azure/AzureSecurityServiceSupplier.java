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

package it.eng.spagobi.security.azure;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.eng.spagobi.security.InternalSecurityServiceSupplierImpl;
import it.eng.spagobi.security.azure.config.AzureSignInConfig;
import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class AzureSecurityServiceSupplier implements ISecurityServiceSupplier {

	private static int USER_JWT_TOKEN_EXPIRE_HOURS = 10; // JWT token for regular users will expire in 10 HOURS

	static private Logger logger = Logger.getLogger(AzureSecurityServiceSupplier.class);

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
		DecodedJWT jwt;
		try {
			logger.debug("JWT token in input : [" + token + "]");
			jwt = JWT.decode(token);
			JwkProvider provider = new UrlJwkProvider(new URL(AzureSignInConfig.getJwkProviderUrl()));
			logger.debug("JWT token Key Id : [" + jwt.getKeyId() + "]");
			Jwk jwk = provider.get(jwt.getKeyId());
			Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
			algorithm.verify(jwt);
			logger.debug("JWT token verified properly");
		} catch (SignatureVerificationException e) {
			throw new SpagoBIRuntimeException("Invalid JWT token signature", e);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error validating JWT token", e);
		}

		String email = jwt.getClaim("email").asString();
		String tenant_id = jwt.getClaim("tid").asString();
		String client_id = jwt.getClaim("aud").asString();

		if (!tenant_id.equals(AzureSignInConfig.getTenantId())) {
			logger.error(
					"Was expecting tenant id {" + AzureSignInConfig.getTenantId() + "} from configuration, but got tenant id {" + tenant_id + "} in token.");
			throw new SpagoBIRuntimeException("Tenant id  not matching!");
		}
		if (!client_id.equals(AzureSignInConfig.getClientId())) {
			logger.error(
					"Was expecting client id {" + AzureSignInConfig.getClientId() + "} from configuration, but got client id {" + client_id + "} in token.");
			throw new SpagoBIRuntimeException("Client id not matching!");
		}

		SpagoBIUserProfile profile = createUserProfileObject(email);
		return profile;
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