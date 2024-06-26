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
package it.eng.spagobi.tools.dataset.dao;

import java.math.BigDecimal;


/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class TestConstants {
	
	public static String workspaceFolder = "D:/Documenti/Sviluppo/workspaces/helios/spagobi/server";
	//public static String workspaceFolder = "C:/Users/cortella/workspaceJEE";

	public static String RESOURCE_PATH = workspaceFolder + "D:/Documenti/Sviluppo/servers/tomcat6spagobi3postgres9.0/resources";
	
	public enum DatabaseType { MYSQL, POSTGRES, ORACLE, SQLSERVER };
	
	public static boolean enableTestsOnMySql = true;
	public static boolean enableTestsOnPostgres = true;
	public static boolean enableTestsOnOracle = true;
	public static boolean enableTestsOnSQLServer = true;



	// =======================================================
	// MYSQL 
	// =======================================================
	public static String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
	public static String MYSQL_DIALECT_CLASS = "org.hibernate.dialect.MySQLInnoDBDialect";
	public static String MYSQL_DIALECT_NAME = "sbidomains.nm.mysql";

	//-------------
	// FOR WRITING
	//-------------
	public static String MYSQL_LABEL_WRITING = "datasetTest_mysql_write";
	public static String MYSQL_URL_WRITING = "jdbc:mysql://172.27.1.83:3306/spagobi_testw";
	public static String MYSQL_USER_WRITING = "spagobi";
	public static String MYSQL_PWD_WRITING = "bispago";
	
	//-------------
	// FOR READING
	//-------------
	public static String MYSQL_LABEL_READING = "datasetTest_mysql_read";
	public static String MYSQL_URL_READING = "jdbc:mysql://172.27.1.83:3306/spagobi_testr";
	public static String MYSQL_USER_READING = "spagobi";
	public static String MYSQL_PWD_READING = "bispago";
	
	// =======================================================
	// POSTGRES
	// =======================================================
	public static String POSTGRES_DRIVER = "org.postgresql.Driver";
	public static String POSTGRES_DIALECT_CLASS = "org.hibernate.dialect.PostgreSQLDialect";
	public static String POSTGRES_DIALECT_NAME = "sbidomains.nm.postgresql";

	//-------------
	// FOR WRITING
	//-------------
	public static String POSTGRES_LABEL_WRITING = "datasetTest_postgres_write";
	public static String POSTGRES_URL_WRITING = "jdbc:postgresql://localhost:5433/spagobi";
	public static String POSTGRES_USER_WRITING = "postgres";
	public static String POSTGRES_PWD_WRITING = "postgres";
	
	//-------------
	// FOR READING
	//-------------
	public static String POSTGRES_LABEL_READING = "datasetTest_postgres_read";
	public static String POSTGRES_URL_READING = "jdbc:postgresql://localhost:5433/spagobi";
	public static String POSTGRES_USER_READING = "postgres";
	public static String POSTGRES_PWD_READING = "postgres";
	
	// =======================================================
	// ORACLE
	// =======================================================
	public static String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
	public static String ORACLE_DIALECT_CLASS = "org.hibernate.dialect.Oracle9Dialect";
	public static String ORACLE_DIALECT_NAME = "sbidomains.nm.oracle_9i10g";

	//-------------
	// FOR WRITING
	//-------------
	public static String ORACLE_LABEL_WRITING = "datasetTest_oracle_write";
	public static String ORACLE_URL_WRITING = "jdbc:oracle:thin:@172.27.1.83:1521:repo"; //sibilla2
	public static String ORACLE_USER_WRITING = "spagobi_testw";
	public static String ORACLE_PWD_WRITING = "spagobi_testw";
	
	//-------------
	// FOR READING
	//-------------
	public static String ORACLE_LABEL_READING = "datasetTest_oracle_read";
	public static String ORACLE_URL_READING = "jdbc:oracle:thin:@172.27.1.83:1521:repo";
	public static String ORACLE_USER_READING = "spagobi_testr";
	public static String ORACLE_PWD_READING = "spagobi_testr";
	
	// =======================================================
	// SQL SERVER
	// =======================================================
	public static String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static String SQLSERVER_DIALECT_CLASS = "org.hibernate.dialect.SQLServerDialect";
	public static String SQLSERVER_DIALECT_NAME = "sbidomains.nm.sqlserver";

	//-------------
	// FOR WRITING
	//-------------
	public static String SQLSERVER_LABEL_WRITING = "datasetTest_sqlserver_write";
	public static String SQLSERVER_URL_WRITING = "jdbc:sqlserver://172.27.1.80:1433;databaseName=testSpagoBI;schema=spagobi_testw"; //server Padova
	public static String SQLSERVER_USER_WRITING = "spagobi";
	public static String SQLSERVER_PWD_WRITING = "bispago";
	
	//-------------
	// FOR READING
	//-------------
	public static String SQLSERVER_LABEL_READING = "datasetTest_sqlserver_read";
	public static String SQLSERVER_URL_READING = "jdbc:sqlserver://172.27.1.80:1433;databaseName=testSpagoBI;schema=spagobi_testr";
	public static String SQLSERVER_USER_READING = "spagobi";
	public static String SQLSERVER_PWD_READING = "bispago";
}
