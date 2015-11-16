/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.check.bo;

import java.io.Serializable;

/**
 * Defines a value constraint object.
 * 
 * @author sulis
 *
 */


public class Check  implements Serializable   {
	
	private Integer checkId;
	private Integer valueTypeId;
	private String Name;
	private String label;
	private String Description;
	private String valueTypeCd;
	private String firstValue;
	private String secondValue;
	
	
	/**
	 * Gets the description.
	 * 
	 * @return Returns the description.
	 */
	public String getDescription() {
		return Description;
	}
	
	/**
	 * Sets the description.
	 * 
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		Description = description;
	}
	
	/**
	 * Gets the first value.
	 * 
	 * @return Returns the firstValue.
	 */
	public String getFirstValue() {
		return firstValue;
	}
	
	/**
	 * Sets the first value.
	 * 
	 * @param firstValue The firstValue to set.
	 */
	public void setFirstValue(String firstValue) {
		this.firstValue = firstValue;
	}
	
	/**
	 * Gets the name.
	 * 
	 * @return Returns the name.
	 */
	public String getName() {
		return Name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name The name to set.
	 */
	public void setName(String name) {
		Name = name;
	}
	
	/**
	 * Gets the second value.
	 * 
	 * @return Returns the secondValue.
	 */
	public String getSecondValue() {
		return secondValue;
	}
	
	/**
	 * Sets the second value.
	 * 
	 * @param secondValue The secondValue to set.
	 */
	public void setSecondValue(String secondValue) {
		this.secondValue = secondValue;
	}
	
	/**
	 * Gets the value type cd.
	 * 
	 * @return Returns the valueTypeCd.
	 */
	public String getValueTypeCd() {
		return valueTypeCd;
	}
	
	/**
	 * Sets the value type cd.
	 * 
	 * @param valueTypeCd The valueTypeCd to set.
	 */
	public void setValueTypeCd(String valueTypeCd) {
		this.valueTypeCd = valueTypeCd;
	}
	
	/*
	public static Check load(SourceBean sb) throws EMFUserError {
		CheckDAOImpl repChecksDAO = new CheckDAOImpl();
		return (Check)repChecksDAO.load(sb);
		
	}

	public void modify() throws EMFUserError {
		CheckDAOImpl repChecksDAO = new CheckDAOImpl();
		repChecksDAO.modify(this);
	}
	
	public void erase() throws EMFUserError {
		CheckDAOImpl repChecksDAO = new CheckDAOImpl();
		repChecksDAO.erase(this);
	}

	public void insert() throws EMFUserError {
		CheckDAOImpl repChecksDAO = new CheckDAOImpl();
		repChecksDAO.insert(this);
	}
	*/
	/**
	 * Gets the check id.
	 * 
	 * @return Returns the CheckId.
	 */
	public Integer getCheckId() {
		return checkId;
	}
	
	/**
	 * Sets the check id.
	 * 
	 * @param checkId The checkId to set.
	 */
	public void setCheckId(Integer checkId) {
		this.checkId = checkId;
	}
	
	/**
	 * Gets the value type id.
	 * 
	 * @return Returns the ValueTypeId.
	 */
	public Integer getValueTypeId() {
		return valueTypeId;
	}
	
	/**
	 * Sets the value type id.
	 * 
	 * @param valueTypeId The valueTypeId to set.
	 */
	public void setValueTypeId(Integer valueTypeId) {
		this.valueTypeId = valueTypeId;
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
}
