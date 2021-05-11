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
package it.eng.spagobi.tools.objmetadata.service;

import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/2.0/objMetadata")
@ManageAuthorization
public class ObjMetadataResource extends AbstractSpagoBIResource {

	public static transient Logger logger = Logger.getLogger(ObjMetadataResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SEE_METADATA_FUNCTIONALITY })
	public List<ObjMetadata> getAllMetadata() {
		List<ObjMetadata> metadata;
		try {
			IObjMetadataDAO dao = DAOFactory.getObjMetadataDAO();
			dao.setUserProfile(getUserProfile());
			metadata = dao.loadAllObjMetadata();
		} catch (Exception e) {
			String message = "Error returning list of object metadata";
			logger.error(message, e);
			throw new SpagoBIServiceException(message, e);
		}
		return metadata;
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SEE_METADATA_FUNCTIONALITY, SpagoBIConstants.SAVE_METADATA_FUNCTIONALITY })
	public Integer insertMetadata(@Valid ObjMetadataDTO metadataDTO) {
		Integer toReturn;
		try {
			IObjMetadataDAO dao = DAOFactory.getObjMetadataDAO();
			dao.setUserProfile(getUserProfile());
			ObjMetadata metaObj = toObjMetadata(dao, metadataDTO);
			toReturn = dao.insertOrUpdateObjMetadata(metaObj);
		} catch (Exception e) {
			String message = "Error inserting metadata object with label: " + metadataDTO.getLabel();
			logger.error(message, e);
			throw new SpagoBIServiceException(message, e);
		}
		return toReturn;
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SEE_METADATA_FUNCTIONALITY, SpagoBIConstants.SAVE_METADATA_FUNCTIONALITY })
	public Response deleteMetadata(@PathParam("id") Integer id) {
		Response response = null;
		try {
			IObjMetadataDAO dao = DAOFactory.getObjMetadataDAO();
			dao.setUserProfile(getUserProfile());
			dao.eraseObjMetadataById(id);
			response = Response.status(Response.Status.OK).build();
		} catch (Exception e) {
			String message = "Error deleting metadata object with id: " + id;
			logger.error(message, e);
			throw new SpagoBIServiceException(message, e);
		}
		return response;
	}

	private ObjMetadata toObjMetadata(IObjMetadataDAO dao, ObjMetadataDTO metadataDTO) {
		ObjMetadata meta = new ObjMetadata();
		meta.setObjMetaId(metadataDTO.getId());
		meta.setLabel(metadataDTO.getLabel());
		meta.setName(metadataDTO.getName());
		meta.setDescription(metadataDTO.getDescription());
		meta.setDataTypeCode(metadataDTO.getDataType());
		return meta;
	}

}
