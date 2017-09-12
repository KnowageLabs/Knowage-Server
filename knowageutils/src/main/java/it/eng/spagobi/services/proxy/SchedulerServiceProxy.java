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
package it.eng.spagobi.services.proxy;

import it.eng.spagobi.services.scheduler.stub.SchedulerService;
import it.eng.spagobi.services.scheduler.stub.SchedulerServiceServiceLocator;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * 
 * Scheduler Service
 *
 */
public final class SchedulerServiceProxy extends AbstractServiceProxy{
	
	static private final String SERVICE_NAME = "Scheduler Service";
	
    static private Logger logger = Logger.getLogger(SchedulerServiceProxy.class);

    /**
     * Use this in engine context only.
     * 
     * @param user user id
     * @param session HttpSession
     */
    public SchedulerServiceProxy(String user,HttpSession session) {
    	super(user, session);
    }

    private  SchedulerServiceProxy() {
    	super();
    }    

    
    private SchedulerService lookUp() throws SecurityException {
	try {
    	    SchedulerServiceServiceLocator locator = new SchedulerServiceServiceLocator();
    	    SchedulerService service = null;
	    if (serviceUrl!=null ){
		    service = locator.getSchedulerService(serviceUrl);		
	    }else {
		    service = locator.getSchedulerService();		
	    } 
	    return service;
	} catch (ServiceException e) {
	    logger.error("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]");
	    throw new SecurityException("Impossible to locate [" + SERVICE_NAME + "] at [" + serviceUrl + "]", e);
	}
    }   
    
    /**
     * Return all jobs.
     * 
     * @return String
     */
    public String getJobList() {
    	logger.debug("IN");
    	try {
    	    return lookUp().getJobList(readTicket(),userId);
    	} catch (Exception e) {
    	    logger.error("Error during service execution",e);
    	}finally{
    	    logger.debug("OUT");
    	}
    	return null;	
    }
    
    /**
     * Gets the job schedulation list.
     * 
     * @param jobName job name
     * @param jobGroup job group
     * 
     * @return String
     */
    public String getJobSchedulationList(String jobName, String jobGroup) {
    	logger.debug("IN.jobName="+jobName+" /jobGroup="+jobGroup);
    	try {
    	    return lookUp().getJobSchedulationList(readTicket(),userId,jobName, jobGroup);
    	} catch (Exception e) {
    	    logger.error("Error during service execution",e);
    	}finally{
    	    logger.debug("OUT");
    	}
    	return null;	
    }
    
    /**
     * Delete schedulation.
     * 
     * @param triggerName String
     * @param triggerGroup String
     * 
     * @return String
     */
    public String deleteSchedulation(String triggerName, String triggerGroup) {
    	logger.debug("IN.triggerName="+triggerName+" /triggerGroup="+triggerGroup);
    	try {
    	    return lookUp().deleteSchedulation(readTicket(),userId,triggerName, triggerGroup);
    	} catch (Exception e) {
    	    logger.error("Error during service execution",e);
    	}finally{
    	    logger.debug("OUT");
    	}
    	return null;	
    }
    
    /**
     * Delete job.
     * 
     * @param jobName String
     * @param jobGroupName String
     * 
     * @return String
     */
    public String deleteJob(String jobName, String jobGroupName) {
    	logger.debug("IN.jobName="+jobName+" /jobGroupName="+jobGroupName);
    	try {
    	    return lookUp().deleteJob(readTicket(),userId,jobName, jobGroupName);
    	} catch (Exception e) {
    	    logger.error("Error during service execution",e);
    	}finally{
    	    logger.debug("OUT");
    	}
    	return null;	
    }
    
    /**
     * Define job.
     * 
     * @param xmlRequest  String
     * 
     * @return String
     */
    public String defineJob(String xmlRequest) {
    	logger.debug("IN.xmlRequest="+xmlRequest);
    	try {
    	    return lookUp().defineJob(readTicket(),userId,xmlRequest);
    	} catch (Exception e) {
    	    logger.error("Error during service execution",e);
    	}finally{
    	    logger.debug("OUT");
    	}
    	return null;	
    }
    
    /**
     * Gets the job definition.
     * 
     * @param jobName String
     * @param jobGroup String
     * 
     * @return String
     */
    public String getJobDefinition(String jobName, String jobGroup) {
    	logger.debug("IN.jobName="+jobName+" /jobGroup="+jobGroup);
    	try {
    	    return lookUp().getJobDefinition(readTicket(),userId,jobName, jobGroup);
    	} catch (Exception e) {
    	    logger.error("Error during service execution",e);
    	}finally{
    	    logger.debug("OUT");
    	}
    	return null;	
    }
    
    /**
     * Schedule job.
     * 
     * @param xmlRequest String
     * 
     * @return String
     */
    public String scheduleJob(String xmlRequest) {
    	logger.debug("IN.xmlRequest="+xmlRequest);
    	try {
    	    return lookUp().scheduleJob(readTicket(),userId,xmlRequest);
    	} catch (Exception e) {
    	    logger.error("Error during service execution",e);
    	}finally{
    	    logger.debug("OUT");
    	}
    	return null;	
    }
    
    /**
     * Gets the job schedulation definition.
     * 
     * @param triggerName String
     * @param triggerGroup String
     * 
     * @return String
     */
    public String getJobSchedulationDefinition(String triggerName, String triggerGroup) {
    	logger.debug("IN.triggerName="+triggerName+" /triggerGroup="+triggerGroup);
    	try {
    	    return lookUp().getJobSchedulationDefinition(readTicket(),userId,triggerName, triggerGroup);
    	} catch (Exception e) {
    	    logger.error("Error during service execution",e);
    	}finally{
    	    logger.debug("OUT");
    	}
    	return null;	
    }
    
    /**
     * Exist job definition.
     * 
     * @param jobName String
     * @param jobGroup String
     * 
     * @return String
     */
    public String existJobDefinition(String jobName, String jobGroup) {
    	logger.debug("IN.jobName="+jobName+" /jobGroup="+jobGroup);
    	try {
    	    return lookUp().existJobDefinition(readTicket(),userId,jobName, jobGroup);
    	} catch (Exception e) {
    	    logger.error("Error during service execution",e);
    	}finally{
    	    logger.debug("OUT");
    	}
    	return null;	
    }

}
