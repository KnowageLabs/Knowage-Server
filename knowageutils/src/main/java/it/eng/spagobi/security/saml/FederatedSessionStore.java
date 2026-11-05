package it.eng.spagobi.security.saml;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Shared database state: no process-local allow-list or revocation cache.
 */
public class FederatedSessionStore {
	private static final ObjectMapper JSON = new ObjectMapper();
	private static final SecureRandom RANDOM = new SecureRandom();
	private final DataSource dataSource;

	public FederatedSessionStore(DataSource dataSource) {
		this.dataSource = Objects.requireNonNull(dataSource);
	}

	public static FederatedSessionStore configured() {
		try {
			String name = System.getProperty("knowage.saml.datasource", System.getenv("KNOWAGE_SAML_DATASOURCE"));
			if (name == null || name.trim().isEmpty()) {
				throw new IllegalStateException("SAML shared datasource is not configured");
			}
			return new FederatedSessionStore((DataSource) new InitialContext().lookup(name));
		} catch (Exception e) {
			throw new IllegalStateException("SAML shared state is unavailable", e);
		}
	}

	public static String randomId() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	public static String digest(String value) {
		try {
			return Base64.getUrlEncoder().withoutPadding()
					.encodeToString(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	public static String subjectKey(String issuer, JsonNode nameId, String spEntityId) {
		// Omitted qualifiers use the issuing IdP and this SP, not wildcard matching.
		return digest(JSON.createArrayNode().add(issuer).add(nameId.path("Value").asText())
				.add(nameId.path("Format").asText("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified"))
				.add(nameId.path("NameQualifier").asText(issuer))
				.add(nameId.path("SPNameQualifier").asText(spEntityId)).toString());
	}

	public void put(String id, String kind, String subject, JsonNode payload, long expiresAt) {
		try (Connection c = dataSource.getConnection()) {
			insert(c, id, kind, subject, payload, expiresAt);
		} catch (SQLException e) {
			throw new IllegalStateException("Cannot persist SAML state", e);
		}
	}

	private void insert(Connection c, String id, String kind, String subject, JsonNode payload, long expiresAt) throws SQLException {
		try (PreparedStatement p = c.prepareStatement(
				"INSERT INTO SBI_SAML_STATE (STATE_ID, STATE_KIND, SUBJECT_KEY, PAYLOAD, EXPIRES_AT, TOKEN_HASH, ACTIVE) VALUES (?, ?, ?, ?, ?, ?, 1)")) {
			p.setString(1, id);
			p.setString(2, kind);
			p.setString(3, subject);
			p.setString(4, payload.toString());
			p.setLong(5, expiresAt);
			p.setString(6, payload.path("tokenHash").asText(null));
			p.executeUpdate();
		}
	}

	public JsonNode get(String id, String kind) {
		try (Connection c = dataSource.getConnection()) {
			return read(c, id, kind);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot read SAML state", e);
		}
	}

	private JsonNode read(Connection c, String id, String kind) throws Exception {
		try (PreparedStatement p = c.prepareStatement(
				"SELECT PAYLOAD FROM SBI_SAML_STATE WHERE STATE_ID = ? AND STATE_KIND = ? AND ACTIVE = 1 AND EXPIRES_AT > ?")) {
			p.setString(1, id);
			p.setString(2, kind);
			p.setLong(3, System.currentTimeMillis());
			try (ResultSet r = p.executeQuery()) {
				return r.next() ? JSON.readTree(r.getString(1)) : null;
			}
		}
	}

	/**
	 * The conditional update is the cross-node compare-and-set for one-time state.
	 */
	public JsonNode consume(String id, String kind, String binding) {
		try (Connection c = dataSource.getConnection()) {
			c.setAutoCommit(false);
			try {
				JsonNode value = read(c, id, kind);
				if (value == null || !Objects.equals(binding, value.path("binding").asText(null))) {
					c.rollback();
					return null;
				}
				try (PreparedStatement p = c.prepareStatement(
						"UPDATE SBI_SAML_STATE SET ACTIVE = 0 WHERE STATE_ID = ? AND STATE_KIND = ? AND ACTIVE = 1 AND EXPIRES_AT > ?")) {
					p.setString(1, id);
					p.setString(2, kind);
					p.setLong(3, System.currentTimeMillis());
					if (p.executeUpdate() != 1) {
						c.rollback();
						return null;
					}
				}
				c.commit();
				return value;
			} catch (Exception e) {
				c.rollback();
				throw e;
			}
		} catch (Exception e) {
			throw new IllegalStateException("Cannot consume SAML state", e);
		}
	}

	public void revoke(String sid) {
		try (Connection c = dataSource.getConnection(); PreparedStatement p = c.prepareStatement(
				"UPDATE SBI_SAML_STATE SET ACTIVE = 0 WHERE STATE_ID = ? AND STATE_KIND = 'SESSION'")) {
			p.setString(1, sid);
			p.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Cannot revoke SAML session", e);
		}
	}

	public void register(String sid, String subject, ObjectNode context, long expiresAt) {
		withSubjectLock(subject, c -> {
			JsonNode tombstone = read(c, "subject:" + subject, "SUBJECT");
			long logout = Math.max(tombstone.path("loggedOutAt").asLong(),
					tombstone.path("indexes").path(context.path("sessionIndex").asText("")).asLong());
			if (logout >= context.path("authnInstant").asLong()) {
				throw new IllegalStateException("SAML authentication predates logout");
			}
			insert(c, sid, "SESSION", subject, context, expiresAt);
		});
	}

	public void revokeSubject(String subject, List<String> indexes, long logoutInstant) {
		withSubjectLock(subject, c -> {
			List<String> ids = new ArrayList<>();
			try (PreparedStatement p = c.prepareStatement(
					"SELECT STATE_ID, PAYLOAD FROM SBI_SAML_STATE WHERE STATE_KIND = 'SESSION' AND SUBJECT_KEY = ? AND ACTIVE = 1")) {
				p.setString(1, subject);
				try (ResultSet r = p.executeQuery()) {
					while (r.next()) {
						JsonNode value = JSON.readTree(r.getString(2));
						if (indexes.isEmpty() || indexes.contains(value.path("sessionIndex").asText(null))) {
							ids.add(r.getString(1));
						}
					}
				}
			}
			try (PreparedStatement p = c.prepareStatement("UPDATE SBI_SAML_STATE SET ACTIVE = 0 WHERE STATE_ID = ?")) {
				for (String id : ids) {
					p.setString(1, id);
					p.executeUpdate();
				}
			}
			ObjectNode value = (ObjectNode) read(c, "subject:" + subject, "SUBJECT");
			if (indexes.isEmpty()) {
				value.put("loggedOutAt", Math.max(logoutInstant, value.path("loggedOutAt").asLong()));
			} else {
				ObjectNode indexed = value.has("indexes") ? (ObjectNode) value.get("indexes") : value.putObject("indexes");
				for (String index : indexes) {
					indexed.put(index, Math.max(logoutInstant, indexed.path(index).asLong()));
				}
			}
			try (PreparedStatement p = c.prepareStatement("UPDATE SBI_SAML_STATE SET PAYLOAD = ? WHERE STATE_ID = ?")) {
				p.setString(1, value.toString());
				p.setString(2, "subject:" + subject);
				p.executeUpdate();
			}
		});
	}

	private interface SubjectOperation {
		void run(Connection connection) throws Exception;
	}

	private void withSubjectLock(String subject, SubjectOperation operation) {
		String key = "subject:" + subject;
		// Seed on a separate auto-commit connection; a duplicate key is harmless.
		try (Connection c = dataSource.getConnection()) {
			try {
				insert(c, key, "SUBJECT", subject, JSON.createObjectNode().put("loggedOutAt", 0),
						System.currentTimeMillis() + 86400000L);
			} catch (SQLException e) {
				if (e.getSQLState() == null || !e.getSQLState().startsWith("23")) {
					throw e;
				}
			}
			c.setAutoCommit(false);
			try {
				try (PreparedStatement p = c.prepareStatement("SELECT STATE_ID FROM SBI_SAML_STATE WHERE STATE_ID = ? FOR UPDATE")) {
					p.setString(1, key);
					try (ResultSet r = p.executeQuery()) {
						if (!r.next()) {
							throw new IllegalStateException("Missing SAML subject lock");
						}
					}
				}
				try (PreparedStatement p = c.prepareStatement("UPDATE SBI_SAML_STATE SET EXPIRES_AT = ? WHERE STATE_ID = ?")) {
					p.setLong(1, System.currentTimeMillis() + 86400000L);
					p.setString(2, key);
					p.executeUpdate();
				}
				operation.run(c);
				c.commit();
			} catch (Exception e) {
				c.rollback();
				throw e;
			}
		} catch (Exception e) {
			throw new IllegalStateException("Cannot update federated sessions", e);
		}
	}

	public void cleanup() {
		try (Connection c = dataSource.getConnection(); PreparedStatement p = c.prepareStatement(
				"DELETE FROM SBI_SAML_STATE WHERE EXPIRES_AT <= ?")) {
			p.setLong(1, System.currentTimeMillis());
			p.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Cannot clean SAML state", e);
		}
	}
}
