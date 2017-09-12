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