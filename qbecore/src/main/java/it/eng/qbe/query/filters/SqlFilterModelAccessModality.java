/**
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
package it.eng.qbe.query.filters;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.Graph;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.accessmodality.AbstractModelAccessModality;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.statement.IStatement;
import it.eng.qbe.statement.graph.GraphManager;
import it.eng.qbe.statement.graph.bean.QueryGraph;
import it.eng.qbe.statement.graph.bean.Relationship;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spagobi.commons.bo.UserProfile;

/**
 * @author FMilosavljevic
 *
 *
 */
public class SqlFilterModelAccessModality extends AbstractModelAccessModality {

	public static transient Logger logger = Logger.getLogger(SqlFilterModelAccessModality.class);

	private UserProfile userProfile = null;

	public Set<IModelEntity> getSqlFilterEntities(IDataSource dataSource, Set<IModelEntity> queryEntitiesa) {
		Set<IModelEntity> sqlFilterEntities = new HashSet<>();

		Set<IModelEntity> queryEntities = new HashSet<IModelEntity>(queryEntitiesa);
		IModelStructure modelStructure = dataSource.getModelStructure();

		Map<String, IModelEntity> entity = modelStructure.getEntities();
		Collection<IModelEntity> entities = entity.values();

		RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(dataSource.getConfiguration().getModelName(), false);

		Iterator<IModelEntity> iterator = entities.iterator();

		while (iterator.hasNext()) {

			IModelEntity tempEntity = iterator.next();

			if (!tempEntity.getProperties().get("sqlFilter").equals("")) {
				queryEntities.add(tempEntity);
				boolean realtionship = rootEntitiesGraph.areRootEntitiesConnected(queryEntities);
				if (realtionship) {
					sqlFilterEntities.addAll(queryEntities);
				}
			}
		}

		return sqlFilterEntities;
	}

	public QueryGraph setGraphWithSqlQueryEntities(Set<IModelEntity> unjoinedEntities, IStatement parentStatement) {
		String modelNameFM = parentStatement.getDataSource().getConfiguration().getModelName();
		Graph<IModelEntity, Relationship> rootEntitiesGraphFM = parentStatement.getDataSource().getModelStructure().getRootEntitiesGraph(modelNameFM, false)
				.getRootEntitiesGraph();

		return GraphManager.getDefaultCoverGraphInstance(((String) ConfigSingleton.getInstance().getAttribute("QBE.GRAPH-PATH.defaultCoverImpl")))
				.getCoverGraph(rootEntitiesGraphFM, unjoinedEntities);
	}

}
