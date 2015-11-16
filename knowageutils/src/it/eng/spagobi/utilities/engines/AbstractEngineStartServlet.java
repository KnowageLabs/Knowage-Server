/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.engines;



import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.configuration.FileCreatorConfiguration;
import it.eng.spagobi.utilities.service.AbstractBaseServlet;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractEngineStartServlet extends AbstractBaseServlet {


    /**
     * Logger component
     */
    private static transient Logger logger = Logger.getLogger(AbstractEngineStartServlet.class);
    
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);	
    	
    	String path = getServletConfig().getServletContext().getRealPath("/WEB-INF");
    	ConfigSingleton.setConfigurationCreation( new FileCreatorConfiguration( path ) );
    	ConfigSingleton.setRootPath( path );
    	ConfigSingleton.setConfigFileName("/empty.xml");    	
    }
    
    public void doService( BaseServletIOManager servletIOManager ) throws SpagoBIEngineException {
    	
    	EngineStartServletIOManager engineServletIOManager;
    	
    	engineServletIOManager = new EngineStartServletIOManager(servletIOManager);
    	
    	try {
			this.doService( engineServletIOManager );
		} catch (Throwable t) {
			handleException(servletIOManager, t);
		}
		
    }
    
    public void doService( EngineStartServletIOManager servletIOManager ) throws SpagoBIEngineException {
    	
    	logger.debug("User Id: " + servletIOManager.getUserId());
		logger.debug("Audit Id: " + servletIOManager.getAuditId());
		logger.debug("Document Id: " + servletIOManager.getDocumentId());
		logger.debug("Template: " + servletIOManager.getTemplateAsSourceBean());
				
    }

    public void handleException(EngineStartServletIOManager servletIOManager, Throwable t) {
    	logger.error("Service execution failed", t);
    	
    	servletIOManager.auditServiceErrorEvent(t.getMessage());			
		
    	String reponseMessage = servletIOManager.getLocalizedMessage("msg.error.generic");
    	if(t instanceof SpagoBIEngineException) {
    		SpagoBIEngineException e = (SpagoBIEngineException)t;
    		if(e.getDescription() != null) {
    			reponseMessage = servletIOManager.getLocalizedMessage(e.getDescription());
    		} else {
    			reponseMessage = servletIOManager.getLocalizedMessage(e.getMessage());
    		}		
    	} 		
    	
    	servletIOManager.tryToWriteBackToClient( reponseMessage );		
    }
    
    public void handleException(BaseServletIOManager servletIOManager, Throwable t) {
    	handleException( new EngineStartServletIOManager(servletIOManager), t);
    }
    

   
	
}
