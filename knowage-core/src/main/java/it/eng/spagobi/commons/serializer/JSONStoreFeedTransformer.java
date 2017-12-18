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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class JSONStoreFeedTransformer {
	
	private static JSONStoreFeedTransformer instance;
	
	public static JSONStoreFeedTransformer getInstance() {
		if(instance == null) {
			instance = new JSONStoreFeedTransformer();
		}
		
		return instance;
	}
	
	private JSONStoreFeedTransformer() {}
	
	
	public Object transform(Object jsonData, 
			String valueField,
			String displayField,
			String descriptionField,
			String[] fields,
			Integer results) throws SerializationException {
		
		JSONObject result;
	
		JSONObject jsonObject;
		JSONArray jsonArray;
		
		
		if(jsonData instanceof JSONObject) {
			jsonArray = new JSONArray();
			jsonArray.put(jsonData);
		} else {
			jsonArray = (JSONArray)jsonData;
		}
		
		result = new JSONObject();
		try {
			JSONObject meta = new JSONObject();
			meta.put("root", "root");
			meta.put("totalProperty", "results");
			meta.put("valueField", valueField);
			meta.put("displayField", displayField);
			meta.put("descriptionField", descriptionField);
			JSONArray fieldsJSON = new JSONArray();
			fieldsJSON.put("recNo");
			
			boolean isValueFieldVisible = false;
			JSONObject field;
			for(int i = 0; i < fields.length; i++) {
				field = new JSONObject();
				if(fields[i].equalsIgnoreCase(valueField)) {
					isValueFieldVisible = true;
				}
				field.put("name", fields[i]);
				field.put("header", fields[i]);
				fieldsJSON.put(field);
			}
			if(!isValueFieldVisible){
				field = new JSONObject();
				field.put("name", valueField);
				field.put("header", valueField);
				field.put("hidden", true);
				fieldsJSON.put(field);
			}
			fieldsJSON.put("recCk");
			meta.put("fields", fieldsJSON);
			result.put("metaData", meta);
			result.put("root", jsonArray);
			result.put("results", results.intValue());
		} catch (JSONException e) {
			throw new SerializationException("An error occurred while transforming object: " + jsonData, e);
		}
		
		return result;
	}
}
