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
package it.eng.spagobi.tools.scheduler.dao.quartz;

import it.eng.qbe.datasource.configuration.dao.DAOException;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDOAException;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tenant.Tenant;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.scheduler.bo.CronExpression;
import it.eng.spagobi.tools.scheduler.bo.Frequency;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.bo.TriggerPaused;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.metadata.SbiTriggerPaused;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogMF;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 * 
 */
public class QuarzSchedulerDAOImpl extends AbstractHibernateDAO implements ISchedulerDAO {

	private Scheduler scheduler;

	private String tenant;

	static private Logger logger = Logger.getLogger(QuarzSchedulerDAOImpl.class);

	public static String GROUP_NAME_SEPARATOR = "/";

	public QuarzSchedulerDAOImpl() {
		logger.debug("IN");
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
		} catch (Throwable t) {
			throw new SpagoBIDOAException("Impossible to access to the default quartz scheduler", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public boolean jobGroupExists(String jobGroupName) {
		Assert.assertNotNull(jobGroupName, "Input parameter [jobGroupName] cannot be null");
		List<String> jobGroupNames = this.getJobGroupNames();
		return jobGroupNames.contains(jobGroupName);
	}

	@Override
	public boolean jobExists(String jobGroupName, String jobName) {
		boolean exists = false;
		try {
			Assert.assertNotNull(jobGroupName, "Input parameter [jobGroupName] cannot be null");
			Assert.assertNotNull(jobGroupName, "Input parameter [jobName] cannot be null");

			if (jobGroupExists(jobGroupName) == false)
				return false;

			jobGroupName = this.applyTenant(jobGroupName);
			String[] jobNames = scheduler.getJobNames(jobGroupName);
			for (int i = 0; i < jobNames.length; i++) {
				if (jobName.equalsIgnoreCase(jobNames[i])) {
					exists = true;
					break;
				}
			}
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while checking for the existence of job [" + jobName + "] in group [" + jobGroupName
					+ "]", t);
		} finally {
			logger.debug("OUT");
		}
		return exists;
	}

	private String applyTenant(String jobGroupName) {
		LogMF.debug(logger, "IN: jobGroupName = [{0}]", jobGroupName);
		String tenant = this.getTenant();
		if (tenant != null) {
			jobGroupName = this.getTenantPrefix(tenant) + jobGroupName;
		}
		LogMF.debug(logger, "OUT: jobGroupName = [{0}]", jobGroupName);
		return jobGroupName;
	}

	private String removeTenant(String jobGroupName) {
		LogMF.debug(logger, "IN: jobGroupName = [{0}]", jobGroupName);
		String tenant = this.getTenant();
		if (tenant != null) {
			jobGroupName = jobGroupName.substring(this.getTenantPrefix(tenant).length());
		}
		LogMF.debug(logger, "OUT: jobGroupName = [{0}]", jobGroupName);
		return jobGroupName;
	}

	@Override
	public List<String> getJobGroupNames() {
		List<String> jobGroupNames;

		logger.debug("IN");

		jobGroupNames = new ArrayList<String>();
		try {
			String[] names = scheduler.getJobGroupNames();
			List<String> l = Arrays.asList(names);
			if (l != null) {
				jobGroupNames.addAll(l);
				jobGroupNames = this.filterForTenant(jobGroupNames);
			}
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while loading job group names", t);
		} finally {
			logger.debug("OUT");
		}

		return jobGroupNames;
	}

	private List<String> filterForTenant(List<String> jobGroupNames) {
		LogMF.debug(logger, "IN: jobGroupNames = [{0}]", jobGroupNames);
		String tenant = this.getTenant();
		List<String> toReturn = new ArrayList<String>();
		if (tenant != null) {
			Iterator<String> it = jobGroupNames.iterator();
			while (it.hasNext()) {
				String aJobGroupName = it.next();
				if (aJobGroupName.startsWith(this.getTenantPrefix(tenant))) {
					toReturn.add(this.removeTenant(aJobGroupName));
				}
			}
		} else {
			toReturn = jobGroupNames;
		}
		LogMF.debug(logger, "OUT: jobGroupNames = [{0}]", toReturn);
		return toReturn;
	}

	private String getTenantPrefix(String tenant) {
		return tenant + GROUP_NAME_SEPARATOR;
	}

	/**
	 * @return all jobs. If there are no jobs already stored it returns an empty list
	 */
	@Override
	public List<Job> loadJobs() {
		List<Job> jobs;

		logger.debug("IN");

		jobs = new ArrayList<Job>();

		try {
			List<String> jobGroupNames = getJobGroupNames();
			jobs = loadJobs(jobGroupNames);
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while loading jobs", t);
		} finally {
			logger.debug("OUT");
		}

		return jobs;
	}

	/**
	 * @param jobGroupNames
	 *            the list of group names in which to look for jobs. It can be empty but it cannot be null. If it is an empty list an empty list of jobs will be
	 *            returned.
	 * 
	 * @return the jobs contained in the specified groups. Never returns null. If there are no jobs in the specified groups it returns an empty list of jobs
	 */
	@Override
	public List<Job> loadJobs(List<String> jobGroupNames) {
		List<Job> jobs;

		logger.debug("IN");

		jobs = new ArrayList<Job>();

		try {
			Assert.assertNotNull(jobGroupNames, "Input parameter [jobGroupNames] cannot be null");

			for (String jobGroupName : jobGroupNames) {
				List<Job> jobDetailsInGroup = loadJobs(jobGroupName);
				jobs.addAll(jobDetailsInGroup);
			}
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while loading jobs", t);
		} finally {
			logger.debug("OUT");
		}

		return jobs;
	}

	/**
	 * @param jobGroupName
	 *            the name of the group in which to look for jobs. It it cannot be empty.
	 * 
	 * @return the jobs contained in the specified group. Never returns null. If there are no jobs in the specified group it returns an empty list of jobs
	 */
	@Override
	public List<Job> loadJobs(String jobGroupName) {
		List<Job> jobs;

		logger.debug("IN");

		jobs = new ArrayList<Job>();

		try {
			Assert.assertTrue(StringUtilities.isNotEmpty(jobGroupName), "Input parameter [jobGroupName] cannot be empty");

			String actualJobGroupName = this.applyTenant(jobGroupName);
			String[] jobNames = scheduler.getJobNames(actualJobGroupName);
			if (jobNames != null) {
				logger.debug("Job group [" + jobGroupName + "] contains [" + jobNames.length + "] job(s)");
				for (int j = 0; j < jobNames.length; j++) {
					Job job = loadJob(jobGroupName, jobNames[j]);
					if (job != null) {
						jobs.add(job);
					} else {
						logger.warn("Impossible to load job [" + jobNames[j] + "] from group [" + jobGroupName + "]");
					}
				}
			} else {
				logger.debug("Job group [" + jobGroupName + "] does not contain jobs");
			}
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while loading jobs of group [" + jobGroupName + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return jobs;
	}

	/**
	 * @param jobGroupName
	 *            the name of the group in which to look up. It it cannot be empty.
	 * @param jobName
	 *            the name of the job to load. It it cannot be empty.
	 * 
	 * @return the job if exists a job named jobName in group jobGroupName. null otherwise
	 */
	@Override
	public Job loadJob(String jobGroupName, String jobName) {
		Job job;

		logger.debug("IN");

		job = null;
		try {
			Assert.assertTrue(StringUtilities.isNotEmpty(jobGroupName), "Input parameter [jobGroupName] cannot be empty");
			Assert.assertTrue(StringUtilities.isNotEmpty(jobName), "Input parameter [jobName] cannot be empty");

			String actualJobGroupName = this.applyTenant(jobGroupName);
			JobDetail jobDetail = scheduler.getJobDetail(jobName, actualJobGroupName);
			if (jobDetail != null) {
				job = QuartzNativeObjectsConverter.convertJobFromNativeObject(jobDetail);
				adjustTenant(job);
				logger.debug("Job [" + jobName + "] succesfully loaded from group [" + jobGroupName + "]");
			} else {
				logger.debug("Job [" + jobName + "] not found in group [" + jobGroupName + "]");
			}
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while loading job [" + jobName + "] in group [" + jobGroupName + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return job;
	}

	private void adjustTenant(Job job) {
		String tenant = this.getTenant();
		if (tenant != null) {
			String jobGroupName = job.getGroupName();
			LogMF.debug(logger, "before: jobGroupName = [{0}]", jobGroupName);
			job.setGroupName(this.removeTenant(jobGroupName));
			LogMF.debug(logger, "after: jobGroupName = [{0}]", job.getGroupName());
		}
	}

	@Override
	public void deleteJob(String jobName, String jobGroupName) {
		logger.debug("IN");

		try {
			// TODO delete trigger associated to the job first (?)
			// Reply: no need to delete trigger because deleteJob(...) loops through all the triggers having a reference to this job,
			// to unschedule them and removes the job from the jobstore
			String actualJobGroupName = this.applyTenant(jobGroupName);
			scheduler.deleteJob(jobName, actualJobGroupName);
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while deleting job [" + jobName + "] of job group [" + jobGroupName + "]", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public void insertJob(Job spagobiJob) {

		logger.debug("IN");

		try {
			Assert.assertNotNull(spagobiJob, "Input parameter [spagobiJob] cannot be null");
			JobDetail quartzJob = QuartzNativeObjectsConverter.convertJobToNativeObject(spagobiJob);
			if (quartzJob.getDescription() == null)
				quartzJob.setDescription("");
			String jobGroupName = quartzJob.getGroup() != null ? quartzJob.getGroup() : Scheduler.DEFAULT_GROUP;
			quartzJob.setGroup(jobGroupName);
			adjustTenant(quartzJob);
			scheduler.addJob(quartzJob, true);
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while inserting job [" + spagobiJob + "]", t);
		} finally {
			logger.debug("OUT");
		}
	}

	private void adjustTenant(JobDetail quartzJob) {
		String jobGroupName = quartzJob.getGroup();
		LogMF.debug(logger, "before: jobGroupName = [{0}]", jobGroupName);
		jobGroupName = this.applyTenant(jobGroupName);
		LogMF.debug(logger, "after: jobGroupName = [{0}]", jobGroupName);
		quartzJob.setGroup(jobGroupName);
	}

	@Override
	public boolean triggerExists(Trigger spagobiTrigger) {
		boolean exists;

		logger.debug("IN");

		exists = false;
		try {
			Assert.assertNotNull(spagobiTrigger, "Input parameter [trigger] cannot be null");
			Assert.assertNotNull(spagobiTrigger.getJob(), "The attribute [job] of trigger cannot be null");
			Assert.assertTrue(StringUtilities.isNotEmpty(spagobiTrigger.getJob().getName()),
					"The attribute [name] of the job associated to the trigger cannot be empty]");
			Assert.assertTrue(StringUtilities.isNotEmpty(spagobiTrigger.getJob().getGroupName()),
					"The attribute [groupName] of the job associated to the trigger cannot be empty]");

			List<Trigger> jobTriggers = loadTriggers(spagobiTrigger.getJob().getGroupName(), spagobiTrigger.getJob().getName());
			for (Trigger jobTrigger : jobTriggers) {
				if (jobTrigger.getName().equals(spagobiTrigger.getName())) {
					exists = true;
					break;
				}
			}
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while checking for the existence of trigger [" + spagobiTrigger.getName()
					+ "] of trigger group [" + spagobiTrigger.getGroupName() + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return exists;
	}

	@Override
	public Trigger loadTrigger(String triggerGroupName, String triggerName) {
		Trigger spagobiTrigger;

		logger.debug("IN");

		spagobiTrigger = null;
		try {
			Assert.assertTrue(StringUtilities.isNotEmpty(triggerGroupName), "Input parameter [triggerGroupName] cannot be null");
			Assert.assertTrue(StringUtilities.isNotEmpty(triggerName), "Input parameter [triggerName] cannot be null");

			org.quartz.Trigger quartzTrigger = scheduler.getTrigger(triggerName, triggerGroupName);
			if (quartzTrigger != null) {
				spagobiTrigger = QuartzNativeObjectsConverter.convertTriggerFromNativeObject(quartzTrigger);
				adjustTenant(spagobiTrigger);
			}
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while checking for the existence of trigger [" + spagobiTrigger.getName()
					+ "] of trigger group [" + spagobiTrigger.getGroupName() + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return spagobiTrigger;
	}

	@Override
	public List<Trigger> loadTriggers(String jobGroupName, String jobName) {
		List<Trigger> spagobiTriggers;

		logger.debug("IN");

		spagobiTriggers = new ArrayList<Trigger>();
		try {
			String actualJobGroupName = this.applyTenant(jobGroupName);
			org.quartz.Trigger[] t = scheduler.getTriggersOfJob(jobName, actualJobGroupName);
			List<org.quartz.Trigger> quartzTriggers = Arrays.asList(t);
			if (quartzTriggers != null) {
				for (org.quartz.Trigger quartzTrigger : quartzTriggers) {
					Trigger spagobiTrigger = QuartzNativeObjectsConverter.convertTriggerFromNativeObject(quartzTrigger);
					adjustTenant(spagobiTrigger);
					spagobiTriggers.add(spagobiTrigger);
				}
			}
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while loading trigger s of job [" + jobName + "] in job group [" + jobGroupName + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return spagobiTriggers;
	}

	private void adjustTenant(Trigger spagobiTrigger) {
		Job job = spagobiTrigger.getJob();
		String jobGroupName = job.getGroupName();
		LogMF.debug(logger, "before: jobGroupName = [{0}]", jobGroupName);
		jobGroupName = this.removeTenant(jobGroupName);
		LogMF.debug(logger, "after: jobGroupName = [{0}]", jobGroupName);
		job.setGroupName(jobGroupName);
	}

	@Override
	public void deleteTrigger(String triggerName, String triggerGroupName) {
		logger.debug("IN");

		try {
			Assert.assertTrue(StringUtilities.isNotEmpty(triggerName), "Input parameter [triggerName] cannot be empty");
			Assert.assertTrue(StringUtilities.isNotEmpty(triggerGroupName), "Input parameter [triggerGroupName] cannot be empty");

			scheduler.unscheduleJob(triggerName, triggerGroupName);
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while deleting trigger [" + triggerName + "] of trigger group [" + triggerGroupName
					+ "]", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public boolean saveTrigger(Trigger spagobiTrigger) {
		boolean overwrite;

		logger.debug("IN");

		try {
			Assert.assertNotNull(spagobiTrigger, "Input parameter [spagobiTrigger] cannot be null");

			org.quartz.Trigger quartzTrigger = QuartzNativeObjectsConverter.convertTriggerToNativeObject(spagobiTrigger);
			if (quartzTrigger.getGroup() == null)
				quartzTrigger.setGroup(Scheduler.DEFAULT_GROUP);
			adjustTenant(quartzTrigger);

			if (triggerExists(spagobiTrigger)) {
				scheduler.rescheduleJob(quartzTrigger.getName(), quartzTrigger.getGroup(), quartzTrigger);
				overwrite = true;
			} else {
				scheduler.scheduleJob(quartzTrigger);
				overwrite = false;
			}

		} catch (DAOException t) {
			throw t;
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while inserting trigger[" + spagobiTrigger + "]", t);
		} finally {
			logger.debug("OUT");
		}

		return overwrite;
	}

	private void adjustTenant(org.quartz.Trigger quartzTrigger) {
		String jobGroupName = quartzTrigger.getJobGroup();
		LogMF.debug(logger, "before: jobGroupName = [{0}]", jobGroupName);
		jobGroupName = this.applyTenant(jobGroupName);
		LogMF.debug(logger, "after: jobGroupName = [{0}]", jobGroupName);
		quartzTrigger.setJobGroup(jobGroupName);
	}

	@Override
	public void insertTrigger(Trigger spagobiTrigger) {
		logger.debug("IN");

		try {
			Assert.assertNotNull(spagobiTrigger, "Input parameter [spagobiTrigger] cannot be null");
			if (triggerExists(spagobiTrigger)) {
				throw new DAOException("Trigger [" + spagobiTrigger + "] already exists");
			}
			org.quartz.Trigger quartzTrigger = QuartzNativeObjectsConverter.convertTriggerToNativeObject(spagobiTrigger);
			adjustTenant(quartzTrigger);
			scheduler.scheduleJob(quartzTrigger);
		} catch (DAOException t) {
			throw t;
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while inserting trigger[" + spagobiTrigger + "]", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public void updateTrigger(Trigger spagobiTrigger) {
		logger.debug("IN");

		try {
			Assert.assertNotNull(spagobiTrigger, "Input parameter [spagobiTrigger] cannot be null");
			if (!triggerExists(spagobiTrigger)) {
				throw new DAOException("Trigger [" + spagobiTrigger + "] does not exist");
			}
			org.quartz.Trigger quartzTrigger = QuartzNativeObjectsConverter.convertTriggerToNativeObject(spagobiTrigger);
			adjustTenant(quartzTrigger);
			scheduler.rescheduleJob(quartzTrigger.getName(), quartzTrigger.getGroup(), quartzTrigger);
		} catch (DAOException t) {
			throw t;
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while inserting trigger[" + spagobiTrigger + "]", t);
		} finally {
			logger.debug("OUT");
		}
	}

	@Override
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	@Override
	public String getTenant() {
		logger.debug("IN");

		// if a tenant is set into the DAO object, it wins
		String tenantId = this.tenant;
		LogMF.debug(logger, "This DAO object instance tenant = [{0}]", tenantId);

		if (tenantId == null) {
			logger.debug("Tenant id not find in this DAO object instance nor in the user profile object; " + "looking for it using TenantManager ... ");
			// look for tenant using TenantManager
			Tenant tenant = TenantManager.getTenant();
			if (tenant != null) {
				tenantId = tenant.getName();
				LogMF.debug(logger, "TenantManager returns tenant = [{0}]", tenantId);
			} else {
				logger.debug("TenantManager did not return any Tenant");
			}
		}

		int index = tenantId.indexOf(GROUP_NAME_SEPARATOR);
		if (index > 0) {
			SpagoBIRuntimeException e = new SpagoBIRuntimeException("Tenant name [" + tenantId + "] not valid since it contains " + GROUP_NAME_SEPARATOR);
			throw e;
		}

		LogMF.debug(logger, "OUT: tenant = [{0}]", tenantId);
		return tenantId;
	}

	@Override
	public Tenant findTenant(JobDetail jobDetail) {
		logger.debug("IN");
		try {
			Assert.assertNotNull(jobDetail, "Input parameter [jobDetail] cannot be null");
			String groupName = jobDetail.getGroup();
			int index = groupName.indexOf(GROUP_NAME_SEPARATOR);
			if (index < 0) {
				SpagoBIRuntimeException e = new SpagoBIRuntimeException("Cannot find tenant name from string [" + groupName + "]");
				e.addHint("Job group name should start with [<tenant name>" + GROUP_NAME_SEPARATOR + "]. Check job definition.");
				throw e;
			}
			String tenant = groupName.substring(0, index);
			LogMF.debug(logger, "Tenant : [{0}]", tenant);
			return new Tenant(tenant);
		} catch (Throwable t) {
			throw new SpagoBIDOAException("An unexpected error occured while finding tenant for job [" + jobDetail + "]", t);
		} finally {
			logger.debug("OUT");
		}

	}

	// ----------------------------------------------------------------------------
	// Trigger_Paused Management
	// ----------------------------------------------------------------------------

	// insert the trigger info in the Trigger_Paused table
	@Override
	public void pauseTrigger(TriggerPaused triggerPaused) throws EMFUserError {
		logger.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiTriggerPaused hibTriggerPaused = null;

			// insertion
			logger.debug("Insert new TriggerPaused");
			hibTriggerPaused = fromTriggerPaused(triggerPaused);
			updateSbiCommonInfo4Insert(hibTriggerPaused);

			Integer newId = (Integer) aSession.save(hibTriggerPaused);
			tx.commit();

			triggerPaused.setId(newId);

		} catch (HibernateException he) {
			logger.error("HibernateException", he);

			if (tx != null)
				tx.rollback();

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen())
					aSession.close();
			}
		}
		logger.debug("OUT");

	}

	// delete the trigger info from the Trigger_Paused table
	@Override
	public boolean resumeTrigger(String triggerGroup, String triggerName, String jobGroup, String jobName) {
		logger.debug("IN");
		Session session = null;
		Transaction transaction = null;
		boolean deleted = false;

		try {
			if (triggerGroup == null) {
				throw new IllegalArgumentException("Input parameter [triggerGroup] cannot be null");
			}
			if (triggerName == null) {
				throw new IllegalArgumentException("Input parameter [triggerName] cannot be null");
			}
			if (jobGroup == null) {
				throw new IllegalArgumentException("Input parameter [jobGroup] cannot be null");
			}
			if (jobName == null) {
				throw new IllegalArgumentException("Input parameter [jobName] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			Query hibQuery = session.createQuery("from SbiTriggerPaused h where h.triggerName = ? and h.triggerGroup = ? and h.jobGroup = ? and h.jobName = ?");
			hibQuery.setString(0, triggerName);
			hibQuery.setString(1, triggerGroup);
			hibQuery.setString(2, jobGroup);
			hibQuery.setString(3, jobName);

			List<SbiTriggerPaused> sbiTriggerPausedList = hibQuery.list();
			for (SbiTriggerPaused sbiTriggerPaused : sbiTriggerPausedList) {
				if (sbiTriggerPaused != null) {
					// delete trigger
					session.delete(sbiTriggerPaused);
					deleted = true;
				}
			}
			transaction.commit();
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			String msg = (t.getMessage() != null) ? t.getMessage() : "An unexpected error occured while deleting trigger paused "
					+ "whose triggerName is equal to [" + triggerName + "] and trigger group [" + triggerName + "]";
			throw new SpagoBIDOAException(msg, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return deleted;

	}

	// check if the trigger is in the Trigger_Paused table
	@Override
	public boolean isTriggerPaused(String triggerGroup, String triggerName, String jobGroup, String jobName) {
		logger.debug("IN");
		Session session = null;
		Transaction transaction = null;
		boolean isTriggerPaused = false;

		try {
			if (triggerGroup == null) {
				throw new IllegalArgumentException("Input parameter [triggerGroup] cannot be null");
			}
			if (triggerName == null) {
				throw new IllegalArgumentException("Input parameter [triggerName] cannot be null");
			}
			if (jobGroup == null) {
				throw new IllegalArgumentException("Input parameter [jobGroup] cannot be null");
			}
			if (jobName == null) {
				throw new IllegalArgumentException("Input parameter [jobName] cannot be null");
			}

			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDOAException("An error occured while creating the new transaction", t);
			}

			Query hibQuery = session.createQuery("from SbiTriggerPaused h where h.triggerName = ? and h.triggerGroup = ? and h.jobGroup = ? and h.jobName = ?");
			hibQuery.setString(0, triggerName);
			hibQuery.setString(1, triggerGroup);
			hibQuery.setString(2, jobGroup);
			hibQuery.setString(3, jobName);

			List<SbiTriggerPaused> sbiTriggerPausedList = hibQuery.list();
			if (sbiTriggerPausedList.size() > 0) {
				isTriggerPaused = true;
			} else {
				isTriggerPaused = false;
			}

			transaction.commit();
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			String msg = (t.getMessage() != null) ? t.getMessage() : "An unexpected error occured while checking trigger paused "
					+ "whose triggerName is equal to [" + triggerName + "] and trigger group [" + triggerName + "]";
			throw new SpagoBIDOAException(msg, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return isTriggerPaused;
	}

	public SbiTriggerPaused fromTriggerPaused(TriggerPaused triggerPaused) {
		SbiTriggerPaused hibTriggerPaused = new SbiTriggerPaused();
		hibTriggerPaused.setId(triggerPaused.getId());
		hibTriggerPaused.setJobGroup(triggerPaused.getJobGroup());
		hibTriggerPaused.setJobName(triggerPaused.getJobName());
		hibTriggerPaused.setTriggerGroup(triggerPaused.getTriggerGroup());
		hibTriggerPaused.setTriggerName(triggerPaused.getTriggerName());
		return hibTriggerPaused;
	}

	@Override
	public void createOrUpdateJobAndTrigger(String jobName, Class jobClass, String groupName, String triggerGroup, Frequency frequency,
			Map<String, String> parameters) {
		Job job = createOrUpdateJob(jobName, groupName, jobClass, parameters);

		Calendar startTime = Calendar.getInstance();
		startTime.setTimeInMillis(frequency.getStartDate());

		String[] sp = frequency.getStartTime().split(":");
		String startHour = sp[0];
		String startMinute = sp[1];
		startTime.set(Calendar.HOUR_OF_DAY, new Integer(startHour).intValue());
		startTime.set(Calendar.MINUTE, new Integer(startMinute).intValue());

		Trigger trigger = new Trigger();

		if (frequency.getEndDate() != null) {
			Calendar endTime = Calendar.getInstance();
			endTime.setTimeInMillis(frequency.getEndDate());
			if (frequency.getEndTime() != null) {
				String[] endArr = frequency.getEndTime().split(":");
				String endHour = endArr[0];
				String endMinute = endArr[1];
				endTime.set(Calendar.HOUR_OF_DAY, new Integer(endHour).intValue());
				endTime.set(Calendar.MINUTE, new Integer(endMinute).intValue());
			}
			trigger.setEndTime(endTime.getTime());
		}

		trigger.setGroupName(triggerGroup);
		trigger.setStartTime(startTime.getTime());
		trigger.setChronType(frequency.getCron());
		if (frequency.getCron() != null) {
			trigger.setCronExpression(new CronExpression(frequency.getCron().replace('"', '\'')));
		}
		trigger.setName(job.getName());
		trigger.setJob(job);

		saveTrigger(trigger);
	}

	@Override
	public Job createOrUpdateJob(String name, String groupName, Class jobClass, Map<String, String> parameters) {
		Job job = null;
		boolean exists = jobExists(groupName, name);
		if (exists) {
			job = loadJob(groupName, name);
		} else {
			job = new Job();
			job.setName(name);
			job.setGroupName(groupName);
			job.setJobClass(jobClass);
			job.setDurable(false);
			job.setVolatile(false);
			job.setRequestsRecovery(true);
			if (parameters != null) {
				for (String key : parameters.keySet()) {
					job.addParameter(key, parameters.get(key));

				}
			}
		}
		if (!exists) {
			insertJob(job);
		}
		return job;
	}

	@Override
	public List<String> listTriggerPausedByGroup(final String triggerGroup, final String jobGroup) {
		return executeOnTransaction(new IExecuteOnTransaction<List<String>>() {
			@Override
			public List<String> execute(Session session) throws Exception {
				List<String> suspendedTriggers = session.createCriteria(SbiTriggerPaused.class).add(Restrictions.eq("triggerGroup", triggerGroup))
						.add(Restrictions.eq("jobGroup", jobGroup)).setProjection(Property.forName("triggerName")).list();
				return suspendedTriggers;
			}
		});
	}

	@Override
	public void pauseTrigger(String triggerGroup, String triggerName, String jobGroup, String jobName) throws EMFUserError {
		TriggerPaused triggerPaused = new TriggerPaused();
		triggerPaused.setJobGroup(jobGroup);
		triggerPaused.setJobName(jobName);
		triggerPaused.setTriggerGroup(triggerGroup);
		triggerPaused.setTriggerName(triggerName);
		pauseTrigger(triggerPaused);
	}

}
