/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.model.structure;

import java.util.ArrayList;
import java.util.List;

public class HierarchicalDimensionField extends ModelField{

	private String name;
	private String entity;
	private List<Hierarchy> hierarchies;


	public HierarchicalDimensionField(String name) {
		setName(name);
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
	 * @param name the name to set
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
	 * @param entity the entity to set
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

}
