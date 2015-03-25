/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.tools.dataset.validation;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class HierarchyLevel {
	
	private String hierarchy_name;
	private String level_name;
	private String column_type;
	
	/**
	 * @param column_name
	 * @param level_name
	 */
	public HierarchyLevel(String hierarchy_name, String level_name) {
		
		this.hierarchy_name = hierarchy_name;
		this.level_name = level_name;
		this.column_type = null;

	}
	
	public HierarchyLevel(String hierarchy_name, String level_name, String column_type){
		this.hierarchy_name = hierarchy_name;
		this.level_name = level_name;
		this.column_type = column_type;
	}
	
	public HierarchyLevel(){
		this.hierarchy_name = null;
		this.level_name = null;
		this.column_type = null;
	}

	/**
	 * @return the hierarchy_name
	 */
	public String getHierarchy_name() {
		return hierarchy_name;
	}

	/**
	 * @param hierarchy_name the hierarchy_name to set
	 */
	public void setHierarchy_name(String hierarchy_name) {
		this.hierarchy_name = hierarchy_name;
	}

	/**
	 * @return the level_name
	 */
	public String getLevel_name() {
		return level_name;
	}

	/**
	 * @param level_name the level_name to set
	 */
	public void setLevel_name(String level_name) {
		this.level_name = level_name;
	}
	
	/**
	 * @return the column_type
	 */
	public String getColumn_type() {
		return column_type;
	}

	/**
	 * @param column_type the column_type to set
	 */
	public void setColumn_type(String column_type) {
		this.column_type = column_type;
	}

	public boolean isValidEntry(){
		if ((this.level_name != null) && (this.hierarchy_name != null)){
			return true;
		} else {
			return false;
		}
	}
	
	public String toString(){
		return this.hierarchy_name+" "+this.level_name;
	}
	
	
	
	

}
