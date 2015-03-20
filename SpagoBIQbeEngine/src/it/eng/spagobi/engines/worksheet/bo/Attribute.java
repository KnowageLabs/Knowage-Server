/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.bo;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

public class Attribute extends Field {
	/**
	 * contains an array of selected values encoded into a string
	 */
	String values = null;
	public String getValues() {
		return values;
	}
	public Attribute(String entityId, String alias, String iconCls, String nature, String values) {
		super(entityId, alias, iconCls, nature);
		this.values = values;
	}
	public List<String> getValuesAsList() {
		List<String> toReturn = new ArrayList<String>();
		if (values == null) {
			return toReturn;
		}
		JSONArray array = null;
		try {
			array = new JSONArray(values);
		} catch (JSONException e) {
			throw new SpagoBIEngineRuntimeException("Cannot convert the string [" + values + "] into a valid JSONArray", e);
		}
		for (int i = 0; i < array.length(); i++) {
			String aValue = array.optString(i);
			toReturn.add(aValue);
		}
		return toReturn;
	}
	public Attribute clone() {
		return new Attribute(entityId, alias, iconCls, nature, values);
	}
	public void setValues(List<String> values) {
		JSONArray array = new JSONArray();
		if (values != null) {
			for (int i = 0; i < values.size(); i++) {
				String aValue = values.get(i);
				array.put(aValue);
			}
		}
		String valuesStr = array.toString();
		setValues(valuesStr);
	}
	private void setValues(String values) {
		this.values = values;
	}
}