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

package it.eng.spagobi.services.rest.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class JSONObjectMessageBodyReader implements MessageBodyReader<JSONObject> {

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return type == JSONObject.class;
	}

	@Override
	public JSONObject readFrom(Class<JSONObject> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {

		JSONObject ret = null;

		try (InputStreamReader r1 = new InputStreamReader(entityStream); BufferedReader r2 = new BufferedReader(r1)) {
			StringBuilder sb = new StringBuilder();
			int c = 0;
			while((c = r2.read()) != -1) {
				sb.append((char) c);
			}

			try {
				ret = new JSONObject(sb.toString());
			} catch (JSONException e) {
				throw new IOException("Cannot read JSON object. Buffer was: " + String.valueOf(sb), e);
			}
		}

		return ret;
	}

}
