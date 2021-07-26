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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spago.error.EMFErrorHandler;
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
import it.eng.spagobi.tools.dataset.cache.CacheFactory;
import it.eng.spagobi.tools.dataset.cache.ICache;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.dataset.cache.impl.sqldbcache.SQLDBCache;
import it.eng.spagobi.tools.dataset.ckan.CKANConfig;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 */
public class SelfServiceDatasetStartAction extends ManageDatasets {

	private static final long serialVersionUID = 1L;

	public static final String SERVICE_NAME = "SELF_SERVICE_DATASET_ACTION";
	public static final String LANGUAGE = "LANGUAGE";;
	public static final String COUNTRY = "COUNTRY";
	public static final String OUTPUT_PARAMETER_EXECUTION_ID = "executionId";

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

	@Override
	public void doService() {
		logger.debug("IN");
		HttpServletRequest httpRequest = getHttpRequest();
		try {

			String executionId = ExecuteAdHocUtility.createNewExecutionId();

			String qbeEditFromBMActionUrl = buildQbeEditFromBMServiceUrl(executionId);
			String qbeEditFromFederationActionUrl = buildQbeEditFromFederationServiceUrl(executionId);
			String qbeEditFromDataSetActionUrl = buildQbeEditFromDataSetServiceUrl(executionId);
			String qbeEditDataSetActionUrl = buildQbeEditDataSetServiceUrl(executionId);
			String geoereportEditActionUrl = buildGeoreportEditServiceUrl(executionId);
			String cockpitEditActionUrl = buildCockpitEditServiceUrl(executionId);
			String isFromMyData = (getAttributeAsString("MYDATA") == null) ? "FALSE" : getAttributeAsString("MYDATA");
			String isFromMyAnalysis = (getAttributeAsString("MYANALYSIS") == null) ? "FALSE" : getAttributeAsString("MYANALYSIS");
			String isFromDocBrowser = (getAttributeAsString("SBI_ENVIRONMENT") == null) ? "FALSE"
					: (getAttributeAsString("SBI_ENVIRONMENT").equals("DOCBROWSER")) ? "TRUE" : "FALSE";
			String typeDoc = getAttributeAsString("TYPE_DOC");
			String userCanPersist = userCanPersist();
			String tableNamePrefix = getTableNamePrefix();
			String isSmartFilterEnabled = isSmartFilterEnabled();
			String isCkanEnabled = isCkanEnabled();
			String ckanUrls = getCkanUrls();
			String canCreateDatasetAsFinalUser = canCreateDatasetAsFinalUser();
			String canUseFederatedDataset = canUseFederatedDatasetAsFinalUser();

			String callBackFunction = (getAttributeAsString(CALLBACK_FUNCTION) == null) ? "empty" : getAttributeAsString("CALLBACK_FUNCTION");// name of a
																																				// function that
																																				// has to be
																																				// invoked on
																																				// manageSelfService.jsp.
																																				// Now we use
																																				// toFederation
																																				// in order to
																																				// pen the
																																				// federation
																																				// tab after.

			logger.trace("Copying output parameters to response...");
			try {
				Locale locale = getLocale();
				setAttribute(LANGUAGE, locale.getLanguage());
				setAttribute(COUNTRY, locale.getCountry());
				setAttribute(OUTPUT_PARAMETER_EXECUTION_ID, executionId);
				setAttribute(OUTPUT_PARAMETER_QBE_EDIT_FROM_FEDERATION_SERVICE_URL, qbeEditFromFederationActionUrl);
				setAttribute(OUTPUT_PARAMETER_QBE_EDIT_FROM_BM_SERVICE_URL, qbeEditFromBMActionUrl);
				setAttribute(OUTPUT_PARAMETER_QBE_EDIT_FROM_DATA_SET_SERVICE_URL, qbeEditFromDataSetActionUrl);
				setAttribute(OUTPUT_PARAMETER_QBE_EDIT_DATASET_SERVICE_URL, qbeEditDataSetActionUrl);
				setAttribute(OUTPUT_PARAMETER_GEOREPORT_EDIT_SERVICE_URL, geoereportEditActionUrl);
				setAttribute(OUTPUT_PARAMETER_COCKPIT_EDIT_SERVICE_URL, cockpitEditActionUrl);
				setAttribute(IS_FROM_MYDATA, isFromMyData);
				setAttribute(TYPE_DOC, typeDoc);
				setAttribute(IS_FROM_MYANALYSIS, isFromMyAnalysis);
				setAttribute(IS_FROM_DOCBROWSER, isFromDocBrowser);
				setAttribute(USER_CAN_PERSIST, userCanPersist);
				setAttribute(TABLE_NAME_PREFIX, tableNamePrefix);
				setAttribute(IS_SMARTFILTER_ENABLED, isSmartFilterEnabled);
				setAttribute(IS_CKAN_ENABLED, isCkanEnabled);
				setAttribute(CKAN_URLS, ckanUrls);
				setAttribute(CAN_CREATE_DATASET_AS_FINAL_USER, canCreateDatasetAsFinalUser);
				setAttribute(CAN_USE_FEDERATED_DATASET_AS_FINAL_USER, canUseFederatedDataset);
				setAttribute(CALLBACK_FUNCTION, callBackFunction);

			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "An error occurred while creating service response", t);
			}
			logger.trace("Output parameter succesfully copied to response");

		} catch (Exception ex) {
			try {
				logger.error(ex);
				EMFErrorHandler errorHandler = getErrorHandler();
				EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
				errorHandler.addError(internalError);
				return;
			} catch (Exception s) {
				throw new SpagoBIRuntimeException("An error occurred while creating service response", s);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	// GEO
	protected String buildGeoreportEditServiceUrl(String executionId) {
		Map<String, String> parametersMap = buildGeoreportEditServiceBaseParametersMap();
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

	protected Map<String, String> buildGeoreportEditServiceBaseParametersMap() {
		Map<String, String> parametersMap = buildServiceBaseParametersMap();

		return parametersMap;
	}

	// QBE from BM
	protected String buildQbeEditFromDataSetServiceUrl(String executionId) {
		Engine qbeEngine = null;
		String qbeEditActionUrl = null;
		String label = null;

		Map<String, String> parametersMap = buildQbeEditFromFederationServiceBaseParametersMap();
		parametersMap.put("SBI_EXECUTION_ID", executionId);

		IDataSource datasource;
		try {
			datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
		} catch (EMFUserError e) {
			throw new SpagoBIRuntimeException("Error while loading default datasource for writing", e);
		}
		if (datasource != null) {
			parametersMap.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
			parametersMap.put(EngineConstants.ENV_DATASOURCE_FOR_CACHE, datasource.getLabel());
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

	protected String buildQbeEditFromBMServiceUrl(String executionId) {
		Engine qbeEngine = null;
		String qbeEditActionUrl = null;

		Map<String, String> parametersMap = buildQbeEditFromBMServiceBaseParametersMap();
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
	protected String buildQbeEditFromFederationServiceUrl(String executionId) throws Exception {
		Engine qbeEngine = null;
		String qbeEditActionUrl = null;
		String label = null;
		Map<String, String> parametersMap = buildQbeEditFromFederationServiceBaseParametersMap();
		parametersMap.put("SBI_EXECUTION_ID", executionId);

		ICache cache = CacheFactory.getCache(SpagoBICacheConfiguration.getInstance());
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
	// Map<String, String> parametersMap = buildQbeEditFromDataSetServiceBaseParametersMap();
	// parametersMap.put("SBI_EXECUTION_ID", executionId);
	//
	// IDataSource datasource;
	// try {
	// datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
	// } catch (EMFUserError e) {
	// throw new SpagoBIRuntimeException("Error while loading default datasource for writing", e);
	// }
	// if (datasource != null) {
	// parametersMap.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
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
	// LogMF.debug(logger, "Engine label is equal to [{0}]", qbeEngine.getLabel());
	//
	// // create the qbe Edit Service's URL
	// qbeEditActionUrl = GeneralUtilities.getUrl(qbeEngine.getUrl(), parametersMap);
	// LogMF.debug(logger, "Qbe edit service invocation url is equal to [{}]", qbeEditActionUrl);
	// }
	// return qbeEditActionUrl;
	// }

	// QBE to edit a dataset
	protected String buildQbeEditDataSetServiceUrl(String executionId) {
		Engine qbeEngine = null;
		String qbeEditActionUrl = null;

		Map<String, String> parametersMap = buildQbeEditDataSetServiceBaseParametersMap();
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

	protected Map<String, String> buildQbeEditFromBMServiceBaseParametersMap() {
		Map<String, String> parametersMap = buildServiceBaseParametersMap();
		parametersMap.put("ACTION_NAME", QBE_EDIT_FROM_BM_ACTION);
		return parametersMap;
	}

	protected Map<String, String> buildQbeEditFromFederationServiceBaseParametersMap() {
		Map<String, String> parametersMap = buildServiceBaseParametersMap();
		parametersMap.put("ACTION_NAME", QBE_EDIT_FROM_FEDERATION_ACTION);
		return parametersMap;
	}

	protected Map<String, String> buildQbeEditFromDataSetServiceBaseParametersMap() {
		Map<String, String> parametersMap = buildServiceBaseParametersMap();
		parametersMap.put("ACTION_NAME", QBE_EDIT_FROM_DATA_SET_ACTION);
		return parametersMap;
	}

	protected Map<String, String> buildQbeEditDataSetServiceBaseParametersMap() {
		Map<String, String> parametersMap = buildServiceBaseParametersMap();
		parametersMap.put("ACTION_NAME", QBE_EDIT_DATA_SET_ACTION);
		return parametersMap;
	}

	protected Map<String, String> buildServiceBaseParametersMap() {
		HashMap<String, String> parametersMap = new HashMap<>();

		parametersMap.put("NEW_SESSION", "TRUE");

		parametersMap.put(SpagoBIConstants.SBI_LANGUAGE, getLocale().getLanguage());
		parametersMap.put(SpagoBIConstants.SBI_COUNTRY, getLocale().getCountry());
		parametersMap.put(SpagoBIConstants.SBI_SCRIPT, getLocale().getScript());

		// if (!GeneralUtilities.isSSOEnabled()) {
		UserProfile userProfile = (UserProfile) getUserProfile();
		parametersMap.put(SsoServiceInterface.USER_ID, (String) userProfile.getUserUniqueIdentifier());
		// }

		return parametersMap;
	}

	// COCKPIT
	protected String buildCockpitEditServiceUrl(String executionId) {
		Engine cockpitEngine = null;
		String cockpitEditActionUrl = null;

		Map<String, String> parametersMap = buildCockpitEditServiceBaseParametersMap();
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

	protected Map<String, String> buildCockpitEditServiceBaseParametersMap() {
		Map<String, String> parametersMap = buildServiceBaseParametersMap();

		return parametersMap;
	}

	// Check if user can persist a dataset
	protected String userCanPersist() {
		List funcs;
		try {
			profile = getUserProfile();
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
	protected String isCkanEnabled() {
		List funcs;
		try {
			profile = getUserProfile();
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
	protected String getCkanUrls() {
		List funcs;
		try {
			profile = getUserProfile();
			funcs = (List) profile.getFunctionalities();
			// Check if user can user CKAN
			if (!isAbleTo(SpagoBIConstants.CKAN_FUNCTIONALITY, funcs)) {
				return "";
			} else {
				Properties ckanUrls = CKANConfig.getInstance().getConfig();
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
	protected String canCreateDatasetAsFinalUser() {
		List funcs;
		try {
			profile = getUserProfile();
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
	protected String canUseFederatedDatasetAsFinalUser() {
		List funcs;
		try {
			profile = getUserProfile();
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
			throw new SpagoBIEngineRuntimeException("Impossible get smart filter availability");
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
