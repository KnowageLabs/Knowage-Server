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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.analiticalmodel.document.AnalyticalModelDocumentManagementAPI;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/1.0/documentexecution")
public class DocumentExecutionResource extends AbstractSpagoBIResource {

	private class DocumentExecutionException extends Exception {
		private static final long serialVersionUID = -1882998632783944575L;

		DocumentExecutionException(String message) {
			super(message);
		}
	}

	static protected Logger logger = Logger.getLogger(DocumentExecutionResource.class);

	protected AnalyticalModelDocumentManagementAPI documentManager = new AnalyticalModelDocumentManagementAPI(
			getUserProfile());

	/**
	 * @return { executionURL: 'http:...', errors: 1 - 'role missing' 2
	 *         -'Missing paramters' [list of missing mandatory filters ] 3
	 *         -'operation not allowed' [if the request role is not owned by the
	 *         requesting user] }
	 * @throws EMFInternalError
	 */
	@GET
	@Path("/url")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionURL(@QueryParam("label") String label, @QueryParam("role") String role,
			@QueryParam("modality") String modality, @QueryParam("displayToolbar") String displayToolbar,
			@QueryParam("parameters") String jsonParameters, @Context HttpServletRequest req) {
		logger.debug("IN");

		String toBeReturned = "{}";

		try {
			role = getExecutionRole(role);

		} catch (DocumentExecutionException e) {
			return Response.ok("{errors: '" + e.getMessage() + "', }").build();
		} catch (Exception e) {
			logger.error("Error while getting the document execution url", e);
			throw new SpagoBIRuntimeException("Error while getting the document execution url", e);
		}

		logger.debug("OUT");
		return Response.ok(toBeReturned).build();
	}

	/**
	 * @return { filterStatus: [ { title: 'Provincia', urlName: 'provincia',
	 *         type: 'list', lista:[[k,v],[k,v], [k,v]] }, { title: 'Comune',
	 *         urlName: 'comune', type: 'list' lista:[], dependsOn: 'provincia'
	 *         }, { title: 'Free Search', urlName: 'freesearch' } ], errors: 1 -
	 *         'role missing' 2 - 'operation not allowed' (ruolo non associato
	 *         al profilo utente) }
	 */
	@SuppressWarnings("unchecked")
	@GET
	@Path("/filters")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionFilters(@QueryParam("label") String label, @QueryParam("role") String role,
			@QueryParam("parameters") String jsonParameters, @Context HttpServletRequest req) {
		logger.debug("IN");

		String toBeReturned = "{}";

		try {
			role = getExecutionRole(role);

			// recuperiamo il documento by label
			BIObject document = documentManager.getDocument(label);

			// recuperiamo i driver analitici
			List<JSONObject> documentParameters = (List<JSONObject>) documentManager.getDocumentParameters(label);
			for (JSONObject parameter : documentParameters) {
				Parameter analyticalDriver = documentManager.getAnalyticalDriver(parameter.get("label"));
				// analyticalDriver.get
			}

		} catch (DocumentExecutionException e) {
			return Response.ok("{errors: '" + e.getMessage() + "', }").build();
		} catch (Exception e) {
			logger.error("Error while getting the document execution filters", e);
			throw new SpagoBIRuntimeException("Error while getting the document execution filters", e);
		}

		logger.debug("OUT");
		return Response.ok(toBeReturned).build();
	}

	/**
	 * @return the list of values when input parameter (urlName) is correlated
	 *         to another
	 */
	@GET
	@Path("/filterlist")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getDocumentExecutionFilterList(@QueryParam("label") String label, @QueryParam("role") String role,
			@QueryParam("parameters") String jsonParameters, @QueryParam("urlName") String urlName,
			@Context HttpServletRequest req) {
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
		if ((role == null || "".equals(role)) && userProfile.getRoles().size() == 1) {
			role = userProfile.getRoles().iterator().next().toString();
		} else {
			throw new DocumentExecutionException("role.missing");
		}
		return role;
	}

}
