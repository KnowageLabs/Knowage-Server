/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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


import it.eng.spagobi.commons.metadata.SbiHibernateModel;

import java.util.Date;

public class SbiAlarmEvent  extends SbiHibernateModel {

 	/**
     * 
     */
    private static final long serialVersionUID = 5985137769481979013L;
	private Integer id; 
 	private Date eventTs; 
 	private boolean active;
 	private String kpiValue; 
 	private String thresholdValue; 
 	private String kpiName; 
 	private String resources; 
 	private SbiAlarm sbiAlarms; 
 	private String kpiDescription;
 	private Integer resourcesId;
 	private Integer kpiInstanceId;
 	
    public Integer getKpiInstanceId() {
		return kpiInstanceId;
	}


	public void setKpiInstanceId(Integer kpiInstanceId) {
		this.kpiInstanceId = kpiInstanceId;
	}


	public Integer getResourcesId() {
		return resourcesId;
	}


	public void setResourcesId(Integer resourcesId) {
		this.resourcesId = resourcesId;
	}


	public String getKpiDescription() {
		return kpiDescription;
	}


	public void setKpiDescription(String kpiDescription) {
		this.kpiDescription = kpiDescription;
	}


	public SbiAlarmEvent() {}
	

    public SbiAlarmEvent(Date eventTs, Boolean active, String kpiValue, String thresholdValue, String kpiName, String resources, SbiAlarm sbiAlarms,String kpiDescription) {
       this.eventTs = eventTs;
       this.active = active;
       this.kpiValue = kpiValue;
       this.thresholdValue = thresholdValue;
       this.kpiName = kpiName;
       this.resources = resources;
       this.sbiAlarms = sbiAlarms;
       this.kpiDescription=kpiDescription;
    }

   
    public Integer getId() {
        return this.id;
    }    
    
    public void setId(Integer id) {
        this.id = id;
    }    
    public Date getEventTs() {
        return this.eventTs;
    }    
    
    public void setEventTs(Date eventTs) {
        this.eventTs = eventTs;
    }    
    public boolean isActive() {
        return this.active;
    }    
    
    public void setActive(boolean active) {
        this.active = active;
    }
    public String getKpiValue() {
        return this.kpiValue;
    }    
    
    public void setKpiValue(String kpiValue) {
        this.kpiValue = kpiValue;
    }    
    public String getThresholdValue() {
        return this.thresholdValue;
    }    
    
    public void setThresholdValue(String thresholdValue) {
        this.thresholdValue = thresholdValue;
    }    
    public String getKpiName() {
        return this.kpiName;
    }    
    
    public void setKpiName(String kpiName) {
        this.kpiName = kpiName;
    }    
    public String getResources() {
        return this.resources;
    }    
    
    public void setResources(String resources) {
        this.resources = resources;
    }    
    public SbiAlarm getSbiAlarms() {
        return this.sbiAlarms;
    }    
    
    public void setSbiAlarms(SbiAlarm sbiAlarms) {
        this.sbiAlarms = sbiAlarms;
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
	    retValue.append("SbiAlarmEvent ( ")
	        .append(super.toString()).append(TAB)
			.append("id = ").append(this.id).append(TAB)			
			.append("eventTs = ").append(this.eventTs).append(TAB)			
			.append("active = ").append(this.active).append(TAB)			
			.append("kpiValue = ").append(this.kpiValue).append(TAB)			
			.append("thresholdValue = ").append(this.thresholdValue).append(TAB)			
			.append("kpiName = ").append(this.kpiName).append(TAB)			
			.append("resources = ").append(this.resources).append(TAB)		
	        .append(" )");
	    return retValue.toString();
	}


}


