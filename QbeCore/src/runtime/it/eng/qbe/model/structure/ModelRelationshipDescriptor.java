/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure;





import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ModelRelationshipDescriptor implements IModelRelationshipDescriptor {
	JSONObject relationshipJSON;
	private static transient Logger logger = Logger.getLogger(ModelRelationshipDescriptor.class);
	
	public ModelRelationshipDescriptor(JSONObject relationshipJSON) {
		this.relationshipJSON = relationshipJSON;
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelRelationshipDescriptor#getType()
	 */
	public String getType() {
		try {
			return relationshipJSON.getString("cardinality");
		} catch (JSONException t) {
			throw new RuntimeException("Impossible to read property [cardinality] from relationship json object", t);
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelRelationshipDescriptor#getSourceEntityName()
	 */
	public String getSourceEntityUniqueName() {
		try {
			JSONObject sourceTable = relationshipJSON.getJSONObject("sourceTable");
			String tableName = sourceTable.optString("className");
			String tableType = sourceTable.optString("package");
			return tableType + "." + tableName + "::" + tableName;
		} catch (JSONException t) {
			throw new RuntimeException("Impossible to read source table's name from relationship json object", t);
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelRelationshipDescriptor#getDestinationEntityName()
	 */
	public String getDestinationEntityUniqueName() {
		try {
			JSONObject destinationTable = relationshipJSON.getJSONObject("destinationTable");
			String tableName = destinationTable.optString("className");
			String tableType = destinationTable.optString("package");
			return tableType + "." + tableName + "::" + tableName;
		} catch (JSONException t) {
			throw new RuntimeException("Impossible to read destination table's name from relationship json object", t);
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelRelationshipDescriptor#getSourceFieldNames()
	 */
	public List<String> getSourceFieldUniqueNames() {
		try {
			List<String> sourceColumnNames = new ArrayList<String>();
			
			JSONObject sourceTable = relationshipJSON.getJSONObject("sourceTable");
			String tableName = sourceTable.optString("className");
			String tableType = sourceTable.optString("package");
			
			JSONArray sourceColumn = relationshipJSON.getJSONArray("sourceColumns");
			for(int i = 0; i < sourceColumn.length(); i++) {
				sourceColumnNames.add( tableType + "." +  tableName + ":" + sourceColumn.getString(i) );
			}
			return sourceColumnNames;
		} catch (JSONException t) {
			throw new RuntimeException("Impossible to read source column names from relationship json object", t);
		}
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelRelationshipDescriptor#getDestinationFieldNames()
	 */
	public List<String> getDestinationFieldUniqueNames() {
		try {
			List<String> destinationColumnNames = new ArrayList<String>();
			
			JSONObject destinationTable = relationshipJSON.getJSONObject("destinationTable");
			String tableName = destinationTable.optString("className");
			String tableType = destinationTable.optString("package");
			
			JSONArray destinationColumn = relationshipJSON.getJSONArray("destinationColumns");
			for(int i = 0; i < destinationColumn.length(); i++) {
				destinationColumnNames.add( tableType + "." +  tableName + ":" + destinationColumn.getString(i) );
			}
			return destinationColumnNames;
		} catch (JSONException t) {
			throw new RuntimeException("Impossible to read destination column names from relationship json object", t);
		}
	}

	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelRelationshipDescriptor#getName()
	 */
	public String getName() {
		try {
			return relationshipJSON.getString("name");
		} catch (JSONException t) {
			throw new RuntimeException("Impossible to read property [name] from relationship json object", t);
		}
	}
	
	/* (non-Javadoc)
	 * @see it.eng.qbe.model.structure.IModelRelationshipDescriptor#getName()
	 */
	public String getLabel() {
		return relationshipJSON.optString("label");
	}
}
