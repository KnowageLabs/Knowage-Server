/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.serializer.json;

import it.eng.qbe.serializer.IDeserializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class AttributeJSONDeserializer implements IDeserializer {

    public static transient Logger logger = Logger.getLogger(AttributeJSONDeserializer.class);

	//@Override
	public Attribute deserialize(Object o) throws SerializationException {
		Attribute toReturn = null;
		JSONObject attributeJSON = null;
		
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(o, "Object to be deserialized cannot be null");
			
			if(o instanceof String) {
				logger.debug("Deserializing string [" + (String)o + "]");
				try {
					attributeJSON = new JSONObject( (String)o );
				} catch(Throwable t) {
					logger.debug("Object to be deserialized must be string encoding a JSON object");
					throw new SerializationException("An error occurred while deserializing query: " + (String)o, t);
				}
			} else if(o instanceof JSONObject) {
				attributeJSON = (JSONObject)o;
			} else {
				Assert.assertUnreachable("Object to be deserialized must be of type string or of type JSONObject, not of type [" + o.getClass().getName() + "]");
			}
			
			try {
				toReturn = deserializeAttribute(attributeJSON);
			} catch (Exception e) {
				throw new SerializationException("An error occurred while deserializing attribute: " + attributeJSON.toString(), e);
			}

		} finally {
			logger.debug("OUT");
		}
		logger.debug("Attribute deserialized");
		return toReturn;
	}

	private Attribute deserializeAttribute(JSONObject obj) throws JSONException {
		return new Attribute(obj.getString(FieldsSerializationConstants.ID),
				obj.getString(FieldsSerializationConstants.ALIAS),
				obj.getString(FieldsSerializationConstants.ICON_CLS),
				obj.getString(FieldsSerializationConstants.NATURE),
				obj.getString(FieldsSerializationConstants.VALUES));
	}
    
		
}
