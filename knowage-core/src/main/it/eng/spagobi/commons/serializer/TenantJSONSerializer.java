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