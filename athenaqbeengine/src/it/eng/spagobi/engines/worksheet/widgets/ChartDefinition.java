/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
