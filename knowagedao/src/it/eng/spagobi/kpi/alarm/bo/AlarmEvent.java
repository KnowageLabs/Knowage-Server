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


import java.util.Date;

public class AlarmEvent  implements java.io.Serializable {

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
 	private Alarm sbiAlarms; 
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


	public AlarmEvent() {}
	

    public AlarmEvent(Date eventTs, Boolean active, String kpiValue, String thresholdValue, String kpiName, String resources, Alarm sbiAlarms,String kpiDescr) {
       this.eventTs = eventTs;
       this.active = active;
       this.kpiValue = kpiValue;
       this.thresholdValue = thresholdValue;
       this.kpiName = kpiName;
       this.resources = resources;
       this.sbiAlarms = sbiAlarms;
       this.kpiDescription=kpiDescr;
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
    public Alarm getSbiAlarms() {
        return this.sbiAlarms;
    }    
    
    public void setSbiAlarms(Alarm sbiAlarms) {
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


