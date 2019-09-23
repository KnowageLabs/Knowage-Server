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
package it.eng.spagobi.tools.dataset.cache.impl.sqldbcache;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hazelcast.core.IMap;

import it.eng.spagobi.cache.dao.ICacheDAO;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.ICacheMetadata;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.database.CacheDataBase;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.DatabaseUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 *
 */
public class SQLDBCacheMetadata implements ICacheMetadata {

	private final ICacheDAO cacheDao;

	SQLDBCacheConfiguration cacheConfiguration;

	private BigDecimal totalMemory;

	private boolean isActiveCleanAction = false;
	private Integer cachePercentageToClean;
	private Integer cacheDsLastAccessTtl;
	private Integer cachePercentageToStore;

	static private Logger logger = Logger.getLogger(SQLDBCacheMetadata.class);

	public SQLDBCacheMetadata(SQLDBCacheConfiguration cacheConfiguration) {
		this.cacheConfiguration = cacheConfiguration;
			totalMemory = this.cacheConfiguration.getCacheSpaceAvailable();
			cachePercentageToClean = this.cacheConfiguration.getCachePercentageToClean();
			cacheDsLastAccessTtl = this.cacheConfiguration.getCacheDsLastAccessTtl();
			cachePercentageToStore = this.cacheConfiguration.getCachePercentageToStore();

		String tableNamePrefix = this.cacheConfiguration.getTableNamePrefix();
		if (StringUtilities.isEmpty(tableNamePrefix)) {
			throw new CacheException("An unexpected error occured while initializing cache metadata: SPAGOBI.CACHE.NAMEPREFIX cannot be empty");
		}

		// Cleaning behavior now is driven by totalMemory value
		// TotalMemory = -1 -> Caching with no cleaning action, TotalMemory = 0 -> No caching action, TotalMemory > 0 -> Caching with cleaning action
		if (totalMemory != null && (totalMemory.intValue()) != -1 && cachePercentageToClean != null) {
			isActiveCleanAction = true;
		}

		cacheDao = DAOFactory.getCacheDao();
		if (cacheDao == null) {
			throw new CacheException(
					"An unexpected error occured while initializing cache metadata: the return value of DAOFactory.getCacheDao() cannot be null");
		}
	}

	@Override
	public BigDecimal getTotalMemory() {
		logger.debug("Total memory is equal to [" + totalMemory + "]");
		return totalMemory;
	}

	/**
	 * Returns the number of bytes used by the table already cached (approximate)
	 * 
	 * @throws DataBaseException
	 */

	@Override
	public BigDecimal getUsedMemory() throws DataBaseException {
		CacheDataBase dataBase = DataBaseFactory.getCacheDataBase(cacheConfiguration.getCacheDataSource());
		BigDecimal usedMemory = DatabaseUtilities.getUsedMemorySize(dataBase, cacheConfiguration.getSchema(), cacheConfiguration.getTableNamePrefix());
		logger.debug("Used memory is equal to [" + usedMemory + "]");
		return usedMemory;
	}

	/**
	 * Returns the number of bytes available in the cache (approximate)
	 * 
	 * @throws DataBaseException
	 */

	@Override
	public BigDecimal getAvailableMemory() throws DataBaseException {
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

	@Override
	public BigDecimal getRequiredMemory(IDataStore store) {
		return DataStoreStatistics.extimateMemorySize(store, cacheConfiguration.getObjectsTypeDimension());
	}

	@Override
	public Integer getAvailableMemoryAsPercentage() throws DataBaseException {
		Integer toReturn = 0;
		BigDecimal spaceAvailable = getAvailableMemory();
		toReturn = Integer.valueOf(((spaceAvailable.multiply(new BigDecimal(100)).divide(getTotalMemory(), RoundingMode.HALF_UP)).intValue()));
		return toReturn;
	}

	@Override
	public Integer getNumberOfObjects() {
		return cacheDao.loadAllCacheItems().size();
	}

	@Override
	public boolean isCleaningEnabled() {
		return isActiveCleanAction;
	}

	@Override
	public Integer getCleaningQuota() {
		return cachePercentageToClean;
	}

	public Integer getCacheDsLastAccessTtl() {
		return cacheDsLastAccessTtl;
	}

	public Integer getCachePercentageToStore() {
		return cachePercentageToStore;
	}

	@Override
	public boolean isAvailableMemoryGreaterThen(BigDecimal requiredMemory) throws DataBaseException {
		BigDecimal availableMemory = getAvailableMemory();
		if (availableMemory.compareTo(requiredMemory) <= 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean hasEnoughMemoryForStore(IDataStore store) throws DataBaseException {
		BigDecimal availableMemory = getAvailableMemory();
		BigDecimal requiredMemory = getRequiredMemory(store);
		if (availableMemory.compareTo(requiredMemory) <= 0) {
			return false;
		} else {
			return true;
		}
	}

	public List<CacheItem> getCacheItems() {
		return cacheDao.loadAllCacheItems();
	}

	@Override
	public void addCacheItem(String dataSetName, String resultsetSignature, Map<String, Object> properties, String tableName, IDataStore resultset) {
		addCacheItem(dataSetName, resultsetSignature, properties, tableName, getRequiredMemory(resultset));
	}

	@Override
	public void addCacheItem(String dataSetName, String resultsetSignature, String tableName, BigDecimal dimension) {
		addCacheItem(dataSetName, resultsetSignature, new HashMap<String, Object>(0), tableName, dimension);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCacheItem(String dataSetName, String resultsetSignature, Map<String, Object> properties, String tableName, BigDecimal dimension) {
		String hashedSignature = Helper.sha256(resultsetSignature);

		IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME, SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
		mapLocks.lock(hashedSignature); // it is possible to use also the method tryLock(...) with timeout parameter
		try {
			removeCacheItem(resultsetSignature);

			CacheItem item = new CacheItem();
			item.setName(dataSetName);
			item.setTable(tableName);
			item.setSignature(hashedSignature);
			item.setDimension(dimension);
			Date now = new Date();
			item.setCreationDate(now);
			item.setLastUsedDate(now);
			item.setProperties(properties);
			cacheDao.insertCacheItem(item);

			logger.debug("Added cacheItem : [ Name: " + item.getName() + " \n Signature: " + item.getSignature() + " \n Dimension: " + item.getDimension()
					+ " bytes (approximately)  ]");
		} finally {
			mapLocks.unlock(hashedSignature);
		}
	}

	@Override
	public void updateCacheItem(CacheItem cacheItem) {
		String hashedSignature = cacheItem.getSignature();

		IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME, SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
		mapLocks.lock(hashedSignature); // it is possible to use also the method tryLock(...) with timeout parameter
		try {
			if (containsCacheItem(hashedSignature, true)) {
				cacheDao.updateCacheItem(cacheItem);
				logger.debug("The dataset with hash [" + hashedSignature + "] has been updated");
			} else {
				logger.debug("The dataset with hash [" + hashedSignature + "] does not exist in cache");
			}
		} finally {
			mapLocks.unlock(hashedSignature);
		}
	}
	
	/**
	 * Updates all items in sbi_cache_item setting the correct dimension calculated
	 * on the effective DB space occupation.
	 * 
	 */
	@Override
	public void updateAllCacheItems(IDataSource dataSource) {
		List<CacheItem> cacheItems = getAllCacheItems();
		for (CacheItem cacheItem : cacheItems) {
			try {
				cacheItem.setDimension(DatabaseUtilities.getUsedMemorySize(
				            				DataBaseFactory.getCacheDataBase(dataSource), "cache", cacheItem.getTable()));
			} catch (DataBaseException e) {
				logger.error("Error updating cacheitem [" + cacheItem.getTable() + "]", e);
				throw new SpagoBIRuntimeException("Error updating cacheitem [" + cacheItem.getTable() + "]", e);
			}
			cacheDao.updateCacheItem(cacheItem);
		}
		
	}

	@Override
	public void removeCacheItem(String signature) {
		String hashedSignature = Helper.sha256(signature);

		IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME, SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
		mapLocks.lock(hashedSignature); // it is possible to use also the method tryLock(...) with timeout parameter
		try {
			if (containsCacheItem(signature)) {
				cacheDao.deleteCacheItemBySignature(hashedSignature);
				logger.debug("The dataset with signature[" + signature + "] and hash [" + hashedSignature + "] has been updated");
			} else {
				logger.debug("The dataset with signature[" + signature + "] and hash [" + hashedSignature + "] does not exist in cache");
			}
		} finally {
			mapLocks.unlock(hashedSignature);
		}
	}

	public void removeCacheItem(String signature, boolean isHash) {
		if (isHash) {
			IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME,
					SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
			mapLocks.lock(signature); // it is possible to use also the method tryLock(...) with timeout parameter
			try {
				if (containsCacheItem(signature, true)) {
					cacheDao.deleteCacheItemBySignature(signature);
					logger.debug("The dataset with hash [" + signature + "] has been deleted");
				} else {
					logger.debug("The dataset with hash [" + signature + "] does not exist in cache");
				}
			} finally {
				mapLocks.unlock(signature);
			}
		} else {
			removeCacheItem(signature);
		}
	}

	@Override
	public void removeAllCacheItems() {
		cacheDao.deleteAllCacheItem();
	}

	// public CacheItem getCacheItemByResultSetTableName(String tableName) {
	// return cacheDao.loadCacheItemByTableName(tableName);
	// }

	@Override
	public CacheItem getCacheItem(String resultSetSignature) {
		CacheItem cacheItem = null;

		String hashedSignature = Helper.sha256(resultSetSignature);

		IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME, SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
		mapLocks.lock(hashedSignature); // it is possible to use also the method tryLock(...) with timeout parameter
		try {
			cacheItem = cacheDao.loadCacheItemBySignature(hashedSignature);
			if (cacheItem != null) {
				logger.debug("The dataset with signature[" + resultSetSignature + "] and hash [" + hashedSignature + "] has been found in cache");
			} else {
				logger.debug("The dataset with signature[" + resultSetSignature + "] and hash [" + hashedSignature + "] does not exist in cache");
			}
		} finally {
			mapLocks.unlock(hashedSignature);
		}
		return cacheItem;
	}

	public CacheItem getCacheItem(String signature, boolean isHash) {
		if (isHash) {
			CacheItem cacheItem = null;
			IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME,
					SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
			mapLocks.lock(signature); // it is possible to use also the method tryLock(...) with timeout parameter
			try {
				cacheItem = cacheDao.loadCacheItemBySignature(signature);
				if (cacheItem != null) {
					logger.debug("The dataset with hash [" + signature + "] has been found in cache");
				} else {
					logger.debug("The dataset with hash [" + signature + "] does not exist in cache");
				}
			} finally {
				mapLocks.unlock(signature);
			}
			return cacheItem;
		} else {
			return getCacheItem(signature);
		}
	}

	// public boolean containsCacheItemByTableName(String tableName) {
	// return getCacheItemByResultSetTableName(tableName) != null;
	// }

	@Override
	public boolean containsCacheItem(String resultSetSignature) {
		return containsCacheItem(resultSetSignature, false);
	}

	public boolean containsCacheItem(String signature, boolean isHash) {
		if (isHash) {
			return getCacheItem(signature, isHash) != null;
		} else {
			return getCacheItem(signature) != null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheMetadata#getSignatures()
	 */

	@Override
	public List<String> getSignatures() {
		List<String> signatures = new ArrayList<String>();
		List<CacheItem> cacheItems = cacheDao.loadAllCacheItems();
		for (CacheItem item : cacheItems) {
			signatures.add(item.getSignature());
		}
		return signatures;
	}

	@Override
	public List<CacheItem> getAllCacheItems() {
		return cacheDao.loadAllCacheItems();
	}

	public String getTableNamePrefix() {
		return cacheConfiguration.getTableNamePrefix().toUpperCase();
	}

}
