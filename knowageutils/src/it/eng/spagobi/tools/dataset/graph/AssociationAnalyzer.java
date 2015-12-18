package it.eng.spagobi.tools.dataset.graph;

import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.Association.Field;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.jgrapht.graph.Pseudograph;

public class AssociationAnalyzer {
	private Map<String, Map<String, String>> datasetAssociationColumnMap;
	private Pseudograph<String, LabeledEdge> graph;

	public AssociationAnalyzer(Collection<Association> associations) {
		this.datasetAssociationColumnMap = new HashMap<String, Map<String, String>>();
		this.graph = new Pseudograph<String, LabeledEdge>(new ClassBasedEdgeFactory<String, LabeledEdge>(LabeledEdge.class));
		
		calc(associations, datasetAssociationColumnMap, graph);
	}

	private void calc(Collection<Association> associations,
			Map<String, Map<String, String>> datasetToAssociationColumns,
			Pseudograph<String, LabeledEdge> graph) {
		
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
				if(datasetToAssociationColumns.containsKey(datasetLabel)){
					associationToColumns = datasetToAssociationColumns.get(datasetLabel);
				} else {
					associationToColumns = new HashMap<String, String>();
					datasetToAssociationColumns.put(datasetLabel, associationToColumns);
				}
				associationToColumns.put(associationId, field.getFieldName());
			}
			
			// add edges to the graph
			for (int i = 0; i < associationFields.size() - 1; i++) {
				String source = associationFields.get(i).getDataSetLabel();
				for (int j = i + 1; j < associationFields.size(); j++) {
					String target = associationFields.get(j).getDataSetLabel();
					LabeledEdge<String> labeledEdge= new LabeledEdge<String>(source, target, associationId);
					graph.addEdge(source, target, labeledEdge);
				}
			}
		}
	}

	public Map<String, Map<String, String>> getDatasetAssociationColumnMap() {
		return datasetAssociationColumnMap;
	}

	public Pseudograph<String, LabeledEdge> getGraph() {
		return graph;
	}
}
