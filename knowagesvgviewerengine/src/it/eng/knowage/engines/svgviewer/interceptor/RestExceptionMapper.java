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

package it.eng.knowage.engines.svgviewer.interceptor;

import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 *
 * Updates the audit log for the services that throw exceptions
 *
 */

@Provider
public class RestExceptionMapper implements ExceptionMapper<RuntimeException> {

	static private Logger logger = Logger.getLogger(RestExceptionMapper.class);
	private static final String LOCALIZED_MESSAGE = "localizedMessage";
	private static final String ERROR_MESSAGE = "message";
	private static final String ERROR_SERVICE = "errorService";
	private static final String ERROR_MESSAGES = "errors";

	@Override
	public Response toResponse(RuntimeException e) {
		logger.debug("RestExceptionMapper:toResponse IN");
		String localizedMessage = e.getLocalizedMessage();
		String errorMessage = (e.getCause() != null) ? e.getCause().toString() : "";
		String errorService = "";

		// logs the error
		logger.error("Catched error", e);

		if (e instanceof SpagoBIEngineServiceException) {
			SpagoBIEngineServiceException exception = (SpagoBIEngineServiceException) e;
			errorService = exception.getServiceName();
		}

		JSONObject error = new JSONObject();
		JSONObject serializedMessages = new JSONObject();
		JSONArray errors = new JSONArray();

		try {
			error.put(LOCALIZED_MESSAGE, localizedMessage);
			error.put(ERROR_MESSAGE, errorMessage);
			error.put(ERROR_SERVICE, errorService);

			errors.put(error);

			serializedMessages.put(ERROR_MESSAGES, errors);
		} catch (JSONException e1) {
			logger.debug("Error serializing the exception ", e1);
		}

		byte[] bytesResponse = null;

		try {
			bytesResponse = serializedMessages.toString().getBytes("UTF-8");
		} catch (UnsupportedEncodingException e1) {
			logger.error("Error setting the encoding of the response", e1);
			bytesResponse = serializedMessages.toString().getBytes();
		}

		// Response response = Response.status(500).entity(bytesResponse).header(HttpHeaders.CONTENT_ENCODING, "UTF8").build();
		// return response;

		String msgError = "An error occoured loading SVG: [" + e.getLocalizedMessage() + "] " + e.getCause();
		ResponseBuilder response = Response.ok(msgError);
		response.status(500);
		response.header("Content-Type", MediaType.APPLICATION_JSON + "; charset=UTF-8");
		response.header("Content-Disposition", "inline; filename=map.svg");

		logger.debug("RestExceptionMapper:toResponse OUT");
		return response.build();

	}

}
