package it.eng.spagobi.api.v2;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.workspace.bo.FunctionsOrganizer;
import it.eng.spagobi.workspace.dao.IFunctionsOrganizerDAO;

/**
 * @deprecated Replaced by KNOWAGE_TM-513
 * TODO : Delete
 */
@Path("/2.0/organizer/folders")
@ManageAuthorization
@Deprecated
public class FunctionsOrganizerResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = Logger.getLogger(FunctionsOrganizerResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FunctionsOrganizer> getFolders() throws EMFUserError {
		IFunctionsOrganizerDAO foldersOrganizerDAO = DAOFactory.getFunctionsOrganizerDAO();
		foldersOrganizerDAO.setUserProfile(getUserProfile());
		List<FunctionsOrganizer> allFolders = foldersOrganizerDAO.loadFolderByUser();
		return allFolders;
	}
}
