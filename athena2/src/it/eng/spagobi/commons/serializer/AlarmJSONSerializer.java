/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarm;
import it.eng.spagobi.kpi.alarm.metadata.SbiAlarmContact;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class AlarmJSONSerializer implements Serializer{
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String DESCRIPTION = "description";
	public static final String LABEL = "label";
	public static final String MODALITY = "modality";
	public static final String SINGLE_EVENT = "singleEvent";
	public static final String AUTO_DISABLED = "autoDisabled";
	public static final String TEXT = "text";
	public static final String URL = "url";
	public static final String KPI = "kpi";
	public static final String THRESHOLD = "threshold";
	
	public Object serialize(Object o, Locale locale)
			throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof SbiAlarm) ) {
			throw new SerializationException("AlarmJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			SbiAlarm sbiAlarm = (SbiAlarm)o;
			result = new JSONObject();
			result.put(ID, sbiAlarm.getId());
			result.put(NAME, sbiAlarm.getName());
			result.put(LABEL, sbiAlarm.getLabel());			
			result.put(SINGLE_EVENT, sbiAlarm.isSingleEvent());
			result.put(AUTO_DISABLED, sbiAlarm.getAutoDisabled());
			result.put(DESCRIPTION, sbiAlarm.getDescr());	
			result.put(TEXT, sbiAlarm.getText());	
			result.put(URL, sbiAlarm.getUrl());	
			if(sbiAlarm.getSbiKpiInstance() != null){
				result.put(KPI, sbiAlarm.getSbiKpiInstance().getIdKpiInstance());	
			}
			if(sbiAlarm.getSbiThresholdValue() != null){
				result.put(THRESHOLD, sbiAlarm.getSbiThresholdValue().getIdThresholdValue());	
			}
			SbiDomains modalityDomain = sbiAlarm.getModality();
			if(modalityDomain != null)
				result.put(MODALITY, modalityDomain.getValueCd());
			else
				result.put(MODALITY, "");
			
			//contacts
			List<SbiAlarmContact> allContacts = DAOFactory.getAlarmContactDAO().findAll();
			Iterator itAllContacts = allContacts.iterator();
			
			Set<SbiAlarmContact> alarmContacts = sbiAlarm.getSbiAlarmContacts();			
			JSONArray contactsJSON = new JSONArray();
			
			while(itAllContacts.hasNext()){
				JSONObject jsonContact = new JSONObject();
				SbiAlarmContact contact = (SbiAlarmContact)itAllContacts.next();
				if(contact!=null){
					Integer contactId = contact.getId();
					jsonContact.put("id", contact.getId());
					jsonContact.put("name", contact.getName());					
					jsonContact.put("mobile", contact.getMobile());
					jsonContact.put("email", contact.getEmail());
					jsonContact.put("resources", contact.getResources());
					Iterator itTemp = alarmContacts.iterator();
					boolean contained = false;
					while(itTemp.hasNext()){
						SbiAlarmContact c =(SbiAlarmContact)itTemp.next();
						if(c.getId().equals(contactId)){
							jsonContact.put("checked", true);
							contained = true;
							break;
						}
					}
					if(!contained){
						jsonContact.put("checked", false);
					}
					contactsJSON.put(jsonContact);
				}			
			}
			result.put("contacts", contactsJSON);
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}
