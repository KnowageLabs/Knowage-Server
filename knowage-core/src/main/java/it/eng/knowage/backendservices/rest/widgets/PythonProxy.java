/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.backendservices.rest.widgets;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.api.common.AbstractDataSetResource;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;
import it.eng.spagobi.utilities.rest.RestUtilities.HttpMethod;

/*
 https://localhost:8080/knowage/restful-services/2.0/backendservices/widgets/python
 */

@Path("/2.0/backendservices/widgets/python")
public class PythonProxy extends AbstractDataSetResource {

	Map<String, String> headers;
	HttpMethod methodPost = HttpMethod.valueOf("Post");
	HttpMethod methodGet = HttpMethod.valueOf("Get");

	static protected Logger logger = Logger.getLogger(PythonProxy.class);

	@GET
	@Path("/libraries/{env_label}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.EDIT_PYTHON_SCRIPTS })
	public Response libraries(@PathParam("env_label") String envLabel) {
		logger.debug("IN");
		it.eng.spagobi.utilities.rest.RestUtilities.Response response = null;
		try {
			String pythonAddress = PythonUtils.getPythonAddress(envLabel);
			response = RestUtilities.makeRequest(methodGet, pythonAddress + "dataset/libraries", headers, null);
		} catch (Exception e) {
			logger.error("cannot retrieve list of available libraries from Python engine");
			throw new SpagoBIRuntimeException("cannot retrieve list of available libraries from Python engine", e);
		}
		if (response == null || response.getStatusCode() != 200) {
			return Response.status(400).build();
		} else {
			JSONObject toReturn;
			try {
				toReturn = new JSONObject().put("result", response.getResponseBody());
			} catch (Exception e) {
				logger.error("error while creating response json containing available python libraries");
				throw new SpagoBIRuntimeException("error while creating response json containing available python libraries", e);
			}
			return Response.ok(toReturn.toString()).build();
		}
	}
}
