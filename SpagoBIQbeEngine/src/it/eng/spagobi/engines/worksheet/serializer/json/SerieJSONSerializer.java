/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.serializer.json;

import it.eng.qbe.serializer.ISerializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.engines.worksheet.bo.Serie;
import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class SerieJSONSerializer extends MeasureJSONSerializer implements ISerializer {

    public static transient Logger logger = Logger.getLogger(SerieJSONSerializer.class);

	@Override
	public Object serialize(Object o) throws SerializationException {
		JSONObject toReturn = null;
		Serie serie;
				
		Assert.assertNotNull(o, "Input parameter cannot be null");
		Assert.assertTrue(o instanceof Serie, "Unable to serialize objects of type [" + o.getClass().getName() + "]");
		
		try {
			serie = (Serie) o;
			
			toReturn = (JSONObject) super.serialize(o);
			
			toReturn.put(FieldsSerializationConstants.SERIENAME, serie.getSerieName());
			toReturn.put(FieldsSerializationConstants.COLOR, serie.getColor());
			toReturn.put(FieldsSerializationConstants.SHOWCOMMA, serie.getShowComma());
			toReturn.put(FieldsSerializationConstants.PRECISION, serie.getPrecision());
			toReturn.put(FieldsSerializationConstants.SUFFIX, serie.getSuffix());

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return toReturn;
	}
    
		
}
