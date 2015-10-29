/**
 * SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2015 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package it.eng.spagobi.security.hmacfilter;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * This class implements a HMAC ( https://en.wikipedia.org/wiki/Hash-based_message_authentication_code ) filter. The shared {@link HMACFilter#key} is
 * initialized by filter configuration. It's used internally by the engines. The HMAC key is configured through JNDI : {@link HMACFilter#HMAC_JNDI_LOOKUP} .
 *
 * @author fabrizio
 *
 */
public class HMACFilter implements Filter {

	public static final String HMAC_JNDI_LOOKUP = "java:/comp/env/hmacKey";

	public static final String DEFAULT_ENCODING = "UTF-8";

	public static final String HMAC_TOKEN_HEADER = "SpagoBI_HMAC_Token";

	public static final String HMAC_SIGNATURE_HEADER = "SpagoBI_HMAC_Signature";

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
		// it permits to read body more than once
		req = new MultiReadHttpServletRequest(req);

		checkUniqueToken(req);

		String signature = calcSignature(req);
		String signatureClient = getSignatureClient(req);
		if (signatureClient == null) {
			throw new HMACSecurityException("Signature of request not present.");
		}
		if (!signature.equals(signatureClient)) {
			throw new HMACSecurityException("Signature of request is not correct.");
		}

		chain.doFilter(req, response);
	}

	private void checkUniqueToken(HttpServletRequest req) throws HMACSecurityException {
		String uniqueToken = getUniqueToken(req);
		if (uniqueToken == null) {
			throw new HMACSecurityException("HMAC token is not present.");
		}
		tokenValidator.validate(uniqueToken);
	}

	private String calcSignature(HttpServletRequest req) throws IOException {
		String body = RestUtilities.readBody(req);
		String queryPath = getQueryPath(req);
		String paramsString = getParamsString(req);
		String uniqueToken = getUniqueToken(req);
		String signature = sign(queryPath, paramsString, body, uniqueToken, key);
		return signature;
	}

	private String getParamsString(HttpServletRequest req) {
		String res = req.getQueryString();
		return res == null ? "" : res;
	}

	private String getSignatureClient(HttpServletRequest req) {
		return req.getHeader(HMAC_SIGNATURE_HEADER);
	}

	public static String sign(String queryPath, String paramsString, String body, String uniqueToken, String key) throws IOException {
		StringBuilder res = new StringBuilder(queryPath);
		res.append(paramsString);
		res.append(body);
		res.append(uniqueToken);
		res.append(key);
		String s = res.toString();
		return StringUtilities.sha256(s);
	}

	private static String getUniqueToken(HttpServletRequest req) {
		return req.getHeader(HMAC_TOKEN_HEADER);
	}

	/**
	 * http://example.com:80/docs/books/tutorial/index.html?name=networking -> /docs/books/tutorial/index.html
	 *
	 * @param req
	 * @return
	 */
	private static String getQueryPath(HttpServletRequest req) {
		return req.getRequestURI();
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		key = filterConfig.getInitParameter(KEY_CONFIG_NAME);
		if (key == null || key.isEmpty()) {
			try {
				key = (String) (new InitialContext().lookup(HMACFilter.HMAC_JNDI_LOOKUP));
			} catch (NamingException e) {
				throw new ServletException("HMAC key not correctly configured", e);
			}
			if (key == null || key.isEmpty()) {
				throw new IllegalStateException("key is null or empty");
			}
		}

		String maxDeltaMs = filterConfig.getInitParameter(MAX_DELTA_CONFIG_NAME);
		if (maxDeltaMs == null) {
			maxDeltaMs = Long.toString(MAX_TIME_DELTA_DEFAULT_MS);
		}
		// default implementation
		tokenValidator = new SystemTimeHMACTokenValidator(Long.parseLong(maxDeltaMs));
	}
}
