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

package it.eng.spagobi.tools.dataset.service;

import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.execution.service.ExecuteAdHocUtility;
import it.eng.spagobi.commons.bo.Config;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IConfigDAO;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.config.dao.IEngineDAO;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.tools.dataset.cache.CacheException;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheManager;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.ckan.CKANConfig;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

public class SelfServiceDatasetAction {

	private static final long serialVersionUID = 1L;

	public static final String SERVICE_NAME = "SELF_SERVICE_DATASET_ACTION";
	public static final String LANGUAGE = "LANGUAGE";
	public static final String COUNTRY = "COUNTRY";
	public static final String OUTPUT_PARAMETER_EXECUTION_ID = "executionId";

	public static final String OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL = "worksheetServiceUrl";
	public static final String WORKSHEET_EDIT_ACTION = "WORKSHEET_WITH_DATASET_START_EDIT_ACTION";

	public static final String OUTPUT_PARAMETER_QBE_EDIT_FROM_BM_SERVICE_URL = "qbeFromBMServiceUrl";
	public static final String OUTPUT_PARAMETER_QBE_EDIT_FROM_DATA_SET_SERVICE_URL = "qbeFromDataSetServiceUrl";
	public static final String OUTPUT_PARAMETER_QBE_EDIT_DATASET_SERVICE_URL = "qbeEditDatasetServiceUrl";
	public static final String OUTPUT_PARAMETER_QBE_EDIT_FROM_FEDERATION_SERVICE_URL = "qbeEditFederationServiceUrl";

	public static final String QBE_EDIT_FROM_BM_ACTION = "QBE_ENGINE_START_ACTION_FROM_BM";
	public static final String QBE_EDIT_FROM_FEDERATION_ACTION = "QBE_ENGINE_FROM_FEDERATION_START_ACTION";
	public static final String QBE_EDIT_FROM_DATA_SET_ACTION = "QBE_ENGINE_FROM_DATASET_START_ACTION";
	public static final String QBE_EDIT_DATA_SET_ACTION = "QBE_ENGINE_EDIT_DATASET_START_ACTION";

	public static final String OUTPUT_PARAMETER_GEOREPORT_EDIT_SERVICE_URL = "georeportServiceUrl";
	public static final String OUTPUT_PARAMETER_COCKPIT_EDIT_SERVICE_URL = "cockpitServiceUrl";
	public static final String IS_FROM_MYDATA = "MYDATA";
	public static final String TYPE_DOC = "TYPE_DOC";
	public static final String IS_FROM_MYANALYSIS = "MYANALYSIS";
	public static final String IS_FROM_DOCBROWSER = "DOCBROWSER";
	public static final String USER_CAN_PERSIST = "USER_CAN_PERSIST";
	public static final String TABLE_NAME_PREFIX = "TABLE_NAME_PREFIX";

	public static final String PERSIST_TABLE_PREFIX_CONFIG = "SPAGOBI.DATASET.PERSIST.TABLE_PREFIX";
	public static final String IS_WORKSHEET_ENABLED = "IS_WORKSHEET_ENABLED";
	public static final String IS_SMARTFILTER_ENABLED = "IS_SMARTFILTER_ENABLED";
	public static final String IS_CKAN_ENABLED = "IS_CKAN_ENABLED";
	public static final String CKAN_URLS = "CKAN_URLS";
	public static final String CAN_CREATE_DATASET_AS_FINAL_USER = "CAN_CREATE_DATASET_AS_FINAL_USER";
	public static final String CAN_USE_FEDERATED_DATASET_AS_FINAL_USER = "CAN_USE_FEDERATED_DATASET_AS_FINAL_USER";
	public static final String CALLBACK_FUNCTION = "CALLBACK_FUNCTION";

	// public static final String GEOREPORT_EDIT_ACTION =
	// "GEOREPORT_ENGINE_START_EDIT_ACTION";

	// logger component
	private static Logger logger = Logger.getLogger(SelfServiceDatasetStartAction.class);

	public Map<String, String> getParameters(UserProfile profile, Locale locale) {
		logger.debug("IN");
		try {

			String executionId = ExecuteAdHocUtility.createNewExecutionId();

			String qbeEditFromBMActionUrl = buildQbeEditFromBMServiceUrl(executionId, locale, profile);
			String qbeEditFromFederationActionUrl = buildQbeEditFromFederationServiceUrl(executionId, locale, profile);
			String qbeEditFromDataSetActionUrl = buildQbeEditFromDataSetServiceUrl(executionId, locale, profile);
			String qbeEditDataSetActionUrl = buildQbeEditDataSetServiceUrl(executionId, locale, profile);
			String worksheetEditActionUrl = buildWorksheetEditServiceUrl(executionId, locale, profile);
			String geoereportEditActionUrl = buildGeoreportEditServiceUrl(executionId, profile, locale);
			String cockpitEditActionUrl = buildCockpitEditServiceUrl(executionId, locale, profile);
			String userCanPersist = userCanPersist(profile);
			String tableNamePrefix = getTableNamePrefix();
			String isWorksheetEnabled = isWorksheetEnabled();
			String isSmartFilterEnabled = isSmartFilterEnabled();
			String isCkanEnabled = isCkanEnabled(profile);
			String ckanUrls = getCkanUrls(profile);
			String canCreateDatasetAsFinalUser = canCreateDatasetAsFinalUser(profile);
			String canUseFederatedDataset = canUseFederatedDatasetAsFinalUser(profile);

			logger.trace("Copying output parameters to response...");
			try {
				Map<String, String> parameters = new HashMap<>();

				parameters.put(OUTPUT_PARAMETER_EXECUTION_ID, executionId);
				parameters.put(OUTPUT_PARAMETER_QBE_EDIT_FROM_FEDERATION_SERVICE_URL, qbeEditFromFederationActionUrl);
				parameters.put(OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL, worksheetEditActionUrl);
				parameters.put(OUTPUT_PARAMETER_QBE_EDIT_FROM_BM_SERVICE_URL, qbeEditFromBMActionUrl);
				parameters.put(OUTPUT_PARAMETER_QBE_EDIT_FROM_DATA_SET_SERVICE_URL, qbeEditFromDataSetActionUrl);
				parameters.put(OUTPUT_PARAMETER_QBE_EDIT_DATASET_SERVICE_URL, qbeEditDataSetActionUrl);
				parameters.put(OUTPUT_PARAMETER_GEOREPORT_EDIT_SERVICE_URL, geoereportEditActionUrl);
				parameters.put(OUTPUT_PARAMETER_COCKPIT_EDIT_SERVICE_URL, cockpitEditActionUrl);
				parameters.put(USER_CAN_PERSIST, userCanPersist);
				parameters.put(TABLE_NAME_PREFIX, tableNamePrefix);
				parameters.put(IS_WORKSHEET_ENABLED, isWorksheetEnabled);
				parameters.put(IS_SMARTFILTER_ENABLED, isSmartFilterEnabled);
				parameters.put(IS_CKAN_ENABLED, isCkanEnabled);
				parameters.put(CKAN_URLS, ckanUrls);
				parameters.put(CAN_CREATE_DATASET_AS_FINAL_USER, canCreateDatasetAsFinalUser);
				parameters.put(CAN_USE_FEDERATED_DATASET_AS_FINAL_USER, canUseFederatedDataset);

				return parameters;
			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "An error occurred while creating service response", t);
			}
			// logger.trace("Output parameter succesfully copied to response");

		} catch (CacheException ex) {
			try {
				logger.error(ex);
				Map<String, String> errorMap = new HashMap<>();

				// EMFErrorHandler errorHandler = getErrorHandler();
				EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
				// errorHandler.addError(internalError);
				errorMap.put("error", ex.getMessage());
				return errorMap;
			} catch (Exception s) {
				throw new SpagoBIRuntimeException("An error occurred while creating service response", s);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	// GEO
	protected String buildGeoreportEditServiceUrl(String executionId, UserProfile profile, Locale locale) {
		Map<String, String> parametersMap = buildGeoreportEditServiceBaseParametersMap(locale, profile);
		parametersMap.put("SBI_EXECUTION_ID", executionId);
		Engine georeportEngine = null;
		String georeportEditActionUrl = null;
		try {
			georeportEngine = ExecuteAdHocUtility.getGeoreportEngine();
		} catch (SpagoBIRuntimeException r) {
			// the geo engine is not found
			logger.info("Engine not found. Error: ", r);
		}
		if (georeportEngine != null) {
			String baseEditUrl = georeportEngine.getUrl().replace("GeoReportEngineStartAction", "GeoReportEngineStartEditAction");
			georeportEditActionUrl = GeneralUtilities.getUrl(baseEditUrl, parametersMap);
			LogMF.debug(logger, "Georeport edit service invocation url is equal to [{}]", georeportEditActionUrl);
		}
		return georeportEditActionUrl;
	}

	protected Map<String, String> buildGeoreportEditServiceBaseParametersMap(Locale locale, UserProfile profile) {
		Map<String, String> parametersMap = buildServiceBaseParametersMap(locale, profile);

		return parametersMap;
	}

	// WORKSHEET
	protected String buildWorksheetEditServiceUrl(String executionId, Locale locale, UserProfile profile) {
		Engine worksheetEngine = null;
		String worksheetEditActionUrl = null;

		Map<String, String> parametersMap = buildWorksheetEditServiceBaseParametersMap(locale, profile);
		parametersMap.put("SBI_EXECUTION_ID", executionId);

		IDataSource datasource;
		try {
			datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while loading default datasource for writing", e);
		}
		if (datasource != null) {
			parametersMap.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
		} else {
			logger.debug("There is no default datasource for writing");
		}

		try {
			worksheetEngine = ExecuteAdHocUtility.getWorksheetEngine();
		} catch (SpagoBIRuntimeException r) {
			// the ws engine is not found
			logger.info("Engine not found. Error: ", r);
		}
		if (worksheetEngine != null) {
			LogMF.debug(logger, "Engine label is equal to [{0}]", worksheetEngine.getLabel());
			// create the WorkSheet Edit Service's URL
			worksheetEditActionUrl = GeneralUtilities.getUrl(worksheetEngine.getUrl(), parametersMap);
			LogMF.debug(logger, "Worksheet edit service invocation url is equal to [{}]", worksheetEditActionUrl);
		}

		return worksheetEditActionUrl;
	}

	protected Map<String, String> buildWorksheetEditServiceBaseParametersMap(Locale locale, UserProfile profile) {
		Map<String, String> parametersMap = buildServiceBaseParametersMap(locale, profile);
		parametersMap.put("ACTION_NAME", WORKSHEET_EDIT_ACTION);
		return parametersMap;
	}

	// QBE from BM
	protected String buildQbeEditFromDataSetServiceUrl(String executionId, Locale locale, UserProfile profile) {
		Engine qbeEngine = null;
		String qbeEditActionUrl = null;
		String label = null;

		Map<String, String> parametersMap = buildQbeEditFromFederationServiceBaseParametersMap(locale, profile);
		parametersMap.put("SBI_EXECUTION_ID", executionId);

		IDataSource datasource;
		try {
			datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while loading default datasource for writing", e);
		}
		if (datasource != null) {
			parametersMap.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
		} else {
			logger.debug("There is no default datasource for writing");
		}

		logger.debug("Getting the cache data source");
		ICache cache = SpagoBICacheManager.getCache();
		if (cache instanceof SQLDBCache) {
			logger.debug("The cache is a SQL cache so we have the datasource");
			label = ((SQLDBCache) cache).getDataSource().getLabel();
			logger.debug("The datasource is " + label);
		}
		if (label != null) {
			parametersMap.put(EngineConstants.ENV_DATASOURCE_FOR_CACHE, label);
		} else {
			logger.error("There is no default datasource for writing");
		}

		try {
			qbeEngine = ExecuteAdHocUtility.getQbeEngine();
		} catch (SpagoBIRuntimeException r) {
			// the qbe engine is not found
			logger.info("Engine not found. Error: ", r);
		}

		if (qbeEngine != null) {
			LogMF.debug(logger, "Engine label is equal to [{0}]", qbeEngine.getLabel());

			// create the qbe Edit Service's URL
			qbeEditActionUrl = GeneralUtilities.getUrl(qbeEngine.getUrl(), parametersMap);
			LogMF.debug(logger, "Qbe edit service invocation url is equal to [{}]", qbeEditActionUrl);
		}
		return qbeEditActionUrl;
	}

	protected String buildQbeEditFromBMServiceUrl(String executionId, Locale locale, UserProfile profile) {
		Engine qbeEngine = null;
		String qbeEditActionUrl = null;

		Map<String, String> parametersMap = buildQbeEditFromBMServiceBaseParametersMap(locale, profile);
		parametersMap.put("SBI_EXECUTION_ID", executionId);

		IDataSource datasource;
		try {
			datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while loading default datasource for writing", e);
		}
		if (datasource != null) {
			parametersMap.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
		} else {
			logger.debug("There is no default datasource for writing");
		}

		try {
			qbeEngine = ExecuteAdHocUtility.getQbeEngine();
		} catch (SpagoBIRuntimeException r) {
			// the qbe engine is not found
			logger.info("Engine not found. Error: ", r);
		}

		if (qbeEngine != null) {
			LogMF.debug(logger, "Engine label is equal to [{0}]", qbeEngine.getLabel());

			// create the qbe Edit Service's URL
			qbeEditActionUrl = GeneralUtilities.getUrl(qbeEngine.getUrl(), parametersMap);
			LogMF.debug(logger, "Qbe edit service invocation url is equal to [{}]", qbeEditActionUrl);
		}
		return qbeEditActionUrl;
	}

	// QBE from dataset
	protected String buildQbeEditFromFederationServiceUrl(String executionId, Locale locale, UserProfile profile) {
		Engine qbeEngine = null;
		String qbeEditActionUrl = null;
		String label = null;
		Map<String, String> parametersMap = buildQbeEditFromFederationServiceBaseParametersMap(locale, profile);
		parametersMap.put("SBI_EXECUTION_ID", executionId);

		ICache cache = SpagoBICacheManager.getCache();
		if (cache instanceof SQLDBCache) {
			logger.debug("The cache is a SQL cache so we have the datasource");
			label = ((SQLDBCache) cache).getDataSource().getLabel();
			logger.debug("The datasource is " + label);
		}

		if (label != null) {
			parametersMap.put(EngineConstants.ENV_DATASOURCE_FOR_CACHE, label);
		} else {
			logger.debug("There is no default datasource for writing");
		}

		try {
			qbeEngine = ExecuteAdHocUtility.getQbeEngine();
		} catch (SpagoBIRuntimeException r) {
			// the qbe engine is not found
			logger.info("Engine not found. Error: ", r);
		}

		if (qbeEngine != null) {
			LogMF.debug(logger, "Engine label is equal to [{0}]", qbeEngine.getLabel());

			// create the qbe Edit Service's URL
			qbeEditActionUrl = GeneralUtilities.getUrl(qbeEngine.getUrl(), parametersMap);
			LogMF.debug(logger, "Qbe edit service invocation url is equal to [{}]", qbeEditActionUrl);
		}
		return qbeEditActionUrl;
	}

	// // QBE from dataset
	// protected String buildQbeEditFromDataSetServiceUrl2(String executionId) {
	// Engine qbeEngine = null;
	// String qbeEditActionUrl = null;
	//
	// Map<String, String> parametersMap =
	// buildQbeEditFromDataSetServiceBaseParametersMap();
	// parametersMap.put("SBI_EXECUTION_ID", executionId);
	//
	// IDataSource datasource;
	// try {
	// datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
	// } catch (EMFUserError e) {
	// throw new
	// SpagoBIRuntimeException("Error while loading default datasource for writing",
	// e);
	// }
	// if (datasource != null) {
	// parametersMap.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL,
	// datasource.getLabel());
	// } else {
	// logger.debug("There is no default datasource for writing");
	// }
	//
	// try {
	// qbeEngine = ExecuteAdHocUtility.getQbeEngine();
	// } catch (SpagoBIRuntimeException r) {
	// // the qbe engine is not found
	// logger.info("Engine not found. Error: ", r);
	// }
	//
	// if (qbeEngine != null) {
	// LogMF.debug(logger, "Engine label is equal to [{0}]",
	// qbeEngine.getLabel());
	//
	// // create the qbe Edit Service's URL
	// qbeEditActionUrl = GeneralUtilities.getUrl(qbeEngine.getUrl(),
	// parametersMap);
	// LogMF.debug(logger, "Qbe edit service invocation url is equal to [{}]",
	// qbeEditActionUrl);
	// }
	// return qbeEditActionUrl;
	// }

	// QBE to edit a dataset
	protected String buildQbeEditDataSetServiceUrl(String executionId, Locale locale, UserProfile profile) {
		Engine qbeEngine = null;
		String qbeEditActionUrl = null;

		Map<String, String> parametersMap = buildQbeEditDataSetServiceBaseParametersMap(locale, profile);
		parametersMap.put("SBI_EXECUTION_ID", executionId);

		IDataSource datasource;
		try {
			datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while loading default datasource for writing", e);
		}
		if (datasource != null) {
			parametersMap.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
		} else {
			logger.debug("There is no default datasource for writing");
		}

		try {
			qbeEngine = ExecuteAdHocUtility.getQbeEngine();
		} catch (SpagoBIRuntimeException r) {
			// the qbe engine is not found
			logger.info("Engine not found. Error: ", r);
		}

		if (qbeEngine != null) {
			LogMF.debug(logger, "Engine label is equal to [{0}]", qbeEngine.getLabel());

			// create the qbe Edit Service's URL
			qbeEditActionUrl = GeneralUtilities.getUrl(qbeEngine.getUrl(), parametersMap);
			LogMF.debug(logger, "Qbe edit service invocation url is equal to [{}]", qbeEditActionUrl);
		}
		return qbeEditActionUrl;
	}

	protected Map<String, String> buildQbeEditFromBMServiceBaseParametersMap(Locale locale, UserProfile profile) {
		Map<String, String> parametersMap = buildServiceBaseParametersMap(locale, profile);
		parametersMap.put("ACTION_NAME", QBE_EDIT_FROM_BM_ACTION);
		return parametersMap;
	}

	protected Map<String, String> buildQbeEditFromFederationServiceBaseParametersMap(Locale locale, UserProfile profile) {
		Map<String, String> parametersMap = buildServiceBaseParametersMap(locale, profile);
		parametersMap.put("ACTION_NAME", QBE_EDIT_FROM_FEDERATION_ACTION);
		return parametersMap;
	}

	protected Map<String, String> buildQbeEditFromDataSetServiceBaseParametersMap(Locale locale, UserProfile profile) {
		Map<String, String> parametersMap = buildServiceBaseParametersMap(locale, profile);
		parametersMap.put("ACTION_NAME", QBE_EDIT_FROM_DATA_SET_ACTION);
		return parametersMap;
	}

	protected Map<String, String> buildQbeEditDataSetServiceBaseParametersMap(Locale locale, UserProfile profile) {
		Map<String, String> parametersMap = buildServiceBaseParametersMap(locale, profile);
		parametersMap.put("ACTION_NAME", QBE_EDIT_DATA_SET_ACTION);
		return parametersMap;
	}

	protected Map<String, String> buildServiceBaseParametersMap(Locale locale, UserProfile userProfile) {
		HashMap<String, String> parametersMap = new HashMap<String, String>();

		parametersMap.put("NEW_SESSION", "TRUE");

		parametersMap.put(SpagoBIConstants.SBI_CONTEXT, GeneralUtilities.getSpagoBiContext());
		parametersMap.put(SpagoBIConstants.SBI_HOST, GeneralUtilities.getSpagoBiHost());

		parametersMap.put(SpagoBIConstants.SBI_LANGUAGE, locale.getLanguage());
		parametersMap.put(SpagoBIConstants.SBI_COUNTRY, locale.getCountry());

		// if (!GeneralUtilities.isSSOEnabled()) {
		// UserProfile userProfile = (UserProfile) getUserProfile();
		parametersMap.put(SsoServiceInterface.USER_ID, (String) userProfile.getUserUniqueIdentifier());
		// }

		return parametersMap;
	}

	// COCKPIT
	protected String buildCockpitEditServiceUrl(String executionId, Locale locale, UserProfile profile) {
		Engine cockpitEngine = null;
		String cockpitEditActionUrl = null;

		Map<String, String> parametersMap = buildCockpitEditServiceBaseParametersMap(locale, profile);
		parametersMap.put("SBI_EXECUTION_ID", executionId);

		try {
			cockpitEngine = ExecuteAdHocUtility.getCockpitEngine();
		} catch (SpagoBIRuntimeException r) {
			// the cockpit engine is not found
			logger.info("Engine not found. Error: ", r);
		}

		if (cockpitEngine != null) {
			String baseEditUrl = cockpitEngine.getUrl().replace("pages/execute", "pages/edit");
			cockpitEditActionUrl = GeneralUtilities.getUrl(baseEditUrl, parametersMap);
			LogMF.debug(logger, "Cockpit edit service invocation url is equal to [{}]", cockpitEditActionUrl);
		}

		return cockpitEditActionUrl;
	}

	protected Map<String, String> buildCockpitEditServiceBaseParametersMap(Locale locale, UserProfile profile) {
		Map<String, String> parametersMap = buildServiceBaseParametersMap(locale, profile);

		return parametersMap;
	}

	// Check if user can persist a dataset
	protected String userCanPersist(UserProfile profile) {
		List funcs;
		try {
			// profile = getUserProfile();
			funcs = (List) profile.getFunctionalities();
			if (isAbleTo(SpagoBIConstants.ENABLE_DATASET_PERSISTENCE, funcs)) {
				return "true";
			} else {
				return "false";
			}
		} catch (EMFInternalError e) {
			throw new SpagoBIRuntimeException("Error while loading role functionalities of user", e);
		}

	}

	// Check if user can user CKAN
	protected String isCkanEnabled(UserProfile profile) {
		List funcs;
		try {
			// profile = getUserProfile();
			funcs = (List) profile.getFunctionalities();
			if (isAbleTo(SpagoBIConstants.CKAN_FUNCTIONALITY, funcs)) {
				return "true";
			} else {
				return "false";
			}
		} catch (EMFInternalError e) {
			throw new SpagoBIRuntimeException("Error while loading role functionalities of user", e);
		}

	}

	// Get the list of CKAN repository
	protected String getCkanUrls(UserProfile profile) {
		List funcs;
		try {
			// profile = getUserProfile();
			funcs = (List) profile.getFunctionalities();
			// Check if user can user CKAN
			if (!isAbleTo(SpagoBIConstants.CKAN_FUNCTIONALITY, funcs)) {
				return "";
			} else {
				Properties ckanUrls = CKANConfig.getInstance().getUrl();
				StringBuilder sb = new StringBuilder();
				sb.append("");
				for (Object objKey : ckanUrls.keySet()) {
					String key = (String) objKey;
					String value = (String) ckanUrls.get(objKey);
					sb.append(value);
					sb.append("|");
					sb.append(key);
					sb.append("|");
				}
				return sb.toString();
			}
		} catch (EMFInternalError e) {
			throw new SpagoBIRuntimeException("Error while loading role functionalities of user", e);
		}

	}

	// Check if user can create dataset as final user
	protected String canCreateDatasetAsFinalUser(UserProfile profile) {
		List funcs;
		try {
			// profile = getUserProfile();
			funcs = (List) profile.getFunctionalities();
			if (isAbleTo(SpagoBIConstants.CREATE_DATASETS_AS_FINAL_USER, funcs)) {
				return "true";
			} else {
				return "false";
			}
		} catch (EMFInternalError e) {
			throw new SpagoBIRuntimeException("Error while loading role functionalities of user", e);
		}

	}

	// Check if user can use federated dataset
	protected String canUseFederatedDatasetAsFinalUser(UserProfile profile) {
		List funcs;
		try {
			// profile = getUserProfile();
			funcs = (List) profile.getFunctionalities();
			if (isAbleTo(SpagoBIConstants.ENABLE_FEDERATED_DATASET, funcs)) {
				return "true";
			} else {
				return "false";
			}
		} catch (EMFInternalError e) {
			throw new SpagoBIRuntimeException("Error while loading role functionalities of user", e);
		}

	}

	// Get Table name prefix used for dataset persistence
	protected String getTableNamePrefix() {
		try {
			String tablePrefix = "";
			IConfigDAO configDao = DAOFactory.getSbiConfigDAO();
			Config tablePrefixConfig = configDao.loadConfigParametersByLabel(PERSIST_TABLE_PREFIX_CONFIG);
			if ((tablePrefixConfig != null) && (tablePrefixConfig.isActive())) {
				tablePrefix = tablePrefixConfig.getValueCheck();
			}
			return tablePrefix;

		} catch (EMFUserError e) {
			logger.error("Error while loading table name prefix used for dataset persistence");
			throw new SpagoBIRuntimeException("Error while loading table name prefix used for dataset persistence", e);
		} catch (Exception e) {
			logger.error("Error while loading table name prefix used for dataset persistence");
			throw new SpagoBIRuntimeException("Error while loading table name prefix used for dataset persistence", e);
		}

	}

	protected String isWorksheetEnabled() {
		String toReturn = "false";

		try {
			IEngineDAO engineDao = DAOFactory.getEngineDAO();
			List<Engine> nonPagedEngines = engineDao.loadAllEnginesByTenant();
			for (int i = 0, l = nonPagedEngines.size(); i < l; i++) {
				Engine elem = nonPagedEngines.get(i);
				IDomainDAO domainDAO = DAOFactory.getDomainDAO();
				Domain domainType = domainDAO.loadDomainById(elem.getBiobjTypeId());
				if (domainType.getValueCd().equalsIgnoreCase("WORKSHEET")) {
					toReturn = "true";
					break;
				}
			}
		} catch (Throwable t) {
			logger.error("Impossible to load engines from database ", t);
			throw new SpagoBIEngineRuntimeException("Impossible get worksheet availability");
		}

		return toReturn;

	}

	protected String isSmartFilterEnabled() {
		String toReturn = "false";

		try {
			IEngineDAO engineDao = DAOFactory.getEngineDAO();
			List<Engine> nonPagedEngines = engineDao.loadAllEnginesByTenant();
			for (int i = 0, l = nonPagedEngines.size(); i < l; i++) {
				Engine elem = nonPagedEngines.get(i);
				IDomainDAO domainDAO = DAOFactory.getDomainDAO();
				Domain domainType = domainDAO.loadDomainById(elem.getBiobjTypeId());
				if (domainType.getValueCd().equalsIgnoreCase("SMART_FILTER")) {
					toReturn = "true";
					break;
				}
			}
		} catch (Throwable t) {
			logger.error("Impossible to load engines from database ", t);
			throw new SpagoBIEngineRuntimeException("Impossible get worksheet availability");
		}
		return toReturn;
	}

	private boolean isAbleTo(String func, List funcs) {
		boolean toReturn = false;
		for (int i = 0; i < funcs.size(); i++) {
			if (func.equals(funcs.get(i))) {
				toReturn = true;
				break;
			}
		}
		return toReturn;
	}
}
