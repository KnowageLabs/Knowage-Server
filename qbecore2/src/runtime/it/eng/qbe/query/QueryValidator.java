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

package it.eng.qbe.query;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.statement.IStatement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryValidator {

	public static final String FILTER_ROLES_ERROR = "FILTER_ROLES_ERROR";
	
	public static List<String> validate(Query query,IDataSource dataSource){
		List<String> errors = new ArrayList<String>();
		
		if(!validateFiltersWithRoles(query, dataSource)){
			errors.add(FILTER_ROLES_ERROR);
		}

		return errors;
	}
	
	
	private static boolean validateFiltersWithRoles(Query query,IDataSource dataSource){
	
		//get all the where filters
		Map<IModelField, Set<IQueryField>> fieldsInvolved = new  HashMap<IModelField, Set<IQueryField>>();
		query.getWhereIModelFields(fieldsInvolved,  dataSource);
		
		//iterate on the fields involved in the where filter clause
		Set<IModelField> qf = fieldsInvolved.keySet();
		Iterator<IModelField> setIterator = qf.iterator();
		while (setIterator.hasNext()) {
			IModelField aModelField = (IModelField) setIterator.next();
			IModelEntity aModelFieldEntity = aModelField.getParent();
			Map<IModelEntity, List<String>> entityFieldFromRoleMap = query.getEntityFieldFromRoleMap(dataSource);
			
			if(entityFieldFromRoleMap.containsKey(aModelFieldEntity)){//if the entity linked to the field has more than one alias
				List<String> aliasFields = entityFieldFromRoleMap.get(aModelFieldEntity);
				Set<IQueryField> whereFields = fieldsInvolved.get(aModelField);
				Iterator<IQueryField> whileIterator = whereFields.iterator();
				
				//check if the alias of the field is mapped on a entity alias
				while(whileIterator.hasNext()){
					WhereField where = (WhereField) whileIterator.next();
					if(where.getLeftOperand().type.equals(IStatement.OPERAND_TYPE_SIMPLE_FIELD) && !aliasFields.contains(where.getLeftOperand().alias)){
						return false;
					}
					if(where.getRightOperand().type.equals(IStatement.OPERAND_TYPE_SIMPLE_FIELD) && !aliasFields.contains(where.getRightOperand().alias)){
						return false;
					}
				}
			}
					
		}
		return true;
	}
}
