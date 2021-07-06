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
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import it.eng.knowage.resourcemanager.resource.utils.FileDTO;
import it.eng.knowage.resourcemanager.resource.utils.MetadataDTO;
import it.eng.knowage.resourcemanager.service.ResourceManagerAPI;
import it.eng.spagobi.services.security.SecurityServiceService;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Path("/2.0/resources/files")
@Component
public class FilesResource {

	private static final Logger LOGGER = Logger.getLogger(FilesResource.class);

	@Autowired
	@Lazy
	SecurityServiceService securityServiceService;

	@Autowired
	ResourceManagerAPI resourceManagerAPIservice;

	@Autowired
	BusinessRequestContext businessContext;

	// Files Management

	/**
	 * @param path
	 * @return list of files, one of them could be "metadata.json", it will be excluded
	 */
	@GET
	@Path("/{path}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FileDTO> getFiles(@PathParam("path") String path) {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		List<FileDTO> files = resourceManagerAPIservice.getListOfFiles(path, profile);
		return files;
	}

	@GET
	@Path("/download/file/")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadFiles(@QueryParam("list") List<String> listOfPaths) {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		if (listOfPaths.size() == 1) {
			java.nio.file.Path file = resourceManagerAPIservice.getDownloadFilePath(listOfPaths, profile, false);
			try {
				return Response.ok(file.toFile()).header("Content-length", "" + Files.size(file))
						.header("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getFileName())).build();
			} catch (IOException e) {
				throw new KnowageRuntimeException("Error calculating file size for " + file, e);
			}
		} else {
			java.nio.file.Path zipFile = resourceManagerAPIservice.getDownloadFilePath(listOfPaths, profile, true);
			String filename = zipFile.getFileName() + ".zip";
			try {
				return Response.ok(zipFile.toFile()).header("Content-length", "" + Files.size(zipFile))
						.header("Content-Disposition", String.format("attachment; filename=\"%s\"", filename)).build();
			} catch (IOException e) {
				throw new KnowageRuntimeException("Error calculating file size for " + zipFile, e);
			}
		}
	}

	@POST
	@Path("/uploadfile")
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON })
	public Response uploadFile() {
		return null;

	}

	@GET
	@Path("/metadata/{path}")
	@Produces(MediaType.APPLICATION_JSON)
	public MetadataDTO getMetadata(@PathParam("path") String path) {
		MetadataDTO file = null; // TODO: METADATA DTO
		return file;
	}

	@PUT
	@Path("/metadata/{path}")
	@Produces(MediaType.APPLICATION_JSON)
	public MetadataDTO saveMetadata(MetadataDTO fileDTO, @PathParam("path") String path) {
		return null;
	}

	@POST
	@Path("/metadata/{path}")
	@Produces(MediaType.APPLICATION_JSON)
	public MetadataDTO addMetadata(MetadataDTO fileDTO, @PathParam("path") String path) {
		return null;
	}

	// Common methods

	@DELETE
	@Path("/{path}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@PathParam("path") String path) {
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
