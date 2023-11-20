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

public class SDKMap implements java.io.Serializable {
	private Integer binId;

	private String descr;

	private String format;

	private Integer mapId;

	private String name;

	private it.eng.spagobi.sdk.maps.bo.SDKFeature[] sdkFeatures;

	private String url;

	public SDKMap() {
	}

	public SDKMap(Integer binId, String descr, String format, Integer mapId, String name,
			it.eng.spagobi.sdk.maps.bo.SDKFeature[] sdkFeatures, String url) {
		this.binId = binId;
		this.descr = descr;
		this.format = format;
		this.mapId = mapId;
		this.name = name;
		this.sdkFeatures = sdkFeatures;
		this.url = url;
	}

	/**
	 * Gets the binId value for this SDKMap.
	 *
	 * @return binId
	 */
	public Integer getBinId() {
		return binId;
	}

	/**
	 * Sets the binId value for this SDKMap.
	 *
	 * @param binId
	 */
	public void setBinId(Integer binId) {
		this.binId = binId;
	}

	/**
	 * Gets the descr value for this SDKMap.
	 *
	 * @return descr
	 */
	public String getDescr() {
		return descr;
	}

	/**
	 * Sets the descr value for this SDKMap.
	 *
	 * @param descr
	 */
	public void setDescr(String descr) {
		this.descr = descr;
	}

	/**
	 * Gets the format value for this SDKMap.
	 *
	 * @return format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * Sets the format value for this SDKMap.
	 *
	 * @param format
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	/**
	 * Gets the mapId value for this SDKMap.
	 *
	 * @return mapId
	 */
	public Integer getMapId() {
		return mapId;
	}

	/**
	 * Sets the mapId value for this SDKMap.
	 *
	 * @param mapId
	 */
	public void setMapId(Integer mapId) {
		this.mapId = mapId;
	}

	/**
	 * Gets the name value for this SDKMap.
	 *
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name value for this SDKMap.
	 *
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the sdkFeatures value for this SDKMap.
	 *
	 * @return sdkFeatures
	 */
	public it.eng.spagobi.sdk.maps.bo.SDKFeature[] getSdkFeatures() {
		return sdkFeatures;
	}

	/**
	 * Sets the sdkFeatures value for this SDKMap.
	 *
	 * @param sdkFeatures
	 */
	public void setSdkFeatures(it.eng.spagobi.sdk.maps.bo.SDKFeature[] sdkFeatures) {
		this.sdkFeatures = sdkFeatures;
	}

	/**
	 * Gets the url value for this SDKMap.
	 *
	 * @return url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url value for this SDKMap.
	 *
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	private Object __equalsCalc = null;

	@Override
	public synchronized boolean equals(Object obj) {
		if (!(obj instanceof SDKMap))
			return false;
		SDKMap other = (SDKMap) obj;
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
				&& ((this.binId == null && other.getBinId() == null)
						|| (this.binId != null && this.binId.equals(other.getBinId())))
				&& ((this.descr == null && other.getDescr() == null)
						|| (this.descr != null && this.descr.equals(other.getDescr())))
				&& ((this.format == null && other.getFormat() == null)
						|| (this.format != null && this.format.equals(other.getFormat())))
				&& ((this.mapId == null && other.getMapId() == null)
						|| (this.mapId != null && this.mapId.equals(other.getMapId())))
				&& ((this.name == null && other.getName() == null)
						|| (this.name != null && this.name.equals(other.getName())))
				&& ((this.sdkFeatures == null && other.getSdkFeatures() == null) || (this.sdkFeatures != null
						&& java.util.Arrays.equals(this.sdkFeatures, other.getSdkFeatures())))
				&& ((this.url == null && other.getUrl() == null)
						|| (this.url != null && this.url.equals(other.getUrl())));
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
		if (getBinId() != null) {
			_hashCode += getBinId().hashCode();
		}
		if (getDescr() != null) {
			_hashCode += getDescr().hashCode();
		}
		if (getFormat() != null) {
			_hashCode += getFormat().hashCode();
		}
		if (getMapId() != null) {
			_hashCode += getMapId().hashCode();
		}
		if (getName() != null) {
			_hashCode += getName().hashCode();
		}
		if (getSdkFeatures() != null) {
			for (int i = 0; i < java.lang.reflect.Array.getLength(getSdkFeatures()); i++) {
				Object obj = java.lang.reflect.Array.get(getSdkFeatures(), i);
				if (obj != null && !obj.getClass().isArray()) {
					_hashCode += obj.hashCode();
				}
			}
		}
		if (getUrl() != null) {
			_hashCode += getUrl().hashCode();
		}
		__hashCodeCalc = false;
		return _hashCode;
	}

}
