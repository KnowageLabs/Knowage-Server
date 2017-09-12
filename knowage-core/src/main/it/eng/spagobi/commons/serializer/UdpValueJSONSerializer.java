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
