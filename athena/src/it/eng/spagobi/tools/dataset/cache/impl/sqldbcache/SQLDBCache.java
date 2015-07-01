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

import it.eng.qbe.dataset.QbeDataSet;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.CacheItem;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.ICacheActivity;
import it.eng.spagobi.tools.dataset.cache.ICacheEvent;
import it.eng.spagobi.tools.dataset.cache.ICacheListener;
import it.eng.spagobi.tools.dataset.cache.ICacheTrigger;
import it.eng.spagobi.tools.dataset.cache.JoinedDataSet;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.work.SQLDBCacheWriteWork;
import it.eng.spagobi.tools.dataset.common.association.Association;
import it.eng.spagobi.tools.dataset.common.association.AssociationGroup;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.persist.IDataSetTableDescriptor;
import it.eng.spagobi.tools.dataset.persist.PersistedTableManager;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import commonj.work.Work;
import commonj.work.WorkItem;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class SQLDBCache implements ICache {

	private boolean enabled;
	private IDataSource dataSource;

	private final SQLDBCacheMetadata cacheMetadata;

	private WorkManager spagoBIWorkManager;

	public static final String CACHE_NAME_PREFIX_CONFIG = "SPAGOBI.CACHE.NAMEPREFIX";

	static private Logger logger = Logger.getLogger(SQLDBCache.class);

	public SQLDBCache(SQLDBCacheConfiguration cacheConfiguration) {

		if (cacheConfiguration == null) {
			throw new CacheException("Impossible to initialize cache. The cache configuration object cannot be null");
		}

		this.enabled = true;
		this.dataSource = cacheConfiguration.getCacheDataSource();
		this.cacheMetadata = new SQLDBCacheMetadata(cacheConfiguration);

		this.spagoBIWorkManager = cacheConfiguration.getWorkManager();

		eraseExistingTables(cacheMetadata.getTableNamePrefix().toUpperCase());

		String databaseSchema = cacheConfiguration.getSchema();
		if (databaseSchema != null) {
			// test schema
			testDatabaseSchema(databaseSchema, dataSource);
		}
	}

	// ===================================================================================
	// CONTAINS METHODS
	// ===================================================================================

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#contains(it.eng.spagobi.tools .dataset.bo.IDataSet)
	 */
	public boolean contains(IDataSet dataSet) {
		return contains(dataSet.getSignature());
	}

	/**
	 * Returns signature of joining dataset containing the dataset passed as parameter
	 *
	 * @param dataSet
	 * @return
	 */
	public List<String> getJoinedDatasetReferringTo(IDataSet dataSet) {
		return getMetadata().getJoinedsReferringDataset(dataSet.getSignature());

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#contains(java.lang.String)
	 */
	public boolean contains(String resultsetSignature) {
		return getMetadata().containsCacheItem(resultsetSignature);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#contains(java.util.List)
	 */
	public boolean contains(List<IDataSet> dataSets) {
		return getNotContained(dataSets).size() > 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#getNotContained(java.util.List)
	 */
	public List<IDataSet> getNotContained(List<IDataSet> dataSets) {
		List<IDataSet> notContainedDataSets = new ArrayList<IDataSet>();
		for (IDataSet dataSet : dataSets) {
			if (contains(dataSet) == false) {
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
	public IDataStore get(String resultsetSignature) {
		IDataStore dataStore = null;

		logger.debug("IN");

		try {
			if (getMetadata().containsCacheItem(resultsetSignature)) {
				logger.debug("Resultset with signature [" + resultsetSignature + "] found");
				CacheItem cacheItem = getMetadata().getCacheItem(resultsetSignature);
				String tableName = cacheItem.getTable();
				logger.debug("The table associated to dataset [" + resultsetSignature + "] is [" + tableName + "]");
				dataStore = dataSource.executeStatement("SELECT * FROM " + tableName, 0, 0);

				/*
				 * StringBuffer selectBuffer = new StringBuffer(); IDataSetTableDescriptor descriptor = TemporaryTableManager.getTableDescriptor(null,
				 * tableName, dataSource); Set<String> columns = descriptor.getColumnNames(); Iterator<String> it = columns.iterator(); while (it.hasNext()) {
				 * String column = it.next(); if (column.equalsIgnoreCase("sbicache_row_id")) { continue; }
				 * selectBuffer.append(AbstractJDBCDataset.encapsulateColumnAlaias (column, dataSource)); if (it.hasNext()) { selectBuffer.append(", "); } }
				 * String selectClause = selectBuffer.toString(); if (selectClause.endsWith(", ")) { selectClause = selectClause.substring(0,
				 * selectClause.length() - 2); } String sql = "SELECT " + selectClause + " FROM " + tableName; dataStore = dataSource.executeStatement(sql, 0,
				 * 0);
				 */
			} else {
				logger.debug("Resultset with signature [" + resultsetSignature + "] not found");
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
	 * @see it.eng.spagobi.dataset.cache.ICache#get(it.eng.spagobi.tools.dataset. bo.IDataSet, java.util.List, java.util.List, java.util.List)
	 */
	public IDataStore get(IDataSet dataSet, List<GroupCriteria> groups, List<FilterCriteria> filters, List<ProjectionCriteria> projections) {
		IDataStore dataStore = null;

		logger.debug("IN");
		try {
			if (dataSet != null) {
				dataStore = getInternal(dataSet, groups, filters, projections);
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

	public IDataStore getInternal(IDataSet dataSet, List<GroupCriteria> groups, List<FilterCriteria> filters, List<ProjectionCriteria> projections) {
		logger.debug("IN");

		try {

			String resultsetSignature = dataSet.getSignature();
			if (!getMetadata().containsCacheItem(resultsetSignature)) {
				logger.debug("Not found resultSet with signature [" + resultsetSignature + "] inside the Cache");
				return null;
			}

			if (dataSet instanceof JoinedDataSet) {
				return queryJoinedCachedDataset((JoinedDataSet) dataSet, groups, filters, projections, resultsetSignature);
			} else {
				return queryStandardCachedDataset(groups, filters, projections, resultsetSignature);
			}

		} finally {
			logger.debug("OUT");
		}

	}

	private IDataStore queryJoinedCachedDataset(JoinedDataSet dataSet, List<GroupCriteria> groups, List<FilterCriteria> filters,
			List<ProjectionCriteria> projections, String resultsetSignature) {
		CacheItem cacheItem = getMetadata().getCacheItem(resultsetSignature);
		String tableName = cacheItem.getTable();
		logger.debug("Found resultSet with signature [" + resultsetSignature + "] inside the Cache, table used [" + tableName + "]");

		// we assume that all projections refer to the same dataset: this is not 100% true!!! TODO fix this
		List<ProjectionCriteria> projectionsForInLineView = null;
		if (projections != null && !projections.isEmpty()) {
			String dataSetName = projections.get(0).getDataset();
			projectionsForInLineView = getProjectionsForInLineView(cacheItem, tableName, dataSetName);
		}

		String inLineViewSQL = getInLineViewSQLDefinition(projectionsForInLineView, filters, cacheItem, tableName);
		String inLineViewAlias = "T" + Math.abs(new Random().nextInt());

		InLineViewBuilder sqlBuilder = new InLineViewBuilder(inLineViewSQL, inLineViewAlias);

		// Columns to SELECT
		if (projections != null) {
			for (ProjectionCriteria projection : projections) {
				String aggregateFunction = projection.getAggregateFunction();

				Map<String, String> datasetAlias = (Map<String, String>) cacheItem.getProperty("DATASET_ALIAS");
				String columnName = projection.getColumnName();
				if (datasetAlias != null) {
					columnName = datasetAlias.get(projection.getDataset()) + " - " + projection.getColumnName();
				}
				columnName = inLineViewAlias + "." + AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);

				if ((aggregateFunction != null) && (!aggregateFunction.isEmpty()) && (columnName != "*")) {
					columnName = aggregateFunction + "(" + columnName + ")";
				}

				String aliasName = projection.getAliasName();
				aliasName = AbstractJDBCDataset.encapsulateColumnName(aliasName, dataSource);
				if (aliasName != null && !aliasName.isEmpty()) {
					columnName += " AS " + aliasName;
				}

				sqlBuilder.column(columnName);

			}
		}

		// GROUP BY conditions
		if (groups != null) {
			for (GroupCriteria group : groups) {
				String aggregateFunction = group.getAggregateFunction();

				Map<String, String> datasetAlias = (Map<String, String>) cacheItem.getProperty("DATASET_ALIAS");
				String columnName = group.getColumnName();
				if (datasetAlias != null) {
					columnName = datasetAlias.get(group.getDataset()) + " - " + group.getColumnName();
				}
				columnName = inLineViewAlias + "." + AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);
				if ((aggregateFunction != null) && (!aggregateFunction.isEmpty()) && (columnName != "*")) {
					columnName = aggregateFunction + "(" + columnName + ")";
				}
				sqlBuilder.groupBy(columnName);
			}
		}

		String queryText = sqlBuilder.toString();
		logger.debug("Cached dataset access query is equal to [" + queryText + "]");

		IDataStore dataStore = dataSource.executeStatement(queryText, 0, 0);
		DataStore toReturn = (DataStore) dataStore;

		List<Integer> breakIndexes = (List<Integer>) cacheItem.getProperty("BREAK_INDEXES");
		if (breakIndexes != null) {
			dataStore.getMetaData().setProperty("BREAK_INDEXES", breakIndexes);
		}

		return toReturn;
	}

	private List<ProjectionCriteria> getProjectionsForInLineView(CacheItem cacheItem, String tableName, String dataSet) {
		IDataSetTableDescriptor descriptor = null;
		try {
			descriptor = TemporaryTableManager.getTableDescriptor(null, tableName, getDataSource());
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot read columns of table [" + tableName + "]", e);
		}
		Map<String, String> datasetsAlias = (Map<String, String>) cacheItem.getProperty("DATASET_ALIAS");
		Assert.assertNotNull(datasetsAlias, "Datasets' aliases must be specified!!");
		String dataSetAlias = datasetsAlias.get(dataSet);
		Assert.assertNotNull(dataSetAlias, "Dataset's alias must be specified!!");

		List<ProjectionCriteria> toReturn = new ArrayList<ProjectionCriteria>();
		Set<String> columnsName = descriptor.getColumnNames();
		Iterator<String> it = columnsName.iterator();
		while (it.hasNext()) {
			String column = it.next();
			String prefix = dataSetAlias.toUpperCase() + " - ";
			if (column.toUpperCase().startsWith(prefix)) {
				String colunmName = column.substring(prefix.length());
				ProjectionCriteria projection = new ProjectionCriteria(dataSet, colunmName, null, colunmName);
				toReturn.add(projection);
			}
		}
		return toReturn;
	}

	private String getInLineViewSQLDefinition(List<ProjectionCriteria> projections, List<FilterCriteria> filters, CacheItem cacheItem, String tableName) {

		SelectBuilder sqlBuilder = new SelectBuilder();
		sqlBuilder.from(tableName);
		sqlBuilder.setDistinctEnabled(true);

		// Columns to SELECT
		if (projections != null) {
			for (ProjectionCriteria projection : projections) {
				String aggregateFunction = projection.getAggregateFunction();

				Map<String, String> datasetAlias = (Map<String, String>) cacheItem.getProperty("DATASET_ALIAS");
				String columnName = projection.getColumnName();
				if (datasetAlias != null) {
					columnName = datasetAlias.get(projection.getDataset()) + " - " + projection.getColumnName();
				}
				columnName = AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);

				if ((aggregateFunction != null) && (!aggregateFunction.isEmpty()) && (columnName != "*")) {
					String aliasName = projection.getAliasName();
					aliasName = AbstractJDBCDataset.encapsulateColumnName(aliasName, dataSource);
					if (aliasName != null && !aliasName.isEmpty()) {
						columnName = aggregateFunction + "(" + columnName + ") AS " + aliasName;
					}
				}
				sqlBuilder.column(columnName);

			}
		}

		// WHERE conditions
		if (filters != null) {
			for (FilterCriteria filter : filters) {
				String leftOperand = null;
				if (filter.getLeftOperand().isCostant()) {
					// why? warning!
					leftOperand = filter.getLeftOperand().getOperandValueAsString();
				} else { // it's a column
					Map<String, String> datasetAlias = (Map<String, String>) cacheItem.getProperty("DATASET_ALIAS");
					String datasetLabel = filter.getLeftOperand().getOperandDataSet();
					leftOperand = filter.getLeftOperand().getOperandValueAsString();
					if (datasetAlias != null) {
						if (datasetAlias.get(datasetLabel) == null)
							continue;

						leftOperand = datasetAlias.get(datasetLabel) + " - " + filter.getLeftOperand().getOperandValueAsString();
					}
					leftOperand = AbstractJDBCDataset.encapsulateColumnName(leftOperand, dataSource);
				}

				String operator = filter.getOperator();

				String rightOperand = null;
				if (filter.getRightOperand().isCostant()) {
					if (filter.getRightOperand().isMultivalue()) {
						rightOperand = "(";
						String separator = "";
						String stringDelimiter = "'";
						List<String> values = filter.getRightOperand().getOperandValueAsList();
						for (String value : values) {
							rightOperand += separator + stringDelimiter + value + stringDelimiter;
							separator = ",";
						}
						rightOperand += ")";
					} else {
						rightOperand = filter.getRightOperand().getOperandValueAsString();
					}
				} else { // it's a column
					rightOperand = filter.getRightOperand().getOperandValueAsString();
					rightOperand = AbstractJDBCDataset.encapsulateColumnName(rightOperand, dataSource);
				}

				sqlBuilder.where(leftOperand + " " + operator + " " + rightOperand);
			}
		}

		String inLineViewSQL = sqlBuilder.toString();
		return inLineViewSQL;
	}

	private IDataStore queryStandardCachedDataset(List<GroupCriteria> groups, List<FilterCriteria> filters, List<ProjectionCriteria> projections,
			String resultsetSignature) {
		CacheItem cacheItem = getMetadata().getCacheItem(resultsetSignature);
		String tableName = cacheItem.getTable();
		logger.debug("Found resultSet with signature [" + resultsetSignature + "] inside the Cache, table used [" + tableName + "]");

		SelectBuilder sqlBuilder = new SelectBuilder();
		sqlBuilder.from(tableName);

		// Columns to SELECT
		if (projections != null) {
			for (ProjectionCriteria projection : projections) {
				String aggregateFunction = projection.getAggregateFunction();

				Map<String, String> datasetAlias = (Map<String, String>) cacheItem.getProperty("DATASET_ALIAS");
				String columnName = projection.getColumnName();
				if (datasetAlias != null) {
					columnName = datasetAlias.get(projection.getDataset()) + " - " + projection.getColumnName();
				}
				columnName = AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);

				if ((aggregateFunction != null) && (!aggregateFunction.isEmpty()) && (columnName != "*")) {
					String aliasName = projection.getAliasName();
					aliasName = AbstractJDBCDataset.encapsulateColumnName(aliasName, dataSource);
					if (aliasName != null && !aliasName.isEmpty()) {
						columnName = aggregateFunction + "(" + columnName + ") AS " + aliasName;
					}
				}
				sqlBuilder.column(columnName);

			}
		}

		// WHERE conditions
		if (filters != null) {
			for (FilterCriteria filter : filters) {
				String leftOperand = null;
				if (filter.getLeftOperand().isCostant()) {
					// why? warning!
					leftOperand = filter.getLeftOperand().getOperandValueAsString();
				} else { // it's a column
					Map<String, String> datasetAlias = (Map<String, String>) cacheItem.getProperty("DATASET_ALIAS");
					String datasetLabel = filter.getLeftOperand().getOperandDataSet();
					leftOperand = filter.getLeftOperand().getOperandValueAsString();
					if (datasetAlias != null) {
						leftOperand = datasetAlias.get(datasetLabel) + " - " + filter.getLeftOperand().getOperandValueAsString();
					}
					leftOperand = AbstractJDBCDataset.encapsulateColumnName(leftOperand, dataSource);
				}

				String operator = filter.getOperator();

				String rightOperand = null;
				if (filter.getRightOperand().isCostant()) {
					if (filter.getRightOperand().isMultivalue()) {
						rightOperand = "(";
						String separator = "";
						String stringDelimiter = "'";
						List<String> values = filter.getRightOperand().getOperandValueAsList();
						for (String value : values) {
							rightOperand += separator + stringDelimiter + value + stringDelimiter;
							separator = ",";
						}
						rightOperand += ")";
					} else {
						rightOperand = filter.getRightOperand().getOperandValueAsString();
					}
				} else { // it's a column
					rightOperand = filter.getRightOperand().getOperandValueAsString();
					rightOperand = AbstractJDBCDataset.encapsulateColumnName(rightOperand, dataSource);
				}

				sqlBuilder.where(leftOperand + " " + operator + " " + rightOperand);
			}
		}

		// GROUP BY conditions
		if (groups != null) {
			for (GroupCriteria group : groups) {
				String aggregateFunction = group.getAggregateFunction();

				Map<String, String> datasetAlias = (Map<String, String>) cacheItem.getProperty("DATASET_ALIAS");
				String columnName = group.getColumnName();
				if (datasetAlias != null) {
					columnName = datasetAlias.get(group.getDataset()) + " - " + group.getColumnName();
				}
				columnName = AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);
				if ((aggregateFunction != null) && (!aggregateFunction.isEmpty()) && (columnName != "*")) {
					columnName = aggregateFunction + "(" + columnName + ")";
				}
				sqlBuilder.groupBy(columnName);
			}
		}

		String queryText = sqlBuilder.toString();
		logger.debug("Cached dataset access query is equal to [" + queryText + "]");

		IDataStore dataStore = dataSource.executeStatement(queryText, 0, 0);
		DataStore toReturn = (DataStore) dataStore;

		List<Integer> breakIndexes = (List<Integer>) cacheItem.getProperty("BREAK_INDEXES");
		if (breakIndexes != null) {
			dataStore.getMetaData().setProperty("BREAK_INDEXES", breakIndexes);
		}

		return toReturn;
	}

	// ===================================================================================
	// LOAD METHODS
	// ===================================================================================

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#load(it.eng.spagobi.tools.dataset .bo.IDataSet, boolean)
	 */
	public IDataStore load(IDataSet dataSet, boolean wait) {
		List<IDataSet> dataSets = new ArrayList<IDataSet>();
		dataSets.add(dataSet);
		List<IDataStore> dataStores = load(dataSets, wait);
		return dataStores.get(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#load(java.util.List, boolean)
	 */
	public List<IDataStore> load(List<IDataSet> dataSets, boolean wait) {
		List<IDataStore> dataStores = new ArrayList<IDataStore>();

		try {
			List<Work> works = new ArrayList<Work>();
			for (IDataSet dataSet : dataSets) {
				// first we set parameters because they change the signature
				// dataSet.setParamsMap(parametersValues);

				IDataStore dataStore = null;

				// then we verified if the store associated to the joined
				// datatset is in cache
				if (contains(dataSet)) {
					dataStore = get(dataSet);
					dataStores.add(dataStore);
					continue;
				}

				// if not we create a work to store it and we add it to works
				// list
				dataSet.loadData();
				dataStore = dataSet.getDataStore();
				dataStores.add(dataStore);

				Work cacheWriteWork = new SQLDBCacheWriteWork(this, dataStore, dataSet);
				works.add(cacheWriteWork);
			}

			if (works.size() > 0) {
				if (wait == true) {
					if (spagoBIWorkManager == null) {
						for (int i = 0; i < dataSets.size(); i++) {
							this.put(dataSets.get(i), dataStores.get(i));
						}
					} else {
						commonj.work.WorkManager workManager = spagoBIWorkManager.getInnerInstance();
						List<WorkItem> workItems = new ArrayList<WorkItem>();
						for (Work work : works) {
							WorkItem workItem = workManager.schedule(work);
							workItems.add(workItem);
						}

						workManager.waitForAll(workItems, workManager.INDEFINITE);
					}
				} else {
					if (spagoBIWorkManager == null) {
						throw new RuntimeException("Impossible to save the store in background because the work manager is not properly initialized");
					}

					commonj.work.WorkManager workManager = spagoBIWorkManager.getInnerInstance();
					for (Work workItem : works) {
						workManager.schedule(workItem);
					}
				}

			}
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		}

		return dataStores;
	}

	// ===================================================================================
	// REFRESH METHODS
	// ===================================================================================

	public synchronized void refreshIfNotContained(IDataSet dataSet, boolean wait) {
		if (contains(dataSet) == false) {
			refresh(dataSet, wait);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#load(it.eng.spagobi.tools.dataset .bo.IDataSet, boolean)
	 */
	public IDataStore refresh(IDataSet dataSet, boolean wait) {

		IDataStore dataStore = null;
		try {
			dataSet.loadData();
			dataStore = dataSet.getDataStore();

			if (wait == true) {
				this.put(dataSet, dataStore);
			} else {
				if (spagoBIWorkManager == null) {
					throw new RuntimeException("Impossible to save the store in background because the work manager is not properly initialized");
				}

				commonj.work.WorkManager workManager = spagoBIWorkManager.getInnerInstance();
				Work cacheWriteWork = new SQLDBCacheWriteWork(this, dataStore, dataSet);
				workManager.schedule(cacheWriteWork);
			}
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}

		return dataStore;
	}

	public IDataStore refresh(JoinedDataSet joinedDataSet, AssociationGroup associationGroup) {
		logger.trace("IN");
		try {

			List<IDataSet> dataSets = joinedDataSet.getDataSets();
			SelectBuilder sqlBuilder = new SelectBuilder();

			Map<String, String> datasetAliases = new HashMap<String, String>();
			int aliasNo = 0;

			Map<String, List<String>> columnNames = new HashMap<String, List<String>>();
			List<Integer> columnBreakIndexes = new ArrayList<Integer>();
			int lastIndex = 0;
			columnBreakIndexes.add(lastIndex);
			for (IDataSet dataSet : dataSets) {
				List<String> names = new ArrayList<String>();
				if (contains(dataSet) == false)
					return null;
				String tableName = getMetadata().getCacheItem(dataSet.getSignature()).getTable();
				String tableAlias = "t" + ++aliasNo;
				datasetAliases.put(dataSet.getLabel(), tableAlias);
				sqlBuilder.from(tableName + " " + tableAlias);

				// TODO move this to dataset?
				String column = AbstractJDBCDataset.encapsulateColumnName("sbicache_row_id", dataSource);
				String alias = AbstractJDBCDataset.encapsulateColumnAlaias(tableAlias + " - sbicache_row_id", dataSource);
				names.add(alias);
				sqlBuilder.column(tableAlias + "." + column + " as " + alias);

				for (int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
					IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
					if (dataSet instanceof QbeDataSet
							|| (dataSet instanceof VersionedDataSet && ((VersionedDataSet) dataSet).getWrappedDataset() instanceof QbeDataSet)) {
						// in case of Qbe dataset, the name of the column is the
						// field alias, not the field not (that is
						// it.eng.spagobi.meta.Entity:field.....)
						column = AbstractJDBCDataset.encapsulateColumnName(fieldMeta.getAlias(), dataSource);
					} else {
						column = AbstractJDBCDataset.encapsulateColumnName(fieldMeta.getName(), dataSource);
					}
					alias = AbstractJDBCDataset.encapsulateColumnAlaias(tableAlias + " - " + fieldMeta.getAlias(), dataSource);
					names.add(alias);
					sqlBuilder.column(tableAlias + "." + column + " as " + alias);
				}
				lastIndex += dataSet.getMetadata().getFieldCount() + 1; // +1 since we added a surrogated key
				columnBreakIndexes.add(lastIndex);
				columnNames.put(dataSet.getLabel(), names);
			}
			columnBreakIndexes.remove(0);
			columnBreakIndexes.remove(columnBreakIndexes.size() - 1);

			Collection<Association> associaions = associationGroup.getAssociations();
			for (Association association : associaions) {
				String whereClause;
				Association.Field previousField = null;
				for (Association.Field field : association.getFields()) {
					if (previousField != null) {
						whereClause = "";
						String dataset, column;

						dataset = previousField.getDataSetLabel();
						column = previousField.getFieldName();
						column = AbstractJDBCDataset.encapsulateColumnName(column, dataSource);
						whereClause += datasetAliases.get(dataset) + "." + column;

						dataset = field.getDataSetLabel();
						column = field.getFieldName();
						column = AbstractJDBCDataset.encapsulateColumnName(column, dataSource);
						whereClause += " = " + datasetAliases.get(dataset) + "." + column;

						sqlBuilder.where(whereClause);

					}
					previousField = field;

				}

			}

			String queryText = sqlBuilder.toString();
			logger.trace("Join query is equal to [" + queryText + "]");

			IDataSetTableDescriptor descriptor = null;
			String tableName = new PersistedTableManager().generateRandomTableName(this.getMetadata().getTableNamePrefix());
			try {
				logger.debug("Creating cache table [" + tableName + "] with base query [" + queryText + "] for joined dataset ...");
				descriptor = TemporaryTableManager.createTable(null, queryText, tableName, getDataSource());
				logger.debug("Created cache table [" + tableName + "] with base query [" + queryText + "] for joined dataset.");
			} catch (Exception e) {
				throw new SpagoBIRuntimeException("Error while creating cache table with base query [" + queryText + "]", e);
			}

			IDataStore dataStore = dataSource.executeStatement("SELECT * FROM " + descriptor.getTableName(), 0, 0);

			// dataStore.getMetaData().setProperty("BREAK_INDEXES", columnBreakIndexes);
			// dataStore.getMetaData().setProperty("COLUMN_NAMES", columnNames);
			// dataStore.getMetaData().setProperty("DATASET_ALIAS", datasetAliases);

			CacheItem item = getMetadata().addCacheItem(joinedDataSet.getSignature(), tableName, dataStore);
			item.setProperty("BREAK_INDEXES", columnBreakIndexes);
			item.setProperty("COLUMN_NAMES", columnNames);
			item.setProperty("DATASET_ALIAS", datasetAliases);

			logger.debug("dataset " + joinedDataSet.getLabel() + " is a joined dataset, add reference in map");
			List<IDataSet> dsReferred = joinedDataSet.getDataSets();
			// item.setProperty("DATASETS_REFERRED", datasetAlias);

			for (Iterator iterator = dsReferred.iterator(); iterator.hasNext();) {
				logger.debug("add reference");
				IDataSet iDataSet = (IDataSet) iterator.next();
				getMetadata().addJoinedDatasetReference(iDataSet.getSignature(), joinedDataSet.getSignature());
			}

			DataStore toReturn = (DataStore) dataStore;

			return toReturn;
		} catch (Throwable t) {
			if (t instanceof CacheException)
				throw (CacheException) t;
			else
				throw new CacheException("An unexpected error occured while loading joined store from cache", t);
		} finally {
			logger.trace("OUT");
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
	public synchronized void put(IDataSet dataSet, IDataStore dataStore) {
		logger.trace("IN");
		try {

			// check again it is not already inserted
			if (getMetadata().containsCacheItem(dataSet.getSignature())) {
				logger.debug("Cache item already inserted for dataset with label " + dataSet.getLabel() + " and signature " + dataSet.getSignature());
				return;
			}

			BigDecimal requiredMemory = getMetadata().getRequiredMemory(dataStore);

			if (getMetadata().isCleaningEnabled() && !getMetadata().isAvailableMemoryGreaterThen(requiredMemory)) {
				deleteToQuota();
			}

			// check again if the cleaning mechanism is on and if there is enough space for the resultset
			if (!getMetadata().isCleaningEnabled() || getMetadata().isAvailableMemoryGreaterThen(requiredMemory)) {
				String signature = dataSet.getSignature();
				String tableName = persistStoreInCache(dataSet, signature, dataStore);
				CacheItem item = getMetadata().addCacheItem(signature, tableName, dataStore);
				List<Integer> breakIndexes = (List<Integer>) dataStore.getMetaData().getProperty("BREAK_INDEXES");
				if (breakIndexes != null) {
					item.setProperty("BREAK_INDEXES", breakIndexes);
				}
				Map<String, List<String>> columnNames = (Map<String, List<String>>) dataStore.getMetaData().getProperty("COLUMN_NAMES");
				if (columnNames != null) {
					item.setProperty("COLUMN_NAMES", columnNames);
				}
				// a
				Map<String, String> datasetAlias = (Map<String, String>) dataStore.getMetaData().getProperty("DATASET_ALIAS");
				if (datasetAlias != null) {
					item.setProperty("DATASET_ALIAS", datasetAlias);
				}
				// if it is a joined dataset update references
				if (dataSet instanceof JoinedDataSet) {
					logger.debug("dataset " + dataSet.getLabel() + " is a joined dataset, add reference in map");
					JoinedDataSet jDs = (JoinedDataSet) dataSet;
					List<IDataSet> dsReferred = jDs.getDataSets();
					// item.setProperty("DATASETS_REFERRED", datasetAlias);

					for (Iterator iterator = dsReferred.iterator(); iterator.hasNext();) {
						logger.debug("add reference");
						IDataSet iDataSet = (IDataSet) iterator.next();
						getMetadata().addJoinedDatasetReference(iDataSet.getSignature(), dataSet.getSignature());
					}

				}

			} else {
				throw new CacheException("Store is to big to be persisted in cache." + " Store extimated dimenion is ["
						+ getMetadata().getRequiredMemory(dataStore) + "]" + " while cache available space is [" + getMetadata().getAvailableMemory() + "]."
						+ " Incrase cache size or execute the dataset disabling cache.");
			}
		} catch (Throwable t) {
			if (t instanceof CacheException)
				throw (CacheException) t;
			else
				throw new CacheException("An unexpected error occured while adding store into cache", t);
		} finally {
			logger.trace("OUT");
		}

		logger.debug("OUT");
	}

	private String persistStoreInCache(IDataSet dataset, String signature, IDataStore resultset) {
		logger.trace("IN");
		try {
			PersistedTableManager persistedTableManager = new PersistedTableManager();
			persistedTableManager.setRowCountColumIncluded(!(dataset instanceof JoinedDataSet));
			String tableName = persistedTableManager.generateRandomTableName(this.getMetadata().getTableNamePrefix());
			persistedTableManager.persistDataset(dataset, resultset, getDataSource(), tableName);
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

	// ===================================================================================
	// DELETE METHODS
	// ===================================================================================

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.dataset.cache.ICache#delete(it.eng.spagobi.tools.dataset .bo.IDataSet)
	 */
	public boolean delete(IDataSet dataSet) {
		boolean result = false;

		logger.debug("IN");
		try {
			if (dataSet != null) {
				String dataSetSignature = dataSet.getSignature();
				result = delete(dataSetSignature);

				// delete also Joined dataset using current dataset

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
	public boolean delete(String signature) {
		logger.debug("IN");
		logger.debug("delete " + signature);
		if (getMetadata().containsCacheItem(signature)) {
			PersistedTableManager persistedTableManager = new PersistedTableManager();
			String tableName = getMetadata().getCacheItem(signature).getTable();
			persistedTableManager.dropTableIfExists(getDataSource(), tableName);
			getMetadata().removeCacheItem(tableName);
			logger.debug("Removed table " + tableName + " from [SQLDBCache] corresponding to the result Set: " + signature);
			logger.debug("OUT deleted");

			return true;
		}
		logger.debug("OUT not deleted");

		return false;
	}

	/*
	 * (non-Javadoc)
	 */
	public void deleteDatasetAndJoined(String signature) {
		logger.debug("IN");

		boolean deleted = delete(signature);

		// keep track of signatures of joined removed so that they can later be removed from map
		List<String> removed = new ArrayList<String>();

		if (deleted == true) {
			logger.debug("delete joined dataset cache referring to " + signature);
			List<String> joinedSignatures = getMetadata().getJoinedsReferringDataset(signature);
			List<String> toRemove = new ArrayList<String>();
			if (joinedSignatures != null) {

				for (Iterator iterator = joinedSignatures.iterator(); iterator.hasNext();) {
					String joinedSignature = (String) iterator.next();
					logger.debug("Joined signature cache to delete " + joinedSignature);
					boolean del = delete(joinedSignature);
					logger.debug("joined dataset cache " + joinedSignature + " deleted? " + del);
					if (del) {
						removed.add(joinedSignature);
					}
				}

				// remove from map all signature in removed List
				for (Iterator iterator = removed.iterator(); iterator.hasNext();) {
					String sigToRemove = (String) iterator.next();
					Set<String> cachedSignatures = getMetadata().getDatasetToJoinedMap().keySet();
					for (Iterator iterator2 = cachedSignatures.iterator(); iterator2.hasNext();) {
						String cachedSignature = (String) iterator2.next();
						List<String> list = getMetadata().getDatasetToJoinedMap().get(cachedSignature);
						if (list != null && list.contains(sigToRemove)) {
							list.remove(sigToRemove);
						}
					}
				}

			}

		}

		logger.debug("OUT");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.dataset.cache.ICache#deleteQuota()
	 */
	public void deleteToQuota() {
		logger.trace("IN");
		try {
			List<String> signatures = getMetadata().getSignatures();
			for (String signature : signatures) {
				delete(signature);
				if (getMetadata().getAvailableMemoryAsPercentage() > getMetadata().getCleaningQuota()) {
					break;
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
	public void deleteAll() {
		logger.debug("Removing all tables from [SQLDBCache]");

		List<String> signatureToDelete = new ArrayList<String>();

		List<String> signatures = getMetadata().getSignatures();
		for (String signature : signatures) {
			CacheItem item = getMetadata().getCacheItem(signature);
			signatureToDelete.add(item.getSignature());
		}

		for (String signature : signatureToDelete) {
			delete(signature);
		}

		logger.debug("[SQLDBCache] All tables removed, Cache cleaned ");
	}

	/**
	 * Erase existing tables that begins with the prefix
	 *
	 * @param prefix
	 *            table name prefix
	 *
	 */
	private void eraseExistingTables(String prefix) {
		PersistedTableManager persistedTableManager = new PersistedTableManager();
		persistedTableManager.dropTablesWithPrefix(getDataSource(), prefix);
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
	 * @param dataSource
	 *            the dataSource to set
	 */
	public void setDataSource(IDataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Test if the passed schema name is correct. Create a table in the database via the dataSource then try to select the table using the schema.table syntax
	 *
	 * @param schema
	 *            the schema name
	 * @param dataSource
	 *            the DataSource
	 */
	private void testDatabaseSchema(String schema, IDataSource dataSource) {

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
		String dialect = dataSource.getHibDialectClass();
		PersistedTableManager persistedTableManager = new PersistedTableManager();
		persistedTableManager.setDialect(dialect);
		Random ran = new Random();
		int x = ran.nextInt(100);
		String tableName = "SbiTest" + x;
		persistedTableManager.setTableName(tableName);

		try {
			persistedTableManager.persistDataset(dataStore, dataSource);
		} catch (Exception e) {
			logger.error("Error persisting dataset", e);
		}

		// try to query the table using the Schema.TableName syntax if
		// schemaName is valorized

		try {
			if (schema.isEmpty()) {
				dataSource.executeStatement("SELECT * FROM " + tableName, 0, 0);

			} else {
				dataSource.executeStatement("SELECT * FROM " + schema + "." + tableName, 0, 0);
			}
		} catch (Exception e) {
			throw new CacheException("An unexpected error occured while testing database schema for cache", e);
		} finally {
			// Dropping table
			persistedTableManager.dropTableIfExists(dataSource, tableName);

		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.dataset.cache.ICache#getCacheMetadata()
	 */
	public SQLDBCacheMetadata getMetadata() {
		return cacheMetadata;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#addListener(it.eng.spagobi. tools.dataset.cache.ICacheEvent,
	 * it.eng.spagobi.tools.dataset.cache.ICacheListener)
	 */
	public void addListener(ICacheEvent event, ICacheListener listener) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#scheduleActivity(it.eng.spagobi .tools.dataset.cache.ICacheActivity,
	 * it.eng.spagobi.tools.dataset.cache.ICacheTrigger)
	 */
	public void scheduleActivity(ICacheActivity activity, ICacheTrigger trigger) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#enable(boolean)
	 */
	public void enable(boolean enable) {
		this.enabled = enabled;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#isEnabled()
	 */
	public boolean isEnabled() {
		return enabled;
	}

	public WorkManager getSpagoBIWorkManager() {
		return spagoBIWorkManager;
	}

	public void setSpagoBIWorkManager(WorkManager spagoBIWorkManager) {
		this.spagoBIWorkManager = spagoBIWorkManager;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spagobi.tools.dataset.cache.ICache#refresh(java.util.List, boolean)
	 */
	public IDataStore refresh(List<IDataSet> dataSets, boolean wait) {
		// TODO Auto-generated method stub
		return null;
	}

}
