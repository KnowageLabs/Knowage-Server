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
package it.eng.spagobi.tools.scheduler.init;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Scheduler;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.dataset.cache.SpagoBICacheConfiguration;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.jobs.CleanCacheJob;
import it.eng.spagobi.tools.scheduler.utils.PredefinedCronExpression;

public class CleanCacheQuartzInitializer implements InitializerIFace {

	public static final String DEFAULT_JOB_NAME = "CleanCacheJob";
	public static final String DEFAULT_TRIGGER_NAME = "schedule_full_cache_cleaning";

	private final SourceBean _config = null;
	private static final Logger LOGGER = LogManager.getLogger(CleanCacheQuartzInitializer.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.init.InitializerIFace#init(it.eng.spago.base.SourceBean)
	 */
	@Override
	public void init(SourceBean config) {
		LOGGER.debug("IN");
		try {
			initCleanForDefaultTenant();
		} catch (Exception e) {
			LOGGER.debug("NO WRITE DATASOURCE AVAILABLE.", e);
		} finally {
			LOGGER.debug("OUT");
		}
	}

	public void initCleanForDefaultTenant() {

		ISchedulerDAO schedulerDAO = null;
		try {
			LOGGER.debug("IN");
			schedulerDAO = DAOFactory.getSchedulerDAO();
			schedulerDAO.setTenant("DEFAULT_TENANT");
			schedulerDAO.setGlobal(true);

			// WORKAROUND : Fix the past
			// TODO : could be deleted in version 9
			schedulerDAO.deleteTriggerWhereNameLikes(DEFAULT_TRIGGER_NAME);
			schedulerDAO.deleteJobWhereNameLikes(DEFAULT_JOB_NAME);

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
				LOGGER.debug("Added job with name " + DEFAULT_JOB_NAME);
			}
			String valueCheck = SpagoBICacheConfiguration.getInstance().getCacheSchedulingFullClean();
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
				LOGGER.debug("Added trigger with name " + DEFAULT_TRIGGER_NAME);
			} else {
				LOGGER.debug("The value " + valueCheck
						+ " is not a valid value for schedule cache cleaning trigger. Please provide a valid one and restart the Server. PERIODIC CACHE CLEANING DISABLED.");
			}
			LOGGER.debug("OUT");
		} catch (Exception e) {
			LOGGER.error("Error while initializing scheduler ", e);
		} finally {
			if (schedulerDAO != null) {
				schedulerDAO.setTenant(null);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.init.InitializerIFace#getConfig()
	 */
	@Override
	public SourceBean getConfig() {
		return _config;
	}

	private String getCronExpression(String valueCheck) {
		if (valueCheck == null) {
			LOGGER.debug("This value is [" + valueCheck + "]");
			return null;
		}

		for (PredefinedCronExpression value : PredefinedCronExpression.values()) {
			if (valueCheck.equalsIgnoreCase(value.getLabel())) {
				LOGGER.debug("Found a predefined cron expression with label equals to [" + valueCheck + "]");
				LOGGER.debug("The cron expression is equals to [" + value.getExpression() + "]");
				return value.getExpression();
			}
		}
		LOGGER.debug("No predefined cron expression found with label equals to [" + valueCheck + "]. Returning null.");
		return null;
	}
}
