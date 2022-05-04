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

package it.eng.spagobi.engines.whatif.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.plugins.providers.html.View;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.bo.UserProfile;
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

public class WhatIfEngineAbstractStartAction extends AbstractEngineStartRestService {

	// SESSION PARAMETRES
	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;
	public static final String STARTUP_ERROR = EngineConstants.STARTUP_ERROR;
	private static final String SUCCESS_REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/whatIf2.jsp";
	private static final String FAILURE_REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/errors/startupError.jsp";

	// Defaults
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
	@Context
	HttpServletRequest request;
	@Context
	HttpServletResponse response;

	@Override
	public String getEngineName() {
		return WhatIfConstants.ENGINE_NAME;
	}

	@Override
	public HttpServletRequest getServletRequest() {
		return request;
	}

	public View startAction(boolean whatif) {
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

			Map<String, Object> env = getEnv();

			WhatIfEngineAnalysisState analysisState = getAnalysisState();
			if (analysisState != null) {
				// in case there is a subobject (i.e. analyses state is not null), we must update env variable with drivers saved along with subobject
				Map<String, Object> drivers = analysisState.getDriversValues();
				env.putAll(drivers);
			}

			WhatIfEngineInstance whatIfEngineInstance = null;

			logger.debug("Creating engine instance ...");

			try {
				whatIfEngineInstance = WhatIfEngine.createInstance(templateBean, whatif, env);
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
			if (analysisState != null) {
				logger.debug("Loading subobject [" + whatIfEngineInstance.getAnalysisMetadata().getName() + "] ...");
				try {
					whatIfEngineInstance.setAnalysisState(analysisState);
				} catch (Exception e) {
					logger.error("Error loading the subobject", e);
					throw new SpagoBIRestServiceException("sbi.olap.start.load.subobject.error", getLocale(), "Error loading the subobject", e);
				}
				logger.debug("Subobject [" + whatIfEngineInstance.getAnalysisMetadata().getName() + "] succesfully loaded");
			}

			getExecutionSession().setAttributeInSession(ENGINE_INSTANCE, whatIfEngineInstance);

			try {
				return new View(SUCCESS_REQUEST_DISPATCHER_URL);
			} catch (Exception e) {
				logger.error("Error starting the What-If engine: error while forwarding the execution to the jsp " + SUCCESS_REQUEST_DISPATCHER_URL, e);
				throw new SpagoBIEngineRuntimeException(
						"Error starting the What-If engine: error while forwarding the execution to the jsp " + SUCCESS_REQUEST_DISPATCHER_URL, e);
			} finally {
				if (getAuditServiceProxy() != null) {
					getAuditServiceProxy().notifyServiceEndEvent();
				}
			}
		} catch (Exception e) {
			logger.error("Error starting the What-If engine", e);
			if (getAuditServiceProxy() != null) {
				getAuditServiceProxy().notifyServiceErrorEvent(e.getMessage());
			}

			SpagoBIEngineStartupException serviceException = this.getWrappedException(e);

			getExecutionSession().setAttributeInSession(STARTUP_ERROR, serviceException);
			try {
				return new View(FAILURE_REQUEST_DISPATCHER_URL);
			} catch (Exception ex) {
				logger.error("Error starting the What-If engine: error while forwarding the execution to the jsp " + FAILURE_REQUEST_DISPATCHER_URL, ex);
				throw new SpagoBIEngineRuntimeException(
						"Error starting the What-If engine: error while forwarding the execution to the jsp " + FAILURE_REQUEST_DISPATCHER_URL, ex);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	private WhatIfEngineAnalysisState getAnalysisState() {
		WhatIfEngineAnalysisState toReturn = null;
		try {
			if (getAnalysisStateRowData() != null) {
				toReturn = new WhatIfEngineAnalysisState();
				toReturn.load(getAnalysisStateRowData());
			}
		} catch (Exception e) {
			throw new SpagoBIEngineRuntimeException("Error while getting analyses state content", e);
		}
		return toReturn;
	}

	protected SpagoBIEngineStartupException getWrappedException(Exception e) {
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

	@Override
	public Locale getLocale() {
		logger.debug("IN");
		Locale toReturn = null;
		try {
			String language = this.getServletRequest().getParameter(LANGUAGE);
			String country = this.getServletRequest().getParameter(COUNTRY);
			String script = this.getServletRequest().getParameter(SCRIPT);
			logger.debug("Locale parameters received: language = [" + language + "] ; country = [" + country + "]");

			if (StringUtils.isNotEmpty(language) && StringUtils.isNotEmpty(country)) {
				Builder builder = new Builder().setLanguage(language).setRegion(country);
				if (StringUtils.isNotBlank(script)) {
					builder.setScript(script);
				}
				toReturn = builder.build();
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
	public UserProfile getUserProfile() {
		return super.getUserProfile();

	}
}
