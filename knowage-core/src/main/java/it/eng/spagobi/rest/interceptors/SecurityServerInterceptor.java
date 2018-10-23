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

import java.util.StringTokenizer;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.axis.encoding.Base64;
import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.rest.AbstractSecurityServerInterceptor;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;
import it.eng.spagobi.services.security.service.SecurityServiceSupplierFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 *
 * Similar to SpagoBIAccessFilter but designed for REST services
 *
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityServerInterceptor extends AbstractSecurityServerInterceptor {

	static private Logger logger = Logger.getLogger(SecurityServerInterceptor.class);

	@Override
	protected void notAuthenticated(ContainerRequestContext requestContext) {
		/*
		 * This response is standard in Basic authentication. If the header with credentials is missing the server send the response asking for the header. The
		 * browser will show a popup that requires the user credential.
		 */
		requestContext.abortWith(Response.status(401).build());
	}

	@Override
	protected UserProfile authenticateUser() {
		UserProfile profile = null;

		logger.trace("IN");

		try {
			/*
			 * author radmila.selakovic@mht.net checking if request header is "X-Auth-Token"
			 */
			if (servletRequest.getHeader("X-Auth-Token") == null) {
				String auto = servletRequest.getHeader("Authorization");
				int position = auto.indexOf("Direct");
				if (position > -1 && position < 5) {// Direct stay at the beginning of the header
					String encodedUser = auto.replaceFirst("Direct ", "");
					byte[] decodedBytes = Base64.decode(encodedUser);
					String user = new String(decodedBytes, "UTF-8");
					profile = (UserProfile) UserUtilities.getUserProfile(user);
				} else {
					String encodedUserPassword = auto.replaceFirst("Basic ", "");
					String credentials = null;
					byte[] decodedBytes = Base64.decode(encodedUserPassword);
					credentials = new String(decodedBytes, "UTF-8");

					StringTokenizer tokenizer = new StringTokenizer(credentials, ":");
					String user = tokenizer.nextToken();
					String password = tokenizer.nextToken();

					ISecurityServiceSupplier supplier = SecurityServiceSupplierFactory.createISecurityServiceSupplier();

					SpagoBIUserProfile spagoBIUserProfile = supplier.checkAuthentication(user, password);
					if (spagoBIUserProfile != null) {
						profile = (UserProfile) UserUtilities.getUserProfile(spagoBIUserProfile.getUniqueIdentifier());
					}
				}
			} else {
				// if request header is
				// "X-Auth-Token chencking authorization will be by access token"
				String token = servletRequest.getHeader("X-Auth-Token");
				ISecurityServiceSupplier supplier = SecurityServiceSupplierFactory.createISecurityServiceSupplier();

				SpagoBIUserProfile spagoBIUserProfile = supplier.checkAuthenticationToken(token);
				if (spagoBIUserProfile != null) {
					profile = (UserProfile) UserUtilities.getUserProfile(spagoBIUserProfile.getUniqueIdentifier());
				}

				servletRequest.getSession().setAttribute(IEngUserProfile.ENG_USER_PROFILE, profile);

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
		boolean authenticated = true;
		IEngUserProfile engProfile = getUserProfileFromSession();

		if (engProfile != null) {
			// verify if the profile stored in session is still valid
			// String userId = null;
			// try {
			// userId = getUserIdentifier();
			// } catch (Exception e) {
			// logger.debug("User identifier not found");
			// }
			// if (userId != null && userId.equals(engProfile.getUserUniqueIdentifier().toString()) == false) {
			// logger.debug("User is authenticated but the profile store in session need to be updated");
			// engProfile = this.getUserProfileFromUserId();
			// } else {
			// logger.debug("User is authenticated and his profile is already stored in session");
			// }

		} else {
			engProfile = this.getUserProfileFromUserId();

			if (engProfile != null) {
				logger.debug("User is authenticated but his profile is not already stored in session");
			} else {
				logger.debug("User is not authenticated");
				authenticated = false;
			}
		}
		return authenticated;
	}

	@Override
	protected IEngUserProfile createProfile(String userId) {
		try {
			return GeneralUtilities.createNewUserProfile(userId);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while creating user profile with user id = [" + userId + "]", e);
		}
	}

	@Override
	protected IEngUserProfile getUserProfileFromSession() {
		return (IEngUserProfile) servletRequest.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	}

	@Override
	protected boolean isBackEndService() {
		String auto = servletRequest.getHeader("Authorization");
		// no header provided
		if (auto == null) {
			return false;
		}
		int position = auto.indexOf("Direct");
		return (position > -1 && position < 5);
	}
}
