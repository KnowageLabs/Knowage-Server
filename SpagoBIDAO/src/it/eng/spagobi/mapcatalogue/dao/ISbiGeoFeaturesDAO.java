/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.mapcatalogue.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.mapcatalogue.bo.GeoFeature;

import java.util.List;

/**
 * @author giachino
 *
 */
public interface ISbiGeoFeaturesDAO extends ISpagoBIDao{
	/**
	 * Loads all detail information for a map identified by its <code>mapID</code>. All these information,
	 * achived by a query to the DB, are stored into an <code>map</code> object, which is
	 * returned.
	 * 
	 * @param featureID The id for the feature to load
	 * @return	A <code>SbiGeoFeatures</code> object containing all loaded information
	 * @throws EMFUserError If an Exception occurred
	 */
	public GeoFeature loadFeatureByID(Integer featureID) throws EMFUserError;
		
	/**
	 * Loads all detail information for feature whose name is equal to <code>name</code>. Each feature
	 * that is added into a <code>SbiGeoFeatures</code> object, which is
	 * returned.
	 * 
	 * @param name The name for the feature to load
	 * @return	An <code>SbiGeoFeatures</code> object containing all loaded information
	 * @throws EMFUserError If an Exception occurred
	 */
	public GeoFeature loadFeatureByName(String name) throws EMFUserError;
	
	/**
	 * Loads all detail information for all features. For each of them, detail 
	 * information is stored into an <code>feature</code> object. After that, all features 
	 * are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all feature objects
	 * @throws EMFUserError If an Exception occurred
	 */	
	public List loadAllFeatures() throws EMFUserError;	
	
	/**
	 * Implements the query to modify a feature. All information needed is stored 
	 * into the input <code>feature</code> object.
	 * 
	 * @param afeature The object containing all modify information
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyFeature(GeoFeature aFeature) throws EMFUserError;
	
	/**
	 * Implements the query to insert a feature. All information needed is stored 
	 * into the input <code>feature</code> object.
	 * 
	 * @param aFeature The object containing all insert information
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertFeature(GeoFeature aFeature) throws EMFUserError;
	
	/**
	 * Implements the query to erase a feature. All information needed is stored 
	 * into the input <code>feature</code> object.
	 * 
	 * @param afeature The object containing all delete information
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public void eraseFeature(GeoFeature aFeature) throws EMFUserError;
	
	
	/**
	 * Tells if a feature is associated to any maps. 
	 * It is useful because a feature cannot be exists without a map,
	 * so if there arent more maps, the feature must to be deleted.
	 *
	 * @param featureId The feature identifier
	 * @return True if the feature is used by one or more 
	 * 		    objects, else false 
	 * @throws EMFUserError If any exception occurred 
	 */
	public boolean hasMapsAssociated (String featureId) throws EMFUserError;
}
