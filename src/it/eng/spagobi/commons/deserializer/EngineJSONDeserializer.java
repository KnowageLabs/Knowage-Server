/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.deserializer;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class EngineJSONDeserializer implements Deserializer {
	
	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String NAME = "name";
	
	public static final String DESCRIPTION = "description";
	public static final String DOCUMENT_TYPE = "documentType";
	public static final String ENGINE_TYPE = "engineType";	
	
	public static final String USE_DATASET = "useDataSet";
	public static final String USE_DATASOURCE = "useDataSource";
	
	public static final String CLASS = "engine_class";
	public static final String URL = "url";
	public static final String SECONDARY_URL = "secondaryUrl";

	public static final String DRIVER = "driver";
	
	

	private static Logger logger = Logger.getLogger(EngineJSONDeserializer.class);
	  
	public Object deserialize(Object o, Class clazz) throws DeserializationException {
		
		Engine engine;
		
		Integer id = null;
		Integer criptable = null; 
		String name = "";
		String description = "";
		String url = "";
		String secondaryUrl = "";
		String dirUpload = "";
		String dirUsable = "";
		String driverName = "";	
		String label = "";
		String className = "";
		Integer biobjTypeId = null;
		Integer engineTypeId = null;
		boolean useDataSource = false;
		boolean useDataSet = false;
		
		
		
		
		
		logger.debug("IN");
		
		engine = new Engine();		
		
		try {
			Assert.assertNotNull(o, "Input parameter [" + o + "] cannot be null");
			Assert.assertNotNull(o, "Input parameter [" + clazz + "] cannot be null");
			
			JSONObject json = null;
			if(o instanceof JSONObject) {
				json = (JSONObject)o;
			} else if (o instanceof String) {
				json = new JSONObject((String)o);
			} else {
				throw new DeserializationException("Impossible to deserialize from an object of type [" + o.getClass().getName() +"]");
			}
			if (json.getString(ID).length() > 0){
				id = Integer.parseInt(json.getString(ID));
			}
			label = json.getString(LABEL);
			name = json.getString(NAME);
			description = json.getString(DESCRIPTION);
			if (json.getString(DOCUMENT_TYPE).length() > 0){
				biobjTypeId = Integer.parseInt(json.getString(DOCUMENT_TYPE));
			}
			if (json.getString(ENGINE_TYPE).length() > 0){
				engineTypeId = Integer.parseInt(json.getString(ENGINE_TYPE));
			}
			useDataSet = Boolean.parseBoolean(json.getString(USE_DATASET));
			useDataSource =  Boolean.parseBoolean(json.getString(USE_DATASOURCE));
			className = json.getString(CLASS);
			url = json.getString(URL);
			secondaryUrl = json.getString(SECONDARY_URL);
			driverName =  json.getString(DRIVER);
			
			engine.setId(id);
			engine.setLabel(label);
			engine.setName(name);
			engine.setDescription(description);
			engine.setBiobjTypeId(biobjTypeId);
			engine.setEngineTypeId(engineTypeId);
			engine.setUseDataSet(useDataSet);
			engine.setUseDataSource(useDataSource);
			engine.setClassName(className);
			engine.setUrl(url);
			engine.setSecondaryUrl(secondaryUrl);
			engine.setDriverName(driverName);
			
			//TODO: verify other options not set
            engine.setCriptable(new Integer(0));
			engine.setDirUpload(dirUpload);
			engine.setDirUsable(dirUsable);


		} catch (Throwable t) {
			throw new DeserializationException("An error occurred while deserializing object: " + o, t);
		} finally {
			logger.debug("OUT");
		}
		
		return engine;
	}
	


}
