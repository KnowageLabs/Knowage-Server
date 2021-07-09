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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.api.common.AbstractDataSetResource;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.content.service.ContentServiceImplSupplier;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.user.UserProfileManager;
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

	@POST
	@Path("/edit/{output_type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.EDIT_PYTHON_SCRIPTS })
	public Response editWidget(@PathParam("output_type") String outputType, PythonWidgetDTO pythonWidgetDTO) {
		return executeWidget(outputType, pythonWidgetDTO);
	}

	@POST
	@Path("/view/{output_type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response viewWidget(@PathParam("output_type") String outputType, PythonWidgetDTO pythonWidgetDTO) {
		try {
			UserProfile userProfile = UserProfileManager.getProfile();
			String userId = (String) userProfile.getUserUniqueIdentifier();
			ContentServiceImplSupplier supplier = new ContentServiceImplSupplier();
			String script = PythonUtils.getScriptFromTemplate(
					supplier.readTemplate(userId, pythonWidgetDTO.getDocumentId(), pythonWidgetDTO.getDrivers()).getContent(), pythonWidgetDTO.getWidgetId());
			pythonWidgetDTO.setScript(script);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Cannot retrieve script from template", e);
		}
		return executeWidget(outputType, pythonWidgetDTO);
	}

	private Response executeWidget(String outputType, PythonWidgetDTO pythonWidgetDTO) {
		String pythonAddress = null, body = null, datastore = null;
		it.eng.spagobi.utilities.rest.RestUtilities.Response pythonResponse = null;

		try {
			String dsLabel = pythonWidgetDTO.getDatasetLabel();
			Map<String, Object> drivers = pythonWidgetDTO.getDrivers();
			if (dsLabel != null) {
				datastore = getDataStore(dsLabel, pythonWidgetDTO.getParameters(), drivers, pythonWidgetDTO.getSelections(), null, -1,
						pythonWidgetDTO.getAggregations(), null, -1, -1, null, null);
			}
			body = PythonUtils.createPythonEngineRequestBody(datastore, dsLabel, pythonWidgetDTO.getScript(), drivers, pythonWidgetDTO.getOutputVariable());
			pythonAddress = PythonUtils.getPythonAddress(pythonWidgetDTO.getEnvironmentLabel());
			pythonResponse = RestUtilities.makeRequest(methodPost, pythonAddress + "2.0/widget/" + outputType, headers, body);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while making request to python engine", e);
		}

		try {
			if (pythonResponse == null || pythonResponse.getStatusCode() != 200) {
				JSONObject toReturn = new JSONObject().put("error", pythonResponse.getResponseBody());
				return Response.status(Status.BAD_REQUEST).entity(toReturn.toString()).build();
			} else {
				JSONObject toReturn = new JSONObject().put("result", pythonResponse.getResponseBody());
				return Response.ok(toReturn.toString()).build();
			}
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while creating response json", e);
		}
	}

	@GET
	@Path("/libraries/{env_label}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.EDIT_PYTHON_SCRIPTS })
	public Response libraries(@PathParam("env_label") String envLabel) {
		logger.debug("IN");
		it.eng.spagobi.utilities.rest.RestUtilities.Response response = null;
		String pythonAddress = null;
		try {
			pythonAddress = PythonUtils.getPythonAddress(envLabel);
			response = RestUtilities.makeRequest(methodGet, pythonAddress + "dataset/libraries", headers, null);
		} catch (Exception e) {
			logger.error("cannot retrieve list of available libraries from Python engine at address [" + pythonAddress + "]");
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
