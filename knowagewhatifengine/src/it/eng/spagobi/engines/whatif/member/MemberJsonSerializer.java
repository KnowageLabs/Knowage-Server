/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * 
 * Json Serializer for the org.olap4j.metadata.Member Class
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */
package it.eng.spagobi.engines.whatif.member;

import java.io.IOException;

import org.olap4j.OlapException;
import org.olap4j.metadata.Member;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.gson.JsonParseException;

public class MemberJsonSerializer extends JsonSerializer<Member> {

	private static final String NAME = "name";
	private static final String UNIQUE_NAME = "uniqueName";
	private static final String ID = "id";
	private static final String TEXT = "text";
	private static final String LEAF = "leaf";
	private static final String VISIBLE = "visible";

	@Override
	public void serialize(Member value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		// value.getUniqueName().equals("[Customers].[Canada]"){
		// value.getProperties()
		// }

		jgen.writeStringField(NAME, value.getName());
		jgen.writeStringField(UNIQUE_NAME, value.getUniqueName());
		jgen.writeStringField(ID, value.getUniqueName());
		jgen.writeStringField(TEXT, value.getName());
		// jgen.writeBooleanField(VISIBLE, value.isVisible());
		try {
			jgen.writeBooleanField(LEAF, value.getChildMemberCount() == 0);
		} catch (OlapException e) {
			throw new JsonParseException("Error getting the childs count for the member " + value.getUniqueName(), e);
		}
		jgen.writeEndObject();
	}
}
