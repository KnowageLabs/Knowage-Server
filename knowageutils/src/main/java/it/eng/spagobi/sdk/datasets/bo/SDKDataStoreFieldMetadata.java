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

package it.eng.spagobi.sdk.datasets.bo;

public class SDKDataStoreFieldMetadata implements java.io.Serializable {
	private String className;

	private String name;

	private java.util.HashMap properties;

	public SDKDataStoreFieldMetadata() {
	}

	public SDKDataStoreFieldMetadata(String className, String name, java.util.HashMap properties) {
		this.className = className;
		this.name = name;
		this.properties = properties;
	}

	/**
	 * Gets the className value for this SDKDataStoreFieldMetadata.
	 *
	 * @return className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Sets the className value for this SDKDataStoreFieldMetadata.
	 *
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Gets the name value for this SDKDataStoreFieldMetadata.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SDKDataStoreFieldMetadata.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the properties value for this SDKDataStoreFieldMetadata.
	 *
	 * @return properties
	 */
	public java.util.HashMap getProperties() {
		return properties;
	}

	/**
	 * Sets the properties value for this SDKDataStoreFieldMetadata.
	 *
	 * @param properties
	 */
	public void setProperties(java.util.HashMap properties) {
		this.properties = properties;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKDataStoreFieldMetadata))
			return false;
		SDKDataStoreFieldMetadata other = (SDKDataStoreFieldMetadata) obj;
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
				&& ((this.className == null && other.getClassName() == null)
						|| (this.className != null && this.className.equals(other.getClassName())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& ((this.properties == null && other.getProperties() == null)
						|| (this.properties != null && this.properties.equals(other.getProperties())));
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
		if (getClassName() != null) {
			_hashCode += getClassName().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		if (getProperties() != null) {
			_hashCode += getProperties().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
