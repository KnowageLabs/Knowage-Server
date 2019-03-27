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
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.services.rest.AbstractKnowageInterceptor;

/**
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 *         Updates the audit log for the successful requests. For the services that throw exceptions look at RestExceptionMapper
 *
 */
@Provider
public class AuditRestPostInterceptor extends AbstractKnowageInterceptor {

	static private Logger logger = Logger.getLogger(AuditRestPostInterceptor.class);

	@Context
	private ResourceInfo resourceInfo;

	@Context
	private HttpServletRequest servletRequest;

	@Context
	private UriInfo uriInfo;

	/**
	 * Postprocess all the REST requests.. Add an entry into the audit log for every rest service
	 */
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		logger.debug("AuditRestInterceptor:postProcess IN");
		try {
			String serviceUrl = uriInfo.getPath();
			MultivaluedMap<String, String> parameters = uriInfo.getPathParameters();

			UserProfile profile = this.getUserProfile();

			String action = "[Service:" + serviceUrl + " ; Class:" + resourceInfo.getResourceClass() + " ; Method:" + resourceInfo.getResourceMethod() + "]";

			String result = "";
			if (responseContext.getStatusInfo().getStatusCode() == 200) {
				result = "OK";
			} else {
				result = "ERR (" + responseContext.getStatusInfo().getStatusCode() + ")";
			}
			AuditLogUtilities.updateAudit(servletRequest, profile, action, InterceptorUtilities.fromMultivaluedMapToHashMap(parameters), result);
		} finally {
			logger.debug("AuditRestInterceptor:postProcess OUT");
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
	}

}