package it.eng.spagobi.api.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.util.Collections;

import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.monitoring.dao.IAuditDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.view.dao.ISbiViewDAO;
import it.eng.spagobi.view.dao.ISbiViewForDocDAO;
import it.eng.spagobi.view.dao.ISbiViewHierarchyDAO;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class WorkspaceRecentDocumentsTest {

	@Test
	public void includesAnExecutedDocumentWithoutPersonalFolderLinksOrHotLinkConfiguration() throws Exception {
		prepare(null);

		JSONArray documents = new JSONArray(resource().getDocuments());

		assertEquals(1, documents.length());
		assertEquals("DOCUMENT", documents.getJSONObject(0).getString("kind"));
		assertEquals(2, documents.getJSONObject(0).getInt("biObjectId"));
		assertEquals("KN_TEST_1", documents.getJSONObject(0).getString("label"));
		assertEquals("1970-01-01T00:00:02Z", documents.getJSONObject(0).getString("lastRun"));
		assertFalse(documents.getJSONObject(0).getBoolean("personal"));
		assertEquals(0, documents.getJSONObject(0).getJSONArray("links").length());
	}

	@Test
	public void doesNotReturnASuccessfulEmptyListWhenAuditRetrievalFails() {
		SpagoBIDAOException failure = new SpagoBIDAOException("Audit unavailable");
		prepare(failure);
		try {
			resource().getDocuments();
			fail("Expected audit retrieval failure");
		} catch (SpagoBIRuntimeException expected) {
			assertSame(failure, expected.getCause());
		}
	}

	private WorkspaceResource resource() {
		return new WorkspaceResource() {
			@Override
			public UserProfile getUserProfile() {
				return new UserProfile("alice", "tenant");
			}
		};
	}

	private void prepare(RuntimeException failure) {
		BIObject document = new BIObject();
		document.setId(2);
		document.setLabel("KN_TEST_1");
		document.setName("KN_TEST_1");
		document.setStateCode("REL");
		document.setBiObjectTypeCode("DASHBOARD");
		document.setVisible(true);
		IAuditDAO auditDao = (IAuditDAO) Proxy.newProxyInstance(IAuditDAO.class.getClassLoader(),
				new Class<?>[] { IAuditDAO.class }, (proxy, method, args) -> {
					if ("setUserProfile".equals(method.getName())) {
						assertEquals("tenant", ((UserProfile) args[0]).getOrganization());
						return null;
					}
					if ("getLastDocumentExecutionTimes".equals(method.getName())) {
						assertEquals("alice", args[0]);
						if (failure != null) {
							throw failure;
						}
						return Collections.singletonMap(2, 2000L);
					}
					throw new AssertionError("Unexpected audit DAO call: " + method.getName());
				});
		IBIObjectDAO documentDao = (IBIObjectDAO) Proxy.newProxyInstance(IBIObjectDAO.class.getClassLoader(),
				new Class<?>[] { IBIObjectDAO.class }, (proxy, method, args) -> {
					if ("setUserProfile".equals(method.getName())) {
						return null;
					}
					if ("loadPaginatedSearchBIObjects".equals(method.getName())) {
						return Collections.singletonList(document);
					}
					throw new AssertionError("Unexpected document DAO call: " + method.getName());
				});
		new MockUp<DAOFactory>() {
			@Mock
			public IAuditDAO getAuditDAO() {
				return auditDao;
			}

			@Mock
			public IBIObjectDAO getBIObjectDAO() {
				return documentDao;
			}

			@Mock
			public ISbiViewHierarchyDAO getSbiViewHierarchyDAO() {
				return emptyWorkspaceDao(ISbiViewHierarchyDAO.class);
			}

			@Mock
			public ISbiViewDAO getSbiViewDAO() {
				return emptyWorkspaceDao(ISbiViewDAO.class);
			}

			@Mock
			public ISbiViewForDocDAO getSbiViewForDocDAO() {
				return emptyWorkspaceDao(ISbiViewForDocDAO.class);
			}
		};
		new MockUp<UserUtilities>() {
			@Mock
			public LowFunctionality loadUserFunctionalityRoot(UserProfile profile, boolean create) {
				return null;
			}
		};
		new MockUp<ObjectsAccessVerifier>() {
			@Mock
			public boolean canSee(BIObject source, IEngUserProfile profile) {
				return true;
			}
		};
	}

	private <T> T emptyWorkspaceDao(Class<T> type) {
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (proxy, method, args) -> {
			if ("setUserProfile".equals(method.getName())) {
				return null;
			}
			if ("readAllOwned".equals(method.getName())) {
				return Collections.emptySet();
			}
			throw new AssertionError("Unexpected workspace DAO call: " + method.getName());
		}));
	}
}
