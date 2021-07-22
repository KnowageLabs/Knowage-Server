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
package it.eng.spagobi.engines.qbe.services.initializers;

import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.qbe.QbeEngine;
import it.eng.spagobi.engines.qbe.QbeEngineAnalysisState;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.registry.bo.RegistryConfiguration;
import it.eng.spagobi.engines.qbe.registry.serializer.RegistryConfigurationJSONSerializer;
import it.eng.spagobi.engines.qbe.template.QbeTemplateParseException;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.AbstractEngineStartAction;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * The Class QbeEngineStartAction.
 *
 * @author Andrea Gioia
 */
public class QbeEngineStartAction extends AbstractEngineStartAction {

	// INPUT PARAMETERS

	// OUTPUT PARAMETERS
	public static final String LANGUAGE = "LANGUAGE";
	public static final String COUNTRY = "COUNTRY";
	public static final String SCRIPT = "SCRIPT";

	// SESSION PARAMETRES
	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;
	public static final String REGISTRY_CONFIGURATION = "REGISTRY_CONFIGURATION";

	public static final String IS_FEDERATED = "IS_FEDERATED";
	public static final String IS_TECHNICAL_USER = "isTechnicalUser";

	/** Logger component. */
	private static transient Logger logger = Logger.getLogger(QbeEngineStartAction.class);

	public static final String ENGINE_NAME = "SpagoBIQbeEngine";

	@Override
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) {
		QbeEngineInstance qbeEngineInstance = null;
		QbeEngineAnalysisState analysisState;
		Locale locale;

		logger.debug("IN");

		try {
			setEngineName(ENGINE_NAME);
			super.service(serviceRequest, serviceResponse);

			// if(true) throw new SpagoBIEngineStartupException(getEngineName(), "Test exception");
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

			// Add the datyaset
			Map env = addDatasetsToEnv();
			logger.debug("Creating engine instance ...");
			try {
				qbeEngineInstance = QbeEngine.createInstance(templateBean, env);
			} catch (Throwable t) {
				SpagoBIEngineStartupException serviceException;
				Throwable rootException = t;
				while (rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String str = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
				serviceException = new SpagoBIEngineStartupException(ENGINE_NAME, str, t);

				if (rootException instanceof QbeTemplateParseException) {
					QbeTemplateParseException e = (QbeTemplateParseException) rootException;
					serviceException.setDescription(e.getDescription());
					serviceException.setHints(e.getHints());
				}

				throw serviceException;
			}
			logger.debug("Engine instance succesfully created");

			// CHECKS WHETHER IF IT IS A QBE DOCUMENT OR REGISTRY, BY LOOKING AT THE TEMPLATE
			RegistryConfiguration registryConf = qbeEngineInstance.getRegistryConfiguration();
			if (registryConf != null) {
				if (!getUserProfile().isAbleToExecuteAction(SpagoBIConstants.REGISTRY_DATA_ENTRY)) {
					throw new SpagoBIRuntimeException("It is not allowed to use the Registry document.");
				}
				logger.debug("Registry document");
				getServiceResponse().setAttribute("DOCTYPE", "REGISTRY");
				Assert.assertNotNull(registryConf, "Registry configuration not found, check document's template");
				RegistryConfigurationJSONSerializer serializer = new RegistryConfigurationJSONSerializer();
				JSONObject registryConfJSON = serializer.serialize(registryConf);
				setAttribute(REGISTRY_CONFIGURATION, registryConfJSON);

			} else {
				logger.debug("Qbe document");
				getServiceResponse().setAttribute("DOCTYPE", "QBE");
			}

			qbeEngineInstance.setAnalysisMetadata(getAnalysisMetadata());
			if (getAnalysisStateRowData() != null) {
				logger.debug("Loading subobject [" + qbeEngineInstance.getAnalysisMetadata().getName() + "] ...");
				try {
					analysisState = new QbeEngineAnalysisState(qbeEngineInstance.getDataSource());
					analysisState.load(getAnalysisStateRowData());
					qbeEngineInstance.setAnalysisState(analysisState);
				} catch (Throwable t) {
					SpagoBIEngineStartupException serviceException;
					String msg = "Impossible load subobject [" + qbeEngineInstance.getAnalysisMetadata().getName() + "].";
					Throwable rootException = t;
					while (rootException.getCause() != null) {
						rootException = rootException.getCause();
					}
					String str = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
					msg += "\nThe root cause of the error is: " + str;
					serviceException = new SpagoBIEngineStartupException(ENGINE_NAME, msg, t);

					throw serviceException;
				}
				logger.debug("Subobject [" + qbeEngineInstance.getAnalysisMetadata().getName() + "] succesfully loaded");
			}

			locale = (Locale) qbeEngineInstance.getEnv().get(EngineConstants.ENV_LOCALE);

			setAttributeInSession(ENGINE_INSTANCE, qbeEngineInstance);
			setAttribute(ENGINE_INSTANCE, qbeEngineInstance);

			setAttribute(LANGUAGE, locale.getLanguage());
			setAttribute(COUNTRY, locale.getCountry());
			setAttribute(SCRIPT, locale.getScript());
			setAttribute(IS_FEDERATED, isFederated());

			if (getServiceRequest().containsAttribute(IS_TECHNICAL_USER)) {
				String isTech = (String) getServiceRequest().getAttribute(IS_TECHNICAL_USER);
				qbeEngineInstance.setTechnicalUser(Boolean.valueOf(isTech));
			}

		} catch (Throwable e) {
			SpagoBIEngineStartupException serviceException = null;

			if (e instanceof SpagoBIEngineStartupException) {
				serviceException = (SpagoBIEngineStartupException) e;
			} else {
				Throwable rootException = e;
				while (rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String str = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
				String message = "An unpredicted error occurred while executing " + getEngineName() + " service." + "\nThe root cause of the error is: " + str;

				serviceException = new SpagoBIEngineStartupException(getEngineName(), message, e);
			}

			throw serviceException;

			// throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), qbeEngineInstance, e);
		} finally {
			logger.debug("OUT");
		}

	}

	public boolean isFederated() {
		return false;
	}

	public Map addDatasetsToEnv() {
		Map env = getEnv();
		return env;
	}

	@Override
	public Map getEnv() {

		IDataSource dataSource = this.getDataSource();
		Map env = super.getEnv();

		if (dataSource == null || dataSource.checkIsReadOnly()) {
			logger.debug("Getting datasource for writing, since the datasource is not defined or it is read-only");
			IDataSource datasourceForWriting = this.getDataSourceForWriting();
			env.put(EngineConstants.DATASOURCE_FOR_WRITING, datasourceForWriting);
		} else {
			env.put(EngineConstants.DATASOURCE_FOR_WRITING, dataSource);
		}

		return env;
	}

	/**
	 * Gets the datasource of the cache
	 *
	 * @return
	 */
	protected IDataSource getCacheDataSource() {
		logger.debug("Loading the cache datasource");
		String datasourceLabel = (String) getSpagoBIRequestContainer().get(EngineConstants.ENV_DATASOURCE_FOR_CACHE);
		logger.debug("The datasource for cahce is " + datasourceLabel);
		IDataSource dataSource = getDataSourceServiceProxy().getDataSourceByLabel(datasourceLabel);
		logger.debug("cache datasource loaded");
		return dataSource;
	}

}
