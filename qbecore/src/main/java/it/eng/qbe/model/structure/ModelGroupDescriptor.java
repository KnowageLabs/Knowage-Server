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
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class ModelGroupDescriptor implements IModelGroupDescriptor {

	private final JSONObject groupJSON;
	private final String modelName;

	public ModelGroupDescriptor(JSONObject groupJSON) {
		this(groupJSON, null);
	}

	public ModelGroupDescriptor(JSONObject groupJSON, String modelName) {
		this.groupJSON = groupJSON;
		this.modelName = modelName;
		if (getName().trim().isEmpty()) {
			throw new SpagoBIRuntimeException("Group name cannot be empty");
		}
		getEntityTypes();
	}

	@Override
	public String getModelName() {
		return modelName == null ? groupJSON.optString("model") : modelName;
	}

	@Override
	public String getName() {
		return groupJSON.optString("name");
	}

	@Override
	public String getUniqueName() {
		String uniqueName = groupJSON.optString("uniqueName");
		return uniqueName.trim().isEmpty() ? getName() : uniqueName;
	}

	@Override
	public String getDescription() {
		return groupJSON.optString("description");
	}

	@Override
	public List<String> getEntityTypes() {
		List<String> entityTypes = new ArrayList<>();
		try {
			JSONArray entitiesJSON = groupJSON.getJSONArray("entities");
			for (int i = 0; i < entitiesJSON.length(); i++) {
				String entityType = entitiesJSON.getString(i);
				if (entityType.trim().isEmpty()) {
					throw new SpagoBIRuntimeException("Group entity type cannot be empty");
				}
				entityTypes.add(entityType);
			}
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Invalid entities for group [" + getName() + "]", e);
		}
		return entityTypes;
	}
}
