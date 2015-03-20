/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.widgets;

import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.engines.worksheet.bo.Field;
import it.eng.spagobi.engines.worksheet.bo.SheetContent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class TableDefinition extends SheetContent {
	
	private List<Field> fields;
	
	public TableDefinition() {
		fields = new ArrayList<Field>();
	}
	
	public List<Field> getFields() {
		return fields;
	}

	public void setFields(List<Field> fields) {
		this.fields = fields;
	}
	
	public void addField(Field field) {
		this.fields.add(field);
	}

	@Override
	public List<Attribute> getFiltersOnDomainValues() {
		List<Attribute> toReturn = new ArrayList<Attribute>();
		List<Field> fields = getFields();
		Iterator<Field> it = fields.iterator();
		while (it.hasNext()) {
			Field field = it.next();
			if (field instanceof Attribute) {
				Attribute attribute = (Attribute) field;
				String values = attribute.getValues();
				if (values != null && !values.equals(new JSONArray().toString())) {
					toReturn.add(attribute);
				}
			}
		}
		return toReturn;
	}

	@Override
	public List<Field> getAllFields() {
		return getFields();
	}
}
