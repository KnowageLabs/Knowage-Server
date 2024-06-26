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

public class HierarchicalDimensionField extends ModelField {

	private String name;
	private String entity;
	private List<Hierarchy> hierarchies;

	public HierarchicalDimensionField() {
	}

	public HierarchicalDimensionField(String name, String entity) {
		setName(name);
		setEntity(entity);
		setHierarchies(new ArrayList<Hierarchy>());
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the entity
	 */
	public String getEntity() {
		return entity;
	}

	/**
	 * @param entity
	 *            the entity to set
	 */
	public void setEntity(String entity) {
		this.entity = entity;
	}

	public List<Hierarchy> getHierarchies() {
		return hierarchies;
	}

	public void setHierarchies(List<Hierarchy> hierarchies) {
		this.hierarchies = hierarchies;
	}

	public Hierarchy getDefaultHierarchy() {
		for (Hierarchy hierarchy : hierarchies) {
			if (hierarchy.getIsDefault()) {
				return hierarchy;
			}
		}
		return hierarchies.size() > 0 ? hierarchies.get(0) : null;
	}


	public void setDefaultHierarchy(String hierarchyName){
		for (Hierarchy h:getHierarchies()){
			h.setIsDefault(false);
			if(h.getName().equals(hierarchyName)){
				h.setIsDefault(true);
			}
		}
	}


}
