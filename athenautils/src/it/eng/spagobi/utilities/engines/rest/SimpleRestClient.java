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

import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tools.dataset.ckan.CKANClient;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.json.JSONArray;

/**
 * 
 * @author ALberto Ghedin (alberto.ghedin@eng.it)
 */


public class SimpleRestClient {
	
	static protected Logger logger = Logger.getLogger(SimpleRestClient.class);
	
	private boolean addServerUrl = true;

	
	/**
	 * Invokes a rest service and return parameters
	 * @param parameters
	 * @param serviceUrl
	 * @return
	 * @throws ServletException
	 * @throws IOException
	 * @throws Exception
	 */
	protected ClientResponse executeService(Map<String,Object> parameters,  String serviceUrl) throws Exception {

		logger.debug("IN");

		if(!serviceUrl.contains("http") && addServerUrl){
			logger.debug("Adding the server URL");
			String serverUrl = EnginConf.getInstance().getSpagoBiServerUrl();
			serviceUrl = serverUrl + serviceUrl;
			logger.debug("Call service URL " + serviceUrl);
		}

		ApacheHttpClientExecutor httpExecutor = new ApacheHttpClientExecutor(CKANClient.getHttpClient());
		ClientRequest request = new ClientRequest(serviceUrl, httpExecutor);

		JSONArray array = new JSONArray();
		if(parameters!=null){
			Iterator<String> iter = parameters.keySet().iterator();
			while (iter.hasNext()) {
				String param = (String) iter.next();
				request.queryParameter(param, parameters.get(param));
				logger.debug("Adding parameter "+param);
			}
		}

		logger.debug("Call service");
		ClientResponse response = request.get();

		if (response.getStatus() >= 400) {
			throw new RuntimeException("Request failed with HTTP error code : " + response.getStatus());
		}

		logger.debug("OUT");
		return response;
	}
	
	
	
	public boolean isAddServerUrl() {
		return addServerUrl;
	}



	public void setAddServerUrl(boolean addServerUrl) {
		this.addServerUrl = addServerUrl;
	}

}
