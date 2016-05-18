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
package it.eng.spagobi.api.v2;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.DataSourceModel;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.DB;
import com.mongodb.MongoClient;

@Path("/2.0/datasources")
@ManageAuthorization
public class DataSourceResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DataSourceResource.class);

	IDataSourceDAO dataSourceDAO;
	DataSource dataSource;
	List<DataSource> dataSourceList;

	@SuppressWarnings("unchecked")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public List<DataSource> getAllDataSources() {

		logger.debug("IN");

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSourceList = dataSourceDAO.loadAllDataSources();

			return dataSourceList;

		} catch (Exception exception) {

			logger.error("Error while getting the list of DS", exception);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), exception);

		} finally {

			logger.debug("OUT");

		}
	}

	@GET
	@Path("/{dsId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public String getDataSourceById(@PathParam("dsId") Integer dsId) {

		logger.debug("IN");

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSource = dataSourceDAO.loadDataSourceByID(dsId);

			return JsonConverter.objectToJson(dataSource, null);

		} catch (Exception e) {

			logger.error("Error while loading a single data set", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String postDataSource(@Valid DataSource dataSource) {

		logger.debug("IN");

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSourceDAO.insertDataSource(dataSource, getUserProfile().getOrganization());

			DataSource newLabel = (DataSource) dataSourceDAO.loadDataSourceByLabel(dataSource.getLabel());
			int newId = newLabel.getDsId();

			return Integer.toString(newId);

		} catch (Exception exception) {

			logger.error("Error while posting DS", exception);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), exception);

		} finally {

			logger.debug("OUT");

		}
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<DataSource> putDataSource(DataSourceModel dataSource) {

		logger.debug("IN");

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();

			dataSourceDAO.setUserProfile(getUserProfile());
			IDataSource oldDataSource = dataSourceDAO.loadDataSourceWriteDefault();

			if (oldDataSource != null && dataSource.getWriteDefault() && oldDataSource.getDsId() != dataSource.getDsId()) {
				// unset the cache
				// SpagoBICacheManager.removeCache();
				oldDataSource.setWriteDefault(false);
				dataSourceDAO.modifyDataSource(oldDataSource);
			}
			dataSourceDAO.modifyDataSource(dataSource);
			return DAOFactory.getDataSourceDAO().loadAllDataSources();

		} catch (Exception e) {

			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}
	}

	@DELETE
	@Path("/{dsId}")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<DataSource> deleteDataSourceById(@PathParam("dsId") Integer dsId) throws EMFUserError {

		logger.debug("IN");

		try {

			DataSource dataSource = new DataSource();
			dataSource.setDsId(dsId);
			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSourceDAO.eraseDataSource(dataSource);

			return DAOFactory.getDataSourceDAO().loadAllDataSources();

		} catch (Exception e) {

			logger.error("Error while updating url of the new resource", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}
	}

	@DELETE
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<DataSource> deleteMultiple(@QueryParam("id") int[] ids) {

		logger.debug("IN");

		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());

			for (int i = 0; i < ids.length; i++) {
				DataSource ds = new DataSource();
				ds.setDsId(ids[i]);
				dataSourceDAO.eraseDataSource(ds);
			}

			return dataSourceDAO.loadAllDataSources();

		} catch (Exception e) {

			logger.error("Error while deleting url of the new resource", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}
	}

	@GET
	@Path("/structure/{dsId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public String getDataSourceStruct(@PathParam("dsId") Integer dsId) {

		logger.debug("IN");
		JSONObject tableContent = new JSONObject();
		try {

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSource = dataSourceDAO.loadDataSourceByID(dsId);
			Connection conn = dataSource.getConnection();

			tableContent = getTableMetadata(conn);

		} catch (Exception e) {

			logger.error("Error while loading a single data set", e);
			throw new SpagoBIRestServiceException("sbi.tools.dataset.preview.params.error", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}

		return tableContent.toString();
	}

	private static ConcurrentMap<String, JSONObject> metadataCache = new ConcurrentHashMap<>();

	private JSONObject getTableMetadata(Connection conn) throws HibernateException, JSONException {
		String metadataCacheKey = null;
		JSONObject tableContent = new JSONObject();

		try {
			DatabaseMetaData meta = conn.getMetaData();
			String userName = meta.getUserName();
			String url = meta.getURL();
			metadataCacheKey = url + "|" + userName;
			if (metadataCache.get(metadataCacheKey) != null) {
				return metadataCache.get(metadataCacheKey);
			}

			ResultSet rs = null;
			try {
				if (conn.getMetaData().getDatabaseProductName().toLowerCase().contains("oracle")) {
					String q = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE FROM ALL_TAB_COLUMNS WHERE OWNER = '" + userName + "'";
					Statement stmt = conn.createStatement();
					rs = stmt.executeQuery(q);
					while (rs.next()) {
						if (!tableContent.has(rs.getString(1))) {
							tableContent.put(rs.getString(1), new JSONObject());
						}
						tableContent.getJSONObject(rs.getString(1)).put(rs.getString(2), rs.getString(3));
					}
					rs.close();
				} else {
					final String[] TYPES = { "TABLE", "VIEW" };
					final String tableNamePattern = "%";
					final String catalog = null;
					rs = meta.getTables(catalog, null, tableNamePattern, TYPES);
					while (rs.next()) {
						String tableName = rs.getString(3);

						JSONObject column = new JSONObject();
						ResultSet tabCol = meta.getColumns(rs.getString(1), rs.getString(2), tableName, "%");
						while (tabCol.next()) {
							column.put(tabCol.getString(4), "null");
						}
						tabCol.close();
						tableContent.put(tableName, column);
					}
				}
			} finally {
				if (rs != null) {
					rs.close();
				}
				if (!conn.isClosed()) {
					conn.close();
				}
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
		metadataCache.put(metadataCacheKey, tableContent);
		return tableContent;
	}

	@POST
	@Path("/test")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String testDataSource(@Valid DataSource dataSource) throws Exception {

		logger.debug("IN");

		String url = dataSource.getUrlConnection();
		String user = dataSource.getUser();
		String pwd = dataSource.getPwd();
		String driver = dataSource.getDriver();
		String schemaAttr = dataSource.getSchemaAttribute();
		String jndi = dataSource.getJndi();

		IEngUserProfile profile = getUserProfile();

		String schema = (String) profile.getUserAttribute(schemaAttr);
		logger.debug("schema:" + schema);
		Connection connection = null;

		if (jndi != null && jndi.length() > 0) {
			String jndiName = schema == null ? jndi : jndi + schema;
			logger.debug("Lookup JNDI name:" + jndiName);
			Context ctx = new InitialContext();
			javax.sql.DataSource ds = (javax.sql.DataSource) ctx.lookup(jndiName);
			connection = ds.getConnection();
		} else {

			if (driver.toLowerCase().contains("mongo")) {
				logger.debug("Checking the connection for MONGODB");
				MongoClient mongoClient = null;
				try {
					int databaseNameStart = url.lastIndexOf("/");
					if (databaseNameStart < 0) {
						logger.error("Error connecting to the mongoDB. No database selected");
					}
					String databaseUrl = url.substring(0, databaseNameStart);
					String databaseName = url.substring(databaseNameStart + 1);

					mongoClient = new MongoClient(databaseUrl);
					DB database = mongoClient.getDB(databaseName);
					database.getCollectionNames();

					logger.debug("Connection OK");
					return new JSONObject().toString();
				} catch (Exception e) {
					logger.error("Error connecting to the mongoDB", e);
				} finally {
					if (mongoClient != null) {
						mongoClient.close();
					}
				}
			} else {
				Class.forName(driver);
				connection = DriverManager.getConnection(url, user, pwd);
			}

		}

		return new JSONObject().toString();

	}
}
