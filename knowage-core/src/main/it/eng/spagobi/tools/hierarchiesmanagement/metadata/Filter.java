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
package it.eng.spagobi.tools.hierarchiesmanagement.metadata;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author Antonella Giachino (giachino.antonella@eng.it)
 *
 */
public class Filter {

	String name;
	String type;
	String defaultValue;
	HashMap<String, String> conditions;

	/**
	 * @param name
	 * @param type
	 */
	public Filter(String name, String type, LinkedHashMap<String, String> conditions) {
		this(name, type, null, conditions);
	}

	public Filter(String name, String type, String defaultValue, LinkedHashMap<String, String> conditions) {
		super();
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.conditions = conditions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public HashMap<String, String> getConditions() {
		return conditions;
	}

	public void setConditions(HashMap<String, String> conditions) {
		this.conditions = conditions;
	}

}
