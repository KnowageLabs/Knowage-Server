package it.eng.spagobi.security.saml;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class FederatedSessionStoreTest {
	private final ObjectMapper json = new ObjectMapper();
	private FederatedSessionStore first;
	private FederatedSessionStore second;

	@Before
	public void setup() throws Exception {
		JDBCDataSource source = new JDBCDataSource();
		source.setUrl("jdbc:hsqldb:mem:" + FederatedSessionStore.randomId());
		source.setUser("SA");
		try (Connection c = source.getConnection()) {
			c.createStatement().execute("CREATE TABLE SBI_SAML_STATE (STATE_ID VARCHAR(128) PRIMARY KEY, STATE_KIND VARCHAR(16),"
					+ " SUBJECT_KEY VARCHAR(64), PAYLOAD CLOB, EXPIRES_AT BIGINT, TOKEN_HASH VARCHAR(64), ACTIVE INTEGER)");
		}
		first = new FederatedSessionStore(source);
		second = new FederatedSessionStore(source);
	}

	@Test
	public void browserBoundCodesAreConsumedExactlyOnceAcrossNodes() throws Exception {
		first.put("code", "CODE", null, json.createObjectNode().put("binding", "expected"), System.currentTimeMillis() + 60000);
		assertNull(second.consume("code", "CODE", "wrong-browser"));
		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Callable<Boolean> consumeFirst = () -> first.consume("code", "CODE", "expected") != null;
			Callable<Boolean> consumeSecond = () -> second.consume("code", "CODE", "expected") != null;
			List<Future<Boolean>> results = executor.invokeAll(List.of(consumeFirst, consumeSecond));
			assertNotEquals(results.get(0).get(), results.get(1).get());
			assertNull(first.consume("code", "CODE", "expected"));
		} finally {
			executor.shutdownNow();
		}
	}

	@Test
	public void logoutWithoutBrowserRevokesOnlyMatchingIndexesThenAllSessions() {
		long now = System.currentTimeMillis();
		first.register("one", "subject", context("index-one", now), now + 60000);
		second.register("two", "subject", context("index-two", now), now + 60000);
		first.register("other", "other-subject", context("index-one", now), now + 60000);
		second.revokeSubject("subject", List.of("index-one"), now + 1);
		assertNull(first.get("one", "SESSION"));
		assertNotNull(first.get("two", "SESSION"));
		assertNotNull(first.get("other", "SESSION"));
		first.revokeSubject("subject", List.of(), now + 2);
		assertNull(second.get("two", "SESSION"));
		assertNotNull(second.get("other", "SESSION"));
		assertThrows(IllegalStateException.class, () -> second.register("late", "subject", context("index-one", now), now + 60000));
		first.revokeSubject("subject", List.of(), now + 2);
	}

	@Test
	public void indexTombstonePreventsInFlightLoginFromSurvivingLogout() {
		long now = System.currentTimeMillis();
		first.revokeSubject("subject", List.of("index"), now);
		assertThrows(IllegalStateException.class,
				() -> second.register("late", "subject", context("index", now - 1), now + 60000));
		second.register("new", "subject", context("new-index", now + 1), now + 60000);
		assertNotNull(first.get("new", "SESSION"));
	}

	@Test
	public void expiredStateAndMissingSessionsAreNeverActive() {
		first.put("expired", "CODE", null, json.createObjectNode(), System.currentTimeMillis() - 1);
		assertNull(second.consume("expired", "CODE", null));
		assertNull(second.get("unknown", "SESSION"));
		first.cleanup();
		assertNull(first.get("expired", "CODE"));
	}

	private ObjectNode context(String index, long authnInstant) {
		return json.createObjectNode().put("sessionIndex", index).put("authnInstant", authnInstant).put("tokenHash", "synthetic");
	}
}
