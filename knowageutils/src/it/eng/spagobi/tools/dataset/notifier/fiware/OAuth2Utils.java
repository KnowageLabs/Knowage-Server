package it.eng.spagobi.tools.dataset.notifier.fiware;

import it.eng.spagobi.services.common.SsoServiceFactory;
import it.eng.spagobi.services.common.SsoServiceInterface;
import it.eng.spagobi.services.oauth2.Oauth2SsoService;
import it.eng.spagobi.utilities.assertion.Assert;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class OAuth2Utils {

	private static final String X_AUTH_TOKEN_HEADER = "X-Auth-Token";

	public static Map<String, String> getOAuth2Headers(String user) throws MalformedURLException {
		Map<String, String> res = new HashMap<String, String>();
		res.put(X_AUTH_TOKEN_HEADER, user);  //orion doesn't use bearer mode
		return res;
	}

	public static boolean isOAuth2() {
		SsoServiceInterface userProxy = SsoServiceFactory.createProxyService();
		Assert.assertNotNull(userProxy, "security proxy");
		
		return userProxy instanceof Oauth2SsoService;
	}

	public static boolean containsOAuth2(Map<String, String> headers) {
		for (String key : headers.keySet()) {

			if (key.equalsIgnoreCase(X_AUTH_TOKEN_HEADER)) {
				return true;
			}
		}
		return false;
	}
}
