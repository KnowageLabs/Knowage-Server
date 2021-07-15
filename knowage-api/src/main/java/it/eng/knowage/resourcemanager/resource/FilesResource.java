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
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import it.eng.knowage.knowageapi.context.BusinessRequestContext;
import it.eng.knowage.knowageapi.error.KNRM001Exception;
import it.eng.knowage.knowageapi.error.KnowageBusinessException;
import it.eng.knowage.knowageapi.error.KnowageRuntimeException;
import it.eng.knowage.resourcemanager.resource.dto.FileDTO;
import it.eng.knowage.resourcemanager.resource.dto.MetadataDTO;
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
	 * @throws KNRM001Exception
	 */
	@GET
	@Path("/{path}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FileDTO> getFiles(@QueryParam("path") String path) throws KNRM001Exception {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		List<FileDTO> files = resourceManagerAPIservice.getListOfFiles(path, profile);
		return files;
	}

	@POST
	@Path("/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response downloadFiles(List<String> listOfPaths) throws KNRM001Exception {
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
	@Path("/uploadFile")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadFile(MultipartFormDataInput multipartFormDataInput, @QueryParam("path") String path,
			@DefaultValue("false") @QueryParam("extract") boolean extract) throws KnowageBusinessException, KNRM001Exception {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		Map<String, List<InputPart>> formDataMap = multipartFormDataInput.getFormDataMap();

		if (!formDataMap.containsKey("file")) {
			throw new KnowageBusinessException("Cannot find the file part in input");
		}

		InputPart inputPart = formDataMap.get("file").get(0);
		MediaType mediaType = inputPart.getMediaType();

		if (!Arrays.asList("application/x-zip-compressed", "application/zip").contains(mediaType.toString())) {
			try (InputStream is = inputPart.getBody(InputStream.class, null)) {

				resourceManagerAPIservice.importFile(is, path, profile);
				return Response.status(Response.Status.OK).build();

			} catch (IOException e) {
				throw new KnowageRuntimeException(e.getMessage());
			}
		} else {
			try (InputStream is = inputPart.getBody(InputStream.class, null)) {

				if (extract) {
					resourceManagerAPIservice.importFileAndExtract(is, path, profile);

				} else {
					resourceManagerAPIservice.importFile(is, path, profile);
				}

				return Response.status(Response.Status.OK).build();

			} catch (IOException e) {
				throw new KnowageRuntimeException(e.getMessage());
			}
		}

	}

	@GET
	@Path("/metadata")
	@Produces(MediaType.APPLICATION_JSON)
	public MetadataDTO getMetadata(@QueryParam("path") String path) throws KNRM001Exception {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		MetadataDTO file = resourceManagerAPIservice.getMetadata(path, profile);
		return file;
	}

	@POST
	@Path("/metadata")
	@Produces(MediaType.APPLICATION_JSON)
	public MetadataDTO saveMetadata(MetadataDTO fileDTO, @QueryParam("path") String path) throws KNRM001Exception {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		MetadataDTO file = resourceManagerAPIservice.saveMetadata(fileDTO, path, profile);
		return file;
	}

	// Common methods

	@DELETE
	@Path("/{path}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response delete(@QueryParam("path") String path) {
		Response response = null;
		try {
			SpagoBIUserProfile profile = businessContext.getUserProfile();
			boolean ok = resourceManagerAPIservice.delete(path, profile);
			if (ok)
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
