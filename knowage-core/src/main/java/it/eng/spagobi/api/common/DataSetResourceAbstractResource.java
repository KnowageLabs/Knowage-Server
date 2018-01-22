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
package it.eng.spagobi.api.common;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.FileDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.FilterCriteria;
import it.eng.spagobi.tools.dataset.cache.GroupCriteria;
import it.eng.spagobi.tools.dataset.cache.Operand;
import it.eng.spagobi.tools.dataset.cache.ProjectionCriteria;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.CockpitJSONDataWriter;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.DataSetFactory;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.exceptions.DatasetInUseException;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.dataset.utils.datamart.SpagoBICoreDatamartRetriever;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.datasource.dao.IDataSourceDAO;
import it.eng.spagobi.utilities.assertion.UnreachableCodeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;
import it.eng.spagobi.utilities.sql.SqlUtils;

public abstract class DataSetResourceAbstractResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DataSetResourceAbstractResource.class);

	static final protected String DEFAULT_TABLE_NAME_DOT = DataStore.DEFAULT_TABLE_NAME + ".";

	private static final String DATE_TIME_FORMAT_MYSQL = CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT.replace("yyyy", "%Y").replace("MM", "%m")
			.replace("dd", "%d").replace("HH", "%H").replace("mm", "%i").replace("ss", "%s");
	private static final String DATE_TIME_FORMAT_SQL_STANDARD = CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT.replace("yyyy", "YYYY").replace("MM", "MM")
			.replace("dd", "DD").replace("HH", "HH24").replace("mm", "MI").replace("ss", "SS");
	private static final String DATE_TIME_FORMAT_SQLSERVER = "yyyyMMdd HH:mm:ss";

	protected enum DatasetEvaluationStrategy {
		PERSISTED, FLAT, JDBC, NEAR_REALTIME, CACHED
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

	protected Map<String, Object> getDataSetWriterProperties() throws JSONException {
		Map<String, Object> properties = new HashMap<String, Object>();
		JSONArray fieldOptions = new JSONArray("[{id: 1, options: {measureScaleFactor: 0.5}}]");
		properties.put(JSONDataWriter.PROPERTY_FIELD_OPTION, fieldOptions);
		return properties;
	}

	protected Integer[] getIdsAsIntegers(String ids) {
		Integer[] idArray = null;
		if (ids != null && !ids.isEmpty()) {
			String[] split = ids.split(",");
			idArray = new Integer[split.length];
			for (int i = 0; i < split.length; i++) {
				idArray[i] = Integer.parseInt(split[i]);
			}
		}
		return idArray;
	}

	public String getDataStore(String label, String parameters, String selections, String likeSelections, int maxRowCount, String aggregations,
			String summaryRow, int offset, int fetchSize, boolean isNearRealtime) {
		logger.debug("IN");

		try {
			int maxResults = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
			logger.debug("Offset [" + offset + "], fetch size [" + fetchSize + "], max results[" + maxResults + "]");
			if (maxResults <= 0) {
				throw new SpagoBIRuntimeException("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER value cannot be a non-positive integer");
			}

			if (offset < 0 || fetchSize <= 0) {
				logger.debug("Offset or fetch size are not valid. Setting them to [0] and [" + maxResults + "] by default.");
				offset = 0;
				fetchSize = maxResults;
			}
			if (fetchSize > maxResults) {
				throw new IllegalArgumentException("The page requested is too big. Max page size is equals to [" + maxResults + "]");
			}
			if (maxRowCount > maxResults) {
				throw new IllegalArgumentException("The dataset requested is too big. Max row count is equals to [" + maxResults + "]");
			}

			List<ProjectionCriteria> projectionCriteria = new ArrayList<ProjectionCriteria>();
			List<GroupCriteria> groupCriteria = new ArrayList<GroupCriteria>();
			Map<String, String> columnAliasToName = new HashMap<String, String>();
			if (aggregations != null && !aggregations.equals("")) {
				JSONObject aggregationsObject = new JSONObject(aggregations);
				JSONArray categoriesObject = aggregationsObject.getJSONArray("categories");
				JSONArray measuresObject = aggregationsObject.getJSONArray("measures");

				projectionCriteria = getProjectionCriteria(label, categoriesObject, measuresObject);
				groupCriteria = getGroupCriteria(label, categoriesObject, measuresObject);

				loadColumnAliasToName(categoriesObject, columnAliasToName);
				loadColumnAliasToName(measuresObject, columnAliasToName);
			}

			List<FilterCriteria> filterCriteria = new ArrayList<FilterCriteria>();
			List<FilterCriteria> filterCriteriaForMetaModel = new ArrayList<FilterCriteria>();

			List<ProjectionCriteria> summaryRowProjectionCriteria = new ArrayList<ProjectionCriteria>();
			if (summaryRow != null && !summaryRow.equals("")) {
				JSONObject summaryRowObject = new JSONObject(summaryRow);
				JSONArray summaryRowMeasuresObject = summaryRowObject.getJSONArray("measures");
				summaryRowProjectionCriteria = getProjectionCriteria(label, new JSONArray(), summaryRowMeasuresObject);
			}

			if (selections != null && !selections.equals("")) {
				JSONObject selectionsObject = new JSONObject(selections);
				// in same case object is empty '{}'
				if (selectionsObject.names() != null) {

					filterCriteria = getFilterCriteria(label, selectionsObject, false, columnAliasToName);
					filterCriteriaForMetaModel = getFilterCriteria(label, selectionsObject, true, columnAliasToName);

				}

			}

			List<FilterCriteria> havingCriteria = new ArrayList<FilterCriteria>();
			List<FilterCriteria> havingCriteriaForMetaModel = new ArrayList<FilterCriteria>();
			if (likeSelections != null && !likeSelections.equals("")) {
				JSONObject likeSelectionsObject = new JSONObject(likeSelections);
				if (likeSelectionsObject.names() != null) {
					filterCriteria.addAll(getLikeFilterCriteria(label, likeSelectionsObject, isNearRealtime, columnAliasToName, projectionCriteria, true));
					havingCriteria.addAll(getLikeFilterCriteria(label, likeSelectionsObject, isNearRealtime, columnAliasToName, projectionCriteria, false));

					filterCriteriaForMetaModel.addAll(getLikeFilterCriteria(label, likeSelectionsObject, true, columnAliasToName, projectionCriteria, true));
					havingCriteriaForMetaModel.addAll(getLikeFilterCriteria(label, likeSelectionsObject, true, columnAliasToName, projectionCriteria, false));
				}
			}

			if (selections != null && !selections.equals("")) {
				// check if max or min filters are used and caclulate it
				filterCriteria = getDatasetManagementAPI().calculateMinMaxFilter(label, parameters, selections, likeSelections, maxRowCount, aggregations,
						summaryRow, offset, fetchSize, false, groupCriteria, filterCriteriaForMetaModel, new ArrayList<ProjectionCriteria>() // ,
																																				// summaryRowProjectionCriteria
						, havingCriteria, havingCriteriaForMetaModel, filterCriteria, projectionCriteria);
			}

			IDataStore dataStore = getDatasetManagementAPI().getDataStore(label, offset, fetchSize, maxRowCount, isNearRealtime,
					DataSetUtilities.getParametersMap(parameters), groupCriteria, filterCriteria, filterCriteriaForMetaModel, havingCriteria,
					havingCriteriaForMetaModel, projectionCriteria, summaryRowProjectionCriteria);

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

	protected List<ProjectionCriteria> getProjectionCriteria(String dataset, JSONArray categoriesObject, JSONArray measuresObject) throws JSONException {
		List<ProjectionCriteria> projectionCriterias = new ArrayList<ProjectionCriteria>();
		for (int i = 0; i < categoriesObject.length(); i++) {
			JSONObject categoryObject = categoriesObject.getJSONObject(i);

			// In the Cockpit Engine, table, you can insert many times the same category.
			// To manage this, it's not possibile to use the alias as column name.
			// So in the category object there is also a "columnName" field
			String columnName;
			if (!categoryObject.isNull("columnName")) {
				columnName = categoryObject.getString("columnName");
			} else {
				columnName = categoryObject.getString("alias");
			}

			String aliasName = categoryObject.getString("alias");

			String orderTypeFinal = (String) categoryObject.opt("orderType");
			if (orderTypeFinal != null) {
				orderTypeFinal = orderTypeFinal.toUpperCase();
			}

			String orderColumnFinal = (String) categoryObject.opt("orderColumn");

			ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, aliasName, orderTypeFinal, orderColumnFinal);
			projectionCriterias.add(aProjectionCriteria);
		}
		for (int i = 0; i < measuresObject.length(); i++) {
			JSONObject measureObject = measuresObject.getJSONObject(i);

			// In the Cockpit Engine, table, you can insert many times the same measure.
			// To manage this, it's not possibile to use the alias as column name.
			// So in the measure object there is also a "columnName" field.
			String columnName;
			if (!measureObject.isNull("columnName")) {
				columnName = measureObject.getString("columnName");
			} else {
				columnName = measureObject.getString("alias");
			}

			String aliasName = measureObject.getString("alias");

			// https://production.eng.it/jira/browse/KNOWAGE-149
			String orderTypeFinal = (String) measureObject.opt("orderType");
			if (orderTypeFinal != null) {
				orderTypeFinal = orderTypeFinal.toUpperCase();
			}

			IAggregationFunction function = AggregationFunctions.get(measureObject.getString("funct"));
			if (function != AggregationFunctions.NONE_FUNCTION) {
				ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, function.getName(), aliasName, orderTypeFinal);
				projectionCriterias.add(aProjectionCriteria);
			} else {
				ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, aliasName, orderTypeFinal);
				projectionCriterias.add(aProjectionCriteria);
			}
		}
		return projectionCriterias;
	}

	protected List<GroupCriteria> getGroupCriteria(String dataset, JSONArray categoriesObject, JSONArray measuresObject) throws JSONException {
		List<GroupCriteria> groupCriterias = new ArrayList<GroupCriteria>();

		boolean isAggregationPresentOnMeasures = false;
		for (int i = 0; i < measuresObject.length(); i++) {
			JSONObject measureObject = measuresObject.getJSONObject(i);
			String aggregationFunction = measureObject.optString("funct");
			if (aggregationFunction != null && !aggregationFunction.isEmpty() && !aggregationFunction.toUpperCase().equals("NONE")) {
				isAggregationPresentOnMeasures = true;
				break;
			}
		}

		if (isAggregationPresentOnMeasures) {
			for (int i = 0; i < categoriesObject.length(); i++) {
				JSONObject categoryObject = categoriesObject.getJSONObject(i);

				String columnName;

				// In the table of Cockpit Engine you can insert many times the same measure.
				// To manage this, it's not possibile to use the alias as column name.
				// So in the measure object there is also a "columnName" field.

				if (!categoryObject.isNull("columnName")) {
					columnName = categoryObject.getString("columnName");
				} else {
					columnName = categoryObject.getString("alias");
				}

				GroupCriteria groupCriteria = new GroupCriteria(dataset, columnName, null);
				groupCriterias.add(groupCriteria);
			}
		}

		return groupCriterias;
	}

	protected List<FilterCriteria> getFilterCriteria(String dataset, JSONObject selectionsObject, boolean isRealtime, Map<String, String> columnAliasToName)
			throws JSONException {
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

	protected List<FilterCriteria> getLikeFilterCriteria(String datasetLabel, JSONObject likeSelectionsObject, boolean isNearRealtime,
			Map<String, String> columnAliasToName, List<ProjectionCriteria> projectionCriteria, boolean getAttributes) throws JSONException {
		List<FilterCriteria> likeFilterCriterias = new ArrayList<FilterCriteria>();
		return likeFilterCriterias;
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
				Map<String, String> filtersMap = DataSetUtilities.getParametersMap(datasetFilters);
				toReturn.put(datasetLabel, filtersMap);
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading spagobi filters [" + parameters + "]", t);
		}

		return toReturn;
	}

	protected void loadColumnAliasToName(JSONArray jsonArray, Map<String, String> columnAliasToName) throws JSONException {
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject category = jsonArray.getJSONObject(i);
			String alias = category.optString("alias");
			String columnName = category.optString("columnName");
			if (alias != null && !alias.isEmpty() && columnName != null && !columnName.isEmpty()) {
				columnAliasToName.put(alias, columnName);
			}
		}
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
	protected JSONArray putActions(IEngUserProfile profile, JSONArray datasetsJSONArray, String typeDocWizard) throws JSONException, EMFInternalError {

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
				// isGeoDataset = ExecuteAdHocUtility.hasGeoHierarchy(meta); //
				// [A]

				String meta = datasetJSON.optString("meta");

				if (meta != null && !meta.equals(""))
					isGeoDataset = ExecuteAdHocUtility.hasGeoHierarchy(meta);

			} catch (Exception e) {
				logger.error("Error during check of Geo spatial column", e);
			}

			if (isGeoDataset && geoEngine != null) {
				actions.put(georeportAction);
			}

			String dsType = datasetJSON.optString(DataSetConstants.DS_TYPE_CD);
			if (dsType == null || !dsType.equals(DataSetFactory.FEDERATED_DS_TYPE)) {
				if (qbeEngine != null && (typeDocWizard == null || typeDocWizard.equalsIgnoreCase("REPORT"))) {
					if (profile.getFunctionalities() != null && profile.getFunctionalities().contains(SpagoBIConstants.BUILD_QBE_QUERIES_FUNCTIONALITY)) {
						actions.put(qbeAction);
					}
				}
			}

			datasetJSON.put("actions", actions);

			if ("GEO".equalsIgnoreCase(typeDocWizard)) {
				// if is caming from myAnalysis - create Geo Document - must
				// shows only ds geospatial --> isGeoDataset == true
				if (geoEngine != null && isGeoDataset) {
					datasetsJSONReturn.put(datasetJSON);
				}
			} else {
				datasetsJSONReturn.put(datasetJSON);
			}

		}
		return datasetsJSONReturn;
	}

	public String getDataSet(String label) {
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

	public Response deleteDataset(String label) {
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
			if (dataset instanceof VersionedDataSet) {
				VersionedDataSet versioneDataSet = (VersionedDataSet) dataset;
				IDataSet wrappedDataset = versioneDataSet.getWrappedDataset();
				if (wrappedDataset instanceof FileDataSet) {
					// remove also the file from resources
					FileDataSet fileDataSet = (FileDataSet) wrappedDataset;
					String fileDirectory = SpagoBIUtilities.getDatasetResourcePath() + File.separatorChar + "files";
					String fileName = fileDataSet.getFileName();
					File fileToDelete = new File(fileDirectory + File.separatorChar + fileName);
					if (fileToDelete.exists()) {
						// delete the file
						if (fileToDelete.delete()) {
							// delete success
							logger.debug("Dataset file correctly delete: " + fileName);
						} else {
							// delete failed
							logger.error("Error deleting file dataset: " + fileName);
							throw new SpagoBIRuntimeException("Error deleting file dataset: " + fileName);
						}
					}
				}

			}
		} catch (Exception e) {
			String message = null;
			if (e instanceof DatasetInUseException) {
				DatasetInUseException dui = (DatasetInUseException) e;
				message = dui.getMessage() + "Used by: objects" + dui.getObjectsLabel() + " federations: " + dui.getFederationsLabel();
			} else {
				message = "Error while deleting the specified dataset";
			}

			logger.error("Error while deleting the specified dataset", e);
			throw new SpagoBIRuntimeException(message, e);
		}

		return Response.ok().build();
	}

	public Response execute(String label, String body) {
		SDKDataSetParameter[] parameters = null;

		if (request.getParameterMap() != null && request.getParameterMap().size() > 0) {

			parameters = new SDKDataSetParameter[request.getParameterMap().size()];

			int i = 0;
			for (Iterator iterator = request.getParameterMap().keySet().iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				String[] values = request.getParameterMap().get(key);
				SDKDataSetParameter sdkDataSetParameter = new SDKDataSetParameter();
				sdkDataSetParameter.setName(key);
				sdkDataSetParameter.setValues(values);
				parameters[i] = sdkDataSetParameter;
				i++;
			}
		}
		return Response.ok(executeDataSet(label, parameters)).build();
	}

	protected String executeDataSet(String label, SDKDataSetParameter[] params) {
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

	protected IDataSetDAO getDataSetDAO() {
		IDataSetDAO dsDAO;
		try {
			dsDAO = DAOFactory.getDataSetDAO();
		} catch (EMFUserError e) {
			String error = "Error while looking for datasets";
			logger.error(error, e);
			throw new SpagoBIRuntimeException(error, e);
		}
		return dsDAO;
	}

	protected IDataSourceDAO getDataSourceDAO() {
		IDataSourceDAO dataSourceDAO;
		try {
			dataSourceDAO = DAOFactory.getDataSourceDAO();
		} catch (EMFUserError e) {
			String error = "Error while looking for datasources";
			logger.error(error, e);
			throw new SpagoBIRuntimeException(error, e);
		}
		return dataSourceDAO;
	}

	protected String convertDateString(String dateString, String srcFormatString, String dstFormatString) {
		try {
			SimpleDateFormat srcFormat = new SimpleDateFormat(srcFormatString);
			Date date = srcFormat.parse(dateString);
			SimpleDateFormat dstFormat = new SimpleDateFormat(dstFormatString);
			return dstFormat.format(date);
		} catch (ParseException e) {
			String message = "Unable to parse date [" + dateString + "] with format [" + srcFormatString + "]";
			logger.error(message, e);
			throw new SpagoBIRuntimeException(message, e);
		}
	}

	protected String getDateForQuery(String dateStringToConvert, IDataSource dataSource) {
		String properDateString = dateStringToConvert;

		if (dataSource != null && dateStringToConvert != null) {
			String actualDialect = dataSource.getHibDialectClass();

			if (SqlUtils.DIALECT_MYSQL.equalsIgnoreCase(actualDialect)) {
				properDateString = "STR_TO_DATE('" + dateStringToConvert + "', '" + DATE_TIME_FORMAT_MYSQL + "')";
			} else if (SqlUtils.DIALECT_POSTGRES.equalsIgnoreCase(actualDialect) || SqlUtils.DIALECT_ORACLE.equalsIgnoreCase(actualDialect)
					|| SqlUtils.DIALECT_ORACLE9i10g.equalsIgnoreCase(actualDialect) || SqlUtils.DIALECT_HSQL.equalsIgnoreCase(actualDialect)
					|| SqlUtils.DIALECT_TERADATA.equalsIgnoreCase(actualDialect)) {
				properDateString = "TO_DATE('" + dateStringToConvert + "','" + DATE_TIME_FORMAT_SQL_STANDARD + "')";
			} else if (SqlUtils.DIALECT_SQLSERVER.equalsIgnoreCase(actualDialect)) {
				properDateString = "'" + convertDateString(dateStringToConvert, CockpitJSONDataWriter.CACHE_DATE_TIME_FORMAT, DATE_TIME_FORMAT_SQLSERVER) + "'";
			} else if (SqlUtils.DIALECT_INGRES.equalsIgnoreCase(actualDialect)) {
				properDateString = "DATE('" + dateStringToConvert + "')";
			}
		}

		return properDateString;
	}

	protected boolean isDateColumn(String columnName, IDataSet dataSet) {
		for (int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
			IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
			if (fieldMeta.getName().equals(columnName) && Date.class.isAssignableFrom(fieldMeta.getType())) {
				return true;
			}
		}
		return false;
	}

	protected IDataSource getDataSource(IDataSet dataSet, boolean isNearRealTime) {
		DatasetEvaluationStrategy strategy = getDatasetEvaluationStrategy(dataSet, isNearRealTime);
		IDataSource dataSource = null;

		if (strategy == DatasetEvaluationStrategy.PERSISTED) {
			dataSource = dataSet.getDataSourceForWriting();
		} else if (strategy == DatasetEvaluationStrategy.FLAT || strategy == DatasetEvaluationStrategy.JDBC) {
			try {
				dataSource = dataSet.getDataSource();
			} catch (UnreachableCodeException e) {
			}
		} else if (strategy == DatasetEvaluationStrategy.NEAR_REALTIME) {
			dataSource = null;
		} else {
			dataSource = SpagoBICacheConfiguration.getInstance().getCacheDataSource();
		}

		return dataSource;
	}

	protected DatasetEvaluationStrategy getDatasetEvaluationStrategy(IDataSet dataSet, boolean isNearRealtime) {
		DatasetEvaluationStrategy result;

		if (dataSet.isPersisted()) {
			result = DatasetEvaluationStrategy.PERSISTED;
		} else if (dataSet.isFlatDataset()) {
			result = DatasetEvaluationStrategy.FLAT;
		} else {
			boolean isJDBCDataSet = DatasetManagementAPI.isJDBCDataSet(dataSet);
			boolean isBigDataDialect = SqlUtils.isBigDataDialect(dataSet.getDataSource() != null ? dataSet.getDataSource().getHibDialectName() : "");
			if (isNearRealtime && isJDBCDataSet && !isBigDataDialect && !dataSet.hasDataStoreTransformer()) {
				result = DatasetEvaluationStrategy.JDBC;
			} else if (isNearRealtime) {
				result = DatasetEvaluationStrategy.NEAR_REALTIME;
			} else {
				result = DatasetEvaluationStrategy.CACHED;
			}
		}

		return result;
	}

	protected String getFilter(IDataSet dataset, boolean isNearRealtime, String column, String values) {
		IDataSource dataSource = getDataSource(dataset, isNearRealtime);
		String tablePrefix = getTablePrefix(dataset, isNearRealtime);
		DatasetEvaluationStrategy strategy = getDatasetEvaluationStrategy(dataset, isNearRealtime);

		if (DatasetEvaluationStrategy.NEAR_REALTIME.equals(strategy) || SqlUtils.hasSqlServerDialect(dataSource) || SqlUtils.hasTeradataDialect(dataSource)) {
			return getOrFilterString(column, values, dataSource, tablePrefix);
		} else {
			return getInFilterString(column, values, dataSource, tablePrefix);
		}
	}

	private String getOrFilterString(String column, String values, IDataSource dataSource, String tablePrefix) {
		String encapsulateColumnName = tablePrefix + AbstractJDBCDataset.encapsulateColumnName(column, dataSource);
		String[] singleValues = values.split(",");
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < singleValues.length; i++) {
			if (i > 0) {
				sb.append(" OR ");
			}
			sb.append(encapsulateColumnName);
			sb.append("=");
			sb.append(singleValues[i]);
		}

		return sb.toString();
	}

	private String getInFilterString(String column, String values, IDataSource dataSource, String tablePrefix) {
		StringBuilder sb = new StringBuilder();

		sb.append(tablePrefix);
		sb.append(AbstractJDBCDataset.encapsulateColumnName(column, dataSource));
		sb.append(" IN (");
		sb.append(values);
		sb.append(")");

		return sb.toString();
	}

	private String getTablePrefix(IDataSet dataset, boolean isNearRealtime) {
		DatasetEvaluationStrategy datasetEvaluationStrategy = getDatasetEvaluationStrategy(dataset, isNearRealtime);
		if (datasetEvaluationStrategy == DatasetEvaluationStrategy.NEAR_REALTIME) {
			return DEFAULT_TABLE_NAME_DOT;
		} else {
			return "";
		}
	}
}
