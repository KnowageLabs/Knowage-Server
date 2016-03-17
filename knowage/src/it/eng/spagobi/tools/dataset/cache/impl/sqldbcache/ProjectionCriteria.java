/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.tools.dataset.cache.impl.sqldbcache;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class ProjectionCriteria {

	// Projection for the SELECT clause
	String dataset; // the dataset name
	String columnName; // the column name
	String aggregateFunction; // optional aggregate function like SUM, AVG, etc...
	String aliasName; // alias for the column

	// ORDER field
	// https://production.eng.it/jira/browse/KNOWAGE-149
	String orderType;

	/**
	 * The property (field) that serves as a keeper of the ordering column for the first category of the chart document (if set). *
	 * 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	String orderColumn;

	public ProjectionCriteria(String dataset, String columnName, String aggregateFunction, String aliasName) {
		this.dataset = dataset;
		this.columnName = columnName;
		this.aggregateFunction = aggregateFunction;
		// if (aggregateFunction != null) {
		// this.aliasName = aggregateFunction + "(" + aliasName + ")";
		// } else {
		this.aliasName = aliasName;
		// }
	}

	public ProjectionCriteria(String dataset, String columnName, String aggregateFunction, String aliasName, String orderType) {
		this.dataset = dataset;
		this.columnName = columnName;
		this.aggregateFunction = aggregateFunction;
		// if (aggregateFunction != null) {
		// this.aliasName = aggregateFunction + "(" + aliasName + ")";
		// } else {
		this.aliasName = aliasName;
		this.orderType = orderType;
		// }
	}

	/**
	 * The new projection constructor that handles not just ordering type, but also the ordering column for the first category of the chart document, if set.
	 * 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	public ProjectionCriteria(String dataset, String columnName, String aggregateFunction, String aliasName, String orderType, String orderColumn) {
		this.dataset = dataset;
		this.columnName = columnName;
		this.aggregateFunction = aggregateFunction;
		// if (aggregateFunction != null) {
		// this.aliasName = aggregateFunction + "(" + aliasName + ")";
		// } else {
		this.aliasName = aliasName;
		this.orderType = orderType;
		this.orderColumn = orderColumn;
		// }
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
	 * @param columnName
	 *            the columnName to set
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
	 * @param aggregateFunction
	 *            the aggregateFunction to set
	 */
	public void setAggregateFunction(String aggregateFunction) {
		this.aggregateFunction = aggregateFunction;
	}

	/**
	 * @return the aliasName
	 */
	public String getAliasName() {
		return aliasName;
	}

	/**
	 * @param aliasName
	 *            the aliasName to set
	 */
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	/**
	 * Getter for the order column for the chart documents first category.
	 * 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	public String getOrderColumn() {
		return orderColumn;
	}

	/**
	 * Setter for the order column for the chart documents first category.
	 * 
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	public void setOrderColumn(String orderColumn) {
		this.orderColumn = orderColumn;
	}

}
