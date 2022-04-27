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

package it.eng.spagobi.tools.datasource.service.rest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
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

import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect;
import it.eng.spagobi.tools.datasource.bo.DataSourceFactory;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
@Path("/datasources")
public class DataSourceCRUD extends AbstractSpagoBIResource {

	static private Logger logger = Logger.getLogger(DataSourceCRUD.class);
	static private String deleteNullIdDataSourceError = "error.mesage.description.data.source.cannot.be.null";
	static private String deleteInUseDSError = "error.mesage.description.data.source.deleting.inuse";
	static private String canNotFillResponseError = "error.mesage.description.generic.can.not.responce";
	static private String saveDuplicatedDSError = "error.mesage.description.data.source.saving.duplicated";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String getAllDataSources(@Context HttpServletRequest req, @QueryParam("onlySqlLike") boolean onlySqlLike) {
		IDataSourceDAO dataSourceDao = null;
		IDomainDAO domaindao = null;
		List<IDataSource> dataSources;
		List<Domain> dialects = null;
		UserProfile profile = getUserProfile();
		JSONObject datasorcesJSON = new JSONObject();
		try {
			dataSourceDao = DAOFactory.getDataSourceDAO();
			dataSourceDao.setUserProfile(profile);
			if (profile.getIsSuperadmin() != null && profile.getIsSuperadmin()) {
				TenantManager.unset();
				dataSources = dataSourceDao.loadDataSourcesForSuperAdmin();
			} else {
				dataSourceDao.setUserProfile(profile);
				dataSources = dataSourceDao.loadAllDataSources();
			}

			if (onlySqlLike) {
				Iterator<IDataSource> dataSourceIterator = dataSources.iterator();
				while (dataSourceIterator.hasNext()) {
					DatabaseDialect dialect = DataBaseFactory.getDataBase(dataSourceIterator.next()).getDatabaseDialect();
					if (!dialect.isSqlLike()) {
						dataSourceIterator.remove();
					}

				}
			}

			domaindao = DAOFactory.getDomainDAO();
			dialects = domaindao.loadListDomainsByType("DIALECT_HIB");

			datasorcesJSON = serializeDatasources(dataSources, dialects);

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while instatiating the dao", t);
		}

		return datasorcesJSON.toString();

	}

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String deleteDataSource(@Context HttpServletRequest req) {
		IEngUserProfile profile = getUserProfile();
		HashMap<String, String> logParam = new HashMap();

		try {
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			String id = ((Integer) requestBodyJSON.opt("DATASOURCE_ID")).toString();

			Assert.assertNotNull(id, deleteNullIdDataSourceError);
			// if the ds is associated with any BIEngine or BIObjects, creates
			// an error
			boolean bObjects = DAOFactory.getDataSourceDAO().hasBIObjAssociated(id);
			// boolean bEngines =
			// DAOFactory.getDataSourceDAO().hasBIEngineAssociated(id);
			if (bObjects) { // || bEngines) {
				HashMap params = new HashMap();
				logger.debug(deleteInUseDSError);
				updateAudit(req, profile, "DATA_SOURCE.DELETE", null, "ERR");
				return (ExceptionUtilities.serializeException(deleteInUseDSError, null));
			}

			IDataSource ds = DAOFactory.getDataSourceDAO().loadDataSourceByID(new Integer(id));

			// it is necessary to clean the cache otherwise SpagoBI will look
			// for dataset that are
			// not in cache anymore since the caching db is changed
			if (ds.checkIsWriteDefault()) {
				ICache cache = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
				// unset the cache
				cache.deleteAll();
			}
			DAOFactory.getDataSourceDAO().eraseDataSource(ds);
			logParam.put("TYPE", ds.getJndi());
			logParam.put("NAME", ds.getLabel());
			updateAudit(req, profile, "DATA_SOURCE.DELETE", null, "OK");
			return ("");
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SOURCE.DELETE", null, "ERR");
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DATASOURCE_MANAGEMENT })
	public String saveDataSource(@Context HttpServletRequest req) {
		UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		try {

			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			IDataSourceDAO dao = DAOFactory.getDataSourceDAO();

			if (profile.getIsSuperadmin()) {
				TenantManager.unset();
				dao.setUserID(profile.getUserId().toString());
			} else {
				dao.setUserProfile(profile);
			}

			IDataSource dsNew = recoverDataSourceDetails(requestBodyJSON);

			HashMap<String, String> logParam = new HashMap();
			logParam.put("JNDI", dsNew.getJndi());
			logParam.put("NAME", dsNew.getLabel());
			logParam.put("URL", dsNew.getUrlConnection());

			if (dsNew.getDsId() == -1) {
				// if a ds with the same label not exists on db ok else error
				if (DAOFactory.getDataSourceDAO().loadDataSourceByLabel(dsNew.getLabel()) != null) {
					updateAudit(req, profile, "DATA_SOURCE.ADD", logParam, "KO");
					throw new SpagoBIRuntimeException(saveDuplicatedDSError);
				}
				Integer id = dao.insertDataSource(dsNew, profile.getOrganization());
				dsNew.setDsId(id);
				// it is necessary to clean the cache otherwise SpagoBI will
				// look for dataset that are
				// not in cache yet since the caching db is changed
				if (dsNew.checkIsWriteDefault()) {
					ICache cache = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
					// unset the cache
					cache.deleteAll();
				}
				updateAudit(req, profile, "DATA_SOURCE.ADD", logParam, "OK");
			} else {
				IDataSource dsOld = DAOFactory.getDataSourceDAO().loadDataSourceByID(dsNew.getDsId());
				// update ds
				dao.modifyDataSource(dsNew);

				// logical XOR operator -> it is true only if one has true
				// value, but not both
				boolean isWriteDefaultChanged = dsNew.checkIsWriteDefault() ^ dsOld.checkIsWriteDefault();

				// it is necessary to clean the cache otherwise SpagoBI will
				// look for dataset that are
				// not in cache yet since the caching db is changed
				if (isWriteDefaultChanged) {
					ICache cache = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
					// unset the cache
					cache.deleteAll();
				}
				updateAudit(req, profile, "DATA_SOURCE.MODIFY", logParam, "OK");
			}

			return ("{DATASOURCE_ID:" + dsNew.getDsId() + " }");
		} catch (SpagoBIRuntimeException ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SOURCE.DELETE", null, "ERR");
			logger.debug(ex.getMessage());
			try {
				return (ExceptionUtilities.serializeException(ex.getMessage(), null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			updateAudit(req, profile, "DATA_SOURCE.DELETE", null, "ERR");
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}
	}

	private static void updateAudit(HttpServletRequest request, IEngUserProfile profile, String action_code, HashMap<String, String> parameters, String esito) {
		try {
			AuditLogUtilities.updateAudit(request, profile, action_code, parameters, esito);
		} catch (Exception e) {
			logger.debug("Error writnig audit", e);
		}
	}

	private JSONObject serializeDatasources(List<IDataSource> dataSources, List<Domain> dialects) throws SerializationException, JSONException {

		JSONObject dataSourcesJSON = new JSONObject();
		// JSONObject aDataSourcesJSON = new JSONObject();
		JSONArray dataSourcesJSONArray = new JSONArray();
		JSONArray dialectsJSONArray = new JSONArray();
		if (dataSources != null) {
			dataSourcesJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(dataSources, null);
			dataSourcesJSON.put("root", dataSourcesJSONArray);
			// aDataSourcesJSON = dataSourcesJSONArray.getJSONObject(0);
			// Iterator<String> iter = aDataSourcesJSON.keys();
		}
		if (dialects != null) {
			dialectsJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(dialects, null);
			dataSourcesJSON.put("dialects", dialectsJSONArray);
		}
		return dataSourcesJSON;
	}

	private IDataSource recoverDataSourceDetails(JSONObject requestBodyJSON) throws EMFUserError, SourceBeanException, IOException {
		IDataSource ds = DataSourceFactory.getDataSource();
		Integer id = -1;
		String idStr = (String) requestBodyJSON.opt("DATASOURCE_ID");
		if (idStr != null && !idStr.equals("")) {
			id = new Integer(idStr);
		}
		String dialectName = requestBodyJSON.optString("DIALECT_NAME");

		String description = requestBodyJSON.optString("DESCRIPTION");
		String label = requestBodyJSON.optString("DATASOURCE_LABEL");
		String jndi = requestBodyJSON.optString("JNDI_URL");
		String url = requestBodyJSON.optString("CONNECTION_URL");
		String user = requestBodyJSON.optString("USER");
		String pwd = requestBodyJSON.optString("PASSWORD");
		String driver = requestBodyJSON.optString("DRIVER");
		String schemaAttr = requestBodyJSON.optString("SCHEMA");
		String multiSchema = requestBodyJSON.optString("MULTISCHEMA");

		Boolean readOnly = requestBodyJSON.optBoolean("READ_ONLY");

		Boolean writeDefault = requestBodyJSON.optBoolean("WRITE_DEFAULT");

		Boolean useForDataprep = requestBodyJSON.optBoolean("USE_FOR_DATAPREP");

		Boolean isMultiSchema = false;
		if (multiSchema != null && (multiSchema.equals("on") || multiSchema.equals("true"))) {
			isMultiSchema = true;
		}

		ds.setDsId(id.intValue());
		ds.setDialectName(dialectName);
		ds.setLabel(label);
		ds.setDescr(description);
		ds.setJndi(jndi);
		ds.setUrlConnection(url);
		ds.setUser(user);
		ds.setPwd(pwd);
		ds.setDriver(driver);
		ds.setSchemaAttribute(schemaAttr);
		ds.setMultiSchema(isMultiSchema);
		ds.setReadOnly(readOnly);
		ds.setWriteDefault(writeDefault);
		ds.setUseForDataprep(useForDataprep);

		return ds;
	}
}
