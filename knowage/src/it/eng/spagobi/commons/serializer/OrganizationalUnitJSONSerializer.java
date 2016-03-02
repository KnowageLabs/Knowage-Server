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

import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;

import java.util.Locale;

import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class OrganizationalUnitJSONSerializer implements Serializer {
	
	public static final String ID = "id";
	public static final String LABEL = "label";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof OrganizationalUnit) ) {
			throw new SerializationException("OrganizationalUnitJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			OrganizationalUnit ou = (OrganizationalUnit) o;
			result = new JSONObject();
			result.put(ID, ou.getId() );
			result.put(LABEL, ou.getLabel() );
			result.put(NAME, ou.getName() );
			result.put(DESCRIPTION, ou.getDescription() );
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		}
		
		return result;
	}
	
	
}
