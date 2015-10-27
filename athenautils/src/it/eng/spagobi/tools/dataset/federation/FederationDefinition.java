/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.tools.dataset.federation;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FederationDefinition {


	private int federation_id;

	private String name;

	private String label;

	private String description;

	private String relationships;

	private Set<IDataSet> sourceDatasets;

	public int getFederation_id() {
		return federation_id;
	}

	public void setFederation_id(int federation_id) {
		this.federation_id = federation_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getRelationships() {
		return relationships;
	}
	
	public JSONObject getRelationshipsAsJSONObject(){
		JSONObject relations = null;
		if(getRelationships()!=null && getRelationships().length()>0){
			try {
				JSONArray array = getFlatReslationsShips();
				relations = new JSONObject();
				relations.put("relationships", array);
			} catch (JSONException e) {
				throw new SpagoBIEngineRuntimeException("Error building the relations object" ,e);
			}
		}
		return relations;
	}

	/**
	 * Flats the relationships and return the single relations between couple tables
	 * @return
	 * @throws JSONException
	 */
	public JSONArray getFlatReslationsShips() throws JSONException{

		JSONArray flatJSONArray = new JSONArray();
		JSONArray array = new JSONArray(getRelationships());
		if(array!=null && array.length()>0){
			for(int i=0; i<array.length();i++){
				JSONArray temp = array.getJSONArray(i);
				for(int j=0; j<temp.length(); j++){
					flatJSONArray.put(temp.get(j));
				}
			}
		}

		return flatJSONArray;
	}

	public void setRelationships(String relationships) {
		this.relationships = relationships;
	}

	public Set<IDataSet> getSourceDatasets() {
		return sourceDatasets;
	}

	public void setSourceDatasets(Set<IDataSet> sourceDatasets) {
		this.sourceDatasets = sourceDatasets;
	}



}
