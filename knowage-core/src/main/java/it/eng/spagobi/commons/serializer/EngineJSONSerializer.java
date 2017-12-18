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
package it.eng.spagobi.commons.serializer;

import java.util.Locale;

import org.json.JSONObject;

import it.eng.spagobi.engines.config.bo.Engine;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class EngineJSONSerializer implements Serializer {
	
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
	public static final String DRIVER = "driver";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof Engine) ) {
			throw new SerializationException("EngineJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			Engine engine = (Engine)o;
			result = new JSONObject();
			
			result.put(ID, engine.getId() );
			result.put(LABEL, engine.getLabel() );
			result.put(NAME, engine.getName() );
			
			result.put(DESCRIPTION, engine.getDescription() );
			result.put(DOCUMENT_TYPE, engine.getBiobjTypeId() );			
			result.put(ENGINE_TYPE, engine.getEngineTypeId() );
			
			result.put(USE_DATASET, engine.getUseDataSet() );
			result.put(USE_DATASOURCE, engine.getUseDataSource() );
			
			result.put(CLASS, engine.getClassName() );
			result.put(URL, engine.getUrl() );
			result.put(DRIVER, engine.getDriverName() );	
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}
	
	
}
