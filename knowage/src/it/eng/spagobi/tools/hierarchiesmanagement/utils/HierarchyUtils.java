package it.eng.spagobi.tools.hierarchiesmanagement.utils;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
			if (key != null && value != null)
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

	public static String createDateAfterCondition(IDataSource dataSource, String filterDate, String beginDtColumn) {

		String fDateConverted = HierarchyUtils.getConvertedDate(filterDate, dataSource);
		String dateAfterCondition = " AND " + beginDtColumn + " >= " + fDateConverted;

		return dateAfterCondition;

	}

	public static String createNotInHierarchyCondition(IDataSource dataSource, String hierarchyTable, String hierNameCol, String hierarchyName,
			String hierTypeCol, String hierarchyType, String dimFilterFieldCol, String selectFilterField, String vDateWhereClause) {

		logger.debug("Filter Hierarchy [" + hierarchyName + "]");

		StringBuffer query = new StringBuffer();

		query.append(" AND " + dimFilterFieldCol + " NOT IN (SELECT " + selectFilterField + " FROM " + hierarchyTable);
		query.append(" WHERE " + hierNameCol + " = \"" + hierarchyName + "\" AND " + hierTypeCol + " = \"" + hierarchyType + "\" AND " + vDateWhereClause
				+ " )");

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
			String filterDate, String filterHierarchy, String filterHierType, String hierTableName, String prefix) {

		String dimFilterField = prefix + HierarchyConstants.DIM_FILTER_FIELD;
		String hierFilterField = prefix + HierarchyConstants.SELECT_HIER_FILTER_FIELD;

		// 2 - execute query to get dimension data
		String queryText = HierarchyUtils.createDimensionDataQuery(dataSource, metadataFields, dimensionName, validityDate, filterDate, filterHierarchy,
				filterHierType, hierTableName, dimFilterField, hierFilterField);

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
			String filterDate, String filterHierarchy, String filterHierType, String hierTableName, String dimFilterField, String hierFilterField) {

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

		String vDateWhereClause = vDateConverted + " >= " + beginDtColumn + " AND " + vDateConverted + " <= " + endDtColumn;

		StringBuffer query = new StringBuffer("SELECT " + selectClause + " FROM " + dimensionName + " WHERE " + vDateWhereClause);

		if (filterDate != null) {
			logger.debug("Filter date is [" + filterDate + "]");

			query.append(HierarchyUtils.createDateAfterCondition(dataSource, filterDate, beginDtColumn));
		}

		if (filterHierarchy != null) {
			logger.debug("Filter Hierarchy is [" + filterHierarchy + "]");

			String hierNameCol = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
			String hierTypeCol = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_TP, dataSource);
			String dimFilterFieldCol = AbstractJDBCDataset.encapsulateColumnName(dimFilterField, dataSource);
			String selectFilterField = AbstractJDBCDataset.encapsulateColumnName(hierFilterField, dataSource);

			query.append(HierarchyUtils.createNotInHierarchyCondition(dataSource, hierTableName, hierNameCol, filterHierarchy, hierTypeCol, filterHierType,
					dimFilterFieldCol, selectFilterField, vDateWhereClause));
		}

		logger.debug("Query for dimension data is: " + query);
		logger.debug("END");
		return query.toString();
	}

	public static List<Field> createBkpFields(List<Field> genFields, String[] bkpGenFields) {

		logger.debug("START");

		List<Field> result = new ArrayList<Field>();

		// first we take real fields from generic fields...
		for (int i = 0; i < genFields.size(); i++) {

			Field tmpField = genFields.get(i);

			for (int j = 0; j < bkpGenFields.length; j++) {

				if (tmpField.getId().equals(bkpGenFields[j])) {
					result.add(tmpField);
					break;
				}

			}

		}

		// ...then we build a field for the others backup info
		Field bkpField = new Field(HierarchyConstants.BKP_TIMESTAMP_COLUMN, "Date", "Date", null, true, false, false, true, false);
		result.add(bkpField);

		logger.debug("END");
		return result;
	}

}
