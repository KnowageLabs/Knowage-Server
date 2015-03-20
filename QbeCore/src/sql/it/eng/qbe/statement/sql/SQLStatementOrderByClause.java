/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.qbe.statement.sql;

import it.eng.qbe.query.Query;
import it.eng.qbe.statement.AbstractStatementOrderByClause;
import it.eng.qbe.statement.jpa.JPQLStatementOrderByClause;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Alberto Ghedin (arberto.ghedin@eng.it)
 *
 */

public class SQLStatementOrderByClause  extends AbstractStatementOrderByClause {
	
	public static transient Logger logger = Logger.getLogger(JPQLStatementOrderByClause.class);
	
	public static String build(SQLStatement parentStatement, Query query, Map<String, Map<String, String>> entityAliasesMaps){
		SQLStatementOrderByClause clause = new SQLStatementOrderByClause(parentStatement);
		return clause.buildClause(query, entityAliasesMaps);
	}
	
	protected SQLStatementOrderByClause(SQLStatement statement) {
		parentStatement = statement;
	}
	

	
	
	
}
