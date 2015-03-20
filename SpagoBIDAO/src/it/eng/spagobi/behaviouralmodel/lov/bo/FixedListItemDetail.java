/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.lov.bo;

import java.io.Serializable;

/**
 * Defines the <code>LovDetail</code> objects. This object is used to store 
 * Fixed Lov Selection Wizard detail information.
 */
public class FixedListItemDetail  implements Serializable  {
	
	private String value= "" ;
	private String description = "";
	
	/**
	 * Returns the description.
	 * 
	 * @return the description.
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
	 * Returns the value.
	 * 
	 * @return the value.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param value the value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
}