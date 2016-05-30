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
import it.eng.spagobi.security.hmacfilter.HMACFilter;
import it.eng.spagobi.security.hmacfilter.HMACFilterAuthenticationProvider;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tools.dataset.ckan.CKANClient;
import it.eng.spagobi.utilities.Helper;

import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.apache.axis.encoding.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;

/**
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it), Giulio Gavardi(giulio.gavardi@eng.it)
 */

@SuppressWarnings("deprecation")
public class SimpleRestClient {

	static protected Logger logger = Logger.getLogger(SimpleRestClient.class);

	private boolean addServerUrl = true;

	private HMACFilterAuthenticationProvider authenticationProvider;

	public SimpleRestClient(String hmacKey) {
		Helper.checkNotNullNotTrimNotEmpty(hmacKey, "hmacKey");

		authenticationProvider = new HMACFilterAuthenticationProvider(hmacKey);
	}

	public SimpleRestClient() {
		String key = EnginConf.getInstance().getHmacKey();
		if (key == null || key.isEmpty()) {
			key = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue(HMACFilter.HMAC_JNDI_LOOKUP));
		}
		if (key == null || key.isEmpty()) {
			logger.warn("HMAC key not found. Requests will not be authenticated.");
		} else {
			authenticationProvider = new HMACFilterAuthenticationProvider(key);
		}
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
	protected ClientResponse executeGetService(Map<String, Object> parameters, String serviceUrl, String userId) throws Exception {
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
	protected ClientResponse executePostService(Map<String, Object> parameters, String serviceUrl, String userId, MediaType mediaType, Object data)
			throws Exception {
		return executeService(parameters, serviceUrl, userId, RequestTypeEnum.POST, mediaType, data);
	}

	@SuppressWarnings({ "rawtypes" })
	private ClientResponse executeService(Map<String, Object> parameters, String serviceUrl, String userId, RequestTypeEnum type, MediaType mediaType,
			Object data) throws Exception {
		logger.debug("IN");

		if (!serviceUrl.contains("http") && addServerUrl) {
			logger.debug("Adding the server URL");
			String serverUrl = EnginConf.getInstance().getSpagoBiServerUrl();
			if (serverUrl != null) {
				logger.debug("Executing the dataset from the core so use relative path to service");
				serviceUrl = serverUrl + serviceUrl;
			}
			logger.debug("Call service URL " + serviceUrl);
		}

		HttpClient httpClient = getHttpClient();
		ApacheHttpClientExecutor httpExecutor = httpClient == null ? new ApacheHttpClientExecutor() : new ApacheHttpClientExecutor(httpClient);
		ClientRequest request = new ClientRequest(serviceUrl, httpExecutor);

		logger.debug("adding headers");

		addAuthorizations(request, userId);

		if (mediaType != null && data != null) {
			logger.debug("adding body");
			request.body(mediaType, data);
		}

		if (parameters != null) {
			Iterator<String> iter = parameters.keySet().iterator();
			while (iter.hasNext()) {
				String param = iter.next();
				request.queryParameter(param, parameters.get(param));
				logger.debug("Adding parameter " + param);
			}
		}

		logger.debug("Call service");
		ClientResponse response = null;

		// provide authentication exactly before of call
		authenticationProvider.provideAuthentication(request);
		if (type.equals(RequestTypeEnum.POST))
			response = request.post();
		else
			response = request.get();

		if (response.getStatus() >= 400) {
			throw new RuntimeException("Request failed with HTTP error code : " + response.getStatus());
		}

		logger.debug("Rest query status "+response.getStatus());
		logger.debug("Rest query status info "+response.getStatusInfo());
		logger.debug("Rest query status getReasonPhrase "+response.getResponseStatus().getReasonPhrase());
		logger.debug("OUT");
		return response;
	}

	private void addAuthorizations(ClientRequest request, String userId) throws Exception {
		logger.debug("Adding auth for user " + userId);

		String encodedBytes = Base64.encode(userId.getBytes("UTF-8"));
		request.header("Authorization", "Direct " + encodedBytes);

	}

	protected HttpClient getHttpClient() {
		return CKANClient.getHttpClient();
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

}
