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
package it.eng.spagobi.api.v2;

import static it.eng.spagobi.tools.glossary.util.Util.getNumberOrNull;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
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
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.geotools.data.DataSourceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.knowage.commons.security.PathTraversalChecker;
import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.BusinessModelOpenUtils;
import it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelDriverRuntime;
import it.eng.spagobi.analiticalmodel.document.handlers.BusinessModelRuntime;
import it.eng.spagobi.analiticalmodel.execution.bo.LovValue;
import it.eng.spagobi.analiticalmodel.execution.bo.defaultvalues.DefaultValuesList;
import it.eng.spagobi.api.BusinessModelOpenParameters;
import it.eng.spagobi.api.common.AbstractDataSetResource;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.tools.catalogue.dao.IMetaModelsDAO;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.DataSetBasicInfo;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.tools.dataset.common.datawriter.IDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.constants.DatasetFunctionsConfig;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.ISbiDataSetDAO;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.dataset.metasql.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.BetweenFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.InFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.LikeFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.NotInFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.NullaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.PlaceholderFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilterOperator;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnsatisfiedFilter;
import it.eng.spagobi.tools.dataset.persist.IPersistedManager;
import it.eng.spagobi.tools.dataset.persist.PersistedHDFSManager;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.IDataBase;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.ValidationServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 */

@Path("/2.0/datasets")
@ManageAuthorization
public class DataSetResource extends AbstractDataSetResource {

	static protected Logger logger = Logger.getLogger(DataSetResource.class);

	public String getNotDerivedDataSets(@QueryParam("callback") String callback) {
		logger.debug("IN");

		IDataSetDAO dsDAO = getDataSetDAO();

		List<IDataSet> toBeReturned = dsDAO.loadNotDerivedDataSets(getUserProfile());

		try {
			logger.debug("OUT");
			if (callback == null || callback.isEmpty())

				return ((JSONArray) SerializerFactory.getSerializer("application/json").serialize(toBeReturned, buildLocaleFromSession())).toString();

			else {
				String jsonString = ((JSONArray) SerializerFactory.getSerializer("application/json").serialize(toBeReturned, buildLocaleFromSession()))
						.toString();

				return callback + "(" + jsonString + ")";
			}
		} catch (SerializationException e) {
			throw new SpagoBIRestServiceException(getLocale(), e);
		}
	}

	@GET
	@Path("/availableFunctions/{dsId}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String availableFunctions(@PathParam("dsId") String datasetId, @QueryParam("useCache") boolean useCache)
			throws JSONException, DataBaseException, EMFUserError, DataSourceException {
		logger.debug("IN");

		ISbiDataSetDAO dsDAO = DAOFactory.getSbiDataSetDAO();

		JSONObject jo = new JSONObject();

		DatasetFunctionsConfig datasetFunctionsConfig = new DatasetFunctionsConfig();

		if (useCache) {
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			IDataSource dataSource = dataSourceDAO.loadDataSourceWriteDefault();

			if (dataSource == null)
				throw new DataSourceException("No data source found for cache");

			String dataBaseDialect = dataSource.getDialectName();
			List<String> availableFunctions = datasetFunctionsConfig.getAvailableFunctions(dataBaseDialect);
			jo.put("availableFunctions", availableFunctions);

			List<String> nullIfFunction = datasetFunctionsConfig.getNullifFunction(dataBaseDialect);
			jo.put("nullifFunction", nullIfFunction);

		} else {
			Tenant tenantManager = TenantManager.getTenant();
			SbiDataSet sbiDataSet = dsDAO.loadSbiDataSetByIdAndOrganiz(Integer.valueOf(datasetId), tenantManager.getName());
			IDataSet iDataSet = DataSetFactory.toDataSet(sbiDataSet);
			IDataBase database = DataBaseFactory.getDataBase(iDataSet.getDataSource());
			String dataBaseDialect = database.getDatabaseDialect().getValue();

			List<String> availableFunctions = datasetFunctionsConfig.getAvailableFunctions(dataBaseDialect);
			jo.put("availableFunctions", availableFunctions);

			List<String> nullIfFunction = datasetFunctionsConfig.getNullifFunction(dataBaseDialect);
			jo.put("nullifFunction", nullIfFunction);
		}

		logger.debug("OUT");
		return jo.toString();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSets(@QueryParam("includeDerived") String includeDerived, @QueryParam("callback") String callback,
			@QueryParam("asPagedList") Boolean paged, @QueryParam("Page") String pageStr, @QueryParam("ItemPerPage") String itemPerPageStr,
			@QueryParam("label") String search, @QueryParam("seeTechnical") Boolean seeTechnical, @QueryParam("ids") String ids,
			@QueryParam("spatialOnly") boolean spatialOnly) {
		logger.debug("IN");

		if ("no".equalsIgnoreCase(includeDerived)) {
			return getNotDerivedDataSets(callback);
		}

		if (Boolean.TRUE.equals(paged)) {
			return getDatasetsAsPagedList(pageStr, itemPerPageStr, search, seeTechnical, ids, spatialOnly);
		}

		ISbiDataSetDAO dsDAO = DAOFactory.getSbiDataSetDAO();

		List<SbiDataSet> dataSets = dsDAO.loadSbiDataSets();
		List<SbiDataSet> toBeReturned = new ArrayList<SbiDataSet>();

		for (SbiDataSet dataset : dataSets) {
			IDataSet iDataSet = DataSetFactory.toDataSet(dataset);
			if (DataSetUtilities.isExecutableByUser(iDataSet, getUserProfile()))
				toBeReturned.add(dataset);
		}

		logger.debug("OUT");
		if (callback == null || callback.isEmpty())
			return JsonConverter.objectToJson(toBeReturned, toBeReturned.getClass());
		else {
			String jsonString = JsonConverter.objectToJson(toBeReturned, toBeReturned.getClass());

			return callback + "(" + jsonString + ")";
		}
	}

	@GET
	@Path("/basicinfo/all")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDatasetsBasicInfo() {
		logger.debug("IN");
		List<DataSetBasicInfo> toReturn = new ArrayList<>();
		IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();

		try {
			toReturn = dsDAO.loadDatasetsBasicInfo();
			return Response.ok(toReturn).build();
		} catch (Exception e) {
			logger.error("Error while loading the datasets basic info", e);
			throw new SpagoBIRuntimeException("Error while loading the datasets basic info", e);
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

	@Override
	@GET
	@Path("/{label}/content")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response execute(@PathParam("label") String label, String body) {
		return super.execute(label, body);
	}

	@POST
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response addDataSet(String body) {
		SbiDataSet sbiDataset = (SbiDataSet) JsonConverter.jsonToValidObject(body, SbiDataSet.class);

		sbiDataset.setId(new SbiDataSetId(null, 1, getUserProfile().getOrganization()));
		sbiDataset.setOwner((String) getUserProfile().getUserId());
		IDataSet dataset = DataSetFactory.toDataSet(sbiDataset);

		try {
			DAOFactory.getDataSetDAO().insertDataSet(dataset);

			if (dataset.isPersistedHDFS()) {
				IPersistedManager ptm = new PersistedHDFSManager(getUserProfile());
				ptm.persistDataSet(dataset);
			}

			if (dataset.isPersisted()) {
				IPersistedManager ptm = new PersistedTableManager(getUserProfile());
				ptm.persistDataSet(dataset);
			}

		} catch (Exception e) {
			logger.error("Error while creating the dataset: " + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while creating the dataset: " + e.getMessage(), e);
		}

		try {
			return Response.created(new URI("1.0/datasets/" + dataset.getLabel().replace(" ", "%20"))).build();
		} catch (URISyntaxException e) {
			logger.error("Error while creating the resource url, maybe an error in the label", e);
			throw new SpagoBIRuntimeException("Error while creating the resource url, maybe an error in the label", e);
		}
	}

	@PUT
	@Path("/{label}")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response modifyDataSet(@PathParam("label") String label, String body) {
		IDataSet dataset = null;

		try {
			dataset = getDatasetManagementAPI().getDataSet(label);
		} catch (Exception e) {
			logger.error("Error while creating the dataset: " + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while creating the dataset: " + e.getMessage(), e);
		}

		SbiDataSet sbiDataset = (SbiDataSet) JsonConverter.jsonToValidObject(body, SbiDataSet.class);

		int version = 1;
		if (dataset instanceof VersionedDataSet) {
			version = ((VersionedDataSet) dataset).getVersionNum();
		}

		sbiDataset.setId(new SbiDataSetId(dataset.getId(), version + 1, dataset.getOrganization()));
		sbiDataset.setOwner(dataset.getOwner());
		sbiDataset.setLabel(label);

		IDataSet newDataset = DataSetFactory.toDataSet(sbiDataset);

		try {
			DAOFactory.getDataSetDAO().modifyDataSet(newDataset);
		} catch (Exception e) {
			logger.error("Error while creating the dataset: " + e.getMessage(), e);
			throw new SpagoBIRuntimeException("Error while creating the dataset: " + e.getMessage(), e);
		}

		return Response.ok().build();
	}

	@Override
	@DELETE
	@Path("/{label}")
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response deleteDataset(@PathParam("label") String label) {
		return super.deleteDataset(label);
	}

	@GET
	@Path("/download/file")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadDataSetFile(@QueryParam("dsLabel") String dsLabel, @QueryParam("type") String type) {
		File file = null;
		ResponseBuilder response = null;
		IDataSet myDataset = getDatasetManagementAPI().getDataSet(dsLabel);
		List<IDataSet> visibleDatasets = getDatasetManagementAPI().getDataSets();
		if (!visibleDatasets.contains(myDataset)) {
			logger.warn("User not allowed to download file from dataset: " + dsLabel);
			return Response.status(Status.UNAUTHORIZED).build();
		}
		try {
			String fileName = getFileNameFromDatasetConfiguration(myDataset);
			String resourcePath = SpagoBIUtilities.getResourcePath();
			File fileDirectory = new File(resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files");
			file = new File(fileDirectory, fileName);

			PathTraversalChecker.preventPathTraversalAttack(file, fileDirectory);

			if (file == null || !file.exists()) {
				logger.error("File cannot be found");
				throw new SpagoBIRuntimeException("File [" + fileName + "] is not found");
			}

			response = Response.ok(file);
			String mimeType = getMimeType(type);
			if (mimeType != null) {
				response.header("Content-Type", mimeType);
			}
			response.header("Content-Disposition", "attachment; fileName=" + fileName + "; fileType=" + type + "; extensionFile=" + type);
		} catch (SpagoBIRuntimeException e) {
			throw new SpagoBIRestServiceException(getLocale(), e);
		} catch (Exception e) {
			logger.error("Error while downloading Dataset file", e);
			throw new SpagoBIRuntimeException("Error while downloading dataset file");
		}
		return response.build();
	}

	private String getFileNameFromDatasetConfiguration(IDataSet dataSet) {
		try {
			dataSet = dataSet instanceof VersionedDataSet ? ((VersionedDataSet) dataSet).getWrappedDataset() : dataSet;
			JSONObject conf = new JSONObject(dataSet.getConfiguration());
			String fileName = conf.getString("fileName");
			return fileName;
		} catch (Exception e) {
			logger.error("Error while getting file name from configuration", e);
			throw new SpagoBIRuntimeException("Error while getting file name from configuration");
		}
	}

	private String getMimeType(String type) {
		String mimeType = "";
		if (type.equalsIgnoreCase("XLS") || type.equalsIgnoreCase("CSV")) {
			mimeType = "application/vnd.ms-excel";
		} else if (type.equalsIgnoreCase("XLSX")) {
			mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
		} else {
			mimeType = null;
		}
		return mimeType;
	}

	public String getDatasetsAsPagedList(String pageStr, String itemPerPageStr, String search, Boolean seeTechnical, String ids, boolean spatialOnly) {

		try {
			ISbiDataSetDAO dao = DAOFactory.getSbiDataSetDAO();
			IDataSourceDAO dataSourceDAO = DAOFactory.getDataSourceDAO();
			dao.setUserProfile(getUserProfile());
			dataSourceDAO.setUserProfile(getUserProfile());

			Integer page = getNumberOrNull(pageStr);
			Integer item_per_page = getNumberOrNull(itemPerPageStr);
			search = search != null ? search : "";

			Integer[] idArray = getIdsAsIntegers(ids);

			List<SbiDataSet> dataset = null;
			if (UserUtilities.isAdministrator(getUserProfile())) {
				dataset = dao.loadPaginatedSearchSbiDataSet(search, page, item_per_page, null, null, idArray, spatialOnly);
			} else {
				dataset = dao.loadPaginatedSearchSbiDataSet(search, page, item_per_page, getUserProfile(), seeTechnical, idArray, spatialOnly);
			}

			JSONObject jo = new JSONObject();
			JSONArray ja = new JSONArray();

			for (SbiDataSet ds : dataset) {
				IDataSet dataSet = DataSetFactory.toDataSet(ds, getUserProfile());
				boolean isNearRealtimeSupported = isNearRealtimeSupported(dataSet);
				JSONObject jsonIDataSet = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(dataSet, null);

				JSONObject jsonSbiDataSet = new JSONObject(JsonConverter.objectToJson(ds, SbiDataSet.class));
				jsonSbiDataSet.put("isRealtime", dataSet.isRealtime());
				jsonSbiDataSet.put("isCachingSupported", dataSet.isCachingSupported());
				jsonSbiDataSet.put("parameters", jsonIDataSet.getJSONArray("pars"));
				jsonSbiDataSet.put("isIterable", dataSet.isIterable());
				jsonSbiDataSet.put("isNearRealtimeSupported", isNearRealtimeSupported);

				setDriversIntoDsJSONConfig(dataSet, ds, jsonSbiDataSet);

				ja.put(jsonSbiDataSet);
			}
			jo.put("item", ja);
			jo.put("itemCount", dao.countSbiDataSet(search, idArray));

			return jo.toString();
		} catch (Exception e) {
			logger.error("Error while getting the list of datasets", e);
			throw new SpagoBIRuntimeException("Error while getting the list of datasets", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void setDriversIntoDsJSONConfig(IDataSet dataSet, SbiDataSet ds, JSONObject jsonSbiDataSet) throws Exception {
		dataSet = dataSet instanceof VersionedDataSet ? ((VersionedDataSet) dataSet).getWrappedDataset() : dataSet;
		if (dataSet instanceof QbeDataSet) {
			try {
				Boolean loadDSwithDrivers = true;
				ArrayList<HashMap<String, Object>> drivers = null;
				String businessModelName = (String) jsonSbiDataSet.getJSONObject("configuration").get("qbeDatamarts");
				drivers = getDatasetDriversByModelName(businessModelName, loadDSwithDrivers);
				if (drivers != null) {
					jsonSbiDataSet.put("drivers", drivers);
				}
			} catch (Exception e) {
				LogMF.error(logger, "Error loading dataset {0} with id {1}", new String[] { ds.getName(), ds.getId().getDsId().toString() });
				throw e;
			}
		}
	}

	@Override
	public List<Filter> getFilters(String datasetLabel, JSONObject selectionsObject, Map<String, String> columnAliasToColumnName) throws JSONException {
		List<Filter> filters = new ArrayList<>(0);

		if (selectionsObject.has(datasetLabel)) {
			JSONObject datasetSelectionObject = selectionsObject.getJSONObject(datasetLabel);
			Iterator<String> it = datasetSelectionObject.keys();

			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(datasetLabel);

			boolean isAnEmptySelection = false;
			while (!isAnEmptySelection && it.hasNext()) {
				String columns = it.next();

				// check two cases: in case of click selection the contained is JSON array and operator is IN, in case of filter the contained is JSON object
				Object filtersObject = datasetSelectionObject.get(columns);

				String firstFilterOperator = null;
				JSONArray firstFilterValues = null;

				String secondFilterOperator = null;
				JSONArray secondFilterValues = null;

				if (filtersObject instanceof JSONArray) {
					logger.debug("coming from click");
					firstFilterOperator = "IN";
					firstFilterValues = (JSONArray) filtersObject;
					for (int i = 0; i < firstFilterValues.length(); i++) { // looking for filter embedded in array
						JSONObject filterJsonObject = firstFilterValues.optJSONObject(i);
						if (filterJsonObject != null && filterJsonObject.has("filterOperator")) {
							secondFilterOperator = filterJsonObject.optString("filterOperator");
							secondFilterValues = filterJsonObject.getJSONArray("filterVals");
							firstFilterValues.remove(i);
							break; // there is at most one filter embedded in array
						}
					}
				} else if (filtersObject instanceof JSONObject) {
					logger.debug("coming from filters");
					JSONObject filterJsonObject = (JSONObject) filtersObject;
					firstFilterOperator = filterJsonObject.optString("filterOperator");
					firstFilterValues = filterJsonObject.getJSONArray("filterVals");
				} else {
					throw new SpagoBIRuntimeException("Not recognised filter object " + filtersObject);
				}

				SimpleFilter firstSimpleFilter = getFilter(firstFilterOperator, firstFilterValues, columns, dataSet, columnAliasToColumnName);
				if (firstSimpleFilter != null) {
					SimpleFilter secondSimpleFilter = getFilter(secondFilterOperator, secondFilterValues, columns, dataSet, columnAliasToColumnName);
					if (secondSimpleFilter != null) {
						Filter compoundFilter = getComplexFilter((InFilter) firstSimpleFilter, secondSimpleFilter);
						filters.add(compoundFilter);
					} else {
						filters.add(firstSimpleFilter);
					}
				} else {
					isAnEmptySelection = true;
				}
			}

			if (isAnEmptySelection) {
				filters.clear();
				filters.add(new UnsatisfiedFilter());
			}
		}

		return filters;
	}

	public Filter getComplexFilter(InFilter inFilter, SimpleFilter anotherFilter) {
		SimpleFilterOperator operator = anotherFilter.getOperator();
		if (SimpleFilterOperator.EQUALS_TO_MIN.equals(operator) || SimpleFilterOperator.EQUALS_TO_MAX.equals(operator)) {
			List<Object> operands = inFilter.getOperands();
			Object result = operands.get(0);
			if (result instanceof Comparable == false) {
				throw new SpagoBIRuntimeException("Unable to compare operands of type [" + result.getClass().getName() + "]");
			}
			Comparable comparableResult = (Comparable) result;
			for (int i = 1; i < operands.size(); i++) {
				Object operand = operands.get(i);
				Comparable comparableOperand = (Comparable) operand;
				if (SimpleFilterOperator.EQUALS_TO_MIN.equals(operator)) { // min case
					if (comparableOperand.compareTo(comparableResult) < 0) {
						result = operand;
						comparableResult = comparableOperand;
					}
				} else { // max case
					if (comparableOperand.compareTo(comparableResult) > 0) {
						result = operand;
						comparableResult = comparableOperand;
					}
				}
			}
			return new UnaryFilter(inFilter.getProjections().get(0), SimpleFilterOperator.EQUALS_TO, result);
		} else {
			return new AndFilter(inFilter, anotherFilter);
		}
	}

	public SimpleFilter getFilter(String operatorString, JSONArray valuesJsonArray, String columns, IDataSet dataSet,
			Map<String, String> columnAliasToColumnName) throws JSONException {
		SimpleFilter filter = null;

		if (operatorString != null) {
			SimpleFilterOperator operator = SimpleFilterOperator.ofSymbol(operatorString.toUpperCase());

			if (valuesJsonArray.length() > 0 || operator.isNullary() || operator.isPlaceholder()) {
				List<String> columnsList = getColumnList(columns, dataSet, columnAliasToColumnName);

				List<Projection> projections = new ArrayList<>(columnsList.size());
				for (String columnName : columnsList) {
					projections.add(new Projection(dataSet, columnName));
				}

				List<Object> valueObjects = new ArrayList<>(0);
				if (!operator.isNullary() && !operator.isPlaceholder()) {
					for (int i = 0; i < valuesJsonArray.length(); i++) {
						String[] valuesArray = StringUtilities.splitBetween(valuesJsonArray.getString(i), "'", "','", "'");
						for (int j = 0; j < valuesArray.length; j++) {
							Projection projection = projections.get(j % projections.size());
							valueObjects.add(DataSetUtilities.getValue(valuesArray[j], projection.getType()));
						}
					}
				}

				if (operator.isPlaceholder()) {
					filter = new PlaceholderFilter(projections.get(0), operator);
				} else {
					if (SimpleFilterOperator.IN.equals(operator)) {
						if (valueObjects.isEmpty()) {
							filter = new NullaryFilter(projections.get(0), SimpleFilterOperator.IS_NULL);
						} else {
							filter = new InFilter(projections, valueObjects);
						}
					} else if (SimpleFilterOperator.NOT_IN.equals(operator)) {
						filter = new NotInFilter(projections, valueObjects);
					} else if (SimpleFilterOperator.LIKE.equals(operator)) {
						filter = new LikeFilter(projections.get(0), valueObjects.get(0).toString(), LikeFilter.TYPE.PATTERN);
					} else if (SimpleFilterOperator.BETWEEN.equals(operator)) {
						filter = new BetweenFilter(projections.get(0), valueObjects.get(0), valueObjects.get(1));
					} else if (operator.isNullary()) {
						filter = new NullaryFilter(projections.get(0), operator);
					} else {
						filter = new UnaryFilter(projections.get(0), operator, valueObjects.get(0));
					}
				}
			}
		}
		return filter;
	}

	@GET
	@Path("/preview")
	public void openPreview() {
		logger.debug("IN");
		try {
			response.setContentType(MediaType.TEXT_HTML);
			request.getRequestDispatcher("/WEB-INF/jsp/commons/preview.jsp").forward(request, response);
			response.flushBuffer();
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/{label}/data")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataStorePostWithJsonInBody(@PathParam("label") String label, String body, @DefaultValue("-1") @QueryParam("limit") int maxRowCount,
			@DefaultValue("-1") @QueryParam("offset") int offset, @DefaultValue("-1") @QueryParam("size") int fetchSize,
			@QueryParam("nearRealtime") boolean isNearRealtime, @QueryParam("widgetName") String widgetName) {
		try {
			Monitor timing = MonitorFactory.start("Knowage.DataSetResource.getDataStorePostWithJsonInBody:parseInputs");

			String parameters = null;
			Map<String, Object> driversRuntimeMap = null;
			String selections = null;
			String likeSelections = null;
			String aggregations = null;
			String summaryRow = null;
			String options = null;
			JSONArray jsonIndexes = null;

			if (StringUtilities.isNotEmpty(body)) {
				JSONObject jsonBody = new JSONObject(body);

				JSONObject jsonParameters = jsonBody.optJSONObject("parameters");
				parameters = jsonParameters != null ? jsonParameters.toString() : null;

				JSONObject jsonDrivers = jsonBody.optJSONObject("drivers");
				driversRuntimeMap = DataSetUtilities.getDriversMap(jsonDrivers);

				JSONObject jsonSelections = jsonBody.optJSONObject("selections");
				selections = jsonSelections != null ? jsonSelections.toString() : null;

				JSONObject jsonLikeSelections = jsonBody.optJSONObject("likeSelections");
				likeSelections = jsonLikeSelections != null ? jsonLikeSelections.toString() : null;

				JSONObject jsonAggregations = jsonBody.optJSONObject("aggregations");
				aggregations = jsonAggregations != null ? jsonAggregations.toString() : null;

				JSONObject jsonSummaryRow = jsonBody.optJSONObject("summaryRow");
				if (jsonSummaryRow != null) {
					summaryRow = jsonSummaryRow != null ? jsonSummaryRow.toString() : null;
				} else {
					JSONArray jsonSummaryRowArray = jsonBody.optJSONArray("summaryRow");
					summaryRow = jsonSummaryRowArray != null ? jsonSummaryRowArray.toString() : null;
				}

				JSONObject jsonOptions = jsonBody.optJSONObject("options");
				options = jsonOptions != null ? jsonOptions.toString() : null;

				jsonIndexes = jsonBody.optJSONArray("indexes");
			}

			Set<String> columns = null;

			if (jsonIndexes != null && jsonIndexes.length() > 0) {
				columns = new HashSet<String>();

				for (int k = 0; k < jsonIndexes.length(); k++) {
					JSONArray columnsArrayTemp = jsonIndexes.getJSONObject(k).getJSONArray("fields");

					JSONObject columnsArray = columnsArrayTemp.getJSONObject(0);

					if (columnsArray.getString("store").equals(label)) {
						columns.add(columnsArray.getString("column"));
					}
				}
			}
			timing.stop();
			return getDataStore(label, parameters, driversRuntimeMap, selections, likeSelections, maxRowCount, aggregations, summaryRow, offset, fetchSize,
					isNearRealtime, options, columns, widgetName);
		} catch (Exception e) {
			logger.error("Error loading dataset data from " + label, e);
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		}
	}

	@POST
	@Path("/{label}/preview")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataStorePreview(@PathParam("label") String label, String body) {
		try {
			Monitor timing = MonitorFactory.start("Knowage.DataSetResource.getDataStorePreview:parseInputs");

			String aggregations = null;
			String parameters = null;
			Map<String, Object> driversRuntimeMap = null;
			String likeSelections = null;
			int start = -1;
			int limit = -1;
			Set<String> columns = null;

			if (StringUtilities.isNotEmpty(body)) {
				JSONObject jsonBody = new JSONObject(body);

				if (jsonBody.has("start")) {
					start = jsonBody.getInt("start");
				}

				if (jsonBody.has("limit")) {
					limit = jsonBody.getInt("limit");
				}

				JSONArray jsonFilters = jsonBody.optJSONArray("filters");
				if (jsonFilters != null && jsonFilters.length() > 0) {
					JSONObject jsonLikeSelections = new JSONObject();

					for (int i = 0; i < jsonFilters.length(); i++) {
						JSONObject jsonFilter = jsonFilters.getJSONObject(i);
						jsonLikeSelections.put(jsonFilter.getString("column"), jsonFilter.get("value"));
					}

					likeSelections = new JSONObject().put(label, jsonLikeSelections).toString();
				}

				String sortingColumn = null;
				String sortingType = null;
				if (jsonBody.has("sorting")) {
					JSONObject jsonSorting = jsonBody.optJSONObject("sorting");
					sortingColumn = jsonSorting.getString("column");
					sortingType = jsonSorting.getString("order");
				}

				JSONArray jsonMeasures = new JSONArray();
				JSONArray jsonCategories = new JSONArray();
				IDataSet dataSet = getDatasetManagementAPI().getDataSet(label);

				IMetaData metadata = dataSet.getMetadata();
				for (int i = 0; i < metadata.getFieldCount(); i++) {
					IFieldMetaData fieldMetaData = metadata.getFieldMeta(i);
					JSONObject json = new JSONObject();
					String alias = fieldMetaData.getAlias();
					json.put("id", alias);
					json.put("alias", alias);
					json.put("columnName", alias);
					json.put("orderType", alias.equals(sortingColumn) ? sortingType : "");
					json.put("orderColumn", alias);
					json.put("funct", "NONE");

					if ("ATTRIBUTE".equals(fieldMetaData.getFieldType())) {
						jsonCategories.put(json);
					} else {
						jsonMeasures.put(json);
					}
				}

				JSONObject jsonAggregations = new JSONObject();
				jsonAggregations.put("measures", jsonMeasures);
				jsonAggregations.put("categories", jsonCategories);
				aggregations = jsonAggregations.toString();

				JSONArray jsonPars = jsonBody.optJSONArray("pars");
				JSONObject jsonObject = DataSetUtilities.parametersJSONArray2JSONObject(dataSet, jsonPars);
				parameters = jsonObject.toString();

				JSONObject jsonDrivers = jsonBody.optJSONObject("drivers");
				driversRuntimeMap = DataSetUtilities.getDriversMap(jsonDrivers);
				JSONArray jsonIndexes = jsonBody.optJSONArray("indexes");
				if (jsonIndexes != null && jsonIndexes.length() > 0) {

					for (int k = 0; k < jsonIndexes.length(); k++) {
						JSONArray columnsArrayTemp = jsonIndexes.getJSONObject(k).getJSONArray("fields");

						JSONObject columnsArray = columnsArrayTemp.getJSONObject(0);

						if (columnsArray.getString("store").equals(label)) {
							columns = new HashSet<String>(columnsArray.length());
							columns.add(columnsArray.getString("column"));
						}
					}
				}
			}

			timing.stop();
			return getDataStore(label, parameters, driversRuntimeMap, null, likeSelections, -1, aggregations, null, start, limit, columns, null);
		} catch (JSONException e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} catch (Exception e) {
			logger.error("Error while previewing dataset " + label, e);
			throw new SpagoBIRuntimeException("Error while previewing dataset " + label + ". " + e.getMessage(), e);
		}
	}

	@POST
	@Path("/addDatasetInCache")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public Response addDatasetInCache(@Context HttpServletRequest req) {
		Set<String> columns = new HashSet<String>();

		logger.debug("IN");
		try {
			JSONArray requestBodyJSONArray = RestUtilities.readBodyAsJSONArray(req);
			for (int i = 0; i < requestBodyJSONArray.length(); i++) {
				JSONObject info = requestBodyJSONArray.getJSONObject(i);
				boolean isNearRealtime = info.optBoolean("nearRealtime");
				if (!isNearRealtime) {
					String label = info.getString("datasetLabel");
					String parameters = info.getString("parameters");

					IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(label);
					ICache cache = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());

					DatasetManagementAPI datasetManagementAPI = new DatasetManagementAPI();
					dataSet.setParametersMap(DataSetUtilities.getParametersMap(parameters));
					datasetManagementAPI.putDataSetInCache(dataSet, cache, columns);
				}
			}
			return Response.ok().build();
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	protected IDataWriter getDataStoreWriter() throws JSONException {
		CockpitJSONDataWriter dataWriter = new CockpitJSONDataWriter(getDataSetWriterProperties());
		dataWriter.setLocale(buildLocaleFromSession());
		return dataWriter;
	}

	/**
	 * Dataset-Tags: Subresource
	 */
	@Path("{dsId}/dstags")
	@Produces(MediaType.APPLICATION_JSON)
	public DatasetTagsResource getDatasetTagsResource(@PathParam("dsId") Integer dsId) {
		logger.debug("Getting DatasetTagsResource Instace...");
		return new DatasetTagsResource();
	}

	@POST
	@Path("/validateFormula")
	@Produces(MediaType.APPLICATION_JSON)
	public String validateFormulaJson(String body) {
		try {
			Monitor timing = MonitorFactory.start("Knowage.DataSetResource.getDataStorePostWithJsonInBody:parseInputs");

			String formulaString = "";
			if (StringUtilities.isNotEmpty(body)) {
				JSONObject jsonBody = new JSONObject(body);

				formulaString = jsonBody.getString("formula");
				JSONArray columns = jsonBody.getJSONArray("measuresList");
				List<SimpleSelectionField> l = new ArrayList<SimpleSelectionField>();

				for (int i = 0; i < columns.length(); i++) {
					SimpleSelectionField a = new SimpleSelectionField();
					a.setName(((JSONObject) columns.get(i)).getString("name"));
					l.add(a);
				}

				try {
					validateFormula(formulaString, l);
					JSONObject okResponse = new JSONObject();
					okResponse.put("msg", "ok");
					return okResponse.toString();
				} catch (ValidationException v) {
					throw new ValidationServiceException(buildLocaleFromSession(), v);

				}
			}
			timing.stop();
		} catch (JSONException e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		}
		return null;

	}

	public ArrayList<HashMap<String, Object>> transformRuntimeDrivers(List<BusinessModelDriverRuntime> parameters, IParameterUseDAO parameterUseDAO,
			String role, MetaModel businessModel, BusinessModelOpenParameters BMOP) {
		ArrayList<HashMap<String, Object>> parametersArrayList = new ArrayList<>();
		ParameterUse parameterUse;
		for (BusinessModelDriverRuntime objParameter : parameters) {
			Integer paruseId = objParameter.getParameterUseId();
			try {
				parameterUse = parameterUseDAO.loadByUseID(paruseId);
			} catch (EMFUserError e1) {
				logger.debug(e1.getCause(), e1);
				throw new SpagoBIRuntimeException(e1.getMessage(), e1);
			}

			HashMap<String, Object> parameterAsMap = new HashMap<String, Object>();
			parameterAsMap.put("id", objParameter.getBiObjectId());
			parameterAsMap.put("label", objParameter.getLabel());
			parameterAsMap.put("urlName", objParameter.getId());
			parameterAsMap.put("type", objParameter.getParType());
			parameterAsMap.put("typeCode", objParameter.getTypeCode());
			parameterAsMap.put("selectionType", objParameter.getSelectionType());
			parameterAsMap.put("valueSelection", parameterUse.getValueSelection());
			parameterAsMap.put("selectedLayer", objParameter.getSelectedLayer());
			parameterAsMap.put("selectedLayerProp", objParameter.getSelectedLayerProp());
			parameterAsMap.put("visible", ((objParameter.isVisible())));
			parameterAsMap.put("mandatory", ((objParameter.isMandatory())));
			parameterAsMap.put("multivalue", objParameter.isMultivalue());
			parameterAsMap.put("driverLabel", objParameter.getPar().getLabel());
			parameterAsMap.put("driverUseLabel", objParameter.getAnalyticalDriverExecModality().getLabel());

			parameterAsMap.put("allowInternalNodeSelection",
					objParameter.getPar().getModalityValue().getLovProvider().contains("<LOVTYPE>treeinner</LOVTYPE>"));

			// get values
			if (objParameter.getDriver().getParameterValues() != null) {

				List paramValueLst = new ArrayList();
				List paramDescrLst = new ArrayList();
				Object paramValues = objParameter.getDriver().getParameterValues();
				Object paramDescriptionValues = objParameter.getDriver().getParameterValuesDescription();

				if (paramValues instanceof List) {

					List<String> valuesList = (List) paramValues;
					List<String> descriptionList = (List) paramDescriptionValues;
					if (paramDescriptionValues == null || !(paramDescriptionValues instanceof List)) {
						descriptionList = new ArrayList<String>();
					}

					// String item = null;
					for (int k = 0; k < valuesList.size(); k++) {

						String itemVal = valuesList.get(k);

						String itemDescr = descriptionList.size() > k && descriptionList.get(k) != null ? descriptionList.get(k) : itemVal;

						try {
							// % character breaks decode method
							if (!itemVal.contains("%")) {
								itemVal = URLDecoder.decode(itemVal, "UTF-8");
							}
							if (!itemDescr.contains("%")) {
								itemDescr = URLDecoder.decode(itemDescr, "UTF-8");
							}

							// check input value and convert if it's an old multivalue syntax({;{xxx;yyy}STRING}) to list of values :["A-OMP", "A-PO", "CL"]
							if (objParameter.isMultivalue() && itemVal.indexOf("{") >= 0) {
								String sep = itemVal.substring(1, 2);
								String val = itemVal.substring(3, itemVal.indexOf("}"));
								String[] valLst = val.split(sep);
								for (int k2 = 0; k2 < valLst.length; k2++) {
									String itemVal2 = valLst[k2];
									if (itemVal2 != null && !"".equals(itemVal2))
										paramValueLst.add(itemVal2);
								}
							} else {
								if (itemVal != null && !"".equals(itemVal)) {
									paramValueLst.add(itemVal);
									paramDescrLst.add(itemDescr);
								}
							}
						} catch (UnsupportedEncodingException e) {
							logger.debug("An error occured while decoding parameter with value[" + itemVal + "]" + e);
						}
					}
				} else if (paramValues instanceof String) {
					// % character breaks decode method
					if (!((String) paramValues).contains("%")) {
						try {
							paramValues = URLDecoder.decode((String) paramValues, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							logger.debug(e.getCause(), e);
							throw new SpagoBIRuntimeException(e.getMessage(), e);
						}
					}
					paramValueLst.add(paramValues.toString());

					String parDescrVal = paramDescriptionValues != null && paramDescriptionValues instanceof String ? paramDescriptionValues.toString()
							: paramValues.toString();
					if (!parDescrVal.contains("%")) {
						try {
							parDescrVal = URLDecoder.decode(parDescrVal, "UTF-8");
						} catch (UnsupportedEncodingException e) {
							logger.debug(e.getCause(), e);
							throw new SpagoBIRuntimeException(e.getMessage(), e);
						}
					}
					paramDescrLst.add(parDescrVal);
				}

				parameterAsMap.put("parameterValue", paramValueLst);
				parameterAsMap.put("parameterDescription", paramDescriptionValues);
			}

			boolean showParameterLov = true;

			// Parameters NO TREE
			if ("lov".equalsIgnoreCase(parameterUse.getValueSelection())
					&& !objParameter.getSelectionType().equalsIgnoreCase(DocumentExecutionUtils.SELECTION_TYPE_TREE)) {

				ArrayList<HashMap<String, Object>> admissibleValues = objParameter.getAdmissibleValues();

				if (!objParameter.getSelectionType().equalsIgnoreCase(DocumentExecutionUtils.SELECTION_TYPE_LOOKUP)) {
					parameterAsMap.put("defaultValues", admissibleValues);
				} else {
					parameterAsMap.put("defaultValues", new ArrayList<>());
				}
				parameterAsMap.put("defaultValuesMeta", objParameter.getLovColumnsNames());
				parameterAsMap.put(DocumentExecutionUtils.VALUE_COLUMN_NAME_METADATA, objParameter.getLovValueColumnName());
				parameterAsMap.put(DocumentExecutionUtils.DESCRIPTION_COLUMN_NAME_METADATA, objParameter.getLovDescriptionColumnName());

				// hide the parameter if is mandatory and have one value in lov (no error parameter)
				if (admissibleValues != null && admissibleValues.size() == 1 && objParameter.isMandatory() && !admissibleValues.get(0).containsKey("error")
						&& (objParameter.getDataDependencies() == null || objParameter.getDataDependencies().isEmpty())
						&& (objParameter.getLovDependencies() == null || objParameter.getLovDependencies().isEmpty())) {
					showParameterLov = false;
				}

				// if parameterValue is not null and is array, check if all element are present in lov
				Object values = parameterAsMap.get("parameterValue");
				if (values != null && admissibleValues != null) {
					BMOP.checkIfValuesAreAdmissible(values, admissibleValues);
				}
			}

			// DATE RANGE DEFAULT VALUE
			if (objParameter.getParType().equals("DATE_RANGE")) {
				try {
					ArrayList<HashMap<String, Object>> defaultValues = BMOP.manageDataRange(businessModel, role, objParameter.getId());
					parameterAsMap.put("defaultValues", defaultValues);
				} catch (SerializationException | EMFUserError | JSONException | IOException e) {
					logger.debug("Filters DATE RANGE ERRORS ", e);
				}
			}

			// convert the parameterValue from array of string in array of object
			DefaultValuesList parameterValueList = new DefaultValuesList();
			Object oVals = parameterAsMap.get("parameterValue");
			Object oDescr = parameterAsMap.get("parameterDescription") != null ? parameterAsMap.get("parameterDescription") : new ArrayList<String>();

			if (oVals != null) {
				if (oVals instanceof List) {
					// CROSS NAV : INPUT PARAM PARAMETER TARGET DOC IS STRING
					if (oVals.toString().startsWith("[") && oVals.toString().endsWith("]") && parameterUse.getValueSelection().equals("man_in")) {
						List<String> valList = (ArrayList) oVals;
						String stringResult = "";
						for (int k = 0; k < valList.size(); k++) {
							String itemVal = valList.get(k);
							if (objParameter.getParType().equals("STRING") && objParameter.isMultivalue()) {
								stringResult += "'" + itemVal + "'";
							} else {
								stringResult += itemVal;
							}
							if (k != valList.size() - 1) {
								stringResult += ",";
							}
						}
						LovValue defValue = new LovValue();
						defValue.setValue(stringResult);
						defValue.setDescription(stringResult);
						parameterValueList.add(defValue);
					} else {
						List<String> valList = (ArrayList) oVals;
						List<String> descrList = (ArrayList) oDescr;
						for (int k = 0; k < valList.size(); k++) {
							String itemVal = valList.get(k);
							String itemDescr = descrList.size() > k ? descrList.get(k) : itemVal;
							LovValue defValue = new LovValue();
							defValue.setValue(itemVal);
							defValue.setDescription(itemDescr != null ? itemDescr : itemVal);
							parameterValueList.add(defValue);
						}
					}
					parameterAsMap.put("parameterValue", parameterValueList);
				}
			}

			parameterAsMap.put("dependsOn", objParameter.getDependencies());
			parameterAsMap.put("dataDependencies", objParameter.getDataDependencies());
			parameterAsMap.put("visualDependencies", objParameter.getVisualDependencies());
			parameterAsMap.put("lovDependencies", (objParameter.getLovDependencies() != null) ? objParameter.getLovDependencies() : new ArrayList<>());

			// load DEFAULT VALUE if present and if the parameter value is empty
			if (objParameter.getDefaultValues() != null && objParameter.getDefaultValues().size() > 0
					&& objParameter.getDefaultValues().get(0).getValue() != null) {
				DefaultValuesList valueList = null;
				// check if the parameter is really valorized (for example if it isn't an empty list)
				List lstValues = (List) parameterAsMap.get("parameterValue");
				// if (lstValues.size() == 0)
				// jsonCrossParameters.remove(objParameter.getId());

				String parLab = objParameter.getDriver() != null && objParameter.getDriver().getParameter() != null
						? objParameter.getDriver().getParameter().getLabel()
						: "";
				String useModLab = objParameter.getAnalyticalDriverExecModality() != null ? objParameter.getAnalyticalDriverExecModality().getLabel() : "";
				String sessionKey = parLab + "_" + useModLab;

				valueList = objParameter.getDefaultValues();

				// in every case fill default values!
				parameterAsMap.put("driverDefaultValue", valueList);
			}

			if (!showParameterLov) {
				parameterAsMap.put("showOnPanel", "false");
			} else {
				parameterAsMap.put("showOnPanel", "true");
			}
			parametersArrayList.add(parameterAsMap);

		}
		for (int z = 0; z < parametersArrayList.size(); z++) {

			Map docP = parametersArrayList.get(z);
			DefaultValuesList defvalList = (DefaultValuesList) docP.get("parameterValue");
			if (defvalList != null && defvalList.size() == 1) {
				LovValue defval = defvalList.get(0);
				if (defval != null) {
					Object val = defval.getValue();
					if (val != null && val.equals("$")) {
						docP.put("parameterValue", "");
					} else {
						docP.put("parameterValue", val);
					}
				}

			}
		}
		return parametersArrayList;
	}

	public ArrayList<HashMap<String, Object>> getDatasetDriversByModelName(String businessModelName, Boolean loadDSwithDrivers) {
		ArrayList<HashMap<String, Object>> parametersArrList = new ArrayList<>();
		IMetaModelsDAO dao = DAOFactory.getMetaModelsDAO();
		IParameterUseDAO parameterUseDAO = DAOFactory.getParameterUseDAO();
		List<BusinessModelDriverRuntime> parameters = new ArrayList<>();
		BusinessModelOpenParameters BMOP = new BusinessModelOpenParameters();
		String role;
		try {
			role = getUserProfile().getRoles().contains("admin") ? "admin" : (String) getUserProfile().getRoles().iterator().next();
		} catch (EMFInternalError e2) {
			logger.debug(e2.getCause(), e2);
			throw new SpagoBIRuntimeException(e2.getMessage(), e2);
		}
		MetaModel businessModel = dao.loadMetaModelForExecutionByNameAndRole(businessModelName, role, loadDSwithDrivers);
		if (businessModel == null) {
			return null;
		}
		BusinessModelRuntime dum = new BusinessModelRuntime(this.getUserProfile(), null);
		parameters = BusinessModelOpenUtils.getParameters(businessModel, role, request.getLocale(), null, true, dum);
		parametersArrList = transformRuntimeDrivers(parameters, parameterUseDAO, role, businessModel, BMOP);

		return parametersArrList;
	}

}