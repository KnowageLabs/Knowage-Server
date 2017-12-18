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
package it.eng.spagobi.utilities.service;

import java.util.Collection;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.error.EMFAbstractError;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.validation.EMFValidationError;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JSONFailure extends JSONResponse {

	public JSONFailure(Exception exception) throws JSONException {
		super(JSONResponse.FAILURE, createResponseContent(exception) );
	}
	
	public JSONFailure(Exception exception, String callback) throws JSONException {
		super(JSONResponse.FAILURE, createResponseContent(exception), callback );
	}
	
	/*
	public JSONFailure(SpagoBIEngineServiceException exception, String callback) {
		super(JSONResponse.FAILURE, createResponseContent(exception), callback );
	}
	
	public JSONFailure(SpagoBIServiceException exception, String callback) {
		super(JSONResponse.FAILURE, createResponseContent(exception), callback );
	}
	
	public JSONFailure(SpagoBIEngineServiceException exception) {
		super(JSONResponse.FAILURE, createResponseContent(exception) );
	}
	
	public JSONFailure(SpagoBIServiceException exception) {
		super(JSONResponse.FAILURE, createResponseContent(exception) );
	}
	*/
	
	public JSONFailure(Collection<EMFAbstractError> errors) throws JSONException {
		super(JSONResponse.FAILURE, createResponseContent(errors) );
	}

	private static JSONObject createResponseContent(
			Collection<EMFAbstractError> errors) throws JSONException {
		JSONObject content = new JSONObject();
		JSONArray array = new JSONArray();
		Iterator<EMFAbstractError> it = errors.iterator();
		while (it.hasNext()) {
			EMFAbstractError error = it.next();
			JSONObject jsonError = createResponseContent(error);
			array.put(jsonError);
		}
		content.put("errors", array);
		return content;
	}
	
	private static JSONObject createResponseContent(Exception exception) throws JSONException {
		JSONObject content = new JSONObject();
		JSONArray array = new JSONArray();
		JSONObject jsonError = null;
		if (exception instanceof SpagoBIServiceException) {
			jsonError = createResponseContent((SpagoBIServiceException) exception);
		} else if (exception instanceof SpagoBIEngineServiceException) {
			jsonError = createResponseContent((SpagoBIEngineServiceException) exception);
		} else if (exception instanceof EMFAbstractError) {
			jsonError = createResponseContent((EMFAbstractError) exception);
		} else {
			jsonError = new JSONObject();
			content.put("message", exception.getMessage());	
		}
		array.put(jsonError);
		content.put("errors", array);
		return content;
	}
	
	private static JSONObject createResponseContent(EMFAbstractError error) throws JSONException {
		JSONObject jsonObject = new JSONObject();
		if (error instanceof EMFInternalError) {
			EMFInternalError internalError = (EMFInternalError) error;
			Exception e = internalError.getNativeException();
			if (e != null && e instanceof SpagoBIServiceException) {
				SpagoBIServiceException serviceError = (SpagoBIServiceException) e;
				jsonObject = createResponseContent(serviceError);
			} else {
				jsonObject = createResponseContent(internalError);
			}
		} else if (error instanceof EMFUserError) {
			EMFUserError userError = (EMFUserError) error;
			if (userError instanceof EMFValidationError) {
				EMFValidationError validationError = (EMFValidationError) error;
				jsonObject = createResponseContent(validationError);
			} else {
				jsonObject = createResponseContent(userError);
			}
		} else {
			jsonObject = new JSONObject();
			jsonObject.put("severity", error.getSeverity());
			jsonObject.put("message", error.getMessage());
			jsonObject.put("localizedMessage", error.getDescription());
		}
		return jsonObject;
	}

	
	private static JSONObject createResponseContent(EMFUserError error) throws JSONException {
		JSONObject content = new JSONObject();
		content.put("code", error.getErrorCode());
		content.put("severity", error.getSeverity());
		content.put("message", error.getMessage());
		content.put("localizedMessage", error.getDescription());
		return content;
	}
	
	private static JSONObject createResponseContent(EMFValidationError error) throws JSONException {
		JSONObject content = new JSONObject();
		content.put("code", error.getErrorCode());
		content.put("fieldName", error.getFieldName());
		content.put("severity", error.getSeverity());
		content.put("message", error.getMessage());
		content.put("localizedMessage", error.getDescription());
		return content;
	}
	
	private static JSONObject createResponseContent(EMFInternalError error) throws JSONException {
		JSONObject content = new JSONObject();
		content.put("severity", error.getSeverity());
		content.put("message", error.getMessage());
		content.put("localizedMessage", error.getDescription());
		return content;
	}
	
	private static JSONObject createResponseContent(SpagoBIEngineServiceException exception) throws JSONException {
		JSONObject content = new JSONObject();
		content.put("message", exception.getMessage());			
		return content;
	}
	
	private static JSONObject createResponseContent(SpagoBIServiceException exception) throws JSONException {
		JSONObject content = new JSONObject();
		content.put("serviceName", exception.getServiceName());
		content.put("message", exception.getMessage());
		content.put("localizedMessage", exception.getLocalizedMessage());		
		return content;
	}

}
