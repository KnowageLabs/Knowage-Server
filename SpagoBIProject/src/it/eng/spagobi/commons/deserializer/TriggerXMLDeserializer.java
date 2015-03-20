/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.deserializer;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.tools.scheduler.bo.CronExpression;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUIDGenerator;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class TriggerXMLDeserializer implements Deserializer {
	
	public static final String PROPERTY_CONSUMER = "consumer";
	
	public static String TRIGGER_NAME = "triggerName";
	public static String TRIGGER_GROUP = "triggerGroup";
	public static String TRIGGER_DESCRIPTION = "triggerDescription";
	
	public static String TRIGGER_START_DATE = "startDate";
	public static String TRIGGER_START_TIME = "startTime";
	public static String TRIGGER_END_DATE = "endDate";
	public static String TRIGGER_END_TIME = "endTime";
	public static String TRIGGER_RUN_IMMEDIATELY = "runImmediately";
	
	
	
	public static String JOB_NAME = "jobName";
	public static String JOB_GROUP = "jobGroup";
	public static String JOB_PARAMETERS = "PARAMETERS";
	
	public static String CRON_STRING = "chronString";
	
	
	

	private static Logger logger = Logger.getLogger(TriggerXMLDeserializer.class);
	  
	public Object deserialize(Object o, Class clazz) throws DeserializationException {
		
		it.eng.spagobi.tools.scheduler.bo.Trigger trigger;
		Job job;
		
		String triggerName;
		String triggerGroupName;
		String triggerDescription;
		Date startTime;
		Date endTime;
		String jobName;
		String jobGroup;
		String cronString;
		
		Map<String, String> jobParameters;
	
		
		logger.debug("IN");
		
		trigger = null;		
		
		try {
			Assert.assertNotNull(o, "Input parameter [" + o + "] cannot be null");
			Assert.assertNotNull(o, "Input parameter [" + clazz + "] cannot be null");
			
			SourceBean xml = null;
			if(o instanceof SourceBean) {
				xml = (SourceBean)o;
			} else if (o instanceof String) {
				xml = SourceBean.fromXMLString( (String)o);
			} else {
				throw new DeserializationException("Impossible to deserialize from an object of type [" + o.getClass().getName() +"]");
			}
			
			boolean runImmediately = deserializeRunImmediatelyAttribute(xml);
			if(runImmediately) {
				
				triggerName = "schedule_uuid_" + UUIDGenerator.getInstance().generateTimeBasedUUID().toString();
				triggerGroupName = null;
				triggerDescription = null;
				startTime = null;
				endTime = null;
				cronString = null;
				
				jobName = (String)xml.getAttribute( JOB_NAME );
				jobGroup = (String)xml.getAttribute( JOB_GROUP );
				jobParameters = deserializeParametersAttribute(xml);
				
			} else {
			
				triggerName = (String)xml.getAttribute( TRIGGER_NAME );
				triggerGroupName = (String)xml.getAttribute( TRIGGER_GROUP );
				triggerDescription = (String)xml.getAttribute( TRIGGER_DESCRIPTION );
				startTime = deserializeStartTimeAttribute(xml);
				endTime = deserializeEndTimeAttribute(xml);
				cronString = (String) xml.getAttribute(CRON_STRING);
				
				jobName = (String)xml.getAttribute( JOB_NAME );
				jobGroup = (String)xml.getAttribute( JOB_GROUP );
				jobParameters = deserializeParametersAttribute(xml);
			}
			
			
			trigger = new it.eng.spagobi.tools.scheduler.bo.Trigger();
			
			trigger.setName(triggerName);
			trigger.setDescription(triggerDescription);
			trigger.setGroupName(triggerGroupName);
			
			trigger.setRunImmediately(runImmediately);
			
			
			trigger.setStartTime(startTime);
			trigger.setEndTime(endTime);
			trigger.setCronExpression( new CronExpression(cronString) );
			
			job = new Job();
			job.setName(jobName);
			job.setGroupName(jobGroup);
			job.addParameters(jobParameters);
		    job.setVolatile(false);
		  
		    trigger.setJob(job);
		    
		} catch (Throwable t) {
			throw new DeserializationException("An error occurred while deserializing object: " + o, t);
		} finally {
			logger.debug("OUT");
		}
		
		return trigger;
	}
	
	
	private boolean deserializeRunImmediatelyAttribute(SourceBean xml) {
		boolean runImmediately;
		
		runImmediately = false;
		
		String runImmediatelyStr = (String)xml.getAttribute( TRIGGER_RUN_IMMEDIATELY );
		if((runImmediatelyStr!=null) && (runImmediatelyStr.trim().equalsIgnoreCase("true"))) {
			runImmediately = true;
		}
		
		return runImmediately;
	}
	
	// NOTE: start date and and date are encoded with different formats. That's sick!
	
	/**
	 * get the start date param (format dd-mm-yyyy) and end time (format hh:mm:ss)
	 */
	private Date deserializeStartTimeAttribute(SourceBean xml) {
		Calendar calendar;
		
		String startDateStr = (String)xml.getAttribute( TRIGGER_START_DATE );
		String startTimeStr = (String)xml.getAttribute( TRIGGER_START_TIME );
		
		String startDay = startDateStr.substring(0,2);
		String startMonth = startDateStr.substring(3, 5);
		String startYear = startDateStr.substring(6,10);
		
		calendar = new GregorianCalendar(new Integer(startYear).intValue(), 
				                                  new Integer(startMonth).intValue()-1, 
				                                  new Integer(startDay).intValue());
		if(startTimeStr != null) {
			String startHour = startTimeStr.substring(0, 2);
			String startMinute = startTimeStr.substring(3, 5);
			calendar.set(calendar.HOUR_OF_DAY, new Integer(startHour).intValue());
			calendar.set(calendar.MINUTE, new Integer(startMinute).intValue());
		}
		
		return calendar != null? calendar.getTime(): null;
	}
	
	/**
	 * get the end date param (format yyyy-mm-gg) and end time (format hh:mm:ss)
	 */
	private Date deserializeEndTimeAttribute(SourceBean xml) {
		Calendar calendar = null;
		String endDateStr = (String)xml.getAttribute( TRIGGER_END_DATE );
		if(endDateStr!=null){
			String endDay = endDateStr.substring(8);
			String endMonth = endDateStr.substring(5, 7);
			String endYear = endDateStr.substring(0, 4);
			calendar = new GregorianCalendar(new Integer(endYear).intValue(), 
				                           new Integer(endMonth).intValue()-1, 
				                           new Integer(endDay).intValue());
			
			String endTimeStr = (String)xml.getAttribute( TRIGGER_END_TIME );
			if(endTimeStr!=null) {
				String endHour = endTimeStr.substring(0, 2);
				String endMinute = endTimeStr.substring(3, 5);
				calendar.set(calendar.HOUR_OF_DAY, new Integer(endHour).intValue());
				calendar.set(calendar.MINUTE, new Integer(endMinute).intValue());
			}
		}
		
		return calendar != null? calendar.getTime(): null;
	}
	
	private  Map<String, String> deserializeParametersAttribute(SourceBean xml) {
		Map<String, String> parameters = new HashMap<String, String>();
		
		SourceBean jobParameters = (SourceBean)xml.getAttribute( JOB_PARAMETERS );
		
		parameters.put("empty", "empty");
		if(jobParameters != null) {
			List paramsSB = jobParameters.getContainedAttributes();
			Iterator iterParSb = paramsSB.iterator();
			while(iterParSb.hasNext()) {
					SourceBeanAttribute paramSBA = (SourceBeanAttribute)iterParSb.next();
					String nameAttr = (String)paramSBA.getKey();
					if(nameAttr.equalsIgnoreCase("PARAMETER")) {
						SourceBean paramSB = (SourceBean)paramSBA.getValue();
						String name = (String)paramSB.getAttribute("name");
						String value = (String)paramSB.getAttribute("value");
						parameters.put(name, value);
					}
			}
		}
		return parameters;
	}
	
	

}
