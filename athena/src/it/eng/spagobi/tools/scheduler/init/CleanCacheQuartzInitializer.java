package it.eng.spagobi.tools.scheduler.init;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiTenant;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.jobs.CleanCacheJob;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;

public class CleanCacheQuartzInitializer implements InitializerIFace {

	public final String CRON_HOURLY = "0 0 0/1 1/1 * ? *";
	public final String CRON_DAILY = "0 0 0 1/1 * ? *";
	public final String CRON_WEEKLY = "0 0 0 ? * SUN *";
	public final String CRON_MONTHLY = "0 0 0 1 1/1 ? *";
	public final String CRON_YEARLY = "0 0 0 1 1 ? *";

	public final String DEFAULT_JOB_NAME = "CleanCacheJob";
	public final String DEFAULT_TRIGGER_NAME = "schedule_full_cache_cleaning";

	private final SourceBean _config = null;
	private transient Logger logger = Logger.getLogger(CleanCacheQuartzInitializer.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.init.InitializerIFace#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {
		logger.debug("IN");
		try {
			List<SbiTenant> tenants = DAOFactory.getTenantsDAO().loadAllTenants();
			for (SbiTenant tenant : tenants) {
				initCleanForTenant(tenant);
			}
		} finally {
			logger.debug("OUT");
		}
	}

	public void initCleanForTenant(SbiTenant tenant) {
		try {
			logger.debug("IN");
			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();
			schedulerDAO.setTenant(tenant.getName());
			Job jobDetail = schedulerDAO.loadJob(DEFAULT_JOB_NAME, DEFAULT_JOB_NAME);
			if (jobDetail == null) {
				// CREATE JOB DETAIL
				jobDetail = new Job();
				jobDetail.setName(DEFAULT_JOB_NAME);
				jobDetail.setGroupName(DEFAULT_JOB_NAME);
				jobDetail.setDescription(DEFAULT_JOB_NAME);
				jobDetail.setDurable(true);
				jobDetail.setVolatile(false);
				jobDetail.setRequestsRecovery(true);
				jobDetail.setJobClass(CleanCacheJob.class);

				schedulerDAO.insertJob(jobDetail);
				logger.debug("Added job with name " + DEFAULT_JOB_NAME);
			}
			String valueCheck = (String) SpagoBICacheConfiguration.getInstance().getCacheSchedulingFullClean();
			String cronExpression = getCronExpression(valueCheck);
			schedulerDAO.deleteTrigger(DEFAULT_TRIGGER_NAME, Scheduler.DEFAULT_GROUP);
			if (cronExpression != null) {
				String nameTrig = DEFAULT_TRIGGER_NAME;

				Trigger simpleTrigger = new Trigger();
				simpleTrigger.setName(nameTrig);
				simpleTrigger.setJob(jobDetail);
				simpleTrigger.getChronExpression().setExpression(cronExpression);
				simpleTrigger.setRunImmediately(false);

				schedulerDAO.insertTrigger(simpleTrigger);
				logger.debug("Added trigger with name " + DEFAULT_TRIGGER_NAME);
			} else {
				logger.debug("The value "
						+ valueCheck
						+ " is not a valid value for schedule cache cleaning trigger. Please provide a valid one and restart the Server. PERIODIC CACHE CLEANING DISABLED.");
			}
			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error while initializing scheduler ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.init.InitializerIFace#getConfig()
	 */
	public SourceBean getConfig() {
		return _config;
	}

	private String getCronExpression(String valueCheck) {
		if (valueCheck == null) {
			return null;
		}
		if (valueCheck.equals("HOURLY")) {
			return CRON_HOURLY;
		}
		if (valueCheck.equals("DAILY")) {
			return CRON_DAILY;
		}
		if (valueCheck.equals("WEEKLY")) {
			return CRON_WEEKLY;
		}
		if (valueCheck.equals("MONTHLY")) {
			return CRON_MONTHLY;
		}
		if (valueCheck.equals("YEARLY")) {
			return CRON_YEARLY;
		}
		return null;
	}
}
