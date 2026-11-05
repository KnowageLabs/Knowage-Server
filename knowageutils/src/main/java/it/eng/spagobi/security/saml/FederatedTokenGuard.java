package it.eng.spagobi.security.saml;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import it.eng.knowage.commons.security.FederatedSessionVerifier;

import it.eng.spagobi.services.common.JWTSsoService;
import it.eng.spagobi.services.common.JWTSsoServiceAlgorithmFactory;

public final class FederatedTokenGuard {
	public static final String SESSION_CLAIM = "kn_saml_sid";
	public static final String TENANT_CLAIM = "kn_saml_tenant";

	private FederatedTokenGuard() {
	}

	public static boolean isFederated(String token) {
		if (token == null || token.chars().filter(c -> c == '.').count() != 2) {
			return false;
		}
		try {
			return !JWT.decode(token).getClaim(SESSION_CLAIM).isNull();
		} catch (JWTVerificationException e) {
			return false;
		}
	}

	public static void check(String token) {
		if (isFederated(token)) {
			checkDecoded(JWT.require(JWTSsoServiceAlgorithmFactory.getInstance().getAlgorithm())
					.withIssuer(JWTSsoService.KNOWAGE_ISSUER).build().verify(token));
		}
	}

	public static void checkDecoded(DecodedJWT token) {
		String sid = token.getClaim(SESSION_CLAIM).asString();
		if (sid == null) {
			if (!token.getClaim(SESSION_CLAIM).isNull()) {
				throw new JWTVerificationException("Invalid federated session claim");
			}
			return;
		}
		try {
			FederatedSessionVerifier.verify(token.getToken(), sid, token.getIssuer(), token.getExpiresAt());
		} catch (IllegalStateException e) {
			throw new JWTVerificationException("Federated session state unavailable", e);
		}
	}
}
