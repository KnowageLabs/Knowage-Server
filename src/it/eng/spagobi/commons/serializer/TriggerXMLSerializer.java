/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.scheduler.bo.Trigger;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class TriggerXMLSerializer implements Serializer {
	
	Properties properties;
	
	public static final String PROPERTY_CONSUMER = "consumer";
	
	public static final String TRIGGER_NAME = "triggerName";
	public static final String TRIGGER_GROUP = "triggerGroup";
	public static final String TRIGGER_DESCRIPTION = "triggerDescription";	
	public static final String TRIGGER_CALENDAR_NAME = "triggerCalendarName";
	public static final String TRIGGER_START_DATE = "triggerStartDate";
	public static final String TRIGGER_START_TIME = "triggerStartTime";	
	public static final String TRIGGER_END_DATE = "triggerEndDate";
	public static final String TRIGGER_END_TIME = "triggerEndTime";	
		
	private static Logger logger = Logger.getLogger(TriggerXMLSerializer.class);
	
	public TriggerXMLSerializer() {
		properties = new Properties();
	}
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		StringBuffer  result;
		
		logger.debug("IN");		
		
		result = null;
		try {
			if( !(o instanceof Trigger) ) {
				throw new SerializationException("TriggerXMLSerializer is unable to serialize object of type: " + o.getClass().getName());
			}
			
			Trigger trigger = (Trigger)o;
			result = new StringBuffer();
			
			String triggerName = (trigger.getName() != null)? trigger.getName(): "";
			String triggerGroup = (trigger.getGroupName() != null)? trigger.getGroupName(): "";
			String triggerDescription = (trigger.getDescription() != null)? trigger.getDescription(): "";
			
			Date triggerStartTime = trigger.getStartTime();
			String triggerStartDateSerialized = "";
			String triggerStartTimeSerialized = "";
			if(triggerStartTime != null) {
				triggerStartDateSerialized = serailizeDate(triggerStartTime);
				triggerStartTimeSerialized = serailizeTime(triggerStartTime);
			}
			Date triggerEndTime = trigger.getEndTime();
			String triggerEndDateSerialized = "";
			String triggerEndTimeSerialized = "";
			if(triggerEndTime != null) {
				triggerEndDateSerialized = serailizeDate(triggerEndTime);
				triggerEndTimeSerialized = serailizeTime(triggerEndTime);
			}
			
			boolean isForServiceConsumer = "service".equalsIgnoreCase( properties.getProperty( PROPERTY_CONSUMER ) );
			
			String rootTag = "ROW";
			if( isForServiceConsumer ) rootTag = "TRIGGER_DETAILS";
			
			result.append("<" + rootTag + " ");
			result.append(" " + TRIGGER_NAME + "=\"" + triggerName + "\"");
			result.append(" " + TRIGGER_GROUP + "=\"" + triggerGroup + "\"");
			result.append(" " + TRIGGER_DESCRIPTION + "=\"" + triggerDescription + "\"");
			result.append(" " + TRIGGER_START_DATE + "=\"" + triggerStartDateSerialized + "\"");
			result.append(" " + TRIGGER_START_TIME + "=\"" + triggerStartTimeSerialized + "\"");
			result.append(" " + TRIGGER_END_DATE + "=\"" + triggerEndDateSerialized + "\"");
			result.append(" " + TRIGGER_END_TIME + "=\"" + triggerEndTimeSerialized + "\"");
			if( isForServiceConsumer ) {
				Map<String,String> jobParameters = trigger.getJob().getParameters();
				String triggerCronExpression =  trigger.getChronExpression().getExpression();
				result.append(" triggerChronString=\"" + triggerCronExpression + "\"");
				result.append(" >");	
				
				result.append("<JOB_PARAMETERS>");
				
				Set<String> jobParametersName = jobParameters.keySet();
				for (String jobParameterName : jobParametersName) {
					String jobParameterValue = jobParameters.get(jobParameterName);
					// already extracted and processed
//					if(jobParameterName.equals("chronString")) {
//						continue;
//					}
					
					result.append("<JOB_PARAMETER ");
					if (jobParameterValue == null) {
						logger.warn("Job parameter [" + jobParameterName + "] has no value");
					}
					result.append(" name=\"" + jobParameterName + "\"");
					result.append(" value=\"" + jobParameterValue + "\"");
					result.append(" />");
				}
		
				result.append("</JOB_PARAMETERS>");
				result.append("</TRIGGER_DETAILS>");
				
			} else {
				result.append(" />");
			}
			
			
		} catch (SerializationException t) {
			throw t;
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			logger.debug("OUT");	
		}
		
		return result.toString();
	}
	
	public String serailizeTime(Date date) {
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
	
	public String serailizeDate(Date date) {
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

	
	public void setProperty(String name, String value) {
		properties.setProperty(name, value);
	}
	
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
}
