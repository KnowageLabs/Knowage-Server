/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.dataset.crosstab;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
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