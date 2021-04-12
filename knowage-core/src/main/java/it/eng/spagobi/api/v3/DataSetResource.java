/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2020 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.api.v3;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.KnowageMonitor;
import com.jamonapi.KnowageMonitorFactory;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.dao.ISbiDataSetDAO;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetFilter;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.ActionNotPermittedException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * @author Marco Libanori
 */
@Path("/3.0/datasets")
public class DataSetResource {

	private final Logger logger = Logger.getLogger(DataSetResource.class);

	private static final DataSetResourceAction ACTION_DETAIL = new DataSetResourceAction("detaildataset", "Dataset detail");
	private static final DataSetResourceAction ACTION_DELETE = new DataSetResourceAction("delete", "Delete dataset");
	private static final DataSetResourceAction ACTION_LOAD_DATA = new DataSetResourceAction("loaddata", "Load data");
	private static final DataSetResourceAction ACTION_GEO_REPORT = new DataSetResourceAction("georeport", "Show Map");
	private static final DataSetResourceAction ACTION_QBE = new DataSetResourceAction("qbe", "Show Qbe");

	@Inject
	private HttpServletRequest request;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response getDataSets(@QueryParam("typeDoc") String typeDoc, @QueryParam("ids") List<Integer> ids) {

		final UserProfile userProfile = getUserProfile();
		Response response = null;

		try {

			List<IDataSet> dataSets = getDatasetManagementAPI().getDataSets();
			Stream<IDataSet> stream = dataSets.stream().filter(e -> DataSetUtilities.isExecutableByUser(e, userProfile));

			if (!ids.isEmpty()) {
				stream = stream.filter(e -> ids.contains(e.getId()));
			}

			List<DataSetResourceSimpleFacade> collect = stream.map(DataSetResourceSimpleFacade::new).collect(toList());

			DataSetResourceResponseRoot<DataSetResourceSimpleFacade> of = new DataSetResourceResponseRoot<>(collect);

			response = Response.status(Response.Status.OK).entity(of).build();

		} catch (Exception ex) {
			LogMF.error(logger, "Cannot get available datasets with typeDoc {0} and ids {1} for user {2}",
					new Object[] { typeDoc, ids, userProfile.getUserName() });
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", ex);
		}

		return response;
	}

	@GET
	@Path("/pagopt/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public DataSetResourceResponseRoot<DataSetMainDTO> getDataSetsPaginationOption(@QueryParam("typeDoc") String typeDoc,
			@QueryParam("callback") String callback, @DefaultValue("-1") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("fetchSize") int fetchSize, @QueryParam("filters") DataSetResourceFilter filters,
			@QueryParam("ordering") JSONObject ordering, @QueryParam("tags") List<Integer> tags) {

		try (KnowageMonitor m = KnowageMonitorFactory.start("knowage.dataset.paginatedList")) {

			UserProfile userProfile = getUserProfile();

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

			List<DataSetMainDTO> dataSets = getListOfGenericDatasets(offset, fetchSize, filters, ordering, tags).stream().map(DataSetMainDTO::new)
					.collect(toList());

			dataSets = (List<DataSetMainDTO>) putActions(dataSets, typeDoc);

			return new DataSetResourceResponseRoot<>(dataSets);

		} catch (Exception t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/enterprise")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public DataSetResourceResponseRoot<DataSetForWorkspaceDTO> getEnterpriseDataSet(@DefaultValue("-1") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("fetchSize") int fetchSize, @QueryParam("typeDoc") String typeDoc) {

		try {
			List<DataSetForWorkspaceDTO> dataSets = DAOFactory.getSbiDataSetDAO().loadEnterpriseDataSets(offset, fetchSize, getUserProfile()).stream()
					.map(DataSetForWorkspaceDTO::new).collect(toList());

			dataSets = (List<DataSetForWorkspaceDTO>) putActions(dataSets, typeDoc);

			return new DataSetResourceResponseRoot<>(dataSets);
		} catch (Exception t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		}
	}

	@GET
	@Path("/owned")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public DataSetResourceResponseRoot<DataSetForWorkspaceDTO> getOwnedDataSet(@DefaultValue("-1") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("fetchSize") int fetchSize, @QueryParam("typeDoc") String typeDoc) {
		try {
			List<DataSetForWorkspaceDTO> dataSets = DAOFactory.getSbiDataSetDAO().loadDataSetsOwnedByUser(offset, fetchSize, getUserProfile(), true).stream()
					.map(DataSetForWorkspaceDTO::new).collect(toList());

			dataSets = (List<DataSetForWorkspaceDTO>) putActions(dataSets, typeDoc);

			return new DataSetResourceResponseRoot<>(dataSets);

		} catch (Exception t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		}

	}

	@GET
	@Path("/shared")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public DataSetResourceResponseRoot<DataSetForWorkspaceDTO> getSharedDataSet(@DefaultValue("-1") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("fetchSize") int fetchSize, @QueryParam("typeDoc") String typeDoc) {

		try {
			List<DataSetForWorkspaceDTO> dataSets = DAOFactory.getSbiDataSetDAO().loadDatasetsSharedWithUser(offset, fetchSize, getUserProfile(), true).stream()
					.map(DataSetForWorkspaceDTO::new).collect(toList());

			dataSets = (List<DataSetForWorkspaceDTO>) putActions(dataSets, typeDoc);

			return new DataSetResourceResponseRoot<>(dataSets);
		} catch (Exception t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		}

	}

	@GET
	@Path("/uncertified")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public DataSetResourceResponseRoot<DataSetForWorkspaceDTO> getUncertifiedDataSet(@DefaultValue("-1") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("fetchSize") int fetchSize, @QueryParam("typeDoc") String typeDoc) {
		try {
			List<DataSetForWorkspaceDTO> dataSets = DAOFactory.getSbiDataSetDAO().loadDatasetOwnedAndShared(offset, fetchSize, getUserProfile()).stream()
					.map(DataSetForWorkspaceDTO::new).collect(toList());

			dataSets = (List<DataSetForWorkspaceDTO>) putActions(dataSets, typeDoc);

			return new DataSetResourceResponseRoot<>(dataSets);
		} catch (Exception t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		}

	}

	@GET
	@Path("/mydata")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public DataSetResourceResponseRoot<DataSetForWorkspaceDTO> getMyDataDataSet(@DefaultValue("-1") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("fetchSize") int fetchSize, @QueryParam("typeDoc") String typeDoc) {

		try {
			// TODO
			List<DataSetForWorkspaceDTO> dataSets = DAOFactory.getSbiDataSetDAO().loadMyDataSets(offset, fetchSize, getUserProfile()).stream()
					.map(DataSetForWorkspaceDTO::new).collect(toList());

			dataSets = (List<DataSetForWorkspaceDTO>) putActions(dataSets, typeDoc);

			return new DataSetResourceResponseRoot<>(dataSets);

		} catch (Exception t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		}

	}

	private DatasetManagementAPI getDatasetManagementAPI() {
		DatasetManagementAPI managementAPI = new DatasetManagementAPI(getUserProfile());
		return managementAPI;
	}

	private UserProfile getUserProfile() {
		return UserProfileManager.getProfile();
	}

	protected List<SbiDataSet> getListOfGenericDatasets(int start, int limit, DataSetResourceFilter filters, JSONObject ordering, List<Integer> tags)
			throws JSONException, EMFUserError {

		List<SbiDataSet> items = null;

		try {
			String sortByColumn = null;
			boolean reverse = false;
			SbiDataSetFilter daoFilter = null;
			ISbiDataSetDAO dsDao = null;

			dsDao = DAOFactory.getSbiDataSetDAO();
			dsDao.setUserProfile(getUserProfile());

			if (filters != null) {
				daoFilter = ISbiDataSetDAO.createFilter(filters.getColumnFilter(), filters.getTypeFilter(), filters.getValueFilter());
			}

			if (ordering != null) {
				reverse = ordering.optBoolean("reverseOrdering");
				sortByColumn = ordering.optString("columnOrdering");

				if (sortByColumn.equalsIgnoreCase("dsTypeCd")) {
					sortByColumn = "type";
				}

			}

			items = dsDao.list(start, limit, null, sortByColumn, reverse, tags, daoFilter);

		} catch (Exception t) {
			logger.error("Error has occured while getting list of Datasets", t);
			throw t;
		}
		logger.debug("OUT");
		return items;
	}

	private List<? extends DataSetMainDTO> putActions(List<? extends DataSetMainDTO> ret, String typeDocWizard) throws EMFInternalError {

		UserProfile userProfile = getUserProfile();

		boolean isQBEEnginePresent = isQBEEnginePresent();
		boolean isGeoEnginePresent = isGeoEnginePresent();

		ret.forEach(e -> {
			addActions(e, typeDocWizard, userProfile, isQBEEnginePresent, isGeoEnginePresent);
		});

		ret = ret.stream().filter(e -> {
			return "GEO".equalsIgnoreCase(typeDocWizard) ? isGeoEnginePresent && e.isGeoDataSet() : true;
		}).collect(toList());

		return ret;

	}

	private void addActions(DataSetMainDTO dataset, String typeDocWizard, UserProfile userProfile, boolean isQBEEnginePresent, boolean isGeoEnginePresent) {
		try {
			List<DataSetResourceAction> actions = dataset.getActions();
			String currDataSetOwner = dataset.getOwner();
			String currDataSetType = dataset.getDsTypeCd();
			boolean isGeoDataset = dataset.isGeoDataSet();

			if (typeDocWizard == null) {
				actions.add(ACTION_DETAIL);
				if (userProfile.getUserId().toString().equals(currDataSetOwner)) {
					actions.add(ACTION_DELETE);
				}
			}

			if (isGeoDataset && isGeoEnginePresent) {
				actions.add(ACTION_GEO_REPORT);
			}

			if (currDataSetType == null || !currDataSetType.equals(DataSetFactory.FEDERATED_DS_TYPE)) {
				if (isQBEEnginePresent && (typeDocWizard == null || typeDocWizard.equalsIgnoreCase("REPORT"))) {
					if (userProfile.getFunctionalities() != null
							&& userProfile.getFunctionalities().contains(SpagoBIConstants.BUILD_QBE_QUERIES_FUNCTIONALITY)) {
						actions.add(ACTION_QBE);
					}
				}
			}

			try {
				IDataSet actualDataset = DAOFactory.getDataSetDAO().loadDataSetById(dataset.getId());
				new DatasetManagementAPI(getUserProfile()).canLoadData(actualDataset);
				actions.add(ACTION_LOAD_DATA);
			} catch (ActionNotPermittedException e) {
				logger.warn("User " + getUserProfile().getUserId() + " cannot load data for dataset with label " + dataset.getLabel());
			}

		} catch (Exception ex) {
			// TODO
			throw new RuntimeException(ex);
		}
	}

	private boolean isGeoEnginePresent() {
		boolean isGeoEnginePresent = false;
		try {
			isGeoEnginePresent = ExecuteAdHocUtility.getGeoreportEngine() != null;
		} catch (SpagoBIRuntimeException r) {
			// the geo engine is not found
			logger.info("Engine not found. ", r);
		}
		return isGeoEnginePresent;
	}

	private boolean isQBEEnginePresent() {
		boolean isQBEEnginePresent = false;
		try {
			isQBEEnginePresent = ExecuteAdHocUtility.getQbeEngine() != null;
		} catch (SpagoBIRuntimeException r) {
			// the qbe engine is not found
			logger.info("Engine not found. ", r);
		}
		return isQBEEnginePresent;
	}

}
