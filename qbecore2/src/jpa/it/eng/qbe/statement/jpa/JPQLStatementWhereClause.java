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
	
	
	public JPQLStatementWhereClause(JPQLStatement statement) {
		parentStatement = statement;
	}
	
	
	public IConditionalOperator getOperator(String operator){
		return (IConditionalOperator)JPQLStatementConditionalOperators.getOperator( operator );
	}


}
