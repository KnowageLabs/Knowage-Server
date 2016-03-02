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
package it.eng.spagobi.engines.kpi.service;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;
import org.safehaus.uuid.UUIDGenerator;

/**
 * @author Angelo Bernabei angelo.bernabei@eng.it
 */
public class InsertJob {

	static protected Logger logger = Logger.getLogger(InsertJob.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

			JobDataMap data = new JobDataMap();
			data.put("MODEL_INSTANCE_ID", "1");
			data.put("cascade", "false");
			// data.put("PERIODICITY_ID", "2");

			// CREATE JOB DETAIL
			JobDetail jobDetail = new JobDetail();
			jobDetail.setName("KpiEngineJob");
			jobDetail.setGroup("KpiEngineJob");
			jobDetail.setDescription("KpiEngineJob");
			jobDetail.setDurability(true);
			jobDetail.setVolatility(false);
			jobDetail.setRequestsRecovery(true);
			jobDetail.setJobDataMap(data);
			jobDetail.setJobClass(KpiEngineJob.class);

			// scheduler.addJob(jobDetail, true);

			java.util.Calendar cal = new java.util.GregorianCalendar(2008, Calendar.DECEMBER, 24);
			cal.set(cal.HOUR, 06);
			cal.set(cal.MINUTE, 00);
			cal.set(cal.SECOND, 0);
			cal.set(cal.MILLISECOND, 0);
			logger.debug(cal.getTime().getTime());

			java.util.Calendar cal2 = new java.util.GregorianCalendar(2009, Calendar.DECEMBER, 24);
			cal.set(cal.HOUR, 06);
			cal.set(cal.MINUTE, 00);
			cal.set(cal.SECOND, 0);
			cal.set(cal.MILLISECOND, 0);

			Calendar startCal = new GregorianCalendar(new Integer(2008).intValue(), new Integer(12).intValue() - 1, new Integer(1).intValue());

			String nameTrig = "schedule_uuid_" + UUIDGenerator.getInstance().generateTimeBasedUUID().toString();
			CronTrigger trigger = new CronTrigger();
			trigger.setName(nameTrig);
			trigger.setCronExpression("0 0/5 * * * ? *");
			trigger.setJobName("KpiEngineJob");
			trigger.setJobGroup("KpiEngineJob");
			trigger.setStartTime(startCal.getTime());
			trigger.setJobDataMap(data);
			trigger.setVolatility(false);
			trigger.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_SMART_POLICY);

			SimpleTrigger simpleTrigger = new SimpleTrigger();
			simpleTrigger.setRepeatCount(100);
			simpleTrigger.setName(nameTrig);
			// simpleTrigger.setRepeatInterval(24L * 60L * 60L * 1000L);
			simpleTrigger.setRepeatInterval(5 * 60L * 1000L);
			simpleTrigger.setStartTime(cal.getTime());
			simpleTrigger.setEndTime(cal2.getTime());
			simpleTrigger.setJobName("KpiEngineJob");
			simpleTrigger.setJobGroup("KpiEngineJob");
			simpleTrigger.setJobDataMap(data);
			simpleTrigger.setVolatility(false);

			simpleTrigger.setMisfireInstruction(SimpleTrigger.INSTRUCTION_RE_EXECUTE_JOB);

			scheduler.scheduleJob(jobDetail, simpleTrigger);

		} catch (Throwable r) {
			r.printStackTrace();
		}
	}

}
