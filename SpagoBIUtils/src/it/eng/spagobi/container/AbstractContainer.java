/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Abstract class that implements all <code>it.eng.spagobi.container.IContainer</code> methods apart from get/set/remove/getKeys methods.
 * All other methods are implemented starting from abstract set and get methods. 
 * This class provides implementation for standard objects cast and conversion such as String, Boolean, Integer.
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public abstract class AbstractContainer implements IContainer {

	private static transient Logger logger = Logger.getLogger(AbstractContainer.class);
	
	/**
	 * Returns true if no objects are stored into the container with the input key, false otherwise
	 * @param key The input key
	 * @return true if no objects are stored into the container with the input key, false otherwise
	 */
	public boolean isNull(String key) {
		return get(key) == null;
	}
	
	/**
	 * Returns true if no objects are stored into the container with the input key or if the relevant 
	 * object exists and its string representation is blank, false otherwise
	 * @param key The input key
	 * @return true true if no objects are stored into the container with the input key or if the relevant 
	 * object exists and its string representation is blank, false otherwise
	 */
	public boolean isBlankOrNull(String key) {
		return isNull(key) || get(key).toString().trim().equals("");
	}
	
	/**
	 * Returns the string representation of the object with the given key; if the key has no objects associated, null is returned
	 * @param key The input key
	 * @return the string representation of the object with the given key; if the key has no objects associated, null is returned
	 */
	public String getString(String key) {
		assertNotNull(key, "Input paramater [key] cannot be null");
		if( isNull(key) ) return null;
		return ObjectUtils.toString( get(key) );
	}
	
	/** 
	 * If the key has no objects associated, null is returned. If a Integer object is associated to that key, this Integer is returned.
	 * Otherwise the string representation of the object is parsed with <code>Integer.parseInt(string);<code> and the result is returned.
	 * 
	 * @param key The input key
	 * @return If the key has no objects associated, null is returned. If a Integer object is associated to that key, this Integer is returned.
	 * Otherwise the string representation of the object is parsed with <code>Integer.parseInt(string);<code> and the result is returned.
	 */
	public Integer getInteger(String key) {
		assertNotNull(key, "Input paramater [key] cannot be null");
		if( isNull(key) ) return null;
		return ObjectUtils.toInteger( get(key) );
	}
	
	/** 
	 * If the key has no objects associated, null is returned. If a Boolean object is associated to that key, this Boolean is returned.
	 * Otherwise the string representation of the object is parsed with <code>Boolean.parseBoolean(string);<code> and the result is returned.
	 * 
	 * @param key The input key
	 * @return If the key has no objects associated, null is returned. If a Boolean object is associated to that key, this boolean is returned.
	 * Otherwise the string representation of the object is parsed with <code>Boolean.parseBoolean(string);<code> and the result is returned.
	 */
	public Boolean getBoolean(String key) {
		assertNotNull(key, "Input paramater [key] cannot be null");
		if( isNull(key) ) return null;
		return ObjectUtils.toBoolean( get(key) );
	}
	
	public List toList(String key) {
		assertNotNull(key, "Input paramater [key] cannot be null");
		if( isNull(key) ) return null;
		return ObjectUtils.toList( get(key) );
	}
		
	public List toCsvList(String key) {
		assertNotNull(key, "Input paramater [key] cannot be null");
		if( isNull(key) ) return null;
		return ObjectUtils.toCsvList( get(key) );
	}
	
	public JSONObject toJSONObject(String key) {
		assertNotNull(key, "Input paramater [key] cannot be null");
		if( isNull(key) ) return null;
		return ObjectUtils.toJSONObject( get(key) );
	}
	
	public JSONArray toJSONArray(String key) {
		assertNotNull(key, "Input paramater [key] cannot be null");
		if( isNull(key) ) return null;
		return ObjectUtils.toJSONArray( get(key) );
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * Return the List associated with the input key.
	 * If the key is associated to an object that is not a List instance, a ClassCastException is thrown.
	 * 
	 * @param key The input key
	 * @return the List associated with the input key.
	 */
	public List getList(String key) {
		logger.debug("IN");
		List toReturn = null;
		try {
			Object object = get(key);
			toReturn = (List) object;
			return toReturn; 
		} finally {
			logger.debug("OUT");
		}
	}
	
	
	private static void assertNotNull(Object o, String message) {
		if(o == null) {
			throw new IllegalArgumentException( message );
		}
	}
	
}
