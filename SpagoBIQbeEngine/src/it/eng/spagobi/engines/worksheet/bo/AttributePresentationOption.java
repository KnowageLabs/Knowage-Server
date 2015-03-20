/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.bo;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

public class AttributePresentationOption extends FieldOption {

	public static final String NAME = "attributePresentation";
	
	public enum AdmissibleValues {code, description, both};
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public void setValue(Object valueObj) {
		if ( !(valueObj instanceof String) ) {
			throw new SpagoBIEngineRuntimeException("Value for this option must be a string");
		}
		String valueStr = (String) valueObj;
		AdmissibleValues value = null;
		try {
			value = AdmissibleValues.valueOf(valueStr);
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Value [" + valueStr + "] not valid for this option", e);
		}
		super.setValue(value);
	}
	
	@Override
	public Object getValue() {
		AdmissibleValues value = (AdmissibleValues) super.getValue();
		return value.name();
	}

}
