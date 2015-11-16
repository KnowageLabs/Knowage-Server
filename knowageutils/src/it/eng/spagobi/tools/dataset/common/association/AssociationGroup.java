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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A group of associations. It is used to group together associations defined over
 * connected dataset
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AssociationGroup {
	
	Map<String, Association> associations;
	
	public AssociationGroup() {
		associations = new HashMap<String, Association>();
	}
	
	public Collection<Association> getAssociations() {
		return associations.values();
	}
	
	public Association getAssociation(String id) {
		return associations.get(id);
	}
	
	public void addAssociation(Association association) {
		associations.put(association.getId(), association);
	}
	
	public void addAssociations(List<Association> associations) {
		for(Association association : associations) {
			addAssociation(association);
		}
	}
	
	public Set<String> getDataSetLabels() {
		Set<String> dataSetLabels = new HashSet<String>();
		Collection<Association> values = associations.values();
		for(Association asssociation : values) {
			for(Association.Field field :  asssociation.getFields() ) {
				dataSetLabels.add( field.getDataSetLabel() );
			}
		}
		return dataSetLabels;
	}
	
	public boolean containsDataSet(String dataSetLabel) {
		Collection<Association> values = associations.values();
		for(Association asssociation : values) {
			for(Association.Field field :  asssociation.getFields() ) {
				if( field.getDataSetLabel().equals(dataSetLabel) ) return true;
			}
		}
		return false;
	}
	
	
}
