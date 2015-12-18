package it.eng.spagobi.tools.graph;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class EdgeGroup {
	private final Set<String> edgeNames;
	private final Set<String> orderedEdgeNames;

	public EdgeGroup(Set<LabeledEdge<String>> edges) {
		this.edgeNames = new HashSet<String>(edges.size());
		this.orderedEdgeNames = new TreeSet<String>();
		for (LabeledEdge<String> edge : edges) {
			edgeNames.add(edge.getLabel());
			orderedEdgeNames.add(edge.getLabel());
		}
	}

	public Set<String> getEdgeNames() {
		return edgeNames;
	}

	public Set<String> getOrderedEdgeNames() {
		return orderedEdgeNames;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EdgeGroup) {
			return this.edgeNames.equals(((EdgeGroup) obj).edgeNames);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return edgeNames.hashCode();
	}
}
