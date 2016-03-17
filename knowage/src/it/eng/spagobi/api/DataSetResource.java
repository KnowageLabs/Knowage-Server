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

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.deserializer.DeserializerFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.FilterCriteria;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.GroupCriteria;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.Operand;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.ProjectionCriteria;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.crosstab.CrossTab;
import it.eng.spagobi.tools.dataset.crosstab.CrosstabDefinition;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
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

	@POST
	@Path("/{label}/content")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response execute(@PathParam("label") String label, String body) {
		SDKDataSetParameter[] parameters = null;
		if (body != null && !body.equals("")) {
			parameters = (SDKDataSetParameter[]) JsonConverter.jsonToValidObject(body, SDKDataSetParameter[].class);
		}

		return Response.ok(executeDataSet(label, parameters)).build();
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
			if (aggregations != null && !aggregations.equals("")) {
				JSONObject aggregationsObject = new JSONObject(aggregations);
				JSONArray categoriesObject = aggregationsObject.getJSONArray("categories");
				JSONArray measuresObject = aggregationsObject.getJSONArray("measures");

				projectionCriteria = getProjectionCriteria(label, categoriesObject, measuresObject);
				groupCriteria = getGroupCriteria(label, categoriesObject);
			}

			List<FilterCriteria> filterCriteria = new ArrayList<FilterCriteria>();
			if (selections != null && !selections.equals("")) {
				JSONObject selectionsObject = new JSONObject(selections);
				// in same case object is empty '{}'
				if (selectionsObject.names() != null) {
					filterCriteria = getFilterCriteria(label, selectionsObject);
				}
			}

			int maxResults;
			try {
				maxResults = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
			} catch (NumberFormatException nfe) {
				logger.debug("The value of SPAGOBI.API.DATASET.MAX_ROWS_NUMBER config must be an integer");
				maxResults = -1;
			}

			UserProfile profile = getUserProfile();

			HttpSession session = this.getServletRequest().getSession();
			IDataStore dataStore = null;

			dataStore = getDatasetManagementAPI().getDataStore(label, -1, -1, maxResults, getParametersMap(parameters), groupCriteria, filterCriteria,
					projectionCriteria);

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

	private String executeDataSet(String label, SDKDataSetParameter[] params) {
		logger.debug("IN: label in input = " + label);

		try {
			if (label == null) {
				logger.warn("DataSet identifier in input is null!");
				return null;
			}
			IDataSet dataSet = DAOFactory.getDataSetDAO().loadDataSetByLabel(label);
			if (dataSet == null) {
				logger.warn("DataSet with label [" + label + "] not existing.");
				return null;
			}
			if (params != null && params.length > 0) {
				HashMap parametersFilled = new HashMap();
				for (int i = 0; i < params.length; i++) {
					SDKDataSetParameter par = params[i];
					parametersFilled.put(par.getName(), par.getValues()[0]);
					logger.debug("Add parameter: " + par.getName() + "/" + par.getValues()[0]);
				}
				dataSet.setParamsMap(parametersFilled);
			}

			// add the jar retriver in case of a Qbe DataSet
			if (dataSet instanceof QbeDataSet
					|| (dataSet instanceof VersionedDataSet && ((VersionedDataSet) dataSet).getWrappedDataset() instanceof QbeDataSet)) {
				SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
				Map parameters = dataSet.getParamsMap();
				if (parameters == null) {
					parameters = new HashMap();
					dataSet.setParamsMap(parameters);
				}
				dataSet.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
			}

			dataSet.loadData();
			// toReturn = dataSet.getDataStore().toXml();

			JSONDataWriter writer = new JSONDataWriter();
			return (writer.write(dataSet.getDataStore())).toString();
		} catch (Exception e) {
			logger.error("Error while executing dataset", e);
			throw new SpagoBIRuntimeException("Error while executing dataset", e);
		}
	}

	protected List<ProjectionCriteria> getProjectionCriteria(String dataset, JSONArray categoriesObject, JSONArray measuresObject) throws JSONException {
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

			/**
			 * The ordering column and its ordering type for the first category of the document.
			 * 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			String orderColumn = (categoryObject.has("orderColumn") && categoryObject.opt("orderColumn") != null) ? categoryObject.getString("orderColumn")
					: null;
			String orderType = (categoryObject.has("orderColumn") && categoryObject.opt("orderType") != null) ? categoryObject.opt("orderType").toString()
					.toUpperCase() : null;

			ProjectionCriteria aProjectionCriteria = null;

			if (orderColumn == null) {
				aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, aliasName);
			} else {
				/**
				 * Create a new projection object that through the new constructor that handles additional parameter - the ordering column for the category of
				 * the document. Set the associated ordering type (of the first category's column).
				 * 
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, aliasName, orderType, orderColumn);
			}

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

			// https://production.eng.it/jira/browse/KNOWAGE-149
			String orderTypeFinal = (measureObject.opt("orderType") != null) ? orderTypeFinal = measureObject.opt("orderType").toString().toUpperCase() : null;

			IAggregationFunction function = AggregationFunctions.get(measureObject.getString("funct"));
			if (function != AggregationFunctions.NONE_FUNCTION) {
				// ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, function.getName(), columnName);
				ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, function.getName(), aliasName, orderTypeFinal);
				projectionCriterias.add(aProjectionCriteria);
			} else {
				// ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, columnName);
				ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, aliasName);
				projectionCriterias.add(aProjectionCriteria);
			}
		}
		return projectionCriterias;
	}

	protected List<GroupCriteria> getGroupCriteria(String dataset, JSONArray categoriesObject) throws JSONException {
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

	protected List<FilterCriteria> getFilterCriteria(String dataset, JSONObject selectionsObject) throws JSONException {
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

	protected static Map<String, String> getParametersMap(String filters) {
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

	// ===================================================================
	// UTILITY METHODS
	// ===================================================================

	public String getUserId() {
		return getUserProfile().getUserUniqueIdentifier().toString();
	}

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
	 * @param profile
	 * @param datasetsJSONArray
	 * @param typeDocWizard
	 *            Usato dalla my analysis per visualizzare solo i dataset su cui è possi bile costruire un certo tipo di analisi selfservice. Al momento filtra
	 *            la lista dei dataset solo nel caso del GEO in cui vengono eliminati tutti i dataset che non contengono un riferimento alla dimensione
	 *            spaziale. Ovviamente il fatto che un metodo che si chiama putActions filtri in modo silente la lista dei dataset è una follia che andrebbe
	 *            rifattorizzata al più presto.
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

			// boolean valid = getDatasetManagementAPI().checkAssociation(arrayAss);
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
	 * Persist a dataset list
	 *
	 * @param labels
	 */

	@POST
	@Path("/list/persist")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String persistDataSets(@QueryParam("labelsAndKeys") JSONObject labels) {
		logger.debug("IN");

		JSONObject labelsJSON = new JSONObject();

		Iterator<String> keys = labels.keys();
		while (keys.hasNext()) {
			String label = keys.next();
			try {
				DatasetManagementAPI dataSetManagementAPI = getDatasetManagementAPI();
				dataSetManagementAPI.setUserProfile(getUserProfile());
				String tableName = dataSetManagementAPI.persistDataset(label);
				// dataSetManagementAPI.createIndexes(label, new HashSet<String>());
				logger.debug("Dataset with label " + label + " is stored in table with name " + tableName);
				if (tableName != null) {
					labelsJSON.put(label, tableName);
				}
			} catch (JSONException e) {
				logger.error("error in persisting dataset with label: " + label, e);
				throw new RuntimeException("error in persisting dataset with label " + label);
			}
		}

		logger.debug("OUT");
		return labelsJSON.toString();
	}
}
