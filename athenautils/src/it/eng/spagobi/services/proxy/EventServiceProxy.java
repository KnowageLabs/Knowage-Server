/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.proxy;

import it.eng.spagobi.services.event.stub.EventServiceServiceLocator;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * 
 * proxy of Event service
 *
 */
public final class EventServiceProxy extends AbstractServiceProxy{
	
	static private final String SERVICE_NAME = "Event Service";

	public static final String START_EVENT_ID = "startEventId";
	public static final String BIOBJECT_ID = "biobjectId";
	public static final String USER = "user";
	public static final String EVENT_TYPE = "event-type";
	public static final String DOCUMENT_EXECUTION_START = "biobj-start-execution";
	public static final String DOCUMENT_EXECUTION_END = "biobj-end-execution";
	
    static private Logger logger = Logger.getLogger(EventServiceProxy.class);

    /**
     * use it only in engine context.
     * 
     * @param user user Id
     * @param session HttpSession
     */
    public EventServiceProxy(String user,HttpSession session) {
	super( user,session);
    }
    
    private EventServiceProxy() {
	super();
    }    

    private it.eng.spagobi.services.event.stub.EventService lookUp() throws SecurityException {
	try {
	    EventServiceServiceLocator locator = new EventServiceServiceLocator();
	    it.eng.spagobi.services.event.stub.EventService service = null;
	    if (serviceUrl!=null ){
		    service = locator.getEventService(serviceUrl);		
	    }else {
		    service = locator.getEventService();		
	    }
	    return service;
	} catch (ServiceException e) {
		logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
	    throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
	}
    }
    
    /**
     * Fire event.
     * 
     * @param description String
     * @param parameters String
     * @param rolesHandler String
     * @param presentationHandler String
     * 
     * @return String
     */
    public String fireEvent(String description,String parameters,String rolesHandler,String presentationHandler){
	logger.debug("IN");
	try {
	    return lookUp().fireEvent( readTicket(), userId, description, parameters, rolesHandler, presentationHandler);
	} catch (Exception e) {
	    logger.error("Error during service execution",e);

	}finally{
	    logger.debug("OUT");
	}
	return null;	
    }
    
    /**
     * Fire event.
     * 
     * @param description String
     * @param parameters Map
     * @param rolesHandler String
     * @param presentationHandler String
     * 
     * @return String
     */
    public String fireEvent(String description,Map parameters,String rolesHandler,String presentationHandler){
	logger.debug("IN");
	try {
	    return lookUp().fireEvent( readTicket(), userId, description, getParamsStr(parameters), rolesHandler, presentationHandler);
	} catch (Exception e) {
	    logger.error("Error during service execution",e);

	}finally{
	    logger.debug("OUT");
	}
	return null;	
    }    
    
    
    
	private String getParamsStr(Map params) {
		StringBuffer buffer = new StringBuffer();
		Iterator it = params.keySet().iterator();
		boolean isFirstParameter = true;
		while(it.hasNext()) {
			String pname = (String)it.next();
			String pvalue = (String)params.get(pname);
			if(!isFirstParameter) buffer.append("&");
			else isFirstParameter = false;
			buffer.append(pname + "=" + pvalue);
		}
		return buffer.toString();
	}
	

}
