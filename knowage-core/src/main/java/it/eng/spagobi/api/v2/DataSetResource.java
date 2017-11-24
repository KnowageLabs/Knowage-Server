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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.util.JSON;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.common.AbstractDataSetResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.BetweenFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Filter;
import it.eng.spagobi.tools.dataset.cache.query.item.InFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.LikeFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.NullaryFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.PlaceholderFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Projection;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilterOperator;
import it.eng.spagobi.tools.dataset.cache.query.item.UnaryFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.UnsatisfiedFilter;
import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.tools.dataset.common.datawriter.IDataWriter;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.dao.ISbiDataSetDAO;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSet;
import it.eng.spagobi.tools.dataset.metadata.SbiDataSetId;
import it.eng.spagobi.tools.dataset.persist.IPersistedManager;
import it.eng.spagobi.tools.dataset.persist.PersistedHDFSManager;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 *
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
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataSets(@QueryParam("includeDerived") String includeDerived, @QueryParam("callback") String callback,
			@QueryParam("asPagedList") Boolean paged, @QueryParam("Page") String pageStr, @QueryParam("ItemPerPage") String itemPerPageStr,
			@QueryParam("label") String search, @QueryParam("seeTechnical") Boolean seeTechnical, @QueryParam("ids") String ids) {
		logger.debug("IN");

		if ("no".equalsIgnoreCase(includeDerived)) {
			return getNotDerivedDataSets(callback);
		}

		if (Boolean.TRUE.equals(paged)) {
			return getDatasetsAsPagedList(pageStr, itemPerPageStr, search, seeTechnical, ids);
		}

		ISbiDataSetDAO dsDAO;
		try {
			dsDAO = DAOFactory.getSbiDataSetDAO();
		} catch (EMFUserError e) {
			logger.error("Error while looking for datasets", e);
			throw new SpagoBIRuntimeException("Error while looking for datasets", e);
		}

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
		sbiDataset.setOwner((String) getUserProfile().getUserUniqueIdentifier());
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

	public String getDatasetsAsPagedList(String pageStr, String itemPerPageStr, String search, Boolean seeTechnical, String ids) {

		try {
			ISbiDataSetDAO dao = DAOFactory.getSbiDataSetDAO();
			IEngUserProfile profile = this.getUserProfile();
			// TODO check if profile is null
			dao.setUserProfile(profile);

			Integer page = getNumberOrNull(pageStr);
			Integer item_per_page = getNumberOrNull(itemPerPageStr);
			search = search != null ? search : "";

			Integer[] idArray = getIdsAsIntegers(ids);

			List<SbiDataSet> dataset = null;
			if (UserUtilities.isAdministrator(getUserProfile())) {
				dataset = dao.loadPaginatedSearchSbiDataSet(search, page, item_per_page, null, null, idArray);
			} else {
				dataset = dao.loadPaginatedSearchSbiDataSet(search, page, item_per_page, getUserProfile(), seeTechnical, idArray);
			}

			JSONObject jo = new JSONObject();
			JSONArray ja = new JSONArray();
			for (SbiDataSet ds : dataset) {
				ja.put(JSON.parse(JsonConverter.objectToJson(ds, SbiDataSet.class)));
			}
			jo.put("item", ja);
			jo.put("itemCount", dao.countSbiDataSet(search, idArray));

			return jo.toString();
		} catch (Exception e) {
			logger.error("Error while getting the list of documents", e);
			throw new SpagoBIRuntimeException("Error while getting the list of documents", e);
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
						if (filterJsonObject != null) {
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
		final Comparator<Object> comparator = (o1, o2) -> ((String) o1).compareTo((String) o2);

		if (SimpleFilterOperator.EQUALS_TO_MIN.equals(anotherFilter.getOperator())) {
			Object operand = inFilter.getOperands().stream().min(comparator).get();
			return new UnaryFilter(inFilter.getProjections().get(0), SimpleFilterOperator.EQUALS_TO, operand);
		} else if (SimpleFilterOperator.EQUALS_TO_MAX.equals(anotherFilter.getOperator())) {
			Object operand = inFilter.getOperands().stream().max(comparator).get();
			return new UnaryFilter(inFilter.getProjections().get(0), SimpleFilterOperator.EQUALS_TO, operand);
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
						String[] valuesArray = StringUtilities.getSubstringsBetween(valuesJsonArray.getString(i), "'");
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
						filter = new LikeFilter(projections.get(0), valueObjects.get(0).toString());
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

	@POST
	@Path("/{label}/data")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getDataStorePost(@PathParam("label") String label, @QueryParam("parameters") String parameters, String selections,
			@QueryParam("likeSelections") String likeSelections, @DefaultValue("-1") @QueryParam("limit") int maxRowCount,
			@QueryParam("aggregations") String aggregations, @QueryParam("summaryRow") String summaryRow, @DefaultValue("-1") @QueryParam("offset") int offset,
			@DefaultValue("-1") @QueryParam("size") int fetchSize, @QueryParam("nearRealtime") boolean isNearRealtime) {
		logger.debug("IN");
		try {
			return getDataStore(label, parameters, selections, likeSelections, maxRowCount, aggregations, summaryRow, offset, fetchSize, isNearRealtime);
		} catch (Exception e) {
			throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
		} finally {
			logger.debug("OUT");
		}
	}

	@POST
	@Path("/addDatasetInCache")
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
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
					ICache cache = SpagoBICacheManager.getCache();

					DatasetManagementAPI datasetManagementAPI = new DatasetManagementAPI();
					datasetManagementAPI.setDataSetParameters(dataSet, DataSetUtilities.getParametersMap(parameters));
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
	protected IDataWriter getDataSetWriter() throws JSONException {
		CockpitJSONDataWriter dataWriter = new CockpitJSONDataWriter(getDataSetWriterProperties());
		dataWriter.setLocale(buildLocaleFromSession());
		return dataWriter;
	}

}