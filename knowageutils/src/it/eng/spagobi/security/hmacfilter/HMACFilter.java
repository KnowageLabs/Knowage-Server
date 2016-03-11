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

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

	public static final String AUTHORIZATION_USER_HEADER = "Authorization";

	public static List<String> HEADERS_SIGNED = Arrays.asList(AUTHORIZATION_USER_HEADER);

	static {
		// ensure that must be sorted and not modifiable
		Collections.sort(HEADERS_SIGNED);
		HEADERS_SIGNED = Collections.unmodifiableList(HEADERS_SIGNED);
	}

	public static final String HMAC_JNDI_LOOKUP = "SPAGOBI_HMAC.HMAC_JNDI_LOOKUP";

	public static final String DEFAULT_ENCODING = "UTF-8";

	public static final String HMAC_TOKEN_HEADER = "HMAC_Token";

	public static final String HMAC_SIGNATURE_HEADER = "HMAC_Signature";

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
		if (key == null || key.isEmpty()) {
			key = EnginConf.getInstance().getHmacKey();
			if (key == null || key.isEmpty()) {
				key = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue(HMAC_JNDI_LOOKUP));
			}
			if (key == null || key.isEmpty()) {
				throw new IllegalStateException("key is null or empty");
			}
		}

		String body = RestUtilities.readBody(req);
		String queryPath = getQueryPath(req);
		String paramsString = getParamsString(req);
		String uniqueToken = getUniqueToken(req);
		String headers = getHeadersString(req);
		String signature = sign(queryPath, paramsString, body, uniqueToken, key, headers);
		return signature;
	}

	private static String getHeadersString(HttpServletRequest req) {
		StringBuilder res = new StringBuilder();
		for (String header : HEADERS_SIGNED) {
			String value = req.getHeader(header);
			if (value == null) {
				continue;
			}
			res.append(header);
			res.append(value);
		}
		return res.toString();
	}

	private String getParamsString(HttpServletRequest req) {
		String res = req.getQueryString();
		return res == null ? "" : res;
	}

	private String getSignatureClient(HttpServletRequest req) {
		return req.getHeader(HMAC_SIGNATURE_HEADER);
	}

	public static String sign(String queryPath, String paramsString, String body, String uniqueToken, String key, String headers) throws IOException {
		StringBuilder res = new StringBuilder(queryPath);
		res.append(paramsString);
		res.append(body);
		res.append(uniqueToken);
		res.append(headers);
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

		String maxDeltaMs = filterConfig.getInitParameter(MAX_DELTA_CONFIG_NAME);
		if (maxDeltaMs == null) {
			maxDeltaMs = Long.toString(MAX_TIME_DELTA_DEFAULT_MS);
		}
		// default implementation
		tokenValidator = new SystemTimeHMACTokenValidator(Long.parseLong(maxDeltaMs));
	}
}
