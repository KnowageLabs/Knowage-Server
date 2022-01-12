/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.boot.utils;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.eng.knowage.boot.error.KnowageRuntimeException;

/**
 * @deprecated This class is simply not right. We use @Component to let Spring instantiates it as a Singleton,
 *  we inject values in the constructor and we use them to set static variables then we use static members of
 *  this class. It's an horror class!
 */
@Component
@Deprecated
public class ContextPropertiesConfig {

	private static String hmacKey;
	private static String resourcePathKey;

	@Autowired
	public ContextPropertiesConfig(@Value("${jndi.lookup.hmackey}") String hmacKey, @Value("${jndi.lookup.resourcepath}") String resourcePathKey) {
		// TODO : values for static attributes shouldn't not be injected in a non-static way
		this.hmacKey = hmacKey;
		this.resourcePathKey = resourcePathKey;
	}

	public static String jwtToken2userId(String jwtToken) throws JWTVerificationException {
		String userId = null;
		Context ctx;
		try {
			ctx = new InitialContext();
			String key = (String) ctx.lookup(hmacKey);
			Algorithm algorithm = Algorithm.HMAC256(key);
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT decodedJWT = verifier.verify(jwtToken);
			Claim userIdClaim = decodedJWT.getClaim("user_id");
			userId = userIdClaim.asString();
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}
		return userId;
	}

	public static String getResourcePath() {
		String resourcePath = null;
		Context ctx;
		try {
			ctx = new InitialContext();
			resourcePath = (String) ctx.lookup(resourcePathKey);

		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage(), e);
		}
		return resourcePath;
	}

}
