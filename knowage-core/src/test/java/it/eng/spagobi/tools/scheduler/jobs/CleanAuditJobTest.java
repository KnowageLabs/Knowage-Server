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
package it.eng.spagobi.tools.scheduler.jobs;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;

public class CleanAuditJobTest {
	public final String DEFAULT_JOB_NAME = "CleanAuditJob";
	public final String DEFAULT_TRIGGER_NAME = "schedule_clean_audit";
	private transient Logger logger = Logger.getLogger(CleanAuditJobTest.class);

	@Test
	public void launchJob() {

		String jobName = DEFAULT_JOB_NAME;
		String jobGroup = Scheduler.DEFAULT_GROUP;
		String jobDescription = "Delete old audit data %s";

		JobDataMap jobDataMap = new JobDataMap();

		ISchedulerDAO schedulerDAO = null;
		schedulerDAO = DAOFactory.getSchedulerDAO();
		schedulerDAO.setTenant("ten");
		JobKey jobKey = JobKey.jobKey(jobName, jobGroup);
		Job jobDetail = schedulerDAO.loadJob(DEFAULT_JOB_NAME, DEFAULT_JOB_NAME);
//		JobDetail job = JobBuilder.newJob().withIdentity(jobKey).ofType(CleanAuditJob.class).withDescription(jobDescription).usingJobData(jobDataMap)
//				.storeDurably().build();

		try {
			String nameTrig = DEFAULT_TRIGGER_NAME;

			Trigger simpleTrigger = new Trigger();
			simpleTrigger.setName(nameTrig);
			simpleTrigger.setJob(jobDetail);
			simpleTrigger.setRunImmediately(true);

			schedulerDAO.insertTrigger(simpleTrigger);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
