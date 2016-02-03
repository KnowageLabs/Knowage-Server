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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
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
		// Save hierarchy structure
		Connection connection = null;
		try {

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			String validityDate = (!requestVal.isNull("dateValidity")) ? requestVal.getString("dateValidity") : null;
			boolean doBackup = (!requestVal.isNull("doBackup")) ? requestVal.getBoolean("doBackup") : new Boolean("false");
			boolean isInsert = Boolean.valueOf(req.getParameter("isInsert"));
			String dimension = requestVal.getString("dimension");

			String root = requestVal.getString("root");
			JSONObject rootJSONObject = ObjectUtils.toJSONObject(root);
			String hierarchyName = rootJSONObject.getString(HierarchyConstants.HIER_NM);
			String hierarchyType = rootJSONObject.getString(HierarchyConstants.HIER_TP);
			List masterLst = null;
			if (hierarchyType.equals(HierarchyConstants.HIER_TP_MASTER))
				masterLst = new ArrayList();

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
				persistCustomHierarchyPath(connection, hierarchyTable, dataSource, hierarchyPrefix, hierarchyFK, path, isInsert, hierarchyFields, hierConfig);
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
		StringBuffer orderClauseBuffer = new StringBuffer(" ");
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
				// add first node column as order field:
				if (i == 0)
					orderClauseBuffer.append(column);
			} else {
				for (int i2 = 1, l2 = totalLevels; i2 <= l2; i2++) {
					sep = ",";
					column = AbstractJDBCDataset.encapsulateColumnName(f.getId() + i2, dataSource);
					selectClauseBuffer.append(column + sep);
					// add first node column as order field:
					if (i == 0 && i2 == 1)
						orderClauseBuffer.append(column);
				}
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
		// order cluase
		query.append(" ORDER BY " + orderClauseBuffer.toString());

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
			HierarchyTreeNode localPath = null;
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

			for (int i = 1, l = numLevels; i <= l; i++) {
				IField codeField = record.getFieldAt(dsMeta.getFieldIndex(prefix + HierarchyConstants.SUFFIX_CD_LEV + i)); // NODE CODE
				IField nameField = record.getFieldAt(dsMeta.getFieldIndex(prefix + HierarchyConstants.SUFFIX_NM_LEV + i)); // NAME CODE
				IField codeLeafField = record.getFieldAt(dsMeta.getFieldIndex(prefix + HierarchyConstants.SUFFIX_CD_LEAF)); // LEAF CODE
				String leafCode = (String) codeLeafField.getValue();

				if (currentLevel == maxDepth) {
					break; // skip to next iteration
				} else if (codeField.getValue() == null || codeField.getValue().equals("")) {
					// do nothing: it's an empty node
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

					if (localPath == null)
						localPath = new HierarchyTreeNode(data, rootCode, null);

					// check if its a leaf
					if (i == maxDepth) {
						data = setDataValues(dimension, nodeCode, data, record, metadata);
						// update LEVEL informations
						mapAttrs = data.getAttributes();
						mapAttrs.put(HierarchyConstants.LEVEL, i);
						mapAttrs.put(HierarchyConstants.MAX_DEPTH, maxDepth);
						data.setAttributes(mapAttrs);

						attachNodeToLevel(root, nodeCode, lastLevelCodeFound, localPath, data, allNodeCodes);

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
						attachNodeToLevel(root, nodeCode, lastLevelCodeFound, localPath, data, allNodeCodes);
						// attachNodeToLevel(root, nodeCode, lastLevelCodeFound, data, allNodeCodes);
					} else {
						// refresh local structure (for parent management)
						HierarchyTreeNode aNodeLocal = new HierarchyTreeNode(data, nodeCode);
						localPath.add(aNodeLocal, nodeCode);
					}
					lastLevelCodeFound = nodeCode;
					lastLevelNameFound = nodeName;
				}
				currentLevel++;
			}

		}

		if (root != null)
			// set debug mode : error in only for debug
			logger.debug(TreeString.toString(root));

		return root;

	}

	/**
	 * Attach a node as a child of another node (with key lastLevelFound that if it's null means a new record and starts from root)
	 */
	// TODO: remove allNodeCodes from signature
	private void attachNodeToLevel(HierarchyTreeNode root, String nodeCode, String lastLevelFound, HierarchyTreeNode localPath, HierarchyTreeNodeData data,
			Set<String> allNodeCodes) {

		HierarchyTreeNode treeNode = null;
		// get the local element
		HierarchyTreeNode treeLocalNode = localPath.getHierarchyNode(lastLevelFound, false);

		// first search parent node (with all path)
		for (Iterator<HierarchyTreeNode> treeIterator = root.iterator(); treeIterator.hasNext();) {
			treeNode = treeIterator.next();
			String localNodeParent = (treeLocalNode.getParent() != null) ? treeLocalNode.getParent().getKey() : "";
			String treeNodeParent = (treeNode.getParent() != null) ? treeNode.getParent().getKey() : "";
			HierarchyTreeNodeData treeData = (HierarchyTreeNodeData) treeNode.getObject();

			if (lastLevelFound == null) {
				break;
			} else if (treeNode.getKey().equals(lastLevelFound) && treeNodeParent.equals(localNodeParent)) {
				// check second level if levels and first parents are the same
				localNodeParent = (treeLocalNode.getParent().getParent() != null) ? treeLocalNode.getParent().getParent().getKey() : "";
				treeNodeParent = (treeNode.getParent().getParent() != null) ? treeNode.getParent().getParent().getKey() : "";
				if ((treeNodeParent != null && localNodeParent != null) && treeNodeParent.equals(localNodeParent))
					break;
				else if (treeNodeParent == null && localNodeParent == null) // if the grand-father is'n valorized, it's valid
					break;
			}
		}
		// then check if node was already added as a child of this parent
		if (!treeNode.getChildrensKeys().contains(nodeCode)) {
			// node not already attached to the level
			HierarchyTreeNode aNode = new HierarchyTreeNode(data, nodeCode);
			// treeNode.add(aNode, nodeCode);
			Integer nodeLevel = ((Integer) data.getAttributes().get(HierarchyConstants.LEVEL));
			Integer maxDeptLevel = ((Integer) data.getAttributes().get(HierarchyConstants.MAX_DEPTH));
			if (nodeLevel.equals(maxDeptLevel)) {
				// it's the leaf... adds it to the last element if has the same code (for duplicate nodes)
				String lastKey = (treeLocalNode.getLastChild() != null) ? treeLocalNode.getLastChild().getKey() : nodeCode;
				HierarchyTreeNode lastNode = treeNode.getHierarchyNode(lastKey, true);
				lastNode.add(aNode, nodeCode);
			} else {
				treeNode.add(aNode, nodeCode);
			}
			HierarchyTreeNode aNodeLocal = new HierarchyTreeNode(data, nodeCode);
			treeLocalNode.add(aNodeLocal, nodeCode); // updates the local path
		} else {
			// check if it's a duplicate node in different level (in this case its will add to the tree, otherwise not)
			Integer nodeLevel = ((Integer) data.getAttributes().get(HierarchyConstants.LEVEL));
			for (int n = 0; n < treeNode.getChildrensKeys().size(); n++) {
				HierarchyTreeNodeData child = (HierarchyTreeNodeData) treeNode.getChild(n).getObject();
				if (child.getNodeCode().equals(nodeCode)) {
					Integer childLevel = ((Integer) child.getAttributes().get(HierarchyConstants.LEVEL));
					if (childLevel != null && nodeLevel != null && !childLevel.equals(nodeLevel)) {
						// add the node to the tree (because it's a repetitive node BUT in different levels)
						// moves on the tree until the correct element
						HierarchyTreeNode lastNode = treeNode.getHierarchyNode(nodeCode, true);
						HierarchyTreeNode aNode = new HierarchyTreeNode(data, nodeCode); // the new node
						lastNode.add(aNode, nodeCode);
						break;
					}
				}
			}
			// update the local structure for next elements
			HierarchyTreeNode aNodeLocal = new HierarchyTreeNode(data, nodeCode);
			treeLocalNode.add(aNodeLocal, nodeCode); // updates the local path
		}
		localPath = treeLocalNode;

		// ONLY FOR DEBUG
		if (!allNodeCodes.contains(nodeCode)) {
			allNodeCodes.add(nodeCode);
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

	private JSONObject convertHierarchyTreeAsJSON(HierarchyTreeNode root, String hierName, String dimension) {
		JSONArray rootJSONObject = new JSONArray();

		if (root == null)
			return null;

		try {
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			HashMap hierConfig = hierarchies.getConfig(dimension);

			HierarchyTreeNodeData rootData = (HierarchyTreeNodeData) root.getObject();
			JSONArray childrenJSONArray = new JSONArray();

			for (int i = 0; i < root.getChildCount(); i++) {
				HierarchyTreeNode childNode = root.getChild(i);
				JSONObject subTreeJSONObject = getSubTreeJSONObject(childNode, hierConfig);
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
	 * get the JSONObject representing the tree having the passed node as a root
	 *
	 * @param node
	 *            the root of the subtree
	 * @return JSONObject representing the subtree
	 */
	private JSONObject getSubTreeJSONObject(HierarchyTreeNode node, HashMap hierConfig) {

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
				// nodeJSONObject.put(HierarchyConstants.LEAF_ORIG_PARENT_CD, nodeData.getLeafOriginalParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_NM, nodeData.getLeafParentName());
				nodeJSONObject.put("aliasId", hierConfig.get(HierarchyConstants.TREE_NODE_CD));
				nodeJSONObject.put("aliasName", hierConfig.get(HierarchyConstants.TREE_NODE_NM));
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
				nodeJSONObject.put(HierarchyConstants.TREE_NAME, nodeData.getNodeName());
				nodeJSONObject.put(HierarchyConstants.ID, nodeData.getNodeCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_ID, nodeData.getLeafId());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_CD, nodeData.getLeafParentCode());
				// nodeJSONObject.put(HierarchyConstants.LEAF_ORIG_PARENT_CD, nodeData.getLeafOriginalParentCode());
				nodeJSONObject.put(HierarchyConstants.LEAF_PARENT_NM, nodeData.getLeafParentName());
				nodeJSONObject.put("aliasId", hierConfig.get(HierarchyConstants.TREE_LEAF_CD));
				nodeJSONObject.put("aliasName", hierConfig.get(HierarchyConstants.TREE_LEAF_NM));
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
	private void persistCustomHierarchyPath(Connection connection, String hierarchyTable, IDataSource dataSource, String hierarchyPrefix, String hierarchyFK,
			List<HierarchyTreeNodeData> path, boolean isInsert, Hierarchy hierarchyFields, HashMap hierConfig) throws SQLException {

		HashMap values = new HashMap();
		String columns = "";
		HierarchyTreeNodeData node = null;
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

			columns = sbColumns.toString();

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

			// explore the path and set the corresponding columns
			// keeps the column number
			for (int i = 0; i < path.size(); i++) {
				values = new HashMap();
				node = path.get(i);
				boolean isRoot = ((Boolean) node.getAttributes().get("isRoot")).booleanValue();
				boolean isLeaf = ((Boolean) node.getAttributes().get("isLeaf")).booleanValue();
				// if (node.getLeafId() != null && !node.getLeafId().equals("")) {
				if (isLeaf) {
					// it's a leaf
					preparedStatement.setString(getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEAF), node.getNodeCode());
					values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEAF, node.getNodeCode());
					preparedStatement.setString(getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF), node.getNodeName());
					values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF, node.getNodeName());
					if (node.getDepth() != null) {
						preparedStatement.setString(getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV + node.getDepth()),
								node.getNodeCode());
						values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV, node.getNodeCode());
					}
					if (node.getDepth() != null) {
						preparedStatement.setString(getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEV + node.getDepth()),
								node.getNodeName());
						values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV, node.getNodeName());
					} else if (!isRoot) {
						logger.error("Property LEVEL non found for leaf element with code " + node.getNodeCode() + " and name " + node.getNodeName());
						throw new SpagoBIServiceException("persistService", "Property LEVEL non found for leaf element with code " + node.getNodeCode()
								+ " and name " + node.getNodeName());
					}
					preparedStatement.setString(getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF), node.getNodeName());
					values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEAF, node.getNodeName());
					preparedStatement.setObject(getPosField(lstFields, hierarchyPrefix + "_" + HierarchyConstants.LEAF_ID), node.getLeafId());
					values.put(hierarchyPrefix + "_" + HierarchyConstants.LEAF_ID, node.getLeafId());
					preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.LEAF_PARENT_CD), node.getLeafParentCode());
					values.put(HierarchyConstants.LEAF_PARENT_CD, node.getLeafParentCode());
					// preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.LEAF_ORIG_PARENT_CD), node.getLeafOriginalParentCode());
					preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.LEAF_PARENT_NM), node.getLeafParentName());
					values.put(HierarchyConstants.LEAF_PARENT_NM, node.getLeafParentName());
					preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.MAX_DEPTH), node.getDepth());
					values.put(HierarchyConstants.MAX_DEPTH, node.getDepth());
					preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.BEGIN_DT), node.getBeginDt());
					values.put(HierarchyConstants.BEGIN_DT, node.getBeginDt());
					preparedStatement.setObject(getPosField(lstFields, HierarchyConstants.END_DT), node.getEndDt());
					values.put(HierarchyConstants.END_DT, node.getEndDt());

					// get other leaf's attributes (not mandatory)
					Iterator iter = node.getAttributes().keySet().iterator();
					while (iter.hasNext()) {
						String key = (String) iter.next();
						Object value = node.getAttributes().get(key);
						if (key != null && value != null) {
							int attrPos = getPosField(lstFields, key);
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
							int attrPos = getPosField(lstFields, key);
							if (attrPos == -1)
								attrPos = getPosField(lstFields, key + level);
							if (attrPos != -1) {
								preparedStatement.setObject(attrPos, value);
								values.put(key, value);
							}
						}
					}
					if (level > 0) {
						preparedStatement.setString(getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV + level), node.getNodeCode());
						values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_CD_LEV, node.getNodeCode());
						preparedStatement.setString(getPosField(lstFields, hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEV + level), node.getNodeName());
						values.put(hierarchyPrefix + HierarchyConstants.SUFFIX_NM_LEV, node.getNodeName());
					}
				}
			}

			// 5 - Execution of prepared statement
			// ----------------------------------------
			preparedStatement.executeUpdate();
			preparedStatement.close();

		} catch (Throwable t) {
			String errMsg = "Error while inserting element with code: [" + node.getNodeCode() + "] and name: [" + node.getNodeName() + "]";
			if (values.size() > 0) {
				errMsg += " with next values: [";
				Iterator iter = values.keySet().iterator();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					Object value = values.get(key);
					errMsg += " key: " + key + " - value: " + value + ((iter.hasNext()) ? "," : "]");
					// errMsg += "insert into " + hierarchyTable + "(" + columns + ") values (";
					// errMsg += value + ((iter.hasNext()) ? "," : ")");
				}
				logger.error(errMsg, t);
			}
			throw new SpagoBIServiceException("An unexpected error occured while persisting hierarchy structure", t.getMessage() + " - " + errMsg);
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
		logger.info("Attribute '" + name + "' non found in fields' list ");
		return -1;
	}

	/**
	 * Find all paths from root to leaves
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
			ArrayList<Field> generalFields = hierarchies.getHierarchy(dimension).getMetadataGeneralFields();
			for (int f = 0, lf = generalFields.size(); f < lf; f++) {
				Field fld = generalFields.get(f);
				String idFld = fld.getId();
				if (!node.isNull(idFld)) {
					mapAttrs.put(idFld, node.getString(idFld));
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
				// String nodeOriginalParentCode = null;
				if (!node.isNull(HierarchyConstants.LEAF_PARENT_CD))
					nodeParentCode = node.getString(HierarchyConstants.LEAF_PARENT_CD);
				// if (!node.isNull(HierarchyConstants.LEAF_ORIG_PARENT_CD))
				// nodeOriginalParentCode = node.getString(HierarchyConstants.LEAF_ORIG_PARENT_CD);
				// nodeData.setNodeCode(nodeCode.replaceFirst(nodeOriginalParentCode + "_", ""));
				nodeData.setLeafParentCode(nodeParentCode);
				nodeData.setLeafParentName(node.getString(HierarchyConstants.LEAF_PARENT_NM));
				// nodeData.setLeafOriginalParentCode(nodeOriginalParentCode);
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

}
