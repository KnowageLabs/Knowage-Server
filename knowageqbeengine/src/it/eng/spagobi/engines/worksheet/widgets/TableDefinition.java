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
