/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.rest;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.proxy.SecurityServiceProxy;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.filters.FilterIOManager;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

/**
 * The org.jboss.resteasy.spi.interception.PreProcessInterceptor runs after a JAX-RS resource method is found to invoke on, but before the actual invocation
 * happens
 *
 * Similar to SpagoBIAccessFilter but designed for REST services
 *
 */
public class ExternalEngineSecurityServerInterceptor extends AbstractSecurityServerInterceptor implements PreProcessInterceptor, AcceptedByMethod {

	static private Logger logger = Logger.getLogger(ExternalEngineSecurityServerInterceptor.class);

	@Context
	private HttpServletRequest servletRequest;

	@Override
	protected ServerResponse notAuthenticated() {
		return new ServerResponse("", 401, new Headers<Object>());
	}

	@Override
	protected UserProfile authenticateUser() {
		return null;
	}

	@Override
	protected boolean isUserAuthenticatedInSpagoBI() {

		boolean authenticated = true;

		IEngUserProfile engProfile = getUserProfileFromSession();

		if (engProfile != null) {
			logger.debug("User is authenticated and his profile is already stored in session");
		} else {
			engProfile = this.getUserProfileFromUserId();
		}

		if (engProfile != null) {
			logger.debug("User is authenticated but his profile is not already stored in session");
		} else {
			logger.debug("User is not authenticated");
			authenticated = false;
		}

		return authenticated;
	}

	@Override
	protected IEngUserProfile getUserProfileFromSession() {
		IEngUserProfile engProfile = null;
		FilterIOManager ioManager = new FilterIOManager(servletRequest, null);
		ioManager.initConetxtManager();
		engProfile = (IEngUserProfile) ioManager.getContextManager().get(IEngUserProfile.ENG_USER_PROFILE);
		return engProfile;
	}

	@Override
	protected IEngUserProfile createProfile(String userId) {
		SecurityServiceProxy proxy = new SecurityServiceProxy(userId, servletRequest.getSession());
		try {
			return proxy.getUserProfile();
		} catch (SecurityException e) {
			logger.error("Error while creating user profile with user id = [" + userId + "]", e);
			throw new SpagoBIRuntimeException("Error while creating user profile with user id = [" + userId + "]", e);
		}
	}

	public boolean accept(Class arg0, Method arg1) {
		// TODO Auto-generated method stub
		return true;
	}

}
