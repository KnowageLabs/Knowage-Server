package it.eng.spagobi.api.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.DriversRuntimeLoader;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.tools.catalogue.metadata.IDrivableBIResource;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DocumentExecutionAuditTest {

	@Mocked
	private AuditManager auditManager;

	@Test
	public void recordsAuthorizedDashboardExecutionWithItsUserAndRole() throws Exception {
		int[] auditCalls = prepareExecution(true, false, false);

		JSONObject template = resource().getDocumentTemplates(12, null);

		assertTrue(template.getBoolean("dashboard"));
		assertEquals(1, auditCalls[0]);
	}

	@Test
	public void doesNotRecordDeniedOrInvalidExecutions() throws Exception {
		int[] auditCalls = prepareExecution(false, false, false);
		assertRejected();
		assertEquals(0, auditCalls[0]);
	}

	@Test
	public void doesNotRecordExecutionWhenDriverValidationFails() throws Exception {
		int[] auditCalls = prepareExecution(true, true, false);
		assertRejected();
		assertEquals(0, auditCalls[0]);
	}

	@Test
	public void auditFailureDoesNotPreventReturningTheDashboardTemplate() throws Exception {
		int[] auditCalls = prepareExecution(true, false, true);

		assertTrue(resource().getDocumentTemplates(12, null).getBoolean("dashboard"));
		assertEquals(1, auditCalls[0]);
	}

	private void assertRejected() throws Exception {
		try {
			resource().getDocumentTemplates(12, null);
			fail("Expected execution to be rejected");
		} catch (SpagoBIRuntimeException expected) {
		}
	}

	private DocumentExecutionResource resource() {
		return new DocumentExecutionResource() {
			@Override
			public UserProfile getUserProfile() {
				return new UserProfile("alice", "tenant");
			}

			@Override
			public Locale getLocale() {
				return Locale.ENGLISH;
			}
		};
	}

	private int[] prepareExecution(boolean allowed, boolean invalidDrivers, boolean auditFailure) throws Exception {
		ObjTemplate template = new ObjTemplate();
		template.setContent("{\"dashboard\":true}".getBytes(StandardCharsets.UTF_8));
		BIObject document = new BIObject() {
			@Override
			public ObjTemplate getActiveTemplate() {
				return template;
			}
		};
		document.setId(12);
		document.setLabel("SALES");
		IBIObjectDAO dao = (IBIObjectDAO) Proxy.newProxyInstance(IBIObjectDAO.class.getClassLoader(),
				new Class<?>[] { IBIObjectDAO.class }, (proxy, method, args) -> {
					if ("loadBIObjectById".equals(method.getName())) {
						return document;
					}
					throw new AssertionError("Unexpected document DAO call: " + method.getName());
				});
		new MockUp<DAOFactory>() {
			@Mock
			public IBIObjectDAO getBIObjectDAO() {
				return dao;
			}
		};
		new MockUp<RestUtilities>() {
			@Mock
			public JSONObject readBodyAsJSONObject(HttpServletRequest request) throws Exception {
				return new JSONObject().put("role", "analyst").put("label", "SALES");
			}
		};
		new MockUp<ObjectsAccessVerifier>() {
			@Mock
			public boolean canExec(BIObject source, IEngUserProfile profile) {
				return allowed;
			}
		};
		new MockUp<DriversRuntimeLoader>() {
			@Mock
			public BIObject loadBIObjectForExecutionByLabelAndRole(String label, String role) {
				assertEquals("SALES", label);
				assertEquals("analyst", role);
				return document;
			}
		};
		boolean[] validated = { false };
		new MockUp<DocumentExecutionUtils>() {
			@Mock
			public List handleNormalExecutionError(UserProfile profile, IDrivableBIResource source, String role, Locale locale) {
				if (invalidDrivers) {
					throw new SpagoBIRuntimeException("Invalid drivers");
				}
				validated[0] = true;
				return Collections.emptyList();
			}
		};
		int[] auditCalls = { 0 };
		new Expectations() {
			{
				AuditManager.getInstance();
				result = auditManager;
				minTimes = 0;
				auditManager.insertAudit((BIObject) any, (SubObject) any, (IEngUserProfile) any, anyString, anyString);
				result = new Delegate() {
					public Integer insertAudit(BIObject source, SubObject view, IEngUserProfile profile, String role, String modality) {
						assertTrue("Only validated executions should be recorded", validated[0]);
						assertSame(document, source);
						assertEquals("alice", ((UserProfile) profile).getUserId());
						assertEquals("analyst", role);
						assertEquals(SpagoBIConstants.NORMAL_EXECUTION_MODALITY, modality);
						auditCalls[0]++;
						if (auditFailure) {
							throw new SpagoBIRuntimeException("Audit unavailable");
						}
						return 91;
					}
				};
				minTimes = 0;
			}
		};
		return auditCalls;
	}
}
