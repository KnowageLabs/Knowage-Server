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
package it.eng.spagobi.tools.scheduler.services.rest;

import static it.eng.spagobi.tools.scheduler.utils.SchedulerUtilitiesV2.JobInfoToJson;
import static it.eng.spagobi.tools.scheduler.utils.SchedulerUtilitiesV2.JobTriggerToJson;
import static it.eng.spagobi.tools.scheduler.utils.SchedulerUtilitiesV2.getJobTriggerFromJsonRequest;
import static it.eng.spagobi.tools.scheduler.utils.SchedulerUtilitiesV2.getJobTriggerInfo;
import static it.eng.spagobi.tools.scheduler.utils.SchedulerUtilitiesV2.getSchedulingMessage;
import static it.eng.spagobi.tools.scheduler.utils.SchedulerUtilitiesV2.toJsonTreeLowFunctionality;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
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
import it.eng.spagobi.tools.scheduler.Formula;
import it.eng.spagobi.tools.scheduler.FormulaParameterValuesRetriever;
import it.eng.spagobi.tools.scheduler.RuntimeLoadingParameterValuesRetriever;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.bo.TriggerPaused;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.jobs.ExecuteBIDocumentJob;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.tools.scheduler.to.JobInfo;
import it.eng.spagobi.tools.scheduler.to.JobTrigger;
import it.eng.spagobi.tools.scheduler.to.TriggerInfo;
import it.eng.spagobi.tools.scheduler.utils.SchedulerUtilities;
import it.eng.spagobi.tools.scheduler.wsEvents.SbiWsEvent;
import it.eng.spagobi.tools.scheduler.wsEvents.dao.SbiWsEventsDao;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
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
	private static final String ALERT_JOB_GROUP = "ALERT_JOB_GROUP";
	private static final String KPI_SCHEDULER_GROUP = "KPI_SCHEDULER_GROUP";
	private static final String JOB_GROUP = "BIObjectExecutions";

	// private static final String SINGLE_TYPE = "Single";
	// private static final String CUSTOM_TYPE_PREFIX = "Scheduler";

	static private Logger logger = Logger.getLogger(SchedulerService.class);
	static private String canNotFillResponseError = "error.mesage.description.generic.can.not.responce";

	@GET
	@Path("/getJob")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getJob(@Context HttpServletRequest req) throws Exception {
		ILowFunctionalityDAO lowfuncdao = DAOFactory.getLowFunctionalityDAO();
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		lowfuncdao.setUserProfile(profile);

		String jobGroupName = req.getParameter("jobGroup");
		String jobName = req.getParameter("jobName");

		JSONObject resp = new JSONObject();

		@SuppressWarnings("unchecked")
		List<LowFunctionality> functionalities = lowfuncdao.loadAllLowFunctionalities(false);
		resp.put("functionality", toJsonTreeLowFunctionality(functionalities));

		ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
		String jobDetail = schedulerService.getJobDefinition(jobName, jobGroupName);
		SourceBean jobDetailSB = SchedulerUtilities.getSBFromWebServiceResponse(jobDetail);
		if (jobDetailSB == null) {
			throw new Exception("Cannot recover job " + jobName);
		}
		JobInfo jobInfo = SchedulerUtilities.getJobInfoFromJobSourceBean(jobDetailSB);
		resp.put("job", JobInfoToJson(jobInfo));

		return resp.toString();
	}

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

		List<Job> nonKpiSchedulerJobs = new ArrayList<Job>();

		for (Job job : jobs) {
			if (!job.getGroupName().equals(ALERT_JOB_GROUP) && !job.getGroupName().equals(KPI_SCHEDULER_GROUP)) {

				nonKpiSchedulerJobs.add(job);
			}
		}

		JSONSerializer jsonSerializer = (JSONSerializer) SerializerFactory.getSerializer("application/json");
		try {
			// jobsJSONArray = (JSONArray) jsonSerializer.serialize(jobs, null);
			jobsJSONArray = (JSONArray) jsonSerializer.serialize(nonKpiSchedulerJobs, null);

			// add the triggers part for each job

			for (int i = 0; i < jobsJSONArray.length(); i++) {

				JSONObject jobJSONObject = jobsJSONArray.getJSONObject(i);
				String jobName = jobJSONObject.getString(JobJSONSerializer.JOB_NAME);
				String jobGroup = jobJSONObject.getString(JobJSONSerializer.JOB_GROUP);

				// for (Job job : jobs) {
				for (Job job : nonKpiSchedulerJobs) {
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
		HashMap<String, String> logParam = new HashMap<String, String>();
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
			return new JSONObject().put("resp", "ok").toString();
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
		HashMap<String, String> logParam = new HashMap<String, String>();

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
			return new JSONObject().put("resp", "ok").toString();
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
		HashMap<String, String> logParam = new HashMap<String, String>();
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

			// TriggerInfo triggerInfo = this.getTriggerInfo(jobName, jobGroupName, triggerName, triggerGroup);
			// StringBuffer message = createMessageSaveSchedulation(triggerInfo, true, profile);
			JobTrigger jobtri = getJobTriggerInfo(jobName, jobGroupName, triggerName, triggerGroup);
			StringBuffer message = getSchedulingMessage(jobtri, true, profile);

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
			return new JSONObject().put("resp", "ok").toString();
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
		HashMap<String, String> logParam = new HashMap<String, String>();
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
			return new JSONObject().put("resp", "ok").toString();
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
		HashMap<String, String> logParam = new HashMap<String, String>();
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
			return new JSONObject().put("resp", "ok").toString();
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

	// TODO controllare se viene usata ed in caso eliminarlo

	@POST
	@Path("/getTriggerInfo")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getTriggerInfo(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap<String, String>();
		try {
			String jobGroupName = req.getParameter("jobGroup");
			String jobName = req.getParameter("jobName");
			String triggerGroup = req.getParameter("triggerGroup");
			String triggerName = req.getParameter("triggerName");
			logParam.put("JOB NAME", jobName);
			logParam.put("JOB GROUP", jobGroupName);
			logParam.put("TRIGGER NAME", triggerName);
			logParam.put("TRIGGER GROUP", triggerGroup);

			JobTrigger jobtri = getJobTriggerInfo(jobName, jobGroupName, triggerName, triggerGroup);

			// JSONObject j = XML.toJSONObject(getSchedulingMessage(jobtri, false, profile).toString());

			JSONObject jo = new JSONObject();
			if (jobtri == null) {
				jo.put("Status", "NON OK");
				jo.put("Errors", "NO DATA");
			} else {
				jo = JobTriggerToJson(jobtri);
			}
			return jo.toString();

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

	@POST
	@Path("/saveTrigger")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String saveTrigger(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap<String, String>();

		JobTrigger jobTrigger = null;

		try {
			// ISchedulerDAO dao = DAOFactory.getSchedulerDAO();

			JSONObject triggerJson = RestUtilities.readBodyAsJSONObject(req);

			String jobName = (String) triggerJson.opt(JobTrigger.JOB_NAME);
			String jobGroupName = (String) triggerJson.opt(JobTrigger.JOB_GROUP);
			String triggerName = (String) triggerJson.opt(JobTrigger.TRIGGER_NAME);
			String triggerGroup = (String) triggerJson.opt(JobTrigger.TRIGGER_GROUP);

			logParam.put("JOB NAME", jobName);
			logParam.put("JOB GROUP", jobGroupName);
			logParam.put("TRIGGER NAME", triggerName);
			logParam.put("TRIGGER GROUP", triggerGroup);

			JSONArray errorH = new JSONArray();
			jobTrigger = getJobTriggerFromJsonRequest(triggerJson, errorH);

			if (errorH.length() > 0) {
				JSONObject jo = new JSONObject();
				jo.put("Status", "NON OK");
				jo.put("Errors", errorH);
				return jo.toString();
			}

			// JSONObject valid = isValidJobTrigger(jobTrigger);
			// if (!valid.getString("Status").equals("OK")) {
			// return valid.toString();
			// }

			StringBuffer message = getSchedulingMessage(jobTrigger, false, profile);
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
			JSONObject jo;
			jo = new JSONObject(schedulerService.scheduleJob(message.toString()));
			if (jo.has("Status") && jo.getString("Status").equals("OK")) {
				jo = JobTriggerToJson(jobTrigger);
			}
			return jo.toString();
		} catch (Exception e) {
			updateAudit(req, profile, "SCHED_TRIGGER.GET_TRIGGER_SAVE_OPTION", logParam, "KO");
			logger.error("Error while saving trigger : " + jobTrigger.toString(), e);
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException("Error while saving trigger : " + jobTrigger.toString(), null));
			} catch (Exception ex) {
				logger.debug("Error in ExceptionUtilities.serializeException.");
				throw new SpagoBIRuntimeException("ExceptionUtilities.serializeException", ex);
			}
		}
	}

	@GET
	@Path("/triggerEvent")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String triggerEvent(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap<String, String>();

		JSONObject resp = new JSONObject();

		try {
			String eventName = req.getParameter("eventName");
			logParam.put("EVENT NAME", eventName);

			SbiWsEventsDao wsEventsDao = DAOFactory.getWsEventsDao();

			Date incomingDate = new Date();
			// String ipComeFrom = req.getHeader("X-Forwarded-For");
			String ipComeFrom = req.getRemoteAddr();

			SbiWsEvent sbiWsEvent = new SbiWsEvent(eventName, ipComeFrom, incomingDate);
			Integer newId = wsEventsDao.triggerEvent(sbiWsEvent);

			resp.put("sbiWsEventId", newId);

			return resp.toString();

		} catch (Exception e) {
			updateAudit(req, profile, "SCHED_TRIGGER.PAUSE", logParam, "KO");
			logger.error("Error while pausing trigger ", e);
			logger.debug(canNotFillResponseError);
			try {
				return ExceptionUtilities.serializeException(canNotFillResponseError, null);
			} catch (Exception ex) {
				logger.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", ex);
			}
		}
	}

	// TODO To remove asap. This mustn't be a service.
	// functionality for ws event updates
	// @GET
	// @Path("/markTakenCharge")
	// @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	// public String markTakenCharge(@Context HttpServletRequest req) {
	// IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	// HashMap<String, String> logParam = new HashMap<String, String>();
	//
	// JSONObject resp = new JSONObject();
	//
	// try {
	// String eventName = req.getParameter("eventName");
	// logParam.put("EVENT NAME", eventName);
	//
	// SbiWsEventsDao wsEventsDao = DAOFactory.getWsEventsDao();
	//
	// SbiWsEvent sbiWsEvent = wsEventsDao.loadSbiWsEvent(eventName);
	//
	// sbiWsEvent.setTakeChargeDate(new Date());
	//
	// wsEventsDao.updateEvent(sbiWsEvent);
	//
	// resp.put("sbiWsEventId", sbiWsEvent.getId());
	// resp.put("event date taken in charge", sbiWsEvent.getTakeChargeDate());
	//
	// return resp.toString();
	//
	// } catch (Exception e) {
	// updateAudit(req, profile, "SCHED_TRIGGER.PAUSE", logParam, "KO");
	// logger.error("Error while pausing trigger ", e);
	// logger.debug(canNotFillResponseError);
	// try {
	// return ExceptionUtilities.serializeException(canNotFillResponseError, null);
	// } catch (Exception ex) {
	// logger.debug("Cannot fill response container.");
	// throw new SpagoBIRuntimeException("Cannot fill response container", ex);
	// }
	// }
	// }

	public static TriggerInfo getTriggerInfo(String jobName, String jobGroupName, String triggerName, String triggerGroup) {
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

	// @POST
	// @Path("/getTriggerSaveOptions")
	// @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	// public String getTriggerSaveOptions(@Context HttpServletRequest req) {
	// IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	// HashMap<String, String> logParam = new HashMap();
	// try {
	// String jobGroupName = req.getParameter("jobGroup");
	// String jobName = req.getParameter("jobName");
	// String triggerGroup = req.getParameter("triggerGroup");
	// String triggerName = req.getParameter("triggerName");
	// logParam.put("JOB NAME", jobName);
	// logParam.put("JOB GROUP", jobGroupName);
	// logParam.put("TRIGGER NAME", triggerName);
	// logParam.put("TRIGGER GROUP", triggerGroup);
	//
	// ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();
	//
	// TriggerInfo triggerInfo = getTriggerInfo(jobName, jobGroupName, triggerName, triggerGroup);
	//
	// JSONArray serializedSaveOptions = serializeSaveOptions(triggerInfo);
	// JSONObject triggerDocumentsSaveOptions = new JSONObject();
	// triggerDocumentsSaveOptions.put("documents", serializedSaveOptions);
	// updateAudit(req, profile, "SCHED_TRIGGER.GET_TRIGGER_SAVE_OPTION", logParam, "OK");
	//
	// return triggerDocumentsSaveOptions.toString();
	//
	// } catch (Exception e) {
	// updateAudit(req, profile, "SCHED_TRIGGER.GET_TRIGGER_SAVE_OPTION", logParam, "KO");
	// logger.error("Error while create immediate trigger ", e);
	// logger.debug(canNotFillResponseError);
	// try {
	// return (ExceptionUtilities.serializeException(canNotFillResponseError, null));
	// } catch (Exception ex) {
	// logger.debug("Cannot fill response container.");
	// throw new SpagoBIRuntimeException("Cannot fill response container", ex);
	// }
	// }
	// }

	@POST
	@Path("/saveJob")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String saveJob(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap<String, String>();

		try {
			IBIObjectDAO documentDAO = DAOFactory.getBIObjectDAO();
			IBIObjectParameterDAO documentParameterDAO = DAOFactory.getBIObjectParameterDAO();

			JSONObject jsonJob = RestUtilities.readBodyAsJSONObject(req);
			JSONArray jsonDocs = jsonJob.getJSONArray("documents");

			List<BIObject> biObjects = new ArrayList<BIObject>();
			for (int docIndex = 0; docIndex < jsonDocs.length(); docIndex++) {
				JSONObject jsonDoc = jsonDocs.getJSONObject(docIndex);
				BIObject biObject = documentDAO.loadBIObjectByLabel(jsonDoc.getString("name"));

				JSONArray jsonParams = jsonDoc.getJSONArray("parameters");
				List<BIObjectParameter> biObjParams = new ArrayList<BIObjectParameter>();
				List<BIObjectParameter> params = documentParameterDAO.loadBIObjectParametersById(biObject.getId());

				for (int paramIndex = 0; paramIndex < jsonParams.length(); paramIndex++) {
					JSONObject jsonParam = jsonParams.getJSONObject(paramIndex);
					String name = jsonParam.getString("name");
					String type = jsonParam.getString("type");
					String value = jsonParam.getString("value");
					boolean isIterative = jsonParam.getBoolean("iterative");

					BIObjectParameter biObjParam = null;
					for (BIObjectParameter param : params) {
						if (param.getParameterUrlName().equalsIgnoreCase(name)) {
							biObjParam = param;
							break;
						}
					}

					if (type.equalsIgnoreCase("formula")) {
						Formula formula = Formula.getFormula(value);
						FormulaParameterValuesRetriever parameterValuesRetriever = new FormulaParameterValuesRetriever();
						parameterValuesRetriever.setFormula(formula);
						biObjParam.setParameterValuesRetriever(parameterValuesRetriever);
					} else if (type.equalsIgnoreCase("loadAtRuntime")) {
						RuntimeLoadingParameterValuesRetriever parameterValuesRetriever = new RuntimeLoadingParameterValuesRetriever();
						parameterValuesRetriever.setUserIndentifierToBeUsed(profile.getUserUniqueIdentifier().toString());
						parameterValuesRetriever.setRoleToBeUsed(value.split("\\|")[1]);
						biObjParam.setParameterValuesRetriever(parameterValuesRetriever);
					} else if (type.equalsIgnoreCase("fixed")) {
						if (value.trim().equals("")) {
							biObjParam.setParameterValues(new ArrayList<String>());
						} else {
							biObjParam.setParameterValues(Arrays.asList(value.split(";")));
						}
					}
					biObjParam.setIterative(isIterative);
					biObjParams.add(biObjParam);
				}
				biObject.setBiObjectParameters(biObjParams);
				biObjects.add(biObject);
			}

			JobInfo jobInfo = new JobInfo();
			jobInfo.setJobGroupName(JOB_GROUP);
			jobInfo.setJobName(jsonJob.getString("jobName"));
			jobInfo.setJobDescription(jsonJob.getString("jobDescription"));
			jobInfo.setSchedulerAdminstratorIdentifier(profile.getUserUniqueIdentifier().toString());
			jobInfo.setDocuments(biObjects);

			Map<String, Float> documentToCombinationsMap = new HashMap<String, Float>();
			float totalCombinations = calculateTotalIterativeCombinations(jobInfo.getDocuments(), documentToCombinationsMap);
			if (totalCombinations > 10) {
				return new JSONObject().put("Status", "NON OK").put("Errors", "EXCEEDING CONFIGURATIONS").toString();
			}
			saveJob(jobInfo, req, profile);
			return new JSONObject().put("resp", "ok").toString();
		} catch (Exception e) {
			updateAudit(req, profile, "SCHED_JOB.GET_JOB_SAVE_OPTION", logParam, "KO");
			logger.error("Error while saving job", e);
			logger.debug(canNotFillResponseError);
			try {
				return (ExceptionUtilities.serializeException("Error while saving job", null));
			} catch (Exception ex) {
				logger.debug("Error in ExceptionUtilities.serializeException.");
				throw new SpagoBIRuntimeException("ExceptionUtilities.serializeException", ex);
			}
		}
	}

	private void saveJob(JobInfo jobInfo, HttpServletRequest req, IEngUserProfile profile) throws EMFUserError {
		HashMap<String, String> logParam = new HashMap<String, String>();
		try {
			logParam.put("JOB NAME", jobInfo.getJobName());
			ISchedulerServiceSupplier schedulerService = SchedulerServiceSupplierFactory.getSupplier();

			// create message to define the new job
			StringBuilder message = new StringBuilder();
			message.append("<SERVICE_REQUEST ");
			message.append(" jobName=\"" + jobInfo.getJobName() + "\" ");
			message.append(" jobDescription=\"" + jobInfo.getJobDescription() + "\" ");
			message.append(" jobGroupName=\"" + JOB_GROUP + "\" ");
			message.append(" jobRequestRecovery=\"false\" ");
			message.append(" jobClass=\"" + ExecuteBIDocumentJob.class.getName() + "\" ");
			message.append(">");
			message.append("   <PARAMETERS>");

			StringBuilder docLabels = new StringBuilder();
			int index = 0;
			for (BIObject biobj : jobInfo.getDocuments()) {
				StringBuilder fixedParameters = new StringBuilder();
				StringBuilder iterativeParameters = new StringBuilder();
				StringBuilder loadAtRuntimeParameters = new StringBuilder();
				StringBuilder useFormulaParameters = new StringBuilder();
				index++;

				for (BIObjectParameter biobjpar : biobj.getBiObjectParameters()) {
					if (biobjpar.isIterative()) {
						iterativeParameters.append(biobjpar.getParameterUrlName() + ";");
					}

					Object strategyObj = biobjpar.getParameterValuesRetriever();
					if (strategyObj != null && strategyObj instanceof RuntimeLoadingParameterValuesRetriever) {
						RuntimeLoadingParameterValuesRetriever strategy = (RuntimeLoadingParameterValuesRetriever) strategyObj;
						String user = strategy.getUserIndentifierToBeUsed();
						String role = strategy.getRoleToBeUsed();
						loadAtRuntimeParameters.append(biobjpar.getParameterUrlName() + "(" + user + "|" + role + ");");
					} else if (strategyObj != null && strategyObj instanceof FormulaParameterValuesRetriever) {
						FormulaParameterValuesRetriever strategy = (FormulaParameterValuesRetriever) strategyObj;
						String formulaName = strategy.getFormula().getName();
						useFormulaParameters.append(biobjpar.getParameterUrlName() + "(" + formulaName + ");");
					} else {
						StringBuilder concatenatedValue = new StringBuilder();
						List<String> values = biobjpar.getParameterValues();
						if (values != null && !values.isEmpty()) {
							for (String value : values) {
								concatenatedValue.append(value + ";");
							}
							if (concatenatedValue.length() > 0) {
								concatenatedValue = concatenatedValue.deleteCharAt(concatenatedValue.length() - 1);
							}
						}
						fixedParameters.append(biobjpar.getParameterUrlName() + "=" + concatenatedValue + "%26");
					}
				}

				if (fixedParameters.length() > 0) {
					fixedParameters = fixedParameters.delete(fixedParameters.length() - 1, fixedParameters.length() - 1);
				}
				message.append("<PARAMETER name=\"" + biobj.getLabel() + "__" + index + "\" value=\"" + fixedParameters + "\" />");

				if (iterativeParameters.length() > 0) {
					iterativeParameters.deleteCharAt(iterativeParameters.length() - 1);
					message.append("<PARAMETER name=\"" + biobj.getLabel() + "__" + index + "_iterative\" value=\"" + iterativeParameters + "\" />");
				}

				if (loadAtRuntimeParameters.length() > 0) {
					loadAtRuntimeParameters.deleteCharAt(loadAtRuntimeParameters.length() - 1);
					message.append("<PARAMETER name=\"" + biobj.getLabel() + "__" + index + "_loadAtRuntime\" value=\"" + loadAtRuntimeParameters + "\" />");
				}

				if (useFormulaParameters.length() > 0) {
					useFormulaParameters.deleteCharAt(useFormulaParameters.length() - 1);
					message.append("<PARAMETER name=\"" + biobj.getLabel() + "__" + index + "_useFormula\" value=\"" + useFormulaParameters + "\" />");
				}

				docLabels.append(biobj.getLabel() + "__" + index + ",");
			}

			if (docLabels.length() > 0) {
				docLabels.deleteCharAt(docLabels.length() - 1);
			}

			logParam.put("DOC LABELS", docLabels.toString());
			message.append("   	   <PARAMETER name=\"documentLabels\" value=\"" + docLabels + "\" />");
			message.append("   </PARAMETERS>");
			message.append("</SERVICE_REQUEST>");

			// call the web service
			String servoutStr = schedulerService.defineJob(message.toString());
			SourceBean schedModRespSB = SchedulerUtilities.getSBFromWebServiceResponse(servoutStr);
			if (schedModRespSB == null) {
				updateAudit(req, profile, "SCHEDULER.SAVE", logParam, "KO");
				throw new SpagoBIRuntimeException("Imcomplete response returned by the web service during job creation");
			}
			if (!SchedulerUtilities.checkResultOfWSCall(schedModRespSB)) {
				updateAudit(req, profile, "SCHEDULER.SAVE", logParam, "KO");
				throw new SpagoBIRuntimeException("Job not created by the web service");
			}
			updateAudit(req, profile, "SCHEDULER.SAVE", logParam, "OK");
		} catch (Exception e) {
			updateAudit(req, profile, "SCHEDULER.SAVE", logParam, "KO");
			logger.error("Error while saving job", e);
			throw new EMFUserError(EMFErrorSeverity.ERROR, "errors.1004", "component_scheduler_messages");
		}
	}

	private float calculateTotalIterativeCombinations(Collection<BIObject> documents, Map<String, Float> documentToCombinationsMap) {
		float totalCombinations = 0;
		for (BIObject document : documents) {
			String biObjectName = document.getName();
			float combinations = calculateIterativeCombinations(document);
			if (documentToCombinationsMap.containsKey(biObjectName)) {
				float previousCombinations = documentToCombinationsMap.get(biObjectName).floatValue();
				documentToCombinationsMap.put(biObjectName, previousCombinations + combinations);
			} else {
				documentToCombinationsMap.put(biObjectName, combinations);
			}
			totalCombinations += combinations;
		}
		return totalCombinations;
	}

	private float calculateIterativeCombinations(BIObject biobj) {
		float combinations = 1;
		for (BIObjectParameter parameter : biobj.getBiObjectParameters()) {
			if (parameter.isIterative()) {
				List<String> values = parameter.getParameterValues();
				if (values != null && values.size() > 1) {
					combinations *= values.size();
				}
			}
		}
		return combinations;
	}
}
