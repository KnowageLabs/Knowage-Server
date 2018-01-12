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

package it.eng.spagobi.tools.dataset.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Pseudograph;

import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.graph.associativity.utils.AssociativeLogicResult;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */

public class AssociationAnalyzer {
	private final Collection<Association> associations;
	private final Map<String, Map<String, String>> datasetToAssociationToColumnMap;
	private final Pseudograph<String, LabeledEdge<String>> graph;

	public AssociationAnalyzer(Collection<Association> associations) {
		this.associations = associations;
		this.datasetToAssociationToColumnMap = new HashMap<String, Map<String, String>>();
		this.graph = new Pseudograph<String, LabeledEdge<String>>(
				new ClassBasedEdgeFactory<String, LabeledEdge<String>>((Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
	}

	public void process() {

		for (Association association : associations) {
			String associationId = association.getId();
			List<Field> associationFields = association.getFields();

			for (Field field : associationFields) {
				if (field.hasDatasetType()) {
					String datasetLabel = field.getDataSetLabel();

					// add vertex to the graph
					if (!graph.containsVertex(datasetLabel)) {
						graph.addVertex(datasetLabel);
					}

					// add column of association to the map
					Map<String, String> associationToColumnMap = null;
					if (datasetToAssociationToColumnMap.containsKey(datasetLabel)) {
						associationToColumnMap = datasetToAssociationToColumnMap.get(datasetLabel);
					} else {
						associationToColumnMap = new HashMap<String, String>();
						datasetToAssociationToColumnMap.put(datasetLabel, associationToColumnMap);
					}
					associationToColumnMap.put(associationId, field.getFieldName());
				}
			}

			// add edges to the graph
			for (int i = 0; i < associationFields.size() - 1; i++) {
				Field sourceField = associationFields.get(i);
				if (sourceField.hasDatasetType()) {
					String source = sourceField.getDataSetLabel();
					for (int j = i + 1; j < associationFields.size(); j++) {
						Field targetField = associationFields.get(j);
						if (targetField.hasDatasetType()) {
							String target = targetField.getDataSetLabel();
							LabeledEdge<String> labeledEdge = new LabeledEdge<String>(source, target, associationId);
							graph.addEdge(source, target, labeledEdge);
						}
					}
				}
			}
		}
	}

	public Map<String, Map<String, String>> getDatasetToAssociationToColumnMap() {
		return datasetToAssociationToColumnMap;
	}

	public Pseudograph<String, LabeledEdge<String>> getGraph() {
		return graph;
	}

	public static Map<String, Map<String, Set<Tuple>>> getSelections(AssociationGroup associationGroup, Pseudograph<String, LabeledEdge<String>> graph,
			AssociativeLogicResult result) {

		Map<Set<String>, Set<Tuple>> associationsToValuesMap = new HashMap<>();
		for (EdgeGroup edgeGroup : result.getEdgeGroupValues().keySet()) {
			Set<String> associations = new TreeSet<String>();
			String associationString = edgeGroup.getOrderedEdgeNames();
			associations.addAll(Arrays.asList(associationString.split(",")));
			Set<Tuple> values = result.getEdgeGroupValues().get(edgeGroup);
			associationsToValuesMap.put(associations, values);
		}

		Map<String, Set<String>> datasetToAssociationsMap = new HashMap<String, Set<String>>();
		for (String dataset : graph.vertexSet()) {
			Set<String> associations = new TreeSet<String>();
			for (LabeledEdge<String> labeledEdge : graph.edgesOf(dataset)) {
				associations.add(labeledEdge.getLabel());
			}
			datasetToAssociationsMap.put(dataset, associations);
		}

		Map<String, Map<Set<String>, Set<Tuple>>> datasetToAssociationsToValuesMap = new HashMap<>();
		for (String dataset : datasetToAssociationsMap.keySet()) {
			Map<Set<String>, Set<Tuple>> currentAssociationsToValuesMap = null;
			if (datasetToAssociationsToValuesMap.containsKey(dataset)) {
				currentAssociationsToValuesMap = datasetToAssociationsToValuesMap.get(dataset);
			} else {
				currentAssociationsToValuesMap = new HashMap<>();
				datasetToAssociationsToValuesMap.put(dataset, currentAssociationsToValuesMap);
			}

			Set<String> graphAssociations = datasetToAssociationsMap.get(dataset);
			for (Set<String> newAssociations : associationsToValuesMap.keySet()) {
				if (graphAssociations.containsAll(newAssociations)) {
					Set<Tuple> values = associationsToValuesMap.get(newAssociations);
					if (currentAssociationsToValuesMap.isEmpty()) {
						currentAssociationsToValuesMap.put(newAssociations, values);
					} else {
						boolean insert = true;
						for (Iterator<Set<String>> it = currentAssociationsToValuesMap.keySet().iterator(); it.hasNext();) {
							Set<String> currentAssociations = it.next();
							if (newAssociations.containsAll(currentAssociations)) {
								it.remove();
							} else if (currentAssociations.containsAll(newAssociations)) {
								insert = false;
								break;
							}
						}
						if (insert) {
							currentAssociationsToValuesMap.put(newAssociations, values);
						}
					}
				}
			}
		}

		// transform associations to columns
		Map<String, Map<String, Set<Tuple>>> selections = new HashMap<>();
		for (String dataset : datasetToAssociationsToValuesMap.keySet()) {
			Map<Set<String>, Set<Tuple>> datasetAssociationsToValuesMap = datasetToAssociationsToValuesMap.get(dataset);
			Map<String, Set<Tuple>> columnsToValuesMap = new HashMap<>();

			for (Set<String> associations : datasetAssociationsToValuesMap.keySet()) {
				String columns = "";
				for (String associationId : associations) {
					Association association = associationGroup.getAssociation(associationId);
					String column = association.getField(dataset).getFieldName();
					if (columns.isEmpty()) {
						columns = column;
					} else {
						columns += "," + column;
					}
				}
				Set<Tuple> values = datasetAssociationsToValuesMap.get(associations);
				columnsToValuesMap.put(columns, values);
			}
			selections.put(dataset, columnsToValuesMap);
		}

		return selections;
	}
}
