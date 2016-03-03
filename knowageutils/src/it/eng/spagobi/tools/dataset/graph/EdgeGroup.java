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

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

public class EdgeGroup {

	private final Set<String> edgeNames;
	private final String orderedEdgeNames;

	public EdgeGroup(Set<LabeledEdge<String>> edges) {
		this.edgeNames = new HashSet<String>(edges.size());
		for (LabeledEdge<String> edge : edges) {
			edgeNames.add(edge.getLabel());
		}
		SortedSet<String> orderedEdgeNames = new TreeSet<String>(edgeNames);
		this.orderedEdgeNames = StringUtils.join(orderedEdgeNames.iterator(), ",");
	}

	public Set<String> getEdgeNames() {
		return edgeNames;
	}

	public String getOrderedEdgeNames() {
		return orderedEdgeNames;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((orderedEdgeNames == null) ? 0 : orderedEdgeNames.hashCode());
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
		if (orderedEdgeNames == null) {
			if (other.orderedEdgeNames != null)
				return false;
		} else if (!orderedEdgeNames.equals(other.orderedEdgeNames))
			return false;
		if (edgeNames == null) {
			if (other.edgeNames != null)
				return false;
		} else if (!edgeNames.equals(other.edgeNames))
			return false;
		return true;
	}
}
