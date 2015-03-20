/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query.serializer;

import it.eng.qbe.query.serializer.json.QueryJSONDeserializer;
import it.eng.qbe.query.serializer.json.QueryJSONSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class SerializerFactory {
	
	static Map<String, IQuerySerializer> serializerMappings;
	static Map<String, IQueryDeserializer> deserializerMappings;
	
	static {
		serializerMappings = new HashMap();
		serializerMappings.put( "application/json", new QueryJSONSerializer() );
		
		deserializerMappings = new HashMap();
		deserializerMappings.put( "application/json", new QueryJSONDeserializer() );
	}
	
	public static IQuerySerializer getSerializer(String mimeType) {
		return serializerMappings.get( mimeType );
	}
	
	public static IQueryDeserializer getDeserializer(String mimeType) {
		return deserializerMappings.get( mimeType );
	}
}

