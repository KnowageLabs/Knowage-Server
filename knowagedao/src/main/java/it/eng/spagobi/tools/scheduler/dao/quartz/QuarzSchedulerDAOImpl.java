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

import static it.eng.spagobi.tools.scheduler.dao.quartz.QuartzNativeObjectsConverter.GROUP_NAME_SEPARATOR;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ObjectAlreadyExistsException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import it.eng.qbe.datasource.configuration.dao.DAOException;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.IExecuteOnTransaction;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
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

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class QuarzSchedulerDAOImpl extends AbstractHibernateDAO implements ISchedulerDAO {

	private static final Logger LOGGER = LogManager.getLogger(QuarzSchedulerDAOImpl.class);

	private Scheduler scheduler;

	private String tenant;

	private boolean global = false;

	public QuarzSchedulerDAOImpl() {
		LOGGER.debug("IN");
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
		} catch (Throwable t) {
			throw new SpagoBIDAOException("Impossible to access to the default quartz scheduler", t);
		} finally {
			LOGGER.debug("OUT");
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

			if (!jobGroupExists(jobGroupName)) {
				return false;
			}

			jobGroupName = global ? jobGroupName : this.applyTenant(jobGroupName);
			List<String> jobNames = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName)).stream()
					.map(JobKey::getName).collect(toList());
			for (String currJobName : jobNames) {
				if (jobName.equalsIgnoreCase(currJobName)) {
					exists = true;
					break;
				}
			}
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while checking for the existence of job ["
					+ jobName + "] in group [" + jobGroupName + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}
		return exists;
	}

	private String applyTenant(String jobGroupName) {
		LOGGER.debug("IN: jobGroupName = [{}]", jobGroupName);
		String tenant = this.getTenant();
		if (tenant != null) {
			jobGroupName = this.getTenantPrefix(tenant) + jobGroupName;
		}
		LOGGER.debug("OUT: jobGroupName = [{}]", jobGroupName);
		return jobGroupName;
	}

	private String removeTenant(String jobGroupName) {
		LOGGER.debug("IN: jobGroupName = [{}]", jobGroupName);
		String tenant = this.getTenant();
		if (tenant != null && !global) {
			jobGroupName = jobGroupName.substring(this.getTenantPrefix(tenant).length());
		}
		LOGGER.debug("OUT: jobGroupName = [{}]", jobGroupName);
		return jobGroupName;
	}

	@Override
	public List<String> getJobGroupNames() {
		List<String> jobGroupNames = new ArrayList<>();

		LOGGER.debug("IN");

		try {
			List<String> names = scheduler.getJobGroupNames();
			jobGroupNames.addAll(names);
			if (!global) {
				jobGroupNames = filterForTenant(jobGroupNames);
			}
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while loading job group names", t);
		} finally {
			LOGGER.debug("OUT");
		}

		return jobGroupNames;
	}

	private List<String> filterForTenant(List<String> jobGroupNames) {
		LOGGER.debug("IN: jobGroupNames = [{}]", jobGroupNames);
		String tenant = this.getTenant();
		List<String> toReturn = new ArrayList<>();
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
		LOGGER.debug("OUT: jobGroupNames = [{}]", toReturn);
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

		LOGGER.debug("IN");

		jobs = new ArrayList<>();

		try {
			List<String> jobGroupNames = getJobGroupNames();
			jobs = loadJobs(jobGroupNames);
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while loading jobs", t);
		} finally {
			LOGGER.debug("OUT");
		}

		return jobs;
	}

	/**
	 * @param jobGroupNames the list of group names in which to look for jobs. It can be empty but it cannot be null. If it is an empty list an empty list of jobs
	 *                      will be returned.
	 * @return the jobs contained in the specified groups. Never returns null. If there are no jobs in the specified groups it returns an empty list of jobs
	 */
	@Override
	public List<Job> loadJobs(List<String> jobGroupNames) {
		List<Job> jobs;

		LOGGER.debug("IN");

		jobs = new ArrayList<>();

		try {
			Assert.assertNotNull(jobGroupNames, "Input parameter [jobGroupNames] cannot be null");

			for (String jobGroupName : jobGroupNames) {
				List<Job> jobDetailsInGroup = loadJobs(jobGroupName);
				jobs.addAll(jobDetailsInGroup);
			}
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while loading jobs", t);
		} finally {
			LOGGER.debug("OUT");
		}

		return jobs;
	}

	/**
	 * @param jobGroupName the name of the group in which to look for jobs. It it cannot be empty.
	 * @return the jobs contained in the specified group. Never returns null. If there are no jobs in the specified group it returns an empty list of jobs
	 */
	@Override
	public List<Job> loadJobs(String jobGroupName) {
		List<Job> jobs;

		LOGGER.debug("IN");

		jobs = new ArrayList<>();

		try {
			Assert.assertTrue(StringUtils.isNotEmpty(jobGroupName), "Input parameter [jobGroupName] cannot be empty");

			String actualJobGroupName = global ? jobGroupName : this.applyTenant(jobGroupName);
			List<String> jobNames = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(actualJobGroupName)).stream()
					.map(JobKey::getName).collect(toList());

			if (jobNames != null) {
				LOGGER.debug("Job group [" + jobGroupName + "] contains [" + jobNames.size() + "] job(s)");
				for (String jobName : jobNames) {
					Job job = loadJob(jobGroupName, jobName);
					if (job != null) {
						jobs.add(job);
					} else {
						LOGGER.warn("Impossible to load job [" + jobName + "] from group [" + jobGroupName + "]");
					}
				}
			} else {
				LOGGER.debug("Job group [" + jobGroupName + "] does not contain jobs");
			}
		} catch (Throwable t) {
			throw new SpagoBIDAOException(
					"An unexpected error occured while loading jobs of group [" + jobGroupName + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}

		return jobs;
	}

	/**
	 * @param jobGroupName the name of the group in which to look up. It it cannot be empty.
	 * @param jobName      the name of the job to load. It it cannot be empty.
	 * @return the job if exists a job named jobName in group jobGroupName. null otherwise
	 */
	@Override
	public Job loadJob(String jobGroupName, String jobName) {
		Job job;

		LOGGER.debug("IN");

		job = null;
		try {
			Assert.assertTrue(StringUtils.isNotEmpty(jobGroupName), "Input parameter [jobGroupName] cannot be empty");
			Assert.assertTrue(StringUtils.isNotEmpty(jobName), "Input parameter [jobName] cannot be empty");

			String actualJobGroupName = global ? jobGroupName : this.applyTenant(jobGroupName);
			JobKey jobKey = JobKey.jobKey(jobName, actualJobGroupName);
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);
			if (jobDetail != null) {
				job = QuartzNativeObjectsConverter.convertJobFromNativeObject(jobDetail);
				adjustTenant(job);
				LOGGER.debug("Job [" + jobName + "] succesfully loaded from group [" + jobGroupName + "]");
			} else {
				LOGGER.debug("Job [" + jobName + "] not found in group [" + jobGroupName + "]");
			}
		} catch (Throwable t) {
			throw new SpagoBIDAOException(
					"An unexpected error occured while loading job [" + jobName + "] in group [" + jobGroupName + "]",
					t);
		} finally {
			LOGGER.debug("OUT");
		}

		return job;
	}

	private void adjustTenant(Job job) {
		String tenant = this.getTenant();
		if (tenant != null) {
			String jobGroupName = job.getGroupName();
			LOGGER.debug("before: jobGroupName = [{}]", jobGroupName);
			job.setGroupName(this.removeTenant(jobGroupName));
			LOGGER.debug("after: jobGroupName = [{}]", job.getGroupName());
		}
	}

	@Override
	public void deleteJob(String jobName, String jobGroupName) {
		LOGGER.debug("IN");

		try {
			// TODO delete trigger associated to the job first (?)
			// Reply: no need to delete trigger because deleteJob(...) loops through all the triggers having a reference to this job,
			// to unschedule them and removes the job from the jobstore
			String actualJobGroupName = global ? jobGroupName : this.applyTenant(jobGroupName);
			JobKey jobKey = JobKey.jobKey(jobName, actualJobGroupName);
			scheduler.deleteJob(jobKey);
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while deleting job [" + jobName
					+ "] of job group [" + jobGroupName + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@Override
	public void insertJob(Job spagobiJob) {

		LOGGER.debug("IN");

		try {
			Assert.assertNotNull(spagobiJob, "Input parameter [spagobiJob] cannot be null");
			JobDetail quartzJob = QuartzNativeObjectsConverter.convertJobToNativeObject(getTenant(), spagobiJob,
					global);
			scheduler.addJob(quartzJob, true);
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while inserting job [" + spagobiJob + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@Override
	public boolean triggerExists(Trigger spagobiTrigger) {
		boolean exists;

		LOGGER.debug("IN");

		exists = false;
		try {
			Assert.assertNotNull(spagobiTrigger, "Input parameter [trigger] cannot be null");
			Assert.assertNotNull(spagobiTrigger.getJob(), "The attribute [job] of trigger cannot be null");
			Assert.assertTrue(StringUtils.isNotEmpty(spagobiTrigger.getJob().getName()),
					"The attribute [name] of the job associated to the trigger cannot be empty]");
			Assert.assertTrue(StringUtils.isNotEmpty(spagobiTrigger.getJob().getGroupName()),
					"The attribute [groupName] of the job associated to the trigger cannot be empty]");

			List<Trigger> jobTriggers = loadTriggers(spagobiTrigger.getJob().getGroupName(),
					spagobiTrigger.getJob().getName());
			for (Trigger jobTrigger : jobTriggers) {
				if (jobTrigger.getName().equals(spagobiTrigger.getName())) {
					exists = true;
					break;
				}
			}
		} catch (Throwable t) {
			throw new SpagoBIDAOException(
					"An unexpected error occured while checking for the existence of trigger ["
							+ spagobiTrigger.getName() + "] of trigger group [" + spagobiTrigger.getGroupName() + "]",
					t);
		} finally {
			LOGGER.debug("OUT");
		}

		return exists;
	}

	@Override
	public Trigger loadTrigger(String triggerGroupName, String triggerName) {
		Trigger spagobiTrigger;

		LOGGER.debug("IN");

		spagobiTrigger = null;
		try {
			Assert.assertTrue(StringUtils.isNotEmpty(triggerGroupName),
					"Input parameter [triggerGroupName] cannot be null");
			Assert.assertTrue(StringUtils.isNotEmpty(triggerName), "Input parameter [triggerName] cannot be null");

			TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
			org.quartz.Trigger quartzTrigger = scheduler.getTrigger(triggerKey);
			if (quartzTrigger != null) {
				spagobiTrigger = QuartzNativeObjectsConverter.convertTriggerFromNativeObject(quartzTrigger);
				adjustTenant(spagobiTrigger);
			}
		} catch (Throwable t) {
			throw new SpagoBIDAOException(
					"An unexpected error occured while checking for the existence of trigger ["
							+ spagobiTrigger.getName() + "] of trigger group [" + spagobiTrigger.getGroupName() + "]",
					t);
		} finally {
			LOGGER.debug("OUT");
		}

		return spagobiTrigger;
	}

	@Override
	public List<Trigger> loadTriggers(String jobGroupName, String jobName) {
		List<Trigger> spagobiTriggers;

		LOGGER.debug("IN");

		spagobiTriggers = new ArrayList<>();
		try {
			String actualJobGroupName = global ? jobGroupName : this.applyTenant(jobGroupName);
			JobKey jobKey = JobKey.jobKey(jobName, actualJobGroupName);
			List<? extends org.quartz.Trigger> quartzTriggers = scheduler.getTriggersOfJob(jobKey);
			for (org.quartz.Trigger quartzTrigger : quartzTriggers) {
				Trigger spagobiTrigger = QuartzNativeObjectsConverter.convertTriggerFromNativeObject(quartzTrigger);
				adjustTenant(spagobiTrigger);
				spagobiTriggers.add(spagobiTrigger);
			}
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while loading trigger s of job [" + jobName
					+ "] in job group [" + jobGroupName + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}

		return spagobiTriggers;
	}

	private void adjustTenant(Trigger spagobiTrigger) {
		Job job = spagobiTrigger.getJob();
		String jobGroupName = job.getGroupName();
		LOGGER.debug("before: jobGroupName = [{}]", jobGroupName);
		jobGroupName = this.removeTenant(jobGroupName);
		LOGGER.debug("after: jobGroupName = [{}]", jobGroupName);
		job.setGroupName(jobGroupName);
	}

	@Override
	public void deleteTrigger(String triggerName, String triggerGroupName) {
		LOGGER.debug("IN");

		try {
			Assert.assertTrue(StringUtils.isNotEmpty(triggerName), "Input parameter [triggerName] cannot be empty");
			Assert.assertTrue(StringUtils.isNotEmpty(triggerGroupName),
					"Input parameter [triggerGroupName] cannot be empty");

			TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
			scheduler.unscheduleJob(triggerKey);
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while deleting trigger [" + triggerName
					+ "] of trigger group [" + triggerGroupName + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@Override
	public boolean saveTrigger(Trigger spagobiTrigger) {
		boolean overwrite;

		LOGGER.debug("IN");

		try {
			Assert.assertNotNull(spagobiTrigger, "Input parameter [spagobiTrigger] cannot be null");

			org.quartz.Trigger quartzTrigger = QuartzNativeObjectsConverter.convertTriggerToNativeObject(getTenant(),
					spagobiTrigger, global);

			if (triggerExists(spagobiTrigger)) {
				TriggerKey triggerKey = quartzTrigger.getKey();
				scheduler.rescheduleJob(triggerKey, quartzTrigger);
				overwrite = true;
			} else {
				scheduler.scheduleJob(quartzTrigger);
				overwrite = false;
			}

		} catch (ObjectAlreadyExistsException oaee) {
			LOGGER.error("Trigger already existing");
			throw new SpagoBIDAOException("Trigger already existing[" + spagobiTrigger.getName() + "]", oaee);

		} catch (DAOException t) {
			throw t;
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while inserting trigger[" + spagobiTrigger + "]",
					t);
		} finally {
			LOGGER.debug("OUT");
		}

		return overwrite;
	}

	@Override
	public void insertTrigger(Trigger spagobiTrigger) {
		LOGGER.debug("IN");

		try {
			Assert.assertNotNull(spagobiTrigger, "Input parameter [spagobiTrigger] cannot be null");
			if (triggerExists(spagobiTrigger)) {
				throw new DAOException("Trigger [" + spagobiTrigger + "] already exists");
			}
			org.quartz.Trigger quartzTrigger = QuartzNativeObjectsConverter.convertTriggerToNativeObject(getTenant(),
					spagobiTrigger, global);
			scheduler.scheduleJob(quartzTrigger);
		} catch (DAOException t) {
			throw t;
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while inserting trigger[" + spagobiTrigger + "]",
					t);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@Override
	public void updateTrigger(Trigger spagobiTrigger) {
		LOGGER.debug("IN");

		try {
			Assert.assertNotNull(spagobiTrigger, "Input parameter [spagobiTrigger] cannot be null");
			if (!triggerExists(spagobiTrigger)) {
				throw new DAOException("Trigger [" + spagobiTrigger + "] does not exist");
			}
			org.quartz.Trigger quartzTrigger = QuartzNativeObjectsConverter.convertTriggerToNativeObject(getTenant(),
					spagobiTrigger, global);
			TriggerKey key = quartzTrigger.getKey();
			scheduler.rescheduleJob(key, quartzTrigger);
		} catch (DAOException t) {
			throw t;
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An unexpected error occured while inserting trigger[" + spagobiTrigger + "]",
					t);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	@Override
	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	@Override
	public String getTenant() {
		LOGGER.debug("IN");

		// if a tenant is set into the DAO object, it wins
		String tenantId = this.tenant;
		LOGGER.debug("This DAO object instance tenant = [{}]", tenantId);

		if (tenantId == null) {
			LOGGER.debug("Tenant id not find in this DAO object instance nor in the user profile object; "
					+ "looking for it using TenantManager ... ");
			// look for tenant using TenantManager
			Tenant tenant = TenantManager.getTenant();
			if (tenant != null) {
				tenantId = tenant.getName();
				LOGGER.debug("TenantManager returns tenant = [{}]", tenantId);
			} else {
				LOGGER.debug("TenantManager did not return any Tenant");
			}
		}

		int index = tenantId.indexOf(GROUP_NAME_SEPARATOR);
		if (index > 0) {
			throw new SpagoBIRuntimeException(
					"Tenant name [" + tenantId + "] not valid since it contains " + GROUP_NAME_SEPARATOR);
		}

		LOGGER.debug("OUT: tenant = [{}]", tenantId);
		return tenantId;
	}

	@Override
	public Tenant findTenant(JobDetail jobDetail) {
		LOGGER.debug("IN");
		try {
			Assert.assertNotNull(jobDetail, "Input parameter [jobDetail] cannot be null");
			String groupName = jobDetail.getKey().getGroup();
			int index = groupName.indexOf(GROUP_NAME_SEPARATOR);
			if (index < 0) {
				SpagoBIRuntimeException e = new SpagoBIRuntimeException(
						"Cannot find tenant name from string [" + groupName + "]");
				e.addHint("Job group name should start with [<tenant name>" + GROUP_NAME_SEPARATOR
						+ "]. Check job definition.");
				throw e;
			}
			String tenant = groupName.substring(0, index);
			LOGGER.debug("Tenant : [{}]", tenant);
			return new Tenant(tenant);
		} catch (Throwable t) {
			throw new SpagoBIDAOException(
					"An unexpected error occured while finding tenant for job [" + jobDetail + "]", t);
		} finally {
			LOGGER.debug("OUT");
		}

	}

	// ----------------------------------------------------------------------------
	// Trigger_Paused Management
	// ----------------------------------------------------------------------------

	// insert the trigger info in the Trigger_Paused table
	@Override
	public void pauseTrigger(TriggerPaused triggerPaused) throws EMFUserError {
		LOGGER.debug("IN");
		Session aSession = null;
		Transaction tx = null;

		try {
			aSession = getSession();
			tx = aSession.beginTransaction();

			SbiTriggerPaused hibTriggerPaused = null;

			// insertion
			LOGGER.debug("Insert new TriggerPaused");
			hibTriggerPaused = fromTriggerPaused(triggerPaused);
			updateSbiCommonInfo4Insert(hibTriggerPaused);

			Integer newId = (Integer) aSession.save(hibTriggerPaused);
			tx.commit();

			triggerPaused.setId(newId);

		} catch (HibernateException he) {
			LOGGER.error("HibernateException", he);

			if (tx != null) {
				tx.rollback();
			}

			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);

		} finally {
			if (aSession != null) {
				if (aSession.isOpen()) {
					aSession.close();
				}
			}
		}
		LOGGER.debug("OUT");

	}

	// delete the trigger info from the Trigger_Paused table
	@Override
	public boolean resumeTrigger(String triggerGroup, String triggerName, String jobGroup, String jobName) {
		LOGGER.debug("IN");
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
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			Query hibQuery = session.createQuery(
					"from SbiTriggerPaused h where h.triggerName = ? and h.triggerGroup = ? and h.jobGroup = ? and h.jobName = ?");
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
			String msg = (t.getMessage() != null) ? t.getMessage()
					: "An unexpected error occured while deleting trigger paused " + "whose triggerName is equal to ["
							+ triggerName + "] and trigger group [" + triggerName + "]";
			throw new SpagoBIDAOException(msg, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			LOGGER.debug("OUT");
		}
		return deleted;

	}

	// check if the trigger is in the Trigger_Paused table
	@Override
	public boolean isTriggerPaused(String triggerGroup, String triggerName, String jobGroup, String jobName) {
		LOGGER.debug("IN");
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
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			Query hibQuery = session.createQuery(
					"from SbiTriggerPaused h where h.triggerName = ? and h.triggerGroup = ? and h.jobGroup = ? and h.jobName = ?");
			hibQuery.setString(0, triggerName);
			hibQuery.setString(1, triggerGroup);
			hibQuery.setString(2, jobGroup);
			hibQuery.setString(3, jobName);

			List<SbiTriggerPaused> sbiTriggerPausedList = hibQuery.list();
			if (!sbiTriggerPausedList.isEmpty()) {
				isTriggerPaused = true;
			} else {
				isTriggerPaused = false;
			}

			transaction.commit();
		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			String msg = (t.getMessage() != null) ? t.getMessage()
					: "An unexpected error occured while checking trigger paused " + "whose triggerName is equal to ["
							+ triggerName + "] and trigger group [" + triggerName + "]";
			throw new SpagoBIDAOException(msg, t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			LOGGER.debug("OUT");
		}
		return isTriggerPaused;
	}

	public SbiTriggerPaused fromTriggerPaused(TriggerPaused triggerPaused) {
		SbiTriggerPaused hibTriggerPaused = new SbiTriggerPaused(triggerPaused.getId());
		hibTriggerPaused.setJobGroup(triggerPaused.getJobGroup());
		hibTriggerPaused.setJobName(triggerPaused.getJobName());
		hibTriggerPaused.setTriggerGroup(triggerPaused.getTriggerGroup());
		hibTriggerPaused.setTriggerName(triggerPaused.getTriggerName());
		return hibTriggerPaused;
	}

	@Override
	public void createOrUpdateJobAndTrigger(String jobName, Class jobClass, String groupName, String triggerGroup,
			Frequency frequency, Map<String, String> parameters) {
		Job job = createOrUpdateJob(jobName, groupName, jobClass, parameters);

		Calendar startTime = Calendar.getInstance();
		startTime.setTimeInMillis(frequency.getStartDate());

		String startTimeString = frequency.getStartTime();
		if (StringUtils.isNotEmpty(startTimeString)) {
			String[] sp = startTimeString.split(":");
			String startHour = sp[0];
			String startMinute = sp[1];
			startTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
			startTime.set(Calendar.MINUTE, Integer.parseInt(startMinute));
		}

		Trigger trigger = new Trigger();

		if (frequency.getEndDate() != null) {
			Calendar endTime = Calendar.getInstance();
			endTime.setTimeInMillis(frequency.getEndDate());
			String endTimeString = frequency.getEndTime();
			if (StringUtils.isNotEmpty(endTimeString)) {
				String[] endArr = endTimeString.split(":");
				String endHour = endArr[0];
				String endMinute = endArr[1];
				endTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHour));
				endTime.set(Calendar.MINUTE, Integer.parseInt(endMinute));
			}
			trigger.setEndTime(endTime.getTime());
		}

		trigger.setGroupName(triggerGroup);
		trigger.setStartTime(startTime.getTime());
		trigger.setChronType(frequency.getCron());
		if (frequency.getCron() != null && !frequency.getCron().equals("null")) {
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
			/*
			 * Fixes the Quartz error: Jobs added with no trigger must be durable.
			 */
			job.setDurable(true);
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
				List<String> suspendedTriggers = session.createCriteria(SbiTriggerPaused.class)
						.add(Restrictions.eq("triggerGroup", triggerGroup)).add(Restrictions.eq("jobGroup", jobGroup))
						.setProjection(Property.forName("triggerName")).list();
				return suspendedTriggers;
			}
		});
	}

	@Override
	public void pauseTrigger(String triggerGroup, String triggerName, String jobGroup, String jobName)
			throws EMFUserError {
		TriggerPaused triggerPaused = new TriggerPaused();
		triggerPaused.setJobGroup(jobGroup);
		triggerPaused.setJobName(jobName);
		triggerPaused.setTriggerGroup(triggerGroup);
		triggerPaused.setTriggerName(triggerName);
		pauseTrigger(triggerPaused);
	}

	@Override
	public boolean getGlobal() {
		return global;
	}

	@Override
	public void setGlobal(boolean value) {
		this.global = value;
	}

	@Override
	public boolean deleteTriggerWhereNameLikes(String name) {
		boolean ret = true;
		try {
			Set<TriggerKey> allTriggers = scheduler.getTriggerKeys(GroupMatcher.anyGroup());

			for (TriggerKey triggerKey : allTriggers) {
				if (triggerKey.getName().equals(name)) {
					scheduler.unscheduleJob(triggerKey);
				}
			}

		} catch (SchedulerException e) {
			LOGGER.error(e);
			ret = false;
		}
		return ret;
	}

	@Override
	public boolean deleteJobWhereNameLikes(String name) {
		boolean ret = true;
		try {
			Set<JobKey> allTriggers = scheduler.getJobKeys(GroupMatcher.anyGroup());

			for (JobKey jobKey : allTriggers) {
				if (jobKey.getName().equals(name)) {
					scheduler.deleteJob(jobKey);
				}
			}

		} catch (SchedulerException e) {
			LOGGER.error(e);
			ret = false;
		}
		return ret;
	}
}
