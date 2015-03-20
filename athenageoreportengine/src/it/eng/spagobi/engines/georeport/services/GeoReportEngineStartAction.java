/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.georeport.services;

import javax.servlet.RequestDispatcher;

import org.apache.log4j.Logger;

import it.eng.spagobi.engines.georeport.GeoReportEngine;
import it.eng.spagobi.engines.georeport.GeoReportEngineInstance;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.AbstractEngineStartServlet;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;


/**
 * @authors Andrea Gioia (andrea.gioia@eng.it), Fabio D'Ovidio (f.dovidio@inovaos.it)
 */
public class GeoReportEngineStartAction extends AbstractEngineStartServlet {
	
	private static final String ENGINE_NAME = "GeoReportEngine";
	
	private static final String REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/geoReport.jsp";
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(GeoReportEngineStartAction.class);
    
	
	public void doService( EngineStartServletIOManager servletIOManager ) throws SpagoBIEngineException {
		
		GeoReportEngineInstance engineInstance;
		IDataSource dataSource;
        IDataSet dataSet;
		RequestDispatcher requestDispatcher;
		
         
        logger.debug("IN");
        
        try {
        	// log some contextual infos
        	logger.debug("User: [" + servletIOManager.getUserId() + "]");
        	logger.debug("Document: [" + servletIOManager.getDocumentId() + "]");
        	logger.debug("Document Author: [" + servletIOManager.getDocumentAuthor() + "]");

        	dataSource = servletIOManager.getDataSource();
        	logger.debug("Datasource: [" + (dataSource == null? dataSource: dataSource.getLabel()) + "]");
        	 
        	dataSet = servletIOManager.getDataSet();
        	logger.debug("Dataset: [" + (dataSet == null? dataSource: dataSet.getName()) + "]");
        	
        	// create a new engine instance
        	engineInstance = GeoReportEngine.createInstance(
        			servletIOManager.getTemplateAsString(), 
        			servletIOManager.getEnv()
        	);
        	
        	
        	servletIOManager.getHttpSession().setAttribute(EngineConstants.ENGINE_INSTANCE, engineInstance);
        	
        	// dispatch the request to the presentation layer
        	requestDispatcher = getServletContext().getRequestDispatcher( REQUEST_DISPATCHER_URL );
            try {
            	requestDispatcher.forward(servletIOManager.getRequest(), servletIOManager.getResponse());
    		} catch (Throwable t) {
    			throw new SpagoBIServiceException(ENGINE_NAME, "An error occurred while dispatching request to [" + REQUEST_DISPATCHER_URL + "]", t);
    		} 
        } catch(Throwable t) {
        	logger.error("Impossible to execute document", t);
        	t.printStackTrace();
        	throw new SpagoBIServiceException(ENGINE_NAME, t);
        } finally {
        	logger.debug("OUT");        	 
        }        

	}

}
