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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.database.DataBaseException;

/**
 * @author Marco Cortella (marco.cortella@eng.it) Antonella Giachino (antonella.giachino@eng.it)
 *
 */
public interface ICacheMetadata {

	/**
	 * @return total cache memory in bytes
	 */
	BigDecimal getTotalMemory();

	/**
	 * @return used cache memory in bytes
	 * @throws DataBaseException
	 */
	BigDecimal getUsedMemory() throws DataBaseException;

	/**
	 * @return available cache memory in bytes
	 * @throws DataBaseException
	 */
	BigDecimal getAvailableMemory() throws DataBaseException;

	/**
	 * @return the percentage of the available cache memory on the total memory
	 * @throws DataBaseException
	 */
	Integer getAvailableMemoryAsPercentage() throws DataBaseException;

	/**
	 * @return the required memory in bytes to store the input resultSet in cache
	 */
	BigDecimal getRequiredMemory(IDataStore resultset);

	/**
	 * @return true if the cache space can contains the store
	 * @throws DataBaseException
	 */
	boolean hasEnoughMemoryForStore(IDataStore store) throws DataBaseException;

	/**
	 * @return true if the cache space is greater the requiredMemory
	 * @throws DataBaseException
	 */
	boolean isAvailableMemoryGreaterThen(BigDecimal requiredMemory) throws DataBaseException;

	/**
	 * @return true if the configuration about the clean action are correctly defined
	 */
	boolean isCleaningEnabled();

	/**
	 * @return the percentage of the memory to delete while cleaning cache
	 */
	Integer getCleaningQuota();

	/**
	 * TODO improve ordering not by insertion but by last access date from oldest to newest
	 *
	 * @return The signatures of all cached objects ordered by insertion order (FIFO)
	 */
	List<String> getSignatures();

	/**
	 * add a cacheItem
	 */
	public void addCacheItem(String dataSetName, String resultsetSignature, Map<String, Object> properties, String tableName, IDataStore resultset);

	/**
	 * add a cacheItem
	 */
	public void addCacheItem(String dataSetName, String resultsetSignature, Map<String, Object> properties, String tableName, BigDecimal dimension);

	/**
	 * add a cacheItem
	 */
	public void addCacheItem(String dataSetName, String resultsetSignature, String tableName, BigDecimal dimension);

	/**
	 * update the cacheItem
	 */
	public void updateCacheItem(CacheItem cacheItem);
	
	/**
	 * update all cacheItems
	 */
	public void updateAllCacheItems(IDataSource dataSource);

	/**
	 * remove the cacheItem
	 */
	public void removeCacheItem(String signature);

	/**
	 * remove all the cacheItems
	 */
	public void removeAllCacheItems();

	/**
	 * @return the cache item getted by resultset signature
	 */
	public CacheItem getCacheItem(String resultSetSignature);

	/**
	 * @return get all the cacheItems
	 */
	public List<CacheItem> getAllCacheItems();

	/**
	 * @return true if the resultsetSignature already esists
	 */
	public boolean containsCacheItem(String resultSetSignature);

	/**
	 * @return the number of the objects cached
	 */
	public Integer getNumberOfObjects();
}
