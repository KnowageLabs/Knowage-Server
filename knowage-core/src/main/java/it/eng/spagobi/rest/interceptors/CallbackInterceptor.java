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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

/**
 *
 *

 *
 */
@Provider
public class CallbackInterceptor implements ContainerResponseFilter {

	private static Logger logger = Logger.getLogger(CallbackInterceptor.class);

	@Context
	private HttpServletResponse httpResponse;


	@Context
	private HttpServletRequest httpRequest;

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		logger.debug("IN");

		if (httpRequest.getParameter("callback") != null && !httpRequest.getParameter("callback").equals("")) {
			String callback = httpRequest.getParameter("callback");
			logger.debug("Add callback to entity response: " + callback);
//			String entity = httpResponse.getEntity().toString();
//			String entityModified = callback + "(" + entity + ");";
//			httpResponse.setEntity(entityModified);
		}
		;
		logger.debug("OUT");
	}

}
