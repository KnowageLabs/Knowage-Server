/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.serializer.json;

import it.eng.qbe.model.structure.ModelCalculatedField.Slot;
import it.eng.qbe.serializer.IDeserializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.engines.worksheet.bo.Measure;
import it.eng.spagobi.engines.worksheet.serializer.json.FieldsSerializationConstants;
import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SlotJSONDeserializer implements IDeserializer {
	


    public static transient Logger logger = Logger.getLogger(SlotJSONDeserializer.class);

	//@Override
	public Slot deserialize(Object o) throws SerializationException {
		Slot toReturn = null;
		JSONObject slotJSON = null;
		
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(o, "Object to be deserialized cannot be null");
			
			if(o instanceof String) {
				logger.debug("Deserializing string [" + (String)o + "]");
				try {
					slotJSON = new JSONObject( (String)o );
				} catch(Throwable t) {
					logger.debug("Object to be deserialized must be string encoding a JSON object");
					throw new SerializationException("An error occurred while deserializing query: " + (String)o, t);
				}
			} else if(o instanceof JSONObject) {
				slotJSON = (JSONObject)o;
			} else {
				Assert.assertUnreachable("Object to be deserialized must be of type string or of type JSONObject, not of type [" + o.getClass().getName() + "]");
			}
			
			try {
				toReturn = deserializeSlot(slotJSON);
			} catch (Exception e) {
				throw new SerializationException("An error occurred while deserializing measure: " + slotJSON.toString(), e);
			}

		} finally {
			logger.debug("OUT");
		}
		logger.debug("Measure deserialized");
		return toReturn;
	}
	

	private Slot deserializeSlot(JSONObject slotJSON) throws SerializationException {
		Slot slot;
		
		logger.debug("IN");

		slot = null;
		
		try {
			
			Assert.assertNotNull(slotJSON, "Input parameter [slotJSON] cannot be null");
			
			String slotName = slotJSON.getString(QbeSerializationConstants.SLOT_NAME);
			slot = new Slot(slotName);
			
			JSONArray valueset = slotJSON.getJSONArray(QbeSerializationConstants.SLOT_VALUESET);
			for(int i = 0; i < valueset.length(); i++) {
				JSONObject mappedValueDescriptorJSON = valueset.getJSONObject(i);
				String descriptorType = mappedValueDescriptorJSON.getString(QbeSerializationConstants.SLOT_VALUESET_TYPE);
				if(descriptorType.equalsIgnoreCase(QbeSerializationConstants.SLOT_VALUESET_TYPE_PUNCTUAL)) {
					JSONArray valuesJSON = mappedValueDescriptorJSON.getJSONArray(QbeSerializationConstants.SLOT_VALUESET_VALUES);
					Slot.MappedValuesPunctualDescriptor punctualDescriptor = new Slot.MappedValuesPunctualDescriptor();
					for(int j = 0; j < valuesJSON.length(); j++) {
						punctualDescriptor.addValue(valuesJSON.getString(j));
					}
					slot.addMappedValuesDescriptors(punctualDescriptor);
				} else if(descriptorType.equalsIgnoreCase(QbeSerializationConstants.SLOT_VALUESET_TYPE_RANGE)) {
					String fromValue = mappedValueDescriptorJSON.getString(QbeSerializationConstants.SLOT_VALUESET_FROM);
					String toValue = mappedValueDescriptorJSON.getString(QbeSerializationConstants.SLOT_VALUESET_TO);
					Slot.MappedValuesRangeDescriptor rangeDescriptor = new Slot.MappedValuesRangeDescriptor(fromValue, toValue);
					boolean includeFromValue = mappedValueDescriptorJSON.optBoolean(QbeSerializationConstants.SLOT_VALUESET_INCLUDE_FROM);
					rangeDescriptor.setIncludeMinValue(includeFromValue);
					boolean includeToValue = mappedValueDescriptorJSON.optBoolean(QbeSerializationConstants.SLOT_VALUESET_INCLUDE_TO);
					rangeDescriptor.setIncludeMaxValue(includeToValue);
					slot.addMappedValuesDescriptors(rangeDescriptor);
				} else if(descriptorType.equalsIgnoreCase(QbeSerializationConstants.SLOT_VALUESET_TYPE_DEFAULT)) {
					// ok do nothing. we already have all the info we need for defining the default slot 
				} else {
					throw new SerializationException("Impossible to deserialize a mapped values descriptor of type [" + descriptorType + "]");
				}
			} 
		} catch(Throwable t) {
			throw new SerializationException("An error occurred while deserializing slot: " + slotJSON, t);
		} finally {
			logger.debug("OUT");
		}
		
		return slot;
	}
    
		
}
