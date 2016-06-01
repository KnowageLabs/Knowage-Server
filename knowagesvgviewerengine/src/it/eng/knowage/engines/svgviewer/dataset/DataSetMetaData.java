package it.eng.knowage.engines.svgviewer.dataset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// TODO: Auto-generated Javadoc
/**
 * The Class DataSetMetaData.
 *
 * @author Andrea Gioia (andrea.gioia@eng.it)
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
	public String getGeoIdColumnName(String hierarchyName) {
		Set names = getColumnNames();
		if (names != null) {
			Iterator it = names.iterator();
			while (it.hasNext()) {
				String columnName = (String) it.next();
				if (isGeoIdColumn(columnName)) {
					if (hierarchyName.equalsIgnoreCase(getColumnProperty(columnName, "hierarchy"))) {
						return getColumnProperty(columnName, "column_id");
					}
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
