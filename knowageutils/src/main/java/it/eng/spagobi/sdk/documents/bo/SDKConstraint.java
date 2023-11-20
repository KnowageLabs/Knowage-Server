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

package it.eng.spagobi.sdk.documents.bo;

public class SDKConstraint implements java.io.Serializable {
	private String description;

	private String firstValue;

	private Integer id;

	private String label;

	private String name;

	private String secondValue;

	private String type;

	public SDKConstraint() {
	}

	public SDKConstraint(String description, String firstValue, Integer id, String label, String name,
			String secondValue, String type) {
		this.description = description;
		this.firstValue = firstValue;
		this.id = id;
		this.label = label;
		this.name = name;
		this.secondValue = secondValue;
		this.type = type;
	}

	/**
	 * Gets the description value for this SDKConstraint.
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description value for this SDKConstraint.
	 *
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the firstValue value for this SDKConstraint.
	 *
	 * @return firstValue
	 */
	public String getFirstValue() {
		return firstValue;
	}

	/**
	 * Sets the firstValue value for this SDKConstraint.
	 *
	 * @param firstValue
	 */
	public void setFirstValue(String firstValue) {
		this.firstValue = firstValue;
	}

	/**
	 * Gets the id value for this SDKConstraint.
	 *
	 * @return id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id value for this SDKConstraint.
	 *
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the label value for this SDKConstraint.
	 *
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label value for this SDKConstraint.
	 *
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the name value for this SDKConstraint.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SDKConstraint.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the secondValue value for this SDKConstraint.
	 *
	 * @return secondValue
	 */
	public String getSecondValue() {
		return secondValue;
	}

	/**
	 * Sets the secondValue value for this SDKConstraint.
	 *
	 * @param secondValue
	 */
	public void setSecondValue(String secondValue) {
		this.secondValue = secondValue;
	}

	/**
	 * Gets the type value for this SDKConstraint.
	 *
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type value for this SDKConstraint.
	 *
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKConstraint))
			return false;
		SDKConstraint other = (SDKConstraint) obj;
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
				&& ((this.firstValue == null && other.getFirstValue() == null)
						|| (this.firstValue != null && this.firstValue.equals(other.getFirstValue())))
				&& ((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(other.getId())))
				&& ((this.label == null && other.getLabel() == null)
						|| (this.label != null && this.label.equals(other.getLabel())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& ((this.secondValue == null && other.getSecondValue() == null)
						|| (this.secondValue != null && this.secondValue.equals(other.getSecondValue())))
				&& ((this.type == null && other.getType() == null)
						|| (this.type != null && this.type.equals(other.getType())));
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
		if (getFirstValue() != null) {
			_hashCode += getFirstValue().hashCode();
		}
		if (getId() != null) {
			_hashCode += getId().hashCode();
		}
		if (getLabel() != null) {
			_hashCode += getLabel().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		if (getSecondValue() != null) {
			_hashCode += getSecondValue().hashCode();
		}
		if (getType() != null) {
			_hashCode += getType().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
