package it.eng.spagobi.security.saml;

import static org.junit.Assert.*;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;
import javax.sql.DataSource;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FederatedTokenGuardTest {
	private String originalFactory;
	private String originalDatasource;
	private FederatedSessionStore state;
	private Algorithm algorithm;
	private String sid;
	private DecodedJWT token;

	public static class TestNamingFactory implements InitialContextFactory {
		static DataSource source;

		@Override
		public Context getInitialContext(Hashtable<?, ?> environment) {
			return (Context) Proxy.newProxyInstance(Context.class.getClassLoader(), new Class<?>[] { Context.class },
					(proxy, method, args) -> "lookup".equals(method.getName()) ? source : null);
		}
	}

	@Before
	public void setup() throws Exception {
		originalFactory = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
		originalDatasource = System.getProperty("knowage.saml.datasource");
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, TestNamingFactory.class.getName());
		System.setProperty("knowage.saml.datasource", "synthetic-saml-state");
		JDBCDataSource source = new JDBCDataSource();
		source.setUrl("jdbc:hsqldb:mem:" + FederatedSessionStore.randomId());
		source.setUser("SA");
		TestNamingFactory.source = source;
		try (Connection c = source.getConnection(); Statement s = c.createStatement()) {
			s.execute("CREATE TABLE SBI_SAML_STATE (STATE_ID VARCHAR(128) PRIMARY KEY, STATE_KIND VARCHAR(16),"
					+ " SUBJECT_KEY VARCHAR(64), PAYLOAD CLOB, EXPIRES_AT BIGINT, TOKEN_HASH VARCHAR(64), ACTIVE INTEGER)");
		}
		state = new FederatedSessionStore(source);
		algorithm = Algorithm.HMAC256(FederatedSessionStore.randomId());
		sid = FederatedSessionStore.randomId();
		long now = System.currentTimeMillis();
		String encoded = JWT.create().withIssuer("knowage").withExpiresAt(new Date(now + 60000))
				.withClaim(FederatedTokenGuard.SESSION_CLAIM, sid).sign(algorithm);
		token = JWT.require(algorithm).build().verify(encoded);
		state.register(sid, "synthetic-subject", new ObjectMapper().createObjectNode().put("authnInstant", now)
				.put("tokenHash", FederatedSessionStore.digest(encoded)), now + 60000);
	}

	@After
	public void restoreNaming() {
		restore(Context.INITIAL_CONTEXT_FACTORY, originalFactory);
		restore("knowage.saml.datasource", originalDatasource);
		TestNamingFactory.source = null;
	}

	@Test
	public void revocationIsCheckedEvenForAnAlreadyVerifiedOrCachedToken() {
		FederatedTokenGuard.checkDecoded(token);
		FederatedTokenGuard.checkDecoded(token);
		state.revoke(sid);
		assertThrows(JWTVerificationException.class, () -> FederatedTokenGuard.checkDecoded(token));
	}

	@Test
	public void anotherValidSignedCredentialCannotReuseTheRegisteredSessionId() {
		String encoded = JWT.create().withIssuer("knowage").withExpiresAt(token.getExpiresAt())
				.withClaim(FederatedTokenGuard.SESSION_CLAIM, sid).withClaim("user_id", "different-user").sign(algorithm);
		DecodedJWT different = JWT.require(algorithm).build().verify(encoded);
		assertThrows(JWTVerificationException.class, () -> FederatedTokenGuard.checkDecoded(different));
	}

	@Test
	public void registryOutageFailsClosedButLegacyTokensDoNotRequireFederatedState() {
		TestNamingFactory.source = null;
		assertThrows(JWTVerificationException.class, () -> FederatedTokenGuard.checkDecoded(token));
		DecodedJWT legacy = JWT.require(algorithm).build().verify(JWT.create().withClaim("user_id", "technical-user").sign(algorithm));
		FederatedTokenGuard.checkDecoded(legacy);
	}

	private void restore(String key, String value) {
		if (value == null) {
			System.clearProperty(key);
		} else {
			System.setProperty(key, value);
		}
	}
}
