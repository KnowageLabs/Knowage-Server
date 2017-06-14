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
package it.eng.qbe.graph;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.statement.graph.PathInspector;
import it.eng.qbe.statement.graph.QueryGraphBuilder;
import it.eng.qbe.statement.graph.bean.Relationship;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.GraphPathImpl;

public class PathInspectorTestCase  extends AbstractGraphTestCase {
	

	
	
	public void testBuildGraph() {

		Relationship r3 = new Relationship();
		r3.setSourceFields(entities.get(0).getAllFields());
		r3.setTargetFields(entities.get(2).getAllFields());
		List<Relationship> edges = new ArrayList<Relationship>();
		edges.add(r3);
		
		List<GraphPath<IModelEntity, Relationship>> paths = new ArrayList<GraphPath<IModelEntity, Relationship>>();
		GraphPath<IModelEntity, Relationship> path = new GraphPathImpl<IModelEntity, Relationship>(graph, entities.get(0), entities.get(2), edges, 9.0);
		paths.add(path);
		
		
		QueryGraphBuilder qgb = new QueryGraphBuilder();
	//	UndirectedGraph<IModelEntity, Relationship> graph = qgb.buildGraph(paths);
		
	}
	
	public void testPathInspectorEBuildGraph() {
		
		PathInspector pathInspector = new PathInspector(graph, entities);
		QueryGraphBuilder qgb = new QueryGraphBuilder();
		//Graph<IModelEntity, Relationship> graph = qgb.buildGraph(pathInspector.getAllGraphPaths());
	}

}
