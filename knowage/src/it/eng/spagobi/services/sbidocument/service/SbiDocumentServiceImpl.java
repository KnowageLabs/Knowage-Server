/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.sbidocument.service;

import it.eng.spagobi.services.common.AbstractServiceImpl;
import it.eng.spagobi.services.sbidocument.SbiDocumentService;
import it.eng.spagobi.services.sbidocument.bo.SpagobiAnalyticalDriver;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * @author Andrea Gioia
 */
public class SbiDocumentServiceImpl extends AbstractServiceImpl  implements SbiDocumentService {
	 
	private SbiDocumentSupplier supplier = new SbiDocumentSupplier();
	    
	static private Logger logger = Logger.getLogger(SbiDocumentServiceImpl.class);

	/**
     * Instantiates a new data source service impl.
     */
    public SbiDocumentServiceImpl(){
    	super();
    }
    
    public SpagobiAnalyticalDriver[] getDocumentAnalyticalDrivers(String token, String user, Integer id, String language, String country){
    	logger.debug("IN");
    	Monitor monitor = MonitorFactory.start("spagobi.service.sbidocument.getDocumentParameters");
    	try {
    	    validateTicket(token, user);
    	    this.setTenantByUserId(user);
    	    return supplier.getDocumentAnalyticalDrivers(id, language, country);
    	} catch (SecurityException e) {
    	    logger.error("SecurityException", e);
    	    return null;
    	} finally {
    		this.unsetTenant();
    	    monitor.stop();
    	    logger.debug("OUT");
    	}	
    }
    
    public String getDocumentAnalyticalDriversJSON(String token, String user, Integer id, String language, String country){
    	logger.debug("IN");
    	Monitor monitor = MonitorFactory.start("spagobi.service.sbidocument.getDocumentParametersJSON");
    	try {
    	    validateTicket(token, user);
    	    this.setTenantByUserId(user);
    	    return supplier.getDocumentAnalyticalDriversJSON(id, language, country);
    	} catch (SecurityException e) {
    	    logger.error("SecurityException", e);
    	    return null;
    	} finally {
    		this.unsetTenant();
    	    monitor.stop();
    	    logger.debug("OUT");
    	}	
    }
    
        

}
