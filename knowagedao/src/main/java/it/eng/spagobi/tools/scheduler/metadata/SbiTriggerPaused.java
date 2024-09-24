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
package it.eng.spagobi.tools.scheduler.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;
import it.eng.spagobi.tools.scheduler.bo.TriggerPaused;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class SbiTriggerPaused extends SbiHibernateModel{
	
    // Fields  
	
	private Integer id;
    private String triggerName;
    private String triggerGroup;
    private String jobName;
    private String jobGroup;

    // Constructors
    
    /** default constructor */
    public SbiTriggerPaused(){
    	
    }

	public SbiTriggerPaused(Integer id) {
		super();
		this.id = id;
	}

	/**
	 * @param id
	 * @param triggerName
	 * @param triggerGroup
	 * @param jobName
	 * @param jobGroup
	 */
	public SbiTriggerPaused(Integer id, String triggerName,
			String triggerGroup, String jobName, String jobGroup) {
		this.id = id;
		this.triggerName = triggerName;
		this.triggerGroup = triggerGroup;
		this.jobName = jobName;
		this.jobGroup = jobGroup;
	}

    // Property accessors

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	private void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the triggerName
	 */
	public String getTriggerName() {
		return triggerName;
	}

	/**
	 * @param triggerName the triggerName to set
	 */
	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	/**
	 * @return the triggerGroup
	 */
	public String getTriggerGroup() {
		return triggerGroup;
	}

	/**
	 * @param triggerGroup the triggerGroup to set
	 */
	public void setTriggerGroup(String triggerGroup) {
		this.triggerGroup = triggerGroup;
	}

	/**
	 * @return the jobName
	 */
	public String getJobName() {
		return jobName;
	}

	/**
	 * @param jobName the jobName to set
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	/**
	 * @return the jobGroup
	 */
	public String getJobGroup() {
		return jobGroup;
	}

	/**
	 * @param jobGroup the jobGroup to set
	 */
	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}
	
    /**
	 * From the Hibernate SbiTriggerPaused object, gives the corrispondent
	 * <code>TriggerPaused</code> object.
	 * 
	 * 
	 * 
	 * @return the corrispondent output <code>TriggerPaused</code>
	 */
	public TriggerPaused toTriggerPaused(){
		TriggerPaused triggerPaused = new TriggerPaused();
		triggerPaused.setId(getId());
		triggerPaused.setJobGroup(getJobGroup());
		triggerPaused.setJobName(getJobName());
		triggerPaused.setTriggerGroup(getTriggerGroup());
		triggerPaused.setTriggerName(getTriggerName());
		
		return triggerPaused;
	}
	
    
    
}
