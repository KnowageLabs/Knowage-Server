package it.eng.spagobi.services.common;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import it.eng.spagobi.utilities.rest.RestUtilities.Response;

public class GenericJWTSsoService extends JWTSsoService implements SsoServiceInterface {

	private static Logger logger = Logger.getLogger(GenericJWTSsoService.class);

	private static int USER_JWT_TOKEN_EXPIRE_HOURS = 10; // JWT token for regular users will expire in 10 HOURS

	private static final JWTSsoServiceAlgorithmFactory ALGORITHM_FACTORY = JWTSsoServiceAlgorithmFactory.getInstance();

	@Override
	public String readUserIdentifier(HttpServletRequest request) {
		try {

			String jwtToken = this.getJWT(request);
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
		try {

			String signature = System.getProperty("JWT_SIGNATURE", System.getenv("JWT_SIGNATURE"));

			if (signature != null && !signature.equals("")) {
				return this.verifyTokenBySignature(token, signature);
			}


			DecodedJWT jwt = JWT.decode(token);
			Date expiresAt = jwt.getExpiresAt();
			boolean isExpired = expiresAt.before(new Date());
			if (isExpired) {
				throw new SpagoBIRuntimeException("JWT is exipered " + expiresAt);
			} else {
				return jwt;
			}


		} catch (JWTVerificationException e) {
			throw e;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error during JWT token validation", e);
		}

	}

	private DecodedJWT verifyTokenBySignature(String token, String signature) throws Exception {
		Algorithm algorithmInputTocken = Algorithm.HMAC256(signature);
		JWTVerifier verifier = JWT.require(algorithmInputTocken).withIssuer(System.getProperty("JWT_ISSUER", System.getenv("JWT_ISSUER"))).build();
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
			return tokenBuilder.sign(algorithm);

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot create Knowage JWT token from Cot JWT token", e);
		}
	}

	private void completeJWT(DecodedJWT jwtToken, Builder builder) {
		// get simple claims
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
		// get complex claims
		String jwtClaimsMap = System.getProperty("JWT_CLAIMS_MAP", System.getenv("JWT_CLAIMS_MAP"));
		if (jwtClaimsMap != null) {
			String[] listClaimsMap = jwtClaimsMap.split(";");
			for (int i = 0; i < listClaimsMap.length; i++) {
				String valueClaimMap = listClaimsMap[i];
				String[] valueClaimMapSplit = valueClaimMap.split("\\|");
				String nameMap = valueClaimMapSplit[0];
				String nameValueMap = valueClaimMapSplit[1];

				Map<String, Object> map = jwtToken.getClaim(nameMap).asMap();
				Object valoreObj = map.get(nameValueMap);

				if (valoreObj instanceof String valore) {
					builder.withClaim(nameValueMap, valore);
				} else if (valoreObj instanceof List<?> valore) {
					builder.withArrayClaim(nameValueMap, valore.toArray(new String[valore.size()]));
				}

			}
		}

		String jwtClaimsUserId = System.getProperty("JWT_CLAIM_USER_ID", System.getenv("JWT_CLAIM_USER_ID"));
		builder.withClaim(SsoServiceInterface.USER_ID, jwtToken.getClaim(jwtClaimsUserId).asString());
	}

	private String getJWT(HttpServletRequest request) throws Exception {
		String jwtLabel = System.getProperty("JWT_LABEL", System.getenv("JWT_LABEL"));
		logger.debug("jwtLabel" + jwtLabel);
		// get token from request
		if (jwtLabel != null) {
			return this.getJWTFromLabel(request, jwtLabel);
		}

		String jwtURL = System.getProperty("JWT_URL_TOKEN", System.getenv("JWT_URL_TOKEN"));
		logger.debug("jwtURL" + jwtURL);
		// get token from rest service
		if (jwtURL != null) {
			return this.getJWTFromURL(request, jwtURL);
		}

		return null;
	}

	private String getJWTFromLabel(HttpServletRequest request, String jwtLabel) {
		return request.getHeader(jwtLabel) == null ? request.getParameter(jwtLabel) : request.getHeader(jwtLabel);
	}

	private String getJWTFromURL(HttpServletRequest request, String jwtURLMethod) {

		try {
			String jwtParamHeader = System.getProperty("JWT_URL_TOKEN_HEADER", System.getenv("JWT_URL_TOKEN_HEADER"));
			logger.debug("jwtParamHeader" + jwtParamHeader);
			Map<String, String> headers = new HashMap<>();
			if (jwtParamHeader != null) {
				String[] listParamHeader = jwtParamHeader.split(";");
				for (int i = 0; i < listParamHeader.length; i++) {
					String[] listParamHeaderToSplit = listParamHeader[i].split("\\|");
					String nameParam = listParamHeaderToSplit[0];
					String valueParam = listParamHeaderToSplit[1];

					headers.put(nameParam, Optional.ofNullable(request.getParameter(valueParam)).orElse(""));
				}

			}

			String[] jwtURLMethodSplit = jwtURLMethod.split("\\|");
			String jwtURL = jwtURLMethodSplit[0];
			String jwtMethod = jwtURLMethodSplit[1];
			logger.debug("jwtURL" + jwtURL);
			logger.debug("jwtMethod" + jwtMethod);
			Response response = RestUtilities.makeRequest(HttpMethod.valueOf(jwtMethod), jwtURL, headers, null);
			logger.debug("response.getStatusCode()" + response.getStatusCode());
			logger.debug("response.getResponseBody()" + response.getResponseBody());
			if (response.getStatusCode() == HttpStatus.SC_OK) {
				JSONObject obj = new JSONObject(response.getResponseBody());
				return obj.getString(System.getProperty("JWT_URL_TOKEN_RESPONSE", System.getenv("JWT_URL_TOKEN_RESPONSE")));

			}
		} catch (Exception e) {
			logger.error("Errore getJWTFromURL" + e.getMessage(), e);
		}

		return null;

	}





}
