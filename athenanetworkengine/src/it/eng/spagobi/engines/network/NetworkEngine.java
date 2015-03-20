/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author ...
 */
public class NetworkEngine {
	
	private static NetworkEngineConfig engineConfig;
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(NetworkEngine.class);
	
    // init engine
    static {
    	engineConfig = NetworkEngineConfig.getInstance();
    }
    
    public static NetworkEngineConfig getConfig() {
    	return engineConfig;
    }
    
	/**
	 * Creates the instance.
	 * 
	 * @param template the template
	 * @param env the env
	 * 
	 * @return the geo report engine instance
	 */
	public static NetworkEngineInstance createInstance(Object template, Map env) throws NetworkEngineException{
		NetworkEngineInstance worksheetEngineInstance = null;
		logger.debug("IN");
		worksheetEngineInstance = new NetworkEngineInstance(template, env);
		logger.debug("OUT");
		return worksheetEngineInstance;
	}


}
