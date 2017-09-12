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

package it.eng.spagobi.behaviouralmodel.lov.service;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class GridMetadataContainerJSONSerializer extends JsonSerializer<GridMetadataContainer> {

	@Override
	public void serialize(GridMetadataContainer value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException{
		String key;
		jgen.writeStartObject();
			//METADATA
			jgen.writeObjectFieldStart("metaData");
				jgen.writeStringField("totalProperty", value.getTotalProperty());
				jgen.writeStringField("root", value.getRootPropery());

				//OTHER PROPERTIES
				Iterator<String> iterator = value.getMetaData().keySet().iterator();
				while(iterator.hasNext()){
					key = iterator.next();
					jgen.writeObjectField(key,value.getMetaData().get(key));
				}
			
			
				//FIELDS
				List<Object> iteratorFields = value.getFields();
				jgen.writeArrayFieldStart("fields");
					for (Iterator<Object>  iterator2 = iteratorFields.iterator(); iterator2.hasNext();) {
						Object object = iterator2.next();
						if(object instanceof String) {
							jgen.writeString((String)object);
						}else{
							jgen.writeObject(object);
						}
					}
				jgen.writeEndArray();
			jgen.writeEndObject();
		
			//VALUES
			List<Map<String,String>> values = value.getValues();
			jgen.writeArrayFieldStart(value.getRootPropery());
				for (Iterator<Map<String,String>> iterator3 = values.iterator(); iterator3.hasNext();) {
					Map<String,String> object =  (Map<String,String>)iterator3.next();
					Set<String> keys = object.keySet();
					if(keys!=null){
						Iterator<String> iter = keys.iterator();
						Map<String,String> objectescaped = new HashMap<String, String>();
						while(iter.hasNext()){
							String mapKey = iter.next();
							String mapValue = object.get(mapKey);
//							String keyEscaped = StringEscapeUtils.escapeJavaScript(mapKey);
//							String valueEscaped = StringEscapeUtils.escapeJavaScript(mapValue);
//							objectescaped.put(keyEscaped, valueEscaped);
							objectescaped.put(mapKey, mapValue);
						}
						jgen.writeObject(objectescaped);
					}

				}
			jgen.writeEndArray();
			
			//TOTAL
			jgen.writeStringField(value.getTotalProperty(), ""+value.getResults());
		jgen.writeEndObject();
	}
	
	
	
	
	public static JSONObject serialize(GridMetadataContainer value) throws JSONException{
		String key;
		JSONObject toReturn= new JSONObject();

		//METADATA
		JSONObject metaData= new JSONObject();
		metaData.put("totalProperty", value.getTotalProperty());
		metaData.put("root", value.getRootPropery());

		//OTHER PROPERTIES
		Iterator<String> iterator = value.getMetaData().keySet().iterator();
		while(iterator.hasNext()){
			key = iterator.next();
			metaData.put(key,value.getMetaData().get(key));
		}


		//FIELDS
		JSONArray fields = new JSONArray();
		List<Object> iteratorFields = value.getFields();

		for (Iterator<Object>  iterator2 = iteratorFields.iterator(); iterator2.hasNext();) {
			Object object = iterator2.next();
			if(object instanceof String) {
				fields.put((String)object);
			}else if(object instanceof Map) {
				JSONObject field = new JSONObject();
				iterator = ((Map)object).keySet().iterator();
				while(iterator.hasNext()){
					key = iterator.next();
					field.put(key,((Map)object).get(key));
				}
				fields.put(field);					
			}
		}
		metaData.put("fields",fields);
		toReturn.put("metaData",metaData);
		//VALUES
		List<Map<String,String>> values = value.getValues();
		JSONArray root = new JSONArray();

		for (Iterator<Map<String,String>> iterator3 = values.iterator(); iterator3.hasNext();) {
			Map<String,String> object =  (Map<String,String>)iterator3.next();
			JSONObject field = new JSONObject();
			iterator = (object).keySet().iterator();
			while(iterator.hasNext()){
				key = iterator.next();
				field.put(key,(object).get(key));
			}
			root.put(field);
		}
		toReturn.put(value.getRootPropery(), root);	


		//TOTAL
		toReturn.put(value.getTotalProperty(), ""+value.getResults());
		return toReturn;
	}
	
	
}
