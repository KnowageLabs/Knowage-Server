/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package org.json;

import java.util.HashMap;
import java.util.Map;

/**
 * @author albnale
 *
 */
public class TestJSONObjectConstructors {

	/**
	 * @param args
	 * @throws JSONException
	 */
	public static void main(String[] args) throws JSONException {
		Map<String, Object> jsonMap = new HashMap<>();

		/* Constructor with input JSONObject parameter */
		jsonMap.put("key1", "value1");
		jsonMap.put("key2", null);
		JSONObject mapJSON = new JSONObject(jsonMap);
		System.out.println(mapJSON);

		/* Constructor with input String parameter */
		String jsonString = "{'key1':'value1', 'key2':null}";
		JSONObject stringJSON = new JSONObject(jsonString);
		System.out.println(stringJSON);

	}

}
