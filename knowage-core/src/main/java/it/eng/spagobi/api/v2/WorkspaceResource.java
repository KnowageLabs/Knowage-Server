package it.eng.spagobi.api.v2;
 
import java.util.Set;
 
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
 
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
 
    @GET
    @Path("/documents")
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public String getDocuments() {
 
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
 
        // Temporary response: aggregation will be implemented next
        return new JSONArray().toString();
    }
}
