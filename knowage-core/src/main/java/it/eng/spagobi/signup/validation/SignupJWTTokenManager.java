package it.eng.spagobi.signup.validation;

import java.util.Date;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;

import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.security.exceptions.SecurityException;

public class SignupJWTTokenManager {
	
	//Minutes before signup key expirations
	static private Integer expirationDelay = 10;
	
	public static String createJWTToken(String userId) {
		Date expirationDate = new Date(System.currentTimeMillis()+expirationDelay*60*1000);
		return JWTSsoService.userId2jwtToken(userId, expirationDate);
	}
	
	public static String verifyJWTToken(String token) throws TokenExpiredException, SecurityException{
		try {
			String jwtToken = token;
			return JWTSsoService.jwtToken2userId(jwtToken);
		}catch (TokenExpiredException te) {
			throw te;
		}catch (JWTVerificationException e) {
			throw new SecurityException("Invalid JWT token!", e);
		}
	}
}
