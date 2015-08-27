package it.eng.spagobi.commons.utilities;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import it.eng.spagobi.services.common.EnginConf;
import it.eng.spagobi.tools.dataset.ckan.CKANClient;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;
import org.json.JSONArray;

/**
 * 
 * @author Gavardi Giulio(giulio.gavardi@eng.it)
 */

public class DataSetPersister {

	static protected Logger logger = Logger.getLogger(DataSetPersister.class);

	public void persistDataSets(ArrayList<String> labels, String userId) throws ServletException, IOException, Exception {

		logger.debug("IN");

		String serverUrl = EnginConf.getInstance().getSpagoBiServerUrl();
		String serviceUrl = serverUrl + "/restful-services/1.0/datasets/list/persist";

		logger.debug("Call service URL " + serverUrl);

		ApacheHttpClientExecutor httpExecutor = new ApacheHttpClientExecutor(CKANClient.getHttpClient());
		ClientRequest request = new ClientRequest(serviceUrl, httpExecutor);

		JSONArray array = new JSONArray();
		for (int i = 0; i < labels.size(); i++) {
			String lab = labels.get(i);
			array.put(lab);
		}

		request.queryParameter("labels", array);
		request.queryParameter("user_id", userId);

		logger.debug("Call persist service in post");
		ClientResponse response = request.get();

		if (response.getStatus() >= 400) {
			throw new RuntimeException("Request to persist datasetss failed with HTTP error code : " + response.getStatus());
		}

		logger.debug("OUT");
	}

}
