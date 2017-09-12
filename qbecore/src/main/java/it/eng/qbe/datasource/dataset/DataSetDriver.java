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

package it.eng.qbe.datasource.dataset;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.IDriver;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.qbe.datasource.naming.SimpleDataSourceNamingStrategy;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class DataSetDriver implements IDriver {

	
	protected boolean dataSourceCacheEnabled; 
	protected int openedDataSource;
	protected int maxDataSource;
	
	
	public static final String DRIVER_ID = "dataset";
	protected static final Map<String, IDataSource> cache = new HashMap<String, IDataSource>();
	protected static final SimpleDataSourceNamingStrategy namingStrategy = new SimpleDataSourceNamingStrategy();
	
	
	public DataSetDriver() {
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
			throw new SpagoBIRuntimeException("Maximum  number of open data source reached");
		}
		
		dataSource = null;
		dataSourceName = namingStrategy.getDataSourceName(configuration);
		if(dataSourceCacheEnabled) {
			dataSource = cache.containsKey(dataSourceName)? 
						 cache.get(dataSourceName): 
					     new DataSetDataSource(dataSourceName, configuration);
			cache.put(dataSourceName, dataSource);
		} else {
			dataSource = new DataSetDataSource(dataSourceName, configuration);
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

