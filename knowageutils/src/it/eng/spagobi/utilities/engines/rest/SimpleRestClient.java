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
package it.eng.spagobi.utilities.engines.rest;

import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.security.hmacfilter.HMACFilterAuthenticationProvider;
import it.eng.spagobi.security.hmacfilter.HMACUtils;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tools.dataset.ckan.utils.CKANUtils;
import it.eng.spagobi.utilities.Helper;

import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.axis.encoding.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Logger;

/**
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it), Giulio Gavardi(giulio.gavardi@eng.it)
 */

@SuppressWarnings("deprecation")
public class SimpleRestClient {

	static protected Logger logger = Logger.getLogger(SimpleRestClient.class);

	private boolean addServerUrl = true;
	private boolean useProxy = false;

	private HMACFilterAuthenticationProvider authenticationProvider;

	public SimpleRestClient(String hmacKey) {
		Helper.checkNotNullNotTrimNotEmpty(hmacKey, "hmacKey");

		authenticationProvider = new HMACFilterAuthenticationProvider(hmacKey);
	}

	public SimpleRestClient() {
		String key = EnginConf.getInstance().getHmacKey();
		if (key == null || key.isEmpty()) {
			key = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue(HMACUtils.HMAC_JNDI_LOOKUP));
		}
		if (key == null || key.isEmpty()) {
			logger.warn("HMAC key not found. Requests will not be authenticated.");
		} else {
			authenticationProvider = new HMACFilterAuthenticationProvider(key);
		}
	}

	public SimpleRestClient(boolean useProxy) {
		this();
		useProxy = useProxy;
	}

	/**
	 * Invokes a rest service in get and return response
	 *
	 * @param parameters
	 *            the parameters of the request
	 * @param serviceUrl
	 *            the relative (refers always to core application context) path of the service
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	protected Response executeGetService(Map<String, Object> parameters, String serviceUrl, String userId) throws Exception {
		return executeService(parameters, serviceUrl, userId, RequestTypeEnum.GET, null, null);
	}

	/**
	 * Invokes a rest service in post and return response
	 *
	 * @param parameters
	 *            the parameters of the request
	 * @param serviceUrl
	 *            the relative (refers always to core application context) path of the service
	 * @param userId
	 * @param mediaType
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	protected Response executePostService(Map<String, Object> parameters, String serviceUrl, String userId, String mediaType, Object data)
			throws Exception {
		return executeService(parameters, serviceUrl, userId, RequestTypeEnum.POST, mediaType, data);
	}

	@SuppressWarnings({ "rawtypes" })
	private Response executeService(Map<String, Object> parameters, String serviceUrl, String userId, RequestTypeEnum type, String mediaType,
			Object data) throws Exception {
		logger.debug("IN");


		MultivaluedMap<String, Object> myHeaders =	  new MultivaluedHashMap<String, Object>();


		if (!serviceUrl.contains("http") && addServerUrl) {
			logger.debug("Adding the server URL");
			String serverUrl = EnginConf.getInstance().getSpagoBiServerUrl();
			if (serverUrl != null) {
				logger.debug("Executing the dataset from the core so use relative path to service");
				serviceUrl = serverUrl + serviceUrl;
			}
			logger.debug("Call service URL " + serviceUrl);
		}

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(serviceUrl);
		Builder request = target.request(mediaType);

		logger.debug("adding headers");

		addAuthorizations(request, userId,myHeaders);


		if (parameters != null) {
			Iterator<String> iter = parameters.keySet().iterator();
			while (iter.hasNext()) {
				String param = iter.next();
				target = target.queryParam(param, parameters.get(param));
				logger.debug("Adding parameter " + param);
			}
		}

		logger.debug("Call service");
		Response response = null;

		// provide authentication exactly before of call
		authenticationProvider.provideAuthentication(request, target, myHeaders, data);
		if (type.equals(RequestTypeEnum.POST))
			response = request.post(Entity.json(data.toString()));
		else
			response = request.get();

		if (response.getStatus() >= 400) {
			throw new RuntimeException("Request failed with HTTP error code : " + response.getStatus());
		}

		logger.debug("Rest query status " + response.getStatus());
		// logger.debug("Rest query status info "+response.getStatusInfo());
		logger.debug("Rest query status getReasonPhrase " + response.getStatusInfo().getReasonPhrase());
		logger.debug("OUT");
		return response;
	}

	private void addAuthorizations(Builder request, String userId, MultivaluedMap<String, Object> myHeaders ) throws Exception {
		logger.debug("Adding auth for user " + userId);

		String encodedBytes = Base64.encode(userId.getBytes("UTF-8"));
		request.header("Authorization", "Direct " + encodedBytes);
		myHeaders.add("Authorization", "Direct " + encodedBytes);
	}



	public boolean isAddServerUrl() {
		return addServerUrl;
	}

	public void setAddServerUrl(boolean addServerUrl) {
		this.addServerUrl = addServerUrl;
	}

	public enum RequestTypeEnum {
		POST, GET
	}

	public static HttpClient getHttpClient(boolean useProxy) {

		// Getting proxy properties set as JVM args
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");
		int proxyPortInt = CKANUtils.portAsInteger(proxyPort);
		String proxyUsername = System.getProperty("http.proxyUsername");
		String proxyPassword = System.getProperty("http.proxyPassword");

		logger.debug("Setting REST client");
		HttpClient httpClient = new HttpClient();
		httpClient.setConnectionTimeout(500);

		if (proxyHost != null && proxyPortInt > 0 && useProxy) {
			if (proxyUsername != null && proxyPassword != null) {
				logger.debug("Setting proxy with authentication");
				httpClient.getHostConfiguration().setProxy(proxyHost, proxyPortInt);
				HttpState state = new HttpState();
				state.setProxyCredentials(null, null, new UsernamePasswordCredentials(proxyUsername, proxyPassword));
				httpClient.setState(state);
				logger.debug("Proxy with authentication set");
			} else {
				// Username and/or password not acceptable. Trying to set proxy without credentials
				logger.debug("Setting proxy without authentication");
				httpClient.getHostConfiguration().setProxy(proxyHost, proxyPortInt);
				logger.debug("Proxy without authentication set");
			}
		} else {
			logger.debug("No proxy configuration found");
		}
		logger.debug("REST client set");

		return httpClient;
	}

}
