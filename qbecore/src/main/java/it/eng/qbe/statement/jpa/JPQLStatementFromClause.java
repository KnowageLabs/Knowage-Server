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
package it.eng.qbe.statement.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.persistence.criteria.JoinType;

import org.apache.log4j.Logger;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.qbe.statement.sql.AbstractStatementFromClause;
import it.eng.spagobi.utilities.StringUtils;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JPQLStatementFromClause extends AbstractStatementFromClause {

	public static transient Logger logger = Logger.getLogger(JPQLStatementFromClause.class);

	public static String build(JPQLStatement parentStatement, Query query, Map<String, Map<String, String>> entityAliasesMaps) {
		JPQLStatementFromClause clause = new JPQLStatementFromClause(parentStatement);
		return clause.buildClause(query, entityAliasesMaps);
	}

	@Override
	public String buildClause(Query query, Map entityAliasesMaps) {

		QueryGraph queryGraph = query.getQueryGraph();
		Map<String, String> queryEntityAliases = (Map) entityAliasesMaps.get(query.getId());
		if (queryGraph != null && queryGraph.hasJoinPaths()) {

			return this.buildClause(queryGraph, queryEntityAliases, entityAliasesMaps);
		}

		return super.buildClause(query, entityAliasesMaps);

	}

	public String buildClause(final QueryGraph queryGraph, Map<String, String> queryEntityAliases, Map entityAliasesMaps) {

		List<String> fromClauseElements = new ArrayList<>();
		List<String> joinStatments = new ArrayList<String>();

		Set<IModelEntity> vertexSet = queryGraph.vertexSet();
		List<IModelEntity> vertexList = new ArrayList<IModelEntity>(vertexSet);

		/*
		 * Order vertex by counting the in and out relationships, where the vertex with
		 * more out relationship and minor in relationship is first.
		 */
		Collections.sort(vertexList, new Comparator<IModelEntity>() {

			@Override
			public int compare(IModelEntity o1, IModelEntity o2) {
				int o1Index = (1 * queryGraph.inDegreeOf(o1)) +  (-1 * queryGraph.outDegreeOf(o1));
				int o2Index = (1 * queryGraph.inDegreeOf(o2)) +  (-1 * queryGraph.outDegreeOf(o2));
				return o1Index - o2Index;
			}

		});
		joinStatments.addAll(recursionEntryPoint(queryGraph, vertexList, queryEntityAliases, entityAliasesMaps));
		fromClauseElements.addAll(joinStatments);

		return StringUtils.join(fromClauseElements, " ");

	}

	List<String> recursionEntryPoint(QueryGraph queryGraph, List<IModelEntity> vertexList, Map<String, String> queryEntityAliases, Map entityAliasesMaps) {

		List<String> ret = new ArrayList<String>();
		Set<Relationship> traversedRelationships = new HashSet<Relationship>();
		IModelEntity iModelEntity = vertexList.get(0);
		String name = iModelEntity.getName();
		String alias = getAlias(entityAliasesMaps, queryEntityAliases, iModelEntity);

		// The first vertex is the FROM-clause table
		ret.add(FROM);
		ret.add(name);
		ret.add(alias);
		ret.addAll(recursion(queryGraph, traversedRelationships, iModelEntity, queryEntityAliases, entityAliasesMaps));

		return ret;
	}

	private List<String> recursion(QueryGraph queryGraph, Set<Relationship> traversedRelationships, IModelEntity previousEntity, Map<String, String> queryEntityAliases, Map entityAliasesMaps) {
		List<String> ret = new ArrayList<String>();

		// edgesOf() return an unmodifiable set
		Set<Relationship> edgesOf = new HashSet<Relationship>(queryGraph.edgesOf(previousEntity));

		// Remove already traversed relationship
		edgesOf.removeAll(traversedRelationships);

		for (Relationship currEdge : edgesOf) {

			boolean invert = false;
			IModelEntity sourceEntity = currEdge.getSourceEntity();
			IModelEntity targetEntity = currEdge.getTargetEntity();
			JoinType joinType = currEdge.getJoinType();
			String entityJoinPath = currEdge.getTargetJoinPath();

			/*
			 * If the source of the relationship is not the current vertex, we have
			 * changed the direction of the path.
			 */
			if (sourceEntity != previousEntity) {
				invert = true;
			}

			if (invert) {
				IModelEntity tmp = targetEntity;
				targetEntity = sourceEntity;
				sourceEntity = tmp;

				entityJoinPath = currEdge.getSourceJoinPath();
			}

			// In case of OUTER, invert if necessary
			if (invert && joinType == JoinType.RIGHT) {
				joinType = JoinType.LEFT;
			} else if (invert && joinType == JoinType.LEFT) {
				joinType = JoinType.RIGHT;
			}

			String sourceEntityAlias = getAlias(entityAliasesMaps, queryEntityAliases, sourceEntity);
			String targetEntityAlias = getAlias(entityAliasesMaps, queryEntityAliases, targetEntity);
			StringTokenizer st1 = new StringTokenizer(entityJoinPath, ".");
			st1.nextToken();
			String targetPropertyName = st1.nextToken();
			JPQLJoinPath joinPath = new JPQLJoinPath(sourceEntityAlias, targetPropertyName);
			JPQLJoin join = new JPQLJoin();

			join.setJoinType(joinType);
			join.setJoinPath(joinPath);
			join.setTargetEntityAllias(targetEntityAlias);
			ret.add(join.toString());

			// Remember to exclude the current relationship: we don't want to traverse it again
			traversedRelationships.add(currEdge);

			ret.addAll(recursion(queryGraph, traversedRelationships, targetEntity, queryEntityAliases, entityAliasesMaps));
		}

		return ret;
	}

	private String getAlias(Map entityAliasesMaps, Map<String, String> queryEntityAliases, IModelEntity entity) {
		String alias;
		alias = queryEntityAliases.get(entity.getUniqueName());
		if (alias == null) {
			alias = parentStatement.getNextAlias(entityAliasesMaps);

			queryEntityAliases.put(entity.getUniqueName(), alias);
		}
		return alias;
	}

	protected JPQLStatementFromClause(JPQLStatement statement) {
		parentStatement = statement;
	}

}
