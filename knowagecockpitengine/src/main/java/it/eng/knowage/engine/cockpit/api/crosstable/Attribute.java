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
package it.eng.knowage.engine.cockpit.api.crosstable;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

public class Attribute extends Field {

	/**
	 * contains an array of selected values encoded into a string
	 */
	private String values = null;
	private String variable = null;

	public String getValues() {
		return values;
	}

	public String getVariable() {
		return variable;
	}

	public Attribute(String entityId, String alias, String sortingId, String iconCls, String nature, String values, String variable, JSONObject config) {
		super(entityId, alias, sortingId, iconCls, nature, config);
		this.values = values;
		this.variable = variable;
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

	@Override
	public Attribute clone() {
		return new Attribute(entityId, alias, sortingId, iconCls, nature, values, variable, config);
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
	
	public void setVariable(String variable) {
		this.variable = variable;
	}
}