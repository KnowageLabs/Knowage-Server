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
package it.eng.qbe.datasource.jpa.views;

import it.eng.qbe.datasource.DriverManager;
import it.eng.qbe.datasource.configuration.FileDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.datasource.jpa.JPADriver;

import java.io.File;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class NoKeyJpaDataSourceTestCase extends AbstractViewJpaDataSourceTestCase {
	
	private static final String QBE_FILE = "test-resources/jpa/views/noKey/dist/datamart.jar";
	
	@Override
	protected void setUpDataSource() {
		IDataSourceConfiguration configuration;
		
		modelName = "My Model";  
		
		File file = new File(QBE_FILE);
		configuration = new FileDataSourceConfiguration(modelName, file);
		configuration.loadDataSourceProperties().put("connection", connection);
		dataSource = DriverManager.getDataSource(JPADriver.DRIVER_ID, configuration, false);
	}
	
	public void testQbeWithView() {
		doTests() ;
	}
	
	public void doTests() {
		super.doTests();
		// add custom tests here
	}
	
	
}
