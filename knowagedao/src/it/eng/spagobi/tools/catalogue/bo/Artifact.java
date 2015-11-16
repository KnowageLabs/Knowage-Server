/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.catalogue.bo;

public class Artifact {

	private Integer id;
	
	private Integer currentContentId;

	private String name;

	private String description;
	
	private String type;

	private Boolean modelLocked;

	private String modelLocker;
	
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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
	
	public Integer getCurrentContentId() {
		return currentContentId;
	}

	public void setCurrentContentId(Integer currentContentId) {
		this.currentContentId = currentContentId;
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

	@Override
	public String toString() {
		return "Artifact [id=" + id + ", name=" + name + ", description="
				+ description + ", type=" + type + ", currentContentId="
				+ currentContentId + "]";
	}
	
}
