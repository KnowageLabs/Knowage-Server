package it.eng.spagobi.commons.services;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.utilities.fonts.BirtFonts;

@Path("/birt/fonts")
@Produces(MediaType.APPLICATION_JSON)
public class BirtFontsResource extends AbstractSpagoBIResource {

	private static final Logger logger = Logger.getLogger(BirtFontsResource.class);

	@GET
	public Response list() {
		if (!isSuperadmin()) return Response.status(Response.Status.FORBIDDEN).build();
		try {
			return Response.ok(fonts().list()).header("Cache-Control", "no-store").build();
		} catch (Exception e) {
			return failure(e);
		}
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response upload(@Context HttpServletRequest request) {
		if (!isSuperadmin()) return Response.status(Response.Status.FORBIDDEN).build();
		List<FileItem> items = null;
		try {
			ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
			upload.setFileSizeMax(BirtFonts.MAX_BYTES);
			upload.setSizeMax(BirtFonts.MAX_BYTES + 65536L);
			items = upload.parseRequest(request);
			FileItem font = null;
			for (FileItem item : items) {
				if (!item.isFormField()) {
					if (font != null || !"file".equals(item.getFieldName())) {
						throw new IllegalArgumentException("Upload exactly one font in the file field");
					}
					font = item;
				}
			}
			if (font == null) throw new IllegalArgumentException("No font was uploaded");
			fonts().save(font.get(), font.getName());
			return Response.status(Response.Status.CREATED).entity(Collections.singletonMap("restartRequired", true)).build();
		} catch (FileUploadBase.SizeLimitExceededException | FileUploadBase.FileSizeLimitExceededException e) {
			return error(413, "Maximum font size is 10 MB");
		} catch (FileAlreadyExistsException e) {
			return error(409, "A font with this filename already exists");
		} catch (IllegalArgumentException e) {
			return error(400, e.getMessage());
		} catch (Exception e) {
			return failure(e);
		} finally {
			if (items != null) {
				for (FileItem item : items) item.delete();
			}
		}
	}

	@DELETE
	@Path("/{name}")
	public Response delete(@PathParam("name") String name) {
		if (!isSuperadmin()) return Response.status(Response.Status.FORBIDDEN).build();
		try {
			if (!fonts().delete(name)) return Response.status(Response.Status.NOT_FOUND).build();
			return Response.ok(Collections.singletonMap("restartRequired", true)).build();
		} catch (IllegalArgumentException e) {
			return error(400, e.getMessage());
		} catch (Exception e) {
			return failure(e);
		}
	}

	private boolean isSuperadmin() {
		UserProfile profile = getUserProfile();
		return profile != null && Boolean.TRUE.equals(profile.getIsSuperadmin());
	}

	private BirtFonts fonts() {
		return new BirtFonts(Paths.get(SpagoBIUtilities.getRootResourcePath()));
	}

	private Response failure(Exception e) {
		logger.error("Unable to manage global BIRT fonts", e);
		return error(500, "Unable to manage BIRT fonts");
	}

	private Response error(int status, String message) {
		return Response.status(status).entity(Collections.singletonMap("error", message)).build();
	}
}
