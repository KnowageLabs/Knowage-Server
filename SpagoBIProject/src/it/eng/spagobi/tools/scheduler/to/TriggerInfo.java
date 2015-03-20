/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.to;

import java.io.Serializable;
import java.util.Map;


public class TriggerInfo implements Serializable{


	private String triggerName = "";
	private String triggerDescription = "";
	private String startDate = "";
	private String startTime = "";
	private String chronString = "";
	private String endDate = "";
	private String endTime = "";
	private String repeatInterval = "";
	private JobInfo jobInfo = null;
	private Map<String, DispatchContext> saveOptions = null;
    
	/**
	 * Gets the end date.
	 * 
	 * @return the end date
	 */
	public String getEndDate() {
		return endDate;
	}
	
	/**
	 * Sets the end date.
	 * 
	 * @param endDate the new end date
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	
	/**
	 * Gets the end time.
	 * 
	 * @return the end time
	 */
	public String getEndTime() {
		return endTime;
	}
	
	/**
	 * Sets the end time.
	 * 
	 * @param endTime the new end time
	 */
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	/**
	 * Gets the repeat interval.
	 * 
	 * @return the repeat interval
	 */
	public String getRepeatInterval() {
		return repeatInterval;
	}
	
	/**
	 * Sets the repeat interval.
	 * 
	 * @param repeatInterval the new repeat interval
	 */
	public void setRepeatInterval(String repeatInterval) {
		this.repeatInterval = repeatInterval;
	}
	
	/**
	 * Gets the start date.
	 * 
	 * @return the start date
	 */
	public String getStartDate() {
		return startDate;
	}
	
	/**
	 * Sets the start date.
	 * 
	 * @param startDate the new start date
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	
	/**
	 * Gets the start time.
	 * 
	 * @return the start time
	 */
	public String getStartTime() {
		return startTime;
	}
	
	/**
	 * Sets the start time.
	 * 
	 * @param startTime the new start time
	 */
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	
	/**
	 * Gets the trigger description.
	 * 
	 * @return the trigger description
	 */
	public String getTriggerDescription() {
		return triggerDescription;
	}
	
	/**
	 * Sets the trigger description.
	 * 
	 * @param triggerDescription the new trigger description
	 */
	public void setTriggerDescription(String triggerDescription) {
		this.triggerDescription = triggerDescription;
	}
	
	/**
	 * Gets the trigger name.
	 * 
	 * @return the trigger name
	 */
	public String getTriggerName() {
		return triggerName;
	}
	
	/**
	 * Sets the trigger name.
	 * 
	 * @param triggerName the new trigger name
	 */
	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}
	
	/**
	 * Gets the job info.
	 * 
	 * @return the job info
	 */
	public JobInfo getJobInfo() {
		return jobInfo;
	}
	
	/**
	 * Sets the job info.
	 * 
	 * @param jobInfo the new job info
	 */
	public void setJobInfo(JobInfo jobInfo) {
		this.jobInfo = jobInfo;
	}
	
	/**
	 * Gets the save options.
	 * 
	 * @return the save options
	 */
	public Map<String, DispatchContext> getSaveOptions() {
		return saveOptions;
	}
	
	/**
	 * Sets the save options.
	 * 
	 * @param saveOptions the new save options
	 */
	public void setSaveOptions(Map<String, DispatchContext> saveOptions) {
		this.saveOptions = saveOptions;
	}
	
	/**
	 * Gets the chron string.
	 * 
	 * @return the chron string
	 */
	public String getChronString() {
		return chronString;
	}
	
	/**
	 * Sets the chron string.
	 * 
	 * @param chronString the new chron string
	 */
	public void setChronString(String chronString) {
		this.chronString = chronString;
	}
	
	/**
	 * Gets the start date rf c3339.
	 * 
	 * @return the start date rf c3339
	 */
	public String getStartDateRFC3339() {
		String startDRFC = "";
		String startD = this.getStartDate();
		if( (startD!=null) && !startD.trim().equals("") ) {
			String[] dateParts = startD.split("/");
			startDRFC = dateParts[2] + "-" + dateParts[1] + "-" +  dateParts[0];
		}
		return startDRFC;
	}
	
	/**
	 * Gets the end date rf c3339.
	 * 
	 * @return the end date rf c3339
	 */
	public String getEndDateRFC3339() {
		String endDRFC = "";
		String endD = this.getEndDate();
		if( (endD!=null) && !endD.trim().equals("") ) {
			if (endD.indexOf("/") > 0) {
				String[] dateParts = endD.split("/");
				endDRFC = dateParts[2] + "-" + dateParts[1] + "-" +  dateParts[0];
			}else{
				endDRFC = endD;
			}
		}
		return endDRFC;
	}
	
}
