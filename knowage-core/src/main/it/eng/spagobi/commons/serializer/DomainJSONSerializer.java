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
package it.eng.spagobi.commons.serializer;

import java.util.Locale;

import org.json.JSONObject;

import it.eng.spagobi.commons.bo.Domain;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class DomainJSONSerializer implements Serializer {
	
	public static final String DOMAIN_CODE = "DOMAIN_CD";
	public static final String DOMAIN_NAME = "DOMAIN_NM";
	
	public static final String VALUE_ID = "VALUE_ID";
	public static final String VALUE_CODE = "VALUE_CD";
	public static final String VALUE_NAME = "VALUE_NM";
	public static final String VALUE_DECRIPTION = "VALUE_DS";
	
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof Domain) ) {
			throw new SerializationException("DomainJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			Domain domain = (Domain)o;
			result = new JSONObject();
			result.put(DOMAIN_CODE, domain.getDomainCode() ); // BIOBJ_TYPE
			result.put(DOMAIN_NAME, domain.getDomainName() ); // BI Object types
			
			result.put(VALUE_ID, domain.getValueId() ); // ex. 1
			result.put(VALUE_CODE, domain.getValueCd() ); // REPORT
			result.put(VALUE_NAME, domain.getValueName() ); // ex. Report
			result.put(VALUE_DECRIPTION, domain.getValueDescription() ); // Basic business intelligence objects type
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}
