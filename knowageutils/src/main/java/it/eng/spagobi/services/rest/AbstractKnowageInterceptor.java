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
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.user.UserProfileManager;

public abstract class AbstractKnowageInterceptor implements ContainerRequestFilter, ContainerResponseFilter {

	private static Logger logger = Logger.getLogger(AbstractKnowageInterceptor.class);

	@Context
	private HttpServletRequest servletRequest;

	protected UserProfile getUserProfile() {
		logger.debug("IN");
		logger.debug("Looking for user profile in HTTP session ...");
		UserProfile toReturn = getUserProfileFromSession();
		if (toReturn == null) {
			logger.debug("User profile not found in HTTP session, looking for user profile in thread local ...");
			toReturn = getUserProfileFromThreadLocal();
		}
		LogMF.debug(logger, "OUT: returning {0}", toReturn);
		return toReturn;
	}

	protected UserProfile getUserProfileFromSession() {
		logger.debug("IN");
		UserProfile toReturn = null;
		HttpSession session = servletRequest.getSession(false);
		if (session != null) {
			logger.debug("Incoming request has an associated session, looking for user profile object into it...");
			toReturn = (UserProfile) session.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		} else {
			logger.debug("Incoming request hasn't any associated session.");
		}
		LogMF.debug(logger, "OUT: returning {0}", toReturn);
		return toReturn;
	}

	protected UserProfile getUserProfileFromThreadLocal() {
		logger.debug("IN");
		UserProfile toReturn = UserProfileManager.getProfile();
		LogMF.debug(logger, "OUT: returning {0}", toReturn);
		return toReturn;
	}

}
