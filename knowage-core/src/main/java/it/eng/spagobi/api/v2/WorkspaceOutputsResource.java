package it.eng.spagobi.api.v2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.Snapshot;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.ISnapshotDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.CriteriaParameter;
import it.eng.spagobi.commons.bo.CriteriaParameter.Match;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.scheduler.SchedulerConstants;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/2.0/workspace/scheduler")
public class WorkspaceOutputsResource extends AbstractSpagoBIResource {

    @GET
    @Path("/outputs")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    @UserConstraint(functionalities = { CommunityFunctionalityConstants.SEE_SNAPSHOTS_FUNCTIONALITY })
    public String getOutputs(@DefaultValue("200") @QueryParam("limit") int limit) {
        if (limit < 1) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        if (getUserProfile() == null || getUserProfile().getOrganization() == null) {
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        try {
            IBIObjectDAO documentDao = DAOFactory.getBIObjectDAO();
            documentDao.setUserProfile(getUserProfile());
            Map<Integer, BIObject> documents = new HashMap<>();
            Set<Integer> checkedDocuments = new LinkedHashSet<>();
            Map<String, Job> jobs = loadVisibleJobs(documentDao, documents, checkedDocuments);
            JSONArray outputs = new JSONArray();
            if (jobs.isEmpty()) {
                return outputs.toString();
            }
            ISnapshotDAO snapshotDao = DAOFactory.getSnapshotDAO();
            snapshotDao.setUserProfile(getUserProfile());
            int pageSize = Math.min(Math.max(limit, 200), 900);
            int offset = 0;
            Set<Integer> returnedSnapshots = new LinkedHashSet<>();
            while (outputs.length() < limit) {
                List<Snapshot> snapshots = snapshotDao.getWorkspaceSnapshots(jobs.keySet(), offset, pageSize);
                Set<Integer> idsToLoad = new LinkedHashSet<>();
                for (Snapshot snapshot : snapshots) {
                    if (jobs.containsKey(snapshot.getScheduler()) && !checkedDocuments.contains(snapshot.getBiobjId())) {
                        idsToLoad.add(snapshot.getBiobjId());
                    }
                }
                if (!idsToLoad.isEmpty()) {
                    CriteriaParameter byIds = new CriteriaParameter("biobjId", idsToLoad.toArray(new Integer[0]), Match.IN);
                    List<BIObject> loaded = documentDao.loadPaginatedSearchBIObjects(
                            null, null, Collections.emptyList(), Collections.singletonList(byIds));
                    for (BIObject document : loaded) {
                        if (ObjectsAccessVerifier.canSee(document, getUserProfile())) {
                            documents.put(document.getId(), document);
                        }
                    }
                    checkedDocuments.addAll(idsToLoad);
                }
                for (Snapshot snapshot : snapshots) {
                    Job job = jobs.get(snapshot.getScheduler());
                    BIObject document = documents.get(snapshot.getBiobjId());
                    if (job == null || document == null || !returnedSnapshots.add(snapshot.getId())) {
                        continue;
                    }
                    JSONObject item = new JSONObject();
                    item.put("snapshotId", snapshot.getId());
                    item.put("biObjectId", document.getId());
                    item.put("documentLabel", document.getLabel() == null ? JSONObject.NULL : document.getLabel());
                    item.put("documentName", document.getName() == null ? JSONObject.NULL : document.getName());
                    item.put("documentTypeCode", document.getBiObjectTypeCode() == null ? JSONObject.NULL : document.getBiObjectTypeCode());
                    item.put("jobName", job.getName());
                    item.put("jobGroup", job.getGroupName());
                    item.put("name", snapshot.getName() == null ? JSONObject.NULL : snapshot.getName());
                    item.put("created", snapshot.getDateCreation() == null ? JSONObject.NULL
                            : Instant.ofEpochMilli(snapshot.getDateCreation().getTime()).toString());
                    item.put("contentType", snapshot.getContentType() == null ? JSONObject.NULL : snapshot.getContentType());
                    outputs.put(item);
                    if (outputs.length() == limit) {
                        break;
                    }
                }
                if (snapshots.size() < pageSize) {
                    break;
                }
                offset += pageSize;
            }
            return outputs.toString();
        } catch (Exception e) {
            throw new SpagoBIRuntimeException("Error retrieving workspace scheduled outputs", e);
        }
    }

    private Map<String, Job> loadVisibleJobs(IBIObjectDAO documentDao, Map<Integer, BIObject> documents,
            Set<Integer> checkedDocuments) throws Exception {
        ISchedulerDAO schedulerDao = DAOFactory.getSchedulerDAO();
        schedulerDao.setTenant(getUserProfile().getOrganization());
        List<Job> jobs = schedulerDao.loadJobs(SchedulerConstants.JOB_GROUP);
        Map<String, Set<String>> labelsByJob = new HashMap<>();
        Set<String> allLabels = new LinkedHashSet<>();
        for (Job job : jobs) {
            Set<String> labels = new LinkedHashSet<>();
            String configuredLabels = job.getParameters().get("documentLabels");
            if (configuredLabels != null && !configuredLabels.isEmpty()) {
                for (String value : configuredLabels.split(",")) {
                    int suffix = value.lastIndexOf("__");
                    labels.add(suffix > 0 ? value.substring(0, suffix) : value);
                }
            }
            labelsByJob.put(job.getName(), labels);
            allLabels.addAll(labels);
        }
        Set<String> existingLabels = new LinkedHashSet<>();
        Set<String> visibleLabels = new LinkedHashSet<>();
        List<String> labels = new ArrayList<>(allLabels);
        for (int offset = 0; offset < labels.size(); offset += 900) {
            String[] batch = labels.subList(offset, Math.min(offset + 900, labels.size())).toArray(new String[0]);
            CriteriaParameter byLabels = new CriteriaParameter("label", batch, Match.IN);
            for (BIObject document : documentDao.loadPaginatedSearchBIObjects(
                    null, null, Collections.emptyList(), Collections.singletonList(byLabels))) {
                existingLabels.add(document.getLabel());
                checkedDocuments.add(document.getId());
                if (ObjectsAccessVerifier.canSee(document, getUserProfile())) {
                    visibleLabels.add(document.getLabel());
                    documents.put(document.getId(), document);
                }
            }
        }
        Map<String, Job> visibleJobs = new HashMap<>();
        for (Job job : jobs) {
            boolean visible = true;
            // Match scheduleree/listAllJobs: one inaccessible document hides the whole job.
            for (String label : labelsByJob.get(job.getName())) {
                if (existingLabels.contains(label) && !visibleLabels.contains(label)) {
                    visible = false;
                    break;
                }
            }
            if (visible) {
                visibleJobs.put(job.getName(), job);
            }
        }
        return visibleJobs;
    }
}
