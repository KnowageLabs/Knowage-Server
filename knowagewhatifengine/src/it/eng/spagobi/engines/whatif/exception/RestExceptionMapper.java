/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */

package it.eng.spagobi.engines.whatif.exception;

import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;

import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 * 
 */
@Provider
public class RestExceptionMapper extends AbstractWhatIfEngineService implements ExceptionMapper<Throwable>
{

	static private Logger logger = Logger.getLogger(RestExceptionMapper.class);
	private static final String LOCALIZED_MESSAGE = "localizedMessage";
	private static final String ERROR_MESSAGE = "message";
	private static final String ERROR_SERVICE = "errorService";
	private static final String ERROR_MESSAGES = "errors";

	public Response toResponse(Throwable e) {
		logger.debug("RestExceptionMapper:toResponse IN");
		String localizedMessage = e.getLocalizedMessage();
		String errorMessage = e.getMessage();
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

		Response response = Response.status(200)
				.entity(bytesResponse)
				.header(HttpHeaders.CONTENT_ENCODING, "UTF8").build();

		logger.debug("RestExceptionMapper:toResponse OUT");

		return response;
	}

}