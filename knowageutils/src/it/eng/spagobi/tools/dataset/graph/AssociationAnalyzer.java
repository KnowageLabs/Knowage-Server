package it.eng.spagobi.tools.dataset.graph;

import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Pseudograph;

public class AssociationAnalyzer {
	private Collection<Association> associations;
	private Map<String, Map<String, String>> datasetAssociationColumnMap;
	private Pseudograph<String, LabeledEdge<String>> graph;

	public AssociationAnalyzer(Collection<Association> associations) {
		this.associations = associations;
		this.datasetAssociationColumnMap = new HashMap<String, Map<String, String>>();
		this.graph = new Pseudograph<String, LabeledEdge<String>>(new ClassBasedEdgeFactory<String, LabeledEdge<String>>(
				(Class<LabeledEdge<String>>) (Object) LabeledEdge.class));
	}

	public void process() {

		for (Association association : associations) {
			String associationId = association.getId();
			List<Field> associationFields = association.getFields();

			for (Field field : associationFields) {
				String datasetLabel = field.getDataSetLabel();

				// add vertex to the graph
				if (!graph.containsVertex(datasetLabel)) {
					graph.addVertex(datasetLabel);
				}

				// add column of association to the map
				Map<String, String> associationToColumns = null;
				if (datasetAssociationColumnMap.containsKey(datasetLabel)) {
					associationToColumns = datasetAssociationColumnMap.get(datasetLabel);
				} else {
					associationToColumns = new HashMap<String, String>();
					datasetAssociationColumnMap.put(datasetLabel, associationToColumns);
				}
				associationToColumns.put(associationId, field.getFieldName());
			}

			// add edges to the graph
			for (int i = 0; i < associationFields.size() - 1; i++) {
				String source = associationFields.get(i).getDataSetLabel();
				for (int j = i + 1; j < associationFields.size(); j++) {
					String target = associationFields.get(j).getDataSetLabel();
					LabeledEdge<String> labeledEdge = new LabeledEdge<String>(source, target, associationId);
					graph.addEdge(source, target, labeledEdge);
				}
			}
		}
	}

	public Map<String, Map<String, String>> getDatasetAssociationColumnMap() {
		return datasetAssociationColumnMap;
	}

	public Pseudograph<String, LabeledEdge<String>> getGraph() {
		return graph;
	}

	public static Map<String, Map<String, Set<String>>> getSelections(AssociationGroup associationGroup, Map<EdgeGroup, Set<String>> egdeGroupValuesMap) {
		Map<String, Map<String, Set<String>>> selections = new HashMap<String, Map<String, Set<String>>>();

		for (EdgeGroup edgeGroup : egdeGroupValuesMap.keySet()) {
			Set<String> valuesSet = egdeGroupValuesMap.get(edgeGroup);
			String[] associationIds = edgeGroup.getColumnNames().split(",");

			Map<String, String> dataSetColumnsMap = new HashMap<String, String>();
			for (String associationId : associationIds) {
				Association association = associationGroup.getAssociation(associationId);

				for (Field field : association.getFields()) {
					String dataSet = field.getDataSetLabel();
					String column = field.getFieldName();

					if (dataSetColumnsMap.containsKey(dataSet)) {
						String columns = dataSetColumnsMap.get(dataSet);
						dataSetColumnsMap.put(dataSet, columns + "," + column);
					} else {
						dataSetColumnsMap.put(dataSet, column);
					}
				}
			}

			for (String dataSet : dataSetColumnsMap.keySet()) {
				String columns = dataSetColumnsMap.get(dataSet);

				Map<String, Set<String>> columnsValuesMap = null;
				if (selections.containsKey(dataSet)) {
					columnsValuesMap = selections.get(dataSet);
				} else {
					columnsValuesMap = new HashMap<String, Set<String>>();
					selections.put(dataSet, columnsValuesMap);
				}

				columnsValuesMap.put(columns, valuesSet);
			}
		}

		return selections;
	}
}
