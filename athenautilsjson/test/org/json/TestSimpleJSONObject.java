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

import junit.framework.TestCase;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class TestSimpleJSONObject extends TestCase {
	
	JSONObject jsonobject;
	String jsonstring = 
	"{" +
		"\"value1\" : \"john\"," +
		"\"value2\" : 18," +
		"\"value3\" : 18.81," +
		"\"value4\" : true," +
		"\"value6\" : null," +
    	"\"array\" : [ 1, { \"name\" : \"Billy\" }, null ]," +
        "\"object\" : { \"id\" : 123, \"names\" : [ \"Bob\", \"Bobby\" ]  }" +
    "}";

	public void testEmptyConstructor() throws Exception {
		try {
			jsonobject = new JSONObject();
		} catch(Throwable t) {
			fail(t.toString());
		}
	}
		
	public void testStringConstructor() throws Exception {
		try {
			jsonobject = new JSONObject(jsonstring);
		} catch(Throwable t) {
			fail(t.toString());
		}
	}
	
	public void testOpt() throws Exception {
		try {
			jsonobject = new JSONObject(jsonstring);
			Object o = null;
			
			o = jsonobject.opt("value0");
			assertNull(o);
						
			o = jsonobject.opt("value1");
			assertNotNull(o);
			assertTrue(o instanceof String);
			assertEquals(o, "john");
			
			o = jsonobject.opt("value2");
			assertNotNull(o);
			assertTrue(o instanceof Integer);
			assertEquals(o, 18);
			
			o = jsonobject.opt("value3");
			assertNotNull(o);
			assertTrue(o instanceof Double);
			assertEquals(o, 18.81);
			
			o = jsonobject.opt("value4");
			assertNotNull(o);
			assertTrue(o instanceof Boolean);
			assertEquals(o, true);
			
			o = jsonobject.opt("value6");
			assertNotNull(o);
			assertTrue(o == JSONObject.NULL);
			
			o = jsonobject.opt("object");
			assertNotNull(o);
			assertTrue(o instanceof JSONObject); 
			assertEquals( ((JSONObject)o).opt("id"), 123);
			
			o = jsonobject.opt("array");
			assertNotNull(o);
			assertTrue(o instanceof JSONArray);
			JSONArray a = (JSONArray)o;
			assertEquals(3, a.length());
			
		} catch(Exception t) {
			fail(t.toString());
		}
	}
	
	
	public void testNull() throws Exception {
	    jsonobject = new JSONObject("{\"message\":\"null\"}");
	    assertFalse(jsonobject.isNull("message"));
	    assertEquals("null", jsonobject.getString("message"));
	
	    jsonobject = new JSONObject("{\"message\":null}");
	    assertTrue(jsonobject.isNull("message"));
	    
	    jsonobject.put("message2", JSONObject.NULL);
	    assertTrue(jsonobject.isNull("message2"));
	    
	    jsonobject.put("message2", (Object)null);
	    assertEquals(null, jsonobject.opt("message2"));
	    
	    jsonobject.put("message3", (Object)null);
	    assertEquals(null, jsonobject.opt("message3"));
	    
	   
	}
}
