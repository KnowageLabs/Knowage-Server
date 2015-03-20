/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.hibernate;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class HibernateDriverWithClassLoader extends HibernateDriver{

	public static final String DRIVER_ID = "hibernate_with_cl";
	
	public HibernateDriverWithClassLoader(){
		super();
	}
	
	@Override
	public String getName() {
		return DRIVER_ID;
	}

	@Override
	public IDataSource getDataSource(IDataSourceConfiguration configuration) {
		IDataSource dataSource;
		String dataSourceName;
		
		if(maxDataSource > 0 && openedDataSource == maxDataSource) {
			throw new SpagoBIRuntimeException("Maximum  number of open data source reached");
		}
		
		dataSource = null;
		dataSourceName = namingStrategy.getDataSourceName(configuration);
		if(dataSourceCacheEnabled) {
			dataSource = cache.containsKey(dataSourceName)? 
						 cache.get(dataSourceName): 
					     new HibernateDataSourceWithClassLoader(dataSourceName, configuration);
		} else {
			dataSource = new HibernateDataSourceWithClassLoader(dataSourceName, configuration);
		}
		
		openedDataSource++;
		
		return dataSource;
	}
	
}
