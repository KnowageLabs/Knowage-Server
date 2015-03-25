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
package it.eng.spagobi.tools.dataset.cache.impl.sqldbcache;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.CacheItem;
import it.eng.spagobi.tools.dataset.cache.ICacheMetadata;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.database.DataBase;
import it.eng.spagobi.utilities.database.IDataBase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class SQLDBCacheMetadata implements ICacheMetadata {

	private LinkedHashMap<String, CacheItem> cacheRegistry = new LinkedHashMap<String, CacheItem>();

	private final LinkedHashMap<String, List<String>> datasetToJoinedMap = new LinkedHashMap<String, List<String>>();

	SQLDBCacheConfiguration cacheConfiguration;

	private BigDecimal totalMemory;
	// private BigDecimal availableMemory ;

	private boolean isActiveCleanAction = false;
	private Integer cachePercentageToClean;

	private final Map<String, Integer> columnSize = new HashMap<String, Integer>();

	private enum FieldType {
		ATTRIBUTE, MEASURE
	}

	public static final String CACHE_NAME_PREFIX_CONFIG = "SPAGOBI.CACHE.NAMEPREFIX";
	public static final String CACHE_SPACE_AVAILABLE_CONFIG = "SPAGOBI.CACHE.SPACE_AVAILABLE";
	public static final String CACHE_LIMIT_FOR_CLEAN_CONFIG = "SPAGOBI.CACHE.LIMIT_FOR_CLEAN";
	public static final String DIALECT_MYSQL = "MySQL";
	public static final String DIALECT_POSTGRES = "PostgreSQL";
	public static final String DIALECT_ORACLE = "OracleDialect";
	public static final String DIALECT_HSQL = "HSQL";
	public static final String DIALECT_HSQL_PRED = "Predefined hibernate dialect";
	public static final String DIALECT_ORACLE9i10g = "Oracle9Dialect";
	public static final String DIALECT_SQLSERVER = "SQLServer";
	public static final String DIALECT_DB2 = "DB2";
	public static final String DIALECT_INGRES = "Ingres";
	public static final String DIALECT_TERADATA = "Teradata";

	static private Logger logger = Logger.getLogger(SQLDBCacheMetadata.class);

	public SQLDBCacheMetadata(SQLDBCacheConfiguration cacheConfiguration) {
		this.cacheConfiguration = cacheConfiguration;
		if (this.cacheConfiguration != null) {
			totalMemory = this.cacheConfiguration.getCacheSpaceAvailable();
			cachePercentageToClean = this.cacheConfiguration.getCachePercentageToClean();
		}

		String tableNamePrefix = this.cacheConfiguration.getTableNamePrefix();
		if (StringUtilities.isEmpty(tableNamePrefix)) {
			throw new CacheException("An unexpected error occured while initializing cache metadata: SPAGOBI.CACHE.NAMEPREFIX cannot be empty");
		}

		// Modified by Alessandro Portosa
		// Cleaning behaviour now is dominated by totalMemory value
		// TotalMemory = -1 -> Caching with no cleaning action, TotalMemory = 0 -> No caching action, TotalMemory > 0 -> Caching with cleaning action
		if (totalMemory != null && (totalMemory.intValue()) != -1 && cachePercentageToClean != null) {
			isActiveCleanAction = true;
		}
	}

	public BigDecimal getTotalMemory() {
		logger.debug("Total memory is equal to [" + totalMemory + "]");
		return totalMemory;
	}

	/**
	 * Returns the number of bytes used by the table already cached (approximate)
	 */
	public BigDecimal getUsedMemory() {
		IDataBase dataBase = DataBase.getDataBase(cacheConfiguration.getCacheDataSource());
		BigDecimal usedMemory = dataBase.getUsedMemorySize(cacheConfiguration.getSchema(), cacheConfiguration.getTableNamePrefix());
		logger.debug("Used memory is equal to [" + usedMemory + "]");
		return usedMemory;
	}

	/**
	 * Returns the number of bytes available in the cache (approximate)
	 */
	public BigDecimal getAvailableMemory() {
		BigDecimal availableMemory = getTotalMemory();
		BigDecimal usedMemory = getUsedMemory();
		if (usedMemory != null)
			availableMemory = availableMemory.subtract(usedMemory);
		logger.debug("Available memory is equal to [" + availableMemory + "]");
		return availableMemory;
	}

	/**
	 * @return the number of bytes used by the resultSet (approximate)
	 */
	public BigDecimal getRequiredMemory(IDataStore store) {
		return DataStoreStatistics.extimateMemorySize(store, cacheConfiguration.getObjectsTypeDimension());
	}

	public Integer getAvailableMemoryAsPercentage() {
		Integer toReturn = 0;
		BigDecimal spaceAvailable = getAvailableMemory();
		toReturn = Integer.valueOf(((spaceAvailable.multiply(new BigDecimal(100)).divide(getTotalMemory(), RoundingMode.HALF_UP)).intValue()));
		return toReturn;
	}

	public Integer getNumberOfObjects() {
		return cacheRegistry.size();
	}

	public boolean isCleaningEnabled() {
		return isActiveCleanAction;
	}

	public Integer getCleaningQuota() {
		return cachePercentageToClean;
	}

	public boolean isAvailableMemoryGreaterThen(BigDecimal requiredMemory) {
		BigDecimal availableMemory = getAvailableMemory();
		if (availableMemory.compareTo(requiredMemory) <= 0) {
			return false;
		} else {
			return true;
		}
	}

	public boolean hasEnoughMemoryForStore(IDataStore store) {
		BigDecimal availableMemory = getAvailableMemory();
		BigDecimal requiredMemory = getRequiredMemory(store);
		if (availableMemory.compareTo(requiredMemory) <= 0) {
			return false;
		} else {
			return true;
		}
	}

	private Map<String, Integer> getColumnSize() {
		return columnSize;
	}

	public LinkedHashMap<String, CacheItem> getCacheRegistry() {
		return cacheRegistry;
	}

	public void setCacheRegistry(LinkedHashMap<String, CacheItem> cacheRegistry) {
		this.cacheRegistry = cacheRegistry;
	}

	public CacheItem addCacheItem(String resultsetSignature, String tableName, IDataStore resultset) {
		CacheItem item = new CacheItem();
		item.setName(tableName);
		item.setTable(tableName);
		item.setSignature(resultsetSignature);
		item.setDimension(getRequiredMemory(resultset));
		item.setCreationDate(new Date());
		getCacheRegistry().put(tableName, item);

		logger.debug("Added cacheItem : [ Name: " + item.getName() + " \n Signature: " + item.getSignature() + " \n Dimension: " + item.getDimension()
				+ " bytes (approximately)  ]");

		return item;
	}

	public void removeCacheItem(String tableName) {
		getCacheRegistry().remove(tableName);
	}

	public void removeAllCacheItems() {
		Iterator it = getCacheRegistry().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, CacheItem> entry = (Map.Entry<String, CacheItem>) it.next();
			String key = entry.getKey();
			this.removeCacheItem(key);
		}
	}

	public CacheItem getCacheItemByResultSetTableName(String tableName) {
		CacheItem toReturn = null;
		Iterator it = getCacheRegistry().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, CacheItem> entry = (Map.Entry<String, CacheItem>) it.next();
			CacheItem item = entry.getValue();
			if (item.getTable().equalsIgnoreCase(tableName)) {
				toReturn = item;
				break;
			}
		}
		return toReturn;
	}

	public CacheItem getCacheItem(String resultSetSignature) {
		CacheItem toReturn = null;
		Iterator it = getCacheRegistry().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, CacheItem> entry = (Map.Entry<String, CacheItem>) it.next();
			CacheItem item = entry.getValue();
			if (item.getSignature().equalsIgnoreCase(resultSetSignature)) {
				toReturn = item;
				break;
			}
		}
		return toReturn;
	}

	public boolean containsCacheItemByTableName(String tableName) {
		return getCacheItemByResultSetTableName(tableName) != null;
	}

	public boolean containsCacheItem(String resultSetSignature) {
		return getCacheItem(resultSetSignature) != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheMetadata#getSignatures()
	 */
	public List<String> getSignatures() {
		List<String> signatures = new ArrayList<String>();
		Iterator it = getCacheRegistry().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, CacheItem> entry = (Map.Entry<String, CacheItem>) it.next();
			signatures.add(entry.getValue().getSignature());
		}
		return signatures;
	}

	public String getTableNamePrefix() {
		return cacheConfiguration.getTableNamePrefix().toUpperCase();
	}

	public LinkedHashMap<String, List<String>> getDatasetToJoinedMap() {
		return datasetToJoinedMap;
	}

	public List<String> getJoinedsReferringDataset(String datasetSignature) {
		logger.debug("IN");
		logger.debug("Search if dataset with signature " + datasetSignature + " has joined dataset referring to it");
		List<String> joineds = datasetToJoinedMap.get(datasetSignature);
		logger.debug("OUT");
		return joineds;
	}

	public void addJoinedDatasetReference(String signature, String joinedSignature) {
		logger.debug("IN");

		if (datasetToJoinedMap.containsKey(signature) && datasetToJoinedMap.get(signature) != null) {
			List joineds = datasetToJoinedMap.get(signature);
			if (!joineds.contains(joinedSignature)) {
				joineds.add(joinedSignature);
				logger.debug("added information that " + joinedSignature + " refers " + signature);
			} else {
				logger.debug("Already know that that " + datasetToJoinedMap + " refers " + signature);
			}
			datasetToJoinedMap.put(signature, joineds);
		} else {
			List<String> joineds = new ArrayList<String>();
			joineds.add(joinedSignature);
			datasetToJoinedMap.put(signature, joineds);
			logger.debug("added information that " + joinedSignature + " refers " + signature);
		}
		logger.debug("OUT");

	}
}
