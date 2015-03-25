/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.services.scheduler.service;

import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.deserializer.Deserializer;
import it.eng.spagobi.commons.deserializer.DeserializerFactory;
import it.eng.spagobi.commons.deserializer.TriggerXMLDeserializer;
import it.eng.spagobi.commons.serializer.JobXMLSerializer;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.serializer.XMLSerializer;
import it.eng.spagobi.commons.utilities.SpagoBITracer;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class XSchedulerServiceSupplier implements ISchedulerServiceSupplier{

	private ISchedulerDAO schedulerDAO;
	
    static private Logger logger = Logger.getLogger(XSchedulerServiceSupplier.class);
    
    public XSchedulerServiceSupplier() {
    	try {
			schedulerDAO = DAOFactory.getSchedulerDAO();
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to load scheduler DAO", t);
		}
    }
	
	public String getJobList() {
		String xml;
		
		logger.debug("IN");
		
		xml = null;
		try {
			List<Job> jobs = schedulerDAO.loadJobs();
			logger.trace("Succesfully loaded [" + jobs.size() + "] job(s)");
			XMLSerializer xmlSerializer = (XMLSerializer)SerializerFactory.getSerializer("application/xml");
			xmlSerializer.setProperty(JobXMLSerializer.PROPERTY_CONSUMER, "list");
			xml = (String)xmlSerializer.serialize(jobs, null);
			logger.debug("Job list succesfully serialized");
			logger.trace("Job list encoded in xml is uqual to: " + xml);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while loading job list", t);
		} finally {
			logger.debug("OUT");
		}
		
		return xml;
	}
	
	public String getJobDefinition(String jobName, String jobGroup) {
		String xml;
		
		xml = null;
		try{
			Assert.assertTrue(StringUtilities.isNotEmpty(jobName), "Input parameter [jobName] cannot be empty");
			Assert.assertTrue(StringUtilities.isNotEmpty(jobGroup), "Input parameter [jobGroup] cannot be empty");
			
			Job aJob = schedulerDAO.loadJob(jobGroup, jobName);
			if (aJob == null) {
				throw new Exception("Job with name [" + jobName + "] not found in group [" + jobGroup + "]");
			}
			
			XMLSerializer xmlSerializer = (XMLSerializer)SerializerFactory.getSerializer("application/xml");
			xmlSerializer.setProperty(JobXMLSerializer.PROPERTY_CONSUMER, "service");
			xml = (String)xmlSerializer.serialize(aJob, null);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while recovering job definition", t);
		}
		
		return xml;
	}
	
	public String getJobSchedulationList(String jobName, String jobGroupName) {
		String xml = "";
		try {
			Assert.assertNotNull(jobName, "Input parameter [" + jobName + "] cannot be null");
			Assert.assertNotNull(jobName, "Input parameter [" + jobGroupName + "] cannot be null");
			
			List<Trigger> triggers = schedulerDAO.loadTriggers(jobGroupName, jobName);
			// filter out trigger whose property runImmediately is equal to true
			List<Trigger> triggersToSerialize = new ArrayList<Trigger>();
			for (Trigger trigger  : triggers) {
				//if(trigger.getName().startsWith("schedule_uuid_") == false) {
				if(!trigger.isRunImmediately()) {
					triggersToSerialize.add(trigger);
				}
			}
			logger.trace("Succesfully loaded [" + triggersToSerialize.size() + "] trigger(s)");
			
			XMLSerializer xmlSerializer = (XMLSerializer)SerializerFactory.getSerializer("application/xml");
			xmlSerializer.setProperty(TriggerXMLDeserializer.PROPERTY_CONSUMER, "list");
			xml = (String)xmlSerializer.serialize(triggersToSerialize, null);
			logger.debug("Trigger list succesfully serialized");
			logger.trace("Trigger list encoded in xml is uqual to: " + xml);
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while loading trigger list", t);
		} finally {
			logger.debug("OUT");
		}
		return xml;
	}
	
	public  String getJobSchedulationDefinition(String triggerName, String triggerGroupName) {
		String xml;
		
		xml = null;
		try {
			Assert.assertTrue(StringUtilities.isNotEmpty(triggerName), "Input parameter [triggerName] cannot be empty");
			Assert.assertTrue(StringUtilities.isNotEmpty(triggerGroupName), "Input parameter [triggerGroupName] cannot be empty");
			
			Trigger trigger = schedulerDAO.loadTrigger(triggerGroupName, triggerName);
			if (trigger == null) {
				throw new SpagoBIRuntimeException("Trigger with name [" + triggerName + "] not found in group [" + triggerGroupName + "]");
			}
			logger.debug("Trigger [" + triggerName +"] succesfully loaded from group [" + triggerGroupName + "]");
			
			XMLSerializer xmlSerializer = (XMLSerializer)SerializerFactory.getSerializer("application/xml");
			xmlSerializer.setProperty(JobXMLSerializer.PROPERTY_CONSUMER, "service");
			xml = (String)xmlSerializer.serialize(trigger, null);
			logger.debug("Trigger succesfully serialized");
			logger.trace("Trigger encoded in xml is uqual to: " + xml);
			
			//xml = serializeTrigger(trigger);
		} catch (SpagoBIRuntimeException t) {
			throw t;
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while loading trigger [" + triggerName + "] from group [" + triggerGroupName + "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return xml;
	}
	
	
	
	
	public String deleteSchedulation(String triggerName, String triggerGroup) {
		StringBuffer servreponse = new StringBuffer();
		try{
			servreponse.append("<EXECUTION_OUTCOME ");
			schedulerDAO.deleteTrigger(triggerName, triggerGroup);
		} catch (Exception e) {
			servreponse.append("outcome=\"fault\"/>");
		}
		servreponse.append("outcome=\"perform\"/>");
		return servreponse.toString();
	}
	
	public   String deleteJob(String jobName, String jobGroupName) {
		StringBuffer servreponse = new StringBuffer();
		try {
			servreponse.append("<EXECUTION_OUTCOME ");
			schedulerDAO.deleteJob(jobName, jobGroupName);
			servreponse.append("outcome=\"perform\"/>");
		} catch (Exception e) {
			SpagoBITracer.critical(SpagoBIConstants.NAME_MODULE, this.getClass().getName(), "deleteJob", 
					"Error while deleting job", e);
			servreponse.append("outcome=\"fault\"/>");
		}	
		return servreponse.toString();
	}
	
	
	public String defineJob(String xmlRequest) {
		StringBuffer servreponse = new StringBuffer();
		try{
			Deserializer deserializer = DeserializerFactory.getDeserializer("application/xml");
			Job job = (Job)deserializer.deserialize(xmlRequest, Job.class);
						
			schedulerDAO.insertJob(job);
			
			servreponse.append("<EXECUTION_OUTCOME outcome=\"perform\"/>");
		} catch (Exception e) {
			servreponse.append("<EXECUTION_OUTCOME outcome=\"fault\"/>");
		}
		return servreponse.toString();
	}

	
	
	public String scheduleJob(String xmlRequest) {
		StringBuffer servreponse = new StringBuffer();
		try{
			Deserializer deserializer = DeserializerFactory.getDeserializer("application/xml");
			Trigger trigger = (Trigger)deserializer.deserialize(xmlRequest, Trigger.class);
			
			schedulerDAO.saveTrigger(trigger);
			
			// all has been done	
			servreponse.append("<EXECUTION_OUTCOME outcome=\"perform\"/>");
		} catch (Exception e) {
			// something wrong
			logger.error("Cannot schedule job", e);
			servreponse.append("<EXECUTION_OUTCOME outcome=\"fault\"/>");
		}
		return servreponse.toString();
	}

	public   String existJobDefinition(String jobName, String jobGroupName) {
		StringBuffer buffer;
		
		logger.debug("IN");
		
		buffer = new StringBuffer();
		try{
			Assert.assertTrue(StringUtilities.isNotEmpty(jobName), "Input parameter [jobName] cannot be empty");
			Assert.assertTrue(StringUtilities.isNotEmpty(jobGroupName), "Input parameter [jobGroupName] cannot be empty");
			
			if ( schedulerDAO.jobExists(jobGroupName, jobName) ) {
				buffer.append("<JOB_EXISTANCE exists=\"false\" />");
			} else {
				buffer.append("<JOB_EXISTANCE exists=\"true\" />");
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while checking for existence of job [" + jobName + "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return buffer.toString();
	}
}
