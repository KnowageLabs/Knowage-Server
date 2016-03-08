/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
