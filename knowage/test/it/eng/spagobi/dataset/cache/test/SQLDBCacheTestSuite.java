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
package it.eng.spagobi.dataset.cache.test;


import it.eng.spagobi.dataset.cache.impl.sqldbcache.test.hsqldb.HsqldbSQLDBCacheTest;
import it.eng.spagobi.dataset.cache.impl.sqldbcache.test.mysql.MySqlSQLDBCacheTest;
import it.eng.spagobi.dataset.cache.impl.sqldbcache.test.oracle.OracleSQLDBCacheTest;
import it.eng.spagobi.dataset.cache.impl.sqldbcache.test.postgres.PostgresSQLDBCacheTest;
import it.eng.spagobi.dataset.cache.impl.sqldbcache.test.sqlserver.SQLServerSQLDBCacheTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class SQLDBCacheTestSuite extends TestCase {
	static public Test suite() {
		TestSuite suite = new TestSuite("SQLDBCache tests");
		if(TestConstants.enableTestsOnMySql){
			suite.addTestSuite(MySqlSQLDBCacheTest.class);
		}
		if(TestConstants.enableTestsOnPostgres){
			suite.addTestSuite(PostgresSQLDBCacheTest.class);
		}
		if(TestConstants.enableTestsOnOracle){
			suite.addTestSuite(OracleSQLDBCacheTest.class);
		}
		if(TestConstants.enableTestsOnSQLServer){
			suite.addTestSuite(SQLServerSQLDBCacheTest.class);
		}
		if(TestConstants.enableTestsOnHSQLDB){
			suite.addTestSuite(HsqldbSQLDBCacheTest.class);
		}
		return suite;
	}
}
