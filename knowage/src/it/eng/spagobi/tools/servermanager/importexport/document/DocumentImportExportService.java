package it.eng.spagobi.tools.servermanager.importexport.document;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.UploadedFile;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.importexport.ExportUtilities;
import it.eng.spagobi.tools.importexport.IExportManager;
import it.eng.spagobi.tools.importexport.IImportManager;
import it.eng.spagobi.tools.importexport.ImportExportConstants;
import it.eng.spagobi.tools.importexport.ImportManager;
import it.eng.spagobi.tools.importexport.ImportUtilities;
import it.eng.spagobi.tools.importexport.TransformManager;
import it.eng.spagobi.tools.importexport.bo.AssociationFile;
import it.eng.spagobi.tools.importexport.dao.AssociationFileDAO;
import it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO;
import it.eng.spagobi.tools.servermanager.importexport.utils.downloadFileUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

@Path("/1.0/serverManager/importExport/document")
@ManageAuthorization
public class DocumentImportExportService {

	private static final String EXPORT_FILE_NAME = "EXPORT_FILE_NAME";
	private static final String DOCUMENT_ID_LIST = "DOCUMENT_ID_LIST";
	private static final String EXPORT_SUB_OBJ = "EXPORT_SUB_OBJ";
	private static final String EXPORT_SNAPSHOT = "EXPORT_SNAPSHOT";
	private static final String FILE_NAME = "FILE_NAME";

	static protected Logger logger = Logger.getLogger(DocumentImportExportService.class);

	@POST
	@Path("/export")
	public String exportDocument(@Context HttpServletRequest req) throws JSONException, IOException, EMFValidationError {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		JSONObject resp = new JSONObject();
		IExportManager expManager = null;
		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);
		String exportFileName = requestVal.getString(EXPORT_FILE_NAME);

		if ((exportFileName == null) || (exportFileName.trim().equals(""))) {
			logger.error("Missing name of the exported file");
			throw new EMFValidationError(EMFErrorSeverity.ERROR, "exportDocument", "8006", ImportManager.messageBundle);
		}

		try {
			Boolean exportSubObject = requestVal.optBoolean(EXPORT_SUB_OBJ);
			Boolean exportSnapshots = requestVal.optBoolean(EXPORT_SNAPSHOT);
			String pathExportFolder = ExportUtilities.getExportTempFolderPath();
			JSONArray docIdList = requestVal.getJSONArray(DOCUMENT_ID_LIST);
			List ids = JSONUtils.asList(docIdList);
			expManager = ExportUtilities.getExportManagerInstance();
			expManager.setProfile(profile);
			expManager.prepareExport(pathExportFolder, exportFileName, exportSubObject, exportSnapshots);
			expManager.exportObjects(ids);
			expManager.createExportArchive();
			resp.put("STATUS", "OK");
		} catch (Throwable t) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service exportDocument", t);
		}

		return resp.toString();
	}

	@POST
	@Path("/downloadExportFile")
	public Response downloadExportFile(@Context HttpServletRequest req, @Context HttpServletResponse resp) throws IOException, JSONException {
		JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

		logger.debug("IN");
		try {

			String exportFileName = requestVal.getString(FILE_NAME);
			if ((exportFileName == null) || (exportFileName.trim().equals(""))) {
				logger.error("Missing name of the exported file");
				throw new EMFValidationError(EMFErrorSeverity.ERROR, "exportDocument", "8006", ImportManager.messageBundle);
			}
			byte[] zipFile = downloadFileUtils.manageDownloadExportFile(exportFileName, resp);
			if (zipFile != null && zipFile.length > 0) {
				return Response.ok(zipFile).build();
			}
		} catch (Exception e) {
			logger.error("Error while downloading export file", e);
		} finally {
			logger.debug("OUT");
		}

		return Response.serverError().build();

	}

	@POST
	@Path("/import")
	@Consumes("multipart/form-data")
	public Response uploadFile(MultipartFormDataInput input, @Context HttpServletRequest req) throws JSONException {
		JSONObject response = new JSONObject();
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		IImportManager impManager = null;
		AssociationFile assFile = null;
		byte[] archiveBytes = null;
		byte[] associationsFileBytes = null;
		try {
			Map<String, List<InputPart>> formParts = input.getFormDataMap();
			String assKindFromReq = formParts.get("importAssociationKind").get(0).getBodyAsString();
			boolean isNoAssociationModality = assKindFromReq.equalsIgnoreCase("noassociations");
			String exportedArchiveName = "";
			// load the archive to import
			List<InputPart> exportedArchivePart = formParts.get("exportedArchive");
			for (InputPart inputPart : exportedArchivePart) {
				exportedArchiveName = parseFileName(inputPart.getHeaders());
				if (exportedArchiveName != null && exportedArchiveName.endsWith(".zip")) {
					InputStream istream = inputPart.getBody(InputStream.class, null);
					ByteArrayOutputStream archiveByteArrayOS = this.getByteArrayOS(istream);
					if (archiveByteArrayOS.size() > ImportUtilities.getImportFileMaxSize()) {
						logger.error("File is too large!!!");
						throw new EMFValidationError(EMFErrorSeverity.ERROR, "exportedArchive", "202");
					}
					archiveBytes = archiveByteArrayOS.toByteArray();
				} else {
					response.put("STATUS", "NON OK");
					response.put("ERROR", "exportedArchive null or not zip file");
					return Response.status(200).entity(response.toString()).build();
				}
			}

			// if is associations modality load the associations file
			if (!isNoAssociationModality) {
				List<InputPart> associationsFilePart = formParts.get("associationsFile");
				if (associationsFilePart != null) {
					// load the associations file
					for (InputPart inputPart : associationsFilePart) {
						String associationsFileName = parseFileName(inputPart.getHeaders());
						if (associationsFileName != null && associationsFileName.endsWith(".xml")) {
							InputStream istream = inputPart.getBody(InputStream.class, null);
							ByteArrayOutputStream associationsFileByteArrayOS = this.getByteArrayOS(istream);
							// checks if the association file is bigger than 1 MB, that is more than enough!!
							if (associationsFileByteArrayOS.size() > 1048576) {
								logger.error("File is too large!!!");
								throw new EMFValidationError(EMFErrorSeverity.ERROR, "associationsFile", "202");
							}
							associationsFileBytes = associationsFileByteArrayOS.toByteArray();

						} else {
							response.put("STATUS", "NON OK");
							response.put("ERROR", "associationsFile null or not zip file");
							return Response.status(200).entity(response.toString()).build();
						}
					}
				} else {
					// if the association file is empty then check if there is an association id
					String assId = formParts.get("hidAssId").get(0).getBodyAsString();
					if ((assId != null) && !assId.trim().equals("")) {
						IAssociationFileDAO assfiledao = new AssociationFileDAO();
						assFile = assfiledao.loadFromID(assId);
						byte[] content = assfiledao.getContent(assFile);
						UploadedFile uplFile = new UploadedFile();
						uplFile.setSizeInBytes(content.length);
						uplFile.setFileContent(content);
						uplFile.setFileName("association.xml");
						uplFile.setFieldNameInForm("");
						associationsFileBytes = uplFile.getFileContent();
					}

				}
			}

			// get the association mode
			String assMode = IImportManager.IMPORT_ASS_DEFAULT_MODE;
			if (assKindFromReq.equalsIgnoreCase("predefinedassociations")) {
				assMode = IImportManager.IMPORT_ASS_PREDEFINED_MODE;
			}

			// get path of the import tmp directory
			String pathImpTmpFolder = ImportUtilities.getImportTempFolderPath();
			// apply transformation
			TransformManager transManager = new TransformManager();
			String archiveName = GeneralUtilities.getRelativeFileNames(exportedArchiveName);
			archiveBytes = transManager.applyTransformations(archiveBytes, archiveName, pathImpTmpFolder);

			// prepare import environment
			impManager = ImportUtilities.getImportManagerInstance();
			impManager.setUserProfile(profile);
			impManager.init(pathImpTmpFolder, archiveName, archiveBytes);
			impManager.openSession();
			impManager.setAssociationFile(assFile);

			// if the associations file has been uploaded fill the association keeper
			if (associationsFileBytes != null) {
				String assFileStr = new String(associationsFileBytes);
				try {
					impManager.getUserAssociation().fillFromXml(assFileStr);
				} catch (Exception e) {
					logger.error("Error while loading association file content:\n " + e);
					throw new EMFValidationError(EMFErrorSeverity.ERROR, "exportedArchive", "8009", ImportManager.messageBundle);
				}
			}

			// set into import manager the association import mode
			impManager.setImpAssMode(assMode);

			req.getSession().setAttribute(ImportExportConstants.IMPORT_MANAGER, impManager);

			List exportedRoles = impManager.getExportedRoles();
			IRoleDAO roleDAO = DAOFactory.getRoleDAO();
			List currentRoles = roleDAO.loadAllRoles();
			response.put("exportedRoles", new JSONArray(JsonConverter.objectToJson(exportedRoles, exportedRoles.getClass())));
			response.put("currentRoles", new JSONArray(JsonConverter.objectToJson(currentRoles, currentRoles.getClass())));
			response.put("STATUS", "OK");
			System.out.println("fine");
		} catch (Exception e) {
			logger.error("Error while import file", e);

			response.put("STATUS", "NON OK");
			response.put("ERROR", e.getMessage());
			e.printStackTrace();
		} finally {
			if (impManager != null)
				impManager.closeSession();
			logger.debug("OUT");

		}

		return Response.status(200).entity(response.toString()).build();
	}

	// Parse Content-Disposition header to get the original file name
	private String parseFileName(MultivaluedMap<String, String> headers) {

		String[] contentDispositionHeader = headers.getFirst("Content-Disposition").split(";");

		for (String name : contentDispositionHeader) {

			if ((name.trim().startsWith("filename"))) {

				String[] tmp = name.split("=");

				String fileName = tmp[1].trim().replaceAll("\"", "");

				return fileName;
			}
		}
		return null;
	}

	// save uploaded file to a defined location on the server
	private void saveFile(InputStream uploadedInputStream, String serverLocation) {

		try {
			OutputStream outpuStream = new FileOutputStream(new File(serverLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			outpuStream = new FileOutputStream(new File(serverLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				outpuStream.write(bytes, 0, read);
			}
			outpuStream.flush();
			outpuStream.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	// save uploaded file to a defined location on the server
	private ByteArrayOutputStream getByteArrayOS(InputStream uploadedInputStream) {
		try {
			final byte[] inBytes = new byte[1024];
			ByteArrayOutputStream outBytes = new ByteArrayOutputStream(inBytes.length);
			int length = 0;

			while ((length = uploadedInputStream.read(inBytes)) >= 0) {
				outBytes.write(inBytes, 0, length);
			}

			return outBytes;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@POST
	@Path("/import2")
	@Consumes("multipart/form-data")
	public String importFunctionStep02(MultipartFormDataInput input, @Context HttpServletRequest req) throws IOException, JSONException {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		IImportManager impManager = null;
		try {

			// boolean isMultipart = ServletFileUpload.isMultipartContent(new ServletRequestContext(req));
			// FileItemFactory factory = new DiskFileItemFactory();
			// ServletFileUpload upload = new ServletFileUpload(factory);
			// List fileItems = upload.parseRequest(req);
			// ExportUtilities.getExportTempFolderPath();

			// // Create a factory for disk-based file items
			// DiskFileItemFactory factory = new DiskFileItemFactory();
			//
			// // Configure a repository (to ensure a secure temp location is used)
			// File repository = new File(System.getProperty("java.io.tmpdir"));
			// factory.setRepository(repository);
			//
			// // Create a new file upload handler
			// ServletFileUpload upload = new ServletFileUpload(factory);
			//
			// // Parse the request
			//
			// List<FileItem> items = upload.parseRequest(req);

			// FileItemFactory factory = new DiskFileItemFactory();
			// ServletFileUpload upload = new ServletFileUpload(factory);
			// List fileItems = upload.parseRequest(req);
			// Iterator iter = fileItems.iterator();
			// while (iter.hasNext()) {
			// FileItem item = (FileItem) iter.next();
			// }

			// get exported file and eventually the associations file
			Map<String, List<InputPart>> formDataMap = input.getFormDataMap();

			boolean isNoAssociationModality = formDataMap.get("importAssociationKind").get(0).getBodyAsString().equalsIgnoreCase("noassociations");

			// load imported file
			List<InputPart> inputParts = formDataMap.get("exportedArchive");

			// Create a factory for disk-based file items

			for (InputPart inputPart : inputParts) {
				if (true || inputPart.getHeaders().get("Content-Disposition").toString().contains("zip")) {
					System.out.println("zip beccato");
					byte[] data = inputPart.getBodyAsString().replace("data:;base64,", "").getBytes(Charset.forName("UTF-8"));

					byte[] result = new byte[0];
					try {
						result = Base64.decodeBase64(data);
					} catch (Exception e) {

					}

					String fileName = "test";
					String fileExtension = "zip";
					String archivePath = ExportUtilities.getExportTempFolderPath() + "/" + fileName + "." + fileExtension;

					File archiveFile = new File(archivePath);
					if (archiveFile.exists()) {
						archiveFile.delete();
					}

					FileOutputStream fos = new FileOutputStream(archivePath);
					ZipOutputStream out = new ZipOutputStream(fos);

					fos.write(data);
					fos.close();

					File outFile = new File(System.getProperty("java.io.tmpdir") + "/" + fileName + "2." + fileExtension);

					FileUtils.writeByteArrayToFile(outFile, data);
					FileItem fileItem = new DiskFileItem("fileUpload", "plain/text", false, "sometext.zip", 1000, outFile); // You link FileItem to the tmp
					// outFile
					int i = 0;
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		// JSONObject requestBodyJSON = null;
		// Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
		// List<InputPart> dataList = formDataMap.get("data");
		// for (InputPart inputPart : dataList) {
		// requestBodyJSON = new JSONObject(inputPart.getBodyAsString());
		// }
		return "pippo";
	}

	@POST
	@Path("/import23")
	@Consumes("multipart/form-data")
	public String importFunctionStep0(MultipartFormDataInput input, @Context HttpServletRequest req) throws IOException, JSONException {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		IImportManager impManager = null;
		try {
			Map<String, List<InputPart>> formDataMap = input.getFormDataMap();
			List<InputPart> inputParts = formDataMap.get("exportedArchive");
			for (InputPart inputPart : inputParts) {
				if (true || inputPart.getHeaders().get("Content-Disposition").toString().contains("zip")) {
					byte[] data = inputPart.getBodyAsString().replace("data:;base64,", "").getBytes(Charset.forName("UTF-8"));
					byte[] data2 = inputPart.getBodyAsString().getBytes(Charset.forName("UTF-8"));
					byte[] data3 = inputPart.getBodyAsString().getBytes();

					byte[] result = new byte[0];
					result = Base64.decodeBase64(data);

					String fileName = "test";
					String fileExtension = "zip";
					String archive1 = System.getProperty("java.io.tmpdir") + "/" + fileName + "1." + fileExtension;
					String archive2 = System.getProperty("java.io.tmpdir") + "/" + fileName + "2." + fileExtension;
					String archive3 = System.getProperty("java.io.tmpdir") + "/" + fileName + "3." + fileExtension;

					File archiveFile1 = new File(archive1);
					File archiveFile2 = new File(archive2);
					File archiveFile3 = new File(archive3);

					// FileUtils.writeByteArrayToFile(archiveFile1, data);
					// FileUtils.writeByteArrayToFile(archiveFile2, data2);
					// FileUtils.writeByteArrayToFile(archiveFile3, data3);

					FileOutputStream fop = new FileOutputStream(archiveFile1);
					fop.write(data);
					fop.flush();
					fop.close();
					FileOutputStream fop2 = new FileOutputStream(archiveFile2);
					fop2.write(data2);
					fop2.flush();
					fop2.close();
					FileOutputStream fop3 = new FileOutputStream(archiveFile3);
					fop3.write(data3);
					fop3.flush();
					fop3.close();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "pippo";
	}

	@GET
	@Path("/getAssociationsList")
	public String getAssociationsList() throws JSONException {
		IAssociationFileDAO assfiledao = new AssociationFileDAO();
		List assFiles = assfiledao.getAssociationFiles();
		JSONObject response = new JSONObject();
		response.put("STATUS", "OK");
		response.put("associationsList", new JSONArray(JsonConverter.objectToJson(assFiles, assFiles.getClass())));
		return response.toString();
	}

	@POST
	@Path("/test")
	@Consumes("multipart/form-data")
	public String test() {
		return "pippo";
	}

}
