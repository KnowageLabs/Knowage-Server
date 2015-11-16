/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.serializer.json;

import it.eng.qbe.serializer.ISerializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.engines.worksheet.bo.Filter;
import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class FilterJSONSerializer extends AttributeJSONSerializer implements ISerializer {

    public static transient Logger logger = Logger.getLogger(FilterJSONSerializer.class);

	@Override
	public Object serialize(Object o) throws SerializationException {
		JSONObject toReturn = null;
		Filter filter;
				
		Assert.assertNotNull(o, "Input parameter cannot be null");
		Assert.assertTrue(o instanceof Filter, "Unable to serialize objects of type [" + o.getClass().getName() + "]");
		
		try {
			filter = (Filter) o;
			
			toReturn = (JSONObject) super.serialize(o);
			
			String mandatory = filter.isMandatory() ? "yes" : "no";
			String selection = filter.isMultivalue() ? FieldsSerializationConstants.SELECTION_MULTIVALUE : FieldsSerializationConstants.SELECTION_SINGLEVALUE;
			
			toReturn.put(FieldsSerializationConstants.SELECTION, selection);
			toReturn.put(FieldsSerializationConstants.MANDATORY, mandatory);
			
			if(filter.isSplittingFilter()){
				toReturn.put(FieldsSerializationConstants.SPLITTING_FILTER, "on");
			}
			
			

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return toReturn;
	}
    
		
}
