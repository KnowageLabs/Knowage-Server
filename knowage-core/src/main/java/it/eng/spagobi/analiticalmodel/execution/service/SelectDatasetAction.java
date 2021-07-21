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
package it.eng.spagobi.analiticalmodel.execution.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * Action that gets a dataset id and prepare the url to call (depending on TARGET parameter value)
 *
 *
 * 1) the Qbe Engine (QBE_ENGINE_FROM_DATASET_START_ACTION)
 *
 *
 *
 * @author Giulio Gavardi
 */
public class SelectDatasetAction extends ExecuteDocumentAction {

	private static final long serialVersionUID = 1L;

	public static final String QBE_EDIT_ACTION = "QBE_ENGINE_FROM_DATASET_START_ACTION";

	/** parameter that ecides action target */
	public static final String INPUT_PARAMETER_ENGINE = "ENGINE";
	public static final String QBE = "QBE";

	/** label f dataset to open */
	public static final String INPUT_PARAMETER_DS_LABEL = "DATASET_LABEL";

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
			// can be QBE
			LogMF.trace(logger, "Reading input parametr [{0}] from request...", INPUT_PARAMETER_ENGINE);
			target = getAttributeAsString(INPUT_PARAMETER_ENGINE);
			LogMF.debug(logger, "Input parameter [{0}] is equal to [{1}]", INPUT_PARAMETER_ENGINE, target);
			Assert.assertNotNull(target, "Input parameter [" + INPUT_PARAMETER_ENGINE + "] cannot be null");

			String actionToCall = QBE_EDIT_ACTION;

			Map editActionParameters = buildEditServiceBaseParametersMap(actionToCall);

			String executionId = createNewExecutionId();
			editActionParameters.put("SBI_EXECUTION_ID", executionId);

			String typeCode = SpagoBIConstants.DATAMART_TYPE_CODE;
			Engine engineToCall = getEngine(typeCode);

			LogMF.debug(logger, "Engine label is equal to [{0}]", engineToCall.getLabel());

			// String defaultDatasourceLabel = getDatasourceLabel(engineToCall);
			// LogMF.debug(logger, "Default Datasource label is equal to [{0}]", defaultDatasourceLabel);
			// editActionParameters.put("datasource_label" , defaultDatasourceLabel);

			dataset = getDatasetToOpen();
			editActionParameters.put("dataset_label", dataset.getLabel());

			IDataSource datasource = dataset.getDataSource();

			editActionParameters.put("selected_datasource_label", datasource.getLabel());
			// add the data default datasource of teh engine
			Engine qbeEngine = getQbeEngine();
			int defEngineDataSource = 0;
			// qbeEngine.getDataSourceId();
			editActionParameters.put("ENGINE_DATASOURCE_ID", defEngineDataSource);

			// IDataSource dataSource = getDatasourceToOpen(dataSourceId);

			// String parametersStrng = dataset.getParamsMap().getActiveDetail().getParameters();
			// Map<String, String> datasetParameterValuesMap = getDatasetParameterValuesMapFromString(parametersStrng);

			Map<String, String> datasetParameterValuesMap = dataset.getParamsMap() != null ? dataset.getParamsMap() : new HashMap<String, String>();

			if (dataset.getParamsMap() != null)
				editActionParameters.putAll(dataset.getParamsMap());
			else {
				editActionParameters.putAll(new HashMap<String, String>());

			}
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

		Locale locale = getLocale();

		parametersMap.put(SpagoBIConstants.SBI_LANGUAGE, locale.getLanguage());
		parametersMap.put(SpagoBIConstants.SBI_COUNTRY, locale.getCountry());

		if (StringUtils.isNotBlank(locale.getScript())) {
			parametersMap.put(SpagoBIConstants.SBI_SCRIPT, locale.getScript());
		}

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

	private Engine getEngineByDocumentType(String type) {
		Engine engine;
		List<Engine> engines;

		engine = null;
		try {
			Assert.assertNotNull(DAOFactory.getEngineDAO(), "EngineDao cannot be null");
			engines = DAOFactory.getEngineDAO().loadAllEnginesForBIObjectType(type);
			if (engines == null || engines.size() == 0) {
				throw new SpagoBIServiceException(SERVICE_NAME, "There are no engines for documents of type [" + type + "] available");
			} else {
				engine = engines.get(0);
				if (engines.size() > 1) {
					LogMF.warn(logger, "There are more than one engine for document of type [" + type + "]. We will use the one whose label is equal to [{0}]",
							engine.getLabel());
				} else {
					LogMF.debug(logger, "Using engine with label [{0}]", engine.getLabel());
				}
			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to load a valid engine for document of type [" + type + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return engine;
	}

	private Engine getQbeEngine() {
		return getEngineByDocumentType(SpagoBIConstants.DATAMART_TYPE_CODE);
	}
}
