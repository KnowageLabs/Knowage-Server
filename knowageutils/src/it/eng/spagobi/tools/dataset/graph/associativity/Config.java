package it.eng.spagobi.tools.dataset.graph.associativity;

import it.eng.spagobi.tools.dataset.graph.LabeledEdge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.Pseudograph;

public class Config {

	private String strategy;
	private Pseudograph<String, LabeledEdge<String>> graph;
	private Map<String, Map<String, String>> datasetToAssociations;
	private Map<String, String> selections;
	private Set<String> realtimeDatasets;
	private Map<String, Map<String, String>> datasetParameters;
	private Set<String> documents;

	public Config() {
		realtimeDatasets = new HashSet<>(0);
		datasetParameters = new HashMap<>(0);
		documents = new HashSet<>(0);
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public Pseudograph<String, LabeledEdge<String>> getGraph() {
		return graph;
	}

	public void setGraph(Pseudograph<String, LabeledEdge<String>> graph) {
		this.graph = graph;
	}

	public Map<String, Map<String, String>> getDatasetToAssociations() {
		return datasetToAssociations;
	}

	public void setDatasetToAssociations(Map<String, Map<String, String>> datasetToAssociations) {
		this.datasetToAssociations = datasetToAssociations;
	}

	public Map<String, String> getSelections() {
		return selections;
	}

	public void setSelections(Map<String, String> selections) {
		this.selections = selections;
	}

	public Set<String> getRealtimeDatasets() {
		return realtimeDatasets;
	}

	public void setRealtimeDatasets(Set<String> realtimeDatasets) {
		this.realtimeDatasets = realtimeDatasets;
	}

	public Map<String, Map<String, String>> getDatasetParameters() {
		return datasetParameters;
	}

	public void setDatasetParameters(Map<String, Map<String, String>> datasetParameters) {
		this.datasetParameters = datasetParameters;
	}

	public Set<String> getDocuments() {
		return documents;
	}

	public void setDocuments(Set<String> documents) {
		this.documents = documents;
	}

}
