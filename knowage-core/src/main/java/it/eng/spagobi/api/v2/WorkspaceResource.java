package it.eng.spagobi.api.v2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.CriteriaParameter;
import it.eng.spagobi.commons.bo.CriteriaParameter.Match;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.UserUtilities;
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

/**
 * My Workspace Documents (backend-spec v2, 2026-11-05, section 5).
 *
 * Includes: personal folder documents, recently run documents, user workspace
 * document links and saved views. One DOCUMENT item per BIObject id; separate
 * VIEW items. The folder tree continues to come from GET 1.0/repository.
 *
 * Draft for integration and validation. Does not modify existing DAO contracts.
 */
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

            // Preserve existing duplicate links so callers can still remove them.
            Map<Integer, List<SbiViewForDoc>> linksByDocument = new LinkedHashMap<>();
            Map<Integer, List<SbiView>> viewsByDocument = new LinkedHashMap<>();
            Set<Integer> candidateIds = new LinkedHashSet<>();
            Set<Integer> documentIds = new LinkedHashSet<>();

            for (SbiViewForDoc link : links) {
                Integer documentId = link.getBiObjId();
                SbiViewHierarchy parent = link.getParent();
                if (documentId == null || parent == null || !ownedFolderIds.contains(parent.getId())) {
                    continue;
                }
                candidateIds.add(documentId);
                documentIds.add(documentId);
                linksByDocument.computeIfAbsent(documentId, id -> new ArrayList<>()).add(link);
            }

            for (SbiView view : views) {
                Integer documentId = view.getBiObjId();
                SbiViewHierarchy parent = view.getParent();
                if (documentId == null || parent == null || !ownedFolderIds.contains(parent.getId())) {
                    continue;
                }
                candidateIds.add(documentId);
                viewsByDocument.computeIfAbsent(documentId, id -> new ArrayList<>()).add(view);
            }

            // Recents, using the existing audit and configured hotlink limit.
            // Q2 remains open: timestamps currently cover the configured recents,
            // not necessarily every previously executed document.
            Map<Integer, Long> lastRunByDocument = loadRecentDocumentTimes(profile);
            candidateIds.addAll(lastRunByDocument.keySet());
            documentIds.addAll(lastRunByDocument.keySet());

            IBIObjectDAO documentDao = DAOFactory.getBIObjectDAO();
            documentDao.setUserProfile(profile);
            Map<Integer, BIObject> documentsById = new LinkedHashMap<>();
            Set<Integer> personalDocumentIds = new LinkedHashSet<>();

            // Personal folder: use the established DocumentCRUD implementation
            // without its four-engine filter (backend-spec v2, Q1).
            // false ensures that reading the workspace does not create folders.
            LowFunctionality personalFolder = UserUtilities.loadUserFunctionalityRoot(profile, false);
            if (personalFolder != null) {
                List<BIObject> personalDocuments = documentDao.loadBIObjects(personalFolder.getId(), profile, true);
                for (BIObject document : personalDocuments) {
                    Integer documentId = document.getId();
                    if (documentId != null) {
                        personalDocumentIds.add(documentId);
                        candidateIds.add(documentId);
                        documentIds.add(documentId);
                        documentsById.put(documentId, document);
                    }
                }
            }

            // Batch-load remaining candidates once per chunk, never per folder.
            List<Integer> idsToLoad = new ArrayList<>(candidateIds);
            idsToLoad.removeAll(documentsById.keySet());
            for (int offset = 0; offset < idsToLoad.size(); offset += DOCUMENT_BATCH_SIZE) {
                int end = Math.min(offset + DOCUMENT_BATCH_SIZE, idsToLoad.size());
                Integer[] batchIds = idsToLoad.subList(offset, end).toArray(new Integer[0]);
                CriteriaParameter byIds = new CriteriaParameter("biobjId", batchIds, Match.IN);
                List<BIObject> loaded = documentDao.loadPaginatedSearchBIObjects(
                        null, null, Collections.emptyList(), Collections.singletonList(byIds));
                for (BIObject document : loaded) {
                    documentsById.put(document.getId(), document);
                }
            }

            JSONArray response = new JSONArray();
            Set<Integer> visibleDocumentIds = new LinkedHashSet<>();

            // One DOCUMENT per BIObject id, regardless of how many sources refer to it.
            for (Integer documentId : candidateIds) {
                BIObject document = documentsById.get(documentId);
                if (document == null || !isVisibleToUser(document, profile)) {
                    continue;
                }
                visibleDocumentIds.add(documentId);
                if (!documentIds.contains(documentId)) {
                    continue;
                }

                JSONObject item = new JSONObject();
                item.put("kind", "DOCUMENT");
                item.put("id", documentId);
                item.put("biObjectId", documentId);
                item.put("label", asJsonValue(document.getLabel()));
                item.put("name", asJsonValue(document.getName()));
                item.put("description", asJsonValue(document.getDescription()));
                item.put("typeCode", asJsonValue(document.getBiObjectTypeCode()));
                item.put("creationUser", asJsonValue(document.getCreationUser()));
                item.put("creationDate", asJsonValue(toUtcIso(document.getCreationDate())));
                item.put("stateCode", asJsonValue(document.getStateCode()));
                item.put("previewFile", asJsonValue(document.getPreviewFile()));
                Long lastRun = lastRunByDocument.get(documentId);
                item.put("lastRun", asJsonValue(lastRun == null ? null : Instant.ofEpochMilli(lastRun).toString()));
                item.put("personal", personalDocumentIds.contains(documentId));

                JSONArray documentLinks = new JSONArray();
                for (SbiViewForDoc link : linksByDocument.getOrDefault(documentId, Collections.emptyList())) {
                    JSONObject linkJson = new JSONObject();
                    linkJson.put("linkId", link.getId());
                    linkJson.put("folderId", link.getParent().getId());
                    documentLinks.put(linkJson);
                }
                item.put("links", documentLinks);
                response.put(item);
            }

            // VIEW is an independent row in the same JSON array.
            // Exclude views whose document is missing or inaccessible.
            for (Map.Entry<Integer, List<SbiView>> entry : viewsByDocument.entrySet()) {
                Integer documentId = entry.getKey();
                if (!visibleDocumentIds.contains(documentId)) {
                    continue;
                }
                BIObject document = documentsById.get(documentId);
                for (SbiView view : entry.getValue()) {
                    JSONObject item = new JSONObject();
                    item.put("kind", "VIEW");
                    item.put("id", view.getId());
                    item.put("biObjectId", documentId);
                    item.put("documentLabel", asJsonValue(document.getLabel()));
                    item.put("documentName", asJsonValue(document.getName()));
                    item.put("biObjectTypeCode", asJsonValue(document.getBiObjectTypeCode()));
                    item.put("label", asJsonValue(view.getLabel()));
                    item.put("name", asJsonValue(view.getName()));
                    item.put("description", asJsonValue(view.getDescr()));
                    SbiCommonInfo commonInfo = view.getCommonInfo();
                    item.put("creationUser", asJsonValue(commonInfo == null ? null : commonInfo.getUserIn()));
                    item.put("creationDate", asJsonValue(commonInfo == null ? null : toUtcIso(commonInfo.getTimeIn())));
                    JSONArray viewLinks = new JSONArray();
                    JSONObject viewLink = new JSONObject();
                    viewLink.put("folderId", view.getParent().getId());
                    viewLinks.put(viewLink);
                    item.put("links", viewLinks);
                    response.put(item);
                }
            }

            LOGGER.info("Workspace documents: candidateIds=" + candidateIds.size()
                    + ", personalDocuments=" + personalDocumentIds.size()
                    + ", recentDocuments=" + lastRunByDocument.size()
                    + ", visibleDocuments=" + visibleDocumentIds.size()
                    + ", totalItems=" + response.length()
                    + ", elapsedMs=" + (System.nanoTime() - start) / 1_000_000);
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

    private static Object asJsonValue(Object value) {
        return value == null ? JSONObject.NULL : value;
    }

    private static String toUtcIso(Date date) {
        return date == null ? null : Instant.ofEpochMilli(date.getTime()).toString();
    }

    private boolean isVisibleToUser(BIObject document, UserProfile profile) throws EMFInternalError {
        // BE1 includes all document engines, subject to visibility and rights.
        if (!document.isVisible()
                && !profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_ADMIN)
                && !profile.isAbleToExecuteAction(CommunityFunctionalityConstants.DOCUMENT_MANAGEMENT_DEV)) {
            return false;
        }
        return ObjectsAccessVerifier.canSee(document, profile);
    }
}
