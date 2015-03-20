/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.georeport.services;

import it.eng.spagobi.engines.georeport.GeoReportEngineConfig;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.BaseServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.service.AbstractBaseServlet;

import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;


/**
 * This service return the metadata associated to the hierarchy level passed as 
 * input...
 * TODO describe more
 * TODO move to REST?
 * 
 * 
 * @authors Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetHierachyLevelMetaAction extends AbstractBaseServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String LEVEL_NAME = "levelName";
	
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(GetHierachyLevelMetaAction.class);
    
    
	public void doService( BaseServletIOManager servletIOManager ) throws SpagoBIEngineException {
	
		String levelName;
		
		logger.debug("IN");
		
		try {
			
			levelName = servletIOManager.getParameterAsString(LEVEL_NAME); 
			logger.debug("Parameter [" + LEVEL_NAME + "] is equal to [" + levelName + "]");
			Assert.assertNotNull(levelName, "Input parameter [" + LEVEL_NAME + "] cannot be null");
					    
			Properties levelProps = GeoReportEngineConfig.getInstance().getLevelByName(levelName);
			JSONObject levelMeta = new JSONObject();
			Iterator it = levelProps.keySet().iterator();
			while(it.hasNext()) {
				String pName = (String)it.next();
				String pValue = levelProps.getProperty(pName);
				levelMeta.put(pName, pValue);
			}
			
			if(levelProps.get("layer_url") != null) {
				levelMeta.put("featureSourceType", "wfs");
				levelMeta.put("featureSource", levelProps.get("layer_url"));
			} else {
				levelMeta.put("featureSourceType", "file");
				levelMeta.put("featureSource", levelProps.get("layer_file"));
			}

			servletIOManager.tryToWriteBackToClient(levelMeta.toString());
			
		} catch(Throwable t) {
			t.printStackTrace();
		} finally {
			logger.debug("OUT");
		}
	}

	public void handleException(BaseServletIOManager servletIOManager,
			Throwable t) {
		t.printStackTrace();		
	}
}

