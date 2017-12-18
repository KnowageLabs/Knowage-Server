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
