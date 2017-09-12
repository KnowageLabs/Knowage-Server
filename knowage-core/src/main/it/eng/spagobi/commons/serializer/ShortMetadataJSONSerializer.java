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

import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;

import java.util.Locale;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Chiarelli Chiara
 */
public class ShortMetadataJSONSerializer implements Serializer {
	
	private static Logger logger = Logger.getLogger(ShortMetadataJSONSerializer.class);
	
	public static final String LABEL = "label";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		logger.debug("IN");
		JSONObject result = new JSONObject();

		if ( !(o instanceof ObjMetadata) ) {
			throw new SerializationException("ShortMetadataJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {

			ObjMetadata meta = (ObjMetadata)o;

			result.put(LABEL, meta.getLabel());
			result.put(NAME, meta.getName());
			result.put(DESCRIPTION, meta.getDescription());
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			logger.debug("OUT");
		}
		return result;
	}

}
