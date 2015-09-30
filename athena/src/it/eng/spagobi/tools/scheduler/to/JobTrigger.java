/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.to;

import java.io.Serializable;

public class JobTrigger implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TRIGGER_NAME = "triggerName";
	public static final String TRIGGER_DESCRIPTION = "triggerDescription";
	public static final String IS_SUSPENDED = "isSuspended";
	public static final String START_DATE = "startDate";
	public static final String START_TIME = "startTime";
	public static final String END_DATE = "endDate";
	public static final String END_TIME = "endTime";

	public static final String CHRONO = "chronstring";
	public static final String CHRONO_TYPE = "type";
	public static final String CHRONO_PARAMETER = "parameter";

	// obj.get(CHRONO_TYPE) == ( "minute" || "hour" || "day" || "week" || "month" )
	// obj.get(CHRONO_TYPE) == "month" and day choice type IS simple
	public static final String CHRONO_PARAMETER_NUMREPETITION = "numRepetition";
	
	// obj.get(CHRONO_TYPE) == "month" and day choice type IS simple
	public static final String CHRONO_PARAMETER_DAYREPETITION = "dayRepetition";

	// obj.get(CHRONO_TYPE) == "week"
	// obj.get(CHRONO_TYPE) == "month" and month choice type IS NOT simple
	public static final String CHRONO_PARAMETER_DAYS = "days";

	// obj.get(CHRONO_TYPE) == "month" and day choice type IS NOT simple
	public static final String CHRONO_PARAMETER_WEEKS = "weeks";

	// obj.get(CHRONO_TYPE) == "month" and month choice type IS NOT simple
	public static final String CHRONO_PARAMETER_MONTHS = "months";

	private String triggerName = "";
	private String triggerDescription = "";
	private Boolean isSuspended = null;
	private String startDate = "";
	private String startTime = "";
	private String endDate = "";
	private String endTime = "";
	private Integer chronoParameterNumRepetition = null;
	private Integer chronoParameterDayRepetition = null;
	private String[] chronoParameterDayS = null;
	private String[] chronoParameterWeeks = null;
	private String[] chronoParameterMonths = null;

	// "single", "minute", "hour", "day", "week", "month", "event"
	private String chronoType = "";

	public String getChronoType() {
		return chronoType;
	}

	public void setChronoType(String chronoType) {
		this.chronoType = chronoType;
	}

	private String chrono = "";
	private String repeatInterval = "";

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
	 * @param endDate
	 *            the new end date
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
	 * @param endTime
	 *            the new end time
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
	 * @param repeatInterval
	 *            the new repeat interval
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
	 * @param startDate
	 *            the new start date
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
	 * @param startTime
	 *            the new start time
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
	 * @param triggerDescription
	 *            the new trigger description
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
	 * @param triggerName
	 *            the new trigger name
	 */
	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	/**
	 * Gets the isSuspended value.
	 *
	 */
	public Boolean getIsSuspended() {
		return isSuspended;
	}

	/**
	 * Sets the isSuspended value.
	 *
	 * @param isSuspended
	 */
	public void setIsSuspended(Boolean isSuspended) {
		this.isSuspended = isSuspended;
	}

	/**
	 * Gets the chron string.
	 *
	 * @return the chron string
	 */
	public String getChrono() {
		return chrono;
	}

	/**
	 * Sets the chron string.
	 *
	 * @param chrono
	 *            the new chron string
	 */
	public void setChrono(String chrono) {
		this.chrono = chrono;
	}

	/**
	 * Gets the start date rf c3339.
	 *
	 * @return the start date rf c3339
	 */
	public String getStartDateRFC3339() {
		String startDRFC = "";
		String startD = this.getStartDate();
		if ((startD != null) && !startD.trim().equals("")) {
			String[] dateParts = startD.split("/");
			startDRFC = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
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
		if ((endD != null) && !endD.trim().equals("")) {
			if (endD.indexOf("/") > 0) {
				String[] dateParts = endD.split("/");
				endDRFC = dateParts[2] + "-" + dateParts[1] + "-" + dateParts[0];
			} else {
				endDRFC = endD;
			}
		}
		return endDRFC;
	}

	public String getSchedulingMessage() {
		// TODO Auto-generated method stub
		return null;
	}

}
