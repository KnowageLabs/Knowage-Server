/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.rest.interceptors;

import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.net.URI;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.LoggableFailure;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 *         Updates the audit log for the services that throw exceptions
 * 
 */

@Provider
public class RestExceptionMapper implements ExceptionMapper<Throwable> {
	private static final String LOCALIZED_MESSAGE = "localizedMessage";
	private static final String ERROR_MESSAGE = "message";
	private static final String ERROR_SERVICE = "service";
	private static final String ERROR_MESSAGES = "errors";

	static private Logger logger = Logger.getLogger(RestExceptionMapper.class);

	@Context
	private HttpRequest request;
	@Context
	private HttpResponse httpresponse;
	@Context
	private HttpServletRequest servletRequest;
	@Context
	private HttpServletResponse servletResponse;

	public Response toResponse(Throwable t) {

		Response response = null;

		logger.trace("IN");

		if (t instanceof LoggableFailure) {
			// missing authentication --> go to login page
			logger.debug("Missing authentication");
			String url = createUrl(request, servletRequest);
			return Response.status(302).location(URI.create(url)).build();
		} else {
			response = toResponseFromGenericException(t);
		}

		logger.trace("OUT");

		return response;
	}

	private Response toResponseFromGenericException(Throwable t) {
		JSONObject serializedMessages = serializeException(t);
		updateAudit(serializedMessages.toString());
		logger.error("Catched service error: ", t);
		Response response = Response.status(200).entity(serializedMessages.toString()).build();
		return response;
	}

	private JSONObject serializeException(Throwable t) {
		// TODO manage localized messages ASAP
		String localizedMessage = t.getLocalizedMessage();

		String errorMessage = t.getMessage();
		String errorService = "";

		if (t instanceof SpagoBIServiceException) {
			SpagoBIServiceException exception = (SpagoBIServiceException) t;
			errorService = exception.getServiceName();

			String rootCause = exception.getRootException().getMessage();
			if (rootCause == null) {
				rootCause = "An unexpected [" + exception.getRootException().getClass().getName() + "] exception has been trown during service execution";
			}
			errorMessage = rootCause;

			String rootCauseLoaclized = exception.getRootException().getLocalizedMessage();
			if (rootCause != null) {
				localizedMessage = rootCauseLoaclized;
			}

			errorMessage = localizedMessage;
		} else {
			errorMessage = localizedMessage;
		}

		JSONObject error = new JSONObject();
		JSONObject serializedMessages = new JSONObject();
		JSONArray errors = new JSONArray();

		try {
			error.put(ERROR_MESSAGE, errorMessage);
			errors.put(error);
			if (errorService != null) {
				serializedMessages.put(ERROR_SERVICE, errorService);
			}
			serializedMessages.put(ERROR_MESSAGES, errors);
		} catch (JSONException e1) {
			logger.debug("Error serializing the exception ", e1);
		}

		return serializedMessages;
	}

	// private Response toResponseFromGenericException(Throwable e) {
	// String exceptionMessage = "Service Exception";
	// exceptionMessage =
	// ExceptionUtilities.serializeException(e.getMessage(),null);
	// updateAudit(exceptionMessage);
	// logger.error("Application Error", e);
	// Response response =
	// Response.status(500).entity(exceptionMessage).build();
	// return response;
	// }

	private void updateAudit(String exceptionMesage) {
		try {
			UserProfile profile = (UserProfile) servletRequest.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

			String serviceUrl = InterceptorUtilities.getServiceUrl(request);
			String actionCode = "[Service:" + serviceUrl + "]";

			HashMap<String, String> parameters = InterceptorUtilities.getRequestParameters(request, servletRequest);
			parameters.put("Exception", exceptionMesage);

			AuditLogUtilities.updateAudit(servletRequest, profile, actionCode, parameters, "ERR");
		} catch (Exception e2) {
			String serviceUrl = InterceptorUtilities.getServiceUrl(request);
			logger.error("Error in the service " + serviceUrl);
		}
	}

	private String createUrl(HttpRequest request, HttpServletRequest servletRequest) {
		String contextName = ChannelUtilities.getSpagoBIContextName(servletRequest);

		String addr = servletRequest.getServerName();
		Integer port = servletRequest.getServerPort();
		String protocol = servletRequest.getScheme();

		String backUrl = request.getUri().getRequestUri().getPath();

		String community = servletRequest.getParameter("community");
		String owner = servletRequest.getParameter("owner");
		String userToAccept = servletRequest.getParameter("userToAccept");

		String url = protocol + "://" +
				addr + ":" +
				port + "" +
				contextName +
				"/servlet/AdapterHTTP?PAGE=LoginPage&NEW_SESSION=TRUE&" +
				SpagoBIConstants.BACK_URL + "=" + backUrl +
				"&community=" + community +
				"&owner=" + owner +
				"&userToAccept=" + userToAccept +
				"&" + LightNavigationManager.LIGHT_NAVIGATOR_DISABLED + "=TRUE";
		return url;

	}

}
