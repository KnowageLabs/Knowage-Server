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

package it.eng.spagobi.sdk.exceptions;

public class InvalidParameterValue extends AbstractSoapException {

	private static final long serialVersionUID = 6177076647858227596L;

	private String parameterFormat;

	private String parameterName;

	private String parameterType;

	private String wrongParameterValue;

	public InvalidParameterValue() {
	}

	public InvalidParameterValue(String parameterFormat, String parameterName, String parameterType,
			String wrongParameterValue) {
		this.parameterFormat = parameterFormat;
		this.parameterName = parameterName;
		this.parameterType = parameterType;
		this.wrongParameterValue = wrongParameterValue;
	}

	/**
	 * Gets the parameterFormat value for this InvalidParameterValue.
	 * 
	 * @return parameterFormat
	 */
	public String getParameterFormat() {
		return parameterFormat;
	}

	/**
	 * Sets the parameterFormat value for this InvalidParameterValue.
	 * 
	 * @param parameterFormat
	 */
	public void setParameterFormat(String parameterFormat) {
		this.parameterFormat = parameterFormat;
	}

	/**
	 * Gets the parameterName value for this InvalidParameterValue.
	 * 
	 * @return parameterName
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * Sets the parameterName value for this InvalidParameterValue.
	 * 
	 * @param parameterName
	 */
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	/**
	 * Gets the parameterType value for this InvalidParameterValue.
	 * 
	 * @return parameterType
	 */
	public String getParameterType() {
		return parameterType;
	}

	/**
	 * Sets the parameterType value for this InvalidParameterValue.
	 * 
	 * @param parameterType
	 */
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * Gets the wrongParameterValue value for this InvalidParameterValue.
	 * 
	 * @return wrongParameterValue
	 */
	public String getWrongParameterValue() {
		return wrongParameterValue;
	}

	/**
	 * Sets the wrongParameterValue value for this InvalidParameterValue.
	 * 
	 * @param wrongParameterValue
	 */
	public void setWrongParameterValue(String wrongParameterValue) {
		this.wrongParameterValue = wrongParameterValue;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof InvalidParameterValue))
			return false;
		InvalidParameterValue other = (InvalidParameterValue) obj;
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
				&& ((this.parameterFormat == null && other.getParameterFormat() == null)
						|| (this.parameterFormat != null && this.parameterFormat.equals(other.getParameterFormat())))
				&& ((this.parameterName == null && other.getParameterName() == null)
						|| (this.parameterName != null && this.parameterName.equals(other.getParameterName())))
				&& ((this.parameterType == null && other.getParameterType() == null)
						|| (this.parameterType != null && this.parameterType.equals(other.getParameterType())))
				&& ((this.wrongParameterValue == null && other.getWrongParameterValue() == null)
						|| (this.wrongParameterValue != null
								&& this.wrongParameterValue.equals(other.getWrongParameterValue())));
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
		if (getParameterFormat() != null) {
			_hashCode += getParameterFormat().hashCode();
		}
		if (getParameterName() != null) {
			_hashCode += getParameterName().hashCode();
		}
		if (getParameterType() != null) {
			_hashCode += getParameterType().hashCode();
		}
		if (getWrongParameterValue() != null) {
			_hashCode += getWrongParameterValue().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
