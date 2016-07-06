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
package it.eng.spagobi.engines.datamining.common;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.datamining.DataMiningEngine;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplate;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplateParseException;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/")
public class DataMiningEngineStartAction extends AbstractDataMiningEngineService {

	// http://localhost:8080/SpagoBIDataMiningEngine/restful-services/start

	// INPUT PARAMETERS
	public static final String LANGUAGE = "SBI_LANGUAGE";
	public static final String COUNTRY = "SBI_COUNTRY";

	// OUTPUT PARAMETERS

	// SESSION PARAMETRES
	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;
	public static final String STARTUP_ERROR = EngineConstants.STARTUP_ERROR;

	// Defaults
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(DataMiningEngineStartAction.class);

	private static final String SUCCESS_REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/DataMining.jsp";
	private static final String FAILURE_REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/errors/startupError.jsp";

	@GET
	@Path("/start")
	@Produces("text/html")
	public void startAction(@Context HttpServletResponse response) {

		logger.debug("IN");

		try {
			SourceBean templateBean = getTemplateAsSourceBean();
			logger.debug("User Id: " + getUserId());
			logger.debug("Audit Id: " + getAuditId());
			logger.debug("Document Id: " + getDocumentId());
			logger.debug("Template: " + templateBean);

			if (getAuditServiceProxy() != null) {
				logger.debug("Audit enabled: [TRUE]");
				getAuditServiceProxy().notifyServiceStartEvent();
			} else {
				logger.debug("Audit enabled: [FALSE]");
			}

			DataMiningEngineInstance dataMiningEngineInstance = null;

			logger.debug("Creating engine instance ...");

			try {
				dataMiningEngineInstance = DataMiningEngine.createInstance(templateBean, getEnv());
			} catch (DataMiningTemplateParseException e) {
				SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Template not valid", e);
				engineException.setDescription(e.getCause().getMessage());
				engineException.addHint("Check the document's template");
				throw engineException;
			} catch (SpagoBIEngineRuntimeException e) {
				throw e;
			} catch (Exception e) {
				logger.error("Error starting the Data Mining engine: error while generating the engine instance.", e);
				throw new SpagoBIEngineRuntimeException("Error starting the Data Mining engine: error while generating the engine instance.", e);
			}
			logger.debug("Engine instance succesfully created");

			getExecutionSession().setAttributeInSession(ENGINE_INSTANCE, dataMiningEngineInstance);
			// getExecutionSession().setAttributeInSession(EngineConstants.DOCUMENT_ID, getDocumentId());
			getExecutionSession().setAttributeInSession(EngineConstants.ENV_DOCUMENT_LABEL, getDocumentLabel());
			try {
				// To deploy into JBOSSEAP64 is needed a StandardWrapper, instead of RestEasy Wrapper
				servletRequest = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
				response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);
				servletRequest.getRequestDispatcher(SUCCESS_REQUEST_DISPATCHER_URL).forward(servletRequest, response);

			} catch (Exception e) {
				logger.error("Error starting the Data Mining engine: error while forwarding the execution to the jsp " + SUCCESS_REQUEST_DISPATCHER_URL, e);
				throw new SpagoBIEngineRuntimeException("Error starting the Data Mining engine: error while forwarding the execution to the jsp "
						+ SUCCESS_REQUEST_DISPATCHER_URL, e);
			}

			if (getAuditServiceProxy() != null) {
				getAuditServiceProxy().notifyServiceEndEvent();
			}

		} catch (Exception e) {
			logger.error("Error starting the Data Mining engine", e);
			if (getAuditServiceProxy() != null) {
				getAuditServiceProxy().notifyServiceErrorEvent(e.getMessage());
			}

			SpagoBIEngineStartupException serviceException = this.getWrappedException(e);

			getExecutionSession().setAttributeInSession(STARTUP_ERROR, serviceException);
			try {
				// To deploy into JBOSSEAP64 is needed a StandardWrapper, instead of RestEasy Wrapper
				servletRequest = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
				response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);

				servletRequest.getRequestDispatcher(FAILURE_REQUEST_DISPATCHER_URL).forward(servletRequest, response);
			} catch (Exception ex) {
				logger.error("Error starting the Data Mining engine: error while forwarding the execution to the jsp " + FAILURE_REQUEST_DISPATCHER_URL, ex);
				throw new SpagoBIEngineRuntimeException("Error starting the Data Mining engine: error while forwarding the execution to the jsp "
						+ FAILURE_REQUEST_DISPATCHER_URL, ex);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	@GET
	@Path("/executeFunction/{functionId}")
	@Produces("application/json")
	public String executeCatalogFunction(@PathParam("functionId") int functionId, @Context HttpServletResponse response) {
		logger.debug("IN");
		SbiCatalogFunction function = null;
		DataMiningEngineInstance dataMiningEngineInstance = null;
		JSONArray serviceResponse = new JSONArray();

		logger.debug("Creating engine instance ...");

		try {

			DataMiningTemplate template = FunctionExecutionUtils.initializeTemplateByFunctionId(functionId);

			// -----------------

			try {
				Map env = getEnv();
				dataMiningEngineInstance = DataMiningEngine.createInstance(template, env);
			} catch (DataMiningTemplateParseException e) {
				SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Template not valid", e);
				engineException.setDescription(e.getCause().getMessage());
				engineException.addHint("Check the document's template");
				throw engineException;
			} catch (SpagoBIEngineRuntimeException e) {
				throw new SpagoBIEngineRuntimeException("Error starting the Data Mining engine: error while generating the engine instance.", e);
			} catch (Exception e) {
				logger.error("Error starting the Data Mining engine: error while generating the engine instance.", e);
				throw new Exception("Error starting the Data Mining engine: error while generating the engine instance.", e);
			}
			logger.debug("Engine instance succesfully created");

			try {
				List<DataMiningResult> dataminingExecutionResults = FunctionExecutor.executeCatalogFunction(dataMiningEngineInstance, getUserProfile());
				serviceResponse = new JSONArray();
				for (DataMiningResult r : dataminingExecutionResults) {
					JSONObject o = new JSONObject();
					o.put("resultType", r.getOutputType());
					o.put("result", r.getResult());
					if (r.getOutputType().equalsIgnoreCase("Image")) {
						o.put("resultName", r.getPlotName());
					} else { // Dataset Output or Text Output
						o.put("resultName", r.getVariablename());
					}

					serviceResponse.put(o);
				}

			} catch (Exception e) {

				logger.error("Error getting datamining engine execution results!", e);
				throw new SpagoBIEngineRuntimeException("Error getting datamining engine execution results!", e);
			}
		} catch (Exception e) {
			logger.error("Error creating or starting the Data Mining engine, or problems getting execution results!", e);
			throw new SpagoBIEngineRuntimeException("Error creating or starting the Data Mining engine, or problems getting execution results!", e);
		} finally {
			logger.debug("OUT");
		}
		return serviceResponse.toString();

	}

	@POST
	@Path("/executeFunctionWithNewData/{functionId}")
	@Produces("application/json")
	public String executeFunctionWithNewData(String body, @PathParam("functionId") int functionId, @Context HttpServletResponse response) {

		logger.debug("IN");
		SbiCatalogFunction function = null;
		DataMiningEngineInstance dataMiningEngineInstance = null;
		JSONArray serviceResponse = new JSONArray();
		String replacementType = null;

		Map<String, String> variablesInMap = null;
		Map<String, String> datasetsInMap = null;
		Map<String, String> datasetsOutMap = null;
		Map<String, String> textOutMap = null;
		Map<String, String> imageOutMap = null;

		ObjectMapper objMap = new ObjectMapper();

		// Example received JSON:
		// [{"type":"variablesIn","items":{"b":"2","a":"1"}},{"type":"datasetsIn","items":{"df":..*..}},{"type":"datasetsOut","items":{"datasetOut":"datasetOut"}},{"type":"textOut","items":{}},{"type":"imageOut","items":{"res":"res"}}]
		// *="{\"id\":{\"dsId\":6,\"versionNum\":2,\"organization\":\"DEFAULT_TENANT\"},\"name\":\"df\",\"description\":\"df\",\"label\":\"df\",\"active\":true,\"type\":\"SbiFileDataSet\",\"configuration\":{\"fileType\":\"CSV\",\"csvDelimiter\":\",\",\"csvQuote\":\"\\\"\",\"skipRows\":\"\",\"limitRows\":\"\",\"xslSheetNumber\":\"\",\"fileName\":\"df.csv\"},\"numRows\":false,\"persisted\":false,\"persistTableName\":\"\",\"owner\":\"biadmin\",\"publicDS\":true,\"scope\":{\"valueId\":191,\"domainCd\":\"DS_SCOPE\",\"domainNm\":\"Dataset scope\",\"valueCd\":\"USER\",\"valueNm\":\"User\",\"valueDs\":\"Dataset scope\"},\"scopeId\":191,\"metadata\":{\"fieldsMeta\":[{\"name\":\"ID\",\"alias\":\"ID\",\"type\":\"java.lang.String\",\"properties\":{},\"fieldType\":\"ATTRIBUTE\"},{\"name\":\"Hair\",\"alias\":\"Hair\",\"type\":\"java.lang.String\",\"properties\":{},\"fieldType\":\"ATTRIBUTE\"},{\"name\":\"Eye\",\"alias\":\"Eye\",\"type\":\"java.lang.String\",\"properties\":{},\"fieldType\":\"ATTRIBUTE\"},{\"name\":\"Sex\",\"alias\":\"Sex\",\"type\":\"java.lang.String\",\"properties\":{},\"fieldType\":\"ATTRIBUTE\"},{\"name\":\"Freq\",\"alias\":\"Freq\",\"type\":\"java.lang.String\",\"properties\":{},\"fieldType\":\"ATTRIBUTE\"}],\"properties\":{}},\"categoryId\":156,\"parameters\":[]}"}}
		//
		JSONArray replacements;
		try {
			replacements = new JSONArray(body);
			for (int i = 0; i < replacements.length(); i++) {
				JSONObject object = replacements.getJSONObject(i);
				JSONObject items = object.getJSONObject("items");
				String type = object.getString("type");
				if (type.equalsIgnoreCase("variablesIn")) {
					variablesInMap = objMap.readValue(items.toString(), HashMap.class);
				} else if (type.equalsIgnoreCase("datasetsIn")) {
					datasetsInMap = objMap.readValue(items.toString(), HashMap.class);
				} else if (type.equalsIgnoreCase("datasetsOut")) {
					datasetsOutMap = objMap.readValue(items.toString(), HashMap.class);
				} else if (type.equalsIgnoreCase("textOut")) {
					textOutMap = objMap.readValue(items.toString(), HashMap.class);
				} else if (type.equalsIgnoreCase("imageOut")) {
					imageOutMap = objMap.readValue(items.toString(), HashMap.class);
				}

			}
		} catch (Exception e) {
			logger.error("Error parsing new execution data", e);
			throw new SpagoBIEngineRuntimeException("Error parsing new execution data", e);
		}

		logger.debug("Creating engine instance ...");
		try {

			// Every map is in the form <OldValue,NewValue>
			DataMiningTemplate template = FunctionExecutionUtils.getTemplateWithReplacingValues(functionId, body, variablesInMap, datasetsInMap,
					datasetsOutMap, textOutMap, imageOutMap);
			// -----------------

			try {
				Map env = getEnv();
				dataMiningEngineInstance = DataMiningEngine.createInstance(template, env);
			} catch (DataMiningTemplateParseException e) {
				SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Template not valid", e);
				engineException.setDescription(e.getCause().getMessage());
				engineException.addHint("Check the document's template");
				throw engineException;
			} catch (SpagoBIEngineRuntimeException e) {
				throw e;
			} catch (Exception e) {
				logger.error("Error starting the Data Mining engine: error while generating the engine instance.", e);
				throw new SpagoBIEngineRuntimeException("Error starting the Data Mining engine: error while generating the engine instance.", e);
			}
			logger.debug("Engine instance succesfully created");

			// getExecutionSession().setAttributeInSession(ENGINE_INSTANCE, dataMiningEngineInstance);

			List<DataMiningResult> dataminingExecutionResults = FunctionExecutor.executeCatalogFunction(dataMiningEngineInstance, getUserProfile());
			serviceResponse = new JSONArray();
			for (DataMiningResult r : dataminingExecutionResults) {
				JSONObject o = new JSONObject();
				o.put("resultType", r.getOutputType());
				o.put("result", r.getResult());
				if (r.getOutputType().equalsIgnoreCase("Image")) {
					o.put("resultName", r.getPlotName());
				} else { // Dataset Output o Text Output
					o.put("resultName", r.getVariablename());
				}
				serviceResponse.put(o);
			}

		} catch (Exception e) {
			logger.error("Error starting the Data Mining engine or getting datamining engine execution results!", e);
			throw new SpagoBIEngineRuntimeException("Error starting the Data Mining engine or getting datamining engine execution results!", e);

		} finally {
			logger.debug("OUT");
		}
		return serviceResponse.toString();

	}

	@GET
	@Path("/startTest")
	@Produces(MediaType.APPLICATION_JSON)
	public String testAction(@Context HttpServletResponse response) {

		logger.debug("IN");

		try {
			JSONObject obj = new JSONObject();
			try {
				obj.put("result", "ok");
			} catch (JSONException e) {
				logger.error("Error building the success string");
				throw new SpagoBIRuntimeException("Error building the success string");
			}
			String successString = obj.toString();
			return successString;
		} finally {
			logger.debug("OUT");
		}
	}

	private SpagoBIEngineStartupException getWrappedException(Exception e) {
		SpagoBIEngineStartupException serviceException;
		if (e instanceof SpagoBIEngineStartupException) {
			serviceException = (SpagoBIEngineStartupException) e;
		} else if (e instanceof SpagoBIEngineRuntimeException) {
			SpagoBIEngineRuntimeException ex = (SpagoBIEngineRuntimeException) e;
			serviceException = new SpagoBIEngineStartupException(this.getEngineName(), ex.getMessage(), ex.getCause());
			serviceException.setDescription(ex.getDescription());
			serviceException.setHints(ex.getHints());
		} else {
			Throwable rootException = e;
			while (rootException.getCause() != null) {
				rootException = rootException.getCause();
			}
			String str = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
			String message = "An unpredicted error occurred while executing " + getEngineName() + " service." + "\nThe root cause of the error is: " + str;
			serviceException = new SpagoBIEngineStartupException(getEngineName(), message, e);
		}
		return serviceException;
	}

	@Override
	public Map getEnv() {
		Map env = new HashMap();

		env.put(EngineConstants.ENV_USER_PROFILE, getUserProfile());
		env.put(EngineConstants.ENV_CONTENT_SERVICE_PROXY, getContentServiceProxy());
		env.put(EngineConstants.ENV_AUDIT_SERVICE_PROXY, getAuditServiceProxy());
		env.put(EngineConstants.ENV_DATASET_PROXY, getDataSetServiceProxy());
		env.put(EngineConstants.ENV_DATASOURCE_PROXY, getDataSourceServiceProxy());
		env.put(EngineConstants.ENV_ARTIFACT_PROXY, getArtifactServiceProxy());
		env.put(EngineConstants.ENV_LOCALE, this.getLocale());
		env.put(SpagoBIConstants.SBI_ARTIFACT_VERSION_ID, this.getServletRequest().getParameter(SpagoBIConstants.SBI_ARTIFACT_VERSION_ID));
		env.put(SpagoBIConstants.SBI_ARTIFACT_ID, this.getServletRequest().getParameter(SpagoBIConstants.SBI_ARTIFACT_ID));
		env.put(SpagoBIConstants.SBI_ARTIFACT_STATUS, this.getServletRequest().getParameter(SpagoBIConstants.SBI_ARTIFACT_STATUS));
		env.put(SpagoBIConstants.SBI_ARTIFACT_LOCKER, this.getServletRequest().getParameter(SpagoBIConstants.SBI_ARTIFACT_LOCKER));

		copyRequestParametersIntoEnv(env, this.getServletRequest());

		return env;
	}

	private void copyRequestParametersIntoEnv(Map env, HttpServletRequest servletRequest) {
		Set parameterStopList = null;

		logger.debug("IN");

		parameterStopList = new HashSet();
		parameterStopList.add("template");
		parameterStopList.add("ACTION_NAME");
		parameterStopList.add("NEW_SESSION");
		parameterStopList.add("document");
		parameterStopList.add("spagobicontext");
		parameterStopList.add("BACK_END_SPAGOBI_CONTEXT");
		parameterStopList.add("userId");
		parameterStopList.add("auditId");

		HashMap requestParameters = ParametersDecoder.getDecodedRequestParameters(servletRequest);

		Iterator it = requestParameters.keySet().iterator();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = requestParameters.get(key);
			logger.debug("Parameter [" + key + "] has been read from request");
			if (value == null) {
				logger.debug("Parameter [" + key + "] is null");
				logger.debug("Parameter [" + key + "] copyed into environment parameters list: FALSE");
				continue;
			} else {
				logger.debug("Parameter [" + key + "] is of type  " + value.getClass().getName());
				logger.debug("Parameter [" + key + "] is equal to " + value.toString());
				if (parameterStopList.contains(key)) {
					logger.debug("Parameter [" + key + "] copyed into environment parameters list: FALSE");
					continue;
				}
				env.put(key, value);
				logger.debug("Parameter [" + key + "] copyed into environment parameters list: TRUE");
			}
		}

		logger.debug("OUT");

	}

	@Override
	public Locale getLocale() {
		logger.debug("IN");
		Locale toReturn = null;
		try {
			String language = this.getServletRequest().getParameter(LANGUAGE);
			String country = this.getServletRequest().getParameter(COUNTRY);
			if (StringUtils.isNotEmpty(language) && StringUtils.isNotEmpty(country)) {
				toReturn = new Locale(language, country);
			} else {
				logger.warn("Language and country not specified in request. Considering default locale that is " + DEFAULT_LOCALE.toString());
				toReturn = DEFAULT_LOCALE;
			}
		} catch (Exception e) {
			logger.error("An error occurred while retrieving locale from request, using default locale that is " + DEFAULT_LOCALE.toString(), e);
			toReturn = DEFAULT_LOCALE;
		}
		logger.debug("OUT");
		return toReturn;
	}

}
