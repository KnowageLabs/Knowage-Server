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

package it.eng.qbe.statement.sql;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.query.Query;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class SQLStatementFromClause extends AbstractStatementFromClause {

	public static transient Logger logger = Logger.getLogger(SQLStatementFromClause.class);

	public static String build(SQLStatement parentStatement, Query query, Map<String, Map<String, String>> entityAliasesMaps, IDataSet initialDataset) {
		SQLStatementFromClause clause = new SQLStatementFromClause(parentStatement);
		if (initialDataset != null) {
			return clause.buildClauseWithDataset(query, entityAliasesMaps, initialDataset);
		}
		return clause.buildClause(query, entityAliasesMaps);
	}

	protected SQLStatementFromClause(SQLStatement statement) {
		parentStatement = statement;
	}

	// returns the table name for an entity
	@Override
	public String getTableName(IModelEntity me) {
		String name = (String) me.getProperties().get("tableName");
		return name;
	}

	public String buildClauseWithDataset(Query query, Map entityAliasesMaps, IDataSet initialDataset) {
		StringBuffer buffer;

		logger.debug("IN");
		buffer = new StringBuffer();
		try {
			Map entityAliases = (Map) entityAliasesMaps.get(query.getId());

			if (entityAliases == null || entityAliases.keySet().size() == 0) {
				return "";
			}

			buffer.append(" " + FROM + " ");

			List<IModelEntity> cubes = new ArrayList<IModelEntity>();
			List<IModelEntity> normalEntities = new ArrayList<IModelEntity>();

			Iterator it = entityAliases.keySet().iterator();
			while (it.hasNext()) {
				String entityUniqueName = (String) it.next();
				logger.debug("entity [" + entityUniqueName + "]");

				IModelEntity modelEntity = parentStatement.getDataSource().getModelStructure().getEntity(entityUniqueName);

				String type = (String) modelEntity.getProperty("type");
				if ("cube".equalsIgnoreCase(type)) {
					cubes.add(modelEntity);
				} else {
					normalEntities.add(modelEntity);
				}

			}

			addEntityAliasesWithDataset(cubes, buffer, entityAliases, initialDataset);

			if (normalEntities.size() > 0 && cubes.size() > 0)
				buffer.append(",");

			addEntityAliasesWithDataset(normalEntities, buffer, entityAliases, initialDataset);

		} finally {
			logger.debug("OUT");
		}

		return buffer.toString().trim();
	}

	private void addEntityAliasesWithDataset(List<IModelEntity> entities, StringBuffer buffer, Map entityAliases, IDataSet initialDataset) {
		if (entities != null) {
			for (int i = 0; i < entities.size(); i++) {
				IModelEntity me = entities.get(i);

				Map<String, List<String>> roleAliasMap = parentStatement.getQuery().getMapEntityRoleField(parentStatement.getDataSource()).get(me);
				java.util.Set<String> roleAlias = null;
				if (roleAliasMap != null) {
					roleAlias = roleAliasMap.keySet();
				}

				String entityAlias = (String) entityAliases.get(me.getUniqueName());

				if (roleAlias != null && roleAlias.size() > 1) {

					Iterator<String> iter = roleAlias.iterator();
					while (iter.hasNext()) {

						String firstRole = iter.next();
						String fromClauseElement = parentStatement.buildFromEntityAliasWithRoles(me, firstRole, entityAlias);
						buffer.append(fromClauseElement);
						if (iter.hasNext()) {
							buffer.append(",");
						}
					}

				} else {
					// for Cassandra dont add the entityAlias
					String fromClauseElement = "";
					if (initialDataset instanceof JDBCDataSet) {
						JDBCDataSet datasetJDBC = (JDBCDataSet) initialDataset;
						String queryJDBC = datasetJDBC.getQuery().toString();
						fromClauseElement = " (select * from ( " + queryJDBC + " )) " + entityAlias;
					}

					buffer.append(fromClauseElement);
				}
				if (i < entities.size() - 1) {
					buffer.append(",");
				}
			}
		}

	}

}
