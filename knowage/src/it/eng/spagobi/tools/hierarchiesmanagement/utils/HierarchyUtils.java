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
package it.eng.spagobi.tools.hierarchiesmanagement.utils;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchyTreeNodeData;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Filter;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Hierarchy;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HierarchyUtils {

	private static Logger logger = Logger.getLogger(HierarchyUtils.class);

	/**
	 * This method creates a JSON from a Hierarchy field
	 *
	 * @param field
	 *            Field read from hierarchies config
	 * @param isHierarchyField
	 *            to manage differences between dimensions and hierarchies fields
	 * @return a JSON that represents a field
	 * @throws JSONException
	 */
	private static JSONObject createJSONObjectFromField(Field field, boolean isHierarchyField) throws JSONException {

		logger.debug("START");

		JSONObject result = new JSONObject();

		Assert.assertNotNull(field, "Impossible to create a JSON from a null field");

		if (field.getId() != null)
			result.put(HierarchyConstants.FIELD_ID, field.getId());
		logger.debug("Field [" + HierarchyConstants.FIELD_ID + "] is " + field.getId());

		if (field.getName() != null)
			result.put(HierarchyConstants.FIELD_NAME, field.getName());
		logger.debug("Field [" + HierarchyConstants.FIELD_NAME + "] is " + field.getName());

		if (field.getFixValue() != null)
			result.put(HierarchyConstants.FIELD_FIX_VALUE, field.getFixValue());
		logger.debug("Field [" + HierarchyConstants.FIELD_FIX_VALUE + "] is " + field.getFixValue());

		result.put(HierarchyConstants.FIELD_VISIBLE, field.isVisible());
		logger.debug("Field [" + HierarchyConstants.FIELD_VISIBLE + "] is " + field.isVisible());

		result.put(HierarchyConstants.FIELD_EDITABLE, field.isEditable());
		logger.debug("Field [" + HierarchyConstants.FIELD_EDITABLE + "] is " + field.isEditable());

		result.put(HierarchyConstants.FIELD_PARENT, field.isParent());
		logger.debug("Field [" + HierarchyConstants.FIELD_PARENT + "] is " + field.isParent());

		if (field.getType() != null)
			result.put(HierarchyConstants.FIELD_TYPE, field.getType());
		logger.debug("Field [" + HierarchyConstants.FIELD_TYPE + "] is " + field.getType());

		if (isHierarchyField) { // add these values only for hierarchies fields

			logger.debug("This Field is a Hierarchy field");

			result.put(HierarchyConstants.FIELD_SINGLE_VALUE, field.isSingleValue());
			logger.debug("Field [" + HierarchyConstants.FIELD_SINGLE_VALUE + "] is " + field.isSingleValue());

			result.put(HierarchyConstants.FIELD_REQUIRED, field.isRequired());
			logger.debug("Field [" + HierarchyConstants.FIELD_REQUIRED + "] is " + field.isRequired());

			result.put(HierarchyConstants.FIELD_IS_ORDER, field.isOrderField());
			logger.debug("Field [" + HierarchyConstants.FIELD_IS_ORDER + "] is " + field.isOrderField());
		}

		logger.debug("END");
		return result;

	}

	/**
	 * This method creates a JSON from a Hierarchy filter
	 *
	 * @param filter
	 *            Filter read from hierarchies config
	 * @return a JSON that represents a filter
	 * @throws JSONException
	 */
	private static JSONObject createJSONObjectFromFilter(Filter filter) throws JSONException {

		logger.debug("START");

		JSONObject result = new JSONObject();

		Assert.assertNotNull(filter, "Impossible to create a JSON from a null filter");

		if (filter.getName() != null)
			result.put(HierarchyConstants.FILTER_NAME, filter.getName());
		logger.debug("Filter [" + HierarchyConstants.FILTER_NAME + "] is " + filter.getName());

		if (filter.getType() != null)
			result.put(HierarchyConstants.FILTER_TYPE, filter.getType());
		logger.debug("Filter [" + HierarchyConstants.FILTER_TYPE + "] is " + filter.getType());

		if (filter.getDefaultValue() != null)
			result.put(HierarchyConstants.FILTER_DEFAULT, filter.getDefaultValue());
		logger.debug("Filter [" + HierarchyConstants.FILTER_DEFAULT + "] is " + filter.getDefaultValue());

		HashMap<String, String> conditions = filter.getConditions();
		for (int i = 1; i <= conditions.size(); i++) {
			String key = HierarchyConstants.FILTER_CONDITION + i;
			String value = conditions.get(key);
			result.put(key, value);
			logger.debug("Filter [" + HierarchyConstants.FILTER_CONDITION + i + "] is " + filter.getConditions());
		}

		logger.debug("END");
		return result;

	}

	/**
	 * This method creates a JSON array from a list of fields
	 *
	 * @param fields
	 *            List of Fields read from hierarchies config
	 * @param hierarchyFields
	 *            to manage differences between dimensions and hierarchies fields
	 * @return a JSON array that represents fields
	 * @throws JSONException
	 */
	public static JSONArray createJSONArrayFromFieldsList(List<Field> fields, boolean hierarchyFields) throws JSONException {

		logger.debug("START");

		JSONArray result = new JSONArray();

		for (Field tmpField : fields) {
			result.put(createJSONObjectFromField(tmpField, hierarchyFields));
		}

		logger.debug("END");
		return result;

	}

	/**
	 * This method creates a JSON array from a list of filters
	 *
	 * @param filters
	 *            List of Filters read from hierarchies config
	 * @return a JSON array that represents filters
	 * @throws JSONException
	 */
	public static JSONArray createJSONArrayFromFiltersList(List<Filter> filters) throws JSONException {
		logger.debug("START");

		JSONArray result = new JSONArray();

		for (Filter tmpFilter : filters) {
			result.put(createJSONObjectFromFilter(tmpFilter));
		}

		logger.debug("END");
		return result;
	}

	/**
	 * This method creates a JSON array from a list of fields
	 *
	 * @param fields
	 *            List of Fields read from hierarchies config
	 * @param hierarchyFields
	 *            to manage differences between dimensions and hierarchies fields
	 * @return a JSON array that represents fields
	 * @throws JSONException
	 */
	public static JSONObject createJSONArrayFromHashMap(HashMap values, JSONObject result) throws JSONException {

		logger.debug("START");

		if (result == null)
			result = new JSONObject();

		Iterator iter = values.keySet().iterator();

		while (iter.hasNext()) {
			String key = (String) iter.next();
			Object value = values.get(key);
			if (key != null && value != null && !result.has(key))
				result.put(key, value);
			logger.debug("Field [" + key + "] is " + value);
		}

		logger.debug("END");
		return result;

	}

	/**
	 * Returns the datasource object referenced to the dimension in input
	 *
	 * @param dimension
	 * @return
	 * @throws SpagoBIServiceException
	 */
	public static IDataSource getDataSource(String dimension) throws SpagoBIServiceException {
		Hierarchies hierarchies = HierarchiesSingleton.getInstance();
		String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
		try {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}
			return dataSource;
		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving hierarchy datasource informations");
			throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchy datasource informations", t);
		}

	}

	/**
	 * This method find the max int value of the table primary key
	 *
	 * @param primaryKey
	 *            of the table, name of the table, connection to the db
	 * @return -1 if it finds a result, >= 0 otherwise
	 * @throws SQLException
	 */
	public static int getCountId(String primaryKey, String hierTableName, Connection dbConnection, IDataSource dataSource) {
		if (primaryKey == null || hierTableName == null) {
			return -1;
		}
		Integer countId = null;
		String primaryKeyColum = AbstractJDBCDataset.encapsulateColumnName(primaryKey, dataSource);
		String query = "SELECT MAX(" + primaryKeyColum + ") as COUNT_ID FROM " + hierTableName;
		try {
			PreparedStatement stm = dbConnection.prepareStatement(query);
			ResultSet resultSet = stm.executeQuery();
			if (resultSet.next()) {
				countId = resultSet.getInt("COUNT_ID");
			}
		} catch (SQLException se) {
			logger.error("Error while retrieve id with stmt: [" + query.toString() + "]");
			return -1;
		}

		return countId == null ? -1 : countId.intValue();
	}

	/**
	 * This method creates a JSON array with ids of all visible fields
	 *
	 * @param fields
	 * @return a JSON array with ids of all visible fields
	 * @throws JSONException
	 */
	public static JSONArray createColumnsSearch(List<Field> fields) throws JSONException {

		logger.debug("START");

		JSONArray result = new JSONArray();

		for (Field tmpField : fields) {
			if (tmpField.isVisible()) {
				result.put(tmpField.getId());
			}
		}

		logger.debug("END");
		return result;
	}

	/**
	 * This method converts a date in a correct format for the datasource
	 *
	 * @param dateToConvert
	 * @param dataSource
	 * @return the converted date
	 */
	public static String getConvertedDate(String dateToConvert, IDataSource dataSource) {
		logger.debug("START");

		// defining date conversion
		String format = (SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"));
		String convertedDate = "";
		String actualDialect = dataSource.getHibDialectClass();
		if (HierarchyConstants.DIALECT_MYSQL.equalsIgnoreCase(actualDialect)) {
			convertedDate = "STR_TO_DATE('" + dateToConvert + "','" + format + "')";
		} else if (HierarchyConstants.DIALECT_POSTGRES.equalsIgnoreCase(actualDialect)) {
			convertedDate = "TO_DATE('" + dateToConvert + "','" + format + "')";
		} else if (HierarchyConstants.DIALECT_ORACLE.equalsIgnoreCase(actualDialect) || HierarchyConstants.DIALECT_ORACLE9i10g.equalsIgnoreCase(actualDialect)) {
			convertedDate = "TO_DATE('" + dateToConvert + "','" + format + "')";
		} else if (HierarchyConstants.DIALECT_HSQL.equalsIgnoreCase(actualDialect)) {
			convertedDate = "TO_DATE('" + dateToConvert + "','" + format + "')";
		} else if (HierarchyConstants.DIALECT_SQLSERVER.equalsIgnoreCase(actualDialect)) {
			convertedDate = "TO_DATE('" + dateToConvert + "','" + format + "')";
		} else if (HierarchyConstants.DIALECT_INGRES.equalsIgnoreCase(actualDialect)) {
			convertedDate = "DATE('" + dateToConvert + "')";
		} else if (HierarchyConstants.DIALECT_TERADATA.equalsIgnoreCase(actualDialect)) {
			convertedDate = "'" + dateToConvert + "',AS DATE FORMAT '" + format + "')";
		}

		logger.debug("END");
		return convertedDate;
	}

	/**
	 * Return a 'fixed' condition for test validity post the input date (used by trees)
	 *
	 * @param dataSource
	 * @param filterDate
	 * @param beginDtColumn
	 * @return String with condition
	 */
	public static String createDateAfterCondition(IDataSource dataSource, String filterDate, String beginDtColumn) {

		String fDateConverted = HierarchyUtils.getConvertedDate(filterDate, dataSource);
		String dateAfterCondition = " AND " + beginDtColumn + " >= " + fDateConverted;

		return dateAfterCondition;

	}

	/**
	 * Return a condition for test validity on a date (used by dimension OPTIONAL filters)
	 *
	 * @param dataSource
	 * @param filterConditions
	 *            : an hashmap with one or more conditions that will added on the input date
	 * @param filterValue
	 *            : the value of the date to test
	 * @return String with conditions
	 */
	public static String createDateFilterCondition(IDataSource dataSource, HashMap<String, String> filterConditions, String filterValue) {
		String toReturn = "";
		String fDateConverted = HierarchyUtils.getConvertedDate(filterValue, dataSource);

		for (int c = 1; c <= filterConditions.size(); c++) {
			String condition = filterConditions.get(HierarchyConstants.FILTER_CONDITION + c);
			toReturn += " AND " + condition + " " + fDateConverted;
		}
		return toReturn;

	}

	/**
	 * Return a condition for test validity on a string (used by dimension OPTIONAL filters)
	 *
	 * @param dataSource
	 * @param filterConditions
	 *            : an hashmap with one or more conditions that will added on the input value
	 * @param filterValue
	 *            : the string value to test
	 * @return String with conditions
	 */
	public static String createStringFilterCondition(IDataSource dataSource, HashMap<String, String> filterConditions, String filterValue) {
		String toReturn = "";

		for (int c = 1; c <= filterConditions.size(); c++) {
			String condition = filterConditions.get(HierarchyConstants.FILTER_CONDITION + c);
			toReturn += " AND " + condition + " '" + filterValue + "'";
		}
		return toReturn;

	}

	/**
	 * Return a condition for test validity on a number (used by dimension OPTIONAL filters)
	 *
	 * @param dataSource
	 * @param filterConditions
	 *            : an hashmap with one or more conditions that will added on the input value
	 * @param filterValue
	 *            : the numeric value to test
	 * @return String with conditions
	 */
	public static String createNumberFilterCondition(IDataSource dataSource, HashMap<String, String> filterConditions, String filterValue) {
		String toReturn = "";

		for (int c = 1; c <= filterConditions.size(); c++) {
			String condition = filterConditions.get(HierarchyConstants.FILTER_CONDITION + c);
			toReturn += " AND " + condition + " " + filterValue;
		}
		return toReturn;

	}

	public static String createInHierarchyCondition(IDataSource dataSource, String hierarchyTable, String hierNameCol, String hierarchyName,
			String hierTypeCol, String hierarchyType, String dimFilterFieldCol, String selectFilterField, String vFilterDateWhereClause, boolean exludeHierLeaf) {

		logger.debug("Filter Hierarchy [" + hierarchyName + "]");

		StringBuffer query = new StringBuffer();
		String clauseIn = (exludeHierLeaf) ? " NOT IN " : " IN ";
		if (vFilterDateWhereClause != null && !vFilterDateWhereClause.equals("")) {
			vFilterDateWhereClause = " AND " + vFilterDateWhereClause;
		} else
			vFilterDateWhereClause = "";

		query.append(" AND " + dimFilterFieldCol + clauseIn + " (SELECT " + selectFilterField + " FROM " + hierarchyTable);
		query.append(" WHERE " + hierNameCol + " = '" + hierarchyName + "' AND " + hierTypeCol + " = '" + hierarchyType + "' " + vFilterDateWhereClause + " )");

		return query.toString();
	}

	public static JSONArray createRootData(IDataStore dataStore) throws JSONException {

		logger.debug("START");

		JSONArray rootArray = new JSONArray();

		IMetaData columnsMetaData = dataStore.getMetaData();

		Iterator iterator = dataStore.iterator();

		while (iterator.hasNext()) {

			IRecord record = (IRecord) iterator.next();
			List<IField> recordFields = record.getFields();

			JSONObject tmpJSON = new JSONObject();

			for (int i = 0; i < recordFields.size(); i++) {

				IField tmpField = recordFields.get(i);

				String tmpKey = columnsMetaData.getFieldName(i);
				tmpJSON.put(tmpKey, tmpField.getValue());
			}

			rootArray.put(tmpJSON);
		}

		logger.debug("END");
		return rootArray;
	}

	public static IDataStore getDimensionDataStore(IDataSource dataSource, String dimensionName, List<Field> metadataFields, String validityDate,
			String optionalFilter, String filterDate, String filterHierarchy, String filterHierType, String hierTableName, String prefix, boolean exludeHierLeaf) {

		String dimFilterField = prefix + HierarchyConstants.DIM_FILTER_FIELD;
		String hierFilterField = prefix + HierarchyConstants.SELECT_HIER_FILTER_FIELD;

		// 1 - execute query to get dimension data
		String queryText = HierarchyUtils.createDimensionDataQuery(dataSource, metadataFields, dimensionName, validityDate, optionalFilter, filterDate,
				filterHierarchy, filterHierType, hierTableName, dimFilterField, hierFilterField, exludeHierLeaf);

		IDataStore dataStore = dataSource.executeStatement(queryText, 0, 0);

		return dataStore;
	}

	public static IDataStore getDimensionFromHierDataStore(IDataSource dataSource, String dimensionName, List<Field> metadataFields, String validityDate,
			String optionalFilter, String filterDate, String filterHierarchy, String filterHierType, String hierTableName, String prefix, boolean exludeHierLeaf) {

		String dimFilterField = prefix + HierarchyConstants.DIM_FILTER_FIELD;
		String hierFilterField = prefix + HierarchyConstants.SELECT_HIER_FILTER_FIELD;

		// 1 - execute query to get dimension data
		String queryText = HierarchyUtils.createDimensionFromHierDataQuery(dataSource, metadataFields, dimensionName, validityDate, optionalFilter, filterDate,
				filterHierarchy, filterHierType, hierTableName, dimFilterField, hierFilterField, exludeHierLeaf);

		IDataStore dataStore = dataSource.executeStatement(queryText, 0, 0);

		return dataStore;
	}

	public static Map<String, Integer> getMetadataFieldsMap(List<Field> metadataFields) {

		Map<String, Integer> resultMap = new HashMap<String, Integer>();

		for (int i = 0; i < metadataFields.size(); i++) {

			Field tmpField = metadataFields.get(i);
			resultMap.put(tmpField.getId(), i);
		}

		return resultMap;
	}

	private static String createDimensionDataQuery(IDataSource dataSource, List<Field> metadataFields, String dimensionName, String validityDate,
			String optionalFilter, String filterDate, String filterHierarchy, String filterHierType, String hierTableName, String dimFilterField,
			String hierFilterField, boolean exludeHierLeaf) {

		logger.debug("START");

		StringBuffer query = getDimensionQuerySB(dataSource, metadataFields, dimensionName, validityDate, optionalFilter, filterDate, filterHierarchy,
				filterHierType, hierTableName, dimFilterField, hierFilterField, exludeHierLeaf);

		logger.debug("Query for dimension data is: " + query);
		logger.debug("END");
		return query.toString();
	}

	private static String createDimensionFromHierDataQuery(IDataSource dataSource, List<Field> metadataFields, String dimensionName, String validityDate,
			String optionalFilter, String filterDate, String filterHierarchy, String filterHierType, String hierTableName, String dimFilterField,
			String hierFilterField, boolean exludeHierLeaf) {

		logger.debug("START");

		StringBuffer query = getDimensionFromHierQuerySB(dataSource, metadataFields, dimensionName, validityDate, optionalFilter, filterDate, filterHierarchy,
				filterHierType, hierTableName, dimFilterField, hierFilterField, exludeHierLeaf);

		logger.debug("Query for dimension data is: " + query);
		logger.debug("END");
		return query.toString();
	}

	private static StringBuffer getDimensionQuerySB(IDataSource dataSource, List<Field> metadataFields, String dimensionName, String validityDate,
			String optionalFilter, String filterDate, String filterHierarchy, String filterHierType, String hierTableName, String dimFilterField,
			String hierFilterField, boolean exludeHierLeaf) {

		logger.debug("START");

		StringBuffer selectClauseBuffer = new StringBuffer(" ");
		String sep = ",";

		int fieldsSize = metadataFields.size();

		for (int i = 0; i < fieldsSize; i++) {
			Field tmpField = metadataFields.get(i);
			String column = AbstractJDBCDataset.encapsulateColumnName(tmpField.getId(), dataSource);

			if (i == fieldsSize - 1) {
				sep = " ";
			}

			selectClauseBuffer.append(column + sep);
		}

		String selectClause = selectClauseBuffer.toString();

		// where
		String beginDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BEGIN_DT, dataSource);
		String endDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.END_DT, dataSource);

		String vDateConverted = HierarchyUtils.getConvertedDate(validityDate, dataSource);
		String vDateWhereClause = vDateConverted + ">= " + beginDtColumn + " AND " + vDateConverted + " <= " + endDtColumn;

		StringBuffer query = new StringBuffer("SELECT " + selectClause + " FROM " + dimensionName + " WHERE " + vDateWhereClause);

		if (optionalFilter != null) {
			logger.debug("Optional Filters are [" + optionalFilter + "]");

			// get optional filters and add them to the query
			JSONArray filtersJSONArray = ObjectUtils.toJSONArray(optionalFilter);
			try {
				for (int i = 1; i <= filtersJSONArray.length(); i++) {
					JSONObject filter = filtersJSONArray.getJSONObject(i - 1);
					String filterType = (String) filter.get(HierarchyConstants.FILTER_TYPE);
					String filterValue = (!filter.isNull(HierarchyConstants.FILTER_VALUE)) ? filter.get(HierarchyConstants.FILTER_VALUE).toString() : null;
					HashMap<String, String> filterCondition = new HashMap<String, String>();
					int cIdx = 1;
					while (!filter.isNull(HierarchyConstants.FILTER_CONDITION + cIdx)) {
						filterCondition.put(HierarchyConstants.FILTER_CONDITION + cIdx, (String) filter.get(HierarchyConstants.FILTER_CONDITION + cIdx));
						cIdx++;
					}
					String filterString = "";
					if (filterValue != null) {
						if (filterType.equals(HierarchyConstants.FIELD_TP_STRING)) {
							filterString = HierarchyUtils.createStringFilterCondition(dataSource, filterCondition, filterValue);
						} else if (filterType.equals(HierarchyConstants.FIELD_TP_NUMBER)) {
							filterString = HierarchyUtils.createNumberFilterCondition(dataSource, filterCondition, filterValue);
						} else if (filterType.equals(HierarchyConstants.FIELD_TP_DATE)) {
							filterString = HierarchyUtils.createDateFilterCondition(dataSource, filterCondition, filterValue);
						}
						query.append(filterString);
					}
				}
			} catch (JSONException je) {
				logger.error("Error while getting optional filters. Error: " + je);
				throw new SpagoBIServiceException("An unexpected error occured while deserializing optional filters structure from JSON", je);
			}
		}

		if (filterHierarchy != null) {
			logger.debug("Filter Hierarchy is [" + filterHierarchy + "]");
			String vFilterDateConverted = HierarchyUtils.getConvertedDate(filterDate, dataSource);
			String vFilterDateWhereClause = vFilterDateConverted + ">= " + beginDtColumn + " AND " + vFilterDateConverted + " <= " + endDtColumn;

			String hierNameCol = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
			String hierTypeCol = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_TP, dataSource);
			String dimFilterFieldCol = AbstractJDBCDataset.encapsulateColumnName(dimFilterField, dataSource);
			String selectFilterField = AbstractJDBCDataset.encapsulateColumnName(hierFilterField, dataSource);

			query.append(HierarchyUtils.createInHierarchyCondition(dataSource, hierTableName, hierNameCol, filterHierarchy, hierTypeCol, filterHierType,
					dimFilterFieldCol, selectFilterField, vFilterDateWhereClause, exludeHierLeaf));
		}

		logger.debug("END");
		return query;
	}

	private static StringBuffer getDimensionFromHierQuerySB(IDataSource dataSource, List<Field> metadataFields, String dimensionName, String validityDate,
			String optionalFilter, String filterDate, String filterHierarchy, String filterHierType, String hierTableName, String dimFilterField,
			String hierFilterField, boolean exludeHierLeaf) {
		logger.debug("START");

		String selectClause = getDimenensionSelectClause(dataSource, metadataFields);

		// where
		StringBuffer query = new StringBuffer("SELECT " + selectClause + " FROM " + dimensionName + " WHERE 1=1 ");

		String beginDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BEGIN_DT, dataSource);
		String endDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.END_DT, dataSource);
		String hierNameCol = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
		String hierTypeCol = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_TP, dataSource);
		String dimFilterFieldCol = AbstractJDBCDataset.encapsulateColumnName(dimFilterField, dataSource);
		String selectFilterField = AbstractJDBCDataset.encapsulateColumnName(hierFilterField, dataSource);
		String vFilterDateConverted = HierarchyUtils.getConvertedDate(filterDate, dataSource);
		String vFilterDateWhereClause = vFilterDateConverted + ">= " + beginDtColumn + " AND " + vFilterDateConverted + " <= " + endDtColumn;

		query.append(HierarchyUtils.createInHierarchyCondition(dataSource, hierTableName, hierNameCol, filterHierarchy, hierTypeCol, filterHierType,
				dimFilterFieldCol, selectFilterField, vFilterDateWhereClause, false));

		logger.debug("END");
		return query;
	}

	private static String getDimenensionSelectClause(IDataSource dataSource, List<Field> metadataFields) {
		String toReturn = "";
		logger.debug("START");

		StringBuffer selectClauseBuffer = new StringBuffer(" ");
		String sep = ",";

		int fieldsSize = metadataFields.size();

		for (int i = 0; i < fieldsSize; i++) {
			Field tmpField = metadataFields.get(i);
			String column = AbstractJDBCDataset.encapsulateColumnName(tmpField.getId(), dataSource);

			if (i == fieldsSize - 1) {
				sep = " ";
			}

			selectClauseBuffer.append(column + sep);
		}

		toReturn = selectClauseBuffer.toString();

		return toReturn;
	}

	public static List<Field> createBkpFields(List<Field> genFields, String[] bkpGenFields) {

		logger.debug("START");

		List<Field> result = new ArrayList<Field>();

		// Create fixed fields (code, name, description and timestamp where only name and description are editable)
		Field bkpField = new Field(HierarchyConstants.HIER_CD, "Code", "String", null, true, false, false, true, false, false, false);
		result.add(bkpField);
		bkpField = new Field(HierarchyConstants.HIER_NM, "Name", "String", null, true, true, false, true, false, false, false);
		result.add(bkpField);
		bkpField = new Field(HierarchyConstants.HIER_DS, "Description", "String", null, true, true, false, true, false, false, false);
		result.add(bkpField);
		// ...then we build a field for the others backup info
		bkpField = new Field(HierarchyConstants.BKP_TIMESTAMP_COLUMN, "Date", "Date", null, true, false, false, true, false, false, false);
		result.add(bkpField);

		logger.debug("END");
		return result;
	}

	public static boolean deleteHierarchy(String dimension, String hierarchyName, IDataSource dataSource, Connection connection) throws SQLException {
		// delete hierarchy
		logger.debug("START");

		try {
			logger.debug("Preparing delete statement. Name of the hierarchy is [" + hierarchyName + "]");

			// 1 - get hierarchy table postfix(ex: _CDC)
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();

			// 2 - create query text
			String hierarchyNameCol = AbstractJDBCDataset.encapsulateColumnName("HIER_NM", dataSource);
			String tableName = hierarchies.getHierarchyTableName(dimension);
			String queryText = "DELETE FROM " + tableName + " WHERE " + hierarchyNameCol + "='" + hierarchyName + "' ";

			logger.debug("The delete query is [" + queryText + "]");

			// 3 - Execute DELETE statement
			Statement statement = connection.createStatement();
			statement.executeUpdate(queryText);
			statement.close();

			logger.debug("Delete query successfully executed");
			logger.debug("END");

		} catch (Throwable t) {
			logger.error("An unexpected error occured while deleting custom hierarchy");
			throw new SpagoBIServiceException("An unexpected error occured while deleting custom hierarchy", t);
		}

		return true;
	}

	/**
	 *
	 * @param lstFields
	 * @param name
	 * @return the position of the field with the input name in order to the stmt
	 */
	public static int getPosField(LinkedHashMap<String, String> lstFields, String name) {
		int toReturn = 1;

		for (String key : lstFields.keySet()) {
			if (key.equalsIgnoreCase(name))
				return toReturn;

			toReturn++;
		}
		logger.info("Attribute '" + name + "' non found in fields' list ");
		return -1;
	}

	public static int getTotalNodeFieldsNumber(int totalLevels, List<Field> nodeMetadataFields) {
		int toReturn = 0;

		for (int i = 0, l = nodeMetadataFields.size(); i < l; i++) {
			Field f = nodeMetadataFields.get(i);
			if (f.isSingleValue()) {
				toReturn += 1;
			} else {
				toReturn += totalLevels;
			}
		}

		return toReturn;
	}

	/**
	 * Sets records' value to the tree structure (leaf informations, date and strings)
	 */
	public static HierarchyTreeNodeData setDataValues(String dimension, String nodeCode, HierarchyTreeNodeData data, IRecord record, IMetaData metadata) {
		// inject leafID into node

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();

		IField leafIdField = record.getFieldAt(metadata.getFieldIndex(hierarchies.getHierarchyTableForeignKeyName(dimension)));
		String leafIdString = null;
		if (leafIdField.getValue() instanceof Integer) {
			Integer leafId = (Integer) leafIdField.getValue();
			leafIdString = String.valueOf(leafId);
		} else if (leafIdField.getValue() instanceof Long) {
			Long leafId = (Long) leafIdField.getValue();
			leafIdString = String.valueOf(leafId);
		}
		data.setLeafId(leafIdString);

		IField leafParentCodeField = record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.LEAF_PARENT_CD));
		String leafParentCodeString = (String) leafParentCodeField.getValue();
		// data.setNodeCode(leafParentCodeString + "_" + nodeCode); //anto
		data.setNodeCode(nodeCode);
		// nodeCode = leafParentCodeString + "_" + nodeCode; //anto
		data.setLeafParentCode(leafParentCodeString);
		// data.setLeafOriginalParentCode(leafParentCodeString); // backup code

		IField leafParentNameField = record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.LEAF_PARENT_NM));
		String leafParentNameString = (String) leafParentNameField.getValue();
		data.setLeafParentName(leafParentNameString);

		IField beginDtField = record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.BEGIN_DT));
		Date beginDtDate = null;
		if (beginDtField.getValue() instanceof Timestamp) {
			Timestamp timestamp = (Timestamp) beginDtField.getValue();
			beginDtDate = new Date(timestamp.getTime());
		} else {
			beginDtDate = (Date) beginDtField.getValue();
		}
		data.setBeginDt(beginDtDate);

		IField endDtField = record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.END_DT));
		Date endDtDate = null;
		if (endDtField.getValue() instanceof Timestamp) {
			Timestamp timestamp = (Timestamp) endDtField.getValue();
			endDtDate = new Date(timestamp.getTime());
		} else {
			endDtDate = (Date) beginDtField.getValue();
		}
		data.setEndDt(endDtDate);

		HashMap mapAttrs = new HashMap();
		int numLevels = Integer.valueOf((String) hierarchies.getConfig(dimension).get(HierarchyConstants.NUM_LEVELS));
		Integer maxDepth = null;
		IField field = record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.MAX_DEPTH));
		if (field.getValue() instanceof BigDecimal) {
			maxDepth = ((BigDecimal) field.getValue()).intValue();
		} else {
			maxDepth = (Integer) field.getValue();
		}

		// add leaf field attributes for automatic edit field GUI
		ArrayList<Field> leafFields = hierarchies.getHierarchy(dimension).getMetadataLeafFields();
		for (int f = 0, lf = leafFields.size(); f < lf; f++) {
			Field fld = leafFields.get(f);
			String idFld = fld.getId();
			if (!fld.isSingleValue()) {
				IField fldValue = record.getFieldAt(metadata.getFieldIndex(idFld + maxDepth));
				mapAttrs.put(idFld, (fld.getFixValue() != null) ? fld.getFixValue() : fldValue.getValue());
			} else {
				IField fldValue = record.getFieldAt(metadata.getFieldIndex(idFld));
				mapAttrs.put(idFld, (fld.getFixValue() != null) ? fld.getFixValue() : fldValue.getValue());
			}
		}
		data.setAttributes(mapAttrs);
		return data;
	}

	public static JSONObject setDetailsInfo(JSONObject nodeJSONObject, HierarchyTreeNodeData nodeData) {
		try {
			JSONObject toReturn = nodeJSONObject;

			toReturn.put(HierarchyConstants.BEGIN_DT, nodeData.getBeginDt());
			toReturn.put(HierarchyConstants.END_DT, nodeData.getEndDt());

			HashMap mapAttrs = nodeData.getAttributes();
			HierarchyUtils.createJSONArrayFromHashMap(mapAttrs, toReturn);

			return toReturn;
		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while serializing hierarchy details structure to JSON", t);
		}

	}

	public static IDataStore getHierarchyDataStore(IDataSource dataSource, String dimension, String hierarchyType, String hierarchyName, String hierarchyDate,
			String filterDate, String filterDimension, String optionDate, String optionHierarchy, String optionHierType, boolean exludeHierLeaf) {

		// 1 - execute query to get hierarchy data
		String queryText = HierarchyUtils.createHierarchyDataQuery(dataSource, dimension, hierarchyType, hierarchyName, hierarchyDate, filterDate,
				filterDimension, optionDate, optionHierarchy, optionHierType, exludeHierLeaf);

		IDataStore dataStore = dataSource.executeStatement(queryText, 0, 0);

		return dataStore;
	}

	/**
	 * Create query for extracting automatic hierarchy rows
	 *
	 * @param dataSource
	 *            : the datasource
	 * @param dimension
	 *            : the selected dimension
	 * @param hierarchyType
	 *            : the selected hierarchy type
	 * @param hierarchyName
	 *            : the selected hierarchy name
	 * @param hierarchyDate
	 *            : the validity date
	 * @param filterDimension
	 *            : true for get nodes that aren't present into the selected dimension
	 * @param optionDate
	 *            : optional after date
	 * @param optionHierarchy
	 *            : optional hierarchy for get nodes that aren't present into the hierarchy
	 * @param optionHierType
	 *            : optional hierarchy type (ref. to optionHierarchy)
	 * @return String the query to extract the hierarchy (all nodes and leaves)
	 */
	private static String createHierarchyDataQuery(IDataSource dataSource, String dimension, String hierarchyType, String hierarchyName, String hierarchyDate,
			String filterDate, String filterDimension, String optionDate, String optionHierarchy, String optionHierType, boolean exludeHierLeaf) {

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();

		// 1 -get hierarchy informations
		String hierarchyTable = hierarchies.getHierarchyTableName(dimension);
		String dimensionName = (hierarchies.getDimension(dimension).getName());
		String prefix = hierarchies.getPrefix(dimension);
		Hierarchy hierarchyFields = hierarchies.getHierarchy(dimension);
		Assert.assertNotNull(hierarchyFields, "Impossible to find a hierarchy configurations for the dimension called [" + dimension + "]");
		HashMap hierConfig = hierarchies.getConfig(dimension);

		List<Field> generalMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataGeneralFields());
		List<Field> nodeMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataNodeFields());
		List<Field> leafMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataLeafFields());

		// 2 - get total columns number
		int totalColumns = 0;
		int totalLevels = Integer.parseInt((String) hierConfig.get(HierarchyConstants.NUM_LEVELS));
		int totalGeneralFields = generalMetadataFields.size();
		int totalLeafFields = leafMetadataFields.size();
		int totalNodeFields = HierarchyUtils.getTotalNodeFieldsNumber(totalLevels, nodeMetadataFields);

		totalColumns = totalGeneralFields + totalLeafFields + totalNodeFields;

		// 3 - define select command
		StringBuffer selectClauseBuffer = new StringBuffer(" ");
		StringBuffer orderClauseBuffer = new StringBuffer(" ");
		// general fields:
		for (int i = 0, l = generalMetadataFields.size(); i < l; i++) {
			Field f = generalMetadataFields.get(i);
			String sep = ", ";
			String column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
			selectClauseBuffer.append(column + sep);
		}

		String orderField = null;
		// node fields:
		for (int i = 0, l = nodeMetadataFields.size(); i < l; i++) {
			Field f = nodeMetadataFields.get(i);
			String sep = ", ";
			String column = "";
			if (f.isSingleValue()) {
				column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
				selectClauseBuffer.append(column + sep);
			} else {
				for (int i2 = 1, l2 = totalLevels; i2 <= l2; i2++) {
					sep = ",";
					column = AbstractJDBCDataset.encapsulateColumnName(f.getId() + i2, dataSource);
					selectClauseBuffer.append(column + sep);
				}
			}
			if (f.isOrderField()) {
				orderField = f.getId();
			}
		}
		// order clause
		for (int o = 1, l2 = totalLevels; o <= l2; o++) {
			String sep = (o == totalLevels) ? "" : ",";
			String column = null;
			// String column = AbstractJDBCDataset.encapsulateColumnName(prefix + HierarchyConstants.SUFFIX_CD_LEV + o, dataSource);
			// String column = AbstractJDBCDataset.encapsulateColumnName((String) hierConfig.get(HierarchyConstants.TREE_NODE_CD) + o, dataSource);
			if (orderField != null) {
				column = AbstractJDBCDataset.encapsulateColumnName(orderField + o, dataSource);
				// DESC is used to order the row placing the NULL fields at the end. Is used in combination with negative order fields to reach ascending sort
				orderClauseBuffer.append(column + " DESC" + sep);
			} else {
				column = AbstractJDBCDataset.encapsulateColumnName((String) hierConfig.get(HierarchyConstants.TREE_NODE_CD) + o, dataSource);
				orderClauseBuffer.append(column + sep);
			}
		}

		// leaf fields:
		for (int i = 0, l = leafMetadataFields.size(); i < l; i++) {
			Field f = leafMetadataFields.get(i);
			String sep = (i == l - 1) ? " " : ",";
			String column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
			selectClauseBuffer.append(column + sep);
		}
		String selectClause = selectClauseBuffer.toString();

		// where
		String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
		String hierTypeColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_TP, dataSource);
		String hierDateBeginColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BEGIN_DT, dataSource);
		String hierDateEndColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.END_DT, dataSource);
		String vDateConverted = HierarchyUtils.getConvertedDate(hierarchyDate, dataSource);
		String vDateWhereClause = vDateConverted + " >= " + hierDateBeginColumn + " AND " + vDateConverted + " <= " + hierDateEndColumn;

		StringBuffer query = new StringBuffer("SELECT " + selectClause + " FROM " + hierarchyTable + " WHERE " + hierNameColumn + " = '" + hierarchyName
				+ "' AND " + hierTypeColumn + " = '" + hierarchyType + "' AND " + vDateWhereClause);

		if (filterDimension != null) {
			logger.debug("Filter dimension is [" + filterDimension + "]");

			String vDateOptionConverted = HierarchyUtils.getConvertedDate(optionDate, dataSource);
			String vDateOptionWhereClause = vDateOptionConverted + " >= " + hierDateBeginColumn + " AND " + vDateOptionConverted + " <= " + hierDateEndColumn;
			String dimFilterField = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEAF", dataSource);
			String selectFilterField = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD", dataSource);

			query.append(" AND " + dimFilterField + " NOT IN (SELECT " + selectFilterField + "FROM " + dimensionName);
			query.append(" WHERE " + vDateOptionWhereClause + ")");
		}

		if (filterDate != null) {
			logger.debug("Filter date is [" + optionDate + "]");

			query.append(HierarchyUtils.createDateAfterCondition(dataSource, filterDate, hierDateBeginColumn));
		}

		if (optionHierarchy != null) {
			logger.debug("Filter Hierarchy is [" + optionHierarchy + "]");

			String vDateOptionConverted = HierarchyUtils.getConvertedDate(optionDate, dataSource);
			String vDateOptionWhereClause = vDateOptionConverted + " >= " + hierDateBeginColumn + " AND " + vDateOptionConverted + " <= " + hierDateEndColumn;

			String dimFilterField = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEAF", dataSource);

			query.append(HierarchyUtils.createInHierarchyCondition(dataSource, hierarchyTable, hierNameColumn, optionHierarchy, hierTypeColumn, optionHierType,
					dimFilterField, dimFilterField, vDateOptionWhereClause, exludeHierLeaf));
		}
		// order cluase
		query.append(" ORDER BY " + orderClauseBuffer.toString());

		logger.debug("Query for get hierarchies: " + query);
		return query.toString();
	}

	/**
	 * Do backup of current hierarchy state with its relational MT data
	 *
	 * @param dataSource
	 * @param databaseConnection
	 * @param paramsMap
	 */
	public static String updateHierarchyForBackup(IDataSource dataSource, Connection databaseConnection, HashMap paramsMap) {
		logger.debug("START");

		String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
		String beginDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BEGIN_DT, dataSource);
		String endDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.END_DT, dataSource);
		String hierTypeColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_TP, dataSource);
		String bkpColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BKP_COLUMN, dataSource);
		String bkpTimestampColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BKP_TIMESTAMP_COLUMN, dataSource);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(calendar.getTime());
		long timestamp = calendar.getTimeInMillis();
		String newHierName = (String) paramsMap.get("hierTargetName") + "_" + timestamp;

		Date vDateConverted = Date.valueOf((String) paramsMap.get("validityDate"));

		String vDateWhereClause = " ? >= " + beginDtColumn + " AND ? <= " + endDtColumn;

		String updateQuery = "UPDATE " + (String) paramsMap.get("hierarchyTable") + " SET " + hierNameColumn + "= ?, " + bkpColumn + " = ?, "
				+ bkpTimestampColumn + "= ? WHERE " + hierNameColumn + "=? AND " + hierTypeColumn + "= ? AND " + vDateWhereClause;

		logger.debug("The update query is [" + updateQuery + "]");

		try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(updateQuery)) {
			preparedStatement.setString(1, newHierName);
			preparedStatement.setBoolean(2, true);
			preparedStatement.setTimestamp(3, new java.sql.Timestamp(timestamp));
			preparedStatement.setString(4, (String) paramsMap.get("hierTargetName"));
			preparedStatement.setString(5, (String) paramsMap.get("hierTargetType"));
			preparedStatement.setDate(6, vDateConverted);
			preparedStatement.setDate(7, vDateConverted);

			preparedStatement.executeUpdate();

			logger.debug("Update query successfully executed");
			logger.debug("END");

		} catch (Throwable t) {
			logger.error("An unexpected error occured while updating hierarchy for backup");
			throw new SpagoBIServiceException("An unexpected error occured while updating hierarchy for backup", t);
		}

		if (paramsMap.get("doPropagation") != null && (Boolean) paramsMap.get("doPropagation")) {
			String hierNameTargetColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM_T, dataSource);

			String updateRelQuery = "UPDATE " + HierarchyConstants.REL_MASTER_TECH_TABLE_NAME + " SET " + hierNameTargetColumn + "= ?, " + bkpColumn + " = ?, "
					+ bkpTimestampColumn + "= ? WHERE " + hierNameTargetColumn + "= ? AND (" + bkpColumn + "= ? OR " + bkpColumn + " IS NULL)";

			logger.debug("The relations update query is [" + updateRelQuery + "]");

			String relColumns = getRelationalColumns(dataSource);
			HashMap fixedValuesMap = new HashMap();
			fixedValuesMap.put(HierarchyConstants.HIER_NM_T, paramsMap.get("hierTargetName"));
			String primaryKeyColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID, dataSource);

			String relValuesColumns = getRelationalValuesColumns(dataSource, fixedValuesMap);
			// if a primary key is present replace the column name with the a counter of the column [E.g. "MT_ID" becomes ? and is set to {countId} ]
			// relValuesColumns = relValuesColumns.replace(primaryKeyColumn, "MAX(" + primaryKeyColumn + ") + 1"); // MAX(REL_MASTER_TECH_TABLE_NAME_ID)

			String selectRelQuery = "select  " + relValuesColumns + " from  " + HierarchyConstants.REL_MASTER_TECH_TABLE_NAME + " where "
					+ hierNameTargetColumn + "= ? and " + bkpColumn + "= ?";
			logger.debug("The relations insert original query is [" + selectRelQuery + "]");

			try (PreparedStatement preparedRelStatement = databaseConnection.prepareStatement(updateRelQuery);
					PreparedStatement preparedSelRelStatement = databaseConnection.prepareStatement(selectRelQuery)) {

				preparedRelStatement.setString(1, newHierName);
				preparedRelStatement.setBoolean(2, true);
				preparedRelStatement.setTimestamp(3, new java.sql.Timestamp(timestamp));
				preparedRelStatement.setString(4, (String) paramsMap.get("hierTargetName"));
				preparedRelStatement.setBoolean(5, false);

				preparedRelStatement.executeUpdate();
				preparedRelStatement.closeOnCompletion();
				// duplicate original record for don't loose relations in the new version (insert from select)
				preparedSelRelStatement.setString(1, newHierName);
				preparedSelRelStatement.setBoolean(2, true);

				ResultSet rs = preparedSelRelStatement.executeQuery();
				preparedSelRelStatement.closeOnCompletion();
				int countId = HierarchyUtils.getCountId(HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID, HierarchyConstants.REL_MASTER_TECH_TABLE_NAME,
						databaseConnection, dataSource);
				while (rs.next()) {
					countId++;
					ResultSetMetaData mt = rs.getMetaData();
					String relInsValuesColumns = "";
					for (int i = 1; i <= mt.getColumnCount(); i++) {
						String sep = i < mt.getColumnCount() ? "," : "";
						relInsValuesColumns = relInsValuesColumns + "?" + sep;
					}
					String insertOrigRelQuery = "insert into " + HierarchyConstants.REL_MASTER_TECH_TABLE_NAME + " (" + relColumns + ") VALUES ("
							+ relInsValuesColumns + ")";
					PreparedStatement preparedInsRelStatement = databaseConnection.prepareStatement(insertOrigRelQuery);
					setParameterPropagationInsertQuery(preparedInsRelStatement, rs, countId);
					preparedInsRelStatement.executeUpdate();
					preparedInsRelStatement.closeOnCompletion();
				}

				logger.debug("Insert original relations query successfully executed");

				logger.debug("Update relations query successfully executed");
				logger.debug("END");

			} catch (Throwable t) {
				logger.error("An unexpected error occured while updating hierarchy relations for backup");
				throw new SpagoBIServiceException("An unexpected error occured while updating hierarchy relations for backup", t);
			}
			logger.debug("Update relations query successfully executed");

		}
		return newHierName;
	}

	/**
	 * The function set the PreparedStatement parameters for the insert query in table {HierarchyConstants.REL_MASTER_TECH_TABLE_NAME}. The parameters are taken
	 * from a resultSet of a previous query.
	 *
	 * @param PreparedStatement
	 *            , resultSet, countPrimaryKey
	 * 
	 * @return void
	 */
	private static void setParameterPropagationInsertQuery(PreparedStatement ps, ResultSet rs, int countPrimaryKey) {
		try {
			ResultSetMetaData mt = rs.getMetaData();

			if (rs.getObject(HierarchyConstants.DIMENSION) != null) {
				int idx = rs.findColumn(HierarchyConstants.DIMENSION);
				ps.setString(idx, rs.getString(HierarchyConstants.DIMENSION));
			}
			if (rs.getObject(HierarchyConstants.HIER_CD_T) != null) {
				int idx = rs.findColumn(HierarchyConstants.HIER_CD_T);
				ps.setString(idx, rs.getString(HierarchyConstants.HIER_CD_T));
			}
			if (rs.getObject(HierarchyConstants.HIER_NM_T) != null) {
				int idx = rs.findColumn(HierarchyConstants.HIER_NM_T);
				ps.setString(idx, rs.getString(HierarchyConstants.HIER_NM_T));
			}
			if (rs.getObject(HierarchyConstants.NODE_CD_T) != null) {
				int idx = rs.findColumn(HierarchyConstants.NODE_CD_T);
				ps.setString(idx, rs.getString(HierarchyConstants.NODE_CD_T));
			}
			if (rs.getObject(HierarchyConstants.NODE_NM_T) != null) {
				int idx = rs.findColumn(HierarchyConstants.NODE_NM_T);
				ps.setString(idx, rs.getString(HierarchyConstants.NODE_NM_T));
			}
			if (rs.getObject(HierarchyConstants.NODE_LEV_T) != null) {
				int idx = rs.findColumn(HierarchyConstants.NODE_LEV_T);
				ps.setFloat(idx, rs.getFloat(HierarchyConstants.NODE_LEV_T));
			}
			if (rs.getObject(HierarchyConstants.PATH_CD_T) != null) {
				int idx = rs.findColumn(HierarchyConstants.PATH_CD_T);
				ps.setClob(idx, rs.getClob(HierarchyConstants.PATH_CD_T));
			}
			if (rs.getObject(HierarchyConstants.PATH_NM_T) != null) {
				int idx = rs.findColumn(HierarchyConstants.PATH_NM_T);
				ps.setClob(idx, rs.getClob(HierarchyConstants.PATH_NM_T));
			}
			if (rs.getObject(HierarchyConstants.HIER_CD_M) != null) {
				int idx = rs.findColumn(HierarchyConstants.HIER_CD_M);
				ps.setString(idx, rs.getString(HierarchyConstants.HIER_CD_M));
			}
			if (rs.getObject(HierarchyConstants.HIER_NM_M) != null) {
				int idx = rs.findColumn(HierarchyConstants.HIER_NM_M);
				ps.setString(idx, rs.getString(HierarchyConstants.HIER_NM_M));
			}
			if (rs.getObject(HierarchyConstants.NODE_CD_M) != null) {
				int idx = rs.findColumn(HierarchyConstants.NODE_CD_M);
				ps.setString(idx, rs.getString(HierarchyConstants.NODE_CD_M));
			}
			if (rs.getObject(HierarchyConstants.NODE_NM_M) != null) {
				int idx = rs.findColumn(HierarchyConstants.NODE_NM_M);
				ps.setString(idx, rs.getString(HierarchyConstants.NODE_NM_M));
			}
			if (rs.getObject(HierarchyConstants.NODE_LEV_M) != null) {
				int idx = rs.findColumn(HierarchyConstants.NODE_LEV_M);
				ps.setFloat(idx, rs.getFloat(HierarchyConstants.NODE_LEV_M));
			}
			if (rs.getObject(HierarchyConstants.GENERAL_INFO_T) != null) {
				int idx = rs.findColumn(HierarchyConstants.GENERAL_INFO_T);
				ps.setClob(idx, rs.getClob(HierarchyConstants.GENERAL_INFO_T));
			}
			// set the primary key HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID
			int idx = rs.findColumn(HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID);
			ps.setInt(idx, countPrimaryKey);
		} catch (SQLException e) {
			logger.error("Error during propagation backup. Impossible to set element in insert query");
			throw new SpagoBIServiceException("Error during propagation backup. Impossible to set element in insert query", e);
		}

	}

	/**
	 * Persist informations about relation between the master node and the technical node
	 *
	 * @param connection
	 * @param node
	 * @param dataSource
	 * @throws SQLException
	 */
	public static void persistRelationMasterTechnical(Connection connection, HashMap values, IDataSource dataSource, HashMap paramsMap) throws SQLException {
		try {
			// prepare stmt ONLY for original MASTER (with '_M' suffix) nodes
			if (values.get(HierarchyConstants.HIER_NM_M) != null) {
				String relColumns = getRelationalColumns(dataSource);
				int idCount = HierarchyUtils.getCountId(HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID, HierarchyConstants.REL_MASTER_TECH_TABLE_NAME,
						connection, dataSource);
				values.put(HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID, idCount + 1);
				String insertRelQuery = "insert into " + HierarchyConstants.REL_MASTER_TECH_TABLE_NAME + " (" + relColumns
						+ ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
				try (PreparedStatement relPreparedStatement = connection.prepareStatement(insertRelQuery);) {
					for (int k = 1; k <= 15; k++) {
						relPreparedStatement.setObject(k, null);
					}
					valorizeRelPlaceholders(relPreparedStatement, values);
					relPreparedStatement.executeUpdate();
					relPreparedStatement.close();
				} catch (Throwable t2) {
					logger.error("An unexpected error occured while updating hierarchy for propagation");
					throw new SpagoBIServiceException("An unexpected error occured while updating hierarchy for propagation", t2);
				}
			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while persisting hierarchy relations", t.getMessage());
		}
	}

	/**
	 * Returns a string with columns of the HIER_MASTER_TECHNICAL for SQL stmt
	 *
	 * @param dataSource
	 *            the datasource
	 * @return String the columns list
	 */
	public static String getRelationalColumns(IDataSource dataSource) {

		String toReturn = null;

		StringBuffer sbColumns = new StringBuffer();
		String column = null;

		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.DIMENSION, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_CD_T, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM_T, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_CD_T, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_NM_T, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_LEV_T, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.PATH_CD_T, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.PATH_NM_T, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_CD_M, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM_M, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_CD_M, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_NM_M, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_LEV_M, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.GENERAL_INFO_T, dataSource);
		sbColumns.append(column + ",");
		column = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID, dataSource);
		sbColumns.append(column);

		toReturn = sbColumns.toString();
		return toReturn;
	}

	/**
	 * Returns a string with columns values of the HIER_MASTER_TECHNICAL for SQL stmt. It can set fixed values if they are defined into the input HashMap
	 *
	 * @param dataSource
	 *            the datasource
	 * @return String the columns list
	 */
	private static String getRelationalValuesColumns(IDataSource dataSource, HashMap fixedFields) {

		String toReturn = null;

		StringBuffer sbColumns = new StringBuffer();
		String column = null;

		column = (fixedFields.get(HierarchyConstants.DIMENSION) != null) ? "'" + fixedFields.get(HierarchyConstants.DIMENSION) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.DIMENSION, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.DIMENSION, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.HIER_CD_T) != null) ? "'" + fixedFields.get(HierarchyConstants.HIER_CD_T) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.HIER_CD_T, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_CD_T, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.HIER_NM_T) != null) ? "'" + fixedFields.get(HierarchyConstants.HIER_NM_T) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.HIER_NM_T, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM_T, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.NODE_CD_T) != null) ? "'" + fixedFields.get(HierarchyConstants.NODE_CD_T) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.NODE_CD_T, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_CD_T, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.NODE_NM_T) != null) ? "'" + fixedFields.get(HierarchyConstants.NODE_NM_T) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.NODE_NM_T, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_NM_T, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.NODE_LEV_T) != null) ? "'" + fixedFields.get(HierarchyConstants.NODE_LEV_T) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.NODE_LEV_T, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_LEV_T, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.PATH_CD_T) != null) ? "'" + fixedFields.get(HierarchyConstants.PATH_CD_T) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.PATH_CD_T, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.PATH_CD_T, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.PATH_NM_T) != null) ? "'" + fixedFields.get(HierarchyConstants.PATH_NM_T) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.PATH_NM_T, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.PATH_NM_T, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.HIER_CD_M) != null) ? "'" + fixedFields.get(HierarchyConstants.HIER_CD_M) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.HIER_CD_M, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_CD_M, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.HIER_NM_M) != null) ? "'" + fixedFields.get(HierarchyConstants.HIER_NM_M) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.HIER_NM_M, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM_M, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.NODE_CD_M) != null) ? "'" + fixedFields.get(HierarchyConstants.NODE_CD_M) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.NODE_CD_M, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_CD_M, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.NODE_NM_M) != null) ? "'" + fixedFields.get(HierarchyConstants.NODE_NM_M) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.NODE_NM_M, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_NM_M, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.NODE_LEV_M) != null) ? "'" + fixedFields.get(HierarchyConstants.NODE_LEV_M) + "'" : AbstractJDBCDataset
				.encapsulateColumnName(HierarchyConstants.NODE_LEV_M, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_LEV_M, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.GENERAL_INFO_T) != null) ? "'" + fixedFields.get(HierarchyConstants.GENERAL_INFO_T) + "'"
				: AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.GENERAL_INFO_T, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.GENERAL_INFO_T, dataSource) + ",");

		column = (fixedFields.get(HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID) != null) ? "'"
				+ fixedFields.get(HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID) + "'" : AbstractJDBCDataset.encapsulateColumnName(
				HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID, dataSource);
		sbColumns.append(column + " AS " + AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID, dataSource));

		toReturn = sbColumns.toString();
		return toReturn;
	}

	/**
	 *
	 * Set values for the preparedStatement of relations between MASTER and TECHNICAL (HIER_XXX)
	 *
	 * @param preparedStatement
	 * @param values
	 * @return Set values for the preparedStatement of relations between MASTER and TECHNICAL (HIER_XXX)
	 * @throws SQLException
	 */
	private static PreparedStatement valorizeRelPlaceholders(PreparedStatement preparedStatement, HashMap values) throws SQLException {

		PreparedStatement toReturn = preparedStatement;

		try {
			toReturn.setObject(1, values.get(HierarchyConstants.DIMENSION));
			toReturn.setObject(2, values.get(HierarchyConstants.HIER_CD_T));
			toReturn.setObject(3, values.get(HierarchyConstants.HIER_NM_T));
			toReturn.setObject(4, values.get(HierarchyConstants.NODE_CD_T));
			toReturn.setObject(5, values.get(HierarchyConstants.NODE_NM_T));
			toReturn.setObject(6, values.get(HierarchyConstants.NODE_LEV_T));
			toReturn.setObject(7, values.get(HierarchyConstants.PATH_CD_T));
			toReturn.setObject(8, values.get(HierarchyConstants.PATH_NM_T));
			toReturn.setObject(9, values.get(HierarchyConstants.HIER_CD_M));
			toReturn.setObject(10, values.get(HierarchyConstants.HIER_NM_M));
			toReturn.setObject(11, values.get(HierarchyConstants.NODE_CD_M));
			toReturn.setObject(12, values.get(HierarchyConstants.NODE_NM_M));
			toReturn.setObject(13, values.get(HierarchyConstants.NODE_LEV_M));
			toReturn.setObject(14, values.get(HierarchyConstants.GENERAL_INFO_T));
			toReturn.setObject(15, values.get(HierarchyConstants.REL_MASTER_TECH_TABLE_NAME_ID));
		} catch (Throwable t) {
			String errMsg = "Error while inserting relation of element with code: [" + values.get(HierarchyConstants.NODE_CD_T) + "] and name: ["
					+ values.get(HierarchyConstants.NODE_NM_T) + "]";
			if (values.size() > 0) {
				errMsg += " with next values: [";
				Iterator iter = values.keySet().iterator();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					Object value = values.get(key);
					errMsg += " key: " + key + " - value: " + value + ((iter.hasNext()) ? "," : "]");
				}
				logger.error(errMsg, t);
			}
			throw new SpagoBIServiceException("An unexpected error occured while persisting hierarchy structure: ", t.getMessage() + " - " + t.getCause()
					+ " - " + errMsg);
		}

		return toReturn;
	}

}
