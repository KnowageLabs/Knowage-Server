/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.scheduler.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.base.SourceBeanException;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.impl.StdSchedulerFactory;
import org.safehaus.uuid.UUIDGenerator;


public class SchedulerServiceSupplier implements ISchedulerServiceSupplier{

    static private Logger logger = Logger.getLogger(SchedulerServiceSupplier.class);
    
	
	
	/**
	 * Gets the job list.
	 * 
	 * @return the job list
	 */
	public String getJobList() {
		String xml = "";
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler(); 
			List toReturn = new ArrayList();
			String[] groups = scheduler.getJobGroupNames();
			if (groups == null || groups.length == 0) {
				SpagoBITracer.warning(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
						              "getJobList", "No job groups defined!");
			} else {
				for (int i = 0; i < groups.length; i++) {
					String group = groups[i];
					String[] jobNames = scheduler.getJobNames(group);
					if (jobNames == null || jobNames.length == 0) {
						SpagoBITracer.warning(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
								              "getJobList", "No job defined for group " + group + "!");
					} else {
						for (int j = 0; j < jobNames.length; j++) {
							JobDetail aJob = scheduler.getJobDetail(jobNames[j], group);
							toReturn.add(aJob);
						}
					}
				}
			}
			xml = buildJobListXmlString(toReturn);
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
		              			"getJobList", "Error while recovering job list");
			xml = "<ROWS></ROWS>";
		}
		return xml;
	}
	
	
	
	/**
	 * Builds the job list xml string.
	 * 
	 * @param toReturn the to return
	 * 
	 * @return the string
	 * 
	 * @throws SourceBeanException the source bean exception
	 */
	public  String buildJobListXmlString(List toReturn) throws SourceBeanException {
		StringBuffer buffer = new StringBuffer("<ROWS>");
		Iterator it = toReturn.iterator();
		while (it.hasNext()) {
			JobDetail job = (JobDetail) it.next();
			String jobName = job.getName();
			String jobGroupName = job.getGroup();
			String jobDescription = job.getDescription();
			String jobClassName = job.getJobClass().getName();
			String jobDurability = job.isDurable() ? "true" : "false";
			String jobRequestRecovery = job.requestsRecovery() ? "true" : "false";
			String jobVolatility = job.isVolatile() ? "true" : "false";
			buffer.append("<ROW ");
			buffer.append(" jobName=\"" + (jobName != null ? jobName : "") + "\"");
			buffer.append(" jobGroupName=\"" + (jobGroupName != null ? jobGroupName : "") + "\"");
			buffer.append(" jobDescription=\"" + (jobDescription != null ? jobDescription : "") + "\"");
			buffer.append(" jobClass=\"" + (jobClassName != null ? jobClassName : "") + "\"");
			buffer.append(" jobDurability=\"" + jobDurability + "\"");
			buffer.append(" jobRequestRecovery=\"" + jobRequestRecovery + "\"");
			buffer.append(" jobVolatility=\"" + jobVolatility + "\"");
			buffer.append(" />");
		}
		buffer.append("</ROWS>");
		return buffer.toString();
	}
	
	
	/**
	 * Gets the job schedulation list.
	 * 
	 * @param jobName the job name
	 * @param jobGroup the job group
	 * 
	 * @return the job schedulation list
	 */
	public   String getJobSchedulationList(String jobName, String jobGroup) {
		String xml = "";
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler(); 
			if (jobName == null || jobName.trim().equals("")) {
				SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getJobDefinition", 
									  "Missing job name request parameter!");
				throw new Exception("Job name not found !");
			}
			if (jobGroup == null || jobGroup.trim().equals("")) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getJobDefinition", 
									"Missing job group name! Using default group...");
				jobGroup = Scheduler.DEFAULT_GROUP;
			}
			Trigger[] triggers = scheduler.getTriggersOfJob(jobName, jobGroup);
			xml = buildTriggersListXmlString(triggers);
		} catch (Exception e) {
			SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getJobDefinition", 
								"Error while recovering job schedulation list ", e);
			xml = "<ROWS></ROWS>";
		}
		return xml;
	}
	
	
	/**
	 * Builds the triggers list xml string.
	 * 
	 * @param triggers the triggers
	 * 
	 * @return the string
	 * 
	 * @throws SourceBeanException the source bean exception
	 */
	public  String buildTriggersListXmlString(Trigger[] triggers) throws SourceBeanException {
		StringBuffer buffer = new StringBuffer("<ROWS>");
		if (triggers != null && triggers.length > 0) {
			for (int i = 0; i < triggers.length; i++) {
				Trigger trigger = triggers[i];
				if(trigger.getName().startsWith("schedule_uuid_")) {
					continue;
				}
				buffer.append("<ROW ");
				String triggerName = trigger.getName();
				String triggerGroup = trigger.getGroup();
				String triggerDescription = trigger.getDescription();
				String triggerCalendarName = trigger.getCalendarName();
				Date triggerStartTime = trigger.getStartTime();
				String triggerStartTimeStr = triggerStartTime != null ? triggerStartTime.toString(): "";
				Date triggerEndTime = trigger.getEndTime();
				String triggerEndTimeStr = triggerEndTime != null ? triggerEndTime.toString(): "";
				buffer.append(" triggerName=\"" + (triggerName != null ? triggerName : "") + "\"");
				buffer.append(" triggerGroup=\"" + (triggerGroup != null ? triggerGroup : "") + "\"");
				buffer.append(" triggerDescription=\"" + (triggerDescription != null ? triggerDescription : "") + "\"");
				buffer.append(" triggerCalendarName=\"" + (triggerCalendarName != null ? triggerCalendarName : "") + "\"");
				buffer.append(" triggerStartTime=\"" + triggerStartTimeStr + "\"");
				buffer.append(" triggerEndTime=\"" + triggerEndTimeStr + "\"");
				buffer.append(" />");
			}
		}
		buffer.append("</ROWS>");
		return buffer.toString();
	}
	
	
	/**
	 * Delete schedulation.
	 * 
	 * @param triggerName the trigger name
	 * @param triggerGroup the trigger group
	 * 
	 * @return the string
	 */
	public   String deleteSchedulation(String triggerName, String triggerGroup) {
		StringBuffer servreponse = new StringBuffer();
		try{
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler(); 
			servreponse.append("<EXECUTION_OUTCOME ");
			try {
				scheduler.unscheduleJob(triggerName, triggerGroup);
			} catch (SchedulerException e) {
				SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
									   "deleteSchedulation", "Error while deleting trigger", e);
				throw e;
			}
		} catch (Exception e) {
			servreponse.append("outcome=\"fault\"/>");
		}
		servreponse.append("outcome=\"perform\"/>");
		return servreponse.toString();
	}
	
	
	
	/**
	 * Delete job.
	 * 
	 * @param jobName the job name
	 * @param jobGroupName the job group name
	 * 
	 * @return the string
	 */
	public   String deleteJob(String jobName, String jobGroupName) {
		StringBuffer servreponse = new StringBuffer();
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler(); 
			servreponse.append("<EXECUTION_OUTCOME ");
			scheduler.deleteJob(jobName, jobGroupName);
			servreponse.append("outcome=\"perform\"/>");
		} catch (SchedulerException e) {
			SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "deleteJob", 
					"Error while deleting job", e);
			servreponse.append("outcome=\"fault\"/>");
		}	
		return servreponse.toString();
	}
	
	
	
	/**
	 * Define job.
	 * 
	 * @param xmlRequest the xml request
	 * 
	 * @return the string
	 */
	public   String defineJob(String xmlRequest) {
		StringBuffer servreponse = new StringBuffer();
		try{
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			logger.debug("Obtained scheduler from factory");
			SourceBean request = SourceBean.fromXMLString(xmlRequest);
			logger.debug("Got source bean from xml request:"+xmlRequest);
			servreponse.append("<EXECUTION_OUTCOME ");
			// READ REQUEST
			String jobName = (String)request.getAttribute("jobName");
			logger.debug("jobName:"+jobName);
			String jobgroupName = (String)request.getAttribute("jobGroupName");
			logger.debug("jobgroupName:"+jobgroupName);
			if(jobgroupName==null)
				jobgroupName = Scheduler.DEFAULT_GROUP;
			String jobDescription = (String)request.getAttribute("jobDescription");
			logger.debug("jobDescription:"+jobDescription);
			if(jobDescription==null)
				jobDescription = "";
			String jobRequestRecoveryStr = (String)request.getAttribute("jobRequestRecovery");
			logger.debug("jobRequestRecoveryStr:"+jobRequestRecoveryStr);
			boolean jobRequestRecovery = false;
			if((jobRequestRecoveryStr!=null) && (jobRequestRecoveryStr.trim().equalsIgnoreCase("true")))
				jobRequestRecovery = true;
			SourceBean jobParameters = (SourceBean)request.getAttribute("PARAMETERS");
			logger.debug("got job parameters");
			// transform parameters sourcebean into JobDataMap structure and set it into the jobDetail
			JobDataMap jdm = getJobDataMap(jobParameters);
			logger.debug("got JobDataMap");
			// get the job class
			String jobClassName = (String)request.getAttribute("jobClass");
			logger.debug("jobClassName:"+jobClassName);
			Class jobClass = null;
			try {
				jobClass = Class.forName(jobClassName);
			} catch (ClassNotFoundException e) {
				SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE+"(SCHEDULER)", this.getClass().getName(), 
						               "defineJob", "Class '" + jobClassName + "' not found for job with name '" + jobName + "' of group '" + jobgroupName + "'!");
				throw e;
			}
			logger.debug("CREATE JOB DETAIL BEGIN");
			// CREATE JOB DETAIL 
			JobDetail jobDetail = new JobDetail();
			jobDetail.setName(jobName);
			jobDetail.setGroup(jobgroupName);
			jobDetail.setDescription(jobDescription);
			jobDetail.setDurability(true);
			jobDetail.setVolatility(false);
			jobDetail.setRequestsRecovery(jobRequestRecovery);
			jobDetail.setJobDataMap(jdm);
			jobDetail.setJobClass(jobClass);
			logger.debug("CREATE JOB DETAIL END");
			// ADD JOB
			try {
				scheduler.addJob(jobDetail, true);
				logger.debug("ADDED JOB TO SCHEDULER");
			} catch (SchedulerException e) {
				logger.error("Error while adding job to the scheduler", e);
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE+"(SCHEDULER)", this.getClass().getName(), 
						               "defineJob", "Error while adding job to the scheduler", e);
				throw e;
			}
			servreponse.append("outcome=\"perform\"/>");
		} catch (Exception e) {
			servreponse.append("outcome=\"fault\"/>");
		}
		return servreponse.toString();
	}
	
	
	/**
	 * Gets the job data map.
	 * 
	 * @param jobParameters the job parameters
	 * 
	 * @return the job data map
	 */
	public  JobDataMap getJobDataMap(SourceBean jobParameters) {
		JobDataMap jdm = new JobDataMap();
		jdm.put("empty", "empty");
		if(jobParameters!=null) {
			List paramsSB = jobParameters.getContainedAttributes();
			Iterator iterParSb = paramsSB.iterator();
			while(iterParSb.hasNext()) {
					SourceBeanAttribute paramSBA = (SourceBeanAttribute)iterParSb.next();
					String nameAttr = (String)paramSBA.getKey();
					if(nameAttr.equalsIgnoreCase("PARAMETER")) {
						SourceBean paramSB = (SourceBean)paramSBA.getValue();
						String name = (String)paramSB.getAttribute("name");
						String value = (String)paramSB.getAttribute("value");
						jdm.put(name, value);
					}
			}
		}
		return jdm;
	}
	
	
	
	/**
	 * Gets the job definition.
	 * 
	 * @param jobName the job name
	 * @param jobGroup the job group
	 * 
	 * @return the job definition
	 */
	public   String getJobDefinition(String jobName, String jobGroup) {
		String jobStr = "";
		try{
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			if (jobName == null || jobName.trim().equals("")) {
				SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
									   "getJobDefinition", "Missing job name request parameter!");
				throw new Exception("Missing job name request parameter!");
			}
			if (jobGroup == null || jobGroup.trim().equals("")) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
									"getJobDefinition", "Missing job group name! Using default group...");
				jobGroup = Scheduler.DEFAULT_GROUP;
			}
			JobDetail aJob = scheduler.getJobDetail(jobName, jobGroup);
			if (aJob == null) {
				SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "getJobDefinition", 
									   "Job with name '" + jobName + "' not found in group '" + jobGroup + "'!");
				throw new Exception("Job with name '" + jobName + "' not found in group '" + jobGroup + "'!");
			}
			jobStr = serializeJobDetail(aJob);
		} catch (Exception e) {
			SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
					   			   "getJobDefinition", "Error while recovering job definition");
		}
		return jobStr;
	}
	
	
	/**
	 * Serialize job detail.
	 * 
	 * @param job the job
	 * 
	 * @return the string
	 * 
	 * @throws SourceBeanException the source bean exception
	 */
	public  String serializeJobDetail(JobDetail job) throws SourceBeanException {
		StringBuffer buffer = new StringBuffer("<JOB_DETAIL ");
		String jobName = job.getName();
		String jobGroupName = job.getGroup();
		String jobDescription = job.getDescription();
		String jobClassName = job.getJobClass().getName();
		String jobDurability = job.isDurable() ? "true" : "false";
		String jobRequestRecovery = job.requestsRecovery() ? "true" : "false";
		String jobVolatility = job.isVolatile() ? "true" : "false";
		JobDataMap jobDataMap = job.getJobDataMap();
		buffer.append(" jobName=\"" + (jobName != null ? jobName : "") + "\"");
		buffer.append(" jobGroupName=\"" + (jobGroupName != null ? jobGroupName : "") + "\"");
		buffer.append(" jobDescription=\"" + (jobDescription != null ? jobDescription : "") + "\"");
		buffer.append(" jobClass=\"" + (jobClassName != null ? jobClassName : "") + "\"");
		buffer.append(" jobDurability=\"" + jobDurability + "\"");
		buffer.append(" jobRequestRecovery=\"" + jobRequestRecovery + "\"");
		buffer.append(" jobVolatility=\"" + jobVolatility + "\"");
		buffer.append(" >");
		buffer.append("<JOB_PARAMETERS>");
		if (jobDataMap != null && !jobDataMap.isEmpty()) {
			String[] keys = jobDataMap.getKeys();
			if (keys != null && keys.length > 0) {
				for (int i = 0; i < keys.length; i++) {
					buffer.append("<JOB_PARAMETER ");
					String key = keys[i];
					String value = jobDataMap.getString(key);
					if (value == null) {
						SpagoBITracer.warning("SCHEDULER", this.getClass().getName(), "loadJobDetailIntoResponse", 
						"Job parameter '" + key + "' has no String value!!");	
					}
					buffer.append(" name=\"" + key + "\"");
					buffer.append(" value=\"" + value + "\"");
					buffer.append(" />");
				}
			}
		}
		buffer.append("</JOB_PARAMETERS>");
		buffer.append("</JOB_DETAIL>");
		return buffer.toString();
	}
	
	
	
	/**
	 * Schedule job.
	 * 
	 * @param xmlRequest the xml request
	 * 
	 * @return the string
	 */
	public String scheduleJob(String xmlRequest) {
		StringBuffer servreponse = new StringBuffer();
		try{
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			SourceBean request = SourceBean.fromXMLString(xmlRequest);
			servreponse.append("<EXECUTION_OUTCOME ");
			String runImmediately = (String) request.getAttribute("runImmediately");
			if( (runImmediately!=null) && runImmediately.equalsIgnoreCase("true")) {
				String jobName = (String) request.getAttribute("jobName");
				String jobGroup = (String) request.getAttribute("jobGroup");
				if(jobGroup==null)
					jobGroup = Scheduler.DEFAULT_GROUP;
				// recover scheduling parameters
				SourceBean jobParameters = (SourceBean)request.getAttribute("PARAMETERS");
				// transform parameters sourcebean into JobDataMap structure and set it into the jobDetail
				JobDataMap jdm = getJobDataMap(jobParameters);
				String nameTrig = "schedule_uuid_" + UUIDGenerator.getInstance().generateTimeBasedUUID().toString();
				Trigger trigger = TriggerUtils.makeImmediateTrigger(nameTrig, 0, 10000);
				trigger.setJobName(jobName);
			    trigger.setJobGroup(jobGroup);
				trigger.setJobDataMap(jdm);
				scheduler.scheduleJob(trigger);
			} else {
				String triggerName = (String) request.getAttribute("triggerName");
				String triggerDescription = (String) request.getAttribute("triggerDescription");
				String triggerGroup = (String) request.getAttribute("triggerGroup");
				if(triggerGroup==null)
					triggerGroup = Scheduler.DEFAULT_GROUP;
				String jobName = (String) request.getAttribute("jobName");
				String jobGroup = (String) request.getAttribute("jobGroup");
				if(jobGroup==null)
					jobGroup = Scheduler.DEFAULT_GROUP;
				// recover scheduling parameters
				SourceBean jobParameters = (SourceBean)request.getAttribute("PARAMETERS");
				// transform parameters sourcebean into JobDataMap structure and set it into the jobDetail
				JobDataMap jdm = getJobDataMap(jobParameters);
				// recover and transform dates
				// get the start date param (format yyyy-mm-gg) and start time (format hh:mm:ss....)
				String startDateStr = (String)request.getAttribute("startDate");
				String startTimeStr = (String)request.getAttribute("startTime");
				String startDay = startDateStr.substring(0,2);
				String startMonth = startDateStr.substring(3, 5);
				String startYear = startDateStr.substring(6,10);
				Calendar startCal = new GregorianCalendar(new Integer(startYear).intValue(), 
						                                  new Integer(startMonth).intValue()-1, 
						                                  new Integer(startDay).intValue());
				if(startTimeStr!=null) {
					String startHour = startTimeStr.substring(0, 2);
					String startMinute = startTimeStr.substring(3, 5);
					startCal.set(startCal.HOUR_OF_DAY, new Integer(startHour).intValue());
					startCal.set(startCal.MINUTE, new Integer(startMinute).intValue());
				}
				Date startDate = startCal.getTime();
				//	get the end date param (format yyyy-mm-gg) and end time (format hh:mm:ss)
				Date endDate = null;
				String endDateStr = (String)request.getAttribute("endDate");
				if(endDateStr!=null){
					String endDay = endDateStr.substring(8);
					String endMonth = endDateStr.substring(5, 7);
					String endYear = endDateStr.substring(0, 4);
					Calendar endCal = new GregorianCalendar(new Integer(endYear).intValue(), 
						                           new Integer(endMonth).intValue()-1, 
						                           new Integer(endDay).intValue());
					String endTimeStr = (String)request.getAttribute("endTime");
					if(endTimeStr!=null) {
						String endHour = endTimeStr.substring(0, 2);
						String endMinute = endTimeStr.substring(3, 5);
						endCal.set(endCal.HOUR_OF_DAY, new Integer(endHour).intValue());
						endCal.set(endCal.MINUTE, new Integer(endMinute).intValue());
					}
					endDate = endCal.getTime();
				}
				// get the chron string
				String chronStr = (String) request.getAttribute("chronString");
				// add chron string to job parameters
				jdm.put("chronString", chronStr);
				// get quartz chron expression
				String chronExp = getChronExpression(chronStr, startCal, startDate);
				Trigger trigger = null;
				if(chronExp==null) {
					trigger = new SimpleTrigger();
				} else {
					CronTrigger crontrigger = new CronTrigger();
					crontrigger.setCronExpression(chronExp);
					trigger = crontrigger;
				}
				trigger.setName(triggerName);
				trigger.setDescription(triggerDescription);
				trigger.setGroup(triggerGroup);
				
				trigger.setStartTime(startDate);
				if(endDate!=null) {
					trigger.setEndTime(endDate);
				}
				trigger.setJobName(jobName);
			    trigger.setJobGroup(jobGroup);
			    trigger.setJobDataMap(jdm);
			    trigger.setVolatility(false);    

				// check if the trigger already exists 
				boolean exists = false;
				Trigger[] jobTrgs = scheduler.getTriggersOfJob(jobName, jobGroup);
		        for(int ind=0; ind<jobTrgs.length; ind++) {
		           	Trigger trg = jobTrgs[ind];
		           	if(trg.getName().equals(triggerName)) {
		           		exists = true;
		           		break;
		           	}
		        }
			    // schedule trigger 
				try{
					if(!exists) {
						scheduler.scheduleJob(trigger);
					} else {
						scheduler.rescheduleJob(triggerName, triggerGroup, trigger);
					}
				} catch (SchedulerException e) {
					SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
										   "scheduleJob", "Error while scheduling job ", e);
					throw e;
				}		
			}	
			// all has been done	
			servreponse.append("outcome=\"perform\"/>");
		} catch (Exception e) {
			// somethig wrong
			servreponse.append("outcome=\"fault\"/>");
		}
		return servreponse.toString();
	}

	
	
	/**
	 * Gets the chron expression.
	 * 
	 * @param chronStr the chron str
	 * @param sc the sc
	 * @param sd the sd
	 * 
	 * @return the chron expression
	 */
	public  String getChronExpression(String chronStr, Calendar sc, Date sd) {
		String chronExp = null;
		try{
			sc.setTime(sd);
			int day = sc.get(Calendar.DAY_OF_MONTH);
			int month = sc.get(Calendar.MONTH);
			int year = sc.get(Calendar.YEAR);
			int hour = sc.get(Calendar.HOUR_OF_DAY);
			int minute = sc.get(Calendar.MINUTE);
			String type = "";
	    	String params = "";
	    	if(chronStr.indexOf("{")!=-1) {
	    		int indFirstBra = chronStr.indexOf("{");
	    		type = chronStr.substring(0, indFirstBra);
	    		params = chronStr.substring((indFirstBra+1), (chronStr.length()-1));
	    	} else {
	    		return chronExp;
	    	}
	    	if(type.equals("single")) {
	    		return chronExp; // this will be a normal trigger
	    	}
	    	if(type.equals("minute")) {
	    		int indeq = params.indexOf("=");
	    		String numrep = params.substring(indeq+1);
	    		chronExp = "0 0/"+numrep+" * * * ? *";
	    	}
	    	if(type.equals("hour")) {
	    		int indeq = params.indexOf("=");
	    		String numrep = params.substring(indeq+1);
	    		chronExp = "0 "+minute+" 0/"+numrep+" * * ? *";
	    	}
	    	if(type.equals("day")) {
	    		int indeq = params.indexOf("=");
	    		String numrep = params.substring(indeq+1);
	    		chronExp = "0 "+minute+" "+hour+" 1/"+numrep+" * ? *";
	    	}
	    	if(type.equals("week")) {
	    		int indeq = params.indexOf("=");
	    		int indsplit = params.indexOf(";");
	    		int ind2eq = params.indexOf("=", (indeq + 1));
	    		String numrep = params.substring((indeq+1), indsplit);
	    		Integer numrepInt = new Integer(numrep);
	    		String daysstr = params.substring(ind2eq+1);
	    		if( (daysstr==null) || (daysstr.trim().equals(""))) daysstr = "MON";
	    		if(daysstr.endsWith(",")) daysstr = daysstr.substring(0, (daysstr.length() - 1));
	    		chronExp = "0 "+minute+" "+hour+" ? * "+daysstr+"/"+numrep+" *";
	    	}
	    	if(type.equals("month")) {
	    		String numRep = "";
	    		String selmonths = "";
	    		String dayRep = "";
	    		String weeks = "";
	    		String days = "";
	    		String[] parchuncks = params.split(";");
	    		for(int i=0; i<parchuncks.length; i++) {
	    			String parchunk = parchuncks[i];
	    			String[] singleparchunks = parchunk.split("=");
	    			String key = singleparchunks[0];
	    			String value = singleparchunks[1];
	    			value = value.trim();
	    			if(value.endsWith(",")) {
    					value = value.substring(0, (value.length()-1));
    				}
	    			if(key.equals("numRepetition")) numRep= value;
	    			if(key.equals("months")) selmonths= value;
	    			if(key.equals("dayRepetition")) dayRep= value;
	    			if(key.equals("weeks")) weeks= value;
	    			if(key.equals("days")) days= value; 
	    		}
	            String monthcron = "";
	            if(selmonths.equals("NONE")){
	            	monthcron = (month + 1) + "/" + numRep;
	            } else {
	            	if(selmonths.equals("")) selmonths = "*";
	            	monthcron = selmonths;
	            }
	            String daycron = "?";
	            if( weeks.equals("NONE") && days.equals("NONE") ){
	            	if(dayRep.equals("0")) dayRep = "1";
	            	daycron = dayRep;
	            }
	            String dayinweekcron = "?";
	            if(!days.equals("NONE")){
	            	if(days.equals("")) days = "*";
	            	dayinweekcron = days;
	            }
	            if( !weeks.equals("NONE")  ){
	            	if(!weeks.equals("")) 
	            		if(weeks.equals("L")) dayinweekcron = dayinweekcron + weeks;
	            		else dayinweekcron = dayinweekcron + "#" + weeks;
	            		dayinweekcron = dayinweekcron.replaceFirst("SUN", "1");
	            		dayinweekcron = dayinweekcron.replaceFirst("MON", "2");
	            		dayinweekcron = dayinweekcron.replaceFirst("TUE", "3");
	            		dayinweekcron = dayinweekcron.replaceFirst("WED", "4");
	            		dayinweekcron = dayinweekcron.replaceFirst("THU", "5");
	            		dayinweekcron = dayinweekcron.replaceFirst("FRI", "6");
	            		dayinweekcron = dayinweekcron.replaceFirst("SAT", "7");
	            }
	    		chronExp = "0 "+minute+" "+hour+" "+daycron+" "+monthcron+" "+dayinweekcron+ " *";
	    	}
	    } catch (Exception e) {
	    	SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
	    						"getChronExpression", "Error while generating quartz chron expression", e);
	    }
		return chronExp;
	}
	
	
	
	/**
	 * Gets the job schedulation definition.
	 * 
	 * @param triggerName the trigger name
	 * @param triggerGroup the trigger group
	 * 
	 * @return the job schedulation definition
	 */
	public   String getJobSchedulationDefinition(String triggerName, String triggerGroup) {
		String schedDef = "";
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			if (triggerName == null || triggerName.trim().equals("")) {
				SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
								   	   "getJobSchedulationDefinition", "Missing trigger name request parameter!");
				throw new Exception("Missing trigger name request parameter!");
			}
			if (triggerGroup == null || triggerGroup.trim().equals("")) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
									"getJobSchedulationDefinition", "Missing trigger group name! Using default group...");
				triggerGroup = Scheduler.DEFAULT_GROUP;
			}
			Trigger trigger = (Trigger) scheduler.getTrigger(triggerName, triggerGroup);
			if (trigger == null) {
				SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
									   "getJobSchedulationDefinition", 
									   "Trigger with name '" + triggerName + "' not found in group '" + triggerGroup + "'!");
				throw new Exception("Trigger with name '" + triggerName + "' not found in group '" + triggerGroup + "'!");
			}
			schedDef = serializeTrigger(trigger);
		} catch (Exception e) {
			SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
		   			   			   "getJobSchedulationDefinition", "Error while recovering schedule definition");
		}
		return schedDef;
	}
	
	
	
	/**
	 * Serialize trigger.
	 * 
	 * @param trigger the trigger
	 * 
	 * @return the string
	 * 
	 * @throws SourceBeanException the source bean exception
	 */
	public  String serializeTrigger(Trigger trigger) throws SourceBeanException {
		StringBuffer buffer = new StringBuffer("<TRIGGER_DETAILS ");
		buffer.append(" ");
		String triggerName = trigger.getName();
		String triggerDescription = trigger.getDescription();
		// get job data map
		JobDataMap jdm = trigger.getJobDataMap();
		Date triggerStartTime = trigger.getStartTime();
		String triggerStartDateStr = "";
		String triggerStartTimeStr = "";
		if (triggerStartTime != null) {
			Calendar startCal = new GregorianCalendar();
			startCal.setTime(triggerStartTime);
			// date format: dd/mm/yyyy
			int day = startCal.get(Calendar.DAY_OF_MONTH);
			int month = startCal.get(Calendar.MONTH);
			int year = startCal.get(Calendar.YEAR);
			triggerStartDateStr = ((day < 10) ? "0" : "") + day + 
						"/" + 
						((month + 1 < 10) ? "0" : "") + (month + 1) + 
						"/" + 
						year;
			int hour = startCal.get(Calendar.HOUR_OF_DAY);
			int minute = startCal.get(Calendar.MINUTE);
			// hour format: hh:mm
			triggerStartTimeStr = ((hour < 10) ? "0" : "") + hour + ":" + ((minute < 10) ? "0" : "") + minute;
		}
		Date triggerEndTime = trigger.getEndTime();
		String triggerEndDateStr = "";
		String triggerEndTimeStr = "";
		if (triggerEndTime != null) {
			Calendar endCal = new GregorianCalendar();
			endCal.setTime(triggerEndTime);
			// date format: dd/mm/yyyy
			int day = endCal.get(Calendar.DAY_OF_MONTH);
			int month = endCal.get(Calendar.MONTH);
			int year = endCal.get(Calendar.YEAR);
			triggerEndDateStr = ((day < 10) ? "0" : "") + day + 
						"/" + 
						((month + 1 < 10) ? "0" : "") + (month + 1) + 
						"/" + 
						year;
			int hour = endCal.get(Calendar.HOUR_OF_DAY);
			int minute = endCal.get(Calendar.MINUTE);
			// hour format: hh:mm
			triggerEndTimeStr = ((hour < 10) ? "0" : "") + hour + ":" + ((minute < 10) ? "0" : "") + minute;
		}
		buffer.append(" triggerName=\"" + (triggerName != null ? triggerName : "") + "\"");
		buffer.append(" triggerDescription=\"" + (triggerDescription != null ? triggerDescription : "") + "\"");
		buffer.append(" triggerStartDate=\"" + triggerStartDateStr + "\"");
		buffer.append(" triggerStartTime=\"" + triggerStartTimeStr + "\"");
		buffer.append(" triggerEndDate=\"" + triggerEndDateStr + "\"");
		buffer.append(" triggerEndTime=\"" + triggerEndTimeStr + "\"");
		// extract the chron string and add it the source bean
		String chronStr = jdm.getString("chronString");
		if((chronStr == null) || (chronStr.trim().equals(""))) {
			chronStr = "single{}";
		}
		buffer.append(" triggerChronString=\"" + chronStr + "\"");
		buffer.append(" >");
		// extract other parameters and put them into source bean
		buffer.append("<JOB_PARAMETERS>");
		if (jdm != null && !jdm.isEmpty()) {
			String[] keys = jdm.getKeys();
			if (keys != null && keys.length > 0) {
				for (int i = 0; i < keys.length; i++) {
					String key = keys[i];
					String value = jdm.getString(key);
					// already extracted and processed
					if(key.equals("chronString")) {
						continue;
					}
					buffer.append("<JOB_PARAMETER ");
					if (value == null) {
						SpagoBITracer.warning(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
								              "loadJobDetailIntoResponse", 
								              "Trigger parameter '" + key + "' has no String value!!");	
					}
					buffer.append(" name=\"" + key + "\"");
					buffer.append(" value=\"" + value + "\"");
					buffer.append(" />");
				}
			}
		}
		buffer.append("</JOB_PARAMETERS>");
		buffer.append("</TRIGGER_DETAILS>");
		return buffer.toString();
	}
	
	
	
	/**
	 * Exist job definition.
	 * 
	 * @param jobName the job name
	 * @param jobGroup the job group
	 * 
	 * @return the string
	 */
	public   String existJobDefinition(String jobName, String jobGroup) {
		StringBuffer buffer = new StringBuffer("<JOB_EXISTANCE  ");
		try{
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			if (jobName == null || jobName.trim().equals("")) {
				SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
									   "existJobDefinition", "Missing job name request parameter!");
				throw new Exception("Missing job name request parameter!");
			}
			if (jobGroup == null || jobGroup.trim().equals("")) {
				SpagoBITracer.major(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
									"existJobDefinition", "Missing job group name! Using default group...");
				jobGroup = Scheduler.DEFAULT_GROUP;
			}
			JobDetail aJob = scheduler.getJobDetail(jobName, jobGroup);
			if (aJob == null) {
				buffer.append(" exists=\"false\" />");
			} else {
				buffer.append(" exists=\"true\" />");
			}
		} catch (Exception e) {
			SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), 
					   			  "existJobDefinition", "Error while checking existence of job", e);
			buffer = new StringBuffer("<JOB_EXISTANCE/> ");
		}
		return buffer.toString();
	}
	
	
}
