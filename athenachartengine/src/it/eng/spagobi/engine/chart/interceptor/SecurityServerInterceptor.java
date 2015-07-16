/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engine.chart.interceptor;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.engine.chart.ChartEngineRuntimeException;
import it.eng.spagobi.engine.chart.api.ExceptionUtilities;
import it.eng.spagobi.engine.chart.api.SecurityServiceSupplierFactory;
import it.eng.spagobi.security.ExternalServiceController;
import it.eng.spagobi.services.common.SsoServiceFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.filters.FilterIOManager;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.LoggableFailure;
import org.jboss.resteasy.spi.interception.AcceptedByMethod;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

/**
 * The org.jboss.resteasy.spi.interception.PreProcessInterceptor runs after a JAX-RS resource method is found to invoke on, but before the actual invocation
 * happens
 * 
 * Similar to SpagoBIAccessFilter but designed for REST services
 * 
 */
@Provider
@ServerInterceptor
@Precedence("SECURITY")
public class SecurityServerInterceptor implements PreProcessInterceptor, AcceptedByMethod {

	static private Logger logger = Logger.getLogger(SecurityServerInterceptor.class);

	@Context
	private HttpServletRequest servletRequest;

	/**
	 * Preprocess all the REST requests.
	 * 
	 * Get the UserProfile from the session and checks if has the grants to execute the service
	 */
	@Override
	public ServerResponse preProcess(HttpRequest request, ResourceMethod resourceMethod) throws Failure, WebApplicationException {

		ServerResponse response;

		logger.trace("IN");

		response = null;
		try {
			String serviceUrl = InterceptorUtilities.getServiceUrl(request);
			serviceUrl = serviceUrl.replaceAll("/1.0/", "/");
			int index = serviceUrl.indexOf("/", 1);
			if (index > 0) {
				serviceUrl = serviceUrl.substring(0, index);
			}

			String methodName = resourceMethod.getMethod().getName();

			logger.info("Receiving request from: " + servletRequest.getRemoteAddr());
			logger.info("Attempt to invoke method [" + methodName + "] on class [" + resourceMethod.getResourceClass().getName() + "]");

			// Check for Services that can be invoked externally without user
			// login in SpagoBI
			ExternalServiceController externalServiceController = ExternalServiceController.getInstance();
			boolean isExternalService = externalServiceController.isExternalService(serviceUrl);
			if (isExternalService == true) {
				// TODO check if here we need to create and put in session a
				// profile for the guest user
				return null; // we return null to continue with the service
								// execution
			}

			UserProfile profile = null;

			// Other checks are required
			boolean authenticated = isUserAuthenticatedInSpagoBI();
			if (!authenticated) {
				// try to authenticate the user on the fly using
				// simple-authentication schema
				profile = authenticateUser();
			} else {
				// get the user profile from session
				profile = (UserProfile) getUserProfileFromSession();
			}

			if (profile == null) {
				// TODO check if the error can be processed by the client
				// throws unlogged user exception that will be managed by
				// RestExcepionMapper
				logger.info("User not logged");
				throw new LoggableFailure(request.getUri().getRequestUri().getPath());
			}

			boolean authorized = false;
			try {
				authorized = profile.isAbleToExecuteService(serviceUrl);
			} catch (Exception e) {
				logger.debug("Error checking if the user [" + profile.getUserName() + "] has the rights to call the service [" + serviceUrl + "]", e);
				throw new SpagoBIRuntimeException("Error checking if the user [" + profile.getUserName() + "] has the rights to call the service ["
						+ serviceUrl + "]", e);
			}

			if (!authorized) {
				try {
					return new ServerResponse(ExceptionUtilities.serializeException("not-enabled-to-call-service", null), 400, new Headers<Object>());
				} catch (Exception e) {
					throw new SpagoBIRuntimeException("Error checking if the user [" + profile.getUserName() + "] has the rights to call the service ["
							+ serviceUrl + "]", e);
				}
			} else {
				logger.debug("The user [" + profile.getUserName() + "] is enabled to execute the service [" + serviceUrl + "]");
			}
		} catch (Exception e) {
			if (e instanceof ChartEngineRuntimeException) {
				// ok it's a known exception
			} else {
				throw new ChartEngineRuntimeException("An unexpected error occured while preprocessing service request", e);
			}
			String msg = e.getMessage();
			if (e.getCause() != null && e.getCause().getMessage() != null)
				msg += ": " + e.getCause().getMessage();
			response = new ServerResponse(msg, 400, new Headers<Object>());
		} finally {
			logger.trace("OUT");
		}

		return response;
	}

	private UserProfile authenticateUser() {
		UserProfile profile = null;

		logger.trace("IN");

		try {
			String user = servletRequest.getHeader("user");
			String password = servletRequest.getHeader("password");

			ISecurityServiceSupplier supplier = SecurityServiceSupplierFactory.createISecurityServiceSupplier();

			SpagoBIUserProfile spagoBIUserProfile = supplier.checkAuthentication(user, password);
			if (spagoBIUserProfile != null) {
				profile = (UserProfile) UserUtilities.getUserProfile(user);
			}
		} catch (Exception e) {
			throw new RuntimeException("An unexpected error occured while authenticating user", e);
		} finally {
			logger.trace("OUT");
		}

		return profile;
	}

	private boolean isUserAuthenticatedInSpagoBI() {

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

	private IEngUserProfile getUserProfileFromSession() {
		IEngUserProfile engProfile = null;
		FilterIOManager ioManager = new FilterIOManager(servletRequest, null);
		ioManager.initConetxtManager();
		engProfile = (IEngUserProfile) ioManager.getContextManager().get(IEngUserProfile.ENG_USER_PROFILE);
		return engProfile;
	}

	private void setUserProfileInSession(IEngUserProfile engProfile) {
		FilterIOManager ioManager = new FilterIOManager(servletRequest, null);
		ioManager.initConetxtManager();
		ioManager.getContextManager().set(IEngUserProfile.ENG_USER_PROFILE, engProfile);
	}

	private IEngUserProfile getUserProfileFromUserId() {
		IEngUserProfile engProfile = null;
		String userId = null;
		try {
			userId = getUserIdentifier();
		} catch (Exception e) {
			logger.debug("User identifier not found");
			throw new SpagoBIRuntimeException("User identifier not found", e);
		}

		logger.debug("User id = " + userId);
		if (StringUtilities.isNotEmpty(userId)) {
			try {
				engProfile = GeneralUtilities.createNewUserProfile(userId);
			} catch (Exception e) {
				logger.error("Error while creating user profile with user id = [" + userId + "]", e);
				throw new SpagoBIRuntimeException("Error while creating user profile with user id = [" + userId + "]", e);
			}
			setUserProfileInSession(engProfile);
		}

		return engProfile;
	}

	/**
	 * Finds the user identifier from http request or from SSO system (by the http request in input). Use the SsoServiceInterface for read the userId in all
	 * cases, if SSO is disabled use FakeSsoService. Check spagobi_sso.xml
	 * 
	 * @param httpRequest
	 *            The http request
	 * 
	 * @return the current user unique identified
	 * 
	 * @throws Exception
	 *             in case the SSO is enabled and the user identifier specified on http request is different from the SSO detected one.
	 */

	private String getUserIdentifier() throws Exception {
		logger.debug("IN");
		String userId = null;
		try {
			SsoServiceInterface userProxy = SsoServiceFactory.createProxyService();
			userId = userProxy.readUserIdentifier(servletRequest);
		} finally {
			logger.debug("OUT");
		}
		return userId;
	}

	@Override
	public boolean accept(Class arg0, Method arg1) {
		// TODO Auto-generated method stub
		return true;
	}

}
