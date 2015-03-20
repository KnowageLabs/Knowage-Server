/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.services.core.catalogue;

import it.eng.qbe.query.Query;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONAcknowledge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;


/**
 * Remove from the catalogue queries whose ids has been passed as argument to the service. 
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class DeleteQueriesAction extends AbstractQbeEngineAction {	
	
	public static final String SERVICE_NAME = "DELETE_QUERIES_ACTION";
	public String getActionName(){return SERVICE_NAME;}
	
	
	// INPUT PARAMETERS
	public static final String QUERY_IDS = "queries";
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(DeleteQueriesAction.class);
   
    
	
	public void service(SourceBean request, SourceBean response)  {				
				
		JSONArray ids;
		Iterator it;
			
		logger.debug("IN");
		
		try {
		
			super.service(request, response);		
			
			ids = getAttributeAsJSONArray( QUERY_IDS );
			logger.debug("Parameter [" + QUERY_IDS + "] is equals to [" + ids + "]");
			
			Assert.assertNotNull(getEngineInstance(), "It's not possible to execute " + this.getActionName() + " service before having properly created an instance of EngineInstance class");
			
			
			if(ids != null && ids.length() > 0) {
				List list = new ArrayList();
				for(int i = 0; i < ids.length(); i++) {
					list.add(ids.getString(i));
				}
				it = list.iterator();
			} else {
				it = getEngineInstance().getQueryCatalogue().getIds().iterator();
			}
			
			while(it.hasNext()) {
				String id = (String)it.next();
				// leave query definition inside the while in order to release to gc the removed query asap			
				Query query = getEngineInstance().getQueryCatalogue().removeQuery(id);
				Assert.assertNotNull(query, "A query with id equals to [" + id + "] does not exist in teh catalogue");
				logger.debug("Qury with id equals to [" + QUERY_IDS + "] has been removed succesfully from the catalogue]");
			}
			
			try {
				writeBackToClient( new JSONAcknowledge() );
			} catch (IOException e) {
				String message = "Impossible to write back the responce to the client";
				throw new SpagoBIEngineServiceException(getActionName(), message, e);
			}
			
		} catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}	
		
	}

}
