package it.eng.knowage.resourcemanager.resource;

import java.util.List;

import javax.validation.Valid;
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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import it.eng.knowage.resourcemanager.resource.utils.FileDTO;
import it.eng.knowage.resourcemanager.resource.utils.FolderDTO;
import it.eng.knowage.resourcemanager.resource.utils.RootFolderDTO;
import it.eng.knowage.resourcemanager.service.ResourceManagerAPI;
import it.eng.spagobi.services.security.SecurityServiceService;
import it.eng.spagobi.services.security.SpagoBIUserProfile;

@Path("/1.0/resourcemanager")
@Component
public class ResourceManagerResource {

	private static final Logger LOGGER = Logger.getLogger(ResourceManagerResource.class);

	@Autowired
	@Lazy
	SecurityServiceService securityServiceService;

	@Autowired
	ResourceManagerAPI resourceManagerAPIservice;

	// Folders management

	/**
	 * @return folders JSON tree from resource folder
	 */
	@GET
	@Path("/folders")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public RootFolderDTO folder() {
		RootFolderDTO folders = resourceManagerAPIservice.getFolders(null);
		return folders;
	}

	@POST
	@Path("/{path}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public FolderDTO createFolder(@Valid FolderDTO newFolder, @PathParam("path") String path) {

		return null;
	}

	@GET
	@Path("/download/folder/{path}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadFolder(@QueryParam("path") String path) {
		return null;
	}

	// Files Management

	@GET
	@Path("/files/{path}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<FileDTO> files(@PathParam("path") String path) {
		List<FileDTO> files = null;
		return files;
	}

	@GET
	@Path("/download/file/{path}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadFiles(@QueryParam("path") String path) {
		return null;
	}

	@POST
	@Path("/uploadfile")
	@Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON })
	public Response uploadFile() {
		return null;

	}

	@GET
	@Path("/metadata/{path}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public FileDTO metadata(@PathParam("path") String path) {
		FileDTO file = null;
		return file;
	}

	@POST
	@Path("/metadata/{path}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public FileDTO saveMetadata(@Valid FileDTO fileDTO, @PathParam("path") String path) {
		return null;
	}

	// Common methods

	@DELETE
	@Path("/{path}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response deleteFolder(@PathParam("path") String path) {
		Response response = null;

		return response;

	}

	private SpagoBIUserProfile getUserProfile() {
		SpagoBIUserProfile profile = (SpagoBIUserProfile) RequestContextHolder.currentRequestAttributes().getAttribute("userProfile",
				RequestAttributes.SCOPE_REQUEST);
		return profile;
	}

}
