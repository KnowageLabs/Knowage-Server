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
package it.eng.spagobi.tools.scheduler.jobs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.services.JobManagementModule;
import it.eng.spagobi.tools.scheduler.wsEvents.SbiWsEvent;
import it.eng.spagobi.tools.scheduler.wsEvents.dao.SbiWsEventsDao;

public class RestEventJob extends AbstractSpagoBIJob implements Job {

	private static final String TRIGGER_NAME_PREFIX = "schedule_rest_event_";

	static private Logger logger = Logger.getLogger(RestEventJob.class);

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		logger.debug("IN");
		try {
			setTenant(jobExecutionContext);
			executeInternal();
		} finally {
			this.unsetTenant();
			logger.debug("OUT");
		}
	}

	private void executeInternal() throws JobExecutionException {

		logger.debug("IN");
		try {
			SbiWsEventsDao dao = DAOFactory.getWsEventsDao();
			List<SbiWsEvent> events = dao.loadSbiWsEventsNotConsumed();
			if (events.size() > 0) {
				Set<String> eventNames = new HashSet<>();
				for (SbiWsEvent sbiWsEvent : events) {
					if (sbiWsEvent.getTakeChargeDate() == null) {
						eventNames.add(sbiWsEvent.getEventName());
					}
				}

				for (String eventName : eventNames) {
					executeEvent(eventName);
				}

				logger.debug("REST events scheduled successfully!");
			}
		} catch (Exception e) {
			logger.error("Error while executiong job ", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void executeEvent(String jobName) {
		logger.debug("IN");

		ISchedulerDAO schedulerDAO = null;
		try {
			schedulerDAO = DAOFactory.getSchedulerDAO();
			schedulerDAO.setTenant(getTenant().getName());

			String jobGroup = JobManagementModule.JOB_GROUP;
			// it.eng.spagobi.tools.scheduler.bo.Job jobDetail = schedulerDAO.loadJob(jobGroup, jobName);

			List<Trigger> triggers = schedulerDAO.loadTriggers(jobGroup, jobName);
			for (Trigger trigger : triggers) {
				String triggerName = trigger.getName();
				if (!schedulerDAO.isTriggerPaused(trigger.getGroupName(), triggerName, jobGroup, jobName)) {
					it.eng.spagobi.tools.scheduler.bo.Job job = trigger.getJob();

					Map<String, String> map = new HashMap<>();
					map.put("type", "rest");
					job.addParameter("event_info", new JSONObject(map).toString());

					job.addParameter("originalTriggerName", triggerName);
					trigger.setName(TRIGGER_NAME_PREFIX + triggerName);

					trigger.setRunImmediately(true);

					schedulerDAO.insertTrigger(trigger);
				}
			}
			// Trigger trigger = new Trigger();
			// trigger.setName(TRIGGER_NAME);
			// trigger.setJob(jobDetail);
			// trigger.setRunImmediately(true);
			//
			// schedulerDAO.insertTrigger(trigger);
			// logger.debug("Added trigger with name " + TRIGGER_NAME);

			logger.debug("OUT");
		} catch (Exception e) {
			logger.error("Error while initializing scheduler ", e);
		} finally {
			if (schedulerDAO != null) {
				schedulerDAO.setTenant(null);
			}
		}

	}
}
