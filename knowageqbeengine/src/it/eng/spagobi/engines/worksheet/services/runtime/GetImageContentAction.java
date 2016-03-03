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
package it.eng.spagobi.engines.worksheet.services.runtime;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.worksheet.services.AbstractWorksheetEngineAction;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.mime.MimeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;


/**
 * The Class GetImageContentAction.
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class GetImageContentAction extends AbstractWorksheetEngineAction {	
	
	// INPUT PARAMETERS
	public static String FILE_NAME = "FILE_NAME";
	
	// OUTPUT PARAMETERS
	
	// SESSION PARAMETRES	
	
	// AVAILABLE PUBLISHERS

	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(GetImageContentAction.class);
    
    public static final String ENGINE_NAME = "SpagoBIQbeEngine";
		
    public void service(SourceBean request, SourceBean response) {
    	
    	logger.debug("IN");
       
    	try {
			super.service(request, response);
			
			freezeHttpResponse();
			
			String fileName = this.getAttributeAsString(FILE_NAME);
			logger.debug("File name parameter is [" + fileName + "]");
			
			File image = getImage();
			
			checkImageFilePosition(image);
			
			if (!image.exists() || image.isDirectory()) {
				throw new FileNotFoundException("Could not find file [" + fileName + "]");
			}
			
			UserProfile userProfile = (UserProfile) getEnv().get(EngineConstants.ENV_USER_PROFILE);
			logger.info("User [id : " + userProfile.getUserId() + ", name : " + userProfile.getUserName() + "] " +
					"is getting file [" + image.getAbsolutePath() + "]");
			
			String mimetype = MimeUtils.getMimeType(image);
			
			try {
				writeBackToClient(image, null, true, fileName, mimetype);
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}
			
		} catch (Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}		

	}
    
	private void checkImageFilePosition(File file) {
		logger.debug("IN");
		try {
			File imagesDir = QbeEngineConfig.getInstance().getWorksheetImagesDir();
			File parent = file.getParentFile();
			// Prevent directory traversal (path traversal) attacks
			if (!imagesDir.equals(parent)) {
				logger.error("Trying to access the file [" + file.getAbsolutePath() 
			                 + "] that is not inside [" + imagesDir.getAbsolutePath() + "]!!!");
				throw new SecurityException("Trying to access the file [" 
			                 + file.getAbsolutePath() + "] that is not inside [" 
			                 + imagesDir.getAbsolutePath() + "]!!!");
			}
		} finally {
			logger.debug("OUT");
		}
	}
    
	private File getImage() {
		logger.debug("IN");
		File toReturn = null;
		File imagesDir = QbeEngineConfig.getInstance().getWorksheetImagesDir();
		String fileName = this.getAttributeAsString(FILE_NAME);
		toReturn = new File(imagesDir, fileName);
		logger.debug("OUT");
		return toReturn;
	}

}
