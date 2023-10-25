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

package it.eng.spagobi.sdk.domains.bo;

public class SDKDomain implements java.io.Serializable {
	private String domainCd;

	private String domainNm;

	private String valueCd;

	private String valueDs;

	private Integer valueId;

	private String valueNm;

	public SDKDomain() {
	}

	public SDKDomain(String domainCd, String domainNm, String valueCd, String valueDs, Integer valueId,
			String valueNm) {
		this.domainCd = domainCd;
		this.domainNm = domainNm;
		this.valueCd = valueCd;
		this.valueDs = valueDs;
		this.valueId = valueId;
		this.valueNm = valueNm;
	}

	/**
	 * Gets the domainCd value for this SDKDomain.
	 *
	 * @return domainCd
	 */
	public String getDomainCd() {
		return domainCd;
	}

	/**
	 * Sets the domainCd value for this SDKDomain.
	 *
	 * @param domainCd
	 */
	public void setDomainCd(String domainCd) {
		this.domainCd = domainCd;
	}

	/**
	 * Gets the domainNm value for this SDKDomain.
	 *
	 * @return domainNm
	 */
	public String getDomainNm() {
		return domainNm;
	}

	/**
	 * Sets the domainNm value for this SDKDomain.
	 *
	 * @param domainNm
	 */
	public void setDomainNm(String domainNm) {
		this.domainNm = domainNm;
	}

	/**
	 * Gets the valueCd value for this SDKDomain.
	 *
	 * @return valueCd
	 */
	public String getValueCd() {
		return valueCd;
	}

	/**
	 * Sets the valueCd value for this SDKDomain.
	 *
	 * @param valueCd
	 */
	public void setValueCd(String valueCd) {
		this.valueCd = valueCd;
	}

	/**
	 * Gets the valueDs value for this SDKDomain.
	 *
	 * @return valueDs
	 */
	public String getValueDs() {
		return valueDs;
	}

	/**
	 * Sets the valueDs value for this SDKDomain.
	 *
	 * @param valueDs
	 */
	public void setValueDs(String valueDs) {
		this.valueDs = valueDs;
	}

	/**
	 * Gets the valueId value for this SDKDomain.
	 *
	 * @return valueId
	 */
	public Integer getValueId() {
		return valueId;
	}

	/**
	 * Sets the valueId value for this SDKDomain.
	 *
	 * @param valueId
	 */
	public void setValueId(Integer valueId) {
		this.valueId = valueId;
	}

	/**
	 * Gets the valueNm value for this SDKDomain.
	 *
	 * @return valueNm
	 */
	public String getValueNm() {
		return valueNm;
	}

	/**
	 * Sets the valueNm value for this SDKDomain.
	 *
	 * @param valueNm
	 */
	public void setValueNm(String valueNm) {
		this.valueNm = valueNm;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKDomain))
			return false;
		SDKDomain other = (SDKDomain) obj;
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
				&& ((this.domainCd == null && other.getDomainCd() == null)
						|| (this.domainCd != null && this.domainCd.equals(other.getDomainCd())))
				&& ((this.domainNm == null && other.getDomainNm() == null)
						|| (this.domainNm != null && this.domainNm.equals(other.getDomainNm())))
				&& ((this.valueCd == null && other.getValueCd() == null)
						|| (this.valueCd != null && this.valueCd.equals(other.getValueCd())))
				&& ((this.valueDs == null && other.getValueDs() == null)
						|| (this.valueDs != null && this.valueDs.equals(other.getValueDs())))
				&& ((this.valueId == null && other.getValueId() == null)
						|| (this.valueId != null && this.valueId.equals(other.getValueId())))
				&& ((this.valueNm == null && other.getValueNm() == null)
						|| (this.valueNm != null && this.valueNm.equals(other.getValueNm())));
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
		if (getDomainCd() != null) {
			_hashCode += getDomainCd().hashCode();
		}
		if (getDomainNm() != null) {
			_hashCode += getDomainNm().hashCode();
		}
		if (getValueCd() != null) {
			_hashCode += getValueCd().hashCode();
		}
		if (getValueDs() != null) {
			_hashCode += getValueDs().hashCode();
		}
		if (getValueId() != null) {
			_hashCode += getValueId().hashCode();
		}
		if (getValueNm() != null) {
			_hashCode += getValueNm().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
