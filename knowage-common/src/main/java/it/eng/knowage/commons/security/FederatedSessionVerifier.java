package it.eng.knowage.commons.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Date;

import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * Called only after the consumer has cryptographically verified the JWT.
 */
public final class FederatedSessionVerifier {
	private FederatedSessionVerifier() {
	}

	public static void verify(String token, String sid, String issuer, Date expiration) {
		if (!"knowage".equals(issuer) || expiration == null || expiration.getTime() <= System.currentTimeMillis()
				|| sid == null || !sid.matches("[A-Za-z0-9_-]{43}")) {
			throw new IllegalStateException("Invalid federated credential");
		}
		try {
			String name = System.getProperty("knowage.saml.datasource", System.getenv("KNOWAGE_SAML_DATASOURCE"));
			if (name == null || name.trim().isEmpty()) {
				throw new IllegalStateException("Federated datasource is not configured");
			}
			DataSource source = (DataSource) new InitialContext().lookup(name);
			String hash = Base64.getUrlEncoder().withoutPadding().encodeToString(
					MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
			try (Connection c = source.getConnection(); PreparedStatement p = c.prepareStatement(
					"SELECT TOKEN_HASH FROM SBI_SAML_STATE WHERE STATE_ID = ? AND STATE_KIND = 'SESSION' AND ACTIVE = 1 AND EXPIRES_AT > ?")) {
				p.setString(1, sid);
				p.setLong(2, System.currentTimeMillis());
				try (ResultSet r = p.executeQuery()) {
					if (!r.next() || !hash.equals(r.getString(1))) {
						throw new IllegalStateException("Federated credential expired or revoked");
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("Federated session is unavailable or revoked", e);
		}
	}
}
