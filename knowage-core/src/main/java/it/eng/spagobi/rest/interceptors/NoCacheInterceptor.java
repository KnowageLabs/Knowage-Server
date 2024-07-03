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

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

/**
 * This interceptor injects into HTTP response headers information to disable browser cache, in case the response does not already contain any header about
 * cache, i.e. "CacheControl", "Pragma" and "Expires".
 *
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
@Provider
public class NoCacheInterceptor implements ContainerResponseFilter {

	private static Logger logger = Logger.getLogger(NoCacheInterceptor.class);

	@Context
	private HttpServletResponse httpResponse;

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		logger.debug("IN");
		if (!httpResponse.containsHeader("CacheControl") && !httpResponse.containsHeader("Pragma") && !httpResponse.containsHeader("Expires")) {
			httpResponse.setHeader("CacheControl", "no-cache");
			httpResponse.setHeader("Pragma", "no-cache");
			httpResponse.setHeader("Expires", "-1");
		}
		logger.debug("OUT");
	}

}
