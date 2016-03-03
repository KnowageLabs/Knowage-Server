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
