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
package it.eng.spagobi.tools.hierarchiesmanagement.metadata;

/**
 * @author Antonella Giachino (giachino.antonella@eng.it)
 *
 */
public class Field {

	String id;
	String name;
	String type;
	String fixValue;
	boolean isVisible;
	boolean isEditable;
	boolean isRequired;
	boolean isSingleValue;
	boolean isParent;

	/**
	 * @param name
	 * @param type
	 */
	public Field(String id, String name, String alias, String type) {
		this(id, name, type, null, false, false, false, true, false);
	}

	public Field(String id, String name, String type, String fixValue, boolean isVisible, boolean isEditable, boolean isRequired, boolean isSingleValue,
			boolean isParent) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.fixValue = fixValue;
		this.isVisible = isVisible;
		this.isEditable = isEditable;
		this.isEditable = isEditable;
		this.isSingleValue = isSingleValue;
		this.isParent = isParent;
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

	public String getFixValue() {
		return fixValue;
	}

	public void setFixValue(String fixValue) {
		this.fixValue = fixValue;
	}

	public boolean isParent() {
		return isParent;
	}

	public void setParent(boolean isParent) {
		this.isParent = isParent;
	}

}
