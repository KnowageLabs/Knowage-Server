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
package it.eng.qbe.utility.bo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class CustomizedFunction {

	String name;
	String label;
	String value;

	final static String NAME = "name";
	final static String LABEL = "label";
	final static String VALUE = "value";
	final static String PARAMETERS = "parameters";

	static protected Logger logger = Logger.getLogger(CustomizedFunction.class);

	List<CustomizedFunctionParameter> parameters = new ArrayList<CustomizedFunctionParameter>();

	public CustomizedFunction(JSONObject json) {
		logger.debug("IN");
		name = json.optString(NAME);
		label = json.optString(LABEL);
		value = json.optString(VALUE);

		logger.debug("function " + name);

		JSONArray parametersJson = json.optJSONArray(PARAMETERS);

		if (parametersJson != null) {
			for (int i = 0; i < parametersJson.length(); i++) {
				JSONObject par = parametersJson.optJSONObject(i);
				CustomizedFunctionParameter custFuncPar = new CustomizedFunctionParameter(par);
				parameters.add(custFuncPar);
			}
		}
		logger.debug("OUT");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public List<CustomizedFunctionParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<CustomizedFunctionParameter> parameters) {
		this.parameters = parameters;
	}

}
