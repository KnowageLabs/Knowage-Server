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
import java.util.Set;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tools.dataset.associativity.AbstractAssociativityManager;
import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.graph.associativity.AssociativeDatasetContainer;
import it.eng.spagobi.tools.dataset.graph.associativity.Config;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class OuterAssociativityManager extends AbstractAssociativityManager {

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
						if (!edges.isEmpty()) {
							EdgeGroup group = AssociativeLogicUtils.getOrCreate(result.getEdgeGroupValues().keySet(), new EdgeGroup(edges));
							result.getDatasetToEdgeGroup().get(v1).add(group);

							if (!documentsAndExcludedDatasets.contains(v1)) {
								container.addGroup(group);

								if (!result.getEdgeGroupValues().containsKey(group)) {
									result.getEdgeGroupValues().put(group, new HashSet<String>());
								}

								if (!result.getEdgeGroupToDataset().containsKey(group)) {
									result.getEdgeGroupToDataset().put(group, new HashSet<String>());
									result.getEdgeGroupToDataset().get(group).add(v1);
								} else {
									result.getEdgeGroupToDataset().get(group).add(v1);
								}
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
	protected void calculateDatasets(String dataset, EdgeGroup fromEdgeGroup, String filter) throws Exception {
		Assert.assertTrue(!documentsAndExcludedDatasets.contains(dataset), "Dataset [" + dataset + "] cannot be processed.");

		// clean containers and groups -> set to unresolved
		AssociativeLogicUtils.unresolveDatasetContainers(associativeDatasetContainers.values());
		resetValues();

		Set<String> totalChildren = new HashSet<>();

		AssociativeDatasetContainer container = associativeDatasetContainers.get(dataset);
		container.addFilter(filter);

		// 1. Per ogni gruppo associativo del dataset primario
		Iterator<EdgeGroup> iterator = container.getGroups().iterator();
		while (iterator.hasNext()) {
			EdgeGroup group = iterator.next();

			// a. Calcolo i valori distinct
			String columnNames = getColumnNames(group.getOrderedEdgeNames(), dataset);
			if (columnNames.length() <= 0) {
				throw new SpagoBIException("Impossible to obtain column names for association " + group);
			}
			String query = container.buildQuery(columnNames);
			Set<String> distinctValues = container.getTupleOfValues(query);

			// b. Li imposto come unici valori ammissibili per quel gruppo associativo
			group.addValues(distinctValues);
			result.getEdgeGroupValues().get(group).addAll(distinctValues);

			// c. Rimuovo tale gruppo associativo tra quelli da filtrare per il dataset primario.
			// Tale gruppo associativo è di fatto un’associazione in uscita
			// container.removeGroup(group);
			// iterator.remove();

			// d. Per ogni dataset coinvolto in questo gruppo associativo, lo inserisco tra quelli filtri tranne il dataset primario
			Set<String> children = result.getEdgeGroupToDataset().get(group);
			// children.remove(dataset);
			for (String child : children) {
				if (!documentsAndExcludedDatasets.contains(child)) {
					AssociativeDatasetContainer childContainer = associativeDatasetContainers.get(child);
					String columns = getColumnNames(group.getOrderedEdgeNames(), child);
					if (!columns.isEmpty()) {
						childContainer.addFilter(columns, distinctValues);
					}
				}
			}
			totalChildren.addAll(children);

			// e. Imposto tutti i children come processati
			// f. Dichiaro il dataset come risolto
			resolveDatasets(children);

			// f. Dichiaro tale gruppo associativo come risolto
			group.resolve();
		}

		while (!getUnresolvedGroups(totalChildren).isEmpty()) {

			// 3. Calcolo tutti i gruppi associativi non risolti relativi ai soli dataset contenuti in totalChildren
			Set<EdgeGroup> groups = getUnresolvedGroups(totalChildren);
			totalChildren.clear();

			// 4. Per ogni gruppo associativo cosi precedentemente calcolato
			iterator = groups.iterator();
			while (iterator.hasNext()) {
				EdgeGroup group = iterator.next();

				for (String childDataset : result.getEdgeGroupToDataset().get(group)) {
					container = associativeDatasetContainers.get(childDataset);
					if (container.isResolved()) {

						// i. calcolo i valori distinct del gruppo associativo
						String columnNames = getColumnNames(group.getOrderedEdgeNames(), childDataset);
						if (!columnNames.isEmpty()) {
							String query = container.buildQuery(columnNames);
							Set<String> distinctValues = container.getTupleOfValues(query);

							// ii. aggiungo tali valori tra quelli ammissibili per quel gruppo associativo
							group.addValues(distinctValues);
							result.getEdgeGroupValues().get(group).addAll(distinctValues);
						}

						// iii. Rimuovo tale gruppo associativo tra quelli da filtrare per il dataset. Tale gruppo associativo è di fatto un’associazione in
						// uscita
						// container.removeGroup(group);
					}
				}
				for (String childDataset : result.getEdgeGroupToDataset().get(group)) {
					container = associativeDatasetContainers.get(childDataset);
					if (!container.isResolved()) {

						String columnNames = getColumnNames(group.getOrderedEdgeNames(), childDataset);
						if (!columnNames.isEmpty()) {
							container.addFilter(columnNames, group.getValues());
						}
						totalChildren.add(childDataset);
					}
				}
				group.resolve();
			}

			// 5. Terminati i gruppi associativi, imposto tutti i dataset trattati precedentemente come processati
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
