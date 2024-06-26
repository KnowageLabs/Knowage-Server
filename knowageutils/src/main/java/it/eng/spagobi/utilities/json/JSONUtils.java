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
package it.eng.spagobi.utilities.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JSONUtils {
	public static List asList(JSONArray array) {
		List list;

		if (array == null) {
			throw new IllegalArgumentException();
		}
		list = new ArrayList();

		for (int i = 0; i < array.length(); i++) {
			try {
				list.add(array.get(i));
			} catch (JSONException e) {
				Assert.assertUnreachable("An out of bound error here is a signal of an internal JSON.org's bug");
			}
		}

		return list;
	}

	public static String[] asStringArray(JSONArray jSONArray) throws JSONException {
		if (jSONArray == null) {
			return null;
		}
		int length = jSONArray.length();
		String[] toReturn = new String[jSONArray.length()];
		for (int i = 0; i < length; i++) {
			toReturn[i] = jSONArray.getString(i).toString();
		}
		return toReturn;
	}

	public static JSONArray asJSONArray(String[] stringArray) throws JSONException {
		if (stringArray == null) {
			return null;
		}
		int length = stringArray.length;
		JSONArray toReturn = new JSONArray();
		for (int i = 0; i < length; i++) {
			toReturn.put(stringArray[i]);
		}
		return toReturn;
	}

	public static JSONArray toJSONArray(String object) throws JSONException, JsonMappingException, JsonParseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode df = (ArrayNode) mapper.readValue(object, JsonNode.class);
		return toJSONArray(df);
	}

	public static JSONObject toJSONObject(String object) throws JSONException, JsonMappingException, JsonParseException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode df = (ObjectNode) mapper.readValue(object, JsonNode.class);
		return toJSONObject(df);
	}

	public static JSONArray toJSONArray(ArrayNode df) throws JSONException {
		JSONArray toReturn = new JSONArray();
		for (int i = 0; i < df.size(); i++) {

			toReturn.put(getValueFromJsonNode(df.get(i)));
		}
		return toReturn;
	}

	public static JSONObject toJSONObject(ObjectNode df) throws JSONException {
		JSONObject toReturn = new JSONObject();
		Iterator<String> namesIter = df.fieldNames();
		while (namesIter.hasNext()) {
			String key = namesIter.next();
			JsonNode node = df.get(key);
			Object value = getValueFromJsonNode(node);
			toReturn.put(key, value);
		}
		return toReturn;
	}

	public static Object getValueFromJsonNode(JsonNode node) throws JSONException {

		Object value = null;
		if (node instanceof TextNode) {
			value = ((TextNode) (node)).textValue();
		} else if (node instanceof ObjectNode) {
			value = toJSONObject((ObjectNode) node);
		} else if (node instanceof ArrayNode) {
			value = toJSONArray((ArrayNode) node);
		} else if (node instanceof ValueNode) {
			value = ((ValueNode) node).asText();
		}
		return value;
	}

	public static String escapeJsonString(String object) {
		String toReturn = object;

		toReturn = toReturn.replaceAll("\n", "\\\\n");
		toReturn = toReturn.replaceAll("\r", "\\\\r");
		toReturn = toReturn.replaceAll("\t", "\\\\t");
		toReturn = toReturn.replaceAll("\b", "\\\\b");
		toReturn = toReturn.replaceAll("\f", "\\\\f");
		// toReturn = toReturn.replaceAll("\"", "\\\\\"");

		// toReturn = toReturn.replaceAll("{", "\\{");
		// toReturn = toReturn.replaceAll("}", "\\}");
		// toReturn = toReturn.replaceAll("(", "\\\\(");
		// toReturn = toReturn.replaceAll(")", "\\\\)");
		// toReturn = toReturn.replaceAll("[", "\\[");
		// toReturn = toReturn.replaceAll("]", "\\]");

		return toReturn;
	}

	/**
	 * Transform a jsonobject into a map
	 *
	 * @param object
	 * @return
	 * @throws JSONException
	 */
	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> toReturn = new HashMap<String, Object>();

		if (object != null) {
			String[] names = JSONObject.getNames(object);
			for (int i = 0; i < names.length; i++) {
				toReturn.put(names[i], object.get(names[i]));
			}
		}

		return toReturn;

	}


	/**
	 * Transform a jsonarray into a unique map with ALL json object properties
	 *
	 * @param object
	 * @return
	 * @throws JSONException
	 */
	public static List<Map<String, Object>> toMap(JSONArray object) throws JSONException {
		List<Map<String, Object>> toReturn = new ArrayList<Map<String, Object>>();

		if (object != null) {
			for (int o=0; o < object.length(); o++) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				JSONObject obj = (JSONObject)object.get(o);
				String[] names = JSONObject.getNames(obj);
				for (int i = 0; i < names.length; i++) {
					map.put(names[i], obj.get(names[i]));
				}
				toReturn.add(map);
			}
		}

		return toReturn;

	}
	/**
	 * Transform a map into a jsonObject
	 *
	 * @param map
	 * @return
	 * @throws JSONException
	 */
	public static JSONObject getJsonFromMap(Map<String, Object> map) throws JSONException {
		JSONObject jsonData = new JSONObject();
		for (String key : map.keySet()) {
			Object value = map.get(key);
			if (value != null && value instanceof Map<?, ?>) {
				value = getJsonFromMap((Map<String, Object>) value);
			}
			jsonData.put(key, value);
		}
		return jsonData;
	}

	public static String getQueryString(JSONObject json) throws JSONException {
		StringBuilder sb = new StringBuilder();
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = json.get(key);
			if (value != null && value instanceof String) {
				sb.append(key);
				sb.append("=");
				sb.append(json.get(key));
				sb.append("&"); // To allow for another argument.
			}
		}

		return sb.toString();
	}

	/**
	 * Check if a JSONObject is empty
	 *
	 * @param a
	 *            not null object
	 * @return boolean
	 * @throws JSONException
	 */
	public static boolean isEmpty(JSONObject object) {
		return object.length() == 0 ? true : false;
	}

}
