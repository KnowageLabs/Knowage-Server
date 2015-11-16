/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.bo.TriggerPaused;

import java.util.List;

import org.quartz.JobDetail;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public interface ISchedulerDAO {
	
	void setTenant(String tenant);
	
	Tenant findTenant(JobDetail jobDetail);
	
	boolean jobGroupExists(String jobGroupName) ;
	boolean jobExists(String jobGroupName, String jobName);
	List<String> getJobGroupNames() ;
	
	/**
	 * @return all jobs. If there are no jobs already stored it returns an empty list
	 */
	List<Job> loadJobs() ;
	
	/**
	 * @param jobGroupNames the list of group names in which to look for jobs. It can be empty but it cannot be null. 
	 * If it is an empty list an empty list of jobs will be returned.
	 * 
	 * @return the jobs contained in the specified groups. Never returns null. If there are no jobs in the specified groups
	 * it returns an empty list of jobs
	 */
	List<Job> loadJobs(List<String> jobGroupNames);
	
	/**
	 * @param jobGroupName the name of the group in which to look for jobs. It it cannot be empty.
	 * 
	 * @return the jobs contained in the specified group. Never returns null. If there are no jobs in the specified group
	 * it returns an empty list of jobs
	 */
	List<Job> loadJobs(String jobGroupName);
	
	/**
	 * @param jobGroupName the name of the group in which to look up. It it cannot be empty.
	 * @param jobName the name of the job to load. It it cannot be empty.
	 * 
	 * @return the job if exists a job named jobName in group jobGroupName. null otherwise
	 */
	Job loadJob(String jobGroupName, String jobName);
	void deleteJob(String jobName, String jobGroupName);
	void insertJob(Job spagobiJob);
	
	boolean triggerExists(Trigger spagobiTrigger) ;
	Trigger loadTrigger(String triggerGroupName, String triggerName);
	List<Trigger> loadTriggers(String jobGroupName, String jobName);	
	void deleteTrigger(String triggerName, String triggerGroupName);	
	boolean saveTrigger(Trigger spagobiTrigger);	
	void insertTrigger(Trigger spagobiTrigger);	
	void updateTrigger(Trigger spagobiTrigger);
	
	void pauseTrigger(TriggerPaused triggerPaused) throws EMFUserError;
	boolean resumeTrigger(String triggerGroup, String triggerName, String jobGroup, String jobName);
	boolean isTriggerPaused(String triggerGroup, String triggerName, String jobGroup, String jobName);

}
