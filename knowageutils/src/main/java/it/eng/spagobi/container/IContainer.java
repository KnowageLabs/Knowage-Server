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
package it.eng.spagobi.container;

import java.util.List;

import org.json.JSONObject;

/**
 * Interface for a generic container (i.e. an object where I can put and retrieve other objects).
 * Objects are stored with a key that is a String.
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public interface IContainer {

	/**
	 * Returns true if no objects are stored into the container with the input key, false otherwise
	 * @param key The input key
	 * @return true if no objects are stored into the container with the input key, false otherwise
	 */
	public boolean isNull(String key);
	
	/**
	 * Returns true if no objects are stored into the container with the input key or if the relevant 
	 * object exists and its string representation is blank, false otherwise
	 * @param key The input key
	 * @return true true if no objects are stored into the container with the input key or if the relevant 
	 * object exists and its string representation is blank, false otherwise
	 */
	public boolean isBlankOrNull(String key);
	
	/**
	 * Return the object with the given key
	 * @param key The input key
	 * @return the object with the given key
	 */
	public Object get(String key);
	
	/**
	 * Returns the string representation of the object with the given key; if the key has no objects associated, null is returned
	 * @param key The input key
	 * @return the string representation of the object with the given key; if the key has no objects associated, null is returned
	 */
	public String getString(String key);
	
	/** 
	 * If the key has no objects associated, null is returned. If a Boolean object is associated to that key, this Boolean is returned.
	 * Otherwise the string representation of the object is parsed with <code>Boolean.parseBoolean(string);<code> and the result is returned.
	 * 
	 * @param key The input key
	 * @return If the key has no objects associated, null is returned. If a Boolean object is associated to that key, this boolean is returned.
	 * Otherwise the string representation of the object is parsed with <code>Boolean.parseBoolean(string);<code> and the result is returned.
	 */
	public Boolean getBoolean(String key);
	
	/** 
	 * If the key has no objects associated, null is returned. If a Integer object is associated to that key, this Integer is returned.
	 * Otherwise the string representation of the object is parsed with <code>Integer.parseInt(string);<code> and the result is returned.
	 * 
	 * @param key The input key
	 * @return If the key has no objects associated, null is returned. If a Integer object is associated to that key, this Integer is returned.
	 * Otherwise the string representation of the object is parsed with <code>Integer.parseInt(string);<code> and the result is returned.
	 */
	public Integer getInteger(String key);
	
	/**
	 * If the key has no objects associated, null is returned. Otherwise the object is casted to List and returned.
	 * 
	 * @param key The input key
	 * @return If the key has no objects associated, null is returned. Otherwise the object is casted to List and returned.
	 */
	public List getList(String key);
	
	public List toCsvList(String key);
	
	public JSONObject toJSONObject(String key);
	
	
	/**
	 * Returns all the string keys of the objects stored into this container
	 * @return all the string keys of the objects stored into this container
	 */
	public List getKeys();
	
	/**
	 * Store an object with the given key inside this container. If the key is null or the object is null, the container is not modified
	 * @param key The input key
	 * @param The object to be stored
	 */
	public void set(String key, Object value);
	
	/**
	 * Removes the object associated to the given key
	 * @param key The input key
	 */
	public void remove(String key);
	
}
