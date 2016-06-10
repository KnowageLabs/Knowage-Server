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

import java.io.File;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 * 
 */
public class TestConstants {

	public enum DatabaseType {
		MYSQL, POSTGRES, ORACLE, SQLSERVER
	};

	public static Integer datasourceId = 1;
	public static String[] tables = { "_product_class_", "currency", "currency_view", "customer", "days", "department", "employee", "employee_closure",
			"inventory_fact_1998", "object", "position", "product", "product_class", "promotion", "region", "reserve_employee", "salary", "sales_fact_1998",
			"sales_region", "store", "store_ragged", "test_names", "test_types", "time_by_day", "warehouse", "warehouse_class" };

	public static String[] selectedTables = { "currency", "customer", "days", "department" };

	public static String[] selectedPhysical = { "currency" };

	// public static String AF_CONFIG_FILE = "/WEB-INF/conf/master.xml";

	public static File workspaceFolder = new File("D:/Sviluppo/Athena/knowagemeta-unit-test/workspaces/metadata");
	public static File outputFolder = new File("D:/Sviluppo/Athena/knowagemeta-unit-test/workspaces/unit-test");
	public static File libFolder = new File("D:/Sviluppo/Athena/knowagemeta-unit-test/libs");

	public static boolean enableTestsOnMySql = true;
	public static boolean enableTestsOnPostgres = true;
	public static boolean enableTestsOnOracle = true;
	public static boolean enableTestsOnSQLServer = true;

	public static String MYSQL_DEFAULT_SCHEMA = null;
	public static String POSTGRES_DEFAULT_SCHEMA = "public";
	public static String ORACLE_DEFAULT_SCHEMA = "SPAGOBI";

}
