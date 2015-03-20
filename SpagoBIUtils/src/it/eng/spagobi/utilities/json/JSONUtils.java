/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.json;

import it.eng.spagobi.utilities.assertion.Assert;

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

}
