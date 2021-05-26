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

import java.net.MalformedURLException;

import org.apache.log4j.Logger;

import com.microsoft.aad.msal4j.PublicClientApplication;

import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;

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
		SpagoBIUserProfile profile = new SpagoBIUserProfile();
		String PUBLIC_CLIENT_ID = "54994f1f-389c-4a87-b38a-933803d03709";
		String AUTHORITY = "https://login.microsoftonline.com/angelobernabeieng.onmicrosoft.com/";
		try {
			PublicClientApplication app = PublicClientApplication.builder(PUBLIC_CLIENT_ID).authority(AUTHORITY).build();
//			IAuthenticationResult result = app.acquireToken(AuthorizationCodeParameters.builder(authCode, new URI(REPLY_URL)).scopes(scope).build()).get();
		} catch (MalformedURLException e) {
			logger.error("Could not build user profile", e);
			return null;
		}
		return profile;
	}

}