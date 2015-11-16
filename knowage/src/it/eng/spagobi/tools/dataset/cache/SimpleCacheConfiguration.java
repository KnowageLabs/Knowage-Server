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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class SimpleCacheConfiguration implements ICacheConfiguration {

	IDataSource dataSource;
	Map<String, Object> properties;

	public static final String CACHE_SPACE_AVAILABLE_CONFIG = "SPAGOBI.CACHE.SPACE_AVAILABLE";
	public static final String CACHE_LIMIT_FOR_CLEAN_CONFIG = "SPAGOBI.CACHE.LIMIT_FOR_CLEAN";
	public static final String CACHE_DS_LAST_ACCESS_TTL = "SPAGOBI.CACHE.DS_LAST_ACCESS_TTL";
	public static final String CACHE_SCHEDULING_FULL_CLEAN = "SPAGOBI.CACHE.SCHEDULING_FULL_CLEAN";
	public static final String CACHE_LIMIT_FOR_STORE_CONFIG = "SPAGOBI.CACHE.LIMIT_FOR_STORE";

	private static final String CACHE_WORK_MANAGER = "SPAGOBI.CACHE.WORK_MANAGER";

	public SimpleCacheConfiguration() {
		dataSource = null;
		properties = new HashMap<String, Object>();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheConfiguration#getCacheDataSource()
	 */
	
	public IDataSource getCacheDataSource() {
		return dataSource;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheConfiguration#setCacheDataSource(it.eng.spagobi.tools.datasource.bo.IDataSource)
	 */
	
	public void setCacheDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	// =========================================================================================
	// FACILITY METHODS
	// =========================================================================================

	/**
	 * @return the cacheSpaceAvailable
	 */
	
	public BigDecimal getCacheSpaceAvailable() {
		return (BigDecimal) getProperty(CACHE_SPACE_AVAILABLE_CONFIG);
	}

	/**
	 * @param cacheSpaceAvailable
	 *            the cacheSpaceAvailable to set
	 */
	
	public void setCacheSpaceAvailable(BigDecimal cacheSpaceAvailable) {
		setProperty(CACHE_SPACE_AVAILABLE_CONFIG, cacheSpaceAvailable);
	}

	/**
	 * @return the cachePercentageToClean
	 */
	
	public Integer getCachePercentageToClean() {
		return (Integer) getProperty(CACHE_LIMIT_FOR_CLEAN_CONFIG);
	}

	/**
	 * @param cachePercentageToClean
	 *            the cachePercentageToClean to set
	 */
	
	public void setCachePercentageToClean(Integer cachePercentageToClean) {
		setProperty(CACHE_LIMIT_FOR_CLEAN_CONFIG, cachePercentageToClean);
	}

	/**
	 * @return the cacheDsLastAccessTtl
	 */
	
	public Integer getCacheDsLastAccessTtl() {
		return (Integer) getProperty(CACHE_DS_LAST_ACCESS_TTL);
	}

	/**
	 * @param cacheDsLastAccessTtl
	 *            the cacheDsLastAccessTtl to set
	 */
	
	public void setCacheDsLastAccessTtl(Integer cacheDsLastAccessTtl) {
		setProperty(CACHE_DS_LAST_ACCESS_TTL, cacheDsLastAccessTtl);
	}

	/**
	 * @return the cacheSchedulingFullClean
	 */
	
	public String getCacheSchedulingFullClean() {
		return (String) getProperty(CACHE_SCHEDULING_FULL_CLEAN);
	}

	/**
	 * @param cacheSchedulingFullClean
	 *            the cacheSchedulingFullClean to set
	 */
	
	public void setCacheSchedulingFullClean(String cacheSchedulingFullClean) {
		setProperty(CACHE_SCHEDULING_FULL_CLEAN, cacheSchedulingFullClean);
	}

	/**
	 * @return the cachePercentageToStore
	 */
	
	public Integer getCachePercentageToStore() {
		return (Integer) getProperty(CACHE_LIMIT_FOR_STORE_CONFIG);
	}

	/**
	 * @param cachePercentageToStore
	 *            the cachePercentageToStore to set
	 */
	
	public void setCachePercentageToStore(Integer cachePercentageToStore) {
		setProperty(CACHE_LIMIT_FOR_STORE_CONFIG, cachePercentageToStore);
	}

	/**
	 * @return the work manger used by cache to perform task in background
	 */
	
	public WorkManager getWorkManager() {
		return (WorkManager) getProperty(CACHE_WORK_MANAGER);
	}

	/**
	 * @param the
	 *            work manger used by cache to perform task in background
	 */
	
	public void setWorkManager(WorkManager workManager) {
		setProperty(CACHE_WORK_MANAGER, workManager);
	}

	// =========================================================================================
	// GENERICS
	// =========================================================================================

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheConfiguration#containsProperty(java.lang.String)
	 */
	
	public boolean containsProperty(String propertyName) {
		return properties.containsKey(propertyName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheConfiguration#getPropertyNames()
	 */
	
	public Set<String> getPropertyNames() {
		return properties.keySet();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheConfiguration#getProperty(java.lang.String)
	 */
	
	public Object getProperty(String propertyName) {
		return properties.get(propertyName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheConfiguration#setProperty(java.lang.String, java.lang.Object)
	 */
	
	public Object setProperty(String propertyName, Object propertyValue) {
		Object oldValue = properties.get(propertyName);
		properties.put(propertyName, propertyValue);
		return oldValue;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheConfiguration#deleteProperty(java.lang.String)
	 */
	
	public Object deleteProperty(String propertyName) {
		return properties.remove(propertyName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheConfiguration#deleteAllProperties(java.lang.String)
	 */
	
	public void deleteAllProperties(String propertyName) {
		properties.clear();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheConfiguration#addAllProperties(Map<String, Object>)
	 */
	
	public void addAllProperties(Map<String, Object> properties) {
		properties.putAll(properties);
	}
}
