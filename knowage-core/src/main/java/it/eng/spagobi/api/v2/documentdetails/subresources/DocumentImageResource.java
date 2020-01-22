package it.eng.spagobi.api.v2.documentdetails.subresources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.clerezza.jaxrs.utils.form.ParameterValue;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import com.hazelcast.util.Base64;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.analiticalmodel.document.AnalyticalModelDocumentManagementAPI;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.file.FileUtils;

@Path("")
public class DocumentImageResource extends AbstractSpagoBIResource {
	private static final List<String> VALID_FILE_EXTENSIONS = Arrays.asList("BMP", "JPG", "JPEG", "PNG", "GIF");

	@SuppressWarnings("unchecked")
	@GET
	@Produces({ "image/jpeg,image/png" })
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public Response getDocumentImage(@PathParam("id") Integer id) {
		logger.debug("IN");
		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(id);

		ResponseBuilder rb;

		if (document == null) {
			logger.error("Document with id [" + id + "] doesn't exist");
			rb = Response.status(Status.NOT_FOUND);
			return rb.build();
		}
		try {
			if (ObjectsAccessVerifier.canSee(document, getUserProfile())) {

				String previewFileName = document.getPreviewFile();

				if (previewFileName == null || previewFileName.equalsIgnoreCase("")) {
					logger.debug("No preview file associated to document " + document.getLabel());
					// rb = Response.ok();
					rb = Response.status(Status.NOT_FOUND);
					return rb.build();
				}

				File previewDirectory = GeneralUtilities.getPreviewFilesStorageDirectoryPath();

				String previewFilePath = previewDirectory.getAbsolutePath() + File.separator + previewFileName;

				File previewFile = new File(previewFilePath);

				if (!previewFile.exists()) {
					logger.error("Preview file " + previewFileName + " does not exist");
					rb = Response.status(Status.NOT_FOUND);
					return rb.build();
				}

				// to prevent attacks check file parent is really the expected
				// one
				String parentPath = previewFile.getParentFile().getAbsolutePath();
				String directoryPath = previewDirectory.getAbsolutePath();
				if (!parentPath.equals(directoryPath)) {
					logger.error("Path Traversal Attack security check failed: file parent path: " + parentPath + " is different" + " from directory path: "
							+ directoryPath);
					throw new SpagoBIRuntimeException("Path Traversal Attack security check failed");
				}

				byte[] previewBytes = Files.readAllBytes(previewFile.toPath());
				String encodedfile = new String(Base64.encode(previewBytes), "UTF-8");
				try {
					rb = Response.ok(encodedfile);
				} catch (Exception e) {
					logger.error("Error while getting preview file", e);
					throw new SpagoBIRuntimeException("Error while getting preview file", e);
				}

				rb.header("Content-Disposition", "attachment; filename=" + previewFileName);
				return rb.build();

			} else {
				logger.error("User [" + getUserProfile().getUserName() + "] has no rights to see document with  [" + id + "]");

				rb = Response.status(Status.UNAUTHORIZED);
				return rb.build();
			}
		} catch (SpagoBIRuntimeException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error while converting document in Json", e);
			throw new SpagoBIRuntimeException("Error while converting document in Json", e);
		}

	}

	@POST
	@Path("/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public void postDocumentImage(@PathParam("id") Integer id, MultiPartBody uploadedImage) {
		final String fileContentType = "multipart/form-data";
		String previewFileName = "";
		IBIObjectDAO documentDao = null;
		BIObject document = null;
		FileItemFactory factory = new DiskFileItemFactory();
		ParameterValue[] name = uploadedImage.getParameteValues("fileName");
		previewFileName = name[0].toString();
		FormFile[] files = uploadedImage.getFormFileParameterValues("file");
		FileItem uploaded = factory.createItem("file", fileContentType, false, previewFileName);
		try {
			uploaded.getOutputStream().write(files[0].getContent());
		} catch (IOException e1) {
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "preview file", e1.toString());

		}
		if (uploaded != null) {
			String fileName = GeneralUtilities.getRelativeFileNames(uploaded.getName());
			if (fileName != null && !fileName.trim().equals("")) {
				try {
					previewFileName = uploadFile(uploaded, previewFileName);
					documentDao = DAOFactory.getBIObjectDAO();
					document = documentDao.loadBIObjectById(id);
					document.setPreviewFile(previewFileName);
					documentDao.modifyBIObject(document);
				} catch (EMFUserError e) {
					EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "preview file", e.getErrorCode());
				} catch (Exception e) {
					EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "preview file", e.getMessage());
				}
			}
		}
	}

	@DELETE
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public void deleteDocumentImage(@PathParam("id") Integer id) {
		logger.debug("IN");

		AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());
		BIObject document = documentManager.getDocument(id);
		String previewFileName = "";
		IBIObjectDAO documentDao = null;
		try {
			documentDao = DAOFactory.getBIObjectDAO();
			if (ObjectsAccessVerifier.canSee(document, getUserProfile())) {
				previewFileName = document.getPreviewFile();
			}
			File previewDirectory = GeneralUtilities.getPreviewFilesStorageDirectoryPath();
			String previewFilePath = previewDirectory.getAbsolutePath() + File.separator + previewFileName;
			File previewFile = new File(previewFilePath);
			previewFile.delete();
			document.setPreviewFile(null);
			documentDao.modifyBIObject(document);
		} catch (EMFInternalError e) {
			throw new SpagoBIRuntimeException("User is not allowed to preview document", e);
		} catch (EMFUserError e) {
			logger.error("Preview file cannot be deleted", e);
			throw new SpagoBIRestServiceException("Preview file cannot be deleted", buildLocaleFromSession(), e);
		}
	}

	private String uploadFile(FileItem uploaded, String ime) throws Exception {

		if (uploaded.getSize() == 0) {
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "uploadFile", "201");
			return null;
		}

		logger.info("User [id : " + getUserProfile().getUserId() + ", name : " + getUserProfile().getUserName() + "] " + "is uploading file [" + ime + "]");

		int maxSize = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.DOCUMENTS.MAX_PREVIEW_IMAGE_SIZE"));
		if (uploaded.getSize() > maxSize) {
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "uploadFile", "202");

			return null;
		}

		String fileExtension = FileUtils.getFileExtension(ime);
		if (!VALID_FILE_EXTENSIONS.contains(fileExtension.toLowerCase()) && !VALID_FILE_EXTENSIONS.contains(fileExtension.toUpperCase())) {
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "uploadFile", "203");
			return null;
		}

		File targetDirectory = GeneralUtilities.getPreviewFilesStorageDirectoryPath();

		// check if number of existing images is the max allowed
		int maxFilesAllowed = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.DOCUMENTS.MAX_PREVIEW_IMAGES_NUM"));

		File[] existingImages = FileUtils.getContainedFiles(targetDirectory);
		int existingImagesNumber = existingImages.length;
		if (existingImagesNumber >= maxFilesAllowed) {
			EMFValidationError error = new EMFValidationError(EMFErrorSeverity.ERROR, "uploadFile", "204");
			return null;
		}

		logger.debug("Saving file...");
		File saved = FileUtils.saveFileIntoDirectory(uploaded, targetDirectory);
		logger.debug("File saved");

		return saved.getName();

	}

}
