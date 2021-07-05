/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.resourcemanager.resource;

import java.io.IOException;
import java.nio.file.Files;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.context.BusinessRequestContext;
import it.eng.knowage.knowageapi.error.KnowageRuntimeException;
import it.eng.knowage.resourcemanager.resource.utils.RootFolderDTO;
import it.eng.knowage.resourcemanager.service.ResourceManagerAPI;
import it.eng.spagobi.services.security.SecurityServiceService;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Path("/2.0/resources/folders")
@Component
public class FoldersResource {

	private static final Logger LOGGER = Logger.getLogger(FoldersResource.class);

	@Autowired
	@Lazy
	SecurityServiceService securityServiceService;

	@Autowired
	ResourceManagerAPI resourceManagerAPIservice;

	@Autowired
	BusinessRequestContext businessContext;

	// Folders management

	/**
	 * @return folders JSON tree from resource folder
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public RootFolderDTO getFolders() {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		RootFolderDTO folders = resourceManagerAPIservice.getFolders(profile, null);
		return folders;
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createFolder(@QueryParam("path") String path) {
		Response response = null;
		try {
			SpagoBIUserProfile profile = businessContext.getUserProfile();
			boolean create = resourceManagerAPIservice.createFolder(path, profile);
			if (create)
				response = Response.status(Response.Status.OK).build();
			else {
				response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage());
		}
		return response;
	}

	@POST
	@Path("/download/{path}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("application/zip")
	public Response downloadFolder(@PathParam("path") String path) {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		java.nio.file.Path exportArchive = resourceManagerAPIservice.getDownloadPath(path, profile);
		String filename = exportArchive.getFileName() + ".zip";
		try {
			return Response.ok(exportArchive.toFile()).header("Content-length", "" + Files.size(exportArchive))
					.header("Content-Disposition", String.format("attachment; filename=\"%s\"", filename)).build();
		} catch (IOException e) {
			throw new KnowageRuntimeException("Error calculating file size for " + exportArchive, e);
		}
	}

	// Common methods

	@DELETE
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@QueryParam("path") String path) {
		Response response = null;
		try {
			SpagoBIUserProfile profile = businessContext.getUserProfile();
			boolean create = resourceManagerAPIservice.delete(path, profile);
			if (create)
				response = Response.status(Response.Status.OK).build();
			else {
				response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage());
		}
		return response;

	}

}
