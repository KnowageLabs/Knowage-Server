/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */


package it.eng.qbe.statement.runtime;

import java.util.List;
import java.util.Map;

import it.eng.qbe.query.HavingField;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.AbstractStatementFilteringClause;
import it.eng.qbe.statement.IConditionalOperator;
import it.eng.qbe.statement.jpa.JPQLStatementConditionalOperators;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */

public abstract class AbstractStatementHavingClause extends AbstractStatementFilteringClause{

	public static final String HAVING = "HAVING";
	
	public String buildClause(Query query, Map<String, Map<String, String>> entityAliasesMaps) {
		
		StringBuffer buffer = new StringBuffer();
		
		if( query.getHavingFields().size() > 0) {
			buffer.append(" " + HAVING + " ");
			
			List<HavingField> havingFields = query.getHavingFields();
			String booleanConnetor = "";
			for (HavingField havingField : havingFields) {
				
				buffer.append(" " + booleanConnetor + " ");
				
				String leftOperandType = havingField.getLeftOperand().type;
				if(havingField.getLeftOperand().values[0].contains("expression")){
					String havingClauseElement;
					
					IConditionalOperator conditionalOperator = null;
					conditionalOperator = (IConditionalOperator)JPQLStatementConditionalOperators.getOperator( havingField.getOperator() );
					Assert.assertNotNull(conditionalOperator, "Unsopported operator " + havingField.getOperator() + " used in query definition");

					havingClauseElement =  buildInLineCalculatedFieldClause(havingField.getOperator(), havingField.getLeftOperand(), havingField.isPromptable(), havingField.getRightOperand(), query, entityAliasesMaps, conditionalOperator);
					
					
					
					buffer.append(havingClauseElement);
				}else{
						buffer.append( buildHavingClauseElement(havingField, query, entityAliasesMaps) );
				}
				
				
				booleanConnetor =  havingField.getBooleanConnector();
				
			}
		}
		
		return buffer.toString().trim();
	}
	
	private String buildHavingClauseElement(HavingField havingField, Query query, Map entityAliasesMaps) {
		
		String havingClauseElement;
		String[] leftOperandElements;
		String[] rightOperandElements;
				
		logger.debug("IN");
		
		try {
			IConditionalOperator conditionalOperator = null;
			conditionalOperator = getOperator( havingField.getOperator() );
			Assert.assertNotNull(conditionalOperator, "Unsopported operator " + havingField.getOperator() + " used in query definition");
			
			leftOperandElements = buildOperand(havingField.getLeftOperand(), query, entityAliasesMaps);
			
			if (parentStatement.OPERAND_TYPE_STATIC.equalsIgnoreCase(havingField.getRightOperand().type) 
					&& havingField.isPromptable()) {
				// get last value first (the last value edited by the user)
				rightOperandElements = havingField.getRightOperand().lastValues;
			} else {
				rightOperandElements = buildOperand(havingField.getRightOperand(), query, entityAliasesMaps);
			}
			
			if (parentStatement.OPERAND_TYPE_STATIC.equalsIgnoreCase(havingField.getLeftOperand().type) )  {
				leftOperandElements = getTypeBoundedStaticOperand(havingField.getRightOperand(), havingField.getOperator(), leftOperandElements);
			}
			
			if (parentStatement.OPERAND_TYPE_STATIC.equalsIgnoreCase(havingField.getRightOperand().type) )  {
				rightOperandElements = getTypeBoundedStaticOperand(havingField.getLeftOperand(), havingField.getOperator(), rightOperandElements);
			}
			
			havingClauseElement = conditionalOperator.apply(leftOperandElements[0], rightOperandElements);
			logger.debug("Having clause element value [" + havingClauseElement + "]");
		} finally {
			logger.debug("OUT");
		}
		
		
		return  havingClauseElement;
	}
	
	public abstract IConditionalOperator getOperator(String operator);
	
}
