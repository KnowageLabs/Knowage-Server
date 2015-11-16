/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
