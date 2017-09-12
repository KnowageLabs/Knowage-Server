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
