/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.common;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;

import org.apache.log4j.Logger;

/**
 * Factory Class
 */
public abstract class SsoServiceFactory {

	static private Logger logger = Logger.getLogger(SsoServiceFactory.class);
	
    private SsoServiceFactory(){
	
    }
    
    /**
     * Creates the proxy service.
     * 
     * @return IProxyService
     */
    public static final SsoServiceInterface createProxyService(){
    	
    	logger.debug("IN");
    	SsoServiceInterface daoObject = null;
		try{
			String integrationClass=EnginConf.getInstance().getSpagoBiSsoClass();
			
			if (integrationClass==null){
				// now we are in the core
				integrationClass = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.INTEGRATION_CLASS_JNDI"));
			}
			daoObject = (SsoServiceInterface)Class.forName(integrationClass).newInstance();
			logger.debug(" Instatiate successfully:"+integrationClass);
		}catch(Exception e){
			logger.error( "Error occurred", e);
		}
		return daoObject;
    }
    

}
