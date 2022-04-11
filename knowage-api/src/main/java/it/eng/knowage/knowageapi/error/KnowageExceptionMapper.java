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

package it.eng.knowage.knowageapi.error;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import it.eng.knowage.boot.error.KnowageServiceException;

/**
 *
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 *         Updates the audit log for the services that throw exceptions
 *
 */
@Component
@Provider
public class KnowageExceptionMapper implements ExceptionMapper<RuntimeException> {
	private static final String LOCALIZED_MESSAGE = "localizedMessage";
	private static final String ERROR_MESSAGE = "message";
	private static final String ERROR_SERVICE = "service";
	private static final String ERROR_MESSAGES = "errors";

	static private Logger logger = Logger.getLogger(KnowageExceptionMapper.class);

	@Context
	private HttpServletRequest servletRequest;
	@Context
	private HttpServletResponse servletResponse;

	@Override
	public Response toResponse(RuntimeException t) {
		logger.error("Catched service error: ", t);
		return toResponseFromGenericException(t);
	}

	private Response toResponseFromGenericException(RuntimeException t) {
		JSONObject serializedMessages = serializeException(t);
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(serializedMessages.toString()).build();
	}

	private JSONObject serializeException(RuntimeException t) {
		// TODO manage localized messages ASAP
		String localizedMessage = t.getLocalizedMessage();

		String errorMessage = t.getMessage();
		String errorService = "";

		if (t instanceof KnowageServiceException) {
			KnowageServiceException exception = (KnowageServiceException) t;
			errorService = exception.getServiceName();

			String rootCause = exception.getRootException().getMessage();
			if (rootCause == null) {
				rootCause = "An unexpected [" + exception.getRootException().getClass().getName() + "] exception has been trown during service execution";
			}

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

}
