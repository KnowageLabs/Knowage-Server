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

package it.eng.spagobi.sdk.maps.bo;

public class SDKFeature implements java.io.Serializable {
	private String descr;

	private Integer featureId;

	private String name;

	private String svgGroup;

	private String type;

	private Boolean visibleFlag;

	public SDKFeature() {
	}

	public SDKFeature(String descr, Integer featureId, String name, String svgGroup, String type, Boolean visibleFlag) {
		this.descr = descr;
		this.featureId = featureId;
		this.name = name;
		this.svgGroup = svgGroup;
		this.type = type;
		this.visibleFlag = visibleFlag;
	}

	/**
	 * Gets the descr value for this SDKFeature.
	 * 
	 * @return descr
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * Sets the descr value for this SDKFeature.
	 * 
	 * @param descr
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * Gets the featureId value for this SDKFeature.
	 * 
	 * @return featureId
	 */
	public Integer getFeatureId() {
		return featureId;
	}

	/**
	 * Sets the featureId value for this SDKFeature.
	 * 
	 * @param featureId
	 */
	public void setFeatureId(Integer featureId) {
		this.featureId = featureId;
	}

	/**
	 * Gets the name value for this SDKFeature.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SDKFeature.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the svgGroup value for this SDKFeature.
	 * 
	 * @return svgGroup
	 */
	public String getSvgGroup() {
		return svgGroup;
	}

	/**
	 * Sets the svgGroup value for this SDKFeature.
	 * 
	 * @param svgGroup
	 */
	public void setSvgGroup(String svgGroup) {
		this.svgGroup = svgGroup;
	}

	/**
	 * Gets the type value for this SDKFeature.
	 * 
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type value for this SDKFeature.
	 * 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the visibleFlag value for this SDKFeature.
	 * 
	 * @return visibleFlag
	 */
	public Boolean getVisibleFlag() {
		return visibleFlag;
	}

	/**
	 * Sets the visibleFlag value for this SDKFeature.
	 * 
	 * @param visibleFlag
	 */
	public void setVisibleFlag(Boolean visibleFlag) {
		this.visibleFlag = visibleFlag;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKFeature))
			return false;
		SDKFeature other = (SDKFeature) obj;
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
				&& ((this.descr == null && other.getDescr() == null)
						|| (this.descr != null && this.descr.equals(other.getDescr())))
				&& ((this.featureId == null && other.getFeatureId() == null)
						|| (this.featureId != null && this.featureId.equals(other.getFeatureId())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& ((this.svgGroup == null && other.getSvgGroup() == null)
						|| (this.svgGroup != null && this.svgGroup.equals(other.getSvgGroup())))
				&& ((this.type == null && other.getType() == null)
						|| (this.type != null && this.type.equals(other.getType())))
				&& ((this.visibleFlag == null && other.getVisibleFlag() == null)
						|| (this.visibleFlag != null && this.visibleFlag.equals(other.getVisibleFlag())));
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
		if (getDescr() != null) {
			_hashCode += getDescr().hashCode();
		}
		if (getFeatureId() != null) {
			_hashCode += getFeatureId().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		if (getSvgGroup() != null) {
			_hashCode += getSvgGroup().hashCode();
		}
		if (getType() != null) {
			_hashCode += getType().hashCode();
		}
		if (getVisibleFlag() != null) {
			_hashCode += getVisibleFlag().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
