/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.properties;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ModelPropertyMeta {
	protected String name;
	protected boolean inherited;   
	protected boolean optional;   
	protected String defaultValue; 
	
	public ModelPropertyMeta(String name, boolean inherited, boolean optional, String defaultValue) {
		this.name = name;
		this.inherited = inherited;
		this.optional = optional;
		this.defaultValue = defaultValue;
	}

	public String getName() {
		return name;
	}

	public boolean isInherited() {
		return inherited;
	}

	public boolean isOptional() {
		return optional;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
