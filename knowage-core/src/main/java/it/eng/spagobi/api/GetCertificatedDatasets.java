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
package it.eng.spagobi.api;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.ckan.CKANClient;
import it.eng.spagobi.tools.dataset.ckan.Connection;
import it.eng.spagobi.tools.dataset.ckan.exception.CKANException;
import it.eng.spagobi.tools.dataset.ckan.resource.impl.Resource;
import it.eng.spagobi.tools.dataset.ckan.utils.CKANUtils;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 * @deprecated Use specific services exposed by DataSetResource
 */
@Deprecated
@Path("/certificateddatasets")
public class GetCertificatedDatasets {

	static private Logger logger = Logger.getLogger(GetCertificatedDatasets.class);

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getAllDataSet(@Context HttpServletRequest request) {
		IDataSetDAO dataSetDao = null;
		List<IDataSet> dataSets;

		// FilterIOManager ioManager = new FilterIOManager(request, null);
		// ioManager.initConetxtManager();
		// IEngUserProfile profile = (IEngUserProfile) ioManager.getFromSession(IEngUserProfile.ENG_USER_PROFILE);
		IEngUserProfile profile = (IEngUserProfile) request.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		JSONObject JSONReturn = new JSONObject();
		JSONArray datasetsJSONArray = new JSONArray();
		JSONArray ckanJSONArray = new JSONArray();
		try {
			List<IDataSet> unfilteredDataSets;
			List<Integer> categories = getCategories(profile);
			dataSetDao = DAOFactory.getDataSetDAO();
			dataSetDao.setUserProfile(profile);

			String isTech = request.getParameter("isTech");
			String allMyDataDS = request.getParameter("allMyDataDs");
			String ckanDS = request.getParameter("ckanDs");
			String ckanFilter = request.getParameter("ckanFilter");
			String ckanOffset = request.getParameter("ckanOffset");
			String ckanRepository = request.getParameter("ckanRepository");
			String typeDocWizard = (request.getParameter("typeDoc") != null && !"null".equals(request.getParameter("typeDoc")))
					? request.getParameter("typeDoc") : null;

			if (isTech != null && isTech.equals("true")) {
				// if is technical dataset == ENTERPRISE --> get all ADMIN/DEV public datasets
				unfilteredDataSets = dataSetDao.loadEnterpriseDataSets((UserProfile) profile);
			} else {
				if (allMyDataDS != null && allMyDataDS.equals("true")) {
					// get all the Datasets visible for the current user (MyData,Enterprise,Shared Datasets,Ckan)
					unfilteredDataSets = dataSetDao.loadMyDataDataSets(((UserProfile) profile));
				} else if (ckanDS != null && ckanDS.equals("true")) {
					ckanJSONArray = getOnlineCkanDatasets(profile, ckanRepository, ckanFilter, ckanOffset);
					unfilteredDataSets = dataSetDao.loadCkanDataSets(((UserProfile) profile));
					synchronizeDatasets(unfilteredDataSets, ckanJSONArray);
				} else {
					// else it is a custom dataset list --> get all datasets public with owner != user itself
					unfilteredDataSets = dataSetDao.loadDatasetsSharedWithUser(((UserProfile) profile), true);
				}
			}
			dataSets = getFilteredDatasets(unfilteredDataSets, categories);
			logger.debug("Creating JSON...");
			long start = System.currentTimeMillis();
			datasetsJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(dataSets, null);
			if (ckanDS != null && ckanDS.equals("true")) {
				if (ckanFilter.equals("NOFILTER") && ckanOffset.equals("0")) {
					for (int i = 0; i < ckanJSONArray.length(); i++) {
						datasetsJSONArray.put(ckanJSONArray.get(i));
					}
				} else { // Search by filter: list only unused CKAN datasets
					datasetsJSONArray = ckanJSONArray;
				}
			}

			JSONArray datasetsJSONReturn = putActions(profile, datasetsJSONArray, typeDocWizard);

			JSONReturn.put("root", datasetsJSONReturn);
			logger.debug("JSON created in " + (System.currentTimeMillis() - start) + "ms.");

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while instatiating the dao", t);
		}
		return JSONReturn.toString();
	}

	private JSONArray putActions(IEngUserProfile profile, JSONArray datasetsJSONArray, String typeDocWizard) throws JSONException, EMFInternalError {

		Engine qbeEngine = null;
		try {
			qbeEngine = ExecuteAdHocUtility.getQbeEngine();
		} catch (SpagoBIRuntimeException r) {
			// the qbe engine is not found
			logger.info("Engine not found. ", r);
		}

		Engine geoEngine = null;
		try {
			geoEngine = ExecuteAdHocUtility.getGeoreportEngine();
		} catch (SpagoBIRuntimeException r) {
			// the geo engine is not found
			logger.info("Engine not found. ", r);
		}
		JSONObject detailAction = new JSONObject();
		detailAction.put("name", "detaildataset");
		detailAction.put("description", "Dataset detail");

		JSONObject deleteAction = new JSONObject();
		deleteAction.put("name", "delete");
		deleteAction.put("description", "Delete dataset");

		JSONObject georeportAction = new JSONObject();
		georeportAction.put("name", "georeport");
		georeportAction.put("description", "Show Map");

		JSONObject qbeAction = new JSONObject();
		qbeAction.put("name", "qbe");
		qbeAction.put("description", "Show Qbe");

		JSONObject infoAction = new JSONObject();
		infoAction.put("name", "info");
		infoAction.put("description", "Show Info");

		JSONArray datasetsJSONReturn = new JSONArray();
		for (int i = 0; i < datasetsJSONArray.length(); i++) {
			JSONArray actions = new JSONArray();
			JSONObject datasetJSON = datasetsJSONArray.getJSONObject(i);

			// Check if it is a CKAN dataset and if is already bookmarked
			if (datasetJSON.getString("dsTypeCd").equals("Ckan") && !datasetJSON.has("id")) {
				actions.put(infoAction);
			}

			if (typeDocWizard == null) {
				actions.put(detailAction);
				if (((UserProfile) profile).getUserId().toString().equals(datasetJSON.get("owner"))) {
					// the delete action is able only for private dataset
					actions.put(deleteAction);
				}
			}

			boolean isGeoDataset = false;
			if (!datasetJSON.getString("dsTypeCd").equals("Ckan")) { // GeoDatasets disabled for CKAN!
				try {
					String meta = datasetJSON.getString("meta");
					isGeoDataset = ExecuteAdHocUtility.hasGeoHierarchy(meta);
				} catch (Exception e) {
					logger.error("Error during check of Geo spatial column", e);
				}
			}
			if (isGeoDataset && geoEngine != null && typeDocWizard != null && typeDocWizard.equalsIgnoreCase("GEO")) {
				actions.put(georeportAction); // enable the icon to CREATE a new geo document
			} else {
				if (isGeoDataset && geoEngine != null) {
					// if (isGeoDataset && geoEngine != null &&
					// profile.getUserUniqueIdentifier().toString().equals(datasetJSON.get("owner"))){
					actions.put(georeportAction); // Annotated view map action to release SpagoBI 4
				}
			}

			String dsType = datasetJSON.optString(DataSetConstants.DS_TYPE_CD);
			if (dsType == null || !dsType.equals(DataSetFactory.FEDERATED_DS_TYPE)) {
				if (qbeEngine != null && (typeDocWizard == null || typeDocWizard.equalsIgnoreCase("REPORT"))) {
					if (profile.getFunctionalities().contains(SpagoBIConstants.BUILD_QBE_QUERIES_FUNCTIONALITY)) {
						actions.put(qbeAction);
					}
				}
			}

			datasetJSON.put("actions", actions);
			if (typeDocWizard != null && typeDocWizard.equalsIgnoreCase("GEO")) {
				// if is coming from myAnalysis - create Geo Document - must shows only ds geospatial --> isGeoDataset == true
				if (geoEngine != null && isGeoDataset)
					datasetsJSONReturn.put(datasetJSON);
			} else
				datasetsJSONReturn.put(datasetJSON);
		}
		return datasetsJSONReturn;
	}

	@GET
	@Path("/getflatdataset")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.DOCUMENT_MANAGEMENT_DEV })
	public String getFlatDataSet(@Context HttpServletRequest req) {
		IDataSetDAO dataSetDao = null;
		List<IDataSet> dataSets;
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		JSONObject JSONReturn = new JSONObject();
		JSONArray datasetsJSONArray = new JSONArray();
		try {
			dataSetDao = DAOFactory.getDataSetDAO();
			dataSetDao.setUserProfile(profile);
			dataSets = dataSetDao.loadFlatDatasets();
			// dataSets = dataSetDao.loadFlatDatasets(profile.getUserUniqueIdentifier().toString());

			datasetsJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(dataSets, null);

			JSONArray datasetsJSONReturn = putActions(profile, datasetsJSONArray, null);

			JSONReturn.put("root", datasetsJSONReturn);

		} catch (Throwable t) {
			throw new SpagoBIServiceException("An unexpected error occured while instatiating the dao", t);
		}
		return JSONReturn.toString();
	}

	private JSONArray getOnlineCkanDatasets(IEngUserProfile profile, String url, String filter, String offset) throws JSONException {

		JSONArray datasetsJsonArray = new JSONArray();

		Connection customConnection = new Connection(url, null, null);

		CKANClient client = new CKANClient(customConnection);
		try {
			logger.debug("Getting resources...");
			long start = System.currentTimeMillis();
			List<Resource> list = client.getAllResourcesCompatibleWithSpagoBI(filter, offset);
			logger.debug("Resources got in " + (System.currentTimeMillis() - start) + "ms.");
			logger.debug("Translating resources...");
			start = System.currentTimeMillis();
			for (Resource resource : list) {
				JSONObject jsonObj = CKANUtils.getJsonObjectFromCkanResource(resource);
				datasetsJsonArray.put(jsonObj);
			}
			logger.debug("Resources translated in " + (System.currentTimeMillis() - start) + "ms.");
		} catch (CKANException e) {
			throw new SpagoBIServiceException("REST service /certificateddatasets", "Error while getting CKAN resources: " + e);
		}
		return datasetsJsonArray;
	}

	private void synchronizeDatasets(List<IDataSet> spagobiDs, JSONArray ckanDs) throws JSONException {
		logger.debug("Synchronize resources...");
		long start = System.currentTimeMillis();
		Iterator<IDataSet> iterator = spagobiDs.iterator();
		while (iterator.hasNext()) {
			IDataSet ds = iterator.next();
			String config = JSONUtils.escapeJsonString(ds.getConfiguration());
			JSONObject jsonConf = ObjectUtils.toJSONObject(config);
			for (int i = 0; i < ckanDs.length(); i++) {
				if (jsonConf.getString("ckanId").equals(ckanDs.getJSONObject(i).getJSONObject("configuration").getString("ckanId"))) {
					ckanDs.remove(i);
					break;
				}
			}
		}
		logger.debug("Resources synchronized in " + (System.currentTimeMillis() - start) + "ms.");
	}

	protected List<Integer> getCategories(IEngUserProfile profile) {

		List<Integer> categories = new ArrayList<Integer>();
		try {
			// NO CATEGORY IN THE DOMAINS
			IDomainDAO domainDao = DAOFactory.getDomainDAO();
			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();

			// TODO : Makes sense?
			List<Domain> dialects = categoryDao.getCategoriesForDataset()
				.stream()
				.map(Domain::fromCategory)
				.collect(toList());

			if (dialects == null || dialects.size() == 0) {
				return null;
			}

			Collection userRoles = profile.getRoles();
			Iterator userRolesIter = userRoles.iterator();
			IRoleDAO roledao = DAOFactory.getRoleDAO();
			while (userRolesIter.hasNext()) {
				String roleName = (String) userRolesIter.next();
				Role role = roledao.loadByName(roleName);

				List<RoleMetaModelCategory> aRoleCategories = roledao.getMetaModelCategoriesForRole(role.getId());
				List<RoleMetaModelCategory> resp = new ArrayList<>();
				List<Domain> array = categoryDao.getCategoriesForDataset()
						.stream()
						.map(Domain::fromCategory)
						.collect(toList());
				for (RoleMetaModelCategory r : aRoleCategories) {
					for (Domain dom : array) {
						if (r.getCategoryId().equals(dom.getValueId())) {
							resp.add(r);
						}
					}

				}
				if (resp != null) {
					for (Iterator iterator = resp.iterator(); iterator.hasNext();) {
						RoleMetaModelCategory roleDataSetCategory = (RoleMetaModelCategory) iterator.next();
						categories.add(roleDataSetCategory.getCategoryId());
					}
				}

			}
		} catch (Exception e) {
			logger.error("Error loading the data set categories visible from the roles of the user");
			throw new SpagoBIRuntimeException("Error loading the data set categories visible from the roles of the user");
		}
		return categories;
	}

	private List<IDataSet> getFilteredDatasets(List<IDataSet> unfilteredDataSets, List<Integer> categories) {
		List<IDataSet> dataSets = new ArrayList<IDataSet>();
		if (categories != null && categories.size() != 0) {
			for (IDataSet ds : unfilteredDataSets) {
				if (ds.getCategoryId() != null || categories.contains(ds.getCategoryId())) {
					dataSets.add(ds);
				}
			}
		}
		return dataSets;
	}
}
