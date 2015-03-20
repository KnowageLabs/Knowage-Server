/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query.serializer.json;

import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.tools.dataset.bo.DataSetVariable;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.utilities.assertion.Assert;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * @deprecated Use JSONDataWriter instead
 */
public class LookupStoreJSONSerializer {
	
	public static final String TOTAL_PROPERTY = "results";
	public static final String ROOT = "rows";
	
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "dd/MM/yyyy hh:mm:ss" );

	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(LookupStoreJSONSerializer.class);
	
	public Object serialize(IDataStore dataStore) throws SerializationException {
		JSONObject  result = null;
		JSONObject metadata;
		IField field;
		JSONArray fieldsMetaDataJSON;		
		JSONObject fieldMetaDataJSON;
		IRecord record;
		JSONObject recordJSON;
		int recNo;
		String valueField;
		String displayField;
		String descriptionField;
		
		JSONArray recordsJSON;
		int resultNumber;
		Object propertyRawValue;
		
		Assert.assertNotNull(dataStore, "Object to be serialized connot be null");
		
		try {
			IMetaData dataStoreMeta = dataStore.getMetaData();
			valueField = "Values";
			displayField = "Values";
			descriptionField = "Values";
			
			result = new JSONObject();
	
			metadata = new JSONObject();
			
			metadata.put("root", ROOT);
			metadata.put("totalProperty", TOTAL_PROPERTY);
			metadata.put("valueField", valueField);
			metadata.put("displayField", displayField);
			metadata.put("descriptionField", descriptionField);			
			result.put("metaData", metadata);
			
			propertyRawValue = dataStore.getMetaData().getProperty("resultNumber");
			Assert.assertNotNull(propertyRawValue, "DataStore property [resultNumber] cannot be null");
			Assert.assertTrue(propertyRawValue instanceof Integer, "DataStore property [resultNumber] must be of type [Integer]");
			resultNumber = ((Integer)propertyRawValue).intValue();
			Assert.assertTrue(resultNumber >= 0, "DataStore property [resultNumber] cannot be equal to [" + resultNumber + "]. It must be greater or equal to zero");	
			result.put(TOTAL_PROPERTY, resultNumber);
			
			recordsJSON = new JSONArray();
			result.put(ROOT, recordsJSON);
		
			// field's meta
			fieldsMetaDataJSON = new JSONArray();
			fieldsMetaDataJSON.put("recNo"); // counting column
			for(int i = 0; i < dataStore.getMetaData().getFieldCount(); i++) {
				IFieldMetaData fieldMetaData = dataStore.getMetaData().getFieldMeta(i);
				
				propertyRawValue = fieldMetaData.getProperty("visible");
				if(propertyRawValue != null 
						&& (propertyRawValue instanceof Boolean) 
						&& ((Boolean)propertyRawValue).booleanValue() == false) {
					continue;
				}
				
				fieldMetaDataJSON = new JSONObject();
				fieldMetaDataJSON.put("header", "Values");
				fieldMetaDataJSON.put("name", "Values" );						
				fieldMetaDataJSON.put("dataIndex", "Values");
											
							
				Class clazz = fieldMetaData.getType();
				logger.debug("Column [" + (i+1) + "] class is equal to [" + clazz.getName() + "]");
				if( Number.class.isAssignableFrom(clazz) ) {
					//BigInteger, Integer, Long, Short, Byte
					if(Integer.class.isAssignableFrom(clazz) 
				       || BigInteger.class.isAssignableFrom(clazz) 
					   || Long.class.isAssignableFrom(clazz) 
					   || Short.class.isAssignableFrom(clazz)
					   || Byte.class.isAssignableFrom(clazz)) {
						logger.debug("Column [" + (i+1) + "] type is equal to [" + "INTEGER" + "]");
						fieldMetaDataJSON.put("type", "int");
					} else {
						logger.debug("Column [" + (i+1) + "] type is equal to [" + "FLOAT" + "]");
						fieldMetaDataJSON.put("type", "float");
					}
					
				} else if( String.class.isAssignableFrom(clazz) ) {
					logger.debug("Column [" + (i+1) + "] type is equal to [" + "STRING" + "]");
					fieldMetaDataJSON.put("type", "string");
				} else if( Date.class.isAssignableFrom(clazz) ) {
					logger.debug("Column [" + (i+1) + "] type is equal to [" + "DATE" + "]");
					fieldMetaDataJSON.put("type", "date");
					fieldMetaDataJSON.put("dateFormat", "d/m/Y H:i:s");
				} else if( Boolean.class.isAssignableFrom(clazz) ) {
					logger.debug("Column [" + (i+1) + "] type is equal to [" + "BOOLEAN" + "]");
					fieldMetaDataJSON.put("type", "boolean");
				} else {
					logger.warn("Column [" + (i+1) + "] type is equal to [" + "???" + "]");
					fieldMetaDataJSON.put("type", "string");
				}
				
				Boolean calculated = (Boolean)fieldMetaData.getProperty("calculated");
				if(calculated != null && calculated.booleanValue() == true) {
					DataSetVariable variable =  (DataSetVariable)fieldMetaData.getProperty("variable");
					if(variable.getType().equalsIgnoreCase(DataSetVariable.HTML)) {
						fieldMetaDataJSON.put("type", "auto");
						fieldMetaDataJSON.remove("type");
						fieldMetaDataJSON.put("subtype", "html");
					}				
				}
				fieldsMetaDataJSON.put(fieldMetaDataJSON);
			}
			
//			fieldsMetaDataJSON.put("recCk");
			metadata.put("fields", fieldsMetaDataJSON);
			
			// records
			recNo = 0;
			Iterator records = dataStore.iterator();
			while(records.hasNext()) {
				record = (IRecord)records.next();
				recordJSON = new JSONObject();
				
				for(int i = 0; i < dataStore.getMetaData().getFieldCount(); i++) {
					IFieldMetaData fieldMetaData = dataStore.getMetaData().getFieldMeta(i);
					
					propertyRawValue = fieldMetaData.getProperty("visible");
					if(propertyRawValue != null 
							&& (propertyRawValue instanceof Boolean) 
							&& ((Boolean)propertyRawValue).booleanValue() == false) {
						continue;
					}
					String key = fieldMetaData.getName();
					field = record.getFieldAt( dataStore.getMetaData().getFieldIndex( key ) );
		
					String fieldValue = "";
					if(field.getValue() != null && !field.getValue().equals("")) {
						if(Date.class.isAssignableFrom(fieldMetaData.getType())) {
							fieldValue =  DATE_FORMATTER.format(  field.getValue() );
						} else {
							fieldValue =  field.getValue().toString();
						}
					}
					
					
					recordJSON.put("Values", fieldValue);
				}
				recordsJSON.put(recordJSON);
			}
			
		
			
			
		} catch(Throwable t) {
			throw new SerializationException("An unpredicted error occurred while serializing dataStore", t);
		} finally {
			
		}
		
		return result;
	}
}
