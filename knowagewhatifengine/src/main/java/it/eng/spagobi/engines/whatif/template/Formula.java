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
package it.eng.spagobi.engines.whatif.template;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.utilities.StringUtils;

/**
 * @author Dragan Pirkovic
 *
 */
public class Formula {
	private String name;
	private final List<Argument> arguments;

	/**
	 *
	 */
	public Formula(SourceBean sb) {
		arguments = new ArrayList<>();
		this.name = (String) sb.getAttribute("name");
		List<SourceBean> argumentsSb = sb.getAttributeAsList("argument");
		if (argumentsSb != null) {
			for (SourceBean argument : argumentsSb) {
				arguments.add(new Argument((String) argument.getAttribute("default_value")));
			}
		}

	}

	/**
	 * @param formulaJson
	 * @throws JSONException
	 */
	public Formula(JSONObject formulaJson) throws JSONException {
		arguments = new ArrayList<>();
		name = formulaJson.getString("name");
		JSONArray argumentsJson = formulaJson.getJSONArray("argument");
		for (int i = 0; i < argumentsJson.length(); i++) {
			arguments.add(new Argument(argumentsJson.getJSONObject(i).getString("default_value")));
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	private String getArgumentExpression() {
		// StringUtils
		List<String> list = new ArrayList<>();
		for (Argument argument : arguments) {
			list.add(argument.getDefaultValue());
		}
		return StringUtils.join(list, ",");
	}

	public String getExpression() {
		return name + "(" + getArgumentExpression() + ")";
	}

	/**
	 * @return
	 */
	public List<Argument> getArguments() {
		// TODO Auto-generated method stub
		return arguments;
	}

}
