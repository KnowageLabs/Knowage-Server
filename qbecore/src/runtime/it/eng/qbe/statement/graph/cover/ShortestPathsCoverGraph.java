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
import it.eng.qbe.statement.graph.PathInspector;
import it.eng.qbe.statement.graph.QueryGraphBuilder;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.bean.Relationship;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.alg.KruskalMinimumSpanningTree;
import org.jgrapht.graph.DirectedSubgraph;
import org.jgrapht.graph.Subgraph;
import org.jgrapht.graph.UndirectedSubgraph;

public class ShortestPathsCoverGraph extends AbstractDefaultCover{

	public static transient Logger logger = Logger.getLogger(ShortestPathsCoverGraph.class);

	
	
	

	public QueryGraph getCoverGraph( Graph<IModelEntity, Relationship> rootEntitiesGraph, Set<IModelEntity> entities) {
		Subgraph<IModelEntity, Relationship, Graph<IModelEntity, Relationship>> subgraph;
		Iterator<IModelEntity> it = entities.iterator();
		Set<Relationship> connectingRelatiosnhips = new HashSet<Relationship>();
		Set<Relationship> minimumRelatiosnhips = new HashSet<Relationship>();

		Set<IModelEntity> connectedEntities = new HashSet<IModelEntity>();
		connectedEntities.add( it.next() );

		//build the subgraph that contains all the nodes in entities
		logger.debug("Building the subgraph that contains only the entities involved in the query");
		while(it.hasNext()) {
			IModelEntity entity = it.next();
			for(IModelEntity otherEntity : entities) {

				GraphPath<IModelEntity, Relationship> minimumPath = null;
				
				//check the path from entity to connectedEntity
				DijkstraShortestPath<IModelEntity, Relationship> dsp = new DijkstraShortestPath(rootEntitiesGraph, entity, otherEntity);
				double pathLength = dsp.getPathLength();
				minimumPath = dsp.getPath();
	
				
				if(rootEntitiesGraph instanceof DirectedGraph){
					//check the path from connectedEntity to entity
					DijkstraShortestPath dsp2 = new DijkstraShortestPath(rootEntitiesGraph, otherEntity, entity);
					double pathLength2 = dsp2.getPathLength();
					if(pathLength > pathLength2) {
						minimumPath = dsp2.getPath();
					}
				} 
				
				
				if(minimumPath!=null){
					List<Relationship> pathRelations = minimumPath.getEdgeList();
					
					if(pathRelations!=null){
						for(int i=0; i<pathRelations.size(); i++){
							
							Relationship connectingRel=	pathRelations.get(i);

							if(connectingRel!=null){
								connectingRelatiosnhips.add(connectingRel);
								connectedEntities.add(connectingRel.getSourceEntity());
								connectedEntities.add(connectingRel.getTargetEntity());
							}

						}
					}	
				}
			}
		}

		if(rootEntitiesGraph instanceof DirectedGraph){
			subgraph = (Subgraph) new DirectedSubgraph<IModelEntity, Relationship>((DirectedGraph)rootEntitiesGraph, connectedEntities, connectingRelatiosnhips);
		}else{
			subgraph = (Subgraph) new UndirectedSubgraph<IModelEntity, Relationship>((UndirectedGraph)rootEntitiesGraph, connectedEntities, connectingRelatiosnhips);
		}
		logger.debug("Subgraph built");
		
		logger.debug("Getting the minimum spaning tree on the subgraph");
		KruskalMinimumSpanningTree<IModelEntity, Relationship> kruscal = new KruskalMinimumSpanningTree<IModelEntity, Relationship>(subgraph);
		minimumRelatiosnhips = kruscal.getEdgeSet();
		logger.debug("Finished to load the MST");
		
		logger.debug("Building the graph starting from the MST");
		QueryGraphBuilder qb = new QueryGraphBuilder();
		QueryGraph monimumGraph = qb.buildGraphFromEdges(minimumRelatiosnhips);
		logger.debug("Built the graph starting from the MST");
		
		return monimumGraph;
	}

	public Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>>  getConnectingRelatiosnhips( Graph<IModelEntity, Relationship> rootEntitiesGraph, Set<IModelEntity> entities) {
		QueryGraph monimumGraph = getCoverGraph(rootEntitiesGraph, entities);
		return getConnectingRelatiosnhips(monimumGraph, entities);
	}

	public Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship>>>  getConnectingRelatiosnhips( QueryGraph monimumGraph, Set<IModelEntity> entities) {
		PathInspector pi = new PathInspector(monimumGraph, monimumGraph.vertexSet());
		Map<IModelEntity, Set<GraphPath<IModelEntity, Relationship> >> minimumPaths = pi.getAllEntitiesPathsMap();

		return minimumPaths;
	}


}
