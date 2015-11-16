/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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

