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

/**
 * Authors: Nikola Simović (nikola.simovic@mht.net)
 */

package it.eng.spagobi.tools.dataset.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.spago.base.SourceBean;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.services.scheduler.service.ISchedulerServiceSupplier;
import it.eng.spagobi.services.scheduler.service.SchedulerServiceSupplierFactory;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.scheduler.jobs.ExecutePersistDatasetJob;
import it.eng.spagobi.tools.scheduler.to.JobInfo;
import it.eng.spagobi.tools.scheduler.to.TriggerInfo;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class DatasetPersistenceUtilsForRest {

	// logger component
	public static Logger logger = Logger.getLogger(DatasetPersistenceUtils.class);
	public static final String JOB_GROUP = "PersistDatasetExecutions";
	protected IEngUserProfile profile;
	protected HttpServletRequest request;
	protected String serviceName;
	protected IDataSet dataSet;

	public DatasetPersistenceUtilsForRest(IEngUserProfile profile, HttpServletRequest request, String serviceName, IDataSet dataSet) {
		super();
		this.profile = profile;
		this.request = request;
		this.serviceName = serviceName;
		this.dataSet = dataSet;

	}

	public String saveDatasetJob(IDataSet ds, HashMap<String, String> logParam) {

		ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
		JobInfo jobInfo = new JobInfo();

		jobInfo.setJobName(ds.getLabel());
		jobInfo.setJobDescription(JOB_GROUP);
		String jobGroupName = JOB_GROUP;

		StringBuffer message = new StringBuffer();
		message.append("<SERVICE_REQUEST ");
		message.append(" jobName=\"" + jobInfo.getJobName() + "\" ");
		message.append(" jobDescription=\"" + jobInfo.getJobDescription() + "\" ");
		message.append(" jobGroupName=\"" + jobGroupName + "\" ");
		message.append(" jobRequestRecovery=\"false\" ");
		message.append(" jobClass=\"" + ExecutePersistDatasetJob.class.getName() + "\" ");
		message.append(">");
		message.append("   <PARAMETERS>");
		message.append("   </PARAMETERS>");
		message.append("</SERVICE_REQUEST>");
		String servoutStr = schedulerService.defineJob(message.toString());
		SourceBean schedModRespSB = SchedulerUtilities.getSBFromWebServiceResponse(servoutStr);
		if (schedModRespSB == null) {
			try {
				if (request != null) {
					AuditLogUtilities.updateAudit(request, profile, "SCHEDULER.SAVE", logParam, "KO");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			throw new SpagoBIServiceException(serviceName,
					"Incomplete response returned by the Web service " + "during job " + jobInfo.getJobName() + " creation");
		}
		if (!SchedulerUtilities.checkResultOfWSCall(schedModRespSB)) {
			throw new SpagoBIServiceException(serviceName, "Job " + jobInfo.getJobName() + " not created by the web service");
		}
		try {
			if (request != null) {
				AuditLogUtilities.updateAudit(request, profile, "SCHEDULER.SAVE", logParam, "OK");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jobInfo.getJobName();
	}

	public void saveTriggerForDatasetJob(String jobName, JSONObject json) {

		HashMap<String, String> logParam = new HashMap();
		String quartzMsg = "";
		try {
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String jobDetail = schedulerService.getJobDefinition(jobName, JOB_GROUP);
			SourceBean jobDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(jobDetail);

			if (jobDetailSB == null) {
				throw new SpagoBIServiceException(serviceName, "Cannot recover job " + jobName);
			}

			JobInfo jobInfo = SchedulerUtilities.getJobInfoFromJobSourceBean(jobDetailSB);
			TriggerInfo triggerInfo = new TriggerInfo();
			triggerInfo.setJobInfo(jobInfo);
			setTriggerInfoFromRequest(triggerInfo, json);

			logParam.put("TRIGGER NAME", triggerInfo.getTriggerName());
			logParam.put("JOB GROUP", triggerInfo.getJobInfo().getJobGroupName());
			logParam.put("JOB NAME", triggerInfo.getJobInfo().getJobName());

			StringBuffer message = createMessageSaveSchedulation(triggerInfo, false, profile);
			String servoutStr = schedulerService.scheduleJob(message.toString());
			SourceBean execOutSB = SchedulerUtilities.getSBFromWebServiceResponse(servoutStr);
			if (execOutSB != null) {
				String outcome = (String) execOutSB.getAttribute("outcome");
				if (outcome.equalsIgnoreCase("fault")) {
					quartzMsg = (String) execOutSB.getAttribute("msg");
					try {
						AuditLogUtilities.updateAudit(request, profile, "SCHED_TRIGGER.SAVE", logParam, "KO");
					} catch (Exception e) {
						e.printStackTrace();
					}
					throw new SpagoBIServiceException(serviceName, "Trigger " + triggerInfo.getTriggerName() + " not created by the web service");
				}
			}
			try {
				if (request != null)
					AuditLogUtilities.updateAudit(request, profile, "SCHED_TRIGGER.SAVE", logParam, "OK");
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception ex) {
			try {
				if (request != null)
					AuditLogUtilities.updateAudit(request, profile, "SCHED_TRIGGER.SAVE", logParam, "KO");
			} catch (Exception e) {
				e.printStackTrace();
			}
			String errorMsgToUser = "Error while saving schedule for job";
			if (quartzMsg != null && !quartzMsg.isEmpty()) {
				errorMsgToUser += ": " + quartzMsg;
			}

			logger.error(errorMsgToUser, ex);
			throw new SpagoBIServiceException(serviceName, errorMsgToUser, ex);
		}
	}

	private void setTriggerInfoFromRequest(TriggerInfo triggerInfo, JSONObject json) throws JSONException {
		triggerInfo.setTriggerName("persist_" + triggerInfo.getJobInfo().getJobName());
		triggerInfo.setTriggerDescription("It is used to schedule data update for " + triggerInfo.getJobInfo().getJobName() + " dataset");

		String startTimeStr = json.getString("startDate");
		ZonedDateTime startTime = ZonedDateTime.parse(startTimeStr);
		triggerInfo.setZonedStartTime(startTime);

		String endTimeStr = json.getString("endDate");
		ZonedDateTime endTime = ZonedDateTime.parse(endTimeStr);
		triggerInfo.setZonedEndTime(endTime);

		String chronstr = json.getString("schedulingCronLine");
		triggerInfo.setChronString(chronstr);

	}

	private StringBuffer createMessageSaveSchedulation(TriggerInfo triggerInfo, boolean runImmediately, IEngUserProfile profile) {

		StringBuffer message = new StringBuffer();
		JobInfo jobInfo = triggerInfo.getJobInfo();

		message.append("<SERVICE_REQUEST ");

		message.append(" jobName=\"" + jobInfo.getJobName() + "\" ");

		message.append(" jobGroup=\"" + jobInfo.getJobGroupName() + "\" ");
		if (runImmediately) {
			message.append(" runImmediately=\"true\" ");
		} else {
			message.append(" triggerName=\"" + triggerInfo.getTriggerName() + "\" ");

			message.append(" triggerDescription=\"" + triggerInfo.getTriggerDescription() + "\" ");

			ZonedDateTime zonedStartTime = triggerInfo.getZonedStartTime();
			ZonedDateTime zonedEndTime = triggerInfo.getZonedEndTime();

			DateTimeFormatter iso8601WithMillis = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

			if (zonedStartTime != null) {
				String format = iso8601WithMillis.format(zonedStartTime);
				message.append(" zonedStartTime=\"" + format + "\"");
			} else {
				String startdate = triggerInfo.getStartDate();

				if (!startdate.trim().equals("")) {
					message.append(" startDate=\"" + triggerInfo.getStartDate() + "\" ");
				}
			}

			if (zonedEndTime != null) {
				String format = iso8601WithMillis.format(zonedEndTime);
				message.append(" zonedEndTime=\"" + format + "\"");
			} else {
				String enddate = triggerInfo.getEndDate();

				if (!enddate.trim().equals("")) {
					message.append(" endDate=\"" + enddate + "\" ");
				}
			}

			message.append(" chronString=\"" + triggerInfo.getChronString() + "\" ");
		}
		message.append(">");
		message.append("</SERVICE_REQUEST>");

		return message;
	}

}
