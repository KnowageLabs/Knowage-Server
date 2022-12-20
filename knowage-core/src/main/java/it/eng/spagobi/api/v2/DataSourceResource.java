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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
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
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.metasql.query.SelectQuery;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.bo.JDBCDataSourcePoolConfiguration;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.IDataBase;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/2.0/datasources")
public class DataSourceResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = LogManager.getLogger(DataSourceResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public List<IDataSource> getDataSources(@QueryParam("type") String type) {
		LOGGER.debug("IN");
		try {
			List<IDataSource> dataSources = getDataSources();

			if ("cache".equals(type)) {
				return getCacheDataSources(dataSources);
			} else if ("meta".equals(type)) {
				return getMetaDataSources(dataSources);
			} else {
				return dataSources;
			}
		} catch (Exception exception) {
			LOGGER.error("Error while getting the list of DS", exception);
			throw new SpagoBIRestServiceException("Error while getting the list of DS", buildLocaleFromSession(), exception);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@GET
	@Path("/{dsId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public IDataSource getDataSourceById(@PathParam("dsId") Integer dsId) {
		LOGGER.debug("IN");
		try {
			IDataSourceDAO dataSourceDAO;
			IDataSource dataSource;

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSource = dataSourceDAO.loadDataSourceByID(dsId);

			checkAuthorizationToManageCacheDataSource(dataSource);

			return dataSource;
		} catch (Exception e) {
			LOGGER.error("Error while loading a single data source", e);
			throw new SpagoBIRestServiceException("Error while loading a single data source", buildLocaleFromSession(), e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String postDataSource(IDataSource dataSource) {
		LOGGER.debug("IN");
		try {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			UserProfile userProfile = getUserProfile();

			dataSourceDAO.setUserProfile(userProfile);

			IDataSource existingDS = dataSourceDAO.findDataSourceByLabel(dataSource.getLabel());

			if (existingDS != null && dataSource.getLabel().equals(existingDS.getLabel())) {
				MessageBuilder msgBuilder = new MessageBuilder();
				throw new SpagoBIRestServiceException(msgBuilder.getMessage("sbi.datasource.exists"), buildLocaleFromSession(), new Throwable());
			}

			checkAuthorizationToManageCacheDataSource(dataSource);

			dataSourceDAO.insertDataSource(dataSource, userProfile.getOrganization());

			IDataSource newLabel = dataSourceDAO.loadDataSourceByLabel(dataSource.getLabel());
			int newId = newLabel.getDsId();

			return Integer.toString(newId);

		} catch (SpagoBIRestServiceException e) {
			throw e;
		} catch (Exception exception) {
			LOGGER.error("Error while posting DS", exception);
			throw new SpagoBIRestServiceException("Error while posting DS", buildLocaleFromSession(), exception);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<IDataSource> putDataSource(IDataSource dataSource) {
		LOGGER.debug("IN");
		try {
			checkAuthorizationToManageCacheDataSource(dataSource);

			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();

			dataSourceDAO.setUserProfile(getUserProfile());
			IDataSource oldDataSource = dataSourceDAO.loadDataSourceWriteDefault();

			if (oldDataSource != null && dataSource.checkIsWriteDefault() && oldDataSource.getDsId() != dataSource.getDsId()) {
				oldDataSource.setWriteDefault(false);
				dataSourceDAO.modifyDataSource(oldDataSource);
			}
			if (oldDataSource != null && dataSource.checkUseForDataprep() && oldDataSource.getDsId() != dataSource.getDsId()) {
				oldDataSource.setUseForDataprep(false);
				dataSourceDAO.modifyDataSource(oldDataSource);
			}
			dataSourceDAO.modifyDataSource(dataSource);
			return getDataSources();
		} catch (Exception e) {
			LOGGER.error("Error while updating data source", e);
			throw new SpagoBIRestServiceException("Error while updating data source", buildLocaleFromSession(), e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@DELETE
	@Path("/{dsId}")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<IDataSource> deleteDataSourceById(@PathParam("dsId") Integer dsId) throws EMFUserError {

		LOGGER.debug("IN");

		// if the ds is associated with any BIEngine or BIObjects, creates an error
		Map<String, List<String>> objNames = DAOFactory.getDataSourceDAO().returnEntitiesAssociated(dsId);

		if (objNames.size() > 0) {
			LOGGER.warn("datasource is in use, build message");

			String[] dependsBy = new String[] { "sbi.datasource.usedby.biobject", "sbi.datasource.usedby.metamodel", "sbi.datasource.usedby.dataset",
					"sbi.datasource.usedby.lov" };

			String message = "";
			MessageBuilder msgBuild = new MessageBuilder();
			Locale locale = buildLocaleFromSession();

			for (int j = 0; j < dependsBy.length; j++) {
				String key = dependsBy[j];
				String translatedKey = msgBuild.getMessage(key, locale);
				if (objNames.get(key) != null) {
					int i = 0;
					for (Iterator iterator = objNames.get(key).iterator(); iterator.hasNext();) {
						String objName = (String) iterator.next();
						if (i == 0) {
							message += translatedKey + " ( " + objName;
						} else {
							message += ", " + objName;
						}
						if (i == objNames.get(key).size() - 1) {
							message += ") ";
						}
						i++;
					}

					message += "\n";
				}
			}

			throw new SpagoBIRestServiceException(msgBuild.getMessage("sbi.datasource.usedby", locale) + message, locale, new Exception());
		}

		try {
			IDataSource dataSource = DataSourceFactory.getDataSource();
			dataSource.setDsId(dsId);
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());

			checkAuthorizationToManageCacheDataSource(dataSource);

			dataSourceDAO.eraseDataSource(dataSource);

			return getDataSources();

		} catch (Exception e) {

			LOGGER.error("Error while deleting data source", e);
			throw new SpagoBIRestServiceException("Error while deleting data source", buildLocaleFromSession(), e);

		} finally {

			LOGGER.debug("OUT");

		}
	}

	@DELETE
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<IDataSource> deleteMultiple(@QueryParam("id") List<Integer> ids) {
		LOGGER.debug("IN");
		try {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());

			for (int i = 0; i < ids.size(); i++) {
				IDataSource ds = DataSourceFactory.getDataSource();
				ds.setDsId(ids.get(i));

				checkAuthorizationToManageCacheDataSource(ds);

				dataSourceDAO.eraseDataSource(ds);
			}
			return getDataSources();
		} catch (Exception e) {
			LOGGER.error("Error while deleting multiple data sources", e);
			throw new SpagoBIRestServiceException("Error while deleting multiple data sources", buildLocaleFromSession(), e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@GET
	@Path("/structure/{dsId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public String getDataSourceStruct(@PathParam("dsId") Integer dsId, @QueryParam("tablePrefixLike") String tablePrefixLike,
			@QueryParam("tablePrefixNotLike") String tablePrefixNotLike) {

		LOGGER.debug("IN");
		JSONObject tableContent = new JSONObject();
		try {

			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			IDataSource dataSource = dataSourceDAO.loadDataSourceByID(dsId);

			checkAuthorizationToManageCacheDataSource(dataSource);

			IDataBase dataBase = DataBaseFactory.getDataBase(dataSource);
			Map<String, Map<String, String>> structure = dataBase.getStructure(tablePrefixLike, tablePrefixNotLike);

			tableContent = new JSONObject(structure);

		} catch (Exception e) {
			LOGGER.error("Error while getting structure of data source by id", e);
			throw new SpagoBIRestServiceException("Error while getting structure of data source by id", buildLocaleFromSession(), e);
		} finally {
			LOGGER.debug("OUT");
		}
		return tableContent.toString();
	}

	@POST
	@Path("/test")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public Response testDataSource(IDataSource dataSource) throws Exception {

		LOGGER.debug("IN");

		String url = dataSource.getUrlConnection();
		String user = dataSource.getUser();
		String pwd = dataSource.getPwd();
		String driver = dataSource.getDriver();
		String schemaAttr = dataSource.getSchemaAttribute();
		String jndi = dataSource.getJndi();

		IEngUserProfile profile = getUserProfile();

		String schema = (String) profile.getUserAttribute(schemaAttr);
		LOGGER.debug("schema: {}", schema);

		if (jndi != null && jndi.length() > 0) {
			String jndiName = schema == null ? jndi : jndi + schema;

			try {
				LOGGER.debug("Lookup JNDI name: {}", jndiName);
				Context ctx = new InitialContext();
				javax.sql.DataSource ds = (javax.sql.DataSource) ctx.lookup(jndiName);
				try (Connection connection = ds.getConnection()) {
					LOGGER.debug("Connection performed successfully");
				}
			} catch (AuthenticationException e) {
				LOGGER.error("Error while attempting to reacquire the authentication information on provided JNDI name", e);
				throw new SpagoBIServiceException("Authentication error", e);
			} catch (NamingException e) {
				LOGGER.error("Error with provided JNDI name. Can't find the database with that name.", e);
				throw new SpagoBIServiceException("JNDI error", e);
			} catch (Exception e) {
				LOGGER.error("Error with provided JNDI name.", e);
				throw new SpagoBIServiceException("Generic exception", e);
			}

		} else {

			if (driver.toLowerCase().contains("mongo")) {
				LOGGER.debug("Checking the connection for MONGODB");
				int databaseNameStart = url.lastIndexOf("/");
				if (databaseNameStart < 0) {
					LOGGER.error("Error connecting to the mongoDB. No database selected");
				}
				String databaseUrl = url.substring(0, databaseNameStart);
				String databaseName = url.substring(databaseNameStart + 1);

				try(MongoClient mongoClient = new MongoClient(databaseUrl)) {
					MongoDatabase database = mongoClient.getDatabase(databaseName);
					database.listCollectionNames();
					LOGGER.debug("Connection OK");
					return Response.ok().build();
				} catch (Exception e) {
					LOGGER.error("Error connecting to the mongoDB", e);
				} finally {
				}
			} else {
				try {
					Class.forName(driver);
				} catch (ClassNotFoundException e) {
					LOGGER.error("Driver not found", e);
					throw new SpagoBIRestServiceException("Driver not found: " + driver, buildLocaleFromSession(), e);
				}

				try (Connection connection = DriverManager.getConnection(url, user, pwd)) {
					LOGGER.debug("Connection performed successfully");
				}
			}

		}

		return Response.ok().build();

	}

	private List<IDataSource> getDataSources() throws EMFUserError {
		IDataSourceDAO dataSourceDAO;
		List<IDataSource> dataSources;
		UserProfile profile = getUserProfile();

		dataSourceDAO = DAOFactory.getDataSourceDAO();
		dataSourceDAO.setUserProfile(profile);

		if (Boolean.TRUE.equals(profile.getIsSuperadmin())) {
			dataSources = dataSourceDAO.loadDataSourcesForSuperAdmin();
		} else {
			// @formatter:off
			dataSources = dataSourceDAO.loadAllDataSources()
					.stream()
					// Remove cache datasource for non-super-admin users
					.filter(e -> !e.checkIsWriteDefault())
					.map(this::toOwnedOrNotOwned)
					.collect(Collectors.toList());
			// @formatter:on
		}
		return dataSources;
	}

	private List<IDataSource> getMetaDataSources(List<IDataSource> datasources) throws DataBaseException {
		List<IDataSource> metaDataSources = new ArrayList<>();
		for (IDataSource dataSource : datasources) {
			IDataBase database = DataBaseFactory.getDataBase(dataSource);
			if (database.isMetaSupported()) {
				metaDataSources.add(dataSource);
			}
		}
		return metaDataSources;
	}

	private List<IDataSource> getCacheDataSources(List<IDataSource> datasources) throws DataBaseException {
		List<IDataSource> cacheDataSources = new ArrayList<>();
		for (IDataSource dataSource : datasources) {
			IDataBase database = DataBaseFactory.getDataBase(dataSource);
			if (database.isCacheSupported()) {
				cacheDataSources.add(dataSource);
			}
		}
		return cacheDataSources;
	}

	private void checkAuthorizationToManageCacheDataSource(IDataSource dataSource) {
		UserProfile userProfile = getUserProfile();

		if (Boolean.TRUE.equals(!userProfile.getIsSuperadmin()) && Boolean.TRUE.equals(dataSource.checkIsWriteDefault())) {
			MessageBuilder msgBuilder = new MessageBuilder();
			throw new SpagoBIRestServiceException(msgBuilder.getMessage("sbi.datasource.notAuthorizedToManageCacheDataSource"), buildLocaleFromSession(), new Throwable());
		}
	}

	private IDataSource toOwnedOrNotOwned(IDataSource dataSource) {

		if (!isOwnedByTheUser(dataSource)) {
			dataSource = new _NotOwnedDataSource(dataSource);
		}

		return dataSource;
	}

	private boolean isOwnedByTheUser(IDataSource dataSource) {
		return getUserProfile().getUserName().toString().equals(dataSource.getOwner());
	}

}

@JsonInclude(Include.NON_NULL)
class _NotOwnedDataSource implements IDataSource {

	private final IDataSource wrapped;

	public _NotOwnedDataSource(IDataSource wrapped) {
		this.wrapped = wrapped;
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#toSpagoBiDataSource()
	 */
	@Override
	@JsonIgnore
	public SpagoBiDataSource toSpagoBiDataSource() {
		return wrapped.toSpagoBiDataSource();
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#checkIsMultiSchema()
	 */
	@Override
	public boolean checkIsMultiSchema() {
		return wrapped.checkIsMultiSchema();
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#checkIsJndi()
	 */
	@Override
	public boolean checkIsJndi() {
		return wrapped.checkIsJndi();
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getSchemaAttribute()
	 */
	@Override
	@JsonIgnore
	public String getSchemaAttribute() {
		return wrapped.getSchemaAttribute();
	}

	/**
	 * @param schemaAttribute
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setSchemaAttribute(java.lang.String)
	 */
	@Override
	public void setSchemaAttribute(String schemaAttribute) {
		wrapped.setSchemaAttribute(schemaAttribute);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getMultiSchema()
	 */
	@Override
	@JsonIgnore
	public Boolean getMultiSchema() {
		return wrapped.getMultiSchema();
	}

	/**
	 * @param multiSchema
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setMultiSchema(java.lang.Boolean)
	 */
	@Override
	public void setMultiSchema(Boolean multiSchema) {
		wrapped.setMultiSchema(multiSchema);
	}

	/**
	 * @return
	 * @throws NamingException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getConnection()
	 */
	@Override
	@JsonIgnore
	public Connection getConnection() throws NamingException, SQLException, ClassNotFoundException {
		return wrapped.getConnection();
	}

	/**
	 * @param profile
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getConnectionByUserProfile(it.eng.spago.security.IEngUserProfile)
	 */
	@Override
	@JsonIgnore
	public Connection getConnectionByUserProfile(IEngUserProfile profile) {
		return wrapped.getConnectionByUserProfile(profile);
	}

	/**
	 * @param profile
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getJNDIRunTime(it.eng.spago.security.IEngUserProfile)
	 */
	@Override
	@JsonIgnore
	public String getJNDIRunTime(IEngUserProfile profile) {
		return wrapped.getJNDIRunTime(profile);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDsId()
	 */
	@Override
	public int getDsId() {
		return wrapped.getDsId();
	}

	/**
	 * @param dsId
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDsId(int)
	 */
	@Override
	public void setDsId(int dsId) {
		wrapped.setDsId(dsId);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDescr()
	 */
	@Override
	public String getDescr() {
		return wrapped.getDescr();
	}

	/**
	 * @param descr
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDescr(java.lang.String)
	 */
	@Override
	public void setDescr(String descr) {
		wrapped.setDescr(descr);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getLabel()
	 */
	@Override
	public String getLabel() {
		return wrapped.getLabel();
	}

	/**
	 * @param label
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setLabel(java.lang.String)
	 */
	@Override
	public void setLabel(String label) {
		wrapped.setLabel(label);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getJndi()
	 */
	@Override
	@JsonIgnore
	public String getJndi() {
		return wrapped.getJndi();
	}

	/**
	 * @param jndi
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setJndi(java.lang.String)
	 */
	@Override
	public void setJndi(String jndi) {
		wrapped.setJndi(jndi);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getUrlConnection()
	 */
	@Override
	@JsonIgnore
	public String getUrlConnection() {
		return wrapped.getUrlConnection();
	}

	/**
	 * @param url_connection
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setUrlConnection(java.lang.String)
	 */
	@Override
	public void setUrlConnection(String url_connection) {
		wrapped.setUrlConnection(url_connection);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getUser()
	 */
	@Override
	@JsonIgnore
	public String getUser() {
		return wrapped.getUser();
	}

	/**
	 * @param user
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setUser(java.lang.String)
	 */
	@Override
	public void setUser(String user) {
		wrapped.setUser(user);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getPwd()
	 */
	@Override
	@JsonIgnore
	public String getPwd() {
		return wrapped.getPwd();
	}

	/**
	 * @param pwd
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setPwd(java.lang.String)
	 */
	@Override
	public void setPwd(String pwd) {
		wrapped.setPwd(pwd);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDriver()
	 */
	@Override
	@JsonIgnore
	public String getDriver() {
		return wrapped.getDriver();
	}

	/**
	 * @param driver
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDriver(java.lang.String)
	 */
	@Override
	public void setDriver(String driver) {
		wrapped.setDriver(driver);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getDialectName()
	 */
	@Override
	public String getDialectName() {
		return wrapped.getDialectName();
	}

	/**
	 * @param dialectName
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setDialectName(java.lang.String)
	 */
	@Override
	public void setDialectName(String dialectName) {
		wrapped.setDialectName(dialectName);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getEngines()
	 */
	@Override
	@JsonIgnore
	public Set getEngines() {
		return wrapped.getEngines();
	}

	/**
	 * @param engines
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setEngines(java.util.Set)
	 */
	@Override
	public void setEngines(Set engines) {
		wrapped.setEngines(engines);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getObjects()
	 */
	@Override
	@JsonIgnore
	public Set getObjects() {
		return wrapped.getObjects();
	}

	/**
	 * @param objects
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setObjects(java.util.Set)
	 */
	@Override
	public void setObjects(Set objects) {
		wrapped.setObjects(objects);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getJdbcPoolConfiguration()
	 */
	@Override
	@JsonIgnore
	public JDBCDataSourcePoolConfiguration getJdbcPoolConfiguration() {
		return wrapped.getJdbcPoolConfiguration();
	}

	/**
	 * @param jDBCPoolConfiguration
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setJdbcPoolConfiguration(it.eng.spagobi.tools.datasource.bo.JDBCDataSourcePoolConfiguration)
	 */
	@Override
	public void setJdbcPoolConfiguration(JDBCDataSourcePoolConfiguration jDBCPoolConfiguration) {
		wrapped.setJdbcPoolConfiguration(jDBCPoolConfiguration);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getOwner()
	 */
	@Override
	@JsonIgnore
	public String getOwner() {
		return wrapped.getOwner();
	}

	/**
	 * @param owner
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setOwner(java.lang.String)
	 */
	@Override
	public void setOwner(String owner) {
		wrapped.setOwner(owner);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getHibDialectClass()
	 */
	@Override
	@JsonIgnore
	public String getHibDialectClass() {
		return wrapped.getHibDialectClass();
	}

	/**
	 * @param hibDialectClass
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setHibDialectClass(java.lang.String)
	 */
	@Override
	public void setHibDialectClass(String hibDialectClass) {
		wrapped.setHibDialectClass(hibDialectClass);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#checkIsReadOnly()
	 */
	@Override
	@JsonIgnore
	public Boolean checkIsReadOnly() {
		return wrapped.checkIsReadOnly();
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#checkIsWriteDefault()
	 */
	@Override
	@JsonIgnore
	public Boolean checkIsWriteDefault() {
		return wrapped.checkIsWriteDefault();
	}

	/**
	 * @param writeDefault
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setWriteDefault(java.lang.Boolean)
	 */
	@Override
	public void setWriteDefault(Boolean writeDefault) {
		wrapped.setWriteDefault(writeDefault);
	}

	/**
	 * @param readOnly
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setReadOnly(java.lang.Boolean)
	 */
	@Override
	public void setReadOnly(Boolean readOnly) {
		wrapped.setReadOnly(readOnly);
	}

	/**
	 * @param statement
	 * @param start
	 * @param limit
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#executeStatement(java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public IDataStore executeStatement(String statement, Integer start, Integer limit) {
		return wrapped.executeStatement(statement, start, limit);
	}

	/**
	 * @param statement
	 * @param start
	 * @param limit
	 * @param calculateTotalResultsNumber
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#executeStatement(java.lang.String, java.lang.Integer, java.lang.Integer, boolean)
	 */
	@Override
	public IDataStore executeStatement(String statement, Integer start, Integer limit, boolean calculateTotalResultsNumber) {
		return wrapped.executeStatement(statement, start, limit, calculateTotalResultsNumber);
	}

	/**
	 * @param statement
	 * @param start
	 * @param limit
	 * @param maxRowCount
	 * @param calculateTotalResultsNumber
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#executeStatement(java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.Integer, boolean)
	 */
	@Override
	public IDataStore executeStatement(String statement, Integer start, Integer limit, Integer maxRowCount, boolean calculateTotalResultsNumber) {
		return wrapped.executeStatement(statement, start, limit, maxRowCount, calculateTotalResultsNumber);
	}

	/**
	 * @param selectQuery
	 * @param start
	 * @param limit
	 * @param maxRowCount
	 * @param calculateTotalResultsNumber
	 * @return
	 * @throws DataBaseException
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#executeStatement(it.eng.spagobi.tools.dataset.metasql.query.SelectQuery, java.lang.Integer, java.lang.Integer, java.lang.Integer, boolean)
	 */
	@Override
	public IDataStore executeStatement(SelectQuery selectQuery, Integer start, Integer limit, Integer maxRowCount, boolean calculateTotalResultsNumber)
			throws DataBaseException {
		return wrapped.executeStatement(selectQuery, start, limit, maxRowCount, calculateTotalResultsNumber);
	}

	/**
	 * @param profile
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getSignature(it.eng.spago.security.IEngUserProfile)
	 */
	@Override
	@JsonIgnore
	public String getSignature(IEngUserProfile profile) {
		return wrapped.getSignature(profile);
	}

	/**
	 * @param useForDataprep
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#setUseForDataprep(java.lang.Boolean)
	 */
	@Override
	public void setUseForDataprep(Boolean useForDataprep) {
		wrapped.setUseForDataprep(useForDataprep);
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#checkUseForDataprep()
	 */
	@Override
	public Boolean checkUseForDataprep() {
		return wrapped.checkUseForDataprep();
	}

	/**
	 * @return
	 * @see it.eng.spagobi.tools.datasource.bo.IDataSource#getReadOnly()
	 */
	@Override
	@JsonIgnore
	public Boolean getReadOnly() {
		return wrapped.getReadOnly();
	}

}

