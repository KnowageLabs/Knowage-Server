/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.qbe.statement.graph.bean;

import it.eng.qbe.model.structure.FilteredModelEntity;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.statement.graph.QueryGraphBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DirectedMultigraph;

/**
 * 
 * This is the graph of all the entities of the model
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */


public class RootEntitiesGraph  implements Cloneable{
	
	Set<Relationship> relationships;
	Map<String, IModelEntity> rootEntitiesMap;
	Graph<IModelEntity, Relationship> rootEntitiesGraph;
	
	public RootEntitiesGraph() {
		relationships = new HashSet<Relationship>();
		rootEntitiesMap = new HashMap<String, IModelEntity>();
		rootEntitiesGraph = new DirectedMultigraph<IModelEntity, Relationship>(Relationship.class);
	}
	
	public void addRootEntity(IModelEntity entity) {
		rootEntitiesMap.put(entity.getUniqueName(), entity);
		rootEntitiesGraph.addVertex(entity);
	}
	
	public IModelEntity getRootEntityByName(String entityName) {
		return rootEntitiesMap.get(entityName);
	}
	
	public List<IModelEntity> getAllRootEntities() {
		List<IModelEntity> list = new ArrayList<IModelEntity>();
		Iterator<String> it = rootEntitiesMap.keySet().iterator();
		while(it.hasNext()) {
			String entityName = it.next();
			// TODO replace with this ...
			//list.add( entities.get(entityName).getCopy() );
			list.add( rootEntitiesMap.get(entityName) );
		}
		return list;
	}
	
	/**
	 * @return true if the root entities passed as input belongs to the same connected subgraph
	 */
	public boolean areRootEntitiesConnected(Set<IModelEntity> entities) {
		boolean areConnected = true;
		if(entities.size() > 1) {
			ConnectivityInspector inspector = null;
			if(rootEntitiesGraph instanceof DirectedGraph){
				inspector = new ConnectivityInspector((DirectedGraph)rootEntitiesGraph);
			} else if(rootEntitiesGraph instanceof UndirectedGraph){
				inspector = new ConnectivityInspector((UndirectedGraph)rootEntitiesGraph);
			}

			Iterator<IModelEntity> it = entities.iterator();
			IModelEntity entity = it.next();
			Set<Relationship> edges = rootEntitiesGraph.edgesOf(entity);
			Set<IModelEntity> connectedEntitySet = inspector.connectedSetOf(entity);
			while(it.hasNext()) {
				entity = it.next();
				if(connectedEntitySet.contains(entity) == false) {
					areConnected = false;
					break;
				}
			}
		}
		
		return areConnected;
	}
	
	

	private void setRelationships(Set<Relationship> relationships) {
		this.relationships = relationships;
	}

	private void setRootEntitiesMap(Map<String, IModelEntity> rootEntitiesMap) {
		this.rootEntitiesMap = rootEntitiesMap;
	}

	private void setRootEntitiesGraph(
			Graph<IModelEntity, Relationship> rootEntitiesGraph) {
		this.rootEntitiesGraph = rootEntitiesGraph;
	}

	public Relationship addRelationship(IModelEntity fromEntity, List<IModelField> fromFields,
			IModelEntity toEntity, List<IModelField> toFields, String type, String relationName) {
		Relationship relationship = new Relationship();
		relationship.setType(type); // MANY_TO_ONE : FK da 1 a 2
		relationship.setSourceFields(fromFields);
		relationship.setTargetFields(toFields); 
		relationship.setName(relationName);
		boolean added = rootEntitiesGraph.addEdge(fromEntity, toEntity, relationship);
		if (added) {
			relationships.add(relationship);
		}
		return added? relationship: null;
	}

	public Graph<IModelEntity, Relationship> getRootEntitiesGraph() {
		return rootEntitiesGraph;
	}
	
	public Set<Relationship> getRelationships() {
		return relationships;
	}
	
	public RootEntitiesGraph clone(){
		RootEntitiesGraph reg = new RootEntitiesGraph();
		reg.setRootEntitiesMap(rootEntitiesMap);
		reg.setRelationships(relationships);
		QueryGraphBuilder qgb = new QueryGraphBuilder();
		QueryGraph qg = qgb.buildGraphFromEdges(relationships);
		reg.setRootEntitiesGraph(qg);
		return reg;
	}
	

	/**
	 * 
	 * @return the list of entities reached by an out bound relation
	 */
	public Set<Relationship> getRootEntityDirectConnections(IModelEntity entity) {
		Set<Relationship> targetEnities = new HashSet<Relationship>();

		IModelEntity entityToCheck = entity;
		if(entity instanceof FilteredModelEntity){
			entityToCheck = ((FilteredModelEntity)entity).getWrappedModelEntity();
		}
		
		if(rootEntitiesGraph.containsVertex(entityToCheck)){
			Set<Relationship> edges = rootEntitiesGraph.edgesOf(entityToCheck);
			Iterator<Relationship> it = edges.iterator();
			while(it.hasNext()) {
				Relationship edge = (Relationship)it.next();
				IModelEntity target = edge.getTargetEntity();
				if(!target.equals(entityToCheck)){
					targetEnities.add(edge);
				}
			}
		}

		return targetEnities;
	}
	
	
	/**
	 * 
	 * @return the sets of direct relations from source to target
	 */
	public Set<Relationship> getDirectConnections(IModelEntity source, IModelEntity target) {
		Set<Relationship> conections = new HashSet<Relationship>();

		IModelEntity sourceEntityToCheck = source;
		if(source instanceof FilteredModelEntity){
			sourceEntityToCheck = ((FilteredModelEntity)source).getWrappedModelEntity();
		}
		
		IModelEntity targetEntityToCheck = target;
		if(target instanceof FilteredModelEntity){
			targetEntityToCheck = ((FilteredModelEntity)target).getWrappedModelEntity();
		}
		
		Set<Relationship> relation = rootEntitiesGraph.getAllEdges(sourceEntityToCheck, targetEntityToCheck);
		Iterator<Relationship> it = relation.iterator();
		while(it.hasNext()) {
			Relationship edge = (Relationship)it.next();
			IModelEntity sourceEntity = edge.getTargetEntity();
			if(!sourceEntity.equals(source)){
				conections.add(edge);
			}
		}
		
		return conections;
	}
	

	
	public Set<Relationship> getConnectingRelatiosnhips(Set<IModelEntity> entities) {
		
		Set<Relationship> connectingRelatiosnhips = new HashSet<Relationship>();
		
		Set<IModelEntity> connectedEntities = new HashSet<IModelEntity>();
		
		Iterator<IModelEntity> it = entities.iterator();
		connectedEntities.add( it.next() );
		
		while(it.hasNext()) {
			IModelEntity entity = it.next();
			if(connectedEntities.contains(entity)) continue;
			GraphPath minimumPath = null;
			double minPathLength = Double.MAX_VALUE;
			for(IModelEntity connectedEntity : connectedEntities) {
				DijkstraShortestPath dsp = new DijkstraShortestPath(rootEntitiesGraph, entity, connectedEntity);
				double pathLength = dsp.getPathLength();
				if(minPathLength > pathLength) {
					minPathLength = pathLength;
					minimumPath = dsp.getPath();
				}
			}
			List<Relationship> relationships = (List<Relationship>)minimumPath.getEdgeList();
			connectingRelatiosnhips.addAll(relationships);
			for(Relationship relatioship: relationships) {
				connectedEntities.add( rootEntitiesGraph.getEdgeSource(relatioship) );
				connectedEntities.add( rootEntitiesGraph.getEdgeTarget(relatioship) );
			}
		}
		
		for(Relationship r : connectingRelatiosnhips) {
			IModelEntity source = rootEntitiesGraph.getEdgeSource(r);
			IModelEntity target = rootEntitiesGraph.getEdgeTarget(r);
			System.err.println(source.getName() + " -> " + target.getName());
		}
		
		return connectingRelatiosnhips;
	}
	
}

