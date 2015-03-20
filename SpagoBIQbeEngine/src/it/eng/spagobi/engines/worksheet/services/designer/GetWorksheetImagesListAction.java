/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.services.designer;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONArray;


/**
 * The Class GetWorksheetImagesListAction.
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class GetWorksheetImagesListAction extends AbstractQbeEngineAction {	
	
	// INPUT PARAMETERS
	public static String CALLBACK = "callback";
	
	// OUTPUT PARAMETERS
	
	// SESSION PARAMETRES	
	
	// AVAILABLE PUBLISHERS

	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(GetWorksheetImagesListAction.class);
    
    public static final String ENGINE_NAME = "SpagoBIQbeEngine";
		
    public void service(SourceBean request, SourceBean response) {
    	
    	logger.debug("IN");
       
    	try {
			super.service(request, response);	
			
			File[] images = getImagesList();
			
			JSONArray array = new JSONArray();
			
			for (int i = 0; i < images.length; i++) {
				JSONArray temp = new JSONArray();
				temp.put(images[i].getName());
				array.put(temp);
			}
			
			String callback = getAttributeAsString( CALLBACK );
			
			try {
				if(callback == null) {
					writeBackToClient( new JSONSuccess( array ));
				} else {
					writeBackToClient( new JSONSuccess( array, callback ));
				}
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
    
	public static File[] getImagesList() {
		logger.debug("IN");
		File imagesDir = QbeEngineConfig.getInstance().getWorksheetImagesDir();
		File[] images = imagesDir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				if (pathname.isFile()) {
					return true;
				}
				return false;
			}
		});
		logger.debug("OUT");
		return images;
	}

}
