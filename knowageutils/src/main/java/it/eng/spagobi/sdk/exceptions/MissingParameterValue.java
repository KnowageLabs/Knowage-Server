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

public class MissingParameterValue extends AbstractSoapException {

	/**
	 *
	 */
	private static final long serialVersionUID = -3646792675049126273L;

	private String parameterName;

	public MissingParameterValue() {
	}

	public MissingParameterValue(String parameterName) {
		this.parameterName = parameterName;
	}

	/**
	 * Gets the parameterName value for this MissingParameterValue.
	 *
	 * @return parameterName
	 */
	public String getParameterName() {
		return parameterName;
	}

	/**
	 * Sets the parameterName value for this MissingParameterValue.
	 *
	 * @param parameterName
	 */
	public void setParameterName(String parameterName) {
		this.parameterName = parameterName;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof MissingParameterValue))
			return false;
		MissingParameterValue other = (MissingParameterValue) obj;
		if (obj == null)
			return false;
		if (this == obj)
			return true;
		if (__equalsCalc != null) {
			return (__equalsCalc == obj);
		}
		__equalsCalc = obj;
		boolean _equals;
		_equals = true && ((this.parameterName == null && other.getParameterName() == null)
				|| (this.parameterName != null && this.parameterName.equals(other.getParameterName())));
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
		if (getParameterName() != null) {
			_hashCode += getParameterName().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
