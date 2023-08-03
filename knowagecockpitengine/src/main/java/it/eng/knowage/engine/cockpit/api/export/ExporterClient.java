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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 * @author Marco Balestri (marco.balestri@eng.it)
 */

public class ExporterClient extends SimpleRestClient {

	private static final String SERVICE_URL = "/restful-services/2.0/datasets/%s/data";
	private static final Logger LOGGER = LogManager.getLogger(ExporterClient.class);

	public JSONObject getDataStore(Map<String, Object> parameters, String datasetLabel, String userId, String body)
			throws Exception {
		// if pagination is disabled offset = 0, fetchSize = -1
		return getDataStore(parameters, datasetLabel, userId, body, 0, -1);
	}

	public JSONObject getDataStore(Map<String, Object> parameters, String datasetLabel, String userId, String body,
			int offset, int fetchSize) throws Exception {
		parameters.put("offset", offset);
		parameters.put("size", fetchSize);
		String url = String.format(SERVICE_URL, datasetLabel);

		Response resp = executePostService(parameters, url, userId, MediaType.APPLICATION_JSON, body);
		String resultString = resp.readEntity(String.class);
		JSONObject result = new JSONObject(resultString);

		if (result.has("errors")) {
			LOGGER.error("Error calling {} with parameters {} and body {} returned: {}", url, parameters, body, result);
			throw new SpagoBIRuntimeException("Error in data service for dataset: " + datasetLabel);
		} else {
			LOGGER.debug("Call to {} with parameters {} and body {} returned: {}", url, parameters, body, result);
		}
		return result;
	}
}