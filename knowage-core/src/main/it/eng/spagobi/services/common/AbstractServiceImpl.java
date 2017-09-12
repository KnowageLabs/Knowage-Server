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
package it.eng.spagobi.services.common;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

/**
 * Abstract class for all Service Implementation
 */
public abstract class AbstractServiceImpl {

	static private Logger logger = Logger.getLogger(AbstractServiceImpl.class);

	private String pass = null;
	
	private String userId = null;

	/**
	 * Instantiates a new abstract service impl.
	 */
	public AbstractServiceImpl() {
		init();
	}

	private void init() {
		logger.debug("IN");
		pass = SingletonConfig.getInstance().getConfigValue("SPAGOBI_SSO.PASS");

	}

	/**
	 * check the ticket used for verify the user authentication
	 * 
	 * @param ticket
	 *            String
	 * @return String
	 * @throws SecurityException
	 */
	protected void validateTicket(String ticket, String userId)
			throws SecurityException {
		logger.debug("IN");
		if (ticket == null) {
			logger.warn("Ticket is NULL");
			throw new SecurityException("Ticket is NULL");
		}
		if (userId == null) {
			logger.warn("UserID is NULL");
			throw new SecurityException("Ticket is NULL");
		}
		if (ticket.equals(pass)) {
			logger.debug("JUMP che ticket validation");
		} else {
			SsoServiceInterface proxyService = SsoServiceFactory
					.createProxyService();
			proxyService.validateTicket(ticket, userId);
		}

		logger.debug("OUT");

	}
	
	protected void setTenantByUserProfile(IEngUserProfile profile) {
		logger.debug("IN");
		try {
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

	protected IEngUserProfile setTenantByUserId(String userId) {
		logger.debug("IN");
		try {
			if (UserProfile.isSchedulerUser(userId)) {
				UserProfile scheduler = UserProfile.createSchedulerUserProfile(userId);
				this.setTenantByUserProfile(scheduler);
				return scheduler;
			}
			if (UserProfile.isWorkflowUser(userId)) {
				UserProfile workflow = UserProfile.createWorkflowUserProfile(userId);
				this.setTenantByUserProfile(workflow);
				return workflow;
			}
			IEngUserProfile profile = UserUtilities.getUserProfile(userId);
			this.setTenantByUserProfile(profile);
			return profile;
		} catch (Exception e) {
			logger.error("Cannot set tenant", e);
			throw new SpagoBIRuntimeException("Cannot set tenant", e);
		} finally {
			logger.debug("OUT");
		}
	}

	
}
