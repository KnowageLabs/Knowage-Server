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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.IDataBase;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

@Path("/2.0/datasources")
public class DataSourceResource extends AbstractSpagoBIResource {

	public static final String SERVICE_NAME = "2.0/datasources/";

	static protected Logger logger = Logger.getLogger(DataSourceResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public List<IDataSource> getDataSources(@QueryParam("type") String type) {
		logger.debug("IN");
		try {
			IDataSourceDAO dataSourceDAO;
			List<IDataSource> dataSources;
			UserProfile profile = getUserProfile();

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(profile);

			if (profile.getIsSuperadmin()) {
				dataSources = dataSourceDAO.loadDataSourcesForSuperAdmin();
			} else {
				dataSources = dataSourceDAO.loadAllDataSources();
			}

			if ("cache".equals(type)) {
				return getCacheDataSources(dataSources);
			} else if ("meta".equals(type)) {
				return getMetaDataSources(dataSources);
			} else {
				return dataSources;
			}
		} catch (Exception exception) {
			logger.error("Error while getting the list of DS", exception);
			throw new SpagoBIRestServiceException("Error while getting the list of DS", buildLocaleFromSession(), exception);
		} finally {
			logger.debug("OUT");
		}
	}

	private List<IDataSource> getMetaDataSources(List<IDataSource> datasources) throws EMFUserError, DataBaseException {
		List<IDataSource> metaDataSources = new ArrayList<>();
		for (IDataSource dataSource : datasources) {
			IDataBase database = DataBaseFactory.getDataBase(dataSource);
			if (database.isMetaSupported()) {
				metaDataSources.add(dataSource);
			}
		}
		return metaDataSources;
	}

	private List<IDataSource> getCacheDataSources(List<IDataSource> datasources) throws EMFUserError, DataBaseException {
		List<IDataSource> cacheDataSources = new ArrayList<>();
		for (IDataSource dataSource : datasources) {
			IDataBase database = DataBaseFactory.getDataBase(dataSource);
			if (database.isCacheSupported()) {
				cacheDataSources.add(dataSource);
			}
		}
		return cacheDataSources;
	}

	@GET
	@Path("/{dsId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public String getDataSourceById(@PathParam("dsId") Integer dsId) {
		logger.debug("IN");
		try {
			IDataSourceDAO dataSourceDAO;
			IDataSource dataSource;

			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			dataSource = dataSourceDAO.loadDataSourceByID(dsId);

			return JsonConverter.objectToJson(dataSource, null);
		} catch (Exception e) {
			logger.error("Error while loading a single data source", e);
			throw new SpagoBIRestServiceException("Error while loading a single data source", buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String postDataSource(IDataSource dataSource) {
		logger.debug("IN");
		try {
			IDataSourceDAO dataSourceDAO;

			logger.debug(dataSource);
			dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			IDataSource existingDS = dataSourceDAO.findDataSourceByLabel(dataSource.getLabel());

			if (existingDS != null && dataSource.getLabel().equals(existingDS.getLabel())) {
				MessageBuilder msgBuilder = new MessageBuilder();
				throw new SpagoBIRestServiceException(msgBuilder.getMessage("sbi.datasource.exists"), buildLocaleFromSession(), new Throwable());
			}

			dataSourceDAO.insertDataSource(dataSource, getUserProfile().getOrganization());

			IDataSource newLabel = dataSourceDAO.loadDataSourceByLabel(dataSource.getLabel());
			int newId = newLabel.getDsId();

			return Integer.toString(newId);

		} catch (SpagoBIRestServiceException e) {
			throw e;
		} catch (Exception exception) {
			logger.error("Error while posting DS", exception);
			throw new SpagoBIRestServiceException("Error while posting DS", buildLocaleFromSession(), exception);
		} finally {
			logger.debug("OUT");
		}
	}

	@PUT
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<IDataSource> putDataSource(IDataSource dataSource) {
		logger.debug("IN");
		try {
			IDataSourceDAO dataSourceDAO;
			dataSourceDAO = DAOFactory.getDataSourceDAO();

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
			return DAOFactory.getDataSourceDAO().loadAllDataSources();
		} catch (Exception e) {
			logger.error("Error while updating data source", e);
			throw new SpagoBIRestServiceException("Error while updating data source", buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@DELETE
	@Path("/{dsId}")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<IDataSource> deleteDataSourceById(@PathParam("dsId") Integer dsId) throws EMFUserError {

		logger.debug("IN");

		// if the ds is associated with any BIEngine or BIObjects, creates an error
		Map<String, List<String>> objNames = DAOFactory.getDataSourceDAO().returnEntitiesAssociated(dsId);

		if (objNames.size() > 0) {
			logger.warn("datasource is in use, build message");

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
			dataSourceDAO.eraseDataSource(dataSource);

			return DAOFactory.getDataSourceDAO().loadAllDataSources();

		} catch (Exception e) {

			logger.error("Error while deleting data source", e);
			throw new SpagoBIRestServiceException("Error while deleting data source", buildLocaleFromSession(), e);

		} finally {

			logger.debug("OUT");

		}
	}

	@DELETE
	@Path("/")
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public List<IDataSource> deleteMultiple(@QueryParam("id") List<Integer> ids) {
		logger.debug("IN");
		try {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());

			for (int i = 0; i < ids.size(); i++) {
				IDataSource ds = DataSourceFactory.getDataSource();
				ds.setDsId(ids.get(i));
				dataSourceDAO.eraseDataSource(ds);
			}
			return dataSourceDAO.loadAllDataSources();
		} catch (Exception e) {
			logger.error("Error while deleting multiple data sources", e);
			throw new SpagoBIRestServiceException("Error while deleting multiple data sources", buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/structure/{dsId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_READ })
	public String getDataSourceStruct(@PathParam("dsId") Integer dsId, @QueryParam("tablePrefixLike") String tablePrefixLike,
			@QueryParam("tablePrefixNotLike") String tablePrefixNotLike) {

		logger.debug("IN");
		JSONObject tableContent = new JSONObject();
		try {

			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			dataSourceDAO.setUserProfile(getUserProfile());
			IDataSource dataSource = dataSourceDAO.loadDataSourceByID(dsId);

			IDataBase dataBase = DataBaseFactory.getDataBase(dataSource);
			Map<String, Map<String, String>> structure = dataBase.getStructure(tablePrefixLike, tablePrefixNotLike);

			tableContent = new JSONObject(structure);

		} catch (Exception e) {
			logger.error("Error while getting structure of data source by id", e);
			throw new SpagoBIRestServiceException("Error while getting structure of data source by id", buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
		return tableContent.toString();
	}

	@POST
	@Path("/test")
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String testDataSource(IDataSource dataSource) throws Exception {

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

		if (jndi != null && jndi.length() > 0) {
			String jndiName = schema == null ? jndi : jndi + schema;

			try {
				logger.debug("Lookup JNDI name:" + jndiName);
				Context ctx = new InitialContext();
				javax.sql.DataSource ds = (javax.sql.DataSource) ctx.lookup(jndiName);
				try (Connection connection = ds.getConnection()) {
					logger.debug("Connection performed successfully");
				}
			} catch (AuthenticationException e) {
				logger.error("Error while attempting to reacquire the authentication information on provided JNDI name", e);
				throw new SpagoBIServiceException(SERVICE_NAME, e);
			} catch (NamingException e) {
				logger.error("Error with provided JNDI name. Can't find the database with that name.", e);
				throw new SpagoBIServiceException(SERVICE_NAME, e);
			} catch (Exception e) {
				logger.error("Error with provided JNDI name.", e);
				throw new SpagoBIServiceException(SERVICE_NAME, e);
			}

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

				try {
					Class.forName(driver);
				} catch (ClassNotFoundException e) {
					logger.error("Driver not found", e);
					throw new SpagoBIRestServiceException("Driver not found: " + driver, buildLocaleFromSession(), e);
				}

				try (Connection connection = DriverManager.getConnection(url, user, pwd)) {
					logger.debug("Connection performed successfully");
				}

			}

		}

		return new JSONObject().toString();

	}
}
