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

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.configuration.FileCreatorConfiguration;
import it.eng.spagobi.utilities.service.AbstractBaseServlet;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractEngineStartServlet extends AbstractBaseServlet {
	
	private static final String MSG_OK = "msgKO";

	/**
	 * Logger component
	 */
	private static final Logger LOGGER = LogManager.getLogger(AbstractEngineStartServlet.class);

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		String path = getServletConfig().getServletContext().getRealPath("/WEB-INF");
		ConfigSingleton.setConfigurationCreation(new FileCreatorConfiguration(path));
		ConfigSingleton.setRootPath(path);
		ConfigSingleton.setConfigFileName("/empty.xml");
	}

	@Override
	public final void doService(BaseServletIOManager servletIOManager) {

		EngineStartServletIOManager engineServletIOManager;

		engineServletIOManager = new EngineStartServletIOManager(servletIOManager);

		try {
			this.service(engineServletIOManager);
		} catch (Exception e) {
			handleException(servletIOManager, e);
		}

	}
	
	public abstract void service(EngineStartServletIOManager servletIOManager) throws SpagoBIEngineException;

	public final void doService(EngineStartServletIOManager servletIOManager) {

		LOGGER.debug("User Id: {}", servletIOManager.getUserId());
		LOGGER.debug("Audit Id: {}", servletIOManager.getAuditId());
		LOGGER.debug("Document Id: {}", servletIOManager.getDocumentId());
		LOGGER.debug("Template: {}", servletIOManager.getTemplateAsSourceBean());

	}

	public void handleException(EngineStartServletIOManager servletIOManager, Throwable t) {
		LOGGER.error("Service execution failed", t);

		servletIOManager.auditServiceErrorEvent(t.getMessage());

		String reponseMessage = servletIOManager.getLocalizedMessage("msg.error.generic");
		if (t instanceof SpagoBIEngineException) {
			SpagoBIEngineException e = (SpagoBIEngineException) t;
			if (e.getI18NCode() != null) {
				reponseMessage = servletIOManager.getLocalizedMessage(e.getI18NCode());
			} else {
				reponseMessage = servletIOManager.getLocalizedMessage(e.getMessage());
			}

			String nextJSP = "/jsp/errors/error.jsp";
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(nextJSP);
			HttpServletRequest req = servletIOManager.getRequest();
			if (req.getAttribute(MSG_OK) != null)
				req.removeAttribute(MSG_OK);
			req.setAttribute(MSG_OK, (reponseMessage));
			servletIOManager.setRequest(req);

			try {
				dispatcher.forward(servletIOManager.getRequest(), servletIOManager.getResponse());
			} catch (ServletException | IOException e1) {
				throw new SpagoBIEngineRuntimeException(e1);
			}

		} else
			servletIOManager.tryToWriteBackToClient(reponseMessage);
	}

	@Override
	public void handleException(BaseServletIOManager servletIOManager, Throwable t) {
		handleException(new EngineStartServletIOManager(servletIOManager), t);
	}

}