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

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.workspace.dao.IObjFuncOrganizerDAO;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/2.0/organizer/documents")
@ManageAuthorization
public class DocumentsOrganizerResource extends AbstractSpagoBIResource {

	IObjFuncOrganizerDAO objFuncOrganizer;

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List loadDocumentsForFolder(@PathParam("id") Integer folderId) {
		logger.debug("IN");
		try {
			objFuncOrganizer = DAOFactory.getObjFuncOrganizerDAO();
			objFuncOrganizer.setUserProfile(getUserProfile());
			List documents = objFuncOrganizer.loadDocumentsByFolder(folderId);
			return documents;
		} catch (Exception exception) {
			logger.error("Error while loading documents from organizer.", exception);
			throw new SpagoBIRestServiceException("sbi.workspace.organizer.error.load", buildLocaleFromSession(), exception);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/{id}")
	public Response addDocumentToOrganizer(@PathParam("id") Integer documentId) {
		logger.debug("IN");
		try {
			objFuncOrganizer = DAOFactory.getObjFuncOrganizerDAO();
			objFuncOrganizer.setUserProfile(getUserProfile());
			objFuncOrganizer.addDocumentToOrganizer(documentId);
			return Response.ok().build();
		} catch (Exception exception) {
			logger.error("Error while saving document in organizer.", exception);
			throw new SpagoBIRestServiceException("sbi.workspace.organizer.error.post", buildLocaleFromSession(), exception);
		} finally {
			logger.debug("OUT");
		}
	}

	@DELETE
	@Path("/{folderId}/{docId}")
	public Response deleteDocumentFromOrganizer(@PathParam("folderId") Integer folderId, @PathParam("docId") Integer docId) {
		logger.debug("IN");
		try {
			objFuncOrganizer = DAOFactory.getObjFuncOrganizerDAO();
			objFuncOrganizer.removeDocumentFromOrganizer(folderId, docId);
			return Response.ok().build();
		} catch (Exception exception) {
			logger.error("Error while deleting a document in organizer.", exception);
			throw new SpagoBIRestServiceException("sbi.workspace.organizer.error.delete", buildLocaleFromSession(), exception);
		} finally {
			logger.debug("OUT");
		}
	}

}
