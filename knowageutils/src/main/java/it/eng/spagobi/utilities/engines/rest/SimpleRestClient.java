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

import java.util.Base64;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;

import it.eng.knowage.export.wrapper.beans.RenderOptions;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.security.hmacfilter.HMACFilterAuthenticationProvider;
import it.eng.spagobi.security.hmacfilter.HMACUtils;
import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.utilities.Helper;

/**
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it), Giulio Gavardi(giulio.gavardi@eng.it)
 */

public class SimpleRestClient {

	static protected Logger logger = Logger.getLogger(SimpleRestClient.class);

	private boolean addServerUrl = true;

	private HMACFilterAuthenticationProvider authenticationProvider;

	private String serverUrl;

	public SimpleRestClient(String hmacKey) {
		init(hmacKey);
	}

	public SimpleRestClient() {
		String hmacKey = loadHmacKey();
		init(hmacKey);
	}

	private void init(String hmacKey) {
		Helper.checkNotNullNotTrimNotEmpty(hmacKey, "hmacKey");
		this.authenticationProvider = new HMACFilterAuthenticationProvider(hmacKey);
		this.serverUrl = loadServerUrl();
	}

	private String loadHmacKey() {
		String hmacKey = EnginConf.getInstance().getHmacKey();
		if (StringUtilities.isEmpty(hmacKey)) {
			hmacKey = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue(HMACUtils.HMAC_JNDI_LOOKUP));
		}
		return hmacKey;
	}

	private String loadServerUrl() {
		String serverUrl = EnginConf.getInstance().getSpagoBiServerUrl();
		if (StringUtilities.isEmpty(serverUrl)) {
			serverUrl = SpagoBIUtilities.readJndiResource(SingletonConfig.getInstance().getConfigValue("SPAGOBI.SPAGOBI_SERVICE_JNDI"));
		}
		return serverUrl;
	}

	/**
	 * Invokes a rest service in get and return response
	 *
	 * @param parameters the parameters of the request
	 * @param serviceUrl the relative (refers always to core application context) path of the service
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	protected Response executeGetService(Map<String, Object> parameters, String serviceUrl, String userId) throws Exception {
		return executeService(parameters, serviceUrl, userId, RequestTypeEnum.GET, null, null);
	}

	/**
	 * Invokes a rest service in post and return response
	 *
	 * @param parameters the parameters of the request
	 * @param serviceUrl the relative (refers always to core application context) path of the service
	 * @param userId
	 * @param mediaType
	 * @param data
	 * @return
	 * @throws Exception
	 */
	protected Response executePostService(Map<String, Object> parameters, String serviceUrl, String userId, String mediaType, Object data) throws Exception {
		return executeService(parameters, serviceUrl, userId, RequestTypeEnum.POST, mediaType, data);
	}

	/**
	 * Invokes a rest service in post and return response
	 *
	 * @param parameters the parameters of the request
	 * @param serviceUrl the relative (refers always to core application context) path of the service
	 * @param userId
	 * @param mediaType
	 * @param data
	 * @return
	 * @throws Exception
	 */
	protected Response executePostService(Map<String, Object> parameters, String serviceUrl, String userId, String mediaType, Object data,
			RenderOptions renderOptions) throws Exception {
		return executeService(parameters, serviceUrl, userId, RequestTypeEnum.POST, mediaType, data);
	}

	/**
	 * Invokes a rest service in post and return response
	 *
	 * @param parameters the parameters of the request
	 * @param serviceUrl the relative (refers always to core application context) path of the service
	 * @param userId
	 * @param mediaType
	 * @param data
	 * @return
	 * @throws Exception
	 */
	protected Response executePutService(Map<String, Object> parameters, String serviceUrl, String userId, String mediaType, Object data) throws Exception {
		return executeService(parameters, serviceUrl, userId, RequestTypeEnum.PUT, mediaType, data);
	}

	protected HttpResponse executePostServiceWithFormParams(Map<String, Object> parameters, byte[] form, String serviceUrl, String userId) throws Exception {
		return executeServiceMultipart(parameters, form, serviceUrl, userId);
	}

	@SuppressWarnings({ "rawtypes" })
	private HttpResponse executeServiceMultipart(Map<String, Object> parameters, byte[] form, String serviceUrl, String userId) throws Exception {
		logger.debug("IN");
		CloseableHttpClient client = null;
		MultivaluedMap<String, Object> myHeaders = new MultivaluedHashMap<String, Object>();

		if (!serviceUrl.contains("http") && addServerUrl) {
			logger.debug("Adding the server URL");
			if (serverUrl != null) {
				logger.debug("Executing the dataset from the core so use relative path to service");
				serviceUrl = serverUrl + serviceUrl;
			}
			logger.debug("Call service URL " + serviceUrl);
		}

		try {

			if (parameters != null) {
				logger.debug("adding parameters in the request");
				StringBuilder sb = new StringBuilder(serviceUrl);
				sb.append("?");
				for (Iterator iterator = parameters.keySet().iterator(); iterator.hasNext();) {
					String key = (String) iterator.next();
					sb.append(key);
					sb.append("=");
					sb.append(parameters.get(key));
				}
				logger.debug("finish to add parameters in the request");
			}

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addPart("file", new ByteArrayBody(form, "file"));
			HttpPost request = new HttpPost(serviceUrl);
			request.setEntity(builder.build());
			client = HttpClientBuilder.create().build();

			String encodedBytes = Base64.getEncoder().encodeToString(userId.getBytes("UTF-8"));
			request.addHeader("Authorization", "Direct " + encodedBytes);

			authenticationProvider.provideAuthenticationMultiPart(request, myHeaders);

			HttpResponse response1 = client.execute(request);

			if (response1.getStatusLine().getStatusCode() >= 400) {
				throw new RuntimeException("Request failed with HTTP error code : " + response1.getStatusLine().getStatusCode());
			}

			logger.debug("Rest query status " + response1.getStatusLine().getStatusCode());
			// logger.debug("Rest query status info "+response.getStatusInfo());
			logger.debug("Rest query status getReasonPhrase " + response1.getStatusLine().getReasonPhrase());
			logger.debug("OUT");
			return response1;
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	private Response executeService(Map<String, Object> parameters, String serviceUrl, String userId, RequestTypeEnum type, String mediaType, Object data)
			throws Exception {
		logger.debug("IN");

		MultivaluedMap<String, Object> myHeaders = new MultivaluedHashMap<String, Object>();

		if (!serviceUrl.contains("http") && addServerUrl) {
			logger.debug("Adding the server URL");
			if (serverUrl != null) {
				logger.debug("Executing the dataset from the core so use relative path to service");
				serviceUrl = serverUrl + serviceUrl;
			}
		}

		Client client = ClientBuilder.newBuilder().sslContext(SSLContext.getDefault()).build();

		logger.debug("Service URL to be invoked : " + serviceUrl);
		WebTarget target = client.target(serviceUrl);

		if (parameters != null) {
			Iterator<String> iter = parameters.keySet().iterator();
			while (iter.hasNext()) {
				String param = iter.next();
				LogMF.debug(logger, "Adding parameter [{0}] : [{1}]", param, parameters.get(param));
				target = target.queryParam(param, parameters.get(param));
			}
		}

		logger.debug("Media type : " + mediaType);
		Builder request = target.request(mediaType);

		logger.debug("adding headers");

		addAuthorizations(request, userId, myHeaders);

		logger.debug("Call service");
		Response response = null;

		// provide authentication exactly before of call
		authenticationProvider.provideAuthentication(request, target, myHeaders, data);

		if (type.equals(RequestTypeEnum.POST)) {
			response = request.post(Entity.json(data.toString()));
		} else if (type.equals(RequestTypeEnum.PUT)) {
			response = request.put(Entity.json(data.toString()));
		} else {
			response = request.get();
		}

		logger.debug("Rest query status " + response.getStatus());
		// logger.debug("Rest query status info "+response.getStatusInfo());
		logger.debug("Rest query status getReasonPhrase " + response.getStatusInfo().getReasonPhrase());
		logger.debug("OUT");
		return response;
	}

	private void addAuthorizations(Builder request, String userId, MultivaluedMap<String, Object> myHeaders) throws Exception {
		logger.debug("Adding auth for user " + userId);

		String encodedBytes = Base64.getEncoder().encodeToString(userId.getBytes("UTF-8"));
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
		POST, GET, PUT
	}
}
