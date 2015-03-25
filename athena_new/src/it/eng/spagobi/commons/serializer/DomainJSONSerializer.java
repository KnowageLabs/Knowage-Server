/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
