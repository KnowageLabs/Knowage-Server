/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.georeport;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GeoReportEngine {
	
	private static GeoReportEngineConfig engineConfig;
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(GeoReportEngine.class);
	
    // init engine
    static {
    	engineConfig = GeoReportEngineConfig.getInstance();
    }
    
    public static GeoReportEngineConfig getConfig() {
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
	public static GeoReportEngineInstance createInstance(String template, Map env) {
		GeoReportEngineInstance georeportEngineInstance = null;
		logger.debug("IN");
		georeportEngineInstance = new GeoReportEngineInstance(template, env);
		logger.debug("OUT");
		return georeportEngineInstance;	
	}
}
