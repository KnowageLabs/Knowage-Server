/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.dataset.cache.test;

import it.eng.spagobi.tools.datasource.bo.DataSource;


import java.util.Random;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class TestDataSourceFactory {
	
	public static DataSource createDataSource(TestConstants.DatabaseType type, boolean isWritingDatasource) {
		DataSource dataSource;
		
		dataSource = null;
		switch(type) {
	        case MYSQL:   
	        	if (isWritingDatasource){
		        	dataSource = createDataSource(TestConstants.MYSQL_LABEL_WRITING, TestConstants.MYSQL_URL_WRITING, TestConstants.MYSQL_USER_WRITING, TestConstants.MYSQL_PWD_WRITING,
		        						TestConstants.MYSQL_DRIVER, TestConstants.MYSQL_DIALECT_CLASS ,TestConstants.MYSQL_DIALECT_NAME, false, true );
	        	} else {
		        	dataSource = createDataSource(TestConstants.MYSQL_LABEL_READING, TestConstants.MYSQL_URL_READING, TestConstants.MYSQL_USER_READING, TestConstants.MYSQL_PWD_READING,
    						TestConstants.MYSQL_DRIVER, TestConstants.MYSQL_DIALECT_CLASS ,TestConstants.MYSQL_DIALECT_NAME, true, false );
	        	}
	        	break;
	        case POSTGRES:  
	        	if (isWritingDatasource){
		        	dataSource = createDataSource(TestConstants.POSTGRES_LABEL_WRITING, TestConstants.POSTGRES_URL_WRITING, TestConstants.POSTGRES_USER_WRITING, TestConstants.POSTGRES_PWD_WRITING,
		        						TestConstants.POSTGRES_DRIVER, TestConstants.POSTGRES_DIALECT_CLASS ,TestConstants.POSTGRES_DIALECT_NAME, false, true );
	        	} else {
		        	dataSource = createDataSource(TestConstants.POSTGRES_LABEL_READING, TestConstants.POSTGRES_URL_READING, TestConstants.POSTGRES_USER_READING, TestConstants.POSTGRES_PWD_READING,
    						TestConstants.POSTGRES_DRIVER, TestConstants.POSTGRES_DIALECT_CLASS ,TestConstants.POSTGRES_DIALECT_NAME, true, false );
	        	}
	        	break;

	        case ORACLE:  
	        	if (isWritingDatasource){
		        	dataSource = createDataSource(TestConstants.ORACLE_LABEL_WRITING, TestConstants.ORACLE_URL_WRITING, TestConstants.ORACLE_USER_WRITING, TestConstants.ORACLE_PWD_WRITING,
		        						TestConstants.ORACLE_DRIVER, TestConstants.ORACLE_DIALECT_CLASS ,TestConstants.ORACLE_DIALECT_NAME, false, true );
	        	} else {
		        	dataSource = createDataSource(TestConstants.ORACLE_LABEL_READING, TestConstants.ORACLE_URL_READING, TestConstants.ORACLE_USER_READING, TestConstants.ORACLE_PWD_READING,
    						TestConstants.ORACLE_DRIVER, TestConstants.ORACLE_DIALECT_CLASS ,TestConstants.ORACLE_DIALECT_NAME, true, false );
	        	}
	        	break;
	        case SQLSERVER:  
	        	if (isWritingDatasource){
		        	dataSource = createDataSource(TestConstants.SQLSERVER_LABEL_WRITING, TestConstants.SQLSERVER_URL_WRITING, TestConstants.SQLSERVER_USER_WRITING, TestConstants.SQLSERVER_PWD_WRITING,
		        						TestConstants.SQLSERVER_DRIVER, TestConstants.SQLSERVER_DIALECT_CLASS ,TestConstants.SQLSERVER_DIALECT_NAME, false, true );
	        	} else {
		        	dataSource = createDataSource(TestConstants.SQLSERVER_LABEL_READING, TestConstants.SQLSERVER_URL_READING, TestConstants.SQLSERVER_USER_READING, TestConstants.SQLSERVER_PWD_READING,
    						TestConstants.SQLSERVER_DRIVER, TestConstants.SQLSERVER_DIALECT_CLASS ,TestConstants.SQLSERVER_DIALECT_NAME, true, false );
	        	}
	        	break;
	        case HSQLDB:
	        	if (isWritingDatasource){
		        	dataSource = createDataSource(TestConstants.HSQLDB_LABEL_WRITING, TestConstants.HSQLDB_URL_WRITING, TestConstants.HSQLDB_USER_WRITING, TestConstants.HSQLDB_PWD_WRITING,
		        						TestConstants.HSQLDB_DRIVER, TestConstants.HSQLDB_DIALECT_CLASS ,TestConstants.HSQLDB_DIALECT_NAME, false, true );
	        	} else {
		        	dataSource = createDataSource(TestConstants.HSQLDB_LABEL_READING, TestConstants.HSQLDB_URL_READING, TestConstants.HSQLDB_USER_READING, TestConstants.HSQLDB_PWD_READING,
    						TestConstants.HSQLDB_DRIVER, TestConstants.HSQLDB_DIALECT_CLASS ,TestConstants.HSQLDB_DIALECT_NAME, true, false );
	        	}
	        	break;
		}
		
		return dataSource;
	}
	
	public static DataSource createDataSource(String label, String url,String user,String password, String driver, String hibDialectClass, String hibDialectName, boolean isReadOnly, boolean isWriteDefault ){
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
		//EXAMPLE for reference
		dataSourceFoodmart = new DataSource();
		
		dataSourceFoodmart.setDsId(999999);
		dataSourceFoodmart.setLabel("datasetTest_foodmart");
		dataSourceFoodmart.setDescr("datasetTest_foodmart");
		dataSourceFoodmart.setJndi("");
		dataSourceFoodmart.setUrlConnection("jdbc:mysql://localhost:3306/foodmart");
		dataSourceFoodmart.setUser("root");
		dataSourceFoodmart.setPwd("root");
		dataSourceFoodmart.setDriver("com.mysql.jdbc.Driver");
		//dataSourceFoodmart.setDialectId(hibDataSource.getDialect().getValueId());
		//dataSourceFoodmart.setEngines(hibDataSource.getSbiEngineses());
		//dataSourceFoodmart.setObjects(hibDataSource.getSbiObjectses());
		dataSourceFoodmart.setSchemaAttribute("");
		dataSourceFoodmart.setMultiSchema(false);
		dataSourceFoodmart.setHibDialectClass("org.hibernate.dialect.MySQLInnoDBDialect");
		dataSourceFoodmart.setHibDialectName("sbidomains.nm.mysql");
		dataSourceFoodmart.setReadOnly(false);
		dataSourceFoodmart.setWriteDefault(false);
		*/
		
		return dataSource;
	}

}
