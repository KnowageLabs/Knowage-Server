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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * @author Salvo Lupo
 *
 */
public class HMACUtils {

	private static transient Logger logger = Logger.getLogger(HMACUtils.class);

	public static final String AUTHORIZATION_USER_HEADER = "Authorization";

	public static List<String> HEADERS_SIGNED = Arrays.asList(AUTHORIZATION_USER_HEADER);

	static {
		// ensure that must be sorted and not modifiable
		Collections.sort(HEADERS_SIGNED);
		HEADERS_SIGNED = Collections.unmodifiableList(HEADERS_SIGNED);
	}
	public static final String HMAC_JNDI_LOOKUP = "SPAGOBI_HMAC.HMAC_JNDI_LOOKUP";
	public static final String HMAC_TOKEN_HEADER = "HMAC_Token";

	public static final String HMAC_SIGNATURE_HEADER = "HMAC_Signature";

	public static void checkHMAC(HttpServletRequest req, HMACTokenValidator tokenValidator, String key) throws IOException, HMACSecurityException {
		// it permits to read body more than once
		req = new MultiReadHttpServletRequest(req);
		String signatureClient = getSignatureClient(req);
		logger.debug("Client signature is: [" + signatureClient + "]");
		String uniqueToken = getUniqueToken(req);
		logger.debug("Unique token is: [" + uniqueToken + "]");
		String headers = getHeadersString(req);
		logger.debug("Header string is: [" + headers + "]");
		String paramsString = getParamsString(req);
		logger.debug("Params string is: [" + paramsString + "]");
		String queryPath = getQueryPath(req);
		logger.debug("Query path: [" + queryPath + "]");
		String body = "";// RestUtilities.readBodyXSSUnsafe(req);
		logger.debug("Body: [" + body + "]");
		checkHMAC(body, queryPath, paramsString, headers, uniqueToken, signatureClient, tokenValidator, key);
	}

	public static void checkHMAC(String data, String uniqueToken, String signatureClient, HMACTokenValidator tokenValidator, String key)
			throws HMACSecurityException, IOException {
		checkHMAC(data, "", "", "", uniqueToken, signatureClient, tokenValidator, key);
	}

	public static void checkHMAC(String body, String queryPath, String paramsString, String headers, String uniqueToken, String signatureClient,
			HMACTokenValidator tokenValidator, String key) throws HMACSecurityException, IOException {
		if (uniqueToken == null) {
			throw new HMACSecurityException("HMAC token is not present.");
		}
		tokenValidator.validate(uniqueToken);
		if (key == null || key.isEmpty()) {
			key = EnginConf.getInstance().getHmacKey();
			if (key == null || key.isEmpty()) {
				key = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue(HMAC_JNDI_LOOKUP));
			}
			if (key == null || key.isEmpty()) {
				throw new IllegalStateException("key is null or empty");
			}
		}
		String signature = sign(body, queryPath, paramsString, headers, uniqueToken, key);
		logger.debug("Server signature is [" + signature + "]");
		if (signatureClient == null) {
			throw new HMACSecurityException("Signature of request not present.");
		}
		if (!signature.equals(signatureClient)) {
			throw new HMACSecurityException("Signature of request is not correct.");
		}

	}

	public static String sign(String body, String queryPath, String paramsString, String headers, String uniqueToken, String key) throws IOException {
		StringBuilder res = new StringBuilder(queryPath);
		res.append(paramsString);
		res.append(body);
		res.append(uniqueToken);
		res.append(headers);
		res.append(key);
		String s = res.toString();
		String signature = StringUtilities.sha256(s);
		return signature;
	}

	/**
	 * http://example.com:80/docs/books/tutorial/index.html?name=networking -> /docs/books/tutorial/index.html
	 *
	 * @param req
	 * @return
	 */
	public static String getQueryPath(HttpServletRequest req) {
		return req.getRequestURI();
	}

	public static String getHeadersString(HttpServletRequest req) {
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

	public static String getHeadersString(Map<String, String> headers) {
		StringBuilder res = new StringBuilder();
		for (String header : HEADERS_SIGNED) {
			String value = headers.get(header);
			if (value == null) {
				continue;
			}
			res.append(header);
			res.append(value);
		}
		return res.toString();
	}

	public static String getParamsString(HttpServletRequest req) {
		String res = req.getQueryString();
		return res == null ? "" : res;
	}

	public static String getSignatureClient(HttpServletRequest req) {
		return req.getHeader(HMAC_SIGNATURE_HEADER);
	}

	public static String getUniqueToken(HttpServletRequest req) {
		return req.getHeader(HMAC_TOKEN_HEADER);
	}

}
