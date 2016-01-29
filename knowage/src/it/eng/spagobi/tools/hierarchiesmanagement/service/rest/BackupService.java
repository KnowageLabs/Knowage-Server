package it.eng.spagobi.tools.hierarchiesmanagement.service.rest;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.hierarchiesmanagement.Hierarchies;
import it.eng.spagobi.tools.hierarchiesmanagement.HierarchiesSingleton;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Field;
import it.eng.spagobi.tools.hierarchiesmanagement.metadata.Hierarchy;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyUtils;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
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
import org.json.JSONObject;

/*
 * This class contains all REST services about the backup feature
 */

@Path("/hierarchiesBackup")
public class BackupService {

	static private Logger logger = Logger.getLogger(HierarchyService.class);

	@POST
	@Path("/modifyHierarchyBkps")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String modifyHierarchyBkps(@Context HttpServletRequest req) throws SQLException {
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

			// end transaction
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
	@Path("/restoreHierarchyBkps")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String restoreHierarchyBkps(@Context HttpServletRequest req) throws SQLException {
		// restores a backup hierarchy
		try {

			logger.debug("START");

			JSONObject requestVal = RestUtilities.readBodyAsJSONObject(req);

			String dimension = requestVal.getString("dimension");
			String hierarchyBkpName = requestVal.getString("name");

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

}
