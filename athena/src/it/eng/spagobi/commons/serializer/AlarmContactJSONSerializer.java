/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact;

import java.util.Locale;

import org.json.JSONObject;

public class AlarmContactJSONSerializer implements Serializer{
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String EMAIL = "email";
	public static final String RESOURCES = "resources";
	public static final String MOBILE = "mobile";

	
	public Object serialize(Object o, Locale locale)
			throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof SbiAlarmContact) ) {
			throw new SerializationException("AlarmContactJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			SbiAlarmContact sbiAlarmContact = (SbiAlarmContact)o;
			result = new JSONObject();
			result.put(ID, sbiAlarmContact.getId());
			result.put(NAME, sbiAlarmContact.getName());
			result.put(EMAIL, sbiAlarmContact.getEmail());			
			result.put(RESOURCES, sbiAlarmContact.getResources());
			result.put(MOBILE, sbiAlarmContact.getMobile());

		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}
