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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
public class JSONArraySerializer extends StdSerializer<JSONArray> {

	protected JSONArraySerializer() {
		this(null);
	}

	protected JSONArraySerializer(Class<JSONArray> t) {
		super(t);
	}

	@Override
	public void serialize(JSONArray value, JsonGenerator gen, SerializerProvider provider) throws IOException {

		ArrayNode v = value.getWrappedObject();
		provider.defaultSerializeValue(v, gen);

	}

}
