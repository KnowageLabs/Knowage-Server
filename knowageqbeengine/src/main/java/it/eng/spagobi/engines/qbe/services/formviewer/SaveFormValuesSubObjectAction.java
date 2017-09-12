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
package it.eng.spagobi.engines.qbe.services.formviewer;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.qbe.SmartFilterAnalysisState;
import it.eng.spagobi.engines.qbe.services.core.AbstractQbeEngineAction;
import it.eng.spagobi.services.proxy.ContentServiceProxy;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceException;
import it.eng.spagobi.utilities.engines.SpagoBIEngineServiceExceptionHandler;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Class ExecuteQueryAction.
 */
public class SaveFormValuesSubObjectAction extends AbstractQbeEngineAction {

	// Service parameter
	private final String MESSAGE_DET = "MESSAGE_DET";
	private final String SAVE = "SAVE_SUB_OBJECT";

	private final String NAME = "name";
	private final String DESCRIPTION = "description";
	private final String SCOPE = "scope";
	private final String FORMSTATE = "formState";

	/** Logger component. */
	public static transient Logger logger = Logger
			.getLogger(SaveFormValuesSubObjectAction.class);
	public static transient Logger auditlogger = Logger
			.getLogger("audit.query");

	public void service(SourceBean request, SourceBean response) {

		try {

			super.service(request, response);

			String serviceType = getAttributeAsString(MESSAGE_DET);

			if (serviceType.equals(SAVE)) {
				String name = getAttributeAsString(NAME);
				String description = getAttributeAsString(DESCRIPTION);
				String scope = getAttributeAsString(SCOPE);
				JSONObject formState = getAttributeAsJSONObject(FORMSTATE);
				
				String result = saveAnalysisState(formState, scope, name, description);
				if(!result.trim().toLowerCase().startsWith("ok")) {
					throw new SpagoBIEngineServiceException(getActionName(), result);
				}
				
				try {
					String newSubobjectId = result.substring(5);
					JSONSuccess success = new JSONSuccess(newSubobjectId);
					writeBackToClient( success );
				} catch (IOException e) {
					String message = "Impossible to write back the responce to the client";
					throw new SpagoBIEngineServiceException(getActionName(), message, e);
				}
				
			}
		}catch(Throwable t) {
			throw SpagoBIEngineServiceExceptionHandler.getInstance().getWrappedException(getActionName(), getEngineInstance(), t);
		} finally {
			logger.debug("OUT");
		}	

	}

	public String saveAnalysisState(JSONObject form, String scope, String name,
			String description) throws SpagoBIEngineException {
		String documentId = null;
		SmartFilterAnalysisState analysisState = null;
		ContentServiceProxy contentServiceProxy = null;
		String serviceResponse = null;

		QbeEngineInstance engineInstance = getEngineInstance();
		
		analysisState = new SmartFilterAnalysisState(parseValues(form, engineInstance.getFormState().getIdNameMap()));

		if (getEnv() == null) {
			return "KO - Missing environment";
		}

		contentServiceProxy = (ContentServiceProxy) getEnv().get(
				EngineConstants.ENV_CONTENT_SERVICE_PROXY);
		if (contentServiceProxy == null) {
			return "KO - Missing content service proxy";
		}

		documentId = (String) getEnv().get(EngineConstants.ENV_DOCUMENT_ID);
		if (documentId == null) {
			return "KO - Missing document id";
		}

		String isPublic = "false";
		if (PUBLIC_SCOPE.equalsIgnoreCase(scope))
			isPublic = "true";

		serviceResponse = contentServiceProxy.saveSubObject(documentId, name,
				description, isPublic, new String(analysisState.store()));

		return serviceResponse;
	}
	
	
	/**
	 * Get the form state of the subobject and changes the names of the fields with the ids
	 * Take a look at the method it.eng.spagobi.engines.qbe.FormState.setIdNameMap
	 * @param formState the values of the form
	 * @return the values of the form but with the ids of the fields instead of the names
	 */
	public static JSONObject parseValues(JSONObject formState, Map<String, String> idNameMap){
		JSONObject parsedForm = new JSONObject();
		JSONObject newDynamicFilters, newStaticOpenFilters, newStaticClosedFilters, newStaticClosedFiltersAnd, newStaticClosedFiltersXor;
		try{
			JSONObject staticOpenFilters = formState.optJSONObject("staticOpenFilters");
			newStaticOpenFilters = getPropertyNames(staticOpenFilters, idNameMap, "staticOpenFilters");
			if(newStaticOpenFilters!=null){
				parsedForm.put("staticOpenFilters", newStaticOpenFilters);
			}
			JSONObject dynamicFilters = formState.optJSONObject("dynamicFilters");
			newDynamicFilters = getPropertyNames(dynamicFilters, idNameMap,"dynamicFilters");
			if(newDynamicFilters!=null){
				parsedForm.put("dynamicFilters", newDynamicFilters);
			}
			JSONObject staticClosedFilters = formState.optJSONObject("staticClosedFilters");
			if(staticClosedFilters!=null){
				JSONObject staticClosedFiltersAnd = staticClosedFilters.optJSONObject("onOffFilters");
				JSONObject staticClosedFiltersXor = staticClosedFilters.optJSONObject("xorFilters");
				newStaticClosedFiltersAnd = getPropertyNames(staticClosedFiltersAnd, idNameMap, "onOffFilters");
				newStaticClosedFiltersXor = getPropertyNames(staticClosedFiltersXor, idNameMap, "xorFilters");
				
				newStaticClosedFilters= new JSONObject();
				if(newStaticClosedFilters!=null){
					parsedForm.put("staticClosedFilters", newStaticClosedFilters);
				}
				
				if(newStaticClosedFiltersAnd!=null){
					newStaticClosedFilters.put("onOffFilters", newStaticClosedFiltersAnd);
				}
				if(newStaticClosedFiltersXor!=null){
					newStaticClosedFilters.put("xorFilters", newStaticClosedFiltersXor);
				}
			}		

			JSONObject groupingVariables = formState.optJSONObject("groupingVariables");
			parsedForm.put("groupingVariables", groupingVariables);
			
		}catch (Exception e){
			logger.debug("Error getting the map id-->name of the form fields",e);
			return formState;
		}
		return parsedForm;
	}
	

	
	private static JSONObject getPropertyNames(JSONObject filters, Map<String, String> idNameMap, String prefix) throws JSONException{
		if(filters!=null){
			JSONObject newFilters = new JSONObject();
			String key, newKey;
			Iterator<String> keys = filters.keys();
			while(keys.hasNext()){
				key = keys.next();
				newKey = idNameMap.get(key);
				if(newKey!=null){				
					newFilters.put(newKey, filters.get(key));
				}else{
					newFilters.put(key, filters.get(key));
				}
			}
			return newFilters;
		}
		return null;
	}


}
