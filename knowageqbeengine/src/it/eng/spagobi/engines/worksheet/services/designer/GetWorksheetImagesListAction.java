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
