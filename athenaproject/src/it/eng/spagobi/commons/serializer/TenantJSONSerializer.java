/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.metadata.SbiTenant;

import java.util.Locale;

import org.json.JSONObject;

public class TenantJSONSerializer implements Serializer {

	public static final String TENANT_ID = "MULTITENANT_ID";
	private static final String TENANT_NAME = "MULTITENANT_NAME";
	private static final String TENANT_THEME = "MULTITENANT_THEME";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof SbiTenant) ) {
			throw new SerializationException("TenantJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			SbiTenant ds = (SbiTenant)o;
			result = new JSONObject();
			
			result.put(TENANT_ID, ds.getId());
			result.put(TENANT_NAME, ds.getName() );
			result.put(TENANT_THEME, ds.getTheme() );	
		
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}