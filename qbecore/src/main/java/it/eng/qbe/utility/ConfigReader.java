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
package it.eng.qbe.utility;

import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.utilities.engines.rest.SimpleRestClient;

/**
 *
 * @author Gavardi Giulio(giulio.gavardi@eng.it)
 */

public class ConfigReader extends SimpleRestClient {

	private String serviceUrl = "/restful-services/2.0/configs";

	private static final String CONFIG_LABEL = "KNOWAGE.CUSTOMIZED_DATABASE_FUNCTIONS";

	UserProfile userProfile = null;

	public ConfigReader(UserProfile _userProfile) {
		userProfile = _userProfile;
	}

	static protected Logger logger = Logger.getLogger(ConfigReader.class);

	public String readCustom() throws Exception {

		logger.debug("IN");
		String toReturn = null;
		logger.debug("Call persist service in post");
		String serviceUrlToCall = serviceUrl + "/" + CONFIG_LABEL + "/-1";

		Response resp = executeGetService(null, serviceUrlToCall, userProfile.getUserUniqueIdentifier().toString());

		String respString = resp.readEntity(String.class);

		if (respString != null && !respString.equals("")) {
			JSONObject json = new JSONObject(respString);
			toReturn = json.optString("data");
		}

		logger.debug("OUT");

		return toReturn;
	}

}
