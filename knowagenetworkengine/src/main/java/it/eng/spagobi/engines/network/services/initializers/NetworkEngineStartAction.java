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

package it.eng.spagobi.engines.network.services.initializers;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.network.NetworkEngine;
import it.eng.spagobi.engines.network.NetworkEngineInstance;
import it.eng.spagobi.engines.network.bean.INetwork;
import it.eng.spagobi.engines.network.businness.NetworkBuilder;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.engines.AbstractEngineStartAction;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Entry point action.
 */
public class NetworkEngineStartAction extends AbstractEngineStartAction {

	// INPUT PARAMETERS

	/**
	 *
	 */
	private static final long serialVersionUID = -3424138720053064514L;
	// OUTPUT PARAMETERS
	public static final String LANGUAGE = "LANGUAGE";
	public static final String COUNTRY = "COUNTRY";

	// SESSION PARAMETRES
	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(NetworkEngineStartAction.class);

	private static final String ENGINE_NAME = "knowagenetworkengine";

	// private static final String REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/network.jsp";

	@Override
	public void service(SourceBean serviceRequest, SourceBean serviceResponse) {
		logger.debug("IN");
		Locale locale;
		NetworkEngineInstance networkEngineInstance = null;

		try {
			setEngineName(ENGINE_NAME);
			super.service(serviceRequest, serviceResponse);

			logger.debug("User Id: " + getUserId());
			logger.debug("Audit Id: " + getAuditId());
			logger.debug("Document Id: " + getDocumentId());
			logger.debug("Template: " + getTemplateAsString());

			if (getAuditServiceProxy() != null) {
				logger.debug("Audit enabled: [TRUE]");
				getAuditServiceProxy().notifyServiceStartEvent();
			} else {
				logger.debug("Audit enabled: [FALSE]");
			}

			logger.debug("Creating engine instance ...");
			Map env = getEnv();
			try {
				networkEngineInstance = NetworkEngine.createInstance(getTemplateAsString(), env);
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

			IDataSet dataset = getDataSet();
			dataset.setParamsMap(env);
			INetwork net = NetworkBuilder.buildNetwork(dataset, networkEngineInstance.getTemplate());
			networkEngineInstance.setNet(net);

			logger.debug("Engine instance succesfully created");

			locale = (Locale) networkEngineInstance.getEnv().get(EngineConstants.ENV_LOCALE);

			setAttributeInSession(ENGINE_INSTANCE, networkEngineInstance);
			setAttribute(ENGINE_INSTANCE, networkEngineInstance);

			setAttribute(LANGUAGE, locale.getLanguage());
			setAttribute(COUNTRY, locale.getCountry());

		} catch (Exception e) {
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
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public IDataSet getDataSet() {
		return getDataSetServiceProxy().getDataSet(getDocumentId());
	}

}