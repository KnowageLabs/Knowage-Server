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
package it.eng.spagobi.tools.scheduler.to;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;


public class TriggerInfo implements Serializable{


	private String triggerName = "";
	private String triggerDescription = "";
	/**
	 * @deprecated Prefer {@link #zonedStartTime}
	 */
	@Deprecated
	private String startDate = "";
	/**
	 * @deprecated Prefer {@link #zonedStartTime}
	 */
	@Deprecated
	private String startTime = "";
	private String chronString = "";
	/**
	 * @deprecated Prefer {@link #zonedEndTime}
	 */
	@Deprecated
	private String endDate = "";
	/**
	 * @deprecated Prefer {@link #zonedEndTime}
	 */
	@Deprecated
	private String endTime = "";
	private String repeatInterval = "";
	private JobInfo jobInfo = null;
	private Map<String, DispatchContext> saveOptions = null;
	private ZonedDateTime zonedStartTime = null;
	private ZonedDateTime zonedEndTime = null;

	/**
	 * Gets the end date.
	 *
	 * @return the end date
	 * @deprecated Prefer {@link #getZonedEndTime()}
	 */
	@Deprecated
	public String getEndDate() {
		return endDate;
	}

	/**
	 * Sets the end date.
	 *
	 * @param endDate the new end date
	 * @deprecated Prefer {@link #setZonedEndTime(ZonedDateTime)}
	 */
	@Deprecated
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	/**
	 * Gets the end time.
	 *
	 * @return the end time
	 * @deprecated Prefer {@link #getZonedEndTime()}
	 */
	@Deprecated
	public String getEndTime() {
		return endTime;
	}

	/**
	 * Sets the end time.
	 *
	 * @param endTime the new end time
	 * @deprecated Prefer {@link #setZonedEndTime(ZonedDateTime)}
	 */
	@Deprecated
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
	 * @deprecated Prefer {@link #getZonedStartTime()}
	 */
	@Deprecated
	public String getStartDate() {
		return startDate;
	}

	/**
	 * Sets the start date.
	 *
	 * @param startDate the new start date
	 * @deprecated Prefer {@link #setZonedStartTime(ZonedDateTime)}
	 */
	@Deprecated
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 * @deprecated Prefer {@link #getZonedStartTime()}
	 */
	@Deprecated
	public String getStartTime() {
		return startTime;
	}

	/**
	 * Sets the start time.
	 *
	 * @param startTime the new start time
	 * @deprecated Prefer {@link #setZonedStartTime(ZonedDateTime)}
	 */
	@Deprecated
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

	public ZonedDateTime getZonedStartTime() {
		return zonedStartTime;
	}

	public void setZonedStartTime(ZonedDateTime zonedStartTime) {
		this.zonedStartTime = zonedStartTime;
	}

	public ZonedDateTime getZonedEndTime() {
		return zonedEndTime;
	}

	public void setZonedEndTime(ZonedDateTime zonedEndTime) {
		this.zonedEndTime = zonedEndTime;
	}

}
