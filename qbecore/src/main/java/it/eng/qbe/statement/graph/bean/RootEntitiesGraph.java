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

package it.eng.qbe.statement.graph.bean;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DirectedMultigraph;

import it.eng.qbe.model.structure.FilteredModelEntity;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.statement.graph.QueryGraphBuilder;

/**
 *
 * This is the graph of all the entities of the model
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class RootEntitiesGraph implements Cloneable {

	Set<Relationship> relationships;
	Map<String, IModelEntity> rootEntitiesMap;
	Graph<IModelEntity, Relationship> rootEntitiesGraph;

	protected static Logger logger = Logger.getLogger(RootEntitiesGraph.class);

	public RootEntitiesGraph() {
		relationships = new HashSet<>();
		rootEntitiesMap = new HashMap<>();
		rootEntitiesGraph = new DirectedMultigraph<>(Relationship.class);
	}

	public void addRootEntity(IModelEntity entity) {
		rootEntitiesMap.put(entity.getUniqueName(), entity);
		rootEntitiesGraph.addVertex(entity);
	}

	public IModelEntity getRootEntityByName(String entityName) {
		return rootEntitiesMap.get(entityName);
	}

	public List<IModelEntity> getAllRootEntities() {
		List<IModelEntity> list = new ArrayList<>();
		Iterator<String> it = rootEntitiesMap.keySet().iterator();
		while (it.hasNext()) {
			String entityName = it.next();
			// TODO replace with this ...
			// list.add( entities.get(entityName).getCopy() );
			list.add(rootEntitiesMap.get(entityName));
		}
		return list;
	}

	/**
	 * @return true if the root entities passed as input belongs to the same connected subgraph
	 */
	public boolean areRootEntitiesConnected(Set<IModelEntity> entities) {
		boolean areConnected = true;
		if (entities.size() > 1) {
			ConnectivityInspector inspector = null;
			if (rootEntitiesGraph instanceof DirectedGraph) {
				inspector = new ConnectivityInspector((DirectedGraph) rootEntitiesGraph);
			} else if (rootEntitiesGraph instanceof UndirectedGraph) {
				inspector = new ConnectivityInspector((UndirectedGraph) rootEntitiesGraph);
			}

			Iterator<IModelEntity> it = entities.iterator();
			IModelEntity entity = it.next();
			Set<Relationship> edges = rootEntitiesGraph.edgesOf(entity);
			Set<IModelEntity> connectedEntitySet = inspector.connectedSetOf(entity);
			while (it.hasNext()) {
				entity = it.next();
				if (!connectedEntitySet.contains(entity)) {
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

	private void setRootEntitiesGraph(Graph<IModelEntity, Relationship> rootEntitiesGraph) {
		this.rootEntitiesGraph = rootEntitiesGraph;
	}

	public Relationship addRelationship(IModelEntity fromEntity, List<IModelField> fromFields, IModelEntity toEntity, List<IModelField> toFields, String type,
			String relationName, String sourceJoinPath, String targetJoinPath) {
		Relationship relationship = new Relationship();
		relationship.setType(type); // MANY_TO_ONE : FK da 1 a 2
		relationship.setSourceFields(fromFields);
		relationship.setSourceJoinPath(sourceJoinPath);
		relationship.setTargetFields(toFields);
		relationship.setTargetJoinPath(targetJoinPath);
		relationship.setName(relationName);

		boolean added = rootEntitiesGraph.addEdge(fromEntity, toEntity, relationship);
		if (added) {
			relationships.add(relationship);
		}
		return added ? relationship : null;
	}

	public Graph<IModelEntity, Relationship> getRootEntitiesGraph() {
		return rootEntitiesGraph;
	}

	public Set<Relationship> getRelationships() {
		return relationships;
	}

	@Override
	public final RootEntitiesGraph clone() {
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
		Set<Relationship> targetEnities = new HashSet<>();

		IModelEntity entityToCheck = entity;
		if (entity instanceof FilteredModelEntity) {
			entityToCheck = ((FilteredModelEntity) entity).getWrappedModelEntity();
		}

		if (rootEntitiesGraph.containsVertex(entityToCheck)) {
			Set<Relationship> edges = rootEntitiesGraph.edgesOf(entityToCheck);
			Iterator<Relationship> it = edges.iterator();
			while (it.hasNext()) {
				Relationship edge = it.next();
				IModelEntity target = edge.getTargetEntity();
				if (!target.equals(entityToCheck)) {
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
		Set<Relationship> conections = new HashSet<>();

		IModelEntity sourceEntityToCheck = source;
		if (source instanceof FilteredModelEntity) {
			sourceEntityToCheck = ((FilteredModelEntity) source).getWrappedModelEntity();
		}

		IModelEntity targetEntityToCheck = target;
		if (target instanceof FilteredModelEntity) {
			targetEntityToCheck = ((FilteredModelEntity) target).getWrappedModelEntity();
		}

		Set<Relationship> relation = rootEntitiesGraph.getAllEdges(sourceEntityToCheck, targetEntityToCheck);
		Iterator<Relationship> it = relation.iterator();
		while (it.hasNext()) {
			Relationship edge = it.next();
			IModelEntity sourceEntity = edge.getTargetEntity();
			if (!sourceEntity.equals(source)) {
				conections.add(edge);
			}
		}

		return conections;
	}

	public Set<Relationship> getConnectingRelatiosnhips(Set<IModelEntity> entities) {

		Set<Relationship> connectingRelatiosnhips = new HashSet<>();

		Set<IModelEntity> connectedEntities = new HashSet<>();

		Iterator<IModelEntity> it = entities.iterator();
		connectedEntities.add(it.next());

		while (it.hasNext()) {
			IModelEntity entity = it.next();
			if (connectedEntities.contains(entity))
				continue;
			GraphPath minimumPath = null;
			double minPathLength = Double.MAX_VALUE;
			for (IModelEntity connectedEntity : connectedEntities) {
				DijkstraShortestPath dsp = new DijkstraShortestPath(rootEntitiesGraph, entity, connectedEntity);
				double pathLength = dsp.getPathLength();
				if (minPathLength > pathLength) {
					minPathLength = pathLength;
					minimumPath = dsp.getPath();
				}
			}
			List<Relationship> relationships = minimumPath.getEdgeList();
			connectingRelatiosnhips.addAll(relationships);
			for (Relationship relatioship : relationships) {
				connectedEntities.add(rootEntitiesGraph.getEdgeSource(relatioship));
				connectedEntities.add(rootEntitiesGraph.getEdgeTarget(relatioship));
			}
		}

		for (Relationship r : connectingRelatiosnhips) {
			IModelEntity source = rootEntitiesGraph.getEdgeSource(r);
			IModelEntity target = rootEntitiesGraph.getEdgeTarget(r);
			logger.error(source.getName() + " -> " + target.getName());
		}

		return connectingRelatiosnhips;
	}

}
