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
package it.eng.knowage.engine.cockpit.api.engine;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;

public class ProductProfilerClient extends SimpleRestClient {
	private final String serviceUrl = "/restful-services/2.0/backendservices/productprofiler/cockpit/widget";

	static protected Logger logger = Logger.getLogger(ProductProfilerClient.class);

	public boolean isAllowedToCreateWidget(String userId, String type) throws Exception {
		boolean toReturn = false;
		logger.debug("IN");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("type", type);
		Response resp = executeGetService(params, String.format(serviceUrl), userId);
		toReturn = new Boolean(resp.readEntity(String.class));
		return toReturn;
	}
}