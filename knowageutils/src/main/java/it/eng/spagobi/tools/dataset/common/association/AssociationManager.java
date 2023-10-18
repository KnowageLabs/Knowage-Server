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
package it.eng.spagobi.tools.dataset.common.association;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AssociationManager implements Serializable {

	private static final long serialVersionUID = -7952645147077233635L;
	private static final Logger LOGGER = Logger.getLogger(AssociationManager.class);

	private final List<Association> associations;

	public AssociationManager() {
		associations = new ArrayList<>();
	}

	public List<Association> getAssociations() {
		return associations;
	}

	public List<Association> getAssociations(String dataset) {
		List<Association> datasetAssociations = new ArrayList<>();
		for (Association association : associations) {
			if (association.containsDataset(dataset)) {
				datasetAssociations.add(association);
			}
		}
		return datasetAssociations;
	}

	public void addAssociation(Association association) {
		this.associations.add(association);
	}

	public void addAssociations(List<Association> associations) {
		this.associations.addAll(associations);
	}

	public List<AssociationGroup> getAssociationGroups() {
		List<AssociationGroup> associationGroups = new ArrayList<>();
		UndirectedGraph<String, DefaultEdge> g = buildGraph();
		ConnectivityInspector ci = new ConnectivityInspector(g);
		List<Set> connectedSet = ci.connectedSets();
		for (Set<String> datasets : connectedSet) {
			AssociationGroup associationGroup = new AssociationGroup();
			for (String dataset : datasets) {
				List<Association> currAssociations = getAssociations(dataset);
				associationGroup.addAssociations(currAssociations);
			}
			associationGroups.add(associationGroup);
		}
		return associationGroups;
	}

	public Set<String> getDataSets() {
		Set<String> datasets = new HashSet<>();
		for (Association association : associations) {
			for (Association.Field field : association.getFields()) {
				datasets.add(field.getLabel());
			}
		}
		return datasets;
	}

	// ===========================================================================
	// PRIVATE
	// ===========================================================================

	private UndirectedGraph<String, DefaultEdge> buildGraph() {
		UndirectedGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

		Set<String> datasets = getDataSets();
		for (String dataset : datasets) {
			g.addVertex(dataset);
		}

		for (Association association : associations) {
			List<Association.Field> fields = association.getFields();
			String previousDataset = fields.get(0).getLabel();

			for (int i = 1; i < fields.size(); i++) {
				String dataset = fields.get(i).getLabel();
				g.addEdge(previousDataset, dataset);
				previousDataset = dataset;
			}
		}
		return g;
	}

	/**
	 * Craete a toy graph based on String objects.
	 *
	 * @return a graph based on String objects.
	 */
	private static UndirectedGraph<String, DefaultEdge> createStringGraph() {

		UndirectedGraph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);

		String v1 = "v1";
		String v2 = "v2";
		String v3 = "v3";
		String v4 = "v4";
		String v5 = "v5";

		// add the vertices
		g.addVertex(v1);
		g.addVertex(v2);
		g.addVertex(v3);
		g.addVertex(v4);
		g.addVertex(v5);

		// add edges to create a circuit
		g.addEdge(v1, v2);
		g.addEdge(v3, v4);

		return g;
	}

	public static void main(String[] args) {
		UndirectedGraph<String, DefaultEdge> g = createStringGraph();
		ConnectivityInspector ci = new ConnectivityInspector(g);
		List connectedSet = ci.connectedSets();
		for (Object o : connectedSet) {
			Set vertexes = (Set) o;
			for (Object vertex : vertexes) {
				LOGGER.debug(vertex.toString());
			}
			LOGGER.debug("-----------------------------");
		}
		LOGGER.debug(connectedSet.size());
	}
}
