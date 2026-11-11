package it.eng.spagobi.commons.utilities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.util.Collections;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class SnapshotAccessVerifierTest {

    @Test
    public void rejectsMismatchedDocumentBeforeLoadingDocumentOrContent() throws Exception {
        new MockUp<DAOFactory>() {
            @Mock
            public IBIObjectDAO getBIObjectDAO() {
                throw new AssertionError("Mismatched snapshot must be rejected before document lookup");
            }
        };
        Snapshot snapshot = new Snapshot();
        snapshot.setBiobjId(42);
        try {
            SnapshotAccessVerifier.checkSnapshot(snapshot, 17, new UserProfile("reader", "tenant"));
            fail("Expected mismatched snapshot to be rejected");
        } catch (WebApplicationException expected) {
            assertEquals(403, expected.getResponse().getStatus());
        }
    }

    @Test
    public void rejectsMissingIdentityWithoutReadingTheDatabase() throws Exception {
        new MockUp<DAOFactory>() {
            @Mock
            public IBIObjectDAO getBIObjectDAO() {
                throw new AssertionError("Anonymous access must not perform a document lookup");
            }
        };
        assertFalse(SnapshotAccessVerifier.canSeeDocument(42, null));
    }

    @Test
    public void rejectsOtherTenantEvenForDocumentAdministrator() throws Exception {
        BIObject document = new BIObject();
        document.setTenant("other-tenant");
        IBIObjectDAO dao = (IBIObjectDAO) Proxy.newProxyInstance(IBIObjectDAO.class.getClassLoader(),
                new Class<?>[] { IBIObjectDAO.class }, (proxy, method, args) -> {
                    if ("loadBIObjectById".equals(method.getName())) {
                        return document;
                    }
                    if ("setUserProfile".equals(method.getName())) {
                        return null;
                    }
                    throw new AssertionError("Unexpected document DAO call: " + method.getName());
                });
        new MockUp<DAOFactory>() {
            @Mock
            public IBIObjectDAO getBIObjectDAO() {
                return dao;
            }
        };
        UserProfile profile = new UserProfile("administrator", "tenant");
        profile.setFunctionalities(Collections.singletonList(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN));
        profile.setIsSuperadmin(true);
        assertFalse(SnapshotAccessVerifier.canSeeDocument(42, profile));
    }
}
