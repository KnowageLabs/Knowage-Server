/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.bo;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class WorksheetFieldsOptions {
	
	private List<FieldOptions> options = null;

	private static String ATTRIBUTE_PRESENTATION = "attributePresentation";
	private static String MEASURE_SCALE_FACTOR = "measureScaleFactor";
	
	private static Map<String, Class> generators = null;
	
	static {
		generators = new HashMap<String, Class>();
		generators.put(ATTRIBUTE_PRESENTATION, AttributePresentationOption.class);
		generators.put(MEASURE_SCALE_FACTOR, MeasureScaleFactorOption.class);
	}
	
	
	public WorksheetFieldsOptions() {
		options = new ArrayList<FieldOptions>();
	}
	
	public List<FieldOptions> getFieldsOptions() {
		return options;
	}
	
	public void addFieldOptions(FieldOptions o) {
		options.add(o);
	}
	
	public void removeFieldOptions(FieldOptions o) {
		options.remove(o);
	}

	public FieldOptions getOptionsForFieldByAlias(String alias) {
		FieldOptions toReturn = null;
		Iterator<FieldOptions> it = this.options.iterator();
		while (it.hasNext()) {
			FieldOptions aFieldOptions = it.next();
			Field field = aFieldOptions.getField();
			if (field.getAlias().equalsIgnoreCase(alias)) {
				toReturn = aFieldOptions;
				break;
			}
		}
		return toReturn;
	}
	
	public FieldOptions getOptionsForFieldByFieldId(String fieldId) {
		FieldOptions toReturn = null;
		Iterator<FieldOptions> it = this.options.iterator();
		while (it.hasNext()) {
			FieldOptions aFieldOptions = it.next();
			Field field = aFieldOptions.getField();
			if (field.getEntityId().equalsIgnoreCase(fieldId)) {
				toReturn = aFieldOptions;
				break;
			}
		}
		return toReturn;
	}
	
	public static FieldOption createOption(Field field, String name,
			Object value) {
		Class clazz = generators.get(name);
		if (clazz == null) {
			throw new SpagoBIEngineRuntimeException("Cannot recognize option with name [" + name + "]");
		}
		FieldOption toReturn = null;
		try {
			toReturn = (FieldOption) clazz.newInstance();
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Error instantiating option with name [" + name + "] using class [" + clazz + "]", e);
		}
		toReturn.setField(field);
		toReturn.setValue(value);
		return toReturn;
	}

}
