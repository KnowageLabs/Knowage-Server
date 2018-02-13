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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.InflaterInputStream;

import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeInput;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import commonj.work.Work;
import commonj.work.WorkException;
import commonj.work.WorkItem;
import gnu.trove.set.hash.TLongHashSet;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.dataset.association.DistinctValuesCalculateWork;
import it.eng.spagobi.tools.dataset.association.DistinctValuesClearWork;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.cache.query.PreparedStatementData;
import it.eng.spagobi.tools.dataset.cache.query.SelectQuery;
import it.eng.spagobi.tools.dataset.cache.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Filter;
import it.eng.spagobi.tools.dataset.cache.query.item.NullaryFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.OrFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Projection;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.SimpleFilterOperator;
import it.eng.spagobi.tools.dataset.cache.query.item.SingleProjectionSimpleFilter;
import it.eng.spagobi.tools.dataset.cache.query.item.Sorting;
import it.eng.spagobi.tools.dataset.cache.query.item.UnaryFilter;
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
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.crosstab.CrosstabDefinition;
import it.eng.spagobi.tools.dataset.crosstab.Measure;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.database.AbstractDataBase;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.groovy.GroovySandbox;
import it.eng.spagobi.utilities.threadmanager.WorkManager;
import it.eng.spagobi.utilities.trove.TLongHashSetSerializer;

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
	public static final String MULTIVALUE = "MULTIVALUE";

	private static final int METAMODEL_LIMIT = 5000;

	// Cockpit filters
	public static final String MAX_FILTER = "MAX()";
	public static final String MIN_FILTER = "MIN()";

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
				Integer dsCategoryId = dataSet.getCategoryId();
				// check categories of dataset
				Set<Domain> categoryList = UserUtilities.getDataSetCategoriesByUser(getUserProfile());
				if (categoryList != null && categoryList.size() > 0) {

					SbiDomains[] categoryArray = new SbiDomains[categoryList.size()];
					int i = 0;
					for (Iterator iterator = categoryList.iterator(); iterator.hasNext();) {
						Domain domain = (Domain) iterator.next();
						Integer domainId = domain.getValueId();
						if (dsCategoryId.equals(domainId))
							return dataSet;
					}
				}
				// just if dataset hasn't a category available for the user gives an error
				throw new RuntimeException("User [" + getUserProfile().getUserId() + "] cannot access to dataset [" + label + "]");
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

			List<IFieldMetaData> fieldsMetaData = new ArrayList<>();
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
			List<JSONObject> parametersList = new ArrayList<>();
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
					boolean multiValue = sbRow.getAttribute(MULTIVALUE) != null ? Boolean.valueOf(sbRow.getAttribute(MULTIVALUE).toString()) : false;

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
					paramMetaDataJSON.put("multiValuePar", multiValue);
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

	public IDataStore getDataStore(IDataSet dataSet, boolean isNearRealtime, Map<String, String> parametersValues, List<Projection> projections, Filter filter,
			List<Projection> groups, List<Sorting> sortings, List<Projection> summaryRowProjections, int offset, int fetchSize, int maxRowCount) {
		String errorMessage = "An unexpected error occured while executing method";

		Monitor totalTiming = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore");
		try {
			setDataSetParameters(dataSet, parametersValues);

			// force resolution of parameters
			QuerableBehaviour querableBehaviour = (QuerableBehaviour) dataSet.getBehaviour(QuerableBehaviour.class.getName());
			if (querableBehaviour != null) {
				querableBehaviour.getStatement();
			}

			IDataStore dataStore = null;

			DatasetEvaluationStrategy evaluationStrategy = dataSet.getEvaluationStrategy(isNearRealtime);
			if (DatasetEvaluationStrategy.PERSISTED.equals(evaluationStrategy)) {
				logger.debug("Querying persisted dataset");
				dataStore = queryPersistedDataset(dataSet, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
				dataStore.setCacheDate(getPersistedDate(dataSet));
			} else if (DatasetEvaluationStrategy.FLAT.equals(evaluationStrategy)) {
				logger.debug("Querying flat dataset");
				dataStore = queryFlatDataset(dataSet, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
				dataStore.setCacheDate(new Date());
			} else {
				if (DatasetEvaluationStrategy.INLINE_VIEW.equals(evaluationStrategy)) {
					logger.debug("Querying near realtime/JDBC dataset");
					Monitor timing = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore:inLineView");
					dataStore = queryJDBCDataset(dataSet, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
					timing.stop();
					dataStore.setCacheDate(new Date());
				} else if (DatasetEvaluationStrategy.NEAR_REALTIME.equals(evaluationStrategy)
						|| DatasetEvaluationStrategy.REALTIME.equals(evaluationStrategy)) {
					logger.debug("Querying near realtime dataset");
					dataStore = queryNearRealtimeDataset(dataSet, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
					dataStore.setCacheDate(new Date());
				} else {
					logger.debug("Querying dataset in cache");
					SQLDBCache cache = (SQLDBCache) SpagoBICacheManager.getCache();
					cache.setUserProfile(userProfile);

					Monitor totalCacheTiming = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore:totalCache");

					IDataStore cachedResultSet = cache.get(dataSet, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize,
							maxRowCount);
					if (cachedResultSet == null) {
						logger.debug("Dataset not in cache");

						Monitor timing = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore:putInCache");
						putDataSetInCache(dataSet, cache);
						timing.stop();

						if (dataSet.getDataStore() != null && dataSet.getDataStore().getMetaData().getFieldCount() == 0) {
							// update only datasource's metadata from dataset if for some strange cause it hasn't got fields
							logger.debug("Update datastore's metadata with dataset's metadata for nodata found...");
							dataStore = new DataStore();
							IMetaData metadata = dataSet.getMetadata();
							metadata.setProperty("resultNumber", 0);
							dataStore.setMetaData(metadata);
							adjustMetadata((DataStore) dataStore, dataSet, null);
							return dataStore;
						}

						timing = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore:getFromCache");
						dataStore = cache.get(dataSet, projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
						timing.stop();
						if (dataStore == null) {
							throw new CacheException(errorMessage);
						}
						adjustMetadata((DataStore) dataStore, dataSet, null);
						dataSet.decode(dataStore);

						// if result was not cached put refresh date as now
						dataStore.setCacheDate(new Date());
					} else {
						dataStore = cachedResultSet;
						adjustMetadata((DataStore) dataStore, dataSet, null);
						dataSet.decode(dataStore);
					}
					totalCacheTiming.stop();
				}
			}

			return dataStore;

		} catch (Throwable t) {
			logger.error(errorMessage, t);
			throw new RuntimeException(errorMessage, t);
		} finally {
			totalTiming.stop();
			logger.debug("OUT");
		}
	}

	public void putDataSetInCache(IDataSet dataSet, ICache cache) {
		if (dataSet.isCachingSupported()) {
			if (dataSet instanceof AbstractJDBCDataset && !dataSet.hasDataStoreTransformer()) {
				logger.debug("Copying JDBC dataset in cache using its iterator");
				cache.put(dataSet);
			} else {
				logger.debug("Copying dataset in cache by loading the whole set of data in memory");
				dataSet.loadData();
				if (dataSet.getDataStore().getMetaData().getFieldCount() > 0) {
					cache.put(dataSet, dataSet.getDataStore());
				}
			}
		}
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

			List<Projection> projections = this.getProjections(dataSet, crosstabDefinition);
			List<Projection> groups = this.getGroups(dataSet, crosstabDefinition);
			dataStore = cache.get(dataSet, projections, null, groups, null, null, 0, 0, -1);

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

	// ------------------------------------------------------------------------------
	// Methods for extracting information from CrosstabDefinition and related
	// ------------------------------------------------------------------------------

	/**
	 * @deprecated
	 */
	@Deprecated
	private List<Projection> getProjections(IDataSet dataSet, CrosstabDefinition crosstabDefinition) {
		logger.debug("IN");

		List<Projection> projections = new ArrayList<>();

		List<CrosstabDefinition.Row> rows = crosstabDefinition.getRows();
		List<CrosstabDefinition.Column> colums = crosstabDefinition.getColumns();
		List<Measure> measures = crosstabDefinition.getMeasures();

		// appends columns
		Iterator<CrosstabDefinition.Column> columsIt = colums.iterator();
		while (columsIt.hasNext()) {
			CrosstabDefinition.Column aColumn = columsIt.next();
			String columnName = aColumn.getEntityId();
			projections.add(new Projection(dataSet, columnName));
		}
		// appends rows
		Iterator<CrosstabDefinition.Row> rowsIt = rows.iterator();
		while (rowsIt.hasNext()) {
			CrosstabDefinition.Row aRow = rowsIt.next();
			String columnName = aRow.getEntityId();
			projections.add(new Projection(dataSet, columnName));
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
					projections.add(new Projection(function, dataSet, columnName));
				} else {
					projections.add(new Projection(dataSet, columnName));
				}
			}
		}

		logger.debug("OUT");
		return projections;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	private List<Projection> getGroups(IDataSet dataSet, CrosstabDefinition crosstabDefinition) {
		logger.debug("IN");
		List<Projection> groups = new ArrayList<>();

		List<CrosstabDefinition.Row> rows = crosstabDefinition.getRows();
		List<CrosstabDefinition.Column> colums = crosstabDefinition.getColumns();

		// appends columns
		Iterator<CrosstabDefinition.Column> columsIt = colums.iterator();
		while (columsIt.hasNext()) {
			CrosstabDefinition.Column aColumn = columsIt.next();
			String columnName = aColumn.getEntityId();
			groups.add(new Projection(dataSet, columnName));
		}

		// appends rows
		Iterator<CrosstabDefinition.Row> rowsIt = rows.iterator();
		while (rowsIt.hasNext()) {
			CrosstabDefinition.Row aRow = rowsIt.next();
			String columnName = aRow.getEntityId();
			groups.add(new Projection(dataSet, columnName));
		}
		logger.debug("OUT");
		return groups;
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
				newFieldMetadata.setAlias(dataStoreFieldMetadata.getAlias());
				newFieldMetadata.setFieldType(dataSetFieldMetadata.getFieldType());
				newFieldMetadata.setName(dataStoreFieldMetadata.getName());
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

			try {
				String query = buildIndexStatement(tableName, columns);
				if (query != null) {
					conn = cache.getDataSource().getConnection();
					stmt = conn.createStatement();
					stmt.executeUpdate(query);
				} else {
					logger.debug("Impossible to build the index statement and thus creating the index. Tablename and/or column are null or empty.");
				}
			} catch (ClassNotFoundException | NamingException | SQLException e) {
				logger.debug("Impossible to build index for table [" + tableName + "] and columns [" + columns + "]", e);
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

		} else {
			if (signature != null && !signature.isEmpty()) {
				logger.error("Table name could not be found for signature [" + signature + "] and hash [" + Helper.sha256(signature) + "]");
			} else {
				logger.error("Table name could not be found for signature [" + signature + "]");

			}
		}
		logger.debug("OUT");
	}

	private String buildIndexStatement(String tableName, Set<String> columns) {
		logger.debug("IN - Table [" + tableName + "], Column [" + columns + "]");

		String statement = null;
		if (tableName != null && !tableName.isEmpty() && columns != null && !columns.isEmpty()) {

			StringBuilder columnsSTring = new StringBuilder();
			for (Iterator iterator = columns.iterator(); iterator.hasNext();) {
				String column = (String) iterator.next();
				columnsSTring = columnsSTring.append(column);
				columnsSTring = columnsSTring.append(",");
			}
			if (columnsSTring.length() > 2) {
				columnsSTring.setLength(columnsSTring.length() - 1);

				StringBuilder sb = new StringBuilder();
				sb.append("CREATE INDEX");
				sb.append(" ");
				sb.append("fed");
				sb.append(columns.hashCode());
				sb.append(" ");
				sb.append("ON");
				sb.append(" ");
				sb.append(tableName);
				sb.append("(");
				sb.append(columnsSTring);
				sb.append(")");
				statement = sb.toString();
			}
		}
		return statement;
	}

	private IDataStore queryPersistedDataset(IDataSet dataSet, List<Projection> projections, Filter filter, List<Projection> groups, List<Sorting> sortings,
			List<Projection> summaryRowProjections, int offset, int fetchSize, int maxRowCount) {
		IDataSource dataSource = dataSet.getDataSourceForWriting();
		String tableName = dataSet.getPersistTableName();
		return queryDataset(dataSet, dataSource, projections, tableName, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
	}

	private IDataStore queryFlatDataset(IDataSet dataSet, List<Projection> projections, Filter filter, List<Projection> groups, List<Sorting> sortings,
			List<Projection> summaryRowProjections, int offset, int fetchSize, int maxRowCount) {
		IDataSource dataSource = dataSet.getDataSource();
		String tableName = dataSet.getFlatTableName();
		return queryDataset(dataSet, dataSource, projections, tableName, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
	}

	private IDataStore queryJDBCDataset(IDataSet dataSet, List<Projection> projections, Filter filter, List<Projection> groups, List<Sorting> sortings,
			List<Projection> summaryRowProjections, int offset, int fetchSize, int maxRowCount) {
		IDataSource dataSource = dataSet.getDataSource();
		QuerableBehaviour querableBehaviour = (QuerableBehaviour) dataSet.getBehaviour(QuerableBehaviour.class.getName());
		String tableName = "(" + querableBehaviour.getStatement() + ") T";
		return queryDataset(dataSet, dataSource, projections, tableName, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount);
	}

	private IDataStore queryNearRealtimeDataset(IDataSet dataSet, List<Projection> projections, Filter filter, List<Projection> groups, List<Sorting> sortings,
			List<Projection> summaryRowProjections, int offset, int fetchSize, int maxRowCount) {
		dataSet.loadData();
		IDataStore dataStore = dataSet.getDataStore();
		if (dataStore != null && dataStore.getRecordsCount() < METAMODEL_LIMIT) {
			String tableName = DataStore.DEFAULT_SCHEMA_NAME + "." + DataStore.DEFAULT_TABLE_NAME;
			List<String> orderColumns = new ArrayList<>();

			PreparedStatementData data = new SelectQuery(dataSet).selectDistinct().select(projections).from(tableName).where(filter).groupBy(groups)
					.orderBy(sortings).getPreparedStatementData(null);
			boolean hasSortingOnCalculatedColumns = checkSortingOnCalculatedColumns(orderColumns);

			IDataStore originalDataStore = dataStore.aggregateAndFilterRecords(data.getQuery(), data.getValues(), -1, -1, maxRowCount);
			if (hasSortingOnCalculatedColumns) {
				appendCalculatedColumnsToDataStore(projections, originalDataStore);
				sortColumnsOnDataStore(orderColumns, originalDataStore);
			}

			IDataStore pagedDataStore = originalDataStore.paginateRecords(offset, fetchSize);
			if (!hasSortingOnCalculatedColumns) {
				appendCalculatedColumnsToDataStore(projections, pagedDataStore);
			}

			if (summaryRowProjections != null && !summaryRowProjections.isEmpty()) {
				PreparedStatementData summaryRowData = new SelectQuery(dataSet).selectDistinct().select(summaryRowProjections).from(tableName).where(filter)
						.orderBy(sortings).getPreparedStatementData(null);
				IDataStore summaryRowDataStore = originalDataStore.aggregateAndFilterRecords(summaryRowData.getQuery(), summaryRowData.getValues(), -1, -1,
						maxRowCount);
				appendSummaryRowToPagedDataStore(projections, summaryRowProjections, pagedDataStore, summaryRowDataStore);
			}

			dataStore = pagedDataStore;
			dataStore.setCacheDate(new Date());
		} else {
			throw new SpagoBIRuntimeException("Impossible to return data: the dataStore is [null], or it returns more than [" + METAMODEL_LIMIT
					+ "] rows: it cannot be processed as near realtime dataset.");
		}

		return dataStore;
	}

	private void appendCalculatedColumnsToDataStore(List<Projection> projections, IDataStore pagedDataStore) {
		if (projections != null) {
			IMetaData storeMetaData = pagedDataStore.getMetaData();

			Set<String> notCalculatedColumns = new HashSet<>();
			Map<String, String> basicColumnMap = new HashMap<>(); // aggregated basic column name -> basic column name
			Map<String, Integer> fieldIndexMap = new HashMap<>(); // basic column name -> field index

			for (Projection projection : projections) {
				String columnName = projection.getName();

				IAggregationFunction aggregationFunction = projection.getAggregationFunction();

				if (columnName.contains(AbstractDataBase.STANDARD_ALIAS_DELIMITER)) { // this is a calculated field!
					for (String basicColumnName : getBasicColumnsFromCalculatedColumn(columnName)) {
						basicColumnMap.put(aggregationFunction.apply(basicColumnName), basicColumnName);
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
							Map<String, Object> basicValues = new HashMap<>();
							for (String basicColumnName : fieldIndexMap.keySet()) {
								int fieldIndex = fieldIndexMap.get(basicColumnName);
								IField field = record.getFieldAt(fieldIndex);
								Object value = field.getValue();
								isNullValuePresent = isNullValuePresent || value == null;
								basicValues.put(basicColumnName, value);
							}

							// evaluate calculated column expression
							Object calculatedValue = null;
							if (aggregationFunction.equals(AggregationFunctions.COUNT_FUNCTION)
									|| aggregationFunction.equals(AggregationFunctions.COUNT_DISTINCT_FUNCTION)) {
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
									calculatedValue = new GroovySandbox().evaluate(expression);
								}
							}
							record.appendField(new Field(calculatedValue));
						}

						// add column metadata
						IFieldMetaData fieldMetaData = new FieldMetadata();
						if (aggregationFunction != null && !aggregationFunction.equals(AggregationFunctions.NONE_FUNCTION)) {
							fieldMetaData.setName(aggregationFunction.apply(columnName));
						} else {
							fieldMetaData.setName(columnName);
						}
						String aliasName = projection.getAlias();
						if (aliasName != null && !aliasName.isEmpty()) {
							fieldMetaData.setAlias(aliasName);
						}
						fieldMetaData.setType(java.lang.Double.class);
						fieldMetaData.setFieldType(FieldType.MEASURE);
						storeMetaData.addFiedMeta(fieldMetaData);
					}
				} else {
					if (aggregationFunction != null && !aggregationFunction.equals(AggregationFunctions.NONE_FUNCTION)) {
						columnName = aggregationFunction.apply(columnName);
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

	public void appendSummaryRowToPagedDataStore(List<Projection> projections, List<Projection> summaryRowProjections, IDataStore pagedDataStore,
			IDataStore summaryRowDataStore) {
		// calc a map for summaryRowProjections -> projections
		Map<Integer, Integer> projectionToSummaryRowProjection = new HashMap<>();
		for (int i = 0; i < summaryRowProjections.size(); i++) {
			Projection summaryRowProjection = summaryRowProjections.get(i);
			for (int j = 0; j < projections.size(); j++) {
				Projection projection = projections.get(j);
				String projectionAlias = projection.getAlias();
				if (summaryRowProjection.getAlias().equals(projectionAlias) || summaryRowProjection.getName().equals(projectionAlias)) {
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

	private IDataStore queryDataset(IDataSet dataSet, IDataSource dataSource, List<Projection> projections, String tableName, Filter filter,
			List<Projection> groups, List<Sorting> sortings, List<Projection> summaryRowProjections, int offset, int fetchSize, int maxRowCount) {
		logger.debug("IN");

		SelectQuery selectQuery = new SelectQuery(dataSet).selectDistinct().select(projections).from(tableName).where(filter).groupBy(groups).orderBy(sortings);
		IDataStore pagedDataStore = dataSource.executeStatement(selectQuery, offset, fetchSize, maxRowCount);

		if (summaryRowProjections != null && !summaryRowProjections.isEmpty()) {
			String summaryRowQuery = new SelectQuery(dataSet).selectDistinct().select(summaryRowProjections).from(tableName).where(filter).toSql(dataSource);
			IDataStore summaryRowDataStore = dataSource.executeStatement(summaryRowQuery, -1, -1, maxRowCount);
			appendSummaryRowToPagedDataStore(projections, summaryRowProjections, pagedDataStore, summaryRowDataStore);
		}

		logger.debug("OUT");
		return pagedDataStore;
	}

	private Set<String> getBasicColumnsFromCalculatedColumn(String columnName) {
		int delimiterCount = columnName.length() - columnName.replace(AbstractDataBase.STANDARD_ALIAS_DELIMITER, "").length();
		if (delimiterCount % 2 == 1) {
			throw new SpagoBIRuntimeException("An unexpected error occured while parsing [" + columnName + "]");
		}

		Set<String> columnNames = new HashSet<>();
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

	protected List<Integer> getCategories(IEngUserProfile profile) {

		List<Integer> categories = new ArrayList<>();
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

	public void setDataSetParameters(IDataSet dataSet, Map<String, String> paramValues) {
		List<JSONObject> parameters = getDataSetParameters(dataSet.getLabel());
		if (parameters.size() > paramValues.size()) {
			String parameterNotValorizedStr = getParametersNotValorized(parameters, paramValues);
			throw new ParametersNotValorizedException("The following parameters have no value [" + parameterNotValorizedStr + "]");
		}

		if (paramValues.size() > 0) {
			for (String paramName : paramValues.keySet()) {
				for (int i = 0; i < parameters.size(); i++) {
					JSONObject parameter = parameters.get(i);
					if (paramName.equals(parameter.optString("namePar"))) {
						String[] values = paramValues.get(paramName).split(",");
						boolean isMultiValue = parameter.optBoolean("multiValuePar");
						int length = isMultiValue ? values.length : 1;

						String typePar = parameter.optString("typePar");
						String delim = "string".equalsIgnoreCase(typePar) ? "'" : "";

						List<String> newValues = new ArrayList<>();
						for (int j = 0; j < length; j++) {
							String value = values[j].trim();
							if (!value.isEmpty()) {
								if (!value.startsWith(delim) && !value.endsWith(delim)) {
									newValues.add(delim + value + delim);
								} else {
									newValues.add(value);
								}
							}
						}
						paramValues.put(paramName, StringUtils.join(newValues, ","));
						break;
					}
				}
			}
			dataSet.setParamsMap(paramValues);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, TLongHashSet> readDomainValues(IDataSet dataSet, Map<String, String> parametersValues, boolean wait)
			throws NamingException, WorkException, InterruptedException {
		logger.debug("IN");
		Map<String, TLongHashSet> toReturn = new HashMap<>(0);
		setDataSetParameters(dataSet, parametersValues);
		String signature = dataSet.getSignature();
		logger.debug("Looking for domain values for dataSet with signature [" + signature + "]...");
		String hashSignature = Helper.sha256(signature);
		logger.debug("Corresponding signature hash value is [" + hashSignature + "]");
		String path = SpagoBIUtilities.getDatasetResourcePath() + File.separatorChar + DataSetConstants.DOMAIN_VALUES_FOLDER;
		logger.debug("Reading domain values from binary file located at [" + path + "]");
		UnsafeInput input = null;
		try {
			Kryo kryo = new Kryo();
			kryo.register(TLongHashSet.class, new TLongHashSetSerializer());
			String filepath = path + File.separatorChar + hashSignature + DataSetConstants.DOMAIN_VALUES_EXTENSION;
			File file = new File(filepath);
			if (!file.exists()) {
				logger.debug(
						"Impossible to find a binary file named [" + hashSignature + DataSetConstants.DOMAIN_VALUES_EXTENSION + "] located at [" + path + "]");

				calculateDomainValues(dataSet, wait);
			}
			if (wait) {
				input = new UnsafeInput(new InflaterInputStream(new FileInputStream(filepath)));
				toReturn = kryo.readObject(input, toReturn.getClass());
				logger.debug("Reading domain values: DONE");
			}
		} catch (FileNotFoundException e) {
			throw new SpagoBIRuntimeException("It is likely that no domain values have been calculated for dataSet [" + dataSet.getLabel() + "]", e);
		} finally {
			if (input != null) {
				input.close();
			}
		}
		return toReturn;
	}

	public void calculateDomainValues(IDataSet dataSet) throws NamingException, WorkException, InterruptedException {
		calculateDomainValues(dataSet, false);
	}

	public void calculateDomainValues(IDataSet dataSet, boolean wait) throws NamingException, WorkException, InterruptedException {
		logger.debug("IN");
		logger.debug("Getting the JNDI Work Manager");
		WorkManager spagoBIWorkManager = new WorkManager(GeneralUtilities.getSpagoBIConfigurationProperty("JNDI_THREAD_MANAGER"));
		commonj.work.WorkManager workManager = spagoBIWorkManager.getInnerInstance();
		Work domainValuesWork = new DistinctValuesCalculateWork(dataSet, userProfile);
		logger.debug("Scheduling calculating work for dataSet with label [" + dataSet.getLabel() + "] and signature [" + dataSet.getSignature() + "] by user ["
				+ userProfile.getUserId() + "].");
		WorkItem workItem = workManager.schedule(domainValuesWork);
		if (wait) {
			List<WorkItem> workItems = new ArrayList<>(1);
			workItems.add(workItem);
			long workTimeout = Long.parseLong(SingletonConfig.getInstance().getConfigValue("SPAGOBI.WORKMANAGER.SQLDBCACHE.TIMEOUT"));
			workManager.waitForAll(workItems, workTimeout);
			logger.debug("Synchronous work has finished");
		} else {
			logger.debug("Asynchronous work has been scheduled");
		}
		logger.debug("OUT");
	}

	public void clearDomainValues(IDataSet dataSet) throws NamingException, WorkException {
		logger.debug("IN");
		logger.debug("Getting the JNDI Work Manager");
		WorkManager spagoBIWorkManager = new WorkManager(GeneralUtilities.getSpagoBIConfigurationProperty("JNDI_THREAD_MANAGER"));
		commonj.work.WorkManager workManager = spagoBIWorkManager.getInnerInstance();
		Work domainValuesWork = new DistinctValuesClearWork(dataSet, userProfile);
		logger.debug("Scheduling asynchronous deleting work for dataSet with label [" + dataSet.getLabel() + "] and signature [" + dataSet.getSignature()
				+ "] by user [" + userProfile.getUserId() + "].");
		workManager.schedule(domainValuesWork);
		logger.debug("Asynchronous work has been scheduled");
		logger.debug("OUT");
	}

	public String getQbeDataSetColumn(IDataSet dataSet, String columnName) {
		String result = columnName;

		Assert.assertNotNull(dataSet, "Impossible to load dataset with label [" + dataSet.getLabel() + "]");
		for (int i = 0; i < dataSet.getMetadata().getFieldCount(); i++) {
			IFieldMetaData fieldMeta = dataSet.getMetadata().getFieldMeta(i);
			if (fieldMeta.getName().equals(columnName)) {
				result = fieldMeta.getAlias();
				break;
			}
		}

		return result;
	}

	/**
	 * if a filter has MAX() or MIN() value convert it by calculating the right value
	 *
	 * @param label
	 * @param parameters
	 * @param selections
	 * @param likeSelections
	 * @param maxRowCount
	 * @param aggregations
	 * @param summaryRow
	 * @param offset
	 * @param fetchSize
	 * @param isNearRealtime
	 * @param groupCriteria
	 * @param filterCriteriaForMetaModel
	 * @param summaryRowProjectionCriteria
	 * @param havingCriteria
	 * @param havingCriteriaForMetaModel
	 * @param filterCriteria
	 * @param projectionCriteria
	 * @return
	 */

	// FIXME
	public List<Filter> calculateMinMaxFilters(IDataSet dataSet, boolean isNearRealtime, Map<String, String> parametersValues, List<Projection> projections,
			List<Filter> filters, List<SimpleFilter> likeFilters, List<Projection> groups) {

		logger.debug("IN");

		List<Filter> newFilters = new ArrayList<>(filters);

		List<Integer> minMaxFilterIndexes = new ArrayList<>();
		List<Projection> minMaxProjections = new ArrayList<>();

		List<Filter> noMinMaxFilters = new ArrayList<>();

		for (int i = 0; i < filters.size(); i++) {
			Filter filter = filters.get(i);
			if (filter instanceof SimpleFilter) {
				SimpleFilter simpleFilter = (SimpleFilter) filter;
				SimpleFilterOperator operator = simpleFilter.getOperator();

				if (SimpleFilterOperator.EQUALS_TO_MIN.equals(operator)) {
					logger.debug("Min filter found at index [" + i + "]");
					minMaxFilterIndexes.add(i);

					String columnName = ((SingleProjectionSimpleFilter) filter).getProjection().getName();
					Projection projection = new Projection(AggregationFunctions.MIN_FUNCTION, dataSet, columnName);
					minMaxProjections.add(projection);
				} else if (SimpleFilterOperator.EQUALS_TO_MAX.equals(operator)) {
					logger.debug("Max filter found at index [" + i + "]");
					minMaxFilterIndexes.add(i);

					String columnName = ((SingleProjectionSimpleFilter) filter).getProjection().getName();
					Projection projection = new Projection(AggregationFunctions.MAX_FUNCTION, dataSet, columnName, columnName);
					minMaxProjections.add(projection);
				} else {
					noMinMaxFilters.add(filter);
				}
			} else {
				noMinMaxFilters.add(filter);
			}
		}

		if (minMaxFilterIndexes.size() > 0) {
			logger.debug("MIN/MAX filter found");

			Filter where = getWhereFilter(noMinMaxFilters, likeFilters);

			IDataStore dataStore = getDataStore(dataSet, isNearRealtime, parametersValues, minMaxProjections, where, null, null, null, -1, -1, -1);
			if (dataStore == null) {
				String errorMessage = "Error in getting min and max filters values";
				logger.error(errorMessage);
				throw new SpagoBIRuntimeException(errorMessage);
			}

			logger.debug("MIN/MAX filter values calculated");

			for (int i = 0; i < minMaxProjections.size(); i++) {
				Projection projection = minMaxProjections.get(i);
				String alias = projection.getAlias();
				String errorMessage = "MIN/MAX value for field [" + alias + "] not found";

				int index = minMaxFilterIndexes.get(i);

				List values = dataStore.getFieldValues(i);
				if (values == null) {
					logger.error(errorMessage);
					throw new SpagoBIRuntimeException(errorMessage);
				} else {
					Projection projectionWithoutAggregation = new Projection(projection.getDataset(), projection.getName(), alias);
					if (values.isEmpty()) {
						logger.warn(errorMessage + ", put NULL");
						newFilters.set(index, new NullaryFilter(projectionWithoutAggregation, SimpleFilterOperator.IS_NULL));
					} else {
						Object value = values.get(0);
						logger.debug("MIN/MAX value for field [" + alias + "] is equal to [" + value + "]");
						newFilters.set(index, new UnaryFilter(projectionWithoutAggregation, SimpleFilterOperator.EQUALS_TO, value));
					}
				}
			}
		}

		logger.debug("OUT");
		return newFilters;
	}

	public Filter getWhereFilter(List<Filter> filters, List<SimpleFilter> likeFilters) {
		Filter where = null;
		if (filters.size() > 0) {
			AndFilter andFilter = new AndFilter(filters);
			if (likeFilters.size() > 0) {
				where = andFilter.and(new OrFilter(likeFilters));
			}
			where = andFilter;
		} else if (likeFilters.size() > 0) {
			where = new OrFilter(likeFilters);
		}
		return where;
	}

}
