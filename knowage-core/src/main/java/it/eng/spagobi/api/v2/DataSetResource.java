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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.api.common.AbstractDataSetResource;
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
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.FlatDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.SolrDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.tools.dataset.common.datawriter.IDataWriter;
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
import it.eng.spagobi.tools.dataset.metasql.query.item.NullaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.PlaceholderFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilterOperator;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnsatisfiedFilter;
import it.eng.spagobi.tools.dataset.persist.IPersistedManager;
import it.eng.spagobi.tools.dataset.persist.PersistedHDFSManager;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.IDataBase;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
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
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = {SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT})
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

	@Override
	@GET
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = {SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT})
	public String getDataSet(@PathParam("label") String label) {
		return super.getDataSet(label);
	}

	@Override
	@GET
	@Path("/{label}/content")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = {SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT})
	public Response execute(@PathParam("label") String label, String body) {
		return super.execute(label, body);
	}

	@POST
	@UserConstraint(functionalities = {SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT})
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
	@UserConstraint(functionalities = {SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT})
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
	@UserConstraint(functionalities = {SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT})
	public Response deleteDataset(@PathParam("label") String label) {
		return super.deleteDataset(label);
	}

	@GET
	@Path("/download/file")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response downloadDataSetFile(@QueryParam("fileName") String fileName, @QueryParam("type") String type) {
		File file = null;
		ResponseBuilder response = null;
		try {
			String resourcePath = SpagoBIUtilities.getResourcePath();
			String filePath = resourcePath + File.separatorChar + "dataset" + File.separatorChar + "files" + File.separatorChar + fileName;
			file = new File(filePath);

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
				JSONObject jsonIDataSet = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(dataSet, null);

				JSONObject jsonSbiDataSet = new JSONObject(JsonConverter.objectToJson(ds, SbiDataSet.class));
				boolean isNearRealtimeSupported = false;
				jsonSbiDataSet.put("isRealtime", dataSet.isRealtime());
				jsonSbiDataSet.put("isCachingSupported", dataSet.isCachingSupported());
				jsonSbiDataSet.put("parameters", jsonIDataSet.getJSONArray("pars"));
				dataSet = dataSet instanceof VersionedDataSet ? ((VersionedDataSet) dataSet).getWrappedDataset() : dataSet;
				if (dataSet instanceof AbstractJDBCDataset) {
					IDataBase database = DataBaseFactory.getDataBase(dataSet.getDataSource());
					isNearRealtimeSupported = database.getDatabaseDialect().isInLineViewSupported() && !dataSet.hasDataStoreTransformer();
				} else if (dataSet instanceof FlatDataSet || dataSet.isPersisted() || dataSet.getClass().equals(SolrDataSet.class)) {
					isNearRealtimeSupported = true;
				}
				jsonSbiDataSet.put("isNearRealtimeSupported", isNearRealtimeSupported);

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

	@Override
	protected List<Filter> getFilters(String datasetLabel, JSONObject selectionsObject, Map<String, String> columnAliasToColumnName) throws JSONException {
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

	private SimpleFilter getFilter(String operatorString, JSONArray valuesJsonArray, String columns, IDataSet dataSet,
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
	@UserConstraint(functionalities = {SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT})
	public String getDataStorePostWithJsonInBody(@PathParam("label") String label, String body, @DefaultValue("-1") @QueryParam("limit") int maxRowCount,
												 @DefaultValue("-1") @QueryParam("offset") int offset, @DefaultValue("-1") @QueryParam("size") int fetchSize,
												 @QueryParam("nearRealtime") boolean isNearRealtime) {
		try {
			Monitor timing = MonitorFactory.start("Knowage.DataSetResource.getDataStorePostWithJsonInBody:parseInputs");

			String parameters = null;
			String selections = null;
			String likeSelections = null;
			String aggregations = null;
			String summaryRow = null;

			if (StringUtilities.isNotEmpty(body)) {
				JSONObject jsonBody = new JSONObject(body);

				JSONObject jsonParameters = jsonBody.optJSONObject("parameters");
				parameters = jsonParameters != null ? jsonParameters.toString() : null;

				JSONObject jsonSelections = jsonBody.optJSONObject("selections");
				selections = jsonSelections != null ? jsonSelections.toString() : null;

				JSONObject jsonLikeSelections = jsonBody.optJSONObject("likeSelections");
				likeSelections = jsonLikeSelections != null ? jsonLikeSelections.toString() : null;

				JSONObject jsonAggregations = jsonBody.optJSONObject("aggregations");
				aggregations = jsonAggregations != null ? jsonAggregations.toString() : null;

				JSONObject jsonSummaryRow = jsonBody.optJSONObject("summaryRow");
				summaryRow = jsonSummaryRow != null ? jsonSummaryRow.toString() : null;
			}

			timing.stop();
			return getDataStore(label, parameters, selections, likeSelections, maxRowCount, aggregations, summaryRow, offset, fetchSize, isNearRealtime);
		} catch (JSONException e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		}
	}

	@POST
	@Path("/{label}/preview")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = {SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT})
	public String getDataStorePreview(@PathParam("label") String label, String body) {
		try {
			Monitor timing = MonitorFactory.start("Knowage.DataSetResource.getDataStorePreview:parseInputs");

			String aggregations = null;
			String parameters = null;
			String likeSelections = null;
			int start = -1;
			int limit = -1;

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

				JSONObject jsonDataSets = new JSONObject(getDataSets(null, null, true, null, null, label, true, null, false));
				JSONObject jsonDataSet = jsonDataSets.getJSONArray("item").getJSONObject(0);
				JSONArray jsonFields = jsonDataSet.getJSONObject("metadata").getJSONArray("fieldsMeta");
				for (int i = 0; i < jsonFields.length(); i++) {
					JSONObject jsonField = jsonFields.getJSONObject(i);
					JSONObject json = new JSONObject();
					String alias = jsonField.getString("alias");
					json.put("id", alias);
					json.put("alias", alias);
					json.put("columnName", alias);
					json.put("orderType", alias.equals(sortingColumn) ? sortingType : "");
					json.put("orderColumn", alias);
					json.put("funct", "NONE");

					if ("ATTRIBUTE".equals(jsonField.getString("fieldType"))) {
						jsonCategories.put(json);
					} else {
						jsonMeasures.put(json);
					}
				}

				JSONObject jsonAggregations = new JSONObject();
				jsonAggregations.put("measures", jsonMeasures);
				jsonAggregations.put("categories", jsonCategories);
				aggregations = jsonAggregations.toString();

				JSONArray jsonParameters = jsonDataSet.getJSONArray("parameters");
				JSONArray jsonPars = jsonBody.optJSONArray("pars");
				if (jsonParameters != null) {
					JSONObject json = new JSONObject();
					for (int i = 0; i < jsonParameters.length(); i++) {
						JSONObject jsonParameter = jsonParameters.getJSONObject(i);
						String columnName = jsonParameter.getString("name");
						json.put(columnName, jsonParameter.get("defaultValue"));
						if(jsonPars != null) {
							for (int j = 0; j < jsonPars.length(); j++) {
								JSONObject jsonPar = jsonPars.getJSONObject(j);
								if (columnName.equals(jsonPar.getString("name"))) {
									json.put(columnName, jsonPar.get("value"));
									break;
								}
							}
						}
					}
					parameters = json.toString();
				}
			}

			timing.stop();
			return getDataStore(label, parameters, null, likeSelections, -1, aggregations, null, start, limit, true);
		} catch (JSONException e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		}
	}

	@POST
	@Path("/addDatasetInCache")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = {SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT})
	public Response addDatasetInCache(@Context HttpServletRequest req) {
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
					datasetManagementAPI.putDataSetInCache(dataSet, cache);
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

}