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
package it.eng.spagobi.commons.deserializer;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spagobi.tools.scheduler.bo.CronExpression;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.date.DateUtils;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class TriggerXMLDeserializer implements Deserializer {

	public static final String PROPERTY_CONSUMER = "consumer";

	public static final String TRIGGER_NAME = "triggerName";
	public static final String TRIGGER_GROUP = "triggerGroup";
	public static final String TRIGGER_DESCRIPTION = "triggerDescription";

	/**
	 * @deprecated {@link #TRIGGER_ZONED_START_TIME} may be prefered
	 */
	@Deprecated
	public static final String TRIGGER_START_DATE = "startDate";
	/**
	 * @deprecated {@link #TRIGGER_ZONED_START_TIME} may be prefered
	 */
	@Deprecated
	public static final String TRIGGER_START_TIME = "startTime";
	/**
	 * @deprecated {@link #TRIGGER_ZONED_END_TIME} may be prefered
	 */
	@Deprecated
	public static final String TRIGGER_END_DATE = "endDate";
	/**
	 * @deprecated {@link #TRIGGER_ZONED_END_TIME} may be prefered
	 */
	@Deprecated
	public static final String TRIGGER_END_TIME = "endTime";
	public static final String TRIGGER_RUN_IMMEDIATELY = "runImmediately";

	public static final String JOB_NAME = "jobName";
	public static final String JOB_GROUP = "jobGroup";
	public static final String JOB_PARAMETERS = "PARAMETERS";

	public static final String CRON_STRING = "chronString";

	public static final String TRIGGER_ZONED_START_TIME = "zonedStartTime";
	public static final String TRIGGER_ZONED_END_TIME = "zonedEndTime";

	private static final Logger LOGGER = Logger.getLogger(TriggerXMLDeserializer.class);

	@Override
	public Object deserialize(Object o, Class clazz) throws DeserializationException {

		it.eng.spagobi.tools.scheduler.bo.Trigger trigger;
		Job job;

		String triggerName;
		String triggerGroupName;
		String triggerDescription;
		Date startTime;
		Date endTime;
		Date zonedStartTime = null;
		Date zonedEndTime = null;
		String jobName;
		String jobGroup;
		String cronString;
		String originalTriggerName = "";// this variable is use to pass inof

		Map<String, String> jobParameters;

		LOGGER.debug("IN");

		trigger = null;

		try {
			Assert.assertNotNull(o, "Input parameter [" + o + "] cannot be null");
			Assert.assertNotNull(o, "Input parameter [" + clazz + "] cannot be null");

			SourceBean xml = null;
			if (o instanceof SourceBean) {
				xml = (SourceBean) o;
			} else if (o instanceof String) {
				xml = SourceBean.fromXMLString((String) o);
			} else {
				throw new DeserializationException(
						"Impossible to deserialize from an object of type [" + o.getClass().getName() + "]");
			}

			boolean runImmediately = deserializeRunImmediatelyAttribute(xml);
			if (runImmediately) {
				triggerName = "schedule_uuid_" + UUID.randomUUID().toString();
				triggerGroupName = null;
				triggerDescription = null;
				startTime = null;
				endTime = null;
				cronString = (String) xml.getAttribute(CRON_STRING);
				jobName = (String) xml.getAttribute(JOB_NAME);
				jobGroup = (String) xml.getAttribute(JOB_GROUP);
				jobParameters = deserializeParametersAttribute(xml);
				originalTriggerName = (String) xml.getAttribute("originalTriggerName");
			} else {

				triggerName = (String) xml.getAttribute(TRIGGER_NAME);
				triggerGroupName = (String) xml.getAttribute(TRIGGER_GROUP);
				triggerDescription = (String) xml.getAttribute(TRIGGER_DESCRIPTION);
				startTime = deserializeStartTimeAttribute(xml);
				endTime = deserializeEndTimeAttribute(xml);
				zonedStartTime = deserializeZonedStartTimeAttribute(xml);
				try {
					zonedEndTime = deserializeZonedEndTimeAttribute(xml);
				} catch (NullPointerException e) {
					// End time is nullable
				}
				cronString = (String) xml.getAttribute(CRON_STRING);

				jobName = (String) xml.getAttribute(JOB_NAME);
				jobGroup = (String) xml.getAttribute(JOB_GROUP);
				jobParameters = deserializeParametersAttribute(xml);
			}

			trigger = new it.eng.spagobi.tools.scheduler.bo.Trigger();

			trigger.setName(triggerName);
			trigger.setDescription(triggerDescription);
			trigger.setGroupName(triggerGroupName);

			trigger.setRunImmediately(runImmediately);

			trigger.setStartTime(zonedStartTime != null ? zonedStartTime : startTime);
			trigger.setEndTime(zonedEndTime != null ? zonedEndTime : endTime);
			trigger.setCronExpression(new CronExpression(cronString));

			job = new Job();
			job.setName(jobName);
			job.setGroupName(jobGroup);
			job.addParameters(jobParameters);
			job.setVolatile(false);

			if (originalTriggerName != null && originalTriggerName.trim().compareTo("") != 0) {
				trigger.setOriginalTriggerName(originalTriggerName);
			}

			trigger.setJob(job);

		} catch (Throwable t) {
			throw new DeserializationException("An error occurred while deserializing object: " + o, t);
		} finally {
			LOGGER.debug("OUT");
		}

		return trigger;
	}

	private boolean deserializeRunImmediatelyAttribute(SourceBean xml) {
		boolean runImmediately;

		runImmediately = false;

		String runImmediatelyStr = (String) xml.getAttribute(TRIGGER_RUN_IMMEDIATELY);
		if ((runImmediatelyStr != null) && (runImmediatelyStr.trim().equalsIgnoreCase("true"))) {
			runImmediately = true;
		}

		return runImmediately;
	}

	// NOTE: start date and and date are encoded with different formats. That's sick!

	/**
	 * get the start date param (format dd-mm-yyyy) and end time (format hh:mm:ss)
	 *
	 * @deprecated Zoned values may be prefered
	 */
	@Deprecated
	private Date deserializeStartTimeAttribute(SourceBean xml) {
		Calendar calendar = null;

		String startDateStr = (String) xml.getAttribute(TRIGGER_START_DATE);
		if (startDateStr != null) {
			String[] splitterDate;
			String startDay;
			String startMonth;
			String startYear;
			if (DateUtils.isValidFormat(startDateStr, "dd/MM/yyyy")
					|| DateUtils.isValidFormat(startDateStr, "dd/MM/yy")) {
				splitterDate = startDateStr.split("/");
				startDay = splitterDate[0];
				startMonth = splitterDate[1];
				startYear = splitterDate[2];
			} else if (DateUtils.isValidFormat(startDateStr, "dd-MM-yyyy")) {
				splitterDate = startDateStr.split("-");
				startDay = splitterDate[0];
				startMonth = splitterDate[1];
				startYear = splitterDate[2];
			} else {
				return null;
			}

			calendar = new GregorianCalendar(Integer.parseInt(startYear), Integer.parseInt(startMonth) - 1,
					Integer.parseInt(startDay));
			String startTimeStr = (String) xml.getAttribute(TRIGGER_START_TIME);
			if (startTimeStr != null) {
				String startHour = startTimeStr.substring(0, 2);
				String startMinute = startTimeStr.substring(3, 5);
				calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startHour));
				calendar.set(Calendar.MINUTE, Integer.parseInt(startMinute));
			}
		}

		return calendar != null ? calendar.getTime() : null;
	}

	/**
	 * get the end date param (format yyyy-mm-gg) and end time (format hh:mm:ss)
	 *
	 * @deprecated Zoned values may be prefered
	 */
	@Deprecated
	private Date deserializeEndTimeAttribute(SourceBean xml) {
		Calendar calendar = null;
		String endDateStr = (String) xml.getAttribute(TRIGGER_END_DATE);
		if (endDateStr != null) {
			String[] splitterDate;
			String endDay;
			String endMonth;
			String endYear;
			if (DateUtils.isValidFormat(endDateStr, "dd/MM/yyyy") || DateUtils.isValidFormat(endDateStr, "dd/MM/yy")) {
				splitterDate = endDateStr.split("/");
				endDay = splitterDate[0];
				endMonth = splitterDate[1];
				endYear = splitterDate[2];
			} else if (DateUtils.isValidFormat(endDateStr, "yyyy-MM-dd'T'HH:mm:ss")) {
				endDateStr = endDateStr.split("T")[0];
				splitterDate = endDateStr.split("-");
				endYear = splitterDate[0];
				endMonth = splitterDate[1];
				endDay = splitterDate[2];
			} else {
				return null;
			}

			calendar = new GregorianCalendar(Integer.parseInt(endYear), Integer.parseInt(endMonth) - 1,
					Integer.parseInt(endDay));

			String endTimeStr = (String) xml.getAttribute(TRIGGER_END_TIME);
			if (endTimeStr != null) {
				String endHour = endTimeStr.substring(0, 2);
				String endMinute = endTimeStr.substring(3, 5);
				calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endHour));
				calendar.set(Calendar.MINUTE, Integer.parseInt(endMinute));
			}
		}

		return calendar != null ? calendar.getTime() : null;
	}

	/**
	 * Parse ISO 8601 start time string to {@link Date}.
	 */
	private Date deserializeZonedStartTimeAttribute(SourceBean xml) {
		String zonedStartDate = (String) xml.getAttribute(TRIGGER_ZONED_START_TIME);
		DateTimeFormatter dateTime = ISODateTimeFormat.dateTime();
		return dateTime.parseDateTime(zonedStartDate).toDate();
	}

	/**
	 * Parse ISO 8601 end time string to {@link Date}.
	 */
	private Date deserializeZonedEndTimeAttribute(SourceBean xml) {
		String zonedEndDate = (String) xml.getAttribute(TRIGGER_ZONED_END_TIME);
		DateTimeFormatter dateTime = ISODateTimeFormat.dateTime();
		return dateTime.parseDateTime(zonedEndDate).toDate();
	}

	private Map<String, String> deserializeParametersAttribute(SourceBean xml) {
		Map<String, String> parameters = new HashMap<>();

		SourceBean jobParameters = (SourceBean) xml.getAttribute(JOB_PARAMETERS);

		parameters.put("empty", "empty");
		if (jobParameters != null) {
			List paramsSB = jobParameters.getContainedAttributes();
			Iterator iterParSb = paramsSB.iterator();
			while (iterParSb.hasNext()) {
				SourceBeanAttribute paramSBA = (SourceBeanAttribute) iterParSb.next();
				String nameAttr = paramSBA.getKey();
				if (nameAttr.equalsIgnoreCase("PARAMETER")) {
					SourceBean paramSB = (SourceBean) paramSBA.getValue();
					String name = (String) paramSB.getAttribute("name");
					String value = (String) paramSB.getAttribute("value");
					parameters.put(name, value);
				}
			}
		}
		return parameters;
	}

}
