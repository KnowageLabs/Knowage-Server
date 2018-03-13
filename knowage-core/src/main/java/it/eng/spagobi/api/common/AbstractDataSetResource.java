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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.sdk.datasets.bo.SDKDataSetParameter;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.query.item.Filter;
import it.eng.spagobi.tools.dataset.cache.query.item.InFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.LikeFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.MultipleProjectionSimpleFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Projection;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Sorting;
import it.eng.spagobi.tools.dataset.cache.query.item.UnsatisfiedFilter;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.IDataWriter;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
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
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.assertion.UnreachableCodeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public abstract class AbstractDataSetResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(AbstractDataSetResource.class);

	// ===================================================================
	// UTILITY METHODS
	// ===================================================================

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
		Monitor totalTiming = MonitorFactory.start("Knowage.AbstractDataSetResource.getDataStore");
		try {
			Monitor timing = MonitorFactory.start("Knowage.AbstractDataSetResource.getDataStore:validateParams");

			int maxResults = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER"));
			logger.debug("Offset [" + offset + "], fetch size [" + fetchSize + "], max results[" + maxResults + "]");

			if (maxResults <= 0) {
				throw new SpagoBIRuntimeException("SPAGOBI.API.DATASET.MAX_ROWS_NUMBER value cannot be a non-positive integer");
			}
			if (offset < 0 || fetchSize < 0) {
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

			IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
			dataSetDao.setUserProfile(getUserProfile());
			IDataSet dataSet = dataSetDao.loadDataSetByLabel(label);
			Assert.assertNotNull(dataSet, "Unable to load dataset with label [" + label + "]");

			timing.stop();
			timing = MonitorFactory.start("Knowage.AbstractDataSetResource.getDataStore:getQueryDetails");

			List<Projection> projections = new ArrayList<Projection>(0);
			List<Projection> groups = new ArrayList<Projection>(0);
			List<Sorting> sortings = new ArrayList<Sorting>(0);
			List<Projection> summaryRowProjections = new ArrayList<Projection>(0);
			Map<String, String> columnAliasToName = new HashMap<String, String>();
			if (aggregations != null && !aggregations.isEmpty()) {
				JSONObject aggregationsObject = new JSONObject(aggregations);
				JSONArray categoriesObject = aggregationsObject.getJSONArray("categories");
				JSONArray measuresObject = aggregationsObject.getJSONArray("measures");

				loadColumnAliasToName(categoriesObject, columnAliasToName);
				loadColumnAliasToName(measuresObject, columnAliasToName);

				projections.addAll(getProjections(dataSet, categoriesObject, measuresObject, columnAliasToName));
				groups.addAll(getGroups(dataSet, categoriesObject, measuresObject, columnAliasToName));
				sortings.addAll(getSortings(dataSet, categoriesObject, measuresObject, columnAliasToName));

				if (summaryRow != null && !summaryRow.isEmpty()) {
					JSONObject summaryRowObject = new JSONObject(summaryRow);
					JSONArray summaryRowMeasuresObject = getSummaryRowMeasures(measuresObject);
					summaryRowProjections.addAll(getProjections(dataSet, new JSONArray(), summaryRowMeasuresObject, columnAliasToName));
				}
			}

			List<Filter> filters = new ArrayList<>(0);
			if (selections != null && !selections.isEmpty()) {
				JSONObject selectionsObject = new JSONObject(selections);
				if (selectionsObject.names() != null) {
					filters.addAll(getFilters(label, selectionsObject, columnAliasToName));
				}
			}

			List<SimpleFilter> likeFilters = new ArrayList<>(0);
			if (likeSelections != null && !likeSelections.equals("")) {
				JSONObject likeSelectionsObject = new JSONObject(likeSelections);
				if (likeSelectionsObject.names() != null) {
					likeFilters.addAll(getLikeFilters(label, likeSelectionsObject, columnAliasToName));
				}
			}

			Monitor timingMinMax = MonitorFactory.start("Knowage.AbstractDataSetResource.getDataStore:calculateMinMax");
			filters = getDatasetManagementAPI().calculateMinMaxFilters(dataSet, isNearRealtime, DataSetUtilities.getParametersMap(parameters), projections,
					filters, likeFilters, groups);
			timingMinMax.stop();

			Filter where = getDatasetManagementAPI().getWhereFilter(filters, likeFilters);

			timing.stop();

			IDataStore dataStore = getDatasetManagementAPI().getDataStore(dataSet, isNearRealtime, DataSetUtilities.getParametersMap(parameters), projections,
					where, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
			IDataWriter dataWriter = getDataSetWriter();

			timing = MonitorFactory.start("Knowage.AbstractDataSetResource.getDataStore:convertToJson");
			JSONObject gridDataFeed = (JSONObject) dataWriter.write(dataStore);
			timing.stop();

			String stringFeed = gridDataFeed.toString();
			return stringFeed;
		} catch (ParametersNotValorizedException p) {
			throw new ParametersNotValorizedException(p.getMessage());
		} catch (Throwable t) {
			throw new SpagoBIServiceException(this.request.getPathInfo(), "An unexpected error occured while executing service", t);
		} finally {
			totalTiming.stop();
			logger.debug("OUT");
		}
	}

	private JSONArray getSummaryRowMeasures(JSONArray measures) throws JSONException {
		JSONArray summaryRowMeasures = new JSONArray(measures.toString());
		for (int i = 0; i < summaryRowMeasures.length(); i++) {
			JSONObject summaryRowMeasure = (JSONObject) summaryRowMeasures.get(i);
			if (summaryRowMeasure.has("funct") && summaryRowMeasure.getString("funct").equals("NONE")) {
				summaryRowMeasure.put("funct", "SUM");
			}
		}
		return summaryRowMeasures;
	}

	private void addProjection(IDataSet dataSet, ArrayList<Projection> projections, JSONObject catOrMeasure, Map<String, String> columnAliasToName)
			throws JSONException {

		String functionObj = catOrMeasure.optString("funct");
		// check if it is an array
		if (functionObj.startsWith("[")) {
			// call for each aggregation function
			JSONArray functs = new JSONArray(functionObj);
			for (int j = 0; j < functs.length(); j++) {
				String functName = functs.getString(j);
				Projection projection = getProjectionWithFunct(dataSet, catOrMeasure, columnAliasToName, functName);
				projections.add(projection);
			}
		} else {
			// function Objetc contains only one aggregation
			Projection projection = getProjection(dataSet, catOrMeasure, columnAliasToName);
			projections.add(projection);
		}

	}

	protected List<Projection> getProjections(IDataSet dataSet, JSONArray categories, JSONArray measures, Map<String, String> columnAliasToName)
			throws JSONException {
		ArrayList<Projection> projections = new ArrayList<Projection>(categories.length() + measures.length());

		for (int i = 0; i < categories.length(); i++) {
			JSONObject category = categories.getJSONObject(i);
			addProjection(dataSet, projections, category, columnAliasToName);
		}

		for (int i = 0; i < measures.length(); i++) {
			JSONObject measure = measures.getJSONObject(i);
			addProjection(dataSet, projections, measure, columnAliasToName);

		}

		return projections;
	}

	private Projection getProjectionWithFunct(IDataSet dataSet, JSONObject jsonObject, Map<String, String> columnAliasToName, String functName)
			throws JSONException {
		String columnName = getColumnName(jsonObject, columnAliasToName);
		String columnAlias = getColumnAlias(jsonObject, columnAliasToName);
		IAggregationFunction function = AggregationFunctions.get(functName);
		Projection projection = new Projection(function, dataSet, columnName, columnAlias);
		return projection;
	}

	private Projection getProjection(IDataSet dataSet, JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		String columnName = getColumnName(jsonObject, columnAliasToName);
		String columnAlias = getColumnAlias(jsonObject, columnAliasToName);
		IAggregationFunction function = AggregationFunctions.get(jsonObject.optString("funct"));
		Projection projection = new Projection(function, dataSet, columnName, columnAlias);
		return projection;
	}

	private String getColumnName(JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		if (jsonObject.isNull("id") && jsonObject.isNull("columnName")) {
			return getColumnAlias(jsonObject, columnAliasToName);
		} else {
			String id = jsonObject.getString("id");
			boolean isIdMatching = columnAliasToName.containsKey(id) || columnAliasToName.containsValue(id);

			String columnName = jsonObject.getString("columnName");
			boolean isColumnNameMatching = columnAliasToName.containsKey(columnName) || columnAliasToName.containsValue(columnName);

			Assert.assertTrue(isIdMatching || isColumnNameMatching, "Column name [" + columnName + "] not found in dataset metadata");
			return isColumnNameMatching ? columnName : id;
		}
	}

	private String getColumnAlias(JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		String columnAlias = jsonObject.getString("alias");
		Assert.assertTrue(columnAliasToName.containsKey(columnAlias) || columnAliasToName.containsValue(columnAlias),
				"Column alias [" + columnAlias + "] not found in dataset metadata");
		return columnAlias;
	}

	protected List<Projection> getGroups(IDataSet dataSet, JSONArray categories, JSONArray measures, Map<String, String> columnAliasToName)
			throws JSONException {
		ArrayList<Projection> groups = new ArrayList<Projection>(0);

		boolean isAggregationPresentOnMeasures = false;
		for (int i = 0; i < measures.length(); i++) {
			JSONObject measure = measures.getJSONObject(i);
			String functionName = measure.optString("funct");
			if (!AggregationFunctions.get(functionName).getName().equals(AggregationFunctions.NONE)) {
				isAggregationPresentOnMeasures = true;
				break;
			}
		}

		if (isAggregationPresentOnMeasures) {
			for (int i = 0; i < categories.length(); i++) {
				JSONObject category = categories.getJSONObject(i);
				Projection projection = getProjection(dataSet, category, columnAliasToName);
				groups.add(projection);
			}
		}

		return groups;
	}

	protected List<Sorting> getSortings(IDataSet dataSet, JSONArray categories, JSONArray measures, Map<String, String> columnAliasToName)
			throws JSONException {
		ArrayList<Sorting> sortings = new ArrayList<Sorting>(0);

		for (int i = 0; i < categories.length(); i++) {
			JSONObject categoryObject = categories.getJSONObject(i);
			Sorting sorting = getSorting(dataSet, categoryObject, columnAliasToName);
			if (sorting != null) {
				sortings.add(sorting);
			}
		}

		for (int i = 0; i < measures.length(); i++) {
			JSONObject measure = measures.getJSONObject(i);
			Sorting sorting = getSorting(dataSet, measure, columnAliasToName);
			if (sorting != null) {
				sortings.add(sorting);
			}
		}

		return sortings;
	}

	private Sorting getSorting(IDataSet dataSet, JSONObject jsonObject, Map<String, String> columnAliasToName) throws JSONException {
		Sorting sorting = null;

		String orderType = (String) jsonObject.opt("orderType");
		if (orderType != null && !orderType.isEmpty() && ("ASC".equalsIgnoreCase(orderType) || "DESC".equalsIgnoreCase(orderType))) {
			IAggregationFunction function = AggregationFunctions.get(jsonObject.optString("funct"));
			String orderColumn = (String) jsonObject.opt("orderColumn");

			Projection projection;
			if (orderColumn != null && !orderColumn.isEmpty() && !orderType.isEmpty()) {
				String alias = jsonObject.optString("alias");
				projection = new Projection(function, dataSet, orderColumn, alias);
			} else {
				String columnName = getColumnName(jsonObject, columnAliasToName);
				projection = new Projection(function, dataSet, columnName, orderColumn);
			}

			boolean isAscending = "ASC".equalsIgnoreCase(orderType);

			sorting = new Sorting(projection, isAscending);
		}

		return sorting;
	}

	protected List<Filter> getFilters(String datasetLabel, JSONObject selectionsObject, Map<String, String> columnAliasToColumnName) throws JSONException {
		List<Filter> filters = new ArrayList<>(0);

		if (selectionsObject.has(datasetLabel)) {
			JSONObject datasetSelectionObject = selectionsObject.getJSONObject(datasetLabel);
			Iterator<String> it = datasetSelectionObject.keys();

			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(datasetLabel);

			boolean isAnEmptySelection = false;

			while (!isAnEmptySelection && it.hasNext()) {
				String columnsString = it.next();

				JSONArray valuesJsonArray = datasetSelectionObject.getJSONArray(columnsString);
				if (valuesJsonArray.length() == 0) {
					isAnEmptySelection = true;
					break;
				}

				List<String> columnsList = getColumnList(columnsString, dataSet, columnAliasToColumnName);
				List<Projection> projections = new ArrayList<>(columnsList.size());
				for (String columnName : columnsList) {
					projections.add(new Projection(dataSet, columnName));
				}

				List<Object> valueObjects = new ArrayList<>(0);
				for (int i = 0; i < valuesJsonArray.length(); i++) {
					String[] valuesArray = StringUtilities.splitBetween(valuesJsonArray.getString(i), "'", "','", "'");
					for (int j = 0; j < valuesArray.length; j++) {
						Projection projection = projections.get(j % projections.size());
						valueObjects.add(DataSetUtilities.getValue(valuesArray[j], projection.getType()));
					}
				}

				MultipleProjectionSimpleFilter inFilter = new InFilter(projections, valueObjects);
				filters.add(inFilter);
			}

			if (isAnEmptySelection) {
				filters.clear();
				filters.add(new UnsatisfiedFilter());
			}
		}

		return filters;
	}

	protected List<SimpleFilter> getLikeFilters(String datasetLabel, JSONObject likeSelectionsObject, Map<String, String> columnAliasToColumnName)
			throws JSONException {
		List<SimpleFilter> likeFilters = new ArrayList<>(0);

		if (likeSelectionsObject.has(datasetLabel)) {
			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(datasetLabel);
			boolean isAnEmptySelection = false;

			JSONObject datasetSelectionObject = likeSelectionsObject.getJSONObject(datasetLabel);
			Iterator<String> it = datasetSelectionObject.keys();
			while (!isAnEmptySelection && it.hasNext()) {
				String columns = it.next();
				String value = datasetSelectionObject.getString(columns);
				if (value == null || value.isEmpty()) {
					isAnEmptySelection = true;
					break;
				}
				String pattern = "%" + value + "%";

				List<String> columnsList = getColumnList(columns, dataSet, columnAliasToColumnName);
				List<Projection> projections = new ArrayList<>(columnsList.size());
				for (String columnName : columnsList) {
					projections.add(new Projection(dataSet, columnName));
				}

				for (Projection projection : projections) {
					SimpleFilter filter = new LikeFilter(projection, pattern);
					likeFilters.add(filter);
				}
			}

			if (isAnEmptySelection) {
				likeFilters.clear();
				likeFilters.add(new UnsatisfiedFilter());
			}
		}

		return likeFilters;
	}

	protected List<String> getColumnList(String columns, IDataSet dataSet, Map<String, String> columnAliasToColumnName) {
		List<String> columnList = new ArrayList<>(Arrays.asList(columns.trim().split("\\s*,\\s*"))); // trim spaces while splitting

		// transform QBE columns
		for (int i = 0; i < columnList.size(); i++) {
			String column = columnList.get(i);
			if (column.contains(":")) {
				columnList.set(i, getDatasetManagementAPI().getQbeDataSetColumn(dataSet, column));
			}
		}

		// transform aliases
		if (columnAliasToColumnName != null) {
			Set<String> aliases = columnAliasToColumnName.keySet();
			if (aliases.size() > 0) {
				for (int i = 0; i < columnList.size(); i++) {
					String column = columnList.get(i);
					if (aliases.contains(column)) {
						columnList.set(i, columnAliasToColumnName.get(column));
					}
				}
			}
		}

		return columnList;
	}

	protected IDataWriter getDataSetWriter() throws JSONException {
		JSONDataWriter dataWriter = new JSONDataWriter(getDataSetWriterProperties());
		dataWriter.setLocale(buildLocaleFromSession());
		return dataWriter;
	}

	protected void loadColumnAliasToName(JSONArray jsonArray, Map<String, String> columnAliasToName) throws JSONException {
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject category = jsonArray.getJSONObject(i);
			String alias = category.optString("alias");
			if (alias != null && !alias.isEmpty()) {
				String id = category.optString("id");
				if (id != null && !id.isEmpty()) {
					columnAliasToName.put(alias, id);
				}

				String columnName = category.optString("columnName");
				if (columnName != null && !columnName.isEmpty()) {
					columnAliasToName.put(alias, columnName);
				}
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
				if (((UserProfile) profile).getUserId().toString().equals(datasetJSON.get("owner"))) {
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

	protected IDataSource getDataSource(IDataSet dataSet, boolean isNearRealTime) {
		DatasetEvaluationStrategy strategy = dataSet.getEvaluationStrategy(isNearRealTime);
		IDataSource dataSource = null;

		if (strategy == DatasetEvaluationStrategy.PERSISTED) {
			dataSource = dataSet.getDataSourceForWriting();
		} else if (strategy == DatasetEvaluationStrategy.FLAT || strategy == DatasetEvaluationStrategy.INLINE_VIEW) {
			try {
				dataSource = dataSet.getDataSource();
			} catch (UnreachableCodeException e) {
			}
		} else {
			dataSource = SpagoBICacheConfiguration.getInstance().getCacheDataSource();
		}

		return dataSource;
	}
}
