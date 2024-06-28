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
package it.eng.spagobi.commons.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.security.hmacfilter.HMACTokenValidator;
import it.eng.spagobi.security.hmacfilter.HMACUtils;
import it.eng.spagobi.security.hmacfilter.SystemTimeHMACTokenValidator;

/**
 * @author Salvo Lupo
 *
 */
public class HmacLoginFilter implements Filter {

	private static transient Logger logger = Logger.getLogger(HmacLoginFilter.class);

	private static final String KEY_CONFIG_NAME = "hmacKey";

	private String key;

	private static final long MAX_TIME_DELTA_DEFAULT_MS = 30000; // 30 seconds

	private static final String MAX_DELTA_CONFIG_NAME = "maxDeltaMsToken";

	private HMACTokenValidator tokenValidator;

	private String usernameField;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		logger.debug("AfterHMAC Filter");
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpSession session = httpRequest.getSession();

		String username = request.getParameter(usernameField);
		IEngUserProfile profile = null;
		if (username != null && !username.trim().equals("")) {
			HMACUtils.checkHMAC(httpRequest, tokenValidator, key);
			try {
				profile = (IEngUserProfile) httpRequest.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
				if (profile == null) {
					// in case the profile does not exist, creates a new one
					logger.debug("User profile not found in session, creating a new one and putting in session....");
					logger.debug("User id = " + username);
					profile = GeneralUtilities.createNewUserProfile(username);
					session.setAttribute(IEngUserProfile.ENG_USER_PROFILE, profile);
				}
			} catch (Exception e) {
				logger.error(e);
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		usernameField = filterConfig.getInitParameter("USERNAME_IN_SESSION");
		if (usernameField == null) {
			usernameField = "USER_ID";
		}
		key = filterConfig.getInitParameter(KEY_CONFIG_NAME);

		String maxDeltaMs = filterConfig.getInitParameter(MAX_DELTA_CONFIG_NAME);
		if (maxDeltaMs == null) {
			maxDeltaMs = Long.toString(MAX_TIME_DELTA_DEFAULT_MS);
		}
		// default implementation
		tokenValidator = new SystemTimeHMACTokenValidator(Long.parseLong(maxDeltaMs));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
	}
}
