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

public class SDKDocumentParameter implements java.io.Serializable {
	private it.eng.spagobi.sdk.documents.bo.SDKConstraint[] constraints;

	private Integer id;

	private String label;

	private String type;

	private String urlName;

	private Object[] values;

	public SDKDocumentParameter() {
	}

	public SDKDocumentParameter(it.eng.spagobi.sdk.documents.bo.SDKConstraint[] constraints, Integer id, String label,
			String type, String urlName, Object[] values) {
		this.constraints = constraints;
		this.id = id;
		this.label = label;
		this.type = type;
		this.urlName = urlName;
		this.values = values;
	}

	/**
	 * Gets the constraints value for this SDKDocumentParameter.
	 * 
	 * @return constraints
	 */
	public it.eng.spagobi.sdk.documents.bo.SDKConstraint[] getConstraints() {
		return constraints;
	}

	/**
	 * Sets the constraints value for this SDKDocumentParameter.
	 * 
	 * @param constraints
	 */
	public void setConstraints(it.eng.spagobi.sdk.documents.bo.SDKConstraint[] constraints) {
		this.constraints = constraints;
	}

	/**
	 * Gets the id value for this SDKDocumentParameter.
	 * 
	 * @return id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id value for this SDKDocumentParameter.
	 * 
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the label value for this SDKDocumentParameter.
	 * 
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label value for this SDKDocumentParameter.
	 * 
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Gets the type value for this SDKDocumentParameter.
	 * 
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type value for this SDKDocumentParameter.
	 * 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the urlName value for this SDKDocumentParameter.
	 * 
	 * @return urlName
	 */
	public String getUrlName() {
		return urlName;
	}

	/**
	 * Sets the urlName value for this SDKDocumentParameter.
	 * 
	 * @param urlName
	 */
	public void setUrlName(String urlName) {
		this.urlName = urlName;
	}

	/**
	 * Gets the values value for this SDKDocumentParameter.
	 * 
	 * @return values
	 */
	public Object[] getValues() {
		return values;
	}

	/**
	 * Sets the values value for this SDKDocumentParameter.
	 * 
	 * @param values
	 */
	public void setValues(Object[] values) {
		this.values = values;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKDocumentParameter))
			return false;
		SDKDocumentParameter other = (SDKDocumentParameter) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true && ((this.constraints == null && other.getConstraints() == null)
				|| (this.constraints != null && java.util.Arrays.equals(this.constraints, other.getConstraints())))
				&& ((this.id == null && other.getId() == null) || (this.id != null && this.id.equals(other.getId())))
				&& ((this.label == null && other.getLabel() == null)
						|| (this.label != null && this.label.equals(other.getLabel())))
				&& ((this.type == null && other.getType() == null)
						|| (this.type != null && this.type.equals(other.getType())))
				&& ((this.urlName == null && other.getUrlName() == null)
						|| (this.urlName != null && this.urlName.equals(other.getUrlName())))
				&& ((this.values == null && other.getValues() == null)
						|| (this.values != null && java.util.Arrays.equals(this.values, other.getValues())));
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
		if (getConstraints() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getConstraints()); i++) {
				Object obj = java.lang.reflect.Array.get(getConstraints(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getId() != null) {
			_hashCode += getId().hashCode();
		}
		if (getLabel() != null) {
			_hashCode += getLabel().hashCode();
		}
		if (getType() != null) {
			_hashCode += getType().hashCode();
		}
		if (getUrlName() != null) {
			_hashCode += getUrlName().hashCode();
		}
		if (getValues() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getValues()); i++) {
				Object obj = java.lang.reflect.Array.get(getValues(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
