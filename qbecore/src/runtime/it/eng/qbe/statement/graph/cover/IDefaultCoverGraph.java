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
package it.eng.qbe.statement.graph.cover;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.statement.graph.ModelFieldPaths;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.bean.Relationship;

import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;

public interface IDefaultCoverGraph {

	/**
	 * Calculate the default set of paths that involves all the entities "entities" of the graph rootEntitiesGraph
	 * @param ambiguousModelField the set of Ambiguous paths to activate if containd in the cover graph
	 * @param rootEntitiesGraph the graph
	 * @param entities the set of entities involved in the cover graph
	 */
	public void applyDefault(Set<ModelFieldPaths> ambiguousModelField,  Graph<IModelEntity, Relationship> rootEntitiesGraph, Set<IModelEntity> entities);
	
	/**
	 * Activate the paths of ambiguousModelField that exist in defaultConnections
	 * @param defaultConnections the list of paths contained in the default graph 
	 * @param ambiguousModelField the list of ambiguous paths
	 */
	public void applyDefault(Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> defaultConnections, Set<ModelFieldPaths> ambiguousModelField);
	
	/**
	 * Activate the paths of ambiguousModelField that exist in defaultConnections
	 * @param defaultConnections the list of paths contained in the default graph 
	 * @param ambiguousModelField the list of ambiguous paths
	 * @param withSubpaths if false we remove the subpaths of the default cover graph. For example if the default cover graph contains the path A->B->C and for the field B there are 3 possible paths: A->B, B->C and A->B->C. The algorithm set active only the last one. 
	 */
	public void applyDefault(Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>> defaultConnections, Set<ModelFieldPaths> ambiguousModelField, boolean withSubpaths);
	
	/**
	 * Activate the paths of ambiguousModelField that exist in defaultConnections
	 * @param ambiguousModelField the set of Ambiguous paths to activate if containd in the cover graph
	 * @param monimumGraph the graph cover graph
	 * @param entities the set of entities involved in the cover graph
	 */
	public void applyDefault(Set<ModelFieldPaths> ambiguousModelField, QueryGraph monimumGraph, Set<IModelEntity> entities);
	
	/**
	 * Calculate the cover graph  that involves all the entities "entities" of the graph rootEntitiesGraph
	 * @param rootEntitiesGraph the graph
	 * @param entities the set of entities involved in the cover graph
	 * @return
	 */
	public QueryGraph getCoverGraph(Graph<IModelEntity, Relationship> rootEntitiesGraph, Set<IModelEntity> entities);
	
	
}
