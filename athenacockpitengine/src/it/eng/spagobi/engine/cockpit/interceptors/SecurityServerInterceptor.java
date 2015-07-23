/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
