/*
 * SpagoBI, the Open Source Business Intelligence suite
 * © 2005-2015 Engineering Group
 *
 * This file is part of SpagoBI. SpagoBI is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 2.1 of the License, or any later version. 
 * SpagoBI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with SpagoBI. If not, see: http://www.gnu.org/licenses/.
 * The complete text of SpagoBI license is included in the COPYING.LESSER file. 
 */
package org.json;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JSONArray  extends AbstractJSONArray {

	private ArrayNode rootNode;
	
	ArrayNode getWrappedObject() {
    	return this.rootNode;
    }
	
	/**
     * Construct an empty JSONArray.
     */
    public JSONArray() {
    	ObjectMapper mapper = JacksonMapper.getMapper();
		rootNode = mapper.createArrayNode();
    }
    
	/**
     * Construct a JSONArray from a source JSON text.
     * @param source     A string that begins with
     * <code>[</code>&nbsp;<small>(left bracket)</small>
     *  and ends with <code>]</code>&nbsp;<small>(right bracket)</small>.
     *  @throws JSONException If there is a syntax error.
     */
    public JSONArray(String source) throws JSONException {
    	try {
    		ObjectMapper mapper = JacksonMapper.getMapper();
			// Source can be a File, URL, InputStream etc
			rootNode = mapper.readValue(source, ArrayNode.class); 
		} catch (Throwable t) {
			throw new JSONException(t);
		}
    }
    
    /**
     * Construct a JSONArray from a Collection.
     * @param collection     A Collection.
     */
    public JSONArray(Collection collection) {
    	ObjectMapper mapper = JacksonMapper.getMapper();
		rootNode = mapper.createArrayNode();
        if (collection != null) {
            Iterator iter = collection.iterator();
            while (iter.hasNext()) {
            	put(iter.next());
            }
        }
    }


    /**
     * Construct a JSONArray from an array
     * @throws JSONException If not an array.
     */
    public JSONArray(Object array) throws JSONException {
        this();
        if (array.getClass().isArray()) {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i += 1) {
                put(Array.get(array, i));
            }
        } else {
            throw new JSONException("JSONArray initial value should be a string or collection or array.");
        }
    }
    
    public JSONArray(ArrayNode node) throws JSONException {
    	rootNode = node;
    }

   
    
    /**
     * Get the number of elements in the JSONArray, included nulls.
     *
     * @return The length (or size).
     */
    public int length() {
        return rootNode.size();
    }
    
    /**
     * Get the string associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      A string value.
     * @throws JSONException If there is no string value for the index.
     */
    public String getString(int index) throws JSONException {
        Object object = this.get(index);
        if (object instanceof String) {
            return (String)object;
        }
        throw new JSONException("JSONArray[" + index + "] not a string.");
    }


    /**
     * Determine if the value is null.
     * @param index The index must be between 0 and length() - 1.
     * @return true if the value at the index is null, or if there is no value.
     */
    public boolean isNull(int index) {
    	return JSONObject.NULL.equals(this.opt(index));
    }


    /**
     * Get the optional object value associated with an index.
     * @param index The index must be between 0 and length() - 1.
     * @return      An object value, or null if there is no
     *              object at that index.
     */
    public Object opt(int index) {
        if (index < 0 || index >= this.length()) {
        	return null;
        }
         
        try {
			return  JacksonWrapper.unwrap( rootNode.get(index) );
		} catch (JSONException t) {
			throw new RuntimeException(t);
		}
    }


   
    /**
     * Get the optional string associated with an index.
     * The defaultValue is returned if the key is not found.
     *
     * @param index The index must be between 0 and length() - 1.
     * @param defaultValue     The default value.
     * @return      A String value.
     */
    public String optString(int index, String defaultValue) {
        Object object = this.opt(index);
        return JSONObject.NULL.equals(object)? defaultValue : object.toString();
    }


    /**
     * Append a boolean value. This increases the array's length by one.
     *
     * @param value A boolean value.
     * @return this.
     */
    public JSONArray put(boolean value) {
        this.put(value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param value A Collection value.
     * @return      this.
     */
    public JSONArray put(Collection value) {
        this.put(new JSONArray(value));
        return this;
    }


    /**
     * Append a double value. This increases the array's length by one.
     *
     * @param value A double value.
     * @throws JSONException if the value is not finite.
     * @return this.
     */
    public JSONArray put(double value) throws JSONException {
        Double d = new Double(value);
        this.put(d);
        return this;
    }


    /**
     * Append an int value. This increases the array's length by one.
     *
     * @param value An int value.
     * @return this.
     */
    public JSONArray put(int value) {
        this.put(new Integer(value));
        return this;
    }


    /**
     * Append an long value. This increases the array's length by one.
     *
     * @param value A long value.
     * @return this.
     */
    public JSONArray put(long value) {
        this.put(new Long(value));
        return this;
    }


    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONObject which is produced from a Map.
     * @param value A Map value.
     * @return      this.
     * @throws JSONException 
     */
    public JSONArray put(Map value) throws JSONException {
        this.put(new JSONObject(value));
        return this;
    }


    /**
     * Append an object value. This increases the array's length by one.
     * @param value An object value.  The value should be a
     *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *  JSONObject.NULL object.
     * @return this.
     */
    public JSONArray put(Object value) {
    	try {
			Object wrappedValue = JacksonWrapper.wrap(value);
        	if (wrappedValue instanceof Byte) { 
        		rootNode.add((Byte)wrappedValue );
        	} else if (wrappedValue instanceof Character ) {
        		rootNode.add( (Character)wrappedValue );
        	} else if (wrappedValue instanceof Short ) { 
        		rootNode.add( (Short)wrappedValue );
        	} else if(wrappedValue instanceof Integer){
        		rootNode.add( (Integer)wrappedValue );
        	} else if (wrappedValue instanceof Long ) { 
        		rootNode.add( (Long)wrappedValue );
        	} else if (wrappedValue instanceof Boolean){
        		rootNode.add((Boolean)wrappedValue );
        	} else if (wrappedValue instanceof Float) { 
        		rootNode.add( (Float)wrappedValue );
    		} else if(wrappedValue instanceof Double) {
    			rootNode.add( (Double)wrappedValue );
        	} else if (wrappedValue instanceof String) {
        		rootNode.add( (String)wrappedValue );
        	} else if (wrappedValue instanceof JSONObject) {
        		ObjectNode node = ((JSONObject)wrappedValue).getWrappedObject();
        		rootNode.add( node );
        	} else if (wrappedValue instanceof JSONArray) { 
        		ArrayNode node = ((JSONArray)wrappedValue).getWrappedObject();
        		rootNode.add(node);
        	} else if (wrappedValue == NullNode.instance) { 
        		rootNode.add( NullNode.instance );
        	}   
			
		} catch (JSONException t) {
			throw new RuntimeException(t);
		}
		
		return this;
    }


    /**
     * Put or replace a boolean value in the JSONArray. If the index is greater
     * than the length of the JSONArray, then null elements will be added as
     * necessary to pad it out.
     * @param index The subscript.
     * @param value A boolean value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(int index, boolean value) throws JSONException {
        this.put(index, value ? Boolean.TRUE : Boolean.FALSE);
        return this;
    }


    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param index The subscript.
     * @param value A Collection value.
     * @return      this.
     * @throws JSONException If the index is negative or if the value is
     * not finite.
     */
    public JSONArray put(int index, Collection value) throws JSONException {
        this.put(index, new JSONArray(value));
        return this;
    }


    /**
     * Put or replace a double value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.
     * @param index The subscript.
     * @param value A double value.
     * @return this.
     * @throws JSONException If the index is negative or if the value is
     * not finite.
     */
    public JSONArray put(int index, double value) throws JSONException {
        this.put(index, new Double(value));
        return this;
    }


    /**
     * Put or replace an int value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.
     * @param index The subscript.
     * @param value An int value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(int index, int value) throws JSONException {
        this.put(index, new Integer(value));
        return this;
    }


    /**
     * Put or replace a long value. If the index is greater than the length of
     *  the JSONArray, then null elements will be added as necessary to pad
     *  it out.
     * @param index The subscript.
     * @param value A long value.
     * @return this.
     * @throws JSONException If the index is negative.
     */
    public JSONArray put(int index, long value) throws JSONException {
        this.put(index, new Long(value));
        return this;
    }


    /**
     * Put a value in the JSONArray, where the value will be a
     * JSONObject that is produced from a Map.
     * @param index The subscript.
     * @param value The Map value.
     * @return      this.
     * @throws JSONException If the index is negative or if the the value is
     *  an invalid number.
     */
    public JSONArray put(int index, Map value) throws JSONException {
        this.put(index, new JSONObject(value));
        return this;
    }


    /**
     * Put or replace an object value in the JSONArray. If the index is greater
     *  than the length of the JSONArray, then null elements will be added as
     *  necessary to pad it out.
     * @param index The subscript.
     * @param value The value to put into the array. The value should be a
     *  Boolean, Double, Integer, JSONArray, JSONObject, Long, or String, or the
     *  JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the index is negative or if the the value is
     *  an invalid number.
     */
    public JSONArray put(int index, Object value) throws JSONException {
        if (index < 0) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        if (index < this.length()) {
        	Object wrappedValue = JacksonWrapper.wrap(value);
        	if (wrappedValue instanceof Byte) { 
        		rootNode.remove(index);
        		rootNode.insert( index, (Byte)wrappedValue );
        	} else if (wrappedValue instanceof Character ) {
        		rootNode.remove(index);
        		rootNode.insert( index, (Character)wrappedValue );
        	} else if (wrappedValue instanceof Short ) { 
        		rootNode.remove(index);
        		rootNode.insert( index, (Short)wrappedValue );
        	} else if(wrappedValue instanceof Integer){
        		rootNode.remove(index);
        		rootNode.insert( index, (Integer)wrappedValue );
        	} else if (wrappedValue instanceof Long ) { 
        		rootNode.remove(index);
        		rootNode.insert( index, (Long)wrappedValue );
        	} else if (wrappedValue instanceof Boolean){
        		rootNode.remove(index);
        		rootNode.insert( index, (Boolean)wrappedValue );
        	} else if (wrappedValue instanceof Float) { 
        		rootNode.remove(index);
        		rootNode.insert( index, (Float)wrappedValue );
    		} else if(wrappedValue instanceof Double) {
    			rootNode.remove(index);
    			rootNode.insert( index, (Double)wrappedValue );
        	} else if (wrappedValue instanceof String) {
        		rootNode.remove(index);
        		rootNode.insert( index, (String)wrappedValue );
        	} else if (wrappedValue instanceof JSONObject) { 
        		ObjectNode node = ((JSONObject)wrappedValue).getWrappedObject();
        		rootNode.set( index, node );
        	} else if (wrappedValue instanceof JSONArray) { 
        		ArrayNode node = ((JSONArray)wrappedValue).getWrappedObject();
        		rootNode.set( index, node );
        	} else if (wrappedValue == NullNode.instance) { 
        		rootNode.set( index, NullNode.instance );
        	}   
        } else {
            while (index != this.length()) {
            	rootNode.addNull();
            }
            this.put(value);
        }
        return this;
    }


    /**
     * Remove an index and close the hole.
     * @param index The index of the element to be removed.
     * @return The value that was associated with the index,
     * or null if there was no value.
     */
    public Object remove(int index) {
        Object o = this.opt(index);
        rootNode.remove(index);
        return o;
    }

    /**
     * Make a string from the contents of this JSONArray. The
     * <code>separator</code> string is inserted between each element.
     * Warning: This method assumes that the data structure is acyclical.
     * @param separator A string that will be inserted between the elements.
     * @return a string.
     * @throws JSONException If the array contains an invalid number.
     */
    public String join(String separator) throws JSONException {
        int len = this.length();
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < len; i += 1) {
            if (i > 0) {
                sb.append(separator);
            }
            //sb.append(JSONObject.valueToString(this.myArrayList.get(i)));
            sb.append(rootNode.get(i).toString());
        }
        return sb.toString();
    }
    
    /**
     * Make a prettyprinted JSON text of this JSONArray.
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @return a printable, displayable, transmittable
     *  representation of the object, beginning
     *  with <code>[</code>&nbsp;<small>(left bracket)</small> and ending
     *  with <code>]</code>&nbsp;<small>(right bracket)</small>.
     * @throws JSONException
     */
    public String toString(int indentFactor) throws JSONException {
//        StringWriter sw = new StringWriter();
//        synchronized (sw.getBuffer()) {
//            return this.write(sw, indentFactor, 0).toString();
//        }
//    	return null;
    	return rootNode.toString();
    }

}