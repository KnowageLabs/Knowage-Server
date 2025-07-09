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

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.client.ProxyClientUtilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/ckan-management")
public class CkanHelper {

	private static transient Logger logger = Logger.getLogger(CkanHelper.class);

	private static final String DATASET_FILE_MAX_SIZE = "DATASET_FILE_MAX_SIZE";

	String fileExtension = "";

	@GET
	@Path("/download")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.CKAN_FUNCTIONALITY })
	public String DownloadCkanDataset(@Context HttpServletRequest request) throws JSONException {

		logger.debug("IN");
		try {
			IEngUserProfile profile = (IEngUserProfile) request.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			UserProfile userProfile = (UserProfile) profile;
			String ckanApiKey = null;
			// if (userProfile != null) {
			// ckanApiKey = userProfile.getUserUniqueIdentifier().toString();
			// }

			String fileURL = request.getParameter("url");

			RestUtilities.checkIfAddressIsInWhitelist(fileURL);

			String fileName = request.getParameter("id");
			String fileType = request.getParameter("format");
			fileExtension = fileType;

			// logger.info("User [id : " + userProfile.getUserId() + ", name : " + userProfile.getUserName() + "] " + "is uploading file [" + fileName + "."
			// + fileType + "]");

			File file = checkAndCreateDir(fileName + "." + fileType.toLowerCase());

			checkDatasetFileMaxSize(file.length(), userProfile);

			downloadAndSaveFile(fileURL, ckanApiKey, file);

			return replayToClient(file, null);
		} catch (Throwable t) {
			logger.error("Error while uploading CKAN dataset file", t);
			// SpagoBIServiceException e = SpagoBIServiceExceptionHandler.getInstance().getWrappedException("REST service /ckan-management/download", t);
			// return replayToClient(null, e);
			throw new SpagoBIServiceException("REST service /ckan-management/download", t);
		}

		finally {
			logger.debug("OUT");
		}
	}

	private void downloadAndSaveFile(String fileURL, String ckanApiKey, File saveTo) {
		logger.debug("IN");


		try {
			InputStream is = null;
			FileOutputStream fos = null;
			try {
				int statusCode = -1;
				WebTarget target = ProxyClientUtilities.getTarget(fileURL);
				Builder request = target.request(MediaType.APPLICATION_OCTET_STREAM);
				// For FIWARE CKAN instance
				if (ckanApiKey != null) {
					request.header("X-Auth-Token", ckanApiKey);
				}
				// For ANY CKAN instance
				// httpget.setRequestHeader("Authorization", ckanApiKey);
				is = request.get(InputStream.class);

				fos = new FileOutputStream(saveTo);

				logger.debug("Saving file...");
				byte[] buffer = new byte[1024];
				int len1 = 0;
				while ((len1 = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len1);
				}

				logger.debug("File saved");

			} catch (FileNotFoundException fnfe) {
				logger.error("Error while saving file into server");
				throw new SpagoBIServiceException("REST service /ckan-management/download", "Error while saving file into server", fnfe);
			} finally {
				if (fos != null) {
					fos.close();
				}
				if (is != null) {
					is.close();
				}
				logger.debug("OUT");
			}
		} catch (IOException ioe) {
			logger.error("Error while saving file into server");
			throw new SpagoBIServiceException("REST service /ckan-management/download", "Error while saving file into server", ioe);
		}
	}

	private String replayToClient(final File file, final SpagoBIServiceException e) {

		JSONObject response = new JSONObject();
		try {

			if (e == null) {
				response.put("result", "success");
				response.put("filename", file.getName());
				response.put("filetype", fileExtension);
				response.put("filesize", file.length());
				response.put("filepath", file.getAbsolutePath());

			} else {
				response.put("result", "failure");
				response.put("exception", e.getMessage());
			}
			return response.toString();
		} catch (JSONException jsonEx) {
			logger.error("Error during JSON conversion of result");
			throw new SpagoBIServiceException("REST service /ckan-management/download", "Error while generating JSON response", jsonEx);
		}
	}

	private File checkAndCreateDir(String name) {
		logger.debug("IN");
		try {
			String fileName = SpagoBIUtilities.getRelativeFileNames(name);
			String resourcePath = SpagoBIUtilities.getResourcePath();
			File datasetFileDir = new File(resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar + "temp");
			if (!datasetFileDir.exists()) {
				// Create Directory \dataset\files\temp under \resources if don't exists
				boolean mkdirResult = datasetFileDir.mkdirs();
				if (!mkdirResult) {
					throw new SpagoBIServiceException("REST service /ckan-management/download",
							"Cannot create \\dataset\\files directory into server resources");
				}
			}

			return new File(datasetFileDir, fileName);
		} catch (Throwable t) {
			logger.error("Error while saving file into server: " + t);
			throw new SpagoBIServiceException("REST service /ckan-management/download", "Error while saving file into server", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private int getDatasetsNumberUsingFile(FileItem uploaded) {
		String configuration;
		String fileName = SpagoBIUtilities.getRelativeFileNames(uploaded.getName());
		String fileToSearch = "\"fileName\":\"" + fileName + "\"";
		IDataSet iDataSet;
		int datasetUisng = 0;

		try {
			IDataSetDAO ds = DAOFactory.getDataSetDAO();
			List<IDataSet> datasets = ds.loadDataSets();
			if (datasets != null) {
				for (Iterator<IDataSet> iterator = datasets.iterator(); iterator.hasNext();) {
					iDataSet = iterator.next();
					configuration = iDataSet.getConfiguration();
					if (configuration.indexOf(fileToSearch) >= 0) {
						datasetUisng++;
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error checking if the file is used by other datasets ", e);
			throw new SpagoBIServiceException("REST service /ckan-management/download", "Error checking if the file is used by other datasets ", e);
		}
		return datasetUisng;
	}

	private void checkDatasetFileMaxSize(long fileSize, UserProfile userProfile) {

		logger.debug("Method checkDatasetFileMaxSize(): Start");

		// first check that the user profile isn't null
		if (userProfile == null) {
			throw new SpagoBIServiceException("REST service /ckan-management/download",
					"Impossible to check [ " + DATASET_FILE_MAX_SIZE + "] attribute without a valide user profile");
		}

		try {

			if (userProfile.getUserAttributes().containsKey(DATASET_FILE_MAX_SIZE)) {

				// the user profile contains the attribute that defines dataset file max size

				logger.debug("Method checkDatasetFileMaxSize(): Attribute [ " + DATASET_FILE_MAX_SIZE + " ] defined for [" + userProfile.getUserName()
						+ " ] profile. Validation needed");

				String datasetFileMaxSizeStr = (String) userProfile.getUserAttribute(DATASET_FILE_MAX_SIZE);

				if (!datasetFileMaxSizeStr.equals("")) {

					long datasetFileMaxSize = Long.parseLong(datasetFileMaxSizeStr);

					if (fileSize > datasetFileMaxSize) {

						throw new SpagoBIServiceException("REST service /ckan-management/download",
								"The uploaded file exceeds the maximum size assigned to the user, that is " + datasetFileMaxSize + " bytes");
					}
				} else {

					checkDatasetFileMaxSizeSystem(fileSize);
				}

			} else {
				logger.debug("Method checkDatasetFileMaxSize(): Attribute [ " + DATASET_FILE_MAX_SIZE + " ] not defined for [" + userProfile.getUserName()
						+ " ] profile. Check default system max dimension");
				// check if the uploaded file exceeds the maximum default dimension
				checkDatasetFileMaxSizeSystem(fileSize);
			}

			logger.debug("Method checkDatasetFileMaxSize(): End");
		} catch (Throwable t) {
			logger.error("Error retrieving user attribute [ " + DATASET_FILE_MAX_SIZE + " ] " + t);
			throw new SpagoBIServiceException("REST service /ckan-management/download", "The uploaded file exceeds the maximum size", t);

		}
	}

	private void checkDatasetFileMaxSizeSystem(long fileSize) {
		int maxSize = GeneralUtilities.getDataSetFileMaxSize();
		if (fileSize > maxSize) {
			throw new SpagoBIServiceException("REST service /ckan-management/download",
					"The uploaded file exceeds the maximum size, that is " + maxSize + " bytes");
		}
	}
}
