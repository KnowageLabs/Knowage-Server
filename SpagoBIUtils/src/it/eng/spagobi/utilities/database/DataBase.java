/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.utilities.database;

import it.eng.spagobi.tools.datasource.bo.IDataSource;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DataBase {
	public static final String DIALECT_MYSQL = "MySQL";
	public static final String DIALECT_POSTGRES = "PostgreSQL";
	public static final String DIALECT_ORACLE = "OracleDialect";
	public static final String DIALECT_ORACLE9i10g = "Oracle9Dialect";
	public static final String DIALECT_HSQL = "HSQL";
	public static final String DIALECT_HSQL_PRED = "Predefined hibernate dialect";
	public static final String DIALECT_SQLSERVER = "SQLServer";
	public static final String DIALECT_DB2 = "DB2";
	public static final String DIALECT_INGRES = "Ingres";
	public static final String DIALECT_TERADATA = "Teradata";
	public static final String DIALECT_VOLTDB = "VoltDB";
	
	public static IDataBase getDataBase(IDataSource dataSource) {
		IDataBase dataBase = null;
		String dialect = dataSource.getHibDialectClass();
		if(dialect.contains(DIALECT_MYSQL)) {
			dataBase = new MySQLDataBase(dataSource);
		} else if(dialect.contains(DIALECT_POSTGRES)) {
			dataBase = new PostgreSQLDataBase(dataSource);
		} else if(dialect.contains(DIALECT_ORACLE) || dialect.contains(DIALECT_ORACLE9i10g)) {
			dataBase = new OracleDataBase(dataSource);
		} else if(dialect.contains(DIALECT_HSQL) || dialect.contains(DIALECT_HSQL_PRED)) {
			dataBase = new HSQLDataBase(dataSource);
		} else if(dialect.contains(DIALECT_SQLSERVER)) {
			dataBase = new SQLServerDataBase(dataSource);
		} else if (dialect.contains(DIALECT_VOLTDB)) {
			dataBase = new VoltDBDataBase(dataSource);
		}
		
		return dataBase;
	}
}
