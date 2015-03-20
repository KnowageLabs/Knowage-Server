 /* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.metamodel;

import java.util.Map;

import org.eclipse.emf.common.util.EList;

import it.eng.spagobi.meta.model.olap.Hierarchy;
import it.eng.spagobi.meta.model.olap.Level;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

/**
 * 
 * This class wraps a it.eng.spagobi.meta.model.olap.Hierarchy
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */

public class HierarchyWrapper{
	private  it.eng.spagobi.meta.model.olap.Hierarchy wrappedHierarchy;
	
	
	public HierarchyWrapper( Hierarchy wrappedHierarchy){
		this.wrappedHierarchy = wrappedHierarchy;
		
	}
	
	public String getName(){
		return wrappedHierarchy.getName();
	}
	
	public Map<Object, Object> getMembersMapBetweenLevels(String columnName1, String columnName2){
		return this.wrappedHierarchy.getMembersMapBetweenLevels(columnName1, columnName2);
	}

	public IDataStore getMembers(String columnName1){
		return this.wrappedHierarchy.getMembers(columnName1);
	}
	
	public EList<Level> getLevels(){
		return this.wrappedHierarchy.getLevels();
	}
	
	public IDataStore getSiblingValues(String siblingColumnName){
		return this.wrappedHierarchy.getSiblingValues(siblingColumnName);
	}
	
	public Map<Object, Object> getMembersAndSibling(String levelName, String columnName){
		return this.wrappedHierarchy.getMembersAndSibling(levelName,columnName);
	}
	
	/**
	 * Gets the position of the level in the hierarchy.
	 * The root of the hierarchy has level position =0 
	 */
	public int getLevelPosition(String levelAlias){
		int position =-1;
		for(int i=0; i<wrappedHierarchy.getLevels().size();i++){
			Level l =  wrappedHierarchy.getLevels().get(i);
			if(l.getName().equals(levelAlias)){
				position=i;
			}
		}
		return position;
	}
	
	/**
	 * Gets the Level of the hierarchy with name = levelAlias
	 * It returns null if it can not find the level
	 */
	public Level getLevel(String levelAlias){
		for(int i=0; i<wrappedHierarchy.getLevels().size();i++){
			Level l =  wrappedHierarchy.getLevels().get(i);
			if(l.getName().equals(levelAlias)){
				return l;
			}
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((wrappedHierarchy == null) ? 0 : wrappedHierarchy.getName().hashCode())
				+ ((wrappedHierarchy == null) ? 0 : wrappedHierarchy.getTable().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HierarchyWrapper other = (HierarchyWrapper) obj;
		if (wrappedHierarchy == null) {
			if (other.wrappedHierarchy != null)
				return false;
		} else if (!wrappedHierarchy.getName().equals(other.wrappedHierarchy.getName()) || !wrappedHierarchy.getTable().equals(other.wrappedHierarchy.getTable()))
			return false;
		return true;
	}

	

}
