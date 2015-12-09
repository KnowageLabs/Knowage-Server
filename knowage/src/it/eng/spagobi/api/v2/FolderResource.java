package it.eng.spagobi.api.v2;

import java.util.ArrayList;
import java.util.List;

import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 * 
 */
@Path("/2.0/folders")
@ManageAuthorization
public class FolderResource extends AbstractSpagoBIResource {
	static protected Logger logger = Logger.getLogger(FolderResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getFolders(@DefaultValue("false") @QueryParam("includeDocs") Boolean recoverBIObjects,
			@QueryParam("perm") String permissionOnFolder){
		logger.debug("IN");
		
		try {
			UserProfile profile = getUserProfile();
			ILowFunctionalityDAO dao = DAOFactory.getLowFunctionalityDAO();
			dao.setUserProfile(profile);

			List<LowFunctionality> allFolders = dao.loadAllLowFunctionalities(recoverBIObjects);
			List<LowFunctionality> folders = new ArrayList<LowFunctionality>();
			
			if (permissionOnFolder != null && !permissionOnFolder.isEmpty()) {
				for (LowFunctionality lf : allFolders) {
					if (ObjectsAccessVerifier.canSee(lf, profile)
							&& checkPermissionOnFolder(permissionOnFolder, lf, profile)) {
						folders.add(lf);
					}
				}
			} else {
				for (LowFunctionality lf : allFolders) {
					if (ObjectsAccessVerifier.canSee(lf, profile)) {
						folders.add(lf);
					}
				}
			}
			String jsonObjects = JsonConverter.objectToJson(folders, folders.getClass());
			
			return Response.ok(jsonObjects).build();
		} catch (Exception e) {
			String errorString = "Error while getting the list of folders";
			logger.error(errorString, e);
			throw new SpagoBIRuntimeException(errorString, e);
		} finally {
			logger.debug("OUT");
		}
	}

	private boolean checkPermissionOnFolder(String permission, LowFunctionality lf, UserProfile profile) {
		boolean result = false;
		
		switch (permission.toUpperCase()) {
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_DEVELOP:
			result = ObjectsAccessVerifier.canDev(lf, profile);
			break;
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_TEST:
			result = ObjectsAccessVerifier.canTest(lf, profile);
			break; 
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_EXECUTE:
			result = ObjectsAccessVerifier.canExec(lf, profile);
			break;
		case SpagoBIConstants.PERMISSION_ON_FOLDER_TO_CREATE:
			result = ObjectsAccessVerifier.canCreate(lf, profile);
			break;
		}
		
		return result;
	}
}
