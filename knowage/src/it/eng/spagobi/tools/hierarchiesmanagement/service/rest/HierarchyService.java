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
import it.eng.spagobi.utilities.rest.RestUtilities;

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
import java.util.Set;

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
 * This class contains all REST services used by all hierarchy types (master and technical)
 */

@Path("/hierarchies")
public class HierarchyService {

	static private Logger logger = Logger.getLogger(HierarchyService.class);

	// get automatic hierarchy structure for tree visualization
	@GET
	@Path("/getHierarchyTree")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchyTree(@QueryParam("dimension") String dimension, @QueryParam("filterType") String hierarchyType,
			@QueryParam("filterHierarchy") String hierarchyName, @QueryParam("validityDate") String hierarchyDate,
			@QueryParam("filterDimension") String filterDimension, @QueryParam("filterDate") String filterDate, @QueryParam("optionDate") String optionDate,
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

			// 2 - get datastore with all hierachies' leafs
			IMetaData metadata = null;
			boolean excludeDimLeaf = (filterDimension != null || optionHierarchy != null) ? true : false;
			IDataStore dataStore = HierarchyUtils.getHierarchyDataStore(dataSource, dimension, hierarchyType, hierarchyName, hierarchyDate, filterDate,
					filterDimension, optionDate, optionHierarchy, optionHierType, excludeDimLeaf);

			// 4 - Create ADT for Tree from datastore
			hierarchyTree = createHierarchyTreeStructure(dataStore, dimension, metadata);
			treeJSONObject = convertHierarchyTreeAsJSON(hierarchyTree, hierarchyName, dimension);

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
		Connection connection = null;
		try {

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			HashMap<String, Object> paramsMap = new HashMap<String, Object>();

			String validityDate = (!requestVal.isNull("dateValidity")) ? requestVal.getString("dateValidity") : null;
			paramsMap.put("validityDate", validityDate);
			boolean doBackup = (!requestVal.isNull("doBackup")) ? requestVal.getBoolean("doBackup") : new Boolean("false");
			paramsMap.put("doBackup", doBackup);
			boolean isInsert = Boolean.valueOf(req.getParameter("isInsert"));
			paramsMap.put("isInsert", isInsert);
			String dimension = requestVal.getString("dimension");
			paramsMap.put("dimension", dimension);
			String hierSourceCode = (!requestVal.isNull("hierSourceCode")) ? requestVal.getString("hierSourceCode") : null;
			paramsMap.put("hierSourceCode", hierSourceCode);
			String hierSourceName = (!requestVal.isNull("hierSourceName")) ? requestVal.getString("hierSourceName") : null;
			paramsMap.put("hierSourceName", hierSourceName);
			String hierSourceType = (!requestVal.isNull("hierSourceType")) ? requestVal.getString("hierSourceType") : null;
			paramsMap.put("hierSourceType", hierSourceType);

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
			paramsMap.put("hierarchyTable", hierarchyTable);
			String hierarchyPrefix = hierarchies.getPrefix(dimension);
			paramsMap.put("hierarchyPrefix", hierarchyPrefix);
			String hierarchyFK = hierarchies.getHierarchyTableForeignKeyName(dimension);
			paramsMap.put("hierarchyFK", hierarchyFK);
			Hierarchy hierarchyFields = hierarchies.getHierarchy(dimension);
			HashMap<String, Object> hierConfig = hierarchies.getConfig(dimension);
			paramsMap.put(HierarchyConstants.NUM_LEVELS, hierConfig.get(HierarchyConstants.NUM_LEVELS));

			// 2 - Definition of the context (ex. manage propagations ONLY when the sourceHierType is MASTER and the targtHierType is TECHNICAL)
			boolean doPropagation = false;
			if (hierSourceType != null && hierSourceType.equals(HierarchyConstants.HIER_TP_MASTER) && hierTargetType != null
					&& hierTargetType.equals(HierarchyConstants.HIER_TP_TECHNICAL)) {
				doPropagation = true;
				paramsMap.put("doPropagation", doPropagation);
			}

			// 3 - get all paths from the input json tree
			Collection<List<HierarchyTreeNodeData>> paths = findRootToLeavesPaths(rootJSONObject, dimension);

			// 4 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy", "No datasource found for saving hierarchy");
			}

			// get one ONLY connection for all statements (transactional logic)
			connection = dataSource.getConnection();
			connection.setAutoCommit(false);

			if (!isInsert && doBackup) {
				HierarchyUtils.updateHierarchyForBackup(dataSource, connection, paramsMap);
			} else if (!isInsert && !doBackup) {
				HierarchyUtils.deleteHierarchy(dimension, hierSourceName, dataSource, connection);
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
	public String deleteHierarchy(@Context HttpServletRequest req) throws SQLException {
		// delete hierarchy
		Connection connection = null;
		try {

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			String dimension = requestVal.getString("dimension");
			String hierarchyName = requestVal.getString("name");

			// 1 - get datasource label name
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);

			// 2 - Execute DELETE
			connection = dataSource.getConnection();
			HierarchyUtils.deleteHierarchy(dimension, hierarchyName, dataSource, connection);

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

	@GET
	@Path("/getRelationsMasterTechnical")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getRelationsMasterTechnical(@QueryParam("dimension") String dimension, @QueryParam("hierSourceCode") String hierSourceCode,
			@QueryParam("hierSourceName") String hierSourceName, @QueryParam("nodeSourceCode") String nodeSourceCode) throws SQLException {
		// get relations between master and technical nodes
		Connection connection = null;
		JSONObject result = new JSONObject();
		try {
			HashMap<String, Object> paramsMap = new HashMap<String, Object>();
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

		} catch (Throwable t) {
			logger.error("An unexpected error occured while deleting custom hierarchy");
			throw new SpagoBIServiceException("An unexpected error occured while deleting custom hierarchy", t);
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
	 * Create query for extracting relations between master and technical hierarchies
	 *
	 * @param dataSource
	 * @param paramsMap
	 * @return
	 */
	private String createQueryRelationsHierarchy(IDataSource dataSource, HashMap<String, Object> paramsMap) {

		// 1 - defines select clause and where informations
		String selectClause = HierarchyUtils.getRelationalColumns(dataSource);
		String hierDimensionColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.DIMENSION, dataSource);
		String hierNameMColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.HIER_NM_M, dataSource);
		String hierNodeCdMColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.NODE_CD_M, dataSource);
		String hierBackupColumn = AbstractJDBCDataset.encapsulateColumnName(HierarchyConstants.BKP_COLUMN, dataSource);

		// select distinct HIER_CD_T, HIER_NM_T, DIMENSION, NODE_CD_T, NODE_NM_T, NODE_LEV_T, HIER_CD_M, HIER_NM_M, NODE_CD_M, NODE_NM_M, NODE_LEV_M
		// from HIER_MASTER_TECHNICAL where HIER_NM_M = 'BPC_PATR' and NODE_CD_M = 'PAS_TP_PC_AP_PRE' and BACKUP = 0
		StringBuffer query = new StringBuffer("SELECT DISTINCT " + selectClause + " FROM " + HierarchyConstants.REL_MASTER_TECH_TABLE_NAME + " WHERE "
				+ hierDimensionColumn + " = \"" + paramsMap.get(HierarchyConstants.DIMENSION) + "\" AND " + hierNameMColumn + " = \""
				+ paramsMap.get(HierarchyConstants.HIER_NM_M) + "\" AND " + hierNodeCdMColumn + " = \"" + paramsMap.get(HierarchyConstants.NODE_CD_M)
				+ "\" AND " + hierBackupColumn + " = 0 ");

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
		Set<String> allNodeCodes = new HashSet<String>();

		HierarchyTreeNode root = null;

		metadata = dataStore.getMetaData(); // saving metadata for next using

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();
		String prefix = hierarchies.getPrefix(dimension);
		HashMap hierConfig = hierarchies.getConfig(dimension);
		int numLevels = Integer.parseInt((String) hierConfig.get(HierarchyConstants.NUM_LEVELS));
		String rootCode = null;
		// contains the code of the last level node (not null) inserted in the tree
		IMetaData dsMeta = dataStore.getMetaData();

		for (Iterator iterator = dataStore.iterator(); iterator.hasNext();) {
			String lastLevelCodeFound = null;
			String lastLevelNameFound = null;

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
			int lastValorizedLevel = 0;

			for (int i = 1, l = numLevels; i <= l; i++) {
				IField codeField = record.getFieldAt(dsMeta.getFieldIndex(prefix + HierarchyConstants.SUFFIX_CD_LEV + i)); // NODE CODE
				IField nameField = record.getFieldAt(dsMeta.getFieldIndex(prefix + HierarchyConstants.SUFFIX_NM_LEV + i)); // NAME CODE
				IField codeLeafField = record.getFieldAt(dsMeta.getFieldIndex(prefix + HierarchyConstants.SUFFIX_CD_LEAF)); // LEAF CODE
				String leafCode = (String) codeLeafField.getValue();

				if (currentLevel == maxDepth) {
					break; // skip to next iteration
				} else if (codeField.getValue() == null || codeField.getValue().equals("")) {
					// do nothing: it's an empty node
					// lastValorizedLevel++;
				} else {
					String nodeCode = (String) codeField.getValue();
					String nodeName = (String) nameField.getValue();
					HierarchyTreeNodeData data = new HierarchyTreeNodeData(nodeCode, nodeName);
					// ONLY FOR DEBUG
					if (!allNodeCodes.contains(nodeCode)) {
						allNodeCodes.add(nodeCode);
					}
					// ------------------------

					// update LEVEL && MAX_DEPTH informations
					HashMap mapAttrs = data.getAttributes();
					mapAttrs.put(HierarchyConstants.LEVEL, i);
					mapAttrs.put(HierarchyConstants.MAX_DEPTH, maxDepth);
					data.setAttributes(mapAttrs);

					if (root == null) {
						// get root attribute for automatic edit node GUI
						HashMap rootAttrs = new HashMap();
						ArrayList<Field> generalFields = hierarchies.getHierarchy(dimension).getMetadataGeneralFields();
						for (int f = 0, lf = generalFields.size(); f < lf; f++) {
							Field fld = generalFields.get(f);
							IField fldValue = record.getFieldAt(metadata.getFieldIndex(fld.getId() + ((fld.isSingleValue()) ? "" : i)));
							rootAttrs.put(fld.getId(), (fld.getFixValue() != null) ? fld.getFixValue() : fldValue.getValue());
						}
						rootCode = (String) rootAttrs.get(HierarchyConstants.HIER_CD);
						root = new HierarchyTreeNode(data, rootCode, rootAttrs);

						// ONLY FOR DEBUG
						if (!allNodeCodes.contains(nodeCode)) {
							allNodeCodes.add(nodeCode);
						}
						// ------------------------
					}
					// check if its a leaf
					if (i == maxDepth) {
						data = HierarchyUtils.setDataValues(dimension, nodeCode, data, record, metadata);
						// update LEVEL informations
						mapAttrs = data.getAttributes();
						mapAttrs.put(HierarchyConstants.LEVEL, i);
						mapAttrs.put(HierarchyConstants.MAX_DEPTH, maxDepth);
						data.setAttributes(mapAttrs);
						attachNodeToLevel(root, nodeCode, lastLevelCodeFound, lastValorizedLevel, data, allNodeCodes);
						lastValorizedLevel++;
						lastLevelCodeFound = nodeCode;
						lastLevelNameFound = nodeName;
						break;
					} else if (!root.getKey().contains(nodeCode)) {
						// get nodes attribute for automatic edit node GUI
						ArrayList<Field> nodeFields = hierarchies.getHierarchy(dimension).getMetadataNodeFields();
						for (int f = 0, lf = nodeFields.size(); f < lf; f++) {
							Field fld = nodeFields.get(f);
							IField fldValue = record.getFieldAt(metadata.getFieldIndex(fld.getId() + ((fld.isSingleValue()) ? "" : i)));
							mapAttrs.put(fld.getId(), (fld.getFixValue() != null) ? fld.getFixValue() : fldValue.getValue());
						}
						data.setAttributes(mapAttrs);
						attachNodeToLevel(root, nodeCode, lastLevelCodeFound, lastValorizedLevel, data, allNodeCodes);
					}
					lastValorizedLevel++;
					lastLevelCodeFound = nodeCode;
					lastLevelNameFound = nodeName;
				}
				currentLevel++;
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
	 * @param allNodeCodes
	 *            : codes list for debug
	 */
	private void attachNodeToLevel(HierarchyTreeNode root, String nodeCode, String lastLevelFound, int lastValorizedLevel, HierarchyTreeNodeData data,
			Set<String> allNodeCodes) {

		HierarchyTreeNode treeNode = null;
		// first search parent node (with all path)
		Integer nodeLevel = ((Integer) data.getAttributes().get(HierarchyConstants.LEVEL));
		treeNode = root.getHierarchyNode(lastLevelFound, true, lastValorizedLevel);
		if ((lastValorizedLevel + 1) == nodeLevel) {
			// then check if node was already added as a child of this parent
			if (!treeNode.getChildrensKeys().contains(nodeCode)) {
				// node not already attached to the level
				HierarchyTreeNode aNode = new HierarchyTreeNode(data, nodeCode);
				treeNode.add(aNode, nodeCode);
			}
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
			HashMap hierConfig = hierarchies.getConfig(dimension);

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
			mainObject.put("aliasId", HierarchyConstants.HIER_CD);
			mainObject.put("aliasName", HierarchyConstants.HIER_NM);
			mainObject.put("root", true);
			mainObject.put("children", childrenJSONArray);
			mainObject.put("leaf", false);
			HashMap rootAttrs = root.getAttributes();
			HierarchyUtils.createJSONArrayFromHashMap(rootAttrs, mainObject);

			return mainObject;

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchy structure", t);
		}

	}

	/**
	 * Get the JSONObject representing the tree having the passed node as a root
	 *
	 * @param node
	 *            the root of the subtree
	 * @return JSONObject representing the subtree
	 */
	private JSONObject getSubTreeJSONObject(HierarchyTreeNode node, HashMap hierConfig, String hierTp, String hierNm) {

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
				nodeJSONObject.put("aliasId", hierConfig.get(HierarchyConstants.TREE_NODE_CD));
				nodeJSONObject.put("aliasName", hierConfig.get(HierarchyConstants.TREE_NODE_NM));

				JSONArray childrenJSONArray = new JSONArray();

				for (int i = 0; i < node.getChildCount(); i++) {
					HierarchyTreeNode childNode = node.getChild(i);
					JSONObject subTree = getSubTreeJSONObject(childNode, hierConfig, hierTp, hierNm);
					childrenJSONArray.put(subTree);
				}
				nodeJSONObject.put("children", childrenJSONArray);
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
				nodeJSONObject.put("aliasId", hierConfig.get(HierarchyConstants.TREE_LEAF_CD));
				nodeJSONObject.put("aliasName", hierConfig.get(HierarchyConstants.TREE_LEAF_NM));
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
		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while serializing hierarchy structure to JSON", t);
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
	private void persistHierarchyPath(Connection connection, IDataSource dataSource, HashMap<String, Object> paramsMap) throws SQLException {

		try {
			Hierarchy hierarchyFields = (Hierarchy) paramsMap.get("hierarchyFields");
			List<HierarchyTreeNodeData> path = (List<HierarchyTreeNodeData>) paramsMap.get("path");

			// 1 - get fields structure
			List<Field> generalMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataGeneralFields());
			List<Field> nodeMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataNodeFields());
			List<Field> leafMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataLeafFields());

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
			LinkedHashMap<String, String> lstFields = new LinkedHashMap<String, String>();
			String columns = getHierarchyColumns(dataSource, generalMetadataFields, nodeMetadataFields, leafMetadataFields, numLevels, lstFields);

			String insertQuery = "insert into " + (String) paramsMap.get("hierarchyTable") + "(" + columns + ") values (";
			for (int c = 0, lc = totalColumns; c < lc; c++) {
				insertQuery += "?" + ((c < lc - 1) ? ", " : " ");
			}
			insertQuery += ")";

			// preparedStatement for insert into HIER_XXX
			PreparedStatement hierPreparedStatement = connection.prepareStatement(insertQuery);

			// 4 - Valorization of DEFUALT for prepared statement placeholders
			// -----------------------------------------------
			for (int i = 1; i <= lstFields.size(); i++) {
				hierPreparedStatement.setObject(i, null);
			}

			// 4 - Explore the path and set the corresponding columns for insert hier
			// -----------------------------------------------
			HashMap<String, Object> lstMTFieldsValue = new HashMap<String, Object>();
			String hierGeneralInfos = null;
			for (int i = 0; i < path.size(); i++) {
				HierarchyTreeNodeData node = path.get(i);
				hierPreparedStatement = valorizeHierPlaceholdersFromNode(hierPreparedStatement, node, lstFields, paramsMap, lstMTFieldsValue);
				lstMTFieldsValue = getMTvalues(lstMTFieldsValue, node, paramsMap);
				Boolean isRoot = (Boolean) node.getAttributes().get("isRoot");
				if (isRoot) {
					// set the general info (for mantein the optional valorized fields with the propagation)
					hierGeneralInfos = (String) node.getAttributes().get(HierarchyConstants.GENERAL_INFO_T);
				}
			}

			// 5 - Insert relations between MASTER and TECHNICAL
			// ----------------------------------------------
			if (paramsMap.get("doPropagation") != null && (boolean) paramsMap.get("doPropagation")) {
				// adds only distinct values
				List lstRelMTInserted = (List) paramsMap.get("lstRelMTInserted");
				if (lstRelMTInserted != null && !lstRelMTInserted.contains(lstMTFieldsValue.get(HierarchyConstants.PATH_CD_T))) {
					lstMTFieldsValue.put(HierarchyConstants.GENERAL_INFO_T, hierGeneralInfos);
					HierarchyUtils.persistRelationMasterTechnical(connection, lstMTFieldsValue, dataSource, paramsMap);
				}
				lstRelMTInserted.add(lstMTFieldsValue.get(HierarchyConstants.PATH_CD_T));
			}

			// 6 - Execution of insert prepared statement
			// -----------------------------------------------
			hierPreparedStatement.executeUpdate();
			hierPreparedStatement.close();
		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while persisting hierarchy structure", t.getMessage());
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
	private String getHierarchyColumns(IDataSource dataSource, List<Field> generalMetadataFields, List<Field> nodeMetadataFields,
			List<Field> leafMetadataFields, int numLevels, LinkedHashMap<String, String> lstFields) {

		String toReturn = "";
		StringBuffer sbColumns = new StringBuffer();

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
	private void propagateNewLeaves(Connection connection, IDataSource dataSource, HashMap paramsMap, JSONArray relationsMTJSONObject, Hierarchy hierarchyFields) {
		logger.debug("START");

		try {
			// 1 - get fields structure
			List<Field> generalMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataGeneralFields());
			List<Field> nodeMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataNodeFields());
			List<Field> leafMetadataFields = new ArrayList<Field>(hierarchyFields.getMetadataLeafFields());
			int numLevels = Integer.valueOf((String) paramsMap.get(HierarchyConstants.NUM_LEVELS));

			// 2 - define insert statement with placeholders
			// -----------------------------------------------
			LinkedHashMap<String, String> lstFields = new LinkedHashMap<String, String>();
			String columns = getHierarchyColumns(dataSource, generalMetadataFields, nodeMetadataFields, leafMetadataFields, numLevels, lstFields);
			String insertQuery = "insert into " + (String) paramsMap.get("hierarchyTable") + "(" + columns + ") values (";

			for (int c = 0, lc = lstFields.size(); c < lc; c++) {
				insertQuery += "?" + ((c < lc - 1) ? ", " : " ");
			}
			insertQuery += ")";

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
					hierPreparedStatement = valorizeHierPlaceholdersFromRelation(hierPreparedStatement, lstFields, relationData, leafData, paramsMap);
					hierPreparedStatement.executeUpdate();
					hierPreparedStatement.close();
				}
			}
		} catch (JSONException je) {
			logger.error("An unexpected error occured while propaging leaves to technical hierarchies");
			throw new SpagoBIServiceException("An unexpected error occured while propaging leaves to technical hierarchies", je);
		} catch (Throwable t) {
			logger.error("An unexpected error occured while propaging leaves to technical hierarchies");
			throw new SpagoBIServiceException("An unexpected error occured while propaging leaves to technical hierarchies", t);
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
	private PreparedStatement valorizeHierPlaceholdersFromNode(PreparedStatement preparedStatement, HierarchyTreeNodeData node, LinkedHashMap lstFields,
			HashMap paramsMap, HashMap lstMTFieldsValue) throws SQLException {

		PreparedStatement toReturn = preparedStatement;
		HashMap values = new HashMap();

		try {
			boolean isRoot = ((Boolean) node.getAttributes().get("isRoot")).booleanValue();
			boolean isLeaf = ((Boolean) node.getAttributes().get("isLeaf")).booleanValue();
			String hierarchyPrefix = (String) paramsMap.get("hierarchyPrefix");

			if (isLeaf) {
				// it's a leaf
				toReturn.setString(HierarchyUtils.getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEAF), node.getNodeCode());
				values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEAF, node.getNodeCode());
				toReturn.setString(HierarchyUtils.getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF), node.getNodeName());
				values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF, node.getNodeName());
				if (node.getDepth() != null) {
					toReturn.setString(HierarchyUtils.getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV + node.getDepth()),
							node.getNodeCode());
					values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV, node.getNodeCode());
				}
				if (node.getDepth() != null) {
					toReturn.setString(HierarchyUtils.getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEV + node.getDepth()),
							node.getNodeName());
					values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV, node.getNodeName());
				} else if (!isRoot) {
					logger.error("Property LEVEL non found for leaf element with code " + node.getNodeCode() + " and name " + node.getNodeName());
					throw new SpagoBIServiceException("persistService", "Property LEVEL non found for leaf element with code " + node.getNodeCode()
							+ " and name " + node.getNodeName());
				}
				toReturn.setString(HierarchyUtils.getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF), node.getNodeName());
				values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF, node.getNodeName());
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, hierarchyPrefix + "_" + HierarchyConstants.LEAF_ID), node.getLeafId());
				values.put(hierarchyPrefix + "_" + HierarchyConstants.LEAF_ID, node.getLeafId());
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.LEAF_PARENT_CD), node.getLeafParentCode());
				values.put(HierarchyConstants.LEAF_PARENT_CD, node.getLeafParentCode());
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.LEAF_PARENT_NM), node.getLeafParentName());
				values.put(HierarchyConstants.LEAF_PARENT_NM, node.getLeafParentName());
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.MAX_DEPTH), node.getDepth());
				values.put(HierarchyConstants.MAX_DEPTH, node.getDepth());
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.BEGIN_DT), node.getBeginDt());
				values.put(HierarchyConstants.BEGIN_DT, node.getBeginDt());
				toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.END_DT), node.getEndDt());
				values.put(HierarchyConstants.END_DT, node.getEndDt());

				// get other leaf's attributes (not mandatory)
				Iterator iter = node.getAttributes().keySet().iterator();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					Object value = node.getAttributes().get(key);
					if (key != null && value != null) {
						int attrPos = HierarchyUtils.getPosField(lstFields, key);
						if (attrPos != -1) {
							preparedStatement.setObject(attrPos, value);
							values.put(key, value);
						}
					}
				}
			} else {
				// not-leaf node
				int level = 0;
				// get other node's attributes (not mandatory ie sign)
				Iterator iter = node.getAttributes().keySet().iterator();
				String strLevel = (String) node.getAttributes().get(HierarchyConstants.LEVEL);
				level = (strLevel != null) ? Integer.parseInt(strLevel) : 0;
				if (level == 0 && !isRoot) {
					logger.error("Property LEVEL non found for node element with code: [" + node.getNodeCode() + "] - name: [" + node.getNodeName() + "]");
					throw new SpagoBIServiceException("persistService", "Property LEVEL non found for node element with code " + node.getNodeCode()
							+ " and name " + node.getNodeName());
				}
				while (iter.hasNext()) {
					String key = (String) iter.next();
					Object value = node.getAttributes().get(key);
					if (key != null && value != null) {
						int attrPos = HierarchyUtils.getPosField(lstFields, key);
						if (attrPos == -1)
							attrPos = HierarchyUtils.getPosField(lstFields, key + level);
						if (attrPos != -1) {
							preparedStatement.setObject(attrPos, value);
							values.put(key, value);
						}
					}
				}
				if (level > 0) {
					toReturn.setString(HierarchyUtils.getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV + level), node.getNodeCode());
					values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV, node.getNodeCode());
					toReturn.setString(HierarchyUtils.getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEV + level), node.getNodeName());
					values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEV, node.getNodeName());
				}
			}
		} catch (Throwable t) {
			String errMsg = "Error while inserting element with code: [" + node.getNodeCode() + "] and name: [" + node.getNodeName() + "]";
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
			throw new SpagoBIServiceException("An unexpected error occured while persisting hierarchy structure", t.getMessage() + " - " + errMsg);
		}

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
	private PreparedStatement valorizeHierPlaceholdersFromRelation(PreparedStatement preparedStatement, LinkedHashMap<String, String> lstFields,
			JSONObject relationData, JSONObject leafData, HashMap paramsMap) throws SQLException {

		PreparedStatement toReturn = preparedStatement;
		HashMap values = new HashMap();

		try {

			String prefix = (String) paramsMap.get("hierarchyPrefix");
			int maxDepth = Integer.valueOf(relationData.getString(HierarchyConstants.NODE_LEV_T)).intValue() + 1;

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
			toReturn.setString(HierarchyUtils.getPosField(lstFields, prefix + HierarchyConstants.SUFFIX_CD_LEAF), leafData.getString(prefix + "_CD"));
			values.put(prefix + HierarchyConstants.SUFFIX_CD_LEAF, leafData.getString(prefix + "_CD"));
			toReturn.setString(HierarchyUtils.getPosField(lstFields, prefix + HierarchyConstants.SUFFIX_NM_LEAF), leafData.getString(prefix + "_NM"));
			values.put(prefix + HierarchyConstants.SUFFIX_NM_LEAF, leafData.getString(prefix + "_NM"));
			toReturn.setString(HierarchyUtils.getPosField(lstFields, prefix + "_" + HierarchyConstants.LEAF_ID),
					leafData.getString(prefix + "_" + HierarchyConstants.FIELD_ID));
			values.put(prefix + "_" + HierarchyConstants.LEAF_ID, leafData.getString(prefix + "_" + HierarchyConstants.FIELD_ID));
			toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.BEGIN_DT), leafData.getString(HierarchyConstants.BEGIN_DT));
			values.put(HierarchyConstants.BEGIN_DT, leafData.getString(HierarchyConstants.BEGIN_DT));
			toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.END_DT), leafData.getString(HierarchyConstants.END_DT));
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
				toReturn.setString(HierarchyUtils.getPosField(lstFields, prefix + HierarchyConstants.SUFFIX_CD_LEV + level), pathCd[i]);
				values.put(prefix + HierarchyConstants.SUFFIX_CD_LEV, pathCd[i]);
				toReturn.setString(HierarchyUtils.getPosField(lstFields, prefix + HierarchyConstants.SUFFIX_NM_LEV + level), pathNm[i]);
				values.put(prefix + HierarchyConstants.SUFFIX_NM_LEV, pathNm[i]);
				// if it's the last level before the leaf set parent references
				if (i == pathCd.length - 1) {
					toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.LEAF_PARENT_CD), pathCd[i]);
					values.put(HierarchyConstants.LEAF_PARENT_CD, pathCd[i]);
					toReturn.setObject(HierarchyUtils.getPosField(lstFields, HierarchyConstants.LEAF_PARENT_NM), pathNm[i]);
					values.put(HierarchyConstants.LEAF_PARENT_NM, pathNm[i]);
				}
			}
			// add the leaf as the last level
			toReturn.setString(HierarchyUtils.getPosField(lstFields, prefix + HierarchyConstants.SUFFIX_CD_LEV + maxDepth), leafData.getString(prefix + "_CD"));
			values.put(prefix + HierarchyConstants.SUFFIX_CD_LEV + maxDepth, leafData.getString(prefix + "_CD"));
			toReturn.setString(HierarchyUtils.getPosField(lstFields, prefix + HierarchyConstants.SUFFIX_NM_LEV + maxDepth), leafData.getString(prefix + "_NM"));
			values.put(prefix + HierarchyConstants.SUFFIX_NM_LEV + maxDepth, leafData.getString(prefix + "_NM"));
		} catch (Throwable t) {
			String errMsg = "Error while insert for propagation of element with code: [" + values.get(HierarchyConstants.NODE_CD_T) + "] and name: ["
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

	/**
	 * Returns an hashmap with all relational values
	 *
	 * @param lstMTFieldsValue
	 * @param node
	 * @param paramsMap
	 * @return hashmap
	 */
	private HashMap getMTvalues(HashMap<String, Object> lstMTFieldsValue, HierarchyTreeNodeData node, HashMap paramsMap) {

		HashMap<String, Object> values = lstMTFieldsValue;
		if (values == null)
			values = new HashMap<String, Object>();

		// get level : is null if the node is the root
		Integer level = (node.getAttributes().get(HierarchyConstants.LEVEL) != null) ? Integer.valueOf((String) node.getAttributes().get(
				HierarchyConstants.LEVEL)) : null;
		Integer maxDepth = (node.getAttributes().get(HierarchyConstants.MAX_DEPTH) != null) ? Integer.valueOf((String) node.getAttributes().get(
				HierarchyConstants.MAX_DEPTH)) : null;

		boolean isLeaf = (level != null && maxDepth != null && level.compareTo(maxDepth) == 0) ? true : false;

		if (node.getAttributes().get(HierarchyConstants.HIER_NM_M) != null) {
			values.put(HierarchyConstants.DIMENSION, paramsMap.get("dimension"));
			values.put(HierarchyConstants.HIER_CD_T, paramsMap.get("hierTargetCode"));
			values.put(HierarchyConstants.HIER_NM_T, paramsMap.get("hierTargetName"));
			values.put(HierarchyConstants.NODE_CD_T, node.getLeafParentCode());
			values.put(HierarchyConstants.NODE_NM_T, node.getLeafParentName());
			values.put(HierarchyConstants.NODE_LEV_T, level - 1);
			values.put(HierarchyConstants.HIER_CD_M, paramsMap.get("hierSourceCode"));
			values.put(HierarchyConstants.HIER_NM_M, paramsMap.get("hierSourceName"));
			values.put(HierarchyConstants.NODE_CD_M, node.getAttributes().get(HierarchyConstants.NODE_CD_M));
			values.put(HierarchyConstants.NODE_NM_M, node.getAttributes().get(HierarchyConstants.NODE_NM_M));
			values.put(HierarchyConstants.NODE_LEV_M, node.getAttributes().get(HierarchyConstants.NODE_LEV_M));
		}
		if (!isLeaf && level != null) {
			// update complete path references
			String pathCodes = (values.get(HierarchyConstants.PATH_CD_T) != null) ? (String) values.get(HierarchyConstants.PATH_CD_T) : "";
			pathCodes += "/" + node.getNodeCode();
			values.put(HierarchyConstants.PATH_CD_T, pathCodes);

			String pathNames = (values.get(HierarchyConstants.PATH_NM_T) != null) ? (String) values.get(HierarchyConstants.PATH_NM_T) : "";
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
	private Collection<List<HierarchyTreeNodeData>> findRootToLeavesPaths(JSONObject node, String dimension) {
		Collection<List<HierarchyTreeNodeData>> collectionOfPaths = new HashSet<List<HierarchyTreeNodeData>>();
		try {
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyPrefix = hierarchies.getPrefix(dimension);

			String nodeName = node.getString(HierarchyConstants.TREE_NAME);
			String nodeCode = node.getString(HierarchyConstants.ID);

			HashMap mapAttrs = new HashMap();
			if (!node.isNull(HierarchyConstants.LEVEL)) {
				mapAttrs.put(HierarchyConstants.LEVEL, node.getString(HierarchyConstants.LEVEL));
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
			// add other node attributes if they are valorized
			ArrayList<Field> nodeFields = hierarchies.getHierarchy(dimension).getMetadataNodeFields();
			for (int f = 0, lf = nodeFields.size(); f < lf; f++) {
				Field fld = nodeFields.get(f);
				String idFld = fld.getId();
				if (!node.isNull(idFld)) {
					mapAttrs.put(idFld, node.getString(idFld));
				}
			}
			// add other leaf attributes if they are valorized
			ArrayList<Field> leafFields = hierarchies.getHierarchy(dimension).getMetadataLeafFields();
			for (int f = 0, lf = nodeFields.size(); f < lf; f++) {
				Field fld = nodeFields.get(f);
				String idFld = fld.getId();
				if (!node.isNull(idFld)) {
					mapAttrs.put(idFld, node.getString(idFld));
				}
			}
			// current node is a root?
			boolean isRoot = (node.isNull("root")) ? false : node.getBoolean("root");
			mapAttrs.put("isRoot", isRoot);

			// current node is a leaf?
			boolean isLeaf = node.getBoolean("leaf");
			mapAttrs.put("isLeaf", isLeaf);

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

			String nodeLeafId = !node.isNull(HierarchyConstants.LEAF_ID) ? node.getString(HierarchyConstants.LEAF_ID) : "";
			if (nodeLeafId.equals("")) {
				nodeLeafId = (mapAttrs.get(hierarchyPrefix + "_" + HierarchyConstants.LEAF_ID) != null) ? (String) mapAttrs.get(hierarchyPrefix + "_"
						+ HierarchyConstants.LEAF_ID) : "";
			}
			if (nodeLeafId.equals("") && !node.isNull(hierarchyPrefix + "_" + HierarchyConstants.FIELD_ID)) {
				nodeLeafId = node.getString(hierarchyPrefix + "_" + HierarchyConstants.FIELD_ID); // dimension id (ie: ACCOUNT_ID)
			}
			// create node
			HierarchyTreeNodeData nodeData = new HierarchyTreeNodeData(nodeCode, nodeName, nodeLeafId, "", "", "", mapAttrs);

			if (isLeaf) {
				List<HierarchyTreeNodeData> aPath = new ArrayList<HierarchyTreeNodeData>();
				if (!node.isNull(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEAF))
					nodeData.setNodeCode(node.getString(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEAF));
				if (!node.isNull(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF))
					nodeData.setNodeName(node.getString(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF));
				if (!node.isNull(HierarchyConstants.BEGIN_DT)) {
					nodeData.setBeginDt(Date.valueOf(node.getString(HierarchyConstants.BEGIN_DT)));
				}
				if (!node.isNull(HierarchyConstants.END_DT)) {
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
			logger.error("An unexpected error occured while retriving hierarchy root-leafs paths");
			throw new SpagoBIServiceException("An unexpected error occured while retriving custom hierarchy root-leafs paths", je);
		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving hierarchy root-leafs paths");
			throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchy root-leafs paths", t);
		}

	}
}
