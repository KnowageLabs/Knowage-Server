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
package it.eng.spagobi.services.serialization;

import it.eng.spagobi.services.validation.ObjectValidator;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConverter {

	public static Object jsonToValidObject(String json, Class<?> t) {
		Object obj = jsonToObject(json, t);
		String violations = ObjectValidator.validate(obj);

		if (violations == null) {
			return obj;
		} else {
			throw new SpagoBIRuntimeException(violations);
		}
	}

	public static Object jsonToObject(String Json, Class<?> t) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.readValue(Json, t);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while reading the JSON object", e);
		}
	}

	public static String objectToJson(Object obj, Class<?> t) {
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.writerWithType(t).writeValueAsString(obj);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while writing the JSON string", e);
		}
	}
}
