/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.qbe.datasource.sql;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;

/**
 *
 * @author Gavardi Giulio(giulio.gavardi@eng.it)
 */

public class DataSetPersister extends SimpleRestClient {

	private final String serviceUrl = "/restful-services/1.0/datasets/list/persist";

	public DataSetPersister() {

	}

	static protected Logger logger = Logger.getLogger(DataSetPersister.class);

	public JSONObject cacheDataSets(JSONObject datasetLabels, String userId) throws Exception {

		logger.debug("IN");

		logger.debug("Call persist service in post");
		Response resp = executePostService(null, serviceUrl, userId, MediaType.APPLICATION_JSON, datasetLabels);
		JSONObject ja = null;
		String respString = resp.readEntity(String.class);
		if (respString != null && !respString.equals("")) {
			ja = new JSONObject(respString);
		}

		logger.debug("OUT");

		return ja;
	}

}
