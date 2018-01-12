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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.associativity.AbstractAssociativityManager;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.Tuple;
import it.eng.spagobi.tools.dataset.graph.associativity.AssociativeDatasetContainer;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.exceptions.IllegalEdgeGroupException;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
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
				AssociativeDatasetContainer container = associativeDatasetContainers.get(v1);
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
									addEdgeGroup(v1, edge, container);
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
		resetValues();

		Set<String> totalChildren = new HashSet<>();

		AssociativeDatasetContainer container = associativeDatasetContainers.get(dataset);
		container.addFilter(filter);

		logger.debug("1. For each associative group of the primary dataset " + container.getDataSet().getLabel() + "do the following:");
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
			Set<Tuple> distinctValues = ParametersUtilities.isParameter(columnNames.get(0)) ? container.getTupleOfValues(columnNames.get(0))
					: container.getTupleOfValues(columnNames);

			logger.debug("b. Setting distinct values " + distinctValues + " as the only compatible values for the associative group " + group);
			group.addValues(distinctValues);
			result.getEdgeGroupValues().get(group).addAll(distinctValues);

			// logger.debug("c. Removing the previous associative group among the ones to be filtered for the primary dataset. Such group is indeed an outgoing
			// association");
			// container.removeGroup(group);
			// iterator.remove();

			logger.debug("d. For each dataset involved in the current associative group, inserting it among the ones to be filtered");
			Set<String> children = result.getEdgeGroupToDataset().get(group);
			// children.remove(dataset); // Do I need to keep the primary dataset?
			for (String child : children) {
				if (!documentsAndExcludedDatasets.contains(child)) {
					AssociativeDatasetContainer childContainer = associativeDatasetContainers.get(child);
					List<String> columns = getColumnNames(group.getOrderedEdgeNames(), child);
					childContainer.update(columns, distinctValues);
				}
			}
			totalChildren.addAll(children);

			logger.debug("e. Setting all the children dataset as processed");
			logger.debug("f. Declaring the dataset as resolved");
			resolveDatasets(children);

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

				for (String childDataset : result.getEdgeGroupToDataset().get(group)) {
					container = associativeDatasetContainers.get(childDataset);
					if (container.isResolved()) {

						logger.debug("i. Calculating distinct values for the associative group " + group + " in dataset " + childDataset);
						List<String> columnNames = getColumnNames(group.getOrderedEdgeNames(), childDataset);
						Assert.assertTrue(!columnNames.isEmpty(), "Impossible to obtain column names for association " + group);
						if (ParametersUtilities.containsParameter(columnNames) && columnNames.size() != 1) {
							throw new IllegalEdgeGroupException("Columns " + columnNames
									+ " contain at least one parameter and more than one association. \nThis is a illegal state for an associative group.");
						}
						Set<Tuple> distinctValues = ParametersUtilities.isParameter(columnNames.get(0)) ? container.getTupleOfValues(columnNames.get(0))
								: container.getTupleOfValues(columnNames);

						logger.debug("ii-b. Adding values " + distinctValues + " among the compatible ones for the current associative group");
						group.addValues(distinctValues);
						result.getEdgeGroupValues().get(group).addAll(distinctValues);

						// logger.debug("iii. Removing the previous associative group among the ones to be filtered for the primary dataset. Such group is
						// indeed an outgoing association");
						// container.removeGroup(group);
					}
				}
				for (String childDataset : result.getEdgeGroupToDataset().get(group)) {
					container = associativeDatasetContainers.get(childDataset);
					if (!container.isResolved()) {
						List<String> columnNames = getColumnNames(group.getOrderedEdgeNames(), childDataset);
						container.update(columnNames, group.getValues());
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

	private void resetValues() {
		for (EdgeGroup group : result.getEdgeGroupValues().keySet()) {
			result.getEdgeGroupValues().get(group).clear();
		}
	}
}
