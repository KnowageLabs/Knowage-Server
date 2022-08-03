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

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.InflaterInputStream;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.UnsafeInput;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import commonj.work.Work;
import commonj.work.WorkItem;
import gnu.trove.set.hash.TLongHashSet;
import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.ICategoryDAO;
import it.eng.spagobi.commons.dao.IRoleDAO;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.tools.dataset.actions.DatasetActionsCheckerFactory;
import it.eng.spagobi.tools.dataset.association.DistinctValuesCalculateWork;
import it.eng.spagobi.tools.dataset.association.DistinctValuesClearWork;
import it.eng.spagobi.tools.dataset.bo.AbstractJDBCDataset;
import it.eng.spagobi.tools.dataset.bo.DataSetBasicInfo;
import it.eng.spagobi.tools.dataset.bo.DatasetEvaluationStrategyType;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.bo.VersionedDataSet;
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.common.datastore.DataStore;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.common.metadata.IFieldMetaData;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;
import it.eng.spagobi.tools.dataset.common.query.AggregationFunctions;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.dataset.exceptions.ParametersNotValorizedException;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.AndFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.NullaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.OrFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Projection;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.SimpleFilterOperator;
import it.eng.spagobi.tools.dataset.metasql.query.item.SingleProjectionSimpleFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnaryFilter;
import it.eng.spagobi.tools.dataset.metasql.query.item.UnsatisfiedFilter;
import it.eng.spagobi.tools.dataset.strategy.DatasetEvaluationStrategyFactory;
import it.eng.spagobi.tools.dataset.strategy.IDatasetEvaluationStrategy;
import it.eng.spagobi.tools.dataset.utils.DataSetUtilities;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheItem;
import it.eng.spagobi.utilities.database.DataBaseException;
import it.eng.spagobi.utilities.exceptions.ActionNotPermittedException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
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

	static private Logger logger = Logger.getLogger(DatasetManagementAPI.class);

	private UserProfile userProfile;
	private IDataSetDAO dataSetDao;

	private static final String ROWS = "ROWS";
	private static final String ROW = "ROW";
	private static final String NAME = "NAME";
	private static final String TYPE = "TYPE";
	private static final String MULTIVALUE = "MULTIVALUE";

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

	public List<DataSetBasicInfo> getDatasetsForLov() {
		return getDataSetDAO().loadDatasetsBasicInfoForLov();
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

			UserProfile currUserProfile = getUserProfile();

			if (UserUtilities.hasAdministratorRole(currUserProfile) || UserUtilities.hasDeveloperRole(currUserProfile)) {
				return dataSet;
			} else if (DataSetUtilities.isExecutableByUser(dataSet, currUserProfile) == false) {
				Integer dsCategoryId = dataSet.getCategoryId();
				if (dsCategoryId == null) {
					throw new RuntimeException("Dataset " + label + " doesn't have category set.");
				}
				// check categories of dataset
				Set<Domain> categoryList = UserUtilities.getDataSetCategoriesByUser(currUserProfile);
				if (categoryList != null && categoryList.size() > 0) {
					for (Iterator iterator = categoryList.iterator(); iterator.hasNext();) {
						Domain domain = (Domain) iterator.next();
						Integer domainId = domain.getValueId();

						if (dsCategoryId.equals(domainId)) {
							return dataSet;
						}
					}
				}
				// just if dataset hasn't a category available for the user gives an error
				throw new RuntimeException("User [" + currUserProfile.getUserId() + "] cannot access to dataset [" + label + "]");
			}
			return dataSet;
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method [getDataSet]. " + t.getMessage(), t);
		} finally {
			logger.debug("OUT");
		}
	}

	public List<IFieldMetaData> getDataSetFieldsMetadata(String label) {
		try {
			IDataSet dataSet = getDataSetDAO().loadDataSetByLabel(label);
			Assert.assertNotNull(dataSet, "Impossible to get dataset [" + label + "]");
			IMetaData metadata = dataSet.getMetadata();
			Assert.assertNotNull(dataSet, "Impossible to retrive metadata of dataset [" + metadata + "]");

			return metadata.getFieldsMeta();
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	/*
	 * Refresh cache for a specific dataset
	 */
	public String persistDataset(String label) throws Exception {
		logger.debug("IN dataset label " + label);
		ICache cache = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
		cache.setUserProfile(userProfile);
		IDataSet dataSet = this.getDataSetDAO().loadDataSetByLabel(label);
		cache.refresh(dataSet);

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

	public IDataStore getDataStore(IDataSet dataSet, boolean isNearRealtime, Map<String, String> parametersValues, List<AbstractSelectionField> projections,
			Filter filter, List<AbstractSelectionField> list, List<Sorting> sortings, List<List<AbstractSelectionField>> summaryRowProjections, int offset,
			int fetchSize, int maxRowCount, Set<String> indexes) throws JSONException {

		Monitor totalTiming = MonitorFactory.start("Knowage.DatasetManagementAPI.getDataStore");
		try {
			dataSet.setParametersMap(parametersValues);
			dataSet.resolveParameters();

			IDatasetEvaluationStrategy strategy = DatasetEvaluationStrategyFactory.get(dataSet.getEvaluationStrategy(isNearRealtime), dataSet, userProfile);
			return strategy.executeQuery(projections, filter, list, sortings, summaryRowProjections, offset, fetchSize, maxRowCount, indexes);

		} finally {
			totalTiming.stop();
			logger.debug("OUT");
		}
	}

	public void putDataSetInCache(IDataSet dataSet, ICache cache, Set<String> columns) throws DataBaseException {
		putDataSetInCache(dataSet, cache, DatasetEvaluationStrategyType.CACHED, columns);
	}

	public void putDataSetInCache(IDataSet dataSet, ICache cache, DatasetEvaluationStrategyType evaluationStrategy, Set<String> columns)
			throws DataBaseException {
		if (dataSet.isCachingSupported()) {
			if (dataSet instanceof VersionedDataSet) {
				dataSet = ((VersionedDataSet) dataSet).getWrappedDataset();
			}
			if (dataSet instanceof AbstractJDBCDataset && !dataSet.hasDataStoreTransformer()) {
				logger.debug("Copying JDBC dataset in cache using its iterator");
				cache.put(dataSet, columns);
			} else {
				logger.debug("Copying dataset in cache by loading the whole set of data in memory");
				dataSet.loadData();
				if (dataSet.getDataStore().getMetaData().getFieldCount() > 0) {
					if (DatasetEvaluationStrategyType.REALTIME.equals(evaluationStrategy)) {
						cache.put(dataSet, dataSet.getDataStore(), true, columns);
					} else {
						cache.put(dataSet, dataSet.getDataStore(), columns);
					}
				} else {
					cache.put(dataSet, new DataStore(dataSet.getMetadata()), columns);
				}
			}
		}
	}

	public List<IDataSet> getAllDataSet() {
		try {
			return getDataSetDAO().loadDataSets();
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * @deprecated
	 * TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> getEnterpriseDataSet() {
		try {
			return getDataSetDAO().loadEnterpriseDataSets(getUserProfile());
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * @deprecated
	 * TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> getOwnedDataSet() {
		try {
			return getDataSetDAO().loadDataSetsOwnedByUser(getUserProfile(), true);
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}

	}

	/**
	 * @deprecated
	 * TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> getSharedDataSet() {
		try {
			return getDataSetDAO().loadDatasetsSharedWithUser(getUserProfile(), true);
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * @deprecated
	 * TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> getUncertifiedDataSet() {
		try {
			return getDataSetDAO().loadDatasetOwnedAndShared(getUserProfile());
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * @deprecated
	 * TODO ML-DATASOURCE-V3 Delete
	 */
	@Deprecated
	public List<IDataSet> getMyDataDataSet() {
		try {
			return getDataSetDAO().loadMyDataDataSets(getUserProfile());
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
	}

	public List<DataSetBasicInfo> getFederatedDataSetsByFederation(Integer federationId) {
		List<DataSetBasicInfo> toReturn = new ArrayList<>();
		try {
			toReturn = getDataSetDAO().loadFederatedDataSetsByFederatoinId(federationId);
		} catch (Throwable t) {
			throw new RuntimeException("An unexpected error occured while executing method", t);
		} finally {
			logger.debug("OUT");
		}
		return toReturn;
	}

	/*
	 * Create indexes for the specified dataset and the specified columns
	 */
	public void createIndexes(String label, Set<String> columns) throws Exception {
		logger.debug("IN - Dataset label " + label);
		SQLDBCache cache = (SQLDBCache) CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
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

	protected List<Integer> getCategories(IEngUserProfile profile) {

		List<Integer> categories = new ArrayList<>();
		try {
			// NO CATEGORY IN THE DOMAINS
			ICategoryDAO categoryDao = DAOFactory.getCategoryDAO();

			// TODO : Makes sense?
			List<Domain> dialects = categoryDao.getCategoriesForDataset()
				.stream()
				.map(Domain::fromCategory)
				.collect(toList());
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

				List<Domain> array = categoryDao.getCategoriesForDataset()
					.stream()
					.map(Domain::fromCategory)
					.collect(toList());

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

	public void setDataSetParameters(IDataSet dataSet, Map<String, String> paramValues) {
		if (paramValues != null) {
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
							boolean isMultiValue = parameter.optBoolean("multiValuePar");
							String paramValue = paramValues.get(paramName);
							String[] values = null;
							if (paramValue == null) {
								values = new String[0];
							} else {
								values = isMultiValue ? paramValue.split(",") : Arrays.asList(paramValue).toArray(new String[0]);
							}

							String typePar = parameter.optString("typePar");
							String delim = "string".equalsIgnoreCase(typePar) ? "'" : "";
							boolean isString = "string".equalsIgnoreCase(typePar);
							List<String> newValues = new ArrayList<>();
							for (int j = 0; j < values.length; j++) {
								String value = values[j].trim();
								if (!value.isEmpty()) {
									if (!value.startsWith(delim) && !value.endsWith(delim)) {
										value = value.replaceAll("\'", "\'\'");
										newValues.add(delim + value + delim);
									} else {
										if (isString && value.startsWith(delim) && value.endsWith(delim)) {
											value = value.substring(1, value.length() - 1);
											value = value.replaceAll("\'", "\'\'");
											newValues.add(delim + value + delim);
										} else {
											if (isString)
												value = value.replaceAll("\'", "\'\'");
											newValues.add(value);
										}

									}

//									}
								}
							}
							String newValuesString = StringUtils.join(newValues, ",");
							newValuesString = newValuesString.replaceAll("&comma;", ",");
							paramValues.put(paramName, newValuesString);
							break;
						}
					}
				}
				dataSet.setParamsMap(paramValues);
			}
		}
	}

	public void setDataSetParameters(IDataSet dataSet, Map<String, String> paramValues, String currentParameterName) {
		if (paramValues != null) {
			List<JSONObject> parameters = getDataSetParameters(dataSet.getLabel());
			if (parameters.size() > paramValues.size()) {
				String parameterNotValorizedStr = getParametersNotValorized(parameters, paramValues);
				throw new ParametersNotValorizedException("The following parameters have no value [" + parameterNotValorizedStr + "]");
			}

			if (paramValues.size() > 0) {
				for (String paramName : paramValues.keySet()) {
					for (int i = 0; i < parameters.size(); i++) {
						JSONObject parameter = parameters.get(i);
						if (paramName.equals(parameter.optString("namePar")) && paramName.equals(currentParameterName)) {
							boolean isMultiValue = parameter.optBoolean("multiValuePar");
							String paramValue = paramValues.get(paramName);
							String[] values = null;
							if (paramValue == null) {
								values = new String[0];
							} else {
								values = isMultiValue ? paramValue.split(",") : Arrays.asList(paramValue).toArray(new String[0]);
							}

							String typePar = parameter.optString("typePar");
							String delim = "string".equalsIgnoreCase(typePar) ? "'" : "";
							boolean isString = "string".equalsIgnoreCase(typePar);
							List<String> newValues = new ArrayList<>();
							for (int j = 0; j < values.length; j++) {
								String value = values[j].trim();
								if (!value.isEmpty()) {
									if (!value.startsWith(delim) && !value.endsWith(delim)) {
										value = value.replaceAll("\'", "\'\'");
										newValues.add(delim + value + delim);
									} else {
										if (isString && value.startsWith(delim) && value.endsWith(delim)) {
											value = value.substring(1, value.length() - 1);
											value = value.replaceAll("\'", "\'\'");
											newValues.add(delim + value + delim);
										} else {
											if (isString)
												value = value.replaceAll("\'", "\'\'");
											newValues.add(value);
										}

									}

//									}
								}
							}
							String newValuesString = StringUtils.join(newValues, ",");
							newValuesString = newValuesString.replaceAll("&comma;", ",");
							paramValues.put(paramName, newValuesString);
							break;
						}
					}
				}
				dataSet.setParamsMap(paramValues);
			}
		}
	}

	public Map<String, TLongHashSet> readDomainValues(IDataSet dataSet, Map<String, String> parametersValues, boolean wait)
			throws NamingException, InterruptedException, JSONException {
		logger.debug("IN");
		Map<String, TLongHashSet> toReturn = new HashMap<>(0);
		dataSet.setParametersMap(parametersValues);
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

	public void calculateDomainValues(IDataSet dataSet) throws NamingException, InterruptedException {
		calculateDomainValues(dataSet, false);
	}

	public void calculateDomainValues(IDataSet dataSet, boolean wait) throws NamingException, InterruptedException {
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

	public void clearDomainValues(IDataSet dataSet) throws NamingException {
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

	/**
	 * if a filter has MAX() or MIN() value convert it by calculating the right value
	 */

	// FIXME
	public List<Filter> calculateMinMaxFilters(IDataSet dataSet, boolean isNearRealtime, Map<String, String> parametersValues, List<Filter> filters,
			List<SimpleFilter> likeFilters, Set<String> indexes) throws JSONException {

		logger.debug("IN");

		List<Filter> newFilters = new ArrayList<>(filters);

		List<Integer> minMaxFilterIndexes = new ArrayList<>();
		List<AbstractSelectionField> minMaxProjections = new ArrayList<>();

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
			IDataStore dataStore = null;
			if (dataSet.getEvaluationStrategy(isNearRealtime).equals(DatasetEvaluationStrategyType.CACHED)) {
				dataStore = getDataStore(dataSet, isNearRealtime, parametersValues, minMaxProjections, where, null, null, null, -1, -1, -1, indexes);
			} else {
				// List<List<Projection>> listToPrj = new ArrayList<List<Projection>>();
				// listToPrj.add(minMaxProjections);
				dataStore = getSummaryRowDataStore(dataSet, isNearRealtime, parametersValues, minMaxProjections, where, -1);
			}
			if (dataStore == null) {
				String errorMessage = "Error in getting min and max filters values";
				logger.error(errorMessage);
				throw new SpagoBIRuntimeException(errorMessage);
			}

			logger.debug("MIN/MAX filter values calculated");

			for (int i = 0; i < minMaxProjections.size(); i++) {
				Projection projection = (Projection) minMaxProjections.get(i);
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

	private IDataStore getSummaryRowDataStore(IDataSet dataSet, boolean isNearRealtime, Map<String, String> parametersValues,
			List<AbstractSelectionField> minMaxProjections, Filter filter, int maxRowCount) throws JSONException {
		dataSet.setParametersMap(parametersValues);
		dataSet.resolveParameters();

		IDatasetEvaluationStrategy strategy = DatasetEvaluationStrategyFactory.get(dataSet.getEvaluationStrategy(isNearRealtime), dataSet, userProfile);
		return strategy.executeSummaryRowQuery(minMaxProjections, filter, maxRowCount);
	}

	public Filter getWhereFilter(List<Filter> filters, List<SimpleFilter> likeFilters) {
		Filter where = null;
		if (filters.size() > 0) {
			if (filters.size() == 1 && filters.get(0) instanceof UnsatisfiedFilter) {
				where = filters.get(0);
			} else {
				AndFilter andFilter = new AndFilter(filters);
				if (likeFilters.size() > 0) {
					andFilter.and(new OrFilter(likeFilters));
				}
				where = andFilter;
			}
		} else if (likeFilters.size() > 0) {
			where = new OrFilter(likeFilters);
		}
		return where;
	}

	public void canLoadData(IDataSet dataSet) throws ActionNotPermittedException {
		DatasetActionsCheckerFactory.getDatasetActionsChecker(getUserProfile(), dataSet).canLoadData();
	}

	public void canEdit(IDataSet dataSet) throws ActionNotPermittedException {
		DatasetActionsCheckerFactory.getDatasetActionsChecker(getUserProfile(), dataSet).canEdit();
	}

	public void canSave(IDataSet dataSet) throws ActionNotPermittedException {
		DatasetActionsCheckerFactory.getDatasetActionsChecker(getUserProfile(), dataSet).canSave();
	}

	public void canShare(IDataSet dataSet) throws ActionNotPermittedException {
		DatasetActionsCheckerFactory.getDatasetActionsChecker(getUserProfile(), dataSet).canShare();
	}

}
