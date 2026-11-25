package it.eng.spagobi.api.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.rest.interceptors.RestExceptionMapper;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.DataSourceNotFoundException;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DataSourceAuthorizationTest {

	@Test
	public void hiddenGetAndStructureReturn404WithoutConnecting() throws Exception {
		mockDao(null, new ArrayList<>());
		DataSourceResource resource = resource(false);
		expect404(() -> resource.getDataSourceById(2));
		expect404(() -> resource.getDataSourceStruct(2, null, null));
	}

	@Test
	public void hiddenUpdateIsRejectedBeforeDefaultDatasourceSideEffects() throws Exception {
		List<String> calls = new ArrayList<>();
		mockDao(null, calls);
		expect404(() -> resource(false).putDataSource(source(2)));
		assertEquals(Collections.singletonList("loadDataSourceForModification"), calls);
	}

	@Test
	public void hiddenDeleteIsRejectedBeforeInspectingDependencies() throws Exception {
		List<String> calls = new ArrayList<>();
		mockDao(null, calls);
		expect404(() -> resource(false).deleteDataSourceById(2));
		assertEquals(Collections.singletonList("loadDataSourceForModification"), calls);
	}

	@Test
	public void bulkDeleteChecksAllIdsBeforeDeletingAny() throws Exception {
		List<String> calls = new ArrayList<>();
		mockDao(source(1), calls);
		expect404(() -> resource(false).deleteMultiple(Arrays.asList(1, 2)));
		assertEquals(Arrays.asList("loadDataSourceForModification", "loadDataSourceForModification"), calls);
	}

	@Test
	public void sharedDatasourceDetailsUseSameRedactionAsList() throws Exception {
		IDataSource shared = source(2);
		shared.setOwner("another-admin");
		mockDao(shared, new ArrayList<>());
		IDataSource visible = resource(false).getDataSourceById(2);
		JsonNode json = new ObjectMapper().valueToTree(visible);
		assertEquals(2, json.get("dsId").asInt());
		assertFalse(json.has("urlConnection"));
		assertFalse(json.has("user"));
		assertFalse(json.has("jndi"));
		assertFalse(json.has("pwd"));
	}

	@Test
	public void superadminKeepsFullDatasourceDetails() {
		IDataSource shared = source(2);
		mockDao(shared, new ArrayList<>());
		assertSame(shared, resource(true).getDataSourceById(2));
	}

	@Test
	public void authorizedUpdateAndDeleteStillWork() throws Exception {
		List<String> calls = new ArrayList<>();
		mockDao(source(2), calls);
		DataSourceResource resource = resource(false);
		assertEquals(Collections.emptyList(), resource.putDataSource(source(2)));
		assertEquals(Collections.emptyList(), resource.deleteDataSourceById(2));
		assertEquals(1, Collections.frequency(calls, "modifyDataSource"));
		assertEquals(1, Collections.frequency(calls, "eraseDataSource"));
	}

	@Test
	public void datasource404DoesNotChangeLegacyErrorResponses() {
		RestExceptionMapper mapper = new RestExceptionMapper();
		assertEquals(404, mapper.toResponse(new DataSourceNotFoundException()).getStatus());
		assertEquals(200, mapper.toResponse(new IllegalArgumentException("legacy error")).getStatus());
	}

	private static IDataSource source(int id) {
		IDataSource source = DataSourceFactory.getDataSource();
		source.setDsId(id);
		source.setOwner("admin");
		source.setLabel("shared");
		source.setJndi("java:comp/env/jdbc/shared");
		source.setUrlConnection("jdbc:example:shared");
		source.setUser("database-user");
		return source;
	}

	private DataSourceResource resource(boolean superadmin) {
		UserProfile profile = new UserProfile("admin", "TENANT_B");
		profile.setIsSuperadmin(superadmin);
		return new DataSourceResource() {
			@Override
			public UserProfile getUserProfile() {
				return profile;
			}

			@Override
			public Locale getLocale() {
				return Locale.ENGLISH;
			}
		};
	}

	private void mockDao(IDataSource accessible, List<String> calls) {
		IDataSourceDAO dao = (IDataSourceDAO) Proxy.newProxyInstance(IDataSourceDAO.class.getClassLoader(),
				new Class<?>[] { IDataSourceDAO.class }, (proxy, method, args) -> {
					String name = method.getName();
					if ("setUserProfile".equals(name)) {
						return null;
					}
					calls.add(name);
					switch (name) {
					case "loadDataSourceByID":
					case "loadDataSourceForModification":
						if (accessible == null || accessible.getDsId() != (Integer) args[0]) {
							throw new DataSourceNotFoundException();
						}
						return accessible;
					case "loadDataSourceWriteDefault":
						return null;
					case "loadAllDataSources":
						return Collections.emptyList();
					case "returnEntitiesAssociated":
						return Collections.emptyMap();
					case "modifyDataSource":
					case "eraseDataSource":
						return null;
					default:
						throw new AssertionError("Unexpected DAO call: " + name);
					}
				});
		new MockUp<DAOFactory>() {
			@Mock
			public IDataSourceDAO getDataSourceDAO() {
				return dao;
			}
		};
	}

	private static void expect404(CheckedAction action) throws Exception {
		try {
			action.run();
			fail("Expected HTTP 404");
		} catch (DataSourceNotFoundException expected) {
			assertEquals(404, new RestExceptionMapper().toResponse(expected).getStatus());
		}
	}

	private interface CheckedAction {
		void run() throws Exception;
	}
}
