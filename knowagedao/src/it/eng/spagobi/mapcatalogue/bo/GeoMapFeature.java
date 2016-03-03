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
package it.eng.spagobi.mapcatalogue.bo;

import java.io.Serializable;

/**
 * Defines a value constraint object.
 * 
 * @author giachino
 *
 */


public class GeoMapFeature  implements Serializable   {
	
	/*private SbiGeoMapFeaturesId id;
	private SbiGeoFeatures sbiGeoFeatures;
	private SbiGeoMaps sbiGeoMaps;
	*/
	private int mapId;
	private int featureId;
	private String svgGroup;
	private String visibleFlag;			

	
	
	/*public SbiGeoMapFeaturesId getId() {
		return id;
	}
	public void setId(SbiGeoMapFeaturesId id) {
		this.id = id;
	}
	public SbiGeoFeatures getSbiGeoFeatures() {
		return sbiGeoFeatures;
	}
	public void setSbiGeoFeatures(SbiGeoFeatures sbiGeoFeatures) {
		this.sbiGeoFeatures = sbiGeoFeatures;
	}
	public SbiGeoMaps getSbiGeoMaps() {
		return sbiGeoMaps;
	}
	public void setSbiGeoMaps(SbiGeoMaps sbiGeoMaps) {
		this.sbiGeoMaps = sbiGeoMaps;
	}*/
	
	/**
	 * Gets the svg group.
	 * 
	 * @return the svg group
	 */
	public String getSvgGroup() {
		return svgGroup;
	}
	
	/**
	 * Sets the svg group.
	 * 
	 * @param svgGroup the new svg group
	 */
	public void setSvgGroup(String svgGroup) {
		this.svgGroup = svgGroup;
	}
	
	/**
	 * Gets the visible flag.
	 * 
	 * @return the visible flag
	 */
	public String getVisibleFlag() {
		return visibleFlag;
	}
	
	/**
	 * Sets the visible flag.
	 * 
	 * @param visibleFlag the new visible flag
	 */
	public void setVisibleFlag(String visibleFlag) {
		this.visibleFlag = visibleFlag;
	}
	
	/**
	 * Gets the feature id.
	 * 
	 * @return the feature id
	 */
	public int getFeatureId() {
		return featureId;
	}
	
	/**
	 * Sets the feature id.
	 * 
	 * @param featureId the new feature id
	 */
	public void setFeatureId(int featureId) {
		this.featureId = featureId;
	}
	
	/**
	 * Gets the map id.
	 * 
	 * @return the map id
	 */
	public int getMapId() {
		return mapId;
	}
	
	/**
	 * Sets the map id.
	 * 
	 * @param mapId the new map id
	 */
	public void setMapId(int mapId) {
		this.mapId = mapId;
	}
	
}
