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
package it.eng.spagobi.utilities.callbacks.mapcatalogue;

import it.eng.spagobi.services.content.bo.Content;
import it.eng.spagobi.services.proxy.ContentServiceProxy;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

/**
 * A proxy class used by clients to remotly access the spagoBI EventHandler 
 * interface in a customized way.
 * 
 * @author Gioia
 */
public class MapCatalogueAccessUtils {
	
	
	public static final String GET_STANDARD_HIERARCHY = "getStandardHierarchy";
	public static final String GET_MAPS_BY_FEATURE = "getMapsByFeature";
	public static final String GET_FEATURES_IN_MAP = "getFeaturesInMap";
	public static final String GET_ALL_MAP_NAMES = "getAllMapNames";
	public static final String GET_ALL_FEATURE_NAMES = "getAllFeatureNames";
	public static final String GET_MAP_URL = "getMapUrl";
	public static final String ERROR_PREFIX = "$";
	public static final String ERROR_HIERARCHY_NOT_FOUND = ERROR_PREFIX + "01";
	
	public static final String ERROR_MAP_NOT_FOUND = ERROR_PREFIX + "02";
	public static final String ERROR_FEATURE_NOT_FOUND = ERROR_PREFIX + "03";
	public static final String ERROR_MAP_URL_NOT_FOUND = ERROR_PREFIX + "04";
	
	private HttpSession session = null;
	private String userId = null;
	
	/**
	 * Instantiates a new map catalogue access utils.
	 * 
	 * @param session the session
	 * @param userId the user id
	 */
	public MapCatalogueAccessUtils(HttpSession session, String userId) {
	    this.session = session;
	    this.userId = userId;
	}
	
	
	/**
	 * Gets the standard hierarchy.
	 * 
	 * @return the standard hierarchy
	 * 
	 * @throws Exception the exception
	 */
	public Content readMap(String mapName) throws Exception {
		Content map;
		
		ContentServiceProxy proxy = new ContentServiceProxy(userId, session);
		map = proxy.readMap(mapName);
		if (map == null) {
			throw new Exception("Error while getting map [" + mapName +"] from map catalogue");
		}


		return map;
    }
	
	/**
	 * Gets the standard hierarchy.
	 * 
	 * @return the standard hierarchy
	 * 
	 * @throws Exception the exception
	 */
	public String getStandardHierarchy() throws Exception {

	ContentServiceProxy proxy = new ContentServiceProxy(userId, session);
	String ris = proxy.mapCatalogue( GET_STANDARD_HIERARCHY, null, null, null);
	if (ris == null)
	    throw new Exception("Error while getting default hierarchy");
	if (ris.equalsIgnoreCase(ERROR_HIERARCHY_NOT_FOUND)) {
	    throw new Exception("Default Hierarchy not found. ");
	}
	return ris;
    }
	
	/**
	 * Gets the map names by feature.
	 * 
	 * @param featureName the feature name
	 * 
	 * @return the map names by feature
	 * 
	 * @throws Exception the exception
	 */
	public List getMapNamesByFeature(String featureName) throws Exception {

	ContentServiceProxy proxy = new ContentServiceProxy(userId,session);
	String ris = proxy.mapCatalogue( GET_MAPS_BY_FEATURE, null, featureName, null);
	if (ris == null)
	    throw new Exception("Error while reading maps about feature " + featureName);
	if (ris.equalsIgnoreCase(ERROR_FEATURE_NOT_FOUND)) {
	    throw new Exception("Error while reading maps about feature " + featureName);
	}

	if (ris.equalsIgnoreCase(ERROR_MAP_NOT_FOUND)) {
	    throw new Exception("Maps about " + featureName + " not found. ");
	}

	String[] maps = ris.split(",");
	List mapList = new ArrayList();
	for (int i = 0; i < maps.length; i++) {
	    mapList.add(maps[i]);
	}

	return mapList;
    }

	/**
	 * Gets the feature names in map.
	 * 
	 * @param mapName the map name
	 * 
	 * @return the feature names in map
	 * 
	 * @throws Exception the exception
	 */
	public List getFeatureNamesInMap(String mapName) throws Exception {

	ContentServiceProxy proxy = new ContentServiceProxy(userId,session);
	String ris = proxy.mapCatalogue( GET_FEATURES_IN_MAP, null, null, mapName);

	if (ris == null)
	    throw new Exception("Error while reading features about map " + mapName);

	if (ris.startsWith(ERROR_FEATURE_NOT_FOUND)) {
	    throw new Exception("Features about " + mapName + " not found. ");
	}

	String[] features = ris.split(",");
	List featureList = new ArrayList();
	for (int i = 0; i < features.length; i++) {
	    featureList.add(features[i]);
	}

	return featureList;
    }
	
	/**
	 * Gets the map url.
	 * 
	 * @param mapName the map name
	 * 
	 * @return the map url
	 * 
	 * @throws Exception the exception
	 */
	public String getMapUrl(String mapName) throws Exception {

	ContentServiceProxy proxy = new ContentServiceProxy(userId,session);
	String ris = proxy.mapCatalogue(GET_MAP_URL, null, null, mapName);

	if (ris == null)
	    throw new Exception("Error while reading map url " + mapName);

	if (ris.startsWith(ERROR_FEATURE_NOT_FOUND)) {
	    throw new Exception("Map about " + mapName + " not found. ");
	}
	return ris;
    }
	
	/**
	 * Gets the all map names.
	 * 
	 * @return the all map names
	 * 
	 * @throws Exception the exception
	 */
	public List getAllMapNames() throws Exception {

	ContentServiceProxy proxy = new ContentServiceProxy(userId,session);
	String ris = proxy.mapCatalogue( GET_ALL_MAP_NAMES, null, null, null);

	if (ris == null)
	    throw new Exception("Error while reading maps ");

	if (ris.equalsIgnoreCase(ERROR_MAP_NOT_FOUND)) {
	    throw new Exception("Maps not found. ");
	}
	String[] maps = ris.split(",");
	List mapList = new ArrayList();
	for (int i = 0; i < maps.length; i++) {
	    mapList.add(maps[i]);
	}

	return mapList;
    }
	
	/**
	 * Gets the all feature names.
	 * 
	 * @return the all feature names
	 * 
	 * @throws Exception the exception
	 */
	public List getAllFeatureNames() throws Exception {
	ContentServiceProxy proxy = new ContentServiceProxy(userId,session);
	String ris = proxy.mapCatalogue( GET_ALL_FEATURE_NAMES, null, null, null);
	if (ris == null)
	    throw new Exception("Error while reading Features ");

	if (ris.equalsIgnoreCase(ERROR_FEATURE_NOT_FOUND)) {
	    throw new Exception("Feature not found. ");
	}
	String[] maps = ris.split(",");
	List mapList = new ArrayList();
	for (int i = 0; i < maps.length; i++) {
	    mapList.add(maps[i]);
	}

	return mapList;
    }
}
