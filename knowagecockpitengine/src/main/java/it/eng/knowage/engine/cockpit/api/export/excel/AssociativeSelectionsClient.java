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
package it.eng.knowage.engine.cockpit.api.export.excel;

import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * @authors Francesco Lucchi (francesco.lucchi@eng.it)
 *
 */

public class AssociativeSelectionsClient extends SimpleRestClient {
	private final String serviceUrl = "/restful-services/2.0/associativeSelections";

	static protected Logger logger = Logger.getLogger(AssociativeSelectionsClient.class);

	public JSONObject getAssociativeSelections(Map<String, Object> parameters, String userId, String body) throws Exception {
		logger.debug("IN");

		Response resp = executePostService(parameters, serviceUrl, userId, MediaType.APPLICATION_JSON, body);
		String resultString = resp.readEntity(String.class);
		JSONObject result = new JSONObject(resultString);

		logger.debug("OUT");
		return result;
	}
}