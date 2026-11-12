package it.eng.spagobi.monitoring.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Test;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;

public class DbAuditRecentExecutionsTest {

	@Test
	public void readsOneTimestampPerDocumentWithoutLegacyParametersOrEngineJoins() {
		Map<String, String> parameters = new HashMap<>();
		boolean[] closed = { false };
		Query query = (Query) Proxy.newProxyInstance(Query.class.getClassLoader(), new Class<?>[] { Query.class },
				(proxy, method, args) -> {
					if ("setString".equals(method.getName())) {
						parameters.put((String) args[0], (String) args[1]);
						return proxy;
					}
					if ("list".equals(method.getName())) {
						return Arrays.asList(new Object[] { 2, new Timestamp(2000) }, new Object[] { 7, new Timestamp(1000) });
					}
					throw new AssertionError("Unexpected query call: " + method.getName());
				});
		DbAuditImpl dao = dao(query, closed);

		Map<Integer, Long> times = dao.getLastDocumentExecutionTimes("alice");

		assertEquals(Long.valueOf(2000), times.get(2));
		assertEquals(Long.valueOf(1000), times.get(7));
		assertEquals(2, times.size());
		assertEquals("alice", parameters.get("userId"));
		assertEquals("tenant", parameters.get("tenant"));
		assertTrue(closed[0]);
	}

	@Test
	public void propagatesReadFailureInsteadOfPretendingThereAreNoRecentDocuments() {
		HibernateException failure = new HibernateException("Audit query failed");
		boolean[] closed = { false };
		Query query = (Query) Proxy.newProxyInstance(Query.class.getClassLoader(), new Class<?>[] { Query.class },
				(proxy, method, args) -> {
					if ("setString".equals(method.getName())) {
						return proxy;
					}
					if ("list".equals(method.getName())) {
						throw failure;
					}
					throw new AssertionError("Unexpected query call: " + method.getName());
				});
		try {
			dao(query, closed).getLastDocumentExecutionTimes("alice");
			fail("Expected audit read failure");
		} catch (SpagoBIDAOException expected) {
			assertSame(failure, expected.getCause());
			assertTrue(closed[0]);
		}
	}

	private DbAuditImpl dao(Query query, boolean[] closed) {
		Session session = (Session) Proxy.newProxyInstance(Session.class.getClassLoader(), new Class<?>[] { Session.class },
				(proxy, method, args) -> {
					if ("createQuery".equals(method.getName())) {
						String hql = (String) args[0];
						assertTrue(hql.contains("a.userName = :userId"));
						assertTrue(hql.contains("a.commonInfo.organization = :tenant"));
						assertTrue(hql.contains("group by a.documentId"));
						assertTrue(hql.contains("max(a.requestTime)"));
						assertFalse(hql.contains("documentParameters"));
						assertFalse(hql.contains("sbiEngine"));
						return query;
					}
					if ("isOpen".equals(method.getName())) {
						return !closed[0];
					}
					if ("close".equals(method.getName())) {
						closed[0] = true;
						return null;
					}
					throw new AssertionError("Unexpected session call: " + method.getName());
				});
		DbAuditImpl dao = new DbAuditImpl() {
			@Override
			public Session getSession() {
				return session;
			}
		};
		UserProfile profile = new UserProfile("alice", "tenant");
		profile.setFunctionalities(Collections.emptyList());
		dao.setUserProfile(profile);
		return dao;
	}
}
