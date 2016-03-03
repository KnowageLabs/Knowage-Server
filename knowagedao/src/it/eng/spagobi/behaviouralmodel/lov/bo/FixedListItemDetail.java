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