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

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.QbeEngine;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.AbstractEngineStartAction;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

/**
 * The Class QbeEngineStartAction.
 *
 * @author Andrea Gioia
 */
public class QbeEngineStartAction extends AbstractEngineStartAction {

	// INPUT PARAMETERS

	/**
	 * 
	 */
	private static final long serialVersionUID = 8592939637002043947L;
	// OUTPUT PARAMETERS
	public static final String LANGUAGE = "LANGUAGE";
	public static final String COUNTRY = "COUNTRY";

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
		Locale locale;

		logger.debug("IN");

		try {
			setEngineName(ENGINE_NAME);
			super.service(serviceRequest, serviceResponse);

			// if(true) throw new SpagoBIEngineStartupException(getEngineName(), "Test exception");
			logger.debug("User Id: " + getUserId());
			logger.debug("Audit Id: " + getAuditId());

			if (getAuditServiceProxy() != null) {
				logger.debug("Audit enabled: [TRUE]");
				getAuditServiceProxy().notifyServiceStartEvent();
			} else {
				logger.debug("Audit enabled: [FALSE]");
			}

			// Add the datyaset
			Map env = getEnvWithProperties();
			logger.debug("Creating engine instance ...");

			
			try {
				qbeEngineInstance = QbeEngine.createInstance(env);
			} catch (Throwable t) {
				SpagoBIEngineStartupException serviceException;
				String msg = "Impossible to create engine instance for document [" + getDocumentId() + "].";
				Throwable rootException = t;
				while (rootException.getCause() != null) {
					rootException = rootException.getCause();
				}
				String str = rootException.getMessage() != null ? rootException.getMessage() : rootException.getClass().getName();
				msg += "\nThe root cause of the error is: " + str;
				serviceException = new SpagoBIEngineStartupException(ENGINE_NAME, msg, t);

				throw serviceException;
			}
			logger.debug("Engine instance succesfully created");


			locale = (Locale) qbeEngineInstance.getEnv().get(EngineConstants.ENV_LOCALE);

			setAttributeInSession(ENGINE_INSTANCE, qbeEngineInstance);
			setAttribute(ENGINE_INSTANCE, qbeEngineInstance);

			setAttribute(LANGUAGE, locale.getLanguage());
			setAttribute(COUNTRY, locale.getCountry());

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

	public Map getEnvWithProperties() {
		return  getEnv();
	}

	//neet to return a value in order to use the get env of the parent class
	public String getDocumentId() {
		return "";
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
