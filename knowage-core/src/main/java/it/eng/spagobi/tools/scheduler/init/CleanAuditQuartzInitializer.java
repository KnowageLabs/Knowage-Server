/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.
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

import org.apache.log4j.Logger;
import org.quartz.Scheduler;

import it.eng.spago.base.SourceBean;
import it.eng.spago.init.InitializerIFace;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.jobs.CleanAuditJob;
import it.eng.spagobi.tools.scheduler.utils.PredefinedCronExpression;

public class CleanAuditQuartzInitializer implements InitializerIFace {

	public final String DEFAULT_JOB_NAME = "CleanAuditJob";
	public final String DEFAULT_TRIGGER_NAME = "schedule_clean_audit";

	private final SourceBean _config = null;
	private transient Logger logger = Logger.getLogger(CleanAuditQuartzInitializer.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see it.eng.spago.init.InitializerIFace#init(it.eng.spago.base.SourceBean)
	 */
	@Override
	public void init(SourceBean config) {
		logger.debug("IN");

		try {
			initCleanForDefaultTenant();
		} catch (Exception e) {
		} finally {
			logger.debug("OUT");
		}

		logger.debug("OUT");
	}

	public void initCleanForDefaultTenant() {
		logger.debug("IN");

		ISchedulerDAO schedulerDAO = null;
		try {
			schedulerDAO = DAOFactory.getSchedulerDAO();
			schedulerDAO.setGlobal(true);
			schedulerDAO.setTenant("DEFAULT_TENANT");

			Job jobDetail = schedulerDAO.loadJob(DEFAULT_JOB_NAME, DEFAULT_JOB_NAME);
			if (jobDetail == null) { // create job detail
				jobDetail = new Job();
				jobDetail.setName(DEFAULT_JOB_NAME);
				jobDetail.setGroupName(DEFAULT_JOB_NAME);
				jobDetail.setDescription(DEFAULT_JOB_NAME);
				jobDetail.setDurable(true);
				jobDetail.setVolatile(false);
				jobDetail.setRequestsRecovery(true);
				jobDetail.setJobClass(CleanAuditJob.class);

				schedulerDAO.insertJob(jobDetail);
				logger.debug("Added job with name " + DEFAULT_JOB_NAME);
			}

			String cronExpression = PredefinedCronExpression.DAILY.getExpression();
			schedulerDAO.deleteTrigger(DEFAULT_TRIGGER_NAME, Scheduler.DEFAULT_GROUP);

			String nameTrig = DEFAULT_TRIGGER_NAME;

			Trigger simpleTrigger = new Trigger();
			simpleTrigger.setName(nameTrig);
			simpleTrigger.setJob(jobDetail);
			simpleTrigger.getChronExpression().setExpression(cronExpression);
			simpleTrigger.setRunImmediately(false);

			schedulerDAO.insertTrigger(simpleTrigger);
			logger.debug("Added trigger with name " + DEFAULT_TRIGGER_NAME);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error while initializing scheduler ", e);
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
}
