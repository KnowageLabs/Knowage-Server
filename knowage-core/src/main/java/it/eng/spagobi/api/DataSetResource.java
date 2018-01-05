/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

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
package it.eng.spagobi.api;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.common.AbstractDataSetResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.deserializer.DeserializerFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.crosstab.CrossTab;
import it.eng.spagobi.tools.dataset.crosstab.CrosstabDefinition;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.service.ManageDataSetsForREST;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceParameterException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
@Path("/1.0/datasets")
@ManageAuthorization
public class DataSetResource extends AbstractDataSetResource {

	static protected Logger logger = Logger.getLogger(DataSetResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSets(@QueryParam("typeDoc") String typeDoc, @QueryParam("callback") String callback, @QueryParam("ids") String ids) {
		logger.debug("IN");

		try {

			// The old implementation. (commented by: danristo)
			List<IDataSet> dataSets = getDatasetManagementAPI().getDataSets();
			List<IDataSet> toBeReturned = new ArrayList<IDataSet>();

			List<Integer> idList = null;
			Integer[] idArray = getIdsAsIntegers(ids);
			if (idArray != null) {
				idList = Arrays.asList(idArray);
			}

			for (IDataSet dataset : dataSets) {
				if (DataSetUtilities.isExecutableByUser(dataset, getUserProfile()) && (idList == null || idList.contains(dataset.getId())))
					toBeReturned.add(dataset);
			}

			return serializeDataSets(toBeReturned, typeDoc);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/datasetsforlov")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSetsForLOV(@QueryParam("typeDoc") String typeDoc, @QueryParam("callback") String callback) {
		logger.debug("IN");

		try {

			// The old implementation. (commented by: danristo)
			List<IDataSet> dataSets = getDatasetManagementAPI().getDataSets();
			JSONArray toReturn = new JSONArray();

			for (IDataSet dataset : dataSets) {

				JSONObject obj = new JSONObject();
				if (DataSetUtilities.isExecutableByUser(dataset, getUserProfile())) {
					obj.put("label", dataset.getLabel());
					obj.put("id", dataset.getId());
					toReturn.put(obj);
				}
			}

			return toReturn.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Returns the number of existing datasets. This number is later used for server side pagination.
	 *
	 * @author Nikola Simovic (nsimovic, nikola.simovic@mht.net)
	 */
	@GET
	@Path("/countDataSets")
	@Produces(MediaType.TEXT_PLAIN)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Number getNumberOfDataSets(@QueryParam("typeDoc") String typeDoc, @QueryParam("callback") String callback) {
		logger.debug("IN");

		try {
			IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(getUserProfile());
			Number numOfDataSets = dsDao.countDatasets();
			return numOfDataSets;
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Returns the number of datasets for a particular search. This number is later used for server side pagination when searching.
	 *
	 * @author Nikola Simovic (nsimovic, nikola.simovic@mht.net)
	 */
	@GET
	@Path("/countDataSetSearch/{searchValue}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Number getNumberOfDataSetsSearch(@PathParam("searchValue") String searchValue) {
		logger.debug("IN");

		try {

			IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(getUserProfile());
			Number numOfDataSets = dsDao.countDatasetsSearch(searchValue);
			return numOfDataSets;
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * The new implementation that, besides other useful information about datasets, provides also an information about old dataset versions for particular
	 * dataset. This information was missing before.
	 *
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 * @author Nikola Simovic (nsimovic, nikola.simovic@mht.net)
	 */
	@GET
	@Path("/pagopt/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSetsPaginationOption(@QueryParam("typeDoc") String typeDoc, @QueryParam("callback") String callback,
			@QueryParam("offset") Integer offsetInput, @QueryParam("fetchSize") Integer fetchSizeInput, @QueryParam("filters") JSONObject filters,
			@QueryParam("ordering") JSONObject ordering) {

		logger.debug("IN");

		try {

			int offset = (offsetInput == null) ? -1 : offsetInput;
			int fetchSize = (fetchSizeInput == null) ? -1 : fetchSizeInput;

			IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(getUserProfile());
			ManageDataSetsForREST mdsfr = new ManageDataSetsForREST();

			List<IDataSet> dataSets = getListOfGenericDatasets(dsDao, offset, fetchSize, filters, ordering);

			List<IDataSet> toBeReturned = new ArrayList<IDataSet>();

			for (IDataSet dataset : dataSets) {

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				ISchedulerDAO schedulerDAO;

				try {
					schedulerDAO = DAOFactory.getSchedulerDAO();
				} catch (Throwable t) {
					throw new SpagoBIRuntimeException("Impossible to load scheduler DAO", t);
				}

				if (dataset.isPersisted()) {

					List<Trigger> triggers = schedulerDAO.loadTriggers("PersistDatasetExecutions", dataset.getLabel());

					if (triggers.isEmpty()) {
						// itemJSON.put("isScheduled", false);
						dataset.setScheduled(false);
					} else {

						// Dataset scheduling is mono-trigger
						Trigger trigger = triggers.get(0);

						if (!trigger.isRunImmediately()) {

							// itemJSON.put("isScheduled", true);
							dataset.setScheduled(true);

							if (trigger.getStartTime() != null) {
								dataset.setStartDateField(sdf.format(trigger.getStartTime()));
							} else {
								// itemJSON.put("startDate", "");
								dataset.setStartDateField("");
							}

							if (trigger.getEndTime() != null) {
								// itemJSON.put("endDate", sdf.format(trigger.getEndTime()));
								dataset.setEndDateField(sdf.format(trigger.getEndTime()));
							} else {
								// itemJSON.put("endDate", "");
								dataset.setEndDateField("");
							}

							// itemJSON.put("schedulingCronLine", trigger.getChronExpression().getExpression());
							dataset.setSchedulingCronLine(trigger.getChronExpression().getExpression());
						}
					}
				}
				/**
				 * alberto ghedin next line is commented because the dao that return the datasets will return just datset owned by user or of same category
				 */
				// if (DataSetUtilities.isExecutableByUser(dataset, getUserProfile()))
				toBeReturned.add(dataset);
			}

			return serializeDataSets(toBeReturned, typeDoc);

		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	@GET
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSet(@PathParam("label") String label) {
		return super.getDataSet(label);
	}

	@GET
	@Path("/id/{id}")
	@Produces(MediaType.TEXT_HTML)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSetLabelById(@PathParam("id") String id) {
		logger.debug("IN");

		IDataSetDAO datasetDao = null;
		try {
			datasetDao = DAOFactory.getDataSetDAO();
		} catch (EMFUserError e) {
			logger.error("Internal error", e);
			throw new SpagoBIRuntimeException("Internal error", e);
		}
		IDataSet dataset = datasetDao.loadDataSetById(new Integer(id));

		return dataset.getLabel();
	}

	/**
	 * Return the entire dataset according to its ID.
	 *
	 * @param id
	 * @return
	 * @throws JSONException
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 * @throws SerializationException
	 */
	@GET
	@Path("/dataset/id/{id}")
	@Produces(MediaType.TEXT_HTML)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSetById(@PathParam("id") String id) throws JSONException, SerializationException {
		logger.debug("IN");

		IDataSetDAO datasetDao = null;

		try {
			datasetDao = DAOFactory.getDataSetDAO();
		} catch (EMFUserError e) {
			logger.error("Internal error", e);
			throw new SpagoBIRuntimeException("Internal error", e);
		}

		/**
		 * When retrieving the dataset that is previously saved, call the method that retrieves all the available datasets since they contain also information
		 * about all dataset versions for them. Go through all the collection and find the one that we need (according to its ID).
		 *
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */

		IDataSet datasetToReturn = null;

		datasetDao.setUserProfile(getUserProfile());
		List<IDataSet> dataSets = datasetDao.loadPagedDatasetList(-1, -1);

		for (IDataSet datasetTemp : dataSets) {

			if (datasetTemp.getId() == Integer.parseInt(id)) {

				datasetToReturn = datasetTemp;

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				ISchedulerDAO schedulerDAO;

				try {
					schedulerDAO = DAOFactory.getSchedulerDAO();
				} catch (Throwable t) {
					throw new SpagoBIRuntimeException("Impossible to load scheduler DAO", t);
				}

				if (datasetToReturn.isPersisted()) {

					List<Trigger> triggers = schedulerDAO.loadTriggers("PersistDatasetExecutions", datasetToReturn.getLabel());

					if (triggers.isEmpty()) {
						// itemJSON.put("isScheduled", false);
						datasetToReturn.setScheduled(false);
					} else {

						// Dataset scheduling is mono-trigger
						Trigger trigger = triggers.get(0);

						if (!trigger.isRunImmediately()) {

							// itemJSON.put("isScheduled", true);
							datasetToReturn.setScheduled(true);

							if (trigger.getStartTime() != null) {
								datasetToReturn.setStartDateField(sdf.format(trigger.getStartTime()));
							} else {
								// itemJSON.put("startDate", "");
								datasetToReturn.setStartDateField("");
							}

							if (trigger.getEndTime() != null) {
								// itemJSON.put("endDate", sdf.format(trigger.getEndTime()));
								datasetToReturn.setEndDateField(sdf.format(trigger.getEndTime()));
							} else {
								// itemJSON.put("endDate", "");
								datasetToReturn.setEndDateField("");
							}

							// itemJSON.put("schedulingCronLine", trigger.getChronExpression().getExpression());
							datasetToReturn.setSchedulingCronLine(trigger.getChronExpression().getExpression());
						}
					}
				}

				break;
			}
		}

		return serializeDataSet(datasetToReturn, null);
	}

	/**
	 * Acquire required version of the dataset
	 *
	 * @param id
	 *            The ID of the dataset whose version with the versionId ID should be restored.
	 * @param versionId
	 *            The ID of the version of the dataset that should be restored and exchanged for the current one (active).
	 * @return Serialized dataset that is restored as the old version of the dataset.
	 * @throws JSONException
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	@GET
	@Path("/{id}/restore")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String restoreCurrentDatasetVersion(@PathParam("id") String datasetId, @QueryParam("versionId") String versionId) throws JSONException {

		logger.debug("IN");

		IDataSetDAO datasetDao = null;

		try {
			datasetDao = DAOFactory.getDataSetDAO();
		} catch (EMFUserError e) {
			logger.error("Internal error", e);
			throw new SpagoBIRuntimeException("Internal error", e);
		}

		// Execute restoring of the dataset version (get the required version of the dataset). This will provide changes in the database.
		datasetDao.restoreOlderDataSetVersion(Integer.parseInt(datasetId), Integer.parseInt(versionId));

		// Load all datasets in order to acquire the actual list of datasets and to return the one that is restored.
		datasetDao.setUserProfile(getUserProfile());
		List<IDataSet> dataSets = datasetDao.loadPagedDatasetList(-1, -1);

		IDataSet toBeReturned = null;

		for (IDataSet datasetTemp : dataSets) {
			if (datasetTemp.getId() == Integer.parseInt(datasetId)) {
				toBeReturned = datasetTemp;
			}
		}

		return serializeDataSet(toBeReturned, null);

	}

	@Override
	@GET
	@Path("/{label}/content")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response execute(@PathParam("label") String label, String body) {
		return super.execute(label, body);
	}

	@Override
	@DELETE
	@Path("/{label}")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response deleteDataset(@PathParam("label") String label) {
		return super.deleteDataset(label);
	}

	/**
	 * Delete a version for the selected dataset.
	 *
	 * @param id
	 *            The ID of the selected dataset.
	 * @param versionId
	 *            The ID of the version of the selected dataset.
	 * @return Status of the request (OK status).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	@DELETE
	@Path("/{id}/version/{versionId}")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response deleteDatasetVersion(@PathParam("id") String datasetId, @PathParam("versionId") String versionId) {

		IDataSetDAO datasetDao = null;

		try {
			datasetDao = DAOFactory.getDataSetDAO();
			datasetDao.setUserProfile(getUserProfile());
		} catch (EMFUserError e) {
			logger.error("Internal error", e);
			throw new SpagoBIRuntimeException("Internal error", e);
		}

		boolean deleted = datasetDao.deleteInactiveDataSetVersion(Integer.parseInt(versionId), Integer.parseInt(datasetId));

		if (deleted) {
			logger.debug("Dataset Version deleted");
		} else {
			throw new SpagoBIRuntimeException("Dataset version with the ID=[" + versionId + "] could not be deleted");
		}

		return Response.ok().build();
	}

	/**
	 * Delete all versions for the selected dataset.
	 *
	 * @param id
	 *            The ID of the selected dataset.
	 * @return Status of the request (OK status).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	@DELETE
	@Path("/{id}/allversions")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response deleteAllDatasetVersions(@PathParam("id") String datasetId) {

		IDataSetDAO datasetDao = null;

		try {
			datasetDao = DAOFactory.getDataSetDAO();
			datasetDao.setUserProfile(getUserProfile());
		} catch (EMFUserError e) {
			logger.error("Internal error", e);
			throw new SpagoBIRuntimeException("Internal error", e);
		}

		boolean deleted = datasetDao.deleteAllInactiveDataSetVersions(Integer.parseInt(datasetId));

		if (deleted) {
			logger.debug("All versions for the selected dataset are deleted");
		} else {
			throw new SpagoBIRuntimeException("All versions for the selected dataset could not be deleted");
		}

		return Response.ok().build();
	}

	@GET
	@Path("/{label}/fields")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSetFields(@Context HttpServletRequest req, @PathParam("label") String label) {
		logger.debug("IN");
		try {
			List<IFieldMetaData> fieldsMetaData = getDatasetManagementAPI().getDataSetFieldsMetadata(label);
			JSONArray fieldsJSON = writeFields(fieldsMetaData);
			JSONObject resultsJSON = new JSONObject();
			resultsJSON.put("results", fieldsJSON);

			return resultsJSON.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/{label}/parameters")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSetParameters(@Context HttpServletRequest req, @PathParam("label") String label) {
		logger.debug("IN");
		try {
			List<JSONObject> fieldsParameters = getDatasetManagementAPI().getDataSetParameters(label);
			JSONArray paramsJSON = writeParameters(fieldsParameters);
			JSONObject resultsJSON = new JSONObject();
			resultsJSON.put("results", paramsJSON);
			return resultsJSON.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private static final String CROSSTAB_DEFINITION = "crosstabDefinition";

	@POST
	@Path("/{label}/chartData")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getChartDataStore(@PathParam("label") String label, @QueryParam("offset") @DefaultValue("-1") int offset,
			@QueryParam("fetchSize") @DefaultValue("-1") int fetchSize, @QueryParam("maxResults") @DefaultValue("-1") int maxResults) {
		logger.debug("IN");
		try {
			String crosstabDefinitionParam = request.getParameter(CROSSTAB_DEFINITION);
			if (crosstabDefinitionParam == null) {
				throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Parameter [" + CROSSTAB_DEFINITION + "] cannot be null");
			}

			JSONObject crosstabDefinitionJSON = ObjectUtils.toJSONObject(crosstabDefinitionParam);

			logger.debug("Parameter [" + crosstabDefinitionJSON + "] is equals to [" + crosstabDefinitionJSON.toString() + "]");
			CrosstabDefinition crosstabDefinition = (CrosstabDefinition) DeserializerFactory.getDeserializer("application/json")
					.deserialize(crosstabDefinitionJSON, CrosstabDefinition.class);

			IDataStore dataStore = getDatasetManagementAPI().getAggregatedDataStore(label, offset, fetchSize, maxResults, crosstabDefinition);
			Assert.assertNotNull(dataStore, "Aggregated Datastore is null");

			// serialize crosstab
			CrossTab crossTab;
			if (crosstabDefinition.isPivotTable()) {
				// TODO: see the implementation in LoadCrosstabAction
				throw new SpagoBIServiceException(this.request.getPathInfo(), "Crosstable Pivot not yet managed");
			} else {
				// load the crosstab data structure for all other widgets
				crossTab = new CrossTab(dataStore, crosstabDefinition);
			}
			JSONObject crossTabDefinition = crossTab.getJSONCrossTab();

			return crossTabDefinition.toString();

		} catch (ParametersNotValorizedException p) {
			throw new ParametersNotValorizedException(p.getMessage());
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/federated")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getMyFederatedDataSets(@QueryParam("typeDoc") String typeDoc, @QueryParam("callback") String callback) {
		logger.debug("IN");

		try {

			IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(getUserProfile());
			List<IDataSet> dataSets = getDatasetManagementAPI().getMyFederatedDataSets();

			List<IDataSet> toBeReturned = new ArrayList<IDataSet>(0);

			for (IDataSet dataset : dataSets) {
				if (DataSetUtilities.isExecutableByUser(dataset, getUserProfile()))
					toBeReturned.add(dataset);
			}

			return serializeDataSets(toBeReturned, typeDoc);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/enterprise")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getEnterpriseDataSet(@QueryParam("typeDoc") String typeDoc) {
		logger.debug("IN");
		try {
			List<IDataSet> dataSets = getDatasetManagementAPI().getEnterpriseDataSet();
			return serializeDataSets(dataSets, typeDoc);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/owned")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getOwnedDataSet(@QueryParam("typeDoc") String typeDoc) {
		logger.debug("IN");
		try {
			List<IDataSet> dataSets = getDatasetManagementAPI().getOwnedDataSet();
			return serializeDataSets(dataSets, typeDoc);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/shared")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getSharedDataSet(@QueryParam("typeDoc") String typeDoc) {
		logger.debug("IN");
		try {
			List<IDataSet> dataSets = getDatasetManagementAPI().getSharedDataSet();
			return serializeDataSets(dataSets, typeDoc);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/uncertified")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getUncertifiedDataSet(@QueryParam("typeDoc") String typeDoc) {
		logger.debug("IN");
		try {
			List<IDataSet> dataSets = getDatasetManagementAPI().getUncertifiedDataSet();
			return serializeDataSets(dataSets, typeDoc);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/mydata")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getMyDataDataSet(@QueryParam("typeDoc") String typeDoc) {
		logger.debug("IN");
		try {
			List<IDataSet> dataSets;
			if (UserUtilities.isAdministrator(getUserProfile())) {
				dataSets = getDatasetManagementAPI().getAllDataSet();
			} else {
				dataSets = getDatasetManagementAPI().getMyDataDataSet();
			}
			return serializeDataSets(dataSets, typeDoc);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/mydatanoparams")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getMyDataDataSetWithoutParameters(@QueryParam("typeDoc") String typeDoc) {
		logger.debug("IN");
		try {
			List<IDataSet> dataSets;
			List<IDataSet> dataSetsNoParams = new ArrayList<IDataSet>(0);
			if (UserUtilities.isAdministrator(getUserProfile())) {
				dataSets = getDatasetManagementAPI().getAllDataSet();
			} else {
				dataSets = getDatasetManagementAPI().getMyDataDataSet();
			}

			if (dataSets != null) {
				for (Iterator iterator = dataSets.iterator(); iterator.hasNext();) {
					IDataSet iDataSet = (IDataSet) iterator.next();
					Map params = iDataSet.getParamsMap();
					if (params == null || params.isEmpty()) {
						dataSetsNoParams.add(iDataSet);
					}
				}
			}

			return serializeDataSets(dataSetsNoParams, typeDoc);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public String persistDataSets(@Context HttpServletRequest req) throws IOException, JSONException {
		IDataSetDAO dsDao;
		try {
			dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(getUserProfile());
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
		JSONObject json = RestUtilities.readBodyAsJSONObject(req);
		ManageDataSetsForREST mdsfr = new ManageDataSetsForREST();
		String toReturnString = mdsfr.insertDataset(json.toString(), dsDao, null, getUserProfile(), req);
		return toReturnString;
	}

	@POST
	@Path("/preview")
	@Produces(MediaType.APPLICATION_JSON)
	public String previewDataSet(@Context HttpServletRequest req) throws IOException, JSONException {
		JSONObject json = RestUtilities.readBodyAsJSONObject(req);
		ManageDataSetsForREST mdsfr = new ManageDataSetsForREST();
		String toReturnString = mdsfr.previewDataset(json.toString(), getUserProfile());
		return toReturnString;
	}

	// ==========================================================================================
	// Serialization methods
	// ==========================================================================================

	// PROPERTIES TO LOOK FOR INTO THE FIELDS
	public static final String PROPERTY_VISIBLE = "visible";
	public static final String PROPERTY_CALCULATED_EXPERT = "calculatedExpert";
	public static final String PROPERTY_IS_SEGMENT_ATTRIBUTE = "isSegmentAttribute";
	public static final String PROPERTY_IS_MANDATORY_MEASURE = "isMandatoryMeasure";
	public static final String PROPERTY_AGGREGATION_FUNCTION = "aggregationFunction";

	public JSONArray writeFields(List<IFieldMetaData> fieldsMetaData) throws Exception {

		// field's meta
		JSONArray fieldsMetaDataJSON = new JSONArray();

		List<JSONObject> attributesList = new ArrayList<JSONObject>();
		List<JSONObject> measuresList = new ArrayList<JSONObject>();

		int fieldCount = fieldsMetaData.size();
		logger.debug("Number of fields = " + fieldCount);
		Assert.assertTrue(fieldCount > 0, "Dataset has no fields!!!");

		for (IFieldMetaData fieldMetaData : fieldsMetaData) {

			logger.debug("Evaluating field with name [" + fieldMetaData.getName() + "], alias [" + fieldMetaData.getAlias() + "] ...");

			Boolean isCalculatedExpert = (Boolean) fieldMetaData.getProperty(PROPERTY_CALCULATED_EXPERT);

			if (isCalculatedExpert != null && isCalculatedExpert) {
				logger.debug("The field is a expert calculated field so we skip it");
				// continue;
			}

			Object propertyRawValue = fieldMetaData.getProperty(PROPERTY_VISIBLE);
			logger.debug("Read property " + PROPERTY_VISIBLE + ": its value is [" + propertyRawValue + "]");

			if (propertyRawValue != null && !propertyRawValue.toString().equals("") && (Boolean.parseBoolean(propertyRawValue.toString()) == false)) {
				logger.debug("The field is not visible");
				continue;
			} else {
				logger.debug("The field is visible");
			}
			String fieldName = getFieldName(fieldMetaData);
			String fieldHeader = getFieldAlias(fieldMetaData);
			String fieldColumnType = getFieldColumnType(fieldMetaData);
			JSONObject fieldMetaDataJSON = new JSONObject();
			fieldMetaDataJSON.put("id", fieldName);
			fieldMetaDataJSON.put("alias", fieldHeader);
			fieldMetaDataJSON.put("colType", fieldColumnType);
			FieldType type = fieldMetaData.getFieldType();
			logger.debug("The field type is " + type.name());
			switch (type) {
			case ATTRIBUTE:
				Object isSegmentAttributeObj = fieldMetaData.getProperty(PROPERTY_IS_SEGMENT_ATTRIBUTE);
				logger.debug("Read property " + PROPERTY_IS_SEGMENT_ATTRIBUTE + ": its value is [" + propertyRawValue + "]");
				String attributeNature = (isSegmentAttributeObj != null && Boolean.parseBoolean(isSegmentAttributeObj.toString())) ? "segment_attribute"
						: "attribute";

				logger.debug("The nature of the attribute is recognized as " + attributeNature);
				fieldMetaDataJSON.put("nature", attributeNature);
				fieldMetaDataJSON.put("funct", AggregationFunctions.NONE);
				fieldMetaDataJSON.put("iconCls", attributeNature);
				break;
			case MEASURE:
				Object isMandatoryMeasureObj = fieldMetaData.getProperty(PROPERTY_IS_MANDATORY_MEASURE);
				logger.debug("Read property " + PROPERTY_IS_MANDATORY_MEASURE + ": its value is [" + isMandatoryMeasureObj + "]");
				String measureNature = (isMandatoryMeasureObj != null && (Boolean.parseBoolean(isMandatoryMeasureObj.toString()) == true)) ? "mandatory_measure"
						: "measure";
				logger.debug("The nature of the measure is recognized as " + measureNature);
				fieldMetaDataJSON.put("nature", measureNature);
				String aggregationFunction = (String) fieldMetaData.getProperty(PROPERTY_AGGREGATION_FUNCTION);
				logger.debug("Read property " + PROPERTY_AGGREGATION_FUNCTION + ": its value is [" + aggregationFunction + "]");
				fieldMetaDataJSON.put("funct", AggregationFunctions.get(aggregationFunction).getName());
				fieldMetaDataJSON.put("iconCls", measureNature);
				String decimalPrecision = (String) fieldMetaData.getProperty(IFieldMetaData.DECIMALPRECISION);
				if (decimalPrecision != null) {
					fieldMetaDataJSON.put("precision", decimalPrecision);
				} else {
					fieldMetaDataJSON.put("precision", "2");
				}
				break;
			}

			if (type.equals(it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType.MEASURE)) {
				measuresList.add(fieldMetaDataJSON);
			} else {
				attributesList.add(fieldMetaDataJSON);
			}
		}

		// put first measures and only after attributes

		for (Iterator iterator = measuresList.iterator(); iterator.hasNext();) {
			JSONObject jsonObject = (JSONObject) iterator.next();
			fieldsMetaDataJSON.put(jsonObject);
		}

		for (Iterator iterator = attributesList.iterator(); iterator.hasNext();) {
			JSONObject jsonObject = (JSONObject) iterator.next();
			fieldsMetaDataJSON.put(jsonObject);
		}

		return fieldsMetaDataJSON;
	}

	protected String getFieldAlias(IFieldMetaData fieldMetaData) {
		String fieldAlias = fieldMetaData.getAlias() != null ? fieldMetaData.getAlias() : fieldMetaData.getName();
		return fieldAlias;
	}

	protected String getFieldName(IFieldMetaData fieldMetaData) {
		String fieldName = fieldMetaData.getName();
		return fieldName;
	}

	protected String getFieldColumnType(IFieldMetaData fieldMetaData) {
		String fieldColumnType = fieldMetaData.getType().toString();
		// clean the class type name
		fieldColumnType = fieldColumnType.substring(fieldColumnType.lastIndexOf(".") + 1);
		return fieldColumnType;
	}

	protected String serializeDataSets(List<IDataSet> dataSets, String typeDocWizard) {
		try {
			JSONArray datasetsJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(dataSets, null);
			JSONArray datasetsJSONReturn = putActions(getUserProfile(), datasetsJSONArray, typeDocWizard);
			JSONObject resultJSON = new JSONObject();
			resultJSON.put("root", datasetsJSONReturn);
			return resultJSON.toString();
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while serializing results", t);
		}
	}

	public JSONArray writeParameters(List<JSONObject> paramsMeta) throws Exception {
		JSONArray paramsMetaDataJSON = new JSONArray();

		for (Iterator iterator = paramsMeta.iterator(); iterator.hasNext();) {
			JSONObject jsonObject = (JSONObject) iterator.next();
			paramsMetaDataJSON.put(jsonObject);
		}

		return paramsMetaDataJSON;
	}

	@POST
	@Path("/{datasetLabel}/cleanCache")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String cleanCache(@PathParam("datasetLabel") String datasetLabel) {
		logger.debug("IN");
		try {
			logger.debug("get dataset with label " + datasetLabel);
			IDataSet dataSet = getDatasetManagementAPI().getDataSet(datasetLabel);
			ICache cache = SpagoBICacheManager.getCache();
			logger.debug("Delete from cache dataset references with signature " + dataSet.getSignature());
			cache.delete(dataSet.getSignature());
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(),
					"An unexpected error occurred while cleaning cache for dataset with label " + datasetLabel, t);
		} finally {
			logger.debug("OUT");
		}
		return null;
	}

	/**
	 * Check if the association passed is valid ',' is valid if number of record from association is lower than maximum of single datasets
	 *
	 * @param association
	 */

	@POST
	@Path("/{association}/checkAssociation")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String checkAssociation(@PathParam("association") String association) {
		logger.debug("IN");

		JSONObject toReturn = new JSONObject();

		logger.debug("Association to check " + association);

		try {
			JSONArray arrayAss = new JSONArray(association);

			// boolean valid =
			// getDatasetManagementAPI().checkAssociation(arrayAss);
			// logger.debug("The association is valid? " + valid);
			// toReturn.put("valid", valid);
			toReturn.put("valid", true);

		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "Error while checking association " + association, e);
		} finally {
			logger.debug("OUT");
		}

		return toReturn.toString();
	}

	/**
	 * Persist a dataset list in cache, or use a persisted dataset if its datasource match with the cache datasource
	 *
	 * @param labels
	 */

	@POST
	@Path("/list/persist")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String persistDataSetsService() {
		JSONObject labels = null;
		try {
			labels = RestUtilities.readBodyAsJSONObject(request);
		} catch (Exception e1) {
			logger.error("error reading the labels from request: ", e1);
			throw new RuntimeException("error reading the labels from request ", e1);
		}

		return persistDataSets(labels);
	}

	public String persistDataSets(JSONObject labels) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory.start("spagobi.dataset.persist");
		JSONObject labelsJSON = new JSONObject();

		Iterator<String> keys = labels.keys();
		while (keys.hasNext()) {
			String label = keys.next();
			try {
				IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
				dataSetDao.setUserProfile(getUserProfile());
				IDataSet dataSet = dataSetDao.loadDataSetByLabel(label);
				if (dataSet == null) {
					throw new SpagoBIRuntimeException("Impossibile to load dataSet with label [" + label + "]");
				}
				SQLDBCache cache = (SQLDBCache) SpagoBICacheManager.getCache();
				String tableName = null;
				if (dataSet.isPersisted() && dataSet.getDataSourceForWriting().getDsId() == cache.getDataSource().getDsId()) {
					tableName = dataSet.getTableNameForReading();
				} else if (dataSet.isFlatDataset() && dataSet.getDataSource().getDsId() == cache.getDataSource().getDsId()) {
					tableName = dataSet.getTableNameForReading();
				} else {
					DatasetManagementAPI dataSetManagementAPI = getDatasetManagementAPI();
					dataSetManagementAPI.setUserProfile(getUserProfile());
					tableName = dataSetManagementAPI.persistDataset(label, true);
					Monitor monitorIdx = MonitorFactory.start("spagobi.dataset.persist.indixes");
					if (tableName != null) {
						JSONArray columnsArray = labels.getJSONArray(label);
						Set<String> columns = new HashSet<String>(columnsArray.length());
						for (int i = 0; i < columnsArray.length(); i++) {
							String column = columnsArray.getString(i);
							columns.add(column);
						}
						if (columns.size() > 0) {
							dataSetManagementAPI.createIndexes(label, columns);
						}
					}
					monitorIdx.stop();
				}
				if (tableName != null) {
					logger.debug("Dataset with label " + label + " is stored in table with name " + tableName);
					labelsJSON.put(label, tableName);
				} else {
					logger.debug("Impossible to get dataset with label [" + label + "]");
				}
			} catch (JSONException | EMFUserError e) {
				logger.error("error in persisting dataset with label: " + label, e);
				throw new RuntimeException("error in persisting dataset with label " + label);
			}
		}

		logger.debug("OUT");
		monitor.stop();
		return labelsJSON.toString();
	}

	protected List<IDataSet> getListOfGenericDatasets(IDataSetDAO dsDao, Integer start, Integer limit, JSONObject filters, JSONObject ordering)
			throws JSONException, EMFUserError {

		if (start == null) {
			start = DataSetConstants.START_DEFAULT;
		}
		if (limit == null) {
			// limit = DataSetConstants.LIMIT_DEFAULT;
			limit = DataSetConstants.LIMIT_DEFAULT;
		}
		JSONObject filtersJSON = null;

		List<IDataSet> items = null;
		if (true) {
			filtersJSON = filters;
			String hsql = filterList(filtersJSON, ordering);
			items = dsDao.loadFilteredDatasetList(hsql, start, limit, getUserProfile().getUserId().toString());
		} else {// not filtered
			items = dsDao.loadPagedDatasetList(start, limit);
			// items =
			// dsDao.loadPagedDatasetList(start,limit,profile.getUserUniqueIdentifier().toString(),
			// true);
		}
		return items;
	}

	private String filterList(JSONObject filtersJSON, JSONObject ordering) throws JSONException {
		logger.debug("IN");
		boolean isAdmin = false;
		try {
			// Check if user is an admin
			isAdmin = getUserProfile().isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN);
		} catch (EMFInternalError e) {
			logger.error("Error while filtering datasets");
		}
		String hsql = " from SbiDataSet h where h.active = true ";
		// Ad Admin can see other users' datasets
		/*
		 * if (!isAdmin) { filter is applyed in the dao because need also to take care about categories hsql = hsql + " and h.owner = '" +
		 * getUserProfile().getUserUniqueIdentifier().toString() + "'"; }
		 */
		if (filtersJSON != null) {
			String valuefilter = (String) filtersJSON.get(SpagoBIConstants.VALUE_FILTER);
			String typeFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_FILTER);
			String columnFilter = (String) filtersJSON.get(SpagoBIConstants.COLUMN_FILTER);
			if (typeFilter.equals("=")) {
				hsql += " and h." + columnFilter + " = '" + valuefilter + "'";
			} else if (typeFilter.equals("like")) {
				hsql += " and h." + columnFilter + " like '%" + valuefilter + "%'";
			}
		}

		if (ordering != null) {
			boolean reverseOrdering = ordering.optBoolean("reverseOrdering");
			String columnOrdering = ordering.optString("columnOrdering");
			if (columnOrdering != null && !columnOrdering.isEmpty()) {
				if (reverseOrdering) {
					hsql += "order by h." + columnOrdering.toLowerCase() + " desc";
				} else {
					hsql += "order by h." + columnOrdering.toLowerCase() + " asc";
				}
			}

		}
		logger.debug("OUT");
		return hsql;
	}

}
