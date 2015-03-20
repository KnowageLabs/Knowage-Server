/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.alarm.bo;

/**
 * Title: SpagoBI
 * Description: SpagoBI
 * Copyright: Copyright (c) 2008
 * Company: Xaltia S.p.A.
 * 
 * @author Enrico Cesaretti
 * @version 1.0
 */

 

import java.util.HashSet;
import java.util.Set;

public class Alarm  implements java.io.Serializable {

 	/**
     * 
     */
    private static final long serialVersionUID = 4950020055410842408L;
	private Integer id; 
 	private boolean singleEvent;
 	private boolean autoDisabled;
 	private String label; 
 	private String name; 
 	private String descr; 
 	private String text; 
 	private String url; 
 	private Integer idKpiInstance; 
 	private Integer idThresholdValue; 
 	private Integer modalityId;
 	
 	private Set<AlarmContact> sbiAlarmContacts = new HashSet<AlarmContact>(0); 


    public Alarm() {}
	

    public Alarm(Boolean singleEvent, Boolean autoDisabled, String label, String name, String descr, String text, String url, Set<AlarmContact> sbiAlarmContacts) {
       this.singleEvent = singleEvent;
       this.autoDisabled = autoDisabled;
       this.label = label;
       this.name = name;
       this.descr = descr;
       this.text = text;
       this.url = url;
       this.sbiAlarmContacts = sbiAlarmContacts;
    }

   
    public Integer getId() {
        return this.id;
    }    
    
    public void setId(Integer id) {
        this.id = id;
    }    
    public boolean isSingleEvent() {
        return this.singleEvent;
    }    
    
    public void setSingleEvent(boolean singleEvent) {
        this.singleEvent = singleEvent;
    }
    public boolean isAutoDisabled() {
        return this.autoDisabled;
    }    
    
    public void setAutoDisabled(boolean autoDisabled) {
        this.autoDisabled = autoDisabled;
    }
    public String getLabel() {
        return this.label;
    }    
    
    public void setLabel(String label) {
        this.label = label;
    }    
    public String getName() {
        return this.name;
    }    
    
    public void setName(String name) {
        this.name = name;
    }    
    public String getDescr() {
        return this.descr;
    }    
    
    public void setDescr(String descr) {
        this.descr = descr;
    }    
    public String getText() {
        return this.text;
    }    
    
    public void setText(String text) {
        this.text = text;
    }    
    public String getUrl() {
        return this.url;
    }    
    
    public void setUrl(String url) {
        this.url = url;
    }    
    public Set<AlarmContact> getSbiAlarmContacts() {
        return this.sbiAlarmContacts;
    }    
    
    public void setSbiAlarmContacts(Set<AlarmContact> sbiAlarmContacts) {
        this.sbiAlarmContacts = sbiAlarmContacts;
    }    

    
	public Integer getIdKpiInstance() {
		return idKpiInstance;
	}


	public void setIdKpiInstance(Integer idKpiInstance) {
		this.idKpiInstance = idKpiInstance;
	}


	public Integer getIdThresholdValue() {
		return idThresholdValue;
	}


	public void setIdThresholdValue(Integer _idThresholdValue) {
		this.idThresholdValue = _idThresholdValue;
	}

	

	public Integer getModalityId() {
		return modalityId;
	}


	public void setModalityId(Integer modality) {
		this.modalityId = modality;
	}


	/**
	 * Constructs a <code>String</code> with all attributes
	 * in name = value format.
	 *
	 * @return a <code>String</code> representation 
	 * of this object.
	 */
	public String toString(){
	    final String TAB = ", ";
	    StringBuffer retValue = new StringBuffer();
	    retValue.append("SbiAlarm ( ")
	        .append(super.toString()).append(TAB)
			.append("id = ").append(this.id).append(TAB)			
			.append("singleEvent = ").append(this.singleEvent).append(TAB)			
			.append("autoDisabled = ").append(this.autoDisabled).append(TAB)			
			.append("label = ").append(this.label).append(TAB)			
			.append("name = ").append(this.name).append(TAB)			
			.append("descr = ").append(this.descr).append(TAB)			
			.append("text = ").append(this.text).append(TAB)			
			.append("url = ").append(this.url).append(TAB)
	        .append(" )");
	    return retValue.toString();
	}


}


