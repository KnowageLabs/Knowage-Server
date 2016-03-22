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
package it.eng.knowage.common;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class TestConstants {

	// public static String AF_CONFIG_FILE = "/WEB-INF/conf/master.xml";

	public enum DatabaseType {
		MYSQL, POSTGRES, ORACLE, SQLSERVER
	};

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

	public static String MYSQL_LABEL = "datasetTest_mysql_read";
	public static String MYSQL_URL = "jdbc:mysql://localhost/meta_test";
	public static String MYSQL_USER = "root";
	public static String MYSQL_PWD = "root";
	// public static String MYSQL_URL = "jdbc:mysql://172.27.1.83:3306/spagobi_testr";
	// public static String MYSQL_USER = "spagobi";
	// public static String MYSQL_PWD = "bispago";

	public static String MYSQL_DEFAULT_CATALOGUE = "meta_test";
	public static String MYSQL_DEFAULT_SCHEMA = null;
	public static String MYSQL_DEFAULT_DIALECT = "org.hibernate.dialect.MySQLDialect";
	public static String[] MYSQL_TABLE_NAMES = new String[] { "_product_class_", "currency", "currency_view", "customer", "days", "department", "employee",
			"employee_closure", "inventory_fact_1998", "object", "position", "product", "product_class", "promotion", "region", "reserve_employee", "salary",
			"sales_fact_1998", "sales_region", "store", "store_ragged", "test_names", "test_types", "time_by_day", "warehouse", "warehouse_class" };

	public static String[] MYSQL_FILTERED_TABLES_FOR_PMODEL = { "_product_class_", "product", "product_class", "sales_fact_1998", "currency_view",
			"department", "employee", "employee_closure", "object", "position", "reserve_employee", "salary" };

	// must be a subset of MYSQL_FILTERED_TABLES_FOR_PMODEL
	public static String[] MYSQL_FILTERED_TABLES_FOR_BMODEL = { "_product_class_", "product", "product_class", "sales_fact_1998", "object" };

	// =======================================================
	// POSTGRES
	// =======================================================
	public static String POSTGRES_DRIVER = "org.postgresql.Driver";
	public static String POSTGRES_DIALECT_CLASS = "org.hibernate.dialect.PostgreSQLDialect";
	public static String POSTGRES_DIALECT_NAME = "sbidomains.nm.postgresql";

	public static String POSTGRES_LABEL = "datasetTest_postgres_read";
	public static String POSTGRES_URL = "jdbc:postgresql://172.27.1.83:5432/spagobi_testr";
	public static String POSTGRES_USER = "spagobi";
	public static String POSTGRES_PWD = "spagobi";

	public static String POSTGRES_DEFAULT_CATALOG = "Meta_tesT";
	public static String POSTGRES_DEFAULT_SCHEMA = "public";
	public static String POSTGRES_DEFAULT_DIALECT = "org.hibernate.dialect.PostgreSQLDialect";

	public static String[] POSTGRES_TABLE_NAMES = new String[] { "_Product_ClaSS_", "currency", "cUrReNcY", "customer", "days", "department", "employee",
			"employee_closure",
			"inventory_fact_1998"
			// , "object"
			, "position", "product", "product_class", "promotion", "region", "reserve_employee", "salary", "sales_fact_1998", "sales_region", "STORE",
			"store_ragged"
			// , "test_names"
			, "time_by_day", "warehouse", "warehouse_class" };
	public static final String[] POSTGRES_FILTERED_TABLES_FOR_PMODEL = { "_Product_ClaSS_", "product", "product_class", "sales_fact_1998", "cUrReNcY",
			"department", "employee", "employee_closure", "position", "reserve_employee", "salary", // "object"
	};

	public static final String[] POSTGRES_FILTERED_TABLES_FOR_BMODEL = { "_Product_ClaSS_", "product", "product_class", "sales_fact_1998"// , "object"
	};

	// =======================================================
	// ORACLE
	// =======================================================
	public static String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
	public static String ORACLE_DIALECT_CLASS = "org.hibernate.dialect.Oracle9Dialect";
	public static String ORACLE_DIALECT_NAME = "sbidomains.nm.oracle_9i10g";

	public static String ORACLE_LABEL = "datasetTest_oracle_read";
	public static String ORACLE_URL = "jdbc:oracle:thin:@172.27.1.83:1521:repo";
	public static String ORACLE_USER = "spagobi_testr";
	public static String ORACLE_PWD = "spagobi_testr";

	public static String ORACLE_DEFAULT_CATALOGUE = null;
	public static String ORACLE_DEFAULT_SCHEMA = "SPAGOBI";
	public static String ORACLE_DEFAULT_DIALECT = "org.hibernate.dialect.OracleDialect";

	public static final String[] ORACLE_FILTERED_TABLES_FOR_PMODEL = MYSQL_FILTERED_TABLES_FOR_PMODEL;
	public static final String[] ORACLE_FILTERED_TABLES_FOR_BMODEL = MYSQL_FILTERED_TABLES_FOR_BMODEL;

	// =======================================================
	// SQL SERVER
	// =======================================================
	public static String SQLSERVER_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static String SQLSERVER_DIALECT_CLASS = "org.hibernate.dialect.SQLServerDialect";
	public static String SQLSERVER_DIALECT_NAME = "sbidomains.nm.sqlserver";

	public static String SQLSERVER_LABEL = "datasetTest_sqlserver_read";
	public static String SQLSERVER_URL = "jdbc:sqlserver://172.27.1.80:1433;databaseName=testSpagoBI;schema=spagobi_testr";
	public static String SQLSERVER_USER = "spagobi";
	public static String SQLSERVER_PWD = "bispago";

}
