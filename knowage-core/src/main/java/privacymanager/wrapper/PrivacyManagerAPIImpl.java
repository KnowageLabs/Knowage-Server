/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.

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

package privacymanager.wrapper;

import static javax.ws.rs.core.Response.Status.OK;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import privacymanager.wrapper.exception.PrivacyManagerFailureException;
import privacymanager.wrapper.exception.PrivacyManagerHttpFailureException;

class PrivacyManagerAPIImpl implements IPrivacyManagerAPI {

	private static final Logger LOGGER = LogManager.getLogger(PrivacyManagerAPIImpl.class);

	private static final String HTTP_HEADER_NAME_X_CONSUMER_KEY = "X-Consumer-Key";
	private static final String HTTP_HEADER_VALUE_X_CONSUMER_KEY = "KNOWAGE";

	private static final String DEFAULT_VALUE_DESCRIPTION = "KNOWAGE";
	private static final String DEFAULT_VALUE_VENDOR = "Engineering";
	private static final String DEFAULT_VALUE_URL = "www.knowage-suite.com";

	private final URL pmUrl;
	private final String pmAppId;

	private String appDescription = DEFAULT_VALUE_DESCRIPTION;
	private String appVendor = DEFAULT_VALUE_VENDOR;
	private String appUrl = DEFAULT_VALUE_URL;

	private String token = null;
	private Locale locale = Locale.getDefault();
	private Client restClient = null;

	PrivacyManagerAPIImpl(String pmUrl, String pmAppId) throws MalformedURLException {
		this(new URL(pmUrl), pmAppId);
	}

	PrivacyManagerAPIImpl(URL pmUrl, String pmAppId) {
		this.pmUrl = pmUrl;
		this.pmAppId = pmAppId;
		this.restClient = ClientBuilder.newClient();
	}

	@Override
	public String getToken(String pmUser, String pmPwd) throws PrivacyManagerFailureException {

		PMTokenDTO tokenDTO = new PMTokenDTO(pmUser, pmPwd, locale);

		Response response = restClient.target(pmUrl + "/privacyintegrationdev/api/jwt/login")
				.request()
				.header(HTTP_HEADER_NAME_X_CONSUMER_KEY, HTTP_HEADER_VALUE_X_CONSUMER_KEY)
				.header(HttpHeaders.CONTENT_TYPE, "application/json")
				.accept(MediaType.WILDCARD)
				.post(Entity.entity(tokenDTO, MediaType.APPLICATION_JSON));

		LOGGER.info("Response status: {}", response.getStatus());

		checkResponseStatus(response);

		Map<String, Object> bodyAsMap = response.readEntity(Map.class);

		checkStatus(bodyAsMap);

		LOGGER.debug("Response: {}", bodyAsMap);

		token = bodyAsMap.containsKey("opTargetObject") ? bodyAsMap.get("opTargetObject").toString() : null;

		LOGGER.debug("Token is: {}", token);

		return token;
	}

	@Override
	public String retrieveKey() throws PrivacyManagerFailureException {
		checkIfTokenIsSet();

		PMServiceProviderDTO serviceProvider = new PMServiceProviderDTO(pmAppId, appDescription, appVendor, appUrl);

		PMkeyDTO pMkeyDTO = new PMkeyDTO(serviceProvider, 24);

		Response response = restClient.target(pmUrl + "/privacyintegrationdev/api/integration/keymanagement/retrieve")
				.request()
				.header(HTTP_HEADER_NAME_X_CONSUMER_KEY, HTTP_HEADER_VALUE_X_CONSUMER_KEY)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.header(HttpHeaders.CONTENT_TYPE, "application/json")
				.accept(MediaType.WILDCARD)
				.post(Entity.entity(pMkeyDTO, MediaType.APPLICATION_JSON));

		LOGGER.info("Response status: {}", response.getStatus());

		checkResponseStatus(response);

		Map<String, Object> bodyAsMap = response.readEntity(Map.class);

		LOGGER.debug("Response: {}", bodyAsMap);

		checkStatus(bodyAsMap);

		String key = bodyAsMap.containsKey("opTargetObject") ? ((Map<String, Object>) bodyAsMap.get("opTargetObject")).get("keyValue").toString() : null;

		return key;

	}

	@Override
	public String saveApplication() throws PrivacyManagerFailureException {
		checkIfTokenIsSet();

		// TODO : Really? Two times appDescription?
		PMSaveDTO saveDTO = new PMSaveDTO(appDescription, appVendor, appUrl, appDescription);

		Response response = restClient.target(pmUrl + "/privacyintegrationdev/api/integration/service/provider/save")
				.request()
				.header(HTTP_HEADER_NAME_X_CONSUMER_KEY, HTTP_HEADER_VALUE_X_CONSUMER_KEY)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
				.header(HttpHeaders.CONTENT_TYPE, "application/json")
				.accept(MediaType.WILDCARD)
				.post(Entity.entity(saveDTO, MediaType.APPLICATION_JSON));

		LOGGER.info("Response postForEntity " + response.getStatus());

		checkResponseStatus(response);

		Map<String, Object> bodyAsMap = response.readEntity(Map.class);

		LOGGER.debug("Response: {}", bodyAsMap);

		checkStatus(bodyAsMap);

		Map<String, Object> result = bodyAsMap.containsKey("opTargetObject") ? (Map<String, Object>) bodyAsMap.get("opTargetObject") : null;

		// TODO
		return result.toString();
	}

	private void checkStatus(Map<String, Object> bodyAsMap) throws PrivacyManagerFailureException {

		if (bodyAsMap == null
				|| !bodyAsMap.containsKey("success")
				|| !Boolean.parseBoolean(bodyAsMap.get("success").toString())) {
			throw new PrivacyManagerFailureException("TODO");
		}

	}

	private void checkResponseStatus(Response response) throws PrivacyManagerHttpFailureException {

		StatusType statusInfo = response.getStatusInfo();
		Status statusEnum = statusInfo.toEnum();
		if (!OK.equals(statusEnum)) {
			throw new PrivacyManagerHttpFailureException("TODO");
		}

	}

	private void checkIfTokenIsSet() {
		if (Objects.isNull(token)) {
			throw new IllegalStateException("You must get the token before any other operations.");
		}
	}

	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * @return the appDescription
	 */
	public String getAppDescription() {
		return appDescription;
	}

	/**
	 * @param appDescription the appDescription to set
	 */
	public void setAppDescription(String appDescription) {
		this.appDescription = appDescription;
	}

	/**
	 * @return the appVendor
	 */
	public String getAppVendor() {
		return appVendor;
	}

	/**
	 * @param appVendor the appVendor to set
	 */
	public void setAppVendor(String appVendor) {
		this.appVendor = appVendor;
	}

	/**
	 * @return the appUrl
	 */
	public String getAppUrl() {
		return appUrl;
	}

	/**
	 * @param appUrl the appUrl to set
	 */
	public void setAppUrl(String appUrl) {
		this.appUrl = appUrl;
	}

	/**
	 * @return the pmUrl
	 */
	public URL getPmUrl() {
		return pmUrl;
	}

	/**
	 * @return the pmAppId
	 */
	public String getPmAppId() {
		return pmAppId;
	}
}
