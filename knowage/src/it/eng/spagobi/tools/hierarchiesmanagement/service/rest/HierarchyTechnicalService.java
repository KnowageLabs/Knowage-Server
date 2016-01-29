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
import it.eng.spagobi.tools.hierarchiesmanagement.utils.HierarchyConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.Iterator;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/*
 * This class contains all REST services used TECHNICAL hierarchy types
 */

@Path("/hierarchiesTechnical")
public class HierarchyTechnicalService {

	static private Logger logger = Logger.getLogger(HierarchyService.class);

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
				JSONObject hierarchy = new JSONObject();
				hierarchy.put(HierarchyConstants.HIER_CD, hierarchyCode);
				hierarchy.put(HierarchyConstants.HIER_NM, hierarchyName);
				hierarchy.put(HierarchyConstants.HIER_TP, hierarchyType);
				hierarchy.put(HierarchyConstants.HIER_DS, hierarchyDescription);
				hierarchiesJSONArray.put(hierarchy);

			}

		} catch (Throwable t) {
			logger.error("An unexpected error occured while retriving custom hierarchies names");
			throw new SpagoBIServiceException("An unexpected error occured while retriving custom hierarchies names", t);
		}
		logger.debug("END");
		return hierarchiesJSONArray.toString();
	}
}
