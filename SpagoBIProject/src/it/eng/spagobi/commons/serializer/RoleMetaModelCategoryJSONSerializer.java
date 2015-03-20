/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import java.util.Locale;

import org.json.JSONObject;

import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.bo.RoleMetaModelCategory;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 */
public class RoleMetaModelCategoryJSONSerializer implements Serializer {

	public static final String ROLE_ID = "role_id";
	private static final String CATEGORY_ID = "category_id";
	

	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof RoleMetaModelCategory) ) {
			throw new SerializationException("RoleMetaModelCategoryJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			RoleMetaModelCategory roleMetaModelCategory = (RoleMetaModelCategory)o;
			result = new JSONObject();
			
			
			result.put(ROLE_ID, roleMetaModelCategory.getRoleId() );
			result.put(CATEGORY_ID,roleMetaModelCategory.getCategoryId() );
			
			
			
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}
