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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.jgrapht.graph.Pseudograph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.api.common.AbstractDataSetResource;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ConfigurationConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.services.serialization.JsonConverter;
import it.eng.spagobi.tools.dataset.associativity.IAssociativityManager;
import it.eng.spagobi.tools.dataset.associativity.strategy.AssociativeStrategyFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroupJSONSerializer;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.graph.AssociationAnalyzer;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.InFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.NullaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.OrFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilterOperator;
import it.eng.spagobi.tools.dataset.metasql.query.item.SingleProjectionSimpleFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnsatisfiedFilter;
import it.eng.spagobi.tools.dataset.strategy.DatasetEvaluationStrategyFactory;
import it.eng.spagobi.tools.dataset.strategy.IDatasetEvaluationStrategy;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceParameterException;
import it.eng.spagobi.utilities.sql.SqlUtils;

@Path("/2.0/associativeSelections")
public class AssociativeSelectionsResource extends AbstractDataSetResource {

	static protected Logger logger = Logger.getLogger(AssociativeSelectionsResource.class);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@UserConstraint(functionalities = { SpagoBIConstants.SELF_SERVICE_DATASET_MANAGEMENT })
	public String getAssociativeSelections(String body) {
		logger.debug("IN");
		UserProfile userprofile = getUserProfile();
		Monitor start = MonitorFactory.start("Knowage.AssociativeSelectionsResource.getAssociativeSelections:total");

		String associationGroupString = null;
		String selectionsString = null;
		String datasetsString = null;
		String nearRealtimeDatasetsString = null;
		String filtersString = null;
		try {
			if (StringUtilities.isNotEmpty(body)) {
				JSONObject jsonBody = new JSONObject(body);

				JSONObject jsonAssociationGroup = jsonBody.optJSONObject("associationGroup");
				associationGroupString = jsonAssociationGroup != null ? jsonAssociationGroup.toString() : null;

				JSONObject jsonSelections = jsonBody.optJSONObject("selections");
				selectionsString = jsonSelections != null ? jsonSelections.toString() : null;

				JSONObject jsonDatasets = jsonBody.optJSONObject("datasets");
				datasetsString = jsonDatasets != null ? jsonDatasets.toString() : null;

				JSONArray jsonNearRealtime = jsonBody.optJSONArray("nearRealtime");
				nearRealtimeDatasetsString = jsonNearRealtime != null ? jsonNearRealtime.toString() : null;

				JSONArray jsonFilters = jsonBody.optJSONArray("filters");
				filtersString = jsonFilters != null ? jsonFilters.toString() : null;
			}

			IDataSetDAO dataSetDAO = getDataSetDAO();
			dataSetDAO.setUserProfile(getUserProfile());

			// parse selections
			if (selectionsString == null || selectionsString.isEmpty()) {
				throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [selections] cannot be null or empty");
			}

			JSONObject selectionsObject = new JSONObject(selectionsString);

			// parse association group
			if (associationGroupString == null) {
				throw new SpagoBIServiceParameterException(this.request.getPathInfo(), "Query parameter [associationGroup] cannot be null");
			}

			AssociationGroupJSONSerializer serializer = new AssociationGroupJSONSerializer();

			JSONObject associationGroupObject = new JSONObject(associationGroupString);
			AssociationGroup associationGroup = serializer.deserialize(associationGroupObject);
			fixAssociationGroup(associationGroup); // fixes qbe dataset columns

			// parse documents
			Set<String> documents = new HashSet<>();
			JSONArray associations = associationGroupObject.optJSONArray("associations");
			if (associations != null) {
				for (int associationIndex = 0; associationIndex < associations.length(); associationIndex++) {
					JSONObject association = associations.getJSONObject(associationIndex);
					JSONArray fields = association.optJSONArray("fields");
					if (fields != null) {
						for (int fieldIndex = fields.length() - 1; fieldIndex >= 0; fieldIndex--) {
							JSONObject field = fields.getJSONObject(fieldIndex);
							String type = field.optString("type");
							if ("document".equalsIgnoreCase(type)) {
								String store = field.optString("store");
								documents.add(store);
							}
						}
					}
				}
			}

			// parse dataset parameters
			Map<String, Map<String, String>> datasetParameters = new HashMap<>();
			if (datasetsString != null && !datasetsString.isEmpty()) {
				JSONObject datasetsObject = new JSONObject(datasetsString);
				Iterator<String> datasetsIterator = datasetsObject.keys();
				while (datasetsIterator.hasNext()) {
					String datasetLabel = datasetsIterator.next();

					Map<String, String> parameters = new HashMap<>();
					datasetParameters.put(datasetLabel, parameters);

					JSONObject datasetObject = datasetsObject.getJSONObject(datasetLabel);
					Iterator<String> datasetIterator = datasetObject.keys();
					while (datasetIterator.hasNext()) {
						String param = datasetIterator.next();
						String value = datasetObject.getString(param);
						parameters.put(param, value);
					}
				}
			}

			// parse near realtime datasets
			Set<String> nearRealtimeDatasets = new HashSet<>();
			if (nearRealtimeDatasetsString != null && !nearRealtimeDatasetsString.isEmpty()) {
				JSONArray jsonArray = new JSONArray(nearRealtimeDatasetsString);
				for (int i = 0; i < jsonArray.length(); i++) {
					nearRealtimeDatasets.add(jsonArray.getString(i));
				}
			}

			AssociationAnalyzer analyzer = new AssociationAnalyzer(associationGroup.getAssociations());
			analyzer.process();
			Map<String, Map<String, String>> datasetToAssociationToColumnMap = analyzer.getDatasetToAssociationToColumnMap();
			Pseudograph<String, LabeledEdge<String>> graph = analyzer.getGraph();

			DataSetResource dataRes = new DataSetResource();
			List<SimpleFilter> filtersList = new ArrayList<SimpleFilter>();
			IDataSet dataSetInFilter = null;
			if (filtersString != null && !filtersString.isEmpty()) {
				JSONArray jsonArray = new JSONArray(filtersString);
				JSONArray firstFilterValues = null;
				if (jsonArray instanceof JSONArray) {
					for (int i = 0; i < jsonArray.length(); i++) { // looking for filter embedded in array
						JSONObject filterJsonObject = jsonArray.optJSONObject(i);
						if (filterJsonObject != null && filterJsonObject.has("filterOperator")) {
							String label = filterJsonObject.optString("dataset");
							if (filterJsonObject.optString("dataset") != null) {
								try {
									JSONObject obj = filterJsonObject.getJSONObject("dataset");
									if (obj instanceof JSONObject) {
										label = obj.getString("label");
									}
								} catch (Exception e) {
									// continue
								}
							}
							dataSetInFilter = getDataSetDAO().loadDataSetByLabel(label);
							filterJsonObject.optString("filterOperator");
							firstFilterValues = filterJsonObject.getJSONArray("filterVals");
							SimpleFilter firstSimpleFilter = dataRes.getFilter(filterJsonObject.optString("filterOperator"), firstFilterValues,
									filterJsonObject.optString("colName"), dataSetInFilter, null);
							filtersList.add(firstSimpleFilter);
						}
					}
				}
			}

			// get datasets from selections
			List<SimpleFilter> selectionsFilters = new ArrayList<>();
			Map<String, Map<String, Set<Tuple>>> selectionsMap = new HashMap<>();

			Iterator<String> it = selectionsObject.keys();
			while (it.hasNext()) {
				String datasetDotColumn = it.next();
				Assert.assertTrue(datasetDotColumn.indexOf(".") >= 0, "Data not compliant with format <DATASET_LABEL>.<COLUMN> [" + datasetDotColumn + "]");
				String[] tmpDatasetAndColumn = datasetDotColumn.split("\\.");
				Assert.assertTrue(tmpDatasetAndColumn.length == 2, "Impossible to get both dataset label and column");

				String datasetLabel = tmpDatasetAndColumn[0];
				String column = SqlUtils.unQuote(tmpDatasetAndColumn[1]);

				Assert.assertNotNull(datasetLabel, "A dataset label in selections is null");
				Assert.assertTrue(!datasetLabel.isEmpty(), "A dataset label in selections is empty");
				Assert.assertNotNull(column, "A column for dataset [" + datasetLabel + "]  in selections is null");
				Assert.assertTrue(!column.isEmpty(), "A column for dataset [" + datasetLabel + "] in selections is empty");

				IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(datasetLabel);

				Projection projection = new Projection(dataSet, column);
				List<Object> valueObjects = new ArrayList<>();

				Object object = selectionsObject.get(datasetDotColumn);
				if (object instanceof JSONArray) {
					JSONArray jsonArray = (JSONArray) object;
					for (int i = 0; i < jsonArray.length(); i++) {
						Object valueForQuery = DataSetUtilities.getValue(jsonArray.get(i).toString(), projection.getType());
						valueObjects.add(valueForQuery);
					}
				} else {
					Object valueForQuery = DataSetUtilities.getValue(object.toString(), projection.getType());
					valueObjects.add(valueForQuery);
				}

				selectionsFilters.add(new InFilter(projection, valueObjects));

				if (!selectionsMap.containsKey(datasetLabel)) {
					selectionsMap.put(datasetLabel, new HashMap<String, Set<Tuple>>());
				}
				Map<String, Set<Tuple>> selection = selectionsMap.get(datasetLabel);
				if (!selection.containsKey(column)) {
					selection.put(column, new HashSet<Tuple>());
				}
				Set<Tuple> tupleSet = selection.get(column);
				for (Object value : valueObjects) {
					Tuple tuple = new Tuple();
					tuple.add(value);
					tupleSet.add(tuple);
				}
			}

			logger.debug("Selections list: " + selectionsFilters);

			Map<String, String> datasetSelectionParameters = selectionsFilters.get(selectionsFilters.size() - 1).getDataset().getParamsMap();

			if (datasetSelectionParameters == null || datasetSelectionParameters.isEmpty()) {

				datasetSelectionParameters = datasetParameters.get(selectionsFilters.get(selectionsFilters.size() - 1).getDataset().getLabel());
			}
			filtersList = this.calculateMinMaxFilters(selectionsFilters.get(selectionsFilters.size() - 1).getDataset(), true, datasetSelectionParameters,
					filtersList, selectionsFilters, userprofile, associationGroup);

			String strategy = SingletonConfig.getInstance().getConfigValue(ConfigurationConstants.SPAGOBI_DATASET_ASSOCIATIVE_LOGIC_STRATEGY);
			Config config = AssociativeLogicUtils.buildConfig(strategy, graph, datasetToAssociationToColumnMap, selectionsFilters, filtersList,
					nearRealtimeDatasets, datasetParameters, documents);

			IAssociativityManager manager = AssociativeStrategyFactory.createStrategyInstance(config, getUserProfile());
			manager.process();

			Map<String, Map<String, Set<Tuple>>> selections = manager.getSelections();

			for (String d : selectionsMap.keySet()) {
				if (!selections.containsKey(d)) {
					selections.put(d, new HashMap<String, Set<Tuple>>());
				}
				Map<String, Set<Tuple>> calcSelections = selections.get(d);
				Map<String, Set<Tuple>> inputSelections = selectionsMap.get(d);
				for (String selectionKey : inputSelections.keySet()) {
					calcSelections.put(selectionKey, inputSelections.get(selectionKey));
				}
			}

			String stringFeed = JsonConverter.objectToJson(selections, Map.class);
			return stringFeed;
		} catch (Exception e) {
			String errorMessage = "An error occurred while getting associative selections";
			logger.error(errorMessage, e);
			throw new SpagoBIRestServiceException(errorMessage, buildLocaleFromSession(), e);
		} finally {
			start.stop();
			logger.debug("OUT");
		}
	}

	private void fixAssociationGroup(AssociationGroup associationGroup) {
		IDataSetDAO dataSetDAO = getDataSetDAO();
		Map<String, IMetaData> dataSetLabelToMedaData = new HashMap<>();
		for (Association association : associationGroup.getAssociations()) {
			if (association.getDescription().contains(".")) {
				for (Field field : association.getFields()) {
					String fieldName = field.getFieldName();
					if (fieldName.contains(":")) {
						String dataSetLabel = field.getLabel();
						IMetaData metadata = null;
						if (dataSetLabelToMedaData.containsKey(dataSetLabel)) {
							metadata = dataSetLabelToMedaData.get(dataSetLabel);
						} else {
							metadata = dataSetDAO.loadDataSetByLabel(dataSetLabel).getMetadata();
							dataSetLabelToMedaData.put(dataSetLabel, metadata);
						}
						for (int i = 0; i < metadata.getFieldCount(); i++) {
							IFieldMetaData fieldMeta = metadata.getFieldMeta(i);
							String alias = fieldMeta.getAlias();
							if (fieldMeta.getName().equals(fieldName)) {
								association.setDescription(association.getDescription().replace(dataSetLabel + "." + fieldName, dataSetLabel + "." + alias));
								field.setFieldName(alias);
								break;
							}
						}
					}
				}
			}
		}
	}

	// FIXME
	public List<SimpleFilter> calculateMinMaxFilters(IDataSet dataSet, boolean isNearRealtime, Map<String, String> parametersValues, List<SimpleFilter> filters,
			List<SimpleFilter> likeFilters, UserProfile userprofile, AssociationGroup associationGroup) throws JSONException {
		logger.debug("IN");
		List<SimpleFilter> newFilters = new ArrayList<>(filters);
		List<Integer> minMaxFilterIndexes = new ArrayList<>();
		List<AbstractSelectionField> minMaxProjections = new ArrayList<>();
		List<Filter> noMinMaxFilters = new ArrayList<>();
		for (int i = 0; i < filters.size(); i++) {
			Filter filter = filters.get(i);
			if (filter instanceof SimpleFilter) {
				SimpleFilter simpleFilter = (SimpleFilter) filter;
				SimpleFilterOperator operator = simpleFilter.getOperator();
				if (SimpleFilterOperator.EQUALS_TO_MIN.equals(operator)) {
					if (dataSet.getLabel().equals(((SimpleFilter) filter).getDataset().getLabel())) {
						logger.debug("Min filter found at index [" + i + "]");
						minMaxFilterIndexes.add(i);
						String columnName = ((SingleProjectionSimpleFilter) filter).getProjection().getName();
						Projection projection = new Projection(AggregationFunctions.MIN_FUNCTION, dataSet, columnName);
						minMaxProjections.add(projection);
					}
				} else if (SimpleFilterOperator.EQUALS_TO_MAX.equals(operator)) {
					if (dataSet.getLabel().equals(((SimpleFilter) filter).getDataset().getLabel())) {
						logger.debug("Max filter found at index [" + i + "]");
						minMaxFilterIndexes.add(i);
						String columnName = ((SingleProjectionSimpleFilter) filter).getProjection().getName();
						Projection projection = new Projection(AggregationFunctions.MAX_FUNCTION, dataSet, columnName, columnName);
						minMaxProjections.add(projection);
					}
				} else {
					noMinMaxFilters.add(filter);
				}
			} else {
				noMinMaxFilters.add(filter);
			}
		}

		if (minMaxFilterIndexes.size() > 0) {
			logger.debug("MIN/MAX filter found");
			List<SimpleFilter> filtersToUse = setRightColumnsFromAssociations(associationGroup, dataSet, likeFilters);
			Filter where = getWhereFilter(noMinMaxFilters, filtersToUse);
			IDataStore dataStore = getSummaryRowDataStore(dataSet, isNearRealtime, parametersValues, minMaxProjections, where, -1, userprofile);
			if (dataStore == null) {
				String errorMessage = "Error in getting min and max filters values";
				logger.error(errorMessage);
				throw new SpagoBIRuntimeException(errorMessage);
			}
			logger.debug("MIN/MAX filter values calculated");
			for (int i = 0; i < minMaxProjections.size(); i++) {
				Projection projection = (Projection) minMaxProjections.get(i);
				String alias = projection.getAlias();
				String errorMessage = "MIN/MAX value for field [" + alias + "] not found";
				int index = minMaxFilterIndexes.get(i);
				List values = dataStore.getFieldValues(i);
				if (values == null) {
					logger.error(errorMessage);
					throw new SpagoBIRuntimeException(errorMessage);
				} else {
					Projection projectionWithoutAggregation = new Projection(projection.getDataset(), projection.getName(), alias);
					if (values.isEmpty()) {
						logger.warn(errorMessage + ", put NULL");
						newFilters.set(index, new NullaryFilter(projectionWithoutAggregation, SimpleFilterOperator.IS_NULL));
					} else {
						Object value = values.get(0);
						logger.debug("MIN/MAX value for field [" + alias + "] is equal to [" + value + "]");
						newFilters.set(index, new UnaryFilter(projectionWithoutAggregation, SimpleFilterOperator.EQUALS_TO, value));
					}
				}
			}
		}
		logger.debug("OUT");
		return newFilters;
	}

	private IDataStore getSummaryRowDataStore(IDataSet dataSet, boolean isNearRealtime, Map<String, String> parametersValues,
			List<AbstractSelectionField> projections, Filter filter, int maxRowCount, UserProfile userprofile) throws JSONException {
		dataSet.setParametersMap(parametersValues);
		dataSet.resolveParameters();
		IDatasetEvaluationStrategy strategy = DatasetEvaluationStrategyFactory.get(dataSet.getEvaluationStrategy(isNearRealtime), dataSet, userprofile);
		return strategy.executeSummaryRowQuery(projections, filter, maxRowCount);
	}

	public Filter getWhereFilter(List<Filter> filters, List<SimpleFilter> likeFilters) {
		Filter where = null;
		if (filters.size() > 0) {
			if (filters.size() == 1 && filters.get(0) instanceof UnsatisfiedFilter) {
				where = filters.get(0);
			} else {
				AndFilter andFilter = new AndFilter(filters);
				if (likeFilters.size() > 0) {
					andFilter.and(new OrFilter(likeFilters));
				}
				where = andFilter;
			}
		} else if (likeFilters.size() > 0) {
			where = new OrFilter(likeFilters);
		}
		return where;
	}

	/*
	 * Method used for guarantee that filters use the same column names of dataset fields (with associations it could change)
	 */
	public List<SimpleFilter> setRightColumnsFromAssociations(AssociationGroup associationGroup, IDataSet dataSet, List<SimpleFilter> filters) {
		List<SimpleFilter> filtersToReturn = new ArrayList<SimpleFilter>();
		if (filters.size() > 0) {
			for (SimpleFilter simpleFilter : filters) {
				InFilter filter = (InFilter) simpleFilter;
				List<Projection> projectionsToAdd = new ArrayList<Projection>();
				for (Projection proj : filter.getProjections()) {
					String nameToAdd = findRightNameFromAssociation(proj, associationGroup, dataSet);
					if (nameToAdd != null && !nameToAdd.isEmpty()) {
						Projection projNew = new Projection(dataSet, nameToAdd);
						projectionsToAdd.add(projNew);
					}

				}
				if (projectionsToAdd.size() > 0) {
					InFilter filterToAdd = new InFilter(projectionsToAdd, filter.getOperands());
					filtersToReturn.add(filterToAdd);
				}
			}
		}
		return filtersToReturn;

	}

	public String findRightNameFromAssociation(Projection projection, AssociationGroup associationGroup, IDataSet newDataSet) {
		String newName = "";
		IDataSet dataset = projection.getDataset();
		for (Association association : associationGroup.getAssociations()) {
			Field field = association.getField(dataset.getName());
			if (projection.getName().equals(field.getFieldName())) {
				Field newfield = association.getField(newDataSet.getName());
				newName = newfield.getFieldName();
			}
		}
		return newName;
	}

}
