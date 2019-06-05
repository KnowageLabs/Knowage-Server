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

package it.eng.spagobi.tools.dataset.graph.associativity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.Pseudograph;

import it.eng.spagobi.tools.dataset.graph.LabeledEdge;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;

public class Config {

	private String strategy;
	private Pseudograph<String, LabeledEdge<String>> graph;
	private Map<String, Map<String, String>> datasetToAssociations;
	private List<SimpleFilter> selections;
	private List<SimpleFilter> filters;
	private Set<String> nearRealtimeDatasets;
	private Map<String, Map<String, String>> datasetParameters;
	private Set<String> documents;

	public Config() {
		nearRealtimeDatasets = new HashSet<>(0);
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

	public List<SimpleFilter> getSelections() {
		return selections;
	}

	public void setSelections(List<SimpleFilter> selections) {
		this.selections = selections;
	}

	public Set<String> getNearRealtimeDatasets() {
		return nearRealtimeDatasets;
	}

	public void setNearRealtimeDatasets(Set<String> nearRealtimeDatasets) {
		this.nearRealtimeDatasets = nearRealtimeDatasets;
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

	public List<SimpleFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<SimpleFilter> filters) {
		this.filters = filters;
	}

}
