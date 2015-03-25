/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Action that gets a dataset id and prepare the url to call (depending on TARGET parameter value)
 *
 * 1) the worksheeet's edit service (WORKSHEET_WITH_DATASET_START_EDIT_ACTION) in order to build a worksheet document on the brandnew dataset
 *
 * 2) the Qbe Engine (QBE_ENGINE_FROM_DATASET_START_ACTION)
 *
 *
 *
 * @author Giulio Gavardi
 */
public class SelectDatasetAction extends CreateDatasetForWorksheetAction {

	private static final long serialVersionUID = 1L;

	public static final String WORKSHEET_EDIT_ACTION = "WORKSHEET_WITH_DATASET_START_EDIT_ACTION";
	public static final String QBE_EDIT_ACTION = "QBE_ENGINE_FROM_DATASET_START_ACTION";

	/** parameter that ecides action target */
	public static final String INPUT_PARAMETER_ENGINE = "ENGINE";
	public static final String WORKSHEET = "WORKSHEET";
	public static final String QBE = "QBE";

	/** label f dataset to open */
	public static final String INPUT_PARAMETER_DS_LABEL = "DATASET_LABEL";

	public static final String OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL = "serviceUrl";
	public static final String OUTPUT_PARAMETER_EXECUTION_ID = "executionId";
	public static final String OUTPUT_PARAMETER_DATASET_LABEL = "datasetLabel";
	public static final String OUTPUT_PARAMETER_DATASOURCE_LABEL = "datasourceLabel";

	public static final String OUTPUT_PARAMETER_DATASET_PARAMETERS = "datasetParameters";
	// public static final String OUTPUT_PARAMETER_TARGET = "TARGET";

	// logger component
	private static Logger logger = Logger.getLogger(SelectDatasetAction.class);

	@Override
	public void doService() {

		IDataSet dataset;
		String target = null;

		logger.debug("IN");

		try {
			// can be QBE or WORKSHEETs
			LogMF.trace(logger, "Reading input parametr [{0}] from request...", INPUT_PARAMETER_ENGINE);
			target = getAttributeAsString(INPUT_PARAMETER_ENGINE);
			LogMF.debug(logger, "Input parameter [{0}] is equal to [{1}]", INPUT_PARAMETER_ENGINE, target);
			Assert.assertNotNull(target, "Input parameter [" + INPUT_PARAMETER_ENGINE + "] cannot be null");

			String actionToCall = target == null || target.equals(WORKSHEET) ? WORKSHEET_EDIT_ACTION : QBE_EDIT_ACTION;

			// create the input parameters to pass to the WorkSheet Edit Service
			Map editActionParameters = buildEditServiceBaseParametersMap(actionToCall);

			String executionId = createNewExecutionId();
			editActionParameters.put("SBI_EXECUTION_ID", executionId);

			String typeCode = target == null || target.equals(WORKSHEET) ? SpagoBIConstants.WORKSHEET_TYPE_CODE : SpagoBIConstants.DATAMART_TYPE_CODE;
			Engine engineToCall = getEngine(typeCode);

			LogMF.debug(logger, "Engine label is equal to [{0}]", engineToCall.getLabel());

			// String defaultDatasourceLabel = getDatasourceLabel(engineToCall);
			// LogMF.debug(logger, "Default Datasource label is equal to [{0}]", defaultDatasourceLabel);
			// editActionParameters.put("datasource_label" , defaultDatasourceLabel);

			dataset = getDatasetToOpen();
			editActionParameters.put("dataset_label", dataset.getLabel());

			IDataSource datasource = dataset.getDataSource();
			if (target.equals(WORKSHEET)) {
				editActionParameters.put("datasource_label", datasource.getLabel());
				Engine worksheetEngine = getWorksheetEngine();
				int defEngineDataSource = 0;
				// worksheetEngine.getDataSourceId();
				editActionParameters.put("ENGINE_DATASOURCE_ID", defEngineDataSource);
			} else {
				editActionParameters.put("selected_datasource_label", datasource.getLabel());
				// add the data default datasource of teh engine
				Engine qbeEngine = getQbeEngine();
				int defEngineDataSource = 0;
				// qbeEngine.getDataSourceId();
				editActionParameters.put("ENGINE_DATASOURCE_ID", defEngineDataSource);
			}

			// IDataSource dataSource = getDatasourceToOpen(dataSourceId);

			// String parametersStrng = dataset.getParamsMap().getActiveDetail().getParameters();
			// Map<String, String> datasetParameterValuesMap = getDatasetParameterValuesMapFromString(parametersStrng);

			Map<String, String> datasetParameterValuesMap = dataset.getParamsMap() != null ? dataset.getParamsMap() : new HashMap<String, String>();

			if (dataset.getParamsMap() != null)
				editActionParameters.putAll(dataset.getParamsMap());
			else {
				editActionParameters.putAll(new HashMap<String, String>());

			}

			// create the WorkSheet Edit Service's URL
			String worksheetEditActionUrl = GeneralUtilities.getUrl(engineToCall.getUrl(), editActionParameters);
			LogMF.debug(logger, "Worksheet edit service invocation url is equal to [{}]", worksheetEditActionUrl);

			// create the dataset
			logger.trace("Creating the dataset...");
			Integer datasetId = null;
			try {

				datasetId = dataset.getId();
				Assert.assertNotNull(datasetId, "Dataset Id cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "An error occurred while creating dataset from bean [" + dataset + "]", t);
			}
			LogMF.debug(logger, "Datset [{0}]succesfully created with id [{1}]", dataset, datasetId);

			logger.trace("Copying output parameters to response...");
			try {
				getServiceResponse().setAttribute(OUTPUT_PARAMETER_EXECUTION_ID, executionId);
				getServiceResponse().setAttribute(OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL, worksheetEditActionUrl);
				getServiceResponse().setAttribute(OUTPUT_PARAMETER_DATASET_LABEL, dataset.getLabel());
				getServiceResponse().setAttribute(OUTPUT_PARAMETER_DATASOURCE_LABEL, datasource.getLabel());

				getServiceResponse().setAttribute(OUTPUT_PARAMETER_DATASET_PARAMETERS, datasetParameterValuesMap);

				getServiceResponse().setAttribute(INPUT_PARAMETER_ENGINE, target);

				// business metadata
				// AAA JSONObject businessMetadata = getBusinessMetadataFromRequest();
				// if(businessMetadata != null) {
				// getServiceResponse().setAttribute(OUTPUT_PARAMETER_BUSINESS_METADATA, businessMetadata.toString());
				// }
			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "An error occurred while creating dataset from bean [" + dataset + "]", t);
			}
			logger.trace("Output parameter succesfully copied to response");

		} finally {
			logger.debug("OUT");
		}
	}

	protected Engine getEngine(String typeCode) {
		Engine engine;
		List<Engine> engines;

		engine = null;
		try {
			Assert.assertNotNull(DAOFactory.getEngineDAO(), "EngineDao cannot be null");
			engines = DAOFactory.getEngineDAO().loadAllEnginesForBIObjectType(typeCode);
			if (engines == null || engines.size() == 0) {
				throw new SpagoBIServiceException(SERVICE_NAME, "There are no engines for documents of type [" + typeCode + "] available");
			} else {
				engine = engines.get(0);
				LogMF.warn(logger, "There are more than one engine for document of type [" + typeCode + "]. We will use the one whose label is equal to [{0}]",
						engine.getLabel());
			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to load a valid engine for document of type [" + typeCode + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return engine;
	}

	protected Map<String, String> buildEditServiceBaseParametersMap(String actionToCall) {
		HashMap<String, String> parametersMap = new HashMap<String, String>();

		parametersMap.put("ACTION_NAME", actionToCall);
		parametersMap.put("NEW_SESSION", "TRUE");

		parametersMap.put(SpagoBIConstants.SBI_CONTEXT, GeneralUtilities.getSpagoBiContext());
		parametersMap.put(SpagoBIConstants.SBI_HOST, GeneralUtilities.getSpagoBiHost());

		parametersMap.put(SpagoBIConstants.SBI_LANGUAGE, getLocale().getLanguage());
		parametersMap.put(SpagoBIConstants.SBI_COUNTRY, getLocale().getCountry());

		// if (!GeneralUtilities.isSSOEnabled()) {
		UserProfile userProfile = (UserProfile) getUserProfile();
		parametersMap.put(SsoServiceInterface.USER_ID, (String) userProfile.getUserUniqueIdentifier());
		// }

		return parametersMap;
	}

	protected String createNewExecutionId() {
		String executionId;

		logger.debug("IN");

		executionId = null;
		try {
			UUIDGenerator uuidGen = UUIDGenerator.getInstance();
			UUID uuidObj = uuidGen.generateTimeBasedUUID();
			executionId = uuidObj.toString();
			executionId = executionId.replaceAll("-", "");
		} catch (Throwable t) {

		} finally {
			logger.debug("OUT");
		}

		return executionId;
	}

	/**
	 * Get dataset from dataset label passed as parameter
	 *
	 * @return
	 */

	private IDataSet getDatasetToOpen() {
		IDataSet dataset;

		logger.debug("IN");

		dataset = null;
		String label = null;
		try {
			logger.trace("Reading from request attributes used to get dataset...");

			LogMF.trace(logger, "Reading input parametr [{0}] from request...", INPUT_PARAMETER_DS_LABEL);
			label = getAttributeAsString(INPUT_PARAMETER_DS_LABEL);
			LogMF.debug(logger, "Input parameter [{0}] is equal to [{1}]", INPUT_PARAMETER_DS_LABEL, label);
			Assert.assertNotNull(label, "Input parameter [" + INPUT_PARAMETER_DS_LABEL + "] cannot be null");

			IDataSetDAO dsDao = DAOFactory.getDataSetDAO();

			dataset = dsDao.loadDataSetByLabel(label);
			Assert.assertNotNull(dataset, "Dataset with label [" + label + "] not found");

		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to read from request parameter required to retrieve dataset", t);
		} finally {
			logger.debug("OUT");
		}

		return dataset;
	}

}
