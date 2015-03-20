/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.commons.serializer;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.tools.scheduler.to.JobInfo;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class TriggerJSONSerializer implements Serializer {
	
	private static transient Logger logger = Logger.getLogger(TriggerJSONSerializer.class);

	public static final String JOB_NAME = "jobName";
	public static final String JOB_GROUP = "jobGroup";
	public static final String TRIGGER_NAME = "triggerName";
	public static final String TRIGGER_GROUP = "triggerGroup";
	public static final String TRIGGER_DESCRIPTION = "triggerDescription";	
	public static final String TRIGGER_CALENDAR_NAME = "triggerCalendarName";
	public static final String TRIGGER_START_DATE = "triggerStartDate";
	public static final String TRIGGER_START_TIME = "triggerStartTime";	
	public static final String TRIGGER_END_DATE = "triggerEndDate";
	public static final String TRIGGER_END_TIME = "triggerEndTime";	
	public static final String TRIGGER_CHRON_STRING = "triggerChronString";	
	public static final String TRIGGER_IS_PAUSED = "triggerIsPaused";	

	public static final String JOB_PARAMETERS = "jobParameters";

	public Object serialize(Object o, Locale locale)
	throws SerializationException {
		JSONObject  result = null;
		if( !(o instanceof Trigger) ) {
			throw new SerializationException("TriggerJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		try {
			Trigger trigger = (Trigger)o;
			result = new JSONObject();
			
			//Job Name and Job Group at which this Trigger belongs to
			String jobName = (trigger.getJob().getName() != null)? trigger.getJob().getName():"";
			String jobGroup = (trigger.getJob().getGroupName() != null)? trigger.getJob().getGroupName():"";
			result.put(JOB_NAME, jobName);
			result.put(JOB_GROUP, jobGroup);
			
			String triggerName = (trigger.getName() != null)? trigger.getName(): "";
			String triggerGroup = (trigger.getGroupName() != null)? trigger.getGroupName(): "";
			String triggerDescription = (trigger.getDescription() != null)? trigger.getDescription(): "";
			result.put(TRIGGER_NAME, triggerName);
			result.put(TRIGGER_GROUP, triggerGroup);
			result.put(TRIGGER_DESCRIPTION, triggerDescription);
			
			Date triggerStartTime = trigger.getStartTime();
			String triggerStartDateSerialized = "";
			String triggerStartTimeSerialized = "";
			if(triggerStartTime != null) {
				triggerStartDateSerialized = serializeDate(triggerStartTime);
				triggerStartTimeSerialized = serializeTime(triggerStartTime);
			}
			Date triggerEndTime = trigger.getEndTime();
			String triggerEndDateSerialized = "";
			String triggerEndTimeSerialized = "";
			if(triggerEndTime != null) {
				triggerEndDateSerialized = serializeDate(triggerEndTime);
				triggerEndTimeSerialized = serializeTime(triggerEndTime);
			}
			result.put(TRIGGER_CALENDAR_NAME, triggerName);
			result.put(TRIGGER_START_DATE, triggerStartDateSerialized);
			result.put(TRIGGER_START_TIME, triggerStartTimeSerialized);
			result.put(TRIGGER_END_DATE, triggerEndDateSerialized);
			result.put(TRIGGER_END_TIME, triggerEndTimeSerialized);
			
			String triggerCronExpression =  ((trigger.getChronExpression().getExpression()) != null)? trigger.getChronExpression().getExpression():"";
			result.put(TRIGGER_CHRON_STRING, triggerCronExpression);
			
			ISchedulerDAO schedulerDAO;
			boolean isTriggerPaused = false;
			try {
				schedulerDAO = DAOFactory.getSchedulerDAO();
				isTriggerPaused = schedulerDAO.isTriggerPaused(triggerGroup, triggerName, jobGroup, jobName);
			} catch (EMFUserError e) {
				logger.error("Error while checking if the trigger ["+triggerName+"] is paused");
			}
			result.put(TRIGGER_IS_PAUSED, isTriggerPaused);
			
			
			//Job parameter for trigger details
			JSONArray parsListJSON = new JSONArray();

			Map<String,String> jobParameters = trigger.getJob().getParameters();
			Set<String> jobParametersName = jobParameters.keySet();
			for (String jobParameterName : jobParametersName) {
				String jobParameterValue = jobParameters.get(jobParameterName);
				if (jobParameterValue == null) {
					logger.warn("Job parameter [" + jobParameterName + "] has no value");
					jobParameterValue = "";
				}
				JSONObject jsonPar = new JSONObject();
				jsonPar.put("name", jobParameterName);
				jsonPar.put("value", jobParameterValue);
				parsListJSON.put(jsonPar);
				
				
				
			}
			result.put(JOB_PARAMETERS, parsListJSON);	


			
		}catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {

			
		}
		
		return result;
	}
	
	public String serializeTime(Date date) {
		String serializedTime;
		
		serializedTime = null;
		Calendar startCal = new GregorianCalendar();
		startCal.setTime(date);
		
		int hour = startCal.get(Calendar.HOUR_OF_DAY);
		int minute = startCal.get(Calendar.MINUTE);
		// hour format: hh:mm
		serializedTime = ((hour < 10) ? "0" : "") + hour + ":" + ((minute < 10) ? "0" : "") + minute;
		
		return serializedTime;
	}
	
	public String serializeDate(Date date) {
		String serializedDate;
	
		serializedDate = null;
		Calendar startCal = new GregorianCalendar();
		startCal.setTime(date);
		// date format: dd/mm/yyyy
		int day = startCal.get(Calendar.DAY_OF_MONTH);
		int month = startCal.get(Calendar.MONTH);
		int year = startCal.get(Calendar.YEAR);
		serializedDate = ((day < 10) ? "0" : "") + day + 
					"/" + 
					((month + 1 < 10) ? "0" : "") + (month + 1) + 
					"/" + 
					year;
		
		return serializedDate;
	}
}
