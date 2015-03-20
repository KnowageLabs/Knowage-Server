/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.qbe.statement.graph;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;

/**
 * 
 * This class builds a graph starting from a set of paths 
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class QueryGraphBuilder {

	private static transient Logger logger = Logger.getLogger(QueryGraphBuilder.class);
	private List<IModelEntity> vertexes;


	public QueryGraphBuilder(){
		vertexes = new ArrayList<IModelEntity>();
	}

	
	public QueryGraph buildGraphFromEdges(Collection<Relationship> edges){
		logger.debug("IN");
		logger.debug("Building the multigraph");
		QueryGraph multigraph = buildGraphFromEdges2(edges);
		
		if(multigraph==null){
			logger.debug("The query graph is null");
			return null;
		}
		
//		cleanRoleRelationsPropert(multigraph);
//		IModelEntity nodeConnectedWithMoreThan1Connection =  GraphUtilities.isMultiGraph(multigraph);
//		if(nodeConnectedWithMoreThan1Connection!=null){
//			IModelEntity root = GraphUtilities.getRoot(multigraph);
//			List<List<Relationship>> previousRolePath  =  new ArrayList<List<Relationship>>();
//			previousRolePath.add(new ArrayList<Relationship>());
//			root.getProperties().put(GraphUtilities.roleRelationsProperty,previousRolePath);
//			logger.debug("There is more than one edge that starts from the entity "+nodeConnectedWithMoreThan1Connection.getUniqueName());
//			addOneAliasToGraph(multigraph, root, 0);
//			nodeConnectedWithMoreThan1Connection =  GraphUtilities.isMultiGraph(multigraph);
//		}

		return multigraph;
	}
	
//	private void cleanRoleRelationsPropert(QueryGraph G){
//		logger.debug("IN");
//		logger.debug("Clean the lis");
//		Set<IModelEntity> vertexes = G.vertexSet();
//		if(vertexes!=null){
//			Iterator<IModelEntity> vertexIter = vertexes.iterator();
//
//			//For every node check if there is more than one edge that connect it with another node
//			while (vertexIter.hasNext()) {
//				IModelEntity vertex = (IModelEntity) vertexIter.next();
//				vertex.getProperties().put(GraphUtilities.roleRelationsProperty, null);
//			}
//		}
//		
//	}
//	
//	private void addOneAliasToGraph(QueryGraph multigraph, IModelEntity forkNode, int deep){
//		logger.debug("IN");
//		logger.debug("Getting the map of the roles");
//		Map<IModelEntity, List<Relationship>> edgeMap = GraphUtilities.getEdgeMap(multigraph, forkNode);
//		
//		//check to avoid infinite loops
//		if(deep>GraphUtilities.maxPathLength){
//			return;
//		}
//		deep++;
//		
//		//update the relations of the node
//		List<List<Relationship>> previousRolePath  = (List<List<Relationship>>)forkNode.getProperties().get(GraphUtilities.roleRelationsProperty);
//		
//		
//		Set<IModelEntity> keysList = edgeMap.keySet();
//		if(keysList!=null){
//			Iterator<IModelEntity> iter = keysList.iterator();
//			while (iter.hasNext()) {
//				IModelEntity iModelEntity = (IModelEntity) iter.next();
//				List<Relationship> listRelations = edgeMap.get(iModelEntity);
//				
//				if(listRelations.size()==1){//only one relation between forkNode and iModelentity, so no other forks 
//					logger.debug("There is only one relation between nodes["+forkNode.getName()+","+iModelEntity.getName() +"]");
//					addRelationOnRolePath(iModelEntity, previousRolePath, listRelations.get(0));
//					addOneAliasToGraph(multigraph, iModelEntity,deep);
//				}else{
//					logger.debug("There is more than one relation between nodes["+forkNode.getName()+","+iModelEntity.getName() +"]");
//					forkRelationOnRolePath(iModelEntity, previousRolePath, listRelations);
//					addOneAliasToGraph(multigraph, iModelEntity,deep);
//				}
//			}
//		}
//	}
//	
//	
//	private void addRelationOnRolePath(IModelEntity iModelEntity, List<List<Relationship>> previousRolePath, Relationship relation){
//
//		List<List<Relationship>> newRoleRelations = new ArrayList<List<Relationship>>();
//		
//		for(int i=0; i<previousRolePath.size(); i++){
//			List<Relationship> rolePath = previousRolePath.get(i);
//			List<Relationship> newRolePath = new ArrayList<Relationship>();
//			newRolePath.addAll(rolePath);
//			newRolePath.add(relation);
//			newRoleRelations.add(newRolePath);
//		}
//		
//		iModelEntity.getProperties().put(GraphUtilities.roleRelationsProperty,newRoleRelations);
//	}


//	private void forkRelationOnRolePath(IModelEntity iModelEntity,  List<List<Relationship>> previousPath, List<Relationship> listRelations){
//		
//		List<List<Relationship>> newRoleRelations = new ArrayList<List<Relationship>>();
//		
//		
//		for(int i=0; i<previousPath.size(); i++){
//			List<Relationship> rolePath = previousPath.get(i);
//			for(int j=0; j<listRelations.size(); j++){
//				//creates a new path for each forking relation 
//				List<Relationship> clonedRelations = new ArrayList<Relationship>();
//				clonedRelations.addAll(rolePath);
//				clonedRelations.add(listRelations.get(j));
//				newRoleRelations.add(clonedRelations);
//			}
//		}
//		
//		iModelEntity.getProperties().put(GraphUtilities.roleRelationsProperty,newRoleRelations);
//	}
	
	
	/**
	 * Builds a graph starting from its edges
	 * @param edges the collection of edges
	 * @return the graph built starting from the list of edges
	 */
	public QueryGraph buildGraphFromEdges2(Collection<Relationship> edges){
		logger.debug("IN");
		Assert.assertNotNull(edges, "The list of edges is null. Impossbile to create a graph");
		logger.debug("The number of paths is "+edges.size());

		QueryGraph graph = new QueryGraph(Relationship.class);

		if(edges!=null){
			Iterator< Relationship> pathIter = edges.iterator();
			while(pathIter.hasNext()){
				Relationship edge = pathIter.next();
				addEdgeToGraph(graph, edge);
			}
		}

		logger.debug("OUT");
		return graph;
	}

	/**
	 * Adds a single edge to the graph
	 * @param graph
	 * @param edefEdge
	 */
	private void addEdgeToGraph(Graph<IModelEntity, Relationship> graph, DefaultEdge edefEdge){
		if(edefEdge!=null){
			Relationship edge = (Relationship)edefEdge;
			IModelEntity src= edge.getSourceEntity();
			IModelEntity target= edge.getTargetEntity();

			if(!vertexes.contains(src)){
				logger.debug("Add the vertex "+src.getName());
				vertexes.add(src);
				graph.addVertex(src);
			}
			if(!vertexes.contains(target)){
				logger.debug("Add the vertex "+src.getName());
				vertexes.add(target);
				graph.addVertex(target);
			}

			logger.debug("Add the edge "+src.getName()+"--"+target.getName());
			graph.addEdge(src, target, edge);
		}
		logger.debug("OUT");
	}

	
	//USEFULL BUT NOT USED METHOSDS
	
	public Graph<IModelEntity, Relationship> buildGraphFromPaths(Collection<GraphPath<IModelEntity, Relationship>> paths){
		logger.debug("IN");
		Assert.assertNotNull(paths, "The list of paths is null. Impossbile to create a graph");
		logger.debug("The number of paths is "+paths.size());

		UndirectedGraph<IModelEntity, Relationship> graph = new Multigraph<IModelEntity, Relationship>(Relationship.class);
		
		if(paths!=null){
			Iterator<GraphPath<IModelEntity, Relationship>> pathIter = paths.iterator();
			while(pathIter.hasNext()){
				GraphPath<IModelEntity, Relationship> path = pathIter.next();
				addPathToGraph(graph, path);
			}
		}

		logger.debug("OUT");
		return graph;
	}
	
	private void addPathToGraph(Graph<IModelEntity, Relationship> graph, GraphPath<IModelEntity, Relationship> path ){
		logger.debug("IN");
		List<Relationship> edges = path.getEdgeList();
		if(edges!=null){
			for(int i=0; i<edges.size(); i++){
				Relationship edge = (Relationship)edges.get(i);
				addEdgeToGraph(graph, edge);
			}
		}
		logger.debug("OUT");
	}



}
