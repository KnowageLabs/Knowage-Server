/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.execution.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.tools.dataset.DatasetManagementAPI;
import it.eng.spagobi.tools.dataset.bo.CustomDataSet;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.constants.DataSetConstants;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * Action that create a new dataset and prepare the url to call the worksheeet's edit service (WORKSHEET_WITH_DATASET_START_EDIT_ACTION) in order to build a
 * worksheet document on the brandnew dataset
 *
 * @author Giulio Gavardi, Andrea Gioia (andrea.gioia@eng.it)
 */
public class CreateDatasetForWorksheetAction extends ExecuteDocumentAction {

	private static final long serialVersionUID = 1L;

	public static final String WORKSHEET_EDIT_ACTION = "WORKSHEET_WITH_DATASET_START_EDIT_ACTION";

	public static final String INPUT_PARAMETER_DS_LABEL = DataSetConstants.LABEL;
	public static final String INPUT_PARAMETER_DS_NAME = DataSetConstants.NAME;
	public static final String INPUT_PARAMETER_DS_DESCRIPTION = DataSetConstants.DESCRIPTION;
	public static final String INPUT_PARAMETER_DS_CUSTOM_DATA = DataSetConstants.CUSTOM_DATA;
	public static final String INPUT_PARAMETER_DS_JCLASS_NAME = DataSetConstants.JCLASS_NAME;
	public static final String INPUT_PARAMETER_DS_METADATA = DataSetConstants.DS_METADATA;

	public static final String JCLASS_NAME = "jClassName";
	public static final String CUSTOM_DATA = "customData";

	public static final String INPUT_PARAMETER_DS_PARAMETER_DEFINITION = "parametersDefinition";
	public static final String INPUT_PARAMETER_DS_PARAMETERS_VALUES = "parametersValues";
	public static final String INPUT_PARAMETER_BUSINESS_METADATA = "businessMetadata";

	public static final String OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL = "serviceUrl";
	public static final String OUTPUT_PARAMETER_EXECUTION_ID = "executionId";
	public static final String OUTPUT_PARAMETER_DATASET_LABEL = "datasetLabel";
	public static final String OUTPUT_PARAMETER_DATASET_PARAMETERS = "datasetParameters";
	public static final String OUTPUT_PARAMETER_BUSINESS_METADATA = "businessMetadata";

	// logger component
	private static Logger logger = Logger.getLogger(CreateDatasetForWorksheetAction.class);

	@Override
	public void doService() {

		DatasetManagementAPI creationUtilities;
		IDataSet datasetBean;

		logger.debug("IN");

		try {

			// create the input parameters to pass to the WorkSheet Edit Service
			Map worksheetEditActionParameters = buildWorksheetEditServiceBaseParametersMap();

			String executionId = ExecuteAdHocUtility.createNewExecutionId();
			worksheetEditActionParameters.put("SBI_EXECUTION_ID", executionId);

			Engine worksheetEngine = getWorksheetEngine();
			LogMF.debug(logger, "Engine label is equal to [{0}]", worksheetEngine.getLabel());

			IDataSource datasource;
			try {
				datasource = DAOFactory.getDataSourceDAO().loadDataSourceWriteDefault();
			} catch (EMFUserError e) {
				throw new SpagoBIRuntimeException("Error while loading default datasource for writing", e);
			}
			if (datasource != null) {
				LogMF.debug(logger, "Datasource label is equal to [{0}]", datasource.getLabel());
				worksheetEditActionParameters.put(EngineConstants.DEFAULT_DATASOURCE_FOR_WRITING_LABEL, datasource.getLabel());
			} else {
				logger.debug("There is no default datasource for writing");
			}

			datasetBean = getDatasetAttributesFromRequest();
			worksheetEditActionParameters.put("dataset_label", datasetBean.getLabel());

			Map<String, String> datasetParameterValuesMap = getDatasetParameterValuesMapFromRequest();
			worksheetEditActionParameters.putAll(datasetParameterValuesMap);

			// create the WorkSheet Edit Service's URL
			String worksheetEditActionUrl = GeneralUtilities.getUrl(worksheetEngine.getUrl(), worksheetEditActionParameters);
			LogMF.debug(logger, "Worksheet edit service invocation url is equal to [{}]", worksheetEditActionUrl);

			// create the dataset
			logger.trace("Creating the dataset...");
			Integer datasetId = null;
			try {
				creationUtilities = new DatasetManagementAPI();
				datasetId = creationUtilities.creatDataSet(datasetBean);
				Assert.assertNotNull(datasetId, "Dataset Id cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "An error occurred while creating dataset from bean [" + datasetBean + "]", t);
			}
			LogMF.debug(logger, "Datset [{0}]succesfully created with id [{1}]", datasetBean, datasetId);

			logger.trace("Copying output parameters to response...");
			try {
				getServiceResponse().setAttribute(OUTPUT_PARAMETER_EXECUTION_ID, executionId);
				getServiceResponse().setAttribute(OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL, worksheetEditActionUrl);
				getServiceResponse().setAttribute(OUTPUT_PARAMETER_DATASET_LABEL, datasetBean.getLabel());
				getServiceResponse().setAttribute(OUTPUT_PARAMETER_DATASET_PARAMETERS, datasetParameterValuesMap);

				// business metadata
				JSONObject businessMetadata = getBusinessMetadataFromRequest();
				if (businessMetadata != null) {
					getServiceResponse().setAttribute(OUTPUT_PARAMETER_BUSINESS_METADATA, businessMetadata.toString());
				}
			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "An error occurred while creating dataset from bean [" + datasetBean + "]", t);
			}
			logger.trace("Output parameter succesfully copied to response");

		} finally {
			logger.debug("OUT");
		}
	}

	protected Engine getEngineByDocumentType(String type) {
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
					LogMF.warn(logger, "There are more than one engine for document of type [WORKSHEET]. We will use the one whose label is equal to [{0}]",
							engine.getLabel());
				} else {
					LogMF.debug(logger, "Using worksheet engine with label [{0}]", engine.getLabel());
				}
			}
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to load a valid engine for document of type [WORKSHEET]", t);
		} finally {
			logger.debug("OUT");
		}

		return engine;
	}

	protected Engine getWorksheetEngine() {
		return getEngineByDocumentType(SpagoBIConstants.WORKSHEET_TYPE_CODE);
	}

	protected Engine getQbeEngine() {
		return getEngineByDocumentType(SpagoBIConstants.DATAMART_TYPE_CODE);
	}

	protected Map<String, String> buildWorksheetEditServiceBaseParametersMap() {
		HashMap<String, String> parametersMap = new HashMap<String, String>();

		parametersMap.put("ACTION_NAME", WORKSHEET_EDIT_ACTION);
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

	// protected String createNewExecutionId() {
	// String executionId;
	//
	// logger.debug("IN");
	//
	// executionId = null;
	// try {
	// UUIDGenerator uuidGen = UUIDGenerator.getInstance();
	// UUID uuidObj = uuidGen.generateTimeBasedUUID();
	// executionId = uuidObj.toString();
	// executionId = executionId.replaceAll("-", "");
	// } catch(Throwable t) {
	//
	// } finally {
	// logger.debug("OUT");
	// }
	//
	// return executionId;
	// }

	/**
	 * Read dataset's attributes from request and save them in a bean object.
	 *
	 * @return GuiGenericDataSet the bean object that collect all the dataset attributes read from request.
	 */
	private IDataSet getDatasetAttributesFromRequest() {

		IDataSet datasetBean;

		String label;
		String name;
		String description;
		String customData;
		String jClassName;
		String metadata;
		String parametersDefinitionJson;
		String parametersDefinitionXML;

		logger.debug("IN");

		datasetBean = null;

		try {
			logger.trace("Reading from request attributes used to build the new dataset...");

			LogMF.trace(logger, "Reading input parametr [{0}] from request...", INPUT_PARAMETER_DS_LABEL);
			label = getAttributeAsString(INPUT_PARAMETER_DS_LABEL);
			LogMF.debug(logger, "Input parameter [{0}] is equal to [{1}]", INPUT_PARAMETER_DS_LABEL, label);
			Assert.assertNotNull(label, "Input parameter [" + INPUT_PARAMETER_DS_LABEL + "] cannot be null");

			LogMF.trace(logger, "Reading input parametr [{0}] from request...", INPUT_PARAMETER_DS_NAME);
			name = getAttributeAsString(INPUT_PARAMETER_DS_NAME);
			LogMF.debug(logger, "Input parameter [{0}] is equal to [{1}]", INPUT_PARAMETER_DS_LABEL, name);

			LogMF.trace(logger, "Reading input parametr [{0}] from request...", INPUT_PARAMETER_DS_DESCRIPTION);
			description = getAttributeAsString(INPUT_PARAMETER_DS_DESCRIPTION);
			LogMF.debug(logger, "Input parameter [{0}] is equal to [{1}]", INPUT_PARAMETER_DS_DESCRIPTION, description);

			LogMF.trace(logger, "Reading input parametr [{0}] from request...", INPUT_PARAMETER_DS_CUSTOM_DATA);
			customData = getAttributeAsString(INPUT_PARAMETER_DS_CUSTOM_DATA);
			LogMF.debug(logger, "Input parameter [{0}] is equal to [{1}]", INPUT_PARAMETER_DS_CUSTOM_DATA, customData);

			LogMF.trace(logger, "Reading input parametr [{0}] from request...", INPUT_PARAMETER_DS_JCLASS_NAME);
			jClassName = getAttributeAsString(INPUT_PARAMETER_DS_JCLASS_NAME);
			LogMF.debug(logger, "Input parameter [{0}] is equal to [{1}]", INPUT_PARAMETER_DS_JCLASS_NAME, jClassName);

			LogMF.trace(logger, "Reading input parametr [{0}] from request...", INPUT_PARAMETER_DS_METADATA);
			metadata = getAttributeAsString(INPUT_PARAMETER_DS_METADATA);
			LogMF.debug(logger, "Input parameter [{0}] is equal to [{1}]", INPUT_PARAMETER_DS_METADATA, metadata);

			LogMF.trace(logger, "Reading input parametr [{0}] from request...", INPUT_PARAMETER_DS_PARAMETER_DEFINITION);
			parametersDefinitionJson = getAttributeAsString(INPUT_PARAMETER_DS_PARAMETER_DEFINITION);
			LogMF.debug(logger, "Input parameter [{0}] is equal to [{1}]", INPUT_PARAMETER_DS_METADATA, parametersDefinitionJson);

			parametersDefinitionXML = null;
			if (!StringUtilities.isEmpty(parametersDefinitionJson)) {
				LogMF.trace(logger, "Coverting input parameter [{0}] from JSON format to XML format ...", INPUT_PARAMETER_DS_PARAMETER_DEFINITION);
				parametersDefinitionXML = parametersJsonToXML(parametersDefinitionJson);
				LogMF.trace(logger, "Input parameter [{0}] succesfully converted to xml formt [{1}]", INPUT_PARAMETER_DS_PARAMETER_DEFINITION,
						parametersDefinitionXML);
			}

			logger.trace("Attributes used to build the new dataset succesfully read from request");

			logger.trace("Building the dataset bean...");

			datasetBean = new CustomDataSet();
			datasetBean.setLabel(label);
			datasetBean.setName(name);
			datasetBean.setDescription(description);

			((CustomDataSet) datasetBean).setCustomData(customData);
			((CustomDataSet) datasetBean).setJavaClassName(jClassName);
			datasetBean.setParameters(parametersDefinitionXML);
			datasetBean.setDsType(DataSetConstants.DS_CUSTOM);
			if (!StringUtilities.isEmpty(metadata)) {
				datasetBean.setDsMetadata(metadata);
			}

			try {
				JSONObject jsonConf = new JSONObject();
				jsonConf.put(CUSTOM_DATA, customData);
				jsonConf.put(JCLASS_NAME, jClassName);
				jsonConf.put(DataSetConstants.PARS, parametersDefinitionXML);
				jsonConf.put(DataSetConstants.DS_CUSTOM, DataSetConstants.DS_CUSTOM);
				jsonConf.put(DataSetConstants.DS_TYPE_CD, DataSetConstants.DS_CUSTOM);
				datasetBean.setConfiguration(jsonConf.toString());
			} catch (Exception e) {
				logger.error("Error while defining dataset configuration.  Error: " + e.getMessage());
			}

			logger.trace("Dataset bean succesfully built");

		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to read from request all parameters required to create the new dataset", t);
		} finally {
			logger.debug("OUT");
		}

		return datasetBean;
	}

	private Map<String, String> getDatasetParameterValuesMapFromRequest() {
		Map<String, String> parametersMap;

		parametersMap = new HashMap<String, String>();

		try {
			if (requestContainsAttribute(INPUT_PARAMETER_DS_PARAMETERS_VALUES)) {
				logger.trace("Reading input parametr [" + INPUT_PARAMETER_DS_PARAMETERS_VALUES + "] from request...");
				JSONObject parameterValues = this.getAttributeAsJSONObject(INPUT_PARAMETER_DS_PARAMETERS_VALUES);
				logger.debug("Input parameter [" + INPUT_PARAMETER_DS_PARAMETERS_VALUES + "] is equal to [" + parameterValues + "]");

				for (Iterator<String> iterator = parameterValues.keys(); iterator.hasNext();) {
					String key = iterator.next();
					String value = parameterValues.get(key) != null ? parameterValues.get(key).toString() : null;
					parametersMap.put(key, value);
				}
			} else {
				LogMF.trace(logger, "Input parameter [{0}] not valorized", INPUT_PARAMETER_DS_PARAMETERS_VALUES);
			}
		} catch (Throwable t) {
			logger.error("Impossible to parse input parameter [" + INPUT_PARAMETER_DS_PARAMETERS_VALUES + "]", t);
		}

		return parametersMap;
	}

	private JSONObject getBusinessMetadataFromRequest() {
		JSONObject businessMetadata;

		businessMetadata = null;
		try {
			if (requestContainsAttribute(INPUT_PARAMETER_BUSINESS_METADATA)) {
				logger.trace("Reading input parametr [" + INPUT_PARAMETER_BUSINESS_METADATA + "] from request...");
				businessMetadata = this.getAttributeAsJSONObject(INPUT_PARAMETER_BUSINESS_METADATA);
				logger.debug("Input parameter [" + INPUT_PARAMETER_BUSINESS_METADATA + "] is equal to [" + businessMetadata + "]");
			} else {
				LogMF.trace(logger, "Input parameter [{0}] not valorized", INPUT_PARAMETER_BUSINESS_METADATA);
			}
		} catch (Throwable t) {
			logger.error("Impossible to parse input parameter [" + INPUT_PARAMETER_BUSINESS_METADATA + "]", t);
		}

		return businessMetadata;
	}

	private String parametersJsonToXML(String parsJson) {
		String xml = null;
		SourceBean sb = null;

		try {
			JSONObject json = new JSONObject(parsJson);
			sb = new SourceBean("PARAMETERSLIST");
			SourceBean sb1 = new SourceBean("ROWS");
			for (Iterator iterator = json.keys(); iterator.hasNext();) {
				String key = (String) iterator.next();
				String t = json.getString(key);
				SourceBean b = new SourceBean("ROW");
				b.setAttribute("NAME", key);
				b.setAttribute("TYPE", t);
				sb1.setAttribute(b);
			}

			sb.setAttribute(sb1);
			xml = sb.toXML(false);
		} catch (Exception e) {
			logger.error("error in parsing " + parsJson, e);
		}
		Assert.assertNotNull(xml, "There was an error in parsing " + parsJson);

		return xml;

	}
}
