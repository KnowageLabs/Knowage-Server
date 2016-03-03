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
package it.eng.spagobi.engine.cockpit.interceptors;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.utilities.UserUtilities;
import it.eng.spagobi.engine.cockpit.api.SecurityServiceSupplierFactory;
import it.eng.spagobi.services.rest.ExternalEngineSecurityServerInterceptor;
import it.eng.spagobi.services.security.bo.SpagoBIUserProfile;
import it.eng.spagobi.services.security.service.ISecurityServiceSupplier;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.interception.Precedence;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;

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
public class SecurityServerInterceptor extends ExternalEngineSecurityServerInterceptor {
	static private Logger logger = Logger.getLogger(SecurityServerInterceptor.class);

	@Context
	private HttpServletRequest servletRequest;

	@Override
	protected UserProfile authenticateUser() {
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

}
