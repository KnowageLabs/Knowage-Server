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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("2.0/mondrianSchemasResource")
@ManageAuthorization
public class MondrianResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(MondrianResource.class);

	@Context
	private UriInfo uri;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MONDRIAN_SCHEMA_MANAGEMENT })
	public List<Artifact> getAll() {

		List<Artifact> mondrians;

		try {
			IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			mondrians = artifactDAO.loadAllArtifacts("MONDRIAN_SCHEMA");
			return mondrians;

		} catch (Exception e) {
			LOGGER.error("Non-fatal error getting mondrians", e);
		}

		return new ArrayList<>();
	}

	@GET
	@Path("/{ID}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MONDRIAN_SCHEMA_MANAGEMENT })
	public Artifact getById(@PathParam("ID") int id) {

		Artifact artifact = null;

		try {
			IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			artifact = artifactDAO.loadArtifactById(id);

		} catch (Exception e) {
			LOGGER.error("Non-fatal error while getting artifact with id: " + id, e);
		} finally {
			LOGGER.debug("OUT");
		}
		return artifact;
	}

	@GET
	@Path("/name={name}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MONDRIAN_SCHEMA_MANAGEMENT })
	public Artifact getByName(@PathParam("name") String name) {
		Artifact artifact = null;
		try {
			IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			artifact = artifactDAO.loadArtifactByNameAndType(name, "MONDRIAN_SCHEMA");
		} catch (Exception e) {
			LOGGER.error("Non-fatal error while getting artifact with name: " + name, e);
			throw new SpagoBIServiceException(this.request.getPathInfo(),
					"Error while getting artifact with name: " + name, e);
		} finally {
			LOGGER.debug("OUT");
		}
		return artifact;
	}

	@GET
	@Path("/{ID}/versions")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MONDRIAN_SCHEMA_MANAGEMENT })
	public List<Content> getAllContent(@PathParam("ID") int id) {

		List<Content> versions = null;

		try {
			IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());

			versions = artifactDAO.loadArtifactVersions(id);

		} catch (Exception e) {
			LOGGER.error("Non-fatal error while getting artifact versions with id: " + id, e);
		} finally {
			LOGGER.debug("OUT");
		}
		return versions;
	}

	@GET
	@Path("/{ID}/versions/{contentID}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MONDRIAN_SCHEMA_MANAGEMENT })
	public Content getContent(@PathParam("ID") int id, @PathParam("contentID") int contentId) {

		try {
			IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			List<Content> versions = artifactDAO.loadArtifactVersions(id);
			for (Content content : versions) {
				if (content.getId() == contentId) {
					return content;
				}
			}
		} catch (Exception e) {
			LOGGER.error("Non-fatal error while getting artifact versions with id: " + id, e);
		}
		return null;
	}

	@GET
	@Path("/{ID}/versions/{contentID}/file")
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MONDRIAN_SCHEMA_MANAGEMENT })
	public Response getContentFile(@PathParam("ID") Integer id, @PathParam("contentID") Integer contentId) {

		IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();
		Content content = artifactDAO.loadArtifactContentById(contentId);
		byte[] file = content.getContent();
		String s = new String(file);
		// System.out.println(s);
		try {

			ResponseBuilder response = Response.ok(file);
			response.header("Content-Disposition", "attachment; filename=" + content.getFileName());
			response.header("filename", content.getFileName());
			// System.out.println(uri.getAbsolutePath());
			return response.build();

		} catch (Exception e) {
			LOGGER.error("Non-fatal error while getting artifact versions with id: " + id, e);
		}
		return Response.status(Status.BAD_REQUEST).entity("Error ").build();
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MONDRIAN_SCHEMA_MANAGEMENT })
	public Response add(@Valid Artifact artifact) {

		if (artifact == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}
		if (artifact.getId() != null) {
			return Response.status(Status.BAD_REQUEST).entity("Error new artifact should not have ID value").build();
		}

		try {
			IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());

			artifactDAO.insertArtifact(artifact);

			return Response.ok(artifact).build();

		} catch (Exception e) {
			Response.notModified().build();
			LOGGER.error("Error while adding new artifact", e);
			throw new SpagoBIRuntimeException("Error while adding new artifact", e);
		}

	}

	@POST
	@Path("/{ID}/versions")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MONDRIAN_SCHEMA_MANAGEMENT })
	public Response uploadFile(MultiPartBody input, @PathParam("ID") int artifactId) {

		Content content = new Content();
		byte[] bytes = null;

		IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();

		final FormFile file = input.getFormFileParameterValues("file")[0];

		if (file != null) {

			bytes = file.getContent();
			content.setFileName(file.getFileName());
			content.setContent(bytes);
			content.setCreationDate(new Date());
			content.setCreationUser(getUserProfile().getUserName().toString());

			artifactDAO.insertArtifactContent(artifactId, content);

		} else {
			return Response.status(Status.BAD_REQUEST).build();
		}

		return Response.status(200).build();

	}

	@PUT
	@Path("/{ID}")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.MONDRIAN_SCHEMA_MANAGEMENT })
	public Response update(@PathParam("ID") int artifactId, @Valid Artifact artifact) {

		LOGGER.debug("IN");

		if (artifact == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (artifact.getId() == null) {
			return Response.status(Status.NOT_FOUND).entity("The artifact with id " + artifactId + " doesn't exist")
					.build();
		}

		try {

			IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			if (!artifactDAO.loadArtifactById(artifactId).getModelLocked()) {

				artifactDAO.modifyArtifact(artifact);
				if (artifact.getCurrentContentId() != null) {
					artifactDAO.setActiveVersion(artifactId, artifact.getCurrentContentId());
				}

			} else if (artifactDAO.loadArtifactById(artifactId).getModelLocked() && !artifact.getModelLocked()
					&& artifact.getModelLocker() != null) {

				Artifact temp = artifactDAO.loadArtifactById(artifactId);
				temp.setModelLocked(false);
				temp.setModelLocker(null);
				artifactDAO.modifyArtifact(temp);

			}

			Artifact savedArtifact = artifactDAO.loadArtifactById(artifactId);
			return Response.ok(savedArtifact).build();

		} catch (Exception e) {
			LOGGER.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);

		}

	}

	@DELETE
	@Path("/{ID}")
	public Response delete(@PathParam("ID") int artifactId) {

		try {
			IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());

			if (!artifactDAO.loadArtifactById(artifactId).getModelLocked()) {

				artifactDAO.eraseArtifact(artifactId);
			}

			return Response.ok().build();
		} catch (Exception e) {
			LOGGER.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);
		}

	}

	@DELETE
	@Path("/{ID}/versions/{contentID}")
	public Response deleteContent(@PathParam("ID") int artifactId, @PathParam("contentID") int contentId) {

		try {
			IArtifactsDAO artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			if (!artifactDAO.loadArtifactById(artifactId).getModelLocked()) {

				List<Content> versions = artifactDAO.loadArtifactVersions(artifactId);
				for (Content content : versions) {
					if (content.getId() == contentId) {
						artifactDAO.eraseArtifactContent(contentId);
						return Response.ok().build();
					}

				}

			} else {
				return Response.notModified("not deleted").build();
			}

		} catch (Exception e) {
			LOGGER.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);

		}
		return Response.notModified().build();
	}

}
