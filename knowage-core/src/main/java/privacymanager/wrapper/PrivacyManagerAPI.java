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
package privacymanager.wrapper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class PrivacyManagerAPI {

	private static final Logger LOGGER = LogManager.getLogger(PrivacyManagerAPI.class);

	private String serviceUrl = ""; // TODO: retrieve url by properties or similar

	private String consumerKey = "X-Consumer-Key";

	/*
	 * Usage example
	 */
	public static void main(String[] args) {

		PrivacyManagerAPI pmAPI = new PrivacyManagerAPI();
		String responseToken;
		String responsApplicationSaved;
		String responseRetrieveKey;
		try {
			responseToken = pmAPI.getToken("", "", "it");

			responsApplicationSaved = pmAPI.saveApplication("KNOWAGE", "Engineering", "www.knowage-suite.com", responseToken);

			JSONObject savedApp = new JSONObject(responsApplicationSaved);

			responseRetrieveKey = pmAPI.retrieveKey(savedApp.getString("id"), savedApp.getString("technicalDescription"), savedApp.getString("vendor"),
					savedApp.getString("url"), responseToken);

			JSONObject keyApp = new JSONObject(responseRetrieveKey);

			LOGGER.info("Token retrieved: " + responseToken);
			LOGGER.info("App Saved: " + responsApplicationSaved);
			LOGGER.info("Key retrieved: " + keyApp.getString("keyValue"));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected String getToken(String username, String password, String language) throws JSONException {

		Client restClient = ClientBuilder.newClient();

		PMTokenDTO tokenDTO = new PMTokenDTO(username, password, language);

		Response response = restClient.target(serviceUrl + "/privacyintegrationdev/api/jwt/login").request().header(consumerKey, "KNOWAGE")
				.header(HttpHeaders.CONTENT_TYPE, "application/json").accept(MediaType.WILDCARD).post(Entity.entity(tokenDTO, MediaType.APPLICATION_JSON));

		LOGGER.info("Response postForEntity " + response.getStatus());

		JSONObject instance = new JSONObject(response.readEntity(String.class));

		return instance.getString("opTargetObject");

	}

	protected String saveApplication(String technicalDescription, String vendor, String url, String token) throws JSONException {

		Client restClient = ClientBuilder.newClient();

		PMSaveDTO saveDTO = new PMSaveDTO(technicalDescription, vendor, url, technicalDescription);

		Response response = restClient.target(serviceUrl + "/privacyintegrationdev/api/integration/service/provider/save").request()
				.header(consumerKey, "KNOWAGE").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).header(HttpHeaders.CONTENT_TYPE, "application/json")
				.accept(MediaType.WILDCARD).post(Entity.entity(saveDTO, MediaType.APPLICATION_JSON));

		LOGGER.info("Response postForEntity " + response.getStatus());

		JSONObject instance = new JSONObject(response.readEntity(String.class));

		return instance.getString("opTargetObject");

	}

	protected String retrieveKey(String id, String technicalDescription, String vendor, String url, String token) throws JSONException {

		Client restClient = ClientBuilder.newClient();

		ServiceProvider serviceProvider = new ServiceProvider(id, technicalDescription, vendor, url);

		PMkeyDTO pMkeyDTO = new PMkeyDTO(serviceProvider, 24);

		Response response = restClient.target(serviceUrl + "/privacyintegrationdev/api/integration/keymanagement/retrieve").request()
				.header(consumerKey, "KNOWAGE").header(HttpHeaders.AUTHORIZATION, "Bearer " + token).header(HttpHeaders.CONTENT_TYPE, "application/json")
				.accept(MediaType.WILDCARD).post(Entity.entity(pMkeyDTO, MediaType.APPLICATION_JSON));

		LOGGER.info("Response postForEntity " + response.getStatus());

		JSONObject instance = new JSONObject(response.readEntity(String.class));

		return instance.getString("opTargetObject");

	}

}
