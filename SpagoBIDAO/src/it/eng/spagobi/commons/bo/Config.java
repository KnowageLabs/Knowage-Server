/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.bo;

import java.io.Serializable;

/**
 * Defines a Domain object.
 */

public class Config  implements Serializable  {

	 private Integer id;
     private String label;
     private String name;
     private String description;
     private boolean isActive;
     private String valueCheck;
     private Integer valueTypeId;
     private String category;
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the isActive
	 */
	public boolean isActive() {
		return isActive;
	}
	/**
	 * @param isActive the isActive to set
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	/**
	 * @return the valueCheck
	 */
	public String getValueCheck() {
		return valueCheck;
	}
	/**
	 * @param valueCheck the valueCheck to set
	 */
	public void setValueCheck(String valueCheck) {
		this.valueCheck = valueCheck;
	}
	/**
	 * @return the valueTypeId
	 */
	public Integer getValueTypeId() {
		return valueTypeId;
	}
	/**
	 * @param valueTypeId the valueTypeId to set
	 */
	public void setValueTypeId(Integer valueTypeId) {
		this.valueTypeId = valueTypeId;
	}

	/**
	 * @return the category to get
	 */
	public String getCategory() {
		return category;
	}
	/**
	 * @param category. 
	 * The category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}
	
}




