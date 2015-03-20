/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.rest.interceptors;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;

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
 * The org.jboss.resteasy.spi.interception.PreProcessInterceptor runs after a JAX-RS resource 
 * method is found to invoke on, but before the actual invocation happens
 * 
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
@Provider
@ServerInterceptor
@Precedence("ENCODER")
public class TenantRestProcessInterceptor implements PreProcessInterceptor, PostProcessInterceptor {

	private static Logger logger = Logger
			.getLogger(TenantRestProcessInterceptor.class);

	@Context
	private HttpServletRequest servletRequest;

	/**
	 * Pre-processes all the REST requests. Get the UserProfile from the session
	 * and sets the tenant information into the Thread
	 */
	public ServerResponse preProcess(HttpRequest request,
			ResourceMethod resourceMethod) throws Failure,
			WebApplicationException {
		logger.debug("IN");
		UserProfile profile = (UserProfile) servletRequest.getSession()
				.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		if(profile != null){
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
	public void postProcess(ServerResponse response){
		logger.debug("IN");
		TenantManager.unset();
		logger.debug("OUT");
	}



}
