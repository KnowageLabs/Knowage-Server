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

package it.eng.knowage.initializer;

import it.eng.knowage.common.TestConstants;
import it.eng.spagobi.tools.datasource.bo.DataSource;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.Assert;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class TestConnectionFactory {

	public static String getDefaultCatalogue(TestConstants.DatabaseType type) {
		String catalogue;

		catalogue = null;
		switch (type) {
		case MYSQL:
			catalogue = TestConstants.MYSQL_DEFAULT_CATALOGUE;
			break;
		case POSTGRES:
			// catalogue = TestConstants.POSTGRES_DEFAULT_CATALOG;
			break;
		case ORACLE:
			// catalogue = TestConstants.ORACLE_DEFAULT_CATALOGUE;
			break;
		}

		return catalogue;
	}

	public static String getDefaultSchema(TestConstants.DatabaseType type) {
		String schema;

		schema = null;
		switch (type) {
		case MYSQL:
			schema = TestConstants.MYSQL_DEFAULT_SCHEMA;
			break;
		case POSTGRES:
			// schema = TestConstants.POSTGRES_DEFAULT_SCHEMA;
			break;
		case ORACLE:
			// schema = TestConstants.ORACLE_DEFAULT_SCHEMA;
			break;
		}

		return schema;
	}

	public static Connection createConnection(TestConstants.DatabaseType type) {
		Connection connection;
		DataSource ds = null;
		try {
			connection = null;
			switch (type) {
			case MYSQL:
				ds = DataSourceFactory.createDataSource(TestConstants.DatabaseType.MYSQL);
				Assert.assertNotNull("DataSource on MYSQL not defined correctly", ds);
				// connection = createConnection(TestConstants.MYSQL_URL, TestConstants.MYSQL_USER, TestConstants.MYSQL_PWD, TestConstants.MYSQL_DRIVER);
				break;
			case POSTGRES:
				// connection = createConnection(TestConstants.POSTGRES_URL, TestConstants.POSTGRES_USER, TestConstants.POSTGRES_PWD,
				// TestConstants.POSTGRES_DRIVER);
				ds = DataSourceFactory.createDataSource(TestConstants.DatabaseType.POSTGRES);
				Assert.assertNotNull("DataSource on MYSQL not defined correctly", ds);
				break;
			case ORACLE:
				// connection = createConnection(TestConstants.ORACLE_URL, TestConstants.ORACLE_USER, TestConstants.ORACLE_PWD, TestConstants.ORACLE_DRIVER);
				ds = DataSourceFactory.createDataSource(TestConstants.DatabaseType.ORACLE);
				Assert.assertNotNull("DataSource on MYSQL not defined correctly", ds);
				break;
			}
			connection = ds.getConnection();
		} catch (Throwable t) {
			throw new RuntimeException("Impossible get connection from Datasource", t);
		}
		return connection;
	}

	public static Connection createConnection(String url, String usr, String pwd, String driver) {
		Connection connection;

		connection = null;
		try {
			java.sql.Driver o = (java.sql.Driver) Class.forName(driver).newInstance();
			boolean b = o.acceptsURL(url);
			connection = DriverManager.getConnection(url, usr, pwd);
		} catch (Throwable t) {
			throw new RuntimeException("Impossible to create connection [url: " + url + "; usr: " + usr + "; pwd: " + pwd + "; driver: " + driver + "]", t);
		}
		return connection;
	}
}
