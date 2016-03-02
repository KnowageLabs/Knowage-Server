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
package it.eng.spagobi.dataset.cache.impl.sqldbcache.test;

import it.eng.spagobi.dataset.cache.impl.sqldbcache.DataType;
import it.eng.spagobi.dataset.cache.test.TestConstants;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.ICacheMetadata;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCacheConfiguration;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.utilities.cache.CacheItem;

import java.math.BigDecimal;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public abstract class AbstractSQLDBCacheTest extends AbstractCacheTest {

	static private Logger logger = Logger.getLogger(AbstractSQLDBCacheTest.class);

	// Test cases

	@Override
	public void testCacheInit() {
		assertNotNull("Cache correctly initialized", cache);
	}

	public void testCacheInitWrongTablePrefixName() {
		boolean cacheError = false;
		CacheFactory cacheFactory = new CacheFactory();

		SQLDBCacheConfiguration cacheConfigurationCustom = new SQLDBCacheConfiguration();
		// Setting wrong table prefix for tables created by the cache
		cacheConfigurationCustom.setTableNamePrefix("");
		cacheConfigurationCustom.setCacheSpaceAvailable(TestConstants.CACHE_CONFIG_CACHE_DIMENSION);
		cacheConfigurationCustom.setCachePercentageToClean(TestConstants.CACHE_CONFIG_PERCENTAGE_TO_CLEAN);
		cacheConfigurationCustom.setSchema(TestConstants.CACHE_CONFIG_SCHEMA_NAME);
		cacheConfigurationCustom.setCacheDataSource(dataSourceWriting);

		DataType dataType = new DataType(); // class used for setting data type dimension properties
		cacheConfigurationCustom.setObjectsTypeDimension(dataType.getProps());
		try {
			ICache cacheCustom = cacheFactory.getCache(cacheConfigurationCustom);
		} catch (CacheException e) {
			cacheError = true;
		} finally {
			assertTrue("WRONG: Cache correctly initialized", cacheError);
		}

	}

	public void testCacheInitWrongSchemaName() {
		boolean cacheError = false;
		CacheFactory cacheFactory = new CacheFactory();

		SQLDBCacheConfiguration cacheConfigurationCustom = new SQLDBCacheConfiguration();
		cacheConfigurationCustom.setTableNamePrefix(TestConstants.CACHE_CONFIG_TABLE_PREFIX);
		cacheConfigurationCustom.setCacheSpaceAvailable(TestConstants.CACHE_CONFIG_CACHE_DIMENSION);
		cacheConfigurationCustom.setCachePercentageToClean(TestConstants.CACHE_CONFIG_PERCENTAGE_TO_CLEAN);
		// setting wrong schema name
		cacheConfigurationCustom.setSchema("xxadasf3w324");
		cacheConfigurationCustom.setCacheDataSource(dataSourceWriting);

		DataType dataType = new DataType(); // class used for setting data type dimension properties
		cacheConfigurationCustom.setObjectsTypeDimension(dataType.getProps());
		try {
			ICache cacheCustom = cacheFactory.getCache(cacheConfigurationCustom);
		} catch (CacheException e) {
			cacheError = true;
		} finally {
			assertTrue("WRONG: Cache correctly initialized", cacheError);
		}

	}

	public void testCacheDimension() {
		assertEquals(TestConstants.CACHE_CONFIG_CACHE_DIMENSION, cache.getMetadata().getAvailableMemory());
		assertEquals(new Integer(100), cache.getMetadata().getAvailableMemoryAsPercentage());
		assertEquals(new Integer(TestConstants.CACHE_CONFIG_PERCENTAGE_TO_CLEAN), cache.getMetadata().getCleaningQuota());
		assertEquals(new Integer(0), cache.getMetadata().getNumberOfObjects());
	}

	public void testCachePutJDBCDataSet() {
		IDataStore resultset;

		sqlDataset.loadData();
		resultset = sqlDataset.getDataStore();
		cache.put(sqlDataset, resultset);
		assertNotNull(cache.get(sqlDataset.getSignature()));
		logger.debug("JDBCDataset inserted inside cache");
	}

	public void testCachePutFileDataSet() {
		IDataStore resultset;

		fileDataset.loadData();
		resultset = fileDataset.getDataStore();
		cache.put(fileDataset, resultset);
		assertNotNull(cache.get(fileDataset.getSignature()));
		logger.debug("FileDataSet inserted inside cache");
	}

	public void testCachePutQbeDataSet() {
		IDataStore resultset;

		qbeDataset.loadData();
		resultset = qbeDataset.getDataStore();
		cache.put(qbeDataset, resultset);
		assertNotNull(cache.get(qbeDataset.getSignature()));
		logger.debug("QbeDataSet inserted inside cache");

	}

	public void testCachePutFlatDataSet() {
		IDataStore resultset;

		flatDataset.loadData();
		resultset = flatDataset.getDataStore();
		cache.put(flatDataset, resultset);
		assertNotNull(cache.get(flatDataset.getSignature()));
		logger.debug("FlatDataSet inserted inside cache");

	}

	public void testCachePutScriptDataSet() {
		IDataStore resultset;

		scriptDataset.loadData();
		resultset = scriptDataset.getDataStore();
		cache.put(scriptDataset, resultset);
		assertNotNull(cache.get(scriptDataset.getSignature()));
		logger.debug("ScriptDataset inserted inside cache");

	}

	public void testCacheGetJDBCDataSet() {
		String signature = sqlDataset.getSignature();
		IDataStore dataStore = cache.get(signature);
		assertNull(dataStore);
		if (dataStore == null) {
			sqlDataset.loadData();
			dataStore = sqlDataset.getDataStore();
			cache.put(sqlDataset, dataStore);
		}
		dataStore = cache.get(signature);
		assertNotNull(dataStore);
	}

	public void testCacheGetFileDataSet() {
		String signature = fileDataset.getSignature();
		IDataStore dataStore = cache.get(signature);
		assertNull(dataStore);
		if (dataStore == null) {
			fileDataset.loadData();
			dataStore = fileDataset.getDataStore();
			cache.put(fileDataset, dataStore);
		}
		dataStore = cache.get(signature);
		assertNotNull(dataStore);
	}

	public void testCacheGetQbeDataSet() {
		String signature = qbeDataset.getSignature();
		IDataStore dataStore = cache.get(signature);
		assertNull(dataStore);
		if (dataStore == null) {
			qbeDataset.loadData();
			dataStore = qbeDataset.getDataStore();
			cache.put(qbeDataset, dataStore);
		}
		dataStore = cache.get(signature);
		assertNotNull(dataStore);
	}

	public void testCacheGetFlatDataSet() {
		String signature = flatDataset.getSignature();
		IDataStore dataStore = cache.get(signature);
		assertNull(dataStore);
		if (dataStore == null) {
			flatDataset.loadData();
			dataStore = flatDataset.getDataStore();
			cache.put(flatDataset, dataStore);
		}
		dataStore = cache.get(signature);
		assertNotNull(dataStore);
	}

	public void testCacheGetScriptDataSet() {
		String signature = scriptDataset.getSignature();
		IDataStore dataStore = cache.get(signature);
		assertNull(dataStore);
		if (dataStore == null) {
			scriptDataset.loadData();
			dataStore = scriptDataset.getDataStore();
			cache.put(scriptDataset, dataStore);
		}
		dataStore = cache.get(signature);
		assertNotNull(dataStore);
	}

	public void testCacheDeleteCachedDataset() {
		IDataStore resultset;

		fileDataset.loadData();
		resultset = fileDataset.getDataStore();
		cache.put(fileDataset, resultset);
		logger.debug("FileDataSet inserted inside cache");
		String tableName = cache.getMetadata().getCacheItem(fileDataset.getSignature()).getTable();
		assertTrue(cache.delete(fileDataset.getSignature()));
		assertNull("Dataset still present in cache registry", cache.get(fileDataset.getSignature()));
		IDataStore dataStore = null;
		// Check that the table is not present, if this fail with an exception we know that the table isn't on DB
		try {
			dataStore = dataSourceWriting.executeStatement("SELECT * FROM " + tableName, 0, 0);

		} catch (Exception e) {
			logger.debug("The table [" + tableName + "] not found on cache database");
			logger.debug("Delete successfull: The table [" + tableName + "] not found on cache database");

		}

		assertNull("Delete fail: Dataset is still present on cache ", dataStore);

	}

	// Trying to delete a dataset that is not in the cache
	public void testCacheDeleteUncachedDataset() {
		IDataStore resultset;

		fileDataset.loadData();
		resultset = fileDataset.getDataStore();

		assertFalse(cache.delete(fileDataset.getSignature()));

	}

	public void testCacheDeleteAll() {
		testCachePutJDBCDataSet();
		testCachePutFileDataSet();
		cache.deleteAll();

		ICacheMetadata cacheMetadata = cache.getMetadata();
		BigDecimal cacheSpaceAvaiable = cacheMetadata.getAvailableMemory();
		boolean cacheCleaned = false;
		if (cacheSpaceAvaiable.compareTo(TestConstants.CACHE_CONFIG_CACHE_DIMENSION) == 0) {
			cacheCleaned = true;
		}
		assertTrue("The cache wasn't cleaned correctly", cacheCleaned);
	}

	public void testCacheDatasetDimension() {
		ICacheMetadata cacheMetadata = cache.getMetadata();
		BigDecimal cacheSpaceAvaiable = cacheMetadata.getAvailableMemory();

		IDataStore resultset;

		fileDataset.loadData();
		resultset = fileDataset.getDataStore();

		BigDecimal estimatedDimension = cacheMetadata.getRequiredMemory(resultset);

		BigDecimal usedSpacePrevision = cacheSpaceAvaiable.subtract(estimatedDimension);

		boolean flag = false;
		if (usedSpacePrevision.compareTo(BigDecimal.ZERO) >= 0) {
			flag = true;
			cache.put(fileDataset, resultset);
			IDataStore cachedResultSet = cache.get(fileDataset.getSignature());
			if (cachedResultSet != null) {
				flag = true;
			} else {
				flag = false;
			}
		} else {
			flag = true;
			logger.debug("Not enought space for inserting the dataset in cache");

		}
		assertTrue("Problem calculating space avaiable for inserting dataset in cache", flag);

	}

	public void testCacheHasSpaceForDataset() {
		IDataStore resultset;

		fileDataset.loadData();
		resultset = fileDataset.getDataStore();

		ICacheMetadata cacheMetadata = cache.getMetadata();
		if (cacheMetadata.hasEnoughMemoryForStore(resultset)) {
			cache.put(fileDataset, resultset);
			IDataStore cachedResultSet = cache.get(fileDataset.getSignature());
			assertNotNull("Checking space for dataset is wrong", cachedResultSet);

		}
	}

	public void testCacheZeroSpaceAvaiable() {

		// Create a cache with space available equal to zero
		ICache cacheZero = createCacheZero();
		boolean exception = false;

		IDataStore resultset;

		sqlDataset.loadData();
		resultset = sqlDataset.getDataStore();
		try {
			cacheZero.put(sqlDataset, resultset);

		} catch (CacheException ex) {
			logger.error("Dataset cannot be cached");
			exception = true;
		} finally {
			assertTrue("Wrong behavior: Exception expected but not catched", exception);
			assertNull("Wrong behavior: dataset cached even if the space available is zero", cacheZero.get(sqlDataset.getSignature()));
			cacheZero.deleteAll();
		}

	}

	public void testGetDimensionSpaceAvailable() {
		ICacheMetadata cacheMetadata = cache.getMetadata();
		BigDecimal cacheSpaceAvaiable = cacheMetadata.getAvailableMemory();
		assertNotNull("Error calculating avaiable cache space", cacheSpaceAvaiable);
		logger.debug(" >> Avaiable cache space: " + cacheSpaceAvaiable + " byte");
	}

	public void testGetDimensionSpaceUsed() {
		IDataStore resultset;

		fileDataset.loadData();
		resultset = fileDataset.getDataStore();

		ICacheMetadata cacheMetadata = cache.getMetadata();
		BigDecimal estimatedDatasetDimension = cacheMetadata.getRequiredMemory(resultset);
		assertNotNull("Error calculating dimension of dataset", estimatedDatasetDimension);
		logger.debug(" >> Estimated dataset dimension: " + estimatedDatasetDimension + " byte");
	}

	public void testCountNumberOfObjects() {
		testCachePutJDBCDataSet();
		testCachePutFileDataSet();
		testCachePutQbeDataSet();

		ICacheMetadata cacheMetadata = cache.getMetadata();
		boolean result = false;
		if (cacheMetadata.getNumberOfObjects() == 3) {
			result = true;
		}
		assertTrue(result);
	}

	/*
	 * public void testCacheCleaning(){ ICache cacheCustom = createCache(791449); IDataStore resultset;
	 *
	 * //Insert first dataset qbeDataset.loadData(); resultset = qbeDataset.getDataStore();
	 *
	 * try { cacheCustom.put(qbeDataset, resultset); } finally { assertNotNull(cacheCustom.get(qbeDataset.getSignature()));
	 * logger.debug("QbeDataSet inserted inside cache"); }
	 *
	 * ICacheMetadata cacheMetadata = cacheCustom.getMetadata();
	 *
	 * //Second dataset (too big for avaiable space) fileDataset.loadData(); resultset = fileDataset.getDataStore();
	 *
	 * assertFalse(cacheMetadata.hasEnoughMemoryForStore(resultset));
	 *
	 * try{ cacheCustom.put(fileDataset, resultset);
	 *
	 * } finally { assertNull("Dataset found in cache but there should not be",cacheCustom.get(fileDataset.getSignature()));
	 *
	 * cacheCustom.deleteAll(); } }
	 */

	public void testSchemaName() {

		String schemaName = TestConstants.CACHE_CONFIG_SCHEMA_NAME;

		IDataStore resultset;

		qbeDataset.loadData();
		resultset = qbeDataset.getDataStore();
		String signature = qbeDataset.getSignature();
		cache.put(qbeDataset, resultset);
		logger.debug("QbeDataSet inserted inside cache");

		ICacheMetadata cacheMetadata = cache.getMetadata();
		CacheItem cacheItem = cache.getMetadata().getCacheItem(signature);
		String tableName = cacheItem.getTable();
		if (schemaName.isEmpty()) {
			IDataStore dataStore = dataSourceWriting.executeStatement("SELECT * FROM " + tableName, 0, 0);

		} else {
			IDataStore dataStore = dataSourceWriting.executeStatement("SELECT * FROM " + schemaName + "." + tableName, 0, 0);
		}

	}

	public void testSchemaRead() {
		String schemaName = TestConstants.CACHE_CONFIG_SCHEMA_NAME;

		// Create a fake dataStore
		DataStore dataStore = new DataStore();
		IMetaData metadata = new MetaData();
		IFieldMetaData fieldMetaData = new FieldMetadata();
		fieldMetaData.setAlias("test_column");
		fieldMetaData.setName("test_column");
		fieldMetaData.setType(String.class);
		fieldMetaData.setFieldType(FieldType.ATTRIBUTE);
		metadata.addFiedMeta(fieldMetaData);
		dataStore.setMetaData(metadata);
		Record record = new Record();
		Field field = new Field();
		field.setValue("try");
		record.appendField(field);
		dataStore.appendRecord(record);

		// persist the datastore as a table on db
		String dialect = dataSourceWriting.getHibDialectClass();
		PersistedTableManager persistedTableManager = new PersistedTableManager();
		persistedTableManager.setDialect(dialect);
		Random ran = new Random();
		int x = ran.nextInt(100);
		String tableName = "SbiTest" + x;
		persistedTableManager.setTableName(tableName);

		try {
			persistedTableManager.persistDataset(dataStore, dataSourceWriting);
		} catch (Exception e) {
			logger.error("Error persisting dataset");
		}

		// try to query the table using the SchemaName.TableName syntax if schemaName is valorized

		try {
			if (schemaName.isEmpty()) {
				dataSourceWriting.executeStatement("SELECT * FROM " + tableName, 0, 0);

			} else {
				dataSourceWriting.executeStatement("SELECT * FROM " + schemaName + "." + tableName, 0, 0);
			}
		} finally {
			// Dropping table
			persistedTableManager.dropTableIfExists(dataSourceWriting, tableName);

		}
	}
}
