/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package it.eng.spagobi.tools.dataset.common.association;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AssociationGroupJSONSerializer {
	
	
	public  AssociationGroup deserialize(String o) throws JSONException {
		JSONObject associationGroupJSON = new JSONObject(o);
		return deserialize(associationGroupJSON);
	}
	
	public AssociationGroup deserialize(JSONObject o) throws JSONException {
		AssociationGroup associationGroup = new AssociationGroup(); 
		AssociationJSONSerializer associationSerializer = new AssociationJSONSerializer();
		JSONArray associationsJSON = o.getJSONArray("associations");
		List<Association> associations = associationSerializer.deserialize(associationsJSON);
		associationGroup.addAssociations(associations);
		return associationGroup;
	}
	
	public JSONArray serialize(Collection<AssociationGroup> associationGroups) throws JSONException {
		JSONArray a = new JSONArray();
		for(AssociationGroup associationGroup : associationGroups) {
			a.put( serialize(associationGroup) );
		}
		return a;
	}
	public JSONObject serialize(AssociationGroup associationGroup) throws JSONException {
		JSONObject o = new JSONObject();
		AssociationJSONSerializer associationSerializer = new AssociationJSONSerializer();
		o.put("datasets", serializeDatatsets(associationGroup));
		o.put("associations", associationSerializer.serialize( associationGroup.getAssociations() ));
		return o;
	}
	
	public JSONArray serializeDatatsets(AssociationGroup associationGroup) {
		JSONArray a = new JSONArray();
		Set<String> datasets = associationGroup.getDataSetLabels();
		for(String dataset : datasets) {
			a.put(dataset);
		}
		return a;
	}
}
