/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.tools.hierarchiesmanagement.service.rest;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchyTreeNode;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchyTreeNodeData;
import it.eng.spagobi.tools.hierarchiesmanagement.TreeString;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
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

/**
 * REST Service for Hierarchies Management
 *
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */

@Path("/hierarchies")
public class HierarchiesService {

	static private Logger logger = Logger.getLogger(HierarchiesService.class);
	private static String DIMENSIONS = "DIMENSIONS";
	private static String DIMENSION = "DIMENSION";
	private static String NAME = "NAME";
	private static String DATASOURCE = "DATASOURCE";
	// dialects for correct definition of date's functions
	public static final String DIALECT_MYSQL = "org.hibernate.dialect.MySQLInnoDBDialect";
	public static final String DIALECT_TERADATA = "org.hibernate.dialect.TeradataDialect";
	public static final String DIALECT_POSTGRES = "org.hibernate.dialect.PostgreSQLDialect";
	public static final String DIALECT_ORACLE = "org.hibernate.dialect.OracleDialect";
	public static final String DIALECT_HSQL = "org.hibernate.dialect.HSQLDialect";
	public static final String DIALECT_ORACLE9i10g = "org.hibernate.dialect.Oracle9Dialect";
	public static final String DIALECT_SQLSERVER = "org.hibernate.dialect.SQLServerDialect";
	public static final String DIALECT_INGRES = "org.hibernate.dialect.IngresDialect";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String test(@Context HttpServletRequest req) {
		// TODO: to remove, just for testing rest service

		return "{\"response\":\"ok\"}";
	}

	@GET
	@Path("/dimensions")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDimensions(@Context HttpServletRequest req) {

		Hierarchies hierarchies = HierarchiesSingleton.getInstance();
		SourceBean sb = hierarchies.getTemplate();
		JSONArray dimesionsJSONArray = new JSONArray();

		try {
			SourceBean dimensions = (SourceBean) sb.getAttribute(DIMENSIONS);

			List lst = dimensions.getAttributeAsList(DIMENSION);
			for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
				JSONObject dimension = new JSONObject();
				SourceBean sbRow = (SourceBean) iterator.next();
				String name = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
				dimension.put("DIMENSION_NM", name);
				String datasource = sbRow.getAttribute(DATASOURCE) != null ? sbRow.getAttribute(DATASOURCE).toString() : null;
				dimension.put("DIMENSION_DS", datasource);
				dimesionsJSONArray.put(dimension);
			}
			return dimesionsJSONArray.toString();

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving dimensions names");
			throw new SpagoBIServiceException("An unexpected error occured while retriving dimensions names", t);
		}

	}

	// get hierarchies names of a dimension
	@GET
	@Path("/hierarchiesOfDimension")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getHierarchiesOfDimensions(@QueryParam("dimension") String dimension) {
		JSONArray hierarchiesJSONArray = new JSONArray();

		try {

			// 1 - get hierarchy table postfix(ex: _CDC)
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyPrefix = hierarchies.getHierarchyTablePrefixName(dimension);
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

			String tableName = "HIER_" + hierarchyPrefix;
			IDataStore dataStore = dataSource.executeStatement("SELECT DISTINCT(" + hierarchyNameColumn + ") FROM " + tableName + " WHERE " + typeColumn
					+ "=\"AUTO\"", 0, 0);
			for (Iterator iterator = dataStore.iterator(); iterator.hasNext();) {
				IRecord record = (IRecord) iterator.next();
				IField field = record.getFieldAt(0);
				String hierarchyName = (String) field.getValue();
				JSONObject hierarchy = new JSONObject();
				hierarchy.put("HIERARCHY_NM", hierarchyName);
				hierarchiesJSONArray.put(hierarchy);

			}

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving automatic hierarchies names");
			throw new SpagoBIServiceException("An unexpected error occured while retriving automatic hierarchies names", t);
		}
		return hierarchiesJSONArray.toString();
	}

	// get automatic hierarchy structure for tree visualization
	@GET
	@Path("/getAutomaticHierarchyTree")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getAutomaticHierarchyTree(@QueryParam("dimension") String dimension, @QueryParam("hierarchy") String hierarchy,
			@QueryParam("dateHierarchy") String dateHierarchy) {
		HierarchyTreeNode hierarchyTree;
		JSONObject treeJSONObject;
		try {
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();

			// 1 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}
			// 2 -get hierarchy table postfix
			String hierarchyPrefix = hierarchies.getHierarchyTablePrefixName(dimension);
			String hierarchyFK = hierarchies.getHierarchyTableForeignKeyName(dimension);
			// 3 - execute query to get hierarchies leafs
			String queryText = this.createQueryAutomaticHierarchy(dataSource, hierarchyFK, hierarchyPrefix, hierarchy, dateHierarchy);
			IDataStore dataStore = dataSource.executeStatement(queryText, 0, 0);

			// 4 - Create ADT for Tree from datastore
			hierarchyTree = createHierarchyTreeStructure(dataStore);

			treeJSONObject = convertHierarchyTreeAsJSON(hierarchyTree);

			if (treeJSONObject == null)
				return null;

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving hierarchy structure");
			throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchy structure", t);
		}

		return treeJSONObject.toString();

	}

	// get custom hierarchies names
	@GET
	@Path("/getCustomHierarchies")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getCustomHierarchies(@QueryParam("dimension") String dimension) {
		JSONArray hierarchiesJSONArray = new JSONArray();

		try {

			// 1 - get hierarchy table postfix(ex: _CDC)
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyPrefix = hierarchies.getHierarchyTablePrefixName(dimension);
			// 2 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}
			// 3- execute query to get hierarchies names
			String hierarchyCodeColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_CD", dataSource);
			String hierarchyNameColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_NM", dataSource);
			String typeColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_TP", dataSource);
			String hierarchyDescriptionColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_DS", dataSource);
			String scopeColumn = AbstractJDBCDataset.encapsulateColumnName("SCOPE", dataSource);

			String columns = hierarchyNameColumn + "," + typeColumn + "," + hierarchyDescriptionColumn + "," + scopeColumn + " ";

			String tableName = "HIER_" + hierarchyPrefix;
			IDataStore dataStore = dataSource
					.executeStatement("SELECT DISTINCT(" + hierarchyCodeColumn + ")," + columns + " FROM " + tableName + " WHERE " + typeColumn
							+ "=\"MANUAL\" OR " + typeColumn + "=\"SEMIMANUAL\" OR " + typeColumn + "=\"TECHNICAL\" ORDER BY " + hierarchyCodeColumn, 0, 0);
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
				hierarchy.put("HIERARCHY_CD", hierarchyCode);
				hierarchy.put("HIERARCHY_NM", hierarchyName);
				hierarchy.put("HIERARCHY_TP", hierarchyType);
				hierarchy.put("HIERARCHY_DS", hierarchyDescription);
				hierarchy.put("HIERARCHY_SC", hierarchyScope);
				hierarchiesJSONArray.put(hierarchy);

			}

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving custom hierarchies names");
			throw new SpagoBIServiceException("An unexpected error occured while retriving custom hierarchies names", t);
		}
		return hierarchiesJSONArray.toString();

		// return "{\"response\":\"customHierarchies\"}";

	}

	@GET
	@Path("/getCustomHierarchyTree")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getCustomHierarchyTree(@QueryParam("dimension") String dimension, @QueryParam("hierarchy") String hierarchy) {
		// get custom hierarchy structure for tree visualization
		HierarchyTreeNode hierarchyTree;
		JSONObject treeJSONObject;
		try {
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();

			// 1 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}
			// 2 -get hierarchy table postfix
			String hierarchyPrefix = hierarchies.getHierarchyTablePrefixName(dimension);
			String hierarchyFK = hierarchies.getHierarchyTableForeignKeyName(dimension);

			// 3 - execute query to get hierarchies leafs
			String queryText = this.createQueryCustomHierarchy(dataSource, hierarchyFK, hierarchyPrefix, hierarchy);
			IDataStore dataStore = dataSource.executeStatement(queryText, 0, 0);

			// 4 - Create ADT for Tree from datastore
			hierarchyTree = createHierarchyTreeStructure(dataStore);

			treeJSONObject = convertHierarchyTreeAsJSON(hierarchyTree);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving custom hierarchy structure");
			throw new SpagoBIServiceException("An unexpected error occured while retriving custom hierarchy structure", t);
		}

		return treeJSONObject.toString();

	}

	@POST
	@Path("/cloneCustomHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String cloneCustomHierarchyTree(@Context HttpServletRequest req) {
		// Clone an existing custom hierarchy
		try {

			// dimension of hierarchy to clone
			String dimension = req.getParameter("dimension");
			// code of hierarchy to clone
			String hierarchy = req.getParameter("hierarchy");
			// Attributes of the clone
			String hierarchyCode = req.getParameter("code");
			String hierarchyName = req.getParameter("name");
			String hierarchyDescription = req.getParameter("description");
			String hierarchyScope = req.getParameter("scope");
			String hierarchyType = req.getParameter("type");

			Hierarchies hierarchies = HierarchiesSingleton.getInstance();

			// 1 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while retriving hierarchies names", "No datasource found for Hierarchies");
			}
			// 2 -get hierarchy table postfix
			String hierarchyPrefix = hierarchies.getHierarchyTablePrefixName(dimension);
			String hierarchyFK = hierarchies.getHierarchyTableForeignKeyName(dimension);

			// 3 - execute query to get hierarchies leafs
			String tableName = "HIER_" + hierarchyPrefix;
			String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_CD", dataSource);

			String queryText = "SELECT " + columnsToInsert(hierarchyPrefix, hierarchyFK, dataSource) + " FROM " + tableName + " WHERE " + hierNameColumn
					+ " = \"" + hierarchy + "\" ";
			Connection databaseConnection = dataSource.getConnection();
			Statement stmt = databaseConnection.createStatement();
			ResultSet rs = stmt.executeQuery(queryText);

			// Retrieve original values and duplicate nodes
			String insertQuery = createInsertStatement(hierarchyPrefix, hierarchyFK, dataSource);
			final int COLUMNSNUMBER = 58; // total columns number (with signs and dates)

			// iterate each row to clone
			while (rs.next()) {
				PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertQuery);

				// CHANGED FIELDS
				// ----------------------------------------------
				// HIER_DS column
				preparedStatement.setString(1, hierarchyDescription);
				// HIER_TP column
				preparedStatement.setString(2, hierarchyType);
				// SCOPE column
				preparedStatement.setString(3, hierarchyScope);
				// HIER_CD
				preparedStatement.setString(4, hierarchyCode);
				// HIER_NM
				preparedStatement.setString(5, hierarchyName);
				// ----------------------------------------------

				// set all level column to null by default
				for (int i = 6; i < COLUMNSNUMBER + 1; i++) {
					preparedStatement.setNull(i, java.sql.Types.VARCHAR);
				}

				for (int i = 6; i < COLUMNSNUMBER + 1; i++) {

					if (i == COLUMNSNUMBER - 22) {
						// last node is a leaf
						// _CD_LEAF
						preparedStatement.setString(COLUMNSNUMBER - 22, rs.getString(COLUMNSNUMBER - 22));
						// _NM_LEAF
						preparedStatement.setString(COLUMNSNUMBER - 21, rs.getString(COLUMNSNUMBER - 21));
						// LEAF_ID
						preparedStatement.setLong(COLUMNSNUMBER - 20, rs.getLong(COLUMNSNUMBER - 20));
						// LEAF_PARENT_CD
						preparedStatement.setString(COLUMNSNUMBER - 19, rs.getString(COLUMNSNUMBER - 19));
						// LEAF_PARENT_NM
						preparedStatement.setString(COLUMNSNUMBER - 18, rs.getString(COLUMNSNUMBER - 18));
						// BEGIN_DT
						preparedStatement.setDate(COLUMNSNUMBER - 17, rs.getDate(COLUMNSNUMBER - 17));
						// END_DT
						preparedStatement.setDate(COLUMNSNUMBER - 16, rs.getDate(COLUMNSNUMBER - 16));
						// SIGNS from level 1 to level 15
						preparedStatement.setInt(COLUMNSNUMBER - 15, rs.getInt(COLUMNSNUMBER - 15));
						preparedStatement.setInt(COLUMNSNUMBER - 14, rs.getInt(COLUMNSNUMBER - 14));
						preparedStatement.setInt(COLUMNSNUMBER - 13, rs.getInt(COLUMNSNUMBER - 13));
						preparedStatement.setInt(COLUMNSNUMBER - 12, rs.getInt(COLUMNSNUMBER - 12));
						preparedStatement.setInt(COLUMNSNUMBER - 11, rs.getInt(COLUMNSNUMBER - 11));
						preparedStatement.setInt(COLUMNSNUMBER - 10, rs.getInt(COLUMNSNUMBER - 10));
						preparedStatement.setInt(COLUMNSNUMBER - 9, rs.getInt(COLUMNSNUMBER - 9));
						preparedStatement.setInt(COLUMNSNUMBER - 8, rs.getInt(COLUMNSNUMBER - 8));
						preparedStatement.setInt(COLUMNSNUMBER - 7, rs.getInt(COLUMNSNUMBER - 7));
						preparedStatement.setInt(COLUMNSNUMBER - 6, rs.getInt(COLUMNSNUMBER - 6));
						preparedStatement.setInt(COLUMNSNUMBER - 5, rs.getInt(COLUMNSNUMBER - 5));
						preparedStatement.setInt(COLUMNSNUMBER - 4, rs.getInt(COLUMNSNUMBER - 4));
						preparedStatement.setInt(COLUMNSNUMBER - 3, rs.getInt(COLUMNSNUMBER - 3));
						preparedStatement.setInt(COLUMNSNUMBER - 2, rs.getInt(COLUMNSNUMBER - 2));
						preparedStatement.setInt(COLUMNSNUMBER - 1, rs.getInt(COLUMNSNUMBER - 1));
						// MAX_DEPTH
						preparedStatement.setLong(COLUMNSNUMBER, rs.getLong(COLUMNSNUMBER));
						// exit loop
						break;
					} else {
						// not-leaf node
						// _CD_LEV
						preparedStatement.setString(i, rs.getString(i));
						// _NM_LEV
						preparedStatement.setString(i + 1, rs.getString(i + 1));
					}
				}

				// Execution of prepared statement
				// ----------------------------------------
				preparedStatement.executeUpdate();
				preparedStatement.close();

			}

			return "{\"response\":\"ok\"}";

		} catch (Throwable t) {
			logger.error("An unexpected error occured while cloning custom hierarchy structure");
			throw new SpagoBIServiceException("An unexpected error occured while cloning custom hierarchy structure", t);
		}
	}

	@POST
	@Path("/saveCustomHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String saveCustomHierarchy(@Context HttpServletRequest req) {
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

			String dimension = req.getParameter("dimension");

			if (!isInsert || customTreeInMemorySaved) {
				deleteCustomHierarchy(req);
			}

			Collection<List<HierarchyTreeNodeData>> paths = findRootToLeavesPaths(rootJSONObject);

			// Information for persistence
			// 1 - get hierarchy table postfix(ex: _CDC)
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyPrefix = hierarchies.getHierarchyTablePrefixName(dimension);
			String hierarchyFK = hierarchies.getHierarchyTableForeignKeyName(dimension);
			// 2 - get datasource label name
			String dataSourceName = hierarchies.getDataSourceOfDimension(dimension);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceByLabel(dataSourceName);
			if (dataSource == null) {
				throw new SpagoBIServiceException("An unexpected error occured while saving custom hierarchy", "No datasource found for saving hierarchy");
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
	@Path("/deleteCustomHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String deleteCustomHierarchy(@Context HttpServletRequest req) throws SQLException {
		// delete hierarchy
		Connection connection = null;
		try {
			String dimension = req.getParameter("dimension");
			String hierarchyCode = req.getParameter("code");

			// 1 - get hierarchy table postfix(ex: _CDC)
			Hierarchies hierarchies = HierarchiesSingleton.getInstance();
			String hierarchyPrefix = hierarchies.getHierarchyTablePrefixName(dimension);

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
	@Path("/modifyCustomHierarchy")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String modifyCustomHierarchy(@Context HttpServletRequest req) throws SQLException {
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

			deleteCustomHierarchy(req);
			saveCustomHierarchy(req);

		} catch (Throwable t) {
			logger.error("An unexpected error occured while modifing custom hierarchy");
			throw new SpagoBIServiceException("An unexpected error occured while modifing custom hierarchy", t);
		}

		return "{\"response\":\"ok\"}";

	}

	/*----------------------------------------------
	 * Utilities functions
	 *----------------------------------------------/


	/**
	 * Persist custom hierarchy paths to database
	 */
	private void persistCustomHierarchyPath(String hierarchyCode, String hierarchyName, String hierarchyDescription, String hierarchyScope,
			String hierarchyType, IDataSource dataSource, String hierarchyPrefix, String hierarchyFK, List<HierarchyTreeNodeData> path, boolean isInsert)
			throws SQLException {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			// connection.setAutoCommit(false);

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
					preparedStatement.setInt(COLUMNSNUMBER - 15, node.getSignLev1());
					preparedStatement.setInt(COLUMNSNUMBER - 14, node.getSignLev2());
					preparedStatement.setInt(COLUMNSNUMBER - 13, node.getSignLev3());
					preparedStatement.setInt(COLUMNSNUMBER - 12, node.getSignLev4());
					preparedStatement.setInt(COLUMNSNUMBER - 11, node.getSignLev5());
					preparedStatement.setInt(COLUMNSNUMBER - 10, node.getSignLev6());
					preparedStatement.setInt(COLUMNSNUMBER - 9, node.getSignLev7());
					preparedStatement.setInt(COLUMNSNUMBER - 8, node.getSignLev8());
					preparedStatement.setInt(COLUMNSNUMBER - 7, node.getSignLev9());
					preparedStatement.setInt(COLUMNSNUMBER - 6, node.getSignLev10());
					preparedStatement.setInt(COLUMNSNUMBER - 5, node.getSignLev11());
					preparedStatement.setInt(COLUMNSNUMBER - 4, node.getSignLev12());
					preparedStatement.setInt(COLUMNSNUMBER - 3, node.getSignLev13());
					preparedStatement.setInt(COLUMNSNUMBER - 2, node.getSignLev14());
					preparedStatement.setInt(COLUMNSNUMBER - 1, node.getSignLev15());

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
			HierarchyTreeNodeData nodeData = new HierarchyTreeNodeData(nodeCode, nodeName, nodeLeafId, "", "", "");

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
				nodeData.setSignLev1(Integer.valueOf(node.getString("signLev1")));
				nodeData.setSignLev2(Integer.valueOf(node.getString("signLev2")));
				nodeData.setSignLev3(Integer.valueOf(node.getString("signLev3")));
				nodeData.setSignLev4(Integer.valueOf(node.getString("signLev4")));
				nodeData.setSignLev5(Integer.valueOf(node.getString("signLev5")));
				nodeData.setSignLev6(Integer.valueOf(node.getString("signLev6")));
				nodeData.setSignLev7(Integer.valueOf(node.getString("signLev7")));
				nodeData.setSignLev8(Integer.valueOf(node.getString("signLev8")));
				nodeData.setSignLev9(Integer.valueOf(node.getString("signLev9")));
				nodeData.setSignLev10(Integer.valueOf(node.getString("signLev10")));
				nodeData.setSignLev11(Integer.valueOf(node.getString("signLev11")));
				nodeData.setSignLev12(Integer.valueOf(node.getString("signLev12")));
				nodeData.setSignLev13(Integer.valueOf(node.getString("signLev13")));
				nodeData.setSignLev14(Integer.valueOf(node.getString("signLev14")));
				nodeData.setSignLev15(Integer.valueOf(node.getString("signLev15")));
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

	/**
	 * Create query for extracting automatic hierarchy rows
	 */
	private String createQueryAutomaticHierarchy(IDataSource dataSource, String hierarchyFK, String hierarchyPrefix, String hierarchyName, String dateHierarchy) {

		String tableName = "HIER_" + hierarchyPrefix;

		// select
		StringBuffer selectClauseBuffer = new StringBuffer(" ");
		String maxDepthColumn = AbstractJDBCDataset.encapsulateColumnName("MAX_DEPTH", dataSource);
		String hierarchyNameColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_NM", dataSource);
		String hierarchyCodeColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_CD", dataSource);
		selectClauseBuffer.append(maxDepthColumn + "," + hierarchyCodeColumn + "," + hierarchyNameColumn + ",");

		for (int i = 1; i < 16; i++) {
			String CD_LEV = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_CD_LEV" + i, dataSource);
			String NM_LEV = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_NM_LEV" + i, dataSource);
			selectClauseBuffer.append(CD_LEV + "," + NM_LEV + ",");
		}
		String CD_LEAF = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_CD_LEAF", dataSource);
		String NM_LEAF = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_NM_LEAF", dataSource);
		String LEAF_ID = AbstractJDBCDataset.encapsulateColumnName(hierarchyFK, dataSource);
		String LEAF_PARENT_CD = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_CD", dataSource);
		String LEAF_PARENT_NM = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_NM", dataSource);

		selectClauseBuffer.append(CD_LEAF + "," + NM_LEAF + "," + LEAF_ID + "," + LEAF_PARENT_CD + "," + LEAF_PARENT_NM + ",");

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

		selectClauseBuffer.append(BEGIN_DT + "," + END_DT + "," + G_SIGN_LIV1 + "," + G_SIGN_LIV2 + "," + G_SIGN_LIV3 + "," + G_SIGN_LIV4 + "," + G_SIGN_LIV5
				+ "," + G_SIGN_LIV6 + "," + G_SIGN_LIV7 + "," + G_SIGN_LIV8 + "," + G_SIGN_LIV9 + "," + G_SIGN_LIV10 + "," + G_SIGN_LIV11 + "," + G_SIGN_LIV12
				+ "," + G_SIGN_LIV13 + "," + G_SIGN_LIV14 + "," + G_SIGN_LIV15 + " ");

		String selectClause = selectClauseBuffer.toString();

		// where
		String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_NM", dataSource);
		String hierTypeColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_TP", dataSource);
		String hierDateBeginColumn = AbstractJDBCDataset.encapsulateColumnName("BEGIN_DT", dataSource);
		String hierDateEndColumn = AbstractJDBCDataset.encapsulateColumnName("END_DT", dataSource);

		// defining date conversion cmd for filters on dates
		String format = (SingletonConfig.getInstance().getConfigValue("SPAGOBI.DATE-FORMAT-SERVER.format"));
		String fnc = "";
		String actualDialect = dataSource.getHibDialectClass();
		if (DIALECT_MYSQL.equalsIgnoreCase(actualDialect)) {
			fnc = "STR_TO_DATE('" + dateHierarchy + "','" + format + "')";
		} else if (DIALECT_POSTGRES.equalsIgnoreCase(actualDialect)) {
			fnc = "TO_DATE('" + dateHierarchy + "','" + format + "')";
		} else if (DIALECT_ORACLE.equalsIgnoreCase(actualDialect) || DIALECT_ORACLE9i10g.equalsIgnoreCase(actualDialect)) {
			fnc = "TO_DATE('" + dateHierarchy + "','" + format + "')";
		} else if (DIALECT_HSQL.equalsIgnoreCase(actualDialect)) {
			fnc = "TO_DATE('" + dateHierarchy + "','" + format + "')";
		} else if (DIALECT_SQLSERVER.equalsIgnoreCase(actualDialect)) {
			fnc = "TO_DATE('" + dateHierarchy + "','" + format + "')";
		} else if (DIALECT_INGRES.equalsIgnoreCase(actualDialect)) {
			// fnc = "DATE('" + dateHierarchy + "','" + format + "')";
			fnc = "DATE('" + dateHierarchy + "')";
		} else if (DIALECT_TERADATA.equalsIgnoreCase(actualDialect)) {
			fnc = "'" + dateHierarchy + "',AS DATE FORMAT '" + format + "')";
		}

		String query = "SELECT " + selectClause + " FROM " + tableName + " WHERE " + hierNameColumn + " = \"" + hierarchyName + "\" AND " + hierTypeColumn
				+ " = \"AUTO\" AND " + fnc + " >= " + hierDateBeginColumn + " AND " + fnc + " <= " + hierDateEndColumn;

		logger.debug("Query for AUTOMATIC hierarchies: " + query);
		return query;
	}

	/**
	 * Create query for extracting automatic hierarchy rows
	 */
	private String createQueryCustomHierarchy(IDataSource dataSource, String hierarchyFK, String hierarchyPrefix, String hierarchyCode) {

		String tableName = "HIER_" + hierarchyPrefix;

		// select
		StringBuffer selectClauseBuffer = new StringBuffer(" ");
		String maxDepthColumn = AbstractJDBCDataset.encapsulateColumnName("MAX_DEPTH", dataSource);
		String hierarchyNameColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_NM", dataSource);
		String hierarchyCodeColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_CD", dataSource);
		selectClauseBuffer.append(maxDepthColumn + "," + hierarchyCodeColumn + "," + hierarchyNameColumn + ",");

		for (int i = 1; i < 16; i++) {
			String CD_LEV = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_CD_LEV" + i, dataSource);
			String NM_LEV = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_NM_LEV" + i, dataSource);
			selectClauseBuffer.append(CD_LEV + "," + NM_LEV + ",");
		}
		String CD_LEAF = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_CD_LEAF", dataSource);
		String NM_LEAF = AbstractJDBCDataset.encapsulateColumnName(hierarchyPrefix + "_NM_LEAF", dataSource);
		String LEAF_ID = AbstractJDBCDataset.encapsulateColumnName(hierarchyFK, dataSource);
		String LEAF_PARENT_CD = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_CD", dataSource);
		String LEAF_PARENT_NM = AbstractJDBCDataset.encapsulateColumnName("LEAF_PARENT_NM", dataSource);

		selectClauseBuffer.append(CD_LEAF + "," + NM_LEAF + "," + LEAF_ID + "," + LEAF_PARENT_CD + "," + LEAF_PARENT_NM + ",");

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

		selectClauseBuffer.append(BEGIN_DT + "," + END_DT + "," + G_SIGN_LIV1 + "," + G_SIGN_LIV2 + "," + G_SIGN_LIV3 + "," + G_SIGN_LIV4 + "," + G_SIGN_LIV5
				+ "," + G_SIGN_LIV6 + "," + G_SIGN_LIV7 + "," + G_SIGN_LIV8 + "," + G_SIGN_LIV9 + "," + G_SIGN_LIV10 + "," + G_SIGN_LIV11 + "," + G_SIGN_LIV12
				+ "," + G_SIGN_LIV13 + "," + G_SIGN_LIV14 + "," + G_SIGN_LIV15 + " ");

		String selectClause = selectClauseBuffer.toString();

		// where
		String hierNameColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_CD", dataSource);
		String hierTypeColumn = AbstractJDBCDataset.encapsulateColumnName("HIER_TP", dataSource);

		String query = "SELECT " + selectClause + " FROM " + tableName + " WHERE " + hierNameColumn + " = \"" + hierarchyCode + "\" AND (" + hierTypeColumn
				+ "=\"MANUAL\" OR " + hierTypeColumn + "=\"SEMIMANUAL\" OR " + hierTypeColumn + "=\"TECHNICAL\" )";

		logger.debug("Query for CUSTOM hierarchies: " + query);
		return query;
	}

	/**
	 * Create HierarchyTreeNode tree from datastore with leafs informations
	 */
	private HierarchyTreeNode createHierarchyTreeStructure(IDataStore dataStore) {
		HierarchyTreeNode root = null;

		// ONLY FOR DEBUG
		Set<String> allNodeCodes = new HashSet<String>();

		// contains the code of the last level node (not null) inserted in the
		// tree

		for (Iterator iterator = dataStore.iterator(); iterator.hasNext();) {
			String lastLevelFound = null;

			IRecord record = (IRecord) iterator.next();
			List<IField> recordFields = record.getFields();
			int fieldsCount = recordFields.size();

			// MAX_DEPTH, must be equal to the level of the leaf (that we skip)
			IField maxDepthField = record.getFieldAt(0);
			int maxDepth = 0;
			if (maxDepthField.getValue() instanceof Integer) {
				Integer maxDepthValue = (Integer) maxDepthField.getValue();
				maxDepth = maxDepthValue;
			} else if (maxDepthField.getValue() instanceof Long) {
				Long maxDepthValue = (Long) maxDepthField.getValue();
				maxDepth = (int) (long) maxDepthValue;
			}

			int currentLevel = 0;
			for (int i = 1; i < fieldsCount - 21; i = i + 2) {
				IField codeField = record.getFieldAt(i); // NODE CODE
				IField nameField = record.getFieldAt(i + 1); // NODE NAME

				if ((currentLevel == maxDepth) || (codeField.getValue() == null) || (codeField.getValue().equals(""))) {
					currentLevel++;
					continue; // skip to next iteration
				} else {
					String nodeCode = (String) codeField.getValue();
					String nodeName = (String) nameField.getValue();
					HierarchyTreeNodeData data = new HierarchyTreeNodeData(nodeCode, nodeName);

					// Here I will construct the nodes of the tree
					int j = i - 1;
					switch (j) {
					case 0:
						// first level (root)
						if (root == null) {
							root = new HierarchyTreeNode(data, nodeCode);
							// ONLY FOR DEBUG
							if (allNodeCodes.contains(nodeCode)) {
								logger.error("COLLISION DETECTED ON: " + nodeCode);
							} else {
								allNodeCodes.add(nodeCode);
							}
							// ------------------------
						}
						lastLevelFound = nodeCode;
						break;
					case 2:
						// second level (root's childrens)
						if (!root.getChildrensKeys().contains(nodeCode)) {
							// node not already attached to the root
							HierarchyTreeNode aNode = new HierarchyTreeNode(data, nodeCode);
							root.add(aNode, nodeCode);
							// ONLY FOR DEBUG
							if (allNodeCodes.contains(nodeCode)) {
								logger.error("COLLISION DETECTED ON: " + nodeCode);
							} else {
								allNodeCodes.add(nodeCode);
							}
							// ------------------------
						}
						lastLevelFound = nodeCode;
						break;
					case 4:
					case 6:
					case 8:
					case 10:
					case 12:
					case 14:
					case 16:
					case 18:
					case 20:
					case 22:
					case 24:
					case 26:
					case 28:
					case 30:
					case 32:
						if (j == 32) {
							data = setDataValues(nodeCode, data, record, i);
						}
						attachNodeToLevel(root, nodeCode, lastLevelFound, data, allNodeCodes);
						lastLevelFound = nodeCode;
						// leaf level
						break;
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
	 * Sets records' value to the tree structure (leaf informations, date and strings)
	 */
	private HierarchyTreeNodeData setDataValues(String nodeCode, HierarchyTreeNodeData data, IRecord record, int i) {
		// inject leafID into node
		IField leafIdField = record.getFieldAt(i + 2);
		String leafIdString = null;
		if (leafIdField.getValue() instanceof Integer) {
			Integer leafId = (Integer) leafIdField.getValue();
			leafIdString = String.valueOf(leafId);
		} else if (leafIdField.getValue() instanceof Long) {
			Long leafId = (Long) leafIdField.getValue();
			leafIdString = String.valueOf(leafId);
		}
		data.setLeafId(leafIdString);
		IField leafParentCodeField = record.getFieldAt(i + 3);
		String leafParentCodeString = (String) leafParentCodeField.getValue();
		data.setNodeCode(leafParentCodeString + "_" + nodeCode);
		nodeCode = leafParentCodeString + "_" + nodeCode;
		data.setLeafParentCode(leafParentCodeString);
		data.setLeafOriginalParentCode(leafParentCodeString); // backup
																// code
		IField leafParentNameField = record.getFieldAt(i + 4);
		String leafParentNameString = (String) leafParentNameField.getValue();
		data.setLeafParentName(leafParentNameString);

		IField beginDtField = record.getFieldAt(i + 5);
		Date beginDtDate = (Date) beginDtField.getValue();
		data.setBeginDt(beginDtDate);

		IField endDtField = record.getFieldAt(i + 6);
		Date endDtDate = (Date) endDtField.getValue();
		data.setEndDt(endDtDate);

		IField signLev1Field = record.getFieldAt(i + 7);
		Integer signLev1Int = (Integer) signLev1Field.getValue();
		data.setSignLev1(signLev1Int);

		IField signLev2Field = record.getFieldAt(i + 8);
		Integer signLev2Int = (Integer) signLev2Field.getValue();
		data.setSignLev2(signLev2Int);

		IField signLev3Field = record.getFieldAt(i + 9);
		Integer signLev3Int = (Integer) signLev3Field.getValue();
		data.setSignLev3(signLev3Int);

		IField signLev4Field = record.getFieldAt(i + 10);
		Integer signLev4Int = (Integer) signLev4Field.getValue();
		data.setSignLev4(signLev4Int);

		IField signLev5Field = record.getFieldAt(i + 11);
		Integer signLev5Int = (Integer) signLev5Field.getValue();
		data.setSignLev5(signLev5Int);

		IField signLev6Field = record.getFieldAt(i + 12);
		Integer signLev6Int = (Integer) signLev6Field.getValue();
		data.setSignLev6(signLev6Int);

		IField signLev7Field = record.getFieldAt(i + 13);
		Integer signLev7Int = (Integer) signLev7Field.getValue();
		data.setSignLev7(signLev7Int);

		IField signLev8Field = record.getFieldAt(i + 14);
		Integer signLev8Int = (Integer) signLev8Field.getValue();
		data.setSignLev8(signLev8Int);

		IField signLev9Field = record.getFieldAt(i + 15);
		Integer signLev9Int = (Integer) signLev9Field.getValue();
		data.setSignLev9(signLev9Int);

		IField signLev10Field = record.getFieldAt(i + 16);
		Integer signLev10Int = (Integer) signLev10Field.getValue();
		data.setSignLev10(signLev10Int);

		IField signLev11Field = record.getFieldAt(i + 17);
		Integer signLev11Int = (Integer) signLev11Field.getValue();
		data.setSignLev11(signLev11Int);

		IField signLev12Field = record.getFieldAt(i + 18);
		Integer signLev12Int = (Integer) signLev12Field.getValue();
		data.setSignLev12(signLev12Int);

		IField signLev13Field = record.getFieldAt(i + 19);
		Integer signLev13Int = (Integer) signLev13Field.getValue();
		data.setSignLev13(signLev13Int);

		IField signLev14Field = record.getFieldAt(i + 20);
		Integer signLev14Int = (Integer) signLev14Field.getValue();
		data.setSignLev14(signLev14Int);

		IField signLev15Field = record.getFieldAt(i + 21);
		Integer signLev15Int = (Integer) signLev15Field.getValue();
		data.setSignLev15(signLev15Int);

		return data;
	}

	/**
	 * Attach a node as a child of another node (with key lastLevelFound)
	 */
	// TODO: remove allNodeCodes from signature
	private void attachNodeToLevel(HierarchyTreeNode root, String nodeCode, String lastLevelFound, HierarchyTreeNodeData data, Set<String> allNodeCodes) {
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

			// ONLY FOR DEBUG
			if (allNodeCodes.contains(nodeCode)) {
				logger.error("COLLISION DETECTED ON: " + nodeCode);
			} else {
				allNodeCodes.add(nodeCode);
			}

			// ------------------------

			treeNode.add(aNode, nodeCode);

		}
	}

	/**
	 * Serialize HierarchyTreeNode to JSON
	 *
	 * @param root
	 *            the root of the tree structure
	 * @return a JSONObject representing the tree
	 */
	private JSONObject convertHierarchyTreeAsJSON(HierarchyTreeNode root) {
		JSONObject rootJSONObject = new JSONObject();

		if (root == null)
			return null;

		try {
			HierarchyTreeNodeData rootData = (HierarchyTreeNodeData) root.getObject();
			rootJSONObject.put("text", rootData.getNodeName());
			rootJSONObject.put("id", rootData.getNodeCode());
			// rootJSONObject.put("root", true);
			rootJSONObject.put("expanded", false);
			rootJSONObject.put("leaf", false);

			JSONArray childrenJSONArray = new JSONArray();

			for (int i = 0; i < root.getChildCount(); i++) {
				HierarchyTreeNode childNode = root.getChild(i);
				JSONObject subTreeJSONObject = getSubTreeJSONObject(childNode);
				childrenJSONArray.put(subTreeJSONObject);
			}

			rootJSONObject.put("children", childrenJSONArray);
			// fake root
			JSONObject mainObject = new JSONObject();
			mainObject.put("text", "root");
			mainObject.put("root", true);
			mainObject.put("children", rootJSONObject);
			mainObject.put("leaf", false);
			mainObject.put("expanded", false);

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
				nodeJSONObject.put("text", nodeData.getNodeName());
				nodeJSONObject.put("id", nodeData.getNodeCode());
				nodeJSONObject.put("leafId", nodeData.getLeafId());
				nodeJSONObject.put("leafParentCode", nodeData.getLeafParentCode());
				nodeJSONObject.put("originalLeafParentCode", nodeData.getLeafOriginalParentCode());
				nodeJSONObject.put("leafParentName", nodeData.getLeafParentName());

				JSONArray childrenJSONArray = new JSONArray();

				for (int i = 0; i < node.getChildCount(); i++) {
					HierarchyTreeNode childNode = node.getChild(i);
					JSONObject subTree = getSubTreeJSONObject(childNode);
					childrenJSONArray.put(subTree);
				}
				nodeJSONObject.put("children", childrenJSONArray);
				nodeJSONObject.put("leaf", false);
				nodeJSONObject.put("expanded", false);

				nodeJSONObject = setDetailsInfo(nodeJSONObject, nodeData);
				return nodeJSONObject;

			} else {
				HierarchyTreeNodeData nodeData = (HierarchyTreeNodeData) node.getObject();
				JSONObject nodeJSONObject = new JSONObject();

				nodeJSONObject.put("text", nodeData.getNodeName());
				nodeJSONObject.put("id", nodeData.getNodeCode());
				nodeJSONObject.put("leafId", nodeData.getLeafId());
				nodeJSONObject.put("leafParentCode", nodeData.getLeafParentCode());
				nodeJSONObject.put("originalLeafParentCode", nodeData.getLeafOriginalParentCode());
				nodeJSONObject.put("leafParentName", nodeData.getLeafParentName());
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

			toReturn.put("beginDt", nodeData.getBeginDt());
			toReturn.put("endDt", nodeData.getEndDt());
			toReturn.put("signLev1", nodeData.getSignLev1());
			toReturn.put("signLev2", nodeData.getSignLev2());
			toReturn.put("signLev3", nodeData.getSignLev3());
			toReturn.put("signLev4", nodeData.getSignLev4());
			toReturn.put("signLev5", nodeData.getSignLev5());
			toReturn.put("signLev6", nodeData.getSignLev6());
			toReturn.put("signLev7", nodeData.getSignLev7());
			toReturn.put("signLev8", nodeData.getSignLev8());
			toReturn.put("signLev9", nodeData.getSignLev9());
			toReturn.put("signLev10", nodeData.getSignLev10());
			toReturn.put("signLev11", nodeData.getSignLev11());
			toReturn.put("signLev12", nodeData.getSignLev12());
			toReturn.put("signLev13", nodeData.getSignLev13());
			toReturn.put("signLev14", nodeData.getSignLev14());
			toReturn.put("signLev15", nodeData.getSignLev15());

			return toReturn;
		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while serializing hierarchy details structure to JSON", t);
		}

	}
}
