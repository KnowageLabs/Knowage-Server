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
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.whatif.WhatIfEngine;
import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

@Path("/start-standalone")
public class WhatIfEngineStartStandAloneAction extends AbstractWhatIfEngineService {

	// INPUT PARAMETERS
	public static final String LANGUAGE = "language";
	public static final String COUNTRY = "country";

	// OUTPUT PARAMETERS

	// SESSION PARAMETRES
	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;

	// Defaults
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

	/** Logger component. */
	public static transient Logger logger = Logger.getLogger(WhatIfEngineStartAction.class);

	private static final String REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/whatIf2.jsp";

	@GET
	@Produces("text/html")
	public void startAction(@Context HttpServletResponse response) {

		logger.debug("IN");

		try {

			WhatIfEngineInstance whatIfEngineInstance = null;

			logger.debug("Creating engine instance ...");

			try {
				logger.debug(WhatIfEngineConfig.getInstance().getTemplateFilePath());
				SourceBean template = SourceBean.fromXMLFile(WhatIfEngineConfig.getInstance().getTemplateFilePath());
				whatIfEngineInstance = WhatIfEngine.createInstance(template, getEnv());
			} catch (Exception e) {
				logger.error("Error starting the What-If engine: error while generating the engine instance.", e);
				throw new SpagoBIEngineRuntimeException("Error starting the What-If engine: error while generating the engine instance.", e);
			}
			logger.debug("Engine instance succesfully created");

			getExecutionSession().setAttributeInSession(ENGINE_INSTANCE, whatIfEngineInstance);

			try {
				servletRequest.getRequestDispatcher(REQUEST_DISPATCHER_URL).forward(servletRequest, response);
			} catch (Exception e) {
				logger.error("Error starting the What-If engine: error while forwarding the execution to the jsp " + REQUEST_DISPATCHER_URL, e);
				throw new SpagoBIEngineRuntimeException(
						"Error starting the What-If engine: error while forwarding the execution to the jsp " + REQUEST_DISPATCHER_URL, e);
			}

		} catch (Exception e) {
			logger.error("Error starting the What-If engine", e);
			throw new SpagoBIEngineRuntimeException("Error starting the What-If engine", e);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public Map getEnv() {
		Map env = new HashMap();

		env.put(EngineConstants.ENV_LOCALE, this.getLocale());

		return env;
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
				logger.error("Language and country not specified in request. Considering default locale that is " + DEFAULT_LOCALE.toString());
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