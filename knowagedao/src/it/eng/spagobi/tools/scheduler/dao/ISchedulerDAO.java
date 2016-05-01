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
package it.eng.spagobi.tools.scheduler.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tools.scheduler.bo.Frequency;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.bo.TriggerPaused;

import java.util.List;
import java.util.Map;

import org.quartz.JobDetail;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public interface ISchedulerDAO {

	void setTenant(String tenant);

	Tenant findTenant(JobDetail jobDetail);

	boolean jobGroupExists(String jobGroupName);

	boolean jobExists(String jobGroupName, String jobName);

	List<String> getJobGroupNames();

	/**
	 * @return all jobs. If there are no jobs already stored it returns an empty list
	 */
	List<Job> loadJobs();

	/**
	 * @param jobGroupNames
	 *            the list of group names in which to look for jobs. It can be empty but it cannot be null. If it is an empty list an empty list of jobs will be
	 *            returned.
	 * 
	 * @return the jobs contained in the specified groups. Never returns null. If there are no jobs in the specified groups it returns an empty list of jobs
	 */
	List<Job> loadJobs(List<String> jobGroupNames);

	/**
	 * @param jobGroupName
	 *            the name of the group in which to look for jobs. It it cannot be empty.
	 * 
	 * @return the jobs contained in the specified group. Never returns null. If there are no jobs in the specified group it returns an empty list of jobs
	 */
	List<Job> loadJobs(String jobGroupName);

	/**
	 * @param jobGroupName
	 *            the name of the group in which to look up. It it cannot be empty.
	 * @param jobName
	 *            the name of the job to load. It it cannot be empty.
	 * 
	 * @return the job if exists a job named jobName in group jobGroupName. null otherwise
	 */
	Job loadJob(String jobGroupName, String jobName);

	void deleteJob(String jobName, String jobGroupName);

	void insertJob(Job spagobiJob);

	boolean triggerExists(Trigger spagobiTrigger);

	Trigger loadTrigger(String triggerGroupName, String triggerName);

	List<Trigger> loadTriggers(String jobGroupName, String jobName);

	void deleteTrigger(String triggerName, String triggerGroupName);

	boolean saveTrigger(Trigger spagobiTrigger);

	void insertTrigger(Trigger spagobiTrigger);

	void updateTrigger(Trigger spagobiTrigger);

	void pauseTrigger(TriggerPaused triggerPaused) throws EMFUserError;

	boolean resumeTrigger(String triggerGroup, String triggerName, String jobGroup, String jobName);

	boolean isTriggerPaused(String triggerGroup, String triggerName, String jobGroup, String jobName);

	void createOrUpdateJobAndTrigger(String jobName, Class jobClass, String groupName, String triggerGroup, Frequency frequency, Map<String, String> parameters);

	Job createOrUpdateJob(String name, String groupName, Class jobClass, Map<String, String> parameters);
}
