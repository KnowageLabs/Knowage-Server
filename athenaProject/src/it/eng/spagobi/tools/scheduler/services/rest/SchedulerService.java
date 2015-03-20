/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/
package it.eng.spagobi.tools.scheduler.services.rest;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.JSONSerializer;
import it.eng.spagobi.commons.serializer.JobJSONSerializer;
import it.eng.spagobi.commons.serializer.SerializationException;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.services.scheduler.service.ISchedulerServiceSupplier;
import it.eng.spagobi.services.scheduler.service.SchedulerServiceSupplierFactory;
import it.eng.spagobi.tools.distributionlist.bo.DistributionList;
import it.eng.spagobi.tools.distributionlist.dao.IDistributionListDAO;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.bo.TriggerPaused;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.tools.scheduler.to.JobInfo;
import it.eng.spagobi.tools.scheduler.to.TriggerInfo;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * 
 */

@Path("/scheduler")
public class SchedulerService {
	static private Logger logger = Logger.getLogger(SchedulerService.class);
	static private String canNotFillResponseError = "error.mesage.description.generic.can.not.responce";

	@GET
	@Path("/listAllJobs")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getAllJobs() {
		JSONObject JSONReturn = new JSONObject();
		JSONArray jobsJSONArray = new JSONArray();

		ISchedulerDAO schedulerDAO;
		try {
			schedulerDAO = DAOFactory.getSchedulerDAO();
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("Impossible to load scheduler DAO", t);
		}
		List<Job> jobs = schedulerDAO.loadJobs();

		JSONSerializer jsonSerializer = (JSONSerializer) SerializerFactory.getSerializer("application/json");
		try {
			jobsJSONArray = (JSONArray) jsonSerializer.serialize(jobs, null);

			// add the triggers part for each job

			for (int i = 0; i < jobsJSONArray.length(); i++) {

				JSONObject jobJSONObject = jobsJSONArray.getJSONObject(i);
				String jobName = jobJSONObject.getString(JobJSONSerializer.JOB_NAME);
				String jobGroup = jobJSONObject.getString(JobJSONSerializer.JOB_GROUP);

				for (Job job : jobs) {
					if ((job.getName().equals(jobName)) && (job.getGroupName().equals(jobGroup))) {
						String triggersSerialized = getJobTriggers(job);
						JSONObject triggersJSONObject = new JSONObject(triggersSerialized);
						JSONArray triggersJSONArray = triggersJSONObject.getJSONArray("triggers");
						// put the triggersJSONArray inside the correct jobJSONObject
						jobJSONObject.put("triggers", triggersJSONArray);
					}
				}

			}

		} catch (SerializationException e) {
			throw new SpagoBIRuntimeException("Error serializing Jobs objects in SchedulerService", e);

		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("Error serializing Jobs objects in SchedulerService", e);

		}

		try {
			JSONReturn.put("root", jobsJSONArray);
		} catch (JSONException e) {
			throw new SpagoBIRuntimeException("JSONException in SchedulerService", e);

		}

		return JSONReturn.toString();
	}

	// Triggers are the Schedulation instances
	public String getJobTriggers(Job job) {
		JSONObject JSONReturn = new JSONObject();
		JSONArray triggersJSONArray = new JSONArray();

		String jobGroupName = job.getGroupName();
		String jobName = job.getName();

		try {
			Assert.assertNotNull(jobName, "Input parameter [" + jobName + "] cannot be null");
			Assert.assertNotNull(jobName, "Input parameter [" + jobGroupName + "] cannot be null");
			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();

			List<Trigger> triggers = schedulerDAO.loadTriggers(jobGroupName, jobName);
			// filter out trigger whose property runImmediately is equal to true
			List<Trigger> triggersToSerialize = new ArrayList<Trigger>();
			for (Trigger trigger : triggers) {
				// if(trigger.getName().startsWith("schedule_uuid_") == false) {
				if (!trigger.isRunImmediately()) {
					triggersToSerialize.add(trigger);
				}
			}
			logger.trace("Succesfully loaded [" + triggersToSerialize.size() + "] trigger(s)");

			JSONSerializer jsonSerializer = (JSONSerializer) SerializerFactory.getSerializer("application/json");
			try {
				triggersJSONArray = (JSONArray) jsonSerializer.serialize(triggersToSerialize, null);
			} catch (SerializationException e) {
				throw new SpagoBIRuntimeException("Error serializing Jobs objects in SchedulerService", e);

			}

			try {
				JSONReturn.put("triggers", triggersJSONArray);
			} catch (JSONException e) {
				throw new SpagoBIRuntimeException("JSONException in SchedulerService", e);

			}

			logger.debug("Trigger list succesfully serialized");
		} catch (Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occured while loading trigger list", t);
		} finally {
			logger.debug("OUT");
		}

		return JSONReturn.toString();
	}

	@POST
	@Path("/deleteJob")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String deleteJob(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);

		String jobGroupName = req.getParameter("jobGroup");
		String jobName = req.getParameter("jobName");
		HashMap<String, String> logParam = new HashMap();
		try {

			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();

			String xmlSchedList = schedulerService.getJobSchedulationList(jobName, jobGroupName);
			SourceBean rowsSB_JSL = SourceBean.fromXMLString(xmlSchedList);
			if (rowsSB_JSL == null) {
				throw new Exception("List of job triggers not returned by Web service ");
			}
			logParam.put("JOB NAME", jobName);
			logParam.put("JOB GROUP NAME", jobGroupName);

			// delete each schedulation
			List schedules = rowsSB_JSL.getAttributeAsList("ROW");
			Iterator iterSchedules = schedules.iterator();
			while (iterSchedules.hasNext()) {
				SourceBean scheduleSB = (SourceBean) iterSchedules.next();
				String triggerName = (String) scheduleSB.getAttribute("triggerName");
				String triggerGroup = (String) scheduleSB.getAttribute("triggerGroup");
				DAOFactory.getDistributionListDAO().eraseAllRelatedDistributionListObjects(triggerName);
				String delResp = schedulerService.deleteSchedulation(triggerName, triggerGroup);
				SourceBean schedModRespSB_DS = SchedulerUtilities.getSBFromWebServiceResponse(delResp);
				if (schedModRespSB_DS == null) {
					try {
						updateAudit(req, profile, "SCHEDULER.DELETE", null, "KO");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new Exception("Imcomplete response returned by the Web service " + "during schedule " + triggerName + " deletion");
				}
				if (!SchedulerUtilities.checkResultOfWSCall(schedModRespSB_DS)) {
					try {
						updateAudit(req, profile, "SCHEDULER.DELETE", logParam, "KO");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new Exception("Schedule " + triggerName + " not deleted by the Web Service");
				}
			}
			// delete job
			String resp_DJ = schedulerService.deleteJob(jobName, jobGroupName);
			SourceBean schedModRespSB_DJ = SchedulerUtilities.getSBFromWebServiceResponse(resp_DJ);
			if (schedModRespSB_DJ == null) {
				try {
					updateAudit(req, profile, "SCHEDULER.DELETE", logParam, "KO");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				throw new Exception("Imcomplete response returned by the Web service " + "during job " + jobName + " deletion");
			}
			if (!SchedulerUtilities.checkResultOfWSCall(schedModRespSB_DJ)) {
				try {
					updateAudit(req, profile, "SCHEDULER.DELETE", logParam, "KO");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				throw new Exception("JOb " + jobName + " not deleted by the Web Service");
			}
			// fill response
			updateAudit(req, profile, "SCHEDULER.DELETE", logParam, "OK");

			return ("{resp:'ok'}");
		} catch (Exception ex) {
			updateAudit(req, profile, "SCHEDULER.DELETE", logParam, "KO");
			logger.error("Error while deleting job", ex);
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception e) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", e);
			}
		}

	}

	@POST
	@Path("/deleteTrigger")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String deleteTrigger(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap();

		String jobGroupName = req.getParameter("jobGroup");
		String jobName = req.getParameter("jobName");
		String triggerGroup = req.getParameter("triggerGroup");
		String triggerName = req.getParameter("triggerName");
		logParam.put("JOB NAME", jobName);
		logParam.put("JOB GROUP", jobGroupName);
		logParam.put("TRIGGER NAME", triggerName);
		logParam.put("TRIGGER GROUP", triggerGroup);

		try {
			DAOFactory.getDistributionListDAO().eraseAllRelatedDistributionListObjects(triggerName);
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String resp = schedulerService.deleteSchedulation(triggerName, triggerGroup);
			SourceBean schedModRespSB = SchedulerUtilities.getSBFromWebServiceResponse(resp);
			if (schedModRespSB != null) {
				String outcome = (String) schedModRespSB.getAttribute("outcome");
				if (outcome.equalsIgnoreCase("fault")) {
					try {
						updateAudit(req, profile, "SCHED_TRIGGER.DELETE", logParam, "KO");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new Exception("Trigger not deleted by the service");
				}
			}
			updateAudit(req, profile, "SCHED_TRIGGER.DELETE", logParam, "OK");

			return ("{resp:'ok'}");

		} catch (Exception e) {
			updateAudit(req, profile, "SCHEDULER.DELETE", logParam, "KO");
			logger.error("Error while deleting schedule (trigger) ", e);
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception ex) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", ex);
			}
		}

	}

	@POST
	@Path("/executeTrigger")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String executeTrigger(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap();
		try {
			String jobGroupName = req.getParameter("jobGroup");
			String jobName = req.getParameter("jobName");
			String triggerGroup = req.getParameter("triggerGroup");
			String triggerName = req.getParameter("triggerName");
			logParam.put("JOB NAME", jobName);
			logParam.put("JOB GROUP", jobGroupName);
			logParam.put("TRIGGER NAME", triggerName);
			logParam.put("TRIGGER GROUP", triggerGroup);
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();

			TriggerInfo triggerInfo = this.getTriggerInfo(jobName, jobGroupName, triggerName, triggerGroup);

			StringBuffer message = createMessageSaveSchedulation(triggerInfo, true, profile);

			// call the web service to create the schedule
			String resp = schedulerService.scheduleJob(message.toString());
			SourceBean schedModRespSB = SchedulerUtilities.getSBFromWebServiceResponse(resp);
			if (schedModRespSB != null) {
				String outcome = (String) schedModRespSB.getAttribute("outcome");
				if (outcome.equalsIgnoreCase("fault")) {
					try {
						updateAudit(req, profile, "SCHED_TRIGGER.RUN", logParam, "KO");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					throw new Exception("Immediate Trigger not created by the web service");
				}
			}
			updateAudit(req, profile, "SCHED_TRIGGER.RUN", logParam, "OK");
			return ("{resp:'ok'}");

		} catch (Exception e) {
			updateAudit(req, profile, "SCHED_TRIGGER.RUN", logParam, "KO");
			logger.error("Error while create immediate trigger ", e);
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception ex) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", ex);
			}
		}

	}

	@POST
	@Path("/pauseTrigger")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String pauseTrigger(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap();
		try {
			String jobGroupName = req.getParameter("jobGroup");
			String jobName = req.getParameter("jobName");
			String triggerGroup = req.getParameter("triggerGroup");
			String triggerName = req.getParameter("triggerName");
			logParam.put("JOB NAME", jobName);
			logParam.put("JOB GROUP", jobGroupName);
			logParam.put("TRIGGER NAME", triggerName);
			logParam.put("TRIGGER GROUP", triggerGroup);

			// Insert trigger inside paused triggers table
			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();

			TriggerPaused triggerPaused = new TriggerPaused();
			triggerPaused.setJobGroup(jobGroupName);
			triggerPaused.setJobName(jobName);
			triggerPaused.setTriggerGroup(triggerGroup);
			triggerPaused.setTriggerName(triggerName);

			schedulerDAO.pauseTrigger(triggerPaused);

			updateAudit(req, profile, "SCHED_TRIGGER.PAUSE", logParam, "OK");
			return ("{resp:'ok'}");
		} catch (Exception e) {
			updateAudit(req, profile, "SCHED_TRIGGER.PAUSE", logParam, "KO");
			logger.error("Error while pausing trigger ", e);
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception ex) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", ex);
			}
		}
	}

	@POST
	@Path("/resumeTrigger")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String resumeTrigger(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap();
		try {
			String jobGroup = req.getParameter("jobGroup");
			String jobName = req.getParameter("jobName");
			String triggerGroup = req.getParameter("triggerGroup");
			String triggerName = req.getParameter("triggerName");
			logParam.put("JOB NAME", jobName);
			logParam.put("JOB GROUP", jobGroup);
			logParam.put("TRIGGER NAME", triggerName);
			logParam.put("TRIGGER GROUP", triggerGroup);

			// Remove trigger from paused triggers table
			ISchedulerDAO schedulerDAO = DAOFactory.getSchedulerDAO();

			schedulerDAO.resumeTrigger(triggerGroup, triggerName, jobGroup, jobName);

			updateAudit(req, profile, "SCHED_TRIGGER.RESUME", logParam, "OK");
			return ("{resp:'ok'}");
		} catch (Exception e) {
			updateAudit(req, profile, "SCHED_TRIGGER.RESUME", logParam, "KO");
			logger.error("Error while resuming trigger ", e);
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception ex) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", ex);
			}
		}
	}

	@POST
	@Path("/getTriggerSaveOptions")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getTriggerSaveOptions(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap();
		try {
			String jobGroupName = req.getParameter("jobGroup");
			String jobName = req.getParameter("jobName");
			String triggerGroup = req.getParameter("triggerGroup");
			String triggerName = req.getParameter("triggerName");
			logParam.put("JOB NAME", jobName);
			logParam.put("JOB GROUP", jobGroupName);
			logParam.put("TRIGGER NAME", triggerName);
			logParam.put("TRIGGER GROUP", triggerGroup);

			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();

			TriggerInfo triggerInfo = this.getTriggerInfo(jobName, jobGroupName, triggerName, triggerGroup);

			JSONArray serializedSaveOptions = serializeSaveOptions(triggerInfo);
			JSONObject triggerDocumentsSaveOptions = new JSONObject();
			triggerDocumentsSaveOptions.put("documents", serializedSaveOptions);
			updateAudit(req, profile, "SCHED_TRIGGER.GET_TRIGGER_SAVE_OPTION", logParam, "OK");

			return triggerDocumentsSaveOptions.toString();

		} catch (Exception e) {
			updateAudit(req, profile, "SCHED_TRIGGER.GET_TRIGGER_SAVE_OPTION", logParam, "KO");
			logger.error("Error while create immediate trigger ", e);
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
			} catch (Exception ex) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", ex);
			}
		}
	}

	private JSONArray serializeSaveOptions(TriggerInfo triggerInfo) {
		JSONArray saveOptionsJSONArray = new JSONArray();

		try {
			IBIObjectDAO biObjectDAO = DAOFactory.getBIObjectDAO();

			Map<String, DispatchContext> saveOptions = triggerInfo.getSaveOptions();
			if (!saveOptions.isEmpty()) {
				// iterate Map
				for (Map.Entry<String, DispatchContext> entry : saveOptions.entrySet()) {
					String objIdentifier = entry.getKey();
					if (objIdentifier.contains("__")) {
						JSONObject documentJSONObject = new JSONObject();

						String objId = objIdentifier.substring(0, objIdentifier.indexOf("__"));
						BIObject biObject = biObjectDAO.loadBIObjectById(Integer.valueOf(objId));
						if (biObject != null) {
							String documentLabel = biObject.getLabel();
							documentJSONObject.put("documentLabel", documentLabel);

							DispatchContext dispatchContext = entry.getValue();
							if (dispatchContext != null) {
								String mailTos = dispatchContext.getMailTos();
								documentJSONObject.put("mailTos", mailTos);
								String zipMailName = dispatchContext.getZipMailName();
								if (zipMailName == null) {
									zipMailName = "";
								}
								documentJSONObject.put("zipMailName", zipMailName);
								String datasetLabel = dispatchContext.getDataSetLabel();

								if (datasetLabel == null) {
									datasetLabel = "";
								}
								documentJSONObject.put("datasetLabel", datasetLabel);
								String mailSubject = dispatchContext.getMailSubj();
								documentJSONObject.put("mailSubject", mailSubject);
								String mailTxt = dispatchContext.getMailTxt();
								documentJSONObject.put("mailTxt", mailTxt);
								String containedFileName = dispatchContext.getContainedFileName();
								if (containedFileName == null) {
									containedFileName = "";
								}
								documentJSONObject.put("containedFileName", containedFileName);

							}

							// put JSONObject in JSONArray
							saveOptionsJSONArray.put(documentJSONObject);
						}
					}
				}
			}

		} catch (Exception ex) {
			logger.debug("Error serializing Trigger Save Option in JSON");
			throw new SpagoBIRuntimeException("Cannot fill response container", ex);
		}

		return saveOptionsJSONArray;
	}

	private TriggerInfo getTriggerInfo(String jobName, String jobGroupName, String triggerName, String triggerGroup) {
		try {
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			String respStr_gt = schedulerService.getJobSchedulationDefinition(triggerName, triggerGroup);
			SourceBean triggerDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(respStr_gt);
			String respStr_gj = schedulerService.getJobDefinition(jobName, jobGroupName);
			SourceBean jobDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(respStr_gj);
			if (triggerDetailSB != null) {
				if (jobDetailSB != null) {
					TriggerInfo tInfo = SchedulerUtilities.getTriggerInfoFromTriggerSourceBean(triggerDetailSB, jobDetailSB);
					return tInfo;
				} else {
					throw new Exception("Detail not recovered for job " + jobName + "associated to trigger " + triggerName);
				}
			} else {
				throw new Exception("Detail not recovered for trigger " + triggerName);
			}

		} catch (Exception ex) {
			logger.error("Error while getting detail of the schedule(trigger)", ex);
			throw new SpagoBIRuntimeException("Error while getting detail of the schedule(trigger)", ex);
		}
	}

	private static void updateAudit(HttpServletRequest request, IEngUserProfile profile, String action_code, HashMap<String, String> parameters, String esito) {
		try {
			AuditLogUtilities.updateAudit(request, profile, action_code, parameters, esito);
		} catch (Exception e) {
			logger.debug("Error writing audit", e);
		}
	}

	// ==========================================================================================================
	// SERIALIZER (from TriggerManagementModule)
	// ==========================================================================================================
	private StringBuffer createMessageSaveSchedulation(TriggerInfo triggerInfo, boolean runImmediately, IEngUserProfile profile) throws EMFUserError {

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
			message.append(" startDate=\"" + triggerInfo.getStartDate() + "\" ");

			message.append(" startTime=\"" + triggerInfo.getStartTime() + "\" ");

			message.append(" chronString=\"" + triggerInfo.getChronString() + "\" ");

			String enddate = triggerInfo.getEndDate();
			String endtime = triggerInfo.getEndTime();
			if (!enddate.trim().equals("")) {
				message.append(" endDate=\"" + enddate + "\" ");

				if (!endtime.trim().equals("")) {
					message.append(" endTime=\"" + endtime + "\" ");

				}
			}
		}
		String repeatinterval = triggerInfo.getRepeatInterval();
		if (!repeatinterval.trim().equals("")) {
			message.append(" repeatInterval=\"" + repeatinterval + "\" ");

		}
		message.append(">");

		serializeSaveParameterOptions(message, triggerInfo, runImmediately, profile);

		message.append("</SERVICE_REQUEST>");

		return message;
	}

	private void serializeSaveParameterOptions(StringBuffer message, TriggerInfo triggerInfo, boolean runImmediately, IEngUserProfile profile)
			throws EMFUserError {

		message.append("   <PARAMETERS>");

		Map<String, DispatchContext> saveOptions = triggerInfo.getSaveOptions();
		Set<String> uniqueDispatchContextNames = saveOptions.keySet();

		for (String uniqueDispatchContextName : uniqueDispatchContextNames) {
			DispatchContext dispatchContext = saveOptions.get(uniqueDispatchContextName);

			String saveOptString = "";

			saveOptString += serializeSaveAsSnapshotOptions(dispatchContext);
			saveOptString += serializeSaveAsFileOptions(dispatchContext);
			saveOptString += serializeSaveAsJavaClassOptions(dispatchContext);
			saveOptString += serializeSaveAsDocumentOptions(dispatchContext);
			saveOptString += serializeSaveAsMailOptions(dispatchContext);
			saveOptString += serializeSaveAsDistributionListOptions(dispatchContext, uniqueDispatchContextName, triggerInfo, runImmediately, profile);

			message.append("   	   <PARAMETER name=\"biobject_id_" + uniqueDispatchContextName + "\" value=\"" + saveOptString + "\" />");
		}

		message.append("   </PARAMETERS>");
	}

	private String serializeSaveAsSnapshotOptions(DispatchContext dispatchContext) {
		String saveOptString = "";

		if (dispatchContext.isSnapshootDispatchChannelEnabled()) {
			saveOptString += "saveassnapshot=true%26";
			if ((dispatchContext.getSnapshotName() != null) && !dispatchContext.getSnapshotName().trim().equals("")) {
				saveOptString += "snapshotname=" + dispatchContext.getSnapshotName() + "%26";
			}
			if ((dispatchContext.getSnapshotDescription() != null) && !dispatchContext.getSnapshotDescription().trim().equals("")) {
				saveOptString += "snapshotdescription=" + dispatchContext.getSnapshotDescription() + "%26";
			}
			if ((dispatchContext.getSnapshotHistoryLength() != null) && !dispatchContext.getSnapshotHistoryLength().trim().equals("")) {
				saveOptString += "snapshothistorylength=" + dispatchContext.getSnapshotHistoryLength() + "%26";
			}
		}

		return saveOptString;
	}

	private String serializeSaveAsJavaClassOptions(DispatchContext dispatchContext) {
		String saveOptString = "";

		if (dispatchContext.isJavaClassDispatchChannelEnabled()) {
			saveOptString += "sendtojavaclass=true%26";
			if ((dispatchContext.getJavaClassPath() != null) && !dispatchContext.getJavaClassPath().trim().equals("")) {
				saveOptString += "javaclasspath=" + dispatchContext.getJavaClassPath() + "%26";
			}
		}

		return saveOptString;
	}

	private String serializeSaveAsFileOptions(DispatchContext dispatchContext) {
		String saveOptString = "";

		if (dispatchContext.isFileSystemDispatchChannelEnabled()) {
			saveOptString += "saveasfile=true%26";
			if (StringUtilities.isNotEmpty(dispatchContext.getDestinationFolder())) {
				saveOptString += "destinationfolder=" + dispatchContext.getDestinationFolder() + "%26";
			}
			if (StringUtilities.isNotEmpty(dispatchContext.getDestinationFolder())) {
				saveOptString += "destinationfolder=" + dispatchContext.getDestinationFolder() + "%26";
			}
			if (dispatchContext.isDestinationFolderRelativeToResourceFolder()) {
				saveOptString += "isrelativetoresourcefolder=true%26";
			} else {
				saveOptString += "isrelativetoresourcefolder=false%26";
			}

			if (dispatchContext.isProcessMonitoringEnabled()) {
				saveOptString += "isprocessmonitoringenabled=true%26";
			} else {
				saveOptString += "isprocessmonitoringenabled=false%26";
			}
		}

		if (dispatchContext.isZipFileDocument()) {
			saveOptString += "zipFileDocument=true%26";
		}
		if (dispatchContext.getFileName() != null) {
			saveOptString += "fileName=" + dispatchContext.getFileName() + "%26";
		}

		if (dispatchContext.getZipFileName() != null) {
			saveOptString += "zipFileName=" + dispatchContext.getZipFileName() + "%26";
		}

		return saveOptString;
	}

	private String serializeSaveAsDocumentOptions(DispatchContext dispatchContext) {
		String saveOptString = "";

		if (dispatchContext.isFunctionalityTreeDispatchChannelEnabled()) {
			saveOptString += "saveasdocument=true%26";
			if ((dispatchContext.getDocumentName() != null) && !dispatchContext.getDocumentName().trim().equals("")) {
				saveOptString += "documentname=" + dispatchContext.getDocumentName() + "%26";
			}
			if ((dispatchContext.getDocumentDescription() != null) && !dispatchContext.getDocumentDescription().trim().equals("")) {
				saveOptString += "documentdescription=" + dispatchContext.getDocumentDescription() + "%26";
			}
			if (dispatchContext.isUseFixedFolder() && dispatchContext.getFoldersTo() != null && !dispatchContext.getFoldersTo().trim().equals("")) {
				saveOptString += "foldersTo=" + dispatchContext.getFoldersTo() + "%26";
			}
			if (dispatchContext.isUseFolderDataSet() && dispatchContext.getDataSetFolderLabel() != null
					&& !dispatchContext.getDataSetFolderLabel().trim().equals("")) {
				saveOptString += "datasetFolderLabel=" + dispatchContext.getDataSetFolderLabel() + "%26";
				if (dispatchContext.getDataSetFolderParameterLabel() != null && !dispatchContext.getDataSetFolderParameterLabel().trim().equals("")) {
					saveOptString += "datasetFolderParameterLabel=" + dispatchContext.getDataSetFolderParameterLabel() + "%26";
				}
			}
			if ((dispatchContext.getDocumentHistoryLength() != null) && !dispatchContext.getDocumentHistoryLength().trim().equals("")) {
				saveOptString += "documenthistorylength=" + dispatchContext.getDocumentHistoryLength() + "%26";
			}
			if ((dispatchContext.getFunctionalityIds() != null) && !dispatchContext.getFunctionalityIds().trim().equals("")) {
				saveOptString += "functionalityids=" + dispatchContext.getFunctionalityIds() + "%26";
			}
		}

		return saveOptString;
	}

	private String serializeSaveAsMailOptions(DispatchContext dispatchContext) {
		String saveOptString = "";

		if (dispatchContext.isMailDispatchChannelEnabled()) {
			saveOptString += "sendmail=true%26";
			if (dispatchContext.isUseFixedRecipients() && dispatchContext.getMailTos() != null && !dispatchContext.getMailTos().trim().equals("")) {
				saveOptString += "mailtos=" + dispatchContext.getMailTos() + "%26";
			}
			if (dispatchContext.isUseDataSet() && dispatchContext.getDataSetLabel() != null && !dispatchContext.getDataSetLabel().trim().equals("")) {
				saveOptString += "datasetLabel=" + dispatchContext.getDataSetLabel() + "%26";
				if (dispatchContext.getDataSetParameterLabel() != null && !dispatchContext.getDataSetParameterLabel().trim().equals("")) {
					saveOptString += "datasetParameterLabel=" + dispatchContext.getDataSetParameterLabel() + "%26";
				}
			}
			if (dispatchContext.isUseExpression() && dispatchContext.getExpression() != null && !dispatchContext.getExpression().trim().equals("")) {
				saveOptString += "expression=" + dispatchContext.getExpression() + "%26";
			}
			if ((dispatchContext.getMailSubj() != null) && !dispatchContext.getMailSubj().trim().equals("")) {
				saveOptString += "mailsubj=" + dispatchContext.getMailSubj() + "%26";
			}
			if ((dispatchContext.getMailTxt() != null) && !dispatchContext.getMailTxt().trim().equals("")) {
				saveOptString += "mailtxt=" + dispatchContext.getMailTxt() + "%26";
			}

			// Mail
			if (dispatchContext.isZipMailDocument()) {
				saveOptString += "zipMailDocument=true%26";
			}
			if (dispatchContext.isReportNameInSubject()) {
				saveOptString += "reportNameInSubject=true%26";
			}

			if (dispatchContext.getContainedFileName() != null) {
				saveOptString += "containedFileName=" + dispatchContext.getContainedFileName() + "%26";
			}
			if (dispatchContext.getZipMailName() != null) {
				saveOptString += "zipMailName=" + dispatchContext.getZipMailName() + "%26";
			}

			if (dispatchContext.isUniqueMail()) {
				saveOptString += "uniqueMail=true%26";
			}

		}

		return saveOptString;
	}

	private String serializeSaveAsDistributionListOptions(DispatchContext dispatchContext, String uniqueDispatchContextName, TriggerInfo triggerInfo,
			boolean runImmediately, IEngUserProfile profile) throws EMFUserError {
		String saveOptString = "";

		JobInfo jobInfo = triggerInfo.getJobInfo();

		if (dispatchContext.isDistributionListDispatchChannelEnabled()) {
			String xml = "";
			if (!runImmediately) {
				xml += "<SCHEDULE ";
				xml += " jobName=\"" + jobInfo.getJobName() + "\" ";
				xml += " triggerName=\"" + triggerInfo.getTriggerName() + "\" ";
				xml += " startDate=\"" + triggerInfo.getStartDate() + "\" ";
				xml += " startTime=\"" + triggerInfo.getStartTime() + "\" ";
				xml += " chronString=\"" + triggerInfo.getChronString() + "\" ";
				String enddate = triggerInfo.getEndDate();
				String endtime = triggerInfo.getEndTime();
				if (!enddate.trim().equals("")) {
					xml += " endDate=\"" + enddate + "\" ";
					if (!endtime.trim().equals("")) {
						xml += " endTime=\"" + endtime + "\" ";
					}
				}

				String repeatinterval = triggerInfo.getRepeatInterval();
				if (!repeatinterval.trim().equals("")) {
					xml += " repeatInterval=\"" + repeatinterval + "\" ";
				}
				xml += ">";

				String params = "<PARAMETERS>";

				List biObjects = jobInfo.getDocuments();
				Iterator iterbiobj = biObjects.iterator();
				int index = 0;
				while (iterbiobj.hasNext()) {
					index++;
					BIObject biobj = (BIObject) iterbiobj.next();
					String objpref = biobj.getId().toString() + "__" + new Integer(index).toString();
					if (uniqueDispatchContextName.equals(objpref)) {

						List pars = biobj.getBiObjectParameters();
						Iterator iterPars = pars.iterator();
						String queryString = "";
						while (iterPars.hasNext()) {
							BIObjectParameter biobjpar = (BIObjectParameter) iterPars.next();
							String concatenatedValue = "";
							List values = biobjpar.getParameterValues();
							if (values != null) {
								Iterator itervalues = values.iterator();
								while (itervalues.hasNext()) {
									String value = (String) itervalues.next();
									concatenatedValue += value + ",";
								}
								if (concatenatedValue.length() > 0) {
									concatenatedValue = concatenatedValue.substring(0, concatenatedValue.length() - 1);
									queryString += biobjpar.getParameterUrlName() + "=" + concatenatedValue + "%26";
								}
							}
						}
						if (queryString.length() > 0) {
							queryString = queryString.substring(0, queryString.length() - 3);
						}
						params += "<PARAMETER name=\"" + biobj.getLabel() + "__" + index + "\" value=\"" + queryString + "\" />";
					} else {
						continue;
					}
				}
				params += "</PARAMETERS>";

				xml += params;
				xml += "</SCHEDULE>";
			}

			saveOptString += "sendtodl=true%26";

			List l = dispatchContext.getDlIds();
			if (!l.isEmpty()) {

				String dlIds = "dlId=";
				int objId = dispatchContext.getBiobjId();
				Iterator iter = l.iterator();
				while (iter.hasNext()) {

					Integer dlId = (Integer) iter.next();
					try {
						if (!runImmediately) {
							IDistributionListDAO dao = DAOFactory.getDistributionListDAO();
							dao.setUserProfile(profile);
							DistributionList dl = dao.loadDistributionListById(dlId);
							dao.insertDLforDocument(dl, objId, xml);
						}
					} catch (Exception ex) {
						logger.error("Cannot fill response container" + ex.getLocalizedMessage());
						throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
					}

					if (iter.hasNext()) {
						dlIds += dlId.intValue() + ",";
					} else {
						dlIds += dlId.intValue();
					}

				}
				saveOptString += dlIds + "%26";

			}
		}

		return saveOptString;
	}

}
