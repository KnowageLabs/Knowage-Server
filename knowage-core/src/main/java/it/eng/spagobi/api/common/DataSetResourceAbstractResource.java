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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.container.ObjectUtils;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.cache.FilterCriteria;
import it.eng.spagobi.tools.dataset.cache.GroupCriteria;
import it.eng.spagobi.tools.dataset.cache.Operand;
import it.eng.spagobi.tools.dataset.cache.ProjectionCriteria;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datawriter.JSONDataWriter;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.json.JSONUtils;

public abstract class DataSetResourceAbstractResource extends AbstractSpagoBIResource {

	static protected Logger logger = Logger.getLogger(DataSetResourceAbstractResource.class);

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
			String summaryRow, int offset, int fetchSize, boolean isRealtime) {
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
			// if (selections != null && !selections.equals("")) {
			// JSONObject selectionsObject = new JSONObject(selections);
			// // in same case object is empty '{}'
			// if (selectionsObject.names() != null) {
			// filterCriteria = getFilterCriteria(label, selectionsObject, false, columnAliasToName);
			// filterCriteriaForMetaModel = getFilterCriteria(label, selectionsObject, true, columnAliasToName);
			// }
			// }

			List<FilterCriteria> havingCriteria = new ArrayList<FilterCriteria>();
			List<FilterCriteria> havingCriteriaForMetaModel = new ArrayList<FilterCriteria>();
			if (likeSelections != null && !likeSelections.equals("")) {
				JSONObject likeSelectionsObject = new JSONObject(likeSelections);
				if (likeSelectionsObject.names() != null) {
					filterCriteria.addAll(getLikeFilterCriteria(label, likeSelectionsObject, false, columnAliasToName, projectionCriteria, true));
					havingCriteria.addAll(getLikeFilterCriteria(label, likeSelectionsObject, false, columnAliasToName, projectionCriteria, false));

					filterCriteriaForMetaModel.addAll(getLikeFilterCriteria(label, likeSelectionsObject, true, columnAliasToName, projectionCriteria, true));
					havingCriteriaForMetaModel.addAll(getLikeFilterCriteria(label, likeSelectionsObject, true, columnAliasToName, projectionCriteria, false));
				}
			}

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

					// check if max or min filters are used and caclulate it
					filterCriteria = getDatasetManagementAPI().calculateMinMaxFilter(label, parameters, selections, likeSelections, maxRowCount, aggregations,
							summaryRow, offset, fetchSize, false, groupCriteria, filterCriteriaForMetaModel, summaryRowProjectionCriteria, havingCriteria,
							havingCriteriaForMetaModel, filterCriteriaForMetaModel, projectionCriteria);
				}

			}

			IDataStore dataStore = getDatasetManagementAPI().getDataStore(label, offset, fetchSize, maxRowCount, isRealtime,
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

	protected List<FilterCriteria> getLikeFilterCriteria(String datasetLabel, JSONObject likeSelectionsObject, boolean isRealtime,
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

}
