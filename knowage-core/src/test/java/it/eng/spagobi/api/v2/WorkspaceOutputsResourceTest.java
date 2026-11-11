package it.eng.spagobi.api.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.tools.scheduler.SchedulerConstants;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.user.UserProfileManager;
import mockit.Mock;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class WorkspaceOutputsResourceTest {

    @After
    public void cleanUp() {
        UserProfileManager.unset();
    }

    @Test
    public void excludesMixedVisibilityJobsAndAppliesLimitAfterFiltering() throws Exception {
        List<Snapshot> snapshots = Arrays.asList(
                snapshot(11, 1, "mixed"), snapshot(12, 2, "visible"),
                snapshot(13, 1, "deleted-job"), snapshot(14, 1, "visible"));
        mockDaos(Arrays.asList(job("mixed", "public__1,private__2"),
                job("visible", "public__1,deleted__2")), snapshots, new ArrayList<>());

        JSONArray result = new JSONArray(new WorkspaceOutputsResource().getOutputs(1));
        assertEquals(1, result.length());
        JSONObject output = result.getJSONObject(0);
        assertEquals(14, output.getInt("snapshotId"));
        assertEquals("Visible document", output.getString("documentName"));
        assertEquals("DASHBOARD", output.getString("documentTypeCode"));
        assertEquals(SchedulerConstants.JOB_GROUP, output.getString("jobGroup"));
        assertEquals("1970-01-01T00:00:01Z", output.getString("created"));
        assertTrue(output.has("contentType"));
        assertTrue(output.isNull("contentType"));
    }

    @Test
    public void continuesPastAFullPageOfInvisibleSnapshots() throws Exception {
        List<Snapshot> snapshots = new ArrayList<>();
        for (int id = 1; id <= 200; id++) {
            snapshots.add(snapshot(id, 2, "visible"));
        }
        snapshots.add(snapshot(201, 1, "visible"));
        List<Integer> offsets = new ArrayList<>();
        mockDaos(Arrays.asList(job("hidden", "private__1"), job("visible", "public__1")), snapshots, offsets);

        JSONArray result = new JSONArray(new WorkspaceOutputsResource().getOutputs(1));
        assertEquals(201, result.getJSONObject(0).getInt("snapshotId"));
        assertEquals(Arrays.asList(0, 200), offsets);
    }

    @Test
    public void rejectsInvalidLimitBeforeAccessingDaos() {
        try {
            new WorkspaceOutputsResource().getOutputs(0);
            fail("Expected invalid limit to be rejected");
        } catch (WebApplicationException expected) {
            assertEquals(400, expected.getResponse().getStatus());
        }
    }

    private void mockDaos(List<Job> jobs, List<Snapshot> snapshots, List<Integer> offsets) {
        UserProfileManager.setProfile(new UserProfile("reader", "tenant"));
        List<BIObject> documents = Arrays.asList(document(1, "public"), document(2, "private"));
        IBIObjectDAO documentDao = (IBIObjectDAO) Proxy.newProxyInstance(IBIObjectDAO.class.getClassLoader(),
                new Class<?>[] { IBIObjectDAO.class }, (proxy, method, args) -> {
                    if ("setUserProfile".equals(method.getName())) {
                        return null;
                    }
                    if ("loadPaginatedSearchBIObjects".equals(method.getName())) {
                        return documents;
                    }
                    throw new AssertionError("Unexpected document DAO call: " + method.getName());
                });
        ISnapshotDAO snapshotDao = (ISnapshotDAO) Proxy.newProxyInstance(ISnapshotDAO.class.getClassLoader(),
                new Class<?>[] { ISnapshotDAO.class }, (proxy, method, args) -> {
                    if ("setUserProfile".equals(method.getName())) {
                        return null;
                    }
                    if ("getWorkspaceSnapshots".equals(method.getName())) {
                        assertEquals(Collections.singleton("visible"), args[0]);
                        int offset = (Integer) args[1];
                        offsets.add(offset);
                        return offset >= snapshots.size() ? Collections.emptyList()
                                : snapshots.subList(offset, Math.min(offset + (Integer) args[2], snapshots.size()));
                    }
                    throw new AssertionError("Unexpected snapshot DAO call: " + method.getName());
                });
        ISchedulerDAO schedulerDao = (ISchedulerDAO) Proxy.newProxyInstance(ISchedulerDAO.class.getClassLoader(),
                new Class<?>[] { ISchedulerDAO.class }, (proxy, method, args) -> {
                    if ("setTenant".equals(method.getName())) {
                        assertEquals("tenant", args[0]);
                        return null;
                    }
                    if ("loadJobs".equals(method.getName())) {
                        assertEquals(SchedulerConstants.JOB_GROUP, args[0]);
                        return jobs;
                    }
                    throw new AssertionError("Unexpected scheduler DAO call: " + method.getName());
                });
        new MockUp<DAOFactory>() {
            @Mock
            public IBIObjectDAO getBIObjectDAO() {
                return documentDao;
            }

            @Mock
            public ISnapshotDAO getSnapshotDAO() {
                return snapshotDao;
            }

            @Mock
            public ISchedulerDAO getSchedulerDAO() {
                return schedulerDao;
            }
        };
        new MockUp<ObjectsAccessVerifier>() {
            @Mock
            public boolean canSee(BIObject document, IEngUserProfile profile) {
                return document.getId() == 1;
            }
        };
    }

    private Job job(String name, String labels) {
        Job job = new Job();
        job.setName(name);
        job.setGroupName(SchedulerConstants.JOB_GROUP);
        job.getParameters().put("documentLabels", labels);
        return job;
    }

    private BIObject document(int id, String label) {
        BIObject document = new BIObject();
        document.setId(id);
        document.setLabel(label);
        document.setName("Visible document");
        document.setBiObjectTypeCode("DASHBOARD");
        return document;
    }

    private Snapshot snapshot(int id, int documentId, String scheduler) {
        Snapshot snapshot = new Snapshot() {
            @Override
            public byte[] getContent() {
                throw new AssertionError("Workspace metadata must not load snapshot content");
            }
        };
        snapshot.setId(id);
        snapshot.setBiobjId(documentId);
        snapshot.setScheduler(scheduler);
        snapshot.setDateCreation(new Date(1000));
        return snapshot;
    }
}
