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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.services.content.service.ContentServiceImplSupplier;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/*
 Answers on: https://localhost:8080/knowage/restful-services/2.0/backendservices/widgets/RWidget
 */
@Path("/2.0/backendservices/widgets/RWidget")
public class RWidgetProxy {

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
		String script, documentId = null, outputVariable;
		try {
			documentId = requestBody.get("document_id");
			outputVariable = requestBody.get("output_variable");
			script = getRCodeFromTemplate(supplier.readTemplate(userId, documentId, null).getContent());
		} catch (Exception e) {
			logger.error("error while retrieving request information for userId [" + userId + "] and documentId [" + documentId + "]");
			throw new SpagoBIRuntimeException("error while retrieving request information for userId [" + userId + "] and documentId [" + documentId + "]", e);
		}
		try {
			String toReturn = new JSONObject().put("result", "<div> ciao </div>").toString();
			return Response.ok(toReturn).build();
		} catch (Exception e) {
			logger.error("error while creating response json for userId [" + userId + "] and documentId [" + documentId + "]");
			throw new SpagoBIRuntimeException("error while creating response json for userId [" + userId + "] and documentId [" + documentId + "]", e);
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
		String script, documentId = null, outputVariable;
		try {
			documentId = requestBody.get("document_id");
			outputVariable = requestBody.get("output_variable");
			script = requestBody.get("script");
		} catch (Exception e) {
			logger.error("error while retrieving request information for userId [" + userId + "] and documentId [" + documentId + "]");
			throw new SpagoBIRuntimeException("error while retrieving request information for userId [" + userId + "] and documentId [" + documentId + "]", e);
		}
		try {
			String toReturn = new JSONObject().put("result", "<div> ciao </div>").toString();
			return Response.ok(toReturn).build();
		} catch (Exception e) {
			logger.error("error while creating response json for userId [" + userId + "] and documentId [" + documentId + "]");
			throw new SpagoBIRuntimeException("error while creating response json for userId [" + userId + "] and documentId [" + documentId + "]", e);
		}
	}

	private String getRCodeFromTemplate(String base64template) {
		JSONObject templateJson;
		try {
			byte[] decodedBytes = Base64.decodeBase64(base64template);
			String template = new String(decodedBytes, "UTF-8");
			templateJson = new JSONObject(new String(decodedBytes, "UTF-8"));
//		    for sheet in template["sheets"]:
//		        for widget in sheet["widgets"]:
//		            if widget["id"] == python_widget.widget_id:
//		                return widget["pythonCode"]
		} catch (Exception e) {
			logger.error("error while retrieving code from template");
			return "";
		}
		return "";
	}

}
