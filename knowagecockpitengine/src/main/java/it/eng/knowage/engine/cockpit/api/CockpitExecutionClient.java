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
package it.eng.knowage.engine.cockpit.api;

import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;

public class CockpitExecutionClient extends SimpleRestClient {

	private static final String SERVICE_URL = "/restful-services/2.0/datasets";
	static private Logger logger = Logger.getLogger(CockpitExecutionClient.class);

	public String getDataFromDataset(String aggregations, String label, String userId, Map<String, Object> queryParams) {

		logger.debug("IN");
		Response clientResponse;
		String jsonResponse = null;
		try {
			logger.debug("executing post service to get data");
			clientResponse = executePostService(queryParams, SERVICE_URL + "/" + label + "/data", userId, MediaType.APPLICATION_JSON, aggregations);
			logger.debug("getting data as string from response");
			jsonResponse = clientResponse.readEntity(String.class);

		} catch (Exception e) {
			logger.error("Error while getting data", e);
			throw new SpagoBIEngineRuntimeException("Error while getting data: " + e.getMessage(), e);
		}
		return jsonResponse;
	}

}
