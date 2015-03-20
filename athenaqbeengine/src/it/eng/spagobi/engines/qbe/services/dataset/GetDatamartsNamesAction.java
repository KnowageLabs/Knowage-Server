/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.dataset;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.QbeEngineConfig;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 * The Class GetDatamartsNamesAction.
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class GetDatamartsNamesAction extends AbstractQbeEngineAction {	
	
	// INPUT PARAMETERS
	public static String CALLBACK = "callback";
	
	// OUTPUT PARAMETERS
	
	// SESSION PARAMETRES	
	
	// AVAILABLE PUBLISHERS

	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(GetDatamartsNamesAction.class);
    
    public static final String ENGINE_NAME = "SpagoBIQbeEngine";
		
    public void service(SourceBean request, SourceBean response) {
    	
    	logger.debug("IN");
       
    	try {
			super.service(request, response);	
			
			List<String> datamartsName = getMetamodelNames();
			
			JSONArray array = new JSONArray();
			Iterator<String> it = datamartsName.iterator();
			while (it.hasNext()) {
				String aDatamartName = it.next();
				JSONObject temp = new JSONObject();
				temp.put("datamart", aDatamartName);
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
    
    /**
     * @return the list of existing metamodel names
     */
	private List<String> getMetamodelNames() {
		
		List<String> metamodelNames;
		
		logger.trace("IN");
		
		metamodelNames = null;
		try {
			metamodelNames = new ArrayList<String>();
			File metamodelFolder = QbeEngineConfig.getInstance().getQbeDataMartDir();
			File[] folders = metamodelFolder.listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					if (pathname.isDirectory()) {
						return true;
					}
					return false;
				}
			});
			
			if (folders == null || folders.length == 0) {
				throw new SpagoBIRuntimeException("No metamodels found!! Check configuration for metamodels repository");
			}
			
			for (int i = 0; i < folders.length; i++) {
				metamodelNames.add(folders[i].getName());
			}
			logger.debug("OUT");
		} catch(Throwable t) {
			
		} finally {
			logger.trace("OUT");
		}
		return metamodelNames;
	}

}
