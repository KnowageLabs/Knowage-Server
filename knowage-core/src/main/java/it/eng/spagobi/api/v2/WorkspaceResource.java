package it.eng.spagobi.api.v2;
 
import java.util.ArrayList;
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
 
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.view.dao.ISbiViewHierarchyDAO;
import it.eng.spagobi.view.dao.ISbiViewDAO;
import it.eng.spagobi.view.dao.ISbiViewForDocDAO;
import it.eng.spagobi.view.metadata.SbiViewHierarchy;
import it.eng.spagobi.view.metadata.SbiView;
import it.eng.spagobi.view.metadata.SbiViewForDoc;
 
@Path("/2.0/workspace")
public class WorkspaceResource extends AbstractSpagoBIResource {
 
    private static final Logger LOGGER =
            Logger.getLogger(WorkspaceResource.class);
 
    @GET
    @Path("/documents")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public String getDocuments() {
 
        long start = System.nanoTime();
 
        // Retrieve all folders owned by the user
        ISbiViewHierarchyDAO hierarchyDao =
                DAOFactory.getSbiViewHierarchyDAO();
 
        hierarchyDao.setUserProfile(UserProfileManager.getProfile());
 
        Set<SbiViewHierarchy> folders =
                hierarchyDao.readAllOwned();
 
        // Retrieve all saved views
        ISbiViewDAO viewDao = DAOFactory.getSbiViewDAO();
 
        viewDao.setUserProfile(UserProfileManager.getProfile());
 
        Set<SbiView> views = viewDao.readAllOwned();
 
        // Retrieve all imported document links
        ISbiViewForDocDAO docDao =
                DAOFactory.getSbiViewForDocDAO();
 
        docDao.setUserProfile(UserProfileManager.getProfile());
 
        Set<SbiViewForDoc> importedDocs =
                docDao.readAllOwned();
 
        // Index folders by folder ID
        Map<String, SbiViewHierarchy> foldersById =
                new LinkedHashMap<>();
 
        for (SbiViewHierarchy folder : folders) {
            foldersById.put(folder.getId(), folder);
        }
 
        // Group imported document links by document ID
        Map<Integer, Set<String>> folderIdsByDocument =
                new LinkedHashMap<>();
 
        for (SbiViewForDoc link : importedDocs) {
 
            Integer documentId = link.getBiObjId();
 
            if (documentId == null) {
                continue;
            }
 
            Set<String> folderIds =
                    folderIdsByDocument.computeIfAbsent(
                            documentId,
                            id -> new LinkedHashSet<>()
                    );
 
            SbiViewHierarchy parent = link.getParent();
 
            if (parent != null
&& foldersById.containsKey(parent.getId())) {
                folderIds.add(parent.getId());
            }
        }
 
        // Group saved views by document ID
        Map<Integer, List<SbiView>> viewsByDocument =
                new LinkedHashMap<>();
 
        for (SbiView view : views) {
 
            Integer documentId = view.getBiObjId();
 
            if (documentId == null) {
                continue;
            }
 
            viewsByDocument.computeIfAbsent(
                    documentId,
                    id -> new ArrayList<>()
            ).add(view);
        }
 
        // Collect unique document IDs
        Set<Integer> documentIds = new LinkedHashSet<>();
 
        documentIds.addAll(folderIdsByDocument.keySet());
        documentIds.addAll(viewsByDocument.keySet());
 
        long elapsedMs =
                (System.nanoTime() - start) / 1_000_000;
 
        LOGGER.info(
                "Workspace aggregation prepared: folders="
                + folders.size()
                + ", savedViews=" + views.size()
                + ", importedLinks=" + importedDocs.size()
                + ", uniqueDocuments=" + documentIds.size()
                + ", elapsedMs=" + elapsedMs
        );
 
        // TODO: Load personal and recent documents.
        // TODO: Bulk-load document details and verify visibility.
        // TODO: Build the final deduplicated JSON response.
        return new JSONArray().toString();
    }
}