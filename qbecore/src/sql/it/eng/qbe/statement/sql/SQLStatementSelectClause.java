/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.qbe.statement.sql;

import it.eng.qbe.datasource.dataset.DataSetDataSource;
import it.eng.qbe.query.Query;
import it.eng.qbe.statement.AbstractSelectStatementClause;
import it.eng.qbe.statement.IStatement;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;

import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */

public class SQLStatementSelectClause extends AbstractSelectStatementClause {
	

	 
	public static transient Logger logger = Logger.getLogger(SQLStatementSelectClause.class);
	
//	public static String build(IStatement parentStatement, Query query, Map<String, Map<String, String>> entityAliasesMaps){
//		SQLStatementSelectClause clause = new SQLStatementSelectClause(parentStatement);
//		return clause.buildClause(query, entityAliasesMaps);
//	}
	
	protected SQLStatementSelectClause(IStatement statement) {
		parentStatement = statement;
	}
	

	public static String build(IStatement parentStatement, Query query, Map<String, Map<String, String>> entityAliasesMaps, boolean useAliases){
		SQLStatementSelectClause clause = new SQLStatementSelectClause(parentStatement);
		return clause.buildClause(query, entityAliasesMaps, useAliases);
	}


	@Override
	protected String encapsulate(String alias) {
		// in case of DataSetDataSource, we need to encapsulate alias between quotes (example: Customer id --> "Customer id" in most databases)
		DataSetDataSource dataSource = (DataSetDataSource) this.parentStatement.getDataSource();
		it.eng.spagobi.tools.datasource.bo.IDataSource datasourceForReading = dataSource.getDataSourceForReading();
		return AbstractJDBCDataset.encapsulateColumnName(alias, datasourceForReading);
	}
	
	
	
}