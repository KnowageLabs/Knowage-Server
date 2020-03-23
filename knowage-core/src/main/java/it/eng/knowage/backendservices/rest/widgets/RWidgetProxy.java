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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
 https://localhost:8080/knowage/restful-services/2.0/backendservices/widgets/RWidget
 */

@Path("/2.0/backendservices/widgets/RWidget")
public class RWidgetProxy extends AbstractDataSetResource {

	String rAddress = "http://localhost:5001/";
	Map<String, String> headers;
	HttpMethod methodPost = HttpMethod.valueOf("Post");

	static protected Logger logger = Logger.getLogger(RWidgetProxy.class);

	@POST
	@Path("/view/{output_type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response view(@PathParam("output_type") String outputType, HashMap<String, String> requestBody) {
		logger.debug("IN");
		UserProfile userProfile = UserProfileManager.getProfile();
		String userId = (String) userProfile.getUserUniqueIdentifier();
		ContentServiceImplSupplier supplier = new ContentServiceImplSupplier();
		String script = null, documentId = null, outputVariable = null, dsLabel = null, parameters = null, drivers = null, aggregations = null,
				selections = null, widgetId = null;
		try {
			dsLabel = requestBody.get("dataset");
			documentId = requestBody.get("document_id");
			widgetId = requestBody.get("widget_id");
			outputVariable = requestBody.get("output_variable");
			parameters = requestBody.get("parameters");
			drivers = requestBody.get("drivers");
			aggregations = requestBody.get("aggregations");
			selections = requestBody.get("selections");
			script = RUtils.getRCodeFromTemplate(supplier.readTemplate(userId, documentId, null).getContent(), widgetId);
		} catch (Exception e) {
			logger.error("error while retrieving request information for userId [" + userId + "] and documentId [" + documentId + "]");
			throw new SpagoBIRuntimeException("error while retrieving request information for userId [" + userId + "] and documentId [" + documentId + "]", e);
		}
		String knowageDs = getDataStore(dsLabel, parameters, null, selections, null, -1, aggregations, null, -1, -1, false, null, null);
		String rDataframe = RUtils.DataSet2DataFrame(knowageDs);
		it.eng.spagobi.utilities.rest.RestUtilities.Response rEngineResponse = null;
		try {
			String body = RUtils.createREngineRequestBody(rDataframe, dsLabel, script, outputVariable);
			rEngineResponse = RestUtilities.makeRequest(methodPost, rAddress + outputType, headers, body);
		} catch (Exception e) {
			logger.error("error while making request to R engine for userId [" + userId + "] and documentId [" + documentId + "]");
			throw new SpagoBIRuntimeException("error while making request to R engine for userId [" + userId + "] and documentId [" + documentId + "]", e);
		}
		if (rEngineResponse == null || rEngineResponse.getStatusCode() != 200) {
			return Response.status(400).build();
		} else {
			JSONObject toReturn;
			try {
				toReturn = new JSONObject().put("result", RUtils.getFinalResult(rEngineResponse, outputType));
			} catch (Exception e) {
				logger.error("error while creating response json for userId [" + userId + "] and documentId [" + documentId + "]");
				throw new SpagoBIRuntimeException("error while creating response json for userId [" + userId + "] and documentId [" + documentId + "]", e);
			}
			return Response.ok(toReturn.toString()).build();
		}
	}

	@POST
	@Path("/edit/{output_type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { SpagoBIConstants.EDIT_PYTHON_SCRIPTS })
	public Response edit(@PathParam("output_type") String outputType, HashMap<String, String> requestBody) {
		logger.debug("IN");
		UserProfile userProfile = UserProfileManager.getProfile();
		String userId = (String) userProfile.getUserUniqueIdentifier();
		String script = null, documentId = null, outputVariable = null, dsLabel = null, parameters = null, drivers = null, aggregations = null,
				selections = null;
		try {
			dsLabel = requestBody.get("dataset");
			documentId = requestBody.get("document_id");
			outputVariable = requestBody.get("output_variable");
			parameters = requestBody.get("parameters");
			drivers = requestBody.get("drivers");
			aggregations = requestBody.get("aggregations");
			selections = requestBody.get("selections");
			script = requestBody.get("script");
		} catch (Exception e) {
			logger.error("error while retrieving request information for userId [" + userId + "] and documentId [" + documentId + "]");
			throw new SpagoBIRuntimeException("error while retrieving request information for userId [" + userId + "] and documentId [" + documentId + "]", e);
		}
		String knowageDs = getDataStore(dsLabel, parameters, null, selections, null, -1, aggregations, null, -1, -1, false, null, null);
		String rDataframe = RUtils.DataSet2DataFrame(knowageDs);
		it.eng.spagobi.utilities.rest.RestUtilities.Response rEngineResponse = null;
		try {
			String body = RUtils.createREngineRequestBody(rDataframe, dsLabel, script, outputVariable);
			rEngineResponse = RestUtilities.makeRequest(methodPost, rAddress + outputType, headers, body);
		} catch (Exception e) {
			logger.error("error while making request to R engine for userId [" + userId + "] and documentId [" + documentId + "]");
			throw new SpagoBIRuntimeException("error while making request to R engine for userId [" + userId + "] and documentId [" + documentId + "]", e);
		}
		if (rEngineResponse == null || rEngineResponse.getStatusCode() != 200) {
			return Response.status(400).build();
		} else {
			JSONObject toReturn;
			try {
				toReturn = new JSONObject().put("result", RUtils.getFinalResult(rEngineResponse, outputType));
			} catch (Exception e) {
				logger.error("error while creating response json for userId [" + userId + "] and documentId [" + documentId + "]");
				throw new SpagoBIRuntimeException("error while creating response json for userId [" + userId + "] and documentId [" + documentId + "]", e);
			}
			return Response.ok(toReturn.toString()).build();
		}
	}

}
