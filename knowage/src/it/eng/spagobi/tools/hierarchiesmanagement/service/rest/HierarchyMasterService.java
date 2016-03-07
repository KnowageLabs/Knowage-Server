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
package it.eng.spagobi.tools.hierarchiesmanagement.service.rest;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Dimension;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Hierarchy;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.FillConfiguration;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyUtils;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
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
	@UserConstraint(functionalities = { SpagoBIConstants.HIERARCHIES_MANAGEMENT })
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
			String optionalFilters = (requestVal.isNull("optionalFilters")) ? null : requestVal.getString("optionalFilters");

			if ((dimensionLabel == null) || (validityDate == null)) {
				throw new SpagoBIServiceException("An unexpected error occured while creating hierarchy master", "wrong request parameters");
			}

			IDataSource dataSource = HierarchyUtils.getDataSource(dimensionLabel);

			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}

			dbConnection = dataSource.getConnection();
			dbConnection.setAutoCommit(false);

			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			Assert.assertNotNull(hierarchies, "Impossible to find a valid hierarchies object");

			Dimension dimension = hierarchies.getDimension(dimensionLabel);
			Assert.assertNotNull(dimension, "Impossible to find a valid dimension with label [" + dimensionLabel + "]");

			Hierarchy hierarchy = hierarchies.getHierarchy(dimensionLabel);
			Assert.assertNotNull(hierarchy, "Impossible to find a valid hierarchy with label [" + dimensionLabel + "]");

			String dimensionName = dimension.getName();
			String hierTableName = hierarchies.getHierarchyTableName(dimensionLabel);
			String prefix = hierarchies.getPrefix(dimensionLabel);

			HashMap hierConfig = hierarchies.getConfig(dimensionLabel);

			List<Field> metadataFields = new ArrayList<Field>(dimension.getMetadataFields());
			Map<String, Integer> metatadaFieldsMap = HierarchyUtils.getMetadataFieldsMap(metadataFields);

			List<Field> generalFields = new ArrayList<Field>(hierarchy.getMetadataGeneralFields());
			List<Field> nodeFields = new ArrayList<Field>(hierarchy.getMetadataNodeFields());
			boolean exludeHierLeaf = (filterHierarchy != null) ? true : false;
			IDataStore dataStore = HierarchyUtils.getDimensionDataStore(dataSource, dimensionName, metadataFields, validityDate, optionalFilters, filterDate,
					filterHierarchy, filterHierType, hierTableName, prefix, exludeHierLeaf);

			Iterator iterator = dataStore.iterator();

			while (iterator.hasNext()) {
				// dataStore.
				IRecord record = (IRecord) iterator.next();
				insertHierarchyMaster(dbConnection, dataSource, record, dataStore, hierTableName, generalFields, nodeFields, metatadaFieldsMap, requestVal,
						prefix, dimensionName, validityDate, hierConfig);

			}

			saveHierarchyMasterConfiguration(dbConnection, dataSource, requestVal);

			dbConnection.commit();

		} catch (Throwable t) {
			if (dbConnection != null && !dbConnection.isClosed()) {
				dbConnection.rollback();
			}
			logger.error("An unexpected error occured while retriving dimension data");
			throw new SpagoBIServiceException("An unexpected error occured while retriving dimension data", t);
		} finally {
			dbConnection.close();
		}

		logger.debug("END");
		return "{\"response\":\"ok\"}";

	}

	@POST
	@Path("/syncronizeHierarchyMaster")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.HIERARCHIES_MANAGEMENT })
	public String syncronizeHierarchyMaster(@Context HttpServletRequest req) throws SQLException {

		logger.debug("START");

		Connection dbConnection = null;

		try {

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			String dimensionLabel = requestVal.getString("dimension");
			String validityDate = requestVal.getString("validityDate");
			String validityTreeDate = requestVal.getString("validityTreeDate");
			String filterDate = (requestVal.isNull("filterDate")) ? null : requestVal.getString("filterDate");
			String filterHierarchy = (requestVal.isNull("filterHierarchy")) ? null : requestVal.getString("filterHierarchy");
			String filterHierType = (requestVal.isNull("filterHierType")) ? null : requestVal.getString("filterHierType");
			String optionalFilters = (requestVal.isNull("optionalFilters")) ? null : requestVal.getString("optionalFilters");
			String optionDate = (requestVal.isNull("optionDate")) ? null : requestVal.getString("optionDate");
			String optionHierarchy = (requestVal.isNull("optionHierarchy")) ? null : requestVal.getString("optionHierarchy");
			String optionHierType = (requestVal.isNull("optionHierType")) ? null : requestVal.getString("optionHierType");

			if ((dimensionLabel == null) || (validityDate == null)) {
				throw new SpagoBIServiceException("An unexpected error occured while syncronize hierarchy master", "wrong request parameters");
			}

			IDataSource dataSource = HierarchyUtils.getDataSource(dimensionLabel);

			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}

			dbConnection = dataSource.getConnection();

			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			Assert.assertNotNull(hierarchies, "Impossible to find a valid hierarchies object");

			Dimension dimension = hierarchies.getDimension(dimensionLabel);
			Assert.assertNotNull(dimension, "Impossible to find a valid dimension with label [" + dimensionLabel + "]");

			Hierarchy hierarchy = hierarchies.getHierarchy(dimensionLabel);
			Assert.assertNotNull(hierarchy, "Impossible to find a valid hierarchy for dimension [" + dimensionLabel + "]");

			String dimensionName = dimension.getName();
			String hierTableName = hierarchies.getHierarchyTableName(dimensionLabel);
			String prefix = hierarchies.getPrefix(dimensionLabel);

			HashMap hierConfig = hierarchies.getConfig(dimensionLabel);

			List<Field> metadataFields = new ArrayList<Field>(dimension.getMetadataFields());
			Map<String, Integer> metatadaFieldsMap = HierarchyUtils.getMetadataFieldsMap(metadataFields);

			List<Field> generalFields = new ArrayList<Field>(hierarchy.getMetadataGeneralFields());
			List<Field> nodeFields = new ArrayList<Field>(hierarchy.getMetadataNodeFields());

			// read original configuration of MASTER table from DB
			String masterConfig = getHierMasterConfig(dataSource, dbConnection, filterHierarchy);
			if (masterConfig == null) {
				logger.error("Impossible synchronize the hierarchy. Original configuration not found!");
				throw new SpagoBIServiceException("Error",
						"Impossible synchronize the hierarchy. Original configuration not found. Probably the hierarchy was created throught external tools.");
			}
			requestVal = new JSONObject(masterConfig);

			// 2 - Get the dimension datastore (throught the input parameters): it will be used to create the new version of the hierarchy
			boolean exludeHierLeaf = false;
			String hierNameForDim = null; // hiername for fil
			if (optionHierarchy != null) {
				exludeHierLeaf = true;
				hierNameForDim = optionHierarchy;
			}
			IDataStore dsNewDimensions = HierarchyUtils.getDimensionDataStore(dataSource, dimensionName, metadataFields, validityDate, optionalFilters,
					hierNameForDim, filterDate, filterHierType, hierTableName, prefix, exludeHierLeaf);

			// 3 - Get the dimension leaves already present into the original Hierarchy
			IDataStore dsDimensionsFromHier = HierarchyUtils.getDimensionFromHierDataStore(dataSource, dimensionName, metadataFields, validityDate,
					optionalFilters, validityTreeDate, filterHierarchy, filterHierType, hierTableName, prefix, false);

			// 4 - Iterate on the dimensions' leaves used by the hierarchy datastore and check if the record is present into the dimension datastore:
			// If it exists do nothing (get the new last)
			// If it doesn't exist add the original record into the dimension datatore (merge action)
			IMetaData metaDim = dsDimensionsFromHier.getMetaData();
			int posCD = -1;
			for (int i = 0; i < metaDim.getFieldCount(); i++) {
				IFieldMetaData fieldMeta = metaDim.getFieldMeta(i);
				if (fieldMeta.getName().equals(prefix + HierarchyConstants.DIM_FILTER_FIELD)) {
					posCD = i;
					break;
				}
			}
			if (posCD == -1) {
				logger.error("Impossible synchronize the hierarchy.");
				throw new SpagoBIServiceException("Error", "Impossible synchronize the hierarchy. Column " + prefix + HierarchyConstants.DIM_FILTER_FIELD
						+ " not found into the resultset. ");
			}

			Iterator iterFromHier = dsDimensionsFromHier.iterator();
			while (iterFromHier.hasNext()) {
				// iterate on dimension records
				IRecord hierRecord = (IRecord) iterFromHier.next();
				IField f = hierRecord.getFieldAt(posCD);
				List recs = dsNewDimensions.findRecords(posCD, f.getValue());
				if (recs.size() == 0) {
					dsNewDimensions.appendRecord(hierRecord);
				}
			}
			// begin transaction
			dbConnection.setAutoCommit(false);
			// 1 - Backup the original hierarchy (always)
			HashMap paramsMap = new HashMap();
			paramsMap.put("validityDate", validityTreeDate);
			paramsMap.put("hierarchyTable", hierTableName);
			paramsMap.put("hierTargetName", filterHierarchy);
			paramsMap.put("hierTargetType", filterHierType);
			paramsMap.put("doPropagation", true);

			HierarchyUtils.updateHierarchyForBackup(dataSource, dbConnection, paramsMap);

			// 5 - insert the new hierarchy (merged)
			Iterator iterFromDim = dsNewDimensions.iterator();
			while (iterFromDim.hasNext()) {
				// iterate on dimension records
				IRecord record = (IRecord) iterFromDim.next();
				insertHierarchyMaster(dbConnection, dataSource, record, dsNewDimensions, hierTableName, generalFields, nodeFields, metatadaFieldsMap,
						requestVal, prefix, dimensionName, validityDate, hierConfig);
			}

			dbConnection.commit();

		} catch (Throwable t) {
			if (dbConnection.getAutoCommit() == false && dbConnection != null && !dbConnection.isClosed()) {
				dbConnection.rollback();
			}
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
			List<Field> generalFields, List<Field> nodeFields, Map<String, Integer> metatadaFieldsMap, JSONObject requestVal, String prefix,
			String dimensionName, String validityDate, HashMap hierConfig) {

		logger.debug("START");

		try (Statement stmt = dbConnection.createStatement()) {

			// Create two clauses, one for columns and another for values
			// INSERT INTO name_table [columnsClause] values [valuesClause]
			StringBuffer columnsClause = new StringBuffer("(");
			StringBuffer valuesClause = new StringBuffer("(");

			String sep = ",";

			// fieldsMap is necessary to keep track of the position for values we need to use later when replace the prep. stat.
			Map<Integer, Object> fieldsMap = new HashMap<Integer, Object>();

			// levelsMap is necessary to keep track of values for levels
			Map<Integer, Object[]> levelsMap = new HashMap<Integer, Object[]>();

			// this counter come across different logics to build the insert query and it's used to keep things sequential
			// int index = 0;

			// used to keep track of levels for parent, leaf and max-depth
			// int lvlIndex = 0;

			// DONT' CHANGE SECTIONS ORDER! Indexes to calculate lvls, leaf, parent, etc are ordered with the following logic
			// So, if you need to change this code, be careful of indexes order!

			// configuration for the filling logic
			FillConfiguration fillConfiguration = new FillConfiguration(hierConfig);

			/**********************************************************************************************************
			 * in this section we add columns and values related to hierarchy general fields specified in request JSON*
			 **********************************************************************************************************/

			manageGeneralFieldsSection(dataSource, generalFields, fieldsMap, requestVal, columnsClause, valuesClause, sep);

			/****************************************************************************************
			 * in this section we add columns and values related to levels specified in request JSON*
			 ****************************************************************************************/

			manageLevelsSection(dataSource, nodeFields, record, metatadaFieldsMap, fieldsMap, levelsMap, requestVal, columnsClause, valuesClause, sep, prefix,
					fillConfiguration, hierConfig);

			/***********************************************************************
			 * in this section we add a recursive logic to calculate parents levels*
			 ***********************************************************************/

			manageRecursiveSection(dbConnection, dataSource, nodeFields, record, metatadaFieldsMap, fieldsMap, levelsMap, requestVal, columnsClause,
					valuesClause, sep, prefix, dimensionName, validityDate, fillConfiguration, hierConfig);

			checkMaxLevel(levelsMap, hierConfig);

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

				Object fieldValue = fieldsMap.get(i);

				logger.debug("Set the insert prepared statement with a field value [" + fieldValue + "]");

				insertPs.setObject(i + 1, fieldValue);

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

	private void manageGeneralFieldsSection(IDataSource dataSource, List<Field> generalFields, Map<Integer, Object> fieldsMap, JSONObject requestVal,
			StringBuffer columnsClause, StringBuffer valuesClause, String sep) throws JSONException, ParseException {

		int index = fieldsMap.size();

		for (Field tmpField : generalFields) {

			// retrieve id and type for the general field
			String id = tmpField.getId();

			if (requestVal.isNull(id)) {
				// this general field is missing from the request JSON
				logger.debug("The general field [" + id + "] is not present in the request JSON");
				continue;
			}
			String value = requestVal.getString(id);
			logger.debug("The general field [" + id + "] has value [" + value + "] in the request JSON");

			// create a column from the the general field id and take the value from the request JSON
			String column = AbstractJDBCDataset.encapsulateColumnName(id, dataSource);

			// updating sql clauses for columns and values
			columnsClause.append(column + sep);
			valuesClause.append("?" + sep);

			// updating values and types maps
			fieldsMap.put(index, value);

			index++;

		}

		if (!requestVal.isNull(HierarchyConstants.BEGIN_DT)) {

			String beginDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BEGIN_DT, dataSource);
			String beginDtValue = requestVal.getString(HierarchyConstants.BEGIN_DT);

			// updating sql clauses for columns and values
			columnsClause.append(beginDtColumn + sep);
			valuesClause.append("?" + sep);

			// updating values and types maps
			fieldsMap.put(index, beginDtValue);

			index++;

		}

		if (!requestVal.isNull(HierarchyConstants.END_DT)) {

			String endDtColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.END_DT, dataSource);
			String endDtValue = requestVal.getString(HierarchyConstants.END_DT);

			// updating sql clauses for columns and values
			columnsClause.append(endDtColumn + sep);
			valuesClause.append("?" + sep);

			// updating values and types maps
			fieldsMap.put(index, endDtValue);

		}

	}

	private void manageLevelsSection(IDataSource dataSource, List<Field> nodeFields, IRecord record, Map<String, Integer> metatadaFieldsMap,
			Map<Integer, Object> fieldsMap, Map<Integer, Object[]> levelsMap, JSONObject requestVal, StringBuffer columnsClause, StringBuffer valuesClause,
			String sep, String prefix, FillConfiguration fillConfiguration, HashMap hierConfig) throws JSONException {

		// retrieve levels from request json
		if (requestVal.isNull("levels")) {
			// if levels isn't in the json, just return lvl index
			return;
		}

		JSONArray lvls = requestVal.getJSONArray("levels");

		int index = fieldsMap.size();
		int lvlIndex = levelsMap.size();

		int lvlsLength = lvls.length();
		logger.debug("The user has specified [" + lvlsLength + "] levels");

		String concatNmValues = requestVal.getString(HierarchyConstants.HIER_NM);
		for (int k = 0; k < lvlsLength; k++) {
			// a level found, increment the index level counter
			lvlIndex++;

			JSONObject lvl = lvls.getJSONObject(k);

			// columns for code and name level
			// String cdColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEV" + lvlIndex, dataSource);
			// String nmColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_NM_LEV" + lvlIndex, dataSource);
			String cdColumn = AbstractJDBCDataset.encapsulateColumnName((String) hierConfig.get(HierarchyConstants.TREE_NODE_CD) + lvlIndex, dataSource);
			String nmColumn = AbstractJDBCDataset.encapsulateColumnName((String) hierConfig.get(HierarchyConstants.TREE_NODE_NM) + lvlIndex, dataSource);

			// retrieve values to look for in dimension columns
			String cdLvl = lvl.getString("CD");
			String nmLvl = lvl.getString("NM");

			logger.debug("In the level [" + lvlIndex + "] user has specified the code [" + cdLvl + "] and the name [" + nmLvl + "]");

			// retrieve record fields looking at metafield position in the dimension
			IField cdTmpField = record.getFieldAt(metatadaFieldsMap.get(cdLvl));
			IField nmTmpField = record.getFieldAt(metatadaFieldsMap.get(nmLvl));

			// Filling logic: if the user has enabled the filling option, null values in a level are replaced by values from the previous level

			Object cdValue = ((cdTmpField.getValue()) != null) ? cdTmpField.getValue() : fillConfiguration.fillHandler(levelsMap,
					HierarchyConstants.CD_VALUE_POSITION);

			Object nmValue = ((nmTmpField.getValue()) != null) ? nmTmpField.getValue() : fillConfiguration.fillHandler(levelsMap,
					HierarchyConstants.NM_VALUE_POSITION);

			concatNmValues += (nmValue == null) ? "" : nmValue;

			logger.debug("For the level [" + lvlIndex + "] we are going to insert code [" + cdValue + "] and name [" + nmValue + "]");

			// updating sql clauses for columns and values
			columnsClause.append(cdColumn + "," + nmColumn + sep);
			valuesClause.append("?," + "?" + sep);

			// updating values and types maps
			fieldsMap.put(index, cdValue);
			fieldsMap.put(index + 1, nmValue);

			// updating level values
			Object[] tmpLvl = new Object[] { cdValue, nmValue };
			levelsMap.put(lvlIndex, tmpLvl);

			index = index + 2;

			// After the single level if the node must be unique search for the unique column (only one) and sets it
			String cdUniqueColumn = null;
			Object cdUniqueValue = null;
			boolean uniqueNodeMng = Boolean.parseBoolean((String) hierConfig.get(HierarchyConstants.UNIQUE_NODE));
			if (uniqueNodeMng) {
				for (int n = 0; n < nodeFields.size(); n++) {
					Field f = nodeFields.get(n);
					if (f.isUniqueCode()) {
						cdUniqueColumn = AbstractJDBCDataset.encapsulateColumnName(f.getId() + lvlIndex, dataSource);
						// cdUniqueValue = (cdValue == null || cdValue.equals("")) ? null : Helper.sha256(String.valueOf(Math.random()) + concatNmValues);
						cdUniqueValue = (cdValue == null || cdValue.equals("")) ? null : Helper.sha256(concatNmValues);
						break;
					}
				}
				columnsClause.append(cdUniqueColumn + sep);
				valuesClause.append("?" + sep);
				fieldsMap.put(index, cdUniqueValue);
				index = index + 1;
			}

		}
	}

	private void manageRecursiveSection(Connection dbConnection, IDataSource dataSource, List<Field> nodeFields, IRecord record,
			Map<String, Integer> metatadaFieldsMap, Map<Integer, Object> fieldsMap, Map<Integer, Object[]> levelsMap, JSONObject requestVal,
			StringBuffer columnsClause, StringBuffer valuesClause, String sep, String prefix, String dimensionName, String validityDate,
			FillConfiguration fillConfiguration, HashMap hierConfig) throws JSONException, SQLException {

		int index = fieldsMap.size();
		int lvlIndex = levelsMap.size();

		if (!requestVal.isNull("recursive")) {

			LinkedList<Object> recursiveValuesList = new LinkedList<Object>();

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

			Object recursiveCdValue = recursiveCdField.getValue();
			Object recursiveNmValue = recursiveNmField.getValue();

			logger.debug("Recursive values are [" + recursiveCdValue + "] and [" + recursiveNmValue + "]");

			// Be careful, LIFO logic!!
			recursiveValuesList.addFirst(recursiveNmValue);
			recursiveValuesList.addFirst(recursiveCdValue);

			logger.debug("Recursive values added to recursive level list!");

			// get values from parent fields

			IField recursiveParentCdField = record.getFieldAt(metatadaFieldsMap.get(jsonRecursiveParentCd));
			IField recursiveParentNmField = record.getFieldAt(metatadaFieldsMap.get(jsonRecursiveParentNm));

			Object recursiveParentCdValue = recursiveParentCdField.getValue();
			Object recursiveParentNmValue = recursiveParentNmField.getValue();

			logger.debug("Parent values are [" + recursiveParentCdValue + "] and [" + recursiveParentNmValue + "]");

			if (recursiveParentCdValue != null) {

				recursiveParentSelect(dbConnection, dataSource, recursiveValuesList, recursiveParentCdValue, recursiveParentNmValue, recursiveCdValue,
						dimensionName, jsonRecursiveCd, jsonRecursiveNm, jsonRecursiveParentCd, jsonRecursiveParentNm, validityDate);
			}

			int recursiveValuesSize = recursiveValuesList.size();
			String concatNmValues = requestVal.getString(HierarchyConstants.HIER_NM);

			// we use i+2 because we need CD and NM
			for (int i = 0; i < recursiveValuesSize; i = i + 2) {

				lvlIndex++;

				// columns for code and name level
				// String cdColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEV" + (lvlIndex), dataSource);
				// String nmColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_NM_LEV" + (lvlIndex), dataSource);
				String cdColumn = AbstractJDBCDataset.encapsulateColumnName((String) hierConfig.get(HierarchyConstants.TREE_NODE_CD) + lvlIndex, dataSource);
				String nmColumn = AbstractJDBCDataset.encapsulateColumnName((String) hierConfig.get(HierarchyConstants.TREE_NODE_NM) + lvlIndex, dataSource);

				Object cdValue = ((recursiveValuesList.get(i)) != null) ? recursiveValuesList.get(i) : fillConfiguration.fillHandler(levelsMap,
						HierarchyConstants.CD_VALUE_POSITION);
				Object nmValue = ((recursiveValuesList.get(i + 1)) != null) ? recursiveValuesList.get(i + 1) : fillConfiguration.fillHandler(levelsMap,
						HierarchyConstants.NM_VALUE_POSITION);

				logger.debug("In the level [" + lvlIndex + "] user has specified the code [" + cdValue + "] and the name [" + nmValue + "]");
				concatNmValues += (nmValue == null) ? "" : nmValue;

				// updating sql clauses for columns and values
				columnsClause.append(cdColumn + "," + nmColumn + sep);
				valuesClause.append("?," + "?" + sep);

				// updating values and types maps
				fieldsMap.put(index, cdValue);
				fieldsMap.put(index + 1, nmValue);

				// updating level values
				Object[] tmpLvl = new Object[] { cdValue, nmValue };
				levelsMap.put(lvlIndex, tmpLvl);

				index = index + 2;

				// After the single level if the node must be unique search for the unique column (only one) and sets it
				String cdUniqueColumn = null;
				Object cdUniqueValue = null;
				boolean uniqueNodeMng = Boolean.parseBoolean((String) hierConfig.get(HierarchyConstants.UNIQUE_NODE));
				if (uniqueNodeMng) {
					for (int n = 0; n < nodeFields.size(); n++) {
						Field f = nodeFields.get(n);
						if (f.isUniqueCode()) {
							cdUniqueColumn = AbstractJDBCDataset.encapsulateColumnName(f.getId() + lvlIndex, dataSource);
							// cdUniqueValue = (cdValue == null || cdValue.equals("")) ? null : Helper.sha256(String.valueOf(Math.random()) + concatNmValues);
							cdUniqueValue = (cdValue == null || cdValue.equals("")) ? null : Helper.sha256(concatNmValues);
							break;
						}
					}
					columnsClause.append(cdUniqueColumn + sep);
					valuesClause.append("?" + sep);
					fieldsMap.put(index, cdUniqueValue);
					index = index + 1;
				}

			}

		}

	}

	private void manageLeafSection(IDataSource dataSource, IRecord record, Map<String, Integer> metatadaFieldsMap, Map<Integer, Object> fieldsMap,
			Map<Integer, Object[]> levelsMap, StringBuffer columnsClause, StringBuffer valuesClause, String sep, String prefix) {

		int index = fieldsMap.size();
		int lvlIndex = levelsMap.size();

		// there is at least one level, so we can get the last level as a leaf
		if (lvlIndex >= 1) {

			Object[] lvlValues = levelsMap.get(lvlIndex);

			Object cdLeafValue = lvlValues[HierarchyConstants.CD_VALUE_POSITION];
			Object nmLeafValue = lvlValues[HierarchyConstants.NM_VALUE_POSITION];

			String cdLeafColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEAF", dataSource);
			String nmLeafColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_NM_LEAF", dataSource);

			logger.debug("For the leaf we are going to insert code [" + cdLeafValue + "] and name [" + nmLeafValue + "]");

			// updating sql clauses for columns and values
			columnsClause.append(cdLeafColumn + sep + nmLeafColumn + sep);
			valuesClause.append("?,?" + sep);

			// updating values and types maps
			fieldsMap.put(index, cdLeafValue);
			fieldsMap.put(index + 1, nmLeafValue);

		}

	}

	private void manageParentLeafSection(IDataSource dataSource, Map<Integer, Object> fieldsMap, Map<Integer, Object[]> levelsMap, StringBuffer columnsClause,
			StringBuffer valuesClause, String sep) {

		int index = fieldsMap.size();
		int lvlIndex = levelsMap.size();

		// if there is only one level, we don't have LEAF_PARENT_XX columns
		if (lvlIndex > 1) {

			Object leafParentCdValue = null;
			Object leafParentNmValue = null;

			int parentIndex = lvlIndex - 1;

			for (int i = parentIndex; i > 0; i--) {

				logger.debug("Looking for parent leaf at level [" + i + "]");

				Object[] lvlValues = levelsMap.get(i);

				leafParentCdValue = lvlValues[HierarchyConstants.CD_VALUE_POSITION];
				leafParentNmValue = lvlValues[HierarchyConstants.NM_VALUE_POSITION];

				if (leafParentCdValue != null && leafParentNmValue != null && !leafParentCdValue.equals("") && !leafParentNmValue.equals("")) {

					logger.debug("Found a valorized parent! Break the loop.");
					break;
				}
			}

			String cdLeafParentColumn = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_CD", dataSource);
			String nmLeafParentColumn = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_NM", dataSource);

			logger.debug("We are going to use code [" + leafParentCdValue + "] and name [" + leafParentNmValue + "] for parent");

			// updating sql clauses for columns and values
			columnsClause.append(cdLeafParentColumn + "," + nmLeafParentColumn + sep);
			valuesClause.append("?," + "?" + sep);

			// updating values and types maps
			fieldsMap.put(index, leafParentCdValue);
			fieldsMap.put(index + 1, leafParentNmValue);

		}

	}

	private void manageLeafIdSection(IDataSource dataSource, Map<String, Integer> metatadaFieldsMap, IRecord record, Map<Integer, Object> fieldsMap,
			StringBuffer columnsClause, StringBuffer valuesClause, String sep, String prefix) {

		int index = fieldsMap.size();

		String leafIdColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_LEAF_ID", dataSource);
		IField leafIdTmpField = record.getFieldAt(metatadaFieldsMap.get(prefix + "_ID"));

		Object leafIdValue = leafIdTmpField.getValue();

		logger.debug("Leaf ID is [" + leafIdValue + "]");

		// updating sql clauses for columns and values
		columnsClause.append(leafIdColumn + sep);
		valuesClause.append("?" + sep);

		// updating values and types maps
		fieldsMap.put(index, leafIdValue);

	}

	private void manageHierTypeSection(IDataSource dataSource, Map<Integer, Object> fieldsMap, StringBuffer columnsClause, StringBuffer valuesClause, String sep) {
		String hierTypeColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_TP, dataSource);

		logger.debug("Hierarchy tipe is [" + HierarchyConstants.HIER_TP_MASTER + "]");

		int index = fieldsMap.size();

		// updating sql clauses for columns and values
		columnsClause.append(hierTypeColumn + sep);
		valuesClause.append("?" + sep);

		// updating values and types maps
		fieldsMap.put(index, HierarchyConstants.HIER_TP_MASTER);

	}

	private void manageMaxDepthSection(IDataSource dataSource, Map<Integer, Object> fieldsMap, Map<Integer, Object[]> levelsMap, StringBuffer columnsClause,
			StringBuffer valuesClause) {

		int index = fieldsMap.size();
		int lvlIndex = levelsMap.size();

		String maxDepthColumn = AbstractJDBCDataset.encapsulateColumnName("MAX_DEPTH", dataSource);

		logger.debug("Levels max depth is [" + lvlIndex + "]");

		// updating sql clauses for columns and values
		columnsClause.append(maxDepthColumn + ")");
		valuesClause.append("?)");

		// updating values and types maps
		fieldsMap.put(index, lvlIndex);
	}

	/**
	 * This method looks for a parent in the dimension table. If a parent is found the values are saved and the process restarts
	 *
	 * @throws SQLException
	 */
	private void recursiveParentSelect(Connection dbConnection, IDataSource dataSource, LinkedList<Object> parentValuesList, Object parentCdValue,
			Object parentNmValue, Object oldCdValue, String dimensionName, String jsonRecursiveCd, String jsonRecursiveNm, String jsonRecursiveParentCd,
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

		String recurisveSelect = "SELECT " + recursiveSelectClause + " FROM " + dimensionName + " WHERE " + cdRecursiveColumn + " = ? AND " + nmRecursiveColumn
				+ " = ? AND " + vDateWhereClause;

		logger.debug("Select query is [" + recurisveSelect + "]");

		PreparedStatement ps = dbConnection.prepareStatement(recurisveSelect);
		ps.setObject(1, parentCdValue);
		ps.setObject(2, parentNmValue);

		logger.debug("PreparedStatment is using [" + parentCdValue + "] and [" + parentNmValue + "] with validity date [" + validityDate + "]");

		ResultSet rs = ps.executeQuery();

		if (rs.next()) {

			Object newRecursiveCdValue = rs.getObject(jsonRecursiveCd);
			Object newRecursiveNmValue = rs.getObject(jsonRecursiveNm);

			// be careful, LIFO logic!!
			parentValuesList.addFirst(newRecursiveNmValue);
			parentValuesList.addFirst(newRecursiveCdValue);

			logger.debug("Result found! Creating a new recursive level with values [" + newRecursiveCdValue + "] and [" + newRecursiveNmValue + "]");

			Object tmpParentCdValue = rs.getObject(jsonRecursiveParentCd);
			Object tmpParentNmValue = rs.getObject(jsonRecursiveParentNm);

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

	private void checkMaxLevel(Map<Integer, Object[]> levelsMap, HashMap hierConfig) throws SQLException {

		int lvlIndex = levelsMap.size();

		int numLevels = Integer.parseInt((String) hierConfig.get(HierarchyConstants.NUM_LEVELS));

		if (lvlIndex > numLevels) {
			throw new SQLException("Creation failed. You have " + lvlIndex + " levels, but the maximum is " + numLevels + " levels");
		}
	}

	private String getHierMasterConfig(IDataSource dataSource, Connection dbConnection, String hierarchyName) {
		logger.debug("START");
		String toReturn = null;

		String hierCdColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_CD, dataSource);
		String hierNmColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
		String confColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_MASTERS_CONFIG, dataSource);

		String selectClause = hierCdColumn + "," + hierNmColumn + "," + confColumn;

		String selectQuery = "SELECT " + selectClause + " FROM " + HierarchyConstants.HIER_MASTERS_CONFIG_TABLE + " WHERE HIER_NM = ? ";

		try (Statement stmt = dbConnection.createStatement(); PreparedStatement selectPs = dbConnection.prepareStatement(selectQuery)) {

			selectPs.setString(1, hierarchyName);

			logger.debug("Preparing select statement. Name of the master configuration is [" + hierarchyName + "]");

			ResultSet rs = selectPs.executeQuery();

			logger.debug("Select query executed! Processing result set...");

			if (rs.next()) {
				toReturn = rs.getString(HierarchyConstants.HIER_MASTERS_CONFIG);
			}
		} catch (Throwable t) {
			logger.error("An unexpected error occured while restoring hierarchy backup");
			throw new SpagoBIServiceException("An unexpected error occured while restoring hierarchy backup", t);
		}

		logger.debug("Getted configuration for hierarchy master: " + hierarchyName + " - " + toReturn);
		logger.debug("END");
		return toReturn;
	}
}
