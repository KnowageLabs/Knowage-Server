package it.eng.spagobi.tools.servermanager.importexport.document;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.importexport.ExportUtilities;
import it.eng.spagobi.tools.importexport.IExportManager;
import it.eng.spagobi.tools.importexport.ImportManager;
import it.eng.spagobi.tools.servermanager.importexport.utils.downloadFileUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
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
	@Path("/test")
	public String test() {
		return "pippo";
	}

}
