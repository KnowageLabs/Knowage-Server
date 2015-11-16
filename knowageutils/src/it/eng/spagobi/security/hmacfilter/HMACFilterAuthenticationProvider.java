package it.eng.spagobi.security.hmacfilter;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.utilities.Helper;
import it.eng.spagobi.utilities.assertion.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.client.ClientRequest;

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

	/**
	 * For REST Easy {@link ClientRequest}
	 *
	 * @param req
	 * @throws HMACSecurityException
	 */
	public void provideAuthentication(ClientRequest req) throws HMACSecurityException {
		Helper.checkNotNull(req, "req");

		String token = validator.generateToken();
		Assert.assertNotNull(token, "token");
		String signature;
		try {
			signature = getSignature(req, token);
		} catch (Exception e) {
			throw new HMACSecurityException("Problems while signing the request", e);
		}

		req.header(HMACFilter.HMAC_TOKEN_HEADER, token);
		req.header(HMACFilter.HMAC_SIGNATURE_HEADER, signature);
	}

	private String getSignature(ClientRequest req, String token) throws IOException, Exception {
		String res = HMACFilter.sign(getQueryPath(req), getParamsString(req), getBody(req), token, key, getHeaders(req));
		return res;
	}

	private static String getHeaders(ClientRequest req) {
		MultivaluedMap<String, String> headers = req.getHeaders();
		StringBuilder res = new StringBuilder();
		for (String name : HMACFilter.HEADERS_SIGNED) {
			List<String> values = headers.get(name); // only 1 value admitted
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

	private static String getBody(ClientRequest req) throws IOException {
		Object body = req.getBody();
		if (body == null) {
			return "";
		}
		if (body instanceof String) {
			String bodyS = (String) body;
			return bodyS;
		}
		if (body instanceof InputStream) {
			InputStream stream = (InputStream) body;
			String s = StringUtilities.readStream(stream);
			// replace the already read stream
			InputStream replace = new ByteArrayInputStream(s.getBytes(StringUtilities.DEFAULT_CHARSET));
			req.body(req.getBodyContentType(), replace);
			return s;
		}
		Assert.assertUnreachable("body object not supported");
		return null;
	}

	private static String getParamsString(ClientRequest req) throws Exception {
		String uri = req.getUri();
		URL url = new URL(uri);
		return url.getQuery();
	}

	private static String getQueryPath(ClientRequest req) throws Exception {
		String uri = req.getUri();
		URL url = new URL(uri);
		return url.getPath();
	}
}
