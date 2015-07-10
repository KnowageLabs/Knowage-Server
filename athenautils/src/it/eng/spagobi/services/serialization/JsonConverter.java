/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
