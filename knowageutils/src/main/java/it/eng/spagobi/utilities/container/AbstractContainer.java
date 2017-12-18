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
package it.eng.spagobi.utilities.container;

import it.eng.spagobi.utilities.service.AbstractBaseHttpAction;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public abstract class AbstractContainer implements IPropertiesContainer {
	
	public static final String TRUE = "TRUE";
	public static final String FALSE = "FALSE";
	
	/**
     * Logger component
     */
    private static transient Logger logger = Logger.getLogger(AbstractContainer.class);
    
    
	
	public String getPropertyAsString(String propertyName) {
		if ( containsProperty(propertyName) ) {
			return getProperty(propertyName).toString();
		}
		
		return null;
	}
	
	public Boolean getPropertyAsBoolean(String propertyName) {
		if( !containsProperty(propertyName) ) return null;
		return getPropertyAsBoolean(propertyName, false);
	}

	public Boolean getPropertyAsBoolean(String propertyName, boolean defaultValue) {
		if( !containsProperty(propertyName) ) return new Boolean(defaultValue);
		return new Boolean(getPropertyAsString(propertyName).equalsIgnoreCase(TRUE));
	}
	
	/**
	 * Gets the attribute as integer.
	 * 
	 * @param propertyName the attr name
	 * 
	 * @return the attribute as integer
	 */
	public Integer getPropertyAsInteger(String propertyName) {
		Integer propertyValue;
		
		if( !containsProperty(propertyName) ) return null;
		try {
			propertyValue = new Integer( getPropertyAsString(propertyName) );
		} catch(NumberFormatException e) {
			logger.warn("Impossible to convert request parameter " + propertyName 
					+ " whose value is " + getPropertyAsString(propertyName)
					+ " to an integer", e);
			
			propertyValue = null;
		}
		
		return propertyValue;
	}
	
	public List getPropertyAsStringList(String propertyName) {
		List propertyValue = null;
		Object rawPropertyValue;
		
		if( !containsProperty(propertyName) ) return null;
		
		rawPropertyValue = getProperty(propertyName);		
		if(rawPropertyValue != null) {
			if(rawPropertyValue instanceof ArrayList) {
				propertyValue = (List)rawPropertyValue;
			} else if (rawPropertyValue instanceof String) {
				propertyValue = new ArrayList();
				propertyValue.add( rawPropertyValue );
			} else {
				propertyValue = new ArrayList();
				propertyValue.add(rawPropertyValue.toString());
			}
		}
		
		return propertyValue;		
	}
	
	public List getPropertyAsCsvStringList(String propertyName, String separator) {
		List propertyValue = new ArrayList();
		
		if( !containsProperty(propertyName) ) return null;
		try {
			String[] chunks = getPropertyAsString(propertyName).split(separator);
			for(int i = 0; i < chunks.length; i++) {
				logger.info("Chunks " + i + ":" +  chunks[i]);
				propertyValue.add(chunks[i].trim());
			}
			
		} catch (Exception e) {
			logger.warn("Impossible to convert request parameter " + propertyName 
					+ " whose value is " + getPropertyAsString(propertyName)
					+ " to list", e);
		}
		
		return propertyValue;		
	}
	
	public JSONObject getPropertyAsJSONObject(String propertyName) {
		JSONObject propertyValue = null;
		
		if( !containsProperty(propertyName) ) return null;
		try {
			propertyValue = new JSONObject(getPropertyAsString(propertyName));
		} catch (Exception e) {
			logger.warn("Impossible to convert request parameter " + propertyName 
					+ " whose value is " + getPropertyAsString(propertyName)
					+ " to JSONObject", e);
		}
		
		return propertyValue;		
	}
}
