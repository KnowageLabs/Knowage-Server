/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.serializer.json;

import it.eng.qbe.serializer.ISerializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.engines.worksheet.bo.Measure;
import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class MeasureJSONSerializer implements ISerializer {

    public static transient Logger logger = Logger.getLogger(MeasureJSONSerializer.class);

	//@Override
	public Object serialize(Object o) throws SerializationException {
		JSONObject toReturn = null;
		Measure measure;
				
		Assert.assertNotNull(o, "Input parameter cannot be null");
		Assert.assertTrue(o instanceof Measure, "Unable to serialize objects of type [" + o.getClass().getName() + "]");
		
		try {
			toReturn = new JSONObject();
			
			measure = (Measure) o;
			
			toReturn.put(FieldsSerializationConstants.ID, measure.getEntityId());
			toReturn.put(FieldsSerializationConstants.ALIAS, measure.getAlias());
			toReturn.put(FieldsSerializationConstants.ICON_CLS, measure.getIconCls());
			toReturn.put(FieldsSerializationConstants.NATURE, measure.getNature());
			toReturn.put(FieldsSerializationConstants.FUNCTION, measure.getAggregationFunction().getName());

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return toReturn;
	}
    
		
}
