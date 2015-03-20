 /* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.multitenant.service.rest;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.init.TreeInitializer;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.ITenantsDAO;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasource;
import it.eng.spagobi.commons.metadata.SbiOrganizationDatasourceId;
import it.eng.spagobi.commons.metadata.SbiOrganizationEngine;
import it.eng.spagobi.commons.metadata.SbiOrganizationEngineId;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.metadata.SbiEngines;
import it.eng.spagobi.profiling.bean.SbiUser;
import it.eng.spagobi.rest.interceptors.RestExceptionMapper;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.datasource.bo.DataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.tools.datasource.metadata.SbiDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 * @authors Rossato Luca (luca.rossato@eng.it)
 * 
 */
@Path("/multitenant")
@SuppressWarnings("all")
public class MultitenantCRUD {

	static private Logger logger = Logger.getLogger(MultitenantCRUD.class);
	static private String deleteNullIdTenantError = "error.mesage.name.multitenant.cannot.be.null";
	static private String deleteInUseError = "error.mesage.multitenant.deleting.inuse";
	static private String canNotFillResponseError = "error.mesage.description.generic.can.not.responce";
	static private String saveDuplicatedError = "error.mesage.multitenant.saving.duplicated";
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllTenant(@Context HttpServletRequest req) {
		
		TenantManager.unset();	 		
		ITenantsDAO tenantDao = null;
		List<SbiTenant> tenants;
		JSONObject tenantJSON = new JSONObject();
		try {
			tenantDao = DAOFactory.getTenantsDAO();
			tenants = tenantDao.loadAllTenants();		
			JSONArray tenantJSONArray = new JSONArray();
			if (tenants != null) {
				tenantJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(tenants, null);
				tenantJSON.put("root", tenantJSONArray);
			}

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while istantiating the dao", t);
		}
		
		return tenantJSON.toString();
	}
	
	@GET
	@Path("/themes")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllThemes(@Context HttpServletRequest req) {
		
		TenantManager.unset();
		
		IConfigDAO configDao = null;
		Config theme = null;
		JSONObject result = new JSONObject();
		JSONArray themesJSONArray = new JSONArray();
		try {
			configDao = DAOFactory.getSbiConfigDAO();
			theme = configDao.loadConfigParametersByLabel("SPAGOBI.THEMES.THEMES");		
			String valueCheck = theme.getValueCheck();
			String[] values = valueCheck.split(",");
			for (int i = 0; i < values.length; i++) {
				JSONObject item = new JSONObject();
				item.put("VALUE_CHECK", values[i].trim());
				themesJSONArray.put(item);
			}
			result.put("root", themesJSONArray);
			
		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while istantiating the dao", t);
		}
		
		return result.toString();
	}
	
	@GET
	@Path("/engines")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllEngines(@Context HttpServletRequest req) {
		
		TenantManager.unset();

		List<Engine> engines = null;
		List<SbiOrganizationEngine> selectedEngines = null;
		JSONObject result = new JSONObject();
		JSONArray enginesJSONArray = new JSONArray();
		try {
			engines = DAOFactory.getEngineDAO().loadAllEngines();		
			String tenant = (String)req.getParameter("TENANT");
			selectedEngines = DAOFactory.getTenantsDAO().loadSelectedEngines(tenant);
			for (Engine engine: engines) {
				JSONObject item = new JSONObject();			
				item.put("ID", engine.getId());
				item.put("NAME", engine.getName());
				item.put("CHECKED", isChecked(engine, selectedEngines));
				enginesJSONArray.put(item);			
			}
			result.put("root", enginesJSONArray);
			
		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while istantiating the dao", t);
		}
		
		return result.toString();
	}
	
	private boolean isChecked(Engine engine, List<SbiOrganizationEngine> selectedEngines){
		
		for (SbiOrganizationEngine orgEngine: selectedEngines) {		
			SbiEngines sbiEngine = orgEngine.getSbiEngines();
			if(sbiEngine.getEngineId().intValue() == engine.getId().intValue())
				return true;		
		}
		
		return false;
	}
	
	@GET
	@Path("/datasources")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllDataSources(@Context HttpServletRequest req) {
		
		TenantManager.unset();

		List<DataSource> dataSources = null;
		List<SbiOrganizationDatasource> selectedDataSources = null;
		IDataSourceDAO dataSourceDao = null;
		JSONObject result = new JSONObject();
		JSONArray dsJSONArray = new JSONArray();

		try {
			dataSourceDao = DAOFactory.getDataSourceDAO();
			dataSources = dataSourceDao.loadDataSourcesForSuperAdmin();			
			String tenant = (String)req.getParameter("TENANT");
			selectedDataSources = DAOFactory.getTenantsDAO().loadSelectedDS(tenant);
			for (DataSource ds: dataSources) {
				JSONObject item = new JSONObject();			
				item.put("ID", ds.getDsId());
				item.put("LABEL", ds.getLabel());
				item.put("DESCRIPTION", ds.getDescr());
				item.put("CHECKED", isCheckedDs(ds, selectedDataSources));
				dsJSONArray.put(item);			
			}
			result.put("root", dsJSONArray);
			
		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while istantiating the dao", t);
		}
		
		return result.toString();
	}
	
	private boolean isCheckedDs(DataSource dataSource, List<SbiOrganizationDatasource> selectedDataSources){
		
		for (SbiOrganizationDatasource orgDs: selectedDataSources) {		
			SbiDataSource sbiDataSource = orgDs.getSbiDataSource();
			if(sbiDataSource.getDsId() == dataSource.getDsId())
				return true;		
		}
		
		return false;
	}
	
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public String deleteTenant(@Context HttpServletRequest req) {
		
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		
		try {
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
			String id = (String) requestBodyJSON.opt("MULTITENANT_ID");
			String name = (String) requestBodyJSON.opt("MULTITENANT_NAME");
			SbiTenant aTenant = new SbiTenant(new Integer(id));
			aTenant.setName(name);
			Assert.assertNotNull(id, deleteNullIdTenantError );
			ITenantsDAO tenantDao = DAOFactory.getTenantsDAO();
			tenantDao.deleteTenant(aTenant);
			return ("");
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			logger.debug(canNotFillResponseError);
			try {
				return ( ExceptionUtilities.serializeException(canNotFillResponseError,null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}
	}
	
	@POST
	@Path("/save")
	@Produces(MediaType.APPLICATION_JSON)
	public String saveTenant(@Context HttpServletRequest req) {
		
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		try {
			String saveType = "INSERT";
			SbiUser newAdminUser = null;
			JSONObject requestBodyJSON = RestUtilities.readBodyAsJSONObject(req);
				 
			ITenantsDAO dao = DAOFactory.getTenantsDAO();
			dao.setUserProfile(profile);
			SbiTenant tenantNew = getTenantDetails(requestBodyJSON);		
			Set<SbiOrganizationEngine> sbiOrganizationEngines = getEngines(requestBodyJSON, tenantNew);
			Set<SbiOrganizationDatasource> sbiOrganizationDatasources = getDatasources(requestBodyJSON, tenantNew);
			
			tenantNew.setSbiOrganizationEngines(sbiOrganizationEngines);
			tenantNew.setSbiOrganizationDatasources(sbiOrganizationDatasources);

			if (tenantNew.getId() == -1) {
				//if a tenant with the same name not exists on db ok else error
				if (DAOFactory.getTenantsDAO().loadTenantByName(tenantNew.getName()) != null){
					throw new SpagoBIRuntimeException(saveDuplicatedError);
				}	 		
				dao.insertTenant(tenantNew);
							
				SbiTenant tmp = dao.loadTenantByName(tenantNew.getName());
				tenantNew.setId(tmp.getId());

				// add admin user 
				newAdminUser = dao.initializeAdminUser(tenantNew);
				
				// initialize folders tree structure
				TreeInitializer initializer = new TreeInitializer();
				SourceBean config = (SourceBean) ConfigSingleton.getInstance().getAttribute("TREE_INITIALIZATION");
				initializer.initialize(tenantNew, config);
				
			} else {				
				//update ds
				try{
					dao.modifyTenant(tenantNew);
					saveType = "UPDATE";
				}catch(Throwable e){
					throw new SpagoBIRuntimeException(e.getMessage());
				}
				
			}  
			JSONObject o = new JSONObject();
			o.put("MULTITENANT_ID", tenantNew.getId());
			o.put("SAVE_TYPE", saveType);
			if (newAdminUser != null) {
				o.put("NEW_USER", newAdminUser.getUserId());
			}
			return o.toString();
			
		} catch (SpagoBIRuntimeException ex) {
			logger.error("Cannot fill response container", ex);
			logger.debug(ex.getMessage());
			try {
				return ExceptionUtilities.serializeException(ex.getMessage(),null);
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException(ex.getMessage(), e);
			}
		} catch (Exception ex) {
			logger.error("Cannot fill response container", ex);
			logger.debug(canNotFillResponseError);
			try {
				return ( ExceptionUtilities.serializeException(canNotFillResponseError,null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}
	}

//	private static void updateAudit(HttpServletRequest request,
//			IEngUserProfile profile, String action_code,
//			HashMap<String, String> parameters, String esito) {
//		try {
//			AuditLogUtilities.updateAudit(request, profile, action_code, parameters, esito);
//		} catch (Exception e) {
//			logger.debug("Error writinig audit", e);
//		}
//	}
	
	private SbiTenant getTenantDetails (JSONObject requestBodyJSON) throws EMFUserError, SourceBeanException, IOException  {
		SbiTenant tenant  = new SbiTenant();
		Integer id = -1;
		String idStr = (String)requestBodyJSON.opt("MULTITENANT_ID");
		if(idStr!=null && !idStr.equals("")){
			id = new Integer(idStr);
		}
		
		String name = (String)requestBodyJSON.opt("MULTITENANT_NAME");	
		String theme = (String)requestBodyJSON.opt("MULTITENANT_THEME");
	
		tenant.setId(id.intValue());
		tenant.setName(name);
		tenant.setTheme(theme);
				
		return tenant;
	}
	
	
	private Set<SbiOrganizationEngine> getEngines(JSONObject requestBodyJSON, SbiTenant tenant) throws EMFUserError, SourceBeanException, IOException  {
		
		Set<SbiOrganizationEngine> sbiOrganizationEngines = new HashSet<SbiOrganizationEngine>();
		JSONArray engines = requestBodyJSON.optJSONArray("ENG_LIST");
		for (int i = 0; i < engines.length(); i++) {
			try{
				JSONObject obj = (JSONObject)engines.get(i);
				SbiEngines sbiEngine = new SbiEngines();
				sbiEngine.setEngineId(obj.getInt("ID"));
				sbiEngine.setName(obj.getString("NAME"));
				SbiOrganizationEngine engine = new SbiOrganizationEngine();
				if(tenant.getId() != -1)
					engine.setId(new SbiOrganizationEngineId(sbiEngine.getEngineId(), tenant.getId()));
				engine.setSbiOrganizations(tenant);
				engine.setSbiEngines(sbiEngine);
				sbiOrganizationEngines.add(engine);
			}catch (JSONException e) {
				logger.error("Cannot fill response container", e);
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}
		
		return sbiOrganizationEngines;
	}
	
	private Set<SbiOrganizationDatasource> getDatasources(JSONObject requestBodyJSON, SbiTenant tenant) throws EMFUserError, SourceBeanException, IOException  {
		
		Set<SbiOrganizationDatasource> sbiOrganizationDatasources = new HashSet<SbiOrganizationDatasource>();
		JSONArray ds = requestBodyJSON.optJSONArray("DS_LIST");
		for (int i = 0; i < ds.length(); i++) {
			try{
				JSONObject obj = (JSONObject)ds.get(i);
				SbiDataSource sbiDs = new SbiDataSource();
				sbiDs.setDsId(obj.getInt("ID"));
				sbiDs.setLabel(obj.getString("LABEL"));
				sbiDs.setDescr(obj.getString("DESCRIPTION"));
				SbiOrganizationDatasource sbiOrgDs = new SbiOrganizationDatasource();
				if(tenant.getId() != -1)
					sbiOrgDs.setId(new SbiOrganizationDatasourceId(sbiDs.getDsId(), tenant.getId()));
				sbiOrgDs.setSbiOrganizations(tenant);
				sbiOrgDs.setSbiDataSource(sbiDs);
				sbiOrganizationDatasources.add(sbiOrgDs);
			}catch (JSONException e) {
				logger.error("Cannot fill response container", e);
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}
		
		return sbiOrganizationDatasources;
	}

	
//	private String serializeException(Exception e) throws JSONException{
//		return ExceptionUtilities.serializeException(e.getMessage(),null);
//	}


}
