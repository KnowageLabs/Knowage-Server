/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.mapcatalogue.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.mapcatalogue.bo.GeoMap;

import java.util.List;

/**
 * @author giachino
 *
 */
public interface ISbiGeoMapsDAO extends ISpagoBIDao{
	/**
	 * Loads all detail information for a map identified by its <code>mapID</code>. All these information,
	 * achived by a query to the DB, are stored into an <code>map</code> object, which is
	 * returned.
	 * 
	 * @param mapID The id for the map to load
	 * @return	An <code>map</code> object containing all loaded information
	 * @throws EMFUserError If an Exception occurred
	 */
	public GeoMap loadMapByID(Integer mapID) throws EMFUserError;
	
	/**
	 * Loads all detail information for maps whose name is equal to <code>name</code>. Each map
	 * thatis added into a <code>List</code> object, which is
	 * returned.
	 * 
	 * @param name The name for the map to load
	 * @return	An <code>List</code> list of objects containing all loaded information
	 * @throws EMFUserError If an Exception occurred
	 */
	//public List loadMapByName(String name) throws EMFUserError;
	
	/**
	 * Loads all detail information for maps whose name is equal to <code>name</code>. 
	 * 
	 * @param name The name for the map to load
	 * @return	An <code>map</code> object containing all loaded information
	 * @throws EMFUserError If an Exception occurred
	 */
	public GeoMap loadMapByName(String name) throws EMFUserError;
		
	/**
	 * Loads all detail information for all maps. For each of them, detail 
	 * information is stored into an <code>map</code> object. After that, all maps 
	 * are stored into a <code>List</code>, which is returned.
	 * 
	 * @return A list containing all map objects
	 * @throws EMFUserError If an Exception occurred
	 */	
	public List loadAllMaps() throws EMFUserError;	
	
	/**
	 * Implements the query to modify a map. All information needed is stored 
	 * into the input <code>map</code> object.
	 * 
	 * @param aMap The object containing all modify information
	 * @param content the content of svg file
	 * @throws EMFUserError If an Exception occurred
	 */
	public void modifyMap(GeoMap aMap, byte[] content) throws EMFUserError;
	
	/**
	 * Implements the query to insert a map. All information needed is stored 
	 * into the input <code>map</code> object.
	 * 
	 * @param aMap The object containing all insert information
	 * @param content the content of svg file 
	 * @throws EMFUserError If an Exception occurred
	 */
	public void insertMap(GeoMap aMap, byte[] content) throws EMFUserError;
	
	/**
	 * Implements the query to erase a map. All information needed is stored 
	 * into the input <code>map</code> object.
	 * 
	 * @param aMap The object containing all delete information
	 * @throws EMFUserError If an Exception occurred
	 */
	
	public void eraseMap(GeoMap aMap) throws EMFUserError;

	
	/**
	 * Tells if a map is associated to any Features. 
	 * It is useful because a map cannot be deleted
	 * if it is used by one or more BI Features.
	 *
	 * @param mapId The map identifier
	 * @return True if the map is used by one or more 
	 * 		    objects, else false 
	 * @throws EMFUserError If any exception occurred 
	 */
	public boolean hasFeaturesAssociated (String mapId) throws EMFUserError;
	
	/**
	 * Gets the features (tag <g>) from the SVG File.
	 * @param content The content file
	 * @throws Exception raised If there are some problems
	 */ 
	public List getFeaturesFromSVG(byte[] content) throws Exception ;

}
