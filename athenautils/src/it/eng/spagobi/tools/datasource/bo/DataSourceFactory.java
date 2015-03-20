/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.datasource.bo;

import it.eng.spagobi.services.datasource.bo.SpagoBiDataSource;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class DataSourceFactory {
	
	private static transient Logger logger = Logger.getLogger(DataSourceFactory.class);
	
	public static IDataSource getDataSource( SpagoBiDataSource dataSourceConfig ) {
		IDataSource dataSource = null;
				
		if (dataSourceConfig == null) {
			throw new IllegalArgumentException("datasource-config parameter cannot be null");
		}
		
		dataSource = new DataSource();
		
		dataSource.setDsId(dataSourceConfig.getId());
		dataSource.setDriver( dataSourceConfig.getDriver() );
		dataSource.setJndi( dataSourceConfig.getJndiName() );
		dataSource.setLabel( dataSourceConfig.getLabel() );
		dataSource.setPwd( dataSourceConfig.getPassword() );
		dataSource.setUrlConnection( dataSourceConfig.getUrl() );
		dataSource.setUser( dataSourceConfig.getUser() );
		dataSource.setHibDialectClass( dataSourceConfig.getHibDialectClass());
		dataSource.setHibDialectName( dataSourceConfig.getHibDialectName());
		dataSource.setMultiSchema(dataSourceConfig.getMultiSchema());
		dataSource.setSchemaAttribute(dataSourceConfig.getSchemaAttribute());
		dataSource.setReadOnly(dataSourceConfig.getReadOnly());
		dataSource.setWriteDefault(dataSourceConfig.getWriteDefault());
		return dataSource;
	}
}
