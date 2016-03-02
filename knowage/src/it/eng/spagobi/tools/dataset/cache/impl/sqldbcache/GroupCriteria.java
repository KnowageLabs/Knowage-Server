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
package it.eng.spagobi.tools.dataset.cache.impl.sqldbcache;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class GroupCriteria {
	
	//For the GROUP BY clause
	
	String dataset;
	String columnName; //the column name
	String aggregateFunction; //optional aggregate function like SUM, AVG, etc...
	
	
	
	/**
	 * @param columnName
	 * @param aggregateFunction
	 */
	public GroupCriteria(String dataset, String columnName, String aggregateFunction) {
		this.dataset = dataset;
		this.columnName = columnName;
		this.aggregateFunction = aggregateFunction;
	}
	
	
	
	public String getDataset() {
		return dataset;
	}



	public void setDataset(String dataset) {
		this.dataset = dataset;
	}



	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}
	/**
	 * @param columnName the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	/**
	 * @return the aggregateFunction
	 */
	public String getAggregateFunction() {
		return aggregateFunction;
	}
	/**
	 * @param aggregateFunction the aggregateFunction to set
	 */
	public void setAggregateFunction(String aggregateFunction) {
		this.aggregateFunction = aggregateFunction;
	}
	
	
}
