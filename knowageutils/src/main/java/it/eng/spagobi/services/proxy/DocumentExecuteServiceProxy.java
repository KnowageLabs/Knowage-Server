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
package it.eng.spagobi.services.proxy;

import it.eng.spagobi.services.execute.stub.DocumentExecuteServiceServiceLocator;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import java.util.HashMap;

import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * @author Angelo Bernabei
 *         angelo.bernabei@eng.it
 */
public class DocumentExecuteServiceProxy extends AbstractServiceProxy{

	static private final String SERVICE_NAME = "DocumentExecute Service";
	
    static private Logger logger = Logger.getLogger(DocumentExecuteServiceProxy.class);
    
    /**
     * The Constructor.
     * 
     * @param user userId
     * @param session Http Session
     */
    public DocumentExecuteServiceProxy(String user,HttpSession session) {
	super(user, session);
    }
    
    private DocumentExecuteServiceProxy() {
	super();
    } 
    
    /**
     * LookUp Method
     * @return
     * @throws SecurityException
     */
    private it.eng.spagobi.services.execute.stub.DocumentExecuteService lookUp() throws SecurityException {
	try {
	    DocumentExecuteServiceServiceLocator locator = new DocumentExecuteServiceServiceLocator();
	    it.eng.spagobi.services.execute.stub.DocumentExecuteService service=null;
	    if (serviceUrl!=null ){
		    service = locator.getDocumentExecuteService(serviceUrl);		
	    }else {
		    service = locator.getDocumentExecuteService();		
	    }
	    return service;
	} catch (ServiceException e) {
		logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
	    throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
	}
    }
    
    /**
     * Return the  image of a Chart
     * @param documentLabel
     * @param parameters
     * @return
     */
    public byte[] executeChart(String documentLabel,HashMap parameters){
	logger.debug("IN.documentLabel="+documentLabel);
	if (documentLabel==null || documentLabel.length()==0){
	    logger.error("documentLabel is NULL");
	    return null;
	}	
	try {
	    return lookUp().executeChart(readTicket(), userId,documentLabel,parameters);
	} catch (Exception e) {
	    logger.error("Error during Service LookUp",e);
	}finally{
	    logger.debug("OUT");
	}
	return null;	
    }
    
      
    public java.lang.String getKpiValueXML(java.lang.Integer kpiValueID) {
    	logger.debug("IN.kpiValueID="+kpiValueID);
    	if (kpiValueID==null){
    	    logger.error("kpiValueID is NULL");
    	    return null;
    	}	
    	try {
        return lookUp().getKpiValueXML(readTicket(), userId, kpiValueID);
    	} catch (Exception e) {
    	    logger.error("Error during Service LookUp",e);
    	}finally{
    	    logger.debug("OUT");
    	}
    	return null;
      }
    
}
