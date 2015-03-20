/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.dataset.crosstab;

import it.eng.spago.configuration.ConfigSingleton;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class CrosstabDefinition  {
	
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
		//cellLimit = new Integer((String) ConfigSingleton.getInstance().getAttribute("QBE.QBE-CROSSTAB-CELLS-LIMIT.value")) ;
		cellLimit = new Integer("1000") ;

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
