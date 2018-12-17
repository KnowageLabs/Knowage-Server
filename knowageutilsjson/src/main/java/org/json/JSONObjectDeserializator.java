/**
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
package org.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author FMilosavljevic
 *
 */
public class JSONObjectDeserializator {
	static protected Logger logger = Logger.getLogger(JSONObjectDeserializator.class);

	public static HashMap<String, Object> getHashMapFromJSONObject(JSONObject object) throws IOException {

		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper mapper = JacksonMapper.getMapper();

		try {
			if (object != null)
			map = mapper.readValue(object.toString(), new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException e) {
			throw new IOException(e.getMessage(), e);
		}
		return map;
	}

	public static HashMap<String, Object> getHashMapFromString(String object) throws IOException {

		HashMap<String, Object> map = new HashMap<String, Object>();
		ObjectMapper mapper = JacksonMapper.getMapper();

		try {
			if (object != null)
				map = mapper.readValue(object, new TypeReference<Map<String, Object>>() {
				});
		} catch (IOException e) {
			throw new IOException(e.getMessage(), e);
		}
		return map;
	}
}
