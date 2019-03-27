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

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;

public abstract class AbstractTenantRestProcessInterceptor extends AbstractKnowageInterceptor {

	private static Logger logger = Logger.getLogger(AbstractTenantRestProcessInterceptor.class);

	/**
	 * Pre-processes all the REST requests. Get the UserProfile object and sets the tenant information into the Thread
	 */
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		logger.debug("IN");
		UserProfile profile = this.getUserProfile();
		if (profile != null) {
			LogMF.debug(logger, "User profile retrieved [{0}]", profile);
			// retrieving tenant id
			String tenantId = profile.getOrganization();
			LogMF.debug(logger, "Tenant identifier is [{0}]", tenantId);
			// putting tenant id on thread local
			Tenant tenant = new Tenant(tenantId);
			TenantManager.setTenant(tenant);
		} else {
			logger.debug("User profile object not found.");
		}
		logger.debug("OUT");
	}

	/**
	 * Post-processes all the REST requests. Remove tenant's information from thread
	 */
	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
		logger.debug("IN");
		TenantManager.unset();
		logger.debug("OUT");
	}

}
