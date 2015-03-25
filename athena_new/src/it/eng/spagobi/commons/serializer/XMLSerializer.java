/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class XMLSerializer implements Serializer {
	
	Map<Class, Serializer> mappings;
	Properties properties;
	
	public XMLSerializer() {
		mappings = new HashMap();
		mappings.put( Job.class, new JobXMLSerializer() );
		mappings.put( Trigger.class, new TriggerXMLSerializer() );
		
		properties = new Properties();
	}

	public Object serialize(Object o, Locale locale) throws SerializationException {
		Object result = null;	
		
		try {
			if(o instanceof Collection) {
				StringBuffer buffer = new StringBuffer();
				
				buffer.append("<ROWS>");
				Collection objectCollection = (Collection)o;
				for(Object object : objectCollection) {
					buffer.append( serialize( object, locale) );
				}
				buffer.append("</ROWS>");
				result = buffer.toString();
			} else {
				if( !mappings.containsKey(o.getClass())) {
					throw new SerializationException("XMLSerializer is unable to serialize object of type: " + o.getClass().getName());
				}
				
				Serializer serializer = mappings.get(o.getClass());
				if(serializer instanceof JobXMLSerializer) {
					JobXMLSerializer jobXMLSerializer = (JobXMLSerializer)serializer;
					jobXMLSerializer.setProperties(properties);
				} else if(serializer instanceof TriggerXMLSerializer) {
					TriggerXMLSerializer triggerXMLSerializer = (TriggerXMLSerializer)serializer;
					triggerXMLSerializer.setProperties(properties);
				}
				result = serializer.serialize(o,locale);
			}			
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;	
	}
	
	public void setProperty(String name, String value) {
		properties.setProperty(name, value);
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}


}
