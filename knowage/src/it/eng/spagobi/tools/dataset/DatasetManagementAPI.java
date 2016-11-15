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
package it.eng.spagobi.tools.dataset;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.JDBCDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.FilterCriteria;
import it.eng.spagobi.tools.dataset.cache.GroupCriteria;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.InLineViewBuilder;
import it.eng.spagobi.tools.dataset.cache.ProjectionCriteria;
import it.eng.spagobi.tools.dataset.cache.SelectBuilder;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.work.SQLDBCacheWriteWork;
import it.eng.spagobi.tools.dataset.common.behaviour.QuerableBehaviour;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.Field;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IField;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.datastore.Record;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData.FieldType;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.MetaData;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.common.query.IAggregationFunction;
import it.eng.spagobi.tools.dataset.crosstab.CrosstabDefinition;
import it.eng.spagobi.tools.dataset.crosstab.Measure;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.database.AbstractDataBase;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.sql.SqlUtils;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import commonj.work.Work;

/**
 * DataLayer facade class. It manage the access to SpagoBI's datasets. It is built on top of the dao. It manages all complex operations that involve more than a
 * simple CRUD operations over the dataset. It also manages user's profilation and autorization. Other class must access dataset through this class and not
 * calling directly the DAO.
 *
 * @author gavardi, gioia
 *
 */

public class DatasetManagementAPI {

	private UserProfile userProfile;
	private IDataSetDAO dataSetDao;

	// XML tags
	public static final String PARAMETERSLIST = "PARAMETERSLIST";
	public static final String ROWS = "ROWS";
	public static final String ROW = "ROW";
	public static final String NAME = "NAME";
	public static final String TYPE = "TYPE";

	private static final int METAMODEL_LIMIT = 5000;

	static private Logger logger = Logger.getLogger(DatasetManagementAPI.class);

	// ==============================================================================
	// COSTRUCTOR METHODS
	// ==============================================================================
	public DatasetManagementAPI() {
		setUserProfile(null);
	}

	public DatasetManagementAPI(UserProfile userProfile) {
		setUserProfile(userProfile);
	}

	// ==============================================================================
	// ACCESSOR METHODS
	// ==============================================================================
	public UserProfile getUserProfile() {
		return userProfile;
	}

	public String getUserId() {
		return getUserProfile().getUserUniqueIdentifier().toString();
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
		if (dataSetDao != null) {
			dataSetDao.setUserProfile(userProfile);
		}
	}

	private IDataSetDAO getDataSetDAO() {
		if (dataSetDao == null) {
			try {
				dataSetDao = DAOFactory.getDataSetDAO();
				if (getUserProfile() != null) {
					dataSetDao.setUserProfile(userProfile);
				}
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An unexpected error occured while instatiating the DAO", t);
			}
		}
		return dataSetDao;
	}

	// ==============================================================================
	// API METHODS
	// ==============================================================================
	public List<IDataSet> getDataSets() {
		try {
			List<IDataSet> dataSets = null;
			if (UserUtilities.isTechnicalUser(getUserProfile())) {
				dataSets = getDataSetDAO().loadDataSets();
				// for (IDataSet dataSet : dataSets) {
				// checkQbeDataset(dataSet);
				// }
			} else {
				dataSets = getMyDataDataSet();
			}
			return dataSets;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	public IDataSet getDataSet(String label) {

		logger.debug("IN");

		try {
			if (StringUtilities.isEmpty(label)) {
				throw new RuntimeException("Invalid value [" + label + "] for input parameter [label]");
			}

			IDataSet dataSet = null;
			try {
				dataSet = getDataSetDAO().loadDataSetByLabel(label);
			} catch (Throwable t) {
				throw new RuntimeException("An unexpected error occured while loading dataset [" + label + "]");
			}

			if (dataSet == null) {
				throw new RuntimeException("Dataset [" + label + "] does not exist");
			}

			if (DataSetUtilities.isExecutableByUser(dataSet, getUserProfile()) == false) {
				throw new RuntimeException("User [" + getUserId() + "] cannot access to dataset [" + label + "]");
			}
			return dataSet;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method [getDataSet]", t);
		} finally {
			logger.debug("OUT");
		}
	}

	public List<IFieldMetaData> getDataSetFieldsMetadata(String label) {
		try {
			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(label);

			if (dataSet == null) {
				throw new RuntimeException("Impossible to get dataset [" + label + "] from SpagoBI Server");
			}

			IMetaData metadata = dataSet.getMetadata();
			if (metadata == null) {
				throw new RuntimeException("Impossible to retrive metadata of dataset [" + metadata + "]");
			}

			List<IFieldMetaData> fieldsMetaData = new ArrayList<IFieldMetaData>();
			int fieldCount = metadata.getFieldCount();
			for (int i = 0; i < fieldCount; i++) {
				IFieldMetaData fieldMetaData = metadata.getFieldMeta(i);
				fieldsMetaData.add(fieldMetaData);
			}

			return fieldsMetaData;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	public List<JSONObject> getDataSetParameters(String label) {
		logger.debug("IN");
		try {
			List<JSONObject> parametersList = new ArrayList<JSONObject>();
			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(label);

			if (dataSet == null) {
				throw new RuntimeException("Impossible to get dataset [" + label + "] from SpagoBI Server");
			}

			String strParams = dataSet.getParameters();
			if (strParams == null) {
				return parametersList;
			}

			try {
				SourceBean xmlParams = SourceBean.fromXMLString(strParams);
				SourceBean sbRows = (SourceBean) xmlParams.getAttribute(ROWS);
				List lst = sbRows.getAttributeAsList(ROW);
				for (Iterator iterator = lst.iterator(); iterator.hasNext();) {
					SourceBean sbRow = (SourceBean) iterator.next();
					String namePar = sbRow.getAttribute(NAME) != null ? sbRow.getAttribute(NAME).toString() : null;
					String typePar = sbRow.getAttribute(TYPE) != null ? sbRow.getAttribute(TYPE).toString() : null;

					if (typePar != null && typePar.startsWith("class")) {
						typePar = typePar.substring(6);
					}
					JSONObject paramMetaDataJSON = new JSONObject();
					String filterId = "ds__" + dataSet.getLabel() + "__" + namePar;
					paramMetaDataJSON.put("id", filterId);
					paramMetaDataJSON.put("labelObj", dataSet.getLabel());
					paramMetaDataJSON.put("nameObj", dataSet.getName());
					paramMetaDataJSON.put("typeObj", "Dataset");
					paramMetaDataJSON.put("namePar", namePar);
					paramMetaDataJSON.put("typePar", typePar);
					parametersList.add(paramMetaDataJSON);
				}
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("Impossible to parse parameters [" + strParams + "]", t);
			} finally {
				logger.debug("OUT");
			}

			return parametersList;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	public String persistDataset(String label) {
		return persistDataset(label, false);
	}

	/*
	 * Refresh cache for a specific dataset
	 */
	public String persistDataset(String label, boolean forceRefresh) {
		logger.debug("IN dataset label " + label);
		SQLDBCache cache = (SQLDBCache) SpagoBICacheManager.getCache();
		cache.setUserProfile(userProfile);
		IDataSet dataSet = this.getDataSetDAO().loadDataSetByLabel(label);
		cache.refresh(dataSet, null, true, forceRefresh);

		String signature = dataSet.getSignature();
		logger.debug("Retrieve table name for signature " + signature);
		CacheItem cacheItem = cache.getMetadata().getCacheItem(signature);

		String tableName = null;
		if (cacheItem != null) {
			tableName = cacheItem.getTable();
		} else {
			logger.error("Table name could not be found for signature " + signature);
		}
		logger.debug("OUT");
		return tableName;
	}

	/**
	 * insert into data store last cache date if present
	 *
	 * @param cache
	 * @param dataStore
	 * @param dataSet
	 */
	private void addLastCacheDate(ICache cache, IDataStore dataStore, IDataSet dataSet) {
		logger.debug("IN");
		// In case of using cache add last date
		String signature = dataSet.getSignature();
		if (cache.getMetadata() != null) {
			CacheItem item = cache.getMetadata().getCacheItem(signature);
			if (item != null) {
				Date lastDate = item.getCreationDate();
				if (lastDate != null) {
					logger.debug("Last cache date is " + lastDate);
					dataStore.setCacheDate(lastDate);
				} else {
					logger.debug("last cache date not present");
				}

			}
		}
		logger.debug("OUT");
	}

	public IDataStore getDataStore(String label, int offset, int fetchSize, boolean isRealtime, Map<String, String> parametersValues,
			List<GroupCriteria> groups, List<FilterCriteria> filters, List<FilterCriteria> filterCriteriaForMetaModel, List<ProjectionCriteria> projections,
			List<ProjectionCriteria> summaryRowProjections) {

		try {
			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(label);
			setDataSetParameters(dataSet, parametersValues);
			IDataStore dataStore = null;

			if (dataSet.isPersisted() && !dataSet.isPersistedHDFS()) {
				dataStore = queryPersistedDataset(groups, filters, projections, summaryRowProjections, dataSet, offset, fetchSize);
				dataStore.setCacheDate(getPersistedDate(dataSet));
			} else if (dataSet.isFlatDataset()) {
				dataStore = queryFlatDataset(groups, filters, projections, summaryRowProjections, dataSet, offset, fetchSize);
				dataStore.setCacheDate(new Date());
			} else if (isRealtime && dataSet instanceof JDBCDataSet && !SqlUtils.isBigDataDialect(dataSet.getDataSource().getHibDialectName())) {
				dataStore = queryJDBCDataset(groups, filters, projections, summaryRowProjections, dataSet, offset, fetchSize);
				dataStore.setCacheDate(new Date());
			} else if (isRealtime) {
				dataStore = queryRealtimeDataset(groups, filterCriteriaForMetaModel, projections, summaryRowProjections, dataSet, offset, fetchSize);
				dataStore.setCacheDate(new Date());
			} else {
				SQLDBCache cache = (SQLDBCache) SpagoBICacheManager.getCache();
				cache.setUserProfile(userProfile);

				IDataStore cachedResultSet = cache.get(dataSet, groups, filters, projections, summaryRowProjections, offset, fetchSize);

				if (cachedResultSet == null) {
					dataSet.loadData();
					IDataStore baseDataStore = dataSet.getDataStore();

					if (baseDataStore.getRecordsCount() > METAMODEL_LIMIT || haveCountDistinct(projections) || haveCountDistinct(summaryRowProjections)) {
						cache.put(dataSet, baseDataStore);
						dataStore = cache.get(dataSet, groups, filters, projections, summaryRowProjections, offset, fetchSize);
						if (dataStore == null) {
							throw new CacheException("An unexpected error occured while executing method");
						}
						adjustMetadata((DataStore) dataStore, dataSet, null);
						dataSet.decode(dataStore);
					} else {
						dataStore = cache.refresh(dataSet, baseDataStore, false);

						String tableName = DataStore.DEFAULT_SCHEMA_NAME + "." + DataStore.DEFAULT_TABLE_NAME;
						Map<String, String> datasetAlias = getDatasetAlias(dataSet);
						List<String> orderColumns = new ArrayList<String>();

						String originalQuery = getQueryText(null, tableName, groups, filterCriteriaForMetaModel, projections, null, dataSet, true,
								datasetAlias, orderColumns);
						boolean hasSortingOnCalculatedColumns = checkSortingOnCalculatedColumns(orderColumns);

						IDataStore originalDataStore = dataStore.aggregateAndFilterRecords(originalQuery, -1, -1);
						if (hasSortingOnCalculatedColumns) {
							appendCalculatedColumnsToDataStore(projections, datasetAlias, originalDataStore);
							sortColumnsOnDataStore(orderColumns, originalDataStore);
						}

						IDataStore pagedDataStore = originalDataStore.paginateRecords(offset, fetchSize);
						if (!hasSortingOnCalculatedColumns) {
							appendCalculatedColumnsToDataStore(projections, datasetAlias, pagedDataStore);
						}

						if (summaryRowProjections != null && summaryRowProjections.size() > 0) {
							String summaryRowQuery = getQueryText(null, tableName, groups, filters, projections, summaryRowProjections, dataSet, true,
									datasetAlias, null);
							IDataStore summaryRowDataStore = originalDataStore.aggregateAndFilterRecords(summaryRowQuery, -1, -1);

							appendSummaryRowToPagedDataStore(projections, summaryRowProjections, pagedDataStore, summaryRowDataStore);
						}

						dataStore = pagedDataStore;
					}

					// if result was not cached put refresh date as now
					dataStore.setCacheDate(new Date());
				} else {
					dataStore = cachedResultSet;
					addLastCacheDate(cache, dataStore, dataSet);
					/*
					 * since the datastore, at this point, is a JDBC datastore, it does not contain information about measures/attributes, fields' name and
					 * alias... therefore we adjust its metadata
					 */
					adjustMetadata((DataStore) dataStore, dataSet, null);
					dataSet.decode(dataStore);
				}
			}
			// TODO: caso in cui non riesco a effettuare la pagina lato datasource... pagino direttamente sulla lista di record...
			// pageDataStoreRecords((DataStore) dataStore, offset, fetchSize);
			return dataStore;

		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private boolean haveCountDistinct(List<ProjectionCriteria> projections) {
		for (ProjectionCriteria projection : projections) {
			if (AggregationFunctions.COUNT_DISTINCT.equals(projection.getAggregateFunction())) {
				return true;
			}
		}
		return false;
	}

	private Date getPersistedDate(IDataSet dataSet) {
		Date toReturn = null;
		String triggerGroupName = "DEFAULT";
		String triggerName = "persist_" + dataSet.getName();
		try {
			// try to get the schedule trigger
			Trigger trigger = DAOFactory.getSchedulerDAO().loadTrigger(triggerGroupName, triggerName);
			Date previousFireTime = null;
			if (trigger != null) { // dataset is scheduled
				previousFireTime = trigger.getPreviousFireTime();
			}
			if (previousFireTime != null) {
				toReturn = previousFireTime;
			} else { // dataset is not scheduled or no previous fire time available
				toReturn = dataSet.getDateIn();
			}
		} catch (EMFUserError e) {
			logger.error("Unable to load trigger with name [" + triggerName + "] from group [" + triggerGroupName + "]", e);
		}
		return toReturn;
	}

	protected void pageDataStoreRecords(DataStore dataStore, int offset, int fetchSize) {
		List records = dataStore.getRecords();
		int size = records.size();
		if (offset > size) {
			logger.debug("Offset [" + offset + "] is greater than records size [" + size + "]");
			logger.debug("Returning an  empty list of records");
			records.clear();
		} else {
			if (fetchSize > (size - offset)) {
				logger.debug("Fetch size [" + fetchSize + "] is greater than records size [" + size + "]");
				logger.debug("Returning an list of records from offset to size");
				records = records.subList(offset, size);
			} else {
				logger.debug("Returning an list of records from offset to fetch size");
				records = records.subList(offset, fetchSize);
			}
		}
	}

	private List<IDataStore> storeDataSetsInCache(List<IDataSet> joinedDataSets, Map<String, String> parametersValues, boolean wait) {

		List<IDataStore> dataStores = new ArrayList<IDataStore>();

		try {
			SQLDBCache cache = (SQLDBCache) SpagoBICacheManager.getCache();
			cache.setUserProfile(userProfile);

			WorkManager spagoBIWorkManager;
			try {
				spagoBIWorkManager = new WorkManager(getSpagoBIConfigurationProperty("JNDI_THREAD_MANAGER"));
			} catch (NamingException t) {
				throw new RuntimeException("Impossible to initialize work manager");
			}
			commonj.work.WorkManager workManager = spagoBIWorkManager.getInnerInstance();

			List<Work> workItemList = new ArrayList<Work>();
			for (IDataSet dataSet : joinedDataSets) {
				// first we set parameters because they change the signature
				dataSet.setParamsMap(parametersValues);

				// then we verified if the store associated to the joined
				// dtatset is in cache
				if (cache.contains(dataSet)) {
					continue;
				}

				// if not we create a work to store it and we add it to works
				// list

				dataSet.loadData();
				IDataStore dataStore = dataSet.getDataStore();

				Work cacheWriteWork = new SQLDBCacheWriteWork(cache, dataStore, dataSet, userProfile);

				workItemList.add(cacheWriteWork);
			}

			if (workItemList.size() > 0) {
				if (wait == true) {
					workManager.waitForAll(workItemList, workManager.INDEFINITE);
				} else {
					for (Work work : workItemList) {
						workManager.schedule(work);
					}
				}
			}

		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while storing datasets in cache", t);
		} finally {
			logger.debug("OUT");
		}

		return dataStores;
	}

	private IDataStore storeDataSetInCache(IDataSet dataSet, Map<String, String> parametersValues, boolean wait) {
		try {
			SQLDBCache cache = (SQLDBCache) SpagoBICacheManager.getCache();
			cache.setUserProfile(userProfile);
			dataSet.setParamsMap(parametersValues);
			dataSet.loadData();
			IDataStore dataStore = dataSet.getDataStore();

			WorkManager spagoBIWorkManager;
			try {
				spagoBIWorkManager = new WorkManager(getSpagoBIConfigurationProperty("JNDI_THREAD_MANAGER"));
			} catch (NamingException t) {
				throw new RuntimeException("Impossible to initialize work manager");
			}
			commonj.work.WorkManager workManager = spagoBIWorkManager.getInnerInstance();

			List<Work> workItemList = new ArrayList<Work>();
			Work cacheWriteWork = new SQLDBCacheWriteWork(cache, dataStore, dataSet, userProfile);
			workItemList.add(cacheWriteWork);

			if (workItemList.size() > 0) {
				if (wait == true) {
					workManager.waitForAll(workItemList, workManager.INDEFINITE);
				} else {
					for (Work work : workItemList) {
						workManager.schedule(work);
					}
				}
			}

			return dataStore;
		} catch (ParametersNotValorizedException p) {
			throw new ParametersNotValorizedException(p.getMessage());
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * @deprectade
	 */
	public IDataStore getAggregatedDataStore(String label, int offset, int fetchSize, int maxResults, CrosstabDefinition crosstabDefinition) {
		try {

			IDataSet dataSet = this.getDataSetDAO().loadDataSetByLabel(label);
			// checkQbeDataset(dataSet);

			SQLDBCache cache = (SQLDBCache) SpagoBICacheManager.getCache();
			cache.setUserProfile(userProfile);
			IDataStore cachedResultSet = cache.get(dataSet);
			IDataStore dataStore = null;

			if (cachedResultSet == null) {
				// Dataset not yet cached
				dataSet.loadData(offset, fetchSize, maxResults);
				dataStore = dataSet.getDataStore();

				cache.put(dataSet, dataStore);

			}

			List<ProjectionCriteria> projectionCriteria = this.getProjectionCriteria(label, crosstabDefinition);
			List<FilterCriteria> filterCriteria = new ArrayList<FilterCriteria>(); // empty
																					// in
																					// this
																					// case
			List<GroupCriteria> groupCriteria = this.getGroupCriteria(label, crosstabDefinition);
			dataStore = cache.get(dataSet, groupCriteria, filterCriteria, projectionCriteria, 0, 0);

			/*
			 * since the datastore, at this point, is a JDBC datastore, it does not contain information about measures/attributes, fields' name and alias...
			 * therefore we adjust its metadata
			 */
			this.adjustMetadata((DataStore) dataStore, dataSet, null);

			logger.debug("Decoding dataset ...");
			dataSet.decode(dataStore);
			LogMF.debug(logger, "Dataset decoded: {0}", dataStore);

			return dataStore;

		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private static String getParametersNotValorized(List<JSONObject> parameters, Map<String, String> parametersValues) {
		String toReturn = "";

		for (Iterator<JSONObject> iterator = parameters.iterator(); iterator.hasNext();) {
			JSONObject parameter = iterator.next();
			try {
				String parameterName = parameter.getString("namePar");
				if (parametersValues.get(parameterName) == null) {
					toReturn += parameterName;
					if (iterator.hasNext()) {
						toReturn += ", ";
					}
				}
			} catch (Throwable t) {
				throw new SpagoBIRuntimeException("An unexpected exception occured while checking spagobi filters ", t);
			}
		}
		return toReturn;
	}

	private static String getSpagoBIConfigurationProperty(String propertyName) {
		try {
			String propertyValue = null;
			IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
			Config cacheSpaceCleanableConfig = configDao.loadConfigParametersByLabel(propertyName);
			if ((cacheSpaceCleanableConfig != null) && (cacheSpaceCleanableConfig.isActive())) {
				propertyValue = cacheSpaceCleanableConfig.getValueCheck();
			}
			return propertyValue;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected exception occured while loading spagobi property [" + propertyName + "]", t);
		}
	}

	public List<IDataSet> getAllDataSet() {
		try {
			List<IDataSet> dataSets = getDataSetDAO().loadDataSets();
			return dataSets;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	public List<IDataSet> getEnterpriseDataSet() {
		try {
			List<IDataSet> dataSets = getDataSetDAO().loadEnterpriseDataSets(getUserProfile());
			// for (IDataSet dataSet : dataSets) {
			// checkQbeDataset(dataSet);
			// }
			return dataSets;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	public List<IDataSet> getOwnedDataSet() {
		try {

			List<IDataSet> dataSets = getDataSetDAO().loadDataSetsOwnedByUser(getUserProfile(), true);
			// for (IDataSet dataSet : dataSets) {
			// checkQbeDataset(dataSet);
			// }
			return dataSets;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}

	}

	public List<IDataSet> getSharedDataSet() {
		try {
			List<IDataSet> validDataSets = getDataSetDAO().loadDatasetsSharedWithUser(getUserProfile(), true);

			// for (IDataSet dataSet : dataSets) {
			// checkQbeDataset(dataSet);
			// }
			return validDataSets;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private List<IDataSet> filterDatasetsByUser(List<IDataSet> dataSets, Set<Domain> categories) {
		List<IDataSet> validDataSets = new LinkedList<IDataSet>();
		for (IDataSet ds : dataSets) {
			Integer idCategory = ds.getCategoryId();
			if (idCategory != null) {
				for (Domain dom : categories) {
					if (idCategory.equals(dom.getValueId())) {
						validDataSets.add(ds);
						break;
					}
				}
			}
		}
		return validDataSets;
	}

	public List<IDataSet> getUncertifiedDataSet() {
		try {

			List<IDataSet> dataSets = getDataSetDAO().loadDatasetOwnedAndShared(getUserProfile());
			// for (IDataSet dataSet : dataSets) {
			// checkQbeDataset(dataSet);
			// }
			return dataSets;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	public List<IDataSet> getMyDataDataSet() {
		try {

			List<IDataSet> dataSets = getDataSetDAO().loadMyDataDataSets(getUserProfile());

			// for (IDataSet dataSet : dataSets) {
			// checkQbeDataset(dataSet);
			// }
			return dataSets;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	public List<IDataSet> getMyFederatedDataSets() {
		try {

			List<IDataSet> dataSets = getDataSetDAO().loadMyDataFederatedDataSets(getUserProfile());

			// for (IDataSet dataSet : dataSets) {
			// checkQbeDataset(dataSet);
			// }
			return dataSets;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	public Integer creatDataSet(IDataSet dataSet) {
		logger.debug("IN");
		Integer toReturn = null;
		if (dataSet == null) {
			logger.error("Dataset is null");
			return null;
		}

		try {

			validateDataSet(dataSet);

			// validate
			logger.debug("Getting the data set dao..");
			IDataSetDAO dataSetDao = DAOFactory.getDataSetDAO();
			logger.debug("DatasetDAo loaded");
			logger.debug("Inserting the data set wit the dao...");
			toReturn = dataSetDao.insertDataSet(dataSet);
			logger.debug("Data Set inserted");
			if (toReturn != null) {
				logger.info("DataSet " + dataSet.getLabel() + " saved with id = " + toReturn);
			} else {
				logger.error("DataSet not saved: check error log");
			}

		} catch (ValidationException e) {
			logger.error("Failed validation of dataset " + dataSet.getLabel() + " with cause: " + e.getValidationMessage());
			throw new RuntimeException(e.getValidationMessage(), e);
		} catch (EMFUserError e) {
			logger.error("EmfUserError ", e);
			throw new RuntimeException("EmfUserError ", e);
		}

		logger.debug("OUT");
		return toReturn;
	}

	private boolean validateDataSet(IDataSet dataSet) throws ValidationException, EMFUserError {
		logger.debug("IN");

		logger.debug("check the dataset not alreaduy present with same label");

		IDataSet datasetLab = DAOFactory.getDataSetDAO().loadDataSetByLabel(dataSet.getLabel());
		// checkQbeDataset(datasetLab);

		if (datasetLab != null) {
			throw new ValidationException("Dataset with label " + dataSet.getLabel() + " already found");
		}

		logger.debug("OUT");
		return true;

	}

	public class ValidationException extends Exception {
		private final String validationMessage;

		ValidationException(String _validationMessage) {
			super();
			this.validationMessage = _validationMessage;
		}

		ValidationException(String _validationMessage, Throwable e) {
			super(e);
			this.validationMessage = _validationMessage;
		}

		public String getValidationMessage() {
			return this.validationMessage;
		}

	}

	// ------------------------------------------------------------------------------
	// Methods for extracting information from CrosstabDefinition and related
	// ------------------------------------------------------------------------------

	/**
	 * @deprecated
	 */
	@Deprecated
	private List<ProjectionCriteria> getProjectionCriteria(String dataset, CrosstabDefinition crosstabDefinition) {
		logger.debug("IN");
		List<ProjectionCriteria> projectionCriterias = new ArrayList<ProjectionCriteria>();

		List<CrosstabDefinition.Row> rows = crosstabDefinition.getRows();
		List<CrosstabDefinition.Column> colums = crosstabDefinition.getColumns();
		List<Measure> measures = crosstabDefinition.getMeasures();

		// appends columns
		Iterator<CrosstabDefinition.Column> columsIt = colums.iterator();
		while (columsIt.hasNext()) {
			CrosstabDefinition.Column aColumn = columsIt.next();
			String columnName = aColumn.getEntityId();
			ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, columnName);
			projectionCriterias.add(aProjectionCriteria);
		}
		// appends rows
		Iterator<CrosstabDefinition.Row> rowsIt = rows.iterator();
		while (rowsIt.hasNext()) {
			CrosstabDefinition.Row aRow = rowsIt.next();
			String columnName = aRow.getEntityId();
			ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, columnName);
			projectionCriterias.add(aProjectionCriteria);
		}

		// appends measures
		Iterator<Measure> measuresIt = measures.iterator();
		while (measuresIt.hasNext()) {
			Measure aMeasure = measuresIt.next();
			IAggregationFunction function = aMeasure.getAggregationFunction();
			String columnName = aMeasure.getEntityId();
			if (columnName == null) {
				// when defining a crosstab inside the SmartFilter document, an
				// additional COUNT field with id QBE_SMARTFILTER_COUNT
				// is automatically added inside query fields, therefore the
				// entity id is not found on base query selected fields

				/*
				 * columnName = "Count"; if (aMeasure.getEntityId().equals(QBE_SMARTFILTER_COUNT)) { toReturn
				 * .append(AggregationFunctions.COUNT_FUNCTION.apply("*")); } else { logger.error("Entity id " + aMeasure.getEntityId() +
				 * " not found on the base query!!!!"); throw new RuntimeException("Entity id " + aMeasure.getEntityId() + " not found on the base query!!!!");
				 * }
				 */
			} else {
				if (function != AggregationFunctions.NONE_FUNCTION) {
					ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, function.getName(), columnName);
					projectionCriterias.add(aProjectionCriteria);
				} else {
					ProjectionCriteria aProjectionCriteria = new ProjectionCriteria(dataset, columnName, null, columnName);
					projectionCriterias.add(aProjectionCriteria);
				}
			}

		}

		logger.debug("OUT");
		return projectionCriterias;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	private List<GroupCriteria> getGroupCriteria(String dataset, CrosstabDefinition crosstabDefinition) {
		logger.debug("IN");
		List<GroupCriteria> groupCriterias = new ArrayList<GroupCriteria>();

		List<CrosstabDefinition.Row> rows = crosstabDefinition.getRows();
		List<CrosstabDefinition.Column> colums = crosstabDefinition.getColumns();

		// appends columns
		Iterator<CrosstabDefinition.Column> columsIt = colums.iterator();
		while (columsIt.hasNext()) {
			CrosstabDefinition.Column aColumn = columsIt.next();
			String columnName = aColumn.getEntityId();
			GroupCriteria groupCriteria = new GroupCriteria(dataset, columnName, null);
			groupCriterias.add(groupCriteria);
		}

		// appends rows
		Iterator<CrosstabDefinition.Row> rowsIt = rows.iterator();
		while (rowsIt.hasNext()) {
			CrosstabDefinition.Row aRow = rowsIt.next();
			String columnName = aRow.getEntityId();
			GroupCriteria groupCriteria = new GroupCriteria(dataset, columnName, null);
			groupCriterias.add(groupCriteria);
		}
		logger.debug("OUT");
		return groupCriterias;
	}

	protected void adjustMetadata(DataStore dataStore, IDataSet dataset, JSONArray fieldOptions) {

		IMetaData dataStoreMetadata = dataStore.getMetaData();
		IMetaData dataSetMetadata = dataset.getMetadata();
		MetaData newDataStoreMetadata = new MetaData();
		int fieldCount = dataStoreMetadata.getFieldCount();
		for (int i = 0; i < fieldCount; i++) {
			IFieldMetaData dataStoreFieldMetadata = dataStoreMetadata.getFieldMeta(i);
			String fieldName = dataStoreFieldMetadata.getName();
			int index = dataSetMetadata.getFieldIndex(fieldName);
			IFieldMetaData newFieldMetadata = null;
			if (index >= 0) {
				newFieldMetadata = new FieldMetadata();
				IFieldMetaData dataSetFieldMetadata = dataSetMetadata.getFieldMeta(index);
				String decimalPrecision = (String) dataSetFieldMetadata.getProperty(IFieldMetaData.DECIMALPRECISION);
				if (decimalPrecision != null) {
					newFieldMetadata.setProperty(IFieldMetaData.DECIMALPRECISION, decimalPrecision);
				}
				if (fieldOptions != null) {
					addMeasuresScaleFactor(fieldOptions, dataSetFieldMetadata.getName(), newFieldMetadata);
				}
				newFieldMetadata.setAlias(dataSetFieldMetadata.getAlias());
				newFieldMetadata.setFieldType(dataSetFieldMetadata.getFieldType());
				newFieldMetadata.setName(dataSetFieldMetadata.getName());
				newFieldMetadata.setType(dataStoreFieldMetadata.getType());
			} else {
				newFieldMetadata = dataStoreFieldMetadata;
			}
			newDataStoreMetadata.addFiedMeta(newFieldMetadata);
		}
		newDataStoreMetadata.setProperties(dataStoreMetadata.getProperties());
		dataStore.setMetaData(newDataStoreMetadata);
	}

	public static final String ADDITIONAL_DATA_FIELDS_OPTIONS_OPTIONS = "options";
	public static final String ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR = "measureScaleFactor";

	private void addMeasuresScaleFactor(JSONArray fieldOptions, String fieldId, IFieldMetaData newFieldMetadata) {
		if (fieldOptions != null) {
			for (int i = 0; i < fieldOptions.length(); i++) {
				try {
					JSONObject afield = fieldOptions.getJSONObject(i);
					JSONObject aFieldOptions = afield.getJSONObject(ADDITIONAL_DATA_FIELDS_OPTIONS_OPTIONS);
					String afieldId = afield.getString("id");
					String scaleFactor = aFieldOptions.optString(ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR);
					if (afieldId.equals(fieldId) && scaleFactor != null) {
						newFieldMetadata.setProperty(ADDITIONAL_DATA_FIELDS_OPTIONS_SCALE_FACTOR, scaleFactor);
						return;
					}
				} catch (Exception e) {
					throw new RuntimeException("An unpredicted error occurred while adding measures scale factor", e);
				}
			}
		}
	}

	/**
	 * The association is valid if number of records froma ssociation is less than Maximum of single datasets
	 *
	 * @param dsLabel1
	 * @param dsLabel2
	 * @param field1
	 * @param field2
	 * @return
	 * @throws Exception
	 */

	public boolean checkAssociation(JSONArray arrayAss) throws Exception {
		logger.debug("IN");

		logger.debug("Check join");
		boolean toReturn = false;

		String[] synonims = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "z" };
		String where = "";

		SelectBuilder joinSqlBuilder = new SelectBuilder();
		joinSqlBuilder.column("count(*) counter");

		try {
			SQLDBCache cache = (SQLDBCache) SpagoBICacheManager.getCache();
			cache.setUserProfile(userProfile);
			IDataSource dataSource = cache.getDataSource();
			Long maxSingleCount = 0L;

			// all arrays with fields of a single association
			ArrayList<JSONArray> assFieldsJSONArray = new ArrayList<JSONArray>();
			// mapping datasetLabel to table involved
			Map<String, String> datasetsLabelsMap = new HashMap<String, String>();
			// maps each table to the synonim used in the join clause
			Map<String, String> tableSynonimMap = new HashMap<String, String>();

			logger.debug("cycle on associations");
			for (int j = 0; j < arrayAss.length(); j++) {

				logger.debug("this is an association");

				JSONObject association = (JSONObject) arrayAss.get(j);
				JSONArray fieldsAss = association.getJSONArray("fields");
				assFieldsJSONArray.add(fieldsAss);

				logger.debug("cycle on fields");
				// Collect all tables involved
				for (int z = 0; z < fieldsAss.length(); z++) {
					JSONObject field = (JSONObject) fieldsAss.get(z);
					String dsLabel = field.getString("store");

					if (!datasetsLabelsMap.keySet().contains(dsLabel)) {

						logger.debug("Dataset with label " + dsLabel);
						IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetByLabel(dsLabel);
						// Datasets related to documents are not on DB so
						// 'dataset' can be null
						if (dataset == null) {
							// assFieldsJSONArray.get(assFieldsJSONArray.size()-1).remove(z);
							fieldsAss.remove(z);
							continue;
						}
						// checkQbeDataset(dataset);

						// check datasets are cached otherwise cache it
						// IDataStore cachedResultSet = cache.get(dataset);
						// if (cachedResultSet == null) {
						if (!cache.getMetadata().containsCacheItem(dataset.getSignature())) {
							logger.error("dataset " + dataset.getLabel() + " is not already cached, cache it");
							// IDataStore dataStore = dataStore =
							// cache.refresh(dataset, false);
							cache.refresh(dataset, true);
						}

						String table = cache.getMetadata().getCacheItem(dataset.getSignature()).getTable();
						logger.debug("Table " + table);
						// dataset to table mapping
						datasetsLabelsMap.put(dsLabel, table);

						logger.debug("Execute dataset to count records and keep track of max");

						// count single value
						SelectBuilder sqlBuilder = new SelectBuilder();
						sqlBuilder = new SelectBuilder();
						sqlBuilder.column("count(*) counter");
						sqlBuilder.from(table + " a");
						String queryText1 = sqlBuilder.toString();
						logger.debug("execute " + queryText1);
						IDataStore dataStore = dataSource.executeStatement(queryText1, 0, 0);
						Object countO = ((DataStore) dataStore).getRecordAt(0).getFieldAt(0).getValue();
						Long count1 = (countO instanceof Long) ? (Long) countO : Long.valueOf(((Number) countO).longValue());

						logger.debug("On query on table " + table + " counted " + count1 + " records");

						if (count1 > maxSingleCount) {
							maxSingleCount = count1;
						}

					}

				}

				logger.debug("Maximum among tables count is " + maxSingleCount);

			}

			// Build join query

			logger.debug("Write from and join clauses for join query");

			int index = 0;
			for (Iterator iterator = datasetsLabelsMap.keySet().iterator(); iterator.hasNext();) {
				String dsLabel = (String) iterator.next();
				String table = datasetsLabelsMap.get(dsLabel);
				joinSqlBuilder.from(table + " " + synonims[index]);
				tableSynonimMap.put(table, synonims[index]);

				index++;
			}

			// "fields":[{"store":"ds__4221948","column":"Citta"},{"store":"ALTRO_USER","column":"Regione"},{"store":"ds__3714475","column":"Citta"}]}]"
			logger.debug("Build and write where clauses for join query");

			for (int j = 0; j < assFieldsJSONArray.size(); j++) {
				JSONArray fieldsJSOONArray = assFieldsJSONArray.get(j);
				for (int i = 0; i < fieldsJSOONArray.length(); i++) {
					JSONObject field = fieldsJSOONArray.getJSONObject(i);
					String dsLabel = field.getString("store");
					String column = field.getString("column");
					String table = datasetsLabelsMap.get(dsLabel);
					String synonim = tableSynonimMap.get(table);

					if (i > 0 && table != null) {
						if (!where.isEmpty()) {
							where += " AND ";
						}
						JSONObject previousField = fieldsJSOONArray.getJSONObject(i - 1);
						String previousDsLabel = previousField.getString("store");
						String previousColumn = previousField.getString("column");
						String previousTable = datasetsLabelsMap.get(previousDsLabel);
						String previousSynonim = tableSynonimMap.get(previousTable);

						// add where conditions
						where += synonim + "." + AbstractJDBCDataset.encapsulateColumnName(column, dataSource) + "=" + previousSynonim + "."
								+ AbstractJDBCDataset.encapsulateColumnName(previousColumn, dataSource);
					}
				}
			}
			// checking if there is a where condition
			if (where == null || "".equals(where)) {
				// no one association to check
				toReturn = true;
			} else {
				logger.debug("Join where condition is " + where);
				joinSqlBuilder.where(where);

				String joinQueryText = joinSqlBuilder.toString();
				logger.debug("Join query is equal to [" + joinQueryText + "]");
				IDataStore joinDataStore = dataSource.executeStatement(joinQueryText, 0, 0);
				Object joinCountO = ((DataStore) joinDataStore).getRecordAt(0).getFieldAt(0).getValue();
				Long joinCount = (joinCountO instanceof Long) ? (Long) joinCountO : Long.valueOf(((Number) joinCountO).longValue());

				if (joinCount > maxSingleCount) {
					logger.warn("Chosen join among tables return too many rows");
					toReturn = false;
				} else {
					logger.debug("Chosen join among tables is valid");
					toReturn = true;
				}
			}
		} catch (Exception e) {
			logger.error("Error while checking the join among tables return too many rows", e);
			throw new Exception("Error while checking the join among tables", e);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	/*
	 * Create indexes for the specified dataset and the specified columns
	 */
	public void createIndexes(String label, Set<String> columns) {
		logger.debug("IN - Dataset label " + label);
		SQLDBCache cache = (SQLDBCache) SpagoBICacheManager.getCache();
		cache.setUserProfile(userProfile);
		IDataSet dataSet = this.getDataSetDAO().loadDataSetByLabel(label);
		String signature = dataSet.getSignature();
		logger.debug("Retrieve table name for signature " + signature);
		CacheItem cacheItem = cache.getMetadata().getCacheItem(signature);

		if (cacheItem != null) {
			String tableName = cacheItem.getTable();
			Connection conn = null;
			Statement stmt = null;
			for (String column : columns) {
				try {
					String query = buildIndexStatement(tableName, column);
					if (query != null) {
						conn = cache.getDataSource().getConnection();
						stmt = conn.createStatement();
						stmt.executeUpdate(query);
					} else {
						logger.debug("Impossible to build the index statement and thus creating the index. Tablename and/or column are null or empty.");
					}
				} catch (ClassNotFoundException | NamingException | SQLException e) {
					logger.debug("Impossible to build index for table [" + tableName + "] and column [" + column + "]", e);
				} finally {
					if (stmt != null) {
						try {
							stmt.close();
						} catch (SQLException e) {
							logger.debug(e);
						}
					}
					if (conn != null) {
						try {
							conn.close();
						} catch (SQLException e) {
							logger.debug(e);
						}
					}
				}
			}
		} else {
			if (signature != null && !signature.isEmpty()) {
				logger.error("Table name could not be found for signature [" + signature + "] and hash [" + Helper.sha256(signature) + "]");
			} else {
				logger.error("Table name could not be found for signature [" + signature + "]");

			}
		}
		logger.debug("OUT");
	}

	private String buildIndexStatement(String tableName, String column) {
		logger.debug("IN - Table [" + tableName + "], Column [" + column + "]");
		String statement = null;
		if (tableName != null && !tableName.isEmpty() && column != null && !column.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("CREATE INDEX");
			sb.append(" ");
			sb.append(tableName.toUpperCase());
			sb.append(column.toUpperCase());
			sb.append(" ");
			sb.append("ON");
			sb.append(" ");
			sb.append(tableName);
			sb.append("(");
			sb.append(column);
			sb.append(")");
			statement = sb.toString();
		}
		return statement;
	}

	private IDataStore queryPersistedDataset(List<GroupCriteria> groups, List<FilterCriteria> filters, List<ProjectionCriteria> projections,
			List<ProjectionCriteria> summaryRowProjections, IDataSet dataSet, int offset, int fetchSize) throws InstantiationException, IllegalAccessException {
		IDataSource dataSource = dataSet.getDataSourceForWriting();
		String tableName = dataSet.getPersistTableName();
		return queryDataset(new SelectBuilder(), dataSource, tableName, groups, filters, projections, summaryRowProjections, dataSet, offset, fetchSize);
	}

	private IDataStore queryFlatDataset(List<GroupCriteria> groups, List<FilterCriteria> filters, List<ProjectionCriteria> projections,
			List<ProjectionCriteria> summaryRowProjections, IDataSet dataSet, int offset, int fetchSize) throws InstantiationException, IllegalAccessException {
		IDataSource dataSource = dataSet.getDataSource();
		String tableName = dataSet.getFlatTableName();
		return queryDataset(new SelectBuilder(), dataSource, tableName, groups, filters, projections, summaryRowProjections, dataSet, offset, fetchSize);
	}

	private IDataStore queryJDBCDataset(List<GroupCriteria> groups, List<FilterCriteria> filters, List<ProjectionCriteria> projections,
			List<ProjectionCriteria> summaryRowProjections, IDataSet dataSet, int offset, int fetchSize) throws JSONException, Exception,
			IllegalAccessException {
		IDataSource dataSource = dataSet.getDataSource();
		QuerableBehaviour querableBehaviour = (QuerableBehaviour) dataSet.getBehaviour(QuerableBehaviour.class.getName());
		String tableName = querableBehaviour.getStatement();
		InLineViewBuilder sqlBuilder = new InLineViewBuilder();
		sqlBuilder.setInLineViewAlias("T");
		return queryDataset(sqlBuilder, dataSource, tableName, groups, filters, projections, summaryRowProjections, dataSet, offset, fetchSize);
	}

	private IDataStore queryRealtimeDataset(List<GroupCriteria> groups, List<FilterCriteria> filters, List<ProjectionCriteria> projections,
			List<ProjectionCriteria> summaryRowProjections, IDataSet dataSet, int offset, int fetchSize) {
		dataSet.loadData();
		IDataStore dataStore = dataSet.getDataStore();
		if (dataStore != null && dataStore.getRecordsCount() < METAMODEL_LIMIT) {
			String tableName = DataStore.DEFAULT_SCHEMA_NAME + "." + DataStore.DEFAULT_TABLE_NAME;
			Map<String, String> datasetAlias = getDatasetAlias(dataSet);
			List<String> orderColumns = new ArrayList<String>();

			String originalQuery = getQueryText(null, tableName, groups, filters, projections, null, dataSet, true, datasetAlias, orderColumns);
			boolean hasSortingOnCalculatedColumns = checkSortingOnCalculatedColumns(orderColumns);

			IDataStore originalDataStore = dataStore.aggregateAndFilterRecords(originalQuery, -1, -1);
			if (hasSortingOnCalculatedColumns) {
				appendCalculatedColumnsToDataStore(projections, datasetAlias, originalDataStore);
				sortColumnsOnDataStore(orderColumns, originalDataStore);
			}

			IDataStore pagedDataStore = originalDataStore.paginateRecords(offset, fetchSize);
			if (!hasSortingOnCalculatedColumns) {
				appendCalculatedColumnsToDataStore(projections, datasetAlias, pagedDataStore);
			}

			if (summaryRowProjections != null && summaryRowProjections.size() > 0) {
				String summaryRowQuery = getQueryText(null, tableName, groups, filters, projections, summaryRowProjections, dataSet, true, datasetAlias, null);
				IDataStore summaryRowDataStore = originalDataStore.aggregateAndFilterRecords(summaryRowQuery, -1, -1);
				appendSummaryRowToPagedDataStore(projections, summaryRowProjections, pagedDataStore, summaryRowDataStore);
			}

			dataStore = pagedDataStore;
			dataStore.setCacheDate(new Date());
		} else {
			throw new SpagoBIRuntimeException("Impossible to return data: the dataStore is [null], or it returns more than [" + METAMODEL_LIMIT
					+ "] rows: it cannot be processed as realtime dataset.");
		}

		return dataStore;
	}

	private void appendCalculatedColumnsToDataStore(List<ProjectionCriteria> projections, Map<String, String> datasetAlias, IDataStore pagedDataStore) {
		if (projections != null) {
			IMetaData storeMetaData = pagedDataStore.getMetaData();

			Set<String> notCalculatedColumns = new HashSet<String>();
			Map<String, String> basicColumnMap = new HashMap<String, String>(); // aggregated basic column name -> basic column name
			Map<String, Integer> fieldIndexMap = new HashMap<String, Integer>(); // basic column name -> field index

			for (ProjectionCriteria projection : projections) {
				String columnName = projection.getColumnName();
				if (datasetAlias != null) {
					columnName = datasetAlias.get(projection.getDataset()) + " - " + columnName;
				}

				String aggregateFunction = projection.getAggregateFunction();

				if (columnName.contains(AbstractDataBase.STANDARD_ALIAS_DELIMITER)) { // this is a calculated field!
					for (String basicColumnName : getBasicColumnsFromCalculatedColumn(columnName)) {
						basicColumnMap.put(aggregateFunction + "(" + basicColumnName + ")", basicColumnName);
					}

					if (pagedDataStore.getRecordsCount() > 0) {
						// aggregated basic column name -> field index
						for (int i = 0; i < storeMetaData.getFieldCount(); i++) {
							String fieldName = storeMetaData.getFieldName(i);
							if (basicColumnMap.keySet().contains(fieldName)) {
								fieldIndexMap.put(basicColumnMap.get(fieldName), i);
								break;
							}
						}

						for (int i = 0; i < pagedDataStore.getRecordsCount(); i++) {
							IRecord record = pagedDataStore.getRecordAt(i);

							// get basic values
							boolean isNullValuePresent = false;
							Map<String, Object> basicValues = new HashMap<String, Object>();
							for (String basicColumnName : fieldIndexMap.keySet()) {
								int fieldIndex = fieldIndexMap.get(basicColumnName);
								IField field = record.getFieldAt(fieldIndex);
								Object value = field.getValue();
								isNullValuePresent = isNullValuePresent || value == null;
								basicValues.put(basicColumnName, value);
							}

							// evaluate calculated column expression
							Object calculatedValue = null;
							if (aggregateFunction.toUpperCase().contains("COUNT")) {
								long count = -1;
								for (String basicColumnName : basicValues.keySet()) {
									count = Math.max(count, (long) basicValues.get(basicColumnName));
								}
								calculatedValue = count;
							} else {
								if (!isNullValuePresent) {
									String expression = columnName.replace(AbstractDataBase.STANDARD_ALIAS_DELIMITER, "");
									for (String basicColumnName : basicValues.keySet()) {
										Object object = basicValues.get(basicColumnName);
										expression = expression.replace(basicColumnName, object.toString());
									}
									calculatedValue = groovy.util.Eval.me(expression);
								}
							}
							record.appendField(new Field(calculatedValue));
						}

						// add column metadata
						IFieldMetaData fieldMetaData = new FieldMetadata();
						if (aggregateFunction != null && !aggregateFunction.isEmpty()) {
							fieldMetaData.setName(aggregateFunction + "(" + columnName + ")");
						} else {
							fieldMetaData.setName(columnName);
						}
						String aliasName = projection.getAliasName();
						if (aliasName != null && !aliasName.isEmpty()) {
							fieldMetaData.setAlias(aliasName);
						}
						fieldMetaData.setType(java.lang.Double.class);
						fieldMetaData.setFieldType(FieldType.MEASURE);
						storeMetaData.addFiedMeta(fieldMetaData);
					}
				} else {
					if (aggregateFunction != null && !aggregateFunction.isEmpty()) {
						columnName = aggregateFunction + "(" + columnName + ")";
					}
					notCalculatedColumns.add(columnName);
				}
			}

			basicColumnMap.keySet().removeAll(notCalculatedColumns);
			if (!basicColumnMap.isEmpty()) {
				// delete values in additional columns
				for (int recordIndex = 0; recordIndex < pagedDataStore.getRecordsCount(); recordIndex++) {
					IRecord record = pagedDataStore.getRecordAt(recordIndex);
					for (int fieldIndex = storeMetaData.getFieldCount() - 1; fieldIndex >= 0; fieldIndex--) {
						String fieldName = storeMetaData.getFieldName(fieldIndex);
						if (basicColumnMap.keySet().contains(fieldName)) {
							record.removeFieldAt(fieldIndex);
						}
					}
				}

				// delete additional columns metadata
				for (int fieldIndex = storeMetaData.getFieldCount() - 1; fieldIndex >= 0; fieldIndex--) {
					IFieldMetaData fieldMetaData = storeMetaData.getFieldMeta(fieldIndex);
					if (basicColumnMap.keySet().contains(fieldMetaData.getName())) {
						storeMetaData.deleteFieldMetaDataAt(fieldIndex);
					}
				}
			}
		}
	}

	private boolean checkSortingOnCalculatedColumns(List<String> orderColumns) {
		for (String orderColumn : orderColumns) {
			if (orderColumn.contains(AbstractDataBase.STANDARD_ALIAS_DELIMITER)) {
				return true;
			}
		}
		return false;
	}

	private void sortColumnsOnDataStore(List<String> orderColumns, IDataStore dataStore) {
		for (int i = orderColumns.size() - 1; i >= 0; i--) {
			String orderColumn = orderColumns.get(i);
			String columnName = orderColumn.substring(0, orderColumn.lastIndexOf(' '));
			final boolean isAscending = orderColumn.endsWith(" ASC");

			int fieldIndex;
			IMetaData metaData = dataStore.getMetaData();
			for (fieldIndex = 0; fieldIndex < metaData.getFieldCount(); fieldIndex++) {
				if (metaData.getFieldMeta(fieldIndex).getName().equals(columnName)) {
					break;
				}
			}

			Comparator<IField> comparator = new Comparator<IField>() {
				@Override
				public int compare(IField field1, IField field2) {
					Comparable<Object> value1 = (Comparable<Object>) field1.getValue();
					Comparable<Object> value2 = (Comparable<Object>) field2.getValue();

					if (value1 == null && value2 == null) {
						return 0;
					}
					if (isAscending) {
						if (value1 == null) {
							return -1;
						} else if (value2 == null) {
							return 1;
						} else {
							return value1.compareTo(value2);
						}
					} else {
						if (value2 == null) {
							return -1;
						} else if (value1 == null) {
							return 1;
						} else {
							return value2.compareTo(value1);
						}
					}
				}
			};
			dataStore.sortRecords(fieldIndex, comparator);
		}
	}

	public void appendSummaryRowToPagedDataStore(List<ProjectionCriteria> projections, List<ProjectionCriteria> summaryRowProjections,
			IDataStore pagedDataStore, IDataStore summaryRowDataStore) {
		// calc a map for summaryRowProjections -> projections
		Map<Integer, Integer> projectionToSummaryRowProjection = new HashMap<Integer, Integer>();
		for (int i = 0; i < summaryRowProjections.size(); i++) {
			ProjectionCriteria summaryRowProjection = summaryRowProjections.get(i);
			for (int j = 0; j < projections.size(); j++) {
				ProjectionCriteria projection = projections.get(j);
				if (summaryRowProjection.getColumnName().equals(projection.getAliasName())) {
					projectionToSummaryRowProjection.put(j, i);
					break;
				}
			}
		}

		IMetaData pagedMetaData = pagedDataStore.getMetaData();
		IMetaData summaryRowMetaData = summaryRowDataStore.getMetaData();

		// append summary row
		IRecord summaryRowRecord = summaryRowDataStore.getRecordAt(0);
		Record newRecord = new Record();
		for (int projectionIndex = 0; projectionIndex < pagedMetaData.getFieldCount(); projectionIndex++) {
			Field field = new Field(null);
			if (projectionToSummaryRowProjection.containsKey(projectionIndex)) {
				Integer summaryRowIndex = projectionToSummaryRowProjection.get(projectionIndex);
				Object value = summaryRowRecord.getFieldAt(summaryRowIndex).getValue();
				field.setValue(value);
			}
			newRecord.appendField(field);
		}
		pagedDataStore.appendRecord(newRecord);

		// copy metadata from summary row
		for (Integer projectionIndex : projectionToSummaryRowProjection.keySet()) {
			Integer summaryRowIndex = projectionToSummaryRowProjection.get(projectionIndex);
			pagedMetaData.getFieldMeta(projectionIndex).setType(summaryRowMetaData.getFieldType(summaryRowIndex));
		}
	}

	private IDataStore queryDataset(SelectBuilder sqlBuilder, IDataSource dataSource, String tableName, List<GroupCriteria> groups,
			List<FilterCriteria> filters, List<ProjectionCriteria> projections, List<ProjectionCriteria> summaryRowProjections, IDataSet dataSet, int offset,
			int fetchSize) throws InstantiationException, IllegalAccessException {
		logger.debug("IN");

		Map<String, String> datasetAlias = getDatasetAlias(dataSet);

		String query = getQueryText(sqlBuilder, dataSource, tableName, groups, filters, projections, null, dataSet, false, datasetAlias, null);
		IDataStore pagedDataStore = dataSource.executeStatement(query, offset, fetchSize);

		if (summaryRowProjections != null && summaryRowProjections.size() > 0) {
			String summaryRowQuery = getQueryText(sqlBuilder.getClass().newInstance(), dataSource, tableName, groups, filters, projections,
					summaryRowProjections, dataSet, false, datasetAlias, null);
			IDataStore summaryRowDataStore = dataSource.executeStatement(summaryRowQuery, -1, -1);
			appendSummaryRowToPagedDataStore(projections, summaryRowProjections, pagedDataStore, summaryRowDataStore);
		}

		logger.debug("OUT");
		return pagedDataStore;
	}

	private Map<String, String> getDatasetAlias(IDataSet dataSet) {
		dataSet.loadData(0, 1, 1);
		IDataStore limitedDataStore = dataSet.getDataStore();
		if (limitedDataStore != null) {
			Map<String, String> datasetAlias = (Map<String, String>) limitedDataStore.getMetaData().getProperty("DATASET_ALIAS");
			return datasetAlias;
		} else {
			throw new SpagoBIRuntimeException("Impossible to retrieve datastore to get required metadata.");
		}
	}

	public String getQueryText(IDataSource dataSource, String tableName, List<GroupCriteria> groups, List<FilterCriteria> filters,
			List<ProjectionCriteria> projections, List<ProjectionCriteria> summaryRowProjections, IDataSet dataSet, boolean isRealtime,
			Map<String, String> datasetAlias, List<String> outputOrderColumns) {
		return getQueryText(new SelectBuilder(), dataSource, tableName, groups, filters, projections, summaryRowProjections, dataSet, isRealtime, datasetAlias,
				outputOrderColumns);
	}

	public String getQueryText(SelectBuilder sqlBuilder, IDataSource dataSource, String tableName, List<GroupCriteria> groups, List<FilterCriteria> filters,
			List<ProjectionCriteria> projections, List<ProjectionCriteria> summaryRowProjections, IDataSet dataSet, boolean isRealtime,
			Map<String, String> datasetAlias, List<String> outputOrderColumns) {

		if (tableName == null || tableName.isEmpty() || (!isRealtime && dataSource == null)) {
			throw new IllegalArgumentException("Found one or more arguments invalid. Tablename [" + tableName + "] and/or dataSource [" + dataSource
					+ "] are null or empty.");
		}

		String label = dataSet.getLabel();
		logger.debug("Build query for persisted dataset [" + label + "] with table name [" + tableName + "]");
		logger.debug("Loading data from [" + label + "] to gather its metadata...");

		List<String> orderColumns = new ArrayList<String>();
		String queryText = null;
		if (summaryRowProjections == null || summaryRowProjections.size() == 0 || !isRealtime) {
			sqlBuilder.setWhereOrEnabled(isRealtime);
			sqlBuilder.from(tableName);

			Map<String, String> columnNameWithColonToAliasName = new HashMap<String, String>();

			setColumnsToSelect(dataSource, projections, datasetAlias, sqlBuilder, orderColumns, isRealtime, columnNameWithColonToAliasName);
			setWhereConditions(dataSource, filters, datasetAlias, sqlBuilder);
			setGroupbyConditions(dataSource, groups, datasetAlias, sqlBuilder, columnNameWithColonToAliasName);
			setOrderbyConditions(dataSource, orderColumns, sqlBuilder);

			queryText = sqlBuilder.toString();
		}

		if (summaryRowProjections != null && summaryRowProjections.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			String comma = "";
			for (int i = 0; i < projections.size(); i++) {
				ProjectionCriteria projection = projections.get(i);
				String alias = projection.getAliasName();
				String aggregateFunction = null;
				for (ProjectionCriteria summaryRowProjection : summaryRowProjections) {
					String columnName = summaryRowProjection.getColumnName();
					if (columnName.equals(alias)) {
						aggregateFunction = summaryRowProjection.getAggregateFunction();
						break;
					}
				}
				if (aggregateFunction != null) {
					sb.append(comma);
					comma = ",";
					sb.append(aggregateFunction);
					sb.append("(");
					sb.append(alias);
					sb.append(")");
				}
			}
			sb.append(" FROM ");
			if (isRealtime) {
				sb.append(tableName);
			} else {
				sb.append("(");
				sb.append(queryText);
				sb.append(") AS T");
			}

			queryText = sb.toString();
		}
		logger.debug("Persisted dataset access query is equal to [" + queryText + "]");

		if (outputOrderColumns != null) {
			outputOrderColumns.clear();
			outputOrderColumns.addAll(orderColumns);
		}

		return queryText;
	}

	private void setColumnsToSelect(IDataSource dataSource, List<ProjectionCriteria> projections, Map<String, String> datasetAlias, SelectBuilder sqlBuilder,
			List<String> orderColumns, boolean isRealtime, Map<String, String> columnNameWithColonToAliasName) {
		if (orderColumns == null) {
			throw new SpagoBIRuntimeException("Unable to manage ORDER BY clauses");
		}

		ArrayList<String> arrayCategoriesForOrdering = new ArrayList<String>();
		String keepCategoryForOrdering = "";
		boolean columnAndCategoryAreTheSame = false;
		boolean isOrderColumnPresent = false;

		if (projections != null) {
			Set<String> notCalculatedColumns = new HashSet<String>();
			Set<String> aggregatedBasicColumns = new HashSet<String>();

			for (ProjectionCriteria projection : projections) {
				String columnName = projection.getColumnName();
				String aggregateFunction = projection.getAggregateFunction();
				IAggregationFunction aggregationFunction = AggregationFunctions.get(aggregateFunction);
				String aliasName = projection.getAliasName();
				boolean hasAlias = aliasName != null && !aliasName.isEmpty();
				String orderType = projection.getOrderType().toUpperCase();

				if (columnName.contains(":")) {
					if (hasAlias) {
						columnNameWithColonToAliasName.put(columnName, aliasName);
						columnName = aliasName;
					} else {
						throw new SpagoBIRuntimeException("Projection [" + columnName + "] requires an alias");
					}
				}

				if (datasetAlias != null) {
					columnName = datasetAlias.get(projection.getDataset()) + " - " + columnName;
				}

				if (columnName.contains(AbstractDataBase.STANDARD_ALIAS_DELIMITER)) {
					// this is a calculated field!
					if (isRealtime) {
						if (aggregationFunction == null) {
							throw new SpagoBIRuntimeException("Projection [" + columnName + "] requires an aggregation function");
						}
						Set<String> basicColumns = getBasicColumnsFromCalculatedColumn(columnName);
						for (String basicColumn : basicColumns) {
							aggregatedBasicColumns.add(aggregationFunction.apply(basicColumn));
						}
					} else {
						columnName = AbstractJDBCDataset.substituteStandardWithDatasourceDelimiter(columnName, dataSource);
					}
				} else {
					if (!columnName.equals("*")) {
						columnName = AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);
					}
				}

				aliasName = AbstractJDBCDataset.encapsulateColumnName(aliasName, dataSource);
				if (aggregateFunction != null && !aggregateFunction.isEmpty() && columnName != "*") {
					columnName = aggregationFunction.apply(columnName);
					if (hasAlias) {
						if (orderType != null && !orderType.isEmpty()) {
							orderColumns.add(columnName + " " + orderType);
						}
					} else {
						throw new SpagoBIRuntimeException("Projection [" + columnName + "] requires an alias");
					}
				} else {
					/**
					 * Handling of the ordering criteria set for the first category.
					 *
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					// If the order type is not defined for current item, consider it as it is of an empty value (empty string).
					if (orderType == null) {
						orderType = "";
					}

					String orderColumn = projection.getOrderColumn();
					/**
					 * If the order column is defined and is not an empty string the column (attribute) through which the first category should be ordered is
					 * set.
					 */
					if (orderColumn != null && !orderColumn.isEmpty()) {
						isOrderColumnPresent = true;
						orderColumn = AbstractJDBCDataset.encapsulateColumnName(orderColumn, dataSource);

						/**
						 * If the ordering column is the same as the category for which is set.
						 */
						if (orderColumn.equals(columnName)) {
							columnAndCategoryAreTheSame = true;

							if (orderType.isEmpty()) {
								arrayCategoriesForOrdering.add(columnName + " ASC");
							} else {
								arrayCategoriesForOrdering.add(columnName + " " + orderType);
							}
						} else {
							if (orderType.isEmpty()) {
								arrayCategoriesForOrdering.add(orderColumn + " ASC");
							} else {
								arrayCategoriesForOrdering.add(orderColumn + " " + orderType);
							}

							sqlBuilder.column(orderColumn);
						}
					} else {
						if (!orderType.isEmpty()) {
							orderColumns.add(columnName + " " + orderType);
						}
					}

					/**
					 * Keep the ordering for the first category so it can be appended to the end of the ORDER BY clause when it is needed.
					 */
					if (keepCategoryForOrdering.isEmpty()) {
						keepCategoryForOrdering = columnName + " ASC";
					}
				}

				if (!columnName.contains(AbstractDataBase.STANDARD_ALIAS_DELIMITER)) {
					notCalculatedColumns.add(columnName);
					if (hasAlias) {
						columnName += " AS " + aliasName;
					}
					sqlBuilder.column(columnName);
				}
			}

			if (isOrderColumnPresent) {
				/**
				 * Only in the case when the category name and the name of the column through which it should be ordered are not the same, append the part for
				 * ordering that category to the end of the ORDER BY clause.
				 *
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				if (!columnAndCategoryAreTheSame && !keepCategoryForOrdering.isEmpty()) {
					arrayCategoriesForOrdering.add(keepCategoryForOrdering);
				}

				/**
				 * Append ordering by categories (columns, attributes) at the end of the array of table columns through which the ordering of particular
				 * ordering type should be performed. This is the way in which the query is constructed inside the Chart Engine, so we will keep the same
				 * approach.
				 *
				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
				 */
				for (int i = 0; i < arrayCategoriesForOrdering.size(); i++) {
					orderColumns.add(arrayCategoriesForOrdering.get(i));
				}
			}

			if (isRealtime && orderColumns.isEmpty()) {
				orderColumns.add(keepCategoryForOrdering);
			}

			aggregatedBasicColumns.removeAll(notCalculatedColumns);

			for (String additionalColumnName : aggregatedBasicColumns) {
				sqlBuilder.column(additionalColumnName);
			}
		}
	}

	private Set<String> getBasicColumnsFromCalculatedColumn(String columnName) {
		int delimiterCount = columnName.length() - columnName.replace(AbstractDataBase.STANDARD_ALIAS_DELIMITER, "").length();
		if (delimiterCount % 2 == 1) {
			throw new SpagoBIRuntimeException("An unexpected error occured while parsing [" + columnName + "]");
		}

		Set<String> columnNames = new HashSet<String>();
		int endIndex = -2;
		int beginIndex = -1;
		while ((beginIndex = columnName.indexOf(AbstractDataBase.STANDARD_ALIAS_DELIMITER, endIndex)) > -1) {
			beginIndex++;
			endIndex = columnName.indexOf(AbstractDataBase.STANDARD_ALIAS_DELIMITER, beginIndex);
			columnNames.add(columnName.substring(beginIndex, endIndex));
			endIndex++;
		}
		return columnNames;
	}

	private void setWhereConditions(IDataSource dataSource, List<FilterCriteria> filters, Map<String, String> datasetAlias, SelectBuilder sqlBuilder) {
		if (filters != null) {
			for (FilterCriteria filter : filters) {
				String operator = filter.getOperator();

				String leftOperand = null;
				if ("IN".equalsIgnoreCase(operator)) {
					String[] columns = filter.getLeftOperand().getOperandValueAsString().split(",");
					leftOperand = "(1,";
					String separator = "";
					for (String value : columns) {
						leftOperand += separator + AbstractJDBCDataset.encapsulateColumnName(value, dataSource);
						separator = ",";
					}
					leftOperand += ")";
				} else {
					if (filter.getLeftOperand().isCostant()) {
						// why? warning!
						leftOperand = filter.getLeftOperand().getOperandValueAsString();
					} else { // it's a column
						String datasetLabel = filter.getLeftOperand().getOperandDataSet();
						leftOperand = filter.getLeftOperand().getOperandValueAsString();
						if (datasetAlias != null) {
							leftOperand = datasetAlias.get(datasetLabel) + " - " + filter.getLeftOperand().getOperandValueAsString();
						}
						leftOperand = AbstractJDBCDataset.encapsulateColumnName(leftOperand, dataSource);
					}
				}

				String rightOperand = null;
				if (filter.getRightOperand().isCostant()) {
					if (filter.getRightOperand().isMultivalue()) {
						rightOperand = "(";
						String separator = "";
						String stringDelimiter = "'";
						List<String> values = filter.getRightOperand().getOperandValueAsList();
						for (String value : values) {
							if ("IN".equalsIgnoreCase(operator)) {
								if (value.startsWith(stringDelimiter) && value.endsWith(stringDelimiter)) {
									rightOperand += separator + "(1," + value + ")";
								} else if (value.startsWith("(") && value.endsWith(")")) {
									rightOperand += separator + "(1," + value.substring(1, value.length() - 1) + ")";
								} else {
									rightOperand += separator + "(1," + stringDelimiter + value + stringDelimiter + ")";
								}
							} else {
								rightOperand += separator + stringDelimiter + value + stringDelimiter;
							}
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
				if (sqlBuilder.isWhereOrEnabled() && !rightOperand.contains(" AND ")) {
					sqlBuilder.where(leftOperand + " " + operator + " " + rightOperand);
				} else {
					sqlBuilder.where("(" + leftOperand + " " + operator + " " + rightOperand + ")");
				}
			}
		}
	}

	private void setGroupbyConditions(IDataSource dataSource, List<GroupCriteria> groups, Map<String, String> datasetAlias, SelectBuilder sqlBuilder,
			Map<String, String> columnNameWithColonToAliasName) {
		if (groups != null) {
			List<String> groupColumnNames = new ArrayList<String>();

			for (GroupCriteria group : groups) {
				String columnName = group.getColumnName();
				String aggregateFunction = group.getAggregateFunction();

				if (columnName.contains(":")) {
					columnName = columnNameWithColonToAliasName.get(columnName);
				}

				if (datasetAlias != null) {
					columnName = datasetAlias.get(group.getDataset()) + " - " + group.getColumnName();
				}

				columnName = AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);

				if ((aggregateFunction != null) && (!aggregateFunction.isEmpty()) && (columnName != "*")) {
					columnName = aggregateFunction + "(" + columnName + ")";
				}

				groupColumnNames.add(columnName);
			}

			for (String groupColumnName : groupColumnNames) {
				sqlBuilder.groupBy(groupColumnName);
			}
		}
	}

	private void setOrderbyConditions(IDataSource dataSource, List<String> orderColumns, SelectBuilder sqlBuilder) {
		// ORDER BY conditions
		// https://production.eng.it/jira/browse/KNOWAGE-149
		for (String orderColumn : orderColumns) {
			if (!orderColumn.contains(TemporaryTableManager.getAliasDelimiter(dataSource))) {
				sqlBuilder.orderBy(orderColumn);
			}
		}
	}

	private void setPagingConditions(int limit, int offset, SelectBuilder sqlBuilder) {
		sqlBuilder.setLimit(limit);
		sqlBuilder.setOffset(offset);
	}

	protected List<Integer> getCategories(IEngUserProfile profile) {

		List<Integer> categories = new ArrayList<Integer>();
		try {
			// NO CATEGORY IN THE DOMAINS
			IDomainDAO domaindao = DAOFactory.getDomainDAO();
			List<Domain> dialects = domaindao.loadListDomainsByType("CATEGORY_TYPE");
			if (dialects == null || dialects.size() == 0) {
				return null;
			}

			Collection userRoles = profile.getRoles();
			Iterator userRolesIter = userRoles.iterator();
			IRoleDAO roledao = DAOFactory.getRoleDAO();
			while (userRolesIter.hasNext()) {
				String roleName = (String) userRolesIter.next();
				Role role = roledao.loadByName(roleName);

				List<RoleMetaModelCategory> aRoleCategories = roledao.getMetaModelCategoriesForRole(role.getId());
				List<RoleMetaModelCategory> resp = new ArrayList<>();
				List<Domain> array = DAOFactory.getDomainDAO().loadListDomainsByType("CATEGORY_TYPE");
				for (RoleMetaModelCategory r : aRoleCategories) {
					for (Domain dom : array) {
						if (r.getCategoryId().equals(dom.getValueId())) {
							resp.add(r);
						}
					}
				}
				if (resp != null) {
					for (Iterator iterator = resp.iterator(); iterator.hasNext();) {
						RoleMetaModelCategory roleDataSetCategory = (RoleMetaModelCategory) iterator.next();
						categories.add(roleDataSetCategory.getCategoryId());
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error loading the data set categories visible from the roles of the user");
			throw new SpagoBIRuntimeException("Error loading the data set categories visible from the roles of the user");
		}
		return categories;
	}

	private List<IDataSet> getFilteredDatasets(List<IDataSet> unfilteredDataSets, List<Integer> categories) {
		List<IDataSet> dataSets = new ArrayList<IDataSet>();
		if (categories != null && categories.size() != 0) {
			for (IDataSet ds : unfilteredDataSets) {
				if (ds.getCategoryId() == null || categories.contains(ds.getCategoryId())) {
					dataSets.add(ds);
				}
			}
		}
		return dataSets;
	}

	private void setDataSetParameters(IDataSet dataSet, Map<String, String> parametersValues) {
		dataSet.setParamsMap(parametersValues);
		List<JSONObject> parameters = getDataSetParameters(dataSet.getLabel());
		if (parameters.size() > parametersValues.size()) {
			String parameterNotValorizedStr = getParametersNotValorized(parameters, parametersValues);
			throw new ParametersNotValorizedException("The following parameters have no value [" + parameterNotValorizedStr + "]");
		}
	}
}
