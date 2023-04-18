/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
public class JSONArrayDeserializer extends StdDeserializer<JSONArray> {

	protected JSONArrayDeserializer() {
		this(null);
	}

	protected JSONArrayDeserializer(Class<?> t) {
		super(t);
	}

	@Override
	public JSONArray deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JSONArray ret = new JSONArray();

		ArrayNode v = p.getCodec().readValue(p, ArrayNode.class);

		ret.getWrappedObject().addAll(v);

		return ret;
	}

}
