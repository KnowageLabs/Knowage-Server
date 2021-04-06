/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.spagobi.services.proxy;

import java.net.URL;
import java.security.Principal;

import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.security.SecurityService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 *
 * Security Service Proxy. Use in engine component only
 *
 */
public final class SecurityServiceProxy extends AbstractServiceProxy {

	private static final String SERVICE_NAME = "Security Service";

	private static final QName SERVICE_QNAME = new QName("http://security.services.spagobi.eng.it/", "SecurityService");

	private static Logger logger = Logger.getLogger(SecurityServiceProxy.class);

	/**
	 * Use this constructor.
	 *
	 * @param user
	 *            user ID
	 * @param session
	 *            HttpSession
	 */
	public SecurityServiceProxy(String user, HttpSession session) {
		super(user, session);
	}

	/**
	 * Don't use it.
	 */
	private SecurityServiceProxy() {
		super();
	}

	/**
	 * @return Object used
	 * @throws SecurityException
	 *             catch this if exist error
	 */
	private SecurityService lookUp() throws SecurityException {
		SecurityService service;

		service = null;
		try {
			if (serviceUrl != null) {
				URL serviceUrlWithWsdl = new URL(serviceUrl.toString() + "?wsdl");

				service = Service.create(serviceUrlWithWsdl, SERVICE_QNAME).getPort(SecurityService.class);
			} else {
				service = Service.create(SERVICE_QNAME).getPort(SecurityService.class);
			}
		} catch (Throwable e) {
			logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
			throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
		}

		return service;
	}

	/**
	 * Gets the user profile.
	 *
	 * @return IEngUserProfile with user profile
	 *
	 * @throws SecurityException
	 *             if the process has generated an error
	 */
	@Override
	public IEngUserProfile getUserProfile() throws SecurityException {
		UserProfile userProfile;

		logger.debug("IN");

		userProfile = null;
		try {
			if (UserProfile.isSchedulerUser(userId)) {
				try {
					userProfile = UserProfile.createSchedulerUserProfile(userId);
				} catch (SpagoBIRuntimeException e) {
					userProfile = UserProfile.createSchedulerUserProfile();
				}
			} else {
				SpagoBIUserProfile user = lookUp().getUserProfile(readTicket(), userId);
				if (user != null) {
					userProfile = new UserProfile(user);
				} else {
					logger.error("Error occured while retrieving user profile of user [" + userId + "] from service [" + SERVICE_NAME + "] at endpoint ["
							+ serviceUrl + "]. user is null!");
				}
			}
		} catch (Throwable e) {
			logger.error("Error occured while retrieving user profile of user [" + userId + "] from service [" + SERVICE_NAME + "] at endpoint [" + serviceUrl
					+ "]", e);
			throw new SecurityException("Error occured while retrieving user profile of user [" + userId + "] from service [" + SERVICE_NAME
					+ "] at endpoint [" + serviceUrl + "]", e);
		} finally {
			logger.debug("OUT");
		}

		return userProfile;
	}

	/**
	 * Check if the user is authorized to access the folder.
	 *
	 * @param folderId
	 *            folder id
	 * @param mode
	 *            mode
	 *
	 * @return true/false
	 */
	public boolean isAuthorized(String folderId, String mode) {

		logger.debug("IN");
		try {
			return lookUp().isAuthorized(readTicket(), userId, folderId, mode);
		} catch (Throwable e) {
			logger.error("Error occured while retrieving access right to folder [" + folderId + "] for user [" + folderId + "] in modality [" + mode + "]");
		} finally {
			logger.debug("OUT");
		}
		return false;
	}

	/**
	 * Check if the user can execute the function ( user function ).
	 *
	 * @param function
	 *            function id
	 *
	 * @return true/false
	 */
	public boolean checkAuthorization(String function) {
		return false;
	}

	/**
	 * Check if the user can execute the function ( user function ).
	 *
	 * @param function
	 *            function
	 * @param principal
	 *            user principal
	 *
	 * @return true / false
	 */
	public boolean checkAuthorization(Principal principal, String function) {
		return false;
	}
}
