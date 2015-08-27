/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.api;

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.deserializer.DeserializerFactory;
import it.eng.spagobi.commons.monitor.Monitor;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.FilterCriteria;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.GroupCriteria;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.Operand;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.ProjectionCriteria;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroupJSONSerializer;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.crosstab.CrossTab;
import it.eng.spagobi.tools.dataset.crosstab.CrosstabDefinition;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.dataset.utils.datamart.SpagoBICoreDatamartRetriever;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceParameterException;
import it.eng.spagobi.utilities.json.JSONUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
@Path("/1.0/datasets")
public class DataSetResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DataSetResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDataSets(@QueryParam("typeDoc") String typeDoc, @QueryParam("callback") String callback) {
		logger.debug("IN");

		try {
			List<IDataSet> dataSets = getDatasetManagementAPI().getDataSets();
			List<IDataSet> toBeReturned = new ArrayList<IDataSet>();

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
	@Path("/list/persist")
	public void persistDataSets(@QueryParam("labels") JSONArray labels) {
		logger.debug("IN");

		for (int i = 0; i < labels.length(); i++) {
			String label = null;
			try {
				label = labels.getString(i);
				DatasetManagementAPI dataSetManagementAPI = getDatasetManagementAPI();
				dataSetManagementAPI.persistDataset(label);
			} catch (JSONException e) {
				logger.error("error in persisting dataset with label: " + label, e);
				throw new RuntimeException("error in persisting dataset with label " + label);
			}
		}

		logger.debug("OUT");
	}

	@POST
	@Path("/list/persist")
	public void persistDataSetsPost(@FormParam("labels") JSONArray labels) {
		logger.debug("IN");

		for (int i = 0; i < labels.length(); i++) {
			String label = null;
			try {
				label = labels.getString(i);
				DatasetManagementAPI dataSetManagementAPI = getDatasetManagementAPI();
				dataSetManagementAPI.setUserProfile(getUserProfile());
				dataSetManagementAPI.persistDataset(label);
			} catch (JSONException e) {
				logger.error("error in persisting dataset with label: " + label, e);
				throw new RuntimeException("error in persisting dataset with label: " + label);
			}
		}

		logger.debug("OUT");

	}

	@GET
	@Path("/{label}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getDataSet(@PathParam("label") String label) {
		logger.debug("IN");
		try {
			IDataSet dataSet = getDatasetManagementAPI().getDataSet(label);
			return serializeDataSet(dataSet, null);
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/{label}/content")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response execute(@PathParam("label") String label, @Context UriInfo info) {
		MultivaluedMap<String, String> queryParameters = info.getQueryParameters();
		HashMap<String, String> parameters = new HashMap<String, String>();
		String callback = null;

		if (queryParameters != null) {
			for (String key : queryParameters.keySet()) {
				if (!key.equals("callback"))
					parameters.put(key, queryParameters.getFirst(key));
				else
					callback = queryParameters.getFirst(key);
			}
		}

		if (callback == null || callback.isEmpty())
			return Response.ok(executeDataSet(label, parameters)).build();
		else {
			String jsonString = executeDataSet(label, parameters);

			return Response.ok(callback + "(" + jsonString + ")").build();
		}
	}

	private String executeDataSet(String label, Map parameters) {
		logger.debug("IN: label in input = " + label);

		try {
			if (label == null) {
				logger.warn("Label is null");
				throw new SpagoBIRuntimeException("Label is null!");
			}

			IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(label);
			if (dataSet == null) {
				logger.warn("DataSet with label [" + label + "] doesn't exist");
				throw new SpagoBIRuntimeException("DataSet with label [" + label + "] doesn't exist");
			}
			dataSet.setParamsMap(parameters);

			// add the jar retriver in case of a Qbe DataSet
			if (dataSet instanceof QbeDataSet
					|| (dataSet instanceof VersionedDataSet && ((VersionedDataSet) dataSet).getWrappedDataset() instanceof QbeDataSet)) {
				SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();

				if (parameters == null) {
					parameters = new HashMap();
					dataSet.setParamsMap(parameters);
				}
				dataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
			}

			dataSet.loadData();

			JSONDataWriter writer = new JSONDataWriter();
			return (writer.write(dataSet.getDataStore())).toString();
		} catch (SpagoBIRuntimeException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error while executing dataset", e);
			throw new SpagoBIRuntimeException("Error while executing dataset", e);
		}
	}

	@DELETE
	@Path("/{label}")
	public Response deleteDataset(@PathParam("label") String label) {
		IDataSetDAO datasetDao = null;
		try {
			datasetDao = DAOFactory.getDataSetDAO();
		} catch (EMFUserError e) {
			logger.error("Internal error", e);
			throw new SpagoBIRuntimeException("Internal error", e);
		}

		IDataSet dataset = getDatasetManagementAPI().getDataSet(label);

		try {
			datasetDao.deleteDataSet(dataset.getId());
		} catch (Exception e) {
			logger.error("Error while deleting the specified dataset", e);
			throw new SpagoBIRuntimeException("Error while deleting the specified dataset", e);
		}

		return Response.ok().build();
	}

	@GET
	@Path("/{label}/fields")
	@Produces(MediaType.APPLICATION_JSON)
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
			CrosstabDefinition crosstabDefinition = (CrosstabDefinition) DeserializerFactory.getDeserializer("application/json").deserialize(
					crosstabDefinitionJSON, CrosstabDefinition.class);

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
	@Path("/{label}/data")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDataStore(@PathParam("label") String label, @QueryParam("parameters") String parameters, @QueryParam("selections") String selections // the
			, @QueryParam("aggregations") String aggregations // the aggregation to apply to the joined dataset
	) {

		logger.debug("IN");

		try {
			List<ProjectionCriteria> projectionCriteria = new ArrayList<ProjectionCriteria>();
			List<GroupCriteria> groupCriteria = new ArrayList<GroupCriteria>();
			if (aggregations != null) {
				JSONObject aggregationsObject = new JSONObject(aggregations);
				JSONArray categoriesObject = aggregationsObject.getJSONArray("categories");
				JSONArray measuresObject = aggregationsObject.getJSONArray("measures");

				projectionCriteria = getProjectionCriteria(label, categoriesObject, measuresObject);
				groupCriteria = getGroupCriteria(label, categoriesObject);
			}

			List<FilterCriteria> filterCriteria = new ArrayList<FilterCriteria>();
			if (selections != null) {
				JSONObject selectionsObject = new JSONObject(selections);
				filterCriteria = getFilterCriteria(label, selectionsObject);
			}

			UserProfile profile = getUserProfile();

			HttpSession session = this.getServletRequest().getSession();
			IDataStore dataStore = null;
			synchronized (session) {
				if (groupCriteria.size() == 0 && projectionCriteria.size() == 0 && filterCriteria.size() == 0) {
					dataStore = getDatasetManagementAPI().getDataStore(label, -1, -1, -1, getParametersMap(parameters));
				} else {
					dataStore = getDatasetManagementAPI().getDataStore(label, -1, -1, -1, getParametersMap(parameters), groupCriteria, filterCriteria,
							projectionCriteria);
				}
			}

			Map<String, Object> properties = new HashMap<String, Object>();
			JSONArray fieldOptions = new JSONArray("[{id: 1, options: {measureScaleFactor: 0.5}}]");
			properties.put(JSONDataWriter.PROPERTY_FIELD_OPTION, fieldOptions);
			JSONDataWriter dataSetWriter = new JSONDataWriter(properties);
			dataSetWriter.setLocale(buildLocaleFromSession());
			JSONObject gridDataFeed = (JSONObject) dataSetWriter.write(dataStore);

			String stringFeed = gridDataFeed.toString();
			return stringFeed;
		} catch (ParametersNotValorizedException p) {
			throw new ParametersNotValorizedException(p.getMessage());
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/joined/data")
	@Produces(MediaType.APPLICATION_JSON)
	public String getJoinedDataStore(@QueryParam("associationGroup") String associationGroup // the joined dataset
			, @QueryParam("parameters") String parameters // parameters of the joined dataset
			, @QueryParam("selections") String selections // the filter to apply to the joined dataset
			, @QueryParam("aggregations") String aggregations // the aggregation to apply to the joined dataset
			, @QueryParam("datasets") String datasets // the list of joined dataset to return
	) {

		logger.debug("IN");
		try {
			// unmarshall query param [associationGroup]
			if (associationGroup == null) {
				throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [associationGroup] cannot be null");
			}
			AssociationGroup associationGroupObject = null;
			try {
				AssociationGroupJSONSerializer serializer = new AssociationGroupJSONSerializer();
				associationGroupObject = serializer.deserialize(new JSONObject(associationGroup));
			} catch (Throwable t) {
				throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [associationGroup] value [" + associationGroup
						+ "] is not a valid JSON object", t);
			}

			// TODO process association and apply it to the joined dataset
			List<ProjectionCriteria> projectionCriteria = new ArrayList<ProjectionCriteria>();
			List<GroupCriteria> groupCriteria = new ArrayList<GroupCriteria>();
			if (aggregations != null) {
				JSONObject aggregationsObject = new JSONObject(aggregations);
				String dataset = aggregationsObject.getString("dataset");
				JSONArray categoriesObject = aggregationsObject.getJSONArray("categories");
				JSONArray measuresObject = aggregationsObject.getJSONArray("measures");

				projectionCriteria = getProjectionCriteria(dataset, categoriesObject, measuresObject);
				groupCriteria = getGroupCriteria(dataset, categoriesObject);
			}

			// unmarshal query param [selections]
			JSONObject selectionsJSON = new JSONObject(selections);

			Set<String> datasetsObject = null;
			if (datasets != null) {
				datasetsObject = new HashSet<String>();
				JSONArray datasetsJSON = new JSONArray(datasets);
				for (int i = 0; i < datasetsJSON.length(); i++) {
					datasetsObject.add(datasetsJSON.getString(i));
				}
			}

			IDataStore dataStore = null;
			if (groupCriteria.size() == 0 && projectionCriteria.size() == 0) {
				dataStore = getDatasetManagementAPI().getJoinedDataStore(associationGroupObject, selectionsJSON, getParametersMaps(parameters));
			} else {
				dataStore = getDatasetManagementAPI().getJoinedDataStore(associationGroupObject, selectionsJSON, getParametersMaps(parameters), groupCriteria,
						null, projectionCriteria);
			}

			// serializing response
			Monitor monitor = Monitor.start("serializeStore");
			Map<String, Object> properties = new HashMap<String, Object>();
			JSONArray fieldOptions = new JSONArray("[{id: 1, options: {measureScaleFactor: 0.5}}]");
			properties.put(JSONDataWriter.PROPERTY_FIELD_OPTION, fieldOptions);
			properties.put(JSONDataWriter.PROPERTY_WRITE_DATA_ONLY, true);
			JSONDataWriter dataSetWriter = new JSONDataWriter(properties);
			dataSetWriter.setLocale(buildLocaleFromSession());
			JSONArray gridDataFeed = (JSONArray) dataSetWriter.write(dataStore);
			logger.info("Dataset serialized in: " + monitor.elapsedAsString());

			JSONObject results = null;

			if (groupCriteria.size() == 0 && projectionCriteria.size() == 0) {
				List<Integer> breakIndexes = (List<Integer>) dataStore.getMetaData().getProperty("BREAK_INDEXES");
				List<String> datasetLabels = new ArrayList(associationGroupObject.getDataSetLabels());
				JSONObject resultsAll = splitGridDataFeed(gridDataFeed, datasetLabels, breakIndexes);

				if (datasetsObject != null) {
					results = new JSONObject();
					JSONArray names = resultsAll.names();
					for (int i = 0; i < names.length(); i++) {
						String name = names.getString(i);
						if (datasetsObject.contains(name)) {
							results.put(name, resultsAll.getJSONArray(name));
						}
					}
				} else {
					results = resultsAll;
				}
			} else {
				results = new JSONObject();
				List<String> dsList = new ArrayList(datasetsObject);
				results.put(dsList.get(0), gridDataFeed);
			}

			return results.toString();
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private List<ProjectionCriteria> getProjectionCriteria(String dataset, JSONArray categoriesObject, JSONArray measuresObject) throws JSONException {
		List<ProjectionCriteria> projectionCriterias = new ArrayList<ProjectionCriteria>();
		for (int i = 0; i < categoriesObject.length(); i++) {
			JSONObject categoryObject = categoriesObject.getJSONObject(i);

			String columnName;

			// in the Cockpit Engine, table, you can insert many times the same measure.
			// To manage this, it's not possibile to use the alias as column name.
			// So in the measure object there is also a "columnName" field

			if (!categoryObject.isNull("columnName")) {
				columnName = categoryObject.getString("columnName");
			} else {
				columnName = categoryObject.getString("alias");
			}

			String aliasName = categoryObject.getString("alias");

			ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, aliasName);
			projectionCriterias.add(aProjectionCriteria);
		}
		for (int i = 0; i < measuresObject.length(); i++) {
			JSONObject measureObject = measuresObject.getJSONObject(i);

			String columnName;

			// in the Cockpit Engine, table, you can insert many times the same measure.
			// To manage this, it's not possibile to use the alias as column name.
			// So in the measure object there is also a "columnName" field

			if (!measureObject.isNull("columnName")) {
				columnName = measureObject.getString("columnName");
			} else {
				columnName = measureObject.getString("alias");
			}

			String aliasName = measureObject.getString("alias");

			IAggregationFunction function = AggregationFunctions.get(measureObject.getString("funct"));
			if (function != AggregationFunctions.NONE_FUNCTION) {
				// ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, function.getName(), columnName);
				ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, function.getName(), aliasName);
				projectionCriterias.add(aProjectionCriteria);
			} else {
				// ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, columnName);
				ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, aliasName);
				projectionCriterias.add(aProjectionCriteria);
			}
		}
		return projectionCriterias;
	}

	private List<GroupCriteria> getGroupCriteria(String dataset, JSONArray categoriesObject) throws JSONException {
		List<GroupCriteria> groupCriterias = new ArrayList<GroupCriteria>();

		for (int i = 0; i < categoriesObject.length(); i++) {
			JSONObject categoryObject = categoriesObject.getJSONObject(i);

			String columnName;

			// in the Cockpit Engine, table, you can insert many times the same measure.
			// To manage this, it's not possibile to use the alias as column name.
			// So in the measure object there is also a "columnName" field

			if (!categoryObject.isNull("columnName")) {
				columnName = categoryObject.getString("columnName");
			} else {
				columnName = categoryObject.getString("alias");
			}

			GroupCriteria groupCriteria = new GroupCriteria(dataset, columnName, null);
			groupCriterias.add(groupCriteria);
		}

		return groupCriterias;
	}

	private List<FilterCriteria> getFilterCriteria(String dataset, JSONObject selectionsObject) throws JSONException {
		List<FilterCriteria> filterCriterias = new ArrayList<FilterCriteria>();

		JSONObject datasetSelectionObject = selectionsObject.getJSONObject(dataset);
		Iterator<String> it = datasetSelectionObject.keys();
		while (it.hasNext()) {
			String datasetColumn = it.next();

			JSONArray values = datasetSelectionObject.getJSONArray(datasetColumn);
			if (values.length() == 0)
				continue;
			List<String> valuesList = new ArrayList<String>();
			for (int i = 0; i < values.length(); i++) {
				valuesList.add(values.getString(i));
			}

			Operand leftOperand = new Operand(dataset, datasetColumn);
			Operand rightOperand = new Operand(valuesList);
			FilterCriteria filterCriteria = new FilterCriteria(leftOperand, "=", rightOperand);
			filterCriterias.add(filterCriteria);
		}

		return filterCriterias;
	}

	private JSONObject splitGridDataFeed(JSONArray gridDataFeed, List<String> datasetLabels, List<Integer> breakIndexes) {

		JSONObject results = null;

		logger.debug("IN");
		Monitor monitor = Monitor.start("splitGridDataFeed");

		try {
			breakIndexes.add(Integer.MAX_VALUE);
			int datasetNo = datasetLabels.size();

			results = new JSONObject();
			JSONArray[] datasetRecords = new JSONArray[datasetNo];
			for (int j = 0; j < datasetRecords.length; j++) {
				datasetRecords[j] = new JSONArray();
			}

			// String[] lastRowNo = new String[datasetNo];
			Map<Integer, List<String>> lastRowNo = new HashMap<Integer, List<String>>();
			for (int j = 0; j < datasetRecords.length; j++) {
				lastRowNo.put(j, new ArrayList<String>());
			}
			for (int i = 0; i < gridDataFeed.length(); i++) {
				JSONObject originalRecord = gridDataFeed.getJSONObject(i);
				JSONObject[] datasetRecord = splitRecord(originalRecord, datasetLabels, breakIndexes);
				for (int j = 0; j < datasetRecords.length; j++) {
					String currentRowNoName = "column_0";
					String currentRowNo = datasetRecord[j].getString(currentRowNoName);
					// if (currentRowNo == null || !currentRowNo.equals(lastRowNo[j])) {
					if (currentRowNo == null || !lastRowNo.get(j).contains(currentRowNo)) {
						datasetRecords[j].put(datasetRecord[j]);
						// lastRowNo[j] = currentRowNo;
						lastRowNo.get(j).add(currentRowNo);
					}
				}
			}

			// TODO refactor this because it is very fragile. There is infact no garanty that datasetLabels order is the same splited
			// datasets (i.e. braeking points)
			for (int j = 0; j < datasetRecords.length; j++) {
				results.put(datasetLabels.get(j), datasetRecords[j]);
			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while splittind dataset", t);
		} finally {
			logger.info("Dataset splitted in: " + monitor.elapsedAsString());
			logger.debug("OUT");
		}

		return results;
	}

	public JSONObject[] splitRecord(JSONObject originalRecord, List<String> datasetLabels, List<Integer> breakIndexes) {

		logger.debug("IN");

		try {
			JSONArray props = originalRecord.names();

			int datasetNo = datasetLabels.size();
			JSONObject[] datasetRecord = new JSONObject[datasetNo];
			for (int j = 0; j < datasetRecord.length; j++) {
				datasetRecord[j] = new JSONObject();
				datasetRecord[j].put("id", originalRecord.getString("id"));
			}

			int breakIndexNo = 0;
			int breakIndex = breakIndexes.get(breakIndexNo);
			JSONObject record = datasetRecord[0];
			for (int j = 1, colNo = 0; j < props.length(); j++, colNo++) { // prima colonna cache_id del dataset di join, seconda colonna
				// cache_id primo dataset
				String p = props.getString(j);

				if (j == breakIndex + 1) { // breakIndex is the last element of the previous dataset. breakIndex + 1 is the first one of the new dataset

					breakIndexNo++;
					breakIndex = breakIndexes.get(breakIndexNo);
					record = datasetRecord[breakIndexNo];
					colNo = 0;
					record.put("column_" + colNo, originalRecord.getString(p));

				} else {

					record.put("column_" + colNo, originalRecord.getString(p));

				}

			}

			return datasetRecord;
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while splitting record", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private static Map<String, Map<String, String>> getParametersMaps(String parameters) {
		Map<String, Map<String, String>> toReturn = new HashMap<String, Map<String, String>>();

		if (parameters == null) {
			return toReturn;
		}

		try {
			parameters = JSONUtils.escapeJsonString(parameters);
			JSONObject parametersJSON = ObjectUtils.toJSONObject(parameters);
			Iterator<String> datasetLabels = parametersJSON.keys();
			while (datasetLabels.hasNext()) {
				String datasetLabel = datasetLabels.next();
				JSONObject datasetFilters = parametersJSON.getJSONObject(datasetLabel);
				Map<String, String> filtersMap = getParametersMap(datasetFilters);
				toReturn.put(datasetLabel, filtersMap);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading spagobi filters [" + parameters + "]", t);
		}

		return toReturn;
	}

	private static Map<String, String> getParametersMap(String filters) {
		Map<String, String> toReturn = null;

		if (filters != null) {
			filters = JSONUtils.escapeJsonString(filters);
			JSONObject jsonFilters = ObjectUtils.toJSONObject(filters);
			toReturn = getParametersMap(jsonFilters);
		} else {
			toReturn = new HashMap<String, String>();
		}
		return toReturn;
	}

	private static Map<String, String> getParametersMap(JSONObject jsonFilters) {
		Map<String, String> toReturn = new HashMap<String, String>();

		Iterator<String> keys = jsonFilters.keys();
		try {
			while (keys.hasNext()) {
				String key = keys.next();
				String value = jsonFilters.getString(key);
				toReturn.put(key, value);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading spagobi filters [" + jsonFilters + "]", t);
		}

		return toReturn;
	}

	@GET
	@Path("/enterprise")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
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

	// Start CKAN integration by Alessandro P
	// @GET
	// @Path("/ckan")
	// @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	// public String getCkanDataSet(@QueryParam("typeDoc") String typeDoc) {
	// logger.debug("IN");
	// try {
	// List<IDataSet> dataSets = getDatasetManagementAPI().getCkanDataSet();
	// return serializeDataSets(dataSets, typeDoc);
	// } catch (Throwable t) {
	// throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
	// } finally {
	// logger.debug("OUT");
	// }
	// }
	// End integration

	@GET
	@Path("/owned")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
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
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
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
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
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
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
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

	// @GET
	// @Path("/getflatdataset")
	// @Produces(MediaType.APPLICATION_JSON)
	// public String getFlatDataSet(@Context HttpServletRequest req) {
	// IDataSetDAO dataSetDao = null;
	// List<IDataSet> dataSets;
	// IEngUserProfile profile = (IEngUserProfile) req.getSession()
	// .getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	// JSONObject JSONReturn = new JSONObject();
	// JSONArray datasetsJSONArray = new JSONArray();
	// try {
	// dataSetDao = DAOFactory.getDataSetDAO();
	// dataSetDao.setUserProfile(profile);
	// dataSets = dataSetDao.loadFlatDatasets(profile.getUserUniqueIdentifier().toString());
	//
	// datasetsJSONArray = (JSONArray) SerializerFactory.getSerializer(
	// "application/json").serialize(dataSets, null);
	//
	// JSONArray datasetsJSONReturn = putActions(profile, datasetsJSONArray, null);
	//
	// JSONReturn.put("root", datasetsJSONReturn);
	//
	// } catch (Throwable t) {
	// throw new SpagoBIServiceException(
	// "An unexpected error occured while instatiating the dao", t);
	// }
	// return JSONReturn.toString();
	//
	// }

	// ===================================================================
	// UTILITY METHODS
	// ===================================================================

	public String getUserId() {
		return getUserProfile().getUserUniqueIdentifier().toString();
	}

	// private UserProfile getUserProfile() {
	// UserProfile profile = this.getIOManager().getUserProfile();
	// return profile;
	// }

	// private IDataSetDAO getDataSetDAO() {
	// IDataSetDAO dataSetDao = null;
	// try {
	// dataSetDao = DAOFactory.getDataSetDAO();
	// } catch (Throwable t) {
	// throw new SpagoBIRuntimeException("An unexpected error occured while instatiating the DAO", t);
	// }
	//
	//
	// dataSetDao.setUserProfile(getUserProfile());
	// return dataSetDao;
	// }

	protected DatasetManagementAPI getDatasetManagementAPI() {
		DatasetManagementAPI managementAPI = new DatasetManagementAPI(getUserProfile());
		return managementAPI;
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
				String attributeNature = (isSegmentAttributeObj != null && (Boolean.parseBoolean(isSegmentAttributeObj.toString()) == true)) ? "segment_attribute"
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
		fieldColumnType = fieldColumnType.substring(fieldColumnType.lastIndexOf(".") + 1); // clean the class type name
		return fieldColumnType;
	}

	protected String serializeDataSet(IDataSet dataSet, String typeDocWizard) throws JSONException {
		try {
			JSONObject datasetsJSONObject = (JSONObject) SerializerFactory.getSerializer("application/json").serialize(dataSet, null);
			JSONArray datasetsJSONArray = new JSONArray();
			datasetsJSONArray.put(datasetsJSONObject);
			JSONArray datasetsJSONReturn = putActions(getUserProfile(), datasetsJSONArray, typeDocWizard);
			return datasetsJSONReturn.toString();
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while serializing results", t);
		}
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

	/**
	 * 
	 * @param profile
	 * @param datasetsJSONArray
	 * @param typeDocWizard
	 *            Usato dalla my analysis per visualizzare solo i dataset su cui è possi bile costruire un certo tipo di analisi selfservice. Al momento filtra
	 *            la lista dei dataset solo nel caso del GEO in cui vengono eliminati tutti i dataset che non contengono un riferimento alla dimensione
	 *            spaziale. Ovviamente il fatto che un metodo che si chiama putActions filtri in modo silente la lista dei dataset è una follia che andrebbe
	 *            rifattorizzata al più presto.
	 * 
	 * @return
	 * @throws JSONException
	 * @throws EMFInternalError
	 */
	private JSONArray putActions(IEngUserProfile profile, JSONArray datasetsJSONArray, String typeDocWizard) throws JSONException, EMFInternalError {

		Engine wsEngine = null;
		try {
			wsEngine = ExecuteAdHocUtility.getWorksheetEngine();
		} catch (SpagoBIRuntimeException r) {
			// the ws engine is not found
			logger.info("Engine not found. ", r);
		}

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

		JSONObject worksheetAction = new JSONObject();
		worksheetAction.put("name", "worksheet");
		worksheetAction.put("description", "Show Worksheet");

		JSONObject georeportAction = new JSONObject();
		georeportAction.put("name", "georeport");
		georeportAction.put("description", "Show Map");

		JSONObject qbeAction = new JSONObject();
		qbeAction.put("name", "qbe");
		qbeAction.put("description", "Show Qbe");

		JSONArray datasetsJSONReturn = new JSONArray();
		for (int i = 0; i < datasetsJSONArray.length(); i++) {
			JSONObject datasetJSON = datasetsJSONArray.getJSONObject(i);
			JSONArray actions = new JSONArray();

			if (typeDocWizard == null) {
				actions.put(detailAction);
				if (profile.getUserUniqueIdentifier().toString().equals(datasetJSON.get("owner"))) {
					// the delete action is able only for private dataset
					actions.put(deleteAction);
				}
			}

			boolean isGeoDataset = false;

			try {
				// String meta = datasetJSON.getString("meta"); // [A]
				// isGeoDataset = ExecuteAdHocUtility.hasGeoHierarchy(meta); // [A]

				String meta = datasetJSON.optString("meta");

				if (meta != null && !meta.equals(""))
					isGeoDataset = ExecuteAdHocUtility.hasGeoHierarchy(meta);

			} catch (Exception e) {
				logger.error("Error during check of Geo spatial column", e);
			}

			if (isGeoDataset && geoEngine != null) {
				actions.put(georeportAction);
			}

			if (wsEngine != null && (typeDocWizard == null || typeDocWizard.equalsIgnoreCase("REPORT"))) {
				actions.put(worksheetAction);
				if (qbeEngine != null && profile.getFunctionalities().contains(SpagoBIConstants.BUILD_QBE_QUERIES_FUNCTIONALITY)) {
					actions.put(qbeAction);
				}
			}

			datasetJSON.put("actions", actions);

			if ("GEO".equalsIgnoreCase(typeDocWizard)) {
				// if is caming from myAnalysis - create Geo Document - must shows only ds geospatial --> isGeoDataset == true
				if (geoEngine != null && isGeoDataset) {
					datasetsJSONReturn.put(datasetJSON);
				}
			} else {
				datasetsJSONReturn.put(datasetJSON);
			}

		}
		return datasetsJSONReturn;
	}

	@POST
	@Path("/{datasetLabel}/cleanCache")
	@Produces(MediaType.APPLICATION_JSON)
	public String cleanCache(@PathParam("datasetLabel") String datasetLabel) {
		logger.debug("IN");
		try {
			logger.debug("get dataset with label " + datasetLabel);
			IDataSet dataSet = getDatasetManagementAPI().getDataSet(datasetLabel);
			ICache cache = SpagoBICacheManager.getCache();
			logger.debug("Delete from cache dataset references with signature " + dataSet.getSignature());
			cache.delete(dataSet.getSignature());
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occurred while cleaning cache for dataset with label "
					+ datasetLabel, t);
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
	public String checkAssociation(@PathParam("association") String association) {
		logger.debug("IN");

		JSONObject toReturn = new JSONObject();

		logger.debug("Association to check " + association);

		try {
			JSONArray arrayAss = new JSONArray(association);

			boolean valid = getDatasetManagementAPI().checkAssociation(arrayAss);
			logger.debug("The association is valid? " + valid);
			toReturn.put("valid", valid);

		} catch (Exception e) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "Error while checking association " + association, e);
		} finally {
			logger.debug("OUT");
		}

		return toReturn.toString();
	}

}
