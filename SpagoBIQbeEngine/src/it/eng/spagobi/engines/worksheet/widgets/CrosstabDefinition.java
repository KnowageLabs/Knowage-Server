/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.widgets;

import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.engines.qbe.crosstable.serializer.json.CrosstabSerializationConstants;
import it.eng.spagobi.engines.worksheet.bo.Attribute;
import it.eng.spagobi.engines.worksheet.bo.Field;
import it.eng.spagobi.engines.worksheet.bo.Measure;
import it.eng.spagobi.engines.worksheet.bo.SheetContent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This class wrap the crosstab configuration state (a JSONObject) and provide parsing methods.
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class CrosstabDefinition extends SheetContent {
	
	public static CrosstabDefinition EMPTY_CROSSTAB;
	
	static {
		EMPTY_CROSSTAB = new CrosstabDefinition();
		EMPTY_CROSSTAB.setColumns(new ArrayList<CrosstabDefinition.Column>());
		EMPTY_CROSSTAB.setRows(new ArrayList<CrosstabDefinition.Row>());
		EMPTY_CROSSTAB.setMeasures(new ArrayList<Measure>());
		EMPTY_CROSSTAB.setConfig(new JSONObject());
		EMPTY_CROSSTAB.setCalculatedFields(new JSONArray());
	}
	
	private int cellLimit;
	private List<Row> rows = null;
	private List<Column> columns = null;
	private List<Measure> measures = null;
	private JSONObject config = null;
	private JSONArray calculatedFields = null;
	private JSONObject additionalData = null;
	private boolean isStatic = true;
	
	public CrosstabDefinition() {
		cellLimit = new Integer((String) ConfigSingleton.getInstance().getAttribute("QBE.QBE-CROSSTAB-CELLS-LIMIT.value")) ;
	}
	
	public int getCellLimit() {
		return cellLimit;
	}

	public void setCellLimit(int cellLimit) {
		this.cellLimit = cellLimit;
	}
	
	public List<Row> getRows() {
		return rows;
	}

	public void setRows(List<Row> rows) {
		this.rows = rows;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public List<Measure> getMeasures() {
		return measures;
	}

	public void setMeasures(List<Measure> measures) {
		this.measures = measures;
	}

	public JSONObject getConfig() {
		return config;
	}

	public void setConfig(JSONObject config) {
		this.config = config;
	}

	public JSONArray getCalculatedFields() {
		return calculatedFields;
	}

	public void setCalculatedFields(JSONArray calculatedFields) {
		this.calculatedFields = calculatedFields;
	}

	public boolean isMeasuresOnRows() {
		String value = config.optString(CrosstabSerializationConstants.MEASURESON);
		if (value != null) {
			return value.equalsIgnoreCase("rows");
		} else return false;
	}
	
	public boolean isMeasuresOnColumns() {
		String value = config.optString(CrosstabSerializationConstants.MEASURESON);
		if (value != null) {
			return value.equalsIgnoreCase("columns");
		} else return true;
	}
	
	public class Row extends Attribute {
		
		public Row(String entityId, String alias, String iconCls, String nature, String values) {
			super(entityId, alias, iconCls, nature, values);
		}
		public Row(Attribute attribute) {
			super(attribute.getEntityId(), attribute.getAlias(), attribute.getIconCls(), attribute.getNature(), attribute.getValues());
		}
	}
	
	public class Column extends Attribute {
		public Column(String entityId, String alias, String iconCls, String nature, String values) {
			super(entityId, alias, iconCls, nature, values);
		}
		public Column(Attribute attribute) {
			super(attribute.getEntityId(), attribute.getAlias(), attribute.getIconCls(), attribute.getNature(), attribute.getValues());
		}
	}

	@Override
	public List<Attribute> getFiltersOnDomainValues() {
		List<Attribute> toReturn = new ArrayList<Attribute>();
		List<Row> rows = getRows();
		Iterator<Row> rowsIt = rows.iterator();
		while (rowsIt.hasNext()) {
			Row row = rowsIt.next();
			String values = row.getValues();
			if (values != null && !values.equals(new JSONArray().toString())) {
				toReturn.add(row);
			}
		}
		
		List<Column> columns = getColumns();
		Iterator<Column> columnsIt = columns.iterator();
		while (columnsIt.hasNext()) {
			Column column = columnsIt.next();
			String values = column.getValues();
			if (values != null && !values.equals(new JSONArray().toString())) {
				toReturn.add(column);
			}
		}

		return toReturn;
	}

	@Override
	public List<Field> getAllFields() {
		List<Field> toReturn = new ArrayList<Field>();
		toReturn.addAll(getColumns());
		toReturn.addAll(getRows());
		toReturn.addAll(getMeasures());
		return toReturn;
	}

	public JSONObject getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(JSONObject additionalData) {
		this.additionalData = additionalData;
	}
	
	/**
	 * 
	 * @return true if the component is a pivot table (not a chart)
	 */
	public boolean isPivotTable(){
		String type =  config.optString("type");
		if(type!=null && type.equals(CrosstabSerializationConstants.PIVOT)){
			return true;
		}
		return false;
	}
	
	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}
	
}
