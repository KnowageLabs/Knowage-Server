/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.engines.qbe.QbeEngine;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.api.AbstractQbeEngineResource;
import it.eng.spagobi.engines.qbe.template.QbeTemplateParseException;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineStartupException;

@Path("/start-qbe")
public class QbeEngineStartResource extends AbstractQbeEngineResource {

	public static final String ENGINE_NAME = "SpagoBIQbeEngine";
	public static final String ENGINE_INSTANCE = EngineConstants.ENGINE_INSTANCE;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startQbe(@QueryParam("datamart") String datamart) {

		QbeEngineInstance qbeEngineInstance = null;

		logger.debug("IN");

		try {
			SourceBean templateBean = getTemplateAsSourceBean(datamart);
			logger.debug("Template: " + templateBean);
			logger.debug("Creating engine instance ...");
			try {
				qbeEngineInstance = QbeEngine.createInstance(templateBean, getEnv());
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

			setAttribute(ENGINE_INSTANCE, qbeEngineInstance);

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
				String message = "An unpredicted error occurred while executing " + ENGINE_NAME + " service." + "\nThe root cause of the error is: " + str;

				serviceException = new SpagoBIEngineStartupException(ENGINE_NAME, message, e);
			}

			throw serviceException;

		} finally {
			logger.debug("OUT");
		}

		return Response.ok().build();
	}

	public SourceBean getTemplateAsSourceBean(String modelName) {
		try {
			SourceBean qbeSB = new SourceBean("QBE");
			SourceBean datamartSB = new SourceBean("DATAMART");
			datamartSB.setAttribute("name", modelName);
			qbeSB.setAttribute(datamartSB);
			return qbeSB;
		} catch (SourceBeanException e) {
			SpagoBIEngineStartupException engineException = new SpagoBIEngineStartupException(ENGINE_NAME,
					"Impossible to create a new template for the model " + modelName, e);
			engineException.setDescription("Impossible to parse template's content:  " + e.getMessage());
			engineException.addHint("Check if the document's template is a well formed xml file");
			throw engineException;
		}

	}

}
