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

import it.eng.spagobi.tools.datasource.bo.DataSource;

import java.util.Random;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 * 
 */
public class DataSourceFactory {

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
