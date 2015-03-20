/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe;

import it.eng.spagobi.engines.qbe.analysisstateloaders.formbuilder.FormStateLoaderFactory;
import it.eng.spagobi.engines.qbe.analysisstateloaders.formbuilder.IFormStateLoader;
import it.eng.spagobi.engines.qbe.template.QbeJSONTemplateParser;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.engines.EngineAnalysisState;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Zerbetto Davide (davide.zerbetto@eng.it)
 *
 */
public class FormState extends EngineAnalysisState {
	
	public static final String CURRENT_VERSION = "1";
	
	public static final String ID_NAME_MAP = "ID_NAME_MAP";
	public static final String NAME_ID_MAP = "NAME_ID_MAP";
	
	public static final String FORM_STATE = "FORM_STATE";
	public static final String FORM_STATE_VALUES = "FORM_STATE_VALUES";
	
	/** Logger component. */
    private static transient Logger logger = Logger.getLogger(FormState.class);

	public void load(byte[] rowData) throws SpagoBIEngineException {
		String str = null;
		JSONObject formStateJSON = null;
		JSONObject rowDataJSON = null;
		String encodingFormatVersion;
		
		logger.debug("IN");

		try {
			str = new String( rowData );
			logger.debug("loading form state from row data [" + str + "] ...");
			
			rowDataJSON = new JSONObject(str);
			try {
				encodingFormatVersion = rowDataJSON.getString("version");
			} catch (JSONException e) {
				logger.debug("no version found, default is 0");
				encodingFormatVersion = "0";
			}
			
			logger.debug("Row data encoding version  [" + encodingFormatVersion + "]");
			
			if (encodingFormatVersion.equalsIgnoreCase(CURRENT_VERSION)) {				
				formStateJSON = rowDataJSON;
			} else {
				logger.warn("Row data encoding version [" + encodingFormatVersion + "] does not match with the current version used by the engine [" + CURRENT_VERSION + "] ");
				logger.debug("Converting from encoding version [" + encodingFormatVersion + "] to encoding version [" + CURRENT_VERSION + "]....");
				IFormStateLoader formViewerStateLoader;
				formViewerStateLoader = FormStateLoaderFactory.getInstance().getLoader(encodingFormatVersion);
				if (formViewerStateLoader == null) {
					throw new SpagoBIEngineException("Unable to load data stored in format [" + encodingFormatVersion + "] ");
				}
				formStateJSON = (JSONObject) formViewerStateLoader.load(str);
				logger.debug("Encoding conversion has been executed succesfully");
			}
			
			logger.debug("analysis state loaded succsfully from row data");
			
			// adding other info that are created dynamically
			QbeJSONTemplateParser.addAdditionalInfo(formStateJSON);
			setProperty( FORM_STATE,  formStateJSON);
			
		} catch (JSONException e) {
			throw new SpagoBIEngineException("Impossible to load form state from raw data", e);
		} finally {
			logger.debug("OUT");
		}
	}

	public byte[] store() throws SpagoBIEngineException {
		JSONObject formStateJSON = null;
		String rowData = null;	
				
		try {
			formStateJSON = (JSONObject) getProperty( FORM_STATE );
			formStateJSON.put("version", CURRENT_VERSION);
			rowData = formStateJSON.toString();
		} catch (Throwable e) {
			throw new SpagoBIEngineException("Impossible to store form state", e);
		}
		
		return rowData.getBytes();
	}
	
	public JSONObject getConf() {
		return (JSONObject) getProperty( FORM_STATE );
	}
	
	public void setConf(JSONObject json) {
		Assert.assertNotNull(json, "JSON form state cannot be null");
		QbeJSONTemplateParser.addAdditionalInfo(json);
		QbeJSONTemplateParser.cleanGroupingVariables(json);
		setProperty(FORM_STATE, json);
	}
	
	public JSONObject getFormStateValues() {
		return parseValues((JSONObject) getProperty( FORM_STATE_VALUES ));
	}
	
	public void setFormStateValues(JSONObject json) {
		setProperty(FORM_STATE_VALUES, json);
	}
	
	public Map<String, String> getIdNameMap() {
		return (Map<String, String>) getProperty(ID_NAME_MAP);
	}
	
	public Map<String, String> getNameIdMap() {
		return (Map<String, String>) getProperty(NAME_ID_MAP);
	}
	
	
	/**
	 * Used for save the form values:
	 * For keep the values of the form if the form state changes (id est: if the fields of the form change )
	 * we need to find the relation between the new fields and the ones saved in the sub object.
	 * This relation is the name of the field.
	 * This method maps the id of the field and the name.. In this way we save in the subobject
	 * the name of the field 
	 */
	public void setIdNameMap(){
		Map<String, String> idNameMap = new HashMap<String, String>();
		Map<String, String> nameIdMap = new HashMap<String, String>();
		try{
				JSONObject formState = (JSONObject) getProperty(FORM_STATE);
				if(formState!=null && formState.length()!=0){
					JSONArray staticOpenFilters = formState.optJSONArray("staticOpenFilters");
					fillMaps(staticOpenFilters, idNameMap, nameIdMap, "id", "text","staticOpenFilters");
					JSONArray dynamicFilters = formState.optJSONArray("dynamicFilters");
					fillMaps(dynamicFilters, idNameMap, nameIdMap, "id", "title","dynamicFilters");
					JSONArray staticClosedFilters = formState.optJSONArray("staticClosedFilters");
					fillMaps(staticClosedFilters, idNameMap, nameIdMap, "id", "title","staticClosedFilters");
				}
		}catch (Exception e){
			logger.error("Error getting the map id-->name of the form fields",e);
		}
		setProperty(ID_NAME_MAP, idNameMap);
		setProperty(NAME_ID_MAP, nameIdMap);
	}
	
	private void fillMaps(JSONArray filters, Map<String, String> idNameMap, Map<String, String> nameIdMap, String idRap, String nameRap, String prefix) throws JSONException{
		String id, name;
		for(int i=0; i<filters.length(); i++){
			JSONObject jo = filters.getJSONObject(i);
			id = jo.optString(idRap);
			name = prefix+jo.optString(nameRap);
			idNameMap.put(id, name);
			nameIdMap.put(name, id);
		}
	}

	/**
	 * Get the form state of the subobject and changes the names of the fields
	 * of the form with the ids
	 * @param formState the values of the form
	 * @return the values of the form but with the name of the fields instead of the ids
	 */
	private JSONObject parseValues(JSONObject formState){

		if(formState!=null){
		
			JSONObject formStructure = getConf();
			JSONObject parsedForm = new JSONObject();
			Map<String, String> nameIdMap = getNameIdMap();
			if(nameIdMap==null){
				logger.debug("The nameIdMap is null.. THE STORE STATE VALUES WILL NOT BE PARSED.. It's ok if the engine is worksheet");
				return formState;
			}
			logger.debug("formStateValues: "+formState);
			logger.debug("formState: "+getProperty( FORM_STATE ));
			logger.debug("nameIdMap: "+nameIdMap);
			JSONObject newDynamicFilters, newStaticOpenFilters, newStaticClosedFilters, newStaticClosedFiltersAnd, newStaticClosedFiltersXor;
			try{
				JSONObject staticOpenFilters = formState.optJSONObject("staticOpenFilters");
				newStaticOpenFilters = getPropertyNames(staticOpenFilters, nameIdMap, "staticOpenFilters");
				if(newStaticOpenFilters!=null){
					parsedForm.put("staticOpenFilters", newStaticOpenFilters);
				}
				JSONObject dynamicFilters = formState.optJSONObject("dynamicFilters");
				filterAdmissibleValues(dynamicFilters, formStructure.getJSONArray("dynamicFilters"), "dynamicFilters");
				newDynamicFilters = getPropertyNames(dynamicFilters, nameIdMap,"dynamicFilters");
				
				if(newDynamicFilters!=null){
					parsedForm.put("dynamicFilters", newDynamicFilters);
				}
				JSONObject staticClosedFilters = formState.optJSONObject("staticClosedFilters");
				if(staticClosedFilters!=null){
					JSONObject staticClosedFiltersAnd = staticClosedFilters.optJSONObject("onOffFilters");
					JSONObject staticClosedFiltersXor = staticClosedFilters.optJSONObject("xorFilters");
					newStaticClosedFiltersAnd = getPropertyNames(staticClosedFiltersAnd, nameIdMap, "onOffFilters");
					newStaticClosedFiltersXor = getPropertyNames(staticClosedFiltersXor, nameIdMap, "xorFilters");
					
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
				filterAdmissibleGroups(groupingVariables, formStructure.getJSONArray("groupingVariables"));
				
				parsedForm.put("groupingVariables", groupingVariables);
				
			}catch (Exception e){
				logger.error("Error getting the map id-->name of the form fields",e);
				return formState;
			}
			return parsedForm;
		}
		return formState;
	}
	

	
	private JSONObject getPropertyNames(JSONObject filters, Map<String, String> nameIdMap, String prefix) throws JSONException{
		if(filters!=null){
			JSONObject newFilters = new JSONObject();
			String key, newKey;
			Iterator<String> keys = filters.keys();
			while(keys.hasNext()){
				key = keys.next();
				newKey = nameIdMap.get(key);
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
	
	/**
	 * Filter the values of the subobject. It removes the selected field value if the form is changed and the values
	 * are not consistent with the new configuration.
	 * @param valuesJSON the selected values of the subobject
	 * @param admissibleJSON the admissible values
	 * @param prefix
	 * @throws JSONException
	 */
	private void filterAdmissibleValues(JSONObject valuesJSON, JSONArray admissibleJSON, String prefix) throws JSONException{
		List<String> deprecatedValues = new ArrayList<String>();

		for(int y=0; y<admissibleJSON.length(); y++){
			Iterator<String> keyIter = valuesJSON.keys();
			while(keyIter.hasNext()){
				String key = keyIter.next();
				if(key.equals(prefix+admissibleJSON.getJSONObject(y).getString("title"))){
					if(!filterAdmissibleValues(valuesJSON.getJSONObject(key).getString("field"), admissibleJSON.getJSONObject(y).getJSONArray("admissibleFields"))){
						deprecatedValues.add(key);
						logger.debug("field [" + key + "] does not exist anymore in the form");
					}
				}
			}
		}
		
		for(int i = 0; i < deprecatedValues.size(); i++) {
			valuesJSON.remove(deprecatedValues.get(i));
		}
	}

	/**
	 * Filter the group fields of the subobject. It removes the selected field value if the form is changed and the values
	 * are not consistent with the new configuration.
	 * @param valuesJSON the selected values of the subobject
	 * @param admissibleJSON the admissible values
	 * @throws JSONException
	 */
	private void filterAdmissibleGroups(JSONObject valuesJSON, JSONArray admissibleJSON) throws JSONException{
		List<String> deprecatedValues = new ArrayList<String>();
		
		for(int y=0; y<admissibleJSON.length(); y++){
			Iterator<String> keyIter = valuesJSON.keys();
			while(keyIter.hasNext()){
				String key = keyIter.next();
				if(key.equals(admissibleJSON.getJSONObject(y).getString("id"))){
					if(!filterAdmissibleValues(valuesJSON.getString(key), admissibleJSON.getJSONObject(y).getJSONArray("admissibleFields"))){
						deprecatedValues.add(key);
						logger.debug("field [" + key + "] does not exist anymore in the form");
					}
				}
			}
		}
		for(int i = 0; i < deprecatedValues.size(); i++) {
			valuesJSON.remove(deprecatedValues.get(i));
		}
	}
	
	
	private boolean filterAdmissibleValues(String JSNONValue, JSONArray admissibleJSON) throws JSONException{
		for(int y=0; y<admissibleJSON.length(); y++){
			if(admissibleJSON.getJSONObject(y).getString("field").equals(JSNONValue)){
				return true;
			}
		}
		return false;
	}
	
	
}
