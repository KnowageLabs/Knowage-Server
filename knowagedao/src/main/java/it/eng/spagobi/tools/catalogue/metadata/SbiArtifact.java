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
package it.eng.spagobi.tools.catalogue.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;


public class SbiArtifact extends SbiHibernateModel {

	// Fields    

	private int id;

	private String name;

	private String description;
	
	private String type;
	
    private Boolean modelLocked;

	private String modelLocker;
	
	// Constructors

	public SbiArtifact() {
	}

	public SbiArtifact(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getModelLocked() {
		return modelLocked;
	}

	public void setModelLocked(Boolean modelLocked) {
		this.modelLocked = modelLocked;
	}

	public String getModelLocker() {
		return modelLocker;
	}

	public void setModelLocker(String modelLocker) {
		this.modelLocker = modelLocker;
	}



	
	
}

