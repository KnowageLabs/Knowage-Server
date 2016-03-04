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
package it.eng.spagobi.api;

import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.AnalyticalModelDocumentManagementAPI;
import it.eng.spagobi.analiticalmodel.document.DocumentExecutionUtils;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentParameters;
import it.eng.spagobi.analiticalmodel.document.handlers.DocumentUrlManager;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/1.0/documentexecution")
@ManageAuthorization
public class DocumentExecutionResource extends AbstractSpagoBIResource {

//	public static final String PARAMETERS = "PARAMETERS";
//	public static final String SERVICE_NAME = "GET_URL_FOR_EXECUTION_ACTION";

	private class DocumentExecutionException extends Exception {
		private static final long serialVersionUID = -1882998632783944575L;

		DocumentExecutionException(String message) {
			super(message);
		}
	}

	static protected Logger logger = Logger.getLogger(DocumentExecutionResource.class);
	protected AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(getUserProfile());

	/**
	 * @return { executionURL: 'http:...', errors: 1 - 'role missing' 2 -'Missing paramters' [list of missing mandatory filters ] 3 -'operation not allowed' [if
	 *         the request role is not owned by the requesting user] }
	 * @throws EMFInternalError
	 */
	@GET
	@Path("/url")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionURL(@QueryParam("label") String label, @QueryParam("role") String role, @QueryParam("modality") String modality,
			@QueryParam("displayToolbar") String displayToolbar, @QueryParam("parameters") String jsonParameters, @Context HttpServletRequest req) {

		logger.debug("IN");
		MessageBuilder m = new MessageBuilder();
		Locale locale = m.getLocale(req);
		String toBeReturned = "{}";
		JSONObject response = new JSONObject();
		try {
			String executingRole = getExecutionRole(role);
			// displayToolbar
			// modality
			// BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectByLabel(label);
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByLabelAndRole(label, executingRole);
			List<DocumentParameters> parameters = DocumentExecutionUtils.getParameters(obj, executingRole, locale, modality);
			JSONArray parametersJSON = null;
			try {
				parametersJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(parameters, locale);
			} catch (SerializationException e) {
				e.printStackTrace();
			}
			// URL
//			response = handleNormalExecution(obj, req, executingRole, modality, jsonParameters, locale);
			response = DocumentExecutionUtils.handleNormalExecution(this.getUserProfile(), obj, req, this.getAttributeAsString("SBI_ENVIRONMENT"), executingRole, 
					modality, jsonParameters, locale);
			// PARAMETERS
			response.put("parameters", parametersJSON);
			toBeReturned = response.toString();
		} catch (DocumentExecutionException e) {
			return Response.ok("{\"errors\": [\"" + e.getMessage() + "\"], \"url\":\"\", \"parameters\":\"\"}").build();
		} catch (Exception e) {
			logger.error("Error while getting the document execution url", e);
			throw new SpagoBIRuntimeException("Error while getting the document execution url", e);
		}

		logger.debug("OUT");
		return Response.ok(toBeReturned).build();
	}

	/**
	 * @return { 
	 * 		filterStatus: [{ 
	 * 			title: 'Provincia', 
	 * 			urlName: 'provincia', 
	 * 			type: 'list', 
	 * 			lista:[[k,v],[k,v], [k,v]]
	 * 		}, { 
	 * 			title: 'Comune', 
	 * 			urlName: 'comune',
	 *         	type: 'list', 
	 *         	lista:[], 
	 *         	dependsOn: 'provincia' 
	 *      }, { 
	 *      	title: 'Free Search',
	 *      	type: 'manual', 
	 *      	urlName: 'freesearch' 
	 *     	}], 
	 *     	
	 *     	errors: [
	 *     		'role missing',
	 *     		'operation not allowed'
	 *   	]
	 *   }
	 */
	@SuppressWarnings("unchecked")
	@GET
	@Path("/filters")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionFilters(@QueryParam("label") String label, @QueryParam("role") String role,
			@QueryParam("parameters") String jsonParameters, @Context HttpServletRequest req) {
		logger.debug("IN");

		String toBeReturned = "{}";
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();

		try {
			IBIObjectDAO dao = DAOFactory.getBIObjectDAO();
			BIObject biObject = dao.loadBIObjectForExecutionByLabelAndRole(label, role);

			List<BIObjectParameter> parametersList = biObject.getBiObjectParameters();

			ArrayList<HashMap<String, Object>> parametersArrayList = new ArrayList<>();

			for (BIObjectParameter objParameter : parametersList) {
				Parameter parameter = objParameter.getParameter();

				HashMap<String, Object> parameterAsMap = new HashMap<String, Object>();

				parameterAsMap.put("id", objParameter.getLabel());
				parameterAsMap.put("label", objParameter.getLabel());
				parameterAsMap.put("urlName", objParameter.getParameterUrlName());
				parameterAsMap.put("type", parameter.getType());
				parameterAsMap.put("typeCode", parameter.getModalityValue().getITypeCd());
				parameterAsMap.put("selectionType", parameter.getModalityValue().getSelectionType());
				parameterAsMap.put("valueSelection", parameter.getValueSelection());
				parameterAsMap.put("selectedLayer", parameter.getSelectedLayer());
				parameterAsMap.put("selectedLayerProp", parameter.getSelectedLayerProp());
				parameterAsMap.put("visible", ((objParameter.getVisible() == 1)));
				parameterAsMap.put("mandatory", ((objParameter.getRequired() == 1)));
				parameterAsMap.put("multivalue", objParameter.isMultivalue());
				parameterAsMap.put("dependsOn", new ArrayList<>());
				
				if(parameter.getValueSelection().equalsIgnoreCase("lov")) {
					ILovDetail lovProvDet = DocumentExecutionUtils.getLovDetail(objParameter);
				}

				parametersArrayList.add(parameterAsMap);
			}

			if (parametersList.size() > 0) {
				resultAsMap.put("filterStatus", parametersArrayList);
			} else {
				resultAsMap.put("filterStatus", new ArrayList<>());
			}
			resultAsMap.put("errors", new ArrayList<>());

			// } catch (DocumentExecutionException e) {
			// return Response.ok("{errors: '" + e.getMessage() + "' }").build();
		} catch (Exception e) {
			logger.error("Error while getting the document execution filters", e);
			throw new SpagoBIRuntimeException("Error while getting the document execution filters", e);
		}
		logger.debug("OUT");
		return Response.ok(resultAsMap).build();
	}

	/**
	 * @return the list of values when input parameter (urlName) is correlated to another
	 */
	@GET
	@Path("/filterlist")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionFilterList(@QueryParam("label") String label, @QueryParam("role") String role,
			@QueryParam("parameters") String jsonParameters, @QueryParam("urlName") String urlName, @Context HttpServletRequest req) {
		logger.debug("IN");

		String toBeReturned = "{}";

		try {
			role = getExecutionRole(role);

		} catch (DocumentExecutionException e) {
			return Response.ok("{errors: '" + e.getMessage() + "', }").build();
		} catch (Exception e) {
			logger.error("Error while getting the document execution filterlist", e);
			throw new SpagoBIRuntimeException("Error while getting the document execution filterlist", e);
		}

		logger.debug("OUT");
		return Response.ok(toBeReturned).build();
	}

	protected String getExecutionRole(String role) throws EMFInternalError, DocumentExecutionException {
		UserProfile userProfile = getUserProfile();
		if (role != null && !role.equals("")) {
			logger.debug("role for document execution: " + role);
		} else {
			if (userProfile.getRoles().size() == 1) {
				role = userProfile.getRoles().iterator().next().toString();
				logger.debug("profile role for document execution: " + role);
			} else {
				logger.debug("missing role for document execution, role:" + role);
				throw new DocumentExecutionException("role.missing");
			}
		}
		
		return role;
	}
}
