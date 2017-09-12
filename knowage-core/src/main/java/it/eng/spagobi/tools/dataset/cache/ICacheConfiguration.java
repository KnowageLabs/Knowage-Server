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

	public Integer getCacheDsLastAccessTtl();

	public void setCacheDsLastAccessTtl(Integer cacheDsLastAccessTtl);

	public String getCacheSchedulingFullClean();

	public void setCacheSchedulingFullClean(String cacheSchedulingFullClean);

	public Integer getCachePercentageToStore();

	public void setCachePercentageToStore(Integer cachePercentageToStore);

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

	void deleteAllProperties(String propertyName);

	void addAllProperties(Map<String, Object> properties);
}
