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
package it.eng.spagobi.services.rest;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.common.SsoServiceFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.services.rest.annotations.CheckFunctionalitiesParser;
import it.eng.spagobi.services.rest.annotations.PublicService;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 *
 * Similar to SpagoBIAccessFilter but designed for REST services
 *
 */
public abstract class AbstractSecurityServerInterceptor extends AbstractKnowageInterceptor {

	private static final Logger LOGGER = Logger.getLogger(AbstractSecurityServerInterceptor.class);

	@Context
	private ResourceInfo resourceInfo;

	@Context
	protected HttpServletRequest servletRequest;

	/**
	 * Preprocess all the REST requests.
	 *
	 * Get the UserProfile from the session and checks if has the grants to execute the service
	 */
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		LOGGER.trace("IN");

		try {

			Method method = resourceInfo.getResourceMethod();
			LOGGER.info("Receiving request from: " + servletRequest.getRemoteAddr());
			LOGGER.info("Attempt to invoke method [" + method.getName() + "] on class [" + resourceInfo.getResourceClass() + "]");

			if (method.isAnnotationPresent(PublicService.class)) {
				LOGGER.debug("Invoked service is public");
				return;
			}

			UserProfile profile = null;

			// Other checks are required
			boolean authenticated = isUserAuthenticatedInSpagoBI();

			if (!authenticated) {
				LOGGER.debug("User is not authenticated in SpagoBI");

				// try to authenticate the user on the fly using simple-authentication schema
				profile = authenticateUser();
			} else {
				LOGGER.debug("User is already authenticated in SpagoBI");

				// get the user profile from session
				profile = getUserProfileFromSession();
			}

			if (profile == null) {
				notAuthenticated(requestContext);
				return;
			}
			// Profile is not null
			UserProfileManager.setProfile(profile);
			manageTenant(profile);

			// we put user profile in session only in case incoming request is NOT for a back-end service (because back-end services should be treated in a
			// stateless fashion, otherwise number of HTTP sessions will increase with no control) and it is not already stored in session
			if (!isBackEndService() && getUserProfileFromSession() == null) {
				setUserProfileInSession(profile);
			}

			// look for @UserConstraint annotation
			CheckFunctionalitiesParser checkFunctionalitiesParser = new CheckFunctionalitiesParser();

			// the user is authorized for the service if it does not have a user constraint or in case the user satisfies the constraints
			boolean authorized = !checkFunctionalitiesParser.hasUserConstraints(method)
					|| checkFunctionalitiesParser.checkFunctionalitiesByAnnotation(method, profile);

			if (!authorized) {
				try {
					requestContext.abortWith(Response.status(400).entity(ExceptionUtilities.serializeException("not-enabled-to-call-service", null)).build());
				} catch (Exception e) {
					throw new SpagoBIRuntimeException("Error checking if the user [" + profile.getUserName() + "] has the rights to invoke method ["
							+ method.getName() + "] on class [" + resourceInfo.getResourceClass() + "]", e);
				}
			} else {
				LOGGER.debug("The user [" + profile.getUserName() + "] is enabled to invoke method [" + method.getName() + "] on class ["
						+ resourceInfo.getResourceClass() + "]");
			}

		} catch (SpagoBIRuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("An unexpected error occured while preprocessing service request", e);
		}
	}

	private void manageTenant(IEngUserProfile profile) {
		UserProfile userProfile = (UserProfile) profile;
		// retrieving tenant id
		String tenantId = userProfile.getOrganization();
		LOGGER.debug("Retrieved tenantId from user profile object : [" + tenantId + "]");
		// putting tenant id on thread local
		Tenant tenant = new Tenant(tenantId);
		TenantManager.setTenant(tenant);
		LOGGER.debug("Tenant [" + tenantId + "] set into TenantManager");
	}

	/**
	 * It defines the behaviour of the interceptor when the user is not authenticated. It has to be returned a response with status code 401. If Basic
	 * Authentication is used it should be send back also a the "WWW-Authenticate" header as required by Basic Authentication standard.
	 */
	protected abstract void notAuthenticated(ContainerRequestContext requestContext);

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		LOGGER.debug("IN");
		UserProfileManager.unset();
		LOGGER.debug("OUT");
	}

	protected abstract UserProfile authenticateUser();

	protected abstract boolean isUserAuthenticatedInSpagoBI();

	protected abstract boolean isBackEndService();

	protected IEngUserProfile getUserProfileFromUserId() {
		IEngUserProfile engProfile = null;
		String userId = null;

		if (isBackEndService()) {
			return null;
		}
		try {
			userId = getUserIdentifier();
		} catch (Exception e) {
			LOGGER.debug("User identifier not found");
			throw new SpagoBIRuntimeException("User identifier not found", e);
		}

		LOGGER.debug("User id = " + userId);
		if (StringUtilities.isNotEmpty(userId)) {
			try {
				engProfile = createProfile(userId);
			} catch (Exception e) {
				LOGGER.debug("Error creating user profile");
				throw new SpagoBIRuntimeException("Error creating user profile", e);
			}
			setUserProfileInSession(engProfile);
		}

		return engProfile;
	}

	protected abstract IEngUserProfile createProfile(String userId);

	/**
	 * Finds the user identifier from http request or from SSO system (by the http request in input). Use the SsoServiceInterface for read the userId in all
	 * cases, if SSO is disabled use FakeSsoService. Check spagobi_sso.xml
	 *
	 * @param httpRequest The http request
	 *
	 * @return the current user unique identified
	 *
	 * @throws Exception in case the SSO is enabled and the user identifier specified on http request is different from the SSO detected one.
	 */
	protected String getUserIdentifier() throws Exception {
		LOGGER.debug("IN");
		String userId = null;
		try {
			SsoServiceInterface userProxy = SsoServiceFactory.createProxyService();
			userId = userProxy.readUserIdentifier(servletRequest);
		} finally {
			LOGGER.debug("OUT");
		}
		return userId;
	}

	protected void setUserProfileInSession(IEngUserProfile engProfile) {
		servletRequest.getSession().setAttribute(IEngUserProfile.ENG_USER_PROFILE, engProfile);
	}
}
