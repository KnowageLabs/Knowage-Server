/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.jpa;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.IDriver;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.datasource.naming.SimpleDataSourceNamingStrategy;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JPADriver implements IDriver {

	
	protected boolean dataSourceCacheEnabled; 
	protected int openedDataSource;
	protected int maxDataSource;
	
	
	public static final String DRIVER_ID = "jpa";
	protected static final Map<String, IDataSource> cache = new HashMap<String, IDataSource>();
	protected static final SimpleDataSourceNamingStrategy namingStrategy = new SimpleDataSourceNamingStrategy();
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(JPADriver.class);
	
	public JPADriver() {
		dataSourceCacheEnabled = true;
		openedDataSource = 0;
		maxDataSource = -1;
	}
	
	public String getName() {
		return DRIVER_ID;
	}

	public IDataSource getDataSource(IDataSourceConfiguration configuration) {
		IDataSource dataSource;
		String dataSourceName;
		
		if(maxDataSource > 0 && openedDataSource == maxDataSource) {
			throw new SpagoBIRuntimeException("Maximum  number of open data sources reached");
		}
		
		dataSource = null;
		dataSourceName = namingStrategy.getDataSourceName(configuration);
		
		if(dataSourceCacheEnabled) {
			logger.debug("The Data source cache is enabled");
			dataSource = cache.containsKey(dataSourceName)? 
						 cache.get(dataSourceName): 
					     new JPADataSource(dataSourceName, configuration);
			cache.put(dataSourceName, dataSource);
		} else {
			logger.debug("The Data source cache is not enabled");
			dataSource = new JPADataSource(dataSourceName, configuration);
		}
		
		openedDataSource++;
		
		return dataSource;
	}

	public void setDataSourceCacheEnabled(boolean enabled) {
		dataSourceCacheEnabled = enabled;	
	}

	public boolean isDataSourceCacheEnabled() {
		return dataSourceCacheEnabled;
	}

	public void setMaxDataSource(int n) {
		maxDataSource = n;		
	}

	public boolean acceptDataSourceConfiguration() {
		// TODO Auto-generated method stub
		return true;
	}

}
