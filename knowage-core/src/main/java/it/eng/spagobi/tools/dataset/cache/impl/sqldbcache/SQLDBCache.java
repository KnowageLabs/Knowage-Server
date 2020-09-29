/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.tools.dataset.cache.impl.sqldbcache;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.hazelcast.core.IMap;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.tools.dataset.bo.AbstractDataSet;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategyType;
import it.eng.spagobi.tools.dataset.bo.FlatDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.metasql.query.DatabaseDialect;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.dataset.strategy.DatasetEvaluationStrategyFactory;
import it.eng.spagobi.tools.dataset.strategy.IDatasetEvaluationStrategy;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.database.DataBaseFactory;
import it.eng.spagobi.utilities.database.DatabaseUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.locks.DistributedLockFactory;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 */
public class SQLDBCache implements ICache {

	private IDataSource dataSource;

	private UserProfile userProfile;

	private final SQLDBCacheMetadata cacheMetadata;

	private static final long DEFAULT_HAZELCAST_TIMEOUT = 120;
	private static final long DEFAULT_HAZELCAST_LEASETIME = 240;

	static private Logger logger = Logger.getLogger(SQLDBCache.class);

	public SQLDBCache(IDataSource dataSource, SQLDBCacheMetadata cacheMetadata) {
		this.dataSource = dataSource;
		this.cacheMetadata = cacheMetadata;
	}

	// ===================================================================================
	// CONTAINS METHODS
	// ===================================================================================

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#contains(it.eng.spagobi.tools .dataset.bo.IDataSet)
	 */

	@Override
	public boolean contains(IDataSet dataSet) {
		return contains(dataSet.getSignature());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#contains(java.lang.String)
	 */

	@Override
	public boolean contains(String resultsetSignature) {
		return getMetadata().containsCacheItem(resultsetSignature);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#contains(java.util.List)
	 */

	@Override
	public boolean contains(List<IDataSet> dataSets) {
		return getNotContained(dataSets).size() > 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#getNotContained(java.util.List)
	 */

	@Override
	public List<IDataSet> getNotContained(List<IDataSet> dataSets) {
		List<IDataSet> notContainedDataSets = new ArrayList<IDataSet>();
		for (IDataSet dataSet : dataSets) {
			if (!contains(dataSet)) {
				notContainedDataSets.add(dataSet);
			}
		}
		return notContainedDataSets;
	}

	// ===================================================================================
	// GET METHODS
	// ===================================================================================

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.dataset.cache.ICache#get(it.eng.spagobi.tools.dataset. bo.IDataSet)
	 */

	@Override
	public IDataStore get(IDataSet dataSet) {
		IDataStore dataStore = null;

		logger.debug("IN");
		try {
			if (dataSet != null) {
				String dataSetSignature = null;

				try {
					dataSetSignature = dataSet.getSignature();
				} catch (ParametersNotValorizedException p) {
					logger.warn("Error on getting signature for dataset [ " + dataSet.getLabel() + " ]. Error: " + p.getMessage());
					return null; // doesn't cache data
				}
				dataStore = get(dataSetSignature);
			} else {
				logger.warn("Input parameter [dataSet] is null");
			}
		} catch (Throwable t) {
			if (t instanceof CacheException)
				throw (CacheException) t;
			else
				throw new CacheException("An unexpected error occure while getting dataset from cache", t);
		} finally {
			logger.debug("OUT");
		}

		return dataStore;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.dataset.cache.ICache#get(java.lang.String)
	 */

	@Override
	public IDataStore get(String resultsetSignature) {
		return get(resultsetSignature, false);
	}

	@Override
	public IDataStore get(String signature, boolean isHash) {
		logger.debug("IN");

		IDataStore dataStore = null;
		String hashedSignature = isHash ? signature : Helper.sha256(signature);

		IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME, SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
		try {
			if (mapLocks.tryLock(hashedSignature, getTimeout(), TimeUnit.SECONDS, getLeaseTime(), TimeUnit.SECONDS)) {
				try {
					if (getMetadata().containsCacheItem(signature, isHash)) {
						logger.debug("Resultset with signature [" + signature + "] found");
						CacheItem cacheItem = getMetadata().getCacheItem(signature, isHash);
						cacheItem.setLastUsedDate(new Date());
						// update DB information about this cacheItem
						getMetadata().updateCacheItem(cacheItem);
						String tableName = cacheItem.getTable();
						logger.debug("The table associated to signature [" + signature + "] is [" + tableName + "]");
						dataStore = dataSource.executeStatement("SELECT * FROM " + tableName, 0, 0);
					} else {
						logger.debug("Resultset with signature [" + signature + "] not found");
					}
				} catch (Throwable t) {
					if (t instanceof CacheException)
						throw (CacheException) t;
					else
						throw new CacheException("An unexpected error occure while getting dataset from cache", t);
				} finally {
					mapLocks.unlock(hashedSignature);
				}
			} else {
				logger.debug("Impossible to acquire the lock for dataset [" + signature + "]. Timeout. Returning a null datastore.");
			}
		} catch (InterruptedException e) {
			logger.debug("The current thread has failed to release the lock for dataset [" + hashedSignature + "] in time. Returning a null datastore.", e);
		}

		logger.debug("OUT");
		return dataStore;
	}

	@Override
	public IDataStore get(UserProfile userProfile, IDataSet dataSet, List<AbstractSelectionField> projections, Filter filter,
			List<AbstractSelectionField> groups, List<Sorting> sortings, List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize,
			int maxRowCount, Set<String> indexes) throws DataBaseException {
		logger.debug("IN");
		Assert.assertNotNull(dataSet, "Dataset cannot be null");
		Assert.assertNotNull(userProfile, "User profile cannot be null");
		IDataStore dataStore;
		try {
			dataSet.setUserProfile(userProfile);
			dataStore = getInternal(dataSet, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount, indexes);
		} finally {
			logger.debug("OUT");
		}

		return dataStore;
	}

	private IDataStore getInternal(IDataSet dataSet, List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups,
			List<Sorting> sortings, List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> indexes)
			throws DataBaseException {
		logger.debug("IN");

		try {
			String resultsetSignature = dataSet.getSignature();
			if (!getMetadata().containsCacheItem(resultsetSignature)) {
				logger.debug("Not found resultSet with signature [" + resultsetSignature + "] inside the Cache");
				return null;
			}
			return queryStandardCachedDataset(dataSet, resultsetSignature, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize,
					maxRowCount, indexes);

		} finally {
			logger.debug("OUT");
		}

	}

	@SuppressWarnings("unchecked")
	private IDataStore queryStandardCachedDataset(IDataSet dataSet, String resultsetSignature, List<AbstractSelectionField> projections, Filter filter,
			List<AbstractSelectionField> groups, List<Sorting> sortings, List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize,
			int maxRowCount, Set<String> indexes) throws DataBaseException {

		IDataStore toReturn = null;

		String hashedSignature = Helper.sha256(resultsetSignature);
		Monitor timing = MonitorFactory.start("Knowage.SQLDBCache.queryStandardCachedDataset:gettingMap");
		IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME, SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
		timing.stop();
		try {
			timing = MonitorFactory.start("Knowage.SQLDBCache.queryStandardCachedDataset:gettingLock[" + hashedSignature + "]");
			if (mapLocks.tryLock(hashedSignature, getTimeout(), TimeUnit.SECONDS, getLeaseTime(), TimeUnit.SECONDS)) {
				timing.stop();

				cacheMetadata.updateAllCacheItems(getDataSource());

				try {
					timing = MonitorFactory.start("Knowage.SQLDBCache.queryStandardCachedDataset:usingLock[" + hashedSignature + "]");
					if (getMetadata().containsCacheItem(resultsetSignature)) {
						logger.debug("Found dataset with signature [" + resultsetSignature + "] and hash [" + hashedSignature + "] inside the cache");

						CacheItem cacheItem = getMetadata().getCacheItem(resultsetSignature);
						cacheItem.setLastUsedDate(new Date());
						getMetadata().updateCacheItem(cacheItem); // update DB information about this cacheItem

						String tableName = cacheItem.getTable();
						logger.debug("Found resultSet with signature [" + resultsetSignature + "] inside the Cache, table used [" + tableName + "]");

						FlatDataSet flatDataSet = new FlatDataSet();
						flatDataSet.setDataSource(dataSource);
						flatDataSet.setTableName(tableName);
						flatDataSet.setMetadata(dataSet.getMetadata());

						IDatasetEvaluationStrategy strategy = DatasetEvaluationStrategyFactory.get(DatasetEvaluationStrategyType.FLAT, flatDataSet, null);

						toReturn = strategy.executeQuery(projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount, indexes);
						toReturn.setCacheDate(cacheItem.getCreationDate());

						/* CHECK IF INDEXES EXIST OR CREATE THEM */
						if (indexes != null) {
							Iterator<String> it = indexes.iterator();
							while (it.hasNext()) {
								String currInd = it.next();
								Set<String> currIndSet = new HashSet<String>();
								currIndSet.add(currInd);

								PersistedTableManager persistedTableManager = new PersistedTableManager();
								persistedTableManager.setTableName(tableName);
								persistedTableManager.setDialect(DatabaseDialect.get(getDataSource().getHibDialectClass()));
								persistedTableManager.setRowCountColumIncluded(false);

								int queryTimeout;
								try {
									queryTimeout = Integer
											.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT"));
								} catch (NumberFormatException nfe) {
									logger.debug("The value of SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT config must be an integer");
									queryTimeout = -1;
								}

								if (queryTimeout > 0) {
									logger.debug("Setting query timeout...");
									persistedTableManager.setQueryTimeout(queryTimeout);
								}
								persistedTableManager.createIndexesOnTable(dataSet, dataSource, tableName, currIndSet);
							}
						}

					} else {
						logger.debug("Cannot find dataset with signature [" + resultsetSignature + "] and hash [" + hashedSignature + "] inside the cache");
					}
				} finally {
					timing.stop();
					mapLocks.unlock(hashedSignature);
				}
			} else {
				timing.stop();
				logger.debug("Impossible to acquire the lock for dataset [" + hashedSignature + "]. Timeout. Returning a null datastore.");
			}
		} catch (InterruptedException e) {
			logger.debug("The current thread has failed to release the lock for dataset [" + hashedSignature + "] in time. Returning a null datastore.", e);
		}
		logger.debug("OUT");
		return toReturn;
	}

	// ===================================================================================
	// REFRESH METHODS
	// ===================================================================================

	@Override
	public void refresh(IDataSet dataSet) {
		try {
			dataSet.loadData();
			this.put(dataSet, dataSet.getDataStore(), true, null);
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	// ===================================================================================
	// PUT METHODS
	// ===================================================================================
	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.dataset.cache.ICache#put(java.lang.String, it.eng.spagobi.tools.dataset.common.datastore.IDataStore)
	 */

	@Override
	public void put(IDataSet dataSet, Set<String> columns) {
		logger.trace("IN");
		String signature = dataSet.getSignature();
		String hashedSignature = Helper.sha256(signature);
		Monitor timing = MonitorFactory.start("Knowage.SQLDBCache.putWithIterator:gettingMap");
		IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME, SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
		timing.stop();
		try {
			timing = MonitorFactory.start("Knowage.SQLDBCache.putWithIterator:gettingLock[" + hashedSignature + "]");
			if (mapLocks.tryLock(hashedSignature, getTimeout(), TimeUnit.SECONDS, getLeaseTime(), TimeUnit.SECONDS)) {
				timing.stop();
				try {
					timing = MonitorFactory.start("Knowage.SQLDBCache.putWithIterator:usingLock[" + hashedSignature + "]");
					cacheMetadata.updateAllCacheItems(getDataSource());

					// check again it is not already inserted
					if (!cacheMetadata.containsCacheItem(signature)) {
						String tableName = PersistedTableManager.generateRandomTableName(cacheMetadata.getTableNamePrefix());
						int queryTimeout = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT"));
						logger.debug("Configured query timeout is " + queryTimeout + "ms");
						PersistedTableManager persistedTableManager = new PersistedTableManager();
						persistedTableManager.setTableName(tableName);
						persistedTableManager.setDialect(DatabaseDialect.get(getDataSource().getHibDialectClass()));
						persistedTableManager.setRowCountColumIncluded(false);
						if (queryTimeout > 0) {
							logger.debug("Setting query timeout...");
							persistedTableManager.setQueryTimeout(queryTimeout);
						}
						persistedTableManager.persist(dataSet, getDataSource(), tableName);
						persistedTableManager.createIndexesOnTable(dataSet, getDataSource(), tableName, columns);

						cacheMetadata.addCacheItem(dataSet.getName(), signature, tableName,
								DatabaseUtilities.getUsedMemorySize(DataBaseFactory.getCacheDataBase(getDataSource()), "cache", tableName));

						if (getMetadata().isCleaningEnabled() && getMetadata().getAvailableMemory().compareTo(new BigDecimal(0)) < 0) {
							deleteToQuota();
						}

					}
				} catch (Exception e) {
					new SpagoBIRuntimeException(e);
				} finally {
					timing.stop();
					mapLocks.unlock(hashedSignature);
				}
			} else {
				timing.stop();
				logger.debug("Impossible to acquire the lock for dataset [" + hashedSignature + "]. Timeout.");
			}
		} catch (InterruptedException e) {
			logger.debug("The current thread has failed to release the lock for dataset [" + hashedSignature + "] in time.", e);
		}
		logger.debug("OUT");
	}

	@Override
	@Deprecated
	public long put(IDataSet dataSet, IDataStore dataStore, Set<String> columns) throws DataBaseException {
		return put(dataSet, dataStore, false, columns);
	}

	@Override
	@Deprecated
	public long put(IDataSet dataSet, IDataStore dataStore, boolean forceUpdate, Set<String> columns) throws DataBaseException {
		logger.trace("IN");

		if (dataStore.getMetaData().getFieldCount() == 0) {
			logger.debug("Dataset hasn't fields. The Dataset will not persisted.");
			return 0;
		}
		logger.debug("Dataset has #" + dataStore.getMetaData().getFieldCount() + "  fields. The Dataset will be persisted.");

		String signature = dataSet.getSignature();
		String hashedSignature = Helper.sha256(signature);
		long timeSpent = 0;
		Monitor timing = MonitorFactory.start("Knowage.SQLDBCache.put:gettingMap");
		IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME, SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
		timing.stop();
		try {
			timing = MonitorFactory.start("Knowage.SQLDBCache.put:gettingLock[" + hashedSignature + "]");
			if (mapLocks.tryLock(hashedSignature, getTimeout(), TimeUnit.SECONDS, getLeaseTime(), TimeUnit.SECONDS)) {
				timing.stop();
				try {
					timing = MonitorFactory.start("Knowage.SQLDBCache.put:usingLock[" + hashedSignature + "]");
					if (forceUpdate) {
						logger.debug("Update the dataset in cache if its old enought");
						updateAlreadyPresent();
					}
					// check again it is not already inserted
					if (getMetadata().containsCacheItem(signature)) {
						logger.debug("Cache item already inserted for dataset with label " + dataSet.getLabel() + " and signature " + dataSet.getSignature());
					}
					Monitor timingMemory = MonitorFactory.start("Knowage.SQLDBCache.put:calculateRequiredMemory[" + hashedSignature + "]");
					BigDecimal requiredMemory = getMetadata().getRequiredMemory(dataStore);
					timingMemory.stop();
					timingMemory = MonitorFactory.start("Knowage.SQLDBCache.put:calculateTotalMemory[" + hashedSignature + "]");
					BigDecimal maxUsableMemory = getMetadata().getTotalMemory().multiply(new BigDecimal(getMetadata().getCachePercentageToStore()))
							.divide(new BigDecimal(100), RoundingMode.FLOOR);
					timingMemory.stop();

					if (requiredMemory.compareTo(maxUsableMemory) < 1) { // if requiredMemory is less or equal to maxUsableMemory
						if (getMetadata().isCleaningEnabled() && !getMetadata().isAvailableMemoryGreaterThen(requiredMemory)) {
							deleteToQuota();
						}
						// check again if the cleaning mechanism is on and if there is enough space for the resultset
						if (!getMetadata().isCleaningEnabled() || getMetadata().isAvailableMemoryGreaterThen(requiredMemory)) {
							long start = System.currentTimeMillis();
							String tableName = persistStoreInCache(dataSet, dataStore);
							timeSpent = System.currentTimeMillis() - start;
							Map<String, Object> properties = new HashMap<String, Object>();
							if (dataSet instanceof VersionedDataSet) {
								dataSet = ((VersionedDataSet) dataSet).getWrappedDataset();
							}
							getMetadata().addCacheItem(((AbstractDataSet) dataSet).getName(), signature, properties, tableName, dataStore);

						} else {
							throw new CacheException("Store is to big to be persisted in cache." + " Store estimated dimension is [" + requiredMemory + "]"
									+ " while cache available space is [" + getMetadata().getAvailableMemory() + "]."
									+ " Increase cache size or execute the dataset disabling cache.");
						}
					} else {
						throw new CacheException("Store is to big to be persisted in cache." + " Store estimated dimension is [" + requiredMemory + "]"
								+ " while the maximum dimension allowed is [" + maxUsableMemory + "]."
								+ " Increase cache size or execute the dataset disabling cache.");
					}
				} finally {
					timing.stop();
					mapLocks.unlock(hashedSignature);
				}
			} else {
				timing.stop();
				logger.debug("Impossible to acquire the lock for dataset [" + hashedSignature + "]. Timeout.");
			}
		} catch (InterruptedException e) {
			logger.debug("The current thread has failed to release the lock for dataset [" + hashedSignature + "] in time.", e);
		}
		logger.debug("OUT");
		return timeSpent;
	}

	private String persistStoreInCache(IDataSet dataset, IDataStore resultset) {
		logger.trace("IN");
		try {
			int queryTimeout;
			try {
				queryTimeout = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT"));
			} catch (NumberFormatException nfe) {
				logger.debug("The value of SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT config must be an integer");
				queryTimeout = -1;
			}

			PersistedTableManager persistedTableManager = new PersistedTableManager();
			persistedTableManager.setRowCountColumIncluded(true);
			if (queryTimeout > 0) {
				persistedTableManager.setQueryTimeout(queryTimeout);
			}
			String tableName = PersistedTableManager.generateRandomTableName(this.getMetadata().getTableNamePrefix());
			Monitor monitor = MonitorFactory.start("spagobi.cache.sqldb.persistStoreInCache.persistdataset");
			persistedTableManager.persistDataset(dataset, resultset, getDataSource(), tableName);
			monitor.stop();
			return tableName;
		} catch (Throwable t) {
			if (t instanceof CacheException)
				throw (CacheException) t;
			else
				throw new CacheException("An unexpected error occured while persisting store in cache", t);
		} finally {
			logger.trace("OUT");
		}
	}

	private void updateStoreInCache(CacheItem cacheItem, IDataStore dataStore) {
		logger.trace("IN");

		Monitor monitor = null;
		try {
			int queryTimeout;
			try {
				queryTimeout = Integer.parseInt(SingletonConfig.getInstance().getConfigValue("SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT"));
			} catch (NumberFormatException nfe) {
				logger.debug("The value of SPAGOBI.CACHE.CREATE_AND_PERSIST_TABLE.TIMEOUT config must be an integer");
				queryTimeout = -1;
			}

			PersistedTableManager persistedTableManager = new PersistedTableManager();
			persistedTableManager.setRowCountColumIncluded(true);
			if (queryTimeout > 0) {
				persistedTableManager.setQueryTimeout(queryTimeout);
			}

			monitor = MonitorFactory.start("spagobi.cache.sqldb.updateStoreInCache.updatedataset");
			persistedTableManager.updateDataset(getDataSource(), dataStore, cacheItem.getTable());
		} catch (Exception e) {
			if (e instanceof CacheException)
				throw (CacheException) e;
			else
				throw new CacheException("An unexpected error occured while persisting store in cache", e);
		} finally {
			if (monitor != null) {
				monitor.stop();
			}
			logger.trace("OUT");
		}
	}

	// ===================================================================================
	// DELETE METHODS
	// ===================================================================================

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.dataset.cache.ICache#delete(it.eng.spagobi.tools.dataset .bo.IDataSet)
	 */

	@Override
	public boolean delete(IDataSet dataSet) {
		boolean result = false;

		logger.debug("IN");
		try {
			if (dataSet != null) {
				String dataSetSignature = dataSet.getSignature();
				result = dropTableAndRemoveCacheItem(dataSetSignature, false);
			} else {
				logger.warn("Input parameter [dataSet] is null");
			}
		} catch (Throwable t) {
			if (t instanceof CacheException)
				throw (CacheException) t;
			else
				throw new CacheException("An unexpected error occure while deleting dataset from cache", t);
		} finally {
			logger.debug("OUT");
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.dataset.cache.ICache#delete(java.lang.String)
	 */
	@Override
	public boolean delete(String signature, boolean isHash) {
		boolean result = false;

		logger.debug("IN");
		try {
			if (signature != null) {
				result = dropTableAndRemoveCacheItem(signature, isHash);
			} else {
				logger.warn("Input parameter [" + signature + "] is null");
			}
		} catch (Throwable t) {
			if (t instanceof CacheException)
				throw (CacheException) t;
			else
				throw new CacheException("An unexpected error occure while deleting dataset [" + signature + "] from cache", t);
		} finally {
			logger.debug("OUT");
		}

		return result;
	}

	private boolean dropTableAndRemoveCacheItem(String signature, boolean isHash) {
		logger.debug("IN");
		String hashedSignature;
		if (isHash) {
			hashedSignature = signature;
			logger.debug("Delete dataset with hash [" + signature + "]");
		} else {
			hashedSignature = Helper.sha256(signature);
			logger.debug("Delete dataset with signature [" + signature + "] and hash [" + hashedSignature + "]");
		}
		IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME, SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
		try {
			if (mapLocks.tryLock(hashedSignature, getTimeout(), TimeUnit.SECONDS, getLeaseTime(), TimeUnit.SECONDS)) {
				try {
					if (getMetadata().containsCacheItem(signature, isHash)) {
						PersistedTableManager persistedTableManager = new PersistedTableManager();
						String tableName = getMetadata().getCacheItem(signature, isHash).getTable();
						persistedTableManager.dropTableIfExists(getDataSource(), tableName);
						getMetadata().removeCacheItem(signature, isHash);
						logger.debug("Removed table " + tableName + " from [SQLDBCache] corresponding to the result Set: " + signature);
						logger.debug("Deleted");

						return true;
					} else {
						logger.debug("Not deleted, dataset not in cache");
						return false;
					}
				} finally {
					mapLocks.unlock(hashedSignature);
				}
			} else {
				logger.debug("Impossible to acquire the lock for dataset [" + hashedSignature + "]. Timeout. Returning false.");
			}
		} catch (InterruptedException e) {
			logger.debug("The current thread has failed to release the lock for dataset [" + hashedSignature + "] in time. Returning a null datastore.", e);
		}
		logger.debug("OUT");
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.dataset.cache.ICache#deleteQuota()
	 */

	/**
	 * Update the cache table if it's older than the CacheDsLastAccessTtl time
	 */
	public void updateAlreadyPresent() {
		logger.trace("IN");
		try {
			List<CacheItem> items = getMetadata().getCacheItems();
			for (CacheItem item : items) {
				long elapsedTime = (System.currentTimeMillis() - item.getLastUsedDate().getTime()) / 1000;
				if (elapsedTime > getMetadata().getCacheDsLastAccessTtl()) {
					logger.debug("FORCE REMOVE: The table with name " + item.getTable() + " is old and is going be to be removed");
					delete(item.getSignature(), true);
				}
			}
		} catch (Throwable t) {
			if (t instanceof CacheException)
				throw (CacheException) t;
			else
				throw new CacheException("An unexpected error occured while deleting cache to quota", t);
		} finally {
			logger.trace("OUT");
		}
	}

	private CacheItem getLastCachedItem() {
		CacheItem ci = null;
		Date localDate = new Date();
		List<CacheItem> items = getMetadata().getCacheItems();
		for (CacheItem cacheItem : items) {
			if (cacheItem.getCreationDate().before(localDate)) {
				ci = cacheItem;
				localDate = cacheItem.getCreationDate();
			}

		}
		return ci;
	}

	@Override
	public void deleteToQuota() {
		logger.trace("IN");
		boolean isEnough = false;
		try {
			List<CacheItem> items = getMetadata().getCacheItems();
			for (CacheItem item : items) {
				long elapsedTime = (System.currentTimeMillis() - item.getLastUsedDate().getTime()) / 1000;
				if (elapsedTime > getMetadata().getCacheDsLastAccessTtl()) {
					delete(item.getSignature(), true);
					if (getMetadata().getAvailableMemoryAsPercentage() > getMetadata().getCleaningQuota()) {
						isEnough = true;
						break;
					}
				}
			}
			// Second loop through datasets
			String lastCachedItem = null;
			if (!isEnough) {
				lastCachedItem = getLastCachedItem().getSignature();
				for (String signature : getMetadata().getSignatures()) {
					if (!signature.equals(lastCachedItem)) {
						delete(signature, true);
						if (getMetadata().getAvailableMemoryAsPercentage() > getMetadata().getCleaningQuota()) {
							isEnough = true;
							break;
						}
					}
				}
			}

		} catch (Throwable t) {
			if (t instanceof CacheException)
				throw (CacheException) t;
			else
				throw new CacheException("An unexpected error occured while deleting cache to quota", t);
		} finally {
			logger.trace("OUT");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.dataset.cache.ICache#deleteAll()
	 */

	@Override
	public void deleteAll() {
		logger.debug("Removing all tables from [SQLDBCache]");

		List<String> signatures = getMetadata().getSignatures();
		for (String signature : signatures) {
			delete(signature, true);
		}
		// Delete any other cache tables, even if not recorded as cache item
		// eraseExistingTables(getMetadata().getTableNamePrefix().toUpperCase());
		logger.debug("[SQLDBCache] All tables removed, Cache cleaned ");
	}

	// ===================================================================================
	// ACCESSOR METHODS
	// ===================================================================================

	/**
	 * @return the dataSource
	 */
	public IDataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.dataset.cache.ICache#getCacheMetadata()
	 */

	@Override
	public SQLDBCacheMetadata getMetadata() {
		return cacheMetadata;
	}

	@Override
	public UserProfile getUserProfile() {
		return userProfile;
	}

	@Override
	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public static long getTimeout() {
		long lockTimeout;
		try {
			lockTimeout = Long.parseLong(SingletonConfig.getInstance().getConfigValue("SPAGOBI.CACHE.HAZELCAST.TIMEOUT"));
		} catch (NumberFormatException nfe) {
			logger.debug("The value of SPAGOBI.CACHE.HAZELCAST.TIMEOUT config must be an integer");
			logger.debug("Setting lock timeout to default value [" + DEFAULT_HAZELCAST_TIMEOUT + "]");
			lockTimeout = DEFAULT_HAZELCAST_TIMEOUT;
		}
		return lockTimeout;
	}

	public static long getLeaseTime() {
		long lockLeaseTime;
		try {
			lockLeaseTime = Long.parseLong(SingletonConfig.getInstance().getConfigValue("SPAGOBI.CACHE.HAZELCAST.LEASETIME"));
		} catch (NumberFormatException nfe) {
			logger.debug("The value of SPAGOBI.CACHE.HAZELCAST.LEASETIME config must be an integer");
			logger.debug("Setting lock timeout to default value [" + DEFAULT_HAZELCAST_LEASETIME + "]");
			lockLeaseTime = DEFAULT_HAZELCAST_LEASETIME;
		}
		return lockLeaseTime;
	}

	@Override
	public void update(String hashedSignature, IDataStore dataStore) {
		logger.trace("IN");
		logger.debug("Dataset has #" + dataStore.getMetaData().getFieldCount() + "  fields. The Dataset will be persisted.");

		Monitor timing = MonitorFactory.start("Knowage.SQLDBCache.put:gettingMap");
		IMap mapLocks = DistributedLockFactory.getDistributedMap(SpagoBIConstants.DISTRIBUTED_MAP_INSTANCE_NAME, SpagoBIConstants.DISTRIBUTED_MAP_FOR_CACHE);
		timing.stop();
		try {
			timing = MonitorFactory.start("Knowage.SQLDBCache.put:gettingLock[" + hashedSignature + "]");
			if (mapLocks.tryLock(hashedSignature, getTimeout(), TimeUnit.SECONDS, getLeaseTime(), TimeUnit.SECONDS)) {
				timing.stop();
				try {
					timing = MonitorFactory.start("Knowage.SQLDBCache.put:usingLock[" + hashedSignature + "]");

					// check again it is not already inserted
					CacheItem cacheItem = getMetadata().getCacheItem(hashedSignature, true);
					Assert.assertNotNull(cacheItem, "Cannot find a cache item for [" + hashedSignature + "]");

					updateStoreInCache(cacheItem, dataStore);
					cacheItem.setCreationDate(new Date());
					getMetadata().updateCacheItem(cacheItem);
				} finally {
					timing.stop();
					mapLocks.unlock(hashedSignature);
				}
			} else {
				timing.stop();
				logger.debug("Impossible to acquire the lock for dataset [" + hashedSignature + "]. Timeout.");
			}
		} catch (InterruptedException e) {
			logger.debug("The current thread has failed to release the lock for dataset [" + hashedSignature + "] in time.", e);
		} finally {
			logger.debug("OUT");
		}
	}

}
