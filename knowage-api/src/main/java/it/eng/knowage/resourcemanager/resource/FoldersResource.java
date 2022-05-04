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

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.error.KnowageBusinessException;
import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.knowage.knowageapi.error.ImpossibleToCreateFolderException;
import it.eng.knowage.knowageapi.error.ImpossibleToReadFolderListException;
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
	 * @throws TenantRepositoryMissingException
	 * @throws ImpossibleToReadFolderListException
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public RootFolderDTO getFolders() throws KnowageBusinessException {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		RootFolderDTO folders = null;
		try {
			folders = resourceManagerAPIservice.getFolders(profile);
		} catch (KnowageBusinessException e) {
			throw new KnowageBusinessException(e, businessContext.getLocale());
		} catch (Exception e) {
			throw new KnowageRuntimeException(e);
		}
		return folders;
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createFolder(CreateFolderDTO dto) throws KnowageBusinessException {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		try {
			String path = resourceManagerAPIservice.getFolderByKey(dto.getKey(), profile);
			if (path != null) {
				java.nio.file.Path completePath = Paths.get(path).resolve(dto.getFolderName());
				boolean create = resourceManagerAPIservice.createFolder(completePath.toString(), profile);
				if (!create) {
					throw new ImpossibleToCreateFolderException("");
				}
				return Response.status(Response.Status.OK).build();
			} else {
				throw new ImpossibleToReadFolderListException("");
			}
		} catch (KnowageBusinessException e) {
			throw new KnowageBusinessException(e, businessContext.getLocale());
		} catch (Exception e) {
			throw new KnowageRuntimeException(e);
		}
	}

	@POST
	@Path("/download")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response downloadFolder(DownloadFolderDTO dto) throws KnowageBusinessException {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		java.nio.file.Path exportArchive = null;
		String key = dto.getKey();
		try {
			String path = resourceManagerAPIservice.getFolderByKey(key, profile);
			exportArchive = resourceManagerAPIservice.getDownloadFolderPath(key, path, profile);
			String filename = exportArchive.getFileName() + ".zip";

			return Response.ok(exportArchive.toFile()).header("Content-length", "" + Files.size(exportArchive))
					.header("Content-Disposition", String.format("attachment; filename=\"%s\"", filename)).build();

		} catch (KnowageBusinessException e) {
			throw new KnowageBusinessException(e, businessContext.getLocale());
		} catch (IOException e) {
			throw new KnowageRuntimeException("Error calculating file size for " + exportArchive, e);
		} catch (Exception e) {
			throw new KnowageRuntimeException(e);
		}
	}

	@POST
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateName(CreateFolderDTO dto) throws KnowageBusinessException {
		SpagoBIUserProfile profile = businessContext.getUserProfile();
		try {
			String path = resourceManagerAPIservice.getFolderByKey(dto.getKey(), profile);
			if (path != null) {
				java.nio.file.Path completePath = Paths.get(path);
				boolean update = resourceManagerAPIservice.updateFolder(completePath, dto.getFolderName(), profile);
				if (!update) {
					throw new ImpossibleToCreateFolderException("");
				}
				return Response.status(Response.Status.OK).build();
			} else {
				throw new ImpossibleToReadFolderListException("");
			}
		} catch (KnowageBusinessException e) {
			throw new KnowageBusinessException(e, businessContext.getLocale());
		} catch (Exception e) {
			throw new KnowageRuntimeException(e);
		}
	}

	// Common methods

	@DELETE
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(DownloadFolderDTO dto) throws KnowageBusinessException {
		try {
			SpagoBIUserProfile profile = businessContext.getUserProfile();
			String key = dto.getKey();
			String path = resourceManagerAPIservice.getFolderByKey(key, profile);
			boolean delete = resourceManagerAPIservice.delete(path, profile);
			if (!delete) {
				throw new ImpossibleToReadFolderListException("");
			}

			return Response.status(Response.Status.OK).build();

		} catch (KnowageBusinessException e) {
			throw new KnowageBusinessException(e, businessContext.getLocale());
		} catch (Exception e) {
			throw new KnowageRuntimeException(e);
		}

	}

}
