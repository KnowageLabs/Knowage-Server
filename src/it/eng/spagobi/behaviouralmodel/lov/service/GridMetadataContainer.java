/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.behaviouralmodel.lov.service;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class GridMetadataContainer {
	private String rootPropery;
	private String totalProperty;
	private List<Object> fields;
	private List<Map<String, String>> values;
	private Map<String, String> metaData;
	private int results;

	public GridMetadataContainer() {
		rootPropery = "root";
		totalProperty = "results";
		values = new ArrayList<Map<String, String>>();
		metaData = new HashMap<String, String>();
		fields = new ArrayList<Object>();
	}

	public void setProperty(String propertyName, String propertyValue) {
		metaData.put(propertyName, propertyValue);
	}

	public String getRootPropery() {
		return rootPropery;
	}

	public void setRootPropery(String rootPropery) {
		this.rootPropery = rootPropery;
	}

	public Map<String, String> getMetaData() {
		return metaData;
	}

	public List<Object> getFields() {
		return fields;
	}

	public void setFields(List<Object> fields) {
		this.fields = fields;
	}

	public List<Map<String, String>> getValues() {
		return values;
	}

	public void setValues(List<Map<String, String>> values) {
		this.values = values;
	}

	public String getTotalProperty() {
		return totalProperty;
	}

	public void setTotalProperty(String totalProperty) {
		this.totalProperty = totalProperty;
	}

	public int getResults() {
		return results;
	}

	public void setResults(int results) {
		this.results = results;
	}

	/**
	 * JSON serializer for this object
	 *
	 * @return the network serialized
	 * @throws SerializationException
	 */
	@JsonIgnore
	public String toJSONString() throws it.eng.spagobi.commons.serializer.SerializationException {
		ObjectMapper mapper = new ObjectMapper();
		String s = "";
		try {
			SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
			simpleModule.addSerializer(GridMetadataContainer.class, new GridMetadataContainerJSONSerializer());
			mapper.registerModule(simpleModule);
			s = mapper.writeValueAsString(this);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while transforming into a JSON object", e);
		}
		// s = StringEscapeUtils.unescapeJavaScript(s);
		return s;
	}

	public JSONObject toJSON() throws JSONException {
		return GridMetadataContainerJSONSerializer.serialize(this);
	}

	/**
	 * Builds a list of maps of type {"header":"...", "name":"..." }
	 *
	 * @param colNames
	 * @return
	 */
	public static List<Object> buildHeaderMap(List<String> colNames) {
		List<Object> toReturn = new ArrayList<Object>();
		for (int i = 0; i < colNames.size(); i++) {
			Map<String, String> headerMap = new HashMap<String, String>();
			headerMap.put("name", colNames.get(i));
			headerMap.put("header", colNames.get(i));
			toReturn.add(headerMap);
		}
		return toReturn;
	}

	/**
	 * Builds a list of maps of type {"header":"...", "name":"..." }
	 *
	 * @param colNames
	 * @return
	 */
	public static List<Object> buildHeaderMapForGrid(List<String> colNames) {
		List<Object> toReturn = new ArrayList<Object>();
		for (int i = 0; i < colNames.size(); i++) {
			Map<String, String> headerMap = new HashMap<String, String>();
			headerMap.put("header", colNames.get(i));
			headerMap.put("name", colNames.get(i));
			headerMap.put("dataIndex", colNames.get(i));
			toReturn.add(headerMap);
		}
		return toReturn;
	}

	public static void main(String args[]) {
		GridMetadataContainer gmc = new GridMetadataContainer();

		gmc.setResults(2);

		gmc.getFields().add("strNumber");
		HashMap am = new HashMap<String, String>();
		am.put("a", "a");
		am.put("b", "b");
		gmc.getFields().add(am);
		HashMap am2 = new HashMap<String, String>();
		am2.put("1", "1");
		am2.put("2", "2");
		gmc.getFields().add(am2);

		HashMap am3 = new HashMap<String, String>();
		am3.put("11a", "11a");
		am3.put("11b", "11b");
		gmc.getValues().add(am3);
		HashMap am4 = new HashMap<String, String>();
		am4.put("111", "111");
		am4.put("112", "112");
		gmc.getValues().add(am4);
		try {
			System.out.println(gmc.toJSONString());
		} catch (Exception e) {
			System.out.println("ssss");
		}

	}
}
