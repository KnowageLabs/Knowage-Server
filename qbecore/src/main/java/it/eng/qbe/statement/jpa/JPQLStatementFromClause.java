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
import java.util.List;
import java.util.Map;
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
		if (queryGraph != null) {
			List<Relationship> relationships = queryGraph.getConnections();
			Map<String, String> queryEntityAliases = (Map) entityAliasesMaps.get(query.getId());

			if (queryGraph.hasJoinPaths()) {
				return this.buildClause(relationships, queryEntityAliases);
			}
		}

		return super.buildClause(query, entityAliasesMaps);

	}

	public String buildClause(List<Relationship> relationships, Map<String, String> queryEntityAliases) {

		List<String> fromClauseElements = new ArrayList<>();

		IModelEntity firstEntity = relationships.get(0).getSourceEntity();
		String fistEntityName = firstEntity.getName();
		String firtEntityAlias = queryEntityAliases.get(firstEntity.getUniqueName());
		List<String> joinStatments = createJoinStatements(relationships, queryEntityAliases);

		fromClauseElements.add(FROM);
		fromClauseElements.add(fistEntityName);
		fromClauseElements.add(firtEntityAlias);
		fromClauseElements.addAll(joinStatments);

		return StringUtils.join(fromClauseElements, " ");

	}

	private List<String> createJoinStatements(List<Relationship> relationships, Map<String, String> queryEntityAliases) {

		List<String> joinStatments = new ArrayList<>();
		for (Relationship relationship : relationships) {

			JoinRelationshipTypeMapping joinRelationshipTypeMapping = new JoinRelationshipTypeMapping();
			JoinType joinType = joinRelationshipTypeMapping.getJoinType(relationship.getType());
			String sourceEntityAlias = queryEntityAliases.get(relationship.getSourceEntity().getUniqueName());
			String targetEntityAlias = queryEntityAliases.get(relationship.getTargetEntity().getUniqueName());
			StringTokenizer st1 = new StringTokenizer(relationship.getTargetJoinPath(), ".");
			st1.nextToken();
			String targetPropertyName = st1.nextToken();
			JPQLJoinPath joinPath = new JPQLJoinPath(sourceEntityAlias, targetPropertyName);
			JPQLJoin join = new JPQLJoin();

			join.setJoinType(joinType);
			join.setJoinPath(joinPath);
			join.setTargetEntityAllias(targetEntityAlias);
			joinStatments.add(join.toString());
		}
		return joinStatments;
	}

	protected JPQLStatementFromClause(JPQLStatement statement) {
		parentStatement = statement;
	}

}
