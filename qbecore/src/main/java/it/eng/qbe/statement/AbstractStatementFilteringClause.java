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

package it.eng.qbe.statement;

import it.eng.qbe.model.structure.IModelEntity;
import it.eng.qbe.model.structure.IModelField;
import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.InLineCalculatedSelectField;
import it.eng.qbe.query.Operand;
import it.eng.qbe.query.Query;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.objects.Couple;

import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */
public abstract class AbstractStatementFilteringClause extends AbstractStatementClause {
	
	public static transient Logger logger = Logger.getLogger(AbstractStatementFilteringClause.class);
	
	protected String buildInLineCalculatedFieldClause(String operator, Operand leftOperand, boolean isPromptable, Operand rightOperand, Query query, Map entityAliasesMaps, IConditionalOperator conditionalOperator){
		String[] rightOperandElements;
			
		
		logger.debug("IN");
		try {
			// Se sono qui è perchè il leftOperand è un campo calcolato inline quindi posso parserizzare senza problemi
			JSONObject leftOperandJSON = new JSONObject(leftOperand.values[0]);
			
			String expression = leftOperandJSON.getString("expression");
			String type  = leftOperandJSON.optString("type");
			if(type==null){
				type = "STRING";
			}
			String nature  = leftOperandJSON.optString("nature");
			if(nature==null){
				nature = "ATTRIBUTE";
			}
			String alias  = leftOperandJSON.optString("alias");
			if(alias==null){
				alias = "";
			}
			
			String slots = null;
			String s = leftOperandJSON.optString("slots");
			if(s != null && s.trim().length() > 0) {
				slots = leftOperandJSON.getString("slots");
			}
			
			expression = parseInLinecalculatedField(new InLineCalculatedSelectField(alias, expression, slots, type,nature, true, true, false, "", ""), slots, query, entityAliasesMaps);
					
			if (parentStatement.OPERAND_TYPE_STATIC.equalsIgnoreCase(rightOperand.type) && isPromptable ) {
				// get last value first (the last value edited by the user)
				rightOperandElements = rightOperand.lastValues;
			} else {
				rightOperandElements = buildOperand(rightOperand, query, entityAliasesMaps);
			}
			
			if (parentStatement.OPERAND_TYPE_STATIC.equalsIgnoreCase(rightOperand.type) && slots==null)  {
				rightOperandElements = getTypeBoundedStaticOperand(leftOperand, operator, rightOperandElements);
			}else{
				rightOperandElements = getTypeBoundedStaticOperand("STRING", rightOperandElements);
			}
			
			String operandElement = null;
			if (leftOperand instanceof HavingField.Operand) {
				HavingField.Operand havingFieldOperand = (HavingField.Operand) leftOperand;
				IAggregationFunction function = havingFieldOperand.function;
				operandElement = function.apply( "("+expression+")" );
			} else {
				operandElement = "("+expression+")";
			}
			
			return conditionalOperator.apply(operandElement , rightOperandElements);
		
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to build inline calculated field clause", t);
		}
	}
	
	protected String[] buildOperand(Operand operand, Query query, Map entityAliasesMaps) {
		String[] operandElement;
		
		logger.debug("IN");
		
		try {
			Assert.assertNotNull(operand, "Input parameter [operand] cannot be null in order to execute method [buildUserProvidedWhereField]");
			operandElement = new String[] {""};
			
			if(parentStatement.OPERAND_TYPE_STATIC.equalsIgnoreCase(operand.type)) {
				operandElement = buildStaticOperand(operand);
			} else if (parentStatement.OPERAND_TYPE_SUBQUERY.equalsIgnoreCase(operand.type)) {
				operandElement = new String[] {buildQueryOperand(operand)};
			} else if (parentStatement.OPERAND_TYPE_SIMPLE_FIELD.equalsIgnoreCase(operand.type)
					|| parentStatement.OPERAND_TYPE_INLINE_CALCULATED_FIELD.equalsIgnoreCase(operand.type)) {
				operandElement = new String[] {buildFieldOperand(operand, query, entityAliasesMaps)};
			} else if (parentStatement.OPERAND_TYPE_PARENT_FIELD.equalsIgnoreCase(operand.type)) {
				operandElement = new String[] {buildParentFieldOperand(operand, query, entityAliasesMaps)};
			} else {
				// NOTE: OPERAND_TYPE_CALCULATED_FIELD cannot be used in where condition
				Assert.assertUnreachable("Invalid operand type [" + operand.type+ "]");
			}
		} finally {
			logger.debug("OUT");
		}		
		return operandElement;
	}
	
	protected String[] buildStaticOperand(Operand operand) {
		String[] operandElement;
		
		logger.debug("IN");
		
		try {
			operandElement = operand.values;
		} finally {
			logger.debug("OUT");
		}		
		
		return operandElement;
	}
	
	protected String buildQueryOperand(Operand operand) {
		String operandElement;
		
		logger.debug("IN");
		
		try {
			String subqueryId;
			
			logger.debug("where element right-hand field type [" + parentStatement.OPERAND_TYPE_SUBQUERY + "]");
			
			subqueryId = operand.values[0];
			logger.debug("Referenced subquery [" + subqueryId + "]");
			
			operandElement = "Q{" + subqueryId + "}";
			operandElement = "( " + operandElement + ")";
			logger.debug("where element right-hand field value [" + operandElement + "]");	
		} finally {
			logger.debug("OUT");
		}		
		
		return operandElement;
	}
	
	protected String buildFieldOperand(Operand operand, Query query, Map entityAliasesMaps) {
		String operandElement;
		IModelField datamartField;
		String fieldName;
		Map targetQueryEntityAliasesMap;
		
		logger.debug("IN");
		
		try {
			
			targetQueryEntityAliasesMap = (Map)entityAliasesMaps.get(query.getId());
			Assert.assertNotNull(targetQueryEntityAliasesMap, "Entity aliases map for query [" + query.getId() + "] cannot be null in order to execute method [buildUserProvidedWhereField]");
			
			datamartField = parentStatement.getDataSource().getModelStructure().getField( operand.values[0] );
			Assert.assertNotNull(datamartField, "DataMart does not cantain a field named [" + operand.values[0] + "]");

			fieldName = parentStatement.getFieldAliasWithRolesFromAlias(datamartField, targetQueryEntityAliasesMap, entityAliasesMaps, operand.alias);

			
			if (operand instanceof HavingField.Operand) {
				HavingField.Operand havingFieldOperand = (HavingField.Operand) operand;
				IAggregationFunction function = havingFieldOperand.function;
				operandElement = function.apply(fieldName);
			} else {
				operandElement = fieldName;
			}
			logger.debug("where element operand value [" + operandElement + "]");
		} finally {
			logger.debug("OUT");
		}		
		
		return operandElement;
	}
	
	String buildParentFieldOperand(Operand operand, Query query, Map entityAliasesMaps) {
		String operandElement;
		
		String[] chunks;
		String parentQueryId;
		String fieldName;
		IModelField datamartField;
		IModelEntity rootEntity;
		String queryName;
		String rootEntityAlias;
		
		
		logger.debug("IN");
		
		try {
			
			// it comes directly from the client side GUI. It is a composition of the parent query id and filed name, 
			// separated by a space
			logger.debug("operand  is equals to [" + operand.values[0] + "]");
			
			chunks = operand.values[0].split(" ");
			Assert.assertTrue(chunks.length >= 2, "Operand [" + chunks.toString() + "] does not contains enougth informations in order to resolve the reference to parent field");
			
			parentQueryId = chunks[0];
			logger.debug("where right-hand field belonging query [" + parentQueryId + "]");
			fieldName = chunks[1];
			logger.debug("where right-hand field unique name [" + fieldName + "]");

			datamartField = parentStatement.getDataSource().getModelStructure().getField( fieldName );
			Assert.assertNotNull(datamartField, "DataMart does not cantain a field named [" + fieldName + "]");
			
			Couple queryNameAndRoot = datamartField.getQueryName();
			
			queryName = (String) queryNameAndRoot.getFirst();
			logger.debug("select field query name [" + queryName + "]");
			
			if(queryNameAndRoot.getSecond()!=null){
				rootEntity = (IModelEntity)queryNameAndRoot.getSecond(); 	
			}else{
				rootEntity = datamartField.getParent().getRoot(); 	
			}
			logger.debug("where right-hand field query name [" + queryName + "]");
			logger.debug("where right-hand field root entity unique name [" + rootEntity.getUniqueName() + "]");
			
			Map parentEntityAliases = (Map)entityAliasesMaps.get(parentQueryId);
			if(parentEntityAliases != null) {
				if(!parentEntityAliases.containsKey(rootEntity.getUniqueName())) {
					Assert.assertUnreachable("Filter of subquery [" + query.getId() + "] refers to a non " +
							"existing parent query [" + parentQueryId + "] entity [" + rootEntity.getUniqueName() + "]");
				}
				rootEntityAlias = (String)parentEntityAliases.get( rootEntity.getUniqueName() );
			} else {
				rootEntityAlias = "unresoved_alias";
				logger.warn("Impossible to get aliases map for parent query [" + parentQueryId +"]. Probably the parent query ha not been compiled yet");					
				logger.warn("Query [" + query.getId() +"] refers entities of its parent query [" + parentQueryId +"] so the generated statement wont be executable until the parent query will be compiled");					
			}
			logger.debug("where right-hand field root entity alias [" + rootEntityAlias + "]");
			
			operandElement = rootEntityAlias + "." + queryName;
			logger.debug("where element right-hand field value [" + operandElement + "]");
		} finally {
			logger.debug("OUT");
		}		
		
		return operandElement;
	}
	
	protected String[] getTypeBoundedStaticOperand(Operand leadOperand, String operator, String[] operandValuesToBound) {
		String[] boundedValues = new String[operandValuesToBound.length];

		for (int i = 0; i < operandValuesToBound.length; i++) {
		
			String operandValueToBound = operandValuesToBound[i];
			String boundedValue = operandValueToBound;
			
		
			if (parentStatement.OPERAND_TYPE_INLINE_CALCULATED_FIELD.equalsIgnoreCase(leadOperand.type) ) {
				String leadOpernadValue = leadOperand.values[0];
				JSONObject leadOperandJSON = null;
				try {
					leadOperandJSON = new JSONObject(leadOpernadValue);
				} catch (Throwable t) {
					throw new RuntimeException("Impossible to parse operand value [" + leadOpernadValue + "]", t);
				}
				
				String type = leadOperandJSON.optString("type");
				boundedValue = getValueBounded(operandValueToBound, type);
			} else if (parentStatement.OPERAND_TYPE_SIMPLE_FIELD.equalsIgnoreCase(leadOperand.type) 
					|| parentStatement.OPERAND_TYPE_PARENT_FIELD.equalsIgnoreCase(leadOperand.type)) {
				
				IModelField datamartField = parentStatement.getDataSource().getModelStructure().getField(leadOperand.values[0]);
				boundedValue = getValueBounded(operandValueToBound, datamartField.getType());
			}

			boundedValues[i] = boundedValue;
		
		}
		
		return boundedValues;
	}
	
	String[] getTypeBoundedStaticOperand(String type, String[] operandValuesToBound) {
		String[] boundedValues = new String[operandValuesToBound.length];

		for (int i = 0; i < operandValuesToBound.length; i++) {
		
			String operandValueToBound = operandValuesToBound[i];
			String boundedValue = operandValueToBound;
			boundedValue = getValueBounded(operandValueToBound, type);
			boundedValues[i] = boundedValue;
		
		}
		
		return boundedValues;
	}
	
	public String getValueBounded(String operandValueToBound, String operandType) {
		
		String boundedValue = operandValueToBound;
		if (operandType.equalsIgnoreCase("STRING") || operandType.equalsIgnoreCase("CHARACTER") || operandType.equalsIgnoreCase("java.lang.String") || operandType.equalsIgnoreCase("java.lang.Character")) {
			
			// if the value is already surrounded by quotes, does not neither add quotes nor escape quotes 
			if ( StringUtils.isBounded(operandValueToBound, "'") ) {
				boundedValue = operandValueToBound ;
			} else {
				operandValueToBound = StringUtils.escapeQuotes(operandValueToBound);
				return StringUtils.bound(operandValueToBound, "'");
			}
		} else if( operandType.equalsIgnoreCase("DATE")  || operandType.equalsIgnoreCase("java.sql.date") || operandType.equalsIgnoreCase("java.util.date")){
			boundedValue = parseDate(operandValueToBound);
		} else if(operandType.equalsIgnoreCase("TIMESTAMP") || operandType.equalsIgnoreCase("java.sql.TIMESTAMP")){
			boundedValue = parseTimestamp(operandValueToBound);
		}
		
		
		return boundedValue;
	}
}
