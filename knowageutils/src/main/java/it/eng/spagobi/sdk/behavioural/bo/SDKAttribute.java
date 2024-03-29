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

package it.eng.spagobi.sdk.behavioural.bo;

import java.io.ObjectOutputStream;

public class SDKAttribute implements java.io.Serializable {
	private String description;

	private Integer id;

	private String name;

	public SDKAttribute() {
	}

	public SDKAttribute(String description, Integer id, String name) {
		this.description = description;
		this.id = id;
		this.name = name;
	}

	/**
	 * Gets the description value for this SDKAttribute.
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description value for this SDKAttribute.
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the id value for this SDKAttribute.
	 *
	 * @return id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id value for this SDKAttribute.
	 *
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the name value for this SDKAttribute.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SDKAttribute.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKAttribute))
			return false;
		SDKAttribute other = (SDKAttribute) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true
				&& ((this.description == null && other.getDescription() == null)
						|| (this.description != null && this.description.equals(other.getDescription())))
				&& ((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(other.getId())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())));
		__equalsCalc = null;
		return _equals;
	}

	private boolean __hashCodeCalc = false;

	@Override
	public synchronized int hashCode() {
		if (__hashCodeCalc) {
			return 0;
		}
		__hashCodeCalc = true;
		int _hashCode = 1;
		if (getDescription() != null) {
			_hashCode += getDescription().hashCode();
		}
		if (getId() != null) {
			_hashCode += getId().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

	public void writeObject(ObjectOutputStream aOutputStream) {
		  throw new UnsupportedOperationException("Security violation : cannot serialize object to a stream");
	}

}
