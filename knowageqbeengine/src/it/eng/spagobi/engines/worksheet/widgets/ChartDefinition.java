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
import it.eng.spagobi.engines.worksheet.bo.Serie;
import it.eng.spagobi.engines.worksheet.bo.SheetContent;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class ChartDefinition extends SheetContent {
	
	private Attribute category = null;
	private Attribute groupingVariable = null;
	private List<Serie> series = null;
	private JSONObject config = null;
	
	public ChartDefinition() {}

	public Attribute getCategory() {
		return category;
	}

	public void setCategory(Attribute category) {
		this.category = category;
	}
	
	public void setGroupingVariable(Attribute groupingVariable) {
		this.groupingVariable = groupingVariable;
	}
	
	public Attribute getGroupingVariable() {
		return groupingVariable;
	}

	public List<Serie> getSeries() {
		return series;
	}

	public void setSeries(List<Serie> series) {
		this.series = series;
	}

	public JSONObject getConfig() {
		return config;
	}

	public void setConfig(JSONObject config) {
		this.config = config;
	}

	@Override
	public List<Attribute> getFiltersOnDomainValues() {
		List<Attribute> toReturn = new ArrayList<Attribute>();
		Attribute category = getCategory();
		String values = category.getValues();
		if (values != null && !values.equals(new JSONArray().toString())) {
			toReturn.add(category);
		}
		Attribute groupingVariable = getGroupingVariable();
		values = groupingVariable.getValues();
		if (values != null && !values.equals(new JSONArray().toString())) {
			toReturn.add(groupingVariable);
		}
		return toReturn;
	}

	@Override
	public List<Field> getAllFields() {
		List<Field> toReturn = new ArrayList<Field>();
		toReturn.add(getCategory());
//		Attribute groupingVariable = getGroupingVariable();
//		if (groupingVariable != null) {
//			toReturn.add(groupingVariable);
//		}
		
		toReturn.add(getGroupingVariable());
		toReturn.addAll(getSeries());
		return toReturn;
	}

}
