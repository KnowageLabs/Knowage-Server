/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.

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
package it.eng.knowage.engine.cockpit.api.export;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 * @author Marco Balestri (marco.balestri@eng.it)
 */

public class ExporterClient extends SimpleRestClient {
	private final String serviceUrl = "/restful-services/2.0/datasets/%s/data";

	static protected Logger logger = Logger.getLogger(ExporterClient.class);

	public JSONObject getDataStore(Map<String, Object> parameters, String datasetLabel, String userId, String body) throws Exception {
		// if pagination is disabled offset = 0, fetchSize = -1
		return getDataStore(parameters, datasetLabel, userId, body, 0, -1);
	}

	public JSONObject getDataStore(Map<String, Object> parameters, String datasetLabel, String userId, String body, int offset, int fetchSize)
			throws Exception {
		logger.debug("IN");
		parameters.put("offset", offset);
		parameters.put("size", fetchSize);
		logger.debug("parameters: [" + parameters + "], serviceUrl: [" + serviceUrl + "], datasetLabel: [" + datasetLabel + "], body: [" + body + "]");

		Response resp = executePostService(parameters, String.format(serviceUrl, datasetLabel), userId, MediaType.APPLICATION_JSON, body);
		String resultString = resp.readEntity(String.class);
		JSONObject result = new JSONObject(resultString);

		logger.debug("Response: [" + result + "]");
		logger.debug("OUT");
		if (result.has("errors"))
			throw new SpagoBIRuntimeException("Error in data service for dataset: " + datasetLabel);
		return result;
	}
}