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
package it.eng.qbe.datasource.jpa;

import it.eng.qbe.datasource.IDataSource;
import it.eng.qbe.datasource.configuration.IDataSourceConfiguration;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class JPADriverWithClassLoader extends JPADriver{

	public static final String DRIVER_ID = "jpa_with_cl";
	
	public JPADriverWithClassLoader(){
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
			throw new SpagoBIRuntimeException("Maximum  number of open data sources reached");
		}
		
		dataSource = null;
		dataSourceName = namingStrategy.getDataSourceName(configuration);
		
		if(dataSourceCacheEnabled) {
			logger.debug("The Data source cache is enabled");
			dataSource = cache.containsKey(dataSourceName)? 
						 cache.get(dataSourceName): 
					     new JPADataSourceWithClassLoader(dataSourceName, configuration);
		} else {
			logger.debug("The Data source cache is not enabled");
			dataSource = new JPADataSourceWithClassLoader(dataSourceName, configuration);
		}
		
		openedDataSource++;
		
		return dataSource;
	}
	
}
