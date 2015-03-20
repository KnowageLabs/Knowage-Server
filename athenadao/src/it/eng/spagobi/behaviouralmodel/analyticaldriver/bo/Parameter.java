/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

import it.eng.spagobi.behaviouralmodel.lov.bo.ModalitiesValue;

import java.io.Serializable;
import java.util.List;



/**
 * Defines a <code>Parameter</code> object.
 *
 * @author sulis
 */

public class Parameter implements Serializable {
	
	private Integer  id;
	private String 	 description = ""; 
	private Integer  length;
	private String 	 label = "";
	private String 	 name = "";
	private String type = "";
	private String mask = "";
	private Integer typeId;
	private String modality = "";
	private boolean isFunctional;
	private boolean isTemporal;
	
	private ModalitiesValue modalityValue = null;
	private ModalitiesValue modalityValueForDefault = null;
	
	private String defaultFormula = "";

	private List checks = null;

	/**
	 * Gets the description.
	 * 
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Sets the description.
	 * 
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Gets the id.
	 * 
	 * @return Returns the id.
	 */
	public Integer  getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id The id to set.
	 */
	public void setId(Integer  id) {
		this.id = id;
	}
	
	/**
	 * Gets the label.
	 * 
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Sets the label.
	 * 
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Gets the length.
	 * 
	 * @return Returns the length.
	 */
	public Integer  getLength() {
		return length;
	}
	
	/**
	 * Sets the length.
	 * 
	 * @param length The length to set.
	 */
	public void setLength(Integer  length) {
		this.length = length;
	}
	
	/**
	 * Gets the mask.
	 * 
	 * @return Returns the mask.
	 */
	public String getMask() {
		return mask;
	}
	
	/**
	 * Sets the mask.
	 * 
	 * @param mask The mask to set.
	 */
	public void setMask(String mask) {
		this.mask = mask;
	}
	
	/**
	 * Gets the type.
	 * 
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Sets the type.
	 * 
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * Gets the type id.
	 * 
	 * @return Returns the typeId.
	 */
	public Integer getTypeId() {
		return typeId;
	}
	
	/**
	 * Sets the type id.
	 * 
	 * @param typeId The typeId to set.
	 */
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}
	
	/**
	 * Gets the modality value.
	 * 
	 * @return Returns the modalityValue.
	 */
	public ModalitiesValue getModalityValue() {
		return modalityValue;
	}
	
	/**
	 * Sets the modality value.
	 * 
	 * @param modalityValue The modalityValue to set.
	 */
	public void setModalityValue(ModalitiesValue modalityValue) {
		this.modalityValue = modalityValue;
	}
	
	/**
	 * Gets the modality.
	 * 
	 * @return Returns the modality.
	 */
	public String getModality() {
		return modality;
	}
	
	/**
	 * Sets the modality.
	 * 
	 * @param modality The modality to set.
	 */
	public void setModality(String modality) {
		this.modality = modality;
	}
	
	/**
	 * Gets the checks.
	 * 
	 * @return Returns the checks.
	 */
	public List getChecks() {
		return checks;
	}
	
	/**
	 * Sets the checks.
	 * 
	 * @param checks The checks to set.
	 */
	public void setChecks(List checks) {
		this.checks = checks;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Checks if is functional.
	 * 
	 * @return true, if is functional
	 */
	public boolean isFunctional() {
		return this.isFunctional;
	}
	
	/**
	 * Sets the checks if is functional.
	 * 
	 * @param isFunctional the new checks if is functional
	 */
	public void setIsFunctional(boolean isFunctional) {
		this.isFunctional = isFunctional;
	}

	
	/**
	 * Checks if the parameter is temporal.
	 * 
	 * @return true if the parameter is temporal
	 */
	public boolean isTemporal() {
		return isTemporal;
	}

	/**
	 * Sets the checks if the parameter is temporal.
	 * 
	 * @param isTemporal the new checks if is temporal
	 */
	public void setIsTemporal(boolean isTemporal) {
		this.isTemporal = isTemporal;
	}

	public ModalitiesValue getModalityValueForDefault() {
		return modalityValueForDefault;
	}

	public void setModalityValueForDefault(ModalitiesValue modalityValueForDefault) {
		this.modalityValueForDefault = modalityValueForDefault;
	}
	
	public String getDefaultFormula() {
		return this.defaultFormula;
	}
	
	public void setDefaultFormula(String defaultFormula) {
		this.defaultFormula = defaultFormula;
	}
	
}