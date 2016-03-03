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
package it.eng.spagobi.engines.qbe.serializer.json;

import java.util.Collection;
import java.util.List;

import it.eng.qbe.model.structure.ModelCalculatedField.Slot;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot.IMappedValuesDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot.MappedValuesPunctualDescriptor;
import it.eng.qbe.model.structure.ModelCalculatedField.Slot.MappedValuesRangeDescriptor;
import it.eng.qbe.serializer.ISerializer;
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
public class SlotJSONSerializer implements ISerializer {
		
    public static transient Logger logger = Logger.getLogger(SlotJSONSerializer.class);

	//@Override
	public Object serialize(Object o) throws SerializationException {
		JSONObject toReturn = null;
		Slot slot;
				
		Assert.assertNotNull(o, "Input parameter cannot be null");
		Assert.assertTrue(o instanceof Slot, "Unable to serialize objects of type [" + o.getClass().getName() + "]");
		
		try {
			toReturn = new JSONObject();
			
			slot = (Slot) o;
			
			toReturn.put(QbeSerializationConstants.SLOT_NAME, slot.getName());
			toReturn.put(QbeSerializationConstants.SLOT_VALUESET, serializeMappedValuesDescriptors(slot.getMappedValuesDescriptors()));
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return toReturn;
	}
    
	private JSONArray serializeMappedValuesDescriptors(List<IMappedValuesDescriptor> descriptors) throws SerializationException {
		JSONArray descriptorsJSON;
		
		descriptorsJSON = new JSONArray();
		for(IMappedValuesDescriptor descriptor : descriptors) {
			descriptorsJSON.put( serializeMappedValuesDescriptor(descriptor) );
		}
		
		return descriptorsJSON;
	}

	private JSONObject serializeMappedValuesDescriptor(IMappedValuesDescriptor descriptor) throws SerializationException {
		JSONObject descriptorJSON;
		
		descriptorJSON = new JSONObject();
		
		try {
			if(descriptor instanceof MappedValuesPunctualDescriptor) {
				MappedValuesPunctualDescriptor punctualDescriptor = (MappedValuesPunctualDescriptor)descriptor;
				descriptorJSON.put(QbeSerializationConstants.SLOT_VALUESET_TYPE, QbeSerializationConstants.SLOT_VALUESET_TYPE_PUNCTUAL);
				descriptorJSON.put(QbeSerializationConstants.SLOT_VALUESET_VALUES, new JSONArray(punctualDescriptor.getValues()));
			} else if(descriptor instanceof MappedValuesRangeDescriptor) {
				MappedValuesRangeDescriptor rangeDescriptor = (MappedValuesRangeDescriptor)descriptor;
				descriptorJSON.put(QbeSerializationConstants.SLOT_VALUESET_TYPE, QbeSerializationConstants.SLOT_VALUESET_TYPE_RANGE);
				descriptorJSON.put(QbeSerializationConstants.SLOT_VALUESET_FROM, rangeDescriptor.getMinValue());
				descriptorJSON.put(QbeSerializationConstants.SLOT_VALUESET_INCLUDE_FROM, rangeDescriptor.isIncludeMinValue() );
				descriptorJSON.put(QbeSerializationConstants.SLOT_VALUESET_TO, rangeDescriptor.getMaxValue());
				descriptorJSON.put(QbeSerializationConstants.SLOT_VALUESET_INCLUDE_TO, rangeDescriptor.isIncludeMaxValue());
			} else {
				throw new SerializationException("Impossible to serialize a descriptor of class: " + descriptor.getClass().getName());
			}
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + descriptor, t);
		} finally {
			
		}
		
		return descriptorJSON;
	}
}
