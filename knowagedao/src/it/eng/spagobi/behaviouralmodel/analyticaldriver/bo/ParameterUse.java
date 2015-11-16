/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.analyticaldriver.bo;

import java.io.Serializable;
import java.util.List;

/**
 * Defines a <code>ParameterUse</code> object. 
 */


public class ParameterUse  implements Serializable  {
	
	Integer useID;
	Integer id; // in realtà questo è par_id nella tabella
	Integer idLov;
	Integer idLovForDefault;
	String name = "";
	String label = "";
	String description = "";
	
	List associatedRoles = null;
	List associatedChecks = null;
	
	String selectionType = "";
	boolean multivalue = true;
	
	Integer manualInput;
	boolean maximizerEnabled = true;
	
	private String defaultFormula;
	
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
	public Integer getId() {
		return id;
	}
	
	/**
	 * Sets the id.
	 * 
	 * @param id The id to set.
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * Gets the id lov.
	 * 
	 * @return Returns the idLov.
	 */
	public Integer getIdLov() {
		return idLov;
	}
	
	/**
	 * Sets the id lov.
	 * 
	 * @param idLov The idLov to set.
	 */
	public void setIdLov(Integer idLov) {
		this.idLov = idLov;
	}
	
	public Integer getIdLovForDefault() {
		return idLovForDefault;
	}
	
	public void setIdLovForDefault(Integer idLovForDefault) {
		this.idLovForDefault = idLovForDefault;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	
	/**
	 * Gets the associated roles.
	 * 
	 * @return Returns the associatedRoles.
	 */
	public List getAssociatedRoles() {
		return associatedRoles; 
	}
	
	/**
	 * Sets the associated roles.
	 * 
	 * @param listRoles The associatedRoles to set.
	 */
	public void setAssociatedRoles(List listRoles) {
		this.associatedRoles = listRoles;
	}
	
	/**
	 * Gets the use id.
	 * 
	 * @return Returns the useID.
	 */
	public Integer getUseID() {
		return useID;
	}
	
	/**
	 * Sets the use id.
	 * 
	 * @param useID The UseID to set.
	 */
	public void setUseID(Integer useID) {
		this.useID = useID;
	}
	
	/**
	 * Gets the associated checks.
	 * 
	 * @return Returns the associatedChecks.
	 */
	public List getAssociatedChecks() {
		return associatedChecks;
	}
	
	/**
	 * Sets the associated checks.
	 * 
	 * @param associatedChecks The associatedChecks to set.
	 */
	public void setAssociatedChecks(List associatedChecks) {
		this.associatedChecks = associatedChecks;
	}
	
	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Sets the label.
	 * 
	 * @param label the new label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Gets the manual input.
	 * 
	 * @return Returns the manualInput.
	 */
	public Integer getManualInput() {
		return manualInput;
	}
	
	/**
	 * Sets the manual input.
	 * 
	 * @param manualInput The manualInput to set.
	 */
	public void setManualInput(Integer manualInput) {
		this.manualInput = manualInput;
	}
	
	/**
	 * Gets the selection type.
	 * 
	 * @return the selection type
	 */
	public String getSelectionType() {
		return selectionType;
	}
	
	/**
	 * Sets the selection type.
	 * 
	 * @param selectionType the new selection type
	 */
	public void setSelectionType(String selectionType) {
		this.selectionType = selectionType;
	}
	
	/**
	 * Checks if is multivalue.
	 * 
	 * @return true, if is multivalue
	 */
	public boolean isMultivalue() {
		return multivalue;
	}
	
	/**
	 * Sets the multivalue.
	 * 
	 * @param multivalue the new multivalue
	 */
	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}
	
	public boolean isMaximizerEnabled() {
		return maximizerEnabled;
	}

	public void setMaximizerEnabled(boolean maximizerEnabled) {
		this.maximizerEnabled = maximizerEnabled;
	}
	
	public String getDefaultFormula() {
		return defaultFormula;
	}

	public void setDefaultFormula(String defaultFormula) {
		this.defaultFormula = defaultFormula;
	}
	
}