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

import java.util.Random;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class DataSourceFactory {

	public static DataSource createDataSource(TestConstants.DatabaseType type) {
		DataSource dataSource;

		dataSource = null;

		switch (type) {
		case MYSQL:
			dataSource = createDataSource(TestConstants.MYSQL_LABEL, TestConstants.MYSQL_URL, TestConstants.MYSQL_USER, TestConstants.MYSQL_PWD,
					TestConstants.MYSQL_DRIVER, TestConstants.MYSQL_DIALECT_CLASS, TestConstants.MYSQL_DIALECT_NAME, true, false);
			break;
		case POSTGRES:
			dataSource = createDataSource(TestConstants.POSTGRES_LABEL, TestConstants.POSTGRES_URL, TestConstants.POSTGRES_USER, TestConstants.POSTGRES_PWD,
					TestConstants.POSTGRES_DRIVER, TestConstants.POSTGRES_DIALECT_CLASS, TestConstants.POSTGRES_DIALECT_NAME, true, false);
			break;
		case ORACLE:
			dataSource = createDataSource(TestConstants.ORACLE_LABEL, TestConstants.ORACLE_URL, TestConstants.ORACLE_USER, TestConstants.ORACLE_PWD,
					TestConstants.ORACLE_DRIVER, TestConstants.ORACLE_DIALECT_CLASS, TestConstants.ORACLE_DIALECT_NAME, true, false);
			break;
		case SQLSERVER:
			dataSource = createDataSource(TestConstants.SQLSERVER_LABEL, TestConstants.SQLSERVER_URL, TestConstants.SQLSERVER_USER,
					TestConstants.SQLSERVER_PWD, TestConstants.SQLSERVER_DRIVER, TestConstants.SQLSERVER_DIALECT_CLASS, TestConstants.SQLSERVER_DIALECT_NAME,
					true, false);
			break;
		}

		return dataSource;
	}

	public static DataSource createDataSource(String label, String url, String user, String password, String driver, String hibDialectClass,
			String hibDialectName, boolean isReadOnly, boolean isWriteDefault) {
		DataSource dataSource = new DataSource();
		Random rand = new Random();

		int id = rand.nextInt(999999) + 1;
		dataSource.setDsId(id);
		dataSource.setLabel(label);
		dataSource.setDescr(label);
		dataSource.setJndi("");
		dataSource.setUrlConnection(url);
		dataSource.setUser(user);
		dataSource.setPwd(password);
		dataSource.setDriver(driver);
		dataSource.setSchemaAttribute("");
		dataSource.setMultiSchema(false);
		dataSource.setHibDialectClass(hibDialectClass);
		dataSource.setHibDialectName(hibDialectName);
		dataSource.setReadOnly(isReadOnly);
		dataSource.setWriteDefault(isWriteDefault);

		/*
		 * //EXAMPLE for reference dataSourceFoodmart = new DataSource();
		 *
		 * dataSourceFoodmart.setDsId(999999); dataSourceFoodmart.setLabel("datasetTest_foodmart"); dataSourceFoodmart.setDescr("datasetTest_foodmart");
		 * dataSourceFoodmart.setJndi(""); dataSourceFoodmart.setUrlConnection("jdbc:mysql://localhost:3306/foodmart"); dataSourceFoodmart.setUser("root");
		 * dataSourceFoodmart.setPwd("root"); dataSourceFoodmart.setDriver("com.mysql.jdbc.Driver");
		 * //dataSourceFoodmart.setDialectId(hibDataSource.getDialect().getValueId()); //dataSourceFoodmart.setEngines(hibDataSource.getSbiEngineses());
		 * //dataSourceFoodmart.setObjects(hibDataSource.getSbiObjectses()); dataSourceFoodmart.setSchemaAttribute("");
		 * dataSourceFoodmart.setMultiSchema(false); dataSourceFoodmart.setHibDialectClass("org.hibernate.dialect.MySQLInnoDBDialect");
		 * dataSourceFoodmart.setHibDialectName("sbidomains.nm.mysql"); dataSourceFoodmart.setReadOnly(false); dataSourceFoodmart.setWriteDefault(false);
		 */

		return dataSource;
	}

}
