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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.accessmodality.AbstractModelAccessModality;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelStructure;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.graph.bean.RootEntitiesGraph;
import it.eng.spagobi.commons.bo.UserProfile;

/**
 * @author FMilosavljevic
 *
 *
 */
public class SqlFilterModelAccessModality extends AbstractModelAccessModality {

	public static transient Logger logger = Logger.getLogger(SqlFilterModelAccessModality.class);

	private UserProfile userProfile = null;

	public ArrayList<IModelEntity> getSqlFilterEntities(Query query, IDataSource dataSource) {
		Set<IModelEntity> sqlFilterEntities = new HashSet<>();
		IModelStructure modelStructure = dataSource.getModelStructure();
		Map<String, IModelEntity> entity = modelStructure.getEntities();
		Collection<IModelEntity> entities = entity.values();
		Set<IModelEntity> queryEntities = query.getQueryEntities(dataSource);
		RootEntitiesGraph rootEntitiesGraph = modelStructure.getRootEntitiesGraph(dataSource.getConfiguration().getModelName(), false);

		Iterator<IModelEntity> iterator = entities.iterator();
		while (iterator.hasNext()) {
			Set<IModelEntity> tempEntities = new HashSet<>();
			IModelEntity tempEntity = iterator.next();

			if (!tempEntity.getProperties().get("sqlFilter").equals("")) {
				tempEntities.add(tempEntity);
				tempEntities.addAll(queryEntities);
				boolean realtionship = rootEntitiesGraph.areRootEntitiesConnected(tempEntities);
				if (realtionship) {
					sqlFilterEntities.addAll(tempEntities);
				}
			}
		}

		return new ArrayList<IModelEntity>(sqlFilterEntities);
	}

}
