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
package it.eng.spagobi.kpi.alarm.metadata;

/**
 * Title: SpagoBI
 * Description: SpagoBI
 * Copyright: Copyright (c) 2008
 * Company: Xaltia S.p.A.
 * 
 * @author Enrico Cesaretti
 * @version 1.0
 */

 

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.metadata.SbiDomains;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.kpi.config.metadata.SbiKpiInstance;
import it.eng.spagobi.kpi.threshold.metadata.SbiThresholdValue;

import java.util.HashSet;
import java.util.Set;

public class SbiAlarm  extends SbiHibernateModel{

 	/**
     * 
     */
    private static final long serialVersionUID = 4950020055410842408L;
	private Integer id; 
	private SbiObjects sbiObjects;
	private SbiThresholdValue sbiThresholdValue;
	private SbiKpiInstance sbiKpiInstance;
    private SbiDomains modality;
 	private Boolean singleEvent;
 	private Boolean autoDisabled;
 	private String label; 
 	private String name; 
 	private String descr; 
 	private String text; 
 	private String url; 
 	private Set<SbiAlarmContact> sbiAlarmContacts = new HashSet<SbiAlarmContact>(0); 


    public SbiAlarm() {}
	

    public SbiAlarm(Boolean singleEvent,SbiObjects sbiObjects,SbiThresholdValue sbiThresholdValue, SbiKpiInstance sbiKpiInstance, SbiDomains sbiDomains, Boolean autoDisabled, String label, String name, String descr, String text, String url, Set<SbiAlarmContact> sbiAlarmContacts) {
       this.singleEvent = singleEvent;
       this.autoDisabled = autoDisabled;
       this.sbiObjects = sbiObjects;
       this.sbiThresholdValue = sbiThresholdValue;
       this.sbiKpiInstance = sbiKpiInstance;
       this.modality = sbiDomains;
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
    
    public SbiObjects getSbiObjects() {
        return this.sbiObjects;
    }
    
    public void setSbiObjects(SbiObjects sbiObjects) {
        this.sbiObjects = sbiObjects;
    }

    public SbiThresholdValue getSbiThresholdValue() {
        return this.sbiThresholdValue;
    }
    
    public void setSbiThresholdValue(SbiThresholdValue sbiThresholdValue) {
        this.sbiThresholdValue = sbiThresholdValue;
    }

    public SbiKpiInstance getSbiKpiInstance() {
        return this.sbiKpiInstance;
    }
    
    public void setSbiKpiInstance(SbiKpiInstance sbiKpiInstance) {
        this.sbiKpiInstance = sbiKpiInstance;
    }

    public SbiDomains getModality() {
        return this.modality;
    }
    
    public void setModality(SbiDomains sbiDomains) {
        this.modality = sbiDomains;
    }
    
    public Boolean isSingleEvent() {
        return this.singleEvent;
    }    
    
    public void setSingleEvent(Boolean singleEvent) {
        this.singleEvent = singleEvent;
    }
    public Boolean isAutoDisabled() {
        return this.autoDisabled;
    }    
    
    public void setAutoDisabled(Boolean autoDisabled) {
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
    public Set<SbiAlarmContact> getSbiAlarmContacts() {
        return this.sbiAlarmContacts;
    }    
    
    public void setSbiAlarmContacts(Set<SbiAlarmContact> sbiAlarmContacts) {
        this.sbiAlarmContacts = sbiAlarmContacts;
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


	public Boolean getAutoDisabled() {
		return autoDisabled;
	}


	

}


