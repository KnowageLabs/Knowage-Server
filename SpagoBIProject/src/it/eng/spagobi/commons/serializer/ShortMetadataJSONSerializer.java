/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
