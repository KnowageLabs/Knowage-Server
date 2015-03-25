/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
