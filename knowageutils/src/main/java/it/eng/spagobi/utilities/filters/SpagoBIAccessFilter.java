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
package it.eng.spagobi.utilities.filters;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.common.SsoServiceFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.proxy.SecurityServiceProxy;
import it.eng.spagobi.services.security.exceptions.SecurityException;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.callbacks.audit.AuditAccessUtils;

public class SpagoBIAccessFilter implements Filter {

	public static final String USER_ID_ATTRIBUTE_NAME = "userId";

	public final String AUDIT_ID_PARAM_NAME = "SPAGOBI_AUDIT_ID";
	public final String DOCUMENT_ID_PARAM_NAME = "document";

	public final String IS_BACKEND_ATTR_NAME = "isBackend";

	private static final String EXECUTION_ID = "SBI_EXECUTION_ID";

	private static transient Logger logger = Logger.getLogger(SpagoBIAccessFilter.class);

	@Override
	public void init(FilterConfig config) throws ServletException {
		logger.debug("IN");
		// do nothing
	}

	@Override
	public void destroy() {
		// do nothing
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

		String auditId;
		String userId = null;
		String documentId;
		String executionId;
		IEngUserProfile profile = null;
		String requestUrl;

		logger.debug("IN");

		try {
			FilterIOManager ioManager = new FilterIOManager(request, response);

			documentId = request.getParameter(DOCUMENT_ID_PARAM_NAME);
			logger.info("Filter documentId  from request:" + documentId);

			auditId = request.getParameter(AUDIT_ID_PARAM_NAME);
			logger.debug("Filter auditId from request::" + auditId);

			executionId = request.getParameter(EXECUTION_ID);
			logger.debug("Filter executionId from request::" + executionId);

			userId = request.getParameter(SsoServiceInterface.USER_ID);
			logger.debug("Filter userId from request::" + userId);

			if (request instanceof HttpServletRequest) {

				HttpServletRequest httpRequest = (HttpServletRequest) request;
				requestUrl = httpRequest.getRequestURL().toString();
				logger.info("requestUrl: " + requestUrl);

				ioManager.initConetxtManager();

				ioManager.setInSession(DOCUMENT_ID_PARAM_NAME, documentId);
				ioManager.setInSession(IS_BACKEND_ATTR_NAME, "false");
				ioManager.contextManager.set(DOCUMENT_ID_PARAM_NAME, documentId);
				ioManager.contextManager.set(IS_BACKEND_ATTR_NAME, "false");

				if (requestUrl.endsWith("BackEnd")) {
					// profile=UserProfile.createSchedulerUserProfile();
					ioManager.setInSession(IS_BACKEND_ATTR_NAME, "true");
					ioManager.contextManager.set(IS_BACKEND_ATTR_NAME, "true");

					if (userId != null && UserProfile.isSchedulerUser(userId)) {
						profile = UserProfile.createSchedulerUserProfile(userId);
						ioManager.setInSession(IEngUserProfile.ENG_USER_PROFILE, profile);
						ioManager.contextManager.set(IEngUserProfile.ENG_USER_PROFILE, profile);
						logger.info("IS a Scheduler Request ...");
					} else {
						logger.info("IS a backEnd Request ...");
					}
				} else {
					userId = getUserWithSSO(httpRequest);
				}

				if (userId != null) {
					try {
						// this is not correct. profile in session can come also from a concurrent execution
						profile = (IEngUserProfile) ioManager.getFromSession(IEngUserProfile.ENG_USER_PROFILE);
						if (profile == null || !profile.getUserUniqueIdentifier().toString().equals(userId)) {
							SecurityServiceProxy proxy = new SecurityServiceProxy(userId, ioManager.getSession());
							profile = proxy.getUserProfile();
							if (profile != null) {
								ioManager.setInSession(IEngUserProfile.ENG_USER_PROFILE, profile);
								ioManager.setInSession(USER_ID_ATTRIBUTE_NAME, profile.getUserUniqueIdentifier());
								ioManager.contextManager.set(IEngUserProfile.ENG_USER_PROFILE, profile);
								ioManager.contextManager.set(USER_ID_ATTRIBUTE_NAME, profile.getUserUniqueIdentifier());
							} else {
								logger.error("ERROR WHILE GETTING USER PROFILE!!!!!!!!!!!");
							}
						} else {
							logger.debug("Found user profile in session");
							// replicate anyway the profile in this execution context. Even if the profile can come from
							// a different concurrent execution at least we have somethings that can be consumed by engines
							ioManager.contextManager.set(IEngUserProfile.ENG_USER_PROFILE, profile);
						}
					} catch (SecurityException e) {
						logger.error("SecurityException while reeding user profile", e);
						throw new ServletException("Message: " + e.getMessage() + "; Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "none"));
					}
				} else {
					profile = (IEngUserProfile) ioManager.getFromSession(IEngUserProfile.ENG_USER_PROFILE);
				}

				if (auditId != null) {
					AuditAccessUtils auditAccessUtils = (AuditAccessUtils) ioManager.getSession().getAttribute("SPAGOBI_AUDIT_UTILS");
					if (auditAccessUtils == null) {
						auditAccessUtils = new AuditAccessUtils(auditId);
						ioManager.setInSession("SPAGOBI_AUDIT_UTILS", auditAccessUtils);
						ioManager.contextManager.set("SPAGOBI_AUDIT_UTILS", auditAccessUtils);
					} else {
						auditAccessUtils.addAuditId(auditId);
					}
				}
			}

			List list = ioManager.contextManager.getKeys();

			if (profile != null) {
				manageTenant(profile);
				UserProfileManager.setProfile((UserProfile) profile);
			} else {
				logger.debug("User profile is null. Impossibile to manage tenant.");
			}
			chain.doFilter(request, response);
		} catch (Throwable t) {
			logger.error("--------------------------------------------------------------------------------");
			logger.error("SpagoBIAccessFilter" + ":doFilter ServletException!!", t);
			throw new ServletException(t);
		} finally {
			logger.debug("OUT");
			// since TenantManager and UserProfileManager use ThreadLocal, we must clean thread after request is processed
			TenantManager.unset();
			UserProfileManager.unset();
		}

	}

	private String getUserWithSSO(HttpServletRequest request) throws ServletException {
		logger.debug("IN");
		SsoServiceInterface userProxy = SsoServiceFactory.createProxyService();
		String ssoUserIdentifier = userProxy.readUserIdentifier(request);
		logger.debug("OUT. got ssoUserId from IProxyService=" + ssoUserIdentifier);
		return ssoUserIdentifier;
	}

	private String checkUserWithSSO(String userId, HttpServletRequest request) throws ServletException {
		logger.debug("IN");
		SsoServiceInterface userProxy = SsoServiceFactory.createProxyService();
		String ssoUserIdentifier = userProxy.readUserIdentifier(request);
		logger.debug("got ssoUserId from IProxyService=" + ssoUserIdentifier);
		logger.debug("OU: got userId from IProxyService=" + userId);
		return ssoUserIdentifier;
	}

	private void manageTenant(IEngUserProfile profile) {
		UserProfile userProfile = (UserProfile) profile;
		// retrieving tenant id
		String tenantId = userProfile.getOrganization();
		logger.debug("Retrieved tenantId from user profile object : [" + tenantId + "]");
		// putting tenant id on thread local
		Tenant tenant = new Tenant(tenantId);
		TenantManager.setTenant(tenant);
		logger.debug("Tenant [" + tenantId + "] set into TenantManager");
	}
}
