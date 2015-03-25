/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.sdk.engines.impl;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.sdk.AbstractSDKService;
import it.eng.spagobi.sdk.engines.EnginesService;
import it.eng.spagobi.sdk.engines.bo.SDKEngine;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.sdk.utilities.SDKObjectsConverter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class EnginesServiceImpl extends AbstractSDKService implements EnginesService {

	static private Logger logger = Logger.getLogger(EnginesServiceImpl.class);
	
	public SDKEngine getEngine(Integer engineId) throws NotAllowedOperationException {
		SDKEngine toReturn = null;
		logger.debug("IN: engineId in input = " + engineId);
		
		this.setTenant();
		
        try {
            super.checkUserPermissionForFunctionality(SpagoBIConstants.READ_ENGINES_MANAGEMENT, "User cannot see engines congifuration.");
            if (engineId == null) {
            	logger.warn("Engine identifier in input is null!");
            	return null;
            }
        	Engine engine = DAOFactory.getEngineDAO().loadEngineByID(engineId);
        	if (engine == null) {
        		logger.warn("Engine with identifier [" + engineId + "] not existing.");
        		return null;
        	}
        	toReturn = new SDKObjectsConverter().fromEngineToSDKEngine(engine);
        } catch(NotAllowedOperationException e) {
        	throw e;
        } catch(Exception e) {
            logger.error("Error while retrieving SDKEngine", e);
            logger.debug("Returning null");
            return null;
        } finally {
        	this.unsetTenant();
        	logger.debug("OUT");
        }
        return toReturn;
	}

	public SDKEngine[] getEngines() throws NotAllowedOperationException {
		SDKEngine[] toReturn = null;
        logger.debug("IN");
        
        this.setTenant();
        
        try {
        	super.checkUserPermissionForFunctionality(SpagoBIConstants.READ_ENGINES_MANAGEMENT, "User cannot see engines congifuration.");
        	List enginesList = DAOFactory.getEngineDAO().loadAllEngines();
        	List sdkEnginesList = new ArrayList();
    		if (enginesList != null && enginesList.size() > 0) {
                for (Iterator it = enginesList.iterator(); it.hasNext();) {
                    Engine engine = (Engine) it.next();
                    SDKEngine sdkEngine = new SDKObjectsConverter().fromEngineToSDKEngine(engine);
                    sdkEnginesList.add(sdkEngine);
                }
    		}
    		toReturn = new SDKEngine[sdkEnginesList.size()];
    		toReturn = (SDKEngine[]) sdkEnginesList.toArray(toReturn);
        } catch(NotAllowedOperationException e) {
        	throw e;
        } catch(Exception e) {
            logger.error("Error while retrieving SDKEngine list", e);
            logger.debug("Returning null");
            return null;
        } finally {
        	this.unsetTenant();
        	logger.debug("OUT");
        }
        return toReturn;
	}

}
