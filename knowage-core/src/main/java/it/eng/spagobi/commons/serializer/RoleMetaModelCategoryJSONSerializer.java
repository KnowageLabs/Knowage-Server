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
