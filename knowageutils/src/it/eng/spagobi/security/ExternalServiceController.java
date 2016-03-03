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
package it.eng.spagobi.security;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Check services (actions) that can be invoked without user authorization in SpagoBI 
 * IMPORTANT: this services can be invoked by anyone outside SpagoBI so use them with caution
 * 
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class ExternalServiceController {
	
    private static ExternalServiceController instance = null;

    static private Logger logger = Logger.getLogger(ExternalServiceController.class);
    
    private HashMap mapRestServices = null;


    public ExternalServiceController(){
    	ConfigSingleton config = ConfigSingleton.getInstance();
    	mapRestServices = new HashMap();
    	initRestServicesMap(config);

    }
    
    public void initRestServicesMap(ConfigSingleton config){
    	mapRestServices = new HashMap();
    	List actions =config.getAttributeAsList("BUSINESS_MAP.MAP_EXTERNAL_REST_SERVICES");
    	Iterator it = actions.iterator();
    	while (it.hasNext()) {
    	    SourceBean mapActions = (SourceBean) it.next();
    	    List actionsList =mapActions.getAttributeAsList("MAP_EXTERNAL_REST_SERVICE");
    	    Iterator actionListIt = actionsList.iterator();
    	    while (actionListIt.hasNext()) {
    		SourceBean mapAction = (SourceBean) actionListIt.next();
        	    	String serviceName = (String) mapAction.getAttribute("serviceUrl");
        	    	mapRestServices.put(serviceName.toUpperCase(), null);
    	    }
    	}
  
    }
    
    public boolean isExternalService(String serviceName){
    	serviceName = serviceName.toUpperCase();
    	if (mapRestServices.containsKey(serviceName)){
    		return true;
    	}
    	return false;
    }
    
    public static ExternalServiceController getInstance() {
    	if (instance == null) {
    		synchronized (ExternalServiceController.class) {
    			if (instance == null) {
    				try {
    					instance = new ExternalServiceController();
    				} catch (Exception ex) {
    					logger.error("Exception", ex);
    				}
    			}
    		}
    	}
    	return instance;
    }

}
