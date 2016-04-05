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
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.FilterCriteria;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.GroupCriteria;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.ProjectionCriteria;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SelectBuilder;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.work.SQLDBCacheWriteWork;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.FieldMetadata;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
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
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.threadmanager.WorkManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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

					if (typePar.startsWith("class")) {
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
		cache.refresh(dataSet, true, forceRefresh);

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

	// private void checkQbeDataset(IDataSet dataSet) {
	//
	// IDataSet ds = null;
	// if (dataSet instanceof VersionedDataSet) {
	// VersionedDataSet versionedDataSet = (VersionedDataSet) dataSet;
	// ds = versionedDataSet.getWrappedDataset();
	// } else {
	// ds = dataSet;
	// }
	//
	// if (ds instanceof QbeDataSet) {
	// SpagoBICoreDatamartRetriever retriever = new SpagoBICoreDatamartRetriever();
	// Map parameters = ds.getParamsMap();
	// if (parameters == null) {
	// parameters = new HashMap();
	// ds.setParamsMap(parameters);
	// }
	// ds.getParamsMap().put(SpagoBIConstants.DATAMART_RETRIEVER, retriever);
	// }
	// }

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

	public IDataStore getDataStore(String label, int offset, int fetchSize, int maxResults, Map<String, String> parametersValues,
			List<GroupCriteria> groupCriteria, List<FilterCriteria> filterCriteria, List<ProjectionCriteria> projectionCriteria) {

		try {
			IDataSet dataSet = this.getDataSetDAO().loadDataSetByLabel(label);
			// checkQbeDataset(dataSet);
			IDataStore dataStore = null;

			if (!dataSet.isPersisted()) {
				dataSet.setParamsMap(parametersValues);
				List<JSONObject> parameters = getDataSetParameters(label);
				if (parameters.size() > parametersValues.size()) {
					String parameterNotValorizedStr = getParametersNotValorized(parameters, parametersValues);
					throw new ParametersNotValorizedException("The following parameters have no value [" + parameterNotValorizedStr + "]");
				}

				SQLDBCache cache = (SQLDBCache) SpagoBICacheManager.getCache();
				cache.setUserProfile(userProfile);

				IDataStore cachedResultSet = cache.get(dataSet, groupCriteria, filterCriteria, projectionCriteria);

				if (cachedResultSet == null) {
					dataSet.loadData();
					IDataStore baseDataStore = dataSet.getDataStore();
					if (baseDataStore.getRecordsCount() > 10000) {
						cache.put(dataSet, baseDataStore);
						dataStore = cache.get(dataSet, groupCriteria, filterCriteria, projectionCriteria);
						if (dataStore == null) {
							throw new CacheException("An unexpected error occured while executing method");
						}
						adjustMetadata((DataStore) dataStore, dataSet, null);
						dataSet.decode(dataStore);
					} else {
						dataStore = cache.refresh(dataSet, false);
						// if result was not cached put refresh date as now
						dataStore.setCacheDate(new Date());
						dataStore = dataStore.aggregateAndFilterRecords(generateQuery(groupCriteria, filterCriteria, projectionCriteria, maxResults));
					}
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
			} else {
				dataStore = queryPersistedDataset(groupCriteria, filterCriteria, projectionCriteria, dataSet);
			}
			limitDataStoreRecords((DataStore) dataStore, maxResults);
			return dataStore;

		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	protected void limitDataStoreRecords(DataStore dataStore, int maxResults) {
		List records = dataStore.getRecords();
		int size = records.size();
		if (size > maxResults) {
			records.subList(maxResults, size).clear();
		}
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
			dataStore = cache.get(dataSet, groupCriteria, filterCriteria, projectionCriteria);

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
			List<IDataSet> dataSets = getDataSetDAO().loadEnterpriseDataSets();
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
		return getOwnedDataSet(null);
	}

	public List<IDataSet> getOwnedDataSet(String userId) {
		try {
			if (userId == null) {
				userId = this.getUserId();
			}
			List<IDataSet> dataSets = getDataSetDAO().loadDataSetsOwnedByUser(userId, true);
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
		return getSharedDataSet(null);
	}

	public List<IDataSet> getSharedDataSet(String userId) {
		try {
			if (userId == null) {
				userId = this.getUserId();
			}
			List<IDataSet> dataSets = getDataSetDAO().loadDatasetsSharedWithUser(userId, true);
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

	public List<IDataSet> getUncertifiedDataSet() {
		return getUncertifiedDataSet(null);
	}

	public List<IDataSet> getUncertifiedDataSet(String userId) {
		try {
			if (userId == null) {
				userId = this.getUserId();
			}
			List<IDataSet> dataSets = getDataSetDAO().loadDatasetOwnedAndShared(userId);
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
		return getMyDataDataSet(null);
	}

	public List<IDataSet> getMyDataDataSet(String userId) {
		try {
			if (userId == null) {
				userId = this.getUserId();
			}
			List<IDataSet> dataSets = getDataSetDAO().loadMyDataDataSets(userId);
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
						// Datasets related to documents are not on DB so 'dataset' can be null
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
							// IDataStore dataStore = dataStore = cache.refresh(dataset, false);
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
						if (!where.equals("")) {
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

	private String generateQuery(List<GroupCriteria> groups, List<FilterCriteria> filters, List<ProjectionCriteria> projections, int maxResults) {
		SelectBuilder sqlBuilder = new SelectBuilder();
		sqlBuilder.from(DataStore.DEFAULT_SCHEMA_NAME + "." + DataStore.DEFAULT_TABLE_NAME);

		// Columns to SELECT
		if (projections != null) {
			for (ProjectionCriteria projection : projections) {
				String aggregateFunction = projection.getAggregateFunction();

				String columnName = projection.getColumnName();

				if ((aggregateFunction != null) && (!aggregateFunction.isEmpty()) && (columnName != "*")) {
					String aliasName = projection.getAliasName();
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
					leftOperand = filter.getLeftOperand().getOperandValueAsString();
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
				}

				sqlBuilder.where(leftOperand + " " + operator + " " + rightOperand);
			}
		}

		// GROUP BY conditions
		if (groups != null) {
			for (GroupCriteria group : groups) {
				String aggregateFunction = group.getAggregateFunction();

				String columnName = group.getColumnName();

				if ((aggregateFunction != null) && (!aggregateFunction.isEmpty()) && (columnName != "*")) {
					columnName = aggregateFunction + "(" + columnName + ")";
				}
				sqlBuilder.groupBy(columnName);
			}
		}

		String queryText = sqlBuilder.toString() + " LIMIT " + maxResults;
		logger.debug("Cached dataset access query is equal to [" + queryText + "]");

		return queryText;
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

	private IDataStore queryPersistedDataset(List<GroupCriteria> groups, List<FilterCriteria> filters, List<ProjectionCriteria> projections, IDataSet dataSet) {

		logger.debug("IN");

		DataStore toReturn = null;
		String label = dataSet.getLabel();
		String tableName = dataSet.getPersistTableName();
		IDataSource dataSource = dataSet.getDataSourceForWriting();
		logger.debug("Loading data from [" + label + "] to gather its metadata...");
		dataSet.loadData(0, 1, 1);
		IDataStore limitedDataStore = dataSet.getDataStore();

		if (tableName != null && !tableName.isEmpty() && dataSource != null && limitedDataStore != null) {
			logger.debug("Build query for persisted dataset [" + label + "] with table name [" + tableName + "] stored in datasource [" + dataSource.getLabel()
					+ "]");

			Map<String, String> datasetAlias = (Map<String, String>) limitedDataStore.getMetaData().getProperty("DATASET_ALIAS");

			// https://production.eng.it/jira/browse/KNOWAGE-149
			// This list is used to create the order by clause
			List<String> orderColumns = new ArrayList<String>();

			SelectBuilder sqlBuilder = new SelectBuilder();
			sqlBuilder.from(tableName);

			// Columns to SELECT
			if (projections != null) {
				for (ProjectionCriteria projection : projections) {
					String aggregateFunction = projection.getAggregateFunction();

					String columnName = projection.getColumnName();
					if (datasetAlias != null) {
						columnName = datasetAlias.get(projection.getDataset()) + " - " + projection.getColumnName();
					}
					columnName = AbstractJDBCDataset.encapsulateColumnName(columnName, dataSource);

					if ((aggregateFunction != null) && (!aggregateFunction.isEmpty()) && (columnName != "*")) {
						String aliasName = projection.getAliasName();
						aliasName = AbstractJDBCDataset.encapsulateColumnName(aliasName, dataSource);
						if (aliasName != null && !aliasName.isEmpty()) {

							// https://production.eng.it/jira/browse/KNOWAGE-149
							// This variable is used for the order clause
							String tmpColumn = aggregateFunction + "(" + columnName + ") ";
							String orderType = projection.getOrderType();
							if (orderType != null && !orderType.equals("")) {
								orderColumns.add(tmpColumn + " " + orderType);
							}

							columnName = tmpColumn + " AS " + aliasName;
						}
					}
					sqlBuilder.column(columnName);

				}
			}

			// WHERE conditions
			if (filters != null) {
				for (FilterCriteria filter : filters) {
					String operator = filter.getOperator();

					String leftOperand = null;
					if (operator.equalsIgnoreCase("IN")) {
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
								if (operator.equalsIgnoreCase("IN")) {
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

					sqlBuilder.where(leftOperand + " " + operator + " " + rightOperand);
				}
			}

			// GROUP BY conditions
			if (groups != null) {
				for (GroupCriteria group : groups) {
					String aggregateFunction = group.getAggregateFunction();

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

			// ORDER BY conditions
			// https://production.eng.it/jira/browse/KNOWAGE-149
			for (String orderColumn : orderColumns) {
				sqlBuilder.orderBy(orderColumn);
			}

			String queryText = sqlBuilder.toString();
			logger.debug("Persisted dataset access query is equal to [" + queryText + "]");

			IDataStore dataStore = dataSource.executeStatement(queryText, 0, 0);
			toReturn = (DataStore) dataStore;

		} else {
			logger.debug("Impossible to build query for persisted dataset [" + label + "] with table name [" + tableName + "] stored in datasource ["
					+ dataSource.getLabel() + "]");
		}
		return toReturn;
	}
}
