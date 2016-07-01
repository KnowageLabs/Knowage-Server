package it.eng.spagobi.api.v2;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.workspace.dao.IFunctionsOrganizerDAO;
import it.eng.spagobi.workspace.metadata.SbiFunctionsOrganizer;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

@Path("/2.0/organizer/folders")
@ManageAuthorization
public class FunctionsOrganizerResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DataSetResource.class);
	IFunctionsOrganizerDAO foldersOrganizerDAO;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List getFolders() throws EMFUserError {
		foldersOrganizerDAO = DAOFactory.getFunctionsOrganizerDAO();
		foldersOrganizerDAO.setUserProfile(getUserProfile());
		List allFolders = foldersOrganizerDAO.loadFolderByUser();
		return allFolders;
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public SbiFunctionsOrganizer createFolder(SbiFunctionsOrganizer folder) throws EMFUserError {

		try {

			foldersOrganizerDAO = DAOFactory.getFunctionsOrganizerDAO();
			foldersOrganizerDAO.setUserProfile(getUserProfile());
			SbiFunctionsOrganizer sfo = foldersOrganizerDAO.createFolder(folder);
			return sfo;

		} catch (HibernateException he) {

			/**
			 * If the value of the state is 23000, the user is trying to make a duplicate entry. In our case, user is trying to create a new folder with the
			 * same code value. Inform user about this exception (problem) and throw it to the client side.
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			String state = ((SQLException) he.getCause().getCause()).getSQLState();

			if (state.equals("23000")) {
				logger.error("Duplicate key entry while creating a new folder", he);
				throw new SpagoBIRuntimeException("sbi.workspace.organizer.folder.error.duplicateentry", he);
			} else {
				logger.error("Error saving federation", he);
				throw new SpagoBIRuntimeException("sbi.workspace.organizer.folder.error.create", he);
			}

		}

	}

	@DELETE
	@Path("/{id}")
	public Response deleteFolder(@PathParam("id") Integer folderId) throws EMFUserError {
		try {
			foldersOrganizerDAO = DAOFactory.getFunctionsOrganizerDAO();
			foldersOrganizerDAO.deleteFolder(folderId);
			return Response.ok().build();
		} catch (SpagoBIRuntimeException ex) {
			throw new SpagoBIRestServiceException("sbi.workspace.organizer.folder.error.delete", buildLocaleFromSession(), ex);
		}
	}
}
