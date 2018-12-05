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

package it.eng.spagobi.tools.dataset.graph.associativity.utils;

import it.eng.spagobi.tools.dataset.graph.EdgeGroup;
import it.eng.spagobi.tools.dataset.graph.Tuple;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */

public class AssociativeLogicResult {

	private final Map<String, Set<EdgeGroup>> datasetToEdgeGroup = new HashMap<>();
	private final Map<EdgeGroup, Set<String>> edgeGroupToDataset = new HashMap<>();
	private final Map<EdgeGroup, Set<Tuple>> edgeGroupValues = new HashMap<>();

	public Map<String, Set<EdgeGroup>> getDatasetToEdgeGroup() {
		return datasetToEdgeGroup;
	}

	public Map<EdgeGroup, Set<String>> getEdgeGroupToDataset() {
		return edgeGroupToDataset;
	}

	public Map<EdgeGroup, Set<Tuple>> getEdgeGroupValues() {
		return edgeGroupValues;
	}

	public void clearValues(EdgeGroup group) {
		if (edgeGroupValues.containsKey(group)) {
			edgeGroupValues.get(group).clear();
		}
	}

	public void addValues(EdgeGroup group, Set<Tuple> values) {
		if (!edgeGroupValues.containsKey(group)) {
			edgeGroupValues.put(group, values);
		} else {
			edgeGroupValues.get(group).addAll(values);
		}
	}

	public Set<String> getDatasets(EdgeGroup group) {
		return edgeGroupToDataset.get(group);
	}
}
