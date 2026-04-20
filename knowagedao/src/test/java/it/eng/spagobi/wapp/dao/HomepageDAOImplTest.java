/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2024 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.wapp.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Test;

import it.eng.spagobi.commons.metadata.SbiExtRoles;
import it.eng.spagobi.wapp.bo.Homepage;
import it.eng.spagobi.wapp.metadata.SbiHomepage;

public class HomepageDAOImplTest {

	private static final String LOAD_HOMEPAGE_ID_BY_ROLE_QUERY = "select distinct h.id from SbiHomepage h join h.sbiHomepageRoles r "
			+ "where r.extRoleId = :roleId and h.commonInfo.timeDe is null";
	private static final String LOAD_HOMEPAGE_IDS_BY_ROLE_QUERY = "select distinct h.id from SbiHomepage h join h.sbiHomepageRoles r "
			+ "where r.extRoleId in (:roleIds) and h.commonInfo.timeDe is null";
	private static final String LOAD_HOMEPAGE_BY_ID_QUERY = "from SbiHomepage h where h.id = :homepageId and h.commonInfo.timeDe is null";
	private static final String LOAD_HOMEPAGES_BY_IDS_QUERY = "from SbiHomepage h where h.id in (:homepageIds) and h.commonInfo.timeDe is null";
	private static final String LOAD_DEFAULT_HOMEPAGE_QUERY = "from SbiHomepage h where h.defaultHomepage = true and h.commonInfo.timeDe is null";
	private static final String ORACLE_UNSAFE_DISTINCT_ENTITY_QUERY_PREFIX = "select distinct h from SbiHomepage h join h.sbiHomepageRoles r";

	@Test
	public void shouldLoadRoleHomepageUsingScalarIdsInsteadOfDistinctEntitySelection() throws Exception {
		RecordingSession recordingSession = new RecordingSession();
		QueryStub roleIdQuery = recordingSession.whenQuery(LOAD_HOMEPAGE_ID_BY_ROLE_QUERY);
		roleIdQuery.setUniqueResult(12);
		QueryStub homepageByIdQuery = recordingSession.whenQuery(LOAD_HOMEPAGE_BY_ID_QUERY);
		homepageByIdQuery.setUniqueResult(homepageEntity(12, false, 7));
		TestableHomepageDAOImpl dao = new TestableHomepageDAOImpl(recordingSession.asSession());

		Homepage homepage = dao.loadHomepageByRoleId(7);

		assertNotNull(homepage);
		assertEquals(Integer.valueOf(12), homepage.getId());
		assertEquals(Collections.singletonList(7), homepage.getRoleIds());
		assertEquals(Integer.valueOf(7), roleIdQuery.getIntegerParameter("roleId"));
		assertEquals(Integer.valueOf(1), roleIdQuery.getMaxResults());
		assertEquals(Integer.valueOf(12), homepageByIdQuery.getIntegerParameter("homepageId"));
		assertFalse(recordingSession.containsQueryStartingWith(ORACLE_UNSAFE_DISTINCT_ENTITY_QUERY_PREFIX));
		assertTrue(recordingSession.isClosed());
	}

	@Test
	public void shouldFallbackToDefaultHomepageWhenNoRoleHomepageExists() throws Exception {
		RecordingSession recordingSession = new RecordingSession();
		QueryStub roleIdQuery = recordingSession.whenQuery(LOAD_HOMEPAGE_ID_BY_ROLE_QUERY);
		roleIdQuery.setUniqueResult(null);
		QueryStub defaultQuery = recordingSession.whenQuery(LOAD_DEFAULT_HOMEPAGE_QUERY);
		defaultQuery.setUniqueResult(homepageEntity(3, true));
		TestableHomepageDAOImpl dao = new TestableHomepageDAOImpl(recordingSession.asSession());

		Homepage homepage = dao.loadHomepageByRoleId(42);

		assertNotNull(homepage);
		assertTrue(homepage.isDefaultHomepage());
		assertEquals(Integer.valueOf(42), roleIdQuery.getIntegerParameter("roleId"));
		assertTrue(recordingSession.getCreatedQueries().contains(LOAD_DEFAULT_HOMEPAGE_QUERY));
		assertTrue(recordingSession.isClosed());
	}

	@Test
	public void shouldLoadRoleAssignedHomepagesForSavePathThroughScalarIds() {
		RecordingSession recordingSession = new RecordingSession();
		QueryStub homepageIdsQuery = recordingSession.whenQuery(LOAD_HOMEPAGE_IDS_BY_ROLE_QUERY);
		homepageIdsQuery.setListResult(Arrays.asList(5, 8));
		QueryStub homepagesByIdsQuery = recordingSession.whenQuery(LOAD_HOMEPAGES_BY_IDS_QUERY);
		homepagesByIdsQuery.setListResult(Arrays.asList(homepageEntity(5, false, 10), homepageEntity(8, false, 20)));
		HomepageDAOImpl dao = new HomepageDAOImpl();

		List<SbiHomepage> homepages = dao.loadHomepagesByRoleIds(recordingSession.asSession(), Arrays.asList(10, 20));

		assertEquals(2, homepages.size());
		assertEquals(Arrays.asList(10, 20), homepageIdsQuery.getParameterList("roleIds"));
		assertEquals(Arrays.asList(5, 8), homepagesByIdsQuery.getParameterList("homepageIds"));
		assertFalse(recordingSession.containsQueryStartingWith(ORACLE_UNSAFE_DISTINCT_ENTITY_QUERY_PREFIX));
		assertFalse(recordingSession.isClosed());
	}

	private static SbiHomepage homepageEntity(int homepageId, boolean defaultHomepage, Integer... roleIds) {
		SbiHomepage homepage = new SbiHomepage();
		homepage.changeId(homepageId);
		homepage.setDefaultHomepage(defaultHomepage);
		homepage.setType("static");
		homepage.setSbiHomepageRoles(roles(roleIds));
		return homepage;
	}

	private static Set<SbiExtRoles> roles(Integer... roleIds) {
		Set<SbiExtRoles> roles = new HashSet<>();
		for (Integer roleId : roleIds) {
			SbiExtRoles role = new SbiExtRoles();
			role.changeExtRoleId(roleId);
			roles.add(role);
		}
		return roles;
	}

	private static Object defaultValue(Class<?> returnType) {
		if (!returnType.isPrimitive()) {
			return null;
		}
		if (returnType == boolean.class) {
			return false;
		}
		if (returnType == byte.class) {
			return (byte) 0;
		}
		if (returnType == short.class) {
			return (short) 0;
		}
		if (returnType == int.class) {
			return 0;
		}
		if (returnType == long.class) {
			return 0L;
		}
		if (returnType == float.class) {
			return 0F;
		}
		if (returnType == double.class) {
			return 0D;
		}
		if (returnType == char.class) {
			return '\0';
		}
		return null;
	}

	private static final class TestableHomepageDAOImpl extends HomepageDAOImpl {

		private final Session session;

		private TestableHomepageDAOImpl(Session session) {
			this.session = session;
		}

		@Override
		public Session getSession() {
			return session;
		}
	}

	private static final class RecordingSession implements InvocationHandler {

		private final Map<String, QueryStub> queries = new LinkedHashMap<>();
		private final List<String> createdQueries = new ArrayList<>();
		private boolean open = true;

		private QueryStub whenQuery(String hql) {
			QueryStub query = new QueryStub();
			queries.put(hql, query);
			return query;
		}

		private Session asSession() {
			return (Session) Proxy.newProxyInstance(Session.class.getClassLoader(), new Class<?>[] { Session.class }, this);
		}

		private List<String> getCreatedQueries() {
			return createdQueries;
		}

		private boolean containsQuery(String hql) {
			return createdQueries.contains(hql);
		}

		private boolean containsQueryStartingWith(String prefix) {
			for (String hql : createdQueries) {
				if (hql.startsWith(prefix)) {
					return true;
				}
			}
			return false;
		}

		private boolean isClosed() {
			return !open;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			String methodName = method.getName();
			if ("createQuery".equals(methodName)) {
				String hql = (String) args[0];
				createdQueries.add(hql);
				QueryStub query = queries.get(hql);
				assertNotNull("Unexpected query: " + hql, query);
				return query.asQuery();
			}
			if ("isOpen".equals(methodName)) {
				return open;
			}
			if ("close".equals(methodName)) {
				open = false;
				return null;
			}
			return defaultValue(method.getReturnType());
		}
	}

	private static final class QueryStub implements InvocationHandler {

		private final Map<String, Integer> integerParameters = new HashMap<>();
		private final Map<String, List<?>> listParameters = new HashMap<>();
		private Object uniqueResult;
		private List<?> listResult = Collections.emptyList();
		private Integer maxResults;

		private Query asQuery() {
			return (Query) Proxy.newProxyInstance(Query.class.getClassLoader(), new Class<?>[] { Query.class }, this);
		}

		private void setUniqueResult(Object uniqueResult) {
			this.uniqueResult = uniqueResult;
		}

		private void setListResult(List<?> listResult) {
			this.listResult = listResult;
		}

		private Integer getIntegerParameter(String parameterName) {
			return integerParameters.get(parameterName);
		}

		private List<?> getParameterList(String parameterName) {
			return listParameters.get(parameterName);
		}

		private Integer getMaxResults() {
			return maxResults;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			String methodName = method.getName();
			if ("setInteger".equals(methodName)) {
				integerParameters.put((String) args[0], (Integer) args[1]);
				return proxy;
			}
			if ("setParameterList".equals(methodName)) {
				listParameters.put((String) args[0], toList(args[1]));
				return proxy;
			}
			if ("setMaxResults".equals(methodName)) {
				maxResults = (Integer) args[0];
				return proxy;
			}
			if ("uniqueResult".equals(methodName)) {
				return uniqueResult;
			}
			if ("list".equals(methodName)) {
				return listResult;
			}
			return defaultValue(method.getReturnType());
		}

		private List<?> toList(Object value) {
			if (value instanceof Collection<?>) {
				return new ArrayList<>((Collection<?>) value);
			}
			if (value instanceof Object[]) {
				return Arrays.asList((Object[]) value);
			}
			return Collections.singletonList(value);
		}
	}
}
