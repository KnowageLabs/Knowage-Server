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
package it.eng.qbe.statement.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.criteria.JoinType;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.graph.bean.PathChoice;
import it.eng.qbe.statement.graph.bean.PathChoicePathTextLengthComparator;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;

public class GraphUtilities {

	public static final String RELATIONSHIP_ID = "id";
	public static final String IS_CONSIDERED = "isConsidered";
	public static final String JOIN_TYPE = "joinType";
	public static final int maxPathLength = 5;
	static private Logger logger = Logger.getLogger(GraphUtilities.class);

	/**
	 * Removes the subpaths
	 *
	 * @param ambiguousModelField
	 * @param sort
	 */
	public static void cleanSubPaths(Set<ModelFieldPaths> ambiguousModelField, String orderDirection) {
		StringComparator stringComparator = new StringComparator();

		if (ambiguousModelField != null) {
			Iterator<ModelFieldPaths> iter = ambiguousModelField.iterator();
			while (iter.hasNext()) {
				List<String> listRelations = new ArrayList<String>();
				ModelFieldPaths modelFieldPaths = iter.next();
				Set<PathChoice> pathChoices = modelFieldPaths.getChoices();

				if (pathChoices != null) {

					Set<PathChoice> pathChoicesFiltered;
					if (orderDirection != null) {
						PathChoicePathTextLengthComparator pathChoiceComparator = new PathChoicePathTextLengthComparator(orderDirection);
						pathChoicesFiltered = new TreeSet(pathChoiceComparator);
					} else {
						pathChoicesFiltered = new HashSet<PathChoice>();
					}
					Iterator<PathChoice> pathChoicesIter = pathChoices.iterator();

					while (pathChoicesIter.hasNext()) {
						PathChoice pathChoice2 = pathChoicesIter.next();
						String relation = pathChoice2.getRelations().toString();
						listRelations.add(relation);
					}

					Collections.sort(listRelations, stringComparator);

					pathChoicesIter = pathChoices.iterator();
					while (pathChoicesIter.hasNext()) {
						PathChoice pathChoice2 = pathChoicesIter.next();
						String relation = pathChoice2.getRelations().toString();
						if (!isSubPath(relation, listRelations)) {
							pathChoicesFiltered.add(pathChoice2);
						}
					}

					modelFieldPaths.setChoices(pathChoicesFiltered);
				}

			}
		}
	}

	public static QueryGraph getCoverGraph(IDataSource dataSource, Query query) {
		QueryGraph queryGraph = null;
		IModelStructure modelStructure = dataSource.getModelStructure();
		RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(dataSource.getConfiguration().getModelName(), false);
		Graph<IModelEntity, Relationship> graph = rootEntitiesGraph.getRootEntitiesGraph();
		Set<IModelEntity> entities = query.getQueryEntities(dataSource);
		queryGraph = GraphManager.getDefaultCoverGraphInstance(null).getCoverGraph(graph, entities);
		return queryGraph;
	}

	private static class StringComparator implements Comparator<String> {

		@Override
		public int compare(String arg0, String arg1) {
			if (arg0 == null) {
				return -1;
			}
			if (arg1 == null) {
				return 1;
			}
			if (arg0.length() != arg1.length()) {
				return arg0.length() - arg1.length();
			}
			return arg0.compareTo(arg1);
		}

	}

	public static QueryGraph deserializeGraph(JSONArray relationshipsJSON, Query query, IDataSource dataSource) throws JSONException {

		String modelName = dataSource.getConfiguration().getModelName();
		IModelStructure modelStructure = dataSource.getModelStructure();
		Set<String> relationshipsNames = new HashSet<String>();
		RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(modelName, true);
		if (relationshipsJSON == null || relationshipsJSON.length() == 0) {
			Set<Relationship> allRelationships = rootEntitiesGraph.getRelationships();
			Iterator<Relationship> it = allRelationships.iterator();
			while (it.hasNext()) {
				it.next().setIsConsidered(true);
				;

			}
			return null;
		}

		Map<String, JoinType> joinTypeMapping = new HashMap<>();
		Map<String, Boolean> isConsideredMapping = new HashMap<>();
		Set<PathChoice> choices = new HashSet<PathChoice>();
		for (int i = 0; i < relationshipsJSON.length(); i++) {
			JSONObject relationshipJSON = relationshipsJSON.getJSONObject(i);
			String relationshipsName = relationshipJSON.getString(RELATIONSHIP_ID);

			relationshipsNames.add(relationshipsName);
			JoinType joinType = JoinType.valueOf(relationshipJSON.optString(JOIN_TYPE, JoinType.INNER.toString()));
			joinTypeMapping.put(relationshipsName, joinType);
			isConsideredMapping.put(relationshipsName, relationshipJSON.optBoolean(IS_CONSIDERED, true));

		}

		List<Relationship> queryRelationships = new ArrayList<Relationship>();
		Set<Relationship> allRelationships = rootEntitiesGraph.getRelationships();
		Iterator<String> it = relationshipsNames.iterator();
		while (it.hasNext()) {
			String id = it.next();
			Relationship relationship = getRelationshipById(allRelationships, id);
			relationship.setJoinType(joinTypeMapping.get(id));
			relationship.setIsConsidered(isConsideredMapping.get(id));
			queryRelationships.add(relationship);
		}

		QueryGraphBuilder builder = new QueryGraphBuilder();
		QueryGraph queryGraph = builder.buildGraphFromEdges(queryRelationships);

		return queryGraph;
	}

	private static Relationship getRelationshipById(Set<Relationship> allRelationships, String id) {
		Iterator<Relationship> it = allRelationships.iterator();
		while (it.hasNext()) {
			Relationship relationship = it.next();
			if (relationship.getId().equals(id)) {
				return relationship;
			}
		}
		return null;
	}

	/**
	 * Checks if the 2 paths are equals
	 *
	 * @param path1
	 * @param path2
	 * @return
	 */
	public static boolean arePathsEquals(GraphPath<IModelEntity, Relationship> path1, GraphPath<IModelEntity, Relationship> path2) {
		Set<String> relationsPath1 = getRelationsSet(path1);
		Set<String> relationsPath2 = getRelationsSet(path2);
		return relationsPath1.equals(relationsPath2);
	}

	private static Set<String> getRelationsSet(GraphPath<IModelEntity, Relationship> path1) {
		Set<String> relations = new HashSet<String>();
		List<Relationship> edges = path1.getEdgeList();
		for (int i = 0; i < edges.size(); i++) {
			relations.add(edges.get(i).getId());
		}
		return relations;

	}

	/**
	 * Checks if the pathChoice1 is a subpath of pathChoice2
	 *
	 * @param pathChoice1
	 * @param pathChoice2
	 * @return
	 */
	public static boolean isSubPath(PathChoice pathChoice1, PathChoice pathChoice2) {

		String path1 = pathChoice1.getRelations().toString();
		String path2 = pathChoice2.getRelations().toString();

		return isSubPath(path1, path2);
	}

	/**
	 * Checks if the string relations is a subpath of at least one of the paths in listRelations
	 *
	 * @param relations
	 * @param listRelations
	 * @return
	 */
	public static boolean isSubPath(String relations, List<String> listRelations) {
		for (int i = listRelations.size() - 1; i > 0; i--) {
			String aRelation = (listRelations.get(i));
			if (isSubPath(relations, aRelation)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the list of relations in the path1 is a subpath of path2
	 *
	 * @param path1
	 * @param path2
	 * @return
	 */
	public static boolean isSubPath(String path1, String path2) {
		path1 = path1.substring(1, path1.length() - 1);
		path2 = path2.substring(1, path2.length() - 1);

		if (path2.length() > path1.length() && path2.indexOf(path1) >= 0) {
			return true;
		}
		return false;
	}

	// /**
	// * Check if the graph contains 2 nodes connected with more than 1 vertex
	// * @param G the graph
	// * @return the node connected with more than one relation with another node. Null if there is no node connected with more than one relation to another
	// node
	// */
	// public static IModelEntity isMultiGraph(Graph G){
	//
	// Set<IModelEntity> vertexes = G.vertexSet();
	// if(vertexes!=null){
	// Iterator<IModelEntity> vertexIter = vertexes.iterator();
	//
	// //For every node check if there is more than one edge that connect it with another node
	// while (vertexIter.hasNext()) {
	// IModelEntity vertex = (IModelEntity) vertexIter.next();
	// Set<Relationship> vertexConnection = G.edgesOf(vertex);
	// if(vertexConnection!=null && vertex.getProperties().get(GraphUtilities.roleRelationsProperty)==null){
	// Iterator<Relationship> vertexConnectionIter = vertexConnection.iterator();
	// List<IModelEntity> checkedEntites = new ArrayList<IModelEntity>();
	//
	// while (vertexConnectionIter.hasNext()) {
	// Relationship relationship = (Relationship) vertexConnectionIter.next();
	// IModelEntity src = relationship.getSourceEntity();
	// IModelEntity target = relationship.getTargetEntity();
	// IModelEntity otherEntity = src;//the entity not equal to vertex
	//
	// if(vertex.equals(src)){
	// otherEntity = target;
	// }
	//
	// if(checkedEntites.contains(otherEntity)){
	// return vertex;//there is more than one connection between 2 entities
	// }else{
	// checkedEntites.add(otherEntity);
	// }
	// }
	// }
	// }
	// }
	// return null;
	// }
	//
	public static boolean isCyclic(DirectedGraph G) {
		CycleDetector cd = new CycleDetector(G);
		return cd.detectCycles();
	}

	public static IModelEntity getRoot(DirectedGraph G) {

		Set<IModelEntity> vertexes = G.vertexSet();
		if (vertexes != null) {
			Iterator<IModelEntity> vertexIter = vertexes.iterator();

			// For every node check if there is more than one edge that connect it with another node
			while (vertexIter.hasNext()) {
				IModelEntity vertex = vertexIter.next();
				Set<Relationship> vertexConnection = G.incomingEdgesOf(vertex);
				if (vertexConnection == null || vertexConnection.size() == 0) {
					return vertex;
				}
			}
		}
		return null;
	}

	/**
	 * Get the map of the relation between the node vertex and the other nodes. This map is useful for the role analysis. If a node has more than one role with
	 * the vertex node in the map the linked list has one relation for each role
	 *
	 * @param G
	 * @param vertex
	 * @return
	 */
	public static Map<IModelEntity, List<Relationship>> getEdgeMap(Graph G, IModelEntity vertex) {

		Map<IModelEntity, List<Relationship>> vertexRelationsMap = new HashMap<IModelEntity, List<Relationship>>();

		Set<Relationship> vertexConnection = G.edgesOf(vertex);
		if (vertexConnection != null) {
			Iterator<Relationship> vertexConnectionIter = vertexConnection.iterator();

			while (vertexConnectionIter.hasNext()) {
				Relationship relationship = vertexConnectionIter.next();

				IModelEntity otherEntity = relationship.getTargetEntity();// the entity not equal to vertex

				if (vertex.equals(otherEntity) && (G instanceof UndirectedGraph)) {
					otherEntity = relationship.getSourceEntity();
				}
				if (!vertex.equals(otherEntity)) {
					List<Relationship> relationsWithOther = vertexRelationsMap.get(otherEntity);

					if (relationsWithOther == null) {
						relationsWithOther = new ArrayList<Relationship>();
						vertexRelationsMap.put(otherEntity, relationsWithOther);
					}
					relationsWithOther.add(relationship);
				}

			}
		}
		return vertexRelationsMap;
	}

	public static JSONArray serializeGraph(Query query) throws Exception {
		QueryGraph graph = query.getQueryGraph();
		if (graph != null) {
			logger.debug("The graph of the query is not null" + graph.toString());
			ObjectMapper mapper = new ObjectMapper();
			SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null));
			simpleModule.addSerializer(Relationship.class, new RelationJSONSerializerForAnalysisState());

			mapper.registerModule(simpleModule);
			String serialized = mapper.writeValueAsString(graph.getConnections());
			logger.debug("The serialization of the graph is " + serialized);
			JSONArray array = new JSONArray(serialized);
			return array;
		} else {
			logger.debug("The graph of the query is null");
			return new JSONArray();
		}

	}

	public static class RelationJSONSerializerForAnalysisState extends JsonSerializer<Relationship> {

		@Override
		public void serialize(Relationship value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
			jgen.writeStartObject();
			jgen.writeStringField(GraphUtilities.RELATIONSHIP_ID, value.getId());
			jgen.writeStringField(GraphUtilities.JOIN_TYPE, value.getJoinType().toString());
			jgen.writeBooleanField(GraphUtilities.IS_CONSIDERED, value.isConsidered());
			jgen.writeEndObject();

		}

	}

}
