package it.eng.spagobi.api.v2;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("2.0/mondrianSchemasResource")
@ManageAuthorization
public class MondrianResource extends AbstractSpagoBIResource {

	private IArtifactsDAO artifactDAO = null;
	@Context
	private UriInfo uri;

	@GET
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public List<Artifact> getAll() {

		List<Artifact> mondrians;

		try {
			artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			mondrians = artifactDAO.loadAllArtifacts("MONDRIAN_SCHEMA");
			return mondrians;

		} catch (Exception e) {

			e.printStackTrace();
		}

		return new ArrayList<Artifact>();
	}

	@GET
	@Path("/{ID}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public Artifact getById(@PathParam("ID") int id) {

		Artifact artifact = null;

		try {
			artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			artifact = artifactDAO.loadArtifactById(id);

		} catch (Exception e) {
			logger.error("Error while getting artifact with id: " + id, e);

		} finally {
			logger.debug("OUT");
		}
		return artifact;
	}

	@GET
	@Path("/{ID}/versions")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public List<Content> getAllContent(@PathParam("ID") int id) {

		List<Content> versions = null;

		try {
			artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());

			versions = artifactDAO.loadArtifactVersions(id);

		} catch (Exception e) {
			logger.error("Error while getting artifact versions with id: " + id, e);

		} finally {
			logger.debug("OUT");
		}
		return versions;
	}

	@GET
	@Path("/{ID}/versions/{contentID}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Produces(MediaType.APPLICATION_JSON)
	public Content getContent(@PathParam("ID") int id, @PathParam("contentID") int contentId) {

		try {
			artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			List<Content> versions = artifactDAO.loadArtifactVersions(id);
			for (Content content : versions) {
				if (content.getId() == contentId) {
					return content;
				}
			}

		} catch (Exception e) {
			logger.error("Error while getting artifact versions with id: " + id, e);

		}
		return null;
	}

	@GET
	@Path("/{ID}/versions/{contentID}/file")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	public Response getContentFile(@PathParam("ID") Integer id, @PathParam("contentID") Integer contentId) {

		artifactDAO = DAOFactory.getArtifactsDAO();
		Content content = artifactDAO.loadArtifactContentById(contentId);
		byte[] file = content.getContent();
		String s = new String(file);
		System.out.println(s);
		try {

			ResponseBuilder response = Response.ok(file);
			response.header("Content-Disposition", "attachment; filename=" + content.getFileName());
			response.header("filename", content.getFileName());
			System.out.println(uri.getAbsolutePath());
			return response.build();

		} catch (Exception e) {
			logger.error("Error while getting artifact versions with id: " + id, e);

		}
		return Response.status(Status.BAD_REQUEST).entity("Error ").build();
	}

	private String createLink() {

		return "nesto";
	}

	@POST
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response add(@Valid Artifact artifact) {

		if (artifact == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}
		if (artifact.getId() != null) {
			return Response.status(Status.BAD_REQUEST).entity("Error new artifact should not have ID value").build();
		}

		try {
			artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());

			artifactDAO.insertArtifact(artifact);

			return Response.ok(artifact).build();

		} catch (Exception e) {

			Response.notModified().build();
			logger.error("Error while adding new artifact", e);
			throw new SpagoBIRuntimeException("Error while adding new artifact", e);
		}

	}

	@POST
	@Path("/{ID}/versions")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON })
	public Response uploadFile(@MultipartForm MultipartFormDataInput input, @PathParam("ID") int artifactId) {

		Content content = new Content();
		byte[] bytes = null;

		artifactDAO = DAOFactory.getArtifactsDAO();

		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

		List<InputPart> fileNamePart = uploadForm.get("fileName");
		List<InputPart> fileParts = uploadForm.get("file");

		if (fileNamePart != null && fileParts != null) {
			try {

				content.setFileName(fileNamePart.get(0).getBodyAsString());

				// convert the uploaded file to input stream
				InputStream inputStream = fileParts.get(0).getBody(InputStream.class, null);

				bytes = IOUtils.toByteArray(inputStream);

				content.setContent(bytes);
				content.setCreationDate(new Date());
				content.setCreationUser(getUserProfile().getUserName().toString());

				artifactDAO.insertArtifactContent(artifactId, content);
				String encodedContentId = URLEncoder.encode("" + content.getId(), "UTF-8");
				System.out.println(new URI(uri.getAbsolutePath() + encodedContentId));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			return Response.status(Status.BAD_REQUEST).build();

		}

		return Response.status(200).build();

	}

	@PUT
	@Path("/{ID}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("ID") int artifactId, @Valid Artifact artifact) {

		logger.debug("IN");

		if (artifact == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (artifact.getId() == null) {
			return Response.status(Status.NOT_FOUND).entity("The artifact with id " + artifactId + " doesn't exist").build();
		}

		try {

			artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			if (!artifactDAO.loadArtifactById(artifactId).getModelLocked()) {

				artifactDAO.modifyArtifact(artifact);
				if (artifact.getCurrentContentId() != null) {
					artifactDAO.setActiveVersion(artifactId, artifact.getCurrentContentId());
				}

			} else if (artifactDAO.loadArtifactById(artifactId).getModelLocked() && !artifact.getModelLocked() && artifact.getModelLocker() != null) {

				Artifact temp = artifactDAO.loadArtifactById(artifactId);
				temp.setModelLocked(false);
				temp.setModelLocker(null);
				artifactDAO.modifyArtifact(temp);

			}

			Artifact savedArtifact = artifactDAO.loadArtifactById(artifactId);
			return Response.ok(savedArtifact).build();

		} catch (Exception e) {
			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);

		}

	}

	@DELETE
	@Path("/{ID}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public Response delete(@PathParam("ID") int artifactId) {

		try {
			artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());

			if (!artifactDAO.loadArtifactById(artifactId).getModelLocked()) {

				artifactDAO.eraseArtifact(artifactId);
			}

			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);
		}

	}

	/*
	 * Multiple delete artifacts
	 * 
	 * @DELETE
	 * 
	 * @Path("/")
	 * 
	 * @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON) public Response
	 * deleteSelectedArtifacts(Integer[] selectedIds) {
	 * 
	 * try { for (Integer selectedId : selectedIds) { delete(selectedId); }
	 * 
	 * return Response.ok().build(); } catch (Exception e) {
	 * logger.error("Error while deleting url of the new resource", e); throw
	 * new SpagoBIRestServiceException(getLocale(), e); }
	 * 
	 * }
	 */

	@DELETE
	@Path("/{ID}/versions/{contentID}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public Response deleteContent(@PathParam("ID") int artifactId, @PathParam("contentID") int contentId) {

		try {
			artifactDAO = DAOFactory.getArtifactsDAO();
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
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);

		}
		return Response.notModified().build();
	}

	/*
	 * Multiple delete versions
	 * 
	 * @DELETE
	 * 
	 * @Path("/{ID}/versions")
	 * 
	 * @UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON) public Response
	 * deleteSelectedVersions(@PathParam("ID") int id, Integer[] selectedIds) {
	 * 
	 * try { for (Integer selectedId : selectedIds) { deleteContent(id,
	 * selectedId); }
	 * 
	 * return Response.ok().build(); } catch (Exception e) {
	 * logger.error("Error while deleting url of the new resource", e); throw
	 * new
	 * SpagoBIRuntimeException("Error while deleting url of the new resource",
	 * e); }
	 * 
	 * }
	 */

}
