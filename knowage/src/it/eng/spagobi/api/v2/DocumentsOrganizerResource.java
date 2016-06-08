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

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.workspace.dao.IObjFuncOrganizerDAO;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/2.0/organizer/documents")
@ManageAuthorization
public class DocumentsOrganizerResource extends AbstractSpagoBIResource {

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public List loadDocumentsForFolder(@PathParam("id") Integer folderId) {
		IObjFuncOrganizerDAO objFuncOrganizer;
		try {
			objFuncOrganizer = DAOFactory.getObjFuncOrganizerDAO();
			objFuncOrganizer.setUserProfile(getUserProfile());
			List documents = objFuncOrganizer.loadDocumentsByFolder(folderId);
			return documents;
		} catch (EMFUserError e) {

			throw new SpagoBIRestServiceException("sbi.browser.folder.load.error", buildLocaleFromSession(), e);
		}
	}

}
