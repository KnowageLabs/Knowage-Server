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
import java.nio.file.Paths;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.context.BusinessRequestContext;
import it.eng.knowage.knowageapi.error.KNRM001Exception;
import it.eng.knowage.knowageapi.error.KNRM002Exception;
import it.eng.knowage.knowageapi.error.KNRM004Exception;
import it.eng.knowage.knowageapi.error.KNRM005Exception;
import it.eng.knowage.knowageapi.error.KNRM008Exception;
import it.eng.knowage.knowageapi.error.KnowageRuntimeException;
import it.eng.knowage.resourcemanager.resource.dto.CreateFolderDTO;
import it.eng.knowage.resourcemanager.resource.dto.DownloadFolderDTO;
import it.eng.knowage.resourcemanager.resource.dto.RootFolderDTO;
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
	 * @throws KNRM001Exception
	 * @throws KNRM002Exception
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public RootFolderDTO getFolders() throws KNRM001Exception, KNRM002Exception {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		RootFolderDTO folders = null;
		try {
			folders = resourceManagerAPIservice.getFolders(profile);
		} catch (KNRM001Exception k) {
			throw new KNRM001Exception(""); // TODO: We have to understand how to handle technical messages inside business errors, how can we show them?
		} catch (KNRM002Exception e) {
			throw new KNRM002Exception("");
		}
		return folders;
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createFolder(CreateFolderDTO dto) {
		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		try {
			String path = resourceManagerAPIservice.getFolderByKey(dto.getKey(), profile);
			if (path != null) {
				java.nio.file.Path completePath = Paths.get(path).resolve(dto.getFolderName());
				boolean create = resourceManagerAPIservice.createFolder(completePath.toString(), profile);
				if (create) {
					response = Response.status(Response.Status.OK).build();
				}
			}
		} catch (KNRM004Exception e) {
			return Response.notModified(e.getMessage()).build();
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage());
		}
		return response;
	}

	@POST
	@Path("/download")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response downloadFolder(DownloadFolderDTO dto) throws KNRM001Exception, KNRM008Exception, KNRM005Exception, KNRM002Exception {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		String key = dto.getKey();
		String path = resourceManagerAPIservice.getFolderByKey(key, profile);
		java.nio.file.Path exportArchive = resourceManagerAPIservice.getDownloadFolderPath(key, path, profile);
		String filename = exportArchive.getFileName() + ".zip";
		try {
			return Response.ok(exportArchive.toFile()).header("Content-length", "" + Files.size(exportArchive))
					.header("Content-Disposition", String.format("attachment; filename=\"%s\"", filename)).build();
		} catch (IOException e) {
			throw new KnowageRuntimeException("Error calculating file size for " + exportArchive, e);
		}
	}

	@POST
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateName(CreateFolderDTO dto) {
		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		try {
			String path = resourceManagerAPIservice.getFolderByKey(dto.getKey(), profile);
			if (path != null) {
				java.nio.file.Path completePath = Paths.get(path);
				boolean create = resourceManagerAPIservice.updateFolder(completePath, dto.getFolderName(), profile);
				if (create)
					response = Response.status(Response.Status.OK).build();
			}
		} catch (KNRM004Exception ex) {
			response = Response.status(Response.Status.NOT_MODIFIED).entity(ex).build();
		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage());
		}
		return response;
	}

	// Common methods

	@DELETE
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(DownloadFolderDTO dto) {
		Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		try {
			SpagoBIUserProfile profile = businessContext.getUserProfile();
			String key = dto.getKey();
			String path = resourceManagerAPIservice.getFolderByKey(key, profile);
			boolean create = resourceManagerAPIservice.delete(path, profile);
			if (create)
				response = Response.status(Response.Status.OK).build();

		} catch (Exception e) {
			throw new KnowageRuntimeException(e.getMessage());
		}
		return response;

	}

}
