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

package it.eng.spagobi.tools.dataset.federation;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class FederationDefinition {

	private int federation_id = -1;

	private String name;

	private String label;

	private String description;

	private String relationships;

	private Set<IDataSet> sourceDatasets;

	private boolean degenerated; // true if the federation is degenerated.. When
									// a user creates a derived dataset the
									// system creates a federation that links
									// the original dataste and the derived one

	public int getFederation_id() {
		return federation_id;
	}

	public int setFederation_id(int federation_id) {
		return this.federation_id = federation_id;
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

	@JsonIgnore
	public JSONObject getRelationshipsAsJSONObject() {
		JSONObject relations = null;
		if (getRelationships() != null && getRelationships().length() > 0) {
			try {
				JSONArray array = getFlatReslationsShips();
				relations = new JSONObject();
				relations.put("relationships", array);
			} catch (JSONException e) {
				throw new SpagoBIEngineRuntimeException("Error building the relations object", e);
			}
		}
		return relations;
	}

	/**
	 * Flats the relationships and return the single relations between couple
	 * tables
	 *
	 * @return
	 * @throws JSONException
	 */
	@JsonIgnore
	public JSONArray getFlatReslationsShips() throws JSONException {

		JSONArray flatJSONArray = new JSONArray();
		if (getRelationships() != null) {
			JSONArray array = new JSONArray(getRelationships());
			if (array != null && array.length() > 0) {
				for (int i = 0; i < array.length(); i++) {
					JSONArray temp = array.getJSONArray(i);
					for (int j = 0; j < temp.length(); j++) {
						flatJSONArray.put(temp.get(j));
					}
				}
			}
		}

		return flatJSONArray;
	}

	public void setRelationships(String relationships) {
		this.relationships = relationships;
	}

	@JsonIgnore
	public Set<IDataSet> getSourceDatasets() {
		return sourceDatasets;
	}

	public void setSourceDatasets(Set<IDataSet> sourceDatasets) {
		this.sourceDatasets = sourceDatasets;
	}

	public boolean isDegenerated() {
		return degenerated;
	}

	public void setDegenerated(boolean degenerated) {
		this.degenerated = degenerated;
	}

	/**
	 * Creates a map dataset-->columns involved in at least one relation
	 * 
	 * @return
	 * @throws JSONException
	 */
	@JsonIgnore
	public JSONObject getDataSetRelationKeysMap() throws JSONException {
		Map<String, Set<String>> datasetKeyColumnMap = new HashMap<String, Set<String>>();
		JSONArray ja = getFlatReslationsShips();

		if (ja.length() == 0) {
			Iterator<IDataSet> datasetIter = sourceDatasets.iterator();
			while (datasetIter.hasNext()) {
				datasetKeyColumnMap.put(datasetIter.next().getLabel(), new HashSet<String>());
			}

		} else {
			for (int i = 0; i < ja.length(); i++) {
				JSONObject jo = ja.getJSONObject(i);
				String sourceTable = jo.getJSONObject("sourceTable").getString("name");
				String destTable = jo.getJSONObject("destinationTable").getString("name");
				JSONArray sourceColumns = jo.getJSONArray("sourceColumns");
				JSONArray destColumns = jo.getJSONArray("destinationColumns");

				for (int j = 0; j < sourceColumns.length(); j++) {
					Set<String> aDatasetKeyColumnSet = datasetKeyColumnMap.get(sourceTable);
					if (aDatasetKeyColumnSet == null) {
						aDatasetKeyColumnSet = new HashSet<String>();
						datasetKeyColumnMap.put(sourceTable, aDatasetKeyColumnSet);
					}
					aDatasetKeyColumnSet.add(sourceColumns.getString(j));
				}

				for (int j = 0; j < destColumns.length(); j++) {
					Set<String> aDatasetKeyColumnSet = datasetKeyColumnMap.get(destTable);
					if (aDatasetKeyColumnSet == null) {
						aDatasetKeyColumnSet = new HashSet<String>();
						datasetKeyColumnMap.put(destTable, aDatasetKeyColumnSet);
					}
					aDatasetKeyColumnSet.add(destColumns.getString(j));
				}

			}
		}

		return new JSONObject(datasetKeyColumnMap);

	}

}
