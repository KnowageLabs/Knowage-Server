/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.mapcatalogue.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.mapcatalogue.bo.GeoMapFeature;

import java.util.List;

/**
 * @author giachino
 *
 */
public interface ISbiGeoMapFeaturesDAO extends ISpagoBIDao{
	/**
	 * Loads all detail information for all features compatible to the map specified
	 * at input. For each of them, name is stored into a <code>String</code> object. 
	 * After that, all names are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all feature objects compatible with the map passed at input
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadFeatureNamesByMapId(Integer mapId) throws EMFUserError;
		
	/**
	 * Loads all detail information for all features compatible to the map specified
	 * at input. For each of them, detail information is stored into an <code>GeoFeature</code> object. 
	 * After that, all features are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all feature objects compatible with the map passed at input
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadFeaturesByMapId(Integer mapId) throws EMFUserError;
	/**
	 * Loads all detail information for all maps compatible to the feature specified
	 * at input. For each of them, name information is stored into a <code>String</code> object. 
	 * After that, all names are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all map objects compatible with the feature passed at input
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadMapNamesByFeatureId(Integer featureId) throws EMFUserError;	
	
	/**
	 * Loads all detail information for all maps compatible to the feature specified
	 * at input. For each of them, detail information is stored into an <code>GeoMap</code> object. 
	 * After that, all maps are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all map objects compatible with the feature passed at input
	 * @throws EMFUserError If an Exception occurred
	 */
	public List loadMapsByFeatureId(Integer featureId) throws EMFUserError;	
	
	/**
	 * Loads the list of MapFeatures associated to the input 
	 * <code>mapId</code> and <code>featureId</code>. All these information,
	 * achived by a query to the DB, are stored into a List of <code>SbiGeoMapFeatures</code> object, 
	 * which is returned.
	 * 
	 * @param mapId The id for the map to load
	 * @param featureId The feature id for the feature to load
	 * @return	A List of <code>SbiGeoMapFeature</code> object containing all loaded information
	 * @throws	EMFUserError If an Exception occurred
	 */
	public GeoMapFeature loadMapFeatures(Integer mapId, Integer featureId) throws EMFUserError;

	/**
	 * Implements the query to modify a MapFeature. All information needed is stored 
	 * into the input <code>SbiGeoMapFeatures</code> object.
	 * 
	 * @param aSbiGeoMapFeatures The SbiGeoMapFeatures containing all modify information
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyMapFeatures(GeoMapFeature aGeoMapFeature) throws EMFUserError;

	/**
	 * Implements the query to insert a MapFeature. All information needed is stored 
	 * into the input <code>SbiGeoMapFeatures</code> object.
	 * 
	 * @param aSbiGeoMapFeatures The SbiGeoMapFeatures containing all insert information
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertMapFeatures(GeoMapFeature aGeoMapFeature) throws EMFUserError;

	/**
	 * Implements the query to erase a SbiGeoMapFeatures. All information needed is stored 
	 * into the input <code>SbiGeoMapFeatures</code> object.
	 * 
	 * @param aSbiGeoMapFeatures The object containing all delete information
	 * @throws EMFUserError If an Exception occurred
	 */
	public void eraseMapFeatures(GeoMapFeature aGeoMapFeature) throws EMFUserError;	
	
}
