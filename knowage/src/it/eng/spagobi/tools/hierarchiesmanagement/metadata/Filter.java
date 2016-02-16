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
package it.eng.spagobi.tools.hierarchiesmanagement.metadata;

import java.util.LinkedHashMap;

/**
 * @author Antonella Giachino (giachino.antonella@eng.it)
 *
 */
public class Filter {

	String name;
	String type;
	String defaultValue;
	LinkedHashMap<String, String> conditions;

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

	public LinkedHashMap<String, String> getConditions() {
		return conditions;
	}

	public void setConditions(LinkedHashMap<String, String> conditions) {
		this.conditions = conditions;
	}

}
