/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.tools.udp.metadata.SbiUdpValue;

import java.util.Locale;

import org.json.JSONObject;

public class UdpValueJSONSerializer  implements Serializer{
	public static final String ID = "id";
	public static final String UDPID = "udpId";
	public static final String LABEL = "label";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	public static final String PROG = "prog";
	public static final String FAMILY = "family";
	public static final String REFERENCE_ID = "referenceId";
	public static final String BEGIN_TS = "beginTs";
	public static final String END_TS = "endTs";
	
	public Object serialize(Object o, Locale locale)
			throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof SbiUdpValue) ) {
			throw new SerializationException("UdpValueJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			SbiUdpValue udpdValue = (SbiUdpValue)o;
			result = new JSONObject();
			result.put(ID, udpdValue.getUdpValueId());
			result.put(UDPID, udpdValue.getSbiUdp().getUdpId());
			result.put(LABEL, udpdValue.getLabel()); //...denormilize?
			result.put(NAME, udpdValue.getName()); 	//...denormilize?
			result.put(VALUE, udpdValue.getValue());
			result.put(PROG, udpdValue.getProg());
			result.put(FAMILY, udpdValue.getFamily()); //...denormilize?
			result.put(REFERENCE_ID, udpdValue.getReferenceId());
			result.put(BEGIN_TS, udpdValue.getBeginTs());
			result.put(END_TS, udpdValue.getEndTs());

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}
}
