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
package it.eng.spagobi.utilities.engines;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Locale.Builder;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.services.proxy.DataSetServiceProxy;
import it.eng.spagobi.services.proxy.DataSourceServiceProxy;
import it.eng.spagobi.services.proxy.DocumentExecuteServiceProxy;
import it.eng.spagobi.services.proxy.EventServiceProxy;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.ParametersDecoder;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.messages.EngineMessageBundle;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class EngineStartServletIOManager extends BaseServletIOManager {

	private UserProfile userProfile;
	private String userId;
	private String userUniqueIdentifier;
	private String userExecutionRole;
	private String auditId;
	private String documentId;
	private String documentLabel;
	private String documentAuthor;
	private Locale locale;

	private String templateName;
	private Content template;

	private ContentServiceProxy contentProxy;
	private AuditServiceProxy auditProxy;
	private EventServiceProxy eventProxy;
	private DataSourceServiceProxy datasourceProxy;
	private DataSetServiceProxy datasetProxy;
	private DocumentExecuteServiceProxy documentExecuteProxy;

	IDataSource dataSource;
	IDataSet dataSet;

	private Map env;

	public static final String AUDIT_ID = "SPAGOBI_AUDIT_ID";
	public static final String DOCUMENT_ID = "document";
	public static final String DOCUMENT_LABEL = "DOCUMENT_LABEL";
	public static final String DOCUMENT_AUTHOR = "DOCUMENT_AUTHOR";
	public static final String EXECUTION_ROLE = "SBI_EXECUTION_ROLE";

	public static final String COUNTRY = "SBI_COUNTRY";
	public static final String LANGUAGE = "SBI_LANGUAGE";
	public static final String SCRIPT = "SBI_SCRIPT";
	public static final String ON_EDIT_MODE = "onEditMode";

	private final Logger logger = Logger.getLogger(EngineStartServletIOManager.class);

	public EngineStartServletIOManager(HttpServletRequest request, HttpServletResponse response) {
		super(request, response);
	}

	public EngineStartServletIOManager(BaseServletIOManager baseIOManager) {
		this(baseIOManager.getRequest(), baseIOManager.getResponse());
	}

	public UserProfile getUserProfile() {
		// first we check if there is a user profile in session. If this is the case that means that the user have been authenticated by SpagoBI and the
		// user profile has been succesfully loaded and stored in session by the AccessFilter
		UserProfile userProfile = (UserProfile) getParameterFromSession(IEngUserProfile.ENG_USER_PROFILE);
		if (userProfile == null) {
			// if the user profile is not in session that means that the user has not been authenticated by spagobi.
			// This happens when the user call directly a REST service without log in spagobi before.
			// In these cases the request is catched by SecurityServerInterceptor that perform an authentication
			// based on simple schema (= user and pwd as header proeprties). If the authentication have success
			// and also the following authorization check have success the request is performed.
			// In this case the profile of the user is not stored in session. There is actually no session.
			// The service is stateles. For this reason it is necessary to recreate the profile at each query and pass
			// it explicitely to the ioManager (see method setProfile).
			userProfile = this.userProfile;
		}
		return userProfile;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public String getUserId() {
		UserProfile profile = null;

		if (userId == null) {
			userId = (String) getUserProfile().getUserId();
		}

		return userId;
	}

	public String getUserIdentifier() {
		if (userUniqueIdentifier == null) {
			userUniqueIdentifier = (String) getUserProfile().getUserUniqueIdentifier();
		}

		return userUniqueIdentifier;
	}

	public String getUserExecutionRole() {
		if (userExecutionRole == null) {
			userExecutionRole = this.getParameterAsString(EXECUTION_ROLE);
		}
		return userExecutionRole;
	}

	public String getDocumentId() {
		String documentIdInSection = null;

		if (documentId == null) {
			documentIdInSection = getParameterFromSessionAsString(DOCUMENT_ID);
			logger.debug("documentId in Session:" + documentIdInSection);

			if (requestContainsParameter(DOCUMENT_ID)) {
				documentId = getParameterAsString(DOCUMENT_ID);
			} else {
				documentId = documentIdInSection;
				logger.debug("documentId has been taken from session");
			}
		}

		return documentId;
	}

	public String getDocumentLabel() {
		String documentLabelInSession = null;

		if (documentLabel == null) {
			documentLabelInSession = getParameterFromSessionAsString(DOCUMENT_LABEL);
			logger.debug("documentLabel in Session:" + documentLabelInSession);

			if (requestContainsParameter(DOCUMENT_LABEL)) {
				documentLabel = getParameterAsString(DOCUMENT_LABEL);
			} else {
				documentLabel = documentLabelInSession;
				logger.debug("documentLabel has been taken from session");
			}
		}

		return documentLabel;
	}

	public String getDocumentAuthor() {

		if (documentAuthor == null) {
			if (requestContainsParameter(DOCUMENT_AUTHOR)) {
				documentAuthor = getParameterAsString(DOCUMENT_AUTHOR);
			}
		}

		return documentAuthor;
	}

	/**
	 * Gets the audit id.
	 *
	 * @return the audit id
	 */
	public String getAuditId() {
		if (auditId == null) {
			auditId = getParameterAsString(AUDIT_ID);
		}
		return auditId;
	}

	public JSONObject getTemplateAsJSONObject() {
		JSONObject templateJSON = null;
		try {
			String template = getTemplateAsString();
			templateJSON = template != null ? new JSONObject(template) : null;
		} catch (Throwable t) {
			logger.error("Impossible to decode template's content\n" + t);
			throw new SpagoBIRuntimeException("Impossible to decode template's content [" + template.getFileName() + "]", t);

		}

		return templateJSON;
	}

	public SourceBean getTemplateAsSourceBean() {
		SourceBean templateSB = null;
		try {
			String template = getTemplateAsString();
			templateSB = template != null ? SourceBean.fromXMLString(template) : null;
		} catch (SourceBeanException e) {
			logger.error("Impossible to decode template's content\n" + e);
			throw new SpagoBIRuntimeException("Impossible to decode template's content [" + template.getFileName() + "]", e);

		}

		return templateSB;
	}

	public String getTemplateAsString() {
		return getTemplateAsString(false);
	}

	public String getTemplateAsString(boolean forEdit) {
		byte[] template = getTemplate(forEdit);
		return template != null ? new String(template, StandardCharsets.UTF_8) : null;
	}

	public byte[] getTemplate(boolean forEdit) {
		byte[] templateContent = null;

		if (template == null) {
			contentProxy = getContentServiceProxy();
			HashMap requestParameters = ParametersDecoder.getDecodedRequestParameters(getRequestContainer());
			if (forEdit) {
				requestParameters.put(ON_EDIT_MODE, ON_EDIT_MODE);
			}
			template = contentProxy.readTemplate(getDocumentId(), requestParameters);
		}

		if (template != null) {
			templateName = template.getFileName();
			logger.debug("Read the template [" + template.getFileName() + "]");

			Base64.Decoder decoder = Base64.getDecoder();
			templateContent = decoder.decode(template.getContent());
		} else {
			logger.warn("Document template is not defined or it is impossible to get it from the server");
		}

		return templateContent;
	}

	public String getTemplateName() {
		if (templateName == null) {
			contentProxy = getContentServiceProxy();
			HashMap requestParameters = ParametersDecoder.getDecodedRequestParameters(getRequestContainer());
			template = contentProxy.readTemplate(getDocumentId(), requestParameters);
			if (template != null) {
				templateName = template.getFileName();
				logger.debug("Read the template [" + template.getFileName() + "]");
			} else {
				logger.warn("Document template is not defined or it is impossible to get it from the server");
			}
		}
		return templateName;
	}

	public IDataSource getDataSource() {
		if (dataSource == null) {
			String connectionName = getParameterAsString("connectionName");
			if (connectionName != null) {
				logger.debug("Using dataSource passed in as parameter [" + connectionName + "]");
				dataSource = getDataSourceServiceProxy().getDataSourceByLabel(connectionName);
			} else {
				logger.debug("Using default dataSource");
				dataSource = getDataSourceServiceProxy().getDataSource(getDocumentId());
			}

			// handle multischema
			if (dataSource != null && dataSource.checkIsJndi() && dataSource.checkIsMultiSchema()) {
				logger.debug("Multi schema enabled [TRUE]");
				logger.debug("Schema profile atribute name is equals to [" + dataSource.getSchemaAttribute() + "]");

				if (dataSource.getSchemaAttribute() != null) {

					String schema;
					try {
						schema = (String) getUserProfile().getUserAttribute(dataSource.getSchemaAttribute());
						logger.debug("Schema profile atribute value is equals to [" + schema + "]");
						if (schema != null) {
							dataSource.setJndi(dataSource.getJndi() + schema);
						}
					} catch (EMFInternalError e) {
						logger.warn("Impossible to read attribute [" + dataSource.getSchemaAttribute() + "] from profile of user [" + getUserId() + "]", e);
					}

				}
			}

		}

		return dataSource;
	}

	public IDataSet getDataSet() {
		if (dataSet == null) {
			dataSet = getDataSetServiceProxy().getDataSet(getDocumentId());
		}

		return dataSet;
	}

	public Locale getLocale() {
		String language;
		String country;
		String script;

		if (locale == null) {

			logger.debug("IN");

			language = getParameterAsString(LANGUAGE);
			country = getParameterAsString(COUNTRY);
			script = getParameterAsString(SCRIPT);
			logger.debug("Locale parameters received: language = [" + language + "] ; country = [" + country + "]");

			try {
				Builder builder = new Builder().setLanguage(language).setRegion(country);
				if (StringUtils.isNotBlank(script)) {
					builder.setScript(script);
				}
				locale = builder.build();
			} catch (Exception e) {
				logger.debug("Error while creating Locale object from input parameters: language = [" + language + "] ; country = [" + country + "]");
				logger.debug("Creating default locale [en,US].");
				locale = new Locale("en", "US");
			}

			logger.debug("IN");
		}

		return locale;
	}

	public void auditServiceStartEvent() {
		if (getAuditServiceProxy() != null) {
			getAuditServiceProxy().notifyServiceStartEvent();
		} else {
			logger.warn("Impossible to log START-EVENT because the audit proxy has not been instatiated properly");
		}
	}

	public void auditServiceErrorEvent(String msg) {
		if (getAuditServiceProxy() != null) {
			getAuditServiceProxy().notifyServiceErrorEvent(msg);
		} else {
			logger.warn("Impossible to log ERROR-EVENT because the audit proxy has not been instatiated properly");
		}
	}

	public void auditServiceEndEvent() {
		if (getAuditServiceProxy() != null) {
			getAuditServiceProxy().notifyServiceEndEvent();
		} else {
			logger.warn("Impossible to log END-EVENT because the audit proxy has not been instatiated properly");
		}
	}

	public ContentServiceProxy getContentServiceProxy() {
		if (contentProxy == null) {
			contentProxy = new ContentServiceProxy(getUserIdentifier(), getHttpSession());
		}

		return contentProxy;
	}

	public AuditServiceProxy getAuditServiceProxy() {
		if (auditProxy == null && getAuditId() != null) {
			auditProxy = new AuditServiceProxy(getAuditId(), getUserIdentifier(), getHttpSession());
		}

		return auditProxy;
	}

	public EventServiceProxy getEventServiceProxy() {
		if (eventProxy == null) {
			eventProxy = new EventServiceProxy(getUserIdentifier(), getHttpSession());
		}

		return eventProxy;
	}

	public DataSourceServiceProxy getDataSourceServiceProxy() {
		if (datasourceProxy == null) {
			datasourceProxy = new DataSourceServiceProxy(getUserIdentifier(), getHttpSession());
		}

		return datasourceProxy;
	}

	public DataSetServiceProxy getDataSetServiceProxy() {
		if (datasetProxy == null) {
			datasetProxy = new DataSetServiceProxy(getUserIdentifier(), getHttpSession());
		}

		return datasetProxy;
	}

	public DocumentExecuteServiceProxy getDocumentExecuteServiceProxy() {
		if (documentExecuteProxy == null) {
			documentExecuteProxy = new DocumentExecuteServiceProxy(getUserIdentifier(), getHttpSession());
		}

		return documentExecuteProxy;
	}

	public Map getEnv() {
		getCommonEnvInfo();
		env.put(EngineConstants.ENV_DATASOURCE, getDataSource());
		env.put(EngineConstants.ENV_DATASET, getDataSet());
		env.put(EngineConstants.ENV_DOCUMENT_ID, getDocumentId());

		return env;
	}

	public Map getEnvForWidget() {
		getCommonEnvInfo();
		return env;
	}

	private Map getCommonEnvInfo() {
		if (env == null) {
			env = new HashMap();

			copyRequestParametersIntoEnv(env);
			env.put(EngineConstants.ENV_CONTENT_SERVICE_PROXY, getContentServiceProxy());
			env.put(EngineConstants.ENV_AUDIT_SERVICE_PROXY, getAuditServiceProxy());
			env.put(EngineConstants.ENV_EVENT_SERVICE_PROXY, getEventServiceProxy());
			env.put(EngineConstants.ENV_LOCALE, getLocale());
			env.put(EngineConstants.ENV_USER_PROFILE, getUserProfile());
			env.put(EngineConstants.ENV_EXECUTION_ROLE, getUserExecutionRole());
			env.put(EngineConstants.ENV_DOCUMENT_AUTHOR, getDocumentAuthor());
			env.put(EngineConstants.ENV_DOCUMENT_USER, getUserId());
		}

		return env;
	}

	/**
	 * Copy request parameters into env.
	 *
	 * @param env            the env
	 * @param serviceRequest the service request
	 */
	public void copyRequestParametersIntoEnv(Map env) {
		Set parameterStopList = null;
		Iterator parameterNames = null;

		logger.debug("IN");

		try {
			this.getRequest().setCharacterEncoding("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e);
		}

		parameterStopList = new HashSet();
		parameterStopList.add("template");
		parameterStopList.add("ACTION_NAME");
		parameterStopList.add("NEW_SESSION");
		parameterStopList.add("document");
		parameterStopList.add("spagobicontext");
		parameterStopList.add("BACK_END_SPAGOBI_CONTEXT");
		parameterStopList.add("userId");
		parameterStopList.add("auditId");

		parameterNames = getRequestContainer().getKeys().iterator();
		while (parameterNames.hasNext()) {
			String parameterName = null;
			try {
				parameterName = (String) parameterNames.next();
				String parameterValue = (String) this.getParameter(parameterName);

				logger.debug("Parameter [" + parameterName + "] has been read from request");
				logger.debug("Parameter [" + parameterName + "] is of type  " + parameterValue.getClass().getName());
				logger.debug("Parameter [" + parameterName + "] is equal to " + parameterValue);

				if (parameterStopList.contains(parameterName)) {
					logger.debug("Parameter [" + parameterName + "] copyed into environment parameters list: FALSE");
					continue;
				}

				env.put(parameterName, decodeParameterValue("" + parameterValue));
				logger.debug("Parameter [" + parameterName + "] copyed into environment parameters list: TRUE");

			} catch (Exception e) {
				logger.warn("Error while processing request parameter [" + parameterName + "]: " + e.getMessage());
			}
		}

		logger.debug("OUT");
	}

	/**
	 * Decode parameter value.
	 *
	 * @param parValue the par value
	 * @return the string
	 */
	private String decodeParameterValue(String parValue) {
		String newParValue;

		ParametersDecoder decoder = new ParametersDecoder();
		if (decoder.isMultiValues(parValue)) {
			List values = decoder.decode(parValue);
			newParValue = "";
			for (int i = 0; i < values.size(); i++) {
				newParValue += (i > 0 ? "," : "");
				newParValue += values.get(i);
			}
		} else {
			newParValue = parValue;
		}

		return newParValue;
	}

	public String getLocalizedMessage(String msg) {
		if (msg == null)
			return "";
		return EngineMessageBundle.getMessage(msg, getLocale());
	}

}
