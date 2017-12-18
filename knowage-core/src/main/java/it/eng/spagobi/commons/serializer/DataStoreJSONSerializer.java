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

import java.util.Iterator;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class DataStoreJSONSerializer  implements Serializer {

	public static final String TOTAL_PROPERTY = "results";
	public static final String ROOT = "rows";
	
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		JSONObject metadata;
		IField field;
		JSONArray fieldsMetaDataJSON;		
		JSONObject fieldMetaDataJSON;
		IRecord record;
		JSONObject recordJSON;
		int recNo;
		IDataStore dataStore;
				
		JSONArray recordsJSON;
		int resultNumber;
		Object propertyRawValue;
		
		Assert.assertNotNull(o, "Object to be serialized connot be null");
		if( !(o instanceof IDataStore) ) {
			throw new SerializationException("DataStoreJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		
		
		try {
			dataStore = (IDataStore)o;
			result = new JSONObject();
			
			metadata = new JSONObject();
				
			metadata.put("totalProperty", TOTAL_PROPERTY);
			metadata.put("root", ROOT);
			metadata.put("id", "id");
			result.put("metaData", metadata);
			
			propertyRawValue = dataStore.getMetaData().getProperty("resultNumber");
			
			
			recordsJSON = new JSONArray();
			result.put(ROOT, recordsJSON);
		
			// field's meta
			fieldsMetaDataJSON = new JSONArray();
			//fieldsMetaDataJSON.put("recNo");
			for(int i = 0; i < dataStore.getMetaData().getFieldCount(); i++) {
				IFieldMetaData fieldMetaData = dataStore.getMetaData().getFieldMeta(i);
				
				Object visibleProp = fieldMetaData.getProperty("visible");
				if(visibleProp != null 
						&& (visibleProp instanceof Boolean) 
						&& ((Boolean)visibleProp).booleanValue() == false) {
					continue;
				}
				
				fieldMetaDataJSON = new JSONObject();
				fieldMetaDataJSON.put("header", fieldMetaData.getName());
				fieldMetaDataJSON.put("name", "column-" + (i+1));						
				fieldMetaDataJSON.put("dataIndex", "column-" + (i+1));
				
				fieldsMetaDataJSON.put(fieldMetaDataJSON);		
			}
			metadata.put("fields", fieldsMetaDataJSON);
			
			// records
			recNo = 0;
			Iterator records = dataStore.iterator();
			while(records.hasNext()) {
				record = (IRecord)records.next();
				recordJSON = new JSONObject();
				recordJSON.put("id", ++recNo);
				
				for(int i = 0; i < metadata.getJSONArray("fields").length(); i++) {
					field = record.getFieldAt(i);
					recordJSON.put("column-" + (i+1), field.getValue().toString());
				}
				
				recordsJSON.put(recordJSON);
			}
			
			if(propertyRawValue == null) {
				propertyRawValue = new Integer(recNo);
			}
			
			Assert.assertNotNull(propertyRawValue, "DataStore property [resultNumber] cannot be null");
			Assert.assertTrue(propertyRawValue instanceof Integer, "DataStore property [resultNumber] must be of type [Integer]");
			resultNumber = ((Integer)propertyRawValue).intValue();
			Assert.assertTrue(resultNumber >= 0, "DataStore property [resultNumber] cannot be equal to [" + resultNumber + "]. It must be greater or equal to zero");	
			result.put(TOTAL_PROPERTY, resultNumber);
			
		} catch(Throwable t) {
			throw new SerializationException("An unpredicted error occurred while serializing dataStore", t);
		} finally {
			
		}
		
		return result;
	}

}
