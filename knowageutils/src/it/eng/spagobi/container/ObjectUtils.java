/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * These mathods do not handle conversion exceptions. 
 * Exceptions are catched, logged and then re-thrown
 * 
 * IMPORTANT: if new conversion methods are added please follow the convention described above.
 * Do NOT catch conversion exceptions and return a null value.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class ObjectUtils {
	
	private static transient Logger logger = Logger.getLogger(ObjectUtils.class);
	
	
	private static void assertNotNull(Object o, String message) {
		if(o == null) {
			throw new IllegalArgumentException( message );
		}
	}
	
	
	public static String toString(Object o) {
		String toReturn;
		
		assertNotNull(o, "Input object cannot be null");
		
		toReturn = o.toString();
			
		return toReturn;	
	}
	
	public static Boolean toBoolean(Object o) {
		Boolean toReturn;
		
		assertNotNull(o, "Input object cannot be null");
		
		if (o instanceof Boolean) {
			toReturn = (Boolean)o;
		} else {
			toReturn = new Boolean( toString(o) );			
		}
		
		return toReturn;
	}
	
	public static Integer toInteger(Object o) {
		Integer toReturn;
		
		assertNotNull(o, "Input object cannot be null");
		
		toReturn = null;
		try {
			if(o instanceof Number) {
				toReturn = new Integer( ((Number)o).intValue()  );
			} else {
				toReturn = new Integer( toString(o) );
			}
		} catch(NumberFormatException e) {
			logger.warn("Impossible to convert input object " + o 
					+ " whose value is " + toString(o)
					+ " to an integer", e);
			
			throw e;
		}
		
		return toReturn;
	}
	
	public static List toList(Object o) {
		List toReturn;
		
		assertNotNull(o, "Input object cannot be null");
		
		toReturn = null;
		try {
			if(o instanceof List) {
				toReturn = (List)o;
			} else {
				toReturn = new ArrayList();
				toReturn.add( o );
			}
		} catch(Throwable e) {
			logger.warn("Impossible to convert input object " + o 
					+ " whose value is " + toString(o)
					+ " to list", e);
		}
		
		return toReturn;	
	}
	
	public static List toCsvList(Object o) {
		return toCsvList(o, ",");
	}
	public static List toCsvList(Object o, String separator) {
		List toReturn;
				
		assertNotNull(o, "Input object cannot be null");
		assertNotNull(separator, "Input separator cannot be null");
		
		toReturn = new ArrayList();
		try {
			String[] listItems = toString(o).split(separator);
			for(int i = 0; i < listItems.length; i++) {				
				toReturn.add(listItems[i].trim());
			}
		} catch(Throwable e) {
			logger.warn("Impossible to convert input object " + o 
					+ " whose value is " + toString(o)
					+ " to csv-list", e);
		};
			
		return toReturn;	
	}
	
	public static JSONObject toJSONObject(Object o) {
		JSONObject toReturn;
		
		assertNotNull(o, "Input object cannot be null");
		
		toReturn = null;
		try {
			toReturn = new JSONObject( toString(o) );
		} catch (Exception e) {
			logger.error("Impossible to convert input object " + o 
					+ " whose value is " + toString(o)
					+ " to JSONObject", e);
		}
		
		return toReturn;	
	}
	
	public static JSONArray toJSONArray(Object o) {
		JSONArray toReturn;
		
		assertNotNull(o, "Input object cannot be null");
		
		toReturn = null;
		try {
			toReturn = new JSONArray( toString(o) );
		} catch (Exception e) {
			logger.warn("Impossible to convert input object " + o 
					+ " whose value is " + toString(o)
					+ " to JSONArray", e);
		}
		
		return toReturn;	
	}
	
	 
}
