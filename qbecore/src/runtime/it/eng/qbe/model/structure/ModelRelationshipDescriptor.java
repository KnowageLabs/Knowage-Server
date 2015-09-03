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
			JSONObject destinationTable = relationshipJSON.getJSONObject("sourceTable");
			return getEntityUniqueName(destinationTable);
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
			return getEntityUniqueName(destinationTable);
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
			JSONArray sourceColumn = relationshipJSON.getJSONArray("sourceColumns");

			for (int i = 0; i < sourceColumn.length(); i++) {
				sourceColumnNames.add(getFieldUniqueName(sourceTable, sourceColumn.getString(i)));
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
			JSONArray destinationColumn = relationshipJSON.getJSONArray("destinationColumns");

			for (int i = 0; i < destinationColumn.length(); i++) {
				destinationColumnNames.add(getFieldUniqueName(destinationTable, destinationColumn.getString(i)));
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
	

	/**
	 * retrieves the name of the entity
	 * 
	 * @param table
	 * @return
	 */
	private String getEntityUniqueName(JSONObject table) {
		StringBuffer name = new StringBuffer("");
		String tableName = table.optString("className");
		String tableType = table.optString("package");
		// the qbe works starting from models or datasets. in the case of the datasets there isn't the tabletype but just the name of the dataset
		if (tableType != null && tableType.length() > 0) {
			name.append(tableType);
			name.append(".");
		}
		name.append(tableName);
		name.append("::");
		name.append(tableName);
		return name.toString();
	}

	/**
	 * retrieves the name of the field
	 * 
	 * @param table
	 * @param column
	 * @return
	 */
	private String getFieldUniqueName(JSONObject table, String column) {
		StringBuffer name = new StringBuffer("");
		String tableName = table.optString("className");
		String tableType = table.optString("package");
		// the qbe works starting from models or datasets. in the case of the datasets there isn't the tabletype but just the name of the dataset
		if (tableType != null && tableType.length() > 0) {
			name.append(tableType);
			name.append(".");
		}
		name.append(tableName);
		name.append(":");
		name.append(column);
		return name.toString();
	}
}
