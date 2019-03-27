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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.proxy.SecurityServiceProxy;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * The org.jboss.resteasy.spi.interception.PreProcessInterceptor runs after a JAX-RS resource method is found to invoke on, but before the actual invocation
 * happens
 *
 * Similar to SpagoBIAccessFilter but designed for REST services
 *
 */
public class ExternalEngineSecurityServerInterceptor extends AbstractSecurityServerInterceptor {

	static private Logger logger = Logger.getLogger(ExternalEngineSecurityServerInterceptor.class);

	@Context
	private HttpServletRequest servletRequest;

	@Override
	protected void notAuthenticated(ContainerRequestContext requestContext) {
		requestContext.abortWith(Response.status(401).build());
	}

	@Override
	protected UserProfile authenticateUser() {
		UserProfile profile = null;

		logger.trace("IN");

		try {
			String auto = servletRequest.getHeader("Authorization");
			if (auto != null) {
				int position = auto.indexOf("Direct");
				if (position > -1 && position < 5) {// Direct stay at the beginning of the header
					String encodedUser = auto.replaceFirst("Direct ", "");
					byte[] decodedBytes = Base64.decode(encodedUser);
					String userId = new String(decodedBytes, "UTF-8");
					SecurityServiceProxy proxy = new SecurityServiceProxy(userId, servletRequest.getSession());
					profile = (UserProfile) proxy.getUserProfile();
				}
			}
		} catch (Throwable t) {
			logger.trace("Problem during authentication, returning null", t);
		} finally {
			logger.trace("OUT");
		}

		return profile;
	}

	@Override
	protected boolean isUserAuthenticatedInSpagoBI() {
		logger.debug("IN");
		boolean authenticated = true;

		try {
			IEngUserProfile engProfile = getUserProfileFromSession();

			if (engProfile != null) {
				logger.debug("User is authenticated and his profile is already stored in session");
			} else {
				// TODO THIS NEED TO BE FIXED -> THIS METHOD SHOULD ONLY VERIFY IF THE USER IS IN SESSION, NOT TRYING TO AUTHENTICATE IT
				engProfile = this.getUserProfileFromUserId();
			}

			if (engProfile != null) {
				logger.debug("User is authenticated but his profile is not already stored in session");
			} else {
				logger.debug("User is not authenticated");
				authenticated = false;
			}
		} catch (Exception e) {
			logger.debug("Error while attempt to find user profile in session or authenticate user. Returning [false]", e);
			authenticated = false;
		}

		logger.debug("OUT");
		return authenticated;
	}

	// @Override
	// protected void setUserProfileInSession(IEngUserProfile engProfile) {
	// super.setUserProfileInSession(engProfile);
	// FilterIOManager ioManager = new FilterIOManager(servletRequest, null);
	// ioManager.initConetxtManager();
	// ioManager.getContextManager().set(IEngUserProfile.ENG_USER_PROFILE, engProfile);
	// }

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

	@Override
	protected boolean isBackEndService() {
		// TODO Auto-generated method stub
		return false;
	}

}
