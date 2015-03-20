/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.jpa.query;

import it.eng.qbe.datasource.AbstractDataSourceTestCase;
import it.eng.qbe.datasource.DriverManager;
import it.eng.qbe.datasource.configuration.FileDataSourceConfiguration;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.datasource.jpa.IJpaDataSource;
import it.eng.qbe.datasource.jpa.JPADriver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class QueryDataSourceTestCase extends AbstractDataSourceTestCase {
	
	private static final String QBE_FILE = "test-resources/jpa/query/datamart.jar";
	
	@Override
	protected void setUpDataSource() {
		IDataSourceConfiguration configuration;
		
		modelName = "foodmart_1307532444533";  
		
		File file = new File(QBE_FILE);
		configuration = new FileDataSourceConfiguration(modelName, file);
		configuration.loadDataSourceProperties().put("connection", connection);
		dataSource = DriverManager.getDataSource(JPADriver.DRIVER_ID, configuration, false);
	}
	
	public void testQbeWithKeys() {
		doTests() ;
	}
	
	public void doTests() {
		super.doTests();
		doTestCustomQuery();
	}

	private void doTestCustomQuery() {
		
		javax.persistence.Query jpqlQuery;
		String statementStr = "SELECT t_0.storeId.storeCountry FROM  SalesFact1998 t_0 WHERE  t_0.timeId=t_0.timeId ";
		EntityManager entityManager = ((IJpaDataSource)dataSource).getEntityManager();
		
		try {
			jpqlQuery = entityManager.createQuery( statementStr );
		} catch (Throwable t) {
			System.err.println("statementStr: " + statementStr);
			System.err.println(t.getMessage());
			throw new RuntimeException("Impossible to compile query statement [" + statementStr + "]", t);
		}
			
		
		List result = null;

		try {
			result = jpqlQuery.getResultList();
		} catch (Throwable t) {
			System.err.println("statementStr: " + statementStr);
			System.err.println("statementStr: " + t.getMessage());
			throw new RuntimeException("Impossible to execute statement [" + statementStr + "]", t);
		}
		
	}
}
