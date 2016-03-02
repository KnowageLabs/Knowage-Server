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
