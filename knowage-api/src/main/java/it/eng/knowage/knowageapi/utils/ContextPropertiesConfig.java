package it.eng.knowage.knowageapi.utils;

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

import it.eng.knowage.knowageapi.error.KnowageRuntimeException;

@Component
public class ContextPropertiesConfig {

	private static String hmacKey;
	private static String resourcePathKey;

	@Autowired
	public ContextPropertiesConfig(@Value("${jndi.lookup.hmackey}") String hmacKey, @Value("${jndi.lookup.resourcepath}") String resourcePathKey) {
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
