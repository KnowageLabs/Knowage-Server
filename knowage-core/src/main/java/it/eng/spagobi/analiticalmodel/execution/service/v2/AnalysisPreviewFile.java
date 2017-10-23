/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.analiticalmodel.execution.service.v2;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.file.FileUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

@Path("/2.0/analysis")
@ManageAuthorization
public class AnalysisPreviewFile extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(AnalysisPreviewFile.class);

	private static final String DATASET_FILE_MAX_SIZE = "DATASET_FILE_MAX_SIZE";
	private static final List<String> VALID_FILE_EXTENSIONS = Arrays.asList("BMP", "JPG", "JPEG", "PNG", "GIF");

	String fileExtension = "";

	@Path("/{id}")
	@POST
	public Map<String, String> uploadPreviewFileAnalysis(@Context HttpServletRequest req, @PathParam("id") int documentId) {

		try {
			FileItem uploaded = handleMultipartForm(req);

			if (uploaded == null) {
				throw new SpagoBIServiceException(getActionName(), "No file was uploaded");
			}

			checkPreviewFileMaxSize(uploaded);

			String extension = checExtension(uploaded);

			File file = checkAndCreateDir(uploaded);

			logger.debug("Saving file...");
			saveFile(uploaded, file);
			logger.debug("File saved");

			// start uploading document with preview file name
			BIObject biObject = DAOFactory.getBIObjectDAO().loadBIObjectById((documentId));
			biObject.setPreviewFile(file.getName());
			DAOFactory.getBIObjectDAO().modifyBIObject(biObject);
			// end uploading document with preview file name

			Map<String, String> jsonMap = new HashMap<>();
			jsonMap.put("fileName", file.getName());
			jsonMap.put("fileType", extension);

			return jsonMap;
		} catch (Throwable t) {
			String s = t.getMessage();
			logger.error("Error while uploading preview file for document in workspace", t);
			throw new SpagoBIRestServiceException(s, buildLocaleFromSession(), t);
		} finally {
			logger.debug("OUT");
		}

	}

	private FileItem handleMultipartForm(HttpServletRequest request) throws Exception {

		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		List fileItems = upload.parseRequest(request);
		Iterator iter = fileItems.iterator();

		while (iter.hasNext()) {
			FileItem item = (FileItem) iter.next();
			if (item.isFormField()) {
				String name = item.getFieldName();
				String value = item.getString();
			} else {
				return item;
			}
		}
		return null;
	}

	private String checExtension(FileItem uploaded) {

		logger.debug("IN");
		try {

			// check if the uploaded file is empty
			if (uploaded.getSize() == 0) {
				throw new SpagoBIServiceException(getActionName(), "The uploaded file is empty");
			}

			fileExtension = uploaded.getName().lastIndexOf('.') > 0 ? uploaded.getName().substring(uploaded.getName().lastIndexOf('.') + 1) : null;
			logger.debug("File extension: [" + fileExtension + "]");

			// check if the extension is valid
			String fileExtension = FileUtils.getFileExtension(uploaded.getName());
			if (!VALID_FILE_EXTENSIONS.contains(fileExtension.toLowerCase()) && !VALID_FILE_EXTENSIONS.contains(fileExtension.toUpperCase())) {
				throw new SpagoBIServiceException(getActionName(), "The selected file has an invalid extension. Please, choose a CSV or an XLS file");
			}

			return fileExtension.toUpperCase();

		} finally {
			logger.debug("OUT");
		}
	}

	private File checkAndCreateDir(FileItem uploaded) {
		logger.debug("IN");
		try {
			String fileName = SpagoBIUtilities.getRelativeFileNames(uploaded.getName());
			String resourcePath = SpagoBIUtilities.getResourcePath();
			File previewFileDir = new File(resourcePath + File.separatorChar + "preview" + File.separatorChar + "images");
			if (!previewFileDir.exists()) {
				// Create Directory \preview\images under \resources if
				// don't exists
				boolean mkdirResult = previewFileDir.mkdirs();
				if (!mkdirResult) {
					throw new SpagoBIServiceException(getActionName(), "Cannot create \\preview\\images directory into server resources");
				}
			}

			return new File(previewFileDir, fileName);
		} catch (Throwable t) {
			logger.error("Error while saving file into server: " + t);
			throw new SpagoBIServiceException(getActionName(), "Error while saving file into server", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private void saveFile(FileItem uploaded, File saveTo) {
		logger.debug("IN");
		try {
			uploaded.write(saveTo);
		} catch (Throwable t) {
			logger.error("Error while saving file into server: " + t);
			throw new SpagoBIServiceException(getActionName(), "Error while saving file into server", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private void checkPreviewFileMaxSize(FileItem uploaded) {
		int maxSize = GeneralUtilities.getTemplateMaxSize();
		if (uploaded.getSize() > maxSize) {
			throw new SpagoBIServiceException(getActionName(), "The uploaded file exceeds the maximum size, that is " + maxSize + " bytes");
		}
	}

	private String getActionName() {
		return "PREVIEW_FILE_UPLOAD";
	}

	/*
	 * @POST
	 * 
	 * @Path("/{id}")
	 * 
	 * @Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON }) public Response persistDataSets(@MultipartForm MultipartFormDataInput input,
	 * @PathParam("id") int documentId) {
	 * 
	 * Map<String, List<InputPart>> uploadForm = input.getFormDataMap(); List<InputPart> fileNamePart = uploadForm.get("fileName"); List<InputPart> fileParts =
	 * uploadForm.get("file");
	 * 
	 * try { String previewFile = fileNamePart.get(0).getBodyAsString(); BIObject biObject = DAOFactory.getBIObjectDAO().loadBIObjectById((documentId));
	 * biObject.setPreviewFile(previewFile); DAOFactory.getBIObjectDAO().modifyBIObject(biObject); return Response.status(200).build(); } catch (Exception e) {
	 * throw new SpagoBIRestServiceException(getLocale(), e); }
	 * 
	 * 
	 * }
	 */

	/*
	 * @POST
	 * 
	 * @Path("/{ID}/versions")
	 * 
	 * @Consumes({ MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON }) public Response uploadFile(@MultipartForm MultipartFormDataInput input,
	 * @PathParam("ID") int artifactId) {
	 * 
	 * Content content = new Content(); byte[] bytes = null;
	 * 
	 * artifactDAO = DAOFactory.getArtifactsDAO();
	 * 
	 * Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
	 * 
	 * List<InputPart> fileNamePart = uploadForm.get("fileName"); List<InputPart> fileParts = uploadForm.get("file");
	 * 
	 * if (fileNamePart != null && fileParts != null) { try {
	 * 
	 * content.setFileName(fileNamePart.get(0).getBodyAsString());
	 * 
	 * // convert the uploaded file to input stream InputStream inputStream = fileParts.get(0).getBody(InputStream.class, null);
	 * 
	 * bytes = IOUtils.toByteArray(inputStream);
	 * 
	 * content.setContent(bytes); content.setCreationDate(new Date()); content.setCreationUser(getUserProfile().getUserName().toString());
	 * 
	 * artifactDAO.insertArtifactContent(artifactId, content); String encodedContentId = URLEncoder.encode("" + content.getId(), "UTF-8"); //
	 * System.out.println(new URI(uri.getAbsolutePath() + encodedContentId)); } catch (IOException e) { e.printStackTrace(); }
	 * 
	 * } else { return Response.status(Status.BAD_REQUEST).build();
	 * 
	 * }
	 * 
	 * return Response.status(200).build();
	 * 
	 * }
	 */
}
