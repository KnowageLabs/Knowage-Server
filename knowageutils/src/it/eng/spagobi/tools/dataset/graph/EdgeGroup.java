package it.eng.spagobi.tools.dataset.graph;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

public class EdgeGroup {

	private final Set<String> edgeNames;
	private final String columnNames;

	public EdgeGroup(Set<LabeledEdge<String>> edges) {
		this.edgeNames = new HashSet<String>(edges.size());
		for (LabeledEdge<String> edge : edges) {
			edgeNames.add(edge.getLabel());
		}
		SortedSet<String> orderedEdgeNames = new TreeSet<String>(edgeNames);
		this.columnNames = StringUtils.join(orderedEdgeNames.iterator(), ",");
	}

	public Set<String> getEdgeNames() {
		return edgeNames;
	}

	public String getColumnNames() {
		return columnNames;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnNames == null) ? 0 : columnNames.hashCode());
		result = prime * result + ((edgeNames == null) ? 0 : edgeNames.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EdgeGroup))
			return false;
		EdgeGroup other = (EdgeGroup) obj;
		if (columnNames == null) {
			if (other.columnNames != null)
				return false;
		} else if (!columnNames.equals(other.columnNames))
			return false;
		if (edgeNames == null) {
			if (other.edgeNames != null)
				return false;
		} else if (!edgeNames.equals(other.edgeNames))
			return false;
		return true;
	}
}
