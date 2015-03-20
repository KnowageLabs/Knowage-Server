/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.deserializer;

import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.tools.dataset.crosstab.CrosstabDefinition;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JSONDeserializer implements Deserializer {
	
	Map<Class, Deserializer> mappings;
	
	public JSONDeserializer() {
		mappings = new HashMap();
		mappings.put( Engine.class, new EngineJSONDeserializer() );
		mappings.put( CrosstabDefinition.class, new CrosstabJSONDeserializer() );

	}

	public Object deserialize(Object o, Class clazz) throws DeserializationException {
		Object result = null;	
		
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
			
			Deserializer deserializer = mappings.get(clazz);
			if(deserializer == null) {
				throw new DeserializationException("Impossible to deserialize to an object of type [" + clazz.getName() +"]");
			}
			result = deserializer.deserialize(o, clazz);		
			

		} catch (Throwable t) {
			throw new DeserializationException("An error occurred while deserializing object: " + o, t);
		} finally {
			
		}
		
		return result;	
	}


}
