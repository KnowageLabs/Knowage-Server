package it.eng.spagobi.engines.datamining.common;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngine;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.compute.DataMiningPythonExecutor;
import it.eng.spagobi.engines.datamining.compute.DataMiningRExecutor;
import it.eng.spagobi.engines.datamining.compute.IDataMiningExecutor;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplate;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.security.hmacfilter.HMACSecurityException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import it.eng.spagobi.utilities.rest.RestUtilities.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

public class FunctionExecutor {

	static protected Logger logger = Logger.getLogger(FunctionExecutor.class);

	private static List<DataMiningResult> executeDataminingInstance(DataMiningEngineInstance dataminingEngineInstance, UserProfile userProfile) {
		List<DataMiningCommand> commands = dataminingEngineInstance.getCommands();
		HashMap params = (HashMap) dataminingEngineInstance.getAnalyticalDrivers();
		IDataMiningExecutor executor = null;
		DataMiningResult result = null;
		List<DataMiningResult> results = new ArrayList<DataMiningResult>();
		if (dataminingEngineInstance.getLanguage().equals("Python")) {
			executor = new DataMiningPythonExecutor(dataminingEngineInstance, userProfile);
		} else if (dataminingEngineInstance.getLanguage().equals("R")) {
			executor = new DataMiningRExecutor(dataminingEngineInstance, userProfile);
		}

		try {

			for (DataMiningCommand c : commands) {
				List<Output> outputs = c.getOutputs();
				// setta ambiente
				result = executor.setExecEnvironment(logger, result, params, c, userProfile, false, "function_catalog");

				// esegui script
				for (Output o : outputs) {

					// esegui output
					if (c.getExecuted() == null) {
						c.setExecuted(false);
					}
					// result = executor.execute(params, c, o, userProfile, c.getExecuted(), "function_catalog"); // c.getExecuted() prima era true
					result = executor.executeScript(logger, result, params, c, o, userProfile, c.getExecuted(), "function_catalog"); // c.getExecuted() prima
																																		// era true
					results.add(result);

				}

				// unset ambiente
				result = executor.unsetExecEnvironment(logger, result, params, c, userProfile, false, "function_catalog");
			}

		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error adding dataminingengine execution results", e);
		}
		return results;
	}

	private static JSONArray getRemoteServiceResponse(String url, String requestBody) throws HttpException, IOException, JSONException, HMACSecurityException {
		Map<String, String> requestHeaders = new HashMap<String, String>(2);
		String[][] headers = new String[][] { { "Accept", "application/json" }, { "Content-Type", "application/json" } };
		for (String[] header : headers) {
			if (!requestHeaders.containsKey(header[0])) {
				requestHeaders.put(header[0], header[1]);
			}
		}
		Response response = RestUtilities.makeRequest(HttpMethod.Post, url, requestHeaders, requestBody, null);
		String responseBody = response.getResponseBody();
		if (response.getStatusCode() != HttpStatus.SC_OK) {
			throw new SpagoBIRuntimeException(String.format("The response status is not ok: status=%d, response=%s", response.getStatusCode(), responseBody));
		} else {
			return new JSONArray(responseBody);
		}
	}

	public static String execute(String body, SbiCatalogFunction function, UserProfile userProfile, Map env) {
		logger.debug("IN");
		JSONArray serviceResponse = new JSONArray();
		try {
			logger.debug("Checking if the function is remotely located...");
			if (function.isRemote()) {
				logger.debug("The function is remotely located... Request of execution will be forwarded to [" + function.getUrl() + "]");
				if (body == null || body.isEmpty()) {
					logger.debug("Remote request without user data. Building body by using sample data...");
					body = FunctionExecutionUtils.getBodyRequestForRemoteExecution(function, userProfile);
				}
				FunctionExecutionUtils.adjustBodyContentsForRemoteExecution(body);
				JSONArray remoteResponse = getRemoteServiceResponse(function.getUrl(), body);
				if (remoteResponse != null && FunctionExecutionUtils.isResponseCompliant(function, remoteResponse)) {
					serviceResponse = remoteResponse;
				} else {
					throw new SpagoBIRuntimeException(
							"The remote service response is not compliant with the expected format. Please contact the remote function supplier.");
				}
			} else {
				boolean replacing = false;
				Map<String, Map<String, String>> fileMap = new HashMap<String, Map<String, String>>();
				Map<String, String> datasetMap = new HashMap<String, String>();
				Map<String, String> variablesMap = new HashMap<String, String>();
				if (body != null && !body.isEmpty()) {
					logger.debug("Request with user provided data [" + body + "]");
					fileMap = FunctionExecutionUtils.getReplacementsFilesMap(body);
					datasetMap = FunctionExecutionUtils.getReplacementsDatasetMap(body);
					variablesMap = FunctionExecutionUtils.getReplacementsVariablesMap(body);
					FunctionExecutionUtils.substituteWithReplacingValues(function, variablesMap, datasetMap, fileMap);
					replacing = true;
				}
				logger.debug("Creating engine instance ...");
				DataMiningTemplate template = FunctionExecutionUtils.getDataMiningTemplate(function);
				if (replacing == true) {
					List<DataMiningDataset> datasets = template.getDatasets();
					for (DataMiningDataset d : datasets) {
						String wrongLabel = d.getSpagobiLabel();
						String labelForSubstitution = getKeyByValue(datasetMap, wrongLabel);
						if (labelForSubstitution != null) {
							d.setSubstituteLabel(labelForSubstitution);
						}
					}
				}
				DataMiningEngineInstance dataMiningEngineInstance = DataMiningEngine.createInstance(template, env);
				logger.debug("Engine instance succesfully created");

				List<DataMiningResult> dataminingExecutionResults = executeDataminingInstance(dataMiningEngineInstance, userProfile);
				serviceResponse = FunctionExecutionUtils.buildDataminingResponse(dataminingExecutionResults);
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error starting the Data Mining engine or getting datamining engine execution results!", e);
		} finally {
			logger.debug("OUT");
		}
		return serviceResponse.toString();
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
}
