package it.eng.spagobi.services.common;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class GenericJWTSsoService extends JWTSsoService implements SsoServiceInterface {

	static private Logger logger = Logger.getLogger(GenericJWTSsoService.class);

	private static int USER_JWT_TOKEN_EXPIRE_HOURS = 10; // JWT token for regular users will expire in 10 HOURS

	private static final JWTSsoServiceAlgorithmFactory ALGORITHM_FACTORY = JWTSsoServiceAlgorithmFactory.getInstance();

	@Override
	public String readUserIdentifier(HttpServletRequest request) {
		try {

			String jwtLabel = System.getProperty("JWT_LABEL", System.getenv("JWT_LABEL"));
			String jwtToken = request.getHeader(jwtLabel) == null ? request.getParameter(jwtLabel) : request.getHeader(jwtLabel);
			if (jwtToken == null) {
				logger.debug("Request is coming from Knowage.");
				return super.readUserIdentifier(request);
			}

			logger.debug("JWT token in input is " + jwtToken);
			DecodedJWT decodedJWT = verifyToken(jwtToken);
			return createKnowageJwtToken(decodedJWT);
		} catch (JWTVerificationException e) {
			throw new SpagoBIRuntimeException("Invalid JWT token!", e);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Errore JWT token!", e);
		}

	}

	private DecodedJWT verifyToken(String token) {
		DecodedJWT decodedToken = null;
		try {

			String signature = System.getProperty("JWT_SIGNATURE", System.getenv("JWT_SIGNATURE"));

			if (signature != null && !signature.equals("")) {
				decodedToken = this.verifyTokenBySignature(token, signature);
			}

			if (decodedToken == null) {
				throw new SpagoBIRuntimeException("Error during JWT token validation");
			}

		} catch (JWTVerificationException e) {
			throw e;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error during JWT token validation", e);
		}
		return decodedToken;
	}

	private DecodedJWT verifyTokenBySignature(String token, String signature) throws Exception {
		Algorithm algorithmInputTocken = Algorithm.HMAC256(signature);
		JWTVerifier verifier = JWT.require(algorithmInputTocken).build();
		return verifier.verify(token);
	}

	private String createKnowageJwtToken(DecodedJWT jwtToken) {
		try {

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.HOUR, USER_JWT_TOKEN_EXPIRE_HOURS); // token for services will expire in 5 minutes
			Date expiresAt = calendar.getTime();
			logger.debug("JWT token will expire at [{0}]" + expiresAt);
			Builder tokenBuilder = JWT.create().withIssuer(JWTSsoService.KNOWAGE_ISSUER).withExpiresAt(expiresAt);
			this.completeJWT(jwtToken, tokenBuilder);
			Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
			String token = tokenBuilder.sign(algorithm);

			return token;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot create Knowage JWT token from Cot JWT token", e);
		}
	}

	private void completeJWT(DecodedJWT jwtToken, Builder builder) {

		String jwtClaims = System.getProperty("JWT_CLAIMS", System.getenv("JWT_CLAIMS"));
		String[] listClaims = jwtClaims.split(";");
		for (int i = 0; i < listClaims.length; i++) {
			String nameClaim = listClaims[i];
			String[] array = jwtToken.getClaim(nameClaim).asArray(String.class);
			if (array != null) {
				builder.withArrayClaim(nameClaim, array);
			} else {
				builder.withClaim(nameClaim, jwtToken.getClaim(nameClaim).asString());
			}

		}
		String jwtClaimsUserId = System.getProperty("JWT_CLAIM_USER_ID", System.getenv("JWT_CLAIM_USER_ID"));
		builder.withClaim(SsoServiceInterface.USER_ID, jwtToken.getClaim(jwtClaimsUserId).asString());
	}




}
