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
package it.eng.spagobi.services.security.service;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.common.AbstractServiceImpl;
import it.eng.spagobi.services.security.SecurityService;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * This class create the user profile and implements the security check
 * 
 * @author Bernabei Angelo
 * 
 */
public class SecurityServiceImpl extends AbstractServiceImpl implements
		SecurityService {

	static private Logger logger = Logger.getLogger(SecurityServiceImpl.class);

	/**
	 * Instantiates a new security service impl.
	 */
	public SecurityServiceImpl() {
		super();
	}

	/**
	 * User profile creation.
	 * 
	 * @param token
	 *            the token
	 * @param userId
	 *            the user id
	 * 
	 * @return the user profile
	 */
	public SpagoBIUserProfile getUserProfile(String token, String userId) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.security.getUserProfile");
		try {
			validateTicket(token, userId);
			UserProfile userProfile = (UserProfile) UserUtilities.getUserProfile(userId);
			return userProfile.getSpagoBIUserProfile();
		} catch (Exception e) {
			logger.error("An exception occurred while creating user profile for user " + userId, e);
			return null;
		} finally {
			monitor.stop();
			logger.debug("OUT");
		}

	}

	/**
	 * check if user can access to the folder "idFolder".
	 * 
	 * @param token
	 *            the token
	 * @param userId
	 *            the user id
	 * @param idFolder
	 *            the id folder
	 * @param state
	 *            the state
	 * 
	 * @return true, if checks if is authorized
	 */
	public boolean isAuthorized(String token, String userId, String idFolder,
			String state) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.security.isAuthorized");
		try {
			validateTicket(token, userId);
			UserProfile userProfile = (UserProfile) UserUtilities
					.getUserProfile(userId);
			this.setTenantByUserProfile(userProfile);
			return ObjectsAccessVerifier.canExec(new Integer(idFolder),
					userProfile);
		} catch (Exception e) {
			logger.error(
					"An exception occurred while checking authorization for user "
							+ userId + " to folder " + idFolder
							+ " with state " + state, e);
			return false;
		} finally {
			this.unsetTenant();
			monitor.stop();
			logger.debug("OUT");
		}
	}

	/**
	 * check if the user can execute the function.
	 * 
	 * @param token
	 *            the token
	 * @param userId
	 *            the user id
	 * @param function
	 *            the function
	 * 
	 * @return true, if check authorization
	 */
	public boolean checkAuthorization(String token, String userId,
			String function) {
		logger.debug("IN");
		Monitor monitor = MonitorFactory
				.start("spagobi.service.security.checkAuthorization");
		try {
			validateTicket(token, userId);
			ISecurityServiceSupplier supplier = SecurityServiceSupplierFactory.createISecurityServiceSupplier();
			return supplier.checkAuthorization(userId, function);
		} catch (SecurityException e) {
			logger.error("SecurityException", e);
			return false;
		} finally {
			monitor.stop();
			logger.debug("OUT");
		}

	}

}
