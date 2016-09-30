package it.eng.spagobi.engines.datamining.common;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.engines.datamining.DataMiningEngine;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.compute.DataMiningPythonExecutor;
import it.eng.spagobi.engines.datamining.compute.DataMiningRExecutor;
import it.eng.spagobi.engines.datamining.compute.IDataMiningExecutor;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplate;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;
import it.eng.spagobi.utilities.rest.RestUtilities.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

		for (DataMiningCommand c : commands) {
			List<Output> outputs = c.getOutputs();
			for (Output o : outputs) {
				try {
					result = executor.execute(params, c, o, userProfile, true, "function_catalog");
					results.add(result);
				} catch (Exception e) {
					throw new SpagoBIRuntimeException("Error adding dataminingengine execution results", e);
				}
			}
		}
		return results;
	}

	private static JSONArray getRemoteServiceResponse(SbiCatalogFunction function) throws HttpException, IOException, JSONException {
		Map<String, String> requestHeaders = new HashMap<String, String>(2);
		String[][] headers = new String[][] { { "Accept", "application/json" }, { "Content-Type", "application/json" } };
		for (String[] header : headers) {
			if (!requestHeaders.containsKey(header[0])) {
				requestHeaders.put(header[0], header[1]);
			}
		}
		String requestBody = FunctionExecutionUtils.getRequestBody(function);
		Response response = RestUtilities.makeRequest(HttpMethod.Post, function.getUrl(), requestHeaders, requestBody, null);
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
			if (body != null && !body.isEmpty()) {
				logger.debug("Request with user provided data [" + body + "]");
				FunctionExecutionUtils.substituteWithReplacingValues(function, body);
			}

			logger.debug("Checking if the function is remotely located...");
			if (function.isRemote()) {
				logger.debug("The function is remotely located... Request of execution will be forwarded to [" + function.getUrl() + "]");
				JSONArray remoteResponse = getRemoteServiceResponse(function);
				if (remoteResponse != null && FunctionExecutionUtils.isResponseCompliant(function, remoteResponse)) {
					serviceResponse = remoteResponse;
				} else {
					throw new SpagoBIRuntimeException(
							"The remote service response is not compliant with the expected format. Please contact the remote function supplier.");
				}
			} else {
				logger.debug("Creating engine instance ...");
				DataMiningTemplate template = FunctionExecutionUtils.getDataMiningTemplate(function);
				DataMiningEngineInstance dataMiningEngineInstance = DataMiningEngine.createInstance(template, env);
				logger.debug("Engine instance succesfully created");

				List<DataMiningResult> dataminingExecutionResults = executeDataminingInstance(dataMiningEngineInstance, userProfile);
				serviceResponse = FunctionExecutionUtils.buildDataminingResponse(dataminingExecutionResults);
			}
		} catch (Exception e) {
			logger.error("Error starting the Data Mining engine or getting datamining engine execution results!", e);
			throw new SpagoBIRuntimeException("Error starting the Data Mining engine or getting datamining engine execution results!", e);
		} finally {
			logger.debug("OUT");
		}
		return serviceResponse.toString();
	}
}
