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
package it.eng.spagobi.api.v3;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.node.ObjectNode;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.ObjTemplate;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 *
 * @author albnale
 *
 */

@Path("/3.0/documentexecution")
public class DocumentExecutionResource extends AbstractSpagoBIResource {

	@GET
	@Path("/{id}/templates")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public ObjectNode getDocumentTemplates(@PathParam("id") Integer id) throws JSONException, EMFInternalError {
		logger.debug("IN");

		UserProfile userProfile = getUserProfile();
		if (userProfile == null) {
			String message = "Error while loading user profile";
			logger.error(message);
			throw new SpagoBIRuntimeException(message);
		}
		byte[] temp = null;
		JSONObject jsonTemplate = null;

		try {
			IBIObjectDAO documentDao = DAOFactory.getBIObjectDAO();
			BIObject document = documentDao.loadBIObjectById(id);
			if (!ObjectsAccessVerifier.canExec(document, userProfile)) {
				String message = "User cannot exec the document";
				logger.error(message);
				throw new SpagoBIRuntimeException(message);
			}

			ObjTemplate documentTemplate = document.getActiveTemplate();
			temp = documentTemplate.getContent();
			Assert.assertNotNull(document, "Document can not be null");
			Assert.assertNotNull(documentTemplate, "Document Template can not be null");
			jsonTemplate = new JSONObject(new String(temp));
		} catch (EMFUserError e) {
			logger.debug("Could not get content from template", e);
			throw new SpagoBIRestServiceException("Could not get content from template", buildLocaleFromSession(), e);
		}
		logger.debug("OUT");
		return jsonTemplate.getWrappedObject();
	}

}
