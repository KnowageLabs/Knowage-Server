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
package it.eng.spagobi.api;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.clerezza.jaxrs.utils.form.ParameterValue;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.commons.security.PathTraversalChecker;
import it.eng.knowage.engine.dossier.activity.bo.DossierActivity;
import it.eng.knowage.engines.dossier.utils.DossierDocumentType;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.dossier.dao.ISbiDossierActivityDAO;
import it.eng.spagobi.tools.massiveExport.bo.ProgressThread;
import it.eng.spagobi.tools.massiveExport.dao.IProgressThreadDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Nikola Simovic (nikola.simovic@mht.net)
 */
@Path("/dossier")
public class DossierActivityResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = Logger.getLogger(DossierActivityResource.class);

	@GET
	@Path("/activities/{docId}")
	public List<DossierActivity> loadAllActivitiesByDocument(@PathParam("docId") Integer documentId) {

		ISbiDossierActivityDAO sdaDAO;
		List<DossierActivity> daList = new ArrayList<>();
		try {
			sdaDAO = DAOFactory.getDossierActivityDao();
			LOGGER.debug("Loading all activities for document with id: " + documentId);
			daList = sdaDAO.loadAllActivities(documentId);
			LOGGER.debug("Successfully loaded " + daList.size() + " activities for document with id: " + documentId);
		} catch (Exception e) {
			LOGGER.error("Error while loading activities for document with id: " + documentId, e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
		return daList;
	}

	@GET
	@Path("/activity/{activityId}")
	public DossierActivity laodActivityById(@PathParam("activityId") Integer activityId) {

		ISbiDossierActivityDAO sdaDAO;
		DossierActivity dossierActivity;
		try {
			sdaDAO = DAOFactory.getDossierActivityDao();
			LOGGER.debug("Loading activity with id: " + activityId);
			dossierActivity = sdaDAO.loadActivity(activityId);
			LOGGER.debug("Successfully loaded activity with id: " + activityId);
		} catch (Exception e) {
			LOGGER.error("Error while loading activity with id: " + activityId, e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
		return dossierActivity;
	}

	@GET
	@Path("/random-key/{progressId}")
	@Produces(MediaType.TEXT_PLAIN)
	public String loadRandomKeyByProgressId(@PathParam("progressId") Integer progressId) {

		IProgressThreadDAO ptDAO;
		ProgressThread pt;
		String randomKey;

		try {
			ptDAO = DAOFactory.getProgressThreadDAO();
			LOGGER.debug("Loading random key with progress id: " + progressId);
			pt = ptDAO.loadProgressThreadById(progressId);
			randomKey = pt.getRandomKey();
			LOGGER.debug("Successfully loaded random key of progress thread with id: " + progressId);
			return randomKey;
		} catch (Exception e) {
			LOGGER.error("Error while loading random key of progress thread with id: " + progressId, e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
	}

	@GET
	@Path("/resourcePath")
	public Response getresourcePath(@QueryParam("templateName") String fileName, @QueryParam("documentId") Integer documentId) throws JSONException {
		if (fileName.endsWith("?"))
			fileName = fileName.substring(0, fileName.length() - 1);

		File file = PathTraversalChecker.get(SpagoBIUtilities.getResourcePath(), "dossier", "" + documentId, fileName);

		ResponseBuilder responseBuilder = null;

		JSONObject response = new JSONObject();
		try {
			byte[] bytes = Files.readAllBytes(file.toPath());
			responseBuilder = Response.ok(bytes);
			responseBuilder.header("Content-Disposition", "attachment; filename=" + fileName);
			responseBuilder.header("filename", fileName);
			response.put("STATUS", "OK");
		} catch (Exception e) {
			response.put("STATUS", "KO");
			response.put("ERROR", e.getMessage());
			LOGGER.error(e);
			return Response.status(200).entity(response.toString()).build();
		} finally {
			LOGGER.debug("OUT");

		}

		return responseBuilder.build();
	}

	@GET
	@Path("/checkPathFile")
	public Response checkPathFile(@QueryParam("templateName") String fileName, @QueryParam("documentId") Integer documentId) throws JSONException {
		if (fileName.endsWith("?"))
			fileName = fileName.substring(0, fileName.length() - 1);

		File file = PathTraversalChecker.get(SpagoBIUtilities.getResourcePath(), "dossier", "" + documentId, fileName);

		JSONObject response = new JSONObject();
		try {
			Files.readAllBytes(file.toPath());
			response.put("STATUS", "OK");
		} catch (Exception e) {
			response.put("STATUS", "KO");
			response.put("ERROR", e.getMessage());
			LOGGER.error(e);
			return Response.status(200).entity(response.toString()).build();
		} finally {
			LOGGER.debug("OUT");

		}

		return Response.status(200).entity(response.toString()).build();
	}

	@POST
	@Path("/importTemplateFile")
	@Consumes("multipart/form-data")
	public Response importTemplateFile(MultiPartBody multipartFormDataInput) throws JSONException {
		byte[] archiveBytes = null;
		JSONObject response = new JSONObject();
		try {
			final FormFile file = multipartFormDataInput.getFormFileParameterValues("file")[0];
			ParameterValue[] documentIdArray = multipartFormDataInput.getParameteValues("documentId");
			String identifier = "";

			String fileName = file.getFileName();
			archiveBytes = file.getContent();

			File dossierDir = null;
			if (documentIdArray.length == 1) {
				identifier = documentIdArray[0].toString();
				dossierDir = PathTraversalChecker.get(SpagoBIUtilities.getResourcePath(), "dossier", identifier);
			} else {
				identifier = multipartFormDataInput.getParameteValues("uuid")[0].toString();
				dossierDir = PathTraversalChecker.get(System.getProperty("java.io.tmpdir"), identifier);
			}

			if (!dossierDir.exists()) {
				dossierDir.mkdir();
			}

			File f = PathTraversalChecker.get(dossierDir.getAbsolutePath(), fileName);

			try (FileOutputStream outputStream = new FileOutputStream(f)) {
				outputStream.write(archiveBytes);
			}
			response.put("STATUS", "OK");
		} catch (Exception e) {
			LOGGER.error("Error while import file", e);
			response.put("STATUS", "KO");
			response.put("ERROR", e.getMessage());
			LOGGER.error(e);
		} finally {
			LOGGER.debug("OUT");

		}

		return Response.status(200).entity(response.toString()).build();

	}

	@POST
	@Path("/activity")
	@Produces(MediaType.TEXT_HTML + "; charset=UTF-8")
	@Consumes(MediaType.APPLICATION_JSON)
	public String createNewActivity(DossierActivity dossierActivity, @Context HttpServletRequest req) {

		ISbiDossierActivityDAO sdaDAO;
		Integer id = null;

		try {

			UserProfile profile = getUserProfile();

			sdaDAO = DAOFactory.getDossierActivityDao();
			sdaDAO.setUserProfile(profile);
			LOGGER.debug("Creating new dossier activity");
			id = sdaDAO.insertNewActivity(dossierActivity);
			LOGGER.debug("Successfully created new dossier activity with id: " + id);
		} catch (Exception e) {
			LOGGER.error("Error while creating new activity", e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
		return id.toString();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("/activity/{activityId}/{type}")
	public Integer updateActivity(MultiPartBody multipartFormDataInput, @PathParam("activityId") Integer activityId, @PathParam("type") String type) {

		byte[] file = null;
		UserProfile profile = getUserProfile();
		ISbiDossierActivityDAO sdaDAO;
		Integer id = null;

		file = getFile(multipartFormDataInput);

		try {
			sdaDAO = DAOFactory.getDossierActivityDao();
			sdaDAO.setUserProfile(profile);
			DossierActivity dossierActivity = sdaDAO.loadActivity(activityId);
			LOGGER.debug("Updating dossier activity with id: " + dossierActivity.getId());
			id = sdaDAO.updateActivity(dossierActivity, file, type);
			LOGGER.debug("Successfully updated dossier activity with id: " + dossierActivity.getId());
		} catch (Exception e) {
			LOGGER.error("Error while updating new activity", e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
		return id;
	}

	@GET
	@Path("/activity/{activityId}/{type}")
	public Response downloadPPTExists(@PathParam("activityId") Integer activityId, @PathParam("type") String type,
			@QueryParam("activityName") String activityName) {

		ISbiDossierActivityDAO sdaDAO;
		DossierActivity activity;
		byte[] file = null;

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = format.format(cal.getTime());

		try {
			sdaDAO = DAOFactory.getDossierActivityDao();
			LOGGER.debug("Downloading PPT file with activity id: " + activityId + ". Activity name: " + activityName);
			activity = sdaDAO.loadActivity(activityId);
			String extension = "";

			switch (type) {
			case "doc":
			case "docx":
				file = activity.getDocBinContent();
				extension = DossierDocumentType.DOCX.getType();
				break;
			case "ppt":
				file = activity.getBinContent();
				extension = DossierDocumentType.PPT.getType();
				break;
			case "pptv2":
				file = activity.getPptV2BinContent();
				extension = DossierDocumentType.PPTX.getType();
				break;
			default:
				break;
			}

			if (file == null) {
				String message = "Error while matching the file extesion";
				LOGGER.error(message + " " + type);
				throw new SpagoBIRuntimeException(message);
			}

			ResponseBuilder response = Response.ok(file);
			response.header("Content-Disposition", "attachment; filename=" + activityName + "_" + formattedDate + "." + extension);
			response.header("filename", activityName + "_" + formattedDate + "." + extension);
			return response.build();

		} catch (Exception e) {
			LOGGER.error("Error while downloading file with activity id: " + activityId + " for activity: " + activityName, e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}

	}

	@DELETE
	@Path("/activity/{activityId}")
	public void deleteActivityById(@PathParam("activityId") Integer activityId) {

		ISbiDossierActivityDAO sdaDAO;
		try {
			sdaDAO = DAOFactory.getDossierActivityDao();
			LOGGER.debug("Deleting activity with id: " + activityId);
			sdaDAO.deleteActivity(activityId);
			LOGGER.debug("Successfully deleted activity with id: " + activityId);
		} catch (Exception e) {
			LOGGER.error("Error while deleting activity with id: " + activityId, e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
	}

	private byte[] getFile(MultiPartBody multipartFormDataInput) {
		byte[] bytes = null;
		final FormFile file = multipartFormDataInput.getFormFileParameterValues("file")[0];
		bytes = file.getContent();
		return bytes;
	}

}
