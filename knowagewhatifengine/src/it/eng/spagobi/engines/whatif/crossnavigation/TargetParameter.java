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

package it.eng.spagobi.engines.whatif.crossnavigation;

import java.io.Serializable;

import it.eng.spago.base.SourceBean;
/*
 * Parameter in cross navigation configuration
 */
public class TargetParameter implements Serializable{

	private static final long serialVersionUID = 8834543798002801964L;
	
	private static String TAG_NAME = "name";
	private static String TAG_SCOPE = "scope";
	private static String TAG_VALUE = "value";
	private static String TAG_DIMENSION = "dimension";
	private static String TAG_HIERARCHY = "hierarchy";
	private static String TAG_LEVEL = "level";
	private static String TAG_PROPERTY = "property";
	private static String ABSOLUTE = "absolute";
	protected String name;
	private boolean isAbsolute;
	private String value;
	private String dimension;
	private String hierarchy;
	private String level;
	private String property;
	
	

	public TargetParameter() {
	}

	TargetParameter(SourceBean sb) {
		name = (String) sb.getAttribute(TAG_NAME);
		isAbsolute = ((String) sb.getAttribute(TAG_SCOPE)).trim().equalsIgnoreCase(ABSOLUTE);
		if (isAbsolute) {
			value = (String) sb.getAttribute(TAG_VALUE);
		} else {
			dimension = (String) sb.getAttribute(TAG_DIMENSION);
			hierarchy = (String) sb.getAttribute(TAG_HIERARCHY);
			level = (String) sb.getAttribute(TAG_LEVEL);
		}
		property = (String) sb.getAttribute(TAG_PROPERTY);
	}
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAbsolute() {
		return isAbsolute;
	}

	public void setAbsolute(boolean isAbsolute) {
		this.isAbsolute = isAbsolute;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public String getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}
	
	
}
