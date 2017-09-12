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

package it.eng.qbe.statement.graph.validator;

import it.eng.qbe.model.structure.IModelEntity;

import java.util.Set;

import org.jgrapht.Graph;
/**
 * This is the interface of the graph validators. In general a graph validator
 * should check if a Graph represent a valid query
 * 
 * 
 * @authors
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
import org.jgrapht.UndirectedGraph;

public interface IGraphValidator {

	/**
	 * Check if the graph G is valid
	 * @param G
	 * @param unjoinedEntities
	 * @return
	 */
	public boolean isValid(Graph G, Set<IModelEntity> unjoinedEntities);

}
