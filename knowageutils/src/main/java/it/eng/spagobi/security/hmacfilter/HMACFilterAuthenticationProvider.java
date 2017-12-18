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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.http.client.methods.HttpPost;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;

/**
 * Provide client HMAC authentication for {@link HMACFilter}.
 *
 * @author fabrizio
 *
 */
public class HMACFilterAuthenticationProvider {

	private final String key;
	private final HMACTokenValidator validator;

	public HMACFilterAuthenticationProvider(String key) {
		this(key, new SystemTimeHMACTokenValidator(HMACFilter.MAX_TIME_DELTA_DEFAULT_MS));
	}

	public HMACFilterAuthenticationProvider(String key, HMACTokenValidator validator) {
		Helper.checkNotNullNotTrimNotEmpty(key, "key");
		Helper.checkNotNull(validator, "validator");

		this.key = key;
		this.validator = validator;
	}

	// /**
	// * For REST Easy {@link ClientRequest}
	// *
	// * @param req
	// * @throws HMACSecurityException
	// */
	// public void provideAuthentication(ClientRequest req) throws HMACSecurityException {
	// Helper.checkNotNull(req, "req");
	//
	// String token = validator.generateToken();
	// Assert.assertNotNull(token, "token");
	// String signature;
	// try {
	// signature = getSignature(req, token);
	// } catch (Exception e) {
	// throw new HMACSecurityException("Problems while signing the request", e);
	// }
	//
	// req.header(HMACUtils.HMAC_TOKEN_HEADER, token);
	// req.header(HMACUtils.HMAC_SIGNATURE_HEADER, signature);
	// }

	/**
	 * For REST Easy {@link ClientRequest}
	 *
	 * @param req
	 * @throws HMACSecurityException
	 */
	public void provideAuthentication(Builder request, WebTarget target, MultivaluedMap<String, Object> myHeaders, Object data) throws HMACSecurityException {
		Helper.checkNotNull(request, "req");

		String token = validator.generateToken();
		Assert.assertNotNull(token, "token");
		String signature;
		try {
			signature = getSignature(request, target, data, myHeaders, token);
		} catch (Exception e) {
			throw new HMACSecurityException("Problems while signing the request", e);
		}

		request.header(HMACUtils.HMAC_TOKEN_HEADER, token);
		request.header(HMACUtils.HMAC_SIGNATURE_HEADER, signature);
		myHeaders.add(HMACUtils.HMAC_TOKEN_HEADER, token);
		myHeaders.add(HMACUtils.HMAC_SIGNATURE_HEADER, signature);

	}

	public void provideAuthenticationMultiPart(HttpPost request, MultivaluedMap<String, Object> myHeaders) throws HMACSecurityException {
		Helper.checkNotNull(request, "req");

		String token = validator.generateToken();
		Assert.assertNotNull(token, "token");
		String signature;
		try {
			signature = getSignatureM(request, myHeaders, token);
		} catch (Exception e) {
			throw new HMACSecurityException("Problems while signing the request", e);
		}

		request.addHeader(HMACUtils.HMAC_TOKEN_HEADER, token);
		request.addHeader(HMACUtils.HMAC_SIGNATURE_HEADER, signature);
		myHeaders.add(HMACUtils.HMAC_TOKEN_HEADER, token);
		myHeaders.add(HMACUtils.HMAC_SIGNATURE_HEADER, signature);

	}

	/**
	 *
	 * @param method
	 * @throws HMACSecurityException
	 */
	public void provideAuthentication(HttpMethodBase method, String body) throws HMACSecurityException {

		String token = validator.generateToken();
		Assert.assertNotNull(token, "token");
		String signature;
		try {
			signature = getSignature(method, body, token);
		} catch (Exception e) {
			throw new HMACSecurityException("Problems while signing the request", e);
		}

		method.addRequestHeader(HMACUtils.HMAC_TOKEN_HEADER, token);
		method.addRequestHeader(HMACUtils.HMAC_SIGNATURE_HEADER, signature);
	}

	// private String getSignature(ClientRequest req, String token) throws IOException, Exception {
	// String res = HMACUtils.sign(getBody(req), getQueryPath(req), getParamsString(req), getHeaders(req), token, key);
	// return res;
	// }

	private String getSignature(Builder request, WebTarget target, Object data, MultivaluedMap<String, Object> myHeaders, String token)
			throws IOException, Exception {
		ObjectMapper mo = new ObjectMapper();
		String body = "";

		String res = HMACUtils.sign(body, getQueryPath(target), "", getHeaders(myHeaders), token, key);
		return res;
	}

	private String getSignatureM(HttpPost request, MultivaluedMap<String, Object> myHeaders, String token) throws IOException, Exception {
		ObjectMapper mo = new ObjectMapper();
		String body = "";

		String res = HMACUtils.sign(body, request.getURI().getPath(), "", getHeaders(myHeaders), token, key);
		return res;
	}

	private String getSignature(HttpMethodBase method, String body, String token) throws IOException, Exception {
		String res = HMACUtils.sign(body, getQueryPath(method), getParamsString(method), getHeaders(method), token, key);
		return res;
	}

	private String getSignatureM(HttpMethodBase method, String body, String token) throws IOException, Exception {
		String res = HMACUtils.sign(body, getQueryPath(method), getParamsString(method), getHeaders(method), token, key);
		return res;
	}

	// private static String getHeaders(ClientRequest req) {
	// MultivaluedMap<String, String> headers = req.getHeaders();
	// StringBuilder res = new StringBuilder();
	// for (String name : HMACUtils.HEADERS_SIGNED) {
	// List<String> values = headers.get(name); // only 1 value admitted
	// if (values == null) {
	// // header not present
	// continue;
	// }
	// Assert.assertTrue(values.size() == 1, "only one value admitted for each header");
	// res.append(name);
	// res.append(values.get(0));
	// }
	// return res.toString();
	// }
	//
	//
	private static String getHeaders(MultivaluedMap<String, Object> headers) {

		StringBuilder res = new StringBuilder();
		for (String name : HMACUtils.HEADERS_SIGNED) {
			List<Object> values = headers.get(name); // only 1 value admitted
			if (values == null) {
				// header not present
				continue;
			}
			Assert.assertTrue(values.size() == 1, "only one value admitted for each header");
			res.append(name);
			res.append(values.get(0));
		}
		return res.toString();
	}

	// private static String getBody(ClientRequest req) throws IOException {
	// Object body = req.getBody();
	// if (body == null) {
	// return "";
	// }
	// if (body instanceof String) {
	// String bodyS = (String) body;
	// return bodyS;
	// }
	// if (body instanceof InputStream) {
	// InputStream stream = (InputStream) body;
	// String s = StringUtilities.readStream(stream);
	// // replace the already read stream
	// InputStream replace = new ByteArrayInputStream(s.getBytes(StringUtilities.DEFAULT_CHARSET));
	// req.body(req.getBodyContentType(), replace);
	// return s;
	// }
	// Assert.assertUnreachable("body object not supported");
	// return null;
	// }

	// private static String getParamsString(ClientRequest req) throws Exception {
	// String uri = req.getUri();
	// URL url = new URL(uri);
	// return url.getQuery();
	// }
	//
	//
	// private static String getQueryPath(ClientRequest req) throws Exception {
	// String uri = req.getUri();
	// URL url = new URL(uri);
	// return url.getPath();
	// }

	private static String getQueryPath(WebTarget target) throws Exception {
		URI uri = target.getUri();
		return uri.getPath();
	}

	private static String getHeaders(HttpMethodBase method) {
		Header[] headersArray = method.getRequestHeaders();
		Map<String, String> headers = new HashMap<>(headersArray.length);
		for (int i = 0; i < headersArray.length; i++) { // only 1 value admitted for each header
			headers.put(headersArray[i].getName(), headersArray[i].getValue());
		}

		StringBuilder res = new StringBuilder();
		for (String name : HMACUtils.HEADERS_SIGNED) {
			String value = headers.get(name); // only 1 value admitted
			if (value != null) {
				res.append(name);
				res.append(value);
			}
		}
		return res.toString();
	}

	private static String getParamsString(HttpMethodBase method) throws Exception {
		String queryString = method.getQueryString();
		return queryString != null ? queryString : "";
	}

	private static String getQueryPath(HttpMethodBase method) throws Exception {
		return method.getPath();
	}
}
