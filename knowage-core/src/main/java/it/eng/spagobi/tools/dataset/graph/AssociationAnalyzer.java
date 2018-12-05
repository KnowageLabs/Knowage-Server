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

import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Pseudograph;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
				String datasetLabel = field.getLabel();

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

			// add edges to the graph
			for (int i = 0; i < associationFields.size() - 1; i++) {
				Field sourceField = associationFields.get(i);
				String source = sourceField.getLabel();
				for (int j = i + 1; j < associationFields.size(); j++) {
					Field targetField = associationFields.get(j);

					String target = targetField.getLabel();
					LabeledEdge<String> labeledEdge = new LabeledEdge<String>(source, target, associationId);
					graph.addEdge(source, target, labeledEdge);
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
}
