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
package it.eng.spagobi.engines.worksheet.services.designer;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.worksheet.services.AbstractWorksheetEngineAction;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.mime.MimeUtils;
import it.eng.spagobi.utilities.service.IServiceResponse;
import it.eng.spagobi.utilities.service.JSONResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * The Class UploadWorksheetImageAction.
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class UploadWorksheetImageAction extends AbstractWorksheetEngineAction {
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(UploadWorksheetImageAction.class);
    
    public static final String ENGINE_NAME = "SpagoBIQbeEngine";
		
    public void service(SourceBean request, SourceBean response) {
    	
    	logger.debug("IN");
       
    	try {
			super.service(request, response);
			
			FileItem uploaded = (FileItem) request.getAttribute("UPLOADED_FILE");

			if (uploaded == null) {
				throw new SpagoBIEngineServiceException(getActionName(), "No file was uploaded");
			}
				
			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
			logger.info("User [id : " + userProfile.getUserId() + ", name : " + userProfile.getUserName() + "] " +
					"is uploading file [" + uploaded.getName() + "] with size [" + uploaded.getSize() + "]");
			
			checkUploadedFile(uploaded);
			
			logger.debug("Saving file...");
			saveFile(uploaded);
			logger.debug("File saved");
			
			replayToClient( null);
			
		} catch (Throwable t) {
			SpagoBIEngineServiceException e = SpagoBIEngineServiceExceptionHandler
					.getInstance().getWrappedException(getActionName(),
							getEngineInstance(), t);
			replayToClient( e );
		} finally {
			logger.debug("OUT");
		}	

	}

    /*
     * see Ext.form.BasicForm for file upload
     */
	private void replayToClient(final SpagoBIEngineServiceException e) {

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
					return "{success:true, file:null}";
				}
				
			});
			
		} catch (IOException ioException) {
			logger.error("Impossible to write back the responce to the client", ioException);
		}
	}
	
    private boolean isImgFileExtension(String name){
		logger.debug("IN");
		try {

			ArrayList extensions= new ArrayList<String>();
			extensions.add("bmp");
			extensions.add("dds");
			extensions.add("gif");
			extensions.add("jpg");
			extensions.add("png");
			extensions.add("psd");
			extensions.add("pspimage");
			extensions.add("tga");
			extensions.add("thm");
			extensions.add("tif");
			extensions.add("tiff");
			extensions.add("yuv");

			return FilenameUtils.isExtension(name.toLowerCase(), extensions);
		} finally {
			logger.debug("OUT");

		}
    }
	private void checkUploadedFile(FileItem uploaded) {
		logger.debug("IN");
		try {
			// check if number of existing images is the max allowed
			File[] existingImages = GetWorksheetImagesListAction.getImagesList();
			int existingImagesNumber = existingImages.length;
			int maxNumber = QbeEngineConfig.getInstance().getWorksheetImagesMaxNumber();
			if (existingImagesNumber >= maxNumber) {
				throw new SpagoBIEngineServiceException(getActionName(), "Max images number reached");
			}
			// check if the file already exists
			String fileName = SpagoBIUtilities.getRelativeFileNames(uploaded.getName());
			File imagesDir = QbeEngineConfig.getInstance().getWorksheetImagesDir();
			File saveTo = new File(imagesDir, fileName);
			if (saveTo.exists()) {
				throw new SpagoBIEngineServiceException(getActionName(), "File already exists");
			}
			// check if the uploaded file is empty
			if (uploaded.getSize() == 0) {
				throw new SpagoBIEngineServiceException(getActionName(), "The uploaded file is empty");
			}
			// check if the uploaded file exceeds the maximum dimension
			int maxSize = QbeEngineConfig.getInstance().getWorksheetImagesMaxSize();
			if (uploaded.getSize() > maxSize) {
				throw new SpagoBIEngineServiceException(getActionName(), "The uploaded file exceeds the maximum size, that is " + maxSize);
			}
			//check if it is an image file
			if(!isImgFileExtension(fileName)){
				String message = "Not an image file";
				throw new SpagoBIEngineServiceException(getActionName(), message);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	private void saveFile(FileItem uploaded) {
		logger.debug("IN");
		try {
			String fileName = SpagoBIUtilities.getRelativeFileNames(uploaded.getName());
			File imagesDir = QbeEngineConfig.getInstance().getWorksheetImagesDir();
			File saveTo = new File(imagesDir, fileName);
			uploaded.write(saveTo);
		} catch (Throwable t) {
			throw new SpagoBIEngineServiceException(getActionName(), "Error while saving file into server", t);
		} finally {
			logger.debug("OUT");
		}
	}

}
