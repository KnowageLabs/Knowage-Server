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
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchyTreeNodeData;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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

		// Create fixed fields (code, name, description and timestamp where only name and description are editable)
		Field bkpField = new Field(HierarchyConstants.HIER_CD, "Code", "String", null, true, false, false, true, false);
		result.add(bkpField);
		bkpField = new Field(HierarchyConstants.HIER_NM, "Name", "String", null, true, true, false, true, false);
		result.add(bkpField);
		bkpField = new Field(HierarchyConstants.HIER_DS, "Description", "String", null, true, true, false, true, false);
		result.add(bkpField);
		// ...then we build a field for the others backup info
		bkpField = new Field(HierarchyConstants.BKP_TIMESTAMP_COLUMN, "Date", "Date", null, true, false, false, true, false);
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
			String queryText = "DELETE FROM " + tableName + " WHERE " + hierarchyNameCol + "=\"" + hierarchyName + "\" ";

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
		data.setNodeCode(leafParentCodeString + "_" + nodeCode);
		nodeCode = leafParentCodeString + "_" + nodeCode;
		data.setLeafParentCode(leafParentCodeString);
		// data.setLeafOriginalParentCode(leafParentCodeString); // backup code

		IField leafParentNameField = record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.LEAF_PARENT_NM));
		String leafParentNameString = (String) leafParentNameField.getValue();
		data.setLeafParentName(leafParentNameString);

		IField beginDtField = record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.BEGIN_DT));
		Date beginDtDate = (Date) beginDtField.getValue();
		data.setBeginDt(beginDtDate);

		IField endDtField = record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.END_DT));
		Date endDtDate = (Date) endDtField.getValue();
		data.setEndDt(endDtDate);

		HashMap mapAttrs = new HashMap();
		int numLevels = Integer.valueOf((String) hierarchies.getConfig(dimension).get(HierarchyConstants.NUM_LEVELS));
		Integer maxDepth = (Integer) (record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.MAX_DEPTH)).getValue());

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
}
