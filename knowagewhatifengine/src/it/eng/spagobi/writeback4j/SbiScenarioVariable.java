/**
 * 
 */
package it.eng.spagobi.writeback4j;

import java.io.Serializable;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
public class SbiScenarioVariable implements Serializable {

	private static final long serialVersionUID = -401418035760783513L;
	private String name;
	private String value;
	private SbiScenarioVariableType type;

	public SbiScenarioVariable(String name) {
		super();
		this.name = name;
	}

	public SbiScenarioVariable(String name, String value, String type) {
		super();
		this.name = name;
		this.value = value;
		setType(type);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public SbiScenarioVariableType getType() {
		return type;
	}

	public void setType(String type) {
		if (type == null || type.equalsIgnoreCase("double") || type.equalsIgnoreCase("float")) {
			this.type = SbiScenarioVariableType.DOUBLE;
		}
		else if (type != null && type.equalsIgnoreCase("string")) {
			this.type = SbiScenarioVariableType.STRING;
		} else {
			this.type = SbiScenarioVariableType.INTEGER;
		}
	}

	public void setType(SbiScenarioVariableType type) {
		this.type = type;
	}

	public enum SbiScenarioVariableType {
		INTEGER(Integer.class), DOUBLE(Double.class), STRING(String.class);

		private Class type;

		private SbiScenarioVariableType(Class type) {
			this.type = type;
		}

		public Object getTypedType(String value) {
			if (type.equals(String.class)) {
				return value;
			}
			try {
				return type.getConstructor(String.class).newInstance(value);
			} catch (Exception e) {
				return null;
			}
		}
	}

}
