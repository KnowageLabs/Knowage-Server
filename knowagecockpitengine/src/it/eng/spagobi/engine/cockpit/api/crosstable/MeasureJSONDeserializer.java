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
package it.eng.spagobi.engine.cockpit.api.crosstable;

import it.eng.qbe.serializer.IDeserializer;
import it.eng.qbe.serializer.SerializationException;
import it.eng.spagobi.utilities.assertion.Assert;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class MeasureJSONDeserializer implements IDeserializer {

	public static transient Logger logger = Logger.getLogger(MeasureJSONDeserializer.class);

	// @Override
	@Override
	public Measure deserialize(Object o) throws SerializationException {
		Measure toReturn = null;
		JSONObject measureJSON = null;

		logger.debug("IN");

		try {
			Assert.assertNotNull(o, "Object to be deserialized cannot be null");

			if (o instanceof String) {
				logger.debug("Deserializing string [" + (String) o + "]");
				try {
					measureJSON = new JSONObject((String) o);
				} catch (Exception e) {
					logger.debug("Object to be deserialized must be string encoding a JSON object");
					throw new SerializationException("An error occurred while deserializing query: " + (String) o, e);
				}
			} else if (o instanceof JSONObject) {
				measureJSON = (JSONObject) o;
			} else {
				Assert.assertUnreachable("Object to be deserialized must be of type string or of type JSONObject, not of type [" + o.getClass().getName() + "]");
			}

			try {
				toReturn = deserializeMeasure(measureJSON);
			} catch (Exception e) {
				throw new SerializationException("An error occurred while deserializing measure: " + measureJSON.toString(), e);
			}

		} finally {
			logger.debug("OUT");
		}
		logger.debug("Measure deserialized");
		return toReturn;
	}

	private Measure deserializeMeasure(JSONObject obj) throws JSONException {
		return new Measure(obj.getString(FieldsSerializationConstants.ID), obj.getString(FieldsSerializationConstants.ALIAS),
				obj.getString(FieldsSerializationConstants.ICON_CLS), obj.getString(FieldsSerializationConstants.NATURE),
				obj.getString(FieldsSerializationConstants.FUNCTION));
	}

}
