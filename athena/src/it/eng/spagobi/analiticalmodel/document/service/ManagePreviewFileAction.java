/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.service;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIServiceExceptionHandler;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.file.FileUtils;
import it.eng.spagobi.utilities.service.IServiceResponse;
import it.eng.spagobi.utilities.service.JSONResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ManagePreviewFileAction extends AbstractSpagoBIAction {

	static private Logger logger = Logger.getLogger(ManagePreviewFileAction.class);

	private static final String UPLOADED_FILE = "UPLOADED_FILE";
	private static final String OPER_UPLOAD = "UPLOAD";
	private static final String OPER_DOWNLOAD = "DOWNLOAD";

	private static final String OPERATION = "operation";
	
	private static final List<String> VALID_FILE_EXTENSIONS = Arrays.asList("BMP", "JPG", "JPEG", "PNG", "GIF");

	public String getActionName() { return SERVICE_NAME; }


	public void doService()  {
		logger.debug("IN");
    	
    	try {
			
			String operation = (String) getAttribute(OPERATION);
			logger.debug("Manage operation: " + operation);
			if (operation == null) {
				throw new SpagoBIServiceException(getActionName(),
						"No operation [UPLOAD, DOWNLAOD] is defined. ");
			}

			JSONObject jsonToReturn = new JSONObject();
			if (OPER_UPLOAD.equalsIgnoreCase(operation)) {
				jsonToReturn = uploadFile();
				replayToClient(null, jsonToReturn);
			} else if (OPER_DOWNLOAD.equalsIgnoreCase(operation)) {
				freezeHttpResponse();
				String fileName = (String) getAttribute("fileName");
				FileInputStream fis = null;
				try {
					File file = getFile(fileName);
					fis = new FileInputStream(file);
					HttpServletResponse response = getHttpResponse();
					response.setHeader("Content-Disposition",
							"attachment; filename=\"" + fileName + "\";");
					byte[] content = GeneralUtilities
							.getByteArrayFromInputStream(fis);
					response.setContentLength(content.length);
					response.getOutputStream().write(content);
					response.getOutputStream().flush();
				} finally {
					if (fis != null) {
						fis.close();
					}
				}

			} else {
				throw new SpagoBIServiceException(getActionName(),
						"No valid operation [UPLOAD, DOWNLAOD] was specified.");
			}
    	} catch (Throwable t) {
			logger.error("Error while uploading file", t);
			SpagoBIServiceException e = SpagoBIServiceExceptionHandler
					.getInstance().getWrappedException(this.getActionName(), t);
			replayToClient(e, null);
		} finally {
			logger.debug("OUT");
		}	
	}
	
	// checks for path traversal attacks
	private void checkRequiredFile(String fileName) {
		File targetDirectory = GeneralUtilities.getPreviewFilesStorageDirectoryPath();
		FileUtils.checkPathTraversalAttack(fileName, targetDirectory);
	}

	private JSONObject uploadFile() throws Exception{		
		FileItem uploaded = (FileItem) getAttribute(UPLOADED_FILE);
		
		if (uploaded == null) {
			throw new SpagoBIServiceException(getActionName(), "No file was uploaded");
		}
		
		UserProfile userProfile = (UserProfile) this.getUserProfile();
		logger.info("User [id : " + userProfile.getUserId() + ", name : " + userProfile.getUserName() + "] " +
				"is uploading file [" + uploaded.getName() + "] with size [" + uploaded.getSize() + "]");
		
		int maxSize = Integer.parseInt( SingletonConfig.getInstance().getConfigValue("SPAGOBI.DOCUMENTS.MAX_PREVIEW_IMAGE_SIZE") );
		FileUtils.checkUploadedFile(uploaded, maxSize, VALID_FILE_EXTENSIONS);
		
		File targetDirectory = GeneralUtilities.getPreviewFilesStorageDirectoryPath();
		
		// check if number of existing images is the max allowed
		int maxFilesAllowed = Integer.parseInt( SingletonConfig.getInstance().getConfigValue("SPAGOBI.DOCUMENTS.MAX_PREVIEW_IMAGES_NUM") );
		FileUtils.checkIfFilesNumberExceedsInDirectory(targetDirectory, maxFilesAllowed);
		
		logger.debug("Saving file...");
		File saved = FileUtils.saveFileIntoDirectory(uploaded, targetDirectory);
		logger.debug("File saved");
		
		JSONObject toReturn = new JSONObject();
		try {				
			toReturn.put("success", true);
			toReturn.put("file","null");
			toReturn.put("fileName", saved.getName());
		} catch (JSONException jSONException) {
			logger.error(jSONException);
		}
	
		return toReturn;
		
	}

	private File getFile(String fileName) {
		File toReturn = null;		
		try {
			File targetDirectory = GeneralUtilities.getPreviewFilesStorageDirectoryPath();
			// checks for path traversal attack
			checkRequiredFile(fileName);
			toReturn = new File(targetDirectory, fileName);
			if (!toReturn.exists() || !toReturn.isFile()){					
				throw new SpagoBIServiceException(getActionName(), "Required file does not exist");
			}
		} catch (Exception e) {
    		logger.error("Error while uploading file", e);
    		SpagoBIServiceException e2 = SpagoBIServiceExceptionHandler.getInstance().getWrappedException(this.getActionName(), e);
    		replayToClient( e2, null);
		} 
		return toReturn;
	}

	 /*
     * see Ext.form.BasicForm for file upload
     */
	private void replayToClient(final SpagoBIServiceException e, final JSONObject jr) {
		
		try {
			
			writeBackToClient(  new IServiceResponse() {
				
				public boolean isInline() {
					return false;
				}
				
				public int getStatusCode() {
					if ( e != null) {
						return JSONResponse.FAILURE;
					}
					return JSONResponse.SUCCESS;
				}
				
				public String getFileName() {
					return null;
				}
				
				public String getContentType() {
					return "text/html";
				}
				
				public String getContent() throws IOException {
					if ( e != null) {
						try {
							JSONObject toReturn = new JSONObject();
							toReturn.put("success", false);
							toReturn.put("msg", e.getMessage());
							return toReturn.toString();
						} catch (JSONException jSONException) {
							logger.error(jSONException);
						}
					}
					if ( jr != null) {						
						return jr.toString();
					}
					return "{success:true, file:null}";
				}
				
			});
			
		} catch (IOException ioException) {
			logger.error("Impossible to write back the responce to the client", ioException);
		}
	}

}
