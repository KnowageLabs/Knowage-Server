package it.eng.spagobi.tools.hierarchiesmanagement.utils;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

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

		result.put(HierarchyConstants.FIELD_VISIBLE, field.isVisible());
		logger.debug("Field [" + HierarchyConstants.FIELD_VISIBLE + "] is " + field.isVisible());

		result.put(HierarchyConstants.FIELD_EDITABLE, field.isEditable());
		logger.debug("Field [" + HierarchyConstants.FIELD_EDITABLE + "] is " + field.isEditable());

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

}
