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
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
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

@Path("/1.0/documentexecution")
@ManageAuthorization
public class DocumentExecutionResource extends AbstractSpagoBIResource {

	// public static final String PARAMETERS = "PARAMETERS";
	// public static final String SERVICE_NAME = "GET_URL_FOR_EXECUTION_ACTION";
	public static String MODE_SIMPLE = "simple";
	// public static String MODE_COMPLETE = "complete";
	// public static String START = "start";
	// public static String LIMIT = "limit";

	private static IMessageBuilder message = MessageBuilderFactory.getMessageBuilder();

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
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();
		List errorList = new ArrayList<>();
		MessageBuilder m = new MessageBuilder();
		Locale locale = m.getLocale(req);
		try {
			String executingRole = getExecutionRole(role);
			// displayToolbar
			// modality
			BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectForExecutionByLabelAndRole(label, executingRole);
			List<DocumentParameters> parameters = DocumentExecutionUtils.getParameters(obj, executingRole, locale, modality);
			String url = DocumentExecutionUtils.handleNormalExecutionUrl(this.getUserProfile(), obj, req, this.getAttributeAsString("SBI_ENVIRONMENT"),
					executingRole, modality, jsonParameters, locale);
			errorList = DocumentExecutionUtils.handleNormalExecutionError(this.getUserProfile(), obj, req, this.getAttributeAsString("SBI_ENVIRONMENT"),
					executingRole, modality, jsonParameters, locale);
			resultAsMap.put("parameters", parameters);
			resultAsMap.put("url", url);
			resultAsMap.put("errors", errorList);

		} catch (DocumentExecutionException e) {
			errorList.add(e);
			resultAsMap.put("documentError", errorList);
			resultAsMap.put("url", "");
			resultAsMap.put("parameters", "");
			return Response.ok(resultAsMap).build();
		} catch (Exception e) {
			logger.error("Error while getting the document execution url", e);
			throw new SpagoBIRuntimeException("Error while getting the document execution url", e);
		}

		logger.debug("OUT");
		return Response.ok(resultAsMap).build();
		// return Response.ok(toBeReturned).build();
	}

	/**
	 * @return { filterStatus: [{ title: 'Provincia', urlName: 'provincia', type: 'list', lista:[[k,v],[k,v], [k,v]] }, { title: 'Comune', urlName: 'comune',
	 *         type: 'list', lista:[], dependsOn: 'provincia' }, { title: 'Free Search', type: 'manual', urlName: 'freesearch' }],
	 *
	 *         errors: [ 'role missing', 'operation not allowed' ] }
	 * @throws EMFUserError
	 */
	@GET
	@Path("/filters")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionFilters(@QueryParam("label") String label, @QueryParam("role") String role,
			@QueryParam("parameters") String jsonParameters, @Context HttpServletRequest req) throws DocumentExecutionException, EMFUserError {

		logger.debug("IN");

		String toBeReturned = "{}";
		HashMap<String, Object> resultAsMap = new HashMap<String, Object>();

		IBIObjectDAO dao = DAOFactory.getBIObjectDAO();
		BIObject biObject = dao.loadBIObjectForExecutionByLabelAndRole(label, role);

		List<BIObjectParameter> parametersList = biObject.getBiObjectParameters();

		ArrayList<HashMap<String, Object>> parametersArrayList = new ArrayList<>();

		for (BIObjectParameter objParameter : parametersList) {
			Parameter parameter = objParameter.getParameter();

			HashMap<String, Object> parameterAsMap = new HashMap<String, Object>();

			parameterAsMap.put("id", objParameter.getId());
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

			if (parameter.getValueSelection().equalsIgnoreCase("lov")) {
				ArrayList<HashMap<String, Object>> defaultValues = DocumentExecutionUtils.getLovDefaultValues(role, biObject, objParameter, req);

				parameterAsMap.put("defaultValues", defaultValues);
			}

			parametersArrayList.add(parameterAsMap);
		}

		if (parametersList.size() > 0) {
			resultAsMap.put("filterStatus", parametersArrayList);
		} else {
			resultAsMap.put("filterStatus", new ArrayList<>());
		}
		resultAsMap.put("errors", new ArrayList<>());
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
				throw new DocumentExecutionException(message.getMessage("SBIDev.docConf.execBIObject.selRoles.Title"));
			}
		}

		return role;
	}
}
