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
package it.eng.spagobi.sdk;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.sdk.exceptions.NotAllowedOperationException;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.axis.MessageContext;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.apache.ws.security.handler.WSHandlerConstants;

public class AbstractSDKService {

	static private Logger logger = Logger.getLogger(AbstractSDKService.class);
	
	protected IEngUserProfile getUserProfile() throws Exception {
		logger.debug("IN");
		IEngUserProfile profile = null;
		try {
			MessageContext mc = MessageContext.getCurrentContext();
			profile = (IEngUserProfile) mc.getProperty(IEngUserProfile.ENG_USER_PROFILE);
			if (profile == null) {
				logger.debug("User profile not found.");
				String userIdentifier = (String) mc.getProperty(WSHandlerConstants.USER);
				logger.debug("User identifier found = [" + userIdentifier + "].");
				if (userIdentifier == null) {
					logger.warn("User identifier not found!! cannot build user profile object");
					throw new Exception("Cannot create user profile");
				} else {
					try {
						profile = UserUtilities.getUserProfile(userIdentifier);
						logger.debug("User profile for userId [" + userIdentifier + "] created.");
					} catch (Exception e) {
						logger.error("Exception creating user profile for userId [" + userIdentifier + "]!", e);
						throw new Exception("Cannot create user profile");
					}
				}
				mc.setProperty(IEngUserProfile.ENG_USER_PROFILE, profile);
			} else {
				logger.debug("User profile for user [" + profile.getUserUniqueIdentifier() + "] retrieved.");
			}
			UserProfile userProfile = (UserProfile) profile;
			logger.info("User profile retrieved: userId = [" + userProfile.getUserId() + "]; username = [" + userProfile.getUserName() + "]");
		} finally {
			logger.debug("OUT");
		}
		return profile;
	}
	
	/**
	 * Retrieves user profile and check if he has rights for the functionality in input.
	 * In case he has no rights, a <code>NotAllowedOperationException</code> with the error message in input is thrown.
	 * 
	 * @param userFunctionality The user functionality
	 * @param errorMessage The error message to be used in case a <code>NotAllowedOperationException</code> must be thrown
	 * @throws NotAllowedOperationException In case the user has no rights for the specified user functionality
	 * @throws Exception is case of any other error
	 */
	protected void checkUserPermissionForFunctionality(String userFunctionality, String errorMessage) 
						throws NotAllowedOperationException, Exception {
		logger.debug("IN");
		try {
			IEngUserProfile profile = getUserProfile();
			UserProfile userProfile = (UserProfile) profile;
	    	if (!userProfile.isAbleToExecuteAction(userFunctionality)) {
	    		logger.error("Current user [" + userProfile.getUserId() + "] has no rights for " + userFunctionality + " functionality.");
	    		NotAllowedOperationException e = new NotAllowedOperationException();
	    		e.setFaultString(errorMessage);
	    		throw e;
	    	} else {
	    		logger.debug("Current user [" + userProfile.getUserId() + "] has rights for " + userFunctionality + " functionality.");
	    	}
		} finally {
			logger.debug("OUT");
		}
	}
	
	protected void setTenant() {
		logger.debug("IN");
		try {
			IEngUserProfile profile = getUserProfile();
			Assert.assertNotNull(profile, "Input parameter [profile] cannot be null");
			UserProfile userProfile = (UserProfile) profile;
			String tenant = userProfile.getOrganization();
			LogMF.debug(logger, "Tenant: [{0}]", tenant);
			TenantManager.setTenant(new Tenant(tenant));
			LogMF.debug(logger, "Tenant [{0}] set properly", tenant);
		} catch (Throwable t) {
			logger.error("Cannot set tenant", t);
			throw new SpagoBIRuntimeException("Cannot set tenant", t);
		} finally {
			logger.debug("OUT");
		}
	}
	
	protected void unsetTenant() {
		TenantManager.unset();
	}
}
