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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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

import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
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
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/*
 * This class contains all REST services used by all hierarchy types (master and technical)
 */

@Path("/hierarchies")
public class HierarchyService {

	private static Logger logger = Logger.getLogger(HierarchyService.class);
	
	private static final String START = "START";
	private static final String DO_BACKUP = "doBackup";
	private static final String DO_PROPAGATION = "doPropagation";
	private static final String DIMENSION = "dimension";
	private static final String HIER_SOURCE_CODE = "hierSourceCode";
	private static final String HIER_SOURCE_NAME = "hierSourceName";
	private static final String HIER_SOURCE_TYPE = "hierSourceType";
	private static final String HIERARCHY_TABLE = "hierarchyTable";
	private static final String HIERARCHY_PREFIX = "hierarchyPrefix";
	private static final String PRIMARY_KEY = "primaryKey";
	private static final String PRIMARY_KEY_COUNT = "primaryKeyCount";
	private static final String ALIAS_ID = "aliasId";
	private static final String ALIAS_NAME = "aliasName";
	private static final String CHILDREN = "children";
	private static final String IS_ROOT = "isRoot";
	

	// get automatic hierarchy structure for tree visualization
	@GET
	@Path("/getHierarchyTree")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.HIERARCHIES_MANAGEMENT })
	public String getHierarchyTree(@QueryParam("dimension") String dimension,
			@QueryParam("filterType") String hierarchyType, @QueryParam("filterHierarchy") String hierarchyName,
			@QueryParam("validityDate") String hierarchyDate, @QueryParam("filterDimension") String filterDimension,
			@QueryParam("filterDate") String filterDate, @QueryParam("optionDate") String optionDate,
			@QueryParam("optionHierarchy") String optionHierarchy,
			@QueryParam("optionHierType") String optionHierType) {
		
		logger.debug(START);

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

			// 2 - get datastore with all hierachies' leafs
			IMetaData metadata = null;
			boolean excludeDimLeaf = (filterDimension != null || optionHierarchy != null) ? true : false;
			IDataStore dataStore = HierarchyUtils.getHierarchyDataStore(dataSource, dimension, hierarchyType,
					hierarchyName, hierarchyDate, filterDate, filterDimension, optionDate, optionHierarchy,
					optionHierType, excludeDimLeaf);

			// 4 - Create ADT for Tree from datastore
			hierarchyTree = createHierarchyTreeStructure(dataStore, dimension, metadata);
			treeJSONObject = convertHierarchyTreeAsJSON(hierarchyTree, hierarchyName, dimension);

			if (treeJSONObject == null)
				return null;

		} catch (Exception e) {
			logger.error("An unexpected error occured while retriving hierarchy structure");
			throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchy structure", e);
		}
		logger.debug("END");
		return treeJSONObject.toString();
	}

	@GET
	@Path("/hierarchyMetadata")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.HIERARCHIES_MANAGEMENT })
	public String getHierarchyFields(@QueryParam("dimension") String dimensionName) {

		logger.debug(START);

		JSONObject result = new JSONObject();

		try {

			result = createHierarchyJSON(dimensionName, false);

		} catch (Exception e) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", e);
		}

		logger.debug("END");
		return result.toString();
	}

	@GET
	@Path("/nodeMetadata")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.HIERARCHIES_MANAGEMENT })
	public String getHierarchyNodeFields(@QueryParam("dimension") String dimensionName,
			@QueryParam("excludeLeaf") boolean excludeLeaf) {

		logger.debug(START);

		JSONObject result = new JSONObject();

		try {

			result = createHierarchyJSON(dimensionName, excludeLeaf);

		} catch (Exception e) {
			logger.error("An unexpected error occured while creating dimensions json");
			throw new SpagoBIServiceException("An unexpected error occured while creating dimensions json", e);
		}

		logger.debug("END");
		return result.toString();
	}

	@POST
	@Path("/saveHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.HIERARCHIES_MANAGEMENT })
	public String saveHierarchy(@Context HttpServletRequest req) {
		Connection connection = null;
		try {

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			Map<String, Object> paramsMap = new HashMap<>();

			String validityDate = (!requestVal.isNull("dateValidity")) ? requestVal.getString("dateValidity") : null;
			paramsMap.put("validityDate", validityDate);
			boolean doBackup = (!requestVal.isNull(DO_BACKUP)) ? requestVal.getBoolean(DO_BACKUP)
					: new Boolean("false");
			paramsMap.put(DO_BACKUP, doBackup);
			boolean isInsert = Boolean.parseBoolean(req.getParameter("isInsert"));
			paramsMap.put("isInsert", isInsert);
			String dimension = requestVal.getString(DIMENSION);
			paramsMap.put(DIMENSION, dimension);
			String hierSourceCode = (!requestVal.isNull(HIER_SOURCE_CODE)) ? requestVal.getString(HIER_SOURCE_CODE)
					: null;
			paramsMap.put(HIER_SOURCE_CODE, hierSourceCode);
			String hierSourceName = (!requestVal.isNull(HIER_SOURCE_NAME)) ? requestVal.getString(HIER_SOURCE_NAME)
					: null;
			paramsMap.put(HIER_SOURCE_NAME, hierSourceName);
			String hierSourceType = (!requestVal.isNull(HIER_SOURCE_TYPE)) ? requestVal.getString(HIER_SOURCE_TYPE)
					: null;
			paramsMap.put(HIER_SOURCE_TYPE, hierSourceType);

			String root = requestVal.getString("root"); // tree
			JSONObject rootJSONObject = ObjectUtils.toJSONObject(root);
			String hierTargetCode = rootJSONObject.getString(HierarchyConstants.HIER_CD);
			paramsMap.put("hierTargetCode", hierTargetCode);
			String hierTargetName = rootJSONObject.getString(HierarchyConstants.HIER_NM);
			paramsMap.put("hierTargetName", hierTargetName);
			String hierTargetType = rootJSONObject.getString(HierarchyConstants.HIER_TP);
			paramsMap.put("hierTargetType", hierTargetType);

			// 1 - get informations for persistence (ie. hierarchy table postfix..)
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyTable = hierarchies.getHierarchyTableName(dimension);
			paramsMap.put(HIERARCHY_TABLE, hierarchyTable);

			String hierarchyPrefix = hierarchies.getPrefix(dimension);
			paramsMap.put(HIERARCHY_PREFIX, hierarchyPrefix);
			String hierarchyFK = hierarchies.getHierarchyTableForeignKeyName(dimension);
			paramsMap.put("hierarchyFK", hierarchyFK);
			Hierarchy hierarchyFields = hierarchies.getHierarchy(dimension);
			Map<String, Object> hierConfig = hierarchies.getConfig(dimension);
			paramsMap.put(HierarchyConstants.NUM_LEVELS, hierConfig.get(HierarchyConstants.NUM_LEVELS));
			paramsMap.put(HierarchyConstants.TREE_NODE_CD, hierConfig.get(HierarchyConstants.TREE_NODE_CD));
			paramsMap.put(HierarchyConstants.TREE_NODE_NM, hierConfig.get(HierarchyConstants.TREE_NODE_NM));
			paramsMap.put(HierarchyConstants.TREE_LEAF_CD, hierConfig.get(HierarchyConstants.TREE_LEAF_CD));

			// 2 - Definition of the context (ex. manage propagations ONLY when the sourceHierType is MASTER and the targtHierType is TECHNICAL)
			boolean doPropagation = false;
			if (hierSourceType != null && hierSourceType.equals(HierarchyConstants.HIER_TP_MASTER)
					&& hierTargetType != null && hierTargetType.equals(HierarchyConstants.HIER_TP_TECHNICAL)) {
				doPropagation = true;
				paramsMap.put(DO_PROPAGATION, doPropagation);
			}

			// 3 - get all paths from the input json tree
			Collection<List<HierarchyTreeNodeData>> paths = findRootToLeavesPaths(rootJSONObject, dimension,
					hierTargetName, hierTargetName, 1);

			// 4 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy",
						"No datasource found for saving hierarchy");
			}

			// get one ONLY connection for all statements (transactional logic)
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			String primaryKey = hierarchies.getTablePrimaryKey(dimension);
			if (primaryKey != null) {
				paramsMap.put(PRIMARY_KEY, primaryKey);
				int countId = HierarchyUtils.getCountId(primaryKey, hierarchyTable, connection, dataSource);
				paramsMap.put(PRIMARY_KEY_COUNT, countId);
			}

			if (!isInsert && doBackup) {
				HierarchyUtils.updateHierarchyForBackup(dataSource, connection, paramsMap, false);
			} else if (!isInsert && !doBackup) {
				HierarchyUtils.deleteHierarchy(dimension, hierTargetName, dataSource, connection);
			}

			List lstRelMTInserted = new ArrayList();
			paramsMap.put("hierarchyFields", hierarchyFields);
			paramsMap.put("lstRelMTInserted", lstRelMTInserted);
			for (List<HierarchyTreeNodeData> path : paths) {
				paramsMap.put("path", path);
				persistHierarchyPath(connection, dataSource, paramsMap);
			}

			// propagate new leaves through the relations MT
			String relationsMT = (!requestVal.isNull("relationsMT")) ? requestVal.getString("relationsMT") : null; // relations MASTER-TECHNICAL (propagation
																													 // management)
			if (relationsMT != null && relationsMT.length() > 0) {
				JSONArray relationsMTJSONObject = ObjectUtils.toJSONArray(relationsMT);
				propagateNewLeaves(connection, dataSource, paramsMap, relationsMTJSONObject, hierarchyFields);
			}
			// OK - commit ALL changes!
			connection.commit();
			return "{\"response\":\"ok\"}";
		} catch (Exception e) {
			logger.error("An unexpected error occured while saving custom hierarchy structure");
			try {
				if (connection != null && !connection.isClosed()) {
					connection.rollback();
				}
			} catch (SQLException sqle) {
				throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy structure",
						sqle);
			}
			throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy structure", e);
		} finally {
			try {
				if (connection != null && !connection.isClosed()) {
					connection.close();
				}
			} catch (SQLException sqle) {
				throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy structure",
						sqle);
			}
		}
	}

	@POST
	@Path("/deleteHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.HIERARCHIES_MANAGEMENT })
	public String deleteHierarchy(@Context HttpServletRequest req) throws SQLException {
		// delete hierarchy
		Connection connection = null;
		try {

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			String dimension = requestVal.getString(DIMENSION);
			String hierarchyName = requestVal.getString("name");

			// 1 - get datasource label name
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);

			// 2 - Execute DELETE
			connection = dataSource.getConnection();
			HierarchyUtils.deleteHierarchy(dimension, hierarchyName, dataSource, connection);

		} catch (Exception e) {
			connection.rollback();
			logger.error("An unexpected error occured while deleting custom hierarchy");
			throw new SpagoBIServiceException("An unexpected error occured while deleting custom hierarchy", e);
		} finally {
			if (connection != null && !connection.isClosed())
				connection.close();
		}

		return "{\"response\":\"ok\"}";
	}

	@GET
	@Path("/getRelationsMasterTechnical")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.HIERARCHIES_MANAGEMENT })
	public String getRelationsMasterTechnical(@QueryParam("dimension") String dimension,
			@QueryParam("hierSourceCode") String hierSourceCode, @QueryParam("hierSourceName") String hierSourceName,
			@QueryParam("nodeSourceCode") String nodeSourceCode) throws SQLException {
		// get relations between master and technical nodes
		Connection connection = null;
		JSONObject result = new JSONObject();
		try {
			Map<String, Object> paramsMap = new HashMap<>();
			paramsMap.put(HierarchyConstants.DIMENSION, dimension);
			paramsMap.put(HierarchyConstants.HIER_CD_M, hierSourceCode);
			paramsMap.put(HierarchyConstants.HIER_NM_M, hierSourceName);
			paramsMap.put(HierarchyConstants.NODE_CD_M, nodeSourceCode);

			// 1 - get datasource label name
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);

			// 2 - Execute query
			connection = dataSource.getConnection();

			String selectQuery = createQueryRelationsHierarchy(dataSource, paramsMap);
			IDataStore dataStore = dataSource.executeStatement(selectQuery, 0, 0);

			// Create JSON for relational data from datastore
			JSONArray dataArray = HierarchyUtils.createRootData(dataStore);

			logger.debug("Root array is [" + dataArray.toString() + "]");
			result.put(HierarchyConstants.ROOT, dataArray);

		} catch (Exception e) {
			logger.error("An unexpected error occured while deleting custom hierarchy");
			throw new SpagoBIServiceException("An unexpected error occured while deleting custom hierarchy", e);
		} finally {
			if (connection != null && !connection.isClosed())
				connection.close();
		}
		logger.debug("JSON for relational data is [" + result.toString() + "]");
		logger.debug("END");

		return result.toString();
	}

	/**
	 * This method manages the creation of the JSON for hierarchies fields
	 *
	 * @param dimensionName the name of the dimension
	 * @param excludeLeaf   exclusion for fields in leaf section
	 * @return the JSON with fields in hierarchy section
	 * @throws JSONException
	 */
	private JSONObject createHierarchyJSON(String dimensionName, boolean excludeLeaf) throws JSONException {

		logger.debug(START);

		JSONObject result = new JSONObject();

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();
		Assert.assertNotNull(hierarchies, "Impossible to find valid hierarchies config");

		Hierarchy hierarchy = hierarchies.getHierarchy(dimensionName);
		Assert.assertNotNull(hierarchy,
				"Impossible to find a hierarchy for the dimension called [" + dimensionName + "]");

		JSONObject configs = HierarchyUtils.createJSONArrayFromHashMap(hierarchies.getConfig(dimensionName), null);
		result.put(HierarchyConstants.CONFIGS, configs);

		List<Field> generalMetadataFields = new ArrayList<>(hierarchy.getMetadataGeneralFields());
		JSONArray generalFieldsJSONArray = HierarchyUtils.createJSONArrayFromFieldsList(generalMetadataFields, true);
		result.put(HierarchyConstants.GENERAL_FIELDS, generalFieldsJSONArray);

		List<Field> nodeMetadataFields = new ArrayList<>(hierarchy.getMetadataNodeFields());

		JSONArray nodeFieldsJSONArray = HierarchyUtils.createJSONArrayFromFieldsList(nodeMetadataFields, true);
		result.put(HierarchyConstants.NODE_FIELDS, nodeFieldsJSONArray);

		if (!excludeLeaf) { // add leaf fields
			List<Field> leafMetadataFields = new ArrayList<>(hierarchy.getMetadataLeafFields());

			JSONArray leafFieldsJSONArray = HierarchyUtils.createJSONArrayFromFieldsList(leafMetadataFields, true);
			result.put(HierarchyConstants.LEAF_FIELDS, leafFieldsJSONArray);
		}

		logger.debug("END");
		return result;

	}

	/**
	 * Create query for extracting relations between master and technical hierarchies
	 *
	 * @param dataSource
	 * @param paramsMap
	 * @return
	 */
	private String createQueryRelationsHierarchy(IDataSource dataSource, Map<String, Object> paramsMap) {

		// 1 - defines select clause and where informations
		String selectClause = HierarchyUtils.getRelationalColumns(dataSource);
		String hierDimensionColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.DIMENSION,
				dataSource);
		String hierNameMColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM_M, dataSource);
		String hierNodeCdMColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_CD_M, dataSource);
		String hierBackupColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BKP_COLUMN, dataSource);

		StringBuilder query = new StringBuilder(
				"SELECT DISTINCT " + selectClause + " FROM " + HierarchyConstants.REL_MASTER_TECH_TABLE_NAME + " WHERE "
						+ hierDimensionColumn + " = '" + paramsMap.get(HierarchyConstants.DIMENSION) + "' AND "
						+ hierNameMColumn + " = '" + paramsMap.get(HierarchyConstants.HIER_NM_M) + "' AND "
						+ hierNodeCdMColumn + " = '" + paramsMap.get(HierarchyConstants.NODE_CD_M) + "' AND ("
						+ hierBackupColumn + " = 0 OR " + hierBackupColumn + " IS NULL )");

		logger.debug("Query for get hierarchies relations: " + query);
		return query.toString();
	}

	/**
	 * Create HierarchyTreeNode tree from datastore with leafs informations
	 *
	 * @param dataStore
	 * @param dimension
	 * @param metadata
	 * @return
	 */
	private HierarchyTreeNode createHierarchyTreeStructure(IDataStore dataStore, String dimension, IMetaData metadata) {
		// ONLY FOR DEBUG
		Set<String> allNodeCodes = new HashSet<>();

		metadata = dataStore.getMetaData(); // saving metadata for next using

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();
		String prefix = hierarchies.getPrefix(dimension);
		Map hierConfig = hierarchies.getConfig(dimension);
		int numLevels = Integer.parseInt((String) hierConfig.get(HierarchyConstants.NUM_LEVELS));
		String rootCode = null;
		// contains the code of the last level node (not null) inserted in the tree
		IMetaData dsMeta = dataStore.getMetaData();

		HierarchyTreeNode root = null;
		Map<String, HierarchyTreeNode> attachedNodesMap = new HashMap<>();

		for (Iterator iterator = dataStore.iterator(); iterator.hasNext();) {
			String lastLevelCodeFound = null;
			String lastLevelNameFound = null;

			IRecord currRecord = (IRecord) iterator.next();
			List<IField> recordFields = currRecord.getFields();

			// MAX_DEPTH, must be equal to the level of the leaf (that we skip)
			IField maxDepthField = currRecord.getFieldAt(dsMeta.getFieldIndex(HierarchyConstants.MAX_DEPTH));
			int maxDepth = 0;
			if (maxDepthField.getValue() instanceof Integer) {
				Integer maxDepthValue = (Integer) maxDepthField.getValue();
				maxDepth = maxDepthValue;
			} else if (maxDepthField.getValue() instanceof Long) {
				Long maxDepthValue = (Long) maxDepthField.getValue();
				maxDepth = (int) (long) maxDepthValue;
			} else if (maxDepthField.getValue() instanceof java.math.BigDecimal) {
				BigDecimal maxDepthValue = (BigDecimal) maxDepthField.getValue();
				maxDepth = maxDepthValue.intValue();
			}
			logger.debug("maxDepth: " + maxDepth);

			int lastValorizedLevel = 0;

			Map<Integer, String> map = new TreeMap<>();
			for (int i = numLevels; i > 0; i--) {
				String value = (String) currRecord
						.getFieldAt(dsMeta.getFieldIndex(prefix + HierarchyConstants.SUFFIX_NM_LEV + i)).getValue();
				if (value != null && !value.isEmpty()) {
					map.put(i, value);
				}
			}

			HierarchyTreeNodeData data = null;
			IField codeField = currRecord
					.getFieldAt(dsMeta.getFieldIndex((String) hierConfig.get(HierarchyConstants.TREE_NODE_CD) + 1)); // NODE CODE
			IField nameField = currRecord
					.getFieldAt(dsMeta.getFieldIndex((String) hierConfig.get(HierarchyConstants.TREE_NODE_NM) + 1)); // NAME CODE
			String nodeCode = (String) codeField.getValue();
			String nodeName = (String) nameField.getValue();

			/* SETTING ROOT NODE */
			if (root == null) {
				// get root attribute for automatic edit node GUI
				Map rootAttrs = new HashMap();
				ArrayList<Field> generalFields = hierarchies.getHierarchy(dimension).getMetadataGeneralFields();
				for (int f = 0, lf = generalFields.size(); f < lf; f++) {
					Field fld = generalFields.get(f);
					IField fldValue = currRecord
							.getFieldAt(metadata.getFieldIndex(fld.getId() + ((fld.isSingleValue()) ? "" : 1)));
					rootAttrs.put(fld.getId(), (fld.getFixValue() != null) ? fld.getFixValue() : fldValue.getValue());
				}
				rootCode = (String) rootAttrs.get(HierarchyConstants.HIER_CD);

				nodeCode = String.valueOf(rootAttrs.get(HierarchyConstants.HIER_CD));
				nodeName = String.valueOf(rootAttrs.get(HierarchyConstants.HIER_NM));

				data = new HierarchyTreeNodeData(nodeCode, nodeName);
				root = new HierarchyTreeNode(data, rootCode, rootAttrs);

				// ONLY FOR DEBUG
				if (!allNodeCodes.contains(nodeCode)) {
					allNodeCodes.add(nodeCode);
				}
			}
			// ------------------------

			Iterator<Entry<Integer, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Integer, String> entry = it.next();

				Map mapAttrs = null;
				int i = entry.getKey();
				codeField = currRecord
						.getFieldAt(dsMeta.getFieldIndex((String) hierConfig.get(HierarchyConstants.TREE_NODE_CD) + i)); // NODE CODE
				nameField = currRecord
						.getFieldAt(dsMeta.getFieldIndex((String) hierConfig.get(HierarchyConstants.TREE_NODE_NM) + i)); // NAME CODE
				nodeCode = (String) codeField.getValue();
				nodeName = (String) nameField.getValue();
				data = new HierarchyTreeNodeData(nodeCode, nodeName);

				if (it.hasNext()) {

					// get nodes attribute for automatic edit node GUI
					ArrayList<Field> nodeFields = hierarchies.getHierarchy(dimension).getMetadataNodeFields();
					// update LEVEL && MAX_DEPTH informations
					mapAttrs = data.getAttributes();
					mapAttrs.put(HierarchyConstants.LEVEL, i);
					mapAttrs.put(HierarchyConstants.MAX_DEPTH, maxDepth);
					data.setAttributes(mapAttrs);

					for (int f = 0; f < nodeFields.size(); f++) {
						Field fld = nodeFields.get(f);
						IField fldValue = currRecord
								.getFieldAt(metadata.getFieldIndex(fld.getId() + ((fld.isSingleValue()) ? "" : i)));
						if (fld.isOrderField()) {
							Object value = (fld.getFixValue() != null) ? fld.getFixValue() : fldValue.getValue();
							logger.debug("id: [" + (fld.getId() + i) + "], value: [" + value + "]");
							mapAttrs.put(fld.getId() + i, value);
						}
						Object value = (fld.getFixValue() != null) ? fld.getFixValue() : fldValue.getValue();
						mapAttrs.put(fld.getId(), value);
						logger.debug("id: [" + (fld.getId()) + "], value: [" + value + "]");
					}
					data.setAttributes(mapAttrs);
					attachNodeToLevel(root, nodeCode, lastLevelCodeFound, lastValorizedLevel, data, allNodeCodes, false,
							currRecord, dsMeta, prefix, attachedNodesMap);
					lastValorizedLevel = i;
					lastLevelCodeFound = nodeCode;
					lastLevelNameFound = nodeName;
				} else {
					data = HierarchyUtils.setDataValues(dimension, nodeCode, data, currRecord, metadata);
					// update LEVEL informations
					mapAttrs = data.getAttributes();
					mapAttrs.put(HierarchyConstants.LEVEL, i);
					mapAttrs.put(HierarchyConstants.MAX_DEPTH, maxDepth);
					data.setAttributes(mapAttrs);
					attachNodeToLevel(root, nodeCode, lastLevelCodeFound, lastValorizedLevel, data, allNodeCodes, true,
							currRecord, dsMeta, prefix, attachedNodesMap);
					lastValorizedLevel = i;
					lastLevelCodeFound = nodeCode;
					lastLevelNameFound = nodeName;
				}

			}

		}

		if (root != null)
			// set debug mode : error is only for debug
			logger.debug(TreeString.toString(root));

		return root;

	}

	/**
	 * Attach a node as a child of another node (with key lastLevelFound that if it's null means a new record and starts from root)
	 *
	 * @param root
	 * @param nodeCode
	 * @param lastLevelFound
	 * @param data
	 * @param allNodeCodes   : codes list for debug
	 * @param currRecord
	 */
	private void attachNodeToLevel(HierarchyTreeNode root, String nodeCode, String lastLevelFound,
			int lastValorizedLevel, HierarchyTreeNodeData data, Set<String> allNodeCodes, boolean isLeaf,
			IRecord currRecord, IMetaData dsMeta, String prefix, Map<String, HierarchyTreeNode> attachedNodesMap) {

		HierarchyTreeNode aNode = null;
		HierarchyTreeNode treeNode = null;
		// first search parent node (with all path)
		Integer nodeLevel = ((Integer) data.getAttributes().get(HierarchyConstants.LEVEL));

		String recordCdLev = null;
		String recordCdNodeLevel = null;
		if (lastValorizedLevel > 0)
			recordCdLev = ((String) currRecord
					.getFieldAt(dsMeta.getFieldIndex(prefix + HierarchyConstants.SUFFIX_CD_LEV + lastValorizedLevel))
					.getValue()).trim();
		recordCdNodeLevel = ((String) currRecord
				.getFieldAt(dsMeta.getFieldIndex(prefix + HierarchyConstants.SUFFIX_CD_LEV + nodeLevel)).getValue())
						.trim();
		if (recordCdLev != null && attachedNodesMap.containsKey(recordCdLev)) {
			treeNode = attachedNodesMap.get(recordCdLev);
		} else {
			treeNode = root.getHierarchyNode(lastLevelFound, true, lastValorizedLevel, data, currRecord, dsMeta,
					prefix);
		}
		if (lastValorizedLevel < nodeLevel && !isLeaf) {
			// then check if node was already added as a child of this parent

			if (recordCdNodeLevel != null && !recordCdNodeLevel.isEmpty()
					&& !attachedNodesMap.containsKey(recordCdNodeLevel)) {
				// node not already attached to the level
				aNode = new HierarchyTreeNode(data, nodeCode);
				treeNode.add(aNode, nodeCode);
				String aNodeCdLev = (String) ((HierarchyTreeNodeData) aNode.getObject()).getAttributes()
						.get(prefix + HierarchyConstants.SUFFIX_CD_LEV);
				if (aNodeCdLev != null)
					attachedNodesMap.put(aNodeCdLev, aNode);
			}
		} else if (isLeaf) {
			// attach the leaf to the last node
			aNode = new HierarchyTreeNode(data, nodeCode);
			treeNode.add(aNode, nodeCode);
			String aNodeCdLev = (String) ((HierarchyTreeNodeData) aNode.getObject()).getAttributes()
					.get(prefix + HierarchyConstants.SUFFIX_CD_LEV);
			if (aNodeCdLev != null)
				attachedNodesMap.put(aNodeCdLev, aNode);
		}

		// ONLY FOR DEBUG
		if (!allNodeCodes.contains(nodeCode)) {
			allNodeCodes.add(nodeCode);
		}
	}

	/**
	 * Converts the tree stucture in JSON format
	 *
	 * @param root
	 * @param hierName
	 * @param dimension
	 * @return
	 */
	private JSONObject convertHierarchyTreeAsJSON(HierarchyTreeNode root, String hierName, String dimension) {
		JSONArray rootJSONObject = new JSONArray();

		if (root == null)
			return null;

		try {
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			Map hierConfig = hierarchies.getConfig(dimension);

			HierarchyTreeNodeData rootData = (HierarchyTreeNodeData) root.getObject();
			JSONArray childrenJSONArray = new JSONArray();

			String hierTp = (String) root.getAttributes().get(HierarchyConstants.HIER_TP);

			for (int i = 0; i < root.getChildCount(); i++) {
				HierarchyTreeNode childNode = root.getChild(i);
				JSONObject subTreeJSONObject = getSubTreeJSONObject(childNode, hierConfig, hierTp, hierName);
				childrenJSONArray.put(subTreeJSONObject);
			}

			rootJSONObject.put(childrenJSONArray);

			JSONObject mainObject = new JSONObject();
			mainObject.put(HierarchyConstants.TREE_NAME, root.getKey());
			mainObject.put(HierarchyConstants.ID, "root");
			mainObject.put(ALIAS_ID, HierarchyConstants.HIER_CD);
			mainObject.put(ALIAS_NAME, HierarchyConstants.HIER_NM);
			mainObject.put("root", true);
			mainObject.put(CHILDREN, childrenJSONArray);
			mainObject.put("leaf", false);
			Map rootAttrs = root.getAttributes();
			HierarchyUtils.createJSONArrayFromHashMap(rootAttrs, mainObject);

			return mainObject;

		} catch (Exception e) {
			throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchy structure", e);
		}

	}

	/**
	 * Get the JSONObject representing the tree having the passed node as a root
	 *
	 * @param node the root of the subtree
	 * @return JSONObject representing the subtree
	 */
	private JSONObject getSubTreeJSONObject(HierarchyTreeNode node, Map hierConfig, String hierTp, String hierNm) {

		try {
			HierarchyTreeNodeData nodeData = (HierarchyTreeNodeData) node.getObject();
			JSONObject nodeJSONObject = new JSONObject();
			int level = (Integer) nodeData.getAttributes().get(HierarchyConstants.LEVEL);
			int maxDepth = (Integer) nodeData.getAttributes().get(HierarchyConstants.MAX_DEPTH);

			if (node.getChildCount() > 0) {
				// if (level < maxDepth) {
				// it's a node or a leaf with the same code of the folder
				nodeJSONObject.put(HierarchyConstants.TREE_NAME, nodeData.getNodeName());
				nodeJSONObject.put(HierarchyConstants.ID, nodeData.getNodeCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_ID, nodeData.getLeafId());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_CD, nodeData.getLeafParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_NM, nodeData.getLeafParentName());
				nodeJSONObject.put(ALIAS_ID, hierConfig.get(HierarchyConstants.TREE_NODE_CD));
				nodeJSONObject.put(ALIAS_NAME, hierConfig.get(HierarchyConstants.TREE_NODE_NM));

				JSONArray childrenJSONArray = new JSONArray();

				for (int i = 0; i < node.getChildCount(); i++) {
					HierarchyTreeNode childNode = node.getChild(i);
					JSONObject subTree = getSubTreeJSONObject(childNode, hierConfig, hierTp, hierNm);
					childrenJSONArray.put(subTree);
				}
				nodeJSONObject.put(CHILDREN, childrenJSONArray);
				nodeJSONObject.put("leaf", false);

				nodeJSONObject = HierarchyUtils.setDetailsInfo(nodeJSONObject, nodeData);
				return nodeJSONObject;

			} else {
				// it's a leaf
				nodeJSONObject.put(HierarchyConstants.TREE_NAME, nodeData.getNodeName());
				nodeJSONObject.put(HierarchyConstants.ID, nodeData.getNodeCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_ID, nodeData.getLeafId());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_CD, nodeData.getLeafParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_NM, nodeData.getLeafParentName());
				nodeJSONObject.put(ALIAS_ID, hierConfig.get(HierarchyConstants.TREE_LEAF_CD));
				nodeJSONObject.put(ALIAS_NAME, hierConfig.get(HierarchyConstants.TREE_LEAF_NM));
				nodeJSONObject.put("leaf", true);

				// adds informations for propagation management
				String tpSuffix = "_" + hierTp.substring(0, 1);
				nodeJSONObject.put(HierarchyConstants.HIER_TP + tpSuffix, hierTp);
				nodeJSONObject.put(HierarchyConstants.HIER_NM + tpSuffix, hierNm);
				nodeJSONObject.put("NODE_CD" + tpSuffix, nodeData.getLeafParentCode());
				nodeJSONObject.put("NODE_NM" + tpSuffix, nodeData.getLeafParentName());
				nodeJSONObject.put("NODE_LEV" + tpSuffix, level - 1);

				nodeJSONObject = HierarchyUtils.setDetailsInfo(nodeJSONObject, nodeData);
				return nodeJSONObject;

			}
		} catch (Exception e) {
			throw new SpagoBIServiceException(
					"An unexpected error occured while serializing hierarchy structure to JSON", e);
		}

	}

	/**
	 *
	 * Persist custom hierarchy paths to database
	 *
	 * @param connection
	 * @param dataSource
	 * @param paramsMap
	 * @throws SQLException
	 */
	private void persistHierarchyPath(Connection connection, IDataSource dataSource, Map<String, Object> paramsMap)
			throws SQLException {

		try {
			Hierarchy hierarchyFields = (Hierarchy) paramsMap.get("hierarchyFields");
			String primaryKey = paramsMap.containsKey(PRIMARY_KEY) ? (String) paramsMap.get(PRIMARY_KEY) : null;
			List<HierarchyTreeNodeData> path = (List<HierarchyTreeNodeData>) paramsMap.get("path");

			// 1 - get fields structure
			List<Field> generalMetadataFields = new ArrayList<>(hierarchyFields.getMetadataGeneralFields());
			List<Field> nodeMetadataFields = new ArrayList<>(hierarchyFields.getMetadataNodeFields());
			List<Field> leafMetadataFields = new ArrayList<>(hierarchyFields.getMetadataLeafFields());

			// 2 - get total columns number
			int totalColumns = 0;
			int totalLevels = Integer.parseInt((String) paramsMap.get(HierarchyConstants.NUM_LEVELS));
			int totalGeneralFields = generalMetadataFields.size();
			int totalLeafFields = leafMetadataFields.size();
			int totalNodeFields = HierarchyUtils.getTotalNodeFieldsNumber(totalLevels, nodeMetadataFields);

			totalColumns = totalGeneralFields + totalLeafFields + totalNodeFields;
			int numLevels = totalLevels;
			// 3 - Insert prepared statement construction
			// ------------------------------------------
			Map<String, String> lstFields = new LinkedHashMap<>();
			String columns = getHierarchyColumns(dataSource, generalMetadataFields, nodeMetadataFields,
					leafMetadataFields, numLevels, lstFields);
			// get the primary key counter if is present a primary key column
			int countId = -1;
			if (primaryKey != null) {
				countId = paramsMap.containsKey(PRIMARY_KEY_COUNT) ? (int) paramsMap.get(PRIMARY_KEY_COUNT) : -1;
				countId++;
				if (!lstFields.containsKey(primaryKey)) {
					columns = columns + "," + AbstractJDBCDataset.encapsulateColumnName(primaryKey, dataSource);
					totalColumns++;
				}
				lstFields.put(primaryKey, String.valueOf(countId));
				paramsMap.put(PRIMARY_KEY_COUNT, countId);
			}

			String bufferQuery = null;
			for (int c = 0, lc = totalColumns; c < lc; c++) {
				bufferQuery += String.format("?%s", ((c < lc - 1) ? ", " : " "));
			}
			String insertQuery = String.format("insert into %s(%s) values (%s)", paramsMap.get(HIERARCHY_TABLE), columns, bufferQuery);

			// preparedStatement for insert into HIER_XXX
			PreparedStatement hierPreparedStatement = connection.prepareStatement(insertQuery);

			// 4 - Valorization of DEFUALT for prepared statement placeholders
			// -----------------------------------------------
			logger.debug("Valorization of DEFUALT for prepared statement placeholders");
			for (int i = 1; i <= lstFields.size(); i++) {
				hierPreparedStatement.setObject(i, null);
			}

			// 4 - Explore the path and set the corresponding columns for insert hier
			// -----------------------------------------------
			logger.debug("Explore the path and set the corresponding columns for insert hier");
			Map<String, Object> lstMTFieldsValue = new HashMap<>();
			String hierGeneralInfos = null;
			for (int i = 0; i < path.size(); i++) {
				HierarchyTreeNodeData node = path.get(i);
//				if (node.getNodeCode() != null && !node.getNodeCode().isEmpty() && node.getNodeCode().equals("M_CONSO2021-R-HUB") && path.size() == 10
//						&& path.get(4).getNodeCode() != null && !path.get(4).getNodeCode().isEmpty() && path.get(4).getNodeCode().equals("BU PROD E&U")
//						&&
				if (path.size() == 9 && path.get(8).getLeafId() != null && !path.get(8).getLeafId().isEmpty()
						&& Integer.valueOf(path.get(8).getLeafId()).equals(65239)) {
					boolean banana = true;
				}
				hierPreparedStatement = valorizeHierPlaceholdersFromNode(hierPreparedStatement, node, lstFields,
						paramsMap, lstMTFieldsValue);
				lstMTFieldsValue = getMTvalues(lstMTFieldsValue, node, paramsMap);
				Boolean isRoot = (Boolean) node.getAttributes().get(IS_ROOT);
				if (Boolean.TRUE.equals(isRoot)) {
					// set the general info (for mantein the optional valorized fields with the propagation)
					hierGeneralInfos = (String) node.getAttributes().get(HierarchyConstants.GENERAL_INFO_T);
				}
			}

			// 5 - Insert relations between MASTER and TECHNICAL
			// ----------------------------------------------
			logger.debug("Insert relations between MASTER and TECHNICAL");
			if (paramsMap.get(DO_PROPAGATION) != null && (boolean) paramsMap.get(DO_PROPAGATION)) {
				// adds only distinct values
				List lstRelMTInserted = (List) paramsMap.get("lstRelMTInserted");
				if (lstRelMTInserted != null
						&& !lstRelMTInserted.contains(lstMTFieldsValue.get(HierarchyConstants.PATH_CD_T))) {
					lstMTFieldsValue.put(HierarchyConstants.GENERAL_INFO_T, hierGeneralInfos);
					HierarchyUtils.persistRelationMasterTechnical(connection, lstMTFieldsValue, dataSource, paramsMap);
				}
				lstRelMTInserted.add(lstMTFieldsValue.get(HierarchyConstants.PATH_CD_T));
			}
			// force to set the primary key in statement
			if (primaryKey != null && lstFields.containsKey(primaryKey)) {
				int attrPos = HierarchyUtils.getPosField(lstFields, primaryKey);
				hierPreparedStatement.setObject(attrPos, lstFields.get(primaryKey));
			}

			// 6 - Execution of insert prepared statement
			// -----------------------------------------------
			logger.debug("Execution of insert prepared statement");
			logger.debug(insertQuery);
			hierPreparedStatement.execute();
			hierPreparedStatement.close();
		} catch (Exception e) {
			throw new SpagoBIServiceException("An unexpected error occured while persisting hierarchy structure",
					e.getMessage());
		}
	}

	/**
	 * Returns all hierarchy's columns for sql statements
	 *
	 * @param dataSource
	 * @param generalMetadataFields
	 * @param nodeMetadataFields
	 * @param leafMetadataFields
	 * @param numLevels
	 * @param lstFields
	 * @return : string with the list of the columns
	 */
	private String getHierarchyColumns(IDataSource dataSource, List<Field> generalMetadataFields,
			List<Field> nodeMetadataFields, List<Field> leafMetadataFields, int numLevels,
			Map<String, String> lstFields) {

		String toReturn = "";
		StringBuilder sbColumns = new StringBuilder();

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

		toReturn = sbColumns.toString();

		return toReturn;
	}

	/**
	 * Inserts new leaves where the user wants (selected path throught the GUI)
	 *
	 * @param connection
	 * @param dataSource
	 * @param paramsMap
	 * @param relationsMTJSONObject
	 */
	private void propagateNewLeaves(Connection connection, IDataSource dataSource, Map paramsMap,
			JSONArray relationsMTJSONObject, Hierarchy hierarchyFields) {
		logger.debug(START);

		try {
			// 1 - get fields structure
			List<Field> generalMetadataFields = new ArrayList<>(hierarchyFields.getMetadataGeneralFields());
			List<Field> nodeMetadataFields = new ArrayList<>(hierarchyFields.getMetadataNodeFields());
			List<Field> leafMetadataFields = new ArrayList<>(hierarchyFields.getMetadataLeafFields());
			int numLevels = Integer.parseInt((String) paramsMap.get(HierarchyConstants.NUM_LEVELS));

			// 2 - define insert statement with placeholders
			// -----------------------------------------------
			Map<String, String> lstFields = new LinkedHashMap<>();
			String columns = getHierarchyColumns(dataSource, generalMetadataFields, nodeMetadataFields,
					leafMetadataFields, numLevels, lstFields);
			
			String bufferQuery = null;
			for (int c = 0, lc = lstFields.size(); c < lc; c++) {
				bufferQuery += String.format("?%s", ((c < lc - 1) ? ", " : " "));
			}
			String insertQuery = String.format("insert into %s(%s) values (%s)", paramsMap.get(HIERARCHY_TABLE), columns, bufferQuery);		

			for (int i = 0; i < relationsMTJSONObject.length(); i++) {
				JSONObject relations = relationsMTJSONObject.getJSONObject(i);
				JSONObject leafData = relations.getJSONObject("leafData");
				JSONArray relationsArray = relations.getJSONArray("relationsArray");

				for (int r = 0; r < relationsArray.length(); r++) {
					// 3 - create preparedStatement for insert into HIER_XXX
					// -----------------------------------------------
					PreparedStatement hierPreparedStatement = connection.prepareStatement(insertQuery);

					// 4 - Valorize of DEFUALT for prepared statement
					// -----------------------------------------------
					for (int k = 1; k <= lstFields.size(); k++) {
						hierPreparedStatement.setObject(k, null);
					}
					// 5 - set placeholder with real values
					// -----------------------------------------------
					JSONObject relationData = (JSONObject) relationsArray.get(r);
					paramsMap.put("generalMetadataFields", generalMetadataFields);
					hierPreparedStatement = valorizeHierPlaceholdersFromRelation(hierPreparedStatement, lstFields,
							relationData, leafData, paramsMap);
					hierPreparedStatement.executeUpdate();
					hierPreparedStatement.close();
				}
			}
		} catch (JSONException je) {
			logger.error("An unexpected error occured while propaging leaves to technical hierarchies");
			throw new SpagoBIServiceException(
					"An unexpected error occured while propaging leaves to technical hierarchies", je);
		} catch (Exception e) {
			logger.error("An unexpected error occured while propaging leaves to technical hierarchies");
			throw new SpagoBIServiceException(
					"An unexpected error occured while propaging leaves to technical hierarchies", e);
		}
		logger.debug("END");
	}

	/**
	 * Set values for the preparedStatement of INSERT into the HIER_XXX
	 *
	 * @param preparedStatement
	 * @param node
	 * @param lstFields
	 * @param paramsMap
	 * @param lstMTFieldsValue
	 * @return the prepared stmt with all placeholders (?) valorized getting values from the tree node
	 * @throws SQLException
	 */
	private PreparedStatement valorizeHierPlaceholdersFromNode(PreparedStatement preparedStatement,
			HierarchyTreeNodeData node, Map lstFields, Map paramsMap, Map lstMTFieldsValue) throws SQLException {

		logger.debug("IN");
		PreparedStatement toReturn = preparedStatement;
		Map values = new HashMap();

		try {
			boolean isRoot = ((Boolean) node.getAttributes().get(IS_ROOT)).booleanValue();
			logger.debug("isRoot: " + isRoot);
			boolean isLeaf = ((Boolean) node.getAttributes().get("isLeaf")).booleanValue();
			logger.debug("isLeaf: " + isLeaf);
			String hierarchyPrefix = (String) paramsMap.get(HIERARCHY_PREFIX);
			logger.debug("hierarchyPrefix: " + hierarchyPrefix);
			int level = 0;
			String strLevel = (String) node.getAttributes().get(HierarchyConstants.LEVEL);
			level = (strLevel != null) ? Integer.parseInt(strLevel) : 0;
			logger.debug("level: " + level);
			if (level == 0 && !isRoot) {
				logger.error("Property LEVEL non found for node element with code: [" + node.getNodeCode()
						+ "] - name: [" + node.getNodeName() + "]");
				throw new SpagoBIServiceException("persistService",
						"Property LEVEL non found for node element with code " + node.getNodeCode() + " and name "
								+ node.getNodeName());
			}
			// get other node's attributes (not mandatory ie sign)
			Iterator iter = node.getAttributes().keySet().iterator();
			// String strLevel = (String) node.getAttributes().get(HierarchyConstants.LEVEL);
			// level = (strLevel != null) ? Integer.parseInt(strLevel) : 0;
			// if (level == 0 && !isRoot) {
			// logger.error("Property LEVEL non found for node element with code: [" + node.getNodeCode() + "] - name: [" + node.getNodeName() + "]");
			// throw new SpagoBIServiceException("persistService", "Property LEVEL non found for node element with code " + node.getNodeCode()
			// + " and name " + node.getNodeName());
			// }
			while (iter.hasNext()) {
				String key = (String) iter.next();
				Object value = node.getAttributes().get(key);
				if (key != null && value != null) {
					int attrPos = HierarchyUtils.getPosField(lstFields, key);
					if (attrPos == -1)
						attrPos = HierarchyUtils.getPosField(lstFields, key + level);
					if (attrPos != -1 && !values.containsKey(key)) {
						preparedStatement.setObject(attrPos, value);
						values.put(key, value);
						logger.debug("Field: [" + key + "] with value [" + value + "]");
					}
				}
			}
			if (isLeaf) {
				// it's a leaf
				toReturn.setString(
						HierarchyUtils.getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEAF),
						node.getNodeCode());
				values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEAF, node.getNodeCode());
				toReturn.setString(
						HierarchyUtils.getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF),
						node.getNodeName());
				values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF, node.getNodeName());
				if (node.getDepth() != null) {
					toReturn.setString(
							HierarchyUtils.getPosField(lstFields,
									(String) paramsMap.get(HierarchyConstants.TREE_NODE_CD) + node.getDepth()),
							node.getNodeCode());
					values.put(paramsMap.get(HierarchyConstants.TREE_NODE_CD), node.getNodeCode());
					toReturn.setString(
							HierarchyUtils.getPosField(lstFields,
									(String) paramsMap.get(HierarchyConstants.TREE_NODE_NM) + node.getDepth()),
							node.getNodeName());
					values.put(paramsMap.get(HierarchyConstants.TREE_NODE_NM), node.getNodeName());
				} else if (!isRoot) {
					logger.error("Property LEVEL non found for leaf element with code " + node.getNodeCode()
							+ " and name " + node.getNodeName());
					throw new SpagoBIServiceException("persistService",
							"Property LEVEL non found for leaf element with code " + node.getNodeCode() + " and name "
									+ node.getNodeName());
				}
				toReturn.setString(
						HierarchyUtils.getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF),
						node.getNodeName());
				values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF, node.getNodeName());
				toReturn.setObject(
						HierarchyUtils.getPosField(lstFields, hierarchyPrefix + "_" + HierarchyConstants.LEAF_ID),
						node.getLeafId());
				values.put(hierarchyPrefix + "_" + HierarchyConstants.LEAF_ID, node.getLeafId());
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.LEAF_PARENT_CD),
						node.getLeafParentCode());
				values.put(HierarchyConstants.LEAF_PARENT_CD, node.getLeafParentCode());
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.LEAF_PARENT_NM),
						node.getLeafParentName());
				values.put(HierarchyConstants.LEAF_PARENT_NM, node.getLeafParentName());
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.MAX_DEPTH),
						node.getDepth());
				values.put(HierarchyConstants.MAX_DEPTH, node.getDepth());
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.BEGIN_DT),
						node.getBeginDt(), java.sql.Types.DATE);
				values.put(HierarchyConstants.BEGIN_DT, node.getBeginDt());
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.END_DT), node.getEndDt(),
						java.sql.Types.DATE);
				values.put(HierarchyConstants.END_DT, node.getEndDt());

			}

		} catch (Exception e) {
			String errMsg = "Error while inserting element with code: [" + node.getNodeCode() + "] and name: ["
					+ node.getNodeName() + "]";
			if (values.size() > 0) {
				errMsg += " with next values: [";
				Iterator iter = values.keySet().iterator();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					Object value = values.get(key);
					errMsg += " key: " + key + " - value: " + value + ((iter.hasNext()) ? "," : "]");
				}
				logger.error(errMsg, e);
			}
			throw new SpagoBIServiceException("An unexpected error occured while persisting hierarchy structure",
					e.getMessage() + " - " + errMsg);
		}

		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Set values for the preparedStatement of propagation context getting values from the relation input
	 *
	 * @param preparedStatement
	 * @param lstFields
	 * @param relationData
	 * @param leafData
	 * @param paramsMap
	 * @return the prepared stmt on the with all placeholders (?) valorized
	 * @throws SQLException
	 */
	private PreparedStatement valorizeHierPlaceholdersFromRelation(PreparedStatement preparedStatement,
			Map<String, String> lstFields, JSONObject relationData, JSONObject leafData, Map paramsMap)
			throws SQLException {

		logger.debug("IN");
		PreparedStatement toReturn = preparedStatement;
		Map values = new HashMap();

		try {

			String prefix = (String) paramsMap.get(HIERARCHY_PREFIX);
			int maxDepth = Integer.parseInt(relationData.getString(HierarchyConstants.NODE_LEV_T)) + 1;

			// generic hierarchy informations
			String generalInfoStr = relationData.getString(HierarchyConstants.GENERAL_INFO_T);
			JSONObject generalInfoJSONObject = ObjectUtils.toJSONObject(generalInfoStr);
			ArrayList generalMetadataFields = (ArrayList) paramsMap.get("generalMetadataFields");
			for (int i = 0, l = generalMetadataFields.size(); i < l; i++) {
				Field f = (Field) generalMetadataFields.get(i);
				String key = f.getId();
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, key), generalInfoJSONObject.get(key));
				values.put(key, generalInfoJSONObject.get(key));
			}

			// leaf values (unique for all relations: XXX_CD, XXX_NM, BEGIN_DT and END_DT)
			toReturn.setString(HierarchyUtils.getPosField(lstFields, prefix + HierarchyConstants.SUFFIX_CD_LEAF),
					leafData.getString(prefix + "_CD"));
			values.put(prefix + HierarchyConstants.SUFFIX_CD_LEAF, leafData.getString(prefix + "_CD"));
			toReturn.setString(HierarchyUtils.getPosField(lstFields, prefix + HierarchyConstants.SUFFIX_NM_LEAF),
					leafData.getString(prefix + "_NM"));
			values.put(prefix + HierarchyConstants.SUFFIX_NM_LEAF, leafData.getString(prefix + "_NM"));
			toReturn.setString(HierarchyUtils.getPosField(lstFields, prefix + "_" + HierarchyConstants.LEAF_ID),
					leafData.getString(prefix + "_" + HierarchyConstants.FIELD_ID));
			values.put(prefix + "_" + HierarchyConstants.LEAF_ID,
					leafData.getString(prefix + "_" + HierarchyConstants.FIELD_ID));
			toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.BEGIN_DT),
					leafData.getString(HierarchyConstants.BEGIN_DT));
			values.put(HierarchyConstants.BEGIN_DT, leafData.getString(HierarchyConstants.BEGIN_DT));
			toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.END_DT),
					leafData.getString(HierarchyConstants.END_DT));
			values.put(HierarchyConstants.END_DT, leafData.getString(HierarchyConstants.END_DT));

			// node values
			toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.MAX_DEPTH), maxDepth);
			values.put(HierarchyConstants.MAX_DEPTH, maxDepth);

			// levels values
			String pathsCd = relationData.getString(HierarchyConstants.PATH_CD_T);
			String[] pathCd = pathsCd.split("/");
			String pathsNm = relationData.getString(HierarchyConstants.PATH_NM_T);
			String[] pathNm = pathsNm.split("/");

			int level = 0;
			for (int i = 0; i < pathCd.length; i++) {
				if (pathCd[i].equals(""))
					continue; // skip the first empty element
				level++;
				toReturn.setString(HierarchyUtils.getPosField(lstFields,
						(String) paramsMap.get(HierarchyConstants.TREE_NODE_CD) + level), pathCd[i]);
				values.put(paramsMap.get(HierarchyConstants.TREE_NODE_CD), pathCd[i]);
				toReturn.setString(HierarchyUtils.getPosField(lstFields,
						(String) paramsMap.get(HierarchyConstants.TREE_NODE_NM) + level), pathNm[i]);
				values.put(paramsMap.get(HierarchyConstants.TREE_NODE_NM), pathNm[i]);
				// if it's the last level before the leaf set parent references
				if (i == pathCd.length - 1) {
					toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.LEAF_PARENT_CD),
							pathCd[i]);
					values.put(HierarchyConstants.LEAF_PARENT_CD, pathCd[i]);
					toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.LEAF_PARENT_NM),
							pathNm[i]);
					values.put(HierarchyConstants.LEAF_PARENT_NM, pathNm[i]);
				}
			}
			// add the leaf as the last level
			toReturn.setString(
					HierarchyUtils.getPosField(lstFields,
							(String) paramsMap.get(HierarchyConstants.TREE_NODE_CD) + maxDepth),
					leafData.getString(prefix + "_CD"));
			values.put((String) paramsMap.get(HierarchyConstants.TREE_NODE_CD) + maxDepth,
					leafData.getString(prefix + "_CD"));
			toReturn.setString(
					HierarchyUtils.getPosField(lstFields,
							(String) paramsMap.get(HierarchyConstants.TREE_NODE_NM) + maxDepth),
					leafData.getString(prefix + "_NM"));
			values.put((String) paramsMap.get(HierarchyConstants.TREE_NODE_NM) + maxDepth,
					leafData.getString(prefix + "_NM"));
		} catch (Exception e) {
			String errMsg = "Error while insert for propagation of element with code: ["
					+ values.get(HierarchyConstants.NODE_CD_T) + "] and name: ["
					+ values.get(HierarchyConstants.NODE_NM_T) + "]";
			if (values.size() > 0) {
				errMsg += " with next values: [";
				Iterator iter = values.keySet().iterator();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					Object value = values.get(key);
					errMsg += " key: " + key + " - value: " + value + ((iter.hasNext()) ? "," : "]");
				}
				logger.error(errMsg, e);
			}
			throw new SpagoBIServiceException("An unexpected error occured while persisting hierarchy structure: ",
					e.getMessage() + " - " + e.getCause() + " - " + errMsg);
		}

		logger.debug("OUT");
		return toReturn;
	}

	/**
	 * Returns an hashmap with all relational values
	 *
	 * @param lstMTFieldsValue
	 * @param node
	 * @param paramsMap
	 * @return hashmap
	 */
	private Map getMTvalues(Map<String, Object> lstMTFieldsValue, HierarchyTreeNodeData node, Map paramsMap) {

		Map<String, Object> values = lstMTFieldsValue;
		if (values == null)
			values = new HashMap<>();

		// get level : is null if the node is the root
		Integer level = (node.getAttributes().get(HierarchyConstants.LEVEL) != null)
				? Integer.valueOf((String) node.getAttributes().get(HierarchyConstants.LEVEL))
				: null;
		Integer maxDepth = (node.getAttributes().get(HierarchyConstants.MAX_DEPTH) != null)
				? Integer.valueOf((String) node.getAttributes().get(HierarchyConstants.MAX_DEPTH))
				: null;

		boolean isLeaf = (level != null && maxDepth != null && level.compareTo(maxDepth) == 0) ? true : false;

		if (node.getAttributes().get(HierarchyConstants.HIER_NM_M) != null) {
			values.put(HierarchyConstants.DIMENSION, paramsMap.get(DIMENSION));
			values.put(HierarchyConstants.HIER_CD_T, paramsMap.get("hierTargetCode"));
			values.put(HierarchyConstants.HIER_NM_T, paramsMap.get("hierTargetName"));
			values.put(HierarchyConstants.NODE_CD_T, node.getLeafParentCode());
			values.put(HierarchyConstants.NODE_NM_T, node.getLeafParentName());
			values.put(HierarchyConstants.NODE_LEV_T, level - 1);
			values.put(HierarchyConstants.HIER_CD_M, paramsMap.get(HIER_SOURCE_CODE));
			values.put(HierarchyConstants.HIER_NM_M, paramsMap.get(HIER_SOURCE_NAME));
			values.put(HierarchyConstants.NODE_CD_M, node.getAttributes().get(HierarchyConstants.NODE_CD_M));
			values.put(HierarchyConstants.NODE_NM_M, node.getAttributes().get(HierarchyConstants.NODE_NM_M));
			values.put(HierarchyConstants.NODE_LEV_M, node.getAttributes().get(HierarchyConstants.NODE_LEV_M));
		}
		if (!isLeaf && level != null) {
			// update complete path references
			String pathCodes = (values.get(HierarchyConstants.PATH_CD_T) != null)
					? (String) values.get(HierarchyConstants.PATH_CD_T)
					: "";
			pathCodes += "/" + node.getNodeCode();
			values.put(HierarchyConstants.PATH_CD_T, pathCodes);

			String pathNames = (values.get(HierarchyConstants.PATH_NM_T) != null)
					? (String) values.get(HierarchyConstants.PATH_NM_T)
					: "";
			pathNames += "/" + node.getNodeName();
			values.put(HierarchyConstants.PATH_NM_T, pathNames);
		}
		return values;

	}

	/**
	 * Find all paths from root to leaves
	 *
	 * @param node
	 * @param dimension
	 * @return collection with all tree paths
	 */
	private Collection<List<HierarchyTreeNodeData>> findRootToLeavesPaths(JSONObject node, String dimension,
			String uniqueCode, String hierName, int position) {
		Collection<List<HierarchyTreeNodeData>> collectionOfPaths = new HashSet<>();
		try {
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyPrefix = hierarchies.getPrefix(dimension);
			Map<String, Object> hierConfig = hierarchies.getConfig(dimension);
			String uniqueCodeLocal = uniqueCode;

			String nodeName = node.getString(HierarchyConstants.TREE_NAME);
			String nodeCode = node.getString(HierarchyConstants.ID);
			Map mapAttrs = new HashMap();

			// current node is a root?
			boolean isRoot = (node.isNull("root")) ? false : node.getBoolean("root");
			mapAttrs.put(IS_ROOT, isRoot);

			// current node is a leaf?
			boolean isLeaf = node.getBoolean("leaf");
			mapAttrs.put("isLeaf", isLeaf);

			if (!node.isNull(HierarchyConstants.LEVEL)) {
				mapAttrs.put(HierarchyConstants.LEVEL, node.getString(HierarchyConstants.LEVEL));
			}
			if (!node.isNull(HierarchyConstants.ID)
					&& node.getString(HierarchyConstants.ID).equals(HierarchyConstants.ROOT)) {
				hierName = uniqueCode;
			}

			// add other general attributes if they are valorized
			JSONObject generalInfoJSON = new JSONObject();
			JSONObject infoJSON = new JSONObject();
			ArrayList<Field> generalFields = hierarchies.getHierarchy(dimension).getMetadataGeneralFields();
			for (int f = 0, lf = generalFields.size(); f < lf; f++) {
				Field fld = generalFields.get(f);
				String idFld = fld.getId();
				if (!node.isNull(idFld)) {
					mapAttrs.put(idFld, node.getString(idFld));
					generalInfoJSON.put(idFld, node.getString(idFld));
				}
			}

			String orderField = null;
			// add other node attributes if they are valorized
			ArrayList<Field> nodeFields = hierarchies.getHierarchy(dimension).getMetadataNodeFields();
			if (!isRoot) {
				for (int f = 0, lf = nodeFields.size(); f < lf; f++) {
					Field fld = nodeFields.get(f);
					String idFld = fld.getId();
					// if the column must be unique, get the MD5 value otherwise set value if it isn't null
					if (Boolean.parseBoolean((String) hierConfig.get(HierarchyConstants.UNIQUE_NODE))
							&& fld.isUniqueCode()) {
						// generate hashcode
						uniqueCodeLocal = uniqueCodeLocal + nodeName;
						String hashCode = Helper.sha256(uniqueCodeLocal);
						mapAttrs.put(idFld, hashCode);
					} else if (!node.isNull(idFld)) {
						mapAttrs.put(idFld, node.getString(idFld));
					}
					if (!isLeaf && position < 1 && fld.isOrderField()) {
						// deletes eventual order field without level specification
						if (mapAttrs.containsKey(idFld)) {
							mapAttrs.remove(idFld);
						}
						orderField = idFld + node.getString(HierarchyConstants.LEVEL);
						mapAttrs.put(orderField, position);
					}
				}
			}
			// add other leaf attributes if they are valorized
			ArrayList<Field> leafFields = hierarchies.getHierarchy(dimension).getMetadataLeafFields();
			for (int f = 0, lf = leafFields.size(); f < lf; f++) {
				Field fld = leafFields.get(f);
				String idFld = fld.getId();
				if (!node.isNull(idFld)) {
					mapAttrs.put(idFld, node.getString(idFld));
				}
			}

			// adds as attributes properties about MASTER references (propagation management)
			if (!node.isNull(HierarchyConstants.HIER_CD_M))
				mapAttrs.put(HierarchyConstants.HIER_CD_M, node.getString(HierarchyConstants.HIER_CD_M));
			if (!node.isNull(HierarchyConstants.HIER_NM_M))
				mapAttrs.put(HierarchyConstants.HIER_NM_M, node.getString(HierarchyConstants.HIER_NM_M));
			if (!node.isNull(HierarchyConstants.NODE_CD_M))
				mapAttrs.put(HierarchyConstants.NODE_CD_M, node.getString(HierarchyConstants.NODE_CD_M));
			if (!node.isNull(HierarchyConstants.NODE_NM_M))
				mapAttrs.put(HierarchyConstants.NODE_NM_M, node.getString(HierarchyConstants.NODE_NM_M));
			if (!node.isNull(HierarchyConstants.NODE_LEV_M))
				mapAttrs.put(HierarchyConstants.NODE_LEV_M, node.getString(HierarchyConstants.NODE_LEV_M));
			if (generalInfoJSON.length() > 0)
				mapAttrs.put(HierarchyConstants.GENERAL_INFO_T, generalInfoJSON.toString());

			String nodeLeafId = !node.isNull(HierarchyConstants.LEAF_ID) ? node.getString(HierarchyConstants.LEAF_ID)
					: "";
			if (nodeLeafId.equals("")) {
				nodeLeafId = (mapAttrs.get(hierarchyPrefix + "_" + HierarchyConstants.LEAF_ID) != null)
						? (String) mapAttrs.get(hierarchyPrefix + "_" + HierarchyConstants.LEAF_ID)
						: "";
			}
			if (nodeLeafId.equals("") && !node.isNull(hierarchyPrefix + "_" + HierarchyConstants.FIELD_ID)) {
				nodeLeafId = node.getString(hierarchyPrefix + "_" + HierarchyConstants.FIELD_ID); // dimension id (ie: ACCOUNT_ID)
			}
			// create node
			HierarchyTreeNodeData nodeData = new HierarchyTreeNodeData(nodeCode, nodeName, nodeLeafId, "", "", "",
					mapAttrs);

			if (isLeaf) {
				// reset unique key
				uniqueCodeLocal = hierName;

				List<HierarchyTreeNodeData> aPath = new ArrayList<>();
				if (!node.isNull(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEAF))
					nodeData.setNodeCode(node.getString(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEAF));
				if (!node.isNull(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF))
					nodeData.setNodeName(node.getString(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF));
				if (!node.isNull(HierarchyConstants.BEGIN_DT)) {
					// String beginDate = node.getString(HierarchyConstants.BEGIN_DT);
					// SimpleDateFormat formatter = new SimpleDateFormat();
					// nodeData.setBeginDt(new Date(formatter.parse(beginDate).getTime()));
					nodeData.setBeginDt(Date.valueOf(node.getString(HierarchyConstants.BEGIN_DT)));
				}
				if (!node.isNull(HierarchyConstants.END_DT)) {
					// String endDate = node.getString(HierarchyConstants.END_DT);
					// SimpleDateFormat formatter = new SimpleDateFormat();
					// nodeData.setEndDt(new Date(formatter.parse(endDate).getTime()));
					nodeData.setEndDt(Date.valueOf(node.getString(HierarchyConstants.END_DT)));
				}
				// set parent informations
				String nodeParentCode = null;
				if (!node.isNull(HierarchyConstants.LEAF_PARENT_CD))
					nodeParentCode = node.getString(HierarchyConstants.LEAF_PARENT_CD);
				nodeData.setLeafParentCode(nodeParentCode);
				nodeData.setLeafParentName(node.getString(HierarchyConstants.LEAF_PARENT_NM));
				nodeData.setDepth(node.getString(HierarchyConstants.LEVEL));
				aPath.add(nodeData);
				collectionOfPaths.add(aPath);
				return collectionOfPaths;
			} else {
				// node has children
				JSONArray childs = node.getJSONArray(CHILDREN);
				for (int i = 0; i < childs.length(); i++) {
					JSONObject child = childs.getJSONObject(i);
					if (orderField != null) {
						child.put(orderField, i);
					}
					String parentUniqueCode = "";
					if (!isRoot) {
						parentUniqueCode = uniqueCodeLocal;
					}
					// 0-i is used to store the inverse order nodes, this solution is used to put the leaves (order index = null) to the end of array when is
					// executed the 'ORDER BY field DESC'. Storing the nodes with negative num and using DESC, the final order will be ascendent [0 -1 -2..NULL]
					Collection<List<HierarchyTreeNodeData>> childPaths = findRootToLeavesPaths(child, dimension,
							uniqueCodeLocal, hierName, 0 - i);

					for (List<HierarchyTreeNodeData> path : childPaths) {
						// add this node to start of the path
						path.add(0, nodeData);
						collectionOfPaths.add(path);
						uniqueCodeLocal = parentUniqueCode;
					}
				}
			}
			return collectionOfPaths;
		} catch (JSONException je) {
			logger.error("An unexpected error occured while retriving hierarchy root-leafs paths");
			throw new SpagoBIServiceException(
					"An unexpected error occured while retriving custom hierarchy root-leafs paths", je);
		} catch (Exception e) {
			logger.error("An unexpected error occured while retriving hierarchy root-leafs paths");
			throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchy root-leafs paths",
					e);
		}

	}
}
