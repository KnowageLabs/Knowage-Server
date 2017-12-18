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
package it.eng.spagobi.tools.dataset.persist.temporarytable;

import java.util.HashMap;
import java.util.Map;

public class ColumnMeta {

	String name;
	Class type;
	String alias;

	Integer size;
	Map properties = null;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Class getType() {
		return type;
	}
	public void setType(Class type) {
		this.type = type;
	}
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public ColumnMeta(String name, Class type, Map properties) {
		super();
		this.name = name;
		this.type = type;
		this.size = size;
		if(properties != null) this.properties = properties;
		else properties = new HashMap<String, String>();
	}
	public Map getProperties() {
		return properties;
	}
	public void setProperties(Map properties) {
		this.properties = properties;
	}
	
	
	
	
	
}
