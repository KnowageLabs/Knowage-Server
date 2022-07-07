/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

/**
 *
 */
package it.eng.spagobi.commons.serializer.v3;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

/**
 * @author albnale
 *
 */
public class MenuSerializerCommunity {
	private static final Logger LOGGER = Logger.getLogger(MenuSerializerCommunity.class);

	@Test
	public void test() throws NamingException, FileNotFoundException, IOException, ParseException {

//		String hmacKey = (String) ctx.lookup("jndi.lookup.hmackey");
//		String serviceUrl = (String) ctx.lookup("java:comp/env/service_url");
//		String restURI = serviceUrl + "/restful-services/3.0/menu/enduser?locale=it-IT";
//		SimpleRestClient sc = new SimpleRestClient(hmacKey);
//		Map<String, Object> parameters = new java.util.HashMap<String, Object>();
//		Object o = new Object();

//		InitialContext ctx = new InitialContext();
//		ctx.bind("java:comp/env/service_url", "http://localhost:8080/knowage");
//		ctx.bind("java:comp/env/hmacKey", "abc123");
		Client client = ClientBuilder.newClient();
		String jwtToken = getUserTechnicalToken();
		WebTarget target = client.target("http://localhost:8080/knowage/restful-services/3.0/menu/enduser?locale=en-US");
		Response response = target.request(MediaType.APPLICATION_JSON).header("X-Kn-Authorization", jwtToken).get(Response.class);

		String jsonResponse = response.readEntity(String.class);
		JSONObject json = null;
		try {
			Set<String> expectedMenu = new HashSet<String>();
			Set<String> currentMenu = new HashSet<String>();

			org.json.simple.JSONObject expectedMenuJSON = getExpected();
			org.json.simple.JSONArray expectedTechnical = (org.json.simple.JSONArray) expectedMenuJSON.get("technicalUserFunctionalities");
			populateSet(expectedMenu, expectedTechnical);

			json = new JSONObject(jsonResponse);
			JSONArray technical = json.getJSONArray("technicalUserFunctionalities");

			populateSet(currentMenu, technical);

			assertTrue("Menu content is the same", currentMenu.equals(expectedMenu));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// String message = MessageBundle.getMessage("menu.news", "messages", currentLocale);

		// System.out.println(message);

	}

	/**
	 * @param expectedMenu
	 * @param expectedTechnical
	 * @throws JSONException
	 */
	private void populateSet(Set<String> expectedMenu, JSONArray expectedTechnical) throws JSONException {
		for (int i = 0; i < expectedTechnical.length(); i++) {
			JSONObject menu = (JSONObject) expectedTechnical.get(i);

			expectedMenu.add(menu.getString("label"));

			try {
				if (menu.get("items") != null) {
					JSONArray children = (JSONArray) menu.get("items");
					populateSet(expectedMenu, children);
				}
			} catch (JSONException e) {

			}
		}
	}

	private void populateSet(Set<String> expectedMenu, org.json.simple.JSONArray expectedTechnical) throws JSONException {
		for (int i = 0; i < expectedTechnical.size(); i++) {
			org.json.simple.JSONObject menu = (org.json.simple.JSONObject) expectedTechnical.get(i);

			expectedMenu.add((String) menu.get("label"));

			if (menu.get("items") != null) {
				org.json.simple.JSONArray children = (org.json.simple.JSONArray) menu.get("items");
				populateSet(expectedMenu, children);
			}
		}
	}

	private String getUserTechnicalToken() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, 2);
		Date expiresAt = calendar.getTime();

		String organization = "DEFAULT_TENANT";
		String jwtToken = userId2jwtToken("biadmin", expiresAt);
		return jwtToken;
	}

	public static String userId2jwtToken(String userId, Date expiresAt) {
		LogMF.debug(LOGGER, "User id in input is [{0}]", userId);
		LogMF.debug(LOGGER, "JWT token will expire at [{0}]", expiresAt);
		String key;
		String token = null;
		try {
			key = "abc123";

			token = JWT.create().withClaim("user_id", userId).withExpiresAt(expiresAt) // token will expire at the desired expire date
					.sign(Algorithm.HMAC256(key));
		} catch (IllegalArgumentException | JWTCreationException | UnsupportedEncodingException e) {
			// throw new KnowageRuntimeException(e.getMessage(), e);
		}
		LogMF.debug(LOGGER, "JWT token is [{0}]", token);
		return token;
	}

	private org.json.simple.JSONObject getExpected() throws FileNotFoundException, IOException, ParseException {
		JSONParser parser = new JSONParser();

		Path path = Paths.get("src/test/resources/menu/CE_default.json");
		Object obj = parser.parse(new FileReader(path.toFile()));

		// A JSON object. Key value pairs are unordered. JSONObject supports java.util.Map interface.
		return (org.json.simple.JSONObject) obj;
	}
}
