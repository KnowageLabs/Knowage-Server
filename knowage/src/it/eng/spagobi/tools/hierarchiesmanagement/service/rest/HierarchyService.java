package it.eng.spagobi.tools.hierarchiesmanagement.service.rest;

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
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchyTreeNode;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchyTreeNodeData;
import it.eng.spagobi.tools.hierarchiesmanagement.TreeString;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Dimension;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Hierarchy;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

@Path("/hierarchies")
public class HierarchyService {

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
			// IDataStore dataStore = dataSource.executeStatement("SELECT DISTINCT(" + hierarchyNameColumn + ") FROM " + tableName + " WHERE " + typeColumn
			// + "=\"MASTER\" AND " + bkpColumn + "= 0", 0, 0);

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

	@GET
	@Path("/getHierarchiesTechnical")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchiesTechnical(@QueryParam("dimension") String dimension) {
		JSONArray hierarchiesJSONArray = new JSONArray();

		logger.debug("START");
		try {

			// 1 - get hierarchy table postfix(ex: _CDC)
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
			String hierarchyCodeColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_CD, dataSource);
			String hierarchyNameColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
			String typeColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_TP, dataSource);
			String hierarchyDescriptionColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_DS, dataSource);
			// String scopeColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_SCOPE, dataSource);
			String bkpColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BKP_COLUMN, dataSource);

			// String columns = hierarchyNameColumn + "," + typeColumn + "," + hierarchyDescriptionColumn + "," + scopeColumn + " ";
			String columns = hierarchyNameColumn + "," + typeColumn + "," + hierarchyDescriptionColumn + " ";
			IDataStore dataStore = dataSource.executeStatement("SELECT DISTINCT(" + hierarchyCodeColumn + ")," + columns + " FROM " + tableName + " WHERE "
					+ typeColumn + "=\"TECHNICAL\" AND " + bkpColumn + "= 0 ORDER BY " + hierarchyCodeColumn, 0, 0);
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
				// field = record.getFieldAt(4);
				// String hierarchyScope = (String) field.getValue();
				JSONObject hierarchy = new JSONObject();
				hierarchy.put(HierarchyConstants.HIER_CD, hierarchyCode);
				hierarchy.put(HierarchyConstants.HIER_NM, hierarchyName);
				hierarchy.put(HierarchyConstants.HIER_TP, hierarchyType);
				hierarchy.put(HierarchyConstants.HIER_DS, hierarchyDescription);
				// hierarchy.put(HierarchyConstants.HIER_SCOPE, hierarchyScope);
				hierarchiesJSONArray.put(hierarchy);

			}

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving custom hierarchies names");
			throw new SpagoBIServiceException("An unexpected error occured while retriving custom hierarchies names", t);
		}
		logger.debug("END");
		return hierarchiesJSONArray.toString();

		// return "{\"response\":\"customHierarchies\"}";

	}

	// get automatic hierarchy structure for tree visualization
	@GET
	@Path("/getHierarchyTree")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchyTree(@QueryParam("dimension") String dimension, @QueryParam("filterType") String hierarchyType,
			@QueryParam("filterHierarchy") String hierarchyName, @QueryParam("validityDate") String hierarchyDate,
			@QueryParam("filterDimension") String filterDimension, @QueryParam("optionDate") String optionDate,
			@QueryParam("optionHierarchy") String optionHierarchy, @QueryParam("optionHierType") String optionHierType) {
		logger.debug("START");

		HierarchyTreeNode hierarchyTree;
		JSONObject treeJSONObject;
		try {
			// Check input parameters
			Assert.assertNotNull(dimension, "Request parameter dimension is null");
			Assert.assertNotNull(hierarchyType, "Request parameter hierarchyType is null");
			Assert.assertNotNull(hierarchyName, "Request parameter hierarchyName is null");
			Assert.assertNotNull(hierarchyDate, "Request parameter hierarchyDate is null");

			IDataSource dataSource = null;
			// 1 - get datasource label name
			try {
				dataSource = HierarchyUtils.getDataSource(dimension);
			} catch (SpagoBIServiceException se) {
				throw se;
			}

			// 2 - execute query to get hierarchies leafs
			IMetaData metadata = null;
			String queryText = this.createQueryHierarchy(dataSource, dimension, hierarchyType, hierarchyName, hierarchyDate, filterDimension, optionDate,
					optionHierarchy, optionHierType);
			IDataStore dataStore = dataSource.executeStatement(queryText, 0, 0);

			// 4 - Create ADT for Tree from datastore
			hierarchyTree = createHierarchyTreeStructure(dataStore, dimension, metadata);
			// put hiercode for the second hiername
			treeJSONObject = convertHierarchyTreeAsJSON(hierarchyTree, hierarchyName, hierarchyName, dimension);

			if (treeJSONObject == null)
				return null;

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving hierarchy structure");
			throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchy structure", t);
		}
		logger.debug("END");
		return treeJSONObject.toString();
	}

	@GET
	@Path("/hierarchyMetadata")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchyFields(@QueryParam("dimension") String dimensionName) {

		logger.debug("START");

		JSONObject result = new JSONObject();

		try {

			result = createHierarchyJSON(dimensionName, false);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", t);
		}

		logger.debug("END");
		return result.toString();
	}

	@GET
	@Path("/nodeMetadata")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchyNodeFields(@QueryParam("dimension") String dimensionName, @QueryParam("excludeLeaf") boolean excludeLeaf) {

		logger.debug("START");

		JSONObject result = new JSONObject();

		try {

			result = createHierarchyJSON(dimensionName, excludeLeaf);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", t);
		}

		logger.debug("END");
		return result.toString();
	}

	@POST
	@Path("/saveHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String saveHierarchy(@Context HttpServletRequest req) {
		// Save hierarchy structure
		Connection connection = null;
		try {

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			String hierarchyCode = requestVal.getString("code");
			String hierarchyName = requestVal.getString("name");
			String hierarchyDescription = requestVal.getString("description");
			String hierarchyType = requestVal.getString("type");
			String validityDate = (!requestVal.isNull("dateValidity")) ? requestVal.getString("dateValidity") : null;
			boolean doBackup = (!requestVal.isNull("doBackup")) ? requestVal.getBoolean("doBackup") : new Boolean("false");
			String dimension = requestVal.getString("dimension");

			String root = requestVal.getString("root");
			JSONObject rootJSONObject = ObjectUtils.toJSONObject(root);
			boolean isInsert = Boolean.valueOf(req.getParameter("isInsert"));

			Collection<List<HierarchyTreeNodeData>> paths = findRootToLeavesPaths(rootJSONObject, dimension);

			// Information for persistence
			// 1 - get hierarchy table postfix(ex: _CDC)
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyTable = hierarchies.getHierarchyTableName(dimension);
			String hierarchyPrefix = hierarchies.getPrefix(dimension);
			String hierarchyFK = hierarchies.getHierarchyTableForeignKeyName(dimension);
			Hierarchy hierarchyFields = hierarchies.getHierarchy(dimension);
			HashMap hierConfig = hierarchies.getConfig(dimension);

			// 2 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy", "No datasource found for saving hierarchy");
			}
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			if (!isInsert && doBackup) {
				updateHierarchyForBackup(dataSource, connection, hierarchyType, hierarchyName, validityDate, hierarchyTable);
			} else if (!isInsert && !doBackup) {
				deleteHierarchy(dimension, hierarchyName, dataSource, connection);
			}

			for (List<HierarchyTreeNodeData> path : paths) {
				persistCustomHierarchyPath(connection, hierarchyCode, hierarchyName, hierarchyDescription, hierarchyTable, hierarchyType, dataSource,
						hierarchyPrefix, hierarchyFK, path, isInsert, hierarchyFields, hierConfig);
			}
			connection.commit();
			return "{\"response\":\"ok\"}";
		} catch (Throwable t) {
			logger.error("An unexpected error occured while saving custom hierarchy structure");
			try {
				if (connection != null && !connection.isClosed()) {
					connection.rollback();
				}
			} catch (SQLException sqle) {
				throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy structure", sqle);
			}
			throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy structure", t);
		} finally {
			try {
				if (connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException sqle) {
				throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy structure", sqle);
			}
		}
	}

	@POST
	@Path("/deleteHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String deleteHierarchy(@QueryParam("dimension") String dimension, @QueryParam("name") String hierarchyName) throws SQLException {
		// delete hierarchy
		Connection connection = null;
		try {

			// 1 - get datasource label name
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);

			// 2 - Execute DELETE
			connection = dataSource.getConnection();
			deleteHierarchy(dimension, hierarchyName, dataSource, connection);

		} catch (Throwable t) {
			connection.rollback();
			logger.error("An unexpected error occured while deleting custom hierarchy");
			throw new SpagoBIServiceException("An unexpected error occured while deleting custom hierarchy", t);
		} finally {
			if (connection != null && !connection.isClosed())
				connection.close();
		}

		return "{\"response\":\"ok\"}";

	}

	@POST
	@Path("/modifyHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String modifyHierarchy(@Context HttpServletRequest req) throws SQLException {
		// modify an existing hierarchy (ONLY GENERAL section)
		Connection databaseConnection = null;
		try {
			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			// Information for persistence
			// 1 - get hierarchy table postfix(ex: _CDC)
			String dimension = requestVal.getString("dimension");
			String hierarchyNameNew = req.getParameter("HIER_NM");
			String hierarchyNameOrig = req.getParameter("HIER_NM_ORIG");

			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyTable = hierarchies.getHierarchyTableName(dimension);
			Hierarchy hierarchyFields = hierarchies.getHierarchy(dimension);
			List<Field> generalMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataGeneralFields());

			// 2 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy", "No datasource found for saving hierarchy");
			}

			// 3 - define update command
			LinkedHashMap<String, String> lstFields = new LinkedHashMap<String, String>();
			StringBuffer columnsBuffer = new StringBuffer(" ");
			// general fields:
			for (int i = 0, l = generalMetadataFields.size(); i < l; i++) {
				Field f = generalMetadataFields.get(i);
				String key = f.getId();
				String value = requestVal.getString(key);
				lstFields.put(key, value);
				if (key != null && value != null) {
					String sep = (i < l - 1) ? "= ?, " : "= ? ";
					String column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
					columnsBuffer.append(column + sep);
				}
			}
			String columns = columnsBuffer.toString();
			String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);

			databaseConnection = dataSource.getConnection();
			Statement stmt = databaseConnection.createStatement();

			if (!hierarchyNameNew.equalsIgnoreCase(hierarchyNameOrig)) {
				// if the name is changed check its univocity
				String selectQuery = "SELECT count(*) as num FROM " + hierarchyTable + " WHERE  HIER_NM = " + hierNameColumn + "= ?";

				PreparedStatement selectPs = databaseConnection.prepareStatement(selectQuery);
				selectPs.setString(1, hierNameColumn);
				ResultSet rs = selectPs.executeQuery();

				if (rs.next()) {
					String count = rs.getString("num");
					if (Integer.valueOf(count) > 0) {
						logger.error("A hierarchy with name " + hierarchyNameNew + "  already exists. Change name.");
						throw new SpagoBIServiceException("", "A hierarchy with name " + hierarchyNameNew + "  already exists. Change name.");
					}
				}

			}
			String updateQuery = "UPDATE " + hierarchyTable + " SET " + columns + " WHERE " + hierNameColumn + "= ?";
			logger.debug("The update query is [" + updateQuery + "]");

			PreparedStatement updatePs = databaseConnection.prepareStatement(updateQuery);

			// begin transaction
			databaseConnection.setAutoCommit(false);

			logger.debug("Auto-commit false. Begin transaction!");

			int pos = 1;
			for (String key : lstFields.keySet()) {
				String value = lstFields.get(key);
				updatePs.setObject(pos, value);
			}

			updatePs.executeUpdate();
			logger.debug("Update query executed!");

			logger.debug("Executing commit. End transaction!");
			databaseConnection.commit();

		} catch (Throwable t) {
			if (databaseConnection != null && !databaseConnection.isClosed()) {
				databaseConnection.rollback();
			}
			logger.error("An unexpected error occured while modifing custom hierarchy");
			throw new SpagoBIServiceException("An unexpected error occured while modifing custom hierarchy", t);
		} finally {
			try {
				if (databaseConnection != null && !databaseConnection.isClosed()) {
					databaseConnection.close();
				}
			} catch (SQLException sqle) {
				throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy structure", sqle);
			}
		}
		return "{\"response\":\"ok\"}";
	}

	@POST
	@Path("/restoreHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String restoreHierarchy(@QueryParam("name") String hierarchyBkpName, @QueryParam("dimension") String dimension) throws SQLException {
		// restores a backup hierarchy
		try {

			logger.debug("START");

			if ((dimension == null) || (hierarchyBkpName == null)) {
				throw new SpagoBIServiceException("An unexpected error occured while restoring a backup hierarchy", "wrong request parameters");
			}

			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyTable = hierarchies.getHierarchyTableName(dimension);

			IDataSource dataSource = HierarchyUtils.getDataSource(dimension);

			restoreBackupHierarchy(dataSource, hierarchyBkpName, hierarchyTable);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while restoring a backup hierarchy");
			throw new SpagoBIServiceException("An unexpected error occured while restoring a backup hierarchy", t);
		}

		logger.debug("END");
		return "{\"response\":\"ok\"}";

	}

	@GET
	@Path("/getHierarchyBkps")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchyBkps(@QueryParam("dimension") String dimension, @QueryParam("hierarchyName") String hierarchyName,
			@QueryParam("hierarchyType") String hierarchyType) throws SQLException {

		logger.debug("START");

		JSONObject result = new JSONObject();

		try {

			if ((dimension == null)) {
				throw new SpagoBIServiceException("An unexpected error occured while retrieving hierarchy backups", "wrong request parameters");
			}

			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyTable = hierarchies.getHierarchyTableName(dimension);

			IDataSource dataSource = HierarchyUtils.getDataSource(dimension);

			Hierarchy hierarchy = hierarchies.getHierarchy(dimension);
			List<Field> genFields = hierarchy.getMetadataGeneralFields();

			List<Field> bkpFields = HierarchyUtils.createBkpFields(genFields, HierarchyConstants.BKP_GEN_FIELDS);

			String queryText = selectHierarchyBkps(dataSource, hierarchyTable, hierarchyName, hierarchyType, bkpFields);

			IDataStore dataStore = dataSource.executeStatement(queryText, 0, 0);

			// 3 - Create JSON result
			JSONArray rootArray = HierarchyUtils.createRootData(dataStore);
			JSONArray columnsArray = HierarchyUtils.createJSONArrayFromFieldsList(bkpFields, true);
			JSONArray columnsSearchArray = HierarchyUtils.createColumnsSearch(bkpFields);

			if (rootArray == null || columnsArray == null || columnsSearchArray == null) {
				return null;
			}

			logger.debug("Root array is [" + rootArray.toString() + "]");
			result.put(HierarchyConstants.ROOT, rootArray);

			logger.debug("Columns array is [" + columnsArray.toString() + "]");
			result.put(HierarchyConstants.COLUMNS, columnsArray);

			logger.debug("Columns Search array is [" + columnsSearchArray.toString() + "]");
			result.put(HierarchyConstants.COLUMNS_SEARCH, columnsSearchArray);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retrieving hierarchy backups");
			throw new SpagoBIServiceException("An unexpected error occured while retrieving hierarchy backups", t);
		}

		logger.debug("JSON for hierarchy backups is [" + result.toString() + "]");
		logger.debug("END");
		return result.toString();

	}

	@POST
	@Path("/createHierarchyMaster")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String createHierarchyMaster(@Context HttpServletRequest req, @QueryParam("dimension") String dimensionLabel,
			@QueryParam("validityDate") String validityDate, @QueryParam("filterDate") String filterDate,
			@QueryParam("filterHierarchy") String filterHierarchy, @QueryParam("filterHierType") String filterHierType) throws SQLException {

		logger.debug("START");

		if ((dimensionLabel == null) || (validityDate == null)) {
			throw new SpagoBIServiceException("An unexpected error occured while creating hierarchy master", "wrong request parameters");
		}

		IDataSource dataSource = HierarchyUtils.getDataSource(dimensionLabel);

		if (dataSource == null) {
			throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
		}

		try (Connection dbConnection = dataSource.getConnection()) {

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

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

			// String dimensionLabel = req.getParameter("dimension");
			// String validityDate = req.getParameter("validityDate");

			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			Assert.assertNotNull(hierarchies, "Impossible to find a valid hierarchies object");

			Dimension dimension = hierarchies.getDimension(dimensionLabel);
			Assert.assertNotNull(dimension, "Impossible to find a valid dimension with label [" + dimensionLabel + "]");

			Hierarchy hierarchy = hierarchies.getHierarchy(dimensionLabel);
			Assert.assertNotNull(hierarchy, "Impossible to find a valid hierarchy with label [" + dimensionLabel + "]");

			String hierTableName = hierarchies.getHierarchyTableName(dimensionLabel);
			String prefix = hierarchies.getPrefix(dimensionLabel);

			List<Field> metadataFields = new ArrayList<Field>(dimension.getMetadataFields());
			Map<String, Integer> metatadaFieldsMap = HierarchyUtils.getMetadataFieldsMap(metadataFields);

			List<Field> generalFields = new ArrayList<Field>(hierarchy.getMetadataGeneralFields());

			IDataStore dataStore = HierarchyUtils.getDimensionDataStore(dataSource, dimension, dimensionLabel, metadataFields, validityDate, filterDate,
					filterHierarchy, filterHierType, hierTableName, prefix);

			dbConnection.setAutoCommit(false);

			Iterator iterator = dataStore.iterator();

			while (iterator.hasNext()) {

				IRecord record = (IRecord) iterator.next();
				insertHierarchyMaster(dbConnection, dataSource, record, dataStore, hierTableName, generalFields, metadataFields, metatadaFieldsMap, requestVal,
						prefix);

			}

			dbConnection.commit();

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving dimension data");
			throw new SpagoBIServiceException("An unexpected error occured while retriving dimension data", t);
		}

		logger.debug("END");
		return "{\"response\":\"ok\"}";

	}

	private boolean deleteHierarchy(String dimension, String hierarchyName, IDataSource dataSource, Connection connection) throws SQLException {
		// delete hierarchy

		logger.debug("START");

		try {

			// String dimension = requestVal.getString("dimension");
			// String hierarchyName = requestVal.getString("name");

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
	 * This method manages the creation of the JSON for hierarchies fields
	 *
	 * @param dimensionName
	 *            the name of the dimension
	 * @param excludeLeaf
	 *            exclusion for fields in leaf section
	 * @return the JSON with fields in hierarchy section
	 * @throws JSONException
	 */
	private JSONObject createHierarchyJSON(String dimensionName, boolean excludeLeaf) throws JSONException {

		logger.debug("START");

		JSONObject result = new JSONObject();

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();
		Assert.assertNotNull(hierarchies, "Impossible to find valid hierarchies config");

		Hierarchy hierarchy = hierarchies.getHierarchy(dimensionName);
		Assert.assertNotNull(hierarchy, "Impossible to find a hierarchy for the dimension called [" + dimensionName + "]");

		JSONObject configs = HierarchyUtils.createJSONArrayFromHashMap(hierarchies.getConfig(dimensionName), null);
		result.put(HierarchyConstants.CONFIGS, configs);

		List<Field> generalMetadataFields = new ArrayList<Field>(hierarchy.getMetadataGeneralFields());
		JSONArray generalFieldsJSONArray = HierarchyUtils.createJSONArrayFromFieldsList(generalMetadataFields, true);
		result.put(HierarchyConstants.GENERAL_FIELDS, generalFieldsJSONArray);

		List<Field> nodeMetadataFields = new ArrayList<Field>(hierarchy.getMetadataNodeFields());

		JSONArray nodeFieldsJSONArray = HierarchyUtils.createJSONArrayFromFieldsList(nodeMetadataFields, true);
		result.put(HierarchyConstants.NODE_FIELDS, nodeFieldsJSONArray);

		if (!excludeLeaf) { // add leaf fields
			List<Field> leafMetadataFields = new ArrayList<Field>(hierarchy.getMetadataLeafFields());

			JSONArray leafFieldsJSONArray = HierarchyUtils.createJSONArrayFromFieldsList(leafMetadataFields, true);
			result.put(HierarchyConstants.LEAF_FIELDS, leafFieldsJSONArray);
		}

		logger.debug("END");
		return result;

	}

	/**
	 * Create query for extracting automatic hierarchy rows
	 */
	private String createQueryHierarchy(IDataSource dataSource, String dimension, String hierarchyType, String hierarchyName, String hierarchyDate,
			String filterDimension, String optionDate, String optionHierarchy, String optionHierType) {

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();

		// 1 -get hierarchy informations
		String hierarchyTable = hierarchies.getHierarchyTableName(dimension);
		String hierarchyFK = hierarchies.getHierarchyTableForeignKeyName(dimension);
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
		int totalNodeFields = getTotalNodeFieldsNumber(totalLevels, nodeMetadataFields);

		totalColumns = totalGeneralFields + totalLeafFields + totalNodeFields;

		// 3 - define select command
		StringBuffer selectClauseBuffer = new StringBuffer(" ");
		// general fields:
		for (int i = 0, l = generalMetadataFields.size(); i < l; i++) {
			Field f = generalMetadataFields.get(i);
			String sep = ", ";
			String column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
			selectClauseBuffer.append(column + sep);
		}
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
		}
		// leaf fields:
		for (int i = 0, l = leafMetadataFields.size(); i < l; i++) {
			Field f = leafMetadataFields.get(i);
			String sep = ",";
			String column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
			selectClauseBuffer.append(column + sep);
		}
		// add leafId (last field)
		String leafId = hierarchies.getHierarchyTableForeignKeyName(dimension);
		String column = AbstractJDBCDataset.encapsulateColumnName(leafId, dataSource);
		selectClauseBuffer.append(column);

		String selectClause = selectClauseBuffer.toString();

		// where
		String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
		String hierTypeColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_TP, dataSource);
		String hierDateBeginColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BEGIN_DT, dataSource);
		String hierDateEndColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.END_DT, dataSource);
		String vDateConverted = HierarchyUtils.getConvertedDate(hierarchyDate, dataSource);

		String vDateWhereClause = vDateConverted + " >= " + hierDateBeginColumn + " AND " + vDateConverted + " <= " + hierDateEndColumn;

		StringBuffer query = new StringBuffer("SELECT " + selectClause + " FROM " + hierarchyTable + " WHERE " + hierNameColumn + " = \"" + hierarchyName
				+ "\" AND " + hierTypeColumn + " = \"" + hierarchyType + "\" AND " + vDateWhereClause);

		if (filterDimension != null) {
			logger.debug("Filter dimension is [" + filterDimension + "]");

			String dimFilterField = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEAF", dataSource);
			String selectFilterField = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD", dataSource);

			query.append(" AND " + dimFilterField + " NOT IN (SELECT " + selectFilterField + "FROM " + dimensionName);
			query.append(" WHERE " + vDateConverted + " >= " + hierDateBeginColumn + " AND " + vDateConverted + " <= " + hierDateEndColumn + ")");
		}

		if (optionDate != null) {
			logger.debug("Filter date is [" + optionDate + "]");

			query.append(HierarchyUtils.createDateAfterCondition(dataSource, optionDate, hierDateBeginColumn));
		}

		if (optionHierarchy != null) {
			logger.debug("Filter Hierarchy is [" + optionHierarchy + "]");

			String dimFilterField = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEAF", dataSource);

			query.append(HierarchyUtils.createNotInHierarchyCondition(dataSource, hierarchyTable, hierNameColumn, optionHierarchy, hierTypeColumn,
					optionHierType, dimFilterField, dimFilterField, vDateWhereClause));
		}

		logger.debug("Query for get hierarchies: " + query);
		return query.toString();
	}

	private int getTotalNodeFieldsNumber(int totalLevels, List<Field> nodeMetadataFields) {
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
	 * Create HierarchyTreeNode tree from datastore with leafs informations
	 */
	private HierarchyTreeNode createHierarchyTreeStructure(IDataStore dataStore, String dimension, IMetaData metadata) {

		HierarchyTreeNode root = null;
		metadata = dataStore.getMetaData(); // saving metadata for next using

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();
		String prefix = hierarchies.getPrefix(dimension);
		HashMap hierConfig = hierarchies.getConfig(dimension);
		int numLevels = Integer.parseInt((String) hierConfig.get(HierarchyConstants.NUM_LEVELS));
		// contains the code of the last level node (not null) inserted in the tree
		IMetaData dsMeta = dataStore.getMetaData();
		for (Iterator iterator = dataStore.iterator(); iterator.hasNext();) {
			String lastLevelFound = null;

			IRecord record = (IRecord) iterator.next();
			List<IField> recordFields = record.getFields();
			int fieldsCount = recordFields.size();

			// MAX_DEPTH, must be equal to the level of the leaf (that we skip)
			IField maxDepthField = record.getFieldAt(dsMeta.getFieldIndex(HierarchyConstants.MAX_DEPTH));
			int maxDepth = 0;
			if (maxDepthField.getValue() instanceof Integer) {
				Integer maxDepthValue = (Integer) maxDepthField.getValue();
				maxDepth = maxDepthValue;
			} else if (maxDepthField.getValue() instanceof Long) {
				Long maxDepthValue = (Long) maxDepthField.getValue();
				maxDepth = (int) (long) maxDepthValue;
			}

			int currentLevel = 0;

			for (int i = 1, l = numLevels; i <= l; i++) {
				IField codeField = record.getFieldAt(dsMeta.getFieldIndex(prefix + HierarchyConstants.SUFFIX_CD_LEV + i)); // NODE CODE
				IField nameField = record.getFieldAt(dsMeta.getFieldIndex(prefix + HierarchyConstants.SUFFIX_NM_LEV + i)); // NAME CODE

				if ((currentLevel == maxDepth) || (codeField.getValue() == null) || (codeField.getValue().equals(""))) {
					break; // skip to next iteration
				} else {
					String nodeCode = (String) codeField.getValue();
					String nodeName = (String) nameField.getValue();
					HierarchyTreeNodeData data = new HierarchyTreeNodeData(nodeCode, nodeName);
					// update LEVEL information
					HashMap mapAttrs = data.getAttributes();
					mapAttrs.put(HierarchyConstants.LEVEL, i);
					data.setAttributes(mapAttrs);

					// check if its a leaf
					IField codeLeafField = record.getFieldAt(dsMeta.getFieldIndex(prefix + "_CD_LEAF")); // LEAF CODE
					String leafCode = (String) codeLeafField.getValue();
					if (leafCode.equals(nodeCode)) {
						data = setDataValues(dimension, nodeCode, data, record, metadata);
						// update LEVEL information
						mapAttrs = data.getAttributes();
						mapAttrs.put(HierarchyConstants.LEVEL, i);
						data.setAttributes(mapAttrs);
						attachNodeToLevel(root, nodeCode, lastLevelFound, data, null);
						lastLevelFound = nodeCode;
						break;
					} else if (root == null || (!root.getKey().contains(nodeCode) && !root.getChildrensKeys().contains(nodeCode))) {
						// get nodes attribute for automatic edit node GUI
						ArrayList<Field> nodeFields = hierarchies.getHierarchy(dimension).getMetadataNodeFields();
						for (int f = 0, lf = nodeFields.size(); f < lf; f++) {
							Field fld = nodeFields.get(f);
							if (fld.isVisible() || fld.isEditable()) {
								IField fldValue = record.getFieldAt(metadata.getFieldIndex(fld.getId() + ((fld.isSingleValue()) ? "" : i)));
								mapAttrs.put(fld.getId(), fldValue.getValue());
							}
						}
						data.setAttributes(mapAttrs);
						if (root == null) {
							// get root attribute for automatic edit node GUI
							HashMap rootAttrs = new HashMap();
							ArrayList<Field> generalFields = hierarchies.getHierarchy(dimension).getMetadataGeneralFields();
							for (int f = 0, lf = generalFields.size(); f < lf; f++) {
								Field fld = generalFields.get(f);
								// if (fld.isVisible() || fld.isEditable()) {
								IField fldValue = record.getFieldAt(metadata.getFieldIndex(fld.getId() + ((fld.isSingleValue()) ? "" : i)));
								rootAttrs.put(fld.getId(), fldValue.getValue());
								// }
							}
							root = new HierarchyTreeNode(data, nodeCode, rootAttrs);
						} else {
							attachNodeToLevel(root, nodeCode, lastLevelFound, data, null);
						}
					}
					lastLevelFound = nodeCode;
				}
				currentLevel++;
			}

		}

		if (root != null)
			logger.debug(TreeString.toString(root));

		return root;

	}

	/**
	 * Attach a node as a child of another node (with key lastLevelFound)
	 */
	// TODO: remove allNodeCodes from signature
	private void attachNodeToLevel(HierarchyTreeNode root, String nodeCode, String lastLevelFound, HierarchyTreeNodeData data, String dimension) {
		HierarchyTreeNode treeNode = null;
		// first search parent node
		for (Iterator<HierarchyTreeNode> treeIterator = root.iterator(); treeIterator.hasNext();) {
			treeNode = treeIterator.next();
			if (treeNode.getKey().equals(lastLevelFound)) {
				// parent node found
				break;
			}
		}
		// then check if node was already added as a child of this parent

		if (!treeNode.getChildrensKeys().contains(nodeCode)) {
			// node not already attached to the level
			HierarchyTreeNode aNode = new HierarchyTreeNode(data, nodeCode);

			treeNode.add(aNode, nodeCode);

		}
	}

	/**
	 * Sets records' value to the tree structure (leaf informations, date and strings)
	 */
	private HierarchyTreeNodeData setDataValues(String dimension, String nodeCode, HierarchyTreeNodeData data, IRecord record, IMetaData metadata) {
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
		data.setLeafOriginalParentCode(leafParentCodeString); // backup code

		IField leafParentNameField = record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.LEAF_PARENT_NM));
		String leafParentNameString = (String) leafParentNameField.getValue();
		data.setLeafParentName(leafParentNameString);

		IField beginDtField = record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.BEGIN_DT));
		Date beginDtDate = (Date) beginDtField.getValue();
		data.setBeginDt(beginDtDate);

		IField endDtField = record.getFieldAt(metadata.getFieldIndex(HierarchyConstants.END_DT));
		Date endDtDate = (Date) endDtField.getValue();
		data.setEndDt(endDtDate);

		// add field attributes for automatic edit field GUI
		HashMap mapAttrs = new HashMap();
		int numLevels = Integer.valueOf((String) hierarchies.getConfig(dimension).get(HierarchyConstants.NUM_LEVELS));
		ArrayList<Field> leafFields = hierarchies.getHierarchy(dimension).getMetadataLeafFields();
		for (int f = 0, lf = leafFields.size(); f < lf; f++) {
			Field fld = leafFields.get(f);
			if (fld.isVisible() || fld.isEditable()) {
				String idFld = fld.getId();
				if (!fld.isSingleValue()) {
					for (int idx = 1; idx <= numLevels; idx++) {
						IField fldValue = record.getFieldAt(metadata.getFieldIndex(idFld + idx));
						mapAttrs.put(idFld + idx, fldValue.getValue());
					}
				} else {
					IField fldValue = record.getFieldAt(metadata.getFieldIndex(idFld));
					mapAttrs.put(idFld, fldValue.getValue());
				}

			}
		}
		data.setAttributes(mapAttrs);

		return data;
	}

	private JSONObject convertHierarchyTreeAsJSON(HierarchyTreeNode root, String hierName, String hierCode, String dimension) {
		JSONObject rootJSONObject = new JSONObject();

		if (root == null)
			return null;

		try {
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			HashMap hierConfig = hierarchies.getConfig(dimension);

			HierarchyTreeNodeData rootData = (HierarchyTreeNodeData) root.getObject();
			rootJSONObject.put(HierarchyConstants.TREE_NAME, rootData.getNodeName());
			rootJSONObject.put(HierarchyConstants.ID, rootData.getNodeCode());
			rootJSONObject.put("leaf", false);
			rootJSONObject.put(HierarchyConstants.LEVEL, 1);
			rootJSONObject.put("aliasId", hierConfig.get(HierarchyConstants.TREE_NODE_ID));
			rootJSONObject.put("aliasName", hierConfig.get(HierarchyConstants.TREE_NODE_NAME));
			HashMap mapAttrs = rootData.getAttributes();
			HierarchyUtils.createJSONArrayFromHashMap(mapAttrs, rootJSONObject);

			JSONArray childrenJSONArray = new JSONArray();

			for (int i = 0; i < root.getChildCount(); i++) {
				HierarchyTreeNode childNode = root.getChild(i);
				JSONObject subTreeJSONObject = getSubTreeJSONObject(childNode, hierConfig);
				childrenJSONArray.put(subTreeJSONObject);
			}

			rootJSONObject.put("children", childrenJSONArray);
			// fake root
			JSONObject mainObject = new JSONObject();
			// transform the root's children JSON object in array like all others children
			JSONArray arRootJSONObject = new JSONArray();
			arRootJSONObject.put(rootJSONObject);
			mainObject.put(HierarchyConstants.TREE_NAME, hierName);
			mainObject.put(HierarchyConstants.ID, "root");
			mainObject.put("aliasId", HierarchyConstants.HIER_CD);
			mainObject.put("aliasName", HierarchyConstants.HIER_NM);
			// mainObject.put(HierarchyConstants.HIER_NM, hierName);
			// mainObject.put(HierarchyConstants.HIER_CD, hierCode);
			mainObject.put("root", true);
			mainObject.put("children", arRootJSONObject);
			mainObject.put("leaf", false);
			HashMap rootAttrs = root.getAttributes();
			HierarchyUtils.createJSONArrayFromHashMap(rootAttrs, mainObject);

			return mainObject;

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchy structure", t);
		}

	}

	/**
	 * get the JSONObject representing the tree having the passed node as a root
	 *
	 * @param node
	 *            the root of the subtree
	 * @return JSONObject representing the subtree
	 */
	private JSONObject getSubTreeJSONObject(HierarchyTreeNode node, HashMap hierConfig) {

		try {
			if (node.getChildCount() > 0) {
				// it's a node
				JSONObject nodeJSONObject = new JSONObject();
				HierarchyTreeNodeData nodeData = (HierarchyTreeNodeData) node.getObject();
				nodeJSONObject.put(HierarchyConstants.TREE_NAME, nodeData.getNodeName());
				nodeJSONObject.put(HierarchyConstants.ID, nodeData.getNodeCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_ID, nodeData.getLeafId());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_CD, nodeData.getLeafParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_ORIG_PARENT_CD, nodeData.getLeafOriginalParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_NM, nodeData.getLeafParentName());
				nodeJSONObject.put("aliasId", hierConfig.get(HierarchyConstants.TREE_NODE_ID));
				nodeJSONObject.put("aliasName", hierConfig.get(HierarchyConstants.TREE_NODE_NAME));

				JSONArray childrenJSONArray = new JSONArray();

				for (int i = 0; i < node.getChildCount(); i++) {
					HierarchyTreeNode childNode = node.getChild(i);
					JSONObject subTree = getSubTreeJSONObject(childNode, hierConfig);
					childrenJSONArray.put(subTree);
				}
				nodeJSONObject.put("children", childrenJSONArray);
				nodeJSONObject.put("leaf", false);

				nodeJSONObject = setDetailsInfo(nodeJSONObject, nodeData);
				return nodeJSONObject;

			} else {
				// it's a leaf
				HierarchyTreeNodeData nodeData = (HierarchyTreeNodeData) node.getObject();
				JSONObject nodeJSONObject = new JSONObject();

				nodeJSONObject.put(HierarchyConstants.TREE_NAME, nodeData.getNodeName());
				nodeJSONObject.put(HierarchyConstants.ID, nodeData.getNodeCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_ID, nodeData.getLeafId());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_CD, nodeData.getLeafParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_ORIG_PARENT_CD, nodeData.getLeafOriginalParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_NM, nodeData.getLeafParentName());
				nodeJSONObject.put("aliasId", hierConfig.get(HierarchyConstants.TREE_LEAF_ID));
				nodeJSONObject.put("aliasName", hierConfig.get(HierarchyConstants.TREE_LEAF_NAME));
				nodeJSONObject.put("leaf", true);

				nodeJSONObject = setDetailsInfo(nodeJSONObject, nodeData);
				return nodeJSONObject;

			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while serializing hierarchy structure to JSON", t);
		}

	}

	private JSONObject setDetailsInfo(JSONObject nodeJSONObject, HierarchyTreeNodeData nodeData) {
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

	/**
	 * Persist custom hierarchy paths to database
	 */
	private void persistCustomHierarchyPath(Connection connection, String hierarchyCode, String hierarchyName, String hierarchyDescription,
			String hierarchyTable, String hierarchyType, IDataSource dataSource, String hierarchyPrefix, String hierarchyFK, List<HierarchyTreeNodeData> path,
			boolean isInsert, Hierarchy hierarchyFields, HashMap hierConfig) throws SQLException {
		try {
			// 1 - get fields structure
			List<Field> generalMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataGeneralFields());
			List<Field> nodeMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataNodeFields());
			List<Field> leafMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataLeafFields());

			// 2 - get total columns number
			int totalColumns = 0;
			int totalLevels = Integer.parseInt((String) hierConfig.get(HierarchyConstants.NUM_LEVELS));
			int totalGeneralFields = generalMetadataFields.size();
			int totalLeafFields = leafMetadataFields.size();
			int totalNodeFields = getTotalNodeFieldsNumber(totalLevels, nodeMetadataFields);

			totalColumns = totalGeneralFields + totalLeafFields + totalNodeFields;
			int numLevels = Integer.valueOf((String) hierConfig.get(HierarchyConstants.NUM_LEVELS));

			// 3 - Insert prepared statement construction
			// ------------------------------------------
			StringBuffer sbColumns = new StringBuffer();
			LinkedHashMap<String, String> lstFields = new LinkedHashMap<String, String>();

			for (int i = 0, l = generalMetadataFields.size(); i < l; i++) {
				String column = "";
				Field f = generalMetadataFields.get(i);
				String sep = ", ";
				if (!f.isSingleValue()) {
					for (int idx = 1; idx <= numLevels; idx++) {
						String nameF = f.getId() + idx;
						column = AbstractJDBCDataset.encapsulateColumnName(nameF, dataSource);
						sbColumns.append(column + sep);
						lstFields.put(nameF, f.getType());
					}
				} else {
					lstFields.put(f.getId(), f.getType());
					column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
					sbColumns.append(column + sep);
				}
			}

			for (int i = 0, l = nodeMetadataFields.size(); i < l; i++) {
				String column = "";
				Field f = nodeMetadataFields.get(i);
				String sep = ", ";
				if (!f.isSingleValue()) {
					for (int idx = 1; idx <= numLevels; idx++) {
						String nameF = f.getId() + idx;
						column = AbstractJDBCDataset.encapsulateColumnName(nameF, dataSource);
						sbColumns.append(column + sep);
						lstFields.put(nameF, f.getType());
					}
				} else {
					lstFields.put(f.getId(), f.getType());
					column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
					sbColumns.append(column + sep);
				}
			}

			for (int i = 0, l = leafMetadataFields.size(); i < l; i++) {
				String column = "";
				Field f = leafMetadataFields.get(i);
				String sep = (i == l - 1) ? "" : ", ";
				if (!f.isSingleValue()) {
					for (int idx = 1; idx <= numLevels; idx++) {
						String nameF = f.getId() + idx;
						column = AbstractJDBCDataset.encapsulateColumnName(nameF, dataSource);
						sbColumns.append(column + sep);
						lstFields.put(nameF, f.getType());
					}
				} else {
					lstFields.put(f.getId(), f.getType());
					column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
					sbColumns.append(column + sep);
				}
			}

			String columns = sbColumns.toString();

			String insertQuery = "insert into " + hierarchyTable + "(" + columns + ") values (";
			for (int c = 0, lc = totalColumns; c < lc; c++) {
				insertQuery += "?" + ((c < lc - 1) ? ", " : " ");
			}
			insertQuery += ")";
			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

			// 4 - Valorization of prepared statement placeholder
			// -----------------------------------------------
			// set NULL as default for all values
			for (int i = 1; i <= lstFields.size(); i++) {
				preparedStatement.setObject(i, null);
			}
			// sets mandatory fields (HIER_CD, HIER_NM, HIER_DS, HIER_TP,..)
			preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.HIER_CD), hierarchyCode);
			preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.HIER_NM), hierarchyName);
			preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.HIER_DS), hierarchyDescription);
			preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.HIER_TP), hierarchyType);

			// explore the path and set the corresponding columns
			// keeps the column number
			for (int i = 0; i < path.size(); i++) {
				HierarchyTreeNodeData node = path.get(i);
				int pos = 1;
				// skip the root node
				if (node.getNodeCode().equals(HierarchyConstants.ROOT))
					continue;

				if (node.getLeafId() != null && !node.getLeafId().equals("")) {
					// it's a leaf
					preparedStatement.setString(getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV + node.getDepth()),
							node.getNodeCode());
					preparedStatement.setString(getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEV + node.getDepth()),
							node.getNodeName());
					preparedStatement.setObject(getPosField(lstFields, hierarchyPrefix + "_" + HierarchyConstants.LEAF_ID), node.getLeafId());
					preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.LEAF_PARENT_CD), node.getLeafParentCode());
					// preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.LEAF_ORIG_PARENT_CD), node.getLeafOriginalParentCode());
					preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.LEAF_PARENT_NM), node.getLeafParentName());
					preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.MAX_DEPTH), node.getDepth());
					preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.BEGIN_DT), node.getBeginDt());
					preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.END_DT), node.getEndDt());

					// get other leaf's attributes (not mandatory)
					Iterator iter = node.getAttributes().keySet().iterator();
					while (iter.hasNext()) {
						String key = (String) iter.next();
						Object value = node.getAttributes().get(key);
						if (key != null && value != null) {
							int attrPos = getPosField(lstFields, key);
							if (attrPos != -1)
								preparedStatement.setObject(attrPos, value);
						}
					}
				} else {
					// not-leaf node
					int level = 0;
					// get other node's attributes (not mandatory ie sign)
					Iterator iter = node.getAttributes().keySet().iterator();
					while (iter.hasNext()) {
						String key = (String) iter.next();
						Object value = node.getAttributes().get(key);
						if (key.equalsIgnoreCase(HierarchyConstants.LEVEL))
							level = Integer.valueOf((String) value);
						if (key != null && value != null) {
							int attrPos = getPosField(lstFields, key);
							if (attrPos == -1)
								attrPos = getPosField(lstFields, key + level);
							if (attrPos != -1)
								preparedStatement.setObject(attrPos, value);
						}
					}
					if (level > 0) {
						preparedStatement.setString(getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV + level), node.getNodeCode());
						preparedStatement.setString(getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEV + level), node.getNodeName());
					}
				}
				pos++;
			}

			// 5 - Execution of prepared statement
			// ----------------------------------------
			preparedStatement.executeUpdate();
			preparedStatement.close();

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while persisting hierarchy structure", t);
		}
	}

	/**
	 *
	 * @param lstFields
	 * @param name
	 * @return the position of the field with the input name in order to the stmt
	 */
	private int getPosField(LinkedHashMap<String, String> lstFields, String name) {
		int toReturn = 1;

		for (String key : lstFields.keySet()) {
			if (key.equalsIgnoreCase(name))
				return toReturn;

			toReturn++;
		}
		logger.debug("Attribute '" + name + "' non found in fields' list ");
		return -1;
	}

	/**
	 * Find all paths from root to leaves
	 */
	private Collection<List<HierarchyTreeNodeData>> findRootToLeavesPaths(JSONObject node, String dimension) {
		Collection<List<HierarchyTreeNodeData>> collectionOfPaths = new HashSet<List<HierarchyTreeNodeData>>();
		try {
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();

			String nodeName = node.getString(HierarchyConstants.TREE_NAME);
			String nodeCode = node.getString(HierarchyConstants.ID);

			String nodeLeafId = !node.isNull(HierarchyConstants.LEAF_ID) ? node.getString(HierarchyConstants.LEAF_ID) : null;

			HashMap mapAttrs = new HashMap();
			int numLevels = Integer.valueOf((String) hierarchies.getConfig(dimension).get(HierarchyConstants.NUM_LEVELS));
			// add other leaf attributes if they are valorized
			ArrayList<Field> leafFields = hierarchies.getHierarchy(dimension).getMetadataLeafFields();
			for (int f = 0, lf = leafFields.size(); f < lf; f++) {
				Field fld = leafFields.get(f);
				// if (fld.isVisible() || fld.isEditable()) {
				String idFld = fld.getId();
				// if (!fld.isSingleValue()) {
				// for (int idx = 1; idx <= numLevels; idx++) {
				// if (!node.isNull(idFld + idx)) {
				// mapAttrs.put(idFld + idx, node.getString(idFld + idx));
				// }
				// }
				// } else if (!node.isNull(idFld)) {
				// mapAttrs.put(idFld, node.getString(idFld));
				// }
				if (!node.isNull(idFld)) {
					mapAttrs.put(idFld, node.getString(idFld));
				}
				// }
			}
			if (!node.isNull(HierarchyConstants.LEVEL)) {
				mapAttrs.put(HierarchyConstants.LEVEL, node.getString(HierarchyConstants.LEVEL));
			}

			// add other node attributes if they are valorized
			ArrayList<Field> nodeFields = hierarchies.getHierarchy(dimension).getMetadataNodeFields();
			for (int f = 0, lf = nodeFields.size(); f < lf; f++) {
				Field fld = nodeFields.get(f);
				// if (fld.isVisible() || fld.isEditable()) {
				String idFld = fld.getId();
				// if (!fld.isSingleValue()) {
				// for (int idx = 1; idx <= numLevels; idx++) {
				// if (!node.isNull(idFld + idx)) {
				// mapAttrs.put(idFld + idx, node.getString(idFld + idx));
				// }
				// }
				// } else if (!node.isNull(idFld)) {
				// mapAttrs.put(idFld, node.getString(idFld));
				// }
				if (!node.isNull(idFld)) {
					mapAttrs.put(idFld, node.getString(idFld));
				}
				// }
			}
			HierarchyTreeNodeData nodeData = new HierarchyTreeNodeData(nodeCode, nodeName, nodeLeafId, "", "", "", mapAttrs);

			// current node is a leaf?
			boolean isLeaf = node.getBoolean("leaf");
			if (isLeaf) {
				List<HierarchyTreeNodeData> aPath = new ArrayList<HierarchyTreeNodeData>();
				String nodeParentCode = node.getString(HierarchyConstants.LEAF_PARENT_CD);
				String nodeOriginalParentCode = node.getString(HierarchyConstants.LEAF_ORIG_PARENT_CD);
				nodeData.setNodeCode(nodeCode.replaceFirst(nodeOriginalParentCode + "_", ""));
				nodeData.setLeafParentCode(nodeParentCode);
				nodeData.setLeafParentName(node.getString(HierarchyConstants.LEAF_PARENT_NM));
				nodeData.setLeafOriginalParentCode(nodeOriginalParentCode);
				nodeData.setDepth(node.getString(HierarchyConstants.LEVEL));
				nodeData.setBeginDt(Date.valueOf(node.getString(HierarchyConstants.BEGIN_DT)));
				nodeData.setEndDt(Date.valueOf(node.getString(HierarchyConstants.END_DT)));
				aPath.add(nodeData);
				collectionOfPaths.add(aPath);
				return collectionOfPaths;
			} else {
				// node has children
				JSONArray childs = node.getJSONArray("children");
				for (int i = 0; i < childs.length(); i++) {
					JSONObject child = childs.getJSONObject(i);
					Collection<List<HierarchyTreeNodeData>> childPaths = findRootToLeavesPaths(child, dimension);
					for (List<HierarchyTreeNodeData> path : childPaths) {
						// add this node to start of the path
						path.add(0, nodeData);
						collectionOfPaths.add(path);
					}
				}
			}
			return collectionOfPaths;
		} catch (JSONException je) {
			logger.error("An unexpected error occured while retriving custom hierarchy root-leafs paths");
			throw new SpagoBIServiceException("An unexpected error occured while retriving custom hierarchy root-leafs paths", je);
		}

	}

	private void updateHierarchyForBackup(IDataSource dataSource, Connection databaseConnection, String hierarchyType, String hierarchyName,
			String validityDate, String hierTableName) {

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

		Date vDateConverted = Date.valueOf(validityDate);

		String vDateWhereClause = " ? >= " + beginDtColumn + " AND ? <= " + endDtColumn;

		String updateQuery = "UPDATE " + hierTableName + " SET " + hierNameColumn + "= ?, " + bkpColumn + " = ?, " + bkpTimestampColumn + "= ? WHERE "
				+ hierNameColumn + "=? AND " + hierTypeColumn + "= ? AND " + vDateWhereClause;

		logger.debug("The update query is [" + updateQuery + "]");

		// try (Connection databaseConnection = dataSource.getConnection();
		// Statement stmt = databaseConnection.createStatement();
		// PreparedStatement preparedStatement = databaseConnection.prepareStatement(updateQuery)) {

		try (Statement stmt = databaseConnection.createStatement(); PreparedStatement preparedStatement = databaseConnection.prepareStatement(updateQuery)) {
			preparedStatement.setString(1, hierarchyName + "_" + timestamp);
			preparedStatement.setBoolean(2, true);
			preparedStatement.setTimestamp(3, new java.sql.Timestamp(timestamp));
			preparedStatement.setString(4, hierarchyName);
			preparedStatement.setString(5, hierarchyType);
			preparedStatement.setDate(6, vDateConverted);
			preparedStatement.setDate(7, vDateConverted);

			preparedStatement.executeUpdate();
			preparedStatement.close();

			logger.debug("Update query successfully executed");
			logger.debug("END");

		} catch (Throwable t) {
			logger.error("An unexpected error occured while updating hierarchy for backup");
			throw new SpagoBIServiceException("An unexpected error occured while updating hierarchy for backup", t);
		}

	}

	// private void deleteBackupHierarchy(IDataSource dataSource, Connection databaseConnection, String hierBkpName, String hierTableName) {
	//
	// logger.debug("START");
	//
	// String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
	// String bkpColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BKP_COLUMN, dataSource);
	//
	// String deleteQuery = "DELETE FROM " + hierTableName + " WHERE " + hierNameColumn + "= ? AND " + bkpColumn + "= ?";
	//
	// logger.debug("The delete query is [" + deleteQuery + "]");
	//
	// try (Statement stmt = databaseConnection.createStatement(); PreparedStatement preparedStatement = databaseConnection.prepareStatement(deleteQuery)) {
	//
	// preparedStatement.setString(1, hierBkpName);
	//
	// logger.debug("Preparing delete statement. Name of the hierarchy backup is [" + hierBkpName + "]");
	//
	// preparedStatement.setBoolean(2, true);
	//
	// preparedStatement.executeUpdate();
	//
	// logger.debug("Delete query successfully executed");
	// logger.debug("END");
	//
	// } catch (Throwable t) {
	// logger.error("An unexpected error occured while deleting hierarchy backup");
	// throw new SpagoBIServiceException("An unexpected error occured while deleting hierarchy backup", t);
	// }
	//
	// }

	private void restoreBackupHierarchy(IDataSource dataSource, String hierBkpName, String hierTableName) {

		logger.debug("START");

		String hierCdColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_CD, dataSource);
		String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
		String bkpColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BKP_COLUMN, dataSource);

		String selectQuery = "SELECT DISTINCT(" + hierCdColumn + ") FROM " + hierTableName + " WHERE " + hierNameColumn + "= ? AND " + bkpColumn + " = ?";

		logger.debug("The select query is [" + selectQuery + "]");

		String deleteQuery = "DELETE FROM " + hierTableName + " WHERE " + hierNameColumn + "= ? AND " + bkpColumn + " = ?";

		logger.debug("The delete query is [" + deleteQuery + "]");

		String updateQuery = "UPDATE " + hierTableName + " SET " + hierNameColumn + "= ?, " + bkpColumn + " = ? WHERE " + hierNameColumn + "= ?";

		logger.debug("The update query is [" + updateQuery + "]");

		try (Connection databaseConnection = dataSource.getConnection();
				Statement stmt = databaseConnection.createStatement();
				PreparedStatement selectPs = databaseConnection.prepareStatement(selectQuery);
				PreparedStatement deletePs = databaseConnection.prepareStatement(deleteQuery);
				PreparedStatement updatePs = databaseConnection.prepareStatement(updateQuery)) {

			// begin transaction
			databaseConnection.setAutoCommit(false);

			logger.debug("Auto-commit false. Begin transaction!");

			selectPs.setString(1, hierBkpName);

			logger.debug("Preparing select statement. Name of the hierarchy backup is [" + hierBkpName + "]");

			selectPs.setBoolean(2, true);

			ResultSet rs = selectPs.executeQuery();

			logger.debug("Select query executed! Processing result set...");

			if (rs.next()) {

				String hierCd = rs.getString(HierarchyConstants.HIER_CD);

				deletePs.setString(1, hierCd);
				deletePs.setBoolean(2, false);

				logger.debug("Preparing delete statement. Field [" + HierarchyConstants.HIER_CD + "] has value = " + hierCd);

				deletePs.executeUpdate();

				logger.debug("Delete query executed!");

				logger.debug("Preparing update statement.");

				updatePs.setString(1, hierCd);
				updatePs.setBoolean(2, false);
				updatePs.setString(3, hierBkpName);

				updatePs.executeUpdate();

				logger.debug("Update query executed!");
			}

			logger.debug("Executing commit. End transaction!");
			databaseConnection.commit();

			logger.debug("END");

		} catch (Throwable t) {
			logger.error("An unexpected error occured while restoring hierarchy backup");
			throw new SpagoBIServiceException("An unexpected error occured while restoring hierarchy backup", t);
		}

	}

	private String selectHierarchyBkps(IDataSource dataSource, String hierTableName, String hierarchyName, String hierarchyType, List<Field> bkpFields) {

		logger.debug("START");

		// select
		StringBuffer selectClauseBuffer = new StringBuffer(" ");
		String sep = ",";

		int fieldsSize = bkpFields.size();

		for (int i = 0; i < fieldsSize; i++) {
			Field tmpField = bkpFields.get(i);
			String column = AbstractJDBCDataset.encapsulateColumnName(tmpField.getId(), dataSource);

			if (i == fieldsSize - 1) {
				sep = " ";
			}

			selectClauseBuffer.append(column + sep);
		}

		String hierCdColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_CD, dataSource);
		String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
		String hierTypeColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_TP, dataSource);
		String bkpColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BKP_COLUMN, dataSource);

		String selectClause = selectClauseBuffer.toString();

		StringBuffer query = new StringBuffer("SELECT " + selectClause + " FROM " + hierTableName + " WHERE " + bkpColumn + " = 1 ");

		if (hierarchyName != null) {
			query.append(" AND " + hierCdColumn + "= \"" + hierarchyName + "\"");
		}

		if (hierarchyType != null) {
			query.append(" AND " + hierTypeColumn + "= \"" + hierarchyType + "\"");
		}

		query.append(" GROUP BY " + hierNameColumn);

		logger.debug("Query for hier backups data is: " + query);
		logger.debug("END");
		return query.toString();
	}

	private void insertHierarchyMaster(Connection dbConnection, IDataSource dataSource, IRecord record, IDataStore dataStore, String hTableName,
			List<Field> generalFields, List<Field> metadataFields, Map<String, Integer> metatadaFieldsMap, JSONObject requestVal, String prefix) {

		logger.debug("START");

		try (Statement stmt = dbConnection.createStatement()) {

			// Create two clauses, one for columns and another for values
			// INSERT INTO name_table [columnsClause] values [valuesClause]
			StringBuffer columnsClause = new StringBuffer("(");
			StringBuffer valuesClause = new StringBuffer("(");

			String sep = ",";

			// fieldsMap is necessary to keep track of the position for values we need to use later when replace the prep. stat.
			Map<Integer, String> fieldsMap = new HashMap<Integer, String>();

			// typeMap is necessary to keep track of the value type in a position
			Map<Integer, String> fieldsTypeMap = new HashMap<Integer, String>();

			// this counter come across different logics to build the insert query and it's used to keep things sequential
			int index = 0;

			/**********************************************************************************************************
			 * in this section we add columns and values related to hierarchy general fields specified in request JSON*
			 **********************************************************************************************************/

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
				fieldsMap.put(index, value);
				fieldsTypeMap.put(index, type);

				index++;
			}

			/****************************************************************************************
			 * in this section we add columns and values related to levels specified in request JSON*
			 ****************************************************************************************/

			// retrieve levels from request json
			JSONArray lvls = requestVal.getJSONArray("levels");

			int lvlsLength = lvls.length();
			logger.debug("The user has specified [" + lvlsLength + "] level/s");

			// used to keep track of levels for parent, leaf and max-depth
			int lvlIndex;

			for (lvlIndex = 0; lvlIndex < lvlsLength; lvlIndex++) {

				JSONObject lvl = lvls.getJSONObject(lvlIndex);

				// levels begin from 1, the array from 0
				int tmpLvl = lvlIndex + 1;

				// columns for code and name level
				String cdColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEV" + (tmpLvl), dataSource);
				String nmColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_NM_LEV" + (tmpLvl), dataSource);

				// retrieve values to look for in dimension columns
				String cdLvl = lvl.getString("CD");
				String nmLvl = lvl.getString("NM");

				logger.debug("In the level [" + tmpLvl + "] user has specified the code [" + cdLvl + "] and the name [" + nmLvl + "]");

				// retrieve record fields looking at metafield position in the dimension
				IField cdTmpField = record.getFieldAt(metatadaFieldsMap.get(cdLvl));
				IField nmTmpField = record.getFieldAt(metatadaFieldsMap.get(nmLvl));

				String cdValue = (String) cdTmpField.getValue();
				String nmValue = (String) nmTmpField.getValue();

				logger.debug("For the level [" + tmpLvl + "] we are going to insert code [" + cdValue + "] and name [" + nmValue + "]");

				// updating sql clauses for columns and values
				columnsClause.append(cdColumn + "," + nmColumn + sep);
				valuesClause.append("?," + "?" + sep);

				// updating values and types maps
				fieldsMap.put(index, cdValue);
				fieldsMap.put(index + 1, nmValue);

				fieldsTypeMap.put(index, HierarchyConstants.FIELD_TP_STRING);
				fieldsTypeMap.put(index + 1, HierarchyConstants.FIELD_TP_STRING);

				index = index + 2;

				// this condition is needed to set leaf parent code and name values
				if (lvlIndex == lvlsLength - 1) {

					logger.debug("The level [" + tmpLvl + "] is the last specified from the user. We are going to use code [" + cdValue + "] and name ["
							+ nmValue + "] for parent");

					String cdParentColumn = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_CD", dataSource);
					String nmParentColumn = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_NM", dataSource);

					// updating sql clauses for columns and values
					columnsClause.append(cdParentColumn + "," + nmParentColumn + sep);
					valuesClause.append("?," + "?" + sep);

					// updating values and types maps
					fieldsMap.put(index, cdValue);
					fieldsMap.put(index + 1, nmValue);

					fieldsTypeMap.put(index, HierarchyConstants.FIELD_TP_STRING);
					fieldsTypeMap.put(index + 1, HierarchyConstants.FIELD_TP_STRING);

					index = index + 2;

				}

			}

			/*****************************************************************************
			 * in this section we add columns and values related to the level of the leaf*
			 *****************************************************************************/

			String cdColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEV" + (lvlIndex + 1), dataSource);
			String nmColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_NM_LEV" + (lvlIndex + 1), dataSource);

			IField cdTmpField = record.getFieldAt(metatadaFieldsMap.get(prefix + "_CD"));
			IField nmTmpField = record.getFieldAt(metatadaFieldsMap.get(prefix + "_NM"));

			String cdValue = (String) cdTmpField.getValue();
			String nmValue = (String) nmTmpField.getValue();

			logger.debug("For the leaf level [" + lvlIndex + 1 + "] we are going to insert code [" + cdValue + "] and name [" + nmValue + "]");

			// updating sql clauses for columns and values
			columnsClause.append(cdColumn + "," + nmColumn + sep);
			valuesClause.append("?," + "?" + sep);

			// updating values and types maps
			fieldsMap.put(index, cdValue);
			fieldsMap.put(index + 1, nmValue);

			fieldsTypeMap.put(index, HierarchyConstants.FIELD_TP_STRING);
			fieldsTypeMap.put(index + 1, HierarchyConstants.FIELD_TP_STRING);

			index = index + 2;

			/********************************************************************************************************
			 * in this section we add column and value related to the leaf id that comes from id in dimension record*
			 ********************************************************************************************************/

			String leafIdColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_LEAF_ID", dataSource);
			IField leafIdTmpField = record.getFieldAt(metatadaFieldsMap.get(prefix + "_ID"));

			Long leafIdValue = (Long) leafIdTmpField.getValue();

			logger.debug("Leaf ID is [" + leafIdValue + "]");

			// updating sql clauses for columns and values
			columnsClause.append(leafIdColumn + sep);
			valuesClause.append("?" + sep);

			// updating values and types maps
			fieldsMap.put(index, Long.toString(leafIdValue));
			fieldsTypeMap.put(index, HierarchyConstants.FIELD_TP_NUMBER);

			index++;

			/******************************************************************************
			 * in this section we add columns and values related to the leaf code and name*
			 ******************************************************************************/

			String cdLeafColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_CD_LEAF", dataSource);
			String nmLeafColumn = AbstractJDBCDataset.encapsulateColumnName(prefix + "_NM_LEAF", dataSource);

			IField cdLeafTmpField = record.getFieldAt(metatadaFieldsMap.get(prefix + "_CD"));
			IField nmLeafTmpField = record.getFieldAt(metatadaFieldsMap.get(prefix + "_NM"));

			String cdLeafValue = (String) cdLeafTmpField.getValue();
			String nmLeafValue = (String) nmLeafTmpField.getValue();

			logger.debug("For the leaf we are going to insert code [" + cdLeafValue + "] and name [" + nmLeafValue + "]");

			// updating sql clauses for columns and values
			columnsClause.append(cdLeafColumn + sep + nmLeafColumn + sep);
			valuesClause.append("?,?" + sep);

			// updating values and types maps
			fieldsMap.put(index, cdLeafValue);
			fieldsMap.put(index + 1, nmLeafValue);

			fieldsTypeMap.put(index, HierarchyConstants.FIELD_TP_STRING);
			fieldsTypeMap.put(index + 1, HierarchyConstants.FIELD_TP_STRING);

			index = index + 2;

			/******************************************************************************
			 * in this section we add column and value related to the max depth of levels*
			 ******************************************************************************/

			String maxDepthColumn = AbstractJDBCDataset.encapsulateColumnName("MAX_DEPTH", dataSource);

			logger.debug("Levels max depth is [" + lvlIndex + "]");

			// updating sql clauses for columns and values
			columnsClause.append(maxDepthColumn + ")");
			valuesClause.append("?)");

			// updating values and types maps
			fieldsMap.put(index, String.valueOf(lvlIndex));
			fieldsTypeMap.put(index, HierarchyConstants.FIELD_TP_NUMBER);

			/***************************************************************************************
			 * put together clauses in order to create the insert prepared statement and execute it*
			 ***************************************************************************************/

			StringBuffer insertQuery = new StringBuffer("INSERT INTO " + hTableName + columnsClause + " VALUES " + valuesClause);

			logger.debug("The insert query is [" + insertQuery.toString() + "]");

			PreparedStatement insertPs = dbConnection.prepareStatement(insertQuery.toString());

			for (int i = 0; i < fieldsMap.size(); i++) {

				String fieldType = fieldsTypeMap.get(i);
				String tmpValue = fieldsMap.get(i);

				logger.debug("Set the insert prepared statement with a field of type [" + fieldType + "] and value [" + tmpValue + "]");

				if (fieldType.equals(HierarchyConstants.FIELD_TP_STRING)) {
					insertPs.setString(i + 1, tmpValue);
				} else if (fieldType.equals(HierarchyConstants.FIELD_TP_NUMBER)) {
					insertPs.setLong(i + 1, Long.valueOf(tmpValue));
				} else if (fieldType.equals(HierarchyConstants.FIELD_TP_DATE)) {
					final Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(tmpValue);
					java.sql.Date tmpDate = new java.sql.Date(calendar.getTimeInMillis());
					insertPs.setDate(i + 1, tmpDate);
				} else {
					Object tmpObj = tmpValue;
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
}
