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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONObjectDeserializator;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.api.common.AbstractDataSetResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.DataSetBasicInfo;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.common.iterator.CsvStreamingOutput;
import it.eng.spagobi.tools.dataset.common.iterator.DataIterator;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.service.ManageDataSetsForREST;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.ActionNotPermittedException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
@Path("/1.0/datasets")
@ManageAuthorization
public class DataSetResource extends AbstractDataSetResource {

	static protected Logger logger = Logger.getLogger(DataSetResource.class);

	/**
	 * @deprecated Use {@link it.eng.spagobi.api.v3.DataSetResource#getDataSets(String, List)} TODO : Delete
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	@Deprecated
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
	public Response getDataSetsForLOV(@QueryParam("typeDoc") String typeDoc, @QueryParam("callback") String callback) {
		logger.debug("IN");
		List<DataSetBasicInfo> toReturn = new ArrayList<>();
		try {
			toReturn = getDatasetManagementAPI().getDatasetsForLov();
			return Response.ok(toReturn).build();
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
	 * @deprecated Use {@link it.eng.spagobi.api.v3.DataSetResource#getDataSetsPaginationOption(String, String, int, int, JSONObject, JSONObject, List)} TODO
	 *             ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	@GET
	@Path("/pagopt/")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSetsPaginationOption(@QueryParam("typeDoc") String typeDoc, @QueryParam("callback") String callback,
			@QueryParam("offset") Integer offsetInput, @QueryParam("fetchSize") Integer fetchSizeInput, @QueryParam("filters") JSONObject filters,
			@QueryParam("ordering") JSONObject ordering, @QueryParam("tags") List<Integer> tags) {

		logger.debug("IN");

		try {

			int offset = (offsetInput == null) ? -1 : offsetInput;
			int fetchSize = (fetchSizeInput == null) ? -1 : fetchSizeInput;

			IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(getUserProfile());

			List<IDataSet> dataSets = getListOfGenericDatasets(dsDao, offset, fetchSize, filters, ordering, tags);

			List<IDataSet> toBeReturned = new ArrayList<IDataSet>();

			for (IDataSet dataset : dataSets) {

				if (dataset == null)
					continue;

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

	@GET
	@Path("/olderversions/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String geOlderVersionsForDataset(@PathParam("id") int id) {
		logger.debug("IN");
		List<IDataSet> olderVersions = null;
		JSONObject toReturn = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		try {
			IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(getUserProfile());

			olderVersions = dsDao.loadDataSetOlderVersions(id);

			if (olderVersions != null && !olderVersions.isEmpty()) {
				Iterator<IDataSet> it = olderVersions.iterator();
				while (it.hasNext()) {
					IDataSet oldVersion = it.next();
					Integer dsVersionNum = null;
					if (oldVersion instanceof VersionedDataSet) {
						dsVersionNum = ((VersionedDataSet) oldVersion).getVersionNum();
					}
					String dsType = oldVersion.getDsType();
					String userIn = oldVersion.getUserIn();
					Date timeIn = oldVersion.getDateIn();
					String timeInAsStr = null;

					if (timeIn != null) {
						DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
						DateTime dateTime = new DateTime(timeIn);
						timeInAsStr = formatter.print(dateTime);
					}

					JSONObject oldDsJsonObj = new JSONObject();
					oldDsJsonObj.put("type", dsType);
					oldDsJsonObj.put("userIn", userIn);
					oldDsJsonObj.put("versNum", dsVersionNum);
					oldDsJsonObj.put("dateIn", timeInAsStr);
					oldDsJsonObj.put("dsId", id);
					jsonArray.put(oldDsJsonObj);
				}
				toReturn.put("root", jsonArray);
			}

		} catch (SpagoBIRuntimeException ex) {
			throw ex;
		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
		}

		return toReturn.toString();
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

		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
		IDataSet dataset = datasetDao.loadDataSetById(new Integer(id));

		return dataset.getLabel();
	}

	@PUT
	@Path("/clone-file")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response cloneFile(@QueryParam("fileName") String fileName) {
		try {
			String resourcePath = SpagoBIUtilities.getResourcePath();
			java.nio.file.Path toClone = java.nio.file.Paths.get(resourcePath, "dataset", "files", fileName);
			java.nio.file.Path tempDir = java.nio.file.Paths.get(resourcePath, "dataset", "files", "temp");
			java.nio.file.Path cloned = tempDir.resolve(toClone.getFileName());
			Files.copy(toClone, cloned, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot clone file {" + fileName + "} into temp dir", e);
		}
		return Response.ok().build();
	}

	/**
	 * Return the entire dataset according to its ID.
	 *
	 * @param id
	 * @return
	 * @throws JSONException
	 * @throws SerializationException
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	@GET
	@Path("/dataset/id/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSetById(@PathParam("id") Integer id) throws JSONException, SerializationException {
		logger.debug("IN");
		IDataSetDAO datasetDao;
		IDataSet datasetToReturn;
		ISchedulerDAO schedulerDAO;

		try {
			datasetDao = DAOFactory.getDataSetDAO();
			datasetDao.setUserProfile(getUserProfile());
			datasetToReturn = datasetDao.loadDataSetById(id);

			if (datasetToReturn.isPersisted()) {
				schedulerDAO = DAOFactory.getSchedulerDAO();

				List<Trigger> triggers = schedulerDAO.loadTriggers("PersistDatasetExecutions", datasetToReturn.getLabel());

				if (triggers.isEmpty()) {
					datasetToReturn.setScheduled(false);
				} else {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

					// Dataset scheduling is mono-trigger
					Trigger trigger = triggers.get(0);

					if (!trigger.isRunImmediately()) {

						datasetToReturn.setScheduled(true);

						if (trigger.getStartTime() != null) {
							datasetToReturn.setStartDateField(sdf.format(trigger.getStartTime()));
						} else {
							datasetToReturn.setStartDateField("");
						}

						if (trigger.getEndTime() != null) {
							datasetToReturn.setEndDateField(sdf.format(trigger.getEndTime()));
						} else {
							datasetToReturn.setEndDateField("");
						}

						datasetToReturn.setSchedulingCronLine(trigger.getChronExpression().getExpression());
					}
				}
			}
		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
		}

		return serializeDataSet(datasetToReturn, null);
	}

	@GET
	@Path("/dataset/{dsLabel}/category")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSetCategoryByDsLabel(@PathParam("dsLabel") String dsLabel) {
		try {
			IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
			datasetDao.setUserProfile(getUserProfile());
			IDataSet dataset = datasetDao.loadDataSetByLabel(dsLabel);
			String category = dataset.getCategoryCd();
			return category;
		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
		}
	}

	/**
	 * Acquire required version of the dataset
	 *
	 * @param id        The ID of the dataset whose version with the versionId ID should be restored.
	 * @param versionId The ID of the version of the dataset that should be restored and exchanged for the current one (active).
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

		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();

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

	@GET
	@Path("/{id}/export")
	@Produces(MediaType.TEXT_PLAIN)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response export(@PathParam("id") int id, @QueryParam("outputType") @DefaultValue("csv") String outputType,
			@QueryParam("DRIVERS") JSONObject driversJson, @QueryParam("PARAMETERS") JSONObject params) {

		Map<String, Object> drivers = null;
		Map<String, String> parameters = new HashMap<>();

		if (params != null) {
			try {
				JSONArray paramsJson = params.getJSONArray("parameters");
				JSONObject pars = new JSONObject();
				pars.put(DataSetConstants.PARS, paramsJson);
				ManageDataSetsForREST mdsr = new ManageDataSetsForREST();
				parameters = mdsr.getDataSetParametersAsMap(pars);

			} catch (Exception e) {
				logger.debug("Cannot read dataset parameters");
				throw new SpagoBIRestServiceException(getLocale(), e);
			}
		}

		try {
			drivers = JSONObjectDeserializator.getHashMapFromJSONObject(driversJson);
		} catch (Exception e) {
			logger.debug("Cannot read dataset drivers", e);
			throw new SpagoBIRuntimeException("Cannot read dataset drivers", e);
		}

		IDataSet dataSet = getDataSetDAO().loadDataSetById(id);

		try {
			new DatasetManagementAPI(getUserProfile()).canLoadData(dataSet);
		} catch (ActionNotPermittedException e) {
			logger.error("User " + getUserProfile().getUserId() + " cannot export the dataset with label " + dataSet.getLabel());
			throw new SpagoBIRestServiceException(e.getI18NCode(), buildLocaleFromSession(),
					"User " + getUserProfile().getUserId() + " cannot export the dataset with label " + dataSet.getLabel(), e, "MessageFiles.messages");
		}
		dataSet.setDrivers(drivers);
		dataSet.setParamsMap(parameters);

		dataSet.setUserProfileAttributes(getUserProfile().getUserAttributes());
		Assert.assertNotNull(dataSet, "Impossible to find a dataset with id [" + id + "]");
		// Assert.assertTrue(dataSet.getParamsMap() == null || dataSet.getParamsMap().isEmpty(), "Impossible to export a dataset with parameters");
		Assert.assertTrue(dataSet.isIterable(), "Impossible to export a non-iterable data set");
		DataIterator iterator = null;
		try {
			logger.debug("Starting iteration to transfer data");
			iterator = dataSet.iterator();

			StreamingOutput stream = new CsvStreamingOutput(iterator);

			ResponseBuilder response = Response.ok(stream);
			response.header("Content-Disposition", "attachment;filename=" + dataSet.getName() + "." + outputType);
			return response.build();
		} catch (Exception e) {
			if (iterator != null) {
				iterator.close();
			}
			throw e;
		}
	}

	@Override
	@DELETE
	@Path("/{label}")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response deleteDataset(@PathParam("label") String label) {
		return super.deleteDataset(label);
	}

	@DELETE
	@Path("/id/{id}")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response deleteDatasetById(@PathParam("id") int id) {
		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
		IDataSet dataset = datasetDao.loadDataSetById(id);
		String label = dataset.getLabel();
		return super.deleteDataset(label);
	}

	/**
	 * Delete a version for the selected dataset.
	 *
	 * @param id        The ID of the selected dataset.
	 * @param versionId The ID of the version of the selected dataset.
	 * @return Status of the request (OK status).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	@DELETE
	@Path("/{id}/version/{versionId}")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response deleteDatasetVersion(@PathParam("id") String datasetId, @PathParam("versionId") String versionId) {

		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
		datasetDao.setUserProfile(getUserProfile());

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
	 * @param datasetId The datasetId of the selected dataset.
	 * @return Status of the request (OK status).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	@DELETE
	@Path("/{id}/allversions")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response deleteAllDatasetVersions(@PathParam("id") String datasetId) {

		IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
		datasetDao.setUserProfile(getUserProfile());

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
		Assert.assertTrue(StringUtils.isNotBlank(label), "Dataset label cannot must be valorized");
		try {
			IDataSetDAO datasetDao = DAOFactory.getDataSetDAO();
			IDataSet dataSet = datasetDao.loadDataSetByLabel(label);
			Assert.assertNotNull(dataSet, "Dataset cannot be null");
			List<JSONObject> fieldsParameters = dataSet.getDataSetParameters();
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

	@GET
	@Path("/federated/{federationId}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response getFederatedDataSetsByFederetionId(@PathParam("federationId") Integer federationId) {
		logger.debug("IN");
		List<DataSetBasicInfo> toReturn = new ArrayList<>();
		try {
			IDataSetDAO dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(getUserProfile());
			toReturn = getDatasetManagementAPI().getFederatedDataSetsByFederation(federationId);

			return Response.ok(toReturn).build();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * @deprecated {@link it.eng.spagobi.api.v3.DataSetResource#getEnterpriseDataSet(String)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
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

	/**
	 * @deprecated {@link it.eng.spagobi.api.v3.DataSetResource#getOwnedDataSet(String)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
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

	/**
	 * @deprecated {@link it.eng.spagobi.api.v3.DataSetResource#getSharedDataSet(String)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
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

	/**
	 * @deprecated {@link it.eng.spagobi.api.v3.DataSetResource#getUncertifiedDataSet(String)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
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

	/**
	 * @param typeDoc
	 * @return List of Datasets that Final User can see. All DataSet Tab in Workspace.
	 * @deprecated {@link it.eng.spagobi.api.v3.DataSetResource#getMyDataDataSet(String)} TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	@GET
	@Path("/mydata")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getMyDataDataSet(@QueryParam("typeDoc") String typeDoc) {
		logger.debug("IN");
		try {
			List<IDataSet> dataSets = getDatasetManagementAPI().getMyDataDataSet();
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

	@GET
	@Path("filterbytags/owned")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String filterOwnedDatasetsByTags(@QueryParam("tags") List<Integer> tagIds) {
		logger.debug("IN");
		try {
			List<IDataSet> dataSets = DAOFactory.getDataSetDAO().loadDatasetsByTags(getUserProfile(), tagIds, "owned");
			return serializeDataSets(dataSets, null);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("filterbytags/enterprise")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String filterEnterpriseDatasetsByTags(@QueryParam("tags") List<Integer> tagIds) {
		logger.debug("IN");
		try {
			List<IDataSet> dataSets = DAOFactory.getDataSetDAO().loadDatasetsByTags(getUserProfile(), tagIds, "enterprise");
			return serializeDataSets(dataSets, null);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("filterbytags/shared")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String filterSharedDatasetsByTags(@QueryParam("tags") List<Integer> tagIds) {
		logger.debug("IN");
		try {
			List<IDataSet> dataSets = DAOFactory.getDataSetDAO().loadDatasetsByTags(getUserProfile(), tagIds, "shared");
			return serializeDataSets(dataSets, null);
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
		String toReturnString = null;
		try {
			dsDao = DAOFactory.getDataSetDAO();
			dsDao.setUserProfile(getUserProfile());
			JSONObject json = RestUtilities.readBodyAsJSONObject(req);
			ManageDataSetsForREST mdsfr = new ManageDataSetsForREST();

			toReturnString = mdsfr.insertDataset(json.toString(), dsDao, null, getUserProfile(), req);
		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", e);
			// throw new SpagoBIRestServiceException(getLocale(), e);
		}

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

		List<JSONObject> spatialAttributesList = new ArrayList<JSONObject>();
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

				attributesList.add(fieldMetaDataJSON);
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

				measuresList.add(fieldMetaDataJSON);
				break;
			case SPATIAL_ATTRIBUTE:
				Object isSegmentSpatialAttributeObj = fieldMetaData.getProperty(PROPERTY_IS_SEGMENT_ATTRIBUTE);
				logger.debug("Read property " + PROPERTY_IS_SEGMENT_ATTRIBUTE + ": its value is [" + propertyRawValue + "]");
				String spatialAttributeNature = (isSegmentSpatialAttributeObj != null && Boolean.parseBoolean(isSegmentSpatialAttributeObj.toString()))
						? "segment_attribute"
						: "attribute";

				logger.debug("The nature of the attribute is recognized as " + spatialAttributeNature);
				fieldMetaDataJSON.put("nature", spatialAttributeNature);
				fieldMetaDataJSON.put("funct", AggregationFunctions.NONE);
				fieldMetaDataJSON.put("iconCls", spatialAttributeNature);

				spatialAttributesList.add(fieldMetaDataJSON);
				break;
			default:
				break;
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

		for (Iterator iterator = spatialAttributesList.iterator(); iterator.hasNext();) {
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
			JSONArray datasetsJSONArray = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(dataSets, buildLocaleFromSession());
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
			ICache cache = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
			logger.debug("Delete from cache dataset references with signature " + dataSet.getSignature());
			cache.delete(dataSet.getSignature(), true);
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
				SQLDBCache cache = (SQLDBCache) CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
				String tableName = null;
				if (dataSet.isPersisted() && dataSet.getDataSourceForWriting().getDsId() == cache.getDataSource().getDsId()) {
					tableName = dataSet.getTableNameForReading();
				} else if (dataSet.isFlatDataset() && dataSet.getDataSource().getDsId() == cache.getDataSource().getDsId()) {
					tableName = dataSet.getTableNameForReading();
				} else if (dataSet.isPreparedDataSet() && dataSet.getDataSource().getDsId() == cache.getDataSource().getDsId()) {
					tableName = dataSet.getTableNameForReading();
				} else {
					DatasetManagementAPI dataSetManagementAPI = getDatasetManagementAPI();
					dataSetManagementAPI.setUserProfile(getUserProfile());
					tableName = dataSetManagementAPI.persistDataset(label);
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
			} catch (Exception e) {
				logger.error("error in persisting dataset with label: " + label, e);
				throw new RuntimeException("error in persisting dataset with label " + label);
			}
		}

		logger.debug("OUT");
		monitor.stop();
		return labelsJSON.toString();
	}

	/**
	 * @deprecated TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	protected List<IDataSet> getListOfGenericDatasets(IDataSetDAO dsDao, Integer start, Integer limit, JSONObject filters, JSONObject ordering,
			List<Integer> tags) throws JSONException, EMFUserError {
		logger.debug("IN");
		if (start == null) {
			start = DataSetConstants.START_DEFAULT;
		}
		if (limit == null) {
			limit = DataSetConstants.LIMIT_DEFAULT;
		}

		List<IDataSet> items = null;
		try {
			if (tags.isEmpty() && filters == null) {
				String hsql = getCommonQuery(ordering);
				items = dsDao.loadFilteredDatasetList(hsql, start, limit, getUserProfile().getUserId().toString());
			} else {
				items = dsDao.loadFilteredDatasetList(start, limit, getUserProfile().getUserId().toString(), filters, ordering, tags);
			}
		} catch (Throwable t) {
			logger.error("Error has occured while getting list of Datasets", t);
			throw t;
		}
		logger.debug("OUT");
		return items;
	}

	/**
	 * @deprecated TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	private String getCommonQuery(JSONObject ordering) throws JSONException {
		logger.debug("IN");
		StringBuffer sb = new StringBuffer("from SbiDataSet h where h.active = true ");

		if (ordering != null) {
			boolean reverseOrdering = ordering.optBoolean("reverseOrdering");
			String columnOrdering = ordering.optString("columnOrdering");
			if (columnOrdering.equalsIgnoreCase("dsTypeCd")) {
				columnOrdering = "type";
			}
			if (columnOrdering != null && !columnOrdering.isEmpty()) {
				sb.append("order by h.").append(columnOrdering.toLowerCase());
				if (reverseOrdering) {
					sb.append(" desc");
				}
			}

		}
		logger.debug("OUT");
		return sb.toString();
	}

}
