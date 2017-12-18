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
