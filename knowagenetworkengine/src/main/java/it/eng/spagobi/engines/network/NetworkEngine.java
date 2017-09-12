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
