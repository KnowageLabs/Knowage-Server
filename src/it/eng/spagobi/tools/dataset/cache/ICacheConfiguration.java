/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.tools.dataset.cache;

import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

/**
 * The cache configuration.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface ICacheConfiguration {
	
	IDataSource getCacheDataSource();
	void setCacheDataSource(IDataSource dataSource);
	
	// =========================================================================================
	// FACILITY METHODS
	// =========================================================================================
	BigDecimal getCacheSpaceAvailable();
	void setCacheSpaceAvailable(BigDecimal cacheSpaceAvailable);
	Integer getCachePercentageToClean();
	public void setCachePercentageToClean(Integer cachePercentageToClean);
	WorkManager getWorkManager();
	void setWorkManager(WorkManager workManager);
	
	// =========================================================================================
	// GENERICS
	// =========================================================================================
	Set<String> getPropertyNames();
	boolean containsProperty(String propertyName);
	Object getProperty(String propertyName);
	Object setProperty(String propertyName, Object propertyValue);
	Object deleteProperty(String propertyName);
	void  deleteAllProperties(String propertyName);
	void addAllProperties(Map<String, Object> properties);	
}
