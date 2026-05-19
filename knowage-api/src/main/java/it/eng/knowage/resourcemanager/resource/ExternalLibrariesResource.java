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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.eng.knowage.boot.context.BusinessRequestContext;
import it.eng.knowage.boot.error.KnowageBusinessException;
import it.eng.knowage.boot.error.KnowageRuntimeException;
import it.eng.knowage.knowageapi.utils.PathTraversalChecker;
import it.eng.knowage.resourcemanager.service.ResourceManagerAPI;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Path("/2.0/resources/external-libraries")
@Component
public class ExternalLibrariesResource {

	@Autowired
	ResourceManagerAPI resourceManagerAPIservice;

	@Autowired
	BusinessRequestContext businessContext;

	@GET
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadExternalLibrary(@QueryParam("libraryName") String libraryName) throws KnowageBusinessException {
		if (libraryName == null || libraryName.trim().isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Missing libraryName").build();
		}

		String requestedLibraryName = libraryName.trim();
		try {
			PathTraversalChecker.isValidFileName(requestedLibraryName);
		} catch (KnowageRuntimeException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid libraryName").build();
		}

		SpagoBIUserProfile profile = businessContext.getUserProfile();
		try {
			java.nio.file.Path externalLibraryPath = resourceManagerAPIservice.getExternalLibraryPath(requestedLibraryName, profile);
			File externalLibrary = externalLibraryPath.toFile();
			String mimeType = Files.probeContentType(externalLibraryPath);
			if (mimeType == null || mimeType.trim().isEmpty()) {
				mimeType = new MimetypesFileTypeMap().getContentType(externalLibrary.getName());
			}
			if (mimeType == null || mimeType.trim().isEmpty()) {
				mimeType = MediaType.APPLICATION_OCTET_STREAM;
			}

			return Response.ok(externalLibrary, mimeType)
					.header("Content-length", String.valueOf(Files.size(externalLibraryPath)))
					.header("Content-Disposition", String.format("attachment; filename=\"%s\"", externalLibrary.getName()))
					.build();
		} catch (KnowageBusinessException e) {
			throw new KnowageBusinessException(e, businessContext.getLocale());
		} catch (IOException e) {
			throw new KnowageRuntimeException("Error calculating file size for " + requestedLibraryName, e);
		} catch (Exception e) {
			throw new KnowageRuntimeException(e);
		}
	}
}
