/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.datasource.service.rest;

import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
@Path("/datasources")
public class DataSourceCRUD {

	static private Logger logger = Logger.getLogger(DataSourceCRUD.class);
	static private String deleteNullIdDataSourceError = "error.mesage.description.data.source.cannot.be.null";
	static private String deleteInUseDSError = "error.mesage.description.data.source.deleting.inuse";
	static private String canNotFillResponseError = "error.mesage.description.generic.can.not.responce";
	static private String saveDuplicatedDSError = "error.mesage.description.data.source.saving.duplicated";

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllDataSources(@Context HttpServletRequest req) {
		IDataSourceDAO dataSourceDao = null;
		IDomainDAO domaindao = null;
		List<DataSource> dataSources;
		List<Domain> dialects = null;
		UserProfile profile = (UserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		JSONObject datasorcesJSON = new JSONObject();
		try {
			dataSourceDao = DAOFactory.getDataSourceDAO();

			if (profile.getIsSuperadmin() != null && profile.getIsSuperadmin()) {
				TenantManager.unset();
				dataSources = dataSourceDao.loadDataSourcesForSuperAdmin();
			} else {
				dataSourceDao.setUserProfile(profile);
				dataSources = dataSourceDao.loadAllDataSources();
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
	public String deleteDataSource(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap();

		try {
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			String id = (String) requestBodyJSON.opt("DATASOURCE_ID");
			Assert.assertNotNull(id, deleteNullIdDataSourceError);
			// if the ds is associated with any BIEngine or BIObjects, creates
			// an error
			boolean bObjects = DAOFactory.getDataSourceDAO().hasBIObjAssociated(id);
			// boolean bEngines = DAOFactory.getDataSourceDAO().hasBIEngineAssociated(id);
			if (bObjects) { // || bEngines) {
				HashMap params = new HashMap();
				logger.debug(deleteInUseDSError);
				updateAudit(req, profile, "DATA_SOURCE.DELETE", null, "ERR");
				return (ExceptionUtilities.serializeException(deleteInUseDSError, null));
			}

			IDataSource ds = DAOFactory.getDataSourceDAO().loadDataSourceByID(new Integer(id));
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

			DataSource dsNew = recoverDataSourceDetails(requestBodyJSON);

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
				updateAudit(req, profile, "DATA_SOURCE.ADD", logParam, "OK");
			} else {
				// update ds
				dao.modifyDataSource(dsNew);
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

	private JSONObject serializeDatasources(List<DataSource> dataSources, List<Domain> dialects) throws SerializationException, JSONException {

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

	private DataSource recoverDataSourceDetails(JSONObject requestBodyJSON) throws EMFUserError, SourceBeanException, IOException {
		DataSource ds = new DataSource();
		Integer id = -1;
		String idStr = (String) requestBodyJSON.opt("DATASOURCE_ID");
		if (idStr != null && !idStr.equals("")) {
			id = new Integer(idStr);
		}
		Integer dialectId = Integer.valueOf((String) requestBodyJSON.opt("DIALECT_ID"));
		String description = (String) requestBodyJSON.opt("DESCRIPTION");
		String label = (String) requestBodyJSON.opt("DATASOURCE_LABEL");
		String jndi = (String) requestBodyJSON.opt("JNDI_URL");
		String url = (String) requestBodyJSON.opt("CONNECTION_URL");
		String user = (String) requestBodyJSON.opt("USER");
		String pwd = (String) requestBodyJSON.opt("PASSWORD");
		String driver = (String) requestBodyJSON.opt("DRIVER");
		String schemaAttr = (String) requestBodyJSON.opt("SCHEMA");
		String multiSchema = (String) requestBodyJSON.opt("MULTISCHEMA");
		String readOnlyS = (String) requestBodyJSON.opt("READ_ONLY");
		String writeDefaultS = (String) requestBodyJSON.opt("WRITE_DEFAULT");

		Boolean isMultiSchema = false;
		if (multiSchema != null && (multiSchema.equals("on") || multiSchema.equals("true"))) {
			isMultiSchema = true;
		}

		Boolean readOnly = false;
		if (readOnlyS != null && (readOnlyS.equals("on") || readOnlyS.equals("true"))) {
			readOnly = true;
		}

		Boolean writeDefault = false;
		if (writeDefaultS != null && (writeDefaultS.equals("on") || writeDefaultS.equals("true"))) {
			writeDefault = true;
		}

		ds.setDsId(id.intValue());
		ds.setDialectId(dialectId);
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

		return ds;
	}

	private String serializeException(Exception e) throws JSONException {
		return ExceptionUtilities.serializeException(e.getMessage(), null);
	}

}
