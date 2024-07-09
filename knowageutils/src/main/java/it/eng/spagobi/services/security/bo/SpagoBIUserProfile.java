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

package it.eng.spagobi.services.security.bo;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class SpagoBIUserProfile implements Serializable {

	private static final long serialVersionUID = 4628667082032801018L;

	private final Map<String, Object> attributes = new LinkedHashMap<>();

	private String[] functions;

	private Boolean isSuperadmin;

	private String organization;

	private String[] roles;

	private String uniqueIdentifier;

	private String userId;

	private String userName;

	public SpagoBIUserProfile() {
	}

	public SpagoBIUserProfile(Map<String, Object> attributes, String[] functions, Boolean isSuperadmin,
			String organization, String[] roles, String uniqueIdentifier, String userId, String userName) {
		this.attributes.putAll(attributes);
		this.functions = functions;
		this.isSuperadmin = isSuperadmin;
		this.organization = organization;
		this.roles = roles;
		this.uniqueIdentifier = uniqueIdentifier;
		this.userId = userId;
		this.userName = userName;
	}

	/**
	 * Gets the attributes value for this SpagoBIUserProfile.
	 *
	 * @return attributes
	 */
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes.clear();
		this.attributes.putAll(attributes);
	}

	/**
	 * Gets the functions value for this SpagoBIUserProfile.
	 *
	 * @return functions
	 */
	public String[] getFunctions() {
		return functions;
	}

	/**
	 * Sets the functions value for this SpagoBIUserProfile.
	 *
	 * @param functions
	 */
	public void setFunctions(String[] functions) {
		this.functions = functions;
	}

	/**
	 * Gets the isSuperadmin value for this SpagoBIUserProfile.
	 *
	 * @return isSuperadmin
	 */
	public java.lang.Boolean getIsSuperadmin() {
		return isSuperadmin;
	}

	/**
	 * Sets the isSuperadmin value for this SpagoBIUserProfile.
	 *
	 * @param isSuperadmin
	 */
	public void setIsSuperadmin(java.lang.Boolean isSuperadmin) {
		this.isSuperadmin = isSuperadmin;
	}

	/**
	 * Gets the organization value for this SpagoBIUserProfile.
	 *
	 * @return organization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * Sets the organization value for this SpagoBIUserProfile.
	 *
	 * @param organization
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * Gets the roles value for this SpagoBIUserProfile.
	 *
	 * @return roles
	 */
	public String[] getRoles() {
		return roles;
	}

	/**
	 * Sets the roles value for this SpagoBIUserProfile.
	 *
	 * @param roles
	 */
	public void setRoles(String[] roles) {
		this.roles = roles;
	}

	/**
	 * Gets the uniqueIdentifier value for this SpagoBIUserProfile.
	 *
	 * @return uniqueIdentifier
	 */
	public String getUniqueIdentifier() {
		return uniqueIdentifier;
	}

	/**
	 * Sets the uniqueIdentifier value for this SpagoBIUserProfile.
	 *
	 * @param uniqueIdentifier
	 */
	public void setUniqueIdentifier(String uniqueIdentifier) {
		this.uniqueIdentifier = uniqueIdentifier;
	}

	/**
	 * Gets the userId value for this SpagoBIUserProfile.
	 *
	 * @return userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the userId value for this SpagoBIUserProfile.
	 *
	 * @param userId
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * Gets the userName value for this SpagoBIUserProfile.
	 *
	 * @return userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the userName value for this SpagoBIUserProfile.
	 *
	 * @param userName
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SpagoBIUserProfile))
			return false;
		SpagoBIUserProfile other = (SpagoBIUserProfile) obj;
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
				&& ((this.attributes == null && other.getAttributes() == null)
						|| (this.attributes != null && this.attributes.equals(other.getAttributes())))
				&& ((this.functions == null && other.getFunctions() == null)
						|| (this.functions != null && java.util.Arrays.equals(this.functions, other.getFunctions())))
				&& ((this.isSuperadmin == null && other.getIsSuperadmin() == null)
						|| (this.isSuperadmin != null && this.isSuperadmin.equals(other.getIsSuperadmin())))
				&& ((this.organization == null && other.getOrganization() == null)
						|| (this.organization != null && this.organization.equals(other.getOrganization())))
				&& ((this.roles == null && other.getRoles() == null)
						|| (this.roles != null && java.util.Arrays.equals(this.roles, other.getRoles())))
				&& ((this.uniqueIdentifier == null && other.getUniqueIdentifier() == null)
						|| (this.uniqueIdentifier != null && this.uniqueIdentifier.equals(other.getUniqueIdentifier())))
				&& ((this.userId == null && other.getUserId() == null)
						|| (this.userId != null && this.userId.equals(other.getUserId())))
				&& ((this.userName == null && other.getUserName() == null)
						|| (this.userName != null && this.userName.equals(other.getUserName())));
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
		if (getAttributes() != null) {
			_hashCode += getAttributes().hashCode();
		}
		if (getFunctions() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getFunctions()); i++) {
				Object obj = java.lang.reflect.Array.get(getFunctions(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getIsSuperadmin() != null) {
			_hashCode += getIsSuperadmin().hashCode();
		}
		if (getOrganization() != null) {
			_hashCode += getOrganization().hashCode();
		}
		if (getRoles() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getRoles()); i++) {
				Object obj = java.lang.reflect.Array.get(getRoles(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getUniqueIdentifier() != null) {
			_hashCode += getUniqueIdentifier().hashCode();
		}
		if (getUserId() != null) {
			_hashCode += getUserId().hashCode();
		}
		if (getUserName() != null) {
			_hashCode += getUserName().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
