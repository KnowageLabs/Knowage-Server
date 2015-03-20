/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.tools.dataset.common.association;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AssociationJSONSerializer {
	
	public List<Association> deserialize(JSONArray a) throws JSONException {
		List<Association> associations = new ArrayList<Association>();
		for(int i = 0; i <a.length(); i++) {
			JSONObject o = a.getJSONObject(i);
			associations.add( deserialize(o) );
		}
		return associations;
	}
	
	public Association deserialize(JSONObject o) throws JSONException {
		String id = o.getString("id");
		String description = o.getString("description");
		Association association = new Association(id, description);
		association.addFields( deserializeFields( o.getJSONArray("fields") ) );
		return association;
	}
	
	public List<Association.Field> deserializeFields(JSONArray a) throws JSONException {
		List<Association.Field> fields = new ArrayList<Association.Field>();
		for(int i = 0; i < a.length(); i++) {
			JSONObject o = a.getJSONObject(i);
			String name = o.getString("column");
			String dataset = o.getString("store");
			Association.Field field = new Association.Field(dataset, name);
			fields.add(field);
		}
		return fields;
	}
	
	public JSONArray serialize(Collection<Association> associations) throws JSONException {
		JSONArray a = new JSONArray();
		for(Association association : associations) {
			JSONObject o = serialize(association);
			a.put(o);
		}
		return a;
	}
	
	public JSONObject serialize(Association association) throws JSONException {
		JSONObject o = new JSONObject();
		o.put("id", association.getId());
		o.put("description", association.getDescription());
		o.put("fields", serializeFields(association.getFields()));
		return o;
	}
	
	public JSONArray serializeFields(Collection<Association.Field> fields) throws JSONException {
		JSONArray a = new JSONArray();
		for(Association.Field field : fields) {
			JSONObject o = new JSONObject();
			o.put("column", field.getFieldName());
			o.put("store", field.getDataSetLabel());
			a.put(o);
		}
		return a;
	}
}
