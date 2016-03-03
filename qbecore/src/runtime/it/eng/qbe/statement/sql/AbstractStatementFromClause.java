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

import it.eng.qbe.datasource.hibernate.IHibernateDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.AbstractStatementClause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public abstract class AbstractStatementFromClause extends AbstractStatementClause{

	public static final String FROM = "FROM";
	public static transient Logger logger = Logger.getLogger(AbstractStatementFromClause.class);
	
	public String buildClause(Query query, Map entityAliasesMaps) {
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
				if ("cube".equalsIgnoreCase( type )) {
					cubes.add(modelEntity);
				} else {
					normalEntities.add(modelEntity);
				}

				

			}

			addEntityAliases(cubes, buffer, entityAliases);
			

			if (normalEntities.size() > 0 && cubes.size() > 0)
				buffer.append(",");

			addEntityAliases(normalEntities, buffer, entityAliases);



		} finally {
			logger.debug("OUT");
		}
		
		return buffer.toString().trim();
	}
	
	private void addEntityAliases(List<IModelEntity> entities, StringBuffer buffer, Map entityAliases){
		if(entities!=null){
			for(int i=0; i<entities.size(); i++){
				IModelEntity me = entities.get(i);
				
				Map<String, List<String>> roleAliasMap = parentStatement.getQuery().getMapEntityRoleField( parentStatement.getDataSource()).get(me);
				java.util.Set<String> roleAlias = null;
				if(roleAliasMap!=null){
					roleAlias = roleAliasMap.keySet();
				}
				
				
				String entityAlias = (String) entityAliases.get(me.getUniqueName());
				
				if(roleAlias!=null && roleAlias.size()>1){

					Iterator<String> iter = roleAlias.iterator();
					while(iter.hasNext()){
						
						String firstRole = iter.next();
						String fromClauseElement = parentStatement.buildFromEntityAliasWithRoles(me, firstRole, entityAlias);
						buffer.append(fromClauseElement);
						if(iter.hasNext()){
							buffer.append(",");
						}
					}
					
					
				}else{
					//for Cassandra dont add the entityAlias
					String fromClauseElement = getTableName(me) + " "+ entityAlias;
					buffer.append(fromClauseElement);
				}
				if (i<entities.size()-1) {
					buffer.append(",");
				}
			}
		}

	} 
	
	//returns the table name for an entity
	public String getTableName(IModelEntity me){
		return me.getName();
	}

}
