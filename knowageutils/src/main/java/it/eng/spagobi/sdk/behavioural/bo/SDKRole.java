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

public class SDKRole implements java.io.Serializable {
	private String code;

	private String descr;

	private Integer extRoleId;

	private String name;

	private String organization;

	private String roleTypeCd;

	private Integer roleTypeId;

	public SDKRole() {
	}

	public SDKRole(String code, String descr, Integer extRoleId, String name, String organization, String roleTypeCd,
			Integer roleTypeId) {
		this.code = code;
		this.descr = descr;
		this.extRoleId = extRoleId;
		this.name = name;
		this.organization = organization;
		this.roleTypeCd = roleTypeCd;
		this.roleTypeId = roleTypeId;
	}

	/**
	 * Gets the code value for this SDKRole.
	 * 
	 * @return code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code value for this SDKRole.
	 * 
	 * @param code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Gets the descr value for this SDKRole.
	 * 
	 * @return descr
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * Sets the descr value for this SDKRole.
	 * 
	 * @param descr
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * Gets the extRoleId value for this SDKRole.
	 * 
	 * @return extRoleId
	 */
	public Integer getExtRoleId() {
		return extRoleId;
	}

	/**
	 * Sets the extRoleId value for this SDKRole.
	 * 
	 * @param extRoleId
	 */
	public void setExtRoleId(Integer extRoleId) {
		this.extRoleId = extRoleId;
	}

	/**
	 * Gets the name value for this SDKRole.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SDKRole.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the organization value for this SDKRole.
	 * 
	 * @return organization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * Sets the organization value for this SDKRole.
	 * 
	 * @param organization
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * Gets the roleTypeCd value for this SDKRole.
	 * 
	 * @return roleTypeCd
	 */
	public String getRoleTypeCd() {
		return roleTypeCd;
	}

	/**
	 * Sets the roleTypeCd value for this SDKRole.
	 * 
	 * @param roleTypeCd
	 */
	public void setRoleTypeCd(String roleTypeCd) {
		this.roleTypeCd = roleTypeCd;
	}

	/**
	 * Gets the roleTypeId value for this SDKRole.
	 * 
	 * @return roleTypeId
	 */
	public Integer getRoleTypeId() {
		return roleTypeId;
	}

	/**
	 * Sets the roleTypeId value for this SDKRole.
	 * 
	 * @param roleTypeId
	 */
	public void setRoleTypeId(Integer roleTypeId) {
		this.roleTypeId = roleTypeId;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKRole))
			return false;
		SDKRole other = (SDKRole) obj;
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
				&& ((this.code == null && other.getCode() == null)
						|| (this.code != null && this.code.equals(other.getCode())))
				&& ((this.descr == null && other.getDescr() == null)
						|| (this.descr != null && this.descr.equals(other.getDescr())))
				&& ((this.extRoleId == null && other.getExtRoleId() == null)
						|| (this.extRoleId != null && this.extRoleId.equals(other.getExtRoleId())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& ((this.organization == null && other.getOrganization() == null)
						|| (this.organization != null && this.organization.equals(other.getOrganization())))
				&& ((this.roleTypeCd == null && other.getRoleTypeCd() == null)
						|| (this.roleTypeCd != null && this.roleTypeCd.equals(other.getRoleTypeCd())))
				&& ((this.roleTypeId == null && other.getRoleTypeId() == null)
						|| (this.roleTypeId != null && this.roleTypeId.equals(other.getRoleTypeId())));
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
		if (getCode() != null) {
			_hashCode += getCode().hashCode();
		}
		if (getDescr() != null) {
			_hashCode += getDescr().hashCode();
		}
		if (getExtRoleId() != null) {
			_hashCode += getExtRoleId().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		if (getOrganization() != null) {
			_hashCode += getOrganization().hashCode();
		}
		if (getRoleTypeCd() != null) {
			_hashCode += getRoleTypeCd().hashCode();
		}
		if (getRoleTypeId() != null) {
			_hashCode += getRoleTypeId().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
