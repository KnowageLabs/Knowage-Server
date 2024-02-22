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
package it.eng.qbe.model.structure;





import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.utilities.assertion.Assert;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ModelViewEntityDescriptor implements IModelViewEntityDescriptor {
	JSONObject viewJSON;
	private static final Logger logger = Logger.getLogger(ModelViewEntityDescriptor.class);

	//this relationships will be added later after creation of all Views
	private List<IModelViewRelationshipDescriptor> relationshipToViewsDescriptors;

	public ModelViewEntityDescriptor(JSONObject viewJSON) {
		this.viewJSON = viewJSON;
	}

	@Override
	public Set<String> getInnerEntityUniqueNames() {
		Set<String> innerEntityUniqueNames = new HashSet<>();

		try {
			JSONArray tables = viewJSON.optJSONArray("tables");
			for(int i = 0; i < tables.length(); i++) {
				JSONObject table = tables.getJSONObject(i);
				String tableName = table.optString("name");
				String tableType = table.optString("package");
				innerEntityUniqueNames.add(tableType + "." + tableName + "::" + tableName);
			}
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to read inner entities name from conf file", t);
		}

		return innerEntityUniqueNames;
	}

	@Override
	public String getName() {
		String name = viewJSON.optString("name");
		Assert.assertNotNull(name, "View name cannot be null");
		return name;
	}

	@Override
	public String getType() {
		JSONArray tables = viewJSON.optJSONArray("tables");

		String pkg = null;
		try {
			pkg = tables.getJSONObject(0).optString("package");
		} catch (JSONException e) {
			throw new RuntimeException("Package attribute cannot be null", e);
		}

		return pkg + "." + getName();
	}

	@Override
	public List<IModelViewJoinDescriptor> getJoinDescriptors() {
		List<IModelViewJoinDescriptor> joinDescriptors;

		joinDescriptors = new ArrayList<>();
		try {
			JSONArray joinsJSON = viewJSON.optJSONArray("joins");
			for(int i = 0; i < joinsJSON.length(); i++) {
				JSONObject joinJSON = joinsJSON.getJSONObject(i);
				joinDescriptors.add( new ModelViewJoinDescriptor(joinJSON) );
			}
		} catch(Throwable t) {
			throw new RuntimeException("Impossible to read inner joins from conf file", t);
		}

		return joinDescriptors;
	}

	@Override
	public List<IModelViewRelationshipDescriptor> getRelationshipDescriptors() {
		List<IModelViewRelationshipDescriptor> relationshipDescriptors;

		relationshipDescriptors = new ArrayList<>();
		relationshipToViewsDescriptors = new ArrayList<>();

		try {
			JSONArray inboundRelationshipsJSON = viewJSON.optJSONArray("inbound");
			JSONArray outboundRelationshipsJSON = viewJSON.optJSONArray("outbound");

			for(int i = 0; i < inboundRelationshipsJSON.length(); i++) {
				JSONObject relationshipJSON = inboundRelationshipsJSON.getJSONObject(i);

				JSONObject sourceTable = relationshipJSON.getJSONObject("sourceTable");
				boolean isSourceEntityView = Boolean.parseBoolean(sourceTable.getString("isBusinessView"));

				if (isSourceEntityView){
					relationshipToViewsDescriptors.add(new ModelViewRelationshipDescriptor(relationshipJSON,false));
					continue;
				} else {
					relationshipDescriptors.add( new ModelViewRelationshipDescriptor(relationshipJSON,false) );
				}

			}

			for(int i = 0; i < outboundRelationshipsJSON.length(); i++) {
				JSONObject relationshipJSON = outboundRelationshipsJSON.getJSONObject(i);

				JSONObject destinationTable = relationshipJSON.getJSONObject("destinationTable");
				boolean isDestinationEntityView = Boolean.parseBoolean(destinationTable.getString("isBusinessView"));

				if (isDestinationEntityView){
					relationshipToViewsDescriptors.add( new ModelViewRelationshipDescriptor(relationshipJSON,true) );
					continue;
				} else {
					relationshipDescriptors.add( new ModelViewRelationshipDescriptor(relationshipJSON,true) );
				}
			}


		} catch(Throwable t) {
			throw new RuntimeException("Impossible to read view relationships from conf file", t);
		}
		return relationshipDescriptors;

	}

	/**
	 * @return the relationshipToViewsDescriptors
	 */
	@Override
	public List<IModelViewRelationshipDescriptor> getRelationshipToViewsDescriptors() {
		return relationshipToViewsDescriptors;
	}

	/**
	 * @param relationshipToViewsDescriptors the relationshipToViewsDescriptors to set
	 */
	public void setRelationshipToViewsDescriptors(
			List<IModelViewRelationshipDescriptor> relationshipToViewsDescriptors) {
		this.relationshipToViewsDescriptors = relationshipToViewsDescriptors;
	}

	public class ModelViewJoinDescriptor implements IModelViewJoinDescriptor {

		String sourceEntityUniqueName;
		String destinationEntityUniqueName;
		List<String> sourceColumns;
		List<String> destinationColumns;


		public ModelViewJoinDescriptor(JSONObject joinJSON) {
			try {
				JSONObject sourceTable = joinJSON.getJSONObject("sourceTable");
				JSONObject destinationTable = joinJSON.getJSONObject("destinationTable");

				String pkg;
				String tableName;

				pkg = sourceTable.getString("package");
				tableName = sourceTable.getString("name");
				sourceEntityUniqueName = pkg + "." + tableName + "::" + tableName;

				pkg = destinationTable.getString("package");
				tableName = destinationTable.getString("name");
				destinationEntityUniqueName = pkg + "." + tableName + "::" + tableName;

				JSONArray sourceColumsJSON = joinJSON.optJSONArray("sourceColumns");
				sourceColumns = deserializeColumnsArray( sourceColumsJSON );

				JSONArray destinationColumsJSON = joinJSON.optJSONArray("destinationColumns");
				destinationColumns = deserializeColumnsArray( destinationColumsJSON );
			} catch(Throwable t) {
				throw new RuntimeException("Impossible to initialize ModelViewJoinDescriptor from conf object: " + joinJSON, t);
			}
		}

		private List<String> deserializeColumnsArray(JSONArray columnsJSON) throws JSONException {
			List<String> columns;

			columns = new ArrayList<>();
			for(int i = 0; i < columnsJSON.length(); i++) {
				columns.add( columnsJSON.getString(i) );
			}
			return columns;
		}

		@Override
		public String getSourceEntityUniqueName() {
			return sourceEntityUniqueName;
		}

		@Override
		public String getDestinationEntityUniqueName() {
			return destinationEntityUniqueName;
		}

		@Override
		public List<String> getSourceColumns() {
			return sourceColumns;
		}

		@Override
		public List<String> getDestinationColumns() {
			return destinationColumns;
		}


	}

	public class ModelViewRelationshipDescriptor implements IModelViewRelationshipDescriptor {

		String sourceEntityUniqueName;
		String destinationEntityUniqueName;
		List<String> relationshipSourceColumns;
		List<String> relationshipDestinationColumns;
		boolean isOutbound;
		boolean isSourceEntityView;
		boolean isDestinationEntityView;


		public ModelViewRelationshipDescriptor(JSONObject relationshipJSON, boolean isOutbound) {
			try {
				logger.debug("Descriptor for "+relationshipJSON);
				this.isOutbound = isOutbound;
				String pkg;
				String tableName;

				if(isOutbound){
					JSONObject destinationTable = relationshipJSON.getJSONObject("destinationTable");
					pkg = destinationTable.getString("package");
					tableName = destinationTable.getString("name");
					isDestinationEntityView = Boolean.parseBoolean(destinationTable.getString("isBusinessView"));
					destinationEntityUniqueName = pkg + "." + tableName + "::" + tableName;

					//this is not really a unique name because points to a BusinessView
					sourceEntityUniqueName = getName();

				} else {
					JSONObject sourceTable = relationshipJSON.getJSONObject("sourceTable");
					pkg = sourceTable.getString("package");
					tableName = sourceTable.getString("name");
					isSourceEntityView = Boolean.parseBoolean(sourceTable.getString("isBusinessView"));
					sourceEntityUniqueName = pkg + "." + tableName + "::" + tableName;

					//this is not really a unique name because points to a BusinessView
					destinationEntityUniqueName = getName();
				}

				JSONArray sourceColumsJSON = relationshipJSON.optJSONArray("sourceColumns");
				if (sourceColumsJSON == null){
					logger.error("sourceColumsJSON is null");
				}
				relationshipSourceColumns = deserializeColumnsArray( sourceColumsJSON );

				JSONArray destinationColumsJSON = relationshipJSON.optJSONArray("destinationColumns");
				if (destinationColumsJSON == null){
					logger.error("destinationColumsJSON is null");
				}
				relationshipDestinationColumns = deserializeColumnsArray( destinationColumsJSON );
			}
			catch(Throwable t) {
				logger.debug("Impossible to initialize ModelViewRelationshipDescriptor from conf object: "+ relationshipJSON,t);
				throw new RuntimeException("Impossible to initialize ModelViewRelationshipDescriptor from conf object: " + relationshipJSON, t);
			}
		}

		private List<String> deserializeColumnsArray(JSONArray columnsJSON)  {
			List<String> columns = null;
			try {
				columns = new ArrayList<>();
				int i;
				for(i = 0; i < columnsJSON.length(); i++) {
					String columnName = columnsJSON.getString(i);
					columns.add( columnName );
				}
			} catch (JSONException e){
				e.printStackTrace();
				logger.error("Error in columnsJSON: "+columnsJSON);
			}

			return columns;
		}


		@Override
		public String getSourceEntityUniqueName() {
			return sourceEntityUniqueName;
		}

		@Override
		public String getDestinationEntityUniqueName() {
			return destinationEntityUniqueName;
		}

		@Override
		public List<String> getSourceColumns() {
			return relationshipSourceColumns;
		}

		@Override
		public List<String> getDestinationColumns() {
			return relationshipDestinationColumns;
		}

		@Override
		public boolean isOutbound() {
			return isOutbound;
		}

		/**
		 * @return the isSourceEntityView
		 */
		@Override
		public boolean isSourceEntityView() {
			return isSourceEntityView;
		}

		/**
		 * @return the isDestinationEntityView
		 */
		@Override
		public boolean isDestinationEntityView() {
			return isDestinationEntityView;
		}

	}
}
