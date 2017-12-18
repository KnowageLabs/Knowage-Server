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

import it.eng.spagobi.mapcatalogue.metadata.SbiGeoFeatures;
import it.eng.spagobi.services.dataset.bo.SpagoBiDataSet;

import java.io.Serializable;

/**
 * Defines a value constraint object.
 * 
 * @author giachino
 *
 */


public class GeoFeature  implements Serializable   {
	
	private int featureId;
	private String name;
	private String descr;
	private String type;	
	//private List biMaps = null;
	
	/**
	 * Gets the descr.
	 * 
	 * @return the descr
	 */
	public String getDescr() {
		return descr;
	}
	
	/**
	 * Sets the descr.
	 * 
	 * @param descr the new descr
	 */
	public void setDescr(String descr) {
		this.descr = descr;
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
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name.
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Sets the type.
	 * 
	 * @param type the new type
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/*
	public List getBiMaps() {
		return biMaps;
	}
	public void setBiMaps(List biMaps) {
		this.biMaps = biMaps;
	}
	*/
	
	public SbiGeoFeatures toSpagoBiGeoFeatures() {
		SbiGeoFeatures sbgf = new SbiGeoFeatures();
		sbgf.setFeatureId(getFeatureId());
		sbgf.setName(getName());
		sbgf.setDescr(getDescr());
		sbgf.setType(getType());
		return sbgf;
	}


}
