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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.html.View;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.engines.whatif.WhatIfEngine;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplate;
import it.eng.spagobi.engines.whatif.template.WhatIfTemplateParser;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

@Path("/olap/startwhatif")
@ManageAuthorization
public class WhatIfEditStartAction extends WhatIfEngineAbstractStartAction {

	public static transient Logger logger = Logger.getLogger(WhatIfEditStartAction.class);
	private static final String SUCCESS_REQUEST_DISPATCHER_URL_NEW = "/WEB-INF/jsp/edit.jsp";
	private static final String SUCCESS_REQUEST_DISPATCHER_URL_EDIT = "/WEB-INF/jsp/whatIf2.jsp";
	private static final String FAILURE_REQUEST_DISPATCHER_URL = "/WEB-INF/jsp/errors/startupError.jsp";
	private String url = "";
	private boolean whatif;
	@Context
	HttpServletRequest request;
	@Context
	HttpServletResponse response;

	@GET
	@Path("/edit")
	@Produces("text/html")
	public View startActionGet() {
		return startAction();
	}

	@POST
	@Path("/edit")
	@Produces("text/html")
	public View startActionPost() {
		return startAction();
	}

	/**
	 * @return
	 *
	 */
	private View startAction() {
		logger.debug("IN");

		try {
			logger.debug("User Id: " + getUserId());
			logger.debug("Audit Id: " + getAuditId());
			logger.debug("Document Id: " + getDocumentId());

			if (getAuditServiceProxy() != null) {
				logger.debug("Audit enabled: [TRUE]");
				getAuditServiceProxy().notifyServiceStartEvent();
			} else {
				logger.debug("Audit enabled: [FALSE]");
			}

			WhatIfEngineInstance whatIfEngineInstance = null;

			logger.debug("Creating engine instance ...");

			whatIfEngineInstance = WhatIfEngine.createInstance(getEnv());

			logger.debug("Engine instance succesfully created");

			try {
				if (getEnv().get(SpagoBIConstants.SBI_ARTIFACT_ID) != null) {
					SourceBean templateBean = getTemplateAsSourceBean();
					WhatIfTemplateParser wtp = WhatIfTemplateParser.getInstance();
					WhatIfTemplate template = null;
					if (wtp != null) {
						template = wtp.parse(templateBean);
					} else {
						template = null;
					}
					if (getEnv().get("ENGINE").equals("knowageolapengine")) {
						whatif = false;
					} else {
						whatif = true;
					}
					whatIfEngineInstance.updateWhatIfEngineInstance(template, whatif, getEnv());
					logger.debug("Engine instance succesfully updated");

					url = SUCCESS_REQUEST_DISPATCHER_URL_EDIT;
				} else {
					url = SUCCESS_REQUEST_DISPATCHER_URL_NEW;
				}
				getExecutionSession().setAttributeInSession(ENGINE_INSTANCE, whatIfEngineInstance);
				return new View(url);

			} catch (Exception e) {
				logger.error("Error starting the What-If engine: error while forwarding the execution to the jsp " + url, e);
				throw new SpagoBIEngineRuntimeException("Error starting the What-If engine: error while forwarding the execution to the jsp " + url, e);
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

}
