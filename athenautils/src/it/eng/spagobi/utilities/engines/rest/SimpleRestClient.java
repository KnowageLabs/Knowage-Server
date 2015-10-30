/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version.
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file.
 */
package it.eng.spagobi.utilities.engines.rest;

import it.eng.spagobi.security.hmacfilter.HMACFilter;
import it.eng.spagobi.security.hmacfilter.HMACFilterAuthenticationProvider;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tools.dataset.ckan.CKANClient;
import it.eng.spagobi.utilities.Helper;

import java.util.Iterator;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.MediaType;

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
	private final String credential = "YmlhZG1pbjpiaWFkbWlu";
	private boolean addServerUrl = true;

	private HMACFilterAuthenticationProvider authenticationProvider;

	public SimpleRestClient(String hmacKey) {
		Helper.checkNotNullNotTrimNotEmpty(hmacKey, "hmacKey");

		authenticationProvider = new HMACFilterAuthenticationProvider(hmacKey);
	}

	public SimpleRestClient() {
		try {
			String key = (String) (new InitialContext().lookup(HMACFilter.HMAC_JNDI_LOOKUP));
			if (key == null) {
				logger.warn("HMAC key not found. Requests will not be authenticated.");
			}
			authenticationProvider = new HMACFilterAuthenticationProvider(key);
		} catch (NamingException e) {
			logger.warn("HMAC key not found. Requests will not be authenticated.", e);
		}
	}

	/**
	 * Invokes a rest service in get and return response
	 *
	 * @param parameters
	 *            the parameters of the request
	 * @param serviceUrl
	 *            the relative (refers always to core application context) path of the service
	 * @return the response
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	protected ClientResponse executeGetService(Map<String, Object> parameters, String serviceUrl) throws Exception {
		return executeService(parameters, serviceUrl, RequestTypeEnum.GET, null, null);
	}

	/**
	 * Invokes a rest service in post and return response
	 *
	 * @param parameters
	 *            the parameters of the request
	 * @param serviceUrl
	 *            the relative (refers always to core application context) path of the service
	 * @param mediaType
	 * @param data
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	protected ClientResponse executePostService(Map<String, Object> parameters, String serviceUrl, MediaType mediaType, Object data) throws Exception {
		return executeService(parameters, serviceUrl, RequestTypeEnum.POST, mediaType, data);
	}

	@SuppressWarnings({ "rawtypes" })
	private ClientResponse executeService(Map<String, Object> parameters, String serviceUrl, RequestTypeEnum type, MediaType mediaType, Object data)
			throws Exception {
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
		request.header("Authorization", credential);

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

		logger.debug("OUT");
		return response;
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
