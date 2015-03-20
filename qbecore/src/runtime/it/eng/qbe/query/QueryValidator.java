/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
