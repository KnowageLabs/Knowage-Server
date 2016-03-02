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
