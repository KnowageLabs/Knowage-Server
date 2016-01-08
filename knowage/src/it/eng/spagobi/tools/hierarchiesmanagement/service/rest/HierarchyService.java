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
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Hierarchy;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

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
import java.util.List;

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

			IDataStore dataStore = dataSource.executeStatement("SELECT DISTINCT(" + hierarchyNameColumn + ") FROM " + tableName + " WHERE " + typeColumn
					+ "=\"MASTER\"", 0, 0);
			for (Iterator iterator = dataStore.iterator(); iterator.hasNext();) {
				IRecord record = (IRecord) iterator.next();
				IField field = record.getFieldAt(0);
				String hierarchyName = (String) field.getValue();
				JSONObject hierarchy = new JSONObject();
				hierarchy.put(HierarchyConstants.HIER_NM, hierarchyName);
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
			String scopeColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_SCOPE, dataSource);

			String columns = hierarchyNameColumn + "," + typeColumn + "," + hierarchyDescriptionColumn + "," + scopeColumn + " ";
			IDataStore dataStore = dataSource.executeStatement("SELECT DISTINCT(" + hierarchyCodeColumn + ")," + columns + " FROM " + tableName + " WHERE "
					+ typeColumn + "=\"TECHNICAL\" ORDER BY " + hierarchyCodeColumn, 0, 0);
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
				field = record.getFieldAt(4);
				String hierarchyScope = (String) field.getValue();
				JSONObject hierarchy = new JSONObject();
				hierarchy.put(HierarchyConstants.HIER_CD, hierarchyCode);
				hierarchy.put(HierarchyConstants.HIER_NM, hierarchyName);
				hierarchy.put(HierarchyConstants.HIER_TP, hierarchyType);
				hierarchy.put(HierarchyConstants.HIER_DS, hierarchyDescription);
				hierarchy.put(HierarchyConstants.HIER_SCOPE, hierarchyScope);
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

			treeJSONObject = convertHierarchyTreeAsJSON(hierarchyTree, hierarchyName);

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
		try {
			String root = req.getParameter("root");
			JSONObject rootJSONObject = ObjectUtils.toJSONObject(root);
			boolean isInsert = Boolean.valueOf(req.getParameter("isInsert"));
			boolean customTreeInMemorySaved = Boolean.valueOf(req.getParameter("customTreeInMemorySaved"));

			String hierarchyCode = req.getParameter("code");
			String hierarchyName = req.getParameter("name");
			String hierarchyDescription = req.getParameter("description");
			String hierarchyScope = req.getParameter("scope");
			String hierarchyType = req.getParameter("type");
			String validityDate = req.getParameter("dateValidity");

			String dimension = req.getParameter("dimension");

			Collection<List<HierarchyTreeNodeData>> paths = findRootToLeavesPaths(rootJSONObject);

			// Information for persistence
			// 1 - get hierarchy table postfix(ex: _CDC)
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyTable = hierarchies.getHierarchyTableName(dimension);
			String hierarchyPrefix = hierarchies.getPrefix(dimension);
			String hierarchyFK = hierarchies.getHierarchyTableForeignKeyName(dimension);

			// 2 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy", "No datasource found for saving hierarchy");
			}

			if (!isInsert || customTreeInMemorySaved) {
				// deleteHierarchy(req);
				// updateHierarchyForBackup(dataSource, hierarchyType, hierarchyName, validityDate, hierarchyTable);
			}

			for (List<HierarchyTreeNodeData> path : paths) {
				persistCustomHierarchyPath(hierarchyCode, hierarchyName, hierarchyDescription, hierarchyScope, hierarchyType, dataSource, hierarchyPrefix,
						hierarchyFK, path, isInsert);
			}

			return "{\"response\":\"ok\"}";
		} catch (Throwable t) {
			logger.error("An unexpected error occured while saving custom hierarchy structure");
			throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy structure", t);
		}

	}

	@POST
	@Path("/deleteHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String deleteHierarchy(@Context HttpServletRequest req) throws SQLException {
		// delete hierarchy
		Connection connection = null;
		try {
			String dimension = req.getParameter("dimension");
			String hierarchyCode = req.getParameter("code");

			// 1 - get hierarchy table postfix(ex: _CDC)
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyPrefix = hierarchies.getHierarchyTableName(dimension);

			// 2 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);

			// 3 - create query text
			String hierarchyCodeCol = AbstractJDBCDataset.encapsulateColumnName("HIER_CD", dataSource);
			String tableName = "HIER_" + hierarchyPrefix;
			String queryText = "DELETE FROM " + tableName + " WHERE " + hierarchyCodeCol + "=\"" + hierarchyCode + "\" ";

			// 4 - Execute DELETE statement
			connection = dataSource.getConnection();
			Statement statement = connection.createStatement();
			statement.executeUpdate(queryText);
			statement.close();

		} catch (Throwable t) {
			logger.error("An unexpected error occured while deleting custom hierarchy");
			throw new SpagoBIServiceException("An unexpected error occured while deleting custom hierarchy", t);
		} finally {
			connection.close();
		}

		return "{\"response\":\"ok\"}";

	}

	@POST
	@Path("/modifyHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String modifyHierarchy(@Context HttpServletRequest req) throws SQLException {
		// modify an existing custom hierarchy
		try {
			String dimension = req.getParameter("dimension");
			String hierarchyCode = req.getParameter("code");
			String hierarchyName = req.getParameter("name");
			String root = req.getParameter("root");
			String hierarchyDescription = req.getParameter("description");
			String hierarchyScope = req.getParameter("scope");
			String hierarchyType = req.getParameter("type");

			if ((dimension == null) || (hierarchyCode == null) || (hierarchyName == null) || (root == null) || (hierarchyDescription == null)
					|| (hierarchyScope == null) || (hierarchyType == null)) {
				throw new SpagoBIServiceException("An unexpected error occured while modifing custom hierarchy", "wrong request parameters");
			}
			// rename old hierarchy (as backup)
			// deleteHierarchy(req);
			saveHierarchy(req);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while modifing custom hierarchy");
			throw new SpagoBIServiceException("An unexpected error occured while modifing custom hierarchy", t);
		}

		return "{\"response\":\"ok\"}";

	}

	@POST
	@Path("/restoreHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String restoreHierarchy(@Context HttpServletRequest req) throws SQLException {
		// restores a backup hierarchy
		try {

			logger.debug("START");

			String hierarchyBkpName = req.getParameter("name");
			String dimension = req.getParameter("dimension");

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
		int pos = 0;
		// general fields:
		for (int i = 0, l = generalMetadataFields.size(); i < l; i++) {
			Field f = generalMetadataFields.get(i);
			String sep = ", ";
			String column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
			selectClauseBuffer.append(column + sep);
			pos++;
		}
		// node fields:
		for (int i = 0, l = nodeMetadataFields.size(); i < l; i++) {
			Field f = nodeMetadataFields.get(i);
			String sep = ", ";
			String column = "";
			if (f.isSingleValue()) {
				column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
				selectClauseBuffer.append(column + sep);
				pos++;
			} else {
				for (int i2 = 1, l2 = totalLevels; i2 <= l2; i2++) {
					sep = ",";
					column = AbstractJDBCDataset.encapsulateColumnName(f.getId() + i2, dataSource);
					selectClauseBuffer.append(column + sep);
					pos++;
				}
			}
		}
		// leaf fields:
		for (int i = 0, l = leafMetadataFields.size(); i < l; i++) {
			Field f = leafMetadataFields.get(i);
			String sep = ",";
			String column = AbstractJDBCDataset.encapsulateColumnName(f.getId(), dataSource);
			selectClauseBuffer.append(column + sep);
			pos++;
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
		// ONLY FOR DEBUG
		// Set<String> allNodeCodes = new HashSet<String>();

		metadata = dataStore.getMetaData(); // saving metadata for next using

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();
		String prefix = hierarchies.getPrefix(dimension);
		HashMap hierConfig = hierarchies.getConfig(dimension);
		int numLevels = Integer.parseInt((String) hierConfig.get(HierarchyConstants.NUM_LEVELS));
		// contains the code of the last level node (not null) inserted in the
		// tree
		IMetaData dsMeta = dataStore.getMetaData();
		for (Iterator iterator = dataStore.iterator(); iterator.hasNext();) {
			String lastLevelFound = null;

			IRecord record = (IRecord) iterator.next();
			List<IField> recordFields = record.getFields();
			int fieldsCount = recordFields.size();

			// MAX_DEPTH, must be equal to the level of the leaf (that we skip)
			IField maxDepthField = record.getFieldAt(dsMeta.getFieldIndex("MAX_DEPTH"));
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
				IField codeField = record.getFieldAt(dsMeta.getFieldIndex(prefix + "_CD_LEV" + i)); // NODE CODE
				IField nameField = record.getFieldAt(dsMeta.getFieldIndex(prefix + "_NM_LEV" + i)); // NAME CODE

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

					// first level
					if (root == null) {
						root = new HierarchyTreeNode(data, nodeCode);
						lastLevelFound = nodeCode;
					} else {
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
						} else if (!root.getKey().contains(nodeCode) && !root.getChildrensKeys().contains(nodeCode)) {
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
							attachNodeToLevel(root, nodeCode, lastLevelFound, data, null);
						}
						lastLevelFound = nodeCode;
					}
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

	private JSONObject convertHierarchyTreeAsJSON(HierarchyTreeNode root, String hierName) {
		JSONObject rootJSONObject = new JSONObject();

		if (root == null)
			return null;

		try {
			HierarchyTreeNodeData rootData = (HierarchyTreeNodeData) root.getObject();
			rootJSONObject.put("name", rootData.getNodeName());
			rootJSONObject.put("id", rootData.getNodeCode());
			rootJSONObject.put("leaf", false);
			rootJSONObject.put(HierarchyConstants.LEVEL, 1);

			JSONArray childrenJSONArray = new JSONArray();

			for (int i = 0; i < root.getChildCount(); i++) {
				HierarchyTreeNode childNode = root.getChild(i);
				JSONObject subTreeJSONObject = getSubTreeJSONObject(childNode);
				childrenJSONArray.put(subTreeJSONObject);
			}

			rootJSONObject.put("children", childrenJSONArray);
			// fake root
			JSONObject mainObject = new JSONObject();
			// transform the root's children JSON object in array like all others children
			JSONArray arRootJSONObject = new JSONArray();
			arRootJSONObject.put(rootJSONObject);
			mainObject.put("name", hierName);
			mainObject.put("id", "root");
			mainObject.put("root", true);
			// mainObject.put("children", rootJSONObject);
			mainObject.put("children", arRootJSONObject);
			mainObject.put("leaf", false);

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
	private JSONObject getSubTreeJSONObject(HierarchyTreeNode node) {

		try {
			if (node.getChildCount() > 0) {
				JSONObject nodeJSONObject = new JSONObject();
				HierarchyTreeNodeData nodeData = (HierarchyTreeNodeData) node.getObject();
				nodeJSONObject.put("name", nodeData.getNodeName());
				nodeJSONObject.put("id", nodeData.getNodeCode());
				nodeJSONObject.put("LEAF_ID", nodeData.getLeafId());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_CD, nodeData.getLeafParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_ORIG_PARENT_CD, nodeData.getLeafOriginalParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_NM, nodeData.getLeafParentName());

				JSONArray childrenJSONArray = new JSONArray();

				for (int i = 0; i < node.getChildCount(); i++) {
					HierarchyTreeNode childNode = node.getChild(i);
					JSONObject subTree = getSubTreeJSONObject(childNode);
					childrenJSONArray.put(subTree);
				}
				nodeJSONObject.put("children", childrenJSONArray);
				nodeJSONObject.put("leaf", false);
				// nodeJSONObject.put("expanded", false);

				nodeJSONObject = setDetailsInfo(nodeJSONObject, nodeData);
				return nodeJSONObject;

			} else {
				HierarchyTreeNodeData nodeData = (HierarchyTreeNodeData) node.getObject();
				JSONObject nodeJSONObject = new JSONObject();

				nodeJSONObject.put("name", nodeData.getNodeName());
				nodeJSONObject.put("id", nodeData.getNodeCode());
				nodeJSONObject.put("LEAF_ID", nodeData.getLeafId());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_CD, nodeData.getLeafParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_ORIG_PARENT_CD, nodeData.getLeafOriginalParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_NM, nodeData.getLeafParentName());
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

			toReturn.put("BEGIN_DT", nodeData.getBeginDt());
			toReturn.put("END_DT", nodeData.getEndDt());

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
	private void persistCustomHierarchyPath(String hierarchyCode, String hierarchyName, String hierarchyDescription, String hierarchyScope,
			String hierarchyType, IDataSource dataSource, String hierarchyPrefix, String hierarchyFK, List<HierarchyTreeNodeData> path, boolean isInsert)
			throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			// Insert prepared statement construction
			// ------------------------------------------
			String insertQuery = createInsertStatement(hierarchyPrefix, hierarchyFK, dataSource);
			PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

			// Valorization of prepared statement placeholder
			// -----------------------------------------------
			final int COLUMNSNUMBER = 58; // total columns number (with signs and dates)

			// HIER_DS column
			preparedStatement.setString(1, hierarchyDescription);
			// HIER_TP column
			preparedStatement.setString(2, hierarchyType);
			// SCOPE column
			preparedStatement.setString(3, hierarchyScope);
			// set all level column to null by default
			for (int i = 6; i < COLUMNSNUMBER + 1; i++) {
				preparedStatement.setNull(i, java.sql.Types.VARCHAR);
			}

			// explore the path and set the corresponding columns
			// keeps the column number
			int colIndex = 4;
			// HIER_CD e HIER_NM are the columns 4 and 5

			for (int i = 0; i < path.size(); i++) {
				HierarchyTreeNodeData node = path.get(i);

				if (i == path.size() - 1) {
					// last node is a leaf
					preparedStatement.setString(COLUMNSNUMBER - 22, node.getNodeCode());
					preparedStatement.setString(COLUMNSNUMBER - 21, node.getNodeName());
					preparedStatement.setLong(COLUMNSNUMBER - 20, Long.valueOf(node.getLeafId()));
					preparedStatement.setString(COLUMNSNUMBER - 19, node.getLeafParentCode());
					preparedStatement.setString(COLUMNSNUMBER - 18, node.getLeafParentName());
					preparedStatement.setDate(COLUMNSNUMBER - 17, node.getBeginDt());
					preparedStatement.setDate(COLUMNSNUMBER - 16, node.getEndDt());
					// preparedStatement.setInt(COLUMNSNUMBER - 15, node.getSignLev1());
					// preparedStatement.setInt(COLUMNSNUMBER - 14, node.getSignLev2());
					// preparedStatement.setInt(COLUMNSNUMBER - 13, node.getSignLev3());
					// preparedStatement.setInt(COLUMNSNUMBER - 12, node.getSignLev4());
					// preparedStatement.setInt(COLUMNSNUMBER - 11, node.getSignLev5());
					// preparedStatement.setInt(COLUMNSNUMBER - 10, node.getSignLev6());
					// preparedStatement.setInt(COLUMNSNUMBER - 9, node.getSignLev7());
					// preparedStatement.setInt(COLUMNSNUMBER - 8, node.getSignLev8());
					// preparedStatement.setInt(COLUMNSNUMBER - 7, node.getSignLev9());
					// preparedStatement.setInt(COLUMNSNUMBER - 6, node.getSignLev10());
					// preparedStatement.setInt(COLUMNSNUMBER - 5, node.getSignLev11());
					// preparedStatement.setInt(COLUMNSNUMBER - 4, node.getSignLev12());
					// preparedStatement.setInt(COLUMNSNUMBER - 3, node.getSignLev13());
					// preparedStatement.setInt(COLUMNSNUMBER - 2, node.getSignLev14());
					// preparedStatement.setInt(COLUMNSNUMBER - 1, node.getSignLev15());

					if (isInsert) {
						preparedStatement.setLong(COLUMNSNUMBER, Long.valueOf(node.getDepth()));
					} else {
						// editing an existing hierarchy, we must take note that
						// depth is +1 because of the fake root
						preparedStatement.setLong(COLUMNSNUMBER, Long.valueOf(node.getDepth()) - 1);
					}
					// insert leaf node also as a (last) level
					preparedStatement.setString(colIndex, node.getNodeCode());
					preparedStatement.setString(colIndex + 1, node.getNodeName());

				} else {
					// not-leaf node
					preparedStatement.setString(colIndex, node.getNodeCode());
					preparedStatement.setString(colIndex + 1, node.getNodeName());
				}
				colIndex = colIndex + 2;
			}

			// Execution of prepared statement
			// ----------------------------------------
			preparedStatement.executeUpdate();
			preparedStatement.close();

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while persisting hierarchy structure", t);
		} finally {
			if (connection != null && !connection.isClosed()) {
				connection.close();
			}
		}
	}

	/**
	 * Create insert statement for hierarchy path persistence
	 *
	 * @param hierarchyPrefix
	 * @param hierarchyFK
	 * @param dataSource
	 * @return
	 */
	private String createInsertStatement(String hierarchyPrefix, String hierarchyFK, IDataSource dataSource) {
		String tableName = "HIER_" + hierarchyPrefix;
		String columns = columnsToInsert(hierarchyPrefix, hierarchyFK, dataSource);
		String insertQuery = "insert into " + tableName + "(" + columns
				+ ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," + "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		return insertQuery;
	}

	private String columnsToInsert(String hierarchyPrefix, String hierarchyFK, IDataSource dataSource) {
		String hierarchyNameCode = AbstractJDBCDataset.encapsulateColumnName("HIER_CD", dataSource);
		String hierarchyNameCol = AbstractJDBCDataset.encapsulateColumnName("HIER_NM", dataSource);
		String hierarchyDescriptionCol = AbstractJDBCDataset.encapsulateColumnName("HIER_DS", dataSource);
		String hierarchyTypeCol = AbstractJDBCDataset.encapsulateColumnName("HIER_TP", dataSource);
		String hierarchyScopeCol = AbstractJDBCDataset.encapsulateColumnName("SCOPE", dataSource);
		StringBuffer columns = new StringBuffer(hierarchyDescriptionCol + "," + hierarchyTypeCol + "," + hierarchyScopeCol + "," + hierarchyNameCode + ","
				+ hierarchyNameCol + ",");

		for (int i = 1; i < 16; i++) {
			String CD_LEV = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_CD_LEV" + i, dataSource);
			String NM_LEV = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_NM_LEV" + i, dataSource);
			columns.append(CD_LEV + "," + NM_LEV + ",");
		}
		String CD_LEAF = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_CD_LEAF", dataSource);
		String NM_LEAF = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_NM_LEAF", dataSource);
		String LEAF_ID = AbstractJDBCDataset.encapsulateColumnName(hierarchyFK, dataSource);
		columns.append(CD_LEAF + "," + NM_LEAF + "," + LEAF_ID + ", ");
		String LEAF_PARENT_CD = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_CD", dataSource);
		String LEAF_PARENT_NM = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_NM", dataSource);
		columns.append(LEAF_PARENT_CD + "," + LEAF_PARENT_NM + ",");

		String BEGIN_DT = AbstractJDBCDataset.encapsulateColumnName("BEGIN_DT", dataSource);
		String END_DT = AbstractJDBCDataset.encapsulateColumnName("END_DT", dataSource);
		String G_SIGN_LIV1 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV1", dataSource);
		String G_SIGN_LIV2 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV2", dataSource);
		String G_SIGN_LIV3 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV3", dataSource);
		String G_SIGN_LIV4 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV4", dataSource);
		String G_SIGN_LIV5 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV5", dataSource);
		String G_SIGN_LIV6 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV6", dataSource);
		String G_SIGN_LIV7 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV7", dataSource);
		String G_SIGN_LIV8 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV8", dataSource);
		String G_SIGN_LIV9 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV9", dataSource);
		String G_SIGN_LIV10 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV10", dataSource);
		String G_SIGN_LIV11 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV11", dataSource);
		String G_SIGN_LIV12 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV12", dataSource);
		String G_SIGN_LIV13 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV13", dataSource);
		String G_SIGN_LIV14 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV14", dataSource);
		String G_SIGN_LIV15 = AbstractJDBCDataset.encapsulateColumnName("G_SIGN_LIV15", dataSource);
		String maxDepthCol = AbstractJDBCDataset.encapsulateColumnName("MAX_DEPTH", dataSource);
		columns.append(BEGIN_DT + "," + END_DT + "," + G_SIGN_LIV1 + "," + G_SIGN_LIV2 + "," + G_SIGN_LIV3 + "," + G_SIGN_LIV4 + "," + G_SIGN_LIV5 + ","
				+ G_SIGN_LIV6 + "," + G_SIGN_LIV7 + "," + G_SIGN_LIV8 + "," + G_SIGN_LIV9 + "," + G_SIGN_LIV10 + "," + G_SIGN_LIV11 + "," + G_SIGN_LIV12 + ","
				+ G_SIGN_LIV13 + "," + G_SIGN_LIV14 + "," + G_SIGN_LIV15 + ", " + maxDepthCol);

		return columns.toString();
	}

	/**
	 * Find all paths from root to leaves
	 */
	private Collection<List<HierarchyTreeNodeData>> findRootToLeavesPaths(JSONObject node) {
		Collection<List<HierarchyTreeNodeData>> collectionOfPaths = new HashSet<List<HierarchyTreeNodeData>>();
		try {
			String nodeName = node.getString("text");
			String nodeCode = node.getString("id");

			String nodeLeafId = node.getString("leafId");
			HierarchyTreeNodeData nodeData = new HierarchyTreeNodeData(nodeCode, nodeName, nodeLeafId, "", "", "", new HashMap());

			// current node is a leaf?
			boolean isLeaf = node.getBoolean("leaf");
			if (isLeaf) {
				List<HierarchyTreeNodeData> aPath = new ArrayList<HierarchyTreeNodeData>();
				String nodeParentCode = node.getString("leafParentCode");
				String nodeOriginalParentCode = node.getString("originalLeafParentCode");
				nodeData.setNodeCode(nodeCode.replaceFirst(nodeOriginalParentCode + "_", ""));
				nodeData.setLeafParentCode(nodeParentCode);
				nodeData.setLeafParentName(node.getString("leafParentName"));
				nodeData.setLeafOriginalParentCode(nodeOriginalParentCode);
				nodeData.setDepth(node.getString("depth"));
				nodeData.setBeginDt(Date.valueOf(node.getString("beginDt")));
				nodeData.setEndDt(Date.valueOf(node.getString("endDt")));
				// nodeData.setSignLev1(Integer.valueOf(node.getString("signLev1")));
				// nodeData.setSignLev2(Integer.valueOf(node.getString("signLev2")));
				// nodeData.setSignLev3(Integer.valueOf(node.getString("signLev3")));
				// nodeData.setSignLev4(Integer.valueOf(node.getString("signLev4")));
				// nodeData.setSignLev5(Integer.valueOf(node.getString("signLev5")));
				// nodeData.setSignLev6(Integer.valueOf(node.getString("signLev6")));
				// nodeData.setSignLev7(Integer.valueOf(node.getString("signLev7")));
				// nodeData.setSignLev8(Integer.valueOf(node.getString("signLev8")));
				// nodeData.setSignLev9(Integer.valueOf(node.getString("signLev9")));
				// nodeData.setSignLev10(Integer.valueOf(node.getString("signLev10")));
				// nodeData.setSignLev11(Integer.valueOf(node.getString("signLev11")));
				// nodeData.setSignLev12(Integer.valueOf(node.getString("signLev12")));
				// nodeData.setSignLev13(Integer.valueOf(node.getString("signLev13")));
				// nodeData.setSignLev14(Integer.valueOf(node.getString("signLev14")));
				// nodeData.setSignLev15(Integer.valueOf(node.getString("signLev15")));
				aPath.add(nodeData);
				collectionOfPaths.add(aPath);
				return collectionOfPaths;
			} else {
				// node has children
				JSONArray childs = node.getJSONArray("children");
				for (int i = 0; i < childs.length(); i++) {
					JSONObject child = childs.getJSONObject(i);
					Collection<List<HierarchyTreeNodeData>> childPaths = findRootToLeavesPaths(child);
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

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(calendar.getTime());
		long timestamp = calendar.getTimeInMillis();

		Date vDateConverted = Date.valueOf(validityDate);

		String vDateWhereClause = " ? >= " + beginDtColumn + " AND ? <= " + endDtColumn;

		String updateQuery = "UPDATE " + hierTableName + " SET " + hierNameColumn + "= ?, " + bkpColumn + " = ? WHERE " + hierNameColumn + "=? AND "
				+ hierTypeColumn + "= ? AND " + vDateWhereClause;

		logger.debug("The update query is [" + updateQuery + "]");

		try (Statement stmt = databaseConnection.createStatement(); PreparedStatement preparedStatement = databaseConnection.prepareStatement(updateQuery)) {

			preparedStatement.setString(1, hierarchyName + "_" + timestamp);
			preparedStatement.setBoolean(2, true);
			preparedStatement.setString(3, hierarchyName);
			preparedStatement.setString(4, hierarchyType);
			preparedStatement.setDate(5, vDateConverted);
			preparedStatement.setDate(6, vDateConverted);

			preparedStatement.executeUpdate();

			logger.debug("Update query successfully executed");
			logger.debug("END");

		} catch (Throwable t) {
			logger.error("An unexpected error occured while updating hierarchy for backup");
			throw new SpagoBIServiceException("An unexpected error occured while updating hierarchy for backup", t);
		}

	}

	private void deleteBackupHierarchy(IDataSource dataSource, Connection databaseConnection, String hierBkpName, String hierTableName) {

		logger.debug("START");

		String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
		String bkpColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BKP_COLUMN, dataSource);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(calendar.getTime());

		String deleteQuery = "DELETE FROM " + hierTableName + " WHERE " + hierNameColumn + "= ? AND " + bkpColumn + "= ?";

		logger.debug("The delete query is [" + deleteQuery + "]");

		try (Statement stmt = databaseConnection.createStatement(); PreparedStatement preparedStatement = databaseConnection.prepareStatement(deleteQuery)) {

			preparedStatement.setString(1, hierBkpName);

			logger.debug("Preparing delete statement. Name of the hierarchy backup is [" + hierBkpName + "]");

			preparedStatement.setBoolean(2, true);

			preparedStatement.executeUpdate();

			logger.debug("Delete query successfully executed");
			logger.debug("END");

		} catch (Throwable t) {
			logger.error("An unexpected error occured while deleting hierarchy backup");
			throw new SpagoBIServiceException("An unexpected error occured while deleting hierarchy backup", t);
		}

	}

	private void restoreBackupHierarchy(IDataSource dataSource, String hierBkpName, String hierTableName) {

		logger.debug("START");

		String hierCdColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_CD, dataSource);
		String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM, dataSource);
		String bkpColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BKP_COLUMN, dataSource);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(calendar.getTime());

		String selectQuery = "SELECT " + hierCdColumn + " FROM " + hierTableName + " WHERE " + hierNameColumn + "= ? AND " + bkpColumn + " = ?";

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

}
