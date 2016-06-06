package it.eng.spagobi.engines.datamining.servlet;

import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.datamining.DataMiningEngine;
import it.eng.spagobi.engines.datamining.DataMiningEngineInstance;
import it.eng.spagobi.engines.datamining.api.CommandsResource;
import it.eng.spagobi.engines.datamining.api.OutputsResource;
import it.eng.spagobi.engines.datamining.api.ResultResource;
import it.eng.spagobi.engines.datamining.bo.DataMiningResult;
import it.eng.spagobi.engines.datamining.common.AbstractDataMiningEngineService;
import it.eng.spagobi.engines.datamining.model.DataMiningCommand;
import it.eng.spagobi.engines.datamining.model.Output;
import it.eng.spagobi.engines.datamining.template.DataMiningTemplateParseException;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.proxy.ArtifactServiceProxy;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.services.proxy.DataSourceServiceProxy;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.callbacks.audit.AuditAccessUtils;
import it.eng.spagobi.utilities.engines.AuditServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;
import it.eng.spagobi.utilities.engines.rest.ExecutionSession;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;

public class DataMiningServletBackEnd extends HttpServlet {
	protected static Logger logger = Logger.getLogger(DataMiningServletBackEnd.class);

	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
	public static final String LANGUAGE = "SBI_LANGUAGE";
	public static final String COUNTRY = "SBI_COUNTRY";

	private DataMiningEngineInstance dataMiningEngineInstance = null;
	private ExecutionSession executeSession = null;
	private HashMap requestParameters = null;
	private IEngUserProfile profile = null;
	private String documentLabel = null;
	private String documentId = null;
	private String auditId = null;
	private String userId = null;
	private Map env = null;

	@Override
	public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		logger.debug("Start processing a new request...");
		// USER PROFILE
		HttpSession session = request.getSession();
		IEngUserProfile profile = (IEngUserProfile) session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		String documentId = request.getParameter("document");
		String userId = (String) profile.getUserUniqueIdentifier();
		env = createEnv(request);

		logger.debug("userId=" + userId);
		logger.debug("documentId=" + documentId);
		// AUDIT UPDATE
		auditId = request.getParameter("SPAGOBI_AUDIT_ID");
		logger.debug("auditId=" + auditId);
		AuditAccessUtils auditAccessUtils = (AuditAccessUtils) request.getSession().getAttribute("SPAGOBI_AUDIT_UTILS");
		if (auditId != null) {
			if (auditAccessUtils != null)
				auditAccessUtils.updateAudit(session, userId, auditId, new Long(System.currentTimeMillis()), null, "EXECUTION_STARTED", null, null);
		}
		// execute datamining
		initDataMining(request, response);
		runDocumentDataMining(request, response);
		// AUDIT UPDATE
		if (auditId != null) {
			if (auditAccessUtils != null) {
				auditAccessUtils.updateAudit(session, userId, auditId, null, new Long(System.currentTimeMillis()), "EXECUTION_PERFORMED", null, null);
			}
		}
	}

	private void runDocumentDataMining(HttpServletRequest request, HttpServletResponse response) {
		if (dataMiningEngineInstance == null) {
			logger.error("No DataMining Instance found. Impossible to execute the document");
			return;
		}
		PrintWriter writer;
		try {
			writer = response.getWriter();
		} catch (IOException e) {
			logger.error("Error during getting response writer\n\n ", e);
			return;
		}
		writer.append("<html><body>");
		CommandsResource commandsResource = new CommandsResource();
		OutputsResource outputResource = new OutputsResource();
		ResultResource resultResource = new ResultResource();
		List<DataMiningCommand> commands = dataMiningEngineInstance.getCommands();
		Iterator<DataMiningCommand> itCommand = commands.iterator();
		while (itCommand.hasNext()) {
			DataMiningCommand cmd = itCommand.next();
			String commandName = cmd.getName();
			List<Output> outputs = cmd.getOutputs();
			commandsResource.setAutoModeCommand(commandName, dataMiningEngineInstance);
			Iterator<Output> itOuput = outputs.iterator();
			while (itOuput.hasNext()) {
				Output out = itOuput.next();
				String outputName = out.getOutputName();
				outputResource.setAutoModeOutput(outputName, dataMiningEngineInstance);
				DataMiningResult result = resultResource.getDataMiningResult(commandName, outputName, true, documentLabel, dataMiningEngineInstance, profile);
				appendResultHtml(writer, result, cmd, out);
			}
		}
		writer.append("</body></html>");
	}

	private void initDataMining(HttpServletRequest servletRequest, HttpServletResponse response) {
		logger.debug("IN");
		HttpSession session = servletRequest.getSession();
		AbstractDataMiningEngineService abstractDataMiningEngineService = new AbstractDataMiningEngineService();
		executeSession = new ExecutionSession(servletRequest, session);
		profile = (IEngUserProfile) session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		userId = (String) profile.getUserUniqueIdentifier();
		documentId = servletRequest.getParameter("document");
		documentLabel = (String) executeSession.getAttributeFromSession(EngineConstants.ENV_DOCUMENT_LABEL);
		if (documentLabel == null || executeSession.requestContainsAttribute(EngineConstants.ENV_DOCUMENT_LABEL)) {
			documentLabel = executeSession.getAttributeAsString(EngineConstants.ENV_DOCUMENT_LABEL);
		}
		logger.debug("userId=" + userId);
		logger.debug("documentId=" + documentId);

		ServletContext servletContext = getServletContext();
		SourceBean templateBean = getTemplateContent(servletRequest, servletContext);

		try {
			dataMiningEngineInstance = DataMiningEngine.createInstance(templateBean, env);
			executeSession.setAttributeInSession(EngineConstants.ENGINE_INSTANCE, dataMiningEngineInstance);
			logger.debug("Engine instance succesfully created");
			executeSession.setAttributeInSession(EngineConstants.ENGINE_INSTANCE, documentLabel);
		} catch (DataMiningTemplateParseException e) {
			SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(abstractDataMiningEngineService.getEngineName(),
					"Template not valid", e);
			engineException.setDescription(e.getCause().getMessage());
			engineException.addHint("Check the document's template");
			throw engineException;
		} catch (SpagoBIEngineRuntimeException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Error starting the Data Mining engine: error while generating the engine instance.", e);
			throw new SpagoBIEngineRuntimeException("Error starting the Data Mining engine: error while generating the engine instance.", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void appendResultHtml(PrintWriter writer, DataMiningResult result, DataMiningCommand command, Output output) {
		writer.append("<div>");
		writer.append("<span><b>Command</b>: <i>" + command.getLabel() + "</i></span> - ");
		writer.append("<span><b>Output</b>: <i>" + output.getOuputLabel() + "</i></span><br>");
		if (result.getError() != null && result.getError().length() > 0) {
			writer.append("<span style=\"color: red\"> Error:  " + result.getError() + "</span>");
		} else if (result.getOutputType().equalsIgnoreCase("text")) {
			writer.append("<span> Result = <i>" + result.getResult() + "</i></span>");
		} else if (result.getOutputType().equalsIgnoreCase("html")) {
			writer.append(result.getResult());
		} else if (result.getOutputType().equalsIgnoreCase("image")) {
			writer.append("<img alt=\"Result for " + result.getPlotName() + "\" src=\"data:image/png;base64," + result.getResult() + " \" />");
		}
		writer.append("</div><br>");
	}

	private SourceBean getTemplateContent(HttpServletRequest servletRequest, ServletContext servletContext) {
		logger.debug("IN");
		SourceBean templateSB = null;

		ContentServiceProxy contentProxy = (ContentServiceProxy) env.get(EngineConstants.ENV_CONTENT_SERVICE_PROXY);

		requestParameters = ParametersDecoder.getDecodedRequestParameters(servletRequest);
		Content template = null;
		try {
			template = contentProxy.readTemplate(documentId, requestParameters);
			logger.debug("Read the template=" + template.getFileName());
		} catch (Exception e) {
			logger.error("Impossible to read Template");
			throw new SpagoBIRuntimeException("Impossible to read Template" + e);
		}
		InputStream is = null;
		byte[] templateContent = null;
		String s;
		try {
			BASE64Decoder bASE64Decoder = new BASE64Decoder();
			templateContent = bASE64Decoder.decodeBuffer(template.getContent());
			is = new java.io.ByteArrayInputStream(templateContent);
			int n = is.available();
			byte[] bytes = new byte[n];
			is.read(bytes, 0, n);
			s = new String(bytes, StandardCharsets.UTF_8);
			templateSB = SourceBean.fromXMLString(s);
		} catch (Throwable t) {
			logger.warn("Error on decompile", t);
		}
		return templateSB;
	}

	private Map createEnv(HttpServletRequest request) {
		Map envTmp = new HashMap();
		HttpSession session = request.getSession();
		ContentServiceProxy contentProxy = new ContentServiceProxy(userId, session);
		AuditServiceProxy auditProxy = new AuditServiceProxy(auditId, userId, session);
		DataSourceServiceProxy datasourceProxy = new DataSourceServiceProxy(userId, session);
		ArtifactServiceProxy artifactProxy = new ArtifactServiceProxy(userId, session);
		DataSetServiceProxy datasetProxy = new DataSetServiceProxy(userId, session);

		envTmp.put(EngineConstants.ENV_USER_PROFILE, profile);
		envTmp.put(EngineConstants.ENV_CONTENT_SERVICE_PROXY, contentProxy);
		envTmp.put(EngineConstants.ENV_AUDIT_SERVICE_PROXY, auditProxy);
		envTmp.put(EngineConstants.ENV_DATASET_PROXY, datasetProxy);
		envTmp.put(EngineConstants.ENV_DATASOURCE_PROXY, datasourceProxy);
		envTmp.put(EngineConstants.ENV_ARTIFACT_PROXY, artifactProxy);
		envTmp.put(EngineConstants.ENV_LOCALE, getLocale(request));
		envTmp.put(SpagoBIConstants.SBI_ARTIFACT_VERSION_ID, request.getParameter(SpagoBIConstants.SBI_ARTIFACT_VERSION_ID));
		envTmp.put(SpagoBIConstants.SBI_ARTIFACT_ID, request.getParameter(SpagoBIConstants.SBI_ARTIFACT_ID));
		envTmp.put(SpagoBIConstants.SBI_ARTIFACT_STATUS, request.getParameter(SpagoBIConstants.SBI_ARTIFACT_STATUS));
		envTmp.put(SpagoBIConstants.SBI_ARTIFACT_LOCKER, request.getParameter(SpagoBIConstants.SBI_ARTIFACT_LOCKER));

		copyRequestParametersIntoEnv(envTmp, request);

		return envTmp;
	}

	private Locale getLocale(HttpServletRequest request) {
		logger.debug("IN");
		Locale toReturn = null;
		try {
			String language = request.getParameter(LANGUAGE);
			String country = request.getParameter(COUNTRY);
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

	private void copyRequestParametersIntoEnv(Map env, HttpServletRequest servletRequest) {
		Set<String> parameterStopList = null;

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

}
