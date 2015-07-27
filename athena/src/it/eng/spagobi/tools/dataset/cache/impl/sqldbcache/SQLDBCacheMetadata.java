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

import it.eng.spagobi.cache.dao.ICacheDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.ICacheMetadata;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.database.DataBase;
import it.eng.spagobi.utilities.database.IDataBase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public class SQLDBCacheMetadata implements ICacheMetadata {

	private final ICacheDAO cacheDao;

	SQLDBCacheConfiguration cacheConfiguration;

	private BigDecimal totalMemory;
	// private BigDecimal availableMemory ;

	private boolean isActiveCleanAction = false;
	private Integer cachePercentageToClean;
	private Integer cacheDsLastAccessTtl;

	private final Map<String, Integer> columnSize = new HashMap<String, Integer>();

	private enum FieldType {
		ATTRIBUTE, MEASURE
	}

	public static final String CACHE_NAME_PREFIX_CONFIG = "SPAGOBI.CACHE.NAMEPREFIX";
	public static final String CACHE_SPACE_AVAILABLE_CONFIG = "SPAGOBI.CACHE.SPACE_AVAILABLE";
	public static final String CACHE_LIMIT_FOR_CLEAN_CONFIG = "SPAGOBI.CACHE.LIMIT_FOR_CLEAN";
	public static final String CACHE_DS_LAST_ACCESS_TTL = "SPAGOBI.CACHE.DS_LAST_ACCESS_TTL";
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
			cacheDsLastAccessTtl = this.cacheConfiguration.getCacheDsLastAccessTtl();
		}

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
		return cacheDao.loadAllCacheItems().size();
	}

	public boolean isCleaningEnabled() {
		return isActiveCleanAction;
	}

	public Integer getCleaningQuota() {
		return cachePercentageToClean;
	}

	public Integer getCacheDsLastAccessTtl() {
		return cacheDsLastAccessTtl;
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

	public List<CacheItem> getCacheItems() {
		return cacheDao.loadAllCacheItems();
	}

	public void addCacheItem(String resultsetSignature, Map<String, Object> properties, String tableName, IDataStore resultset) {
		CacheItem item = new CacheItem();
		item.setName(tableName);
		item.setTable(tableName);
		item.setSignature(getHashedSignature(resultsetSignature));
		item.setDimension(getRequiredMemory(resultset));
		Date now = new Date();
		item.setCreationDate(now);
		item.setLastUsedDate(now);
		item.setProperties(properties);
		cacheDao.insertCacheItem(item);

		logger.debug("Added cacheItem : [ Name: " + item.getName() + " \n Signature: " + item.getSignature() + " \n Dimension: " + item.getDimension()
				+ " bytes (approximately)  ]");
	}

	public void updateCacheItem(CacheItem cacheItem) {
		cacheDao.updateCacheItem(cacheItem);
	}

	public void removeCacheItem(String signature) {
		cacheDao.deleteCacheItemBySignature(getHashedSignature(signature));
	}

	public void removeCacheItem(String signature, boolean isHash) {
		if (isHash) {
			cacheDao.deleteCacheItemBySignature(signature);
		} else {
			removeCacheItem(signature);
		}
	}

	public void removeAllCacheItems() {
		cacheDao.deleteAllCacheItem();
	}

	public CacheItem getCacheItemByResultSetTableName(String tableName) {
		return cacheDao.loadCacheItemByTableName(tableName);
	}

	public CacheItem getCacheItem(String resultSetSignature) {
		return cacheDao.loadCacheItemBySignature(getHashedSignature(resultSetSignature));
	}

	public CacheItem getCacheItem(String resultSetSignature, boolean isHash) {
		if (isHash) {
			return cacheDao.loadCacheItemBySignature(resultSetSignature);
		} else {
			return getCacheItem(resultSetSignature);
		}
	}

	public boolean containsCacheItemByTableName(String tableName) {
		return getCacheItemByResultSetTableName(tableName) != null;
	}

	public boolean containsCacheItem(String resultSetSignature) {
		return getCacheItem(resultSetSignature) != null;
	}

	public boolean containsCacheItem(String resultSetSignature, boolean isHash) {
		if (isHash) {
			return getCacheItem(resultSetSignature, isHash) != null;
		} else {
			return getCacheItem(resultSetSignature) != null;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICacheMetadata#getSignatures()
	 */
	public List<String> getSignatures() {
		List<String> signatures = new ArrayList<String>();
		List<CacheItem> cacheItems = cacheDao.loadAllCacheItems();
		for (CacheItem item : cacheItems) {
			signatures.add(item.getSignature());
		}
		return signatures;
	}

	public String getTableNamePrefix() {
		return cacheConfiguration.getTableNamePrefix().toUpperCase();
	}

	public List<String> getJoinedsReferringDataset(String datasetSignature) {
		logger.debug("IN");
		String signature = getHashedSignature(datasetSignature);
		logger.debug("Search if dataset with signature " + signature + " has joined dataset referring to it");
		List<String> toReturn = new ArrayList<String>();
		List<CacheItem> joinedCacheItems = cacheDao.loadCacheJoinedItemsReferringTo(signature);
		for (CacheItem joinedCacheItem : joinedCacheItems) {
			toReturn.add(joinedCacheItem.getSignature());
		}
		logger.debug("OUT");
		return toReturn;
	}

	public void addJoinedDatasetReference(String signature, String joinedSignature) {
		logger.debug("IN");
		String hashedSignature = getHashedSignature(signature);
		String hashedJoinedSignature = getHashedSignature(joinedSignature);
		if (!cacheDao.hasCacheItemReferenceToCacheJoinedItem(hashedSignature, hashedJoinedSignature)) {
			CacheItem cacheItem = cacheDao.loadCacheItemBySignature(hashedSignature);
			CacheItem joinedCacheItem = cacheDao.loadCacheItemBySignature(hashedJoinedSignature);
			cacheDao.insertCacheJoinedItem(cacheItem, joinedCacheItem);
			logger.debug("Added information that " + hashedJoinedSignature + " refers " + hashedSignature);
		} else {
			logger.debug("Already know that " + hashedJoinedSignature + " refers " + hashedSignature);
		}
		logger.debug("OUT");
	}

	private String getHashedSignature(String signature) {
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(signature.getBytes("UTF-8"));
		} catch (Throwable t) {
			throw new CacheException("Error when hashing dataset signature. This step is necessary to generate the cache item signature", t);
		}

		// convert the byte to hex format method 1
		byte byteData[] = messageDigest.digest();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}
}
