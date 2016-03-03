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
