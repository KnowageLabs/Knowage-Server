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
package it.eng.knowage.engines.svgviewer.dataset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * The Class DataSetMetaData.
 *
 */
public class DataSetMetaData {

	/** The columns. */
	private Map columns;

	/**
	 * Instantiates a new data set meta data.
	 */
	public DataSetMetaData() {
		columns = new HashMap();
	}

	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	public Map getColumns() {
		if (columns != null) {
			return columns;
		}
		return null;
	}

	/**
	 * Gets the column names.
	 *
	 * @return the column names
	 */
	public Set getColumnNames() {
		if (columns != null) {
			return columns.keySet();
		}
		return null;
	}

	/**
	 * Gets the column names.
	 *
	 * @param type
	 *            the type
	 *
	 * @return the column names
	 */
	private Set getColumnNames(String type) {
		Set columnNames = getColumnNames();
		if (columnNames != null) {
			Set filteredColumnNames = new HashSet();
			Iterator it = columnNames.iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				if (type.equalsIgnoreCase(getColumnType(columnName))) {
					filteredColumnNames.add(columnName);
				}
			}
			return filteredColumnNames;
		}
		return null;
	}

	/**
	 * Gets the measure column names.
	 *
	 * @return the measure column names
	 */
	public Set getMeasureColumnNames() {
		return getColumnNames("measure");
	}

	/**
	 * Gets the column property.
	 *
	 * @param columnName
	 *            the column name
	 * @param propertyName
	 *            the property name
	 *
	 * @return the column property
	 */
	public String getColumnProperty(String columnName, String propertyName) {
		if (columns != null) {
			Properties properties = (Properties) columns.get(columnName);
			if (properties != null) {
				return properties.getProperty(propertyName);
			}
		}
		return null;
	}

	/**
	 * Sets the column property.
	 *
	 * @param columnName
	 *            the column name
	 * @param propertyName
	 *            the property name
	 * @param propertyValue
	 *            the property value
	 */
	public void setColumnProperty(String columnName, String propertyName, String propertyValue) {
		if (columns != null) {
			Properties properties = (Properties) columns.get(columnName);
			if (properties != null) {
				properties.setProperty(propertyName, propertyValue);
			}
		}
	}

	/**
	 * Adds the column.
	 *
	 * @param columnName
	 *            the column name
	 */
	public void addColumn(String columnName) {
		if (columns != null) {
			columns.put(columnName, new Properties());
		}
	}

	/**
	 * Gets the geo id column name.
	 *
	 * @param hierarchyName
	 *            the hierarchy name
	 *
	 * @return the geo id column name
	 */
	public String getGeoIdColumnName() {
		Set names = getColumnNames();
		if (names != null) {
			Iterator it = names.iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				if (isGeoIdColumn(columnName)) {
					return getColumnProperty(columnName, "column_id");
				}
			}
		}
		return null;
	}

	/**
	 * Gets the geo id column name.
	 *
	 * @return the geo id column name
	 */
	public String getDrillColumnName() {
		Set names = getColumnNames();
		if (names != null) {
			Iterator it = names.iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				if (isDrillColumn(columnName)) {
					return getColumnProperty(columnName, "column_id");
				}
			}
		}
		return null;
	}

	/**
	 * Gets the visibility id column name.
	 *
	 * @return the visibility id column name
	 */
	public String getVisibilityColumnName() {
		Set names = getColumnNames();
		if (names != null) {
			Iterator it = names.iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				if (isVisibilityColumn(columnName)) {
					return getColumnProperty(columnName, "column_id");
				}
			}
		}
		return null;
	}

	/**
	 * Gets the crossable id column name.
	 *
	 * @return the crossable id column name
	 */
	public String getCrossTypeColumnName() {
		Set names = getColumnNames();
		if (names != null) {
			Iterator it = names.iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				if (isCrossTypeColumn(columnName)) {
					return getColumnProperty(columnName, "column_id");
				}
			}
		}
		return null;
	}

	/**
	 * Gets the parent id column name.
	 *
	 * @return the parent id column name
	 */
	public String getParentColumnName() {
		Set names = getColumnNames();
		if (names != null) {
			Iterator it = names.iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				if (isParentColumn(columnName)) {
					return getColumnProperty(columnName, "column_id");
				}
			}
		}
		return null;
	}

	/**
	 * Gets the labels id column name.
	 *
	 * @return the geo id column name
	 */
	public String getLabelsColumnName() {
		Set names = getColumnNames();
		if (names != null) {
			Iterator it = names.iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				if (isLabelsColumn(columnName)) {
					return getColumnProperty(columnName, "column_id");
				}
			}
		}
		return null;
	}

	/**
	 * Gets the level name.
	 *
	 * @param hierarchyName
	 *            the hierarchy name
	 *
	 * @return the level name
	 */
	public String getLevelName(String hierarchyName) {
		Set names = getColumnNames();
		if (names != null) {
			Iterator it = names.iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				if (isGeoIdColumn(columnName)) {
					if (hierarchyName.equalsIgnoreCase(getColumnProperty(columnName, "hierarchy"))) {
						return getColumnProperty(columnName, "level");
					}
				}
			}
		}
		return null;
	}

	/**
	 * Gets the tooltip id column name.
	 *
	 * @return the tooltip id column name
	 */
	public String getTooltipColumnName() {
		Set names = getColumnNames();
		if (names != null) {
			Iterator it = names.iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				if (isTooltipColumn(columnName)) {
					return getColumnProperty(columnName, "column_id");
				}
			}
		}
		return null;
	}

	/**
	 * Gets the info id column name.
	 *
	 * @return the info id column name
	 */
	public String getInfoColumnName() {
		Set names = getColumnNames();
		if (names != null) {
			Iterator it = names.iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				if (isInfoColumn(columnName)) {
					return getColumnProperty(columnName, "column_id");
				}
			}
		}
		return null;
	}

	/**
	 * Gets the column type.
	 *
	 * @param columnName
	 *            the column name
	 *
	 * @return the column type
	 */
	public String getColumnType(String columnName) {
		return getColumnProperty(columnName, "type");
	}

	/**
	 * Checks if is labels id column.
	 *
	 * @param columnName
	 *            the column name
	 *
	 * @return true, if is geo id column
	 */
	public boolean isLabelsColumn(String columnName) {
		return "label".equalsIgnoreCase(getColumnType(columnName));
	}

	/**
	 * Checks if is parent id column.
	 *
	 * @param columnName
	 *            the column name
	 *
	 * @return true, if is geo id column
	 */
	public boolean isParentColumn(String columnName) {
		return "parentid".equalsIgnoreCase(getColumnType(columnName));
	}

	/**
	 * Checks if is geo id column.
	 *
	 * @param columnName
	 *            the column name
	 *
	 * @return true, if is geo id column
	 */
	public boolean isGeoIdColumn(String columnName) {
		return "geoid".equalsIgnoreCase(getColumnType(columnName));
	}

	/**
	 * Checks if is visibility column.
	 *
	 * @param columnName
	 *            the column name
	 *
	 * @return true, if is visibility column
	 */
	public boolean isVisibilityColumn(String columnName) {
		return "visibility".equalsIgnoreCase(getColumnType(columnName));
	}

	/**
	 * Checks if is crossable column.
	 *
	 * @param columnName
	 *            the column name
	 *
	 * @return true, if is crossable column
	 */
	public boolean isCrossTypeColumn(String columnName) {
		return "crosstype".equalsIgnoreCase(getColumnType(columnName));
	}

	/**
	 * Checks if is drill column.
	 *
	 * @param columnName
	 *            the column name
	 *
	 * @return true, if is drill column
	 */
	public boolean isDrillColumn(String columnName) {
		return "drillid".equalsIgnoreCase(getColumnType(columnName));
	}

	/**
	 * Checks if is tooltip column.
	 *
	 * @param columnName
	 *            the column name
	 *
	 * @return true, if is tooltip column
	 */
	public boolean isTooltipColumn(String columnName) {
		return "tooltip".equalsIgnoreCase(getColumnType(columnName));
	}

	/**
	 * Checks if is info column.
	 *
	 * @param columnName
	 *            the column name
	 *
	 * @return true, if is info column
	 */
	public boolean isInfoColumn(String columnName) {
		return "info".equalsIgnoreCase(getColumnType(columnName));
	}

	/**
	 * Gets the aggregation function.
	 *
	 * @param columnName
	 *            the column name
	 *
	 * @return the aggregation function
	 */
	public String getAggregationFunction(String columnName) {
		return getColumnProperty(columnName, "func");
	}

}
