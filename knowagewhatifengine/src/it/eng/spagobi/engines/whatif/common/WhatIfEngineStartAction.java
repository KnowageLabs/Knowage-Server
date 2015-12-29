/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 */
package it.eng.spagobi.engines.whatif.common;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.whatif.WhatIfEngine;
import it.eng.spagobi.engines.whatif.WhatIfEngineAnalysisState;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplateParseException;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;
import it.eng.spagobi.utilities.engines.rest.AbstractEngineStartRestService;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

@Path("/start")
public class WhatIfEngineStartAction extends AbstractEngineStartRestService {

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
	public static transient Logger logger = Logger.getLogger(WhatIfEngineStartAction.class);

	private static final String SUCCESS_REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/whatIf.jsp";
	private static final String FAILURE_REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/errors/startupError.jsp";

	@GET
	@Produces("text/html")
	public void startAction() {

		logger.debug("IN");

		HttpServletRequest request = ResteasyProviderFactory.getContextData(HttpServletRequest.class);
		HttpServletResponse response = ResteasyProviderFactory.getContextData(HttpServletResponse.class);

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

			WhatIfEngineInstance whatIfEngineInstance = null;

			logger.debug("Creating engine instance ...");

			try {
				whatIfEngineInstance = WhatIfEngine.createInstance(templateBean, getEnv());
			} catch (WhatIfTemplateParseException e) {
				SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(getEngineName(), "Template not valid", e);
				engineException.setDescription(e.getCause().getMessage());
				engineException.addHint("Check the document's template");
				throw engineException;
			} catch (SpagoBIEngineRuntimeException e) {
				throw e;
			} catch (Exception e) {
				logger.error("Error starting the What-If engine: error while generating the engine instance.", e);
				throw new SpagoBIEngineRuntimeException("Error starting the What-If engine: error while generating the engine instance.", e);
			}
			logger.debug("Engine instance succesfully created");

			// loads subobjects
			whatIfEngineInstance.setAnalysisMetadata(getAnalysisMetadata());
			if (getAnalysisStateRowData() != null) {
				logger.debug("Loading subobject [" + whatIfEngineInstance.getAnalysisMetadata().getName() + "] ...");
				try {
					WhatIfEngineAnalysisState analysisState = new WhatIfEngineAnalysisState();
					analysisState.load(getAnalysisStateRowData());
					whatIfEngineInstance.setAnalysisState(analysisState);
				} catch (Throwable t) {
					logger.error("Error loading the subobject", t);
					throw new SpagoBIRestServiceException("sbi.olap.start.load.subobject.error", getLocale(), "Error loading the subobject", t);
				}
				logger.debug("Subobject [" + whatIfEngineInstance.getAnalysisMetadata().getName() + "] succesfully loaded");
			}

			getExecutionSession().setAttributeInSession(ENGINE_INSTANCE, whatIfEngineInstance);

			try {

				request.getRequestDispatcher(SUCCESS_REQUEST_DISPATCHER_URL).forward(request, response);
			} catch (Exception e) {
				logger.error("Error starting the What-If engine: error while forwarding the execution to the jsp " + SUCCESS_REQUEST_DISPATCHER_URL, e);
				throw new SpagoBIEngineRuntimeException("Error starting the What-If engine: error while forwarding the execution to the jsp "
						+ SUCCESS_REQUEST_DISPATCHER_URL, e);
			}

			if (getAuditServiceProxy() != null) {
				getAuditServiceProxy().notifyServiceEndEvent();
			}

		} catch (Exception e) {
			logger.error("Error starting the What-If engine", e);
			if (getAuditServiceProxy() != null) {
				getAuditServiceProxy().notifyServiceErrorEvent(e.getMessage());
			}

			SpagoBIEngineStartupException serviceException = this.getWrappedException(e);

			getExecutionSession().setAttributeInSession(STARTUP_ERROR, serviceException);
			try {
				request.getRequestDispatcher(FAILURE_REQUEST_DISPATCHER_URL).forward(request, response);
			} catch (Exception ex) {
				logger.error("Error starting the What-If engine: error while forwarding the execution to the jsp " + FAILURE_REQUEST_DISPATCHER_URL, ex);
				throw new SpagoBIEngineRuntimeException("Error starting the What-If engine: error while forwarding the execution to the jsp "
						+ FAILURE_REQUEST_DISPATCHER_URL, ex);
			}
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map getEnv() {
		Map env = new HashMap();

		IDataSource ds = this.getDataSource();

		// document id can be null (when using QbE for dataset definition)
		if (getDocumentId() != null) {
			env.put(EngineConstants.ENV_DOCUMENT_ID, getDocumentId());
		}

		env.put(EngineConstants.ENV_DATASOURCE, ds);

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

	@SuppressWarnings({ "unchecked", "rawtypes" })
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

	@Override
	public String getEngineName() {
		return WhatIfConstants.ENGINE_NAME;
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return ResteasyProviderFactory.getContextData(HttpServletRequest.class);
	}

}