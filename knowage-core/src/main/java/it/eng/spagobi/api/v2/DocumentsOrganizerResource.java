/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.api.v2;

import java.sql.SQLException;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.workspace.bo.DocumentOrganizer;
import it.eng.spagobi.workspace.dao.IObjFuncOrganizerDAO;

/**
 * @deprecated Replaced by KNOWAGE_TM-513
 * TODO : Delete
 */
@Path("/2.0/organizer/documents")
@ManageAuthorization
@Deprecated
public class DocumentsOrganizerResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(DocumentsOrganizerResource.class);

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DocumentOrganizer> loadDocumentsForFolder(@PathParam("id") Integer folderId) {
		LOGGER.debug("IN");
		try {
			IObjFuncOrganizerDAO objFuncOrganizer = DAOFactory.getObjFuncOrganizerDAO();
			objFuncOrganizer.setUserProfile(getUserProfile());
			List<DocumentOrganizer> documents = objFuncOrganizer.loadDocumentsByFolder(folderId);
			return documents;
		} catch (Exception exception) {
			LOGGER.error("Error while loading documents from organizer.", exception);
			throw new SpagoBIRestServiceException("sbi.workspace.organizer.error.load", getLocale(), exception);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	/**
	 * The GET service method that collects all documents that the current user has inside it's Organizer. It does not look for a particular folder, but rather
	 * for all documents that exist for the user.
	 *
	 * @return The list of all documents available in the user's Organizer.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<DocumentOrganizer> loadDocumentsForFolder() {

		LOGGER.debug("IN");

		try {
			IObjFuncOrganizerDAO objFuncOrganizer = DAOFactory.getObjFuncOrganizerDAO();
			objFuncOrganizer.setUserProfile(getUserProfile());
			List<DocumentOrganizer> documents = objFuncOrganizer.loadAllOrganizerDocuments();
			return documents;

		} catch (HibernateException he) {

			LOGGER.error("Error while loading all documents from the Organizer.", he);
			throw new SpagoBIRestServiceException("sbi.workspace.organizer.error.load", getLocale(), he);

		} catch (Exception exception) {

			LOGGER.error("Error while loading all documents from the Organizer.", exception);
			throw new SpagoBIRestServiceException("sbi.workspace.organizer.error.load", getLocale(), exception);

		} finally {
			LOGGER.debug("OUT");
		}

	}

	@POST
	@Path("/{id}")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.SAVE_INTO_FOLDER_FUNCTIONALITY })
	public Response addDocumentToOrganizer(@PathParam("id") Integer documentId) throws EMFUserError {

		LOGGER.debug("IN");

		try {
			IObjFuncOrganizerDAO objFuncOrganizer = DAOFactory.getObjFuncOrganizerDAO();
			objFuncOrganizer.setUserProfile(getUserProfile());
			objFuncOrganizer.addDocumentToOrganizer(documentId);
			return Response.ok().build();
		}
		/*
		 * The catch block is commented, since the lower is now used.
		 *
		 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		// catch (Exception exception) {
		// logger.error("Error while saving document in organizer.", exception);
		// throw new SpagoBIRestServiceException("sbi.workspace.organizer.error.post", getLocale(), exception);
		//
		// }
		catch (HibernateException he) {

			/**
			 * If the value of the state is 23000, the user is trying to make a duplicate entry. In our case, user is trying to add the document to the
			 * Organizer, i.e. it's 'root' folder in which he already has this document. Inform user about this exception (problem) and throw it to the client
			 * side.
			 *
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			String state = ((SQLException) he.getCause().getCause()).getSQLState();

			if (state.equals("23000")) {
				LOGGER.error("Document duplication while adding document to the Organizer", he);
				throw new SpagoBIRuntimeException("sbi.workspace.organizer.document.addtoorganizer.error.duplicateentry", he);
			} else {
				LOGGER.error("Error while adding document to the Organizer", he);
				throw new SpagoBIRuntimeException("sbi.workspace.organizer.document.addtoorganizer.error.general", he);
			}

		} finally {
			LOGGER.debug("OUT");
		}
	}

	@DELETE
	@Path("/{folderId}/{docId}")
	public Response deleteDocumentFromOrganizer(@PathParam("folderId") Integer folderId, @PathParam("docId") Integer docId) {
		LOGGER.debug("IN");
		try {
			IObjFuncOrganizerDAO objFuncOrganizer = DAOFactory.getObjFuncOrganizerDAO();
			objFuncOrganizer.removeDocumentFromOrganizer(folderId, docId);
			return Response.ok().build();
		} catch (Exception exception) {
			LOGGER.error("Error while deleting a document in organizer.", exception);
			throw new SpagoBIRestServiceException("sbi.workspace.organizer.error.delete", getLocale(), exception);
		} finally {
			LOGGER.debug("OUT");
		}
	}


}
