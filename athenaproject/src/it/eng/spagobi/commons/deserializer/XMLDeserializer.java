/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.deserializer;

import it.eng.spago.base.SourceBean;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class XMLDeserializer implements Deserializer {
	
	Map<Class, Deserializer> mappings;
	
	public XMLDeserializer() {
		mappings = new HashMap();
		mappings.put( Job.class, new JobXMLDeserializer() );
		mappings.put( Trigger.class, new TriggerXMLDeserializer() );
	}

	public Object deserialize(Object o, Class clazz) throws DeserializationException {
		Object result = null;	
		
		try {
			Assert.assertNotNull(o, "Input parameter [" + o + "] cannot be null");
			Assert.assertNotNull(o, "Input parameter [" + clazz + "] cannot be null");
			
			SourceBean xml = null;
			if(o instanceof SourceBean) {
				xml = (SourceBean)o;
			} else if (o instanceof String) {
				xml = SourceBean.fromXMLString( (String)o);
			} else {
				throw new DeserializationException("Impossible to deserialize from an object of type [" + o.getClass().getName() +"]");
			}
			
			Deserializer deserializer = mappings.get(clazz);
			if(deserializer == null) {
				throw new DeserializationException("Impossible to deserialize to an object of type [" + clazz.getName() +"]");
			}
			
			if(xml.getAttribute("ROWS") != null) {
				List list = new ArrayList();
				List<SourceBean> rows = xml.getAttributeAsList("ROWS.ROW");
				for(SourceBean row: rows) {
					list.add( deserializer.deserialize(row, clazz) );
				}
				result = list;
			} else {
				result = deserializer.deserialize(o, clazz);
			}
		} catch (Throwable t) {
			throw new DeserializationException("An error occurred while deserializing object: " + o, t);
		} finally {
			
		}
		
		return result;	
	}


}
