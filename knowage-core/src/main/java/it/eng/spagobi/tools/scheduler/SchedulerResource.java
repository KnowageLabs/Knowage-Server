package it.eng.spagobi.tools.scheduler;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.api.AbstractSpagoBIResource;
import it.eng.spagobi.commons.constants.CommunityFunctionalityConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;
import it.eng.spagobi.services.exceptions.ExceptionUtilities;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.services.rest.annotations.UserConstraint;
import it.eng.spagobi.tools.scheduler.bo.TriggerPaused;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 * @author Alessandro Portosa (alessandro.portosa@eng.it)
 * @author Francesco Lucchi (francesco.lucchi@eng.it)
 */

@Path("/scheduler")
@ManageAuthorization
public class SchedulerResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = Logger.getLogger(SchedulerResource.class);
	private static final String ERROR_CAN_NOT_FILL_RESPONSE = "error.mesage.description.generic.can.not.responce";

	@POST
	@Path("/pauseTrigger")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.SCHEDULER_MANAGEMENT })
	public String pauseTrigger(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap<>();
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
			LOGGER.error("Error while pausing trigger ", e);
			LOGGER.debug(ERROR_CAN_NOT_FILL_RESPONSE);
			try {
				return (ExceptionUtilities.serializeException(ERROR_CAN_NOT_FILL_RESPONSE, null));
			} catch (Exception ex) {
				LOGGER.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", ex);
			}
		}
	}

	@POST
	@Path("/resumeTrigger")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@UserConstraint(functionalities = { CommunityFunctionalityConstants.SCHEDULER_MANAGEMENT, CommunityFunctionalityConstants.KPI_SCHEDULATION })
	public String resumeTrigger(@Context HttpServletRequest req) {
		IEngUserProfile profile = (IEngUserProfile) req.getSession().getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		HashMap<String, String> logParam = new HashMap<>();
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
			LOGGER.error("Error while resuming trigger ", e);
			LOGGER.debug(ERROR_CAN_NOT_FILL_RESPONSE);
			try {
				return (ExceptionUtilities.serializeException(ERROR_CAN_NOT_FILL_RESPONSE, null));
			} catch (Exception ex) {
				LOGGER.debug("Cannot fill response container.");
				throw new SpagoBIRuntimeException("Cannot fill response container", ex);
			}
		}
	}

	protected static void updateAudit(HttpServletRequest request, IEngUserProfile profile, String actionCode, HashMap<String, String> parameters, String esito) {
		try {
			AuditLogUtilities.updateAudit(request, profile, actionCode, parameters, esito);
		} catch (Exception e) {
			LOGGER.debug("Error writing audit", e);
		}
	}
}