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
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.engine.dossier.activity.bo.DossierActivity;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.dossier.dao.ISbiDossierActivityDAO;
import it.eng.spagobi.tools.massiveExport.bo.ProgressThread;
import it.eng.spagobi.tools.massiveExport.dao.IProgressThreadDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

/**
 * @author Nikola Simovic (nikola.simovic@mht.net)
 */
@Path("/dossier")
public class DossierActivityResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DossierActivityResource.class);

	@GET
	@Path("/activities/{docId}")
	public List<DossierActivity> loadAllActivitiesByDocument(@PathParam("docId") Integer documentId) {

		ISbiDossierActivityDAO sdaDAO;
		List<DossierActivity> daList = new ArrayList<DossierActivity>();
		try {
			sdaDAO = DAOFactory.getDossierActivityDao();
			logger.debug("Loading all activities for document with id: " + documentId);
			daList = sdaDAO.loadAllActivities(documentId);
			logger.debug("Successfully loaded " + daList.size() + " activities for document with id: " + documentId);
		} catch (Exception e) {
			logger.error("Error while loading activities for document with id: " + documentId, e);
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
			logger.debug("Loading activity with id: " + activityId);
			dossierActivity = sdaDAO.loadActivity(activityId);
			logger.debug("Successfully loaded activity with id: " + activityId);
		} catch (Exception e) {
			logger.error("Error while loading activity with id: " + activityId, e);
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
			logger.debug("Loading random key with progress id: " + progressId);
			pt = ptDAO.loadProgressThreadById(progressId);
			randomKey = pt.getRandomKey();
			logger.debug("Successfully loaded random key of progress thread with id: " + progressId);
			return randomKey;
		} catch (Exception e) {
			logger.error("Error while loading random key of progress thread with id: " + progressId, e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
	}

	@GET
	@Path("/resourcePath")
	public Response getresourcePath(@QueryParam("templateName") String fileName) throws JSONException {
		String separator = File.separator;
		if (fileName.endsWith("?"))
			fileName = fileName.substring(0, fileName.length() - 1);
		String outPath = SpagoBIUtilities.getResourcePath() + separator + "dossier" + separator + fileName;
		ResponseBuilder responseBuilder = null;
		byte[] bytes;
		File file = new File(outPath);
		JSONObject response = new JSONObject();
		try {
			bytes = Files.readAllBytes(file.toPath());
			responseBuilder = Response.ok(bytes);
			responseBuilder.header("Content-Disposition", "attachment; filename=" + fileName);
			responseBuilder.header("filename", fileName);
			response.put("STATUS", "OK");
		} catch (Exception e) {
			response.put("STATUS", "KO");
			response.put("ERROR", e.getMessage());
			logger.error(e);
			return Response.status(200).entity(response.toString()).build();
		} finally {
			logger.debug("OUT");

		}

		return responseBuilder.build();
	}

	@GET
	@Path("/checkPathFile")
	public Response checkPathFile(@QueryParam("templateName") String fileName) throws JSONException {
		String separator = File.separator;
		if (fileName.endsWith("?"))
			fileName = fileName.substring(0, fileName.length() - 1);
		String outPath = SpagoBIUtilities.getResourcePath() + separator + "dossier" + separator + fileName;
		byte[] bytes;
		File file = new File(outPath);
		JSONObject response = new JSONObject();
		try {
			bytes = Files.readAllBytes(file.toPath());
			response.put("STATUS", "OK");
		} catch (Exception e) {
			response.put("STATUS", "KO");
			response.put("ERROR", e.getMessage());
			logger.error(e);
			return Response.status(200).entity(response.toString()).build();
		} finally {
			logger.debug("OUT");

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

			String separator = File.separator;
			final FormFile file = multipartFormDataInput.getFormFileParameterValues("file")[0];
			String fileName = file.getFileName();
			archiveBytes = file.getContent();
			File f = new File(SpagoBIUtilities.getResourcePath() + separator + "dossier" + separator + fileName);
			FileOutputStream outputStream = new FileOutputStream(f);
			outputStream.write(archiveBytes);
			outputStream.close();
			response.put("STATUS", "OK");
		} catch (Exception e) {
			logger.error("Error while import file", e);
			response.put("STATUS", "KO");
			response.put("ERROR", e.getMessage());
			logger.error(e);
		} finally {
			logger.debug("OUT");

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
			logger.debug("Creating new dossier activity");
			id = sdaDAO.insertNewActivity(dossierActivity);
			logger.debug("Successfully created new dossier activity with id: " + id);
		} catch (Exception e) {
			logger.error("Error while creating new activity", e);
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
			logger.debug("Updating dossier activity with id: " + dossierActivity.getId());
			id = sdaDAO.updateActivity(dossierActivity, file, type);
			logger.debug("Successfully updated dossier activity with id: " + dossierActivity.getId());
		} catch (Exception e) {
			logger.error("Error while updating new activity", e);
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
		byte[] file;

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		String formattedDate = format.format(cal.getTime());

		try {
			sdaDAO = DAOFactory.getDossierActivityDao();
			logger.debug("Downloading PPT file with activity id: " + activityId + ". Activity name: " + activityName);
			activity = sdaDAO.loadActivity(activityId);
			if (type.equals("doc")) {
				file = activity.getDocBinContent();
			} else {
				file = activity.getBinContent();
			}

			ResponseBuilder response = Response.ok(file);
			response.header("Content-Disposition", "attachment; filename=" + activityName + "_" + formattedDate + "." + type);
			response.header("filename", activityName + "_" + formattedDate + "." + type);
			return response.build();

		} catch (Exception e) {
			logger.error("Error while downloading file with activity id: " + activityId + " for activity: " + activityName, e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}

	}

	@DELETE
	@Path("/activity/{activityId}")
	public void deleteActivityById(@PathParam("activityId") Integer activityId) {

		ISbiDossierActivityDAO sdaDAO;
		try {
			sdaDAO = DAOFactory.getDossierActivityDao();
			logger.debug("Deleting activity with id: " + activityId);
			sdaDAO.deleteActivity(activityId);
			logger.debug("Successfully deleted activity with id: " + activityId);
		} catch (Exception e) {
			logger.error("Error while deleting activity with id: " + activityId, e);
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
