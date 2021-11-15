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

package it.eng.spagobi.tools.dataset.associativity.strategy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.associativity.AbstractAssociativityManager;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.ParametricLabeledEdge;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.container.IAssociativeDatasetContainer;
import it.eng.spagobi.tools.dataset.graph.associativity.exceptions.IllegalEdgeGroupException;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
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
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.parameters.ParametersUtilities;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class OuterAssociativityManager extends AbstractAssociativityManager {

	static protected Logger logger = Logger.getLogger(OuterAssociativityManager.class);

	public OuterAssociativityManager(Config config, UserProfile userProfile) throws Exception {
		init(config, userProfile);
	}

	@Override
	protected void initProcess() {
		try {
			for (String v1 : graph.vertexSet()) {
				IAssociativeDatasetContainer container = associativeDatasetContainers.get(v1);
				result.getDatasetToEdgeGroup().put(v1, new HashSet<EdgeGroup>());
				for (String v2 : graph.vertexSet()) {
					if (!v1.equals(v2)) {
						Set<LabeledEdge<String>> edges = graph.getAllEdges(v1, v2);
						Set<LabeledEdge<String>> edgesWithoutParameters = new HashSet<>(edges);
						if (!edges.isEmpty()) {
							for (LabeledEdge<String> edge : edges) {
								List<String> columnNames = getColumnNames(edge.getLabel(), v1);
								columnNames.addAll(getColumnNames(edge.getLabel(), v2));
								if (ParametersUtilities.containsParameter(columnNames)) {
									addEdgeGroup(v1, new ParametricLabeledEdge<String>(edge), container);
									edgesWithoutParameters.remove(edge);
								}
							}
							if (!edgesWithoutParameters.isEmpty()) {
								addEdgeGroup(v1, edgesWithoutParameters, container);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error during the initializing of the AssociativeLogicManager", e);
		}
	}

	@Override
	protected void calculateDatasets(String dataset, EdgeGroup fromEdgeGroup, SimpleFilter filter) throws Exception {
		Assert.assertTrue(!documentsAndExcludedDatasets.contains(dataset), "Dataset [" + dataset + "] cannot be processed.");

		logger.debug("Clean containers and groups -> set to unresolved");
		AssociativeLogicUtils.unresolveDatasetContainers(associativeDatasetContainers.values());

		Set<String> totalChildren = new HashSet<>();

		IAssociativeDatasetContainer container = associativeDatasetContainers.get(dataset);

		if (datasetFilters.get(dataset) != null) {

			List<SimpleFilter> filtersList = this.calculateMinMaxFilters(datasetFilters.get(dataset), datasetFilters.get(dataset), userProfile);

			for (SimpleFilter filterInDataset : filtersList) {

				container.addFilter(filterInDataset);
			}
		}
		if (container != null && filter != null) {
			// added calculation of min max filter

			container.addFilter(filter);
		}
//		logger.debug("1. For each associative group of the primary dataset " + container.getDataSet().getLabel() + "do the following:");
		Iterator<EdgeGroup> iterator = container.getGroups().iterator();
		while (iterator.hasNext()) {
			EdgeGroup group = iterator.next();

			List<String> columnNames = getColumnNames(group.getOrderedEdgeNames(), dataset);
			logger.debug("a. Calculate the distinct values for columns " + columnNames);
			Assert.assertTrue(!columnNames.isEmpty(), "Impossible to obtain column names for association " + group);
			if (ParametersUtilities.containsParameter(columnNames) && columnNames.size() != 1) {
				throw new IllegalEdgeGroupException("Columns " + columnNames
						+ " contain at least one parameter and more than one association. \nThis is a illegal state for an associative group.");
			}
			if (!ParametersUtilities.isParameter(columnNames.get(0))) {
				// clear non parameters previously computed values
				result.clearValues(group);

				Set<Tuple> distinctValues = container.getTupleOfValues(columnNames);
				if (!distinctValues.isEmpty()) {

					logger.debug("b. Setting distinct values " + distinctValues + " as the only compatible values for the associative group " + group);
					group.addValues(distinctValues);
					result.addValues(group, distinctValues);

					logger.debug("d. For each dataset involved in the current associative group, inserting it among the ones to be filtered");
					Set<String> children = result.getEdgeGroupToDataset().get(group);

					for (String child : children) {
						if (!documentsAndExcludedDatasets.contains(child)) {
							IAssociativeDatasetContainer childContainer = associativeDatasetContainers.get(child);
							List<String> columns = getColumnNames(group.getOrderedEdgeNames(), child);
							childContainer.update(group, columns, distinctValues);
						}
					}
					totalChildren.addAll(children);

					logger.debug("e. Setting all the children dataset as processed");
					logger.debug("f. Declaring the dataset as resolved");
					resolveDatasets(children);
				}
			}

			logger.debug("f. Declaring the associative group as resolved");
			group.resolve();
		}

		while (!getUnresolvedGroups(totalChildren).isEmpty()) {

			logger.debug("3. Calculating all the unresolved associative groups related only to dataset contained in " + totalChildren);
			Set<EdgeGroup> groups = getUnresolvedGroups(totalChildren);
			totalChildren.clear();

			logger.debug("4. For each associative group previously calculated:");
			iterator = groups.iterator();
			while (iterator.hasNext()) {
				EdgeGroup group = iterator.next();

				for (String childDataset : result.getDatasets(group)) {
					container = associativeDatasetContainers.get(childDataset);
					if (container.isResolved()) {

						logger.debug("i. Calculating distinct values for the associative group " + group + ", orderedEdgeNames " + group.getOrderedEdgeNames()
								+ " in dataset " + childDataset);
						List<String> columnNames = getColumnNames(group.getOrderedEdgeNames(), childDataset);
						Assert.assertTrue(!columnNames.isEmpty(), "Impossible to obtain column names for association " + group);
						if (ParametersUtilities.containsParameter(columnNames) && columnNames.size() != 1) {
							throw new IllegalEdgeGroupException("Columns " + columnNames
									+ " contain at least one parameter and more than one association. \nThis is a illegal state for an associative group.");
						}

						if (!ParametersUtilities.isParameter(columnNames.get(0))) {
							Set<Tuple> distinctValues = container.getTupleOfValues(columnNames);
							if (!distinctValues.isEmpty()) {
								if (columnNames != null && !columnNames.isEmpty()) {
									for (String column : columnNames) {
										logger.debug("Columns involved: " + column);
									}
								}
								boolean exitNull = false;
								Iterator<Tuple> ite = distinctValues.iterator();
								while (ite.hasNext()) {
									Tuple tup = ite.next();
									if (tup != null && tup.getValues() != null) {
										for (int i = 0; i < tup.getValues().size(); i++) {
											if (tup.get(i) == null) {
												exitNull = true;
											}
										}

									}
								}
								if (exitNull)
									continue;
								logger.debug("ii-b. Adding values " + distinctValues + " among the compatible ones for the current associative group");
								group.addValues(distinctValues);
								result.addValues(group, distinctValues);
							}
						}
					}
				}
				for (String childDataset : result.getDatasets(group)) {
					container = associativeDatasetContainers.get(childDataset);
					if (!container.isResolved() && result.getEdgeGroupValues().keySet().contains(group)) {
						List<String> columnNames = getColumnNames(group.getOrderedEdgeNames(), childDataset);
						container.update(group, columnNames, group.getValues());
						totalChildren.add(childDataset);
					}
				}
				group.resolve();
			}

			logger.debug("5. Finishing to work on associative groups. Setting all the processed datasets " + totalChildren + " as resolved");
			resolveDatasets(totalChildren);
		}
	}

	private Set<EdgeGroup> getUnresolvedGroups(Set<String> totalChildren) {
		Set<EdgeGroup> groups = new HashSet<>();
		for (String child : totalChildren) {
			if (!documentsAndExcludedDatasets.contains(child)) {
				groups.addAll(associativeDatasetContainers.get(child).getUnresolvedGroups());
			}
		}
		return groups;
	}

	private void resolveDatasets(Set<String> datasets) {
		for (String dataset : datasets) {
			if (!documentsAndExcludedDatasets.contains(dataset)) {
				resolve(dataset);
			}
		}
	}

	private void resolve(String dataset) {
		associativeDatasetContainers.get(dataset).resolve();
	}

	// FIXME
	public List<SimpleFilter> calculateMinMaxFilters(List<SimpleFilter> filters, List<SimpleFilter> likeFilters, UserProfile userprofile) throws JSONException {

		logger.debug("IN");

		List<SimpleFilter> newFilters = new ArrayList<>(filters);

		List<Integer> minMaxFilterIndexes = new ArrayList<>();
		List<Projection> minMaxProjections = new ArrayList<>();

		List<Filter> noMinMaxFilters = new ArrayList<>();

		for (int i = 0; i < filters.size(); i++) {
			Filter filter = filters.get(i);
			if (filter instanceof SimpleFilter) {
				SimpleFilter simpleFilter = (SimpleFilter) filter;
				SimpleFilterOperator operator = simpleFilter.getOperator();

				if (SimpleFilterOperator.EQUALS_TO_MIN.equals(operator)) {

					logger.debug("Min filter found at index [" + i + "]");
					minMaxFilterIndexes.add(i);

					String columnName = ((SingleProjectionSimpleFilter) filter).getProjection().getName();
					Projection projection = new Projection(AggregationFunctions.MIN_FUNCTION, ((SimpleFilter) filter).getDataset(), columnName);
					minMaxProjections.add(projection);

				} else if (SimpleFilterOperator.EQUALS_TO_MAX.equals(operator)) {

					logger.debug("Max filter found at index [" + i + "]");
					minMaxFilterIndexes.add(i);

					String columnName = ((SingleProjectionSimpleFilter) filter).getProjection().getName();
					Projection projection = new Projection(AggregationFunctions.MAX_FUNCTION, ((SimpleFilter) filter).getDataset(), columnName, columnName);
					minMaxProjections.add(projection);

				} else {
					noMinMaxFilters.add(filter);
				}
			} else {
				noMinMaxFilters.add(filter);
			}
		}

		if (minMaxFilterIndexes.size() > 0) {
			logger.debug("MIN/MAX filter found");

			logger.debug("MIN/MAX filter values calculated");

			for (int i = 0; i < minMaxProjections.size(); i++) {
				Projection projection = minMaxProjections.get(i);
				String alias = projection.getAlias();
				String errorMessage = "MIN/MAX value for field [" + alias + "] not found";

				int index = minMaxFilterIndexes.get(i);
				List<SimpleFilter> likeFiltersArray = new ArrayList();

				Filter where = getWhereFilter(noMinMaxFilters, likeFiltersArray);

				IDataStore dataStore = getSummaryRowDataStore(projection.getDataset(), true, datasetParameters.get(projection.getDataset().getLabel()),
						projection, where, -1, userprofile);
				if (dataStore == null) {
					String errorMessage2 = "Error in getting min and max filters values";
					logger.error(errorMessage2);
					throw new SpagoBIRuntimeException(errorMessage2);
				}
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

	private IDataStore getSummaryRowDataStore(IDataSet dataSet, boolean isNearRealtime, Map<String, String> parametersValues, Projection projections,
			Filter filter, int maxRowCount, UserProfile userprofile) throws JSONException {
		dataSet.setParametersMap(parametersValues);
		dataSet.resolveParameters();

		List<AbstractSelectionField> listProj = new ArrayList<AbstractSelectionField>();
		listProj.add(projections);

		IDatasetEvaluationStrategy strategy = DatasetEvaluationStrategyFactory.get(dataSet.getEvaluationStrategy(isNearRealtime), dataSet, userprofile);
		return strategy.executeSummaryRowQuery(listProj, filter, maxRowCount);
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

}
