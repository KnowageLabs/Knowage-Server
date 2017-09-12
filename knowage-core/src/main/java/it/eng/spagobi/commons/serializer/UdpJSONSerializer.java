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


import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.udp.metadata.SbiUdp;

import java.util.Locale;

import org.json.JSONObject;

public class UdpJSONSerializer implements Serializer {

	public static final String UDP_ID = "id";
	private static final String UDP_NAME = "name";
	private static final String UDP_DESCRIPTION = "description";
	private static final String UDP_LABEL = "label";
	private static final String UDP_TYPE = "type";
	private static final String UDP_FAMILY = "family";
	private static final String UDP_MULTIVALUE = "multivalue";
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof SbiUdp) ) {
			throw new SerializationException("UdpJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			SbiUdp udp = (SbiUdp)o;
			result = new JSONObject();
			//converts valueId in valueCd information
			Domain tmpDomain = DAOFactory.getDomainDAO().loadDomainById(udp.getTypeId());
			String typeStr = tmpDomain.getValueCd();					
			tmpDomain = DAOFactory.getDomainDAO().loadDomainById((udp.getFamilyId()));
			String familyStr =  tmpDomain.getValueCd();
				
			result.put(UDP_ID, udp.getUdpId());
			result.put(UDP_NAME, udp.getName() );
			result.put(UDP_DESCRIPTION, udp.getDescription() );
			result.put(UDP_LABEL, udp.getLabel() );
			result.put(UDP_TYPE, typeStr );
			result.put(UDP_FAMILY, familyStr);		
			result.put(UDP_MULTIVALUE, udp.isIsMultivalue());	
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}
