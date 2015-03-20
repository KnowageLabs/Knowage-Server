/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.jpa;

import it.eng.qbe.query.Query;
import it.eng.qbe.statement.AbstractStatementWhereClause;
import it.eng.qbe.statement.IConditionalOperator;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class builds the where clause part of the statement
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JPQLStatementWhereClause extends AbstractStatementWhereClause {
	

public static transient Logger logger = Logger.getLogger(JPQLStatementWhereClause.class);
	
	public static String build(JPQLStatement parentStatement, Query query, Map<String, Map<String, String>> entityAliasesMaps){
		JPQLStatementWhereClause clause = new JPQLStatementWhereClause(parentStatement);
		return clause.buildClause(query, entityAliasesMaps);
	}
	
	public static String fix(JPQLStatement parentStatement, String whereClause, Query query, Map<String, Map<String, String>> entityAliasesMaps){
		JPQLStatementWhereClause clause = new JPQLStatementWhereClause(parentStatement);
		return clause.fixWhereClause(whereClause, query, entityAliasesMaps);
	}
	
	static boolean injectWhereClausesEnabled = true;
	
	public static String injectAutoJoins(JPQLStatement parentStatement, String whereClause, Query query, Map<String, Map<String, String>> entityAliasesMaps){
		
		String modifiedWhereClause;
		
		logger.debug("IN");
		
		modifiedWhereClause = null;
		try {
			if(injectWhereClausesEnabled == true) {
				logger.debug("Auto join functionality is enabled");
				logger.debug("Original where clause is equal to [" + whereClause + "]");
				JPQLStatementWhereClause clause = new JPQLStatementWhereClause(parentStatement);
				modifiedWhereClause = clause.injectAutoJoins(whereClause, query, entityAliasesMaps);
				logger.debug("Modified where clause is equal to [" + modifiedWhereClause + "]");
			} else {
				logger.warn("Auto join functionality is not enabled");
				modifiedWhereClause = whereClause;
				logger.debug("Where clause not modified");
			}
		
			return modifiedWhereClause;
		} catch(Throwable t) {
			throw new RuntimeException("An unexpected error occured while injecting auto joins in where conditions", t);
		} finally {
			logger.debug("OUT");
		}
		
		
	}
	
	protected JPQLStatementWhereClause(JPQLStatement statement) {
		parentStatement = statement;
	}
	
	
	public IConditionalOperator getOperator(String operator){
		return (IConditionalOperator)JPQLStatementConditionalOperators.getOperator( operator );
	}


}
