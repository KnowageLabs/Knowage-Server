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
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IDomainDAO;
import it.eng.spagobi.engines.datamining.DataMiningEngine;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.DataMiningDataset;
import it.eng.spagobi.engines.datamining.model.DataMiningScript;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.model.Variable;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplate;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplateParseException;
import it.eng.spagobi.functions.dao.ICatalogFunctionDAO;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.functions.metadata.SbiFunctionInputDataset;
import it.eng.spagobi.functions.metadata.SbiFunctionInputVariable;
import it.eng.spagobi.functions.metadata.SbiFunctionOutput;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.util.ArrayList;
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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
				// C:\Users\piovani\apache-tomcat-7.0.67-Trunk\webapps\knowagedataminingengine\WEB-INF\jsp
				servletRequest.getRequestDispatcher(SUCCESS_REQUEST_DISPATCHER_URL).forward(servletRequest, response);

				// response.sendRedirect("C:/Users/piovani/apache-tomcat-7.0.67-Trunk/webapps/knowagedataminingengine/WEB-INF/jsp/DataMining.jsp");
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

			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			function = fcDAO.getCatalogFunctionById(functionId);

			DataMiningTemplate template = new DataMiningTemplate();
			template.setLanguage(function.getLanguage());

			Set<SbiFunctionInputDataset> datasets = function.getSbiFunctionInputDatasets();
			List<DataMiningDataset> dataminingDatasets = new ArrayList<DataMiningDataset>();

			for (SbiFunctionInputDataset dataset : datasets) {
				DataMiningDataset d = new DataMiningDataset();
				IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();
				int dsId = dataset.getId().getDsId();
				IDataSet iDataset = dsDAO.loadDataSetById(dsId);
				d.setLabel(iDataset.getLabel());
				d.setSpagobiLabel(iDataset.getLabel());
				d.setCanUpload(true);
				d.setName(iDataset.getName());
				d.setFileName(iDataset.getName() + ".csv");
				// d.setType(iDataset.getDsType());
				d.setType("Dataset"); // or DataMiningConstants.DATASET_OUTPUT or DataMiningConstants.SPAGOBI_DS_OUTPUT, the dataminingEngine differences
										// spagoBI datasets from file datasets created when executing a document

				d.setOptions("sep=','");
				d.setReadType("csv"); // Default dataset is CSV file
				dataminingDatasets.add(d);
			}
			template.setDatasets(dataminingDatasets);

			Set<SbiFunctionInputVariable> variables = function.getSbiFunctionInputVariables();
			Set<SbiFunctionOutput> outputs = function.getSbiFunctionOutputs();

			DataMiningCommand c = new DataMiningCommand();
			c.setLabel("CatalogCommand");
			c.setName("CatalogCommand");
			c.setScriptName("CatalogScript");

			List<Variable> vars = new ArrayList<Variable>();
			List<Output> outs = new ArrayList<Output>();

			for (SbiFunctionInputVariable v : variables) {
				Variable var = new Variable();
				var.setName(v.getId().getVarName());
				var.setValue(v.getVarValue());
				vars.add(var);
			}
			for (SbiFunctionOutput o : outputs) {
				Output out = new Output();
				out.setOuputLabel(o.getId().getLabel());
				out.setOutputName(o.getId().getLabel()); // Name=label
				IDomainDAO domainsDAO = DAOFactory.getDomainDAO();
				String type = domainsDAO.loadDomainById(o.getOutType()).getValueName();
				out.setOutputType(type);
				out.setOutputMode("auto"); // TODO: ??? can't figure out what auto means...
				out.setOutputName(o.getId().getLabel());
				out.setOutputValue(o.getId().getLabel());
				outs.add(out);
			}
			c.setVariables(vars);
			c.setOutputs(outs);

			List<DataMiningCommand> commands = new ArrayList<DataMiningCommand>();
			commands.add(c);
			template.setCommands(commands);

			List<DataMiningScript> dataMiningScripts = new ArrayList<DataMiningScript>();
			String scriptCode = function.getScript();
			DataMiningScript script = new DataMiningScript();
			script.setName("CatalogScript");
			script.setCode(scriptCode);
			dataMiningScripts.add(script);
			template.setScripts(dataMiningScripts);

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

			getExecutionSession().setAttributeInSession(ENGINE_INSTANCE, dataMiningEngineInstance);
			try {
				// To deploy into JBOSSEAP64 is needed a StandardWrapper, instead of RestEasy Wrapper
				servletRequest = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
				response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);

				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				List<DataMiningResult> dataminingExecutionResults = FunctionExecutor.executeFunction(dataMiningEngineInstance, getUserProfile());
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
		} catch (JSONException e) {
			logger.error("Error parsing new execution data", e);
			throw new SpagoBIEngineRuntimeException("Error parsing new execution data", e);
		} catch (JsonParseException e) {
			logger.error("Error parsing new execution data", e);
			throw new SpagoBIEngineRuntimeException("Error parsing new execution data", e);
		} catch (JsonMappingException e) {
			logger.error("Error parsing new execution data", e);
			throw new SpagoBIEngineRuntimeException("Error parsing new execution data", e);
		} catch (IOException e) {
			logger.error("Error parsing new execution data", e);
			throw new SpagoBIEngineRuntimeException("Error parsing new execution data", e);
		}

		logger.debug("Creating engine instance ...");

		try {

			ICatalogFunctionDAO fcDAO = DAOFactory.getCatalogFunctionDAO();
			function = fcDAO.getCatalogFunctionById(functionId);

			DataMiningTemplate template = new DataMiningTemplate();
			template.setLanguage(function.getLanguage());

			Set<SbiFunctionInputDataset> datasets = function.getSbiFunctionInputDatasets();
			List<DataMiningDataset> dataminingDatasets = new ArrayList<DataMiningDataset>();

			for (SbiFunctionInputDataset dataset : datasets) {
				DataMiningDataset d = new DataMiningDataset();
				IDataSetDAO dsDAO = DAOFactory.getDataSetDAO();
				int dsId = dataset.getId().getDsId();
				IDataSet iDataset = dsDAO.loadDataSetById(dsId);// *
				String labelDemoDS = iDataset.getLabel();
				if (datasetsInMap.containsKey(labelDemoDS)) // map element format: <demoLabel:replacingLabel>
				{
					// String replacingDSlabel = datasetsInMap.get(labelDemoDS);
					String datasetHavingReplacingDSlabel = datasetsInMap.get(labelDemoDS);
					JSONObject dsHavingReplacingDSlabel = new JSONObject(datasetHavingReplacingDSlabel);
					String replacingDSlabel = dsHavingReplacingDSlabel.getString("label");

					if (replacingDSlabel != null && (!replacingDSlabel.equals(""))) { // if a replacing dataset isn't specified, associate the demo dataset
						IDataSet ds = dsDAO.loadDataSetByLabel(replacingDSlabel);
						if (ds != null) // if a replacing dataset isn't specified, associate the demo dataset
						{
							iDataset = ds;
						}
						// else //just set *
						// {
						// iDataset = dsDAO.loadDataSetById(dsId);
						// }

					}
				}
				// else
				// {
				// iDataset = dsDAO.loadDataSetById(dsId);
				// }
				d.setLabel(iDataset.getLabel());
				d.setSpagobiLabel(iDataset.getLabel()); // Important! used label is spagobiLabel!
				d.setCanUpload(true);
				d.setName(iDataset.getName());
				d.setFileName(iDataset.getName() + ".csv");
				// d.setType(iDataset.getDsType());
				d.setType("Dataset"); // // or DataMiningConstants.DATASET_OUTPUT or DataMiningConstants.SPAGOBI_DS_OUTPUT, the dataminingEngine differences
				// spagoBI datasets from file datasets created when executing a document

				d.setOptions("sep=','");
				d.setReadType("csv"); // Default dataset is CSV file
				dataminingDatasets.add(d);
			}
			template.setDatasets(dataminingDatasets);

			Set<SbiFunctionInputVariable> variables = function.getSbiFunctionInputVariables();
			Set<SbiFunctionOutput> outputs = function.getSbiFunctionOutputs();

			DataMiningCommand c = new DataMiningCommand();
			c.setLabel("CatalogCommand");
			c.setName("CatalogCommand");
			c.setScriptName("CatalogScript");

			List<Variable> vars = new ArrayList<Variable>();
			List<Output> outs = new ArrayList<Output>();

			for (SbiFunctionInputVariable v : variables) {
				Variable var = new Variable();
				String varName = v.getId().getVarName();
				var.setName(varName);
				String varValue = "";
				if (variablesInMap.containsKey(varName)) // map element format: <demoVarName:replacingVALUE>
				{
					String replacingVariableValue = variablesInMap.get(varName);
					if (!replacingVariableValue.equals("") && replacingVariableValue != null) // se non c'è un replacing variable value associato, associa il //
																								// val demo
					{
						varValue = replacingVariableValue;
					} else {
						varValue = v.getVarValue();
					}
				} else // variable not present in input map, use demo variable
				{
					varValue = v.getVarValue();
				}

				var.setValue(varValue);
				vars.add(var);
			}

			HashMap<String, String> mapImageAndTextOut = new HashMap<String, String>();
			mapImageAndTextOut.putAll(textOutMap);
			mapImageAndTextOut.putAll(imageOutMap);

			for (SbiFunctionOutput o : outputs) {
				Output out = new Output();
				String label = o.getId().getLabel();
				String oldLabel = o.getId().getLabel(); // old label is the value of the dataframe variable containing dataset value in script!!
				if (datasetsOutMap.containsKey(label)) {
					String replacingDatasetOutLabel = datasetsOutMap.get(label);
					if (!replacingDatasetOutLabel.equals("") && replacingDatasetOutLabel != null) {
						label = replacingDatasetOutLabel;
					}

				} else if (mapImageAndTextOut.containsKey(label)) {
					String replacingOutLabel = mapImageAndTextOut.get(label);
					if (!replacingOutLabel.equals("") && replacingOutLabel != null) {
						label = replacingOutLabel;
					}
				}
				out.setOuputLabel(label);
				out.setOutputName(label); // Name=label
				out.setOutputValue(oldLabel); // aggiunto, prima era label --> è il nome del dataset nello script(?)
				IDomainDAO domainsDAO = DAOFactory.getDomainDAO();
				String type = domainsDAO.loadDomainById(o.getOutType()).getValueName();
				out.setOutputType(type);
				out.setOutputMode("auto"); // TODO: ??? can't figure out what auto means...
				outs.add(out);
			}
			c.setVariables(vars);
			c.setOutputs(outs);

			List<DataMiningCommand> commands = new ArrayList<DataMiningCommand>();
			commands.add(c);
			template.setCommands(commands);

			List<DataMiningScript> dataMiningScripts = new ArrayList<DataMiningScript>();
			String scriptCode = function.getScript();
			DataMiningScript script = new DataMiningScript();
			script.setName("CatalogScript");
			script.setCode(scriptCode);
			dataMiningScripts.add(script);
			template.setScripts(dataMiningScripts);

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

			getExecutionSession().setAttributeInSession(ENGINE_INSTANCE, dataMiningEngineInstance);
			try {
				// To deploy into JBOSSEAP64 is needed a StandardWrapper, instead of RestEasy Wrapper
				servletRequest = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
				response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);

				response.setContentType("text/html");
				response.setCharacterEncoding("UTF-8");
				List<DataMiningResult> dataminingExecutionResults = FunctionExecutor.executeFunction(dataMiningEngineInstance, getUserProfile());
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
