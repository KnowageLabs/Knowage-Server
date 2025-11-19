package it.eng.spagobi.analiticalmodel.document.service;

import java.io.File;
import java.io.FileInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.knowage.commons.security.PathTraversalChecker;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

@Path("/preview-file")
public class ManagePreviewFileResource extends AbstractSpagoBIResource {

	private static final Logger logger = Logger.getLogger(ManagePreviewFileResource.class);

	@GET
	@Path("/download")
	public Response downloadFile(@Context HttpServletRequest req) {

		String fileName = req.getParameter("fileName");
		logger.debug("IN - Download file: " + fileName);
		try {
			PathTraversalChecker.isValidFileName(fileName);
			File targetDirectory = GeneralUtilities.getPreviewFilesStorageDirectoryPath();
			File file = new File(targetDirectory, fileName);

			if (!file.exists()) {
				return Response.status(Response.Status.NOT_FOUND).entity("File not found").type(MediaType.TEXT_PLAIN).build();
			}

			byte[] content;
			try (FileInputStream fis = new FileInputStream(file)) {
				content = SpagoBIUtilities.getByteArrayFromInputStream(fis);
			}

			return Response.ok(content).header("Content-Disposition", "attachment; filename=" + file.getName()).build();

		} catch (Exception e) {
			logger.error("Error during download", e);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error: " + e.getMessage()).type(MediaType.TEXT_PLAIN).build();
		} finally {
			logger.debug("OUT");
		}
	}

}
