/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.hibernate;

import it.eng.qbe.datasource.AbstractDataSourceTestCase;
import it.eng.qbe.datasource.DriverManager;
import it.eng.qbe.datasource.configuration.FileDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;

import java.io.File;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class StandardHibernateDataSourceTestCase extends AbstractDataSourceTestCase {
	
	private static final String QBE_FILE = "test-resources/hibernate/foodmart/datamart.jar";
	
	@Override
	protected void setUpDataSource() {
		IDataSourceConfiguration configuration;
		
		modelName = "foodmart";
		
		File file = new File(QBE_FILE);
		configuration = new FileDataSourceConfiguration(modelName, file);
		configuration.loadDataSourceProperties().put("connection", connection);
		dataSource = DriverManager.getDataSource(HibernateDriver.DRIVER_ID, configuration);
		
		testEntityUniqueName = "it.eng.spagobi.Customer::Customer";
	}
	
	public void testHibernateImpl() {
		doTests();
	}
	
	public void doTests() {
		super.doTests();
		// add custom tests here
		doTestDataSourceImplementation();
	}
	
	// add Hibernate specific tests here ...
	
	public void doTestDataSourceImplementation() {
		assertTrue(dataSource instanceof HibernateDataSource);
	}
}
