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

import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class JacksonWrapper {
	
		 /**
	     * Wrap an object, if necessary. If the object is null, return the 	NullNode 
	     * object. If it is an array or collection, wrap it in a ArrayNode. If
	     * it is a map, wrap it in a ObjectNode. If it is a standard property
	     * (Double, String, et al) then it is already wrapped. 
	     *
	     * @param object The object to wrap
	     * @return The wrapped value
	     * @throws JSONException 
	     */
		static final Object wrap(Object object) throws JSONException {
	        Object wrappedObject = null;
	        
	    	try {
	            if (object == null || object.equals( JSONObject.NULL ) ) {
	                return NullNode.instance ;
	            }
	            
	            if (    object instanceof Byte   || object instanceof Character  ||
	                    object instanceof Short  || object instanceof Integer    ||
	                    object instanceof Long   || object instanceof Boolean    ||
	                    object instanceof Float  || object instanceof Double     ||
	                    object instanceof String) {
	            	
	            	wrappedObject = object;
	            
	            } else if(object instanceof JSONObject || object instanceof JSONArray) {

	            	wrappedObject = object;
	            
	            } else if(object instanceof ObjectNode) {
	           
	            	wrappedObject = new JSONObject((ObjectNode)object);
	            
	            } else if(object instanceof ArrayNode) {
	 	           
	            	wrappedObject = new JSONArray((ArrayNode)object);
	            
	            } else if (object instanceof Collection) {
	            	
	            	wrappedObject = wrap( new JSONArray((Collection)object) );
	            
	            } else if (object.getClass().isArray()) {
	            	
	            	wrappedObject = wrap( new JSONArray(object) );
	            
	            } else if (object instanceof Map) {
	            	
	            	wrappedObject = wrap (new JSONObject((Map)object));
	            
	            } else {
	            	//wrappedObject = new JSONObject(object);
	            	wrappedObject = object.toString();
	            }
	            
	            return wrappedObject;
	        
	    	} catch(Throwable t) {
	            if(t instanceof JSONException) throw (JSONException)t;
	            throw new JSONException("An unexpected error occured while wrapping value [" + object + "] of type [" + (object!=null?object.getClass().getName(): "null") + "]: " + t.getMessage());
	        }
	    }
		
		static final Object unwrap(Object object) throws JSONException {
	        Object unwrappedObject = null;
	        
	    	try {
	            if ( object == NullNode.instance ) {
	                return JSONObject.NULL ;
	            }
	            
	            if(object instanceof TextNode) {
	            	unwrappedObject = ((TextNode)object).asText();
	    		} else if(object instanceof IntNode) {	
	    			int v = ((IntNode)object).asInt();
	    			unwrappedObject = new Integer(v);
	    		} else if(object instanceof LongNode) {	
	    			long v = ((LongNode)object).asLong();
	    			unwrappedObject = new Long(v);
	    		} else if(object instanceof DoubleNode) {	
	    			double v = ((DoubleNode)object).asDouble();
	    			unwrappedObject = new Double(v);
	    		} else if(object instanceof BooleanNode) {	
	    			boolean v = ((BooleanNode)object).asBoolean();
	    			unwrappedObject = new Boolean(v);
	    		} else if(object instanceof ObjectNode) {
	            	unwrappedObject = new JSONObject( (ObjectNode)object );	            
	            } else if(object instanceof ArrayNode) {	
	            	unwrappedObject = new JSONArray( (ArrayNode)object );
	            } else {
	            	throw new JSONException("Unsupported value type [" + object.getClass().getName() + "]");
	            }
	            
	            return unwrappedObject;
	        
	    	} catch(Throwable t) {
	            if(t instanceof JSONException) throw (JSONException)t;
	            throw new JSONException("An unexpected error occured while wrapping value [" + object + "] of type [" + (object!=null?object.getClass().getName(): "null") + "]: " + t.getMessage());
	        }
		}
	
}
