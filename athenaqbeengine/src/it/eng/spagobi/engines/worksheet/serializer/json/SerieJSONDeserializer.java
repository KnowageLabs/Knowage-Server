/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.serializer.json;

import it.eng.qbe.serializer.IDeserializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.engines.worksheet.bo.Measure;
import it.eng.spagobi.engines.worksheet.bo.Serie;
import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class SerieJSONDeserializer extends MeasureJSONDeserializer implements IDeserializer {

    public static transient Logger logger = Logger.getLogger(SerieJSONDeserializer.class);

	@Override
	public Serie deserialize(Object o) throws SerializationException {
		Serie toReturn = null;
		JSONObject serieJSON = null;
		
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(o, "Object to be deserialized cannot be null");
			
			if(o instanceof String) {
				logger.debug("Deserializing string [" + (String)o + "]");
				try {
					serieJSON = new JSONObject( (String)o );
				} catch(Throwable t) {
					logger.debug("Object to be deserialized must be string encoding a JSON object");
					throw new SerializationException("An error occurred while deserializing query: " + (String)o, t);
				}
			} else if(o instanceof JSONObject) {
				serieJSON = (JSONObject)o;
			} else {
				Assert.assertUnreachable("Object to be deserialized must be of type string or of type JSONObject, not of type [" + o.getClass().getName() + "]");
			}
			
			try {
				toReturn = deserializeSerie(serieJSON);
			} catch (Exception e) {
				throw new SerializationException("An error occurred while deserializing serie: " + serieJSON.toString(), e);
			}

		} finally {
			logger.debug("OUT");
		}
		logger.debug("Measure deserialized");
		return toReturn;
	}
	

	private Serie deserializeSerie(JSONObject obj) throws JSONException, SerializationException {
		Measure m = super.deserialize(obj);
		return new Serie(m.getEntityId(), m.getAlias(), m.getIconCls(), m.getNature(), m.getAggregationFunction().getName(), 
				obj.getString(FieldsSerializationConstants.SERIENAME), 
				obj.getString(FieldsSerializationConstants.COLOR),
				obj.getBoolean(FieldsSerializationConstants.SHOWCOMMA),
				obj.getInt(FieldsSerializationConstants.PRECISION), 
				obj.getString(FieldsSerializationConstants.SUFFIX));
	}
    
		
}
