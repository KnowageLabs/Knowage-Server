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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;



/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class JSONObject extends AbstractJSONObject {
	
	private ObjectNode rootNode;
	
	ObjectNode getWrappedObject() {
    	return this.rootNode;
    }
	
    /**
     * JSONObject.NULL is equivalent to the value that JavaScript calls null,
     * whilst Java's null is equivalent to the value that JavaScript calls
     * undefined.
     */
     private static final class Null {

        /**
         * There is only intended to be a single instance of the NULL object,
         * so the clone method returns itself.
         * @return     NULL.
         */
        protected final Object clone() {
            return this;
        }

        /**
         * A Null object is equal to the null value and to itself.
         * @param object    An object to test for nullness.
         * @return true if the object parameter is the JSONObject.NULL object
         *  or null.
         */
        public boolean equals(Object object) {
            return object == null || object == this;
        }

        /**
         * Get the "null" string value.
         * @return The string "null".
         */
        public String toString() {
            return "null";
        }
    }
     
     /**
      * It is sometimes more convenient and less ambiguous to have a
      * <code>NULL</code> object than to use Java's <code>null</code> value.
      * <code>JSONObject.NULL.equals(null)</code> returns <code>true</code>.
      * <code>JSONObject.NULL.toString()</code> returns <code>"null"</code>.
      */
     public static final Object NULL = new Null();
	
	/**
	 * Construct an empty JSONObject.
	 */
	public JSONObject() {
		ObjectMapper mapper = JacksonMapper.getMapper();
		rootNode = mapper.createObjectNode(); // will be of type ObjectNode
	}

	/**
	 * Construct a JSONObject from a source JSON text string. This is the most
	 * commonly used JSONObject constructor.
	 * 
	 * @param source
	 *            A string beginning with <code>{</code>&nbsp;<small>(left
	 *            brace)</small> and ending with <code>}</code>
	 *            &nbsp;<small>(right brace)</small>.
	 * @exception JSONException
	 *                If there is a syntax error in the source string or a
	 *                duplicated key.
	 */
	public JSONObject(String source) throws JSONException {
		try {
			ObjectMapper mapper = JacksonMapper.getMapper();
			// Source can be a File, URL, InputStream etc
			rootNode = mapper.readValue(source, ObjectNode.class); 
		} catch (Throwable t) {
			throw new JSONException(t);
		}
	}
	
	public JSONObject(Map map) throws JSONException  {
		this();
		Set keys = map.keySet();
		for(Object key : keys) {
			this.put((String)key, map.get(key));
		}
	}
	
	/**
     * Construct a JSONObject from an Object using bean getters.
     * It reflects on all of the public methods of the object.
     * For each of the methods with no parameters and a name starting
     * with <code>"get"</code> or <code>"is"</code> followed by an uppercase letter,
     * the method is invoked, and a key and the value returned from the getter method
     * are put into the new JSONObject.
     *
     * The key is formed by removing the <code>"get"</code> or <code>"is"</code> prefix.
     * If the second remaining character is not upper case, then the first
     * character is converted to lower case.
     *
     * For example, if an object has a method named <code>"getName"</code>, and
     * if the result of calling <code>object.getName()</code> is <code>"Larry Fine"</code>,
     * then the JSONObject will contain <code>"name": "Larry Fine"</code>.
     *
     * @param bean An object that has getter methods that should be used
     * to make a JSONObject.
     */
    public JSONObject(Object bean) {
        this();
        this.populate(bean);	
    }
    
    
    JSONObject(ObjectNode node) throws JSONException {
    	rootNode = node;
    }
	
    
    /**
     * Wrap an object, if necessary. If the object is null, return the NULL
     * object. If it is an array or collection, wrap it in a JSONArray. If
     * it is a map, wrap it in a JSONObject. If it is a standard property
     * (Double, String, et al) then it is already wrapped. Otherwise, if it
     * comes from one of the java packages, turn it into a string. And if
     * it doesn't, try to wrap it in a JSONObject. If the wrapping fails,
     * then null is returned.
     *
     * @param object The object to wrap
     * @return The wrapped value
     * @throws JSONException 
     */
    public static Object wrap(Object object) throws JSONException {
        Object wrappedObject = null;
    	try {
            if (object == null) {
                return NULL;
            }
            
            if (object instanceof JSONObject || object instanceof JSONArray  ||
                    NULL.equals(object)      || 
                    object instanceof Byte   || object instanceof Character  ||
                    object instanceof Short  || object instanceof Integer    ||
                    object instanceof Long   || object instanceof Boolean    ||
                    object instanceof Float  || object instanceof Double     ||
                    object instanceof String) {
            	
            	wrappedObject = object;
            
            } else if(object instanceof JSONObject || object instanceof JSONArray) {
            
            	wrappedObject = object;
            
            } else if (object instanceof Collection) {
            	
            	wrappedObject = new JSONArray((Collection)object);
            
            } else if (object.getClass().isArray()) {
            	
            	wrappedObject = new JSONArray(object);
            
            } else if (object instanceof Map) {
            	
            	wrappedObject = new JSONObject((Map)object);
            
            } else {
            	 throw new JSONException("Unsupported value type [" + object.getClass().getName() + "]");
            }
            
            return wrappedObject;
        
    	} catch(Throwable t) {
            if(t instanceof JSONException) throw (JSONException)t;
            throw new JSONException("An unexpected error occured while wrapping value [" + object + "] of type [" + (object!=null?object.getClass().getName(): "null") + "]: " + t.getMessage());
        }
    }

    
	/**
     * Accumulate values under a key. It is similar to the put method except
     * that if there is already an object stored under the key then a
     * JSONArray is stored under the key to hold all of the accumulated values.
     * If there is already a JSONArray, then the new value is appended to it.
     * In contrast, the put method replaces the previous value.
     *
     * If only one value is accumulated that is not a JSONArray, then the
     * result will be the same as using put. But if multiple values are
     * accumulated, then the result will be like append.
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws JSONException If the value is an invalid number
     *  or if the key is null.
     */
    public JSONObject accumulate(
        String key,
        Object value
    ) throws JSONException {
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, value instanceof JSONArray
                    ? new JSONArray().put(value)
                    : value);
        } else if (object instanceof JSONArray) {
            ((JSONArray)object).put(value);
        } else {
            this.put(key, new JSONArray().put(object).put(value));
        }
        return this;
    }
    
    private void populate(Object bean) {
        Class klass = bean.getClass();

        // If klass is a System class then set includeSuperClass to false.
        boolean includeSuperClass = klass.getClassLoader() != null;

        Method[] methods = includeSuperClass
                ? klass.getMethods()
                : klass.getDeclaredMethods();

        for (int i = 0; i < methods.length; i += 1) {
            try {
                Method method = methods[i];
                if (Modifier.isPublic(method.getModifiers())) {
                    String name = method.getName();
                    String key = "";
                    if (name.startsWith("get")) {
                        if ("getClass".equals(name) ||
                                "getDeclaringClass".equals(name)) {
                            key = "";
                        } else {
                            key = name.substring(3);
                        }
                    } else if (name.startsWith("is")) {
                        key = name.substring(2);
                    }
                    if (key.length() > 0 &&
                            Character.isUpperCase(key.charAt(0)) &&
                            method.getParameterTypes().length == 0) {
                        if (key.length() == 1) {
                            key = key.toLowerCase();
                        } else if (!Character.isUpperCase(key.charAt(1))) {
                            key = key.substring(0, 1).toLowerCase() +
                                key.substring(1);
                        }

                        Object result = method.invoke(bean, (Object[])null);
                        if (result != null) {
                            put(key, result);
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
    }
    
    /**
     * Append values to the array under a key. If the key does not exist in the
     * JSONObject, then the key is put in the JSONObject with its value being a
     * JSONArray containing the value parameter. If the key was already
     * associated with a JSONArray, then the value parameter is appended to it.
     * @param key   A key string.
     * @param value An object to be accumulated under the key.
     * @return this.
     * @throws JSONException If the key is null or if the current value
     *  associated with the key is not a JSONArray.
     */
    public JSONObject append(String key, Object value) throws JSONException {
        Object object = this.opt(key);
        if (object == null) {
            this.put(key, new JSONArray().put(value));
        } else if (object instanceof JSONArray) {
            this.put(key, ((JSONArray)object).put(value));
        } else {
            throw new JSONException("JSONObject[" + key +
                    "] is not a JSONArray.");
        }
        return this;
    }
	
    /**
     * Determine if the value associated with the key is null or if there is
     *  no value.
     * @param key   A key string.
     * @return      true if there is no value associated with the key or if
     *  the value is the JSONObject.NULL object.
     */
    public boolean isNull(String key) {
        return NULL.equals(this.opt(key));
    }
    
	/**
     * Get an enumeration of the keys of the JSONObject.
     *
     * @return An iterator of the keys.
     */
    public Iterator<String> keys() {
        return rootNode.fieldNames();
    }
    
    /**
    * Get the number of keys stored in the JSONObject.
    *
    * @return The number of keys in the JSONObject.
    */
   public int length() { 
	   return rootNode.size();
   }
    
    /**
     * Get an array of field names from a JSONObject.
     *
     * @return An array of field names, or null if there are no names.
     */
    public static String[] getNames(JSONObject jo) {
        int length = jo.length();
        if (length == 0) {
            return null;
        }
        Iterator iterator = jo.keys();
        String[] names = new String[length];
        int i = 0;
        while (iterator.hasNext()) {
            names[i] = (String)iterator.next();
            i += 1;
        }
        return names;
    }
    
    

    /**
     * Determine if the JSONObject contains a specific key.
     * @param key   A key string.
     * @return      true if the key exists in the JSONObject.
     */
    public boolean has(String key) {
    	return !(opt(key) == null);
    }

    /**
     * Get an optional value associated with a key.
     * @param key   A key string.
     * @return      An object which is the value, or null if there is no value.
     */
    public Object opt(String key) {
    	Object value = null;
    	
    	if(key == null) return null;
    	
    	try {
	    	JsonNode node = rootNode.get(key);
	    	if(node != null) {
	    		if(node instanceof TextNode) {
	    			value = ((TextNode)node).asText();
	    		} else if(node instanceof IntNode) {	
	    			int v = ((IntNode)node).asInt();
	    			value = new Integer(v);
	    		} else if(node instanceof LongNode) {	
	    			long v = ((LongNode)node).asLong();
	    			value = new Long(v);
	    		} else if(node instanceof BigIntegerNode) {	
	    			value = ((BigIntegerNode)node).bigIntegerValue();
	    		} else if(node instanceof DoubleNode) {	
	    			double v = ((DoubleNode)node).asDouble();
	    			value = new Double(v);
	    		} else if(node instanceof BooleanNode) {	
	    			boolean v = ((BooleanNode)node).asBoolean();
	    			value = new Boolean(v);
	    		} else if(node instanceof NullNode) {
	    			value = JSONObject.NULL;
	    		} else if(node instanceof ArrayNode) {
	    			ArrayNode v = ((ArrayNode)node);
	    			value = new JSONArray(v);
	    		} else if(node instanceof ObjectNode) {
	    			ObjectNode v = ((ObjectNode)node);
	    			value = new JSONObject(v);
	    		} else {
	    			System.out.println(node.getClass().getName());
	    		}
	    	}
    	} catch(Throwable t) {
    		throw new RuntimeException("Impossible to load property  [" + key + "] from object [" + rootNode + "]: " + t.getMessage(), t);
    	}
    	
        return key == null ? null : value;
    }

    /**
     * Get an optional string associated with a key.
     * It returns the defaultValue if there is no such key.
     *
     * @param key   A key string.
     * @param defaultValue     The default.
     * @return      A string which is the value.
     */
    public String optString(String key, String defaultValue) {
        Object object = this.opt(key);
        return NULL.equals(object) ? defaultValue : object.toString();
    }

    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONArray which is produced from a Collection.
     * @param key   A key string.
     * @param value A Collection value.
     * @return      this.
     * @throws JSONException
     */
    public JSONObject put(String key, Collection value) throws JSONException {
        this.put(key, new JSONArray(value));
        return this;
    }


    /**
     * Put a key/double pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A double which is the value.
     * @return this.
     * @throws JSONException If the key is null or if the number is invalid.
     */
    public JSONObject put(String key, double value) throws JSONException {
        this.put(key, new Double(value));
        return this;
    }


    /**
     * Put a key/int pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value An int which is the value.
     * @return this.
     * @throws JSONException If the key is null.
     */
    public JSONObject put(String key, int value) throws JSONException {
        this.put(key, new Integer(value));
        return this;
    }


    /**
     * Put a key/long pair in the JSONObject.
     *
     * @param key   A key string.
     * @param value A long which is the value.
     * @return this.
     * @throws JSONException If the key is null.
     */
    public JSONObject put(String key, long value) throws JSONException {
        this.put(key, new Long(value));
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, where the value will be a
     * JSONObject which is produced from a Map.
     * @param key   A key string.
     * @param value A Map value.
     * @return      this.
     * @throws JSONException
     */
    public JSONObject put(String key, Map value) throws JSONException {
        this.put(key, new JSONObject(value));
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject. If the value is null,
     * then the key will be removed from the JSONObject if it is present.
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *  or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the value is non-finite number
     *  or if the key is null.
     */
    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key");
        }
        if (value != null) {
        	Object wrappedValue = JacksonWrapper.wrap(value);
        	if (wrappedValue instanceof Byte) { 
        		rootNode.put(key, (Byte)wrappedValue );
        	} else if (wrappedValue instanceof Character ) {
        		rootNode.put(key, (Character)wrappedValue );
        	} else if (wrappedValue instanceof Short ) { 
        		rootNode.put(key, (Short)wrappedValue );
        	} else if(wrappedValue instanceof Integer){
        		rootNode.put(key, (Integer)wrappedValue );
        	} else if (wrappedValue instanceof Long ) { 
        		rootNode.put(key, (Long)wrappedValue );
        	} else if (wrappedValue instanceof Boolean){
        		rootNode.put(key, (Boolean)wrappedValue );
        	} else if (wrappedValue instanceof Float) { 
        		rootNode.put(key, (Float)wrappedValue );
    		} else if(wrappedValue instanceof Double) {
    			rootNode.put(key, (Double)wrappedValue );
        	} else if (wrappedValue instanceof String) {
        		rootNode.put(key, (String)wrappedValue );
        	} else if (wrappedValue instanceof JSONObject) { 
        		ObjectNode node = ((JSONObject)wrappedValue).getWrappedObject();
        		rootNode.put(key, node);
        	} else if (wrappedValue instanceof JSONArray) { 
        		ArrayNode node = ((JSONArray)wrappedValue).getWrappedObject();
        		rootNode.put(key, node);
        	} else if (wrappedValue == NullNode.instance) { 
        		rootNode.put(key, NullNode.instance );
        	}        	
        } else {
            this.remove(key);
        }
        
        return this;
    }
    
    

    /**
     * Put a key/value pair in the JSONObject, but only if the key and the
     * value are both non-null, and only if there is not already a member
     * with that name.
     * @param key
     * @param value
     * @return his.
     * @throws JSONException if the key is a duplicate
     */
    public JSONObject putOnce(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            if (this.opt(key) != null) {
                throw new JSONException("Duplicate key \"" + key + "\"");
            }
            this.put(key, value);
        }
        return this;
    }


    /**
     * Put a key/value pair in the JSONObject, but only if the
     * key and the value are both non-null.
     * @param key   A key string.
     * @param value An object which is the value. It should be of one of these
     *  types: Boolean, Double, Integer, JSONArray, JSONObject, Long, String,
     *  or the JSONObject.NULL object.
     * @return this.
     * @throws JSONException If the value is a non-finite number.
     */
    public JSONObject putOpt(String key, Object value) throws JSONException {
        if (key != null && value != null) {
            this.put(key, value);
        }
        return this;
    }
    
    /**
     * Remove a name and its value, if present.
     * @param key The name to be removed.
     * @return The value that was associated with the name,
     * or null if there was no value.
     */
    public Object remove(String key) {
       return rootNode.remove(key);
    }

    /**
     * Make a prettyprinted JSON text of this JSONObject.
     * <p>
     * Warning: This method assumes that the data structure is acyclical.
     * @param indentFactor The number of spaces to add to each level of
     *  indentation.
     * @return a printable, displayable, portable, transmittable
     *  representation of the object, beginning
     *  with <code>{</code>&nbsp;<small>(left brace)</small> and ending
     *  with <code>}</code>&nbsp;<small>(right brace)</small>.
     * @throws JSONException If the object contains an invalid number.
     */
    public String toString(int indentFactor) throws JSONException {
//        StringWriter w = new StringWriter();
//        synchronized (w.getBuffer()) {
//            return this.write(w, indentFactor, 0).toString();
//        }
    	return rootNode.toString();
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((rootNode == null) ? 0 : rootNode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		JSONObject other = (JSONObject) obj;
		for (Iterator<String> iterator =  other.keys(); iterator.hasNext();) {
			String type =iterator.next();
			Object objItem = this.opt(type);
			if(objItem==null){
				return false;
			}else{
				Object objItemobj = other.opt(type);
				if(!objItemobj.equals(objItem)){
					return false;
				}
			}
		}
		return true;
	}
    
    
	
   
}