package it.eng.spagobi.api.v2;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.catalogue.bo.Artifact;
import it.eng.spagobi.tools.catalogue.bo.Content;
import it.eng.spagobi.tools.catalogue.dao.IArtifactsDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

@Path("2.0/mondrianSchemasResource")
@ManageAuthorization
public class MondrianResource extends AbstractSpagoBIResource {

	private IArtifactsDAO artifactDAO = null;

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
	public Content getAllContent(@PathParam("ID") int id, @PathParam("contentID") int contentId) {

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
			String encodedArtifact = URLEncoder.encode("" + artifact.getId(), "UTF-8");
			return Response.created(new URI("2.0/mondrianSchemas/" + encodedArtifact)).build();

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

		artifactDAO = DAOFactory.getArtifactsDAO();

		Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
		for (String key : uploadForm.keySet()) {

			List<InputPart> inputParts = uploadForm.get(key);

			for (InputPart inputPart : inputParts) {

				try {

					MultivaluedMap<String, String> header = inputPart.getHeaders();
					content.setFileName(getFileName(header));

					// convert the uploaded file to input stream
					InputStream inputStream = inputPart.getBody(InputStream.class, null);

					byte[] bytes = IOUtils.toByteArray(inputStream);

					content.setContent(bytes);

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

			content.setCreationDate(new Date());
			content.setCreationUser(getUserProfile().getUserName().toString());
			content.setDimension(String.valueOf(content.getContent().length));
			artifactDAO.insertArtifactContent(artifactId, content);

		}

		return Response.status(200).entity("uploadFile is called, Uploaded file name : ").build();

	}

	private String getFileName(MultivaluedMap<String, String> header) {
		String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

		for (String filename : contentDisposition) {
			if ((filename.trim().startsWith("filename"))) {

				String[] name = filename.split("=");

				String finalFileName = name[1].trim().replaceAll("\"", "");
				return finalFileName;
			}
		}
		return "unknown";
	}

	@PUT
	@Path("/{ID}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(@PathParam("ID") int id, @Valid Artifact artifact) {

		logger.debug("IN");

		if (artifact == null) {
			return Response.status(Status.BAD_REQUEST).entity("Error JSON parsing").build();
		}

		if (artifact.getId() == null) {
			return Response.status(Status.NOT_FOUND).entity("The artifact with id " + id + " doesn't exist").build();
		}

		try {
			artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			artifactDAO.modifyArtifact(artifact);
			String encodedArtifact = URLEncoder.encode("" + artifact.getId(), "UTF-8");
			return Response.created(new URI("2.0/mondrianSchemas/" + encodedArtifact)).entity(encodedArtifact).build();

		} catch (Exception e) {
			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while updating url of the new resource", e);

		}

	}

	@DELETE
	@Path("/{ID}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public Response delete(@PathParam("ID") int id) {

		try {
			artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			artifactDAO.eraseArtifact(id);

			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);
		}

	}

	@DELETE
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteSelectedArtifacts(Integer[] selectedIds) {

		try {
			for (Integer selectedId : selectedIds) {
				delete(selectedId);
			}

			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}

	}

	@DELETE
	@Path("/{ID}/versions/{contentID}")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	public Response deleteContent(@PathParam("ID") int id, @PathParam("contentID") int contentId) {

		try {
			artifactDAO = DAOFactory.getArtifactsDAO();
			artifactDAO.setUserProfile(getUserProfile());
			List<Content> versions = artifactDAO.loadArtifactVersions(id);
			for (Content content : versions) {
				if (content.getId() == contentId) {
					artifactDAO.eraseArtifactContent(contentId);
					return Response.ok(artifactDAO.loadArtifactVersions(id)).build();
				}

			}
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);

		}
		return Response.notModified().build();
	}

	@DELETE
	@Path("/{ID}/versions")
	@UserConstraint(functionalities = { SpagoBIConstants.DOMAIN_MANAGEMENT })
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteSelectedVersions(@PathParam("ID") int id, Integer[] selectedIds) {

		try {
			for (Integer selectedId : selectedIds) {
				deleteContent(id, selectedId);
			}

			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRuntimeException("Error while deleting url of the new resource", e);
		}

	}

}
