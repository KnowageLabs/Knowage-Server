/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core;
       
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.engines.EngineAnalysisMetadata;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * 
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */
public class GetAnalysisMetaAction extends AbstractQbeEngineAction {

	public static final String SERVICE_NAME = "GET_ANALYSIS_META_ACTION";
	public String getActionName(){return SERVICE_NAME;}
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(GetAnalysisMetaAction.class);
    
    
	public void service(SourceBean request, SourceBean response) {
		EngineAnalysisMetadata analysisMetadata = null;
		JSONObject meta;
		String rowMeta = "";
		
		logger.debug("IN");
		
		try {
			super.service(request, response);	
					
			analysisMetadata = getEngineInstance().getAnalysisMetadata();
			
			meta = new JSONObject();
			meta.put("id", analysisMetadata.getId());
			meta.put("name", analysisMetadata.getName());
			meta.put("description", analysisMetadata.getDescription());				
			meta.put("scope", analysisMetadata.getScope());
						
			
			try {
				writeBackToClient( new JSONSuccess(meta) );
			} catch (IOException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to write back the responce to the client", e);
			} 
			
		} catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}
	}
}
