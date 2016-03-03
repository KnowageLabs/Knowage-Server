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
