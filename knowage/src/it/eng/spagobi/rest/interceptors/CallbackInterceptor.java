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
package it.eng.spagobi.rest.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;

/**
 * 
 *

 *
 */
@Provider
@ServerInterceptor
public class CallbackInterceptor implements PostProcessInterceptor {

	private static Logger logger = Logger.getLogger(CallbackInterceptor.class);

	@Context
	private HttpServletRequest servletRequest;

	/**
	 * Post-processes all the REST requests. Remove tenant's information from thread
	 */
	@Override
	public void postProcess(ServerResponse response) {
		logger.debug("IN");

		if (servletRequest.getParameter("callback") != null && !servletRequest.getParameter("callback").equals("")) {
			String callback = servletRequest.getParameter("callback");
			logger.debug("Add callback to entity response: " + callback);
			String entity = response.getEntity().toString();
			String entityModified = callback + "(" + entity + ");";
			response.setEntity(entityModified);
		}
		;
		logger.debug("OUT");
	}

}
