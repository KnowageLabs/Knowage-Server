/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.security.hmacfilter;

import static it.eng.spagobi.security.hmacfilter.HMACUtils.checkHMAC;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * This class implements a HMAC ( https://en.wikipedia.org/wiki/Hash-based_message_authentication_code ) filter. The shared {@link HMACFilter#key} is
 * initialized by filter configuration. It's used internally by the engines. The HMAC key is configured through JNDI : {@link HMACFilter#HMAC_JNDI_LOOKUP}. See
 * web.xml of knowage project for configuring the Filter.
 * 
 * @author fabrizio
 * 
 */
public class HMACFilter implements Filter {

	public static final String DEFAULT_ENCODING = "UTF-8";

	public static final String KEY_CONFIG_NAME = "hmacKey";

	private String key;

	public final static long MAX_TIME_DELTA_DEFAULT_MS = 30000; // 30 seconds

	private static final String MAX_DELTA_CONFIG_NAME = "maxDeltaMsToken";

	private HMACTokenValidator tokenValidator;

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest)) {
			throw new IllegalArgumentException("request not instance of HttpServletRequest");
		}
		HttpServletRequest req = (HttpServletRequest) request;

		checkHMAC(req, tokenValidator, key);

		chain.doFilter(req, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		key = filterConfig.getInitParameter(KEY_CONFIG_NAME);

		String maxDeltaMs = filterConfig.getInitParameter(MAX_DELTA_CONFIG_NAME);
		if (maxDeltaMs == null) {
			maxDeltaMs = Long.toString(MAX_TIME_DELTA_DEFAULT_MS);
		}
		// default implementation
		tokenValidator = new SystemTimeHMACTokenValidator(Long.parseLong(maxDeltaMs));
	}
}
