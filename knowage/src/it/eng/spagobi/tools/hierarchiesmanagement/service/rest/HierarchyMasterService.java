package it.eng.spagobi.tools.hierarchiesmanagement.service.rest;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Dimension;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Hierarchy;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * This class contains all REST services used for specific MASTER hierarchy types
 */

@Path("/hierarchiesMaster")
public class HierarchyMasterService {

	static private Logger logger = Logger.getLogger(HierarchyService.class);

	// get hierarchies names of a dimension
	@GET
	@Path("/getHierarchiesMaster")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchiesMaster(@QueryParam("dimension") String dimension) {
		JSONArray hierarchiesJSONArray = new JSONArray();
		logger.debug("START");

		try {
			// 1 - get hierarchy table
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String tableName = hierarchies.getHierarchyTableName(dimension);
			// 2 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}
			// 3- execute query to get hierarchies names
			String hierarchyNameColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_NM", dataSource);
			String typeColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_TP", dataSource);
			String hierarchyCodeColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_CD, dataSource);
			String hierarchyDescriptionColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_DS, dataSource);
			String bkpColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BKP_COLUMN, dataSource);
			String columns = hierarchyNameColumn + "," + typeColumn + "," + hierarchyDescriptionColumn + " ";

			IDataStore dataStore = dataSource.executeStatement("SELECT DISTINCT(" + hierarchyCodeColumn + ")," + columns + " FROM " + tableName + " WHERE "
					+ typeColumn + "=\"MASTER\" AND " + bkpColumn + "= 0 ORDER BY " + hierarchyCodeColumn, 0, 0);

			for (Iterator iterator = dataStore.iterator(); iterator.hasNext();) {
				IRecord record = (IRecord) iterator.next();
				IField field = record.getFieldAt(0);
				String hierarchyCode = (String) field.getValue();
				field = record.getFieldAt(1);
				String hierarchyName = (String) field.getValue();
				field = record.getFieldAt(2);
				String hierarchyType = (String) field.getValue();
				field = record.getFieldAt(3);
				String hierarchyDescription = (String) field.getValue();

				JSONObject hierarchy = new JSONObject();
				hierarchy.put(HierarchyConstants.HIER_CD, hierarchyCode);
				hierarchy.put(HierarchyConstants.HIER_NM, hierarchyName);
				hierarchy.put(HierarchyConstants.HIER_TP, hierarchyType);
				hierarchy.put(HierarchyConstants.HIER_DS, hierarchyDescription);
				hierarchiesJSONArray.put(hierarchy);

			}

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving automatic hierarchies names");
			throw new SpagoBIServiceException("An unexpected error occured while retriving automatic hierarchies names", t);
		}
		logger.debug("END");
		return hierarchiesJSONArray.toString();
	}

	@POST
	@Path("/createHierarchyMaster")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String createHierarchyMaster(@Context HttpServletRequest req) throws SQLException {

		logger.debug("START");

		Connection dbConnection = null;

		try {

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			String dimensionLabel = requestVal.getString("dimension");
			String validityDate = requestVal.getString("validityDate");
			String filterDate = (requestVal.isNull("filterDate")) ? null : requestVal.getString("filterDate");
			String filterHierarchy = (requestVal.isNull("filterHierarchy")) ? null : requestVal.getString("filterHierarchy");
			String filterHierType = (requestVal.isNull("filterHierType")) ? null : requestVal.getString("filterHierType");

			if ((dimensionLabel == null) || (validityDate == null)) {
				throw new SpagoBIServiceException("An unexpected error occured while creating hierarchy master", "wrong request parameters");
			}

			IDataSource dataSource = HierarchyUtils.getDataSource(dimensionLabel);

			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}

			dbConnection = dataSource.getConnection();

			// JSONObject testJson = new JSONObject();
			// testJson.put("HIER_CD", "pippoCD");
			// testJson.put("HIER_NM", "pippoNM");
			// testJson.put("HIER_DS", "pippoDS");
			// testJson.put("HIER_TP", "pippoTP");
			// testJson.put("YEAR", "1999");
			// testJson.put("MAX_DEPTH", "");
			// testJson.put("COMPANY_SRC", "");
			// testJson.put("SOURCE_SYSTEM", "");
			// testJson.put("BEGIN_DT", "2010-04-11");
			// testJson.put("END_DT", "2016-04-11");
			//
			// JSONArray testArray = new JSONArray();
			//
			// JSONObject obj1 = new JSONObject();
			// obj1.put("CD", "SEGMENT_CD");
			// obj1.put("NM", "SEGMENT_NM");
			//
			// JSONObject obj2 = new JSONObject();
			// obj2.put("CD", "DEPARTMENT_CD");
			// obj2.put("NM", "DEPARTMENT_NM");
			//
			// testArray.put(obj1);
			// testArray.put(obj2);
			//
			// testJson.put("levels", testArray);
			// JSONObject obj3 = new JSONObject();
			// obj3.put("CD", "CODE");
			// obj3.put("NM", "NAME");
			// obj3.put("CD_PARENT", "CDC_PARENT_CD");
			// obj3.put("NM_PARENT", "CDC_PARENT_NM");
			// testJson.put("recursive", obj3);
			// requestVal.put("fill_empty", "true");
			// requestVal.put("fill_value", "FIX_VALUE");

			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			Assert.assertNotNull(hierarchies, "Impossible to find a valid hierarchies object");

			Dimension dimension = hierarchies.getDimension(dimensionLabel);
			Assert.assertNotNull(dimension, "Impossible to find a valid dimension with label [" + dimensionLabel + "]");

			Hierarchy hierarchy = hierarchies.getHierarchy(dimensionLabel);
			Assert.assertNotNull(hierarchy, "Impossible to find a valid hierarchy with label [" + dimensionLabel + "]");

			String dimensionName = dimension.getName();
			String hierTableName = hierarchies.getHierarchyTableName(dimensionLabel);
			String prefix = hierarchies.getPrefix(dimensionLabel);

			List<Field> metadataFields = new ArrayList<Field>(dimension.getMetadataFields());
			Map<String, Integer> metatadaFieldsMap = HierarchyUtils.getMetadataFieldsMap(metadataFields);

			List<Field> generalFields = new ArrayList<Field>(hierarchy.getMetadataGeneralFields());

			IDataStore dataStore = HierarchyUtils.getDimensionDataStore(dataSource, dimensionName, metadataFields, validityDate, filterDate, filterHierarchy,
					filterHierType, hierTableName, prefix);

			dbConnection.setAutoCommit(false);

			Iterator iterator = dataStore.iterator();

			while (iterator.hasNext()) {

				IRecord record = (IRecord) iterator.next();
				insertHierarchyMaster(dbConnection, dataSource, record, dataStore, hierTableName, generalFields, metatadaFieldsMap, requestVal, prefix,
						dimensionName, validityDate);

			}

			saveHierarchyMasterConfiguration(dbConnection, dataSource, requestVal);

			dbConnection.commit();

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving dimension data");
			throw new SpagoBIServiceException("An unexpected error occured while retriving dimension data", t);
		} finally {
			dbConnection.close();
		}

		logger.debug("END");
		return "{\"response\":\"ok\"}";

	}

	public void saveHierarchyMasterConfiguration(Connection dbConnection, IDataSource dataSource, JSONObject requestVal) throws SQLException, JSONException {

		String hierCdColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_CD, dataSource);
		String hierNmColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
		String confColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_MASTERS_CONFIG, dataSource);

		String insertClause = hierCdColumn + "," + hierNmColumn + "," + confColumn;

		String saveConfQuery = "INSERT INTO " + HierarchyConstants.HIER_MASTERS_CONFIG_TABLE + " (" + insertClause + ") VALUES (?,?,?)";

		logger.debug("Insert query is [" + saveConfQuery + "]");

		String hierCd = requestVal.getString(HierarchyConstants.HIER_CD);
		String hierNm = requestVal.getString(HierarchyConstants.HIER_NM);
		String configuration = requestVal.toString();

		PreparedStatement ps = dbConnection.prepareStatement(saveConfQuery);
		ps.setString(1, hierCd);
		ps.setString(2, hierNm);
		ps.setString(3, configuration);

		ps.executeUpdate();

		logger.debug("Hierarchy Master Configuration correctly saved!");

	}

	private void insertHierarchyMaster(Connection dbConnection, IDataSource dataSource, IRecord record, IDataStore dataStore, String hTableName,
			List<Field> generalFields, Map<String, Integer> metatadaFieldsMap, JSONObject requestVal, String prefix, String dimensionName, String validityDate) {

		logger.debug("START");

		try (Statement stmt = dbConnection.createStatement()) {

			// Create two clauses, one for columns and another for values
			// INSERT INTO name_table [columnsClause] values [valuesClause]
			StringBuffer columnsClause = new StringBuffer("(");
			StringBuffer valuesClause = new StringBuffer("(");

			String sep = ",";

			// fieldsMap is necessary to keep track of the position for values and types we need to use later when replace the prep. stat.
			Map<Integer, String[]> fieldsMap = new HashMap<Integer, String[]>();

			// levelsMap is necessary to keep track of values for levels
			Map<Integer, String[]> levelsMap = new HashMap<Integer, String[]>();

			// this counter come across different logics to build the insert query and it's used to keep things sequential
			// int index = 0;

			// used to keep track of levels for parent, leaf and max-depth
			// int lvlIndex = 0;

			// DONT' CHANGE SECTIONS ORDER! Indexes to calculate lvls, leaf, parent, etc are ordered with the following logic
			// So, if you need to change this code, be careful of indexes order!

			/**********************************************************************************************************
			 * in this section we add columns and values related to hierarchy general fields specified in request JSON*
			 **********************************************************************************************************/

			manageGeneralFieldsSection(dataSource, generalFields, fieldsMap, requestVal, columnsClause, valuesClause, sep);

			/****************************************************************************************
			 * in this section we add columns and values related to levels specified in request JSON*
			 ****************************************************************************************/

			manageLevelsSection(dataSource, record, metatadaFieldsMap, fieldsMap, levelsMap, requestVal, columnsClause, valuesClause, sep, prefix);

			/***********************************************************************
			 * in this section we add a recursive logic to calculate parents levels*
			 ***********************************************************************/

			manageRecursiveSection(dbConnection, dataSource, record, metatadaFieldsMap, fieldsMap, levelsMap, requestVal, columnsClause, valuesClause, sep,
					prefix, dimensionName, validityDate);

			/******************************************************************************
			 * in this section we add columns and values related to the leaf code and name*
			 ******************************************************************************/

			manageLeafSection(dataSource, record, metatadaFieldsMap, fieldsMap, levelsMap, columnsClause, valuesClause, sep, prefix);

			/******************************************************************************
			 * in this section we add columns and values related to the parent of the leaf*
			 ******************************************************************************/

			manageParentLeafSection(dataSource, fieldsMap, levelsMap, columnsClause, valuesClause, sep);

			/********************************************************************************************************
			 * in this section we add column and value related to the leaf id that comes from id in dimension record*
			 ********************************************************************************************************/

			manageLeafIdSection(dataSource, metatadaFieldsMap, record, fieldsMap, columnsClause, valuesClause, sep, prefix);

			/************************************************************************
			 * in this section we add column and value related to the hierarchy type*
			 ************************************************************************/

			manageHierTypeSection(dataSource, fieldsMap, columnsClause, valuesClause, sep);

			/******************************************************************************
			 * in this section we add column and value related to the max depth of levels*
			 ******************************************************************************/

			manageMaxDepthSection(dataSource, fieldsMap, levelsMap, columnsClause, valuesClause);

			/***************************************************************************************
			 * put together clauses in order to create the insert prepared statement and execute it*
			 ***************************************************************************************/

			StringBuffer insertQuery = new StringBuffer("INSERT INTO " + hTableName + columnsClause + " VALUES " + valuesClause);

			logger.debug("The insert query is [" + insertQuery.toString() + "]");

			PreparedStatement insertPs = dbConnection.prepareStatement(insertQuery.toString());

			for (int i = 0; i < fieldsMap.size(); i++) {

				String[] field = fieldsMap.get(i);

				String fieldValue = field[0];
				String fieldType = field[1];

				logger.debug("Set the insert prepared statement with a field of type [" + fieldType + "] and value [" + fieldValue + "]");

				if (fieldType.equals(HierarchyConstants.FIELD_TP_STRING)) {

					insertPs.setString(i + 1, fieldValue);
				} else if (fieldType.equals(HierarchyConstants.FIELD_TP_NUMBER)) {
					insertPs.setLong(i + 1, Long.valueOf(fieldValue));
				} else if (fieldType.equals(HierarchyConstants.FIELD_TP_DATE)) {
					final Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(fieldValue);
					java.sql.Date tmpDate = new java.sql.Date(calendar.getTimeInMillis());
					insertPs.setDate(i + 1, tmpDate);
				} else {
					Object tmpObj = fieldValue;
					insertPs.setObject(i + 1, tmpObj);
				}

			}

			logger.debug("Insert prepared statement correctly set. It's time to execute it");

			insertPs.executeUpdate();

			logger.debug("Insert correctly executed");

		} catch (Throwable t) {
			logger.error("An unexpected error occured while inserting a new hierarchy");
			throw new SpagoBIServiceException("An unexpected error occured while inserting a new hierarchy", t);
		}

		logger.debug("END");
	}

	private void manageGeneralFieldsSection(IDataSource dataSource, List<Field> generalFields, Map<Integer, String[]> fieldsMap, JSONObject requestVal,
			StringBuffer columnsClause, StringBuffer valuesClause, String sep) throws JSONException {

		int index = fieldsMap.size();

		for (Field tmpField : generalFields) {

			// retrieve id and type for the general field
			String id = tmpField.getId();
			String type = tmpField.getType();

			logger.debug("Processing Hierarchy general field [" + id + "] of type [" + type + "]");

			if (requestVal.isNull(id)) {
				// this general field is missing from the request JSON
				logger.debug("The general field [" + id + "] is not present in the request JSON");
				continue;
			}

			// create a column from the the general field id and take the value from the request JSON
			String column = AbstractJDBCDataset.encapsulateColumnName(id, dataSource);
			String value = requestVal.getString(id);

			logger.debug("The general field [" + id + "] has value [" + value + "] in the request JSON");

			// updating sql clauses for columns and values
			columnsClause.append(column + sep);
			valuesClause.append("?" + sep);

			// updating values and types maps
			String[] tmpArr = new String[] { value, type };
			fieldsMap.put(index, tmpArr);

			index++;

		}

		if (!requestVal.isNull(HierarchyConstants.BEGIN_DT)) {

			String beginDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BEGIN_DT, dataSource);
			String beginDtValue = requestVal.getString(HierarchyConstants.BEGIN_DT);

			// updating sql clauses for columns and values
			columnsClause.append(beginDtColumn + sep);
			valuesClause.append("?" + sep);

			// updating values and types maps
			String[] tmpArr = new String[] { beginDtValue, HierarchyConstants.FIELD_TP_DATE };
			fieldsMap.put(index, tmpArr);

			index++;

		}

		if (!requestVal.isNull(HierarchyConstants.END_DT)) {

			String endDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.END_DT, dataSource);
			String endDtValue = requestVal.getString(HierarchyConstants.END_DT);

			// updating sql clauses for columns and values
			columnsClause.append(endDtColumn + sep);
			valuesClause.append("?" + sep);

			// updating values and types maps
			String[] tmpArr = new String[] { endDtValue, HierarchyConstants.FIELD_TP_DATE };
			fieldsMap.put(index, tmpArr);

		}

	}

	private void manageLevelsSection(IDataSource dataSource, IRecord record, Map<String, Integer> metatadaFieldsMap, Map<Integer, String[]> fieldsMap,
			Map<Integer, String[]> levelsMap, JSONObject requestVal, StringBuffer columnsClause, StringBuffer valuesClause, String sep, String prefix)
			throws JSONException {

		// retrieve levels from request json
		if (requestVal.isNull("levels")) {
			// if levels isn't in the json, just return lvl index
			return;
		}

		JSONArray lvls = requestVal.getJSONArray("levels");

		boolean fillEmpty = false;

		if (!requestVal.isNull("fill_empty")) {
			fillEmpty = Boolean.valueOf(requestVal.getString("fill_empty"));
		}

		int index = fieldsMap.size();
		int lvlIndex = levelsMap.size();

		int lvlsLength = lvls.length();
		logger.debug("The user has specified [" + lvlsLength + "] level/s");

		for (int k = 0; k < lvlsLength; k++) {
			// a level found, increment the index level counter
			lvlIndex++;

			JSONObject lvl = lvls.getJSONObject(k);

			// columns for code and name level
			String cdColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEV" + lvlIndex, dataSource);
			String nmColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_NM_LEV" + lvlIndex, dataSource);

			// retrieve values to look for in dimension columns
			String cdLvl = lvl.getString("CD");
			String nmLvl = lvl.getString("NM");

			logger.debug("In the level [" + lvlIndex + "] user has specified the code [" + cdLvl + "] and the name [" + nmLvl + "]");

			// retrieve record fields looking at metafield position in the dimension
			IField cdTmpField = record.getFieldAt(metatadaFieldsMap.get(cdLvl));
			IField nmTmpField = record.getFieldAt(metatadaFieldsMap.get(nmLvl));

			// String cdValue = (String) cdTmpField.getValue();
			// String nmValue = (String) nmTmpField.getValue();

			// Filling logic: if the user has enabled the filling option, null values in a level are replaced by values from the previous level

			String cdValue = ((cdTmpField.getValue()) != null) ? (String) cdTmpField.getValue() : fillValueHandler(fillEmpty, levelsMap, requestVal,
					HierarchyConstants.CD_VALUE_POSITION);

			String nmValue = ((nmTmpField.getValue()) != null) ? (String) nmTmpField.getValue() : fillValueHandler(fillEmpty, levelsMap, requestVal,
					HierarchyConstants.NM_VALUE_POSITION);

			logger.debug("For the level [" + lvlIndex + "] we are going to insert code [" + cdValue + "] and name [" + nmValue + "]");

			// updating sql clauses for columns and values
			columnsClause.append(cdColumn + "," + nmColumn + sep);
			valuesClause.append("?," + "?" + sep);

			// updating values and types maps
			String[] tmpArrCd = new String[] { cdValue, HierarchyConstants.FIELD_TP_STRING };
			String[] tmpArrNm = new String[] { nmValue, HierarchyConstants.FIELD_TP_STRING };

			fieldsMap.put(index, tmpArrCd);
			fieldsMap.put(index + 1, tmpArrNm);

			// updating level values
			String[] tmpLvl = new String[] { cdValue, nmValue };
			levelsMap.put(lvlIndex, tmpLvl);

			index = index + 2;

		}

	}

	private void manageRecursiveSection(Connection dbConnection, IDataSource dataSource, IRecord record, Map<String, Integer> metatadaFieldsMap,
			Map<Integer, String[]> fieldsMap, Map<Integer, String[]> levelsMap, JSONObject requestVal, StringBuffer columnsClause, StringBuffer valuesClause,
			String sep, String prefix, String dimensionName, String validityDate) throws JSONException, SQLException {

		int index = fieldsMap.size();
		int lvlIndex = levelsMap.size();

		if (!requestVal.isNull("recursive")) {

			LinkedList<String> recursiveValuesList = new LinkedList<String>();

			// retrieve recursive object from request json
			JSONObject recursive = requestVal.getJSONObject("recursive");

			// create columns for parent fields selected in the json

			String jsonRecursiveParentCd = recursive.getString(HierarchyConstants.JSON_CD_PARENT);
			String jsonRecursiveParentNm = recursive.getString(HierarchyConstants.JSON_NM_PARENT);

			logger.debug("Parent field selected are [" + jsonRecursiveParentCd + "] and [" + jsonRecursiveParentNm + "]");

			// create columns for recursive fields selected in the json

			String jsonRecursiveCd = recursive.getString("CD");
			String jsonRecursiveNm = recursive.getString("NM");

			logger.debug("Recursive field selected are [" + jsonRecursiveCd + "] and [" + jsonRecursiveNm + "]");

			// get values from recursive selected fields

			IField recursiveCdField = record.getFieldAt(metatadaFieldsMap.get(jsonRecursiveCd));
			IField recursiveNmField = record.getFieldAt(metatadaFieldsMap.get(jsonRecursiveNm));

			String recursiveCdValue = (String) recursiveCdField.getValue();
			String recursiveNmValue = (String) recursiveNmField.getValue();

			logger.debug("Recursive values are [" + recursiveCdValue + "] and [" + recursiveNmValue + "]");

			// Be careful, LIFO logic!!
			recursiveValuesList.addFirst(recursiveNmValue);
			recursiveValuesList.addFirst(recursiveCdValue);

			logger.debug("Recursive values added to recursive level list!");

			// get values from parent fields

			IField recursiveParentCdField = record.getFieldAt(metatadaFieldsMap.get(jsonRecursiveParentCd));
			IField recursiveParentNmField = record.getFieldAt(metatadaFieldsMap.get(jsonRecursiveParentNm));

			String recursiveParentCdValue = (String) recursiveParentCdField.getValue();
			String recursiveParentNmValue = (String) recursiveParentNmField.getValue();

			logger.debug("Parent values are [" + recursiveParentCdValue + "] and [" + recursiveParentNmValue + "]");

			if (recursiveParentCdValue != null) {

				recursiveParentSelect(dbConnection, dataSource, recursiveValuesList, recursiveParentCdValue, recursiveParentNmValue, recursiveCdValue,
						dimensionName, jsonRecursiveCd, jsonRecursiveNm, jsonRecursiveParentCd, jsonRecursiveParentNm, validityDate);
			}

			int recursiveValuesSize = recursiveValuesList.size();

			boolean fillEmpty = false;

			if (!requestVal.isNull("fill_empty")) {
				fillEmpty = Boolean.valueOf(requestVal.getString("fill_empty"));
			}

			// we use i+2 because we need CD and NM
			for (int i = 0; i < recursiveValuesSize; i = i + 2) {

				lvlIndex++;

				// columns for code and name level
				String cdColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEV" + (lvlIndex), dataSource);
				String nmColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_NM_LEV" + (lvlIndex), dataSource);

				String cdValue = ((recursiveValuesList.get(i)) != null) ? (String) recursiveValuesList.get(i) : fillValueHandler(fillEmpty, levelsMap,
						requestVal, HierarchyConstants.CD_VALUE_POSITION);
				String nmValue = ((recursiveValuesList.get(i + 1)) != null) ? (String) recursiveValuesList.get(i + 1) : fillValueHandler(fillEmpty, levelsMap,
						requestVal, HierarchyConstants.NM_VALUE_POSITION);

				// String cdValue = recursiveValuesList.get(i);
				// String nmValue = recursiveValuesList.get(i + 1);

				logger.debug("In the level [" + lvlIndex + "] user has specified the code [" + cdValue + "] and the name [" + nmValue + "]");

				// updating sql clauses for columns and values
				columnsClause.append(cdColumn + "," + nmColumn + sep);
				valuesClause.append("?," + "?" + sep);

				// updating values and types maps
				String[] tmpArrCd = new String[] { cdValue, HierarchyConstants.FIELD_TP_STRING };
				String[] tmpArrNm = new String[] { nmValue, HierarchyConstants.FIELD_TP_STRING };

				fieldsMap.put(index, tmpArrCd);
				fieldsMap.put(index + 1, tmpArrNm);

				// updating level values
				String[] tmpLvl = new String[] { cdValue, nmValue };
				levelsMap.put(lvlIndex, tmpLvl);

				index = index + 2;

			}

		}

	}

	private void manageLeafSection(IDataSource dataSource, IRecord record, Map<String, Integer> metatadaFieldsMap, Map<Integer, String[]> fieldsMap,
			Map<Integer, String[]> levelsMap, StringBuffer columnsClause, StringBuffer valuesClause, String sep, String prefix) {

		int index = fieldsMap.size();
		int lvlIndex = levelsMap.size();

		// there is at least one level, so we can get the last level as a leaf
		if (lvlIndex >= 1) {

			String[] lvlValues = levelsMap.get(lvlIndex);

			String cdLeafValue = lvlValues[0];
			String nmLeafValue = lvlValues[1];

			String cdLeafColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEAF", dataSource);
			String nmLeafColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_NM_LEAF", dataSource);

			logger.debug("For the leaf we are going to insert code [" + cdLeafValue + "] and name [" + nmLeafValue + "]");

			// updating sql clauses for columns and values
			columnsClause.append(cdLeafColumn + sep + nmLeafColumn + sep);
			valuesClause.append("?,?" + sep);

			// updating values and types maps
			String[] tmpArrCd = new String[] { cdLeafValue, HierarchyConstants.FIELD_TP_STRING };
			String[] tmpArrNm = new String[] { nmLeafValue, HierarchyConstants.FIELD_TP_STRING };

			fieldsMap.put(index, tmpArrCd);
			fieldsMap.put(index + 1, tmpArrNm);

		}

	}

	private void manageParentLeafSection(IDataSource dataSource, Map<Integer, String[]> fieldsMap, Map<Integer, String[]> levelsMap,
			StringBuffer columnsClause, StringBuffer valuesClause, String sep) {

		int index = fieldsMap.size();
		int lvlIndex = levelsMap.size();

		// if there is only one level, we don't have LEAF_PARENT_XX columns
		if (lvlIndex > 1) {

			int parentIndex = lvlIndex - 1;
			String[] lvlValues = levelsMap.get(parentIndex);

			String leafParentCdValue = lvlValues[0];
			String leafParentNmValue = lvlValues[1];

			String cdLeafParentColumn = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_CD", dataSource);
			String nmLeafParentColumn = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_NM", dataSource);

			logger.debug("We are going to use code [" + leafParentCdValue + "] and name [" + leafParentNmValue + "] for parent");

			// updating sql clauses for columns and values
			columnsClause.append(cdLeafParentColumn + "," + nmLeafParentColumn + sep);
			valuesClause.append("?," + "?" + sep);

			// updating values and types maps
			String[] tmpArrCd = new String[] { leafParentCdValue, HierarchyConstants.FIELD_TP_STRING };
			String[] tmpArrNm = new String[] { leafParentNmValue, HierarchyConstants.FIELD_TP_STRING };

			fieldsMap.put(index, tmpArrCd);
			fieldsMap.put(index + 1, tmpArrNm);

		}

	}

	private void manageLeafIdSection(IDataSource dataSource, Map<String, Integer> metatadaFieldsMap, IRecord record, Map<Integer, String[]> fieldsMap,
			StringBuffer columnsClause, StringBuffer valuesClause, String sep, String prefix) {

		int index = fieldsMap.size();

		String leafIdColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_LEAF_ID", dataSource);
		IField leafIdTmpField = record.getFieldAt(metatadaFieldsMap.get(prefix + "_ID"));

		Long leafIdValue = (Long) leafIdTmpField.getValue();

		logger.debug("Leaf ID is [" + leafIdValue + "]");

		// updating sql clauses for columns and values
		columnsClause.append(leafIdColumn + sep);
		valuesClause.append("?" + sep);

		// updating values and types maps
		String[] tmpArr = new String[] { Long.toString(leafIdValue), HierarchyConstants.FIELD_TP_NUMBER };
		fieldsMap.put(index, tmpArr);

	}

	private void manageHierTypeSection(IDataSource dataSource, Map<Integer, String[]> fieldsMap, StringBuffer columnsClause, StringBuffer valuesClause,
			String sep) {
		String hierTypeColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_TP, dataSource);

		logger.debug("Hierarchy tipe is [" + HierarchyConstants.HIER_TP_MASTER + "]");

		int index = fieldsMap.size();

		// updating sql clauses for columns and values
		columnsClause.append(hierTypeColumn + sep);
		valuesClause.append("?" + sep);

		// updating values and types maps
		String[] tmpArr = new String[] { HierarchyConstants.HIER_TP_MASTER, HierarchyConstants.FIELD_TP_STRING };
		fieldsMap.put(index, tmpArr);

	}

	private void manageMaxDepthSection(IDataSource dataSource, Map<Integer, String[]> fieldsMap, Map<Integer, String[]> levelsMap, StringBuffer columnsClause,
			StringBuffer valuesClause) {

		int index = fieldsMap.size();
		int lvlIndex = levelsMap.size();

		String maxDepthColumn = AbstractJDBCDataset.encapsulateColumnName("MAX_DEPTH", dataSource);

		logger.debug("Levels max depth is [" + lvlIndex + "]");

		// updating sql clauses for columns and values
		columnsClause.append(maxDepthColumn + ")");
		valuesClause.append("?)");

		// updating values and types maps
		String[] tmpArr = new String[] { String.valueOf(lvlIndex), HierarchyConstants.FIELD_TP_NUMBER };
		fieldsMap.put(index, tmpArr);
	}

	/**
	 * This method looks for a parent in the dimension table. If a parent is found the values are saved and the process restarts
	 *
	 * @throws SQLException
	 */
	private void recursiveParentSelect(Connection dbConnection, IDataSource dataSource, LinkedList<String> parentValuesList, String parentCdValue,
			String parentNmValue, String oldCdValue, String dimensionName, String jsonRecursiveCd, String jsonRecursiveNm, String jsonRecursiveParentCd,
			String jsonRecursiveParentNm, String validityDate) throws SQLException {

		logger.debug("START");

		String cdParentColumn = AbstractJDBCDataset.encapsulateColumnName(jsonRecursiveParentCd, dataSource);
		String nmParentColumn = AbstractJDBCDataset.encapsulateColumnName(jsonRecursiveParentNm, dataSource);

		String cdRecursiveColumn = AbstractJDBCDataset.encapsulateColumnName(jsonRecursiveCd, dataSource);
		String nmRecursiveColumn = AbstractJDBCDataset.encapsulateColumnName(jsonRecursiveNm, dataSource);

		String beginDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BEGIN_DT, dataSource);
		String endDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.END_DT, dataSource);

		String vDateConverted = HierarchyUtils.getConvertedDate(validityDate, dataSource);

		String vDateWhereClause = vDateConverted + " >= " + beginDtColumn + " AND " + vDateConverted + " <= " + endDtColumn;

		String recursiveSelectClause = cdRecursiveColumn + "," + nmRecursiveColumn + "," + cdParentColumn + "," + nmParentColumn;

		String recurisveSelect = "SELECT " + recursiveSelectClause + " FROM " + dimensionName + " WHERE " + cdRecursiveColumn + " = \"" + parentCdValue
				+ "\" AND " + nmRecursiveColumn + " = \"" + parentNmValue + "\" AND " + vDateWhereClause;

		logger.debug("Select query is [" + recurisveSelect + "]");

		Statement statement = dbConnection.createStatement();

		logger.debug("Statment is using [" + parentCdValue + "] and [" + parentNmValue + "] with validity date [" + validityDate + "]");

		ResultSet rs = statement.executeQuery(recurisveSelect);

		if (rs.next()) {

			String newRecursiveCdValue = rs.getString(jsonRecursiveCd);
			String newRecursiveNmValue = rs.getString(jsonRecursiveNm);

			// be careful, LIFO logic!!
			parentValuesList.addFirst(newRecursiveNmValue);
			parentValuesList.addFirst(newRecursiveCdValue);

			logger.debug("Result found! Creating a new recursive level with values [" + newRecursiveCdValue + "] and [" + newRecursiveNmValue + "]");

			String tmpParentCdValue = rs.getString(jsonRecursiveParentCd);
			String tmpParentNmValue = rs.getString(jsonRecursiveParentNm);

			if (tmpParentCdValue != null) {

				logger.debug("Check values validity. New value is [" + tmpParentCdValue + "] and old is [" + oldCdValue + "]");

				if (tmpParentCdValue.equals(oldCdValue)) {
					logger.error("Impossible to create recursive levels. A cycle found during recursive selections");
					throw new SQLException("Impossible to create recursive levels. A cycle found during recursive selections");
				}

				logger.debug("Look for another parent with values [" + tmpParentCdValue + "] and [" + tmpParentNmValue + "]");

				recursiveParentSelect(dbConnection, dataSource, parentValuesList, tmpParentCdValue, tmpParentNmValue, newRecursiveCdValue, dimensionName,
						jsonRecursiveCd, jsonRecursiveNm, jsonRecursiveParentCd, jsonRecursiveParentNm, validityDate);
			} else {
				logger.debug("No parent found!");
				logger.debug("END");
				return;
			}

		}

	}

	private String fillValueHandler(boolean fillEmpty, Map<Integer, String[]> levelsMap, JSONObject requestVal, int valueIndex) throws JSONException {

		String fillValue = null;

		// the level index points at the last level inserted
		int lvlIndex = levelsMap.size();

		if (fillEmpty && lvlIndex == 0) {
			fillValue = (requestVal.isNull("fill_value")) ? null : requestVal.getString("fill_value");
		} else if (fillEmpty && lvlIndex > 0) {
			fillValue = (requestVal.isNull("fill_value")) ? levelsMap.get(lvlIndex)[valueIndex] : requestVal.getString("fill_value");
		}

		return fillValue;

	}

}
