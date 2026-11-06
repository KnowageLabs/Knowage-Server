package it.eng.spagobi.security.saml;

import static org.junit.Assert.*;

import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.io.UnsupportedEncodingException;

public class FederatedTokenGuardTest {
	@Test
	public void distinguishesLocalProfilesFromPmAndLegacyFederatedTokens() throws UnsupportedEncodingException {
		assertTrue(FederatedTokenGuard.isLocalProfile(token("local")));
		assertFalse(FederatedTokenGuard.isLocalProfile(token("pm")));
		assertFalse(FederatedTokenGuard.isLocalProfile(token(null)));
		assertFalse(FederatedTokenGuard.isLocalProfile("reader"));
	}

	@Test
	public void unknownProfilingClaimNeverBypassesPmRestrictions() {
		assertThrows(JWTVerificationException.class, () -> FederatedTokenGuard.isLocalProfile(token("unknown")));
		assertThrows(JWTVerificationException.class, () -> FederatedTokenGuard.isLocalProfile(token("")));
	}

	private String token(String mode) throws UnsupportedEncodingException {
		com.auth0.jwt.JWTCreator.Builder builder = JWT.create().withClaim(FederatedTokenGuard.SESSION_CLAIM, "test-session");
		if (mode != null) {
			builder.withClaim(FederatedTokenGuard.PROFILE_CLAIM, mode);
		}
		return builder.sign(Algorithm.HMAC256("synthetic-profile-selection-test-key"));
	}
}
