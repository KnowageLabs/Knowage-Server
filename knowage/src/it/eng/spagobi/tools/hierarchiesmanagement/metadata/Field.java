/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.tools.hierarchiesmanagement.metadata;

/**
 * @author Antonella Giachino (giachino.antonella@eng.it)
 *
 */
public class Field {

	String id;
	String name;
	String type;
	boolean isVisible;
	boolean isEditable;
	boolean isRequired;
	boolean isSingleValue;

	/**
	 * @param name
	 * @param type
	 */
	public Field(String id, String name, String alias, String type) {
		this(id, name, type, false, false, false, true);
	}

	public Field(String id, String name, String type, boolean isVisible, boolean isEditable, boolean isRequired, boolean isSingleValue) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.isVisible = isVisible;
		this.isEditable = isEditable;
		this.isEditable = isEditable;
		this.isSingleValue = isSingleValue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	public boolean isSingleValue() {
		return isSingleValue;
	}

	public void setSingleValue(boolean isSingleValue) {
		this.isSingleValue = isSingleValue;
	}

}
