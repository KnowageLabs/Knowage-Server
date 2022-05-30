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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import it.eng.spagobi.tools.scheduler.bo.CronExpression;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class QuartzNativeObjectsConverter {

	private static String SPAGOBI_CRON_EXPRESSION_DEPRECATED = "chronString";
	private static String SPAGOBI_CRON_EXPRESSION = "spagoBIcronExpression";
	private static final String MERGE_ALL_SNAPSHOTS = "mergeAllSnapshots";
	private static final String COLLATE_SNAPSHOTS = "collateSnapshots";

	private static Logger logger = Logger.getLogger(QuartzNativeObjectsConverter.class);

	public static final String GROUP_NAME_SEPARATOR = "/";

	private QuartzNativeObjectsConverter() {
		super();
	}

	public static org.quartz.JobDetail convertJobToNativeObject(String tenant, Job spagobiJob, boolean global) {

		String description = Optional.ofNullable(spagobiJob.getDescription()).orElse("");

		String name = spagobiJob.getName();
		String group = Optional.ofNullable(spagobiJob.getGroupName()).orElse(Scheduler.DEFAULT_GROUP);

		group = global ? group : applyTenant(tenant, group);

		JobBuilder jobBuilder = newJob().withIdentity(name, group).withDescription(description).ofType(spagobiJob.getJobClass())
				.storeDurably(spagobiJob.isDurable()).requestRecovery(spagobiJob.isRequestsRecovery())
		// TODO : Not present in Quartz 2.3 : with quartzJob.setVolatility(spagobiJob.isVolatile());
		;

		JobDataMap parameters = convertParametersToNativeObject(spagobiJob.getParameters());
		if (parameters.containsKey(MERGE_ALL_SNAPSHOTS)) {
			throw new SpagoBIRuntimeException(
					"An unexpected error occured while converting Job to native object: " + MERGE_ALL_SNAPSHOTS + " property already defined");
		}
		parameters.put(MERGE_ALL_SNAPSHOTS, spagobiJob.isMergeAllSnapshots() ? "true" : "false");
		if (parameters.containsKey(COLLATE_SNAPSHOTS)) {
			throw new SpagoBIRuntimeException(
					"An unexpected error occured while converting Job to native object: " + COLLATE_SNAPSHOTS + " property already defined");
		}
		parameters.put(COLLATE_SNAPSHOTS, spagobiJob.isCollateSnapshots() ? "true" : "false");
		jobBuilder = jobBuilder.usingJobData(parameters);

		return jobBuilder.build();
	}

	public static Job convertJobFromNativeObject(org.quartz.JobDetail quartzJob) {
		Job spagobiJob = new Job();
		JobKey key = quartzJob.getKey();

		spagobiJob.setName(key.getName());
		spagobiJob.setGroupName(key.getGroup());
		spagobiJob.setDescription(quartzJob.getDescription());
		spagobiJob.setJobClass(quartzJob.getJobClass());
		spagobiJob.setDurable(quartzJob.isDurable());
		spagobiJob.setRequestsRecovery(quartzJob.requestsRecovery());
		spagobiJob.setVolatile(/* TODO : Not present in Quartz 2.3 : quartzJob.isVolatile() */ true);

		Map<String, String> parameters = convertParametersFromNativeObject(quartzJob.getJobDataMap());
		if (parameters.containsKey(MERGE_ALL_SNAPSHOTS)) {
			spagobiJob.setMergeAllSnapshots("true".equalsIgnoreCase(parameters.get(MERGE_ALL_SNAPSHOTS)));
			parameters.remove(MERGE_ALL_SNAPSHOTS);
		}
		if (parameters.containsKey(COLLATE_SNAPSHOTS)) {
			spagobiJob.setCollateSnapshots("true".equalsIgnoreCase(parameters.get(COLLATE_SNAPSHOTS)));
			parameters.remove(COLLATE_SNAPSHOTS);
		}
		spagobiJob.addParameters(parameters);

		return spagobiJob;
	}

	public static org.quartz.Trigger convertTriggerToNativeObject(String tenant, Trigger spagobiTrigger, boolean global) {
		logger.debug("IN");

		org.quartz.Trigger quartzTrigger = null;

		try {
			Assert.assertNotNull(spagobiTrigger, "Input parameter [spagobiTrigger] csannot be null");

			TriggerBuilder triggerBuilder = TriggerBuilder.newTrigger();

			if (!org.quartz.CronExpression.isValidExpression(spagobiTrigger.getChronExpression().getExpression())) {
				try {
					JSONObject jo = new JSONObject(spagobiTrigger.getChronExpression().getExpression());
					if (jo.getString("type").equals("event")) {
						spagobiTrigger.getJob().addParameter("event_info", jo.getString("parameter"));
					}
				} catch (Exception e) {
					logger.debug("Old format of chrono string for Trigger: " + spagobiTrigger.getName() + "  (" + spagobiTrigger.getChronExpression() + ")");
				}
			}

			String triggerName = spagobiTrigger.getName();
			String triggerGroup = Optional.ofNullable(spagobiTrigger.getGroupName()).orElse(Scheduler.DEFAULT_GROUP);

			String jobName = spagobiTrigger.getJob().getName();
			String jobGroup = Optional.ofNullable(spagobiTrigger.getJob().getGroupName()).orElse(Scheduler.DEFAULT_GROUP);

			jobGroup = global ? jobGroup : applyTenant(tenant, jobGroup);

			String description = Optional.ofNullable(spagobiTrigger.getDescription()).orElse("");

			if (spagobiTrigger.isRunImmediately()) {
				spagobiTrigger.getJob().addParameter("originalTriggerName", spagobiTrigger.getOriginalTriggerName());
				JobDataMap jobDataMap = convertParametersToNativeObject(spagobiTrigger.getJob().getParameters());

				SimpleScheduleBuilder schedule = simpleSchedule();

				quartzTrigger = triggerBuilder.forJob(jobName, jobGroup).withDescription(description).usingJobData(jobDataMap).withSchedule(schedule).build();

			} else {

				triggerBuilder = triggerBuilder.withIdentity(triggerName, triggerGroup).forJob(jobName, jobGroup).withDescription(description);

				if (spagobiTrigger.isSimpleTrigger()) {
					triggerBuilder = triggerBuilder.withSchedule(simpleSchedule());
				} else {
					String quartzCronExpression = null;
					if (org.quartz.CronExpression.isValidExpression(spagobiTrigger.getChronExpression().getExpression())) {
						quartzCronExpression = spagobiTrigger.getChronExpression().getExpression();
					} else {
						quartzCronExpression = convertCronExpressionToNativeObject(spagobiTrigger.getChronExpression(), spagobiTrigger.getStartTime());
					}

					/*
					 * Very important during update!
					 *
					 * The update keep the previous start time: if we rewrite the same trigger a missfire happens; we don't want that!
					 */
					triggerBuilder = triggerBuilder.withSchedule(cronSchedule(quartzCronExpression).withMisfireHandlingInstructionDoNothing());

					// dirty trick
					spagobiTrigger.getJob().addParameter(SPAGOBI_CRON_EXPRESSION, spagobiTrigger.getChronExpression().getExpression());
				}

				if (spagobiTrigger.getStartTime() != null) {
					triggerBuilder = triggerBuilder.startAt(spagobiTrigger.getStartTime());
				}
				if (spagobiTrigger.getEndTime() != null) {
					triggerBuilder = triggerBuilder.endAt(spagobiTrigger.getEndTime());
				}

				JobDataMap jobDataMap = convertParametersToNativeObject(spagobiTrigger.getJob().getParameters());
				triggerBuilder = triggerBuilder.usingJobData(jobDataMap);

				quartzTrigger = triggerBuilder.build();
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while converting Trigger to native object", t);
		} finally {
			logger.debug("OUT");
		}

		return quartzTrigger;
	}

	public static Trigger convertTriggerFromNativeObject(org.quartz.Trigger quartzTrigger) {
		Trigger spagobiTrigger;

		spagobiTrigger = new Trigger();
		TriggerKey key = quartzTrigger.getKey();
		spagobiTrigger.setName(key.getName());
		spagobiTrigger.setGroupName(key.getGroup());
		spagobiTrigger.setDescription(quartzTrigger.getDescription());

		// spagobiTrigger.setCalendarName( quartzTrigger.getCalendarName() );
		Assert.assertTrue(quartzTrigger.getCalendarName() == null, "quartz trigger calendar name is not null: " + quartzTrigger.getCalendarName());

		spagobiTrigger.setNextFireTime(quartzTrigger.getNextFireTime());
		spagobiTrigger.setPreviousFireTime(quartzTrigger.getPreviousFireTime());
		spagobiTrigger.setStartTime(quartzTrigger.getStartTime());
		spagobiTrigger.setEndTime(quartzTrigger.getEndTime());

		// triggers that run immediately have a generated name that starts with schedule_uuid_ (see TriggerXMLDeserializer)
		// It would be better anyway to relay on a specific property to recognize if a trigger is thinked to run immediately
		spagobiTrigger.setRunImmediately(spagobiTrigger.getName().startsWith("schedule_uuid_"));

		if (quartzTrigger instanceof org.quartz.CronTrigger) {
			org.quartz.CronTrigger quartzCronTrigger = (org.quartz.CronTrigger) quartzTrigger;
			// dirty trick
			String expression = (String) quartzCronTrigger.getJobDataMap().get(SPAGOBI_CRON_EXPRESSION);
			if (expression != null) {
				quartzCronTrigger.getJobDataMap().remove(SPAGOBI_CRON_EXPRESSION);
			} else {
				// for back compatibility
				expression = (String) quartzCronTrigger.getJobDataMap().get(SPAGOBI_CRON_EXPRESSION_DEPRECATED);
				quartzCronTrigger.getJobDataMap().remove(SPAGOBI_CRON_EXPRESSION_DEPRECATED);
			}
			spagobiTrigger.setCronExpression(new CronExpression(expression));

		}

		spagobiTrigger.setChronType(spagobiTrigger.getChronExpression().getChronoType());

		Job job = new Job();
		job.setName(quartzTrigger.getJobKey().getName());
		job.setGroupName(quartzTrigger.getJobKey().getGroup());
		job.setVolatile(/* TODO : Not present in Quartz 2.3 : quartzTrigger.isVolatile() */ true);
		Map<String, String> parameters = convertParametersFromNativeObject(quartzTrigger.getJobDataMap());
		job.addParameters(parameters);

		spagobiTrigger.setJob(job);

		return spagobiTrigger;
	}

	public static org.quartz.JobDataMap convertParametersToNativeObject(Map<String, String> spagobiParameters) {
		JobDataMap quartzParameters = new JobDataMap();

		Set<String> parameterNames = spagobiParameters.keySet();
		for (String parameterName : parameterNames) {
			String parameterValue = spagobiParameters.get(parameterName);
			quartzParameters.put(parameterName, parameterValue);
		}
		return quartzParameters;
	}

	public static Map<String, String> convertParametersFromNativeObject(org.quartz.JobDataMap quartzParameters) {
		Map<String, String> spagobiParameters = new HashMap<String, String>();

		Set<String> parameterNames = quartzParameters.keySet();
		for (String parameterName : parameterNames) {
			String parameterValue = (String) quartzParameters.get(parameterName);
			if (parameterValue != null) {
				parameterValue = parameterValue.replaceAll("\"", "'");
			}
			spagobiParameters.put(parameterName, parameterValue);
		}
		return spagobiParameters;
	}

	public static String convertCronExpressionToNativeObject(CronExpression cronString, Date startTime) {
		String chronExpression = null;
		try {
			JSONObject chrono;
			try {
				chrono = new JSONObject(cronString.getExpression());
			} catch (Throwable e) {
				// this is for old method where chronostring not is a json
				return convertCronExpressionToNativeObjectOLD(cronString, startTime);
			}

			Calendar calendar = new GregorianCalendar();
			calendar.setTime(startTime);

			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH);
			int year = calendar.get(Calendar.YEAR);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);

			String type = chrono.getString("type");
			JSONObject params = chrono.optJSONObject("parameter");

			if (type.equals("event")) {
				String typeEvent = params.getString("type");
				if (typeEvent.equals("rest")) {
					chronExpression = "0 0/1 * * * ? *"; // every minute
				} else if (typeEvent.equals("dataset")) {
					int freq = params.optInt("frequency");
					int min = freq > 0 ? freq : 1;
					chronExpression = "0 0/" + min + " * * * ? *";

				} else {
					// non ancora settati gli altri casi
					return chronExpression;
				}

			} else if (type.equals("single")) {
				return chronExpression; // this will be a normal trigger
			} else if (type.equals("minute")) {
				String numrep = params.getString("numRepetition");
				chronExpression = "0 0/" + numrep + " * * * ? *";
			} else if (type.equals("hour")) {
				String numrep = params.getString("numRepetition");
				chronExpression = "0 " + minute + " 0/" + numrep + " * * ? *";
			} else if (type.equals("day")) {
				String numrep = params.getString("numRepetition");
				chronExpression = "0 " + minute + " " + hour + " 1/" + numrep + " * ? *";
			} else if (type.equals("week")) {

				JSONArray days = params.getJSONArray("days");
				String daysstr = "";
				for (int i = 0; i < days.length(); i++) {
					daysstr += days.getString(i);
					if (i != days.length() - 1) {
						daysstr += ",";
					}
				}

				chronExpression = "0 " + minute + " " + hour + " ? * " + daysstr + " *";
			} else if (type.equals("month")) {

				String monthcron = "";
				if (params.has("numRepetition")) {
					String numRep = params.optString("numRepetition");
					monthcron = "1/" + numRep;
				} else {
					JSONArray jaMonths = params.optJSONArray("months");
					String selmonths = "";
					if (jaMonths == null || jaMonths.length() == 0) {
						selmonths = "*";
					} else {
						for (int i = 0; i < jaMonths.length(); i++) {
							selmonths += jaMonths.getInt(i);
							if (i != jaMonths.length() - 1) {
								selmonths += ",";
							}
						}
					}
					monthcron = selmonths;
				}

				String daycron = "?";
				String dayinweekcron = "?";
				if (params.has("dayRepetition")) {
					String dayRep = params.optString("dayRepetition");
					daycron = dayRep;
				} else {
					String weeks = params.optString("weeks");
					JSONArray jsDays = params.optJSONArray("days");

					if (jsDays == null || jsDays.length() == 0) {
						dayinweekcron = "*";
					} else {
						String days = "";
						for (int i = 0; i < jsDays.length(); i++) {
							days += jsDays.getInt(i);
							if (i != jsDays.length() - 1) {
								days += ",";
							}
						}
						dayinweekcron = days;
					}

					if (weeks.compareTo("") != 0) {
						dayinweekcron += weeks.compareTo("L") == 0 ? weeks : "#" + weeks;
					}

				}

				chronExpression = "0 " + minute + " " + hour + " " + daycron + " " + monthcron + " " + dayinweekcron + " *";
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Error while converting spagobi chron expression [" + cronString + "] to quartz cron expression", t);
		}
		return chronExpression;
	}

	public static String convertCronExpressionToNativeObjectOLD(CronExpression cronString, Date startTime) {
		String chronExpression = null;
		try {

			Calendar calendar = new GregorianCalendar();
			calendar.setTime(startTime);

			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int month = calendar.get(Calendar.MONTH);
			int year = calendar.get(Calendar.YEAR);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);

			String type = "";
			String params = "";
			if (cronString.getExpression().indexOf("{") != -1) {
				int indFirstBra = cronString.getExpression().indexOf("{");
				type = cronString.getExpression().substring(0, indFirstBra);
				params = cronString.getExpression().substring((indFirstBra + 1), (cronString.getExpression().length() - 1));
			} else {
				return chronExpression;
			}
			if (type.equals("single")) {
				return chronExpression; // this will be a normal trigger
			}
			if (type.equals("minute")) {
				int indeq = params.indexOf("=");
				String numrep = params.substring(indeq + 1);
				chronExpression = "0 0/" + numrep + " * * * ? *";
			}
			if (type.equals("hour")) {
				int indeq = params.indexOf("=");
				String numrep = params.substring(indeq + 1);
				chronExpression = "0 " + minute + " 0/" + numrep + " * * ? *";
			}
			if (type.equals("day")) {
				int indeq = params.indexOf("=");
				String numrep = params.substring(indeq + 1);
				chronExpression = "0 " + minute + " " + hour + " 1/" + numrep + " * ? *";
			}
			if (type.equals("week")) {
				int indeq = params.indexOf("=");
				int indsplit = params.indexOf(";");
				int ind2eq = params.indexOf("=", (indeq + 1));
				String numrep = params.substring((indeq + 1), indsplit);
				Integer numrepInt = new Integer(numrep);
				String daysstr = params.substring(ind2eq + 1);
				if ((daysstr == null) || (daysstr.trim().equals("")))
					daysstr = "MON";
				if (daysstr.endsWith(","))
					daysstr = daysstr.substring(0, (daysstr.length() - 1));
				chronExpression = "0 " + minute + " " + hour + " ? * " + daysstr + "/" + numrep + " *";
			}
			if (type.equals("month")) {
				String numRep = "";
				String selmonths = "";
				String dayRep = "";
				String weeks = "";
				String days = "";
				String[] parchuncks = params.split(";");
				for (int i = 0; i < parchuncks.length; i++) {
					String parchunk = parchuncks[i];
					String[] singleparchunks = parchunk.split("=");
					String key = singleparchunks[0];
					String value = singleparchunks[1];
					value = value.trim();
					if (value.endsWith(",")) {
						value = value.substring(0, (value.length() - 1));
					}
					if (key.equals("numRepetition"))
						numRep = value;
					if (key.equals("months"))
						selmonths = value;
					if (key.equals("dayRepetition"))
						dayRep = value;
					if (key.equals("weeks"))
						weeks = value;
					if (key.equals("days"))
						days = value;
				}
				String monthcron = "";
				if (selmonths.equals("NONE")) {
					monthcron = (month + 1) + "/" + numRep;
				} else {
					if (selmonths.equals(""))
						selmonths = "*";
					monthcron = selmonths;
				}
				String daycron = "?";
				if (weeks.equals("NONE") && days.equals("NONE")) {
					if (dayRep.equals("0"))
						dayRep = "1";
					daycron = dayRep;
				}
				String dayinweekcron = "?";
				if (!days.equals("NONE")) {
					if (days.equals(""))
						days = "*";
					dayinweekcron = days;
				}

				if (!weeks.equals("NONE")) {
					if (!weeks.equals(""))
						if (weeks.equals("L"))
							dayinweekcron = dayinweekcron + weeks;
						else
							dayinweekcron = dayinweekcron + "#" + weeks;
					dayinweekcron = dayinweekcron.replaceFirst("SUN", "1");
					dayinweekcron = dayinweekcron.replaceFirst("MON", "2");
					dayinweekcron = dayinweekcron.replaceFirst("TUE", "3");
					dayinweekcron = dayinweekcron.replaceFirst("WED", "4");
					dayinweekcron = dayinweekcron.replaceFirst("THU", "5");
					dayinweekcron = dayinweekcron.replaceFirst("FRI", "6");
					dayinweekcron = dayinweekcron.replaceFirst("SAT", "7");
				}
				chronExpression = "0 " + minute + " " + hour + " " + daycron + " " + monthcron + " " + dayinweekcron + " *";
			}
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Error while converting spagobi chron expression [" + cronString + "] to quartz cron expression", t);
		}
		return chronExpression;
	}

	private static String applyTenant(String tenant, String jobGroupName) {
		return tenant + GROUP_NAME_SEPARATOR + jobGroupName;
	}

}
