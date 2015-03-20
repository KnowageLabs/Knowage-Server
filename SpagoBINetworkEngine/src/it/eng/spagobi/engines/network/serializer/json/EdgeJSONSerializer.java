/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.engines.network.serializer.json;

import it.eng.spagobi.engines.network.bean.Edge;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class EdgeJSONSerializer extends JsonSerializer<Edge> {

	@Override
	public void serialize(Edge value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException{
		String key;
		jgen.writeStartObject();
		jgen.writeStringField("id", value.getId());
		jgen.writeStringField("target", value.getTargetNode().getId());
		jgen.writeStringField("source", value.getSourceNode().getId());
		Iterator<String> iterator = value.getProperties().keySet().iterator();
		while(iterator.hasNext()){
			key = iterator.next();
			jgen.writeStringField(key,value.getProperties().get(key));
		}
		jgen.writeEndObject();
	}
}
