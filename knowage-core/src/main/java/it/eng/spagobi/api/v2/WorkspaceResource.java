package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.CriteriaParameter;
import it.eng.spagobi.commons.bo.CriteriaParameter.Match;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.hotlink.constants.HotLinkConstants;
import it.eng.spagobi.hotlink.rememberme.bo.HotLink;
import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.view.dao.ISbiViewDAO;
import it.eng.spagobi.view.dao.ISbiViewForDocDAO;
import it.eng.spagobi.view.dao.ISbiViewHierarchyDAO;
import it.eng.spagobi.view.metadata.SbiView;
import it.eng.spagobi.view.metadata.SbiViewForDoc;
import it.eng.spagobi.view.metadata.SbiViewHierarchy;

@Path("/2.0/workspace")
public class WorkspaceResource extends AbstractSpagoBIResource {

    private static final Logger LOGGER = Logger.getLogger(WorkspaceResource.class);
    private static final int DOCUMENT_BATCH_SIZE = 900;

    @GET
    @Path("/documents")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public String getDocuments() {
        long start = System.nanoTime();
        UserProfile profile = getUserProfile();
        if (profile == null) {
            throw new SpagoBIRuntimeException("Authentication required");
        }

        try {
            ISbiViewHierarchyDAO hierarchyDao = DAOFactory.getSbiViewHierarchyDAO();
            hierarchyDao.setUserProfile(profile);
            Set<SbiViewHierarchy> folders = hierarchyDao.readAllOwned();

            ISbiViewDAO viewDao = DAOFactory.getSbiViewDAO();
            viewDao.setUserProfile(profile);
            Set<SbiView> views = viewDao.readAllOwned();

            ISbiViewForDocDAO docLinkDao = DAOFactory.getSbiViewForDocDAO();
            docLinkDao.setUserProfile(profile);
            Set<SbiViewForDoc> links = docLinkDao.readAllOwned();

            Set<String> ownedFolderIds = new LinkedHashSet<>();
            for (SbiViewHierarchy folder : folders) {
                ownedFolderIds.add(folder.getId());
            }

            Map<Integer, Set<String>> folderIdsByDocument = new LinkedHashMap<>();
            Map<Integer, List<SbiView>> viewsByDocument = new LinkedHashMap<>();
            Set<Integer> candidateIds = new LinkedHashSet<>();

            for (SbiViewForDoc link : links) {
                Integer documentId = link.getBiObjId();
                SbiViewHierarchy parent = link.getParent();
                if (documentId == null || parent == null || !ownedFolderIds.contains(parent.getId())) {
                    continue;
                }
                candidateIds.add(documentId);
                folderIdsByDocument.computeIfAbsent(documentId, id -> new LinkedHashSet<>())
                        .add(parent.getId());
            }

            for (SbiView view : views) {
                Integer documentId = view.getBiObjId();
                SbiViewHierarchy parent = view.getParent();
                if (documentId == null || parent == null || !ownedFolderIds.contains(parent.getId())) {
                    continue;
                }
                candidateIds.add(documentId);
                folderIdsByDocument.computeIfAbsent(documentId, id -> new LinkedHashSet<>())
                        .add(parent.getId());
                viewsByDocument.computeIfAbsent(documentId, id -> new ArrayList<>()).add(view);
            }

            // Include recently used documents, preserving the latest execution time per document.
            // Source and configured limit match RecentsResource.
            Map<Integer, Long> lastRunByDocument = loadRecentDocumentTimes(profile);
            candidateIds.addAll(lastRunByDocument.keySet());

            // Fetch candidate documents in batches, not once per linked view/folder.
            IBIObjectDAO documentDao = DAOFactory.getBIObjectDAO();
            documentDao.setUserProfile(profile);
            Map<Integer, BIObject> documentsById = new LinkedHashMap<>();
            List<Integer> ids = new ArrayList<>(candidateIds);
            for (int offset = 0; offset < ids.size(); offset += DOCUMENT_BATCH_SIZE) {
                int end = Math.min(offset + DOCUMENT_BATCH_SIZE, ids.size());
                Integer[] batchIds = ids.subList(offset, end).toArray(new Integer[0]);
                CriteriaParameter byIds = new CriteriaParameter("biobjId", batchIds, Match.IN);
                List<BIObject> loaded = documentDao.loadPaginatedSearchBIObjects(
                        null, null, Collections.emptyList(), Collections.singletonList(byIds));
                for (BIObject document : loaded) {
                    documentsById.put(document.getId(), document);
                }
            }

            JSONArray response = new JSONArray();
            for (Integer documentId : candidateIds) {
                BIObject document = documentsById.get(documentId);
                if (document == null || !isVisibleToUser(document, profile)) {
                    continue;
                }

                JSONObject item = new JSONObject();
                item.put("id", document.getId());
                item.put("label", document.getLabel());
                item.put("name", document.getName());
                item.put("description", document.getDescription() == null ? JSONObject.NULL : document.getDescription());
                item.put("type", document.getBiObjectTypeCode());
                Long lastRun = lastRunByDocument.get(documentId);
                // PROVISIONAL CONTRACT: milliseconds since Unix epoch; confirm with FE specification.
                item.put("lastRun", lastRun == null ? JSONObject.NULL : lastRun);

                JSONArray folderIds = new JSONArray();
                for (String folderId : folderIdsByDocument.getOrDefault(documentId, Collections.emptySet())) {
                    folderIds.put(folderId);
                }
                item.put("folderIds", folderIds);

                JSONArray savedViews = new JSONArray();
                for (SbiView view : viewsByDocument.getOrDefault(documentId, Collections.emptyList())) {
                    JSONObject v = new JSONObject();
                    v.put("id", view.getId());
                    v.put("label", view.getLabel());
                    v.put("name", view.getName());
                    v.put("description", view.getDescr() == null ? JSONObject.NULL : view.getDescr());
                    v.put("folderId", view.getParent().getId());
                    savedViews.put(v);
                }
                item.put("savedViews", savedViews);
                response.put(item);
            }

            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            LOGGER.info("Workspace documents: candidateIds=" + candidateIds.size()
                    + ", recentDocuments=" + lastRunByDocument.size()
                    + ", visibleDocuments=" + response.length() + ", elapsedMs=" + elapsedMs);
            return response.toString();
        } catch (Exception e) {
            LOGGER.error("Error retrieving workspace documents", e);
            throw new SpagoBIRuntimeException("Error retrieving workspace documents", e);
        }
    }

    private Map<Integer, Long> loadRecentDocumentTimes(UserProfile profile) {
        Map<Integer, Long> lastRunByDocument = new LinkedHashMap<>();
        try {
            SourceBean hotlinkConfig = (SourceBean) ConfigSingleton.getInstance()
                    .getAttribute(HotLinkConstants.HOTLINK);
            if (hotlinkConfig == null) {
                LOGGER.warn("HotLink configuration missing: recent documents not included");
                return lastRunByDocument;
            }

            SourceBean recentSection = (SourceBean) hotlinkConfig.getFilteredSourceBeanAttribute(
                    "SECTION", "name", HotLinkConstants.MY_RECENTLY_USED);
            if (recentSection == null) {
                LOGGER.warn("MY_RECENTLY_USED configuration missing: recent documents not included");
                return lastRunByDocument;
            }

            int limit = Integer.parseInt((String) recentSection.getAttribute(HotLinkConstants.ROWS_NUMBER));
            if (limit <= 0) {
                return lastRunByDocument;
            }

            List<?> recentEntries = AuditManager.getInstance().getMyRecentlyUsed(profile, limit);
            if (recentEntries != null) {
                for (Object entry : recentEntries) {
                    if (!(entry instanceof HotLink)) {
                        continue;
                    }
                    HotLink recent = (HotLink) entry;
                    Integer documentId = recent.getObjId();
                    if (documentId == null) {
                        continue;
                    }
                    long lastRun = recent.getRequestTime();
                    Long previous = lastRunByDocument.get(documentId);
                    if (previous == null || lastRun > previous) {
                        lastRunByDocument.put(documentId, lastRun);
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.warn("Recent document retrieval failed; proceeding without recents", ex);
        }
        return lastRunByDocument;
    }

    private boolean isVisibleToUser(BIObject document, UserProfile profile) throws EMFInternalError {
        String type = document.getBiObjectTypeCode();
        if ("CHART".equalsIgnoreCase(type) || "DATA_MINING".equalsIgnoreCase(type)) {
            return false;
        }
        if (!document.isVisible()
                && !profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN)
                && !profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_DEV)) {
            return false;
        }
        return ObjectsAccessVerifier.canSee(document, profile);
    }
}
