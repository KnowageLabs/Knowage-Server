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

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.user.UserProfileManager;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

/**
 * The org.jboss.resteasy.spi.interception.PreProcessInterceptor runs after a JAX-RS resource method is found to invoke on, but before the actual invocation
 * happens
 *
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
@Provider
@ServerInterceptor
@Precedence("ENCODER")
public class TenantRestProcessInterceptor implements PreProcessInterceptor, PostProcessInterceptor {

	private static Logger logger = Logger.getLogger(TenantRestProcessInterceptor.class);

	@Context
	private HttpServletRequest servletRequest;

	/**
	 * Pre-processes all the REST requests. Get the UserProfile from the session and sets the tenant information into the Thread
	 */
	public ServerResponse preProcess(HttpRequest request, ResourceMethod resourceMethod) throws Failure, WebApplicationException {
		logger.debug("IN");
		UserProfile profile = (UserProfile) servletRequest.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		if (profile == null)
			profile = UserProfileManager.getProfile();
		if (profile != null) {
			logger.debug("User profile retrieved [" + profile + "]");
			// retrieving tenant id
			String tenantId = profile.getOrganization();
			logger.debug("Tenant identifier is [" + tenantId + "]");
			// putting tenant id on thread local
			Tenant tenant = new Tenant(tenantId);
			TenantManager.setTenant(tenant);
		}

		logger.debug("OUT");
		return null;
	}

	/**
	 * Post-processes all the REST requests. Remove tenant's information from thread
	 */
	public void postProcess(ServerResponse response) {
		logger.debug("IN");
		TenantManager.unset();
		logger.debug("OUT");
	}

}
